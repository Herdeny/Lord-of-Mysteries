package top.aurora.lordofmysteries.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.minecraft.resources.ResourceLocation;

class PlayerMysteryDataSanitizerTest {

    @Test
    void clampsCorruptedCoreAndTrialValues() {
        PlayerMysteryData data = new PlayerMysteryData();
        data.pathway = ResourceLocation.fromNamespaceAndPath(
                "lord_of_mysteries", "seer");
        data.sequence = 99;
        data.spiritualityMax = Float.NaN;
        data.spirituality = 500f;
        data.digestion = -4f;
        data.pollution = 140f;
        data.insanityPressure = Float.POSITIVE_INFINITY;
        data.potionQuality = "unknown";
        data.m1TrialDeaths = -3;
        data.m1TrialMaxPressure = 120f;
        data.m1TrialSequence7Tick = -9L;
        data.commissionCooldowns.put(null, 20L);
        data.completedCommissions.add(null);
        data.caseDebriefs.put(null, null);
        data.caseHypotheses.put(null, null);
        data.knownKnowledge.add(null);
        data.actingCounters.put(null, -1);
        data.activeCommissionId = "lord_of_mysteries:commission/test";
        data.activeQuestChainId = "lord_of_mysteries:quest/test";
        data.activeQuestStep = 9;
        data.questResolutionRoute = "teleport";
        data.questResolutionReady = true;

        assertTrue(data.sanitize() > 0);
        assertEquals(9, data.sequence);
        assertEquals(100f, data.spiritualityMax);
        assertEquals(100f, data.spirituality);
        assertEquals(0f, data.digestion);
        assertEquals(100f, data.pollution);
        assertEquals(0f, data.insanityPressure);
        assertEquals("complete", data.potionQuality);
        assertEquals(0, data.m1TrialDeaths);
        assertEquals(100f, data.m1TrialMaxPressure);
        assertEquals(-1L, data.m1TrialSequence7Tick);
        assertTrue(data.commissionCooldowns.isEmpty());
        assertTrue(data.completedCommissions.isEmpty());
        assertTrue(data.caseDebriefs.isEmpty());
        assertTrue(data.caseHypotheses.isEmpty());
        assertTrue(data.knownKnowledge.isEmpty());
        assertTrue(data.actingCounters.isEmpty());
        assertEquals("", data.questResolutionRoute);
        assertFalse(data.questResolutionReady);
    }

    @Test
    void clearsOrphanedCommissionState() {
        PlayerMysteryData data = new PlayerMysteryData();
        data.activeCommissionId = "lord_of_mysteries:commission/test";
        data.activeQuestChainId = "";
        data.activeQuestStep = 8;
        data.questObjectiveProgress = 2;
        data.questDefenseWaveSpawned = true;
        data.questResolutionRoute = "assault";
        data.questResolutionReady = true;

        data.sanitize();
        assertEquals("", data.activeCommissionId);
        assertEquals("", data.activeQuestChainId);
        assertEquals(-1, data.activeQuestStep);
        assertEquals(0, data.questObjectiveProgress);
        assertFalse(data.questDefenseWaveSpawned);
        assertEquals("", data.questResolutionRoute);
        assertFalse(data.questResolutionReady);
    }

    @Test
    void validRuntimeStateNeedsNoRepair() {
        PlayerMysteryData data = new PlayerMysteryData();
        assertEquals(0, data.sanitize());
    }

}
