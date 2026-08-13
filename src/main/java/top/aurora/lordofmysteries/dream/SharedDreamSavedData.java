package top.aurora.lordofmysteries.dream;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

public final class SharedDreamSavedData extends SavedData {

    public static final int SCHEMA_VERSION = 1;
    public static final String DATA_NAME = "lord_of_mysteries_shared_dreams";
    private static final int MAX_SESSIONS = 1024;

    private final Map<UUID, DreamSession> sessions = new HashMap<>();
    private final Map<UUID, UUID> playerSessions = new HashMap<>();
    private final Map<UUID, Recovery> recoveries = new HashMap<>();
    private final ListTag orphanedEntries = new ListTag();
    private CompoundTag futureSnapshot;
    private int nextLane;

    public static SharedDreamSavedData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) {
            throw new IllegalStateException("Overworld is unavailable");
        }
        return overworld.getDataStorage().computeIfAbsent(
                SharedDreamSavedData::load,
                SharedDreamSavedData::new,
                DATA_NAME);
    }

    public static SharedDreamSavedData load(CompoundTag tag) {
        SharedDreamSavedData data = new SharedDreamSavedData();
        if (tag.getInt("schema") > SCHEMA_VERSION) {
            data.futureSnapshot = tag.copy();
            return data;
        }
        data.nextLane = Math.floorMod(tag.getInt("next_lane"), MAX_SESSIONS);
        ListTag savedSessions = tag.getList("sessions", Tag.TAG_COMPOUND);
        for (Tag raw : savedSessions) {
            CompoundTag entry = (CompoundTag) raw;
            try {
                DreamSession session = readSession(entry);
                if (data.sessions.containsKey(session.id())
                        || session.participants().keySet().stream()
                        .anyMatch(data.playerSessions::containsKey)) {
                    throw new IllegalArgumentException("duplicate dream session");
                }
                data.sessions.put(session.id(), session);
                session.participants().keySet().forEach(player ->
                        data.playerSessions.put(player, session.id()));
            } catch (RuntimeException exception) {
                data.orphanedEntries.add(entry.copy());
            }
        }
        ListTag savedRecoveries = tag.getList(
                "recoveries", Tag.TAG_COMPOUND);
        for (Tag raw : savedRecoveries) {
            CompoundTag entry = (CompoundTag) raw;
            try {
                Recovery recovery = readRecovery(entry);
                if (data.recoveries.putIfAbsent(
                        recovery.player(), recovery) != null) {
                    throw new IllegalArgumentException(
                            "duplicate dream recovery");
                }
            } catch (RuntimeException exception) {
                data.orphanedEntries.add(entry.copy());
            }
        }
        ListTag savedOrphans = tag.getList(
                "orphaned_entries", Tag.TAG_COMPOUND);
        savedOrphans.forEach(raw -> data.orphanedEntries.add(raw.copy()));
        return data;
    }

    public DreamSession createLobby(
            UUID host, UUID invitee, ResourceLocation organization,
            String teamName, long dreamSeed, long now) {
        if (futureSnapshot != null || host == null || invitee == null
                || host.equals(invitee) || organization == null
                || playerSessions.containsKey(host)
                || playerSessions.containsKey(invitee)) {
            return null;
        }
        int lane = allocateLane();
        if (lane < 0) return null;
        UUID id = UUID.nameUUIDFromBytes((host + ":" + invitee + ":"
                + organization + ":" + Math.max(0L, now) + ":" + lane)
                .getBytes(StandardCharsets.UTF_8));
        Map<UUID, Participant> participants = new LinkedHashMap<>();
        participants.put(host, Participant.acceptedParticipant());
        participants.put(invitee, Participant.pendingParticipant());
        DreamSession session = new DreamSession(
                id, host, organization, sanitizeTeam(teamName),
                SharedDreamPolicy.scenario(dreamSeed, host), dreamSeed, lane,
                DreamState.LOBBY, Math.max(0L, now),
                SharedDreamPolicy.deadline(
                        now, SharedDreamPolicy.INVITE_TTL_TICKS),
                0L, 0L, 0,
                SharedDreamPolicy.STARTING_COHERENCE,
                0, 0, null, participants, Map.of());
        sessions.put(id, session);
        participants.keySet().forEach(player -> playerSessions.put(player, id));
        setDirty();
        return session;
    }

    public DreamSession invite(UUID host, UUID invitee, long now) {
        DreamSession session = forPlayer(host);
        if (futureSnapshot != null || session == null
                || session.state() != DreamState.LOBBY
                || !session.host().equals(host) || invitee == null
                || session.participants().size()
                        >= SharedDreamPolicy.MAX_PARTICIPANTS
                || playerSessions.containsKey(invitee)
                || SharedDreamPolicy.invitationExpired(
                        now, session.expiresAt())) {
            return null;
        }
        Map<UUID, Participant> participants = new LinkedHashMap<>(
                session.participants());
        participants.put(invitee, Participant.pendingParticipant());
        DreamSession changed = session.withParticipants(
                participants,
                SharedDreamPolicy.deadline(
                        now, SharedDreamPolicy.INVITE_TTL_TICKS));
        sessions.put(changed.id(), changed);
        playerSessions.put(invitee, changed.id());
        setDirty();
        return changed;
    }

    public DreamSession accept(UUID player, long now) {
        DreamSession session = forPlayer(player);
        Participant participant = session == null
                ? null : session.participants().get(player);
        if (futureSnapshot != null || session == null || participant == null
                || session.state() != DreamState.LOBBY
                || participant.accepted()
                || SharedDreamPolicy.invitationExpired(
                        now, session.expiresAt())) {
            return null;
        }
        Map<UUID, Participant> participants = new LinkedHashMap<>(
                session.participants());
        participants.put(player, Participant.acceptedParticipant());
        DreamSession changed = session.withParticipants(
                participants, session.expiresAt());
        sessions.put(changed.id(), changed);
        setDirty();
        return changed;
    }

    public DreamSession activate(
            UUID host, Map<UUID, Origin> origins, long now) {
        DreamSession session = forPlayer(host);
        if (futureSnapshot != null || session == null
                || session.state() != DreamState.LOBBY
                || !session.host().equals(host)
                || SharedDreamPolicy.invitationExpired(
                        now, session.expiresAt())
                || session.participants().size() < 2
                || origins == null
                || !origins.keySet().equals(session.participants().keySet())
                || session.participants().values().stream()
                        .anyMatch(participant -> !participant.accepted())) {
            return null;
        }
        Map<UUID, Participant> participants = new LinkedHashMap<>();
        session.participants().forEach((player, participant) ->
                participants.put(player, participant.withOrigin(
                        origins.get(player))));
        DreamSession active = new DreamSession(
                session.id(), session.host(), session.organization(),
                session.teamName(), session.scenario(), session.dreamSeed(),
                session.lane(), DreamState.ACTIVE, session.createdAt(),
                session.expiresAt(), Math.max(0L, now),
                SharedDreamPolicy.deadline(
                        now, SharedDreamPolicy.SESSION_TTL_TICKS),
                0, SharedDreamPolicy.STARTING_COHERENCE, 0, 0,
                SharedDreamPolicy.symbol(
                        session.dreamSeed(), session.scenario(), 0),
                participants, Map.of());
        sessions.put(active.id(), active);
        setDirty();
        return active;
    }

    public VoteResult vote(UUID player, DreamAction action, long now) {
        DreamSession session = forPlayer(player);
        if (futureSnapshot != null || session == null || action == null
                || session.state() != DreamState.ACTIVE
                || session.pendingSymbol() == null
                || session.votes().containsKey(player)
                || !session.participants().containsKey(player)
                || SharedDreamPolicy.invitationExpired(
                        now, session.sessionExpiresAt())) {
            return null;
        }
        Map<UUID, DreamAction> votes = new HashMap<>(session.votes());
        votes.put(player, action);
        if (votes.size() < session.participants().size()) {
            DreamSession waiting = session.withVotes(votes);
            sessions.put(waiting.id(), waiting);
            setDirty();
            return new VoteResult(waiting, false, null, null);
        }
        DreamAction selected = selectVote(votes, session.host());
        SharedDreamPolicy.ActionOutcome outcome = SharedDreamPolicy.resolve(
                session.pendingSymbol(), selected,
                session.participants().size());
        int step = session.step() + 1;
        int coherence = SharedDreamPolicy.clampCoherence(
                session.coherence() + outcome.coherenceDelta());
        int trauma = SharedDreamPolicy.clampTrauma(
                session.trauma() + outcome.traumaDelta());
        boolean complete = step >= SharedDreamPolicy.SYMBOL_STEPS;
        boolean failed = SharedDreamPolicy.failed(coherence, trauma);
        DreamSymbol next = complete || failed ? null
                : SharedDreamPolicy.symbol(
                        session.dreamSeed(), session.scenario(), step);
        DreamState state = complete && !failed
                ? DreamState.COMPLETE : failed ? DreamState.FAILED
                : DreamState.ACTIVE;
        DreamSession resolved = new DreamSession(
                session.id(), session.host(), session.organization(),
                session.teamName(), session.scenario(), session.dreamSeed(),
                session.lane(), state, session.createdAt(),
                session.expiresAt(), session.startedAt(),
                session.sessionExpiresAt(), step, coherence, trauma,
                session.clueScore() + outcome.clueScore(), next,
                session.participants(), Map.of());
        sessions.put(resolved.id(), resolved);
        setDirty();
        return new VoteResult(resolved, true, selected, outcome);
    }

    public DreamSession forPlayer(UUID player) {
        UUID sessionId = player == null ? null : playerSessions.get(player);
        return sessionId == null ? null : sessions.get(sessionId);
    }

    public DreamSession get(UUID sessionId) {
        return sessionId == null ? null : sessions.get(sessionId);
    }

    public DreamSession close(UUID sessionId) {
        if (futureSnapshot != null) return null;
        DreamSession removed = sessions.remove(sessionId);
        if (removed == null) return null;
        removed.participants().keySet().forEach(player ->
                playerSessions.remove(player, sessionId));
        setDirty();
        return removed;
    }

    public boolean queueRecovery(
            UUID player, UUID sessionId, Origin origin,
            DreamCloseReason reason, int clueScore,
            ResourceLocation organization, boolean completed, long now) {
        if (futureSnapshot != null || player == null || sessionId == null
                || origin == null || reason == null || organization == null) {
            return false;
        }
        if (recoveries.containsKey(player)) return false;
        recoveries.put(player, new Recovery(
                player, sessionId, origin, reason,
                Math.max(0, clueScore), organization,
                completed, Math.max(0L, now)));
        setDirty();
        return true;
    }

    public Recovery recovery(UUID player) {
        return player == null ? null : recoveries.get(player);
    }

    public Recovery clearRecovery(UUID player) {
        if (futureSnapshot != null) return null;
        Recovery removed = recoveries.remove(player);
        if (removed != null) setDirty();
        return removed;
    }

    public List<DreamSession> sessions() {
        return sessions.values().stream()
                .sorted(Comparator.comparing(value -> value.id().toString()))
                .toList();
    }

    public int size() {
        return sessions.size();
    }

    public int orphanedCount() {
        return orphanedEntries.size();
    }

    public boolean isReadOnlyFutureSchema() {
        return futureSnapshot != null;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        if (futureSnapshot != null) return futureSnapshot.copy();
        tag.putInt("schema", SCHEMA_VERSION);
        tag.putInt("next_lane", nextLane);
        ListTag savedSessions = new ListTag();
        sessions().stream().map(SharedDreamSavedData::writeSession)
                .forEach(savedSessions::add);
        tag.put("sessions", savedSessions);
        ListTag savedRecoveries = new ListTag();
        recoveries.values().stream()
                .sorted(Comparator.comparing(value ->
                        value.player().toString()))
                .map(SharedDreamSavedData::writeRecovery)
                .forEach(savedRecoveries::add);
        tag.put("recoveries", savedRecoveries);
        tag.put("orphaned_entries", orphanedEntries.copy());
        return tag;
    }

    private int allocateLane() {
        for (int offset = 0; offset < MAX_SESSIONS; offset++) {
            int candidate = Math.floorMod(nextLane + offset, MAX_SESSIONS);
            boolean occupied = sessions.values().stream()
                    .anyMatch(value -> value.lane() == candidate);
            if (!occupied) {
                nextLane = Math.floorMod(candidate + 1, MAX_SESSIONS);
                return candidate;
            }
        }
        return -1;
    }

    private static DreamAction selectVote(
            Map<UUID, DreamAction> votes, UUID host) {
        Map<DreamAction, Integer> counts = new HashMap<>();
        votes.values().forEach(action -> counts.merge(action, 1, Integer::sum));
        int highest = counts.values().stream()
                .mapToInt(Integer::intValue).max().orElse(0);
        List<DreamAction> leaders = counts.entrySet().stream()
                .filter(entry -> entry.getValue() == highest)
                .map(Map.Entry::getKey)
                .sorted(Comparator.comparing(DreamAction::id))
                .toList();
        DreamAction hostVote = votes.get(host);
        return leaders.contains(hostVote) ? hostVote : leaders.get(0);
    }

    private static String sanitizeTeam(String teamName) {
        if (teamName == null || teamName.isBlank()) return "";
        return teamName.length() > 64 ? teamName.substring(0, 64) : teamName;
    }

    private static CompoundTag writeSession(DreamSession session) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("id", session.id());
        tag.putUUID("host", session.host());
        tag.putString("organization", session.organization().toString());
        tag.putString("team", session.teamName());
        tag.putString("scenario", session.scenario().id());
        tag.putLong("dream_seed", session.dreamSeed());
        tag.putInt("lane", session.lane());
        tag.putString("state", session.state().id());
        tag.putLong("created_at", session.createdAt());
        tag.putLong("expires_at", session.expiresAt());
        tag.putLong("started_at", session.startedAt());
        tag.putLong("session_expires_at", session.sessionExpiresAt());
        tag.putInt("step", session.step());
        tag.putInt("coherence", session.coherence());
        tag.putInt("trauma", session.trauma());
        tag.putInt("clue_score", session.clueScore());
        tag.putString("pending_symbol", session.pendingSymbol() == null
                ? "" : session.pendingSymbol().id());
        ListTag participants = new ListTag();
        session.participants().entrySet().stream()
                .sorted(Map.Entry.comparingByKey(
                        Comparator.comparing(UUID::toString)))
                .forEach(entry -> {
                    CompoundTag participant = new CompoundTag();
                    participant.putUUID("player", entry.getKey());
                    participant.putBoolean("accepted", entry.getValue().accepted());
                    Origin origin = entry.getValue().origin();
                    if (origin != null) {
                        participant.putString(
                                "origin_dimension", origin.dimension().toString());
                        participant.putLong("origin", origin.position().asLong());
                    }
                    participants.add(participant);
                });
        tag.put("participants", participants);
        ListTag votes = new ListTag();
        session.votes().entrySet().stream()
                .sorted(Map.Entry.comparingByKey(
                        Comparator.comparing(UUID::toString)))
                .forEach(entry -> {
                    CompoundTag vote = new CompoundTag();
                    vote.putUUID("player", entry.getKey());
                    vote.putString("action", entry.getValue().id());
                    votes.add(vote);
                });
        tag.put("votes", votes);
        return tag;
    }

    private static DreamSession readSession(CompoundTag tag) {
        UUID id = tag.getUUID("id");
        UUID host = tag.getUUID("host");
        ResourceLocation organization = ResourceLocation.tryParse(
                tag.getString("organization"));
        DreamScenario scenario = DreamScenario.fromId(
                tag.getString("scenario"));
        DreamState state = DreamState.fromId(tag.getString("state"));
        DreamSymbol pending = tag.getString("pending_symbol").isBlank()
                ? null : DreamSymbol.fromId(tag.getString("pending_symbol"));
        if (organization == null || scenario == null || state == null
                || (!tag.getString("pending_symbol").isBlank()
                        && pending == null)) {
            throw new IllegalArgumentException("invalid shared dream identity");
        }
        Map<UUID, Participant> participants = new LinkedHashMap<>();
        for (Tag raw : tag.getList("participants", Tag.TAG_COMPOUND)) {
            CompoundTag participantTag = (CompoundTag) raw;
            UUID player = participantTag.getUUID("player");
            Origin origin = null;
            if (participantTag.contains("origin_dimension", Tag.TAG_STRING)) {
                ResourceLocation dimension = ResourceLocation.tryParse(
                        participantTag.getString("origin_dimension"));
                if (dimension == null || !participantTag.contains(
                        "origin", Tag.TAG_LONG)) {
                    throw new IllegalArgumentException("invalid dream origin");
                }
                origin = new Origin(
                        dimension, BlockPos.of(participantTag.getLong("origin")));
            }
            if (participants.putIfAbsent(player, new Participant(
                    participantTag.getBoolean("accepted"), origin)) != null) {
                throw new IllegalArgumentException("duplicate dream participant");
            }
        }
        Map<UUID, DreamAction> votes = new HashMap<>();
        for (Tag raw : tag.getList("votes", Tag.TAG_COMPOUND)) {
            CompoundTag voteTag = (CompoundTag) raw;
            UUID player = voteTag.getUUID("player");
            DreamAction action = DreamAction.fromId(
                    voteTag.getString("action"));
            if (action == null || !participants.containsKey(player)
                    || votes.putIfAbsent(player, action) != null) {
                throw new IllegalArgumentException("invalid dream vote");
            }
        }
        return new DreamSession(
                id, host, organization, tag.getString("team"), scenario,
                tag.getLong("dream_seed"), tag.getInt("lane"), state,
                tag.getLong("created_at"), tag.getLong("expires_at"),
                tag.getLong("started_at"), tag.getLong("session_expires_at"),
                tag.getInt("step"), tag.getInt("coherence"),
                tag.getInt("trauma"), tag.getInt("clue_score"), pending,
                participants, votes);
    }

    private static CompoundTag writeRecovery(Recovery recovery) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("player", recovery.player());
        tag.putUUID("session", recovery.sessionId());
        tag.putString("origin_dimension",
                recovery.origin().dimension().toString());
        tag.putLong("origin", recovery.origin().position().asLong());
        tag.putString("reason", recovery.reason().id());
        tag.putInt("clue_score", recovery.clueScore());
        tag.putString("organization", recovery.organization().toString());
        tag.putBoolean("completed", recovery.completed());
        tag.putLong("created_at", recovery.createdAt());
        return tag;
    }

    private static Recovery readRecovery(CompoundTag tag) {
        ResourceLocation dimension = ResourceLocation.tryParse(
                tag.getString("origin_dimension"));
        ResourceLocation organization = ResourceLocation.tryParse(
                tag.getString("organization"));
        DreamCloseReason reason = DreamCloseReason.fromId(
                tag.getString("reason"));
        if (dimension == null || organization == null || reason == null) {
            throw new IllegalArgumentException("invalid dream recovery");
        }
        return new Recovery(
                tag.getUUID("player"), tag.getUUID("session"),
                new Origin(dimension, BlockPos.of(tag.getLong("origin"))),
                reason, tag.getInt("clue_score"), organization,
                tag.getBoolean("completed"), tag.getLong("created_at"));
    }

    public enum DreamState {
        LOBBY("lobby"), ACTIVE("active"),
        COMPLETE("complete"), FAILED("failed");

        private final String id;

        DreamState(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }

        public static DreamState fromId(String id) {
            for (DreamState state : values()) {
                if (state.id.equals(id)) return state;
            }
            return null;
        }
    }

    public record Origin(ResourceLocation dimension, BlockPos position) {
        public Origin {
            if (dimension == null || position == null) {
                throw new IllegalArgumentException("dream origin is required");
            }
            position = position.immutable();
        }
    }

    public record Participant(boolean accepted, Origin origin) {
        public static Participant acceptedParticipant() {
            return new Participant(true, null);
        }

        public static Participant pendingParticipant() {
            return new Participant(false, null);
        }

        public Participant withOrigin(Origin value) {
            if (!accepted || value == null) {
                throw new IllegalArgumentException(
                        "accepted participant requires an origin");
            }
            return new Participant(true, value);
        }
    }

    public record DreamSession(
            UUID id, UUID host, ResourceLocation organization,
            String teamName, DreamScenario scenario, long dreamSeed,
            int lane, DreamState state, long createdAt, long expiresAt,
            long startedAt, long sessionExpiresAt, int step,
            int coherence, int trauma, int clueScore,
            DreamSymbol pendingSymbol,
            Map<UUID, Participant> participants,
            Map<UUID, DreamAction> votes) {

        public DreamSession {
            if (id == null || host == null || organization == null
                    || teamName == null || scenario == null || state == null
                    || lane < 0 || lane >= MAX_SESSIONS || createdAt < 0L
                    || expiresAt < createdAt || startedAt < 0L
                    || sessionExpiresAt < 0L || step < 0
                    || step > SharedDreamPolicy.SYMBOL_STEPS
                    || coherence != SharedDreamPolicy.clampCoherence(coherence)
                    || trauma != SharedDreamPolicy.clampTrauma(trauma)
                    || clueScore < 0 || participants == null
                    || participants.size() < 2
                    || participants.size()
                            > SharedDreamPolicy.MAX_PARTICIPANTS
                    || !participants.containsKey(host) || votes == null
                    || !participants.keySet().containsAll(votes.keySet())) {
                throw new IllegalArgumentException("invalid shared dream session");
            }
            if (state == DreamState.ACTIVE && (pendingSymbol == null
                    || sessionExpiresAt < startedAt
                    || participants.values().stream().anyMatch(
                            participant -> !participant.accepted()
                                    || participant.origin() == null))) {
                throw new IllegalArgumentException("invalid active dream session");
            }
            participants = Map.copyOf(participants);
            votes = Map.copyOf(votes);
        }

        public DreamSession withParticipants(
                Map<UUID, Participant> value, long newExpiresAt) {
            return new DreamSession(
                    id, host, organization, teamName, scenario, dreamSeed,
                    lane, state, createdAt, newExpiresAt, startedAt,
                    sessionExpiresAt, step, coherence, trauma, clueScore,
                    pendingSymbol, value, votes);
        }

        public DreamSession withVotes(Map<UUID, DreamAction> value) {
            return new DreamSession(
                    id, host, organization, teamName, scenario, dreamSeed,
                    lane, state, createdAt, expiresAt, startedAt,
                    sessionExpiresAt, step, coherence, trauma, clueScore,
                    pendingSymbol, participants, value);
        }

        public boolean allAccepted() {
            return participants.values().stream()
                    .allMatch(Participant::accepted);
        }
    }

    public record VoteResult(
            DreamSession session, boolean resolved,
            DreamAction selected,
            SharedDreamPolicy.ActionOutcome outcome) {}

    public record Recovery(
            UUID player, UUID sessionId, Origin origin,
            DreamCloseReason reason, int clueScore,
            ResourceLocation organization, boolean completed,
            long createdAt) {
        public Recovery {
            if (player == null || sessionId == null || origin == null
                    || reason == null || clueScore < 0
                    || organization == null || createdAt < 0L) {
                throw new IllegalArgumentException("invalid dream recovery");
            }
        }
    }
}
