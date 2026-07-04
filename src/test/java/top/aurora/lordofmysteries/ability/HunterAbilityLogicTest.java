package top.aurora.lordofmysteries.ability;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HunterAbilityLogicTest {

    @Test
    void naturalHostileOrRecordedTargetsCountAsValidHunts() {
        assertTrue(HunterAbilityLogic.validHuntTarget(true, false, false));
        assertTrue(HunterAbilityLogic.validHuntTarget(false, true, false));
        assertTrue(HunterAbilityLogic.validHuntTarget(false, false, true));
        assertFalse(HunterAbilityLogic.validHuntTarget(false, false, false));
    }

    @Test
    void provokeRejectsBossesAndHighResistanceTargets() {
        assertTrue(HunterAbilityLogic.canProvoke(40f, false));
        assertFalse(HunterAbilityLogic.canProvoke(81f, false));
        assertFalse(HunterAbilityLogic.canProvoke(40f, true));
    }

    @Test
    void battleWillRequiresThreeAttackersAndReadyCooldown() {
        assertTrue(HunterAbilityLogic.battleWillReady(3, 100L, 100L));
        assertFalse(HunterAbilityLogic.battleWillReady(2, 100L, 100L));
        assertFalse(HunterAbilityLogic.battleWillReady(3, 101L, 100L));
    }

    @Test
    void sequenceNineHunterRegeneratesTenPercentFasterOutdoors() {
        assertEquals(0.055f,
                HunterAbilityLogic.spiritualityRegen(0.05f, 9, true), 0.0001f);
        assertEquals(0.05f,
                HunterAbilityLogic.spiritualityRegen(0.05f, 9, false), 0.0001f);
        assertEquals(0.067f,
                HunterAbilityLogic.spiritualityRegen(0.067f, 8, true), 0.0001f);
    }
}
