package top.aurora.lordofmysteries.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;

class MistCityDistrictLayoutTest {

    @Test
    void formalDistrictsUseDistinctSymmetricServicePositions() {
        BlockPos outpost = new BlockPos(100, 72, -40);
        Set<BlockPos> positions = new HashSet<>();
        for (MistCityDistrictLayout.District district
                : MistCityDistrictLayout.District.values()) {
            BlockPos service = MistCityDistrictLayout.servicePosition(
                    outpost, district);
            positions.add(service);
            assertEquals(outpost.getY() + 1, service.getY());
            assertNotEquals(outpost.above(), service);
        }

        assertEquals(MistCityDistrictLayout.District.values().length,
                positions.size());
        assertEquals(
                MistCityDistrictLayout.servicePosition(
                        outpost, MistCityDistrictLayout.District.PRESS)
                        .getX() - outpost.getX(),
                -(MistCityDistrictLayout.servicePosition(
                        outpost,
                        MistCityDistrictLayout.District.CONSTABULARY)
                        .getX() - outpost.getX()));
    }

    @Test
    void maximumRadiusIncludesEveryDistrictFootprint() {
        BlockPos origin = BlockPos.ZERO;
        int radius = MistCityDistrictLayout.maximumHorizontalRadius();
        for (MistCityDistrictLayout.District district
                : MistCityDistrictLayout.District.values()) {
            BlockPos center = MistCityDistrictLayout.center(origin, district);
            assertTrue(Math.abs(center.getX()) + 3 <= radius);
            assertTrue(Math.abs(center.getZ()) + 3 <= radius);
        }
    }
}
