package top.aurora.lordofmysteries.nightwatch;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;

public final class NightwatchProgress {
    public int stacks;
    public long clearedDay = -1;
    public long rewardedDay = -1;
    public long wakeCooldown;
    public long wardUntil;
    private long nightDay = -1;
    private long lastWorldTime = -1;
    private long lastGameTime = -1;
    private int observedTicks;
    private final Set<UUID> attackers = new HashSet<>();

    public static boolean isNight(long worldTime) {
        long phase = Math.floorMod(worldTime, 24000);
        return phase >= 13000 && phase < 23000;
    }

    public void interrupt() {
        nightDay = -1;
        lastWorldTime = -1;
        lastGameTime = -1;
        observedTicks = 0;
        attackers.clear();
    }

    public boolean observe(long worldTime, long gameTime, boolean eligible) {
        long day = Math.floorDiv(worldTime, 24000);
        long phase = Math.floorMod(worldTime, 24000);
        if (!eligible || worldTime < 0 || gameTime < 0) {
            interrupt();
            return false;
        }
        if (nightDay >= 0) {
            long elapsed = gameTime - lastGameTime;
            boolean continuous = elapsed > 0 && elapsed <= 40
                    && worldTime - lastWorldTime == elapsed && day == nightDay;
            if (!continuous) {
                interrupt();
                return false;
            }
            observedTicks += (int) elapsed;
            lastWorldTime = worldTime;
            lastGameTime = gameTime;
            if (phase >= 23000) {
                boolean fullNight = observedTicks >= 9960 && nightDay > rewardedDay;
                boolean acting = fullNight && attackers.size() >= 3;
                if (fullNight) {
                    rewardedDay = nightDay;
                    stacks = Math.min(5, stacks + 1);
                }
                interrupt();
                return acting;
            }
        } else if (phase >= 13000 && phase <= 13040 && day > rewardedDay) {
            nightDay = day;
            lastWorldTime = worldTime;
            lastGameTime = gameTime;
            observedTicks = 0;
            attackers.clear();
        }
        return false;
    }

    public void recordAttacker(UUID identity) {
        if (nightDay >= 0 && identity != null && attackers.size() < 3) attackers.add(identity);
    }

    public int attackers() { return attackers.size(); }

    public int watchedTicks() { return observedTicks; }

    public double speedModifier(long worldTime) {
        if (isNight(worldTime)) return Math.min(5, Math.max(0, stacks)) * 0.04;
        return clearedDay == Math.floorDiv(worldTime, 24000) ? 0
                : -Math.min(5, Math.max(0, stacks)) * 0.10;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("stacks", stacks);
        tag.putLong("cleared_day", clearedDay);
        tag.putLong("rewarded_day", rewardedDay);
        tag.putLong("wake_cooldown", wakeCooldown);
        tag.putLong("ward_until", wardUntil);
        return tag;
    }

    public static NightwatchProgress load(CompoundTag tag) {
        NightwatchProgress progress = new NightwatchProgress();
        progress.stacks = Math.max(0, Math.min(5, tag.getInt("stacks")));
        progress.clearedDay = tag.contains("cleared_day") ? Math.max(-1, tag.getLong("cleared_day")) : -1;
        progress.rewardedDay = tag.contains("rewarded_day") ? Math.max(-1, tag.getLong("rewarded_day")) : -1;
        progress.wakeCooldown = Math.max(0, tag.getLong("wake_cooldown"));
        progress.wardUntil = Math.max(0, tag.getLong("ward_until"));
        return progress;
    }
}
