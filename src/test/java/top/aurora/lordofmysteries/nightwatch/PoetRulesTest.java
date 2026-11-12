package top.aurora.lordofmysteries.nightwatch;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PoetRulesTest {
    @Test
    void fearRequiresThreeMurmursAndNeverOverflows() {
        int fear = 0;
        fear = PoetRules.addFear(fear);
        assertEquals(2, fear);
        fear = PoetRules.addFear(fear);
        assertEquals(4, fear);
        assertEquals(5, PoetRules.addFear(fear));
        assertEquals(5, PoetRules.addFear(Integer.MAX_VALUE));
        assertEquals(2, PoetRules.addFear(-99));
    }

    @Test
    void resourcesRejectNanInfinityAndNegativeInputs() {
        for (float invalid : new float[]{Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, -1, 14.9f}) {
            assertFalse(PoetRules.canSpend(invalid, 15));
        }
        assertTrue(PoetRules.canSpend(15, 15));
        assertFalse(PoetRules.canSpend(30, -15));
        assertFalse(PoetRules.canSpend(30, Float.NaN));
    }

    @Test
    void cooldownPersistsAndFutureTimestampsFailClosed() {
        var progress = new NightwatchProgress();
        progress.murmurCooldown = 500;
        progress.poemCooldown = 1200;
        var restored = NightwatchProgress.load(progress.save());
        assertFalse(PoetRules.ready(restored.murmurCooldown, 499));
        assertTrue(PoetRules.ready(restored.murmurCooldown, 500));
        assertFalse(PoetRules.ready(restored.poemCooldown, 1199));
        assertFalse(PoetRules.ready(Long.MAX_VALUE, 100));
        assertFalse(PoetRules.ready(0, -1));
        assertEquals(0, NightwatchProgress.load(new CompoundTag()).poemCooldown);
    }

    @Test
    void harvestRequiresNightNewDayAndRealElapsedTime() {
        assertTrue(PoetRules.canHarvest(13000, 100, -1, 0));
        assertFalse(PoetRules.canHarvest(12999, 100, -1, 0));
        assertFalse(PoetRules.canHarvest(23000, 100, -1, 0));
        assertFalse(PoetRules.canHarvest(14000, 24100, 0, 24100));
        assertFalse(PoetRules.canHarvest(37000, 101, 0, 24100));
        assertTrue(PoetRules.canHarvest(37000, 24100, 0, 24100));
        assertFalse(PoetRules.canHarvest(13000, 50000, 1, 0));
    }
}
