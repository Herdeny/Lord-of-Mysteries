package top.aurora.lordofmysteries.ability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SeerAbilityLogicTest {

    @Test
    void lowerSequenceNumbersRetainEarlierAbilities() {
        assertTrue(SeerAbilityLogic.canUseSequence(7, 8));
        assertTrue(SeerAbilityLogic.canUseSequence(8, 8));
        assertFalse(SeerAbilityLogic.canUseSequence(9, 8));
    }

    @Test
    void intuitiveDodgeRequiresMeleeChanceAndCooldown() {
        assertTrue(SeerAbilityLogic.intuitiveDodge(0.149f, true, 100L, 100L));
        assertFalse(SeerAbilityLogic.intuitiveDodge(0.15f, true, 100L, 100L));
        assertFalse(SeerAbilityLogic.intuitiveDodge(0.01f, false, 100L, 100L));
        assertFalse(SeerAbilityLogic.intuitiveDodge(0.01f, true, 101L, 100L));
    }

    @Test
    void flameLeapAndSubstituteRespectHardLimits() {
        assertEquals(8d, SeerAbilityLogic.clampFlameLeapDistance(20d));
        assertEquals(0d, SeerAbilityLogic.clampFlameLeapDistance(-1d));
        assertTrue(SeerAbilityLogic.paperSubstituteTriggers(10f, 10f, 200L, 199L));
        assertFalse(SeerAbilityLogic.paperSubstituteTriggers(9f, 10f, 200L, 199L));
        assertFalse(SeerAbilityLogic.paperSubstituteTriggers(10f, 10f, 200L, 200L));
    }

    @Test
    void spiritBurningStrengthensCardVolley() {
        assertEquals(6, SeerAbilityLogic.cardVolleyDamage(false));
        assertEquals(8, SeerAbilityLogic.cardVolleyDamage(true));
    }
}
