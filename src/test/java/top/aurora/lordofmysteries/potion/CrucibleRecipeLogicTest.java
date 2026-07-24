package top.aurora.lordofmysteries.potion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

class CrucibleRecipeLogicTest {

    private record RecipeCase(CrucibleRecipeLogic.BrewedPotion potion,
                              List<String> ingredients, float temperature) {}

    @Test
    void perfectRequiresOptionalIngredientCorrectOrderAndIdealHeat() {
        assertEquals(PotionQuality.PERFECT, CrucibleRecipeLogic.evaluate(List.of(
                CrucibleRecipeLogic.SPIRIT_HERB,
                CrucibleRecipeLogic.DIVINATION_CRYSTAL,
                CrucibleRecipeLogic.MOONWATER), 70f));
    }

    @Test
    void correctRequiredIngredientsWithoutMoonwaterAreComplete() {
        assertEquals(PotionQuality.COMPLETE, CrucibleRecipeLogic.evaluate(List.of(
                CrucibleRecipeLogic.SPIRIT_HERB,
                CrucibleRecipeLogic.DIVINATION_CRYSTAL), 70f));
    }

    @Test
    void wrongOrderProducesFlawedPotionWithinSafeHeat() {
        assertEquals(PotionQuality.FLAWED, CrucibleRecipeLogic.evaluate(List.of(
                CrucibleRecipeLogic.DIVINATION_CRYSTAL,
                CrucibleRecipeLogic.SPIRIT_HERB), 70f));
    }

    @Test
    void missingRequiredIngredientIsContaminated() {
        assertEquals(PotionQuality.CONTAMINATED, CrucibleRecipeLogic.evaluate(List.of(
                CrucibleRecipeLogic.SPIRIT_HERB,
                CrucibleRecipeLogic.MOONWATER), 70f));
    }

    @Test
    void extremeTemperatureContaminatesOtherwiseCorrectRecipe() {
        assertEquals(PotionQuality.CONTAMINATED, CrucibleRecipeLogic.evaluate(List.of(
                CrucibleRecipeLogic.SPIRIT_HERB,
                CrucibleRecipeLogic.DIVINATION_CRYSTAL), 105f));
    }

    @Test
    void spectatorSequenceNineRecipeProducesSpectatorPotion() {
        CrucibleRecipeLogic.BrewResult result = CrucibleRecipeLogic.evaluateRecipe(List.of(
                CrucibleRecipeLogic.SPIRIT_HERB,
                CrucibleRecipeLogic.FERMENTED_SPIDER_EYE,
                CrucibleRecipeLogic.HONEY_BOTTLE), 70f);
        assertEquals(CrucibleRecipeLogic.BrewedPotion.SPECTATOR_9, result.potion());
        assertEquals(PotionQuality.PERFECT, result.quality());
    }

    @Test
    void clownRecipeUsesGentleHeat() {
        CrucibleRecipeLogic.BrewResult result = CrucibleRecipeLogic.evaluateRecipe(List.of(
                CrucibleRecipeLogic.DEEP_GRAY_SPIRIT_TEAR,
                CrucibleRecipeLogic.HEATHER,
                CrucibleRecipeLogic.SPIRIT_ALCOHOL), 65f);
        assertEquals(CrucibleRecipeLogic.BrewedPotion.SEER_8, result.potion());
        assertEquals(PotionQuality.PERFECT, result.quality());
    }

    @Test
    void magicianRecipeUsesStrongHeat() {
        CrucibleRecipeLogic.BrewResult result = CrucibleRecipeLogic.evaluateRecipe(List.of(
                CrucibleRecipeLogic.SHAPESHIFTER_SERPENT_GLAND,
                CrucibleRecipeLogic.ASH_POWDER,
                CrucibleRecipeLogic.SILVER_FILINGS), 90f);
        assertEquals(CrucibleRecipeLogic.BrewedPotion.SEER_7, result.potion());
        assertEquals(PotionQuality.PERFECT, result.quality());
    }

