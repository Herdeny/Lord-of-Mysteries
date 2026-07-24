package top.aurora.lordofmysteries.commission;

final class DynamicCaseSchedulePolicy {

    static final long TICKS_PER_DAY = 24_000L;
    static final long TICKS_PER_PERIOD = 6_000L;

    private DynamicCaseSchedulePolicy() {
    }

    static State state(DynamicCaseProfile profile, long dayTime) {
        DynamicCaseProfile.DayPeriod current =
                DynamicCaseProfile.DayPeriod.at(dayTime);
        int dayTick = Math.floorMod(dayTime, (int) TICKS_PER_DAY);
        int targetTick = profile.schedule().observationPeriod().startTick();
        boolean observationOpen =
                current == profile.schedule().observationPeriod();
        long ticksUntilOpen = observationOpen
                ? 0L : Math.floorMod(targetTick - dayTick, (int) TICKS_PER_DAY);
        return new State(current, observationOpen, ticksUntilOpen);
    }

    record State(
            DynamicCaseProfile.DayPeriod currentPeriod,
            boolean observationOpen,
            long ticksUntilOpen) {

        State {
            if (currentPeriod == null || ticksUntilOpen < 0L
                    || ticksUntilOpen >= TICKS_PER_DAY) {
                throw new IllegalArgumentException(
                        "dynamic case schedule state is invalid");
            }
            if (observationOpen && ticksUntilOpen != 0L) {
                throw new IllegalArgumentException(
                        "open observation window cannot have a wait");
            }
        }

        long minutesUntilOpen() {
            return (ticksUntilOpen + 1_199L) / 1_200L;
        }
    }
}
