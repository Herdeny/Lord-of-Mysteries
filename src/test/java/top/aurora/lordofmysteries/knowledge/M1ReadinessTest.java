package top.aurora.lordofmysteries.knowledge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class M1ReadinessTest {

    @Test
    void reportsEachVerticalSliceGate() {
        assertEquals(M1Readiness.Stage.COMMONER,
                M1Readiness.evaluate(null, -1, 0f));
        assertEquals(M1Readiness.Stage.SEER_9_DIGESTING,
                M1Readiness.evaluate("lord_of_mysteries:seer", 9, 99f));
        assertEquals(M1Readiness.Stage.READY_FOR_8,
                M1Readiness.evaluate("lord_of_mysteries:seer", 9, 100f));
        assertEquals(M1Readiness.Stage.SEER_8_DIGESTING,
                M1Readiness.evaluate("lord_of_mysteries:seer", 8, 20f));
        assertEquals(M1Readiness.Stage.READY_FOR_7,
                M1Readiness.evaluate("lord_of_mysteries:seer", 8, 100f));
        assertEquals(M1Readiness.Stage.SEER_7_REACHED,
                M1Readiness.evaluate("lord_of_mysteries:seer", 7, 0f));
        assertEquals(M1Readiness.Stage.OTHER_PATHWAY,
                M1Readiness.evaluate("lord_of_mysteries:hunter", 8, 100f));
    }
}
