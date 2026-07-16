package top.aurora.lordofmysteries.knowledge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class M1TrialTimelineTest {

    @Test
    void acceptsACompleteOnScheduleRun() {
        M1TrialTimeline.Result result = M1TrialTimeline.evaluate(
                9000L, 24000L, 48000L, 70000L);

        assertEquals(4, result.recordedMilestones());
        assertEquals(4, result.onTimeMilestones());
        assertTrue(result.onSchedule());
    }

    @Test
    void reportsMissingAndLateMilestonesSeparately() {
        M1TrialTimeline.Result result = M1TrialTimeline.evaluate(
                13000L, 28000L, -1L, 76000L);

        assertEquals(3, result.recordedMilestones());
        assertEquals(1, result.onTimeMilestones());
        assertFalse(result.sequence8().recorded());
        assertFalse(result.onSchedule());
    }

    @Test
    void targetThresholdsAreInclusive() {
        M1TrialTimeline.Result result = M1TrialTimeline.evaluate(
                M1TrialTimeline.CAMP_TARGET_TICKS,
                M1TrialTimeline.SEQUENCE_9_TARGET_TICKS,
                M1TrialTimeline.SEQUENCE_8_TARGET_TICKS,
                M1TrialTimeline.SEQUENCE_7_TARGET_TICKS);

        assertTrue(result.onSchedule());
    }
}
