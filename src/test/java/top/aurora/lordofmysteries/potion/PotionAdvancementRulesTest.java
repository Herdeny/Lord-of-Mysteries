package top.aurora.lordofmysteries.potion;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PotionAdvancementRulesTest {

    private static final String SPECTATOR = "lord_of_mysteries:spectator";

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
}

