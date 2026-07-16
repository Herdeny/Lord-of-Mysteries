package top.aurora.lordofmysteries.network;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class C2SRequestGateTest {

    @Test
    void repeatedPacketInsideWindowIsRejected() {
        Map<Integer, Long> ticks = new HashMap<>();
        assertTrue(C2SRequestGate.allow(ticks, 2, 100L, 10L));
        assertFalse(C2SRequestGate.allow(ticks, 2, 109L, 10L));
        assertTrue(C2SRequestGate.allow(ticks, 2, 110L, 10L));
    }

    @Test
    void packetTypesHaveIndependentWindows() {
        Map<Integer, Long> ticks = new HashMap<>();
        assertTrue(C2SRequestGate.allow(ticks, 1, 100L, 10L));
        assertTrue(C2SRequestGate.allow(ticks, 2, 100L, 10L));
    }

    @Test
    void gameTimeRollbackStartsFreshWindow() {
        Map<Integer, Long> ticks = new HashMap<>();
        assertTrue(C2SRequestGate.allow(ticks, 1, 500L, 10L));
        assertTrue(C2SRequestGate.allow(ticks, 1, 20L, 10L));
    }
}
