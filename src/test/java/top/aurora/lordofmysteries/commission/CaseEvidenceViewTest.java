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
        assertFalse(view.conclusionReady());
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
        assertTrue(view.conclusionReady());
        assertEquals(EvidenceState.SUSPICIOUS, view.entries().get(4).state());
        assertEquals(EvidenceState.CONFIRMED, view.entries().get(5).state());
    }
}
