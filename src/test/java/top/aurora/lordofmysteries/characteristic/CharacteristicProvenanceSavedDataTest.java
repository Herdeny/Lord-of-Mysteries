package top.aurora.lordofmysteries.characteristic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

class CharacteristicProvenanceSavedDataTest {

    @Test
    void consumesSourcesAtomicallyAndRejectsCrossPlayerReplay() {
        CharacteristicProvenanceSavedData data =
                new CharacteristicProvenanceSavedData();
        UUID firstActor = UUID.randomUUID();
        UUID secondActor = UUID.randomUUID();

        assertEquals(
                CharacteristicProvenanceSavedData.ConsumptionResult.ACCEPTED,
                data.consume(
                        "item_merge",
                        firstActor,
                        120L,
                        List.of("source-a", "source-b"),
                        List.of("output-c")));
        assertEquals(2, data.consumedSourceCount());
        assertEquals(1, data.operationCount("item_merge"));
        assertTrue(data.isConsumed("source-a"));

        assertEquals(
                CharacteristicProvenanceSavedData.ConsumptionResult.REPLAY,
                data.consume(
                        "player_absorb",
                        secondActor,
                        121L,
                        List.of("new-player-ledger", "source-a"),
                        List.of("forged-output")));
        assertFalse(data.isConsumed("new-player-ledger"));
        assertEquals(2, data.consumedSourceCount());
    }

    @Test
    void survivesSavedDataRoundTripWithoutSavingRealActorUuid() {
        CharacteristicProvenanceSavedData data =
                new CharacteristicProvenanceSavedData();
        UUID actor = UUID.randomUUID();
        data.consume(
                "item_split",
                actor,
                400L,
                List.of("parent"),
                List.of("child-a", "child-b"));

        CompoundTag saved = data.save(new CompoundTag());
        CharacteristicProvenanceSavedData restored =
                CharacteristicProvenanceSavedData.load(saved);

        assertEquals(
                CharacteristicProvenanceSavedData.DATA_VERSION,
                saved.getInt("data_version"));
        assertTrue(restored.isConsumed("parent"));
        assertEquals(1, restored.operationCount("item_split"));
        assertFalse(saved.toString().contains(actor.toString()));
    }

    @Test
    void rejectsInvalidOrRepeatedInputsWithoutPartialWrites() {
        CharacteristicProvenanceSavedData data =
                new CharacteristicProvenanceSavedData();
        UUID actor = UUID.randomUUID();

        assertEquals(
                CharacteristicProvenanceSavedData.ConsumptionResult.REPLAY,
                data.consume(
                        "item_merge",
                        actor,
                        1L,
                        List.of("same", "same"),
                        List.of("output")));
        assertEquals(
                CharacteristicProvenanceSavedData.ConsumptionResult.INVALID,
                data.consume(
                        "INVALID OPERATION",
                        actor,
                        1L,
                        List.of("source"),
                        List.of("output")));
        assertEquals(0, data.consumedSourceCount());
    }

    @Test
    void dropsMalformedEntriesDuringLoad() {
        CompoundTag saved = new CompoundTag();
        ListTag entries = new ListTag();
        CompoundTag malformed = new CompoundTag();
        malformed.putString("source", "not-a-hash");
        malformed.putString("operation", "item_split");
        malformed.putString("actor_hash", "also-invalid");
        entries.add(malformed);
        saved.put("consumed_sources", entries);

        assertEquals(
                0,
                CharacteristicProvenanceSavedData.load(saved)
                        .consumedSourceCount());
    }
}
