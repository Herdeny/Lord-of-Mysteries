package top.aurora.lordofmysteries.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.minecraft.resources.ResourceLocation;

import top.aurora.lordofmysteries.commission.CaseGrade;
import top.aurora.lordofmysteries.commission.DynamicCaseHistoryEntry;
import top.aurora.lordofmysteries.commission.DynamicCaseProfile;
import top.aurora.lordofmysteries.commission.DynamicCaseRelationshipPolicy;
import top.aurora.lordofmysteries.commission.DynamicCaseResponseTask;
import top.aurora.lordofmysteries.commission.DynamicCaseWeeklyDirective;

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
        data.dynamicCaseHistory.add(historyEntry(7L, "duplicate"));
        data.dynamicCaseHistory.add(historyEntry(8L, "duplicate"));
        data.dynamicCaseHistory.add(null);
        data.dynamicCaseContactStandings.put(
                DynamicCaseProfile.Subject.DOCK_ACCOUNTANT, 99);
        data.dynamicCaseContactStandings.put(
                DynamicCaseProfile.Subject.HERBALIST_ASSISTANT, 0);
        data.organizationResponseTask = new DynamicCaseResponseTask(
                "duplicate",
                DynamicCaseProfile.Organization.MIST_CITY_PRESS,
                DynamicCaseProfile.Subject.APPRENTICE_REPORTER,
                DynamicCaseWeeklyDirective.SOURCE_VERIFICATION,
                8L, 11L,
                DynamicCaseResponseTask.Stage.ASSIGNED);
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
        assertEquals(1, data.dynamicCaseHistory.size());
        assertEquals(8L, data.dynamicCaseHistory.get(0).caseDay());
        assertEquals(DynamicCaseRelationshipPolicy.MAX_STANDING,
                data.dynamicCaseContactStandings.get(
                        DynamicCaseProfile.Subject.DOCK_ACCOUNTANT));
        assertFalse(data.dynamicCaseContactStandings.containsKey(
                DynamicCaseProfile.Subject.HERBALIST_ASSISTANT));
        assertNull(data.organizationResponseTask);
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

    @Test
    void validOrganizationResponseStateNeedsNoRepair() {
        PlayerMysteryData data = new PlayerMysteryData();
        DynamicCaseHistoryEntry history = new DynamicCaseHistoryEntry(
                14L, 2L, "response-valid",
                DynamicCaseProfile.Archetype.MISSING_PERSON,
                DynamicCaseProfile.Subject.APPRENTICE_REPORTER,
                DynamicCaseProfile.Organization.MIST_CITY_PRESS,
                DynamicCaseProfile.CaseLocation.MIST_CITY_OUTPOST,
                CaseGrade.A, 84, 900L, 2,
                DynamicCaseHistoryEntry.FollowUpStatus.CLAIMED);
        data.dynamicCaseHistory.add(history);
        data.dynamicCaseContactStandings.put(
                history.subject(), 5);
        data.organizationResponseTask = new DynamicCaseResponseTask(
                history.instanceId(),
                history.organization(),
                history.subject(),
                DynamicCaseWeeklyDirective.PUBLIC_REASSURANCE,
                14L, 17L,
                DynamicCaseResponseTask.Stage.BRIEFED);

        assertEquals(0, data.sanitize());
        assertEquals(5, data.dynamicCaseContactStandings.get(
                history.subject()));
        assertEquals("response-valid",
                data.organizationResponseTask.instanceId());
    }

    @Test
    void preservesDynamicCaseRecoveryAndConclusionRoutes() {
        PlayerMysteryData data = new PlayerMysteryData();
        data.activeCommissionId =
                "lord_of_mysteries:commission/dynamic_case_rotation";
        data.activeQuestChainId =
                "lord_of_mysteries:quest/dynamic_case_rotation";
        data.activeQuestStep = 3;
        data.questResolutionRoute = "reconsider";

        assertEquals(0, data.sanitize());
        assertEquals("reconsider", data.questResolutionRoute);

        data.questResolutionRoute = "ritual_diversion";
        data.questResolutionReady = true;
        assertEquals(0, data.sanitize());
        assertEquals("ritual_diversion", data.questResolutionRoute);
        assertTrue(data.questResolutionReady);
    }

    private static DynamicCaseHistoryEntry historyEntry(
            long caseDay, String instanceId) {
        return new DynamicCaseHistoryEntry(
                caseDay, Math.floorDiv(caseDay, 7L), instanceId,
                DynamicCaseProfile.Archetype.MISSING_PERSON,
                DynamicCaseProfile.Subject.APPRENTICE_REPORTER,
                DynamicCaseProfile.Organization.MIST_CITY_PRESS,
                DynamicCaseProfile.CaseLocation.MIST_CITY_OUTPOST,
                CaseGrade.A, 84, 900L, 2,
                DynamicCaseHistoryEntry.FollowUpStatus.PENDING);
    }

}
