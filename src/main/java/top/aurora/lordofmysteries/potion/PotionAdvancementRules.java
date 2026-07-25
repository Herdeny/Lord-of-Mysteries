package top.aurora.lordofmysteries.potion;

public final class PotionAdvancementRules {

    public enum Eligibility {
        ALLOWED,
        DIGESTION_INCOMPLETE,
        RITUAL_REQUIRED,
        INCOMPATIBLE
    }

    private PotionAdvancementRules() {}

    public static boolean canAdvance(String currentPathway, int currentSequence, float digestion,
                                     String targetPathway, int targetSequence) {
        return evaluate(currentPathway, currentSequence, digestion,
                targetPathway, targetSequence, false) == Eligibility.ALLOWED;
    }

    public static Eligibility evaluate(
            String currentPathway, int currentSequence, float digestion,
            String targetPathway, int targetSequence,
            boolean sequenceFiveRitualComplete) {
        if (targetSequence == 9) {
            return (currentPathway == null || currentPathway.isBlank())
                    && currentSequence < 0
                    ? Eligibility.ALLOWED : Eligibility.INCOMPATIBLE;
        }
        if (targetSequence >= 4 && targetSequence <= 8) {
            if (!targetPathway.equals(currentPathway)
                    || currentSequence != targetSequence + 1) {
                return Eligibility.INCOMPATIBLE;
            }
            if (digestion < 100f) {
                return Eligibility.DIGESTION_INCOMPLETE;
            }
            if (targetSequence == 5 && !sequenceFiveRitualComplete) {
                return Eligibility.RITUAL_REQUIRED;
            }
            return Eligibility.ALLOWED;
        }
        return Eligibility.INCOMPATIBLE;
    }

    public static String messageKey(Eligibility eligibility) {
        return switch (eligibility) {
            case DIGESTION_INCOMPLETE ->
                    "message.lord_of_mysteries.potion.digestion_incomplete";
            case RITUAL_REQUIRED ->
                    "message.lord_of_mysteries.potion.sequence_five_ritual_required";
            case INCOMPATIBLE, ALLOWED ->
                    "message.lord_of_mysteries.potion.incompatible";
        };
    }
}

