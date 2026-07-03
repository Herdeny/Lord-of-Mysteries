package top.aurora.lordofmysteries.potion;

import java.util.List;

public final class CrucibleRecipeLogic {

    public static final String SPIRIT_HERB = "lord_of_mysteries:spirit_herb";
    public static final String DIVINATION_CRYSTAL = "lord_of_mysteries:divination_crystal";
    public static final String MOONWATER = "lord_of_mysteries:moonwater";
    public static final String FERMENTED_SPIDER_EYE = "minecraft:fermented_spider_eye";
    public static final String HONEY_BOTTLE = "minecraft:honey_bottle";
    public static final String BOOK = "minecraft:book";
    public static final String AMETHYST_SHARD = "minecraft:amethyst_shard";
    public static final float IDEAL_TEMPERATURE = 70f;

    private CrucibleRecipeLogic() {}

    public static PotionQuality evaluate(List<String> ingredients, float averageTemperature) {
        return evaluateRecipe(ingredients, averageTemperature).quality();
    }

    public static BrewResult evaluateRecipe(List<String> ingredients, float averageTemperature) {
        if (ingredients == null || ingredients.size() < 2 || ingredients.size() > 3) {
            return BrewResult.contaminated();
        }

        Recipe recipe = findRecipe(ingredients);
        if (recipe == null) return BrewResult.contaminated();

        boolean correctOrder = recipe.first.equals(ingredients.get(0))
                && recipe.second.equals(ingredients.get(1))
                && (ingredients.size() == 2 || recipe.optional.equals(ingredients.get(2)));
        float deviation = Math.abs(averageTemperature - IDEAL_TEMPERATURE);

        PotionQuality quality;
        if (correctOrder && ingredients.size() == 3 && deviation <= 5f) {
            quality = PotionQuality.PERFECT;
        } else if (correctOrder && deviation <= 15f) {
            quality = PotionQuality.COMPLETE;
        } else if (deviation <= 30f) {
            quality = PotionQuality.FLAWED;
        } else {
            quality = PotionQuality.CONTAMINATED;
        }
        return quality == PotionQuality.CONTAMINATED
                ? BrewResult.contaminated()
                : new BrewResult(recipe.potion, quality);
    }

    private static Recipe findRecipe(List<String> ingredients) {
        for (Recipe recipe : Recipe.values()) {
            boolean recognizedSize = ingredients.size() == 2 || ingredients.size() == 3;
            boolean required = ingredients.contains(recipe.first)
                    && ingredients.contains(recipe.second);
            boolean allowed = ingredients.stream().allMatch(id ->
                    recipe.first.equals(id)
                            || recipe.second.equals(id)
                            || recipe.optional.equals(id));
            if (recognizedSize && required && allowed) return recipe;
        }
        return null;
    }

    public enum BrewedPotion {
        SEER_9,
        SPECTATOR_9,
        SPECTATOR_8,
        CONTAMINATED
    }

    public record BrewResult(BrewedPotion potion, PotionQuality quality) {
        public static BrewResult contaminated() {
            return new BrewResult(BrewedPotion.CONTAMINATED, PotionQuality.CONTAMINATED);
        }
    }

    private enum Recipe {
        SEER(BrewedPotion.SEER_9, SPIRIT_HERB, DIVINATION_CRYSTAL, MOONWATER),
        SPECTATOR(BrewedPotion.SPECTATOR_9,
                SPIRIT_HERB, FERMENTED_SPIDER_EYE, HONEY_BOTTLE),
        TELEPATHIST(BrewedPotion.SPECTATOR_8, SPIRIT_HERB, BOOK, AMETHYST_SHARD);

        private final BrewedPotion potion;
        private final String first;
        private final String second;
        private final String optional;

        Recipe(BrewedPotion potion, String first, String second, String optional) {
            this.potion = potion;
            this.first = first;
            this.second = second;
            this.optional = optional;
        }
    }
}
