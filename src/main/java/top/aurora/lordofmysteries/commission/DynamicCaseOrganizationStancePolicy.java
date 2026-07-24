package top.aurora.lordofmysteries.commission;

import java.util.Locale;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;

import top.aurora.lordofmysteries.ProjectMystery;

public final class DynamicCaseOrganizationStancePolicy {

    private DynamicCaseOrganizationStancePolicy() {}

    public static ResourceLocation reputationId(
            DynamicCaseProfile.Organization organization) {
        if (organization == null) {
            throw new IllegalArgumentException("organization is required");
        }
        return ResourceLocation.fromNamespaceAndPath(
                ProjectMystery.MOD_ID, organization.reputationPath());
    }

    public static int reputation(
            Map<ResourceLocation, Integer> reputations,
            DynamicCaseProfile.Organization organization) {
        if (reputations == null) {
            throw new IllegalArgumentException("reputations are required");
        }
        return reputations.getOrDefault(reputationId(organization), 0);
    }

    public static Stance stance(int reputation) {
        if (reputation <= -8) return Stance.HOSTILE;
        if (reputation <= -3) return Stance.WARY;
        if (reputation <= 2) return Stance.NEUTRAL;
        if (reputation <= 7) return Stance.COOPERATIVE;
        return Stance.TRUSTED;
    }

    public static DynamicCaseResponseBranch adjustBranch(
            DynamicCaseResponseBranch contactBranch,
            Stance stance) {
        if (contactBranch == null || stance == null) {
            throw new IllegalArgumentException(
                    "response branch and organization stance are required");
        }
        if (stance == Stance.HOSTILE) {
            return DynamicCaseResponseBranch.RECONCILIATION;
        }
        if (stance == Stance.WARY
                && contactBranch == DynamicCaseResponseBranch.PRIORITY) {
            return DynamicCaseResponseBranch.ROUTINE;
        }
        if (stance == Stance.TRUSTED
                && contactBranch == DynamicCaseResponseBranch.ROUTINE) {
            return DynamicCaseResponseBranch.PRIORITY;
        }
        return contactBranch;
    }

    public enum Stance {
        HOSTILE,
        WARY,
        NEUTRAL,
        COOPERATIVE,
        TRUSTED;

        public String id() {
            return name().toLowerCase(Locale.ROOT);
        }

        public String translationKey() {
            return "dynamic_case.lord_of_mysteries.organization_stance." + id();
        }
    }
}
