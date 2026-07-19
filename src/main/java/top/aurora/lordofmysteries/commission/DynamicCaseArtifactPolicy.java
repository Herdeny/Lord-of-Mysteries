package top.aurora.lordofmysteries.commission;

final class DynamicCaseArtifactPolicy {

    private DynamicCaseArtifactPolicy() {
    }

    static RecoveryPlan plan(
            int activeStep, boolean hasPortfolio, boolean hasEvidenceSample) {
        return new RecoveryPlan(
                !hasPortfolio,
                activeStep > 0 && !hasEvidenceSample);
    }

    record RecoveryPlan(boolean restorePortfolio, boolean restoreEvidenceSample) {
        int artifactCount() {
            return (restorePortfolio ? 1 : 0)
                    + (restoreEvidenceSample ? 1 : 0);
        }
    }
}
