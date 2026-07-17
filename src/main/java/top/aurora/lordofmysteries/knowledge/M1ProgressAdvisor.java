package top.aurora.lordofmysteries.knowledge;

public final class M1ProgressAdvisor {

    public enum Stage {
        FIND_CAMP,
        BREW_SEER,
        DIGEST_SEER,
        BREW_CLOWN,
        DIGEST_CLOWN,
        HUNT_SERPENT,
        BREW_MAGICIAN,
        BIND_IDENTITY,
        REFLECT,
        CITY_LIFE,
        COMPLETE,
        OTHER_PATHWAY
    }

    private M1ProgressAdvisor() {}

    public static Stage evaluate(boolean campFound, String pathway, int sequence,
                                 float digestion, boolean hasMagicianMaterial,
                                 boolean identityAnchored,
                                 boolean reflectionCompleted,
                                 boolean streetLifeCompleted) {
        if (pathway == null || pathway.isBlank() || sequence < 0) {
            return campFound ? Stage.BREW_SEER : Stage.FIND_CAMP;
        }
        if (!pathway.endsWith(":seer")) return Stage.OTHER_PATHWAY;
        if (sequence == 9) {
            return digestion >= 100f ? Stage.BREW_CLOWN : Stage.DIGEST_SEER;
        }
        if (sequence == 8) {
            if (digestion < 100f) return Stage.DIGEST_CLOWN;
            return hasMagicianMaterial ? Stage.BREW_MAGICIAN : Stage.HUNT_SERPENT;
        }
        if (sequence <= 7) {
            if (!identityAnchored) return Stage.BIND_IDENTITY;
            if (!reflectionCompleted) return Stage.REFLECT;
            if (!streetLifeCompleted) return Stage.CITY_LIFE;
            return Stage.COMPLETE;
        }
        return Stage.OTHER_PATHWAY;
    }

    public static String translationSuffix(Stage stage) {
        return stage.name().toLowerCase(java.util.Locale.ROOT);
    }
}
