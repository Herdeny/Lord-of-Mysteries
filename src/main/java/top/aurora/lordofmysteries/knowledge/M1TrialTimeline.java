package top.aurora.lordofmysteries.knowledge;

public final class M1TrialTimeline {

    public static final long CAMP_TARGET_TICKS = 12000L;
    public static final long SEQUENCE_9_TARGET_TICKS = 36000L;
    public static final long SEQUENCE_8_TARGET_TICKS = 72000L;
    public static final long SEQUENCE_7_TARGET_TICKS = 108000L;
    public static final long IDENTITY_TARGET_TICKS = 120000L;
    public static final long REFLECTION_TARGET_TICKS = 132000L;
    public static final long STREET_LIFE_TARGET_TICKS = 144000L;

    private M1TrialTimeline() {}

    public static Result evaluate(long campTick, long sequence9Tick,
                                  long sequence8Tick, long sequence7Tick,
                                  long identityTick, long reflectionTick,
                                  long streetLifeTick) {
        Milestone camp = milestone(campTick, CAMP_TARGET_TICKS);
        Milestone sequence9 = milestone(sequence9Tick, SEQUENCE_9_TARGET_TICKS);
        Milestone sequence8 = milestone(sequence8Tick, SEQUENCE_8_TARGET_TICKS);
        Milestone sequence7 = milestone(sequence7Tick, SEQUENCE_7_TARGET_TICKS);
        Milestone identity = milestone(identityTick, IDENTITY_TARGET_TICKS);
        Milestone reflection = milestone(reflectionTick, REFLECTION_TARGET_TICKS);
        Milestone streetLife = milestone(streetLifeTick, STREET_LIFE_TARGET_TICKS);
        int complete = count(camp.recorded(), sequence9.recorded(),
                sequence8.recorded(), sequence7.recorded(), identity.recorded(),
                reflection.recorded(), streetLife.recorded());
        int onTime = count(camp.onTime(), sequence9.onTime(),
                sequence8.onTime(), sequence7.onTime(), identity.onTime(),
                reflection.onTime(), streetLife.onTime());
        return new Result(camp, sequence9, sequence8, sequence7, identity,
                reflection, streetLife, complete, onTime,
                complete == 7 && onTime == 7);
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
                         Milestone identity, Milestone reflection,
                         Milestone streetLife,
                         int recordedMilestones, int onTimeMilestones,
                         boolean onSchedule) {}
}
