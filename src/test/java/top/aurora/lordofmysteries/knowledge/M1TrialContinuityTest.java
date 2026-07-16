package top.aurora.lordofmysteries.knowledge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class M1TrialContinuityTest {

    @Test
    void allContinuityEvidencePasses() {
        M1TrialContinuity.Result result = M1TrialContinuity.evaluate(1, 1, 2, 1);
        assertTrue(result.passed());
        assertEquals(4, result.completedGoals());
    }

    @Test
    void oneWayDimensionTravelDoesNotPassRoundTrip() {
        M1TrialContinuity.Result result = M1TrialContinuity.evaluate(1, 1, 1, 1);
        assertFalse(result.passed());
        assertFalse(result.dimensionComplete());
        assertEquals(3, result.completedGoals());
    }

    @Test
    void emptyRunHasNoContinuityEvidence() {
        M1TrialContinuity.Result result = M1TrialContinuity.evaluate(0, 0, 0, 0);
        assertFalse(result.passed());
        assertEquals(0, result.completedGoals());
    }
}
