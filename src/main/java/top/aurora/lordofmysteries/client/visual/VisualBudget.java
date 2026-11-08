package top.aurora.lordofmysteries.client.visual;

public final class VisualBudget {
    public enum Density {
        LOW(48), STANDARD(96), RICH(160);

        public final int perTick;

        Density(int perTick) {
            this.perTick = perTick;
        }
    }

    private long tick = Long.MIN_VALUE;
    private int used;

    public boolean allow(long gameTick, Density density) {
        if (tick != gameTick) {
            tick = gameTick;
            used = 0;
        }
        if (used >= density.perTick) return false;
        used++;
        return true;
    }

    public void reset() {
        tick = Long.MIN_VALUE;
        used = 0;
    }
}
