package top.aurora.lordofmysteries.organization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;

class OrganizationLiaisonSchedulePolicyTest {

    @Test
    void threeLiaisonsUseDistinctPositionsAcrossEveryShift() {
        BlockPos outpost = new BlockPos(100, 64, -30);
        long[] times = {0L, 6_000L, 18_000L, 42_000L};

        for (long time : times) {
            BlockPos first = OrganizationLiaisonSchedulePolicy.position(
                    outpost, 1, time);
            BlockPos second = OrganizationLiaisonSchedulePolicy.position(
                    outpost, 2, time);
            BlockPos third = OrganizationLiaisonSchedulePolicy.position(
                    outpost, 3, time);
            assertNotEquals(first, second);
            assertNotEquals(second, third);
            assertNotEquals(first, third);
            assertEquals(outpost.getY() + 1, first.getY());
            assertEquals(outpost.getY() + 1, second.getY());
            assertEquals(outpost.getY() + 1, third.getY());
        }
        assertEquals(
                OrganizationLiaisonSchedulePolicy.Shift.BRIEFING,
                OrganizationLiaisonSchedulePolicy.shift(24_100L));
        assertEquals(
                OrganizationLiaisonSchedulePolicy.Shift.SERVICE,
                OrganizationLiaisonSchedulePolicy.shift(6_000L));
        assertEquals(
                OrganizationLiaisonSchedulePolicy.Shift.WATCH,
                OrganizationLiaisonSchedulePolicy.shift(18_000L));
        assertThrows(IllegalArgumentException.class, () ->
                OrganizationLiaisonSchedulePolicy.position(
                        outpost, 4, 0L));
    }
}
