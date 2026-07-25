package top.aurora.lordofmysteries.ability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class M3TravelNetworkLogicTest {

    @Test
    void relayCostIsBoundedToThreePassengers() {
        assertEquals(80f, M3TravelNetworkLogic.relayCost(-1));
        assertEquals(80f, M3TravelNetworkLogic.relayCost(0));
        assertEquals(90f, M3TravelNetworkLogic.relayCost(1));
        assertEquals(110f, M3TravelNetworkLogic.relayCost(3));
        assertEquals(110f, M3TravelNetworkLogic.relayCost(8));
    }

    @Test
    void joiningRequiresExplicitNearbyConsentAndMatchingMarker() {
        assertTrue(M3TravelNetworkLogic.canJoinRelay(
                false, true, true, false, false,
                true, true, true, 16d));
        assertFalse(M3TravelNetworkLogic.canJoinRelay(
                false, true, true, false, false,
                false, true, true, 1d));
        assertFalse(M3TravelNetworkLogic.canJoinRelay(
                false, true, true, false, false,
                true, false, true, 1d));
        assertFalse(M3TravelNetworkLogic.canJoinRelay(
                false, true, true, false, false,
                true, true, true, 16.01d));
    }

    @Test
    void leaderAndUnsafePlayerStatesCanNeverBecomePassengers() {
        assertFalse(M3TravelNetworkLogic.canJoinRelay(
                true, true, true, false, false,
                true, true, true, 1d));
        assertFalse(M3TravelNetworkLogic.canJoinRelay(
                false, false, true, false, false,
                true, true, true, 1d));
        assertFalse(M3TravelNetworkLogic.canJoinRelay(
                false, true, false, false, false,
                true, true, true, 1d));
        assertFalse(M3TravelNetworkLogic.canJoinRelay(
                false, true, true, true, false,
                true, true, true, 1d));
        assertFalse(M3TravelNetworkLogic.canJoinRelay(
                false, true, true, false, true,
                true, true, true, 1d));
        assertFalse(M3TravelNetworkLogic.canJoinRelay(
                false, true, true, false, false,
                true, true, false, 1d));
    }

    @Test
    void invalidDistanceCannotBypassRelayRadius() {
        assertFalse(M3TravelNetworkLogic.canJoinRelay(
                false, true, true, false, false,
                true, true, true, Double.NaN));
        assertFalse(M3TravelNetworkLogic.canJoinRelay(
                false, true, true, false, false,
                true, true, true, Double.POSITIVE_INFINITY));
    }

    @Test
    void persistentDoorTimingMatchesDesignBoundary() {
        assertEquals(400, M3TravelNetworkLogic.DOOR_DURATION_TICKS);
        assertEquals(40L, M3TravelNetworkLogic.TRANSIT_COOLDOWN_TICKS);
    }
}
