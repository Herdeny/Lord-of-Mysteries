package top.aurora.lordofmysteries.ritual;

public final class SequenceFiveRitualLogic {

    public enum Outcome {
        STABLE_SUCCESS(true),
        STRAINED_SUCCESS(true),
        FAILURE(false),
        SEVERE_FAILURE(false);

        private final boolean success;

        Outcome(boolean success) {
            this.success = success;
        }

        public boolean success() {
            return success;
        }
    }

    private SequenceFiveRitualLogic() {}

    public static float stability(float pollution, float pressure,
                                  int supporters, float worldEventBonus) {
        float score = 0.90f
                - clamp(pollution, 0f, 100f) * 0.004f
                - clamp(pressure, 0f, 100f) * 0.0025f
                + Math.min(3, Math.max(0, supporters)) * 0.08f
                + clamp(worldEventBonus, 0f, 0.10f);
        return clamp(score, 0.10f, 1f);
    }

    public static Outcome resolve(float stability, float roll) {
        float safeStability = clamp(stability, 0f, 1f);
        float safeRoll = clamp(roll, 0f, 1f);
        if (safeStability >= 0.85f) return Outcome.STABLE_SUCCESS;
        if (safeRoll <= safeStability) return Outcome.STRAINED_SUCCESS;
        if (safeRoll <= Math.min(1f, safeStability + 0.20f)) {
            return Outcome.FAILURE;
        }
        return Outcome.SEVERE_FAILURE;
    }

    private static float clamp(float value, float minimum, float maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
