package top.aurora.lordofmysteries.ability;

import java.util.Map;

import net.minecraft.resources.ResourceLocation;

import top.aurora.lordofmysteries.ProjectMystery;

public final class TravelerDoorOrganizationPolicy {

    public static final int TRUSTED_REPUTATION = 8;
    public static final int MAX_ID_LENGTH = 128;

    private TravelerDoorOrganizationPolicy() {}

    public static String normalizeId(String requestedId) {
        if (requestedId == null) return "";
        String candidate = requestedId.trim().toLowerCase(
                java.util.Locale.ROOT);
        if (candidate.isEmpty() || candidate.length() > MAX_ID_LENGTH) {
            return "";
        }
        if (!candidate.contains(":")) {
            candidate = candidate.startsWith("organization/")
                    ? ProjectMystery.MOD_ID + ":" + candidate
                    : ProjectMystery.MOD_ID
                            + ":organization/" + candidate;
        }
        ResourceLocation id = ResourceLocation.tryParse(candidate);
        return id != null && id.getPath().startsWith("organization/")
                ? id.toString() : "";
    }

    public static int reputation(
            Map<ResourceLocation, Integer> reputations,
            String organizationId) {
        String normalized = normalizeId(organizationId);
        ResourceLocation id = ResourceLocation.tryParse(normalized);
        return reputations == null || id == null
                ? 0 : reputations.getOrDefault(id, 0);
    }

    public static boolean trusted(int reputation) {
        return reputation >= TRUSTED_REPUTATION;
    }

    public static boolean allows(
            String organizationId,
            int ownerReputation,
            int candidateReputation) {
        return !normalizeId(organizationId).isEmpty()
                && trusted(ownerReputation)
                && trusted(candidateReputation);
    }
}
