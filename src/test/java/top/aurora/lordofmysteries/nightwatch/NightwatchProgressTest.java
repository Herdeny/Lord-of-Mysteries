package top.aurora.lordofmysteries.nightwatch;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NightwatchProgressTest {
    private static boolean finishNight(NightwatchProgress progress, long day, boolean combat) {
        long start = day * 24000 + 13000;
        boolean reward = false;
        for (long time = start; time <= start + 10000; time += 20) {
            reward |= progress.observe(time, time, true);
            if (combat && time >= start + 100 && time <= start + 140) progress.recordAttacker(UUID.randomUUID());
        }
        return reward;
    }

    @Test
    void fullNightWithThreeAttackersAwardsOneStackAndActing() {
        var progress = new NightwatchProgress();
        assertTrue(finishNight(progress, 0, true));
        assertEquals(1, progress.stacks);
        assertEquals(0, progress.rewardedDay);
        assertFalse(finishNight(progress, 0, true));
        assertEquals(1, progress.stacks);
    }

    @Test
    void quietFullNightGivesStackButNoCombatActing() {
        var progress = new NightwatchProgress();
        assertFalse(finishNight(progress, 0, false));
        assertEquals(1, progress.stacks);
    }

    @Test
    void middayJoinCannotPretendToWatchWholeNight() {
        var progress = new NightwatchProgress();
        for (long time = 16000; time <= 23000; time += 20) assertFalse(progress.observe(time, time, true));
        assertEquals(0, progress.stacks);
    }

    @Test
    void timeSkipAndRollbackAreNotElapsedPlaytime() {
        var progress = new NightwatchProgress();
        progress.observe(13000, 0, true);
        assertFalse(progress.observe(23000, 20, true));
        assertEquals(0, progress.stacks);
        progress.observe(37000, 40, true);
        assertFalse(progress.observe(13020, 60, true));
        assertEquals(0, progress.stacks);
    }

    @Test
    void sleepDimensionAndActivityInterruptTheAttempt() {
        for (int interruption = 14000; interruption <= 22000; interruption += 2000) {
            var progress = new NightwatchProgress();
            for (long time = 13000; time <= 23000; time += 20) {
                assertFalse(progress.observe(time, time, time != interruption));
            }
            assertEquals(0, progress.stacks);
        }
    }

    @Test
    void missingServerSamplesInvalidateNight() {
        var progress = new NightwatchProgress();
        progress.observe(13000, 13000, true);
        progress.observe(13100, 13100, true);
        for (long time = 13120; time <= 23000; time += 20) assertFalse(progress.observe(time, time, true));
        assertEquals(0, progress.stacks);
    }

    @Test
    void stacksCapAndCoffeeOnlyClearsTodaysPenalty() {
        var progress = new NightwatchProgress();
        for (int day = 0; day < 9; day++) finishNight(progress, day, true);
        assertEquals(5, progress.stacks);
        assertEquals(-0.5, progress.speedModifier(9 * 24000), 0.00001);
        progress.clearedDay = 9;
        assertEquals(0, progress.speedModifier(9 * 24000), 0.00001);
        assertEquals(0.2, progress.speedModifier(9 * 24000 + 14000), 0.00001);
        assertEquals(-0.5, progress.speedModifier(10 * 24000), 0.00001);
    }

    @Test
    void savePreservesRewardsButNeverResumesOfflineWatch() {
        var progress = new NightwatchProgress();
        finishNight(progress, 0, true);
        progress.observe(37000, 37000, true);
        progress.recordAttacker(UUID.randomUUID());
        progress.clearedDay = 1;
        progress.wakeCooldown = 39000;
        var restored = NightwatchProgress.load(progress.save());
        assertEquals(1, restored.stacks);
        assertEquals(1, restored.clearedDay);
        assertEquals(39000, restored.wakeCooldown);
        assertEquals(0, restored.attackers());
        assertFalse(restored.observe(47000, 47000, true));
    }

    @Test
    void malformedValuesAreClampedWithoutAwardingNight() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("stacks", Integer.MAX_VALUE);
        tag.putLong("wake_cooldown", -12);
        var restored = NightwatchProgress.load(tag);
        assertEquals(5, restored.stacks);
        assertEquals(0, restored.wakeCooldown);
        assertEquals(-1, restored.rewardedDay);
        assertFalse(restored.observe(-100, -100, true));
    }

    @Test
    void futureSchemaRemainsUnchangedAndReadOnly() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("schema", 99);
        tag.putString("unknown_payload", "keep me");
        var saved = NightwatchSavedData.load(tag);
        assertFalse(saved.writable());
        assertThrows(IllegalStateException.class, () -> saved.getOrCreate(UUID.randomUUID()));
        assertEquals(tag, saved.save(new CompoundTag()));
    }

    @Test
    void savedPlayersStayIsolatedAndDuplicateEntriesAreQuarantined() {
        UUID first = UUID.randomUUID(), second = UUID.randomUUID();
        var saved = new NightwatchSavedData();
        saved.getOrCreate(first).stacks = 3;
        saved.getOrCreate(second).stacks = 1;
        CompoundTag encoded = saved.save(new CompoundTag());
        ListTag players = encoded.getList("players", 10);
        players.add(players.get(0).copy());
        players.add(new CompoundTag());
        var restored = NightwatchSavedData.load(encoded);
        assertEquals(3, restored.find(first).stacks);
        assertEquals(1, restored.find(second).stacks);
        assertEquals(2, restored.save(new CompoundTag()).getList("quarantined", 10).size());
    }

    @Test
    void repeatedDeathEventCannotMultiplyAttackerCredit() {
        var progress = new NightwatchProgress();
        progress.observe(13000, 13000, true);
        UUID attacker = UUID.randomUUID();
        for (int repeat = 0; repeat < 100; repeat++) progress.recordAttacker(attacker);
        progress.recordAttacker(null);
        assertEquals(1, progress.attackers());
        progress.interrupt();
        assertEquals(0, progress.attackers());
    }

    @Test
    void malformedListIsPreservedReadOnlyInsteadOfErased() {
        CompoundTag root = new CompoundTag();
        root.putString("players", "not a list");
        var saved = NightwatchSavedData.load(root);
        assertFalse(saved.writable());
        assertEquals(root, saved.save(new CompoundTag()));
    }

    @Test
    void nightBoundariesAreExact() {
        assertFalse(NightwatchProgress.isNight(12999));
        assertTrue(NightwatchProgress.isNight(13000));
        assertTrue(NightwatchProgress.isNight(22999));
        assertFalse(NightwatchProgress.isNight(23000));
    }
}
