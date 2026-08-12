package top.aurora.lordofmysteries.spirit;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public final class SpiritExpeditionWorldBuilder {

    public static final int PAD_Y = 64;
    public static final int PAD_SPACING = 40;
    private static final int LANE_SPACING = 640;
    private static final int LANE_COLUMNS = 64;
    private static final int PAD_RADIUS = 5;

    private SpiritExpeditionWorldBuilder() {}

    public static BlockPos padCenter(int lane, int step) {
        int safeLane = Math.floorMod(lane, 4096);
        int laneX = safeLane % LANE_COLUMNS - LANE_COLUMNS / 2;
        int laneZ = safeLane / LANE_COLUMNS - LANE_COLUMNS / 2;
        return new BlockPos(
                laneX * LANE_SPACING,
                PAD_Y,
                laneZ * LANE_SPACING + Math.max(0, step) * PAD_SPACING);
    }

    public static BlockPos nextPad(
            SpiritExpeditionSavedData.Expedition expedition,
            SpiritDirection direction) {
        BlockPos current = expedition.currentPad();
        return current.offset(
                direction.stepX() * PAD_SPACING,
                0,
                direction.stepZ() * PAD_SPACING);
    }

    public static Vec3 build(
            ServerLevel level, BlockPos center,
            SpiritProjection projection, SpiritWeather weather) {
        if (level == null || center == null || projection == null
                || weather == null || !level.isInWorldBounds(center)
                || !level.getWorldBorder().isWithinBounds(center)) {
            return null;
        }
        level.getChunkAt(center);
        Block floor = floorFor(projection);
        Block edge = edgeFor(weather);
        for (int x = -PAD_RADIUS; x <= PAD_RADIUS; x++) {
            for (int z = -PAD_RADIUS; z <= PAD_RADIUS; z++) {
                BlockPos floorPos = center.offset(x, 0, z);
                boolean boundary = Math.abs(x) == PAD_RADIUS
                        || Math.abs(z) == PAD_RADIUS;
                level.setBlockAndUpdate(
                        floorPos, (boundary ? edge : floor).defaultBlockState());
                for (int y = 1; y <= 4; y++) {
                    level.setBlockAndUpdate(
                            floorPos.above(y), Blocks.AIR.defaultBlockState());
                }
            }
        }
        for (int x : new int[] {-PAD_RADIUS, PAD_RADIUS}) {
            for (int z : new int[] {-PAD_RADIUS, PAD_RADIUS}) {
                level.setBlockAndUpdate(
                        center.offset(x, 1, z),
                        Blocks.SOUL_LANTERN.defaultBlockState());
            }
        }
        return Vec3.atBottomCenterOf(center.above());
    }

    private static Block floorFor(SpiritProjection projection) {
        return switch (projection) {
            case CHURCH -> Blocks.POLISHED_ANDESITE;
            case CEMETERY -> Blocks.MOSSY_STONE_BRICKS;
            case THEATRE -> Blocks.DARK_OAK_PLANKS;
            case HARBOR -> Blocks.PRISMARINE_BRICKS;
            case MANOR -> Blocks.POLISHED_BLACKSTONE_BRICKS;
        };
    }

    private static Block edgeFor(SpiritWeather weather) {
        return switch (weather) {
            case SPIRIT_MIST -> Blocks.CALCITE;
            case STARLESS_NIGHT -> Blocks.OBSIDIAN;
            case WHISPERING_RAIN -> Blocks.CRYING_OBSIDIAN;
            case SPIRITUAL_STORM -> Blocks.COPPER_BLOCK;
            case MEMORY_SNOW -> Blocks.PACKED_ICE;
            case DOORLIGHT_AURORA -> Blocks.AMETHYST_BLOCK;
        };
    }
}
