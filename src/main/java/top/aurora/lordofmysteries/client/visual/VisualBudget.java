package top.aurora.lordofmysteries.client.visual;

import java.util.PriorityQueue;

public final class VisualBudget {
    public enum Density {
        LOW(48, 350), STANDARD(96, 1200), RICH(160, 2200);

        public final int perTick;
        public final int total;

        Density(int perTick, int total) {
            this.perTick = perTick;
            this.total = total;
        }
    }

    private long tick = Long.MIN_VALUE;
    private int used;
    private final PriorityQueue<Long> expires = new PriorityQueue<>();

    public boolean allow(long gameTick, Density density) {
        return allow(gameTick, density, 1);
    }

    public boolean allow(long gameTick, Density density, int lifetime) {
        if (density == null || lifetime <= 0 || lifetime > 200) return false;
        if (gameTick < tick) reset();
        while (!expires.isEmpty() && expires.peek() <= gameTick) expires.remove();
        if (tick != gameTick) {
            tick = gameTick;
            used = 0;
        }
        if (used >= density.perTick || expires.size() >= density.total) return false;
        used++;
        expires.add(gameTick > Long.MAX_VALUE - lifetime ? Long.MAX_VALUE : gameTick + lifetime);
        return true;
    }

    public void reset() {
        tick = Long.MIN_VALUE;
        used = 0;
        expires.clear();
    }
}
