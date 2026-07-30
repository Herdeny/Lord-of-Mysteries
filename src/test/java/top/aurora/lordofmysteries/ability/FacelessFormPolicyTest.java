package top.aurora.lordofmysteries.ability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import net.minecraft.nbt.CompoundTag;

class FacelessFormPolicyTest {

    @Test
    void normalizesNamesMetricsDuplicatesAndSelection() {
        CompoundTag source = record(
                "00000000-0000-0000-0000-000000000001",
                "  Archivist\u0007  ");
        source.putFloat("width", Float.NaN);
        source.putFloat("height", 99f);
        source.putFloat("max_health", -4f);

        List<CompoundTag> normalized =
                FacelessFormPolicy.normalizeRecords(
                        List.of(source, source.copy()));

        assertEquals(1, normalized.size());
        assertEquals("Archivist",
                FacelessFormPolicy.displayName(normalized.get(0)));
        assertEquals(0.6f, normalized.get(0).getFloat("width"));
        assertEquals(6f, normalized.get(0).getFloat("height"));
        assertEquals(1f, normalized.get(0).getFloat("max_health"));
        assertEquals(0,
                FacelessFormPolicy.normalizeSelection(normalized, 9));
        assertNotSame(source, normalized.get(0));
    }

    @Test
    void capsLedgerAndReplacesSelectedSlotWhenFull() {
        List<CompoundTag> records = new ArrayList<>();
        for (int index = 0; index < FacelessFormPolicy.MAX_FORMS; index++) {
            records.add(record(String.format(
                    "00000000-0000-0000-0000-%012d", index + 1),
                    "Form " + index));
        }
        CompoundTag replacement = record(
                "00000000-0000-0000-0000-000000000099",
                "Replacement");

        FacelessFormPolicy.Selection stored =
                FacelessFormPolicy.store(records, replacement, 3);

        assertEquals(FacelessFormPolicy.MAX_FORMS,
                stored.records().size());
        assertEquals(3, stored.selectedIndex());
        assertEquals("Replacement",
                FacelessFormPolicy.displayName(
                        stored.records().get(3)));
    }

    @Test
    void rejectsMalformedOrBlankIdentityRecords() {
        CompoundTag malformed = record(
                "00000000-0000-0000-0000-000000000011",
                "Unknown");
        malformed.putString("entity_type", "not a resource location");
        CompoundTag blank = record(
                "00000000-0000-0000-0000-000000000012",
                " ");

        assertFalse(FacelessFormPolicy.isValid(malformed));
        assertFalse(FacelessFormPolicy.isValid(blank));
        assertTrue(FacelessFormPolicy.isValid(record(
                "00000000-0000-0000-0000-000000000013",
                "Villager")));
    }

    private static CompoundTag record(
            String recordUuid, String displayName) {
        CompoundTag record = new CompoundTag();
        record.putUUID("record_uuid", UUID.fromString(recordUuid));
        record.putString("entity_type", "minecraft:villager");
        record.putString("display_name", displayName);
        record.putBoolean("player_form", false);
        record.putFloat("width", 0.6f);
        record.putFloat("height", 1.95f);
        record.putFloat("max_health", 20f);
        return record;
    }
}
