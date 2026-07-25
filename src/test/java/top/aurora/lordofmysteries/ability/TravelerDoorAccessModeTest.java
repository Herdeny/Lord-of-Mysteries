package top.aurora.lordofmysteries.ability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class TravelerDoorAccessModeTest {

    private static final UUID OWNER = UUID.fromString(
            "0d2e862d-7010-4bd7-92a5-0e9cb8a73b31");
    private static final UUID OTHER = UUID.fromString(
            "e6d3264e-3610-46dc-9055-8a39a5fa9bc3");

    @Test
    void ownerAlwaysRetainsAccess() {
        for (TravelerDoorAccessMode mode
                : TravelerDoorAccessMode.values()) {
            assertTrue(mode.allows(
                    OWNER, "", OWNER, ""));
        }
    }

    @Test
    void privateAndPartyModesRejectUnauthorizedPlayers() {
        assertFalse(TravelerDoorAccessMode.PRIVATE.allows(
                OWNER, "alpha", OTHER, "alpha"));
        assertFalse(TravelerDoorAccessMode.PARTY.allows(
                OWNER, "", OTHER, ""));
        assertFalse(TravelerDoorAccessMode.PARTY.allows(
                OWNER, "alpha", OTHER, "beta"));
        assertTrue(TravelerDoorAccessMode.PARTY.allows(
                OWNER, "alpha", OTHER, "alpha"));
    }

    @Test
    void publicModeStillRejectsMissingIdentity() {
        assertTrue(TravelerDoorAccessMode.PUBLIC.allows(
                OWNER, "", OTHER, ""));
        assertFalse(TravelerDoorAccessMode.PUBLIC.allows(
                null, "", OTHER, ""));
        assertFalse(TravelerDoorAccessMode.PUBLIC.allows(
                OWNER, "", null, ""));
    }

    @Test
    void invalidValuesRepairToSafePartyDefault() {
        assertEquals(
                TravelerDoorAccessMode.PARTY,
                TravelerDoorAccessMode.fromId(null));
        assertEquals(
                TravelerDoorAccessMode.PARTY,
                TravelerDoorAccessMode.fromId("unknown"));
        assertEquals(
                TravelerDoorAccessMode.PRIVATE,
                TravelerDoorAccessMode.fromId(" PRIVATE "));
        assertEquals("", TravelerDoorAccessMode.normalizedTeam(
                "x".repeat(65)));
    }
}
