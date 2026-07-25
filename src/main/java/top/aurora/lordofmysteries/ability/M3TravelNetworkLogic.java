package top.aurora.lordofmysteries.ability;

public final class M3TravelNetworkLogic {

    public static final int MAX_PASSENGERS = 3;
    public static final float BASE_SPIRITUALITY_COST = 80f;
    public static final float PASSENGER_SPIRITUALITY_COST = 10f;
    public static final double CONSENT_RADIUS = 4d;
    public static final int DOOR_DURATION_TICKS = 400;
    public static final long TRANSIT_COOLDOWN_TICKS = 40L;

    private M3TravelNetworkLogic() {}

    public static float relayCost(int passengerCount) {
        int boundedPassengers = Math.max(
                0, Math.min(MAX_PASSENGERS, passengerCount));
        return BASE_SPIRITUALITY_COST
                + boundedPassengers * PASSENGER_SPIRITUALITY_COST;
    }

    public static boolean canJoinRelay(
            boolean leader,
            boolean sameSourceLevel,
            boolean alive,
            boolean spectator,
            boolean sleeping,
            boolean sneaking,
            boolean matchingMarker,
            boolean cooldownReady,
            double distanceSquared) {
        return !leader
                && sameSourceLevel
                && alive
                && !spectator
                && !sleeping
                && sneaking
                && matchingMarker
                && cooldownReady
                && Double.isFinite(distanceSquared)
                && distanceSquared <= CONSENT_RADIUS * CONSENT_RADIUS;
    }
}
