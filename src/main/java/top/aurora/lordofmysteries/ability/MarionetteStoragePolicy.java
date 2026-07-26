package top.aurora.lordofmysteries.ability;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public final class MarionetteStoragePolicy {

    public static final float STORAGE_COST = 5f;
    public static final int ITEM_COOLDOWN_TICKS = 40;
    public static final String TOKEN_KEY = "token";
    public static final String PAYLOAD_KEY = "entity_payload";

    private MarionetteStoragePolicy() {}

    public static CompoundTag createRecord(
            UUID token, CompoundTag entityPayload) {
        if (token == null || entityPayload == null) {
            throw new IllegalArgumentException(
                    "token and entity payload are required");
        }
        CompoundTag record = new CompoundTag();
        record.putUUID(TOKEN_KEY, token);
        record.put(PAYLOAD_KEY, entityPayload.copy());
        return record;
    }

    public static boolean isValidRecord(CompoundTag record) {
        if (record == null
                || !record.hasUUID(TOKEN_KEY)
                || !record.contains(PAYLOAD_KEY, Tag.TAG_COMPOUND)) {
            return false;
        }
        CompoundTag payload = record.getCompound(PAYLOAD_KEY);
        return payload.hasUUID("UUID")
                && payload.contains("id", Tag.TAG_STRING)
                && !payload.getString("id").isBlank();
    }

    public static boolean tokenMatches(
            CompoundTag record, UUID token) {
        return token != null
                && isValidRecord(record)
                && token.equals(record.getUUID(TOKEN_KEY));
    }

    public static CompoundTag payload(CompoundTag record) {
        return isValidRecord(record)
                ? record.getCompound(PAYLOAD_KEY).copy()
                : new CompoundTag();
    }

    public static Map<UUID, CompoundTag> normalizeRecords(
            Map<UUID, CompoundTag> records,
            List<UUID> roster) {
        Map<UUID, CompoundTag> normalized = new LinkedHashMap<>();
        if (records == null || roster == null) return normalized;
        for (UUID entityId : MarionettePolicy.normalizeRoster(roster)) {
            CompoundTag record = records.get(entityId);
            if (entityId != null && isValidRecord(record)
                    && entityId.equals(payload(record).getUUID("UUID"))) {
                normalized.put(entityId, record.copy());
            }
        }
        return normalized;
    }
}
