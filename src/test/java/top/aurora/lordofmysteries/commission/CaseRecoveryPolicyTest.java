package top.aurora.lordofmysteries.commission;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CaseRecoveryPolicyTest {

    @Test
    void noActiveCaseNeverRestoresItems() {
        assertFalse(CaseRecoveryPolicy.plan("", 3, false, false).needed());
        assertFalse(CaseRecoveryPolicy.plan(null, 3, false, false).needed());
    }

    @Test
    void missingCommissionPaperIsRestoredOnce() {
        CaseRecoveryPolicy.RecoveryPlan missing = CaseRecoveryPolicy.plan(
                CommissionService.MISSING_SQUAD.toString(), 7, false, false);
        CaseRecoveryPolicy.RecoveryPlan present = CaseRecoveryPolicy.plan(
                CommissionService.MISSING_SQUAD.toString(), 7, true, false);

        assertTrue(missing.restoreCommissionPaper());
        assertFalse(missing.restoreFormulaDossier());
        assertFalse(present.needed());
    }

    @Test
    void formulaDossierCanBeRecoveredOnlyDuringRequiredSteps() {
        String formula = CommissionService.COUNTERFEIT_FORMULA.toString();

        CaseRecoveryPolicy.RecoveryPlan receiveStep = CaseRecoveryPolicy.plan(
                formula, 2, true, false);
        CaseRecoveryPolicy.RecoveryPlan appraisalStep = CaseRecoveryPolicy.plan(
                formula, 3, true, false);
        CaseRecoveryPolicy.RecoveryPlan verdictStep = CaseRecoveryPolicy.plan(
                formula, 4, true, false);

        assertTrue(receiveStep.restoreFormulaDossier());
        assertFalse(receiveStep.recoveredDossierAppraised());
        assertTrue(appraisalStep.restoreFormulaDossier());
        assertFalse(appraisalStep.recoveredDossierAppraised());
        assertTrue(verdictStep.restoreFormulaDossier());
        assertTrue(verdictStep.recoveredDossierAppraised());
        assertFalse(CaseRecoveryPolicy.plan(
                formula, 1, true, false).restoreFormulaDossier());
        assertFalse(CaseRecoveryPolicy.plan(
                formula, 5, true, false).restoreFormulaDossier());
    }

    @Test
    void existingFormulaDossierIsNeverDuplicated() {
        CaseRecoveryPolicy.RecoveryPlan plan = CaseRecoveryPolicy.plan(
                CommissionService.COUNTERFEIT_FORMULA.toString(),
                3, true, true);

        assertFalse(plan.needed());
    }
}
