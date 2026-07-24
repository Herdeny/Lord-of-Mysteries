package top.aurora.lordofmysteries.ability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class M3LaunchAbilityLogicTest {

    @Test
    void supportsExactlyFiveLaunchPathwaysAtSequencesSixAndFive() {
        List<String> pathways =
                List.of("seer", "spectator", "hunter", "thief", "apprentice");
        for (String pathway : pathways) {
            assertTrue(M3LaunchAbilityLogic.supports(pathway, 6));
            assertTrue(M3LaunchAbilityLogic.supports(pathway, 5));
            assertFalse(M3LaunchAbilityLogic.supports(pathway, 7));
            assertFalse(M3LaunchAbilityLogic.supports(pathway, 4));
        }
        assertFalse(M3LaunchAbilityLogic.supports("assassin", 6));
        assertFalse(M3LaunchAbilityLogic.supports("", 5));
        assertFalse(M3LaunchAbilityLogic.supports(null, 6));
    }

    @Test
    void controlAbilitiesNeverAffectPlayersOrBossScaleTargets() {
        assertTrue(M3LaunchAbilityLogic.canControl(false, 80f));
        assertFalse(M3LaunchAbilityLogic.canControl(true, 20f));
        assertFalse(M3LaunchAbilityLogic.canControl(false, 81f));
        assertFalse(M3LaunchAbilityLogic.canControl(false, 0f));
    }

    @Test
    void threadRestraintRequiresAWeakenedNonPlayerTarget() {
        assertTrue(M3LaunchAbilityLogic.canRestrain(false, 60f, 21f));
        assertFalse(M3LaunchAbilityLogic.canRestrain(false, 60f, 21.1f));
        assertFalse(M3LaunchAbilityLogic.canRestrain(true, 20f, 1f));
        assertFalse(M3LaunchAbilityLogic.canRestrain(false, 100f, 1f));
    }

    @Test
    void retrievalRespectsVanillaItemOwnership() {
        assertTrue(M3LaunchAbilityLogic.canRetrieveItem(false, false));
        assertTrue(M3LaunchAbilityLogic.canRetrieveItem(true, true));
        assertFalse(M3LaunchAbilityLogic.canRetrieveItem(true, false));
    }

    @Test
    void signedBookCopyHonorsVanillaGenerationLimit() {
        assertEquals(1, M3LaunchAbilityLogic.copiedBookGeneration(0));
        assertEquals(2, M3LaunchAbilityLogic.copiedBookGeneration(1));
        assertEquals(-1, M3LaunchAbilityLogic.copiedBookGeneration(2));
        assertEquals(-1, M3LaunchAbilityLogic.copiedBookGeneration(-1));
    }
}
