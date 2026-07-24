package top.aurora.lordofmysteries.commission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class DynamicCaseRelationshipPolicyTest {

    @Test
    void caseGradesApplyBoundedContactConsequences() {
        Map<DynamicCaseProfile.Subject, Integer> standings =
                new HashMap<>();
        DynamicCaseProfile.Subject contact =
                DynamicCaseProfile.Subject.APPRENTICE_REPORTER;

        assertEquals(3,
                DynamicCaseRelationshipPolicy.recordCaseResult(
                        standings, contact, CaseGrade.S));
        assertEquals(1,
                DynamicCaseRelationshipPolicy.recordCaseResult(
                        standings, contact, CaseGrade.D));
    }

    @Test
    void adjustmentClampsAndDropsNeutralSparseEntries() {
        Map<DynamicCaseProfile.Subject, Integer> standings =
                new HashMap<>();
        DynamicCaseProfile.Subject contact =
                DynamicCaseProfile.Subject.RETIRED_CONSTABLE;

        assertEquals(DynamicCaseRelationshipPolicy.MAX_STANDING,
                DynamicCaseRelationshipPolicy.adjust(
                        standings, contact, Integer.MAX_VALUE));
        assertEquals(0,
                DynamicCaseRelationshipPolicy.adjust(
                        standings, contact,
                        -DynamicCaseRelationshipPolicy.MAX_STANDING));
        assertFalse(standings.containsKey(contact));
    }

    @Test
    void sanitizerRemovesInvalidEntriesAndClampsValues() {
        Map<DynamicCaseProfile.Subject, Integer> standings =
                new HashMap<>();
        standings.put(
                DynamicCaseProfile.Subject.DOCK_ACCOUNTANT, 99);
        standings.put(
                DynamicCaseProfile.Subject.HERBALIST_ASSISTANT, 0);
        standings.put(null, 4);

        assertEquals(3,
                DynamicCaseRelationshipPolicy.sanitize(standings));
        assertEquals(Map.of(
                        DynamicCaseProfile.Subject.DOCK_ACCOUNTANT,
                        DynamicCaseRelationshipPolicy.MAX_STANDING),
                standings);
    }

    @Test
    void attitudeThresholdsRemainStable() {
        assertEquals(DynamicCaseRelationshipPolicy.Attitude.HOSTILE,
                DynamicCaseRelationshipPolicy.attitude(-8));
        assertEquals(DynamicCaseRelationshipPolicy.Attitude.WARY,
                DynamicCaseRelationshipPolicy.attitude(-3));
        assertEquals(DynamicCaseRelationshipPolicy.Attitude.NEUTRAL,
                DynamicCaseRelationshipPolicy.attitude(2));
        assertEquals(DynamicCaseRelationshipPolicy.Attitude.TRUSTING,
                DynamicCaseRelationshipPolicy.attitude(7));
        assertEquals(DynamicCaseRelationshipPolicy.Attitude.ALLIED,
                DynamicCaseRelationshipPolicy.attitude(8));
    }
}
