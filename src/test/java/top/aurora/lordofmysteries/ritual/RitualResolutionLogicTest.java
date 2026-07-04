package top.aurora.lordofmysteries.ritual;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RitualResolutionLogicTest {

    @Test
    void completeRitualWithQualifiedLeaderReachesPerfectScore() {
        assertEquals(1f, RitualResolutionLogic.completionScore(
                true, true, 1f, true), 0.0001f);
    }

    @Test
    void ordinaryLeaderCanStillCompleteExistingCalmSeal() {
        assertEquals(0.9f, RitualResolutionLogic.completionScore(
                true, true, 1f, false), 0.0001f);
        assertEquals(RitualResolutionLogic.Outcome.SUCCESS,
                RitualResolutionLogic.resolve(0.9f, 0f));
    }

    @Test
    void resolutionUsesAllFiveOutcomeBands() {
        assertEquals(RitualResolutionLogic.Outcome.PERFECT,
                RitualResolutionLogic.resolve(0.95f, 0f));
        assertEquals(RitualResolutionLogic.Outcome.SUCCESS,
                RitualResolutionLogic.resolve(0.80f, 0f));
        assertEquals(RitualResolutionLogic.Outcome.FAILURE,
                RitualResolutionLogic.resolve(0.50f, 0f));
        assertEquals(RitualResolutionLogic.Outcome.SEVERE_FAILURE,
                RitualResolutionLogic.resolve(0.20f, 0f));
        assertEquals(RitualResolutionLogic.Outcome.CATASTROPHE,
                RitualResolutionLogic.resolve(0.19f, 0f));
    }

    @Test
    void randomDeltaCannotPushScoreOutsideBounds() {
        assertEquals(RitualResolutionLogic.Outcome.PERFECT,
                RitualResolutionLogic.resolve(1f, 10f));
        assertEquals(RitualResolutionLogic.Outcome.CATASTROPHE,
                RitualResolutionLogic.resolve(0f, -10f));
    }

    @Test
    void ordinaryFailureUsesConfiguredFiveThreeTwoRiskBands() {
        assertEquals(RitualResolutionLogic.Outcome.FAILURE,
                RitualResolutionLogic.escalateFailure(
                        RitualResolutionLogic.Outcome.FAILURE, 0.49f));
        assertEquals(RitualResolutionLogic.Outcome.SEVERE_FAILURE,
                RitualResolutionLogic.escalateFailure(
                        RitualResolutionLogic.Outcome.FAILURE, 0.5f));
        assertEquals(RitualResolutionLogic.Outcome.CATASTROPHE,
                RitualResolutionLogic.escalateFailure(
                        RitualResolutionLogic.Outcome.FAILURE, 0.8f));
        assertEquals(RitualResolutionLogic.Outcome.SUCCESS,
                RitualResolutionLogic.escalateFailure(
                        RitualResolutionLogic.Outcome.SUCCESS, 1f));
    }
}
