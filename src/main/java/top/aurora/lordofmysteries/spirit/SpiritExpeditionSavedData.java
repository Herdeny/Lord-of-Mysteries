package top.aurora.lordofmysteries.spirit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.UnaryOperator;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

public final class SpiritExpeditionSavedData extends SavedData {

    public static final int SCHEMA_VERSION = 1;
    private static final String DATA_NAME =
            "lord_of_mysteries_spirit_expeditions";
    private static final int MAX_LANES = 4096;

    private final Map<UUID, Expedition> expeditions = new HashMap<>();
    private final ListTag orphanedEntries = new ListTag();
    private CompoundTag futureSnapshot;
    private int nextLane;

    public static SpiritExpeditionSavedData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) {
            throw new IllegalStateException("Overworld is unavailable");
        }
        return overworld.getDataStorage().computeIfAbsent(
                SpiritExpeditionSavedData::load,
                SpiritExpeditionSavedData::new,
                DATA_NAME);
    }

    public static SpiritExpeditionSavedData load(CompoundTag tag) {
        SpiritExpeditionSavedData data = new SpiritExpeditionSavedData();
        if (tag.getInt("schema") > SCHEMA_VERSION) {
            data.futureSnapshot = tag.copy();
            return data;
        }
        data.nextLane = Math.floorMod(tag.getInt("next_lane"), MAX_LANES);
        ListTag entries = tag.getList("expeditions", Tag.TAG_COMPOUND);
        for (Tag raw : entries) {
            CompoundTag entry = (CompoundTag) raw;
            try {
                Expedition expedition = readExpedition(entry);
                if (data.expeditions.putIfAbsent(
                        expedition.player(), expedition) != null) {
                    throw new IllegalArgumentException(
                            "duplicate player expedition");
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

    public Expedition start(
            UUID player, ResourceLocation originDimension, BlockPos origin,
            SpiritProjection projection, long routeSeed, long now) {
        if (player == null || originDimension == null || origin == null
                || projection == null || expeditions.containsKey(player)) {
            return null;
        }
        if (futureSnapshot != null) return null;
        int lane = allocateLane();
        if (lane < 0) return null;
        SpiritWeather weather = SpiritExpeditionPolicy.weatherFor(
                routeSeed, projection);
        Expedition expedition = new Expedition(
                player, originDimension, origin.immutable(), projection,
                weather, routeSeed,
                SpiritExpeditionPolicy.tideFor(routeSeed), lane,
                SpiritExpeditionWorldBuilder.padCenter(lane, 0),
                Math.max(0L, now),
                SpiritExpeditionPolicy.safeDeadline(now),
                Math.max(0L, now), 0,
                SpiritExpeditionPolicy.STARTING_STABILITY,
                0, 0, null, 0);
        expeditions.put(player, expedition);
        setDirty();
        return expedition;
    }

    public Expedition get(UUID player) {
        return player == null ? null : expeditions.get(player);
    }

    public Expedition update(UUID player, UnaryOperator<Expedition> update) {
        if (futureSnapshot != null) return null;
        Expedition current = get(player);
        if (current == null || update == null) return null;
        Expedition changed = update.apply(current);
        if (changed == null || !player.equals(changed.player())) return current;
        expeditions.put(player, changed);
        setDirty();
        return changed;
    }

    public Expedition remove(UUID player) {
        if (futureSnapshot != null) return null;
        Expedition removed = expeditions.remove(player);
        if (removed != null) setDirty();
        return removed;
    }

    public int size() {
        return expeditions.size();
    }

    public int orphanedCount() {
        return orphanedEntries.size();
    }

    public boolean isReadOnlyFutureSchema() {
        return futureSnapshot != null;
    }

    private int allocateLane() {
        for (int offset = 0; offset < MAX_LANES; offset++) {
            int candidate = Math.floorMod(nextLane + offset, MAX_LANES);
            boolean occupied = expeditions.values().stream()
                    .anyMatch(value -> value.lane() == candidate);
            if (!occupied) {
                nextLane = Math.floorMod(candidate + 1, MAX_LANES);
                return candidate;
            }
        }
        return -1;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        if (futureSnapshot != null) return futureSnapshot.copy();
        tag.putInt("schema", SCHEMA_VERSION);
        tag.putInt("next_lane", nextLane);
        ListTag entries = new ListTag();
        expeditions.values().stream()
                .sorted((left, right) -> left.player().toString()
                        .compareTo(right.player().toString()))
                .map(SpiritExpeditionSavedData::writeExpedition)
                .forEach(entries::add);
        tag.put("expeditions", entries);
        tag.put("orphaned_entries", orphanedEntries.copy());
        return tag;
    }

    private static CompoundTag writeExpedition(Expedition value) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("player", value.player());
        tag.putString("origin_dimension", value.originDimension().toString());
        tag.putLong("origin", value.origin().asLong());
        tag.putString("projection", value.projection().id());
        tag.putString("weather", value.weather().id());
        tag.putLong("route_seed", value.routeSeed());
        tag.putInt("tide", value.tide());
        tag.putInt("lane", value.lane());
        tag.putLong("current_pad", value.currentPad().asLong());
        tag.putLong("started_at", value.startedAt());
        tag.putLong("expires_at", value.expiresAt());
        tag.putLong("last_action_at", value.lastActionAt());
        tag.putInt("step", value.step());
        tag.putInt("stability", value.stability());
        tag.putInt("drift", value.drift());
        tag.putInt("reward_score", value.rewardScore());
        tag.putString("pending_encounter",
                value.pendingEncounter() == null
                        ? "" : value.pendingEncounter().id());
        tag.putInt("resolved_encounters", value.resolvedEncounters());
        return tag;
    }

    private static Expedition readExpedition(CompoundTag tag) {
        UUID player = tag.getUUID("player");
        ResourceLocation originDimension = ResourceLocation.tryParse(
                tag.getString("origin_dimension"));
        SpiritProjection projection = SpiritProjection.fromId(
                tag.getString("projection"));
        SpiritWeather weather = SpiritWeather.fromId(tag.getString("weather"));
        if (originDimension == null || projection == null || weather == null) {
            throw new IllegalArgumentException("invalid expedition identity");
        }
        SpiritEncounter pending = null;
        String encounterId = tag.getString("pending_encounter");
        if (!encounterId.isBlank()) {
            for (SpiritEncounter encounter : SpiritEncounter.values()) {
                if (encounter.id().equals(encounterId)) {
                    pending = encounter;
                    break;
                }
            }
            if (pending == null) {
                throw new IllegalArgumentException("invalid encounter");
            }
        }
        return new Expedition(
                player, originDimension, BlockPos.of(tag.getLong("origin")),
                projection, weather, tag.getLong("route_seed"),
                tag.getInt("tide"), tag.getInt("lane"),
                BlockPos.of(tag.getLong("current_pad")),
                tag.getLong("started_at"), tag.getLong("expires_at"),
                tag.getLong("last_action_at"), tag.getInt("step"),
                tag.getInt("stability"), tag.getInt("drift"),
                tag.getInt("reward_score"), pending,
                tag.getInt("resolved_encounters"));
    }

    public record Expedition(
            UUID player,
            ResourceLocation originDimension,
            BlockPos origin,
            SpiritProjection projection,
            SpiritWeather weather,
            long routeSeed,
            int tide,
            int lane,
            BlockPos currentPad,
            long startedAt,
            long expiresAt,
            long lastActionAt,
            int step,
            int stability,
            int drift,
            int rewardScore,
            SpiritEncounter pendingEncounter,
            int resolvedEncounters) {

        public Expedition {
            if (player == null || originDimension == null || origin == null
                    || projection == null || weather == null
                    || currentPad == null || lane < 0 || lane >= MAX_LANES) {
                throw new IllegalArgumentException("invalid spirit expedition");
            }
            origin = origin.immutable();
            currentPad = currentPad.immutable();
            tide = Math.floorMod(tide, 8);
            startedAt = Math.max(0L, startedAt);
            expiresAt = Math.min(
                    Math.max(startedAt, expiresAt),
                    SpiritExpeditionPolicy.safeDeadline(startedAt));
            lastActionAt = Math.max(startedAt,
                    Math.min(expiresAt, lastActionAt));
            step = Math.max(0, Math.min(
                    SpiritExpeditionPolicy.ROUTE_LEGS, step));
            stability = SpiritExpeditionPolicy.clampStability(stability);
            drift = SpiritExpeditionPolicy.clampDrift(drift);
            rewardScore = Math.max(0, Math.min(
                    SpiritExpeditionPolicy.MAX_REWARD_SCORE, rewardScore));
            resolvedEncounters = Math.max(0, Math.min(
                    SpiritExpeditionPolicy.ROUTE_LEGS, resolvedEncounters));
        }

        public boolean extractionReady() {
            return step >= SpiritExpeditionPolicy.ROUTE_LEGS;
        }

        public boolean failed() {
            return stability <= 0 || drift >= SpiritExpeditionPolicy.MAX_DRIFT;
        }

        public Expedition afterNavigation(
                SpiritExpeditionPolicy.NavigationOutcome outcome,
                BlockPos destination, long now) {
            return new Expedition(
                    player, originDimension, origin, projection, weather,
                    routeSeed, tide, lane, destination, startedAt, expiresAt,
                    Math.max(lastActionAt, now),
                    Math.min(SpiritExpeditionPolicy.ROUTE_LEGS,
                            step + (outcome.correct() ? 1 : 0)),
                    SpiritExpeditionPolicy.clampStability(
                            stability + outcome.stabilityDelta()),
                    SpiritExpeditionPolicy.clampDrift(
                            drift + outcome.driftDelta()),
                    SpiritExpeditionPolicy.saturatedAdd(
                            rewardScore, outcome.rewardDelta()),
                    outcome.encounter(), resolvedEncounters);
        }

        public Expedition afterEncounter(
                SpiritExpeditionPolicy.EncounterOutcome outcome, long now) {
            return new Expedition(
                    player, originDimension, origin, projection, weather,
                    routeSeed, tide, lane, currentPad, startedAt, expiresAt,
                    Math.max(lastActionAt, now), step,
                    SpiritExpeditionPolicy.clampStability(
                            stability + outcome.stabilityDelta()),
                    SpiritExpeditionPolicy.clampDrift(
                            drift + outcome.driftDelta()),
                    SpiritExpeditionPolicy.saturatedAdd(
                            rewardScore, outcome.rewardDelta()),
                    null, SpiritExpeditionPolicy.saturatedAdd(
                            resolvedEncounters, 1));
        }

        public Expedition stabilized(long now) {
            return new Expedition(
                    player, originDimension, origin, projection, weather,
                    routeSeed, tide, lane, currentPad, startedAt, expiresAt,
                    Math.max(lastActionAt, now), step,
                    SpiritExpeditionPolicy.clampStability(stability + 20),
                    SpiritExpeditionPolicy.clampDrift(drift - 20),
                    rewardScore, pendingEncounter, resolvedEncounters);
        }
    }
}
