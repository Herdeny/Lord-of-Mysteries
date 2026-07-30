package top.aurora.lordofmysteries.ability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import net.minecraft.resources.ResourceLocation;

class TravelerDoorOrganizationPolicyTest {

    private static final String DETECTIVE_AGENCY =
            "lord_of_mysteries:organization/detective_agency";

    @Test
    void normalizesShortAndNamespacedOrganizationIds() {
        assertEquals(
                DETECTIVE_AGENCY,
                TravelerDoorOrganizationPolicy.normalizeId(
                        "detective_agency"));
        assertEquals(
                DETECTIVE_AGENCY,
                TravelerDoorOrganizationPolicy.normalizeId(
                        "organization/detective_agency"));
        assertEquals(
                DETECTIVE_AGENCY,
                TravelerDoorOrganizationPolicy.normalizeId(
                        DETECTIVE_AGENCY));
        assertEquals(
                "",
                TravelerDoorOrganizationPolicy.normalizeId(
                        "lord_of_mysteries:not_an_organization"));
    }

    @Test
    void bothOwnerAndCandidateMustBeTrusted() {
        assertTrue(TravelerDoorOrganizationPolicy.allows(
                DETECTIVE_AGENCY, 8, 12));
        assertFalse(TravelerDoorOrganizationPolicy.allows(
                DETECTIVE_AGENCY, 7, 12));
        assertFalse(TravelerDoorOrganizationPolicy.allows(
                DETECTIVE_AGENCY, 12, 7));
        assertFalse(TravelerDoorOrganizationPolicy.allows(
                "", 12, 12));
    }

    @Test
    void reputationLookupUsesStableResourceLocation() {
        ResourceLocation id = ResourceLocation.parse(
                DETECTIVE_AGENCY);
        assertEquals(
                9,
                TravelerDoorOrganizationPolicy.reputation(
                        Map.of(id, 9), "detective_agency"));
        assertEquals(
                0,
                TravelerDoorOrganizationPolicy.reputation(
                        Map.of(), "detective_agency"));
    }
}
