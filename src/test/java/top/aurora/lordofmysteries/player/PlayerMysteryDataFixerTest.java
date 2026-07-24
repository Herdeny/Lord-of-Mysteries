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
import top.aurora.lordofmysteries.commission.CaseGrade;
import top.aurora.lordofmysteries.commission.DynamicCaseHistoryEntry;
import top.aurora.lordofmysteries.commission.DynamicCaseProfile;
import top.aurora.lordofmysteries.commission.DynamicCaseResponseBranch;
import top.aurora.lordofmysteries.commission.DynamicCaseResponseTask;
import top.aurora.lordofmysteries.commission.DynamicCaseWeeklyDirective;

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
        assertEquals(8, result.appliedSteps().size());
        assertEquals("characteristic_bundle_upgrade",
                result.appliedSteps().get(0).id());
        assertEquals("m1_vertical_slice_state",
                result.appliedSteps().get(1).id());
        assertEquals("case_debrief_archive",
                result.appliedSteps().get(2).id());
        assertEquals("case_hypothesis_workspace",
                result.appliedSteps().get(3).id());
        assertEquals("dynamic_case_history",
                result.appliedSteps().get(4).id());
        assertEquals("organization_response_state",
                result.appliedSteps().get(5).id());
        assertEquals("contact_memory_and_response_branches",
                result.appliedSteps().get(6).id());
        assertEquals("city_economy_and_exposure",
                result.appliedSteps().get(7).id());
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

        assertEquals(9, result.appliedSteps().size());
        assertEquals("legacy_key_normalization",
                result.appliedSteps().get(0).id());
        assertEquals("characteristic_bundle_upgrade",
                result.appliedSteps().get(1).id());
        assertEquals("m1_vertical_slice_state",
                result.appliedSteps().get(2).id());
        assertEquals("case_debrief_archive",
                result.appliedSteps().get(3).id());
        assertEquals("case_hypothesis_workspace",
                result.appliedSteps().get(4).id());
        assertEquals("dynamic_case_history",
                result.appliedSteps().get(5).id());
        assertEquals("organization_response_state",
                result.appliedSteps().get(6).id());
        assertEquals("contact_memory_and_response_branches",
                result.appliedSteps().get(7).id());
        assertEquals("city_economy_and_exposure",
                result.appliedSteps().get(8).id());
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

        assertEquals(6, result.appliedSteps().size());
        assertEquals("case_debrief_archive", result.appliedSteps().get(0).id());
        assertEquals("case_hypothesis_workspace",
                result.appliedSteps().get(1).id());
        assertEquals("dynamic_case_history",
                result.appliedSteps().get(2).id());
        assertEquals("organization_response_state",
                result.appliedSteps().get(3).id());
        assertEquals("contact_memory_and_response_branches",
                result.appliedSteps().get(4).id());
        assertEquals("city_economy_and_exposure",
                result.appliedSteps().get(5).id());
        assertTrue(result.data().contains("case_debriefs", Tag.TAG_COMPOUND));
        assertTrue(result.data().contains("case_hypotheses", Tag.TAG_COMPOUND));
        assertEquals("lord_of_mysteries:commission/lost_cat",
                result.data().getList("completed_commissions", Tag.TAG_STRING)
                        .getString(0));
    }

    @Test
    void migratesSchemaEighteenWithEmptyHypothesisWorkspace() {
        CompoundTag previous = new CompoundTag();
        previous.putInt("schema_version", 18);
        previous.put("case_debriefs", new CompoundTag());

        PlayerMysteryDataFixer.MigrationResult result =
                PlayerMysteryDataFixer.migrate(previous);

        assertEquals(5, result.appliedSteps().size());
        assertEquals("case_hypothesis_workspace",
                result.appliedSteps().get(0).id());
        assertEquals("dynamic_case_history",
                result.appliedSteps().get(1).id());
        assertEquals("organization_response_state",
                result.appliedSteps().get(2).id());
        assertEquals("contact_memory_and_response_branches",
                result.appliedSteps().get(3).id());
        assertEquals("city_economy_and_exposure",
                result.appliedSteps().get(4).id());
        assertTrue(result.data().contains("case_hypotheses", Tag.TAG_COMPOUND));
        assertTrue(result.data().contains("case_debriefs", Tag.TAG_COMPOUND));
    }

    @Test
    void migratesSchemaNineteenWithEmptyDynamicCaseHistory() {
        CompoundTag previous = new CompoundTag();
        previous.putInt("schema_version", 19);
        previous.put("case_debriefs", new CompoundTag());
        previous.put("case_hypotheses", new CompoundTag());

        PlayerMysteryDataFixer.MigrationResult result =
                PlayerMysteryDataFixer.migrate(previous);

        assertEquals(4, result.appliedSteps().size());
        assertEquals("dynamic_case_history",
                result.appliedSteps().get(0).id());
        assertEquals("organization_response_state",
                result.appliedSteps().get(1).id());
        assertEquals("contact_memory_and_response_branches",
                result.appliedSteps().get(2).id());
        assertEquals("city_economy_and_exposure",
                result.appliedSteps().get(3).id());
        assertTrue(result.data().contains(
                "dynamic_case_history", Tag.TAG_LIST));
        assertTrue(result.data().getList(
                "dynamic_case_history", Tag.TAG_COMPOUND).isEmpty());
    }

    @Test
    void migratesSchemaTwentyWithEmptyOrganizationResponseState() {
        CompoundTag previous = new CompoundTag();
        previous.putInt("schema_version", 20);
        previous.put("dynamic_case_history", new ListTag());

        PlayerMysteryDataFixer.MigrationResult result =
                PlayerMysteryDataFixer.migrate(previous);

        assertEquals(3, result.appliedSteps().size());
        assertEquals("organization_response_state",
                result.appliedSteps().get(0).id());
        assertEquals("contact_memory_and_response_branches",
                result.appliedSteps().get(1).id());
        assertEquals("city_economy_and_exposure",
                result.appliedSteps().get(2).id());
        assertTrue(result.data().contains(
                "dynamic_case_contact_standings", Tag.TAG_COMPOUND));
        assertTrue(result.data().getCompound(
                "dynamic_case_contact_standings").isEmpty());
        assertTrue(result.data().contains(
                "organization_response_task", Tag.TAG_COMPOUND));
        assertTrue(result.data().getCompound(
                "organization_response_task").isEmpty());
        assertTrue(result.data().contains(
                "dynamic_case_contact_events", Tag.TAG_LIST));
    }

    @Test
    void migratesSchemaTwentyOneHistoryAndActiveTaskIntoMemoryBranches() {
        CompoundTag previous = new CompoundTag();
        previous.putInt("schema_version", 21);
        DynamicCaseHistoryEntry history = new DynamicCaseHistoryEntry(
                14L, 2L, "schema-21-response",
                DynamicCaseProfile.Archetype.MISSING_PERSON,
                DynamicCaseProfile.Subject.APPRENTICE_REPORTER,
                DynamicCaseProfile.Organization.MIST_CITY_PRESS,
                DynamicCaseProfile.CaseLocation.MIST_CITY_OUTPOST,
                CaseGrade.A, 84, 900L, 2,
                DynamicCaseHistoryEntry.FollowUpStatus.CLAIMED);
        ListTag historyTag = new ListTag();
        historyTag.add(history.save());
        previous.put("dynamic_case_history", historyTag);
        previous.put("dynamic_case_contact_standings",
                new CompoundTag());
        DynamicCaseResponseTask task = new DynamicCaseResponseTask(
                history.instanceId(),
                history.organization(),
                history.subject(),
                DynamicCaseWeeklyDirective.SOURCE_VERIFICATION,
                14L, 17L,
                DynamicCaseResponseTask.Stage.BRIEFED);
        CompoundTag legacyTask = task.save();
        legacyTask.remove("branch");
        previous.put("organization_response_task", legacyTask);

        PlayerMysteryDataFixer.MigrationResult result =
                PlayerMysteryDataFixer.migrate(previous);

        assertEquals(2, result.appliedSteps().size());
        assertEquals("contact_memory_and_response_branches",
                result.appliedSteps().get(0).id());
        assertEquals("city_economy_and_exposure",
                result.appliedSteps().get(1).id());
        assertEquals(1, result.data().getList(
                "dynamic_case_contact_events", Tag.TAG_COMPOUND).size());
        CompoundTag migratedTask =
                result.data().getCompound("organization_response_task");
        assertEquals(DynamicCaseResponseBranch.ROUTINE.id(),
                migratedTask.getString("branch"));
        assertTrue(DynamicCaseResponseTask.isValid(migratedTask));
    }

    @Test
    void migratesSchemaTwentyTwoWithCityEconomyDefaults() {
        CompoundTag previous = new CompoundTag();
        previous.putInt("schema_version", 22);
        previous.putInt("city_work_shifts", 9);
        previous.putLong("last_city_work_day", 42L);

        PlayerMysteryDataFixer.MigrationResult result =
                PlayerMysteryDataFixer.migrate(previous);

        assertEquals(1, result.appliedSteps().size());
        assertEquals("city_economy_and_exposure",
                result.appliedSteps().get(0).id());
        assertEquals(0, result.data().getInt("press_work_shifts"));
        assertEquals(0, result.data().getInt("agency_work_shifts"));
        assertEquals(0, result.data().getInt("patrol_work_shifts"));
        assertEquals(0f, result.data().getFloat("mystical_exposure"));
        assertEquals(9, result.data().getInt("city_work_shifts"));
        assertEquals(42L, result.data().getLong("last_city_work_day"));
        assertEquals(PlayerMysteryData.CURRENT_SCHEMA_VERSION,
                result.data().getInt("schema_version"));
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
