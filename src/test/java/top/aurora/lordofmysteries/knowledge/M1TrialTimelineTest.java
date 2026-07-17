package top.aurora.lordofmysteries.knowledge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class M1TrialTimelineTest {

    @Test
    void acceptsACompleteOnScheduleRun() {
        M1TrialTimeline.Result result = M1TrialTimeline.evaluate(
                9000L, 30000L, 68000L, 100000L,
                112000L, 125000L, 140000L);

        assertEquals(7, result.recordedMilestones());
        assertEquals(7, result.onTimeMilestones());
        assertTrue(result.onSchedule());
    }

    @Test
    void reportsMissingAndLateMilestonesSeparately() {
        M1TrialTimeline.Result result = M1TrialTimeline.evaluate(
                13000L, 35000L, -1L, 112000L,
                119000L, -1L, 150000L);

        assertEquals(5, result.recordedMilestones());
        assertEquals(2, result.onTimeMilestones());
        assertFalse(result.sequence8().recorded());
        assertFalse(result.onSchedule());
    }

    @Test
    void targetThresholdsAreInclusive() {
        M1TrialTimeline.Result result = M1TrialTimeline.evaluate(
                M1TrialTimeline.CAMP_TARGET_TICKS,
                M1TrialTimeline.SEQUENCE_9_TARGET_TICKS,
                M1TrialTimeline.SEQUENCE_8_TARGET_TICKS,
                M1TrialTimeline.SEQUENCE_7_TARGET_TICKS,
                M1TrialTimeline.IDENTITY_TARGET_TICKS,
                M1TrialTimeline.REFLECTION_TARGET_TICKS,
                M1TrialTimeline.STREET_LIFE_TARGET_TICKS);

        assertTrue(result.onSchedule());
    }
}
