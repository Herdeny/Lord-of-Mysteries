package top.aurora.lordofmysteries.commission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DynamicCaseSchedulePolicyTest {

    @Test
    void matchingQuarterOpensObservationWindow() {
        DynamicCaseProfile profile = profileWith(
                DynamicCaseProfile.Schedule.DOCK_AFTERNOON);

        DynamicCaseSchedulePolicy.State state =
                DynamicCaseSchedulePolicy.state(profile, 7_000L);

        assertEquals(DynamicCaseProfile.DayPeriod.AFTERNOON,
                state.currentPeriod());
        assertTrue(state.observationOpen());
        assertEquals(0L, state.ticksUntilOpen());
        assertEquals(0L, state.minutesUntilOpen());
    }

    @Test
    void waitWrapsAcrossMidnightWithoutLockingTheCase() {
        DynamicCaseProfile profile = profileWith(
                DynamicCaseProfile.Schedule.PRESS_MORNING);

        DynamicCaseSchedulePolicy.State state =
                DynamicCaseSchedulePolicy.state(profile, 18_500L);

        assertEquals(DynamicCaseProfile.DayPeriod.NIGHT,
                state.currentPeriod());
        assertFalse(state.observationOpen());
        assertEquals(5_500L, state.ticksUntilOpen());
        assertEquals(5L, state.minutesUntilOpen());
    }

    @Test
    void dayPeriodsCoverTheWholeMinecraftDay() {
        assertEquals(DynamicCaseProfile.DayPeriod.MORNING,
                DynamicCaseProfile.DayPeriod.at(0L));
        assertEquals(DynamicCaseProfile.DayPeriod.AFTERNOON,
                DynamicCaseProfile.DayPeriod.at(6_000L));
        assertEquals(DynamicCaseProfile.DayPeriod.EVENING,
                DynamicCaseProfile.DayPeriod.at(12_000L));
        assertEquals(DynamicCaseProfile.DayPeriod.NIGHT,
                DynamicCaseProfile.DayPeriod.at(18_000L));
        assertEquals(DynamicCaseProfile.DayPeriod.MORNING,
                DynamicCaseProfile.DayPeriod.at(24_000L));
    }

    private static DynamicCaseProfile profileWith(
            DynamicCaseProfile.Schedule schedule) {
        for (long day = 0L; day < 128L; day++) {
            DynamicCaseProfile profile =
                    DynamicCaseGenerator.generateForDay(912837L, day);
            if (profile.schedule() == schedule) return profile;
        }
        throw new AssertionError("schedule was not generated");
    }
}
