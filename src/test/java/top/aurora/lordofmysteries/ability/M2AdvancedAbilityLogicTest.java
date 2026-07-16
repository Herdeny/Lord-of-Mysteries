package top.aurora.lordofmysteries.ability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class M2AdvancedAbilityLogicTest {

    @Test
    void sequenceSevenSupportsCurrentAndInheritedAbilityLayers() {
        assertEquals(7, M2AdvancedAbilityLogic.selectedSequence(7, false, false));
        assertEquals(8, M2AdvancedAbilityLogic.selectedSequence(7, true, false));
        assertEquals(9, M2AdvancedAbilityLogic.selectedSequence(7, false, true));
    }

    @Test
    void sequenceEightUsesSneakForFoundationAbilities() {
        assertEquals(8, M2AdvancedAbilityLogic.selectedSequence(8, false, false));
        assertEquals(9, M2AdvancedAbilityLogic.selectedSequence(8, true, false));
    }

    @Test
    void swapRequiresRangeSightAndPvpPermission() {
        assertTrue(M2AdvancedAbilityLogic.canSwap(64d, true, false, false));
        assertFalse(M2AdvancedAbilityLogic.canSwap(64.1d, true, false, true));
        assertFalse(M2AdvancedAbilityLogic.canSwap(16d, false, false, true));
        assertFalse(M2AdvancedAbilityLogic.canSwap(16d, true, true, false));
    }

    @Test
    void runeMasteryNeedsThreeDistinctTargets() {
        assertFalse(M2AdvancedAbilityLogic.runeMasteryReady(2));
        assertTrue(M2AdvancedAbilityLogic.runeMasteryReady(3));
    }

    @Test
    void perfectCrimeWindowLastsThirtySeconds() {
        assertTrue(M2AdvancedAbilityLogic.perfectCrimeReady(100L, 700L));
        assertFalse(M2AdvancedAbilityLogic.perfectCrimeReady(100L, 701L));
    }

    @Test
    void knowledgeLinkChanceScalesAndCaps() {
        assertEquals(0.35f, M2AdvancedAbilityLogic.knowledgeLinkChance(2), 0.001f);
        assertEquals(0.85f, M2AdvancedAbilityLogic.knowledgeLinkChance(100), 0.001f);
    }

    @Test
    void astrologyRequiresNightAndOpenSky() {
        assertTrue(M2AdvancedAbilityLogic.astrologyAvailable(true, true));
        assertFalse(M2AdvancedAbilityLogic.astrologyAvailable(false, true));
        assertFalse(M2AdvancedAbilityLogic.astrologyAvailable(true, false));
    }

    @Test
    void starAtlasNeedsFiveMoonPhases() {
        assertFalse(M2AdvancedAbilityLogic.moonAtlasReady(4));
        assertTrue(M2AdvancedAbilityLogic.moonAtlasReady(5));
    }

    @Test
    void honestDayRequiresCleanCompletedDay() {
        assertTrue(M2AdvancedAbilityLogic.honestDayReady(3L, 4L, false));
        assertFalse(M2AdvancedAbilityLogic.honestDayReady(3L, 4L, true));
        assertFalse(M2AdvancedAbilityLogic.honestDayReady(-1L, 4L, false));
    }
}
