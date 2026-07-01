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
 *
 * <p>当前实现是服务端权威：只在服务端 tick 中修改数据。客户端 HUD 若要显示灵性，
 * 后续应由网络同步包或能力数据同步机制把服务端结果发送过去。
 */
@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SpiritualityRegenHandler {

    /** 事件处理类只包含静态方法。 */
    private SpiritualityRegenHandler() {}

    /**
     * 玩家 tick 回调：每秒检查一次是否满足自然恢复条件。
     *
     * <p>PlayerTickEvent 每 tick 触发两次（START/END），并且客户端/服务端都会收到。
     * 因此这里依次过滤 END 阶段、客户端、非整秒 tick，避免重复恢复。
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide()) return;
        if (player.tickCount % 20 != 0) return; // 每秒检查一次

        PlayerMysteryData data = MysteryCapability.get(player);
        // 普通人没有途径，不参与灵性恢复；未来可给普通人设计基础灵感值时再扩展。
        if (data.pathway == null) return;

        // 最近 5 秒内被生物伤害过，视为战斗状态，不自然恢复。
        boolean inCombat = player.getLastHurtByMobTimestamp() > player.tickCount - 100;

        // 使用原版局部亮度作为“安全环境”的粗略判断；后续可改成维度/方块/天气综合条件。
        boolean goodLight = player.level().getMaxLocalRawBrightness(player.blockPosition()) >= 7;

        // 失控压力过高时不允许自然平复，迫使玩家通过仪式、药剂或休整处理风险。
        boolean lowPressure = data.insanityPressure < 30f;

        if (!inCombat && goodLight && lowPressure) {
            float regenRate = getRegenRate(data.pathway, data.sequence);
            // 恢复值必须被上限截断，避免长时间 tick 后超过 spiritualityMax。
            data.spirituality = Math.min(data.spiritualityMax, data.spirituality + regenRate);
        }
    }

    /**
     * 灵性恢复速率（pt/秒）。文档 §5.2 给的是 pt/Ns，这里换算成每秒近似值：
     * 序列9 ≈ 1.0/20s = 0.05/s，序列8 ≈ 1.2/18s ≈ 0.067/s，序列7 ≈ 1.5/15s = 0.1/s。
     * M1 接入数据驱动后改为读取 sequence JSON 的 spirituality_regen_bonus。
     *
     * @param pathway 玩家当前途径，M0 暂未区分，保留参数用于未来按途径调表
     * @param sequence 玩家当前序列
     * @return 每秒恢复的灵性点数
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
