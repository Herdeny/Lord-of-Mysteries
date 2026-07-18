package top.aurora.lordofmysteries.commission;

public final class CaseRecoveryPolicy {

    private static final String COUNTERFEIT_FORMULA_ID =
            "lord_of_mysteries:commission/counterfeit_formula";

    private CaseRecoveryPolicy() {}

    public static RecoveryPlan plan(
            String activeCommissionId,
            int activeQuestStep,
            boolean hasCommissionPaper,
            boolean hasFormulaDossier) {
        if (activeCommissionId == null || activeCommissionId.isBlank()) {
            return RecoveryPlan.NONE;
        }
        boolean restoreDossier = COUNTERFEIT_FORMULA_ID.equals(activeCommissionId)
                && activeQuestStep >= 2
                && activeQuestStep <= 4
                && !hasFormulaDossier;
        return new RecoveryPlan(
                !hasCommissionPaper,
                restoreDossier,
                restoreDossier && activeQuestStep >= 4);
    }

    public record RecoveryPlan(
            boolean restoreCommissionPaper,
            boolean restoreFormulaDossier,
            boolean recoveredDossierAppraised) {

        public static final RecoveryPlan NONE = new RecoveryPlan(
                false, false, false);

        public boolean needed() {
            return restoreCommissionPaper || restoreFormulaDossier;
        }
    }
}
