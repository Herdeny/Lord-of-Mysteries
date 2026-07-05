package top.aurora.lordofmysteries.world;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.core.config.ServerConfig;

@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class AbandonedCampGenerator {

    private static final ResourceLocation CAMP_LOOT = ResourceLocation.fromNamespaceAndPath(
            ProjectMystery.MOD_ID, "chests/abandoned_investigator_camp");

    private AbandonedCampGenerator() {}

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || level.dimension() != Level.OVERWORLD) return;
        ChunkPos chunkPos = event.getChunk().getPos();
        long chunkKey = ChunkPos.asLong(chunkPos.x, chunkPos.z);
        CampGenerationSavedData generationData = CampGenerationSavedData.get(level);
        if (!generationData.markIfNew(chunkKey)) return;
        long seed = level.getSeed() ^ ChunkPos.asLong(chunkPos.x, chunkPos.z) ^ 0x5EEDCA4FL;
        RandomSource random = RandomSource.create(seed);
        boolean starterCamp = !generationData.hasStarterCamp()
                && chunkPos.equals(starterCampChunk(level));
        double chance = 0.005 * ServerConfig.STRUCTURE_GENERATION_RATE.get();
        if (!starterCamp && random.nextDouble() >= chance) return;

        int x = chunkPos.getMinBlockX() + 8;
        int z = chunkPos.getMinBlockZ() + 8;
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
        if ((!starterCamp && y <= level.getSeaLevel())
                || y > level.getMaxBuildHeight() - 8) return;
        generate(level, new BlockPos(x, y, z), random);
        generationData.recordCamp(new BlockPos(x, y, z), starterCamp);
    }

    public static BlockPos starterCampTarget(ServerLevel level) {
        ChunkPos chunk = starterCampChunk(level);
        int x = chunk.getMinBlockX() + 8;
        int z = chunk.getMinBlockZ() + 8;
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
        return new BlockPos(x, y, z);
    }

    private static ChunkPos starterCampChunk(ServerLevel level) {
        RandomSource random = RandomSource.create(level.getSeed() ^ 0x71A7C0DEL);
        double angle = random.nextDouble() * Math.PI * 2d;
        int radius = 10 + random.nextInt(7);
        BlockPos spawn = level.getSharedSpawnPos();
        int spawnChunkX = Math.floorDiv(spawn.getX(), 16);
        int spawnChunkZ = Math.floorDiv(spawn.getZ(), 16);
        int offsetX = (int) Math.round(Math.cos(angle) * radius);
        int offsetZ = (int) Math.round(Math.sin(angle) * radius);
        return new ChunkPos(spawnChunkX + offsetX, spawnChunkZ + offsetZ);
    }

    private static void generate(ServerLevel level, BlockPos center, RandomSource random) {
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                BlockPos floor = center.offset(dx, -1, dz);
                level.setBlock(floor, Blocks.COARSE_DIRT.defaultBlockState(), 3);
                if (!level.isEmptyBlock(center.offset(dx, 0, dz))) {
                    level.setBlock(center.offset(dx, 0, dz), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }

        level.setBlock(center, Blocks.CAMPFIRE.defaultBlockState()
                .setValue(CampfireBlock.LIT, false), 3);
        level.setBlock(center.offset(-2, 0, -2), Blocks.OAK_LOG.defaultBlockState(), 3);
        level.setBlock(center.offset(-2, 1, -2), Blocks.OAK_LOG.defaultBlockState(), 3);
        level.setBlock(center.offset(2, 0, -2), Blocks.OAK_LOG.defaultBlockState(), 3);
        level.setBlock(center.offset(2, 1, -2), Blocks.OAK_LOG.defaultBlockState(), 3);
        level.setBlock(center.offset(-1, 2, -2), Blocks.BROWN_WOOL.defaultBlockState(), 3);
        level.setBlock(center.offset(0, 2, -2), Blocks.BROWN_WOOL.defaultBlockState(), 3);
        level.setBlock(center.offset(1, 2, -2), Blocks.BROWN_WOOL.defaultBlockState(), 3);
        level.setBlock(center.offset(-1, 0, 1), Blocks.BARREL.defaultBlockState(), 3);

        BlockPos chestPos = center.offset(2, 0, 1);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 3);
        if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setLootTable(CAMP_LOOT, random.nextLong());
        }
    }
}
