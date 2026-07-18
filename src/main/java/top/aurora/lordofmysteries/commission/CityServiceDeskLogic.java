package top.aurora.lordofmysteries.commission;

public final class CityServiceDeskLogic {

    public static final long FIELD_KIT_COST = 18L;
    public static final long SAFE_ROOM_COST = 28L;
    public static final float SAFE_ROOM_PRESSURE_RECOVERY = 20f;
    public static final float SAFE_ROOM_POLLUTION_RECOVERY = 4f;

    private CityServiceDeskLogic() {}

    public static TransactionStatus purchase(long balance, long cost) {
        return balance >= cost
                ? TransactionStatus.SUCCESS
                : TransactionStatus.INSUFFICIENT_FUNDS;
    }

    public static SafeRoomResult requestSafeRoom(
            long balance, float pressure, float pollution) {
        float normalizedPressure = clamp(pressure, 0f, 100f);
        float normalizedPollution = clamp(pollution, 0f, 100f);
        if (normalizedPressure <= 0f && normalizedPollution <= 0f) {
            return new SafeRoomResult(
                    TransactionStatus.NOT_NEEDED, balance,
                    normalizedPressure, normalizedPollution, 0f, 0f);
        }
        if (balance < SAFE_ROOM_COST) {
            return new SafeRoomResult(
                    TransactionStatus.INSUFFICIENT_FUNDS, balance,
                    normalizedPressure, normalizedPollution, 0f, 0f);
        }
        float recoveredPressure = Math.min(
                normalizedPressure, SAFE_ROOM_PRESSURE_RECOVERY);
        float recoveredPollution = Math.min(
                normalizedPollution, SAFE_ROOM_POLLUTION_RECOVERY);
        return new SafeRoomResult(
                TransactionStatus.SUCCESS,
                balance - SAFE_ROOM_COST,
                normalizedPressure - recoveredPressure,
                normalizedPollution - recoveredPollution,
                recoveredPressure,
                recoveredPollution);
    }

    private static float clamp(float value, float minimum, float maximum) {
        if (!Float.isFinite(value)) return minimum;
        return Math.max(minimum, Math.min(maximum, value));
    }

    public enum TransactionStatus {
        SUCCESS,
        INSUFFICIENT_FUNDS,
        NOT_NEEDED
    }

    public record SafeRoomResult(
            TransactionStatus status,
            long balance,
            float pressure,
            float pollution,
            float recoveredPressure,
            float recoveredPollution) {}
}
