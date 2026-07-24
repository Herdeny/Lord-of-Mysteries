package top.aurora.lordofmysteries.commission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.minecraft.nbt.CompoundTag;

class DynamicCaseHistoryEntryTest {

    @Test
    void entrySurvivesNbtRoundTrip() {
        DynamicCaseHistoryEntry entry = entry(
                14L, "case-14", CaseGrade.A,
                DynamicCaseHistoryEntry.FollowUpStatus.PENDING);

        CompoundTag saved = entry.save();

        assertTrue(DynamicCaseHistoryEntry.isValid(saved));
        assertEquals(entry, DynamicCaseHistoryEntry.load(saved));
    }

    @Test
    void invalidWeekAndGradeAreRejected() {
        CompoundTag invalidWeek = entry(
                14L, "case-14", CaseGrade.A,
                DynamicCaseHistoryEntry.FollowUpStatus.PENDING).save();
        invalidWeek.putLong("case_week", 5L);
        assertFalse(DynamicCaseHistoryEntry.isValid(invalidWeek));

        assertThrows(IllegalArgumentException.class, () ->
                new DynamicCaseHistoryEntry(
                        14L, 2L, "case-14",
                        DynamicCaseProfile.Archetype.MISSING_PERSON,
                        DynamicCaseProfile.Subject.APPRENTICE_REPORTER,
                        DynamicCaseProfile.Organization.MIST_CITY_PRESS,
                        DynamicCaseProfile.CaseLocation.MIST_CITY_OUTPOST,
                        CaseGrade.S, 84, 800L, 2,
                        DynamicCaseHistoryEntry.FollowUpStatus.PENDING));
    }

    static DynamicCaseHistoryEntry entry(
            long caseDay,
            String instanceId,
            CaseGrade grade,
            DynamicCaseHistoryEntry.FollowUpStatus status) {
        int score = switch (grade) {
            case S -> 94;
            case A -> 84;
            case B -> 72;
            case C -> 56;
            case D -> 42;
        };
        return new DynamicCaseHistoryEntry(
                caseDay, Math.floorDiv(caseDay, 7L), instanceId,
                DynamicCaseProfile.Archetype.MISSING_PERSON,
                DynamicCaseProfile.Subject.APPRENTICE_REPORTER,
                DynamicCaseProfile.Organization.MIST_CITY_PRESS,
                DynamicCaseProfile.CaseLocation.MIST_CITY_OUTPOST,
                grade, score, 800L + caseDay, 2, status);
    }
}
