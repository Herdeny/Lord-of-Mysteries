package top.aurora.lordofmysteries.ritual;

public final class RitualResolutionLogic {

    private RitualResolutionLogic() {}

    public static float completionScore(boolean materialsValid, boolean environmentValid,
                                        float structureCompletion, boolean qualifiedLeader) {
        return completionScore(
                materialsValid, environmentValid, structureCompletion,
                qualifiedLeader, 0f);
    }

    public static float completionScore(
            boolean materialsValid,
            boolean environmentValid,
            float structureCompletion,
            boolean qualifiedLeader,
            float eventBonus) {
        float score = 0.05f;
        if (materialsValid) score += 0.40f;
        if (environmentValid) score += 0.20f;
        score += Math.max(0f, Math.min(1f, structureCompletion)) * 0.25f;
        if (qualifiedLeader) score += 0.10f;
        score += Math.max(0f, Math.min(0.10f, eventBonus));
        return Math.min(1f, score);
    }

    public static Outcome resolve(float completionScore, float randomDelta) {
        float adjusted = Math.max(0f, Math.min(1f, completionScore + randomDelta));
        if (adjusted >= 0.95f) return Outcome.PERFECT;
        if (adjusted >= 0.80f) return Outcome.SUCCESS;
        if (adjusted >= 0.50f) return Outcome.FAILURE;
        if (adjusted >= 0.20f) return Outcome.SEVERE_FAILURE;
        return Outcome.CATASTROPHE;
    }

    public static Outcome escalateFailure(Outcome outcome, float riskRoll) {
        if (outcome != Outcome.FAILURE) return outcome;
        float roll = Math.max(0f, Math.min(1f, riskRoll));
        if (roll < 0.5f) return Outcome.FAILURE;
        if (roll < 0.8f) return Outcome.SEVERE_FAILURE;
        return Outcome.CATASTROPHE;
    }

    public enum Outcome {
        PERFECT(true),
        SUCCESS(true),
        FAILURE(false),
        SEVERE_FAILURE(false),
        CATASTROPHE(false);

        private final boolean success;

        Outcome(boolean success) {
            this.success = success;
        }

        public boolean success() {
            return success;
        }
    }
}
