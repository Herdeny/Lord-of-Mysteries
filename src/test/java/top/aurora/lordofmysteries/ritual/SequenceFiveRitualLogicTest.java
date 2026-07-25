package top.aurora.lordofmysteries.ritual;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SequenceFiveRitualLogicTest {

    @Test
    void healthySoloPreparationIsDeterministicallyStable() {
        float stability = SequenceFiveRitualLogic.stability(
                0f, 0f, 0, 0f);

        assertEquals(0.90f, stability, 0.0001f);
        assertEquals(SequenceFiveRitualLogic.Outcome.STABLE_SUCCESS,
                SequenceFiveRitualLogic.resolve(stability, 1f));
    }

    @Test
    void supportersAndRitualResonanceOffsetMentalRisk() {
        assertEquals(0.80f, SequenceFiveRitualLogic.stability(
                60f, 80f, 3, 0.10f), 0.0001f);
        assertEquals(0.80f, SequenceFiveRitualLogic.stability(
                60f, 80f, 99, 99f), 0.0001f);
    }

    @Test
    void riskyPreparationUsesExplicitSuccessAndFailureBands() {
        float stability = SequenceFiveRitualLogic.stability(
                80f, 80f, 0, 0f);

        assertEquals(0.38f, stability, 0.0001f);
        assertEquals(SequenceFiveRitualLogic.Outcome.STRAINED_SUCCESS,
                SequenceFiveRitualLogic.resolve(stability, 0.20f));
        assertEquals(SequenceFiveRitualLogic.Outcome.FAILURE,
                SequenceFiveRitualLogic.resolve(stability, 0.50f));
        assertEquals(SequenceFiveRitualLogic.Outcome.SEVERE_FAILURE,
                SequenceFiveRitualLogic.resolve(stability, 0.90f));
    }

    @Test
    void stabilityInputsAreClamped() {
        assertEquals(1f, SequenceFiveRitualLogic.stability(
                -50f, -50f, 3, 1f), 0.0001f);
        assertEquals(0.25f, SequenceFiveRitualLogic.stability(
                500f, 500f, -2, -5f), 0.0001f);
    }
}
