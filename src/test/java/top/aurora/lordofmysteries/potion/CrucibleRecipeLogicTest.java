package top.aurora.lordofmysteries.potion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

class CrucibleRecipeLogicTest {

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
}
