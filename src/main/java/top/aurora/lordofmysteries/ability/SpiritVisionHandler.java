package top.aurora.lordofmysteries.ability;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerMysteryData;

/**
 * 灵视被动能力（批次1，设计文档 §6）。
 *
 * <p>特性：
 * <ul>
 *   <li>持续被动、可开关；服务端权威维护 {@link PlayerMysteryData#spiritVisionActive}；</li>
 *   <li>开启时 0.8 灵性/s 持续扣除，灵性耗尽自动关闭；</li>
 *   <li>每 tick 扫描 32 格内实体，按 {@link SpiritFactionColor} 分类，供客户端渲染。</li>
 * </ul>
 *
 * <p>切换入口：{@link #toggle(ServerPlayer)}（网络包/指令调用）。
 */
@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SpiritVisionHandler {

    private SpiritVisionHandler() {}

    /** 每秒扣费值（0.8 灵性/s）。 */
    public static final float DRAIN_PER_SECOND = 0.8f;
    /** 半径（格）。 */
    public static final double SCAN_RADIUS = 32.0;

    /**
     * 切换灵视开关。灵性不足时不允许开启。
     *
     * @return 切换后的开启状态；null 表示无法切换（非非凡者）
     */
    public static Boolean toggle(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (data.pathway == null) return null;

        if (data.spiritVisionActive) {
            data.spiritVisionActive = false;
            player.sendSystemMessage(Component.literal("§7[灵视] 已关闭"));
            return Boolean.FALSE;
        }

        // 开启前至少要有 1 灵性；否则给出提示但不打开。
        if (data.spirituality < 1f) {
            player.sendSystemMessage(Component.literal("§c灵性不足，无法开启灵视"));
            return Boolean.FALSE;
        }
        data.spiritVisionActive = true;
        player.sendSystemMessage(Component.literal("§b[灵视] 已开启（0.8/s）"));
        return Boolean.TRUE;
    }

    /**
     * 服务端 tick：每 20 tick 扣一次费；灵性耗尽自动关闭。
     * 客户端渲染由 {@code lord_of_mysteries.client.SpiritVisionRenderer}（占位）读取本处结果。
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer sp)) return;

        PlayerMysteryData data = MysteryCapability.get(sp);
        if (!data.spiritVisionActive) return;

        // 每 20 tick 扣 0.8：等价于 0.8/s。这个粒度对玩家可感（HUD 1秒变化一次），也省算力。
        if (sp.tickCount % 20 == 0) {
            if (!SpiritualityCost.tryConsume(data, DRAIN_PER_SECOND)) {
                data.spiritVisionActive = false;
                sp.sendSystemMessage(Component.literal("§c灵性耗尽，灵视自动关闭"));
                return;
            }
        }

        // 每 10 tick 扫一次实体分类（每秒 2 次），减少 chunk 遍历压力。
        if (sp.tickCount % 10 == 0) {
            classifyNearby(sp);
        }
    }

    /**
     * 扫描 32 格内实体并分类；对每个实体在其位置生成一小簇彩色 dust 粒子。
     *
     * <p>粒子作为 M1 的「灵视占位渲染」。用 {@link ServerLevel#sendParticles(ServerPlayer,
     * net.minecraft.core.particles.ParticleOptions, boolean, double, double, double, int, double, double, double, double)}
     * 的定向重载，只把粒子发给灵视开启的玩家，避免其他玩家看到干扰视觉。
     *
     * @return 实体 ID 与颜色对的列表，便于后续接入 S2C 描边渲染
     */
    public static List<EntityColor> classifyNearby(ServerPlayer sp) {
        AABB box = sp.getBoundingBox().inflate(SCAN_RADIUS);
        List<Entity> entities = sp.level().getEntities(sp, box);
        List<EntityColor> results = new ArrayList<>(entities.size());
        ServerLevel level = (ServerLevel) sp.level();
        for (Entity e : entities) {
            SpiritFactionColor color = SpiritFactionColor.classify(e);
            results.add(new EntityColor(e.getId(), color));
            Vector3f rgb = new Vector3f(
                    ((color.rgb >> 16) & 0xFF) / 255f,
                    ((color.rgb >> 8) & 0xFF) / 255f,
                    (color.rgb & 0xFF) / 255f);
            DustParticleOptions dust = new DustParticleOptions(rgb, 1.2f);
            level.sendParticles(sp, dust, true,
                    e.getX(), e.getY() + e.getBbHeight() * 0.5, e.getZ(),
                    3, 0.2, 0.4, 0.2, 0.0);
        }
        return results;
    }

    /** 实体 ID → 颜色的轻量记录。网络包用。 */
    public record EntityColor(int entityId, SpiritFactionColor color) {}
}
