package top.aurora.lordofmysteries.commission;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import net.minecraft.resources.ResourceLocation;

class DynamicCaseOrganizationStancePolicyTest {

    @Test
    void stanceThresholdsCoverNegativeNeutralAndTrustedRelations() {
        assertEquals(DynamicCaseOrganizationStancePolicy.Stance.HOSTILE,
                DynamicCaseOrganizationStancePolicy.stance(-8));
        assertEquals(DynamicCaseOrganizationStancePolicy.Stance.WARY,
                DynamicCaseOrganizationStancePolicy.stance(-3));
        assertEquals(DynamicCaseOrganizationStancePolicy.Stance.NEUTRAL,
                DynamicCaseOrganizationStancePolicy.stance(2));
        assertEquals(DynamicCaseOrganizationStancePolicy.Stance.COOPERATIVE,
                DynamicCaseOrganizationStancePolicy.stance(7));
        assertEquals(DynamicCaseOrganizationStancePolicy.Stance.TRUSTED,
                DynamicCaseOrganizationStancePolicy.stance(8));
    }

    @Test
    void organizationReputationUsesStableRepositoryIds() {
        Map<ResourceLocation, Integer> reputations = new HashMap<>();
        DynamicCaseProfile.Organization organization =
                DynamicCaseProfile.Organization.MIST_CITY_PRESS;
        reputations.put(
                DynamicCaseOrganizationStancePolicy.reputationId(organization),
                6);

        assertEquals(6, DynamicCaseOrganizationStancePolicy.reputation(
                reputations, organization));
    }

    @Test
    void stanceChangesBranchesWithoutBypassingContactConsequences() {
        assertEquals(DynamicCaseResponseBranch.RECONCILIATION,
                DynamicCaseOrganizationStancePolicy.adjustBranch(
                        DynamicCaseResponseBranch.PRIORITY,
                        DynamicCaseOrganizationStancePolicy.Stance.HOSTILE));
        assertEquals(DynamicCaseResponseBranch.ROUTINE,
                DynamicCaseOrganizationStancePolicy.adjustBranch(
                        DynamicCaseResponseBranch.PRIORITY,
                        DynamicCaseOrganizationStancePolicy.Stance.WARY));
        assertEquals(DynamicCaseResponseBranch.PRIORITY,
                DynamicCaseOrganizationStancePolicy.adjustBranch(
                        DynamicCaseResponseBranch.ROUTINE,
                        DynamicCaseOrganizationStancePolicy.Stance.TRUSTED));
        assertEquals(DynamicCaseResponseBranch.RECONCILIATION,
                DynamicCaseOrganizationStancePolicy.adjustBranch(
                        DynamicCaseResponseBranch.RECONCILIATION,
                        DynamicCaseOrganizationStancePolicy.Stance.TRUSTED));
    }
}
