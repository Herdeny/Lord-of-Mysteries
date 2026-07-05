package top.aurora.lordofmysteries.knowledge;

public final class M1TrialProgress {

    public static final long REQUIRED_TICKS = 72000L;
    public static final int REQUIRED_OCCULT_KILLS = 3;
    public static final float REQUIRED_RISK_PEAK = 25f;

    private M1TrialProgress() {}

    public static Result evaluate(long elapsedTicks, boolean campVisited, int bestSequence,
                                  int occultKills, float maxPressure, float maxPollution) {
        boolean durationComplete = elapsedTicks >= REQUIRED_TICKS;
        boolean sequenceComplete = bestSequence >= 0 && bestSequence <= 7;
        boolean killsComplete = occultKills >= REQUIRED_OCCULT_KILLS;
        boolean riskObserved = Math.max(maxPressure, maxPollution) >= REQUIRED_RISK_PEAK;
        int completed = count(durationComplete, campVisited, sequenceComplete,
                killsComplete, riskObserved);
        return new Result(durationComplete, campVisited, sequenceComplete,
                killsComplete, riskObserved, completed, completed == 5);
    }

    public static String formatDuration(long ticks) {
        long seconds = Math.max(0L, ticks) / 20L;
        return "%02d:%02d:%02d".formatted(
                seconds / 3600L, (seconds % 3600L) / 60L, seconds % 60L);
    }

    private static int count(boolean... values) {
        int count = 0;
        for (boolean value : values) if (value) count++;
        return count;
    }

    public record Result(
            boolean durationComplete,
            boolean campVisited,
            boolean sequenceComplete,
            boolean killsComplete,
            boolean riskObserved,
            int completedGoals,
            boolean passed) {}
}
