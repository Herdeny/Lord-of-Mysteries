package top.aurora.lordofmysteries.world;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
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
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
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
    }

    private record PendingOutpost(long levelSeed, BlockPos target) {}
}
