package top.aurora.lordofmysteries.spirit;

public final class SpiritExpeditionPolicy {

    public static final long DURATION_TICKS = 36_000L;
    public static final int ROUTE_LEGS = 6;
    public static final int STARTING_STABILITY = 70;
    public static final int MAX_STABILITY = 100;
    public static final int MAX_DRIFT = 100;
    public static final int EXTRACTION_REWARD_THRESHOLD = 6;
    public static final int MAX_REWARD_SCORE = ROUTE_LEGS * 8;

    private SpiritExpeditionPolicy() {}

    public static SpiritWeather weatherFor(
            long routeSeed, SpiritProjection projection) {
        long mixed = mix(routeSeed, projection.ordinal() * 0x9E3779B97F4A7C15L);
        return SpiritWeather.values()[index(mixed, SpiritWeather.values().length)];
    }

    public static int tideFor(long routeSeed) {
        return index(mix(routeSeed, 0x632BE59BD9B4E019L), 8);
    }

    public static SpiritDirection expectedDirection(
            long routeSeed, int step, int tide,
            SpiritProjection projection, SpiritWeather weather) {
        long salt = ((long) Math.max(0, step) << 32)
                ^ ((long) Math.floorMod(tide, 8) << 24)
                ^ ((long) projection.ordinal() << 12)
                ^ weather.ordinal();
        return SpiritDirection.values()[index(
                mix(routeSeed, salt), SpiritDirection.values().length)];
    }

    public static SpiritEncounter encounterFor(
            long routeSeed, int step, SpiritWeather weather) {
        long salt = ((long) Math.max(0, step) << 20)
                ^ ((long) weather.ordinal() << 8)
                ^ 0x51F15EEDL;
        return SpiritEncounter.values()[index(
                mix(routeSeed, salt), SpiritEncounter.values().length)];
    }

    public static NavigationOutcome navigate(
            long routeSeed, int step, int tide,
            SpiritProjection projection, SpiritWeather weather,
            SpiritDirection chosenDirection) {
        SpiritDirection expected = expectedDirection(
                routeSeed, step, tide, projection, weather);
        boolean correct = expected == chosenDirection;
        int risk = weather.risk();
        int stabilityDelta = correct ? 3 : -(7 + risk * 2);
        int driftDelta = correct ? -2 : 9 + risk * 2;
        int rewardDelta = correct ? 1 + Math.max(0, risk - 1) : 0;
        SpiritEncounter encounter = encounterFor(routeSeed, step, weather);
        return new NavigationOutcome(
                correct, expected, encounter,
                stabilityDelta, driftDelta, rewardDelta);
    }

    public static EncounterOutcome resolve(
            SpiritEncounter encounter, SpiritEncounterAction action) {
        boolean correct = encounter != null
                && action != null
                && encounter.preferredAction() == action;
        int risk = encounter == null ? 3 : encounter.risk();
        return new EncounterOutcome(
                correct,
                correct ? 4 : -(4 + risk * 2),
                correct ? -3 : 5 + risk * 2,
                correct ? 2 + risk : 0);
    }

    public static int clampStability(int value) {
        return Math.max(0, Math.min(MAX_STABILITY, value));
    }

    public static int clampDrift(int value) {
        return Math.max(0, Math.min(MAX_DRIFT, value));
    }

    public static int saturatedAdd(int value, int delta) {
        long result = (long) value + delta;
        return (int) Math.max(0L, Math.min(Integer.MAX_VALUE, result));
    }

    public static long safeDeadline(long startedAt) {
        if (startedAt > Long.MAX_VALUE - DURATION_TICKS) {
            return Long.MAX_VALUE;
        }
        return Math.max(0L, startedAt) + DURATION_TICKS;
    }

    public static boolean timedOut(long now, long expiresAt) {
        return Math.max(0L, now) >= Math.max(0L, expiresAt);
    }

    public static long mix(long value, long salt) {
        long mixed = value ^ salt;
        mixed ^= mixed >>> 30;
        mixed *= 0xBF58476D1CE4E5B9L;
        mixed ^= mixed >>> 27;
        mixed *= 0x94D049BB133111EBL;
        return mixed ^ (mixed >>> 31);
    }

    private static int index(long value, int bound) {
        return Math.floorMod(value, bound);
    }

    public record NavigationOutcome(
            boolean correct,
            SpiritDirection expectedDirection,
            SpiritEncounter encounter,
            int stabilityDelta,
            int driftDelta,
            int rewardDelta) {}

    public record EncounterOutcome(
            boolean correct,
            int stabilityDelta,
            int driftDelta,
            int rewardDelta) {}
}
