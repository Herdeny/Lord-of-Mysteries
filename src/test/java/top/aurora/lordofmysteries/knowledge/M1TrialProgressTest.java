package top.aurora.lordofmysteries.knowledge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class M1TrialProgressTest {

    @Test
    void fullVerticalSlicePassesAllGoals() {
        M1TrialProgress.Result result = M1TrialProgress.evaluate(
                72000L, true, 7, 3, 2, 40f, 12f);
        assertTrue(result.passed());
        assertEquals(6, result.completedGoals());
    }

    @Test
    void partialRunReportsOnlyCompletedGoals() {
        M1TrialProgress.Result result = M1TrialProgress.evaluate(
                36000L, true, 9, 1, 0, 10f, 5f);
        assertFalse(result.passed());
        assertEquals(1, result.completedGoals());
        assertFalse(result.sequenceComplete());
        assertFalse(result.actingComplete());
    }

    @Test
    void durationFormattingIsStable() {
        assertEquals("01:01:01", M1TrialProgress.formatDuration(73220L));
        assertEquals("00:00:00", M1TrialProgress.formatDuration(-20L));
    }
}
