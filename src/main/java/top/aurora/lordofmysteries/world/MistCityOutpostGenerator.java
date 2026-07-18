package top.aurora.lordofmysteries.world;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.knowledge.GuideDirection;
import top.aurora.lordofmysteries.registry.ModBlocks;

@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class MistCityOutpostGenerator {

    private static final ResourceLocation LIVELIHOOD_SUPPLIES =
            ResourceLocation.fromNamespaceAndPath(
                    ProjectMystery.MOD_ID,
                    "chests/mist_city_livelihood_supplies");
    private static final Queue<PendingOutpost> PENDING_OUTPOSTS =
            new ConcurrentLinkedQueue<>();

    private MistCityOutpostGenerator() {}

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || level.dimension() != Level.OVERWORLD) return;
        if (MistCityOutpostSavedData.get(level).hasOutpost()
                || !event.getChunk().getPos().equals(outpostChunk(level))) return;
        ChunkPos chunk = event.getChunk().getPos();
        int x = chunk.getMinBlockX() + 8;
        int z = chunk.getMinBlockZ() + 8;
        BlockPos target = new BlockPos(x,
                event.getChunk().getHeight(Heightmap.Types.WORLD_SURFACE, x, z), z);
        if (target.getY() <= level.getSeaLevel()
                || target.getY() > level.getMaxBuildHeight() - 8) return;
        PENDING_OUTPOSTS.offer(new PendingOutpost(level.getSeed(), target));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        ServerLevel level = event.getServer().getLevel(Level.OVERWORLD);
        if (level == null) return;
        PendingOutpost pending;
        while ((pending = PENDING_OUTPOSTS.poll()) != null) {
            if (pending.levelSeed() != level.getSeed()) continue;
            MistCityOutpostSavedData data = MistCityOutpostSavedData.get(level);
            if (data.hasOutpost()) continue;
            generate(level, pending.target());
            data.recordOutpost(pending.target());
            ProjectMystery.LOGGER.info("Generated Mist City outpost at {}", pending.target());
        }
        MistCityOutpostSavedData data = MistCityOutpostSavedData.get(level);
        data.outpost().filter(level::hasChunkAt).ifPresent(outpost -> {
            if (data.serviceVersion()
                    >= MistCityOutpostSavedData.CURRENT_SERVICE_VERSION) return;
            generateServiceBooths(level, outpost);
            data.recordServiceVersion(
                    MistCityOutpostSavedData.CURRENT_SERVICE_VERSION);
            ProjectMystery.LOGGER.info(
                    "Upgraded Mist City outpost service booths to version {} at {}",
                    data.serviceVersion(), outpost);
        });
    }

    public static BlockPos starterOutpostTarget(ServerLevel level) {
        ChunkPos chunk = outpostChunk(level);
        int x = chunk.getMinBlockX() + 8;
        int z = chunk.getMinBlockZ() + 8;
        return new BlockPos(x, level.getSharedSpawnPos().getY(), z);
    }

    public static void reportOutpost(ServerPlayer player) {
        ServerLevel level = player.getServer().getLevel(Level.OVERWORLD);
        if (level == null) return;
        Optional<BlockPos> generated = MistCityOutpostSavedData.get(level).outpost();
        BlockPos target = generated.orElseGet(() -> starterOutpostTarget(level));
        double deltaX = target.getX() - player.getX();
        double deltaZ = target.getZ() - player.getZ();
        int distance = (int) Math.round(Math.sqrt(deltaX * deltaX + deltaZ * deltaZ));
        String direction = GuideDirection.fromDelta(deltaX, deltaZ);
        player.sendSystemMessage(Component.translatable(
                generated.isPresent()
                        ? "message.lord_of_mysteries.mist_city.outpost"
                        : "message.lord_of_mysteries.mist_city.outpost_uncharted",
                Component.translatable("direction.lord_of_mysteries." + direction),
                distance, target.getX(), target.getZ()).withStyle(ChatFormatting.GOLD));
    }

    private static ChunkPos outpostChunk(ServerLevel level) {
        RandomSource random = RandomSource.create(level.getSeed() ^ 0x4D495354L);
        double angle = random.nextDouble() * Math.PI * 2d;
        int radius = 3 + random.nextInt(3);
        BlockPos spawn = level.getSharedSpawnPos();
        int spawnChunkX = Math.floorDiv(spawn.getX(), 16);
        int spawnChunkZ = Math.floorDiv(spawn.getZ(), 16);
        return new ChunkPos(
                spawnChunkX + (int) Math.round(Math.cos(angle) * radius),
                spawnChunkZ + (int) Math.round(Math.sin(angle) * radius));
    }

    private static void generate(ServerLevel level, BlockPos center) {
        for (int dx = -6; dx <= 6; dx++) {
            for (int dz = -6; dz <= 6; dz++) {
                level.setBlock(center.offset(dx, -1, dz),
                        Blocks.STONE_BRICKS.defaultBlockState(), 3);
                for (int dy = 0; dy <= 5; dy++) {
                    level.setBlock(center.offset(dx, dy, dz),
                            Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                level.setBlock(center.offset(dx, 0, dz),
                        Blocks.SPRUCE_PLANKS.defaultBlockState(), 3);
                level.setBlock(center.offset(dx, 4, dz),
                        Blocks.DARK_OAK_SLAB.defaultBlockState(), 3);
            }
        }
        for (int dy = 1; dy <= 3; dy++) {
            for (int offset = -3; offset <= 3; offset++) {
                if (offset != 0 || dy > 2) {
                    level.setBlock(center.offset(offset, dy, -3),
                            Blocks.SPRUCE_PLANKS.defaultBlockState(), 3);
                }
                if (Math.abs(offset) != 1 || dy != 2) {
                    level.setBlock(center.offset(-3, dy, offset),
                            Blocks.SPRUCE_PLANKS.defaultBlockState(), 3);
                    level.setBlock(center.offset(3, dy, offset),
                            Blocks.SPRUCE_PLANKS.defaultBlockState(), 3);
                }
            }
        }
        int[][] corners = {{-3, -3}, {-3, 3}, {3, -3}, {3, 3}};
        for (int[] corner : corners) {
            for (int dy = 1; dy <= 3; dy++) {
                level.setBlock(center.offset(corner[0], dy, corner[1]),
                        Blocks.DARK_OAK_LOG.defaultBlockState(), 3);
            }
            level.setBlock(center.offset(corner[0], 3, corner[1]),
                    Blocks.LANTERN.defaultBlockState(), 3);
        }
        level.setBlock(center.offset(0, 1, 0),
                ModBlocks.COMMISSION_BOARD.get().defaultBlockState(), 3);
        level.setBlock(center.offset(-2, 1, 1),
                Blocks.BARREL.defaultBlockState(), 3);
        level.setBlock(center.offset(2, 1, 1),
                Blocks.LECTERN.defaultBlockState(), 3);
        level.setBlock(center.offset(0, 1, -3),
                Blocks.SOUL_LANTERN.defaultBlockState(), 3);
        level.setBlock(center.offset(-2, 1, 2),
                Blocks.RED_BED.defaultBlockState()
                        .setValue(BedBlock.PART, BedPart.FOOT)
                        .setValue(BedBlock.FACING, Direction.NORTH), 3);
        level.setBlock(center.offset(-2, 1, 1),
                Blocks.RED_BED.defaultBlockState()
                        .setValue(BedBlock.PART, BedPart.HEAD)
                        .setValue(BedBlock.FACING, Direction.NORTH), 3);
        level.setBlock(center.offset(1, 1, 2),
                Blocks.CRAFTING_TABLE.defaultBlockState(), 3);
        level.setBlock(center.offset(2, 1, 2),
                Blocks.FURNACE.defaultBlockState(), 3);
        level.setBlock(center.offset(4, 0, 2),
                Blocks.BARREL.defaultBlockState(), 3);
        if (level.getBlockEntity(center.offset(4, 0, 2))
                instanceof RandomizableContainerBlockEntity barrel) {
            barrel.setLootTable(LIVELIHOOD_SUPPLIES,
                    level.getSeed() ^ center.asLong());
        }
        for (int x = -6; x <= 6; x++) {
            level.setBlock(center.offset(x, 0, 5),
                    Blocks.STONE_BRICKS.defaultBlockState(), 3);
            level.setBlock(center.offset(x, 0, 6),
                    Blocks.STONE_BRICKS.defaultBlockState(), 3);
        }
        level.setBlock(center.offset(-5, 1, 5),
                Blocks.HAY_BLOCK.defaultBlockState(), 3);
        level.setBlock(center.offset(5, 1, 5),
                Blocks.CAULDRON.defaultBlockState(), 3);
    }

    private static void generateServiceBooths(
            ServerLevel level, BlockPos center) {
        generateServiceBooth(level, center.offset(-5, 0, 3),
                Blocks.CARTOGRAPHY_TABLE, Blocks.BOOKSHELF);
        generateServiceBooth(level, center.offset(5, 0, 3),
                Blocks.SMITHING_TABLE, Blocks.IRON_BARS);
    }

    private static void generateServiceBooth(
            ServerLevel level, BlockPos booth,
            Block counter, Block marker) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                level.setBlock(booth.offset(dx, 0, dz),
                        Blocks.POLISHED_ANDESITE.defaultBlockState(), 3);
                level.setBlock(booth.offset(dx, 3, dz),
                        Blocks.DARK_OAK_SLAB.defaultBlockState(), 3);
            }
            for (int dy = 1; dy <= 2; dy++) {
                level.setBlock(booth.offset(dx, dy, 1),
                        Blocks.SPRUCE_PLANKS.defaultBlockState(), 3);
            }
        }
        level.setBlock(booth.offset(0, 1, -1),
                counter.defaultBlockState(), 3);
        level.setBlock(booth.offset(0, 1, 1),
                marker.defaultBlockState(), 3);
        level.setBlock(booth.offset(0, 2, 1),
                Blocks.LANTERN.defaultBlockState(), 3);
    }

    private record PendingOutpost(long levelSeed, BlockPos target) {}
}
