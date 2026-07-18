package top.aurora.lordofmysteries.commission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import top.aurora.lordofmysteries.player.PlayerMysteryData;

class CaseEvidenceViewTest {

    @Test
    void returnsEmptyEvidenceWithoutAnActiveCase() {
        CaseEvidenceView view = CaseEvidenceView.from(
                new PlayerMysteryData(),
                FormulaAppraisalService.DossierEvidence.NONE);

        assertEquals(CaseEvidenceView.EMPTY, view);
    }

    @Test
    void derivesMissingSquadEvidenceFromServerProgress() {
        PlayerMysteryData data = new PlayerMysteryData();
        data.activeCommissionId = CommissionService.MISSING_SQUAD.toString();
        data.activeQuestStep = 7;

        CaseEvidenceView view = CaseEvidenceView.from(
                data, FormulaAppraisalService.DossierEvidence.NONE);

        assertEquals(4, view.discovered());
        assertEquals(6, view.total());
        assertEquals(67, view.confidence());
        assertEquals(4, view.confirmed());
        assertEquals(0, view.suspicious());
        assertEquals(2, view.missing());
        assertFalse(view.conclusionReady());
        assertEquals(CaseAnalysisStage.CORRELATING, view.analysisStage());
        assertEquals(3, view.relations().size());
        assertEquals(EvidenceState.CONFIRMED, view.relations().get(2).state());
        assertTrue(view.relations().stream().noneMatch(
                relation -> relation.state() == EvidenceState.MISSING));
        assertEquals(
                "screen.lord_of_mysteries.analysis.missing_squad.next.rescue",
                view.nextActionKey());
        assertEquals(EvidenceState.CONFIRMED, view.entries().get(3).state());
        assertEquals(EvidenceState.MISSING, view.entries().get(4).state());
    }

    @Test
    void exposesAppraisedFormulaContradictionsWithoutClientInference() {
        PlayerMysteryData data = new PlayerMysteryData();
        data.activeCommissionId = CommissionService.COUNTERFEIT_FORMULA.toString();
        data.activeQuestStep = 4;
        FormulaAppraisalService.DossierEvidence dossier =
                new FormulaAppraisalService.DossierEvidence(
                        true, true, false, 0b101);

        CaseEvidenceView view = CaseEvidenceView.from(data, dossier);

        assertEquals(6, view.discovered());
        assertEquals(7, view.total());
        assertEquals(86, view.confidence());
        assertEquals(5, view.confirmed());
        assertEquals(1, view.suspicious());
        assertEquals(1, view.missing());
        assertTrue(view.conclusionReady());
        assertEquals(CaseAnalysisStage.READY, view.analysisStage());
        assertEquals(EvidenceRelationKind.CONTRADICTS,
                view.relations().get(2).kind());
        assertEquals(EvidenceState.SUSPICIOUS,
                view.relations().get(4).state());
        assertEquals(
                "screen.lord_of_mysteries.analysis.counterfeit_formula.theory.contradiction",
                view.theoryKey());
        assertEquals(EvidenceState.SUSPICIOUS, view.entries().get(4).state());
        assertEquals(EvidenceState.CONFIRMED, view.entries().get(5).state());
    }

    @Test
    void lostCatReasoningMovesFromCollectionToReady() {
        PlayerMysteryData data = new PlayerMysteryData();
        data.activeCommissionId = CommissionService.LOST_CAT.toString();

        CaseEvidenceView collecting = CaseEvidenceView.from(
                data, FormulaAppraisalService.DossierEvidence.NONE);

        assertEquals(0, collecting.confidence());
        assertEquals(CaseAnalysisStage.COLLECTING,
                collecting.analysisStage());
        assertTrue(collecting.relations().isEmpty());

        data.activeQuestStep = 4;
        CaseEvidenceView ready = CaseEvidenceView.from(
                data, FormulaAppraisalService.DossierEvidence.NONE);

        assertEquals(100, ready.confidence());
        assertEquals(CaseAnalysisStage.READY, ready.analysisStage());
        assertTrue(ready.relations().stream().allMatch(
                relation -> relation.state() == EvidenceState.CONFIRMED));
    }

    @Test
    void constructorClampsUntrustedSummaryValues() {
        CaseEvidenceView view = new CaseEvidenceView(
                "test", "test.title", 9, 2, 140, 9, 8, 7,
                false, CaseAnalysisStage.COLLECTING,
                "test.theory", "test.next", java.util.List.of(),
                java.util.List.of());

        assertEquals(2, view.discovered());
        assertEquals(100, view.confidence());
        assertEquals(2, view.confirmed());
        assertEquals(2, view.suspicious());
        assertEquals(2, view.missing());
    }
}