    @Test
    void spectatorSequenceEightRecipeProducesTelepathistPotion() {
        CrucibleRecipeLogic.BrewResult result = CrucibleRecipeLogic.evaluateRecipe(List.of(
                CrucibleRecipeLogic.SPIRIT_HERB,
                CrucibleRecipeLogic.BOOK,
                CrucibleRecipeLogic.AMETHYST_SHARD), 70f);
        assertEquals(CrucibleRecipeLogic.BrewedPotion.SPECTATOR_8, result.potion());
        assertEquals(PotionQuality.PERFECT, result.quality());
    }

    @Test
    void mixingIngredientsAcrossRecipesContaminatesBatch() {
        CrucibleRecipeLogic.BrewResult result = CrucibleRecipeLogic.evaluateRecipe(List.of(
                CrucibleRecipeLogic.SPIRIT_HERB,
                CrucibleRecipeLogic.BOOK,
                CrucibleRecipeLogic.HONEY_BOTTLE), 70f);
        assertEquals(CrucibleRecipeLogic.BrewedPotion.CONTAMINATED, result.potion());
        assertEquals(PotionQuality.CONTAMINATED, result.quality());
    }

    @Test
    void hunterSequenceNineRecipeProducesHunterPotion() {
        CrucibleRecipeLogic.BrewResult result = CrucibleRecipeLogic.evaluateRecipe(List.of(
                CrucibleRecipeLogic.SPIRIT_HERB,
                CrucibleRecipeLogic.BONE,
                CrucibleRecipeLogic.RABBIT_FOOT), 70f);
        assertEquals(CrucibleRecipeLogic.BrewedPotion.HUNTER_9, result.potion());
        assertEquals(PotionQuality.PERFECT, result.quality());
    }

    @Test
    void hunterSequenceEightRecipeProducesProvokerPotion() {
        CrucibleRecipeLogic.BrewResult result = CrucibleRecipeLogic.evaluateRecipe(List.of(
                CrucibleRecipeLogic.SPIRIT_HERB,
                CrucibleRecipeLogic.GUNPOWDER,
                CrucibleRecipeLogic.REDSTONE), 70f);
        assertEquals(CrucibleRecipeLogic.BrewedPotion.HUNTER_8, result.potion());
        assertEquals(PotionQuality.PERFECT, result.quality());
    }

    @Test
    void hunterCrossRecipeIngredientsContaminateBatch() {
        CrucibleRecipeLogic.BrewResult result = CrucibleRecipeLogic.evaluateRecipe(List.of(
                CrucibleRecipeLogic.SPIRIT_HERB,
                CrucibleRecipeLogic.BONE,
                CrucibleRecipeLogic.REDSTONE), 70f);
        assertEquals(CrucibleRecipeLogic.BrewedPotion.CONTAMINATED, result.potion());
    }

    @Test
    void thiefRecipeUsesShadowClawAndSpiritAlcohol() {
        CrucibleRecipeLogic.BrewResult result = CrucibleRecipeLogic.evaluateRecipe(List.of(
                CrucibleRecipeLogic.SHADOW_MARTEN_CLAW,
                CrucibleRecipeLogic.FERMENTED_SPIDER_EYE,
                CrucibleRecipeLogic.SPIRIT_ALCOHOL), 68f);
        assertEquals(CrucibleRecipeLogic.BrewedPotion.THIEF_9, result.potion());
        assertEquals(PotionQuality.PERFECT, result.quality());
    }

    @Test
    void apprenticeRecipeUsesStarlightMossAndMysticInk() {
        CrucibleRecipeLogic.BrewResult result = CrucibleRecipeLogic.evaluateRecipe(List.of(
                CrucibleRecipeLogic.STARLIGHT_MOSS,
                CrucibleRecipeLogic.AMETHYST_SHARD,
                CrucibleRecipeLogic.MYSTIC_INK), 72f);
        assertEquals(CrucibleRecipeLogic.BrewedPotion.APPRENTICE_9, result.potion());
        assertEquals(PotionQuality.PERFECT, result.quality());
    }

