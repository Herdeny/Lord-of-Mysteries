package top.aurora.lordofmysteries.knowledge;

public final class M1TrialContinuity {

    public static final int REQUIRED_RECONNECTS = 1;
    public static final int REQUIRED_SERVER_RESTARTS = 1;
    public static final int REQUIRED_DIMENSION_CHANGES = 2;
    public static final int REQUIRED_DEATH_RECOVERIES = 1;

    private M1TrialContinuity() {}

    public static Result evaluate(int reconnects, int serverRestarts,
                                  int dimensionChanges, int deathRecoveries) {
        boolean reconnectComplete = reconnects >= REQUIRED_RECONNECTS;
        boolean restartComplete = serverRestarts >= REQUIRED_SERVER_RESTARTS;
        boolean dimensionComplete = dimensionChanges >= REQUIRED_DIMENSION_CHANGES;
        boolean deathComplete = deathRecoveries >= REQUIRED_DEATH_RECOVERIES;
        int completed = count(reconnectComplete, restartComplete,
                dimensionComplete, deathComplete);
        return new Result(reconnectComplete, restartComplete,
                dimensionComplete, deathComplete, completed, completed == 4);
    }

    private static int count(boolean... values) {
        int count = 0;
        for (boolean value : values) if (value) count++;
        return count;
    }

    public record Result(
            boolean reconnectComplete,
            boolean restartComplete,
            boolean dimensionComplete,
            boolean deathComplete,
            int completedGoals,
            boolean passed) {}
}
