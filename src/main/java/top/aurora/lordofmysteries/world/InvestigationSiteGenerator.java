package top.aurora.lordofmysteries.world;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.knowledge.GuideDirection;

@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class InvestigationSiteGenerator {

    private static final ResourceLocation CHURCH_LOOT = ResourceLocation.fromNamespaceAndPath(
            ProjectMystery.MOD_ID, "chests/abandoned_church");
    private static final ResourceLocation CULTIST_CAMP_LOOT =
            ResourceLocation.fromNamespaceAndPath(
                    ProjectMystery.MOD_ID, "chests/cultist_rescue_camp");
    private static final Queue<PendingSite> PENDING_SITES = new ConcurrentLinkedQueue<>();

    private InvestigationSiteGenerator() {}

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || level.dimension() != Level.OVERWORLD) return;
        InvestigationSiteSavedData data = InvestigationSiteSavedData.get(level);
        ChunkPos loaded = event.getChunk().getPos();
        if (!data.hasChurch() && loaded.equals(churchChunk(level))) {
            PENDING_SITES.offer(pending(level, event, SiteType.ABANDONED_CHURCH));
        }
        if (!data.hasCultistCamp() && loaded.equals(cultistCampChunk(level))) {
            PENDING_SITES.offer(pending(level, event, SiteType.CULTIST_CAMP));
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        ServerLevel level = event.getServer().getLevel(Level.OVERWORLD);
        if (level == null) return;
        PendingSite pending;
        while ((pending = PENDING_SITES.poll()) != null) {
            if (pending.levelSeed() != level.getSeed()) continue;
            InvestigationSiteSavedData data = InvestigationSiteSavedData.get(level);
            if (pending.type() == SiteType.ABANDONED_CHURCH && !data.hasChurch()) {
                generateChurch(level, pending.target());
                data.recordChurch(pending.target());
                ProjectMystery.LOGGER.info("Generated abandoned church at {}", pending.target());
            } else if (pending.type() == SiteType.CULTIST_CAMP
                    && !data.hasCultistCamp()) {
                generateCultistCamp(level, pending.target());
                data.recordCultistCamp(pending.target());
                ProjectMystery.LOGGER.info("Generated cultist rescue camp at {}", pending.target());
            }
        }
    }

    public static void reportSites(ServerPlayer player) {
        ServerLevel level = player.getServer().getLevel(Level.OVERWORLD);
        if (level == null) return;
        InvestigationSiteSavedData data = InvestigationSiteSavedData.get(level);
        BlockPos church = data.church().orElseGet(() -> churchTarget(level));
        BlockPos cultistCamp = data.cultistCamp().orElseGet(() -> cultistCampTarget(level));
        report(player, church, "message.lord_of_mysteries.case.church");
        report(player, cultistCamp, "message.lord_of_mysteries.case.cultist_camp");
    }

    public static BlockPos churchTarget(ServerLevel level) {
        return target(level, churchChunk(level));
    }

    public static BlockPos cultistCampTarget(ServerLevel level) {
        return target(level, cultistCampChunk(level));
    }

    private static PendingSite pending(ServerLevel level, ChunkEvent.Load event,
                                       SiteType type) {
        ChunkPos chunk = event.getChunk().getPos();
        int x = chunk.getMinBlockX() + 8;
        int z = chunk.getMinBlockZ() + 8;
        int surface = event.getChunk().getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
        int y = Math.max(surface, level.getSeaLevel() + 1);
        return new PendingSite(level.getSeed(), type, new BlockPos(x, y, z));
    }

    private static BlockPos target(ServerLevel level, ChunkPos chunk) {
        return new BlockPos(chunk.getMinBlockX() + 8,
                level.getSharedSpawnPos().getY(), chunk.getMinBlockZ() + 8);
    }

    private static ChunkPos churchChunk(ServerLevel level) {
        return targetChunk(level, 0x434855524348L, 6, 8);
    }

    private static ChunkPos cultistCampChunk(ServerLevel level) {
        return targetChunk(level, 0x43554C54495354L, 10, 13);
    }

    private static ChunkPos targetChunk(ServerLevel level, long salt,
                                        int minimumRadius, int maximumRadius) {
        RandomSource random = RandomSource.create(level.getSeed() ^ salt);
        double angle = random.nextDouble() * Math.PI * 2d;
        int radius = minimumRadius + random.nextInt(maximumRadius - minimumRadius + 1);
        BlockPos spawn = level.getSharedSpawnPos();
        int spawnChunkX = Math.floorDiv(spawn.getX(), 16);
        int spawnChunkZ = Math.floorDiv(spawn.getZ(), 16);
        return new ChunkPos(
                spawnChunkX + (int) Math.round(Math.cos(angle) * radius),
                spawnChunkZ + (int) Math.round(Math.sin(angle) * radius));
    }

    private static void report(ServerPlayer player, BlockPos target, String key) {
        double deltaX = target.getX() - player.getX();
        double deltaZ = target.getZ() - player.getZ();
        int distance = (int) Math.round(Math.sqrt(deltaX * deltaX + deltaZ * deltaZ));
        String direction = GuideDirection.fromDelta(deltaX, deltaZ);
        player.sendSystemMessage(Component.translatable(
                key, Component.translatable("direction.lord_of_mysteries." + direction),
                distance, target.getX(), target.getZ()).withStyle(ChatFormatting.GOLD));
    }

    private static void generateChurch(ServerLevel level, BlockPos center) {
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -6; dz <= 6; dz++) {
                level.setBlock(center.offset(dx, -1, dz),
                        Blocks.MOSSY_STONE_BRICKS.defaultBlockState(), 3);
                for (int dy = 0; dy <= 5; dy++) {
                    level.setBlock(center.offset(dx, dy, dz), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -6; dz <= 6; dz++) {
                boolean edge = Math.abs(dx) == 4 || Math.abs(dz) == 6;
                if (!edge) continue;
                for (int dy = 0; dy <= 3; dy++) {
                    boolean doorway = dz == 6 && Math.abs(dx) <= 1 && dy <= 2;
                    boolean brokenWindow = Math.abs(dx) == 4 && dz == 0 && dy == 2;
                    if (!doorway && !brokenWindow) {
                        level.setBlock(center.offset(dx, dy, dz),
                                (dy + dx + dz) % 4 == 0
                                        ? Blocks.CRACKED_STONE_BRICKS.defaultBlockState()
                                        : Blocks.STONE_BRICKS.defaultBlockState(), 3);
                    }
                }
            }
        }
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -6; dz <= 6; dz++) {
                if ((Math.abs(dx) + Math.abs(dz)) % 5 != 0) {
                    level.setBlock(center.offset(dx, 4, dz),
                            Blocks.DARK_OAK_SLAB.defaultBlockState(), 3);
                }
            }
        }
        level.setBlock(center.offset(0, 0, -3), Blocks.LECTERN.defaultBlockState(), 3);
        level.setBlock(center.offset(-2, 0, -2), Blocks.COBWEB.defaultBlockState(), 3);
        level.setBlock(center.offset(2, 0, 2), Blocks.COBWEB.defaultBlockState(), 3);
        BlockPos chestPos = center.offset(2, 0, -4);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 3);
        if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setLootTable(CHURCH_LOOT, level.random.nextLong());
        }
    }

    private static void generateCultistCamp(ServerLevel level, BlockPos center) {
        for (int dx = -6; dx <= 6; dx++) {
            for (int dz = -6; dz <= 6; dz++) {
                level.setBlock(center.offset(dx, -1, dz),
                        Blocks.COARSE_DIRT.defaultBlockState(), 3);
                for (int dy = 0; dy <= 4; dy++) {
                    level.setBlock(center.offset(dx, dy, dz), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
        level.setBlock(center, Blocks.CAMPFIRE.defaultBlockState()
                .setValue(CampfireBlock.LIT, false), 3);
        for (int dz = -3; dz <= 3; dz++) {
            for (int dy = 0; dy <= 2; dy++) {
                level.setBlock(center.offset(-4, dy, dz),
                        Blocks.IRON_BARS.defaultBlockState(), 3);
            }
        }
        for (int dx = -4; dx <= -1; dx++) {
            level.setBlock(center.offset(dx, 3, -3),
                    Blocks.DARK_OAK_SLAB.defaultBlockState(), 3);
            level.setBlock(center.offset(dx, 3, 3),
                    Blocks.DARK_OAK_SLAB.defaultBlockState(), 3);
        }
        level.setBlock(center.offset(3, 0, -3), Blocks.BROWN_WOOL.defaultBlockState(), 3);
        level.setBlock(center.offset(3, 1, -3), Blocks.BROWN_WOOL.defaultBlockState(), 3);
        level.setBlock(center.offset(3, 2, -3), Blocks.BLACK_WOOL.defaultBlockState(), 3);
        BlockPos chestPos = center.offset(3, 0, 3);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 3);
        if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setLootTable(CULTIST_CAMP_LOOT, level.random.nextLong());
        }
    }

    private enum SiteType {
        ABANDONED_CHURCH,
        CULTIST_CAMP
    }

    private record PendingSite(long levelSeed, SiteType type, BlockPos target) {}
}
