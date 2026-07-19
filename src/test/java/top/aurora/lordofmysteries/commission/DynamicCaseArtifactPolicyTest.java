package top.aurora.lordofmysteries.commission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DynamicCaseArtifactPolicyTest {

    @Test
    void acceptanceOnlyRequiresThePortfolio() {
        DynamicCaseArtifactPolicy.RecoveryPlan plan =
                DynamicCaseArtifactPolicy.plan(0, false, false);

        assertTrue(plan.restorePortfolio());
        assertFalse(plan.restoreEvidenceSample());
        assertEquals(1, plan.artifactCount());
    }

    @Test
    void progressedCaseRestoresBothMissingArtifacts() {
        DynamicCaseArtifactPolicy.RecoveryPlan plan =
                DynamicCaseArtifactPolicy.plan(2, false, false);

        assertTrue(plan.restorePortfolio());
        assertTrue(plan.restoreEvidenceSample());
        assertEquals(2, plan.artifactCount());
    }

    @Test
    void existingArtifactsAreNeverDuplicated() {
        DynamicCaseArtifactPolicy.RecoveryPlan plan =
                DynamicCaseArtifactPolicy.plan(3, true, true);

        assertFalse(plan.restorePortfolio());
        assertFalse(plan.restoreEvidenceSample());
        assertEquals(0, plan.artifactCount());
    }
}
