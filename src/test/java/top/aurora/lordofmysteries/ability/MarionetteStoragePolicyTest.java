package top.aurora.lordofmysteries.ability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import net.minecraft.nbt.CompoundTag;

class MarionetteStoragePolicyTest {

    private static final UUID ENTITY_ID = UUID.fromString(
            "00000000-0000-0000-0000-000000000028");
    private static final UUID TOKEN = UUID.fromString(
            "10000000-0000-0000-0000-000000000028");

    @Test
    void createsDefensiveAuthoritativeRecord() {
        CompoundTag payload = validPayload(ENTITY_ID);
        CompoundTag record =
                MarionetteStoragePolicy.createRecord(TOKEN, payload);
        payload.putString("id", "minecraft:creeper");

        assertTrue(MarionetteStoragePolicy.isValidRecord(record));
        assertTrue(MarionetteStoragePolicy.tokenMatches(record, TOKEN));
        assertEquals("minecraft:zombie",
                MarionetteStoragePolicy.payload(record).getString("id"));
        assertNotSame(record.getCompound(
                        MarionetteStoragePolicy.PAYLOAD_KEY),
                MarionetteStoragePolicy.payload(record));
    }

    @Test
    void rejectsMissingOrMismatchedToken() {
        CompoundTag record = MarionetteStoragePolicy.createRecord(
                TOKEN, validPayload(ENTITY_ID));

        assertFalse(MarionetteStoragePolicy.tokenMatches(
                record, UUID.randomUUID()));
        assertFalse(MarionetteStoragePolicy.tokenMatches(record, null));
        assertFalse(MarionetteStoragePolicy.isValidRecord(
                new CompoundTag()));
    }

    @Test
    void normalizationKeepsOnlyRosterMatchedPayloads() {
        UUID outsideRoster = UUID.randomUUID();
        UUID mismatchedPayload = UUID.randomUUID();
        Map<UUID, CompoundTag> records = Map.of(
                ENTITY_ID,
                MarionetteStoragePolicy.createRecord(
                        TOKEN, validPayload(ENTITY_ID)),
                outsideRoster,
                MarionetteStoragePolicy.createRecord(
                        UUID.randomUUID(), validPayload(outsideRoster)),
                mismatchedPayload,
                MarionetteStoragePolicy.createRecord(
                        UUID.randomUUID(), validPayload(UUID.randomUUID())));

        Map<UUID, CompoundTag> normalized =
                MarionetteStoragePolicy.normalizeRecords(
                        records,
                        List.of(ENTITY_ID, mismatchedPayload));

        assertEquals(1, normalized.size());
        assertTrue(normalized.containsKey(ENTITY_ID));
    }

    @Test
    void payloadReadsCannotMutateServerRecord() {
        CompoundTag record = MarionetteStoragePolicy.createRecord(
                TOKEN, validPayload(ENTITY_ID));
        CompoundTag firstRead = MarionetteStoragePolicy.payload(record);
        firstRead.putFloat("Health", 999f);

        assertEquals(4f,
                MarionetteStoragePolicy.payload(record).getFloat("Health"));
    }

    private static CompoundTag validPayload(UUID entityId) {
        CompoundTag payload = new CompoundTag();
        payload.putString("id", "minecraft:zombie");
        payload.putUUID("UUID", entityId);
        payload.putFloat("Health", 4f);
        return payload;
    }
}
