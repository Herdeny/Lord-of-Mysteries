package top.aurora.lordofmysteries.ability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class M2FoundationAbilityLogicTest {

    @Test
    void pilferRequiresRangeAndExpiredTargetLock() {
        assertTrue(M2FoundationAbilityLogic.canPilfer(9d, 99L, 100L));
        assertFalse(M2FoundationAbilityLogic.canPilfer(9.01d, 99L, 100L));
        assertFalse(M2FoundationAbilityLogic.canPilfer(4d, 101L, 100L));
    }

    @Test
    void playerTheftIsMoreLikelyToBeDetected() {
        float mobChance = M2FoundationAbilityLogic.alertChance(false, false, 0f);
        float playerChance = M2FoundationAbilityLogic.alertChance(true, false, 0f);
        assertTrue(playerChance > mobChance);
        assertEquals(0.30f, mobChance);
        assertEquals(0.75f, playerChance);
    }

    @Test
    void luckReducesButCannotRemoveDetectionRisk() {
        assertEquals(0.10f,
                M2FoundationAbilityLogic.alertChance(false, false, 100f));
    }

    @Test
    void shadowStepNeedsThirtySeconds() {
        assertFalse(M2FoundationAbilityLogic.shadowActingReady(599));
        assertTrue(M2FoundationAbilityLogic.shadowActingReady(600));
    }

    @Test
    void onlyStableKnowledgeEntriesCanBeCopied() {
        assertTrue(M2FoundationAbilityLogic.copyableKnowledge(
                "lord_of_mysteries:knowledge/m2/commission_system"));
        assertFalse(M2FoundationAbilityLogic.copyableKnowledge(
                "lord_of_mysteries:knowledge/thief_9_acting"));
        assertFalse(M2FoundationAbilityLogic.copyableKnowledge(
                "minecraft:book"));
    }

    @Test
    void fieldNotesRequireThreeBiomes() {
        assertFalse(M2FoundationAbilityLogic.fieldNoteReady(2));
        assertTrue(M2FoundationAbilityLogic.fieldNoteReady(3));
    }
}