    @Test
    void psychiatristRecipeUsesDreamScaleAndMysticInk() {
        CrucibleRecipeLogic.BrewResult result = CrucibleRecipeLogic.evaluateRecipe(List.of(
                CrucibleRecipeLogic.DREAM_SCALE_FRAGMENT,
                CrucibleRecipeLogic.FERMENTED_SPIDER_EYE,
                CrucibleRecipeLogic.MYSTIC_INK), 74f);
        assertEquals(CrucibleRecipeLogic.BrewedPotion.SPECTATOR_7, result.potion());
        assertEquals(PotionQuality.PERFECT, result.quality());
    }

    @Test
    void pyromaniacRecipeUsesSalamanderGlandAndBlazePowder() {
        CrucibleRecipeLogic.BrewResult result = CrucibleRecipeLogic.evaluateRecipe(List.of(
                CrucibleRecipeLogic.EMBER_SALAMANDER_GLAND,
                CrucibleRecipeLogic.BLAZE_POWDER,
                CrucibleRecipeLogic.GUNPOWDER), 82f);
        assertEquals(CrucibleRecipeLogic.BrewedPotion.HUNTER_7, result.potion());
        assertEquals(PotionQuality.PERFECT, result.quality());
    }

    @Test
    void swindlerRecipeUsesMirrorShellAndSlime() {
        CrucibleRecipeLogic.BrewResult result = CrucibleRecipeLogic.evaluateRecipe(List.of(
                CrucibleRecipeLogic.MIRROR_CRAB_SHELL,
                CrucibleRecipeLogic.SLIME_BALL,
                CrucibleRecipeLogic.GOLD_NUGGET), 70f);
        assertEquals(CrucibleRecipeLogic.BrewedPotion.THIEF_8, result.potion());
        assertEquals(PotionQuality.PERFECT, result.quality());
    }

    @Test
    void cryptologistRecipeUsesTabletSporesAndMoss() {
        CrucibleRecipeLogic.BrewResult result = CrucibleRecipeLogic.evaluateRecipe(List.of(
                CrucibleRecipeLogic.ANCIENT_TABLET_SPORE_SAC,
                CrucibleRecipeLogic.MOSS_BLOCK,
                CrucibleRecipeLogic.REDSTONE), 65f);
        assertEquals(CrucibleRecipeLogic.BrewedPotion.THIEF_7, result.potion());
        assertEquals(PotionQuality.PERFECT, result.quality());
    }

    @Test
    void trickmasterRecipeUsesIridescentFeatherAndPearl() {
        CrucibleRecipeLogic.BrewResult result = CrucibleRecipeLogic.evaluateRecipe(List.of(
                CrucibleRecipeLogic.IRIDESCENT_TRICKBIRD_FEATHER,
                CrucibleRecipeLogic.ENDER_PEARL,
                CrucibleRecipeLogic.AMETHYST_SHARD), 78f);
        assertEquals(CrucibleRecipeLogic.BrewedPotion.APPRENTICE_8, result.potion());
        assertEquals(PotionQuality.PERFECT, result.quality());
    }

    @Test
    void astrologerRecipeUsesMeteorDustAndMoonwater() {
        CrucibleRecipeLogic.BrewResult result = CrucibleRecipeLogic.evaluateRecipe(List.of(
                CrucibleRecipeLogic.METEOR_DUST,
                CrucibleRecipeLogic.MOONWATER,
                CrucibleRecipeLogic.COMPASS), 62f);
        assertEquals(CrucibleRecipeLogic.BrewedPotion.APPRENTICE_7, result.potion());
        assertEquals(PotionQuality.PERFECT, result.quality());
    }

