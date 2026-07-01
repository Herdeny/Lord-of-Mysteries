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
 */
@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PollutionEffectHandler {

    private PollutionEffectHandler() {}

    @SubscribeEvent
    public static void onPollutionTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        PlayerMysteryData data = MysteryCapability.get(player);
        if (data.pathway == null) return;

        float pollution = data.pollution;

        if (pollution >= 100f) {
            // TODO(M1): InsanityEventHandler.triggerBreakdown(player, data);
            ProjectMystery.LOGGER.debug("[Pollution] {} 达到失控阈值（占位）", player.getGameProfile().getName());
            return;
        }
        if (pollution >= 75f && player.tickCount % 600 == 0) {
            // TODO(M1): triggerSevereEvent
        } else if (pollution >= 50f && player.tickCount % 2400 == 0) {
            // TODO(M1): triggerModerateEvent
        } else if (pollution >= 25f && player.tickCount % 6000 == 0) {
            // TODO(M1): triggerMildEvent
        }
    }
}
