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
    public static final String BONE = "minecraft:bone";
    public static final String RABBIT_FOOT = "minecraft:rabbit_foot";
    public static final String GUNPOWDER = "minecraft:gunpowder";
    public static final String REDSTONE = "minecraft:redstone";
    public static final String DEEP_GRAY_SPIRIT_TEAR =
            "lord_of_mysteries:deep_gray_spirit_tear";
    public static final String HEATHER = "lord_of_mysteries:heather";
    public static final String SPIRIT_ALCOHOL = "lord_of_mysteries:spirit_alcohol";
    public static final String SHAPESHIFTER_SERPENT_GLAND =
            "lord_of_mysteries:shapeshifter_serpent_gland";
    public static final String ASH_POWDER = "lord_of_mysteries:ash_powder";
    public static final String SILVER_FILINGS = "lord_of_mysteries:silver_filings";
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
        float deviation = Math.abs(averageTemperature - recipe.idealTemperature);

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
        SEER_8,
        SEER_7,
        SPECTATOR_9,
        SPECTATOR_8,
        HUNTER_9,
        HUNTER_8,
        CONTAMINATED
    }

    public record BrewResult(BrewedPotion potion, PotionQuality quality) {
        public static BrewResult contaminated() {
            return new BrewResult(BrewedPotion.CONTAMINATED, PotionQuality.CONTAMINATED);
        }
    }

    private enum Recipe {
        SEER(BrewedPotion.SEER_9, SPIRIT_HERB, DIVINATION_CRYSTAL, MOONWATER, 70f),
        CLOWN(BrewedPotion.SEER_8,
                DEEP_GRAY_SPIRIT_TEAR, HEATHER, SPIRIT_ALCOHOL, 65f),
        MAGICIAN(BrewedPotion.SEER_7,
                SHAPESHIFTER_SERPENT_GLAND, ASH_POWDER, SILVER_FILINGS, 90f),
        SPECTATOR(BrewedPotion.SPECTATOR_9,
                SPIRIT_HERB, FERMENTED_SPIDER_EYE, HONEY_BOTTLE, 70f),
        TELEPATHIST(BrewedPotion.SPECTATOR_8,
                SPIRIT_HERB, BOOK, AMETHYST_SHARD, 70f),
        HUNTER(BrewedPotion.HUNTER_9, SPIRIT_HERB, BONE, RABBIT_FOOT, 70f),
        PROVOKER(BrewedPotion.HUNTER_8,
                SPIRIT_HERB, GUNPOWDER, REDSTONE, 70f);

        private final BrewedPotion potion;
        private final String first;
        private final String second;
        private final String optional;
        private final float idealTemperature;

        Recipe(BrewedPotion potion, String first, String second,
               String optional, float idealTemperature) {
            this.potion = potion;
            this.first = first;
            this.second = second;
            this.optional = optional;
            this.idealTemperature = idealTemperature;
        }
    }
}
