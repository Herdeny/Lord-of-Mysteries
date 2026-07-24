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
    public static final String SPIRIT_SALT = "lord_of_mysteries:spirit_salt";
    public static final String ASHEN_THREAD = "lord_of_mysteries:ashen_thread";
    public static final String WHITE_CANDLE = "lord_of_mysteries:white_candle";
    public static final String SHADOW_MARTEN_CLAW =
            "lord_of_mysteries:shadow_marten_claw";
    public static final String STARLIGHT_MOSS =
            "lord_of_mysteries:starlight_moss";
    public static final String MYSTIC_INK = "lord_of_mysteries:mystic_ink";
    public static final String DREAM_SCALE_FRAGMENT =
            "lord_of_mysteries:dream_scale_fragment";
    public static final String EMBER_SALAMANDER_GLAND =
            "lord_of_mysteries:ember_salamander_gland";
    public static final String MIRROR_CRAB_SHELL =
            "lord_of_mysteries:mirror_crab_shell";
    public static final String ANCIENT_TABLET_SPORE_SAC =
            "lord_of_mysteries:ancient_tablet_spore_sac";
    public static final String IRIDESCENT_TRICKBIRD_FEATHER =
            "lord_of_mysteries:iridescent_trickbird_feather";
    public static final String METEOR_DUST = "lord_of_mysteries:meteor_dust";
    public static final String BLANK_MANUSCRIPT =
            "lord_of_mysteries:blank_manuscript";
    public static final String BLAZE_POWDER = "minecraft:blaze_powder";
    public static final String SLIME_BALL = "minecraft:slime_ball";
    public static final String GOLD_NUGGET = "minecraft:gold_nugget";
    public static final String MOSS_BLOCK = "minecraft:moss_block";
    public static final String ENDER_PEARL = "minecraft:ender_pearl";
    public static final String COMPASS = "minecraft:compass";
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
        SEER_6,
        SEER_5,
        SPECTATOR_9,
        SPECTATOR_8,
        SPECTATOR_7,
        SPECTATOR_6,
        SPECTATOR_5,
        HUNTER_9,
        HUNTER_8,
        HUNTER_7,
        HUNTER_6,
        HUNTER_5,
        THIEF_9,
        THIEF_8,
        THIEF_7,
        THIEF_6,
        THIEF_5,
        APPRENTICE_9,
        APPRENTICE_8,
        APPRENTICE_7,
        APPRENTICE_6,
        APPRENTICE_5,
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
        FACELESS(BrewedPotion.SEER_6,
                SHAPESHIFTER_SERPENT_GLAND, SILVER_FILINGS, ASHEN_THREAD, 75f),
        MARIONETTIST(BrewedPotion.SEER_5,
                ASHEN_THREAD, SPIRIT_SALT, WHITE_CANDLE, 65f),
        SPECTATOR(BrewedPotion.SPECTATOR_9,
                SPIRIT_HERB, FERMENTED_SPIDER_EYE, HONEY_BOTTLE, 70f),
        TELEPATHIST(BrewedPotion.SPECTATOR_8,
                SPIRIT_HERB, BOOK, AMETHYST_SHARD, 70f),
        PSYCHIATRIST(BrewedPotion.SPECTATOR_7,
                DREAM_SCALE_FRAGMENT, FERMENTED_SPIDER_EYE, MYSTIC_INK, 74f),
        HYPNOTIST(BrewedPotion.SPECTATOR_6,
                DREAM_SCALE_FRAGMENT, HEATHER, HONEY_BOTTLE, 70f),
        DREAMWALKER(BrewedPotion.SPECTATOR_5,
                DREAM_SCALE_FRAGMENT, MOONWATER, SILVER_FILINGS, 65f),
        HUNTER(BrewedPotion.HUNTER_9, SPIRIT_HERB, BONE, RABBIT_FOOT, 70f),
        PROVOKER(BrewedPotion.HUNTER_8,
                SPIRIT_HERB, GUNPOWDER, REDSTONE, 70f),
        PYROMANIAC(BrewedPotion.HUNTER_7,
                EMBER_SALAMANDER_GLAND, BLAZE_POWDER, GUNPOWDER, 82f),
        CONSPIRER(BrewedPotion.HUNTER_6,
                EMBER_SALAMANDER_GLAND, SPIRIT_ALCOHOL, BONE, 75f),
        REAPER(BrewedPotion.HUNTER_5,
                ASH_POWDER, BLAZE_POWDER, SPIRIT_SALT, 82f),
        THIEF(BrewedPotion.THIEF_9,
                SHADOW_MARTEN_CLAW, FERMENTED_SPIDER_EYE, SPIRIT_ALCOHOL, 68f),
        SWINDLER(BrewedPotion.THIEF_8,
                MIRROR_CRAB_SHELL, SLIME_BALL, GOLD_NUGGET, 70f),
        CRYPTOLOGIST(BrewedPotion.THIEF_7,
                ANCIENT_TABLET_SPORE_SAC, MOSS_BLOCK, REDSTONE, 65f),
        PROMETHEUS(BrewedPotion.THIEF_6,
                SHADOW_MARTEN_CLAW, ASH_POWDER, SPIRIT_ALCOHOL, 70f),
        DREAM_STEALER(BrewedPotion.THIEF_5,
                DREAM_SCALE_FRAGMENT, MYSTIC_INK, MOONWATER, 65f),
        APPRENTICE(BrewedPotion.APPRENTICE_9,
                STARLIGHT_MOSS, AMETHYST_SHARD, MYSTIC_INK, 72f),
        TRICKMASTER(BrewedPotion.APPRENTICE_8,
                IRIDESCENT_TRICKBIRD_FEATHER, ENDER_PEARL, AMETHYST_SHARD, 78f),
        ASTROLOGER(BrewedPotion.APPRENTICE_7,
                METEOR_DUST, MOONWATER, COMPASS, 62f),
        SCRIBE(BrewedPotion.APPRENTICE_6,
                BLANK_MANUSCRIPT, SPIRIT_SALT, MYSTIC_INK, 65f),
        TRAVELER(BrewedPotion.APPRENTICE_5,
                METEOR_DUST, SILVER_FILINGS, ENDER_PEARL, 75f);

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
