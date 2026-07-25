package top.aurora.lordofmysteries.potion;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PotionAdvancementRulesTest {

    private static final String SPECTATOR = "lord_of_mysteries:spectator";
    private static final String SEER = "lord_of_mysteries:seer";

    @Test
    void commonerCanDrinkSequenceNinePotion() {
        assertTrue(PotionAdvancementRules.canAdvance(null, -1, 0f, SPECTATOR, 9));
    }

    @Test
    void extraordinaryPlayerCannotSwitchPathways() {
        assertFalse(PotionAdvancementRules.canAdvance(
                "lord_of_mysteries:seer", 9, 100f, SPECTATOR, 9));
        assertFalse(PotionAdvancementRules.canAdvance(
                "lord_of_mysteries:seer", 9, 100f, SPECTATOR, 8));
    }

    @Test
    void sequenceEightRequiresMatchingDigestedSequenceNine() {
        assertFalse(PotionAdvancementRules.canAdvance(SPECTATOR, 9, 99.9f, SPECTATOR, 8));
        assertTrue(PotionAdvancementRules.canAdvance(SPECTATOR, 9, 100f, SPECTATOR, 8));
    }

    @Test
    void sequenceSevenRequiresMatchingDigestedSequenceEight() {
        assertFalse(PotionAdvancementRules.canAdvance(SEER, 8, 99.9f, SEER, 7));
        assertTrue(PotionAdvancementRules.canAdvance(SEER, 8, 100f, SEER, 7));
        assertFalse(PotionAdvancementRules.canAdvance(SEER, 9, 100f, SEER, 7));
    }

    @Test
    void sequenceFiveRequiresMatchingCompletedRitual() {
        assertEquals(PotionAdvancementRules.Eligibility.DIGESTION_INCOMPLETE,
                PotionAdvancementRules.evaluate(
                        SEER, 6, 99.9f, SEER, 5, true));
        assertEquals(PotionAdvancementRules.Eligibility.RITUAL_REQUIRED,
                PotionAdvancementRules.evaluate(
                        SEER, 6, 100f, SEER, 5, false));
        assertEquals(PotionAdvancementRules.Eligibility.ALLOWED,
                PotionAdvancementRules.evaluate(
                        SEER, 6, 100f, SEER, 5, true));
        assertEquals(PotionAdvancementRules.Eligibility.INCOMPATIBLE,
                PotionAdvancementRules.evaluate(
                        SPECTATOR, 6, 100f, SEER, 5, true));
    }
}

