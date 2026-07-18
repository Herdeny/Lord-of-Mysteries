package top.aurora.lordofmysteries.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

class MistCityOutpostSavedDataTest {

    @Test
    void serviceVersionSurvivesWorldSavedDataRoundTrip() {
        MistCityOutpostSavedData data = new MistCityOutpostSavedData();
        data.recordOutpost(new BlockPos(12, 80, -9));
        data.recordServiceVersion(
                MistCityOutpostSavedData.CURRENT_SERVICE_VERSION);

        MistCityOutpostSavedData restored = MistCityOutpostSavedData.load(
                data.save(new CompoundTag()));

        assertTrue(restored.hasOutpost());
        assertEquals(new BlockPos(12, 80, -9), restored.outpost().orElseThrow());
        assertEquals(MistCityOutpostSavedData.CURRENT_SERVICE_VERSION,
                restored.serviceVersion());
    }

    @Test
    void legacyOutpostsStartAtServiceVersionZero() {
        CompoundTag legacy = new CompoundTag();
        legacy.putBoolean("generated", true);
        legacy.putLong("outpost_position", new BlockPos(0, 64, 0).asLong());

        assertEquals(0, MistCityOutpostSavedData.load(legacy).serviceVersion());
    }
}
