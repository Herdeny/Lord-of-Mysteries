package top.aurora.lordofmysteries.player;

import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import top.aurora.lordofmysteries.ProjectMystery;

/**
 * 污染与失控压力检定（Forge 1.20.1，设计文档 §5.3）。
 *
 * 阈值表：
 *   0-24  稳定      无检定
 *   25-49 轻度异常  每 5min 1 次
 *   50-74 危险      每 2min 1 次
 *   75-99 临界      每 30s 1 次
 *   100   失控      立即触发结局
 *
 * M0 仅做检定节奏骨架；具体事件由 InsanityEventHandler（M1）实现。
 *
 * <p>这里不直接每 tick 触发负面事件，而是根据污染区间降低检查间隔。这样既符合
 * 设计文档中的风险节奏，也避免在高在线人数服务器上产生过多无意义计算。
 */
@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PollutionEffectHandler {

    /** 事件处理类只包含静态方法。 */
    private PollutionEffectHandler() {}

    /**
     * 玩家污染 tick 检查。
     *
     * <p>只在服务端 ServerPlayer 上运行，保证失控事件、刷怪、爆炸等结果由服务端裁决。
     */
    @SubscribeEvent
    public static void onPollutionTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        PlayerMysteryData data = MysteryCapability.get(player);
        // 普通人没有非凡者污染轨道，暂不参与检定。
        if (data.pathway == null) return;

        float pollution = data.pollution;

        if (pollution >= 100f) {
            // TODO(M1): InsanityEventHandler.triggerBreakdown(player, data);
            ProjectMystery.LOGGER.debug("[Pollution] {} 达到失控阈值（占位）", player.getGameProfile().getName());
            return;
        }

        // 20 tick = 1 秒：600 tick = 30 秒，2400 tick = 2 分钟，6000 tick = 5 分钟。
        // 污染越高，检查越频繁；M1 的具体事件处理器会在这些分支里实现。
        if (pollution >= 75f && player.tickCount % 600 == 0) {
            // TODO(M1): triggerSevereEvent
        } else if (pollution >= 50f && player.tickCount % 2400 == 0) {
            // TODO(M1): triggerModerateEvent
        } else if (pollution >= 25f && player.tickCount % 6000 == 0) {
            // TODO(M1): triggerMildEvent
        }
    }
}
