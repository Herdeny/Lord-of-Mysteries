package top.aurora.projectmystery.player;

import net.minecraft.world.entity.player.Player;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import net.minecraft.resources.ResourceLocation;

import top.aurora.projectmystery.ProjectMystery;

/**
 * 灵性自然恢复（设计文档 §5.2）。
 *
 * 恢复条件：非战斗 + 光照 ≥ 7 + 失控压力 < 30。
 * 恢复速率随途径/序列变化，M0 先用文档给出的近似值。
 */
@EventBusSubscriber(modid = ProjectMystery.MOD_ID)
public final class SpiritualityRegenHandler {

    private SpiritualityRegenHandler() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (player.tickCount % 20 != 0) return; // 每秒检查一次

        PlayerMysteryData data = player.getData(MysteryAttachments.MYSTERY_DATA.get());
        if (data.pathway == null) return;

        boolean inCombat = player.getLastHurtByMobTimestamp() > player.tickCount - 100;
        boolean goodLight = player.level().getMaxLocalRawBrightness(player.blockPosition()) >= 7;
        boolean lowPressure = data.insanityPressure < 30f;

        if (!inCombat && goodLight && lowPressure) {
            float regenRate = getRegenRate(data.pathway, data.sequence);
            data.spirituality = Math.min(data.spiritualityMax, data.spirituality + regenRate);
            player.setData(MysteryAttachments.MYSTERY_DATA.get(), data);
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
