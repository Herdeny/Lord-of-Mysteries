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

/**
 * 危险直觉被动能力（批次1，设计文档 §6）。
 *
 * <p>触发条件：
 * <ul>
 *   <li>玩家将受致命伤害（伤害值 ≥ 当前 health）；</li>
 *   <li>35% 概率给出预警：音效 + 提示 + 短暂速度加成；</li>
 *   <li>30s 冷却，冷却期内不再触发。</li>
 * </ul>
 *
 * <p>预警不阻挡伤害本身——设计意图是「让玩家有 0.8s 时间自救」，符合文档描述。
 * 使用 {@link LivingAttackEvent}（发生在伤害应用前）判断致命并触发预警。
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
        if (data.pathway == null) return;

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

        sp.level().playSound(null, sp.blockPosition(),
                SoundEvents.ALLAY_HURT, SoundSource.PLAYERS, 0.6f, 1.6f);
        sp.sendSystemMessage(Component.literal("§e⚠ 危险直觉：致命威胁正在逼近！"));
        // 短暂速度加成，帮助玩家躲避（0.8s ≈ 16 tick）。
        sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 1, false, false));
    }
}
