package top.aurora.lordofmysteries.world;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.core.BlockPos;

public final class MistCityDistrictLayout {

    private static final Map<District, BlockPos> OFFSETS =
            new EnumMap<>(District.class);

    static {
        OFFSETS.put(District.PRESS, new BlockPos(-12, 0, 0));
        OFFSETS.put(District.DETECTIVE_AGENCY, new BlockPos(0, 0, 12));
        OFFSETS.put(District.CONSTABULARY, new BlockPos(12, 0, 0));
    }

    private MistCityDistrictLayout() {}

    public static BlockPos center(BlockPos outpost, District district) {
        if (outpost == null || district == null) {
            throw new IllegalArgumentException("outpost and district are required");
        }
        return outpost.offset(OFFSETS.get(district));
    }

    public static BlockPos servicePosition(
            BlockPos outpost, District district) {
        return center(outpost, district).above();
    }

    public static int maximumHorizontalRadius() {
        return 15;
    }

    public enum District {
        PRESS,
        DETECTIVE_AGENCY,
        CONSTABULARY
    }
}
