package top.aurora.lordofmysteries.commission;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import top.aurora.lordofmysteries.player.PlayerMysteryData;

public final class QuestPartySnapshot {

    private final String commissionId;
    private final String questChainId;
    private int questStep;
    private int objectiveProgress;
    private long acceptedTick;
    private String reporterUuid;
    private boolean defenseWaveSpawned;
    private long defenseNextTick;
    private String resolutionRoute;
    private boolean resolutionReady;
    private long updatedTick;
    private final Set<UUID> members = new LinkedHashSet<>();
    private final Set<UUID> settledMembers = new LinkedHashSet<>();

    private QuestPartySnapshot(String commissionId, String questChainId) {
        this.commissionId = commissionId;
        this.questChainId = questChainId;
        this.reporterUuid = "";
        this.resolutionRoute = "";
    }

    public static QuestPartySnapshot create(PlayerMysteryData data, UUID member,
                                            long gameTime) {
        QuestPartySnapshot snapshot = new QuestPartySnapshot(
                data.activeCommissionId, data.activeQuestChainId);
        snapshot.members.add(member);
        snapshot.copyState(data);
        snapshot.updatedTick = gameTime;
        return snapshot;
    }

    public static QuestPartySnapshot load(CompoundTag tag) {
        QuestPartySnapshot snapshot = new QuestPartySnapshot(
                tag.getString("commission_id"), tag.getString("quest_chain_id"));
        snapshot.questStep = Math.max(0, tag.getInt("quest_step"));
        snapshot.objectiveProgress = Math.max(0, tag.getInt("objective_progress"));
        snapshot.acceptedTick = Math.max(0L, tag.getLong("accepted_tick"));
        snapshot.reporterUuid = sanitizeUuid(tag.getString("reporter_uuid"));
        snapshot.defenseWaveSpawned = tag.getBoolean("defense_wave_spawned");
        snapshot.defenseNextTick = Math.max(0L, tag.getLong("defense_next_tick"));
        snapshot.resolutionRoute = sanitizeRoute(tag.getString("resolution_route"));
        snapshot.resolutionReady = tag.getBoolean("resolution_ready");
        if (snapshot.resolutionRoute.isBlank()) snapshot.resolutionReady = false;
        snapshot.updatedTick = Math.max(0L, tag.getLong("updated_tick"));
        loadUuids(tag.getList("members", Tag.TAG_COMPOUND), snapshot.members,
                QuestPartyPolicy.MAXIMUM_PERSISTENT_PARTY_SIZE);
        loadUuids(tag.getList("settled_members", Tag.TAG_COMPOUND),
                snapshot.settledMembers,
                QuestPartyPolicy.MAXIMUM_PERSISTENT_PARTY_SIZE);
        snapshot.settledMembers.retainAll(snapshot.members);
        return snapshot;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("commission_id", commissionId);
        tag.putString("quest_chain_id", questChainId);
        tag.putInt("quest_step", questStep);
        tag.putInt("objective_progress", objectiveProgress);
        tag.putLong("accepted_tick", acceptedTick);
        tag.putString("reporter_uuid", reporterUuid);
        tag.putBoolean("defense_wave_spawned", defenseWaveSpawned);
        tag.putLong("defense_next_tick", defenseNextTick);
        tag.putString("resolution_route", resolutionRoute);
        tag.putBoolean("resolution_ready", resolutionReady);
        tag.putLong("updated_tick", updatedTick);
        tag.put("members", saveUuids(members));
        tag.put("settled_members", saveUuids(settledMembers));
        return tag;
    }

    public boolean mergeProgress(PlayerMysteryData data, UUID member, long gameTime) {
        if (!matches(data) || !members.contains(member)
                || settledMembers.contains(member)) return false;
        boolean changed = false;
        int comparison = compareProgress(data.activeQuestStep,
                data.questObjectiveProgress, questStep, objectiveProgress);
        if (comparison > 0) {
            copyState(data);
            changed = true;
        } else if (comparison == 0) {
            if (reporterUuid.isBlank() && !data.escortedReporterUuid.isBlank()) {
                reporterUuid = data.escortedReporterUuid;
                changed = true;
            }
            if (resolutionRoute.isBlank() && !data.questResolutionRoute.isBlank()) {
                resolutionRoute = data.questResolutionRoute;
                changed = true;
            }
            if (!resolutionReady && data.questResolutionReady
                    && resolutionRoute.equals(data.questResolutionRoute)) {
                resolutionReady = true;
                changed = true;
            }
        }
        if (acceptedTick <= 0L && data.commissionAcceptedTick > 0L) {
            acceptedTick = data.commissionAcceptedTick;
            changed = true;
        }
        if (changed) updatedTick = gameTime;
        return changed;
    }

    public boolean updateAuthoritative(PlayerMysteryData data, long gameTime) {
        if (!matches(data) || compareProgress(
                data.activeQuestStep, data.questObjectiveProgress,
                questStep, objectiveProgress) < 0) return false;
        boolean changed = questStep != data.activeQuestStep
                || objectiveProgress != data.questObjectiveProgress
                || acceptedTick != data.commissionAcceptedTick
                || !reporterUuid.equals(data.escortedReporterUuid)
                || defenseWaveSpawned != data.questDefenseWaveSpawned
                || defenseNextTick != data.questDefenseNextTick
                || !resolutionRoute.equals(data.questResolutionRoute)
                || resolutionReady != data.questResolutionReady;
        if (changed) {
            copyState(data);
            updatedTick = gameTime;
        }
        return changed;
    }

