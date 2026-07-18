package top.aurora.lordofmysteries.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import top.aurora.lordofmysteries.characteristic.CharacteristicBundle;

class PlayerMysteryDataFixerTest {

    @Test
    void migratesSchemaFifteenThroughNamedDataFix() {
        CompoundTag legacy = new CompoundTag();
        legacy.putInt("schema_version", 15);
        legacy.putString("pathway", "lord_of_mysteries:seer");
        legacy.putInt("sequence", 8);
        legacy.putString("potion_quality", "complete");

        PlayerMysteryDataFixer.MigrationResult result =
                PlayerMysteryDataFixer.migrate(legacy);

        assertTrue(result.migrated());
        assertFalse(result.futureSchema());
        assertEquals(15, result.sourceSchema());
        assertEquals(3, result.appliedSteps().size());
        assertEquals("characteristic_bundle_upgrade",
                result.appliedSteps().get(0).id());
        assertEquals("m1_vertical_slice_state",
                result.appliedSteps().get(1).id());
        assertEquals("case_debrief_archive",
                result.appliedSteps().get(2).id());
        assertFalse(result.data().getBoolean("identity_anchored"));
        assertEquals(Long.MIN_VALUE,
                result.data().getLong("last_city_work_day"));
        assertEquals(-1L, result.data().getLong(
                "m1_trial_street_life_completed_tick"));
        assertEquals(PlayerMysteryData.CURRENT_SCHEMA_VERSION,
                result.data().getInt("schema_version"));
        ListTag bundles = result.data().getList(
                "characteristic_bundles", Tag.TAG_COMPOUND);
        assertEquals(1, bundles.size());
        CharacteristicBundle bundle = CharacteristicBundle.load(
                bundles.getCompound(0));
        assertEquals(8, bundle.highestSequence());
        assertEquals("lord_of_mysteries:seer", bundle.pathway().toString());
        assertNotNull(result.backup());
        assertEquals(15, result.backup().getInt("source_schema"));
        assertEquals(15, result.backup().getCompound("payload")
                .getInt("schema_version"));
    }

    @Test
    void normalizesUnversionedLegacyAliasesBeforeV16Upgrade() {
        CompoundTag legacy = new CompoundTag();
        ListTag knowledge = new ListTag();
        knowledge.add(StringTag.valueOf("lord_of_mysteries:legacy/formula"));
        legacy.put("knownKnowledge", knowledge);

        PlayerMysteryDataFixer.MigrationResult result =
                PlayerMysteryDataFixer.migrate(legacy);

        assertEquals(4, result.appliedSteps().size());
        assertEquals("legacy_key_normalization",
                result.appliedSteps().get(0).id());
        assertEquals("characteristic_bundle_upgrade",
                result.appliedSteps().get(1).id());
        assertEquals("m1_vertical_slice_state",
                result.appliedSteps().get(2).id());
        assertEquals("case_debrief_archive",
                result.appliedSteps().get(3).id());
        assertEquals("lord_of_mysteries:legacy/formula",
                result.data().getList("known_knowledge", Tag.TAG_STRING)
                        .getString(0));
    }

    @Test
    void migratesSchemaSeventeenWithEmptyDebriefArchive() {
        CompoundTag previous = new CompoundTag();
        previous.putInt("schema_version", 17);
        ListTag completed = new ListTag();
        completed.add(StringTag.valueOf(
                "lord_of_mysteries:commission/lost_cat"));
        previous.put("completed_commissions", completed);

        PlayerMysteryDataFixer.MigrationResult result =
                PlayerMysteryDataFixer.migrate(previous);

        assertEquals(1, result.appliedSteps().size());
        assertEquals("case_debrief_archive", result.appliedSteps().get(0).id());
        assertTrue(result.data().contains("case_debriefs", Tag.TAG_COMPOUND));
        assertEquals("lord_of_mysteries:commission/lost_cat",
                result.data().getList("completed_commissions", Tag.TAG_STRING)
                        .getString(0));
    }

    @Test
    void futureSchemaIsPreservedWithoutDowngradeFixes() {
        CompoundTag future = new CompoundTag();
        future.putInt("schema_version", 99);
        future.putString("future_only_field", "keep-me");

        PlayerMysteryDataFixer.MigrationResult result =
                PlayerMysteryDataFixer.migrate(future);

        assertTrue(result.futureSchema());
        assertFalse(result.migrated());
        assertEquals(99, result.data().getInt("schema_version"));
        assertEquals(1, result.orphanedEntries().size());
        CompoundTag orphan = result.orphanedEntries().get(0);
        assertEquals("future_schema", orphan.getString("section"));
        assertEquals("keep-me", orphan.getCompound("payload")
                .getString("future_only_field"));
    }
}
