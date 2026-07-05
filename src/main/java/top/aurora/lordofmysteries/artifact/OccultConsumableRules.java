package top.aurora.lordofmysteries.artifact;

public final class OccultConsumableRules {

    public static final float INCENSE_PRESSURE_REDUCTION = 18f;
    public static final float INCENSE_POLLUTION_COST = 2f;

    private OccultConsumableRules() {}

    public static Result applyCalmingIncense(float pressure, float pollution) {
        return new Result(
                clamp(pressure - INCENSE_PRESSURE_REDUCTION),
                clamp(pollution + INCENSE_POLLUTION_COST));
    }

    public static boolean canUseCalmingIncense(float pressure) {
        return pressure > 0f;
    }

    private static float clamp(float value) {
        return Math.max(0f, Math.min(100f, value));
    }

    public record Result(float pressure, float pollution) {}
}