    public boolean applyTo(PlayerMysteryData data, UUID member) {
        if (!members.contains(member) || settledMembers.contains(member)) return false;
        if (!data.activeCommissionId.isBlank() && !matches(data)) return false;
        boolean blank = data.activeCommissionId.isBlank();
        int comparison = blank ? 1 : compareProgress(
                questStep, objectiveProgress,
                data.activeQuestStep, data.questObjectiveProgress);
        boolean changed = false;
        if (blank || comparison > 0) {
            data.activeCommissionId = commissionId;
            data.activeQuestChainId = questChainId;
            data.activeQuestStep = questStep;
            data.questObjectiveProgress = objectiveProgress;
            changed = true;
        }
        if (blank || comparison >= 0) {
            changed |= copySharedStateTo(data);
        }
        return changed;
    }

    public boolean addMember(UUID member, int maximumPartySize) {
        if (members.contains(member)) return false;
        if (members.size() >= maximumPartySize) return false;
        return members.add(member);
    }

    public boolean markSettled(UUID member) {
        return members.contains(member) && settledMembers.add(member);
    }

    public boolean removeMember(UUID member) {
        settledMembers.remove(member);
        return members.remove(member);
    }

    public boolean hasMember(UUID member) {
        return members.contains(member);
    }

    public boolean hasSettled(UUID member) {
        return settledMembers.contains(member);
    }

    public boolean isFinished() {
        return members.isEmpty() || settledMembers.containsAll(members);
    }

    public boolean matches(PlayerMysteryData data) {
        return matches(data.activeCommissionId, data.activeQuestChainId);
    }

    public boolean matches(String expectedCommissionId,
                           String expectedQuestChainId) {
        return commissionId.equals(expectedCommissionId)
                && questChainId.equals(expectedQuestChainId);
    }

    public boolean validFor(QuestChainDefinition chain) {
        if (!questChainId.equals(chain.id().toString())
                || questStep < 0 || questStep > chain.steps().size()) return false;
        if (questStep == chain.steps().size()) return objectiveProgress == 0;
        return objectiveProgress >= 0
                && objectiveProgress < chain.steps().get(
                        questStep).objective().count();
    }

    public String commissionId() {
        return commissionId;
    }

    public String questChainId() {
        return questChainId;
    }

    public int questStep() {
        return questStep;
    }

    public int objectiveProgress() {
        return objectiveProgress;
    }

    public long updatedTick() {
        return updatedTick;
    }

    public Set<UUID> members() {
        return Set.copyOf(members);
    }

    public Set<UUID> settledMembers() {
        return Set.copyOf(settledMembers);
    }

    private void copyState(PlayerMysteryData data) {
        questStep = data.activeQuestStep;
        objectiveProgress = data.questObjectiveProgress;
        acceptedTick = data.commissionAcceptedTick;
        reporterUuid = data.escortedReporterUuid;
        defenseWaveSpawned = data.questDefenseWaveSpawned;
        defenseNextTick = data.questDefenseNextTick;
        resolutionRoute = data.questResolutionRoute;
        resolutionReady = data.questResolutionReady;
    }

    private boolean copySharedStateTo(PlayerMysteryData data) {
        boolean changed = false;
        if (data.commissionAcceptedTick != acceptedTick) {
            data.commissionAcceptedTick = acceptedTick;
            changed = true;
        }
        if (!data.escortedReporterUuid.equals(reporterUuid)) {
            data.escortedReporterUuid = reporterUuid;
            changed = true;
        }
        if (data.questDefenseWaveSpawned != defenseWaveSpawned) {
            data.questDefenseWaveSpawned = defenseWaveSpawned;
            changed = true;
        }
        if (data.questDefenseNextTick != defenseNextTick) {
            data.questDefenseNextTick = defenseNextTick;
            changed = true;
        }
        if (!data.questResolutionRoute.equals(resolutionRoute)) {
            data.questResolutionRoute = resolutionRoute;
            changed = true;
        }
        if (data.questResolutionReady != resolutionReady) {
            data.questResolutionReady = resolutionReady;
            changed = true;
        }
        return changed;
    }

    private static int compareProgress(int leftStep, int leftProgress,
                                       int rightStep, int rightProgress) {
        int stepComparison = Integer.compare(leftStep, rightStep);
        return stepComparison != 0 ? stepComparison
                : Integer.compare(leftProgress, rightProgress);
    }

    private static ListTag saveUuids(Set<UUID> values) {
        ListTag list = new ListTag();
        for (UUID value : values) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("id", value);
            list.add(entry);
        }
        return list;
    }

    private static void loadUuids(ListTag list, Set<UUID> output, int maximum) {
        for (int index = 0; index < list.size() && output.size() < maximum; index++) {
            CompoundTag entry = list.getCompound(index);
            if (entry.hasUUID("id")) output.add(entry.getUUID("id"));
        }
    }

    private static String sanitizeRoute(String route) {
        return switch (route) {
            case "assault", "stealth", "divination" -> route;
            default -> DynamicCaseService.isResolutionState(route) ? route : "";
        };
    }

    private static String sanitizeUuid(String value) {
        if (value.isBlank()) return "";
        try {
            return UUID.fromString(value).toString();
        } catch (IllegalArgumentException ignored) {
            return "";
        }
    }
}
