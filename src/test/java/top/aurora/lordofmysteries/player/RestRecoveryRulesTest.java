package top.aurora.lordofmysteries.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RestRecoveryRulesTest {

    @Test
    void fullSleepReducesPressureByTwenty() {
        assertEquals(35f, RestRecoveryRules.pressureAfterRest(55f));
    }

    @Test
    void recoveryDoesNotGoBelowZero() {
        assertEquals(0f, RestRecoveryRules.pressureAfterRest(8f));
    }

    @Test
    void recoveryIsLimitedToOncePerDay() {
        assertTrue(RestRecoveryRules.canRecover(4L, 3L, 20f));
        assertFalse(RestRecoveryRules.canRecover(4L, 4L, 20f));
        assertFalse(RestRecoveryRules.canRecover(4L, 3L, 0f));
    }
}
