package top.aurora.lordofmysteries.player;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import top.aurora.lordofmysteries.ProjectMystery;

/**
 * 灵性自然恢复（Forge 1.20.1，设计文档 §5.2）。
 *
 * 恢复条件：非战斗 + 光照 ≥ 7 + 失控压力 < 30。
 * 恢复速率随途径/序列变化，M0 先用文档给出的近似值。
 */
@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SpiritualityRegenHandler {

    private SpiritualityRegenHandler() {}

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide()) return;
        if (player.tickCount % 20 != 0) return; // 每秒检查一次

        PlayerMysteryData data = MysteryCapability.get(player);
        if (data.pathway == null) return;

        boolean inCombat = player.getLastHurtByMobTimestamp() > player.tickCount - 100;
        boolean goodLight = player.level().getMaxLocalRawBrightness(player.blockPosition()) >= 7;
        boolean lowPressure = data.insanityPressure < 30f;

        if (!inCombat && goodLight && lowPressure) {
            float regenRate = getRegenRate(data.pathway, data.sequence);
            data.spirituality = Math.min(data.spiritualityMax, data.spirituality + regenRate);
        }
    }

    /**
     * 灵性恢复速率（pt/秒）。文档 §5.2 给的是 pt/Ns，这里换算成每秒近似值：
     * 序列9 ≈ 1.0/20s = 0.05/s，序列8 ≈ 1.2/18s ≈ 0.067/s，序列7 ≈ 1.5/15s = 0.1/s。
     * M1 接入数据驱动后改为读取 sequence JSON 的 spirituality_regen_bonus。
     */
    private static float getRegenRate(ResourceLocation pathway, int sequence) {
        return switch (sequence) {
            case 9 -> 0.05f;
            case 8 -> 0.067f;
            case 7 -> 0.1f;
            default -> 0.05f;
        };
    }
}
