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
}