    @Test
    void allFivePathwaysHaveDistinctSequenceSixAndFiveRecipes() {
        List<RecipeCase> cases = List.of(
                new RecipeCase(CrucibleRecipeLogic.BrewedPotion.SEER_6,
                        List.of(CrucibleRecipeLogic.SHAPESHIFTER_SERPENT_GLAND,
                                CrucibleRecipeLogic.SILVER_FILINGS,
                                CrucibleRecipeLogic.ASHEN_THREAD), 75f),
                new RecipeCase(CrucibleRecipeLogic.BrewedPotion.SEER_5,
                        List.of(CrucibleRecipeLogic.ASHEN_THREAD,
                                CrucibleRecipeLogic.SPIRIT_SALT,
                                CrucibleRecipeLogic.WHITE_CANDLE), 65f),
                new RecipeCase(CrucibleRecipeLogic.BrewedPotion.SPECTATOR_6,
                        List.of(CrucibleRecipeLogic.DREAM_SCALE_FRAGMENT,
                                CrucibleRecipeLogic.HEATHER,
                                CrucibleRecipeLogic.HONEY_BOTTLE), 70f),
                new RecipeCase(CrucibleRecipeLogic.BrewedPotion.SPECTATOR_5,
                        List.of(CrucibleRecipeLogic.DREAM_SCALE_FRAGMENT,
                                CrucibleRecipeLogic.MOONWATER,
                                CrucibleRecipeLogic.SILVER_FILINGS), 65f),
                new RecipeCase(CrucibleRecipeLogic.BrewedPotion.HUNTER_6,
                        List.of(CrucibleRecipeLogic.EMBER_SALAMANDER_GLAND,
                                CrucibleRecipeLogic.SPIRIT_ALCOHOL,
                                CrucibleRecipeLogic.BONE), 75f),
                new RecipeCase(CrucibleRecipeLogic.BrewedPotion.HUNTER_5,
                        List.of(CrucibleRecipeLogic.ASH_POWDER,
                                CrucibleRecipeLogic.BLAZE_POWDER,
                                CrucibleRecipeLogic.SPIRIT_SALT), 82f),
                new RecipeCase(CrucibleRecipeLogic.BrewedPotion.THIEF_6,
                        List.of(CrucibleRecipeLogic.SHADOW_MARTEN_CLAW,
                                CrucibleRecipeLogic.ASH_POWDER,
                                CrucibleRecipeLogic.SPIRIT_ALCOHOL), 70f),
                new RecipeCase(CrucibleRecipeLogic.BrewedPotion.THIEF_5,
                        List.of(CrucibleRecipeLogic.DREAM_SCALE_FRAGMENT,
                                CrucibleRecipeLogic.MYSTIC_INK,
                                CrucibleRecipeLogic.MOONWATER), 65f),
                new RecipeCase(CrucibleRecipeLogic.BrewedPotion.APPRENTICE_6,
                        List.of(CrucibleRecipeLogic.BLANK_MANUSCRIPT,
                                CrucibleRecipeLogic.SPIRIT_SALT,
                                CrucibleRecipeLogic.MYSTIC_INK), 65f),
                new RecipeCase(CrucibleRecipeLogic.BrewedPotion.APPRENTICE_5,
                        List.of(CrucibleRecipeLogic.METEOR_DUST,
                                CrucibleRecipeLogic.SILVER_FILINGS,
                                CrucibleRecipeLogic.ENDER_PEARL), 75f));

        for (RecipeCase recipe : cases) {
            CrucibleRecipeLogic.BrewResult result =
                    CrucibleRecipeLogic.evaluateRecipe(
                            recipe.ingredients(), recipe.temperature());
            assertEquals(recipe.potion(), result.potion(),
                    () -> "wrong potion for " + recipe.ingredients());
            assertEquals(PotionQuality.PERFECT, result.quality(),
                    () -> "recipe not perfect at documented heat: " + recipe.potion());
        }
    }

    @Test
    void m3CrossPathwayIngredientMixIsRejected() {
        CrucibleRecipeLogic.BrewResult result = CrucibleRecipeLogic.evaluateRecipe(List.of(
                CrucibleRecipeLogic.DREAM_SCALE_FRAGMENT,
                CrucibleRecipeLogic.SPIRIT_SALT,
                CrucibleRecipeLogic.ENDER_PEARL), 70f);
        assertEquals(CrucibleRecipeLogic.BrewedPotion.CONTAMINATED, result.potion());
        assertEquals(PotionQuality.CONTAMINATED, result.quality());
    }
}
