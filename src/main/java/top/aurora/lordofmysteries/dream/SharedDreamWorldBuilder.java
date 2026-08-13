package top.aurora.lordofmysteries.dream;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class SharedDreamWorldBuilder {

    private static final int LANE_SPACING = 384;
    private static final int BASE_Z = 48_000;
    private static final int FLOOR_Y = 80;
    private static final int RADIUS = 8;

    private SharedDreamWorldBuilder() {}

    public static BlockPos center(int lane) {
        if (lane < 0) throw new IllegalArgumentException("lane must be non-negative");
        return new BlockPos(lane * LANE_SPACING, FLOOR_Y, BASE_Z);
    }

    public static Vec3 build(
            ServerLevel level, int lane, DreamScenario scenario) {
        if (level == null || scenario == null || lane < 0) return null;
        BlockPos center = center(lane);
        BlockPos min = center.offset(-RADIUS, -1, -RADIUS);
        BlockPos max = center.offset(RADIUS, 5, RADIUS);
        if (!level.isInWorldBounds(min) || !level.isInWorldBounds(max)
                || !level.getWorldBorder().isWithinBounds(min)
                || !level.getWorldBorder().isWithinBounds(max)) {
            return null;
        }
        level.getChunkAt(center);
        BlockState floor = floor(scenario);
        BlockState trim = trim(scenario);
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int z = -RADIUS; z <= RADIUS; z++) {
                BlockPos feet = center.offset(x, 0, z);
                boolean edge = Math.abs(x) == RADIUS || Math.abs(z) == RADIUS;
                level.setBlockAndUpdate(feet, edge ? trim : floor);
                for (int y = 1; y <= 4; y++) {
                    level.setBlockAndUpdate(feet.above(y), Blocks.AIR.defaultBlockState());
                }
            }
        }
        buildSymbols(level, center, scenario, trim);
        return Vec3.atBottomCenterOf(center.above());
    }

    private static void buildSymbols(
            ServerLevel level, BlockPos center,
            DreamScenario scenario, BlockState trim) {
        for (int offset = -5; offset <= 5; offset += 5) {
            level.setBlockAndUpdate(
                    center.offset(offset, 1, -5), trim);
            level.setBlockAndUpdate(
                    center.offset(offset, 2, -5), trim);
            level.setBlockAndUpdate(
                    center.offset(offset, 1, 5), trim);
            level.setBlockAndUpdate(
                    center.offset(offset, 2, 5), trim);
        }
        BlockState core = switch (scenario) {
            case MISSING_GUEST -> Blocks.RED_CANDLE.defaultBlockState();
            case REPEATING_THEATRE -> Blocks.JUKEBOX.defaultBlockState();
            case DROWNED_ARCHIVE -> Blocks.LECTERN.defaultBlockState();
            case COLORLESS_STREET -> Blocks.LANTERN.defaultBlockState();
        };
        level.setBlockAndUpdate(center.above(), core);
    }

    private static BlockState floor(DreamScenario scenario) {
        return switch (scenario) {
            case MISSING_GUEST -> Blocks.DARK_OAK_PLANKS.defaultBlockState();
            case REPEATING_THEATRE -> Blocks.RED_WOOL.defaultBlockState();
            case DROWNED_ARCHIVE -> Blocks.PRISMARINE_BRICKS.defaultBlockState();
            case COLORLESS_STREET -> Blocks.GRAY_CONCRETE.defaultBlockState();
        };
    }

    private static BlockState trim(DreamScenario scenario) {
        return switch (scenario) {
            case MISSING_GUEST -> Blocks.POLISHED_BLACKSTONE.defaultBlockState();
            case REPEATING_THEATRE -> Blocks.GILDED_BLACKSTONE.defaultBlockState();
            case DROWNED_ARCHIVE -> Blocks.DARK_PRISMARINE.defaultBlockState();
            case COLORLESS_STREET -> Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState();
        };
    }
}
