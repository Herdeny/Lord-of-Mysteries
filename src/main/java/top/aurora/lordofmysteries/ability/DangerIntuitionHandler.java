package top.aurora.lordofmysteries.ability;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.potion.SeerPotionItem;

/**
 * 危险直觉被动能力（批次1，设计文档 §6）。
 *
 * <p>触发条件：
 * <ul>
 *   <li>玩家将受致命伤害（伤害值 ≥ 当前 health）；</li>
 *   <li>35% 概率避开本次致命攻击，并给出音效、提示与短暂速度加成；</li>
 *   <li>30s 冷却，冷却期内不再触发。</li>
 * </ul>
 *
 * <p>Forge 1.20.1 没有通用的“提前获知下一次攻击”事件，因此本实现使用
 * {@link LivingAttackEvent} 在伤害结算前拦截一次致命攻击，提供等价的反应窗口。
 */
@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DangerIntuitionHandler {

    private DangerIntuitionHandler() {}

    /** 触发概率。 */
    public static final float PROC_CHANCE = 0.35f;
    /** 冷却 30s = 600 tick。 */
    public static final long COOLDOWN_TICKS = 600L;

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;

        PlayerMysteryData data = MysteryCapability.get(sp);
        if (!SeerPotionItem.SEER_PATHWAY.equals(data.pathway) || data.sequence < 0 || data.sequence > 9) {
            return;
        }

        float damage = event.getAmount();
        if (damage < sp.getHealth()) return; // 只对可能致命的伤害预警

        long now = sp.level().getGameTime();
        if (!AbilityCooldowns.ready(data.dangerIntuitionCooldownEndTick, now)) return;

        if (sp.getRandom().nextFloat() >= PROC_CHANCE) {
            // 未触发也进入短冷却，避免每帧检定造成性能问题
            data.dangerIntuitionCooldownEndTick = AbilityCooldowns.start(now, 40L);
            return;
        }

        data.dangerIntuitionCooldownEndTick = AbilityCooldowns.start(now, COOLDOWN_TICKS);
        event.setCanceled(true);

        sp.level().playSound(null, sp.blockPosition(),
                SoundEvents.ALLAY_HURT, SoundSource.PLAYERS, 0.6f, 1.6f);
        sp.sendSystemMessage(Component.literal("§e⚠ 危险直觉：致命威胁正在逼近！"));
        sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 16, 1, false, false));
    }
}
