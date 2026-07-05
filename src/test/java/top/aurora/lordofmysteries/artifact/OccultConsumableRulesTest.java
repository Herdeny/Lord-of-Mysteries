package top.aurora.lordofmysteries.artifact;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class OccultConsumableRulesTest {

    @Test
    void incenseTradesPressureForPollution() {
        OccultConsumableRules.Result result =
                OccultConsumableRules.applyCalmingIncense(40f, 12f);
        assertEquals(22f, result.pressure());
        assertEquals(14f, result.pollution());
    }

    @Test
    void incenseClampsBothMeters() {
        OccultConsumableRules.Result result =
                OccultConsumableRules.applyCalmingIncense(5f, 99f);
        assertEquals(0f, result.pressure());
        assertEquals(100f, result.pollution());
    }

    @Test
    void incenseRequiresExistingPressure() {
        assertFalse(OccultConsumableRules.canUseCalmingIncense(0f));
        assertTrue(OccultConsumableRules.canUseCalmingIncense(0.1f));
    }
}
