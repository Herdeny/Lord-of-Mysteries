package top.aurora.lordofmysteries.artifact;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public final class ArtifactCustodySavedData extends SavedData {

    private static final String DATA_NAME =
            "lord_of_mysteries_artifact_custody";
    private final Map<UUID, CustodyRecord> records = new HashMap<>();
    private final ListTag orphanedEntries = new ListTag();

    public static ArtifactCustodySavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                ArtifactCustodySavedData::load,
                ArtifactCustodySavedData::new,
                DATA_NAME);
    }

    public static ArtifactCustodySavedData load(CompoundTag tag) {
        ArtifactCustodySavedData data =
                new ArtifactCustodySavedData();
        ListTag recordsTag = tag.getList("records", Tag.TAG_COMPOUND);
        for (Tag raw : recordsTag) {
            CompoundTag entry = (CompoundTag) raw;
            try {
                CustodyRecord record = CustodyRecord.load(entry);
                if (data.records.putIfAbsent(
                        record.instanceId(), record) != null) {
                    throw new IllegalArgumentException(
                            "duplicate custody instance");
                }
            } catch (RuntimeException exception) {
                data.orphanedEntries.add(entry.copy());
            }
        }
        ListTag orphans = tag.getList(
                "orphaned_entries", Tag.TAG_COMPOUND);
        orphans.forEach(raw -> data.orphanedEntries.add(raw.copy()));
        return data;
    }

    public UUID issue(
            SealedArtifactDefinition definition,
            UUID responsible,
            long day,
            long gameTime,
            ResourceLocation dimension,
            BlockPos position) {
        if (definition == null || responsible == null || day < 0L
                || gameTime < 0L || dimension == null
                || position == null
                || activeForDefinition(definition.id()) != null) {
            return null;
        }
        UUID instance = UUID.randomUUID();
        records.put(instance, new CustodyRecord(
                instance,
                definition.id(),
                definition.custodyOrganization(),
                ArtifactCustodyState.BORROWED,
                responsible,
                responsible,
                dimension,
                position.asLong(),
                0,
                0,
                day,
                day + definition.loanDays(),
                gameTime,
                0));
        setDirty();
        return instance;
    }

    public Observation observe(
            UUID instance,
            ResourceLocation artifact,
            UUID holder,
            ResourceLocation dimension,
            BlockPos position,
            long day,
            long gameTime) {
        CustodyRecord record = records.get(instance);
        if (record == null || artifact == null
                || !record.artifactId().equals(artifact)) {
            return Observation.INVALID;
        }
        if (!record.state().active()) return Observation.RETURNED;
        if (record.holder() != null
                && !record.holder().equals(holder)
                && record.lastSeenGameTime() == gameTime) {
            records.put(instance, record.withState(
                    ArtifactCustodyState.ABUSED,
                    record.contamination() + 10,
                    record.incidents() + 1));
            setDirty();
            return Observation.DUPLICATE;
        }
        ArtifactCustodyState state = record.state();
        Observation result = Observation.OK;
        if (holder != null && !holder.equals(record.responsible())
                && state == ArtifactCustodyState.BORROWED) {
            state = ArtifactCustodyState.RECOVERED;
            result = Observation.HOLDER_CHANGED;
        }
        if (day > record.dueDay()
                && state == ArtifactCustodyState.BORROWED) {
            state = ArtifactCustodyState.LEAKED;
            result = Observation.OVERDUE_LEAK;
        }
        records.put(instance, new CustodyRecord(
                record.instanceId(),
                record.artifactId(),
                record.organizationId(),
                state,
                record.responsible(),
                holder,
                dimension,
                position.asLong(),
                record.contamination(),
                record.uses(),
                record.issuedDay(),
                record.dueDay(),
                gameTime,
                record.incidents()));
        setDirty();
        return result;
    }

    public ArtifactCustodyState recordUse(
            UUID instance, int danger, int safeUses,
            int leakThreshold) {
        CustodyRecord record = records.get(instance);
        if (record == null || !record.state().active()) {
            return ArtifactCustodyState.ABUSED;
        }
        int uses = Math.min(10_000, record.uses() + 1);
        int contamination = Math.min(
                10_000, record.contamination() + Math.max(1, danger));
        ArtifactCustodyState state = record.state();
        int incidents = record.incidents();
        if (state == ArtifactCustodyState.BORROWED
                && (uses > safeUses
                || contamination >= leakThreshold)) {
            state = ArtifactCustodyState.LEAKED;
            incidents++;
        } else if (state == ArtifactCustodyState.LEAKED) {
            incidents++;
        }
        records.put(instance, new CustodyRecord(
                record.instanceId(), record.artifactId(),
                record.organizationId(), state, record.responsible(),
                record.holder(), record.dimension(), record.position(),
                contamination, uses, record.issuedDay(), record.dueDay(),
                record.lastSeenGameTime(), incidents));
        setDirty();
        return state;
    }

    public boolean stabilize(UUID instance, UUID holder) {
        CustodyRecord record = records.get(instance);
        if (record == null || holder == null
                || !holder.equals(record.holder())
                || record.state() != ArtifactCustodyState.LEAKED) {
            return false;
        }
        records.put(instance, new CustodyRecord(
                record.instanceId(), record.artifactId(),
                record.organizationId(),
                ArtifactCustodyState.RECOVERED,
                record.responsible(), holder,
                record.dimension(), record.position(),
                Math.max(0, record.contamination() / 2),
                record.uses(), record.issuedDay(), record.dueDay(),
                record.lastSeenGameTime(), record.incidents()));
        setDirty();
        return true;
    }

    public boolean returnToVault(UUID instance, UUID holder) {
        CustodyRecord record = records.get(instance);
        if (record == null || holder == null
                || !holder.equals(record.holder())
                || record.state() == ArtifactCustodyState.ABUSED
                || record.state() == ArtifactCustodyState.RETURNED) {
            return false;
        }
        records.put(instance, new CustodyRecord(
                record.instanceId(), record.artifactId(),
                record.organizationId(),
                ArtifactCustodyState.RETURNED,
                record.responsible(), null,
                record.dimension(), record.position(),
                record.contamination(), record.uses(),
                record.issuedDay(), record.dueDay(),
                record.lastSeenGameTime(), record.incidents()));
        setDirty();
        return true;
    }

    public boolean markDropped(
            UUID instance, ResourceLocation dimension,
            BlockPos position, long gameTime) {
        CustodyRecord record = records.get(instance);
        if (record == null || !record.state().active()) return false;
        records.put(instance, new CustodyRecord(
                record.instanceId(), record.artifactId(),
                record.organizationId(), record.state(),
                record.responsible(), null, dimension,
                position.asLong(), record.contamination(),
                record.uses(), record.issuedDay(), record.dueDay(),
                gameTime, record.incidents()));
        setDirty();
        return true;
    }

    public int expireOverdue(long day) {
        if (day < 0L) return 0;
        int expired = 0;
        for (Map.Entry<UUID, CustodyRecord> entry
                : new ArrayList<>(records.entrySet())) {
            CustodyRecord record = entry.getValue();
            if (record.state() != ArtifactCustodyState.BORROWED
                    || day <= record.dueDay()) {
                continue;
            }
            entry.setValue(new CustodyRecord(
                    record.instanceId(), record.artifactId(),
                    record.organizationId(),
                    ArtifactCustodyState.LEAKED,
                    record.responsible(), record.holder(),
                    record.dimension(), record.position(),
                    record.contamination(), record.uses(),
                    record.issuedDay(), record.dueDay(),
                    record.lastSeenGameTime(),
                    record.incidents() + 1));
            expired++;
        }
        if (expired > 0) setDirty();
        return expired;
    }

    public boolean markAbused(UUID instance, int contaminationIncrease) {
        CustodyRecord record = records.get(instance);
        if (record == null || !record.state().active()) return false;
        records.put(instance, record.withState(
                ArtifactCustodyState.ABUSED,
                Math.min(10_000, record.contamination()
                        + Math.max(1, contaminationIncrease)),
                record.incidents() + 1));
        setDirty();
        return true;
    }

    public boolean retireAbused(UUID instance) {
        CustodyRecord record = records.get(instance);
        if (record == null
                || record.state() != ArtifactCustodyState.ABUSED) {
            return false;
        }
        records.put(instance, new CustodyRecord(
                record.instanceId(), record.artifactId(),
                record.organizationId(),
                ArtifactCustodyState.RETURNED,
                record.responsible(), null,
                record.dimension(), record.position(),
                record.contamination(), record.uses(),
                record.issuedDay(), record.dueDay(),
                record.lastSeenGameTime(), record.incidents()));
        setDirty();
        return true;
    }

    public CustodyRecord activeForDefinition(ResourceLocation artifact) {
        return records.values().stream()
                .filter(record -> record.artifactId().equals(artifact))
                .filter(record -> record.state().active())
                .findFirst()
                .orElse(null);
    }

    public CustodyRecord record(UUID instance) {
        return records.get(instance);
    }

    public List<CustodyRecord> records() {
        return records.values().stream()
                .sorted(Comparator.comparing(
                        value -> value.instanceId().toString()))
                .toList();
    }

    public int orphanedCount() {
        return orphanedEntries.size();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag recordsTag = new ListTag();
        records().forEach(record -> recordsTag.add(record.save()));
        tag.put("records", recordsTag);
        tag.put("orphaned_entries", orphanedEntries.copy());
        return tag;
    }

    public enum Observation {
        OK,
        HOLDER_CHANGED,
        OVERDUE_LEAK,
        DUPLICATE,
        INVALID,
        RETURNED
    }

    public record CustodyRecord(
            UUID instanceId,
            ResourceLocation artifactId,
            ResourceLocation organizationId,
            ArtifactCustodyState state,
            UUID responsible,
            UUID holder,
            ResourceLocation dimension,
            long position,
            int contamination,
            int uses,
            long issuedDay,
            long dueDay,
            long lastSeenGameTime,
            int incidents) {

        public CustodyRecord {
            if (instanceId == null || artifactId == null
                    || organizationId == null || state == null
                    || responsible == null || dimension == null
                    || contamination < 0 || uses < 0 || issuedDay < 0L
                    || dueDay < issuedDay || lastSeenGameTime < 0L
                    || incidents < 0) {
                throw new IllegalArgumentException(
                        "invalid artifact custody record");
            }
        }

        private CustodyRecord withState(
                ArtifactCustodyState nextState,
                int nextContamination,
                int nextIncidents) {
            return new CustodyRecord(
                    instanceId, artifactId, organizationId, nextState,
                    responsible, holder, dimension, position,
                    nextContamination, uses, issuedDay, dueDay,
                    lastSeenGameTime, nextIncidents);
        }

        public BlockPos blockPosition() {
            return BlockPos.of(position);
        }

        private CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("instance", instanceId);
            tag.putString("artifact", artifactId.toString());
            tag.putString("organization", organizationId.toString());
            tag.putString("state", state.id());
            tag.putUUID("responsible", responsible);
            if (holder != null) tag.putUUID("holder", holder);
            tag.putString("dimension", dimension.toString());
            tag.putLong("position", position);
            tag.putInt("contamination", contamination);
            tag.putInt("uses", uses);
            tag.putLong("issued_day", issuedDay);
            tag.putLong("due_day", dueDay);
            tag.putLong("last_seen", lastSeenGameTime);
            tag.putInt("incidents", incidents);
            return tag;
        }

        private static CustodyRecord load(CompoundTag tag) {
            ResourceLocation artifact = ResourceLocation.tryParse(
                    tag.getString("artifact"));
            ResourceLocation organization = ResourceLocation.tryParse(
                    tag.getString("organization"));
            ResourceLocation dimension = ResourceLocation.tryParse(
                    tag.getString("dimension"));
            return new CustodyRecord(
                    tag.getUUID("instance"),
                    artifact,
                    organization,
                    ArtifactCustodyState.fromId(tag.getString("state")),
                    tag.getUUID("responsible"),
                    tag.hasUUID("holder") ? tag.getUUID("holder") : null,
                    dimension,
                    tag.getLong("position"),
                    tag.getInt("contamination"),
                    tag.getInt("uses"),
                    tag.getLong("issued_day"),
                    tag.getLong("due_day"),
                    tag.getLong("last_seen"),
                    tag.getInt("incidents"));
        }
    }
}
