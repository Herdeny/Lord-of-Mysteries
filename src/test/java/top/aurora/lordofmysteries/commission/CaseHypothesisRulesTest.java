package top.aurora.lordofmysteries.commission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.minecraft.nbt.CompoundTag;

import top.aurora.lordofmysteries.player.PlayerMysteryData;

class CaseHypothesisRulesTest {

    @Test
    void proposalSanitizesPlayerTextAndRoundTrips() {
        CaseHypothesisRecord record = CaseHypothesisRecord.EMPTY.propose(
                "clue_synthesis", CaseHypothesisStance.CONTRADICTS,
                "  Ink\n\u00a7c conflicts\twith registry.  ");

        assertEquals("Ink conflicts with registry.", record.note());
        assertTrue(record.hasDraft());
        assertEquals(record, CaseHypothesisRecord.load(record.save()));
        assertTrue(CaseHypothesisRecord.isValid(record.save()));
    }

    @Test
    void wrongTestAddsRecoverableStrainAndPressureCost() {
        CaseHypothesisRecord record = CaseHypothesisRecord.EMPTY.propose(
                "record_to_tracks", CaseHypothesisStance.CONTRADICTS,
                "The desk record contradicts the tracks.");

        CaseHypothesisRules.TestResult result = CaseHypothesisRules.test(
                record, EvidenceRelationKind.LEADS_TO, 1_000L);

        assertFalse(result.supported());
        assertEquals(CaseHypothesisStatus.REJECTED, result.record().status());
        assertEquals(1, result.record().unresolvedStrain());
        assertEquals(1, result.record().failedTests());
        assertEquals(CaseHypothesisRules.WRONG_TEST_PRESSURE,
                result.pressureCost());
    }

    @Test
    void correctedHypothesisRepairsOneStrain() {
        CaseHypothesisRecord wrong = CaseHypothesisRules.test(
                CaseHypothesisRecord.EMPTY.propose(
                        "record_to_tracks", CaseHypothesisStance.CONTRADICTS,
                        "The clues conflict."),
                EvidenceRelationKind.LEADS_TO, 1_000L).record();
        CaseHypothesisRecord corrected = wrong.propose(
                "record_to_tracks", CaseHypothesisStance.LEADS_TO,
                "The desk record leads to the camp tracks.");

        CaseHypothesisRules.TestResult result = CaseHypothesisRules.test(
                corrected, EvidenceRelationKind.LEADS_TO, 1_200L);

        assertTrue(result.supported());
        assertEquals(CaseHypothesisStatus.SUPPORTED, result.record().status());
        assertEquals(0, result.record().unresolvedStrain());
        assertEquals(1, result.record().successfulTests());
        assertEquals(0, result.pressureCost());
    }

    @Test
    void testCooldownPreventsRapidGuessing() {
        CaseHypothesisRecord tested = CaseHypothesisRules.test(
                CaseHypothesisRecord.EMPTY.propose(
                        "record_to_tracks", CaseHypothesisStance.LEADS_TO,
                        "The record leads to the tracks."),
                EvidenceRelationKind.LEADS_TO, 1_000L).record();

        assertEquals(1L, CaseHypothesisRules.testCooldownRemaining(
                tested, 1_199L));
        assertThrows(IllegalStateException.class, () ->
                CaseHypothesisRules.test(
                        tested, EvidenceRelationKind.LEADS_TO, 1_199L));
    }

    @Test
    void boardReconsiderationClearsDraftAndOneStrainAfterCooldown() {
        CaseHypothesisRecord rejected = CaseHypothesisRules.test(
                CaseHypothesisRecord.EMPTY.propose(
                        "record_to_tracks", CaseHypothesisStance.SUPPORTS,
                        "The record directly proves the recovery."),
                EvidenceRelationKind.LEADS_TO, 1_000L).record();

        assertThrows(IllegalStateException.class, () ->
                CaseHypothesisRules.reconsider(rejected, 1_599L));
        CaseHypothesisRecord reconsidered = CaseHypothesisRules.reconsider(
                rejected, 1_600L);

        assertFalse(reconsidered.hasDraft());
        assertEquals(0, reconsidered.unresolvedStrain());
        assertEquals(1, reconsidered.failedTests());
    }

    @Test
    void invalidStoredRecordIsRejected() {
        CompoundTag invalid = CaseHypothesisRecord.EMPTY.save();
        invalid.remove("stance");

        assertFalse(CaseHypothesisRecord.isValid(invalid));
    }

    @Test
    void playerDataPersistsAndCopiesHypothesisWorkspace() {
        PlayerMysteryData source = new PlayerMysteryData();
        CaseHypothesisRecord record = CaseHypothesisRecord.EMPTY.propose(
                "clue_synthesis", CaseHypothesisStance.CONTRADICTS,
                "The dossier contains conflicting marks.");
        source.caseHypotheses.put(CommissionService.COUNTERFEIT_FORMULA, record);

        PlayerMysteryData restored = new PlayerMysteryData();
        restored.load(source.save());
        PlayerMysteryData copied = new PlayerMysteryData();
        copied.copyFrom(restored);

        assertEquals(record, restored.caseHypotheses.get(
                CommissionService.COUNTERFEIT_FORMULA));
        assertEquals(restored.caseHypotheses, copied.caseHypotheses);
    }
}
