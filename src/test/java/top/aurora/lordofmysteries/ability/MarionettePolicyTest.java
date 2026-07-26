package top.aurora.lordofmysteries.ability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class MarionettePolicyTest {

    @Test
    void acceptsOnlyWeakenedHostileNonPlayerTargets() {
        assertTrue(MarionettePolicy.canCreate(
                false, true, false, 40f, 8f, 0));
        assertFalse(MarionettePolicy.canCreate(
                true, true, false, 40f, 8f, 0));
        assertFalse(MarionettePolicy.canCreate(
                false, false, false, 40f, 8f, 0));
        assertFalse(MarionettePolicy.canCreate(
                false, true, false, 40f, 8.01f, 0));
        assertFalse(MarionettePolicy.canCreate(
                false, true, false, 81f, 8f, 0));

        UUID owner = UUID.randomUUID();
        UUID otherOwner = UUID.randomUUID();
        assertFalse(MarionettePolicy.canDamage(
                owner, UUID.randomUUID(), true, null));
        assertFalse(MarionettePolicy.canDamage(
                owner, UUID.randomUUID(), false, otherOwner));
        assertFalse(MarionettePolicy.canDamage(
                null, owner, false, owner));
        assertTrue(MarionettePolicy.canDamage(
                null, otherOwner, false, owner));
    }

    @Test
    void rejectsForeignOwnershipAndFullRosters() {
        assertFalse(MarionettePolicy.canCreate(
                false, true, true, 40f, 4f, 0));
        assertFalse(MarionettePolicy.canCreate(
                false, true, false, 40f, 4f,
                MarionettePolicy.MAX_MARIONETTES));
    }

    @Test
    void rosterNormalizationPreservesStableSlots() {
        UUID first = UUID.fromString(
                "00000000-0000-0000-0000-000000000001");
        UUID second = UUID.fromString(
                "00000000-0000-0000-0000-000000000002");
        UUID third = UUID.fromString(
                "00000000-0000-0000-0000-000000000003");
        UUID fourth = UUID.fromString(
                "00000000-0000-0000-0000-000000000004");

        assertEquals(
                List.of(second, first, third),
                MarionettePolicy.normalizeRoster(List.of(
                        second, first, second, third, fourth)));
    }
}
