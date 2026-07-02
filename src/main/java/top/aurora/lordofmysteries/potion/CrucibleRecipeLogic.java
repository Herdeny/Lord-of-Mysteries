package top.aurora.lordofmysteries.potion;

import java.util.List;

public final class CrucibleRecipeLogic {

    public static final String SPIRIT_HERB = "lord_of_mysteries:spirit_herb";
    public static final String DIVINATION_CRYSTAL = "lord_of_mysteries:divination_crystal";
    public static final String MOONWATER = "lord_of_mysteries:moonwater";
    public static final float IDEAL_TEMPERATURE = 70f;

    private CrucibleRecipeLogic() {}

    public static PotionQuality evaluate(List<String> ingredients, float averageTemperature) {
        if (ingredients == null || ingredients.size() < 2 || ingredients.size() > 3) {
            return PotionQuality.CONTAMINATED;
        }

        boolean hasRequired = ingredients.contains(SPIRIT_HERB)
                && ingredients.contains(DIVINATION_CRYSTAL);
        boolean containsUnknown = ingredients.stream().anyMatch(id ->
                !SPIRIT_HERB.equals(id)
                        && !DIVINATION_CRYSTAL.equals(id)
                        && !MOONWATER.equals(id));
        if (!hasRequired || containsUnknown) return PotionQuality.CONTAMINATED;

        boolean correctOrder = SPIRIT_HERB.equals(ingredients.get(0))
                && DIVINATION_CRYSTAL.equals(ingredients.get(1))
                && (ingredients.size() == 2 || MOONWATER.equals(ingredients.get(2)));
        float deviation = Math.abs(averageTemperature - IDEAL_TEMPERATURE);

        if (correctOrder && ingredients.size() == 3 && deviation <= 5f) {
            return PotionQuality.PERFECT;
        }
        if (correctOrder && deviation <= 15f) {
            return PotionQuality.COMPLETE;
        }
        if (deviation <= 30f) {
            return PotionQuality.FLAWED;
        }
        return PotionQuality.CONTAMINATED;
    }
}
