package top.aurora.lordofmysteries.potion;

public final class PotionAdvancementRules {

    private PotionAdvancementRules() {}

    public static boolean canAdvance(String currentPathway, int currentSequence, float digestion,
                                     String targetPathway, int targetSequence) {
        if (targetSequence == 9) {
            return (currentPathway == null || currentPathway.isBlank()) && currentSequence < 0;
        }
        if (targetSequence == 8) {
            return targetPathway.equals(currentPathway)
                    && currentSequence == 9
                    && digestion >= 100f;
        }
        return false;
    }
}

