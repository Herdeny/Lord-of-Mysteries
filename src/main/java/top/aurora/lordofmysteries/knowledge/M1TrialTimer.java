package top.aurora.lordofmysteries.knowledge;

public final class M1TrialTimer {

    private M1TrialTimer() {}

    public static long elapsed(long storedTicks, boolean active,
                               long startTick, long currentTick) {
        long stored = Math.max(0L, storedTicks);
        if (!active || startTick < 0L) return stored;
        return stored + Math.max(0L, currentTick - startTick);
    }
}
