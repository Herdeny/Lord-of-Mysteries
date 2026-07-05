package top.aurora.lordofmysteries.player;

public final class RestRecoveryRules {

    public static final float PRESSURE_RECOVERY = 20f;

    private RestRecoveryRules() {}

    public static float pressureAfterRest(float pressure) {
        return Math.max(0f, Math.min(100f, pressure) - PRESSURE_RECOVERY);
    }

    public static boolean canRecover(long currentDay, long lastRecoveryDay, float pressure) {
        return pressure > 0f && currentDay != lastRecoveryDay;
    }
}
