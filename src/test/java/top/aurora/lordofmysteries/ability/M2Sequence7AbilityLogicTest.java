package top.aurora.lordofmysteries.ability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class M2Sequence7AbilityLogicTest {

    @Test
    void playerMentalTargetsRespectServerConfig() {
        assertFalse(M2Sequence7AbilityLogic.canAffectMentalTarget(
                true, false, 20f));
        assertTrue(M2Sequence7AbilityLogic.canAffectMentalTarget(
                true, true, 20f));
    }

    @Test
    void powerfulNonPlayerTargetsResistMentalControl() {
        assertTrue(M2Sequence7AbilityLogic.canAffectMentalTarget(
                false, false, 100f));
        assertFalse(M2Sequence7AbilityLogic.canAffectMentalTarget(
                false, true, 101f));
    }

    @Test
    void crowdActingRequiresThreeTargets() {
        assertFalse(M2Sequence7AbilityLogic.crowdActingReady(2));
        assertTrue(M2Sequence7AbilityLogic.crowdActingReady(3));
    }

    @Test
    void psychologicalCloakRequiresThirtySeconds() {
        assertFalse(M2Sequence7AbilityLogic.sustainedActingReady(599));
        assertTrue(M2Sequence7AbilityLogic.sustainedActingReady(600));
    }

    @Test
    void fireAffinityRequiresTenSeconds() {
        assertFalse(M2Sequence7AbilityLogic.fireAffinityActingReady(199));
        assertTrue(M2Sequence7AbilityLogic.fireAffinityActingReady(200));
    }

    @Test
    void burningTargetsTakeBonusDamage() {
        assertEquals(6f,
                M2Sequence7AbilityLogic.burningTargetDamage(6f, false));
        assertEquals(7.2f,
                M2Sequence7AbilityLogic.burningTargetDamage(6f, true),
                0.001f);
    }
}
