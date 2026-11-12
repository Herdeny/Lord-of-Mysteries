package top.aurora.lordofmysteries.nightwatch;

public final class PoetRules {
    public static final int MURMUR_COOLDOWN = 500;
    public static final int POEM_COOLDOWN = 1200;
    public static final int FEAR_MEMORY = 1200;
    public static final int SLEEP_TICKS = 160;
    public static final int ROUT_TICKS = 100;

    private PoetRules() {}

    public static int addFear(int current) { return Math.min(5, Math.min(5, Math.max(0, current)) + 2); }

    public static boolean ready(long end, long now) { return now >= 0 && end >= 0 && now >= end; }

    public static boolean canSpend(float spirit, float cost) {
        return Float.isFinite(spirit) && Float.isFinite(cost) && cost > 0 && spirit >= cost;
    }

    public static boolean canHarvest(long worldTime, long gameTime, long lastDay, long cooldown) {
        return worldTime >= 0 && gameTime >= 0 && NightwatchProgress.isNight(worldTime)
                && Math.floorDiv(worldTime, 24000) > lastDay && ready(cooldown, gameTime);
    }
}
