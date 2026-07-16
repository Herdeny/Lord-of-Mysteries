package top.aurora.lordofmysteries.knowledge;

public final class M1TrialTimeline {

    public static final long CAMP_TARGET_TICKS = 12000L;
    public static final long SEQUENCE_9_TARGET_TICKS = 30000L;
    public static final long SEQUENCE_8_TARGET_TICKS = 54000L;
    public static final long SEQUENCE_7_TARGET_TICKS = 72000L;

    private M1TrialTimeline() {}

    public static Result evaluate(long campTick, long sequence9Tick,
                                  long sequence8Tick, long sequence7Tick) {
        Milestone camp = milestone(campTick, CAMP_TARGET_TICKS);
        Milestone sequence9 = milestone(sequence9Tick, SEQUENCE_9_TARGET_TICKS);
        Milestone sequence8 = milestone(sequence8Tick, SEQUENCE_8_TARGET_TICKS);
        Milestone sequence7 = milestone(sequence7Tick, SEQUENCE_7_TARGET_TICKS);
        int complete = count(camp.recorded(), sequence9.recorded(),
                sequence8.recorded(), sequence7.recorded());
        int onTime = count(camp.onTime(), sequence9.onTime(),
                sequence8.onTime(), sequence7.onTime());
        return new Result(camp, sequence9, sequence8, sequence7,
                complete, onTime, complete == 4 && onTime == 4);
    }

    private static Milestone milestone(long actualTick, long targetTick) {
        boolean recorded = actualTick >= 0L;
        return new Milestone(actualTick, targetTick,
                recorded, recorded && actualTick <= targetTick);
    }

    private static int count(boolean... values) {
        int total = 0;
        for (boolean value : values) if (value) total++;
        return total;
    }

    public record Milestone(long actualTick, long targetTick,
                            boolean recorded, boolean onTime) {}

    public record Result(Milestone camp, Milestone sequence9,
                         Milestone sequence8, Milestone sequence7,
                         int recordedMilestones, int onTimeMilestones,
                         boolean onSchedule) {}
}
