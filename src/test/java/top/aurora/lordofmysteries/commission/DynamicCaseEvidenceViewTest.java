package top.aurora.lordofmysteries.commission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import top.aurora.lordofmysteries.player.PlayerMysteryData;

class DynamicCaseEvidenceViewTest {

    private static final DynamicCaseProfile PROFILE =
            DynamicCaseGenerator.generateForDay(12345L, 8L);

    @Test
    void briefingStartsWithOneConfirmedRecord() {
        PlayerMysteryData data = activeData(0);

        CaseEvidenceView view = CaseEvidenceView.from(
                data, FormulaAppraisalService.DossierEvidence.NONE, PROFILE);

        assertEquals(1, view.discovered());
        assertEquals(5, view.total());
        assertEquals(20, view.confidence());
        assertFalse(view.conclusionReady());
        assertEquals(CaseAnalysisStage.COLLECTING, view.analysisStage());
        assertTrue(view.relations().isEmpty());
    }

    @Test
    void threeInvestigationsRevealTruthAndPlantedFalseLead() {
        PlayerMysteryData data = activeData(3);

        CaseEvidenceView view = CaseEvidenceView.from(
                data, FormulaAppraisalService.DossierEvidence.NONE, PROFILE);

        assertEquals(5, view.discovered());
        assertEquals(4, view.confirmed());
        assertEquals(1, view.suspicious());
        assertTrue(view.conclusionReady());
        assertEquals(CaseAnalysisStage.READY, view.analysisStage());
        assertEquals(5, view.relations().size());
        assertEquals(EvidenceRelationKind.CONTRADICTS,
                view.relations().get(3).kind());
        assertEquals(EvidenceState.SUSPICIOUS,
                view.relations().get(3).state());
        assertEquals(
                "screen.lord_of_mysteries.evidence.dynamic_case.false_lead.title",
                view.entries().get(4).titleKey());
        assertEquals(
                "screen.lord_of_mysteries.analysis.dynamic_case.next.conclude",
                view.nextActionKey());
    }

    @Test
    void recoveryTurnsTheFalseLeadIntoAConfirmedContradiction() {
        PlayerMysteryData data = activeData(3);
        data.questResolutionRoute = "recovered";

        CaseEvidenceView view = CaseEvidenceView.from(
                data, FormulaAppraisalService.DossierEvidence.NONE, PROFILE);

        assertEquals(5, view.confirmed());
        assertEquals(0, view.suspicious());
        assertEquals(EvidenceState.CONFIRMED, view.entries().get(4).state());
        assertEquals(
                "screen.lord_of_mysteries.evidence.dynamic_case.false_lead.revealed.title",
                view.entries().get(4).titleKey());
        assertEquals(EvidenceState.CONFIRMED,
                view.relations().get(3).state());
    }

    private static PlayerMysteryData activeData(int step) {
        PlayerMysteryData data = new PlayerMysteryData();
        data.activeCommissionId = CommissionService.DYNAMIC_CASE.toString();
        data.activeQuestChainId =
                "lord_of_mysteries:quest/dynamic_case_rotation";
        data.activeQuestStep = step;
        data.commissionAcceptedTick = PROFILE.caseDay()
                * DynamicCaseGenerator.TICKS_PER_CASE_DAY;
        return data;
    }
}
