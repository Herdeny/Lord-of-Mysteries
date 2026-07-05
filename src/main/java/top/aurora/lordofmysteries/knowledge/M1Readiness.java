package top.aurora.lordofmysteries.knowledge;

public final class M1Readiness {

    public enum Stage {
        COMMONER,
        SEER_9_DIGESTING,
        READY_FOR_8,
        SEER_8_DIGESTING,
        READY_FOR_7,
        SEER_7_REACHED,
        OTHER_PATHWAY
    }

    private M1Readiness() {}

    public static Stage evaluate(String pathway, int sequence, float digestion) {
        if (pathway == null || pathway.isBlank() || sequence < 0) return Stage.COMMONER;
        if (!pathway.endsWith(":seer")) return Stage.OTHER_PATHWAY;
        if (sequence == 9) {
            return digestion >= 100f ? Stage.READY_FOR_8 : Stage.SEER_9_DIGESTING;
        }
        if (sequence == 8) {
            return digestion >= 100f ? Stage.READY_FOR_7 : Stage.SEER_8_DIGESTING;
        }
        if (sequence <= 7) return Stage.SEER_7_REACHED;
        return Stage.OTHER_PATHWAY;
    }
}
