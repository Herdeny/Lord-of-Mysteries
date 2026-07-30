package top.aurora.lordofmysteries.ability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class TravelerDoorPolicyTest {

    private static final UUID OWNER =
            UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID MEMBER =
            UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Test
    void ownerAlwaysCrossesAndBlockedPlayersNeverBypassPublicMode() {
        assertTrue(TravelerDoorPolicy.allows(
                OWNER,
                "alpha",
                TravelerDoorAccessMode.PRIVATE,
                Set.of(OWNER),
                OWNER,
                ""));
        assertFalse(TravelerDoorPolicy.allows(
                OWNER,
                "alpha",
                TravelerDoorAccessMode.PUBLIC,
                Set.of(MEMBER),
                MEMBER,
                "alpha"));
    }

    @Test
    void partyAccessStillAppliesAfterBlacklistCheck() {
        assertTrue(TravelerDoorPolicy.allows(
                OWNER,
                "alpha",
                TravelerDoorAccessMode.PARTY,
                Set.of(),
                MEMBER,
                "alpha"));
        assertFalse(TravelerDoorPolicy.allows(
                OWNER,
                "alpha",
                TravelerDoorAccessMode.PARTY,
                Set.of(),
                MEMBER,
                "beta"));
    }

    @Test
    void organizationAccessRequiresMutualTrustAndStillHonorsBlocklist() {
        String organization =
                "lord_of_mysteries:organization/detective_agency";
        assertTrue(TravelerDoorPolicy.allows(
                OWNER,
                "",
                TravelerDoorAccessMode.ORGANIZATION,
                organization,
                8,
                Set.of(),
                MEMBER,
                "",
                9));
        assertFalse(TravelerDoorPolicy.allows(
                OWNER,
                "",
                TravelerDoorAccessMode.ORGANIZATION,
                organization,
                8,
                Set.of(MEMBER),
                MEMBER,
                "",
                9));
        assertFalse(TravelerDoorPolicy.allows(
                OWNER,
                "",
                TravelerDoorAccessMode.ORGANIZATION,
                organization,
                8,
                Set.of(),
                MEMBER,
                "",
                7));
    }

    @Test
    void namesRemoveFormattingControlsAndRepeatedWhitespace() {
        assertEquals(
                "North Gate Safe",
                TravelerDoorPolicy.normalizeName(
                        "  \u00a7cNorth\tGate\nSafe  "));
        assertEquals("", TravelerDoorPolicy.normalizeName("\u0000\u00a7c"));
    }

    @Test
    void namesUseCodePointLimitWithoutSplittingUnicode() {
        String normalized = TravelerDoorPolicy.normalizeName(
                "门".repeat(TravelerDoorPolicy.MAX_NAME_LENGTH + 5));
        assertEquals(
                TravelerDoorPolicy.MAX_NAME_LENGTH,
                normalized.codePointCount(0, normalized.length()));
    }

    @Test
    void blacklistIsDeterministicDeduplicatedAndBounded() {
        ArrayList<UUID> values = new ArrayList<>();
        for (int index = 40; index >= 0; index--) {
            values.add(new UUID(0L, index + 1L));
        }
        values.add(null);
        values.add(values.get(0));
        for (int index = 0; index < 40; index++) {
            values.add(new UUID(0L, 1L));
        }

        Set<UUID> normalized =
                TravelerDoorPolicy.normalizeBlacklist(values);

        assertEquals(
                TravelerDoorPolicy.MAX_BLOCKED_PLAYERS,
                normalized.size());
        assertEquals(
                normalized,
                TravelerDoorPolicy.normalizeBlacklist(
                        new HashSet<>(values)));
        assertTrue(normalized.contains(new UUID(0L, 32L)));
    }
}
