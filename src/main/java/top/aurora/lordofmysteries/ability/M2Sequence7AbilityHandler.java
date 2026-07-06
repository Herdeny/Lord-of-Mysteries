package top.aurora.lordofmysteries.ability;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.acting.ActingEvent;
import top.aurora.lordofmysteries.acting.ActingEventHandler;
import top.aurora.lordofmysteries.core.config.ServerConfig;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.potion.HunterPotionItem;
import top.aurora.lordofmysteries.potion.SpectatorPotionItem;

@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class M2Sequence7AbilityHandler {

    public static final float PACIFY_COST = 22f;
    public static final long PACIFY_COOLDOWN = 400L;
    public static final float MIND_SHOCK_COST = 18f;
    public static final long MIND_SHOCK_COOLDOWN = 240L;
    public static final float PSYCHOLOGICAL_CLOAK_DRAIN = 0.9f;
    public static final float FLAME_SPEAR_COST = 10f;
    public static final long FLAME_SPEAR_COOLDOWN = 40L;
    public static final float FIRE_RING_COST = 24f;
    public static final long FIRE_RING_COOLDOWN = 240L;

    private static final String CLOAK_TICKS = "psychiatrist7:cloak_ticks";
    private static final String FIRE_TICKS = "pyromaniac7:fire_ticks";

    private M2Sequence7AbilityHandler() {}

    public static boolean use(ServerPlayer player,
                              M2FoundationAbilityHandler.AbilitySlot slot) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (isPsychiatrist(data)) {
            return slot == M2FoundationAbilityHandler.AbilitySlot.PRIMARY
                    ? pacify(player, data) : mindShock(player, data);
        }
        if (isPyromaniac(data)) {
            return slot == M2FoundationAbilityHandler.AbilitySlot.PRIMARY
                    ? flameSpear(player, data) : fireRing(player, data);
        }
        return false;
    }

    private static boolean pacify(ServerPlayer player,
                                  PlayerMysteryData data) {
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.psychPacifyCooldownEndTick, now)) {
            return cooldown(player, data.psychPacifyCooldownEndTick, now);
        }
        List<Mob> targets = player.level().getEntitiesOfClass(
                Mob.class, player.getBoundingBox().inflate(8d),
                mob -> mob.isAlive()
                        && mob.getTarget() != null
                        && M2Sequence7AbilityLogic.canAffectMentalTarget(
                                false,
                                ServerConfig.PVP_MENTAL_ABILITIES.get(),
                                mob.getMaxHealth()));
        if (targets.isEmpty()) return noTarget(player);
        if (!SpiritualityCost.tryConsume(data, PACIFY_COST)) {
            return insufficient(player, PACIFY_COST);
        }

        boolean powerfulTarget = false;
        for (Mob target : targets) {
            powerfulTarget |= target.getMaxHealth() >= 40f;
            target.setTarget(null);
            target.getNavigation().stop();
            target.addEffect(new MobEffectInstance(
                    MobEffects.WEAKNESS, 160, 1, false, true));
        }
        data.psychPacifyCooldownEndTick =
                AbilityCooldowns.start(now, PACIFY_COOLDOWN);
        ServerLevel level = player.serverLevel();
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                player.getX(), player.getY() + 1d, player.getZ(),
                28, 1.5d, 0.8d, 1.5d, 0.02d);
        level.playSound(null, player.blockPosition(),
                SoundEvents.AMETHYST_BLOCK_RESONATE,
                SoundSource.PLAYERS, 0.8f, 0.9f);
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.psychiatrist.pacified",
                targets.size()).withStyle(ChatFormatting.AQUA));
        if (M2Sequence7AbilityLogic.crowdActingReady(targets.size())) {
            ActingEventHandler.trigger(
                    player, ActingEvent.PSYCHIATRIST7_PACIFY_CROWD, null);
        }
        if (powerfulTarget) {
            ActingEventHandler.trigger(
                    player, ActingEvent.PSYCHIATRIST7_DEESCALATE, null);
        }
        return true;
    }

    private static boolean mindShock(ServerPlayer player,
                                     PlayerMysteryData data) {
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.psychShockCooldownEndTick, now)) {
            return cooldown(player, data.psychShockCooldownEndTick, now);
        }
        LivingEntity target = AbilityTargeting.findLookTarget(player, 12d);
        if (target == null) return noTarget(player);
        if (!M2Sequence7AbilityLogic.canAffectMentalTarget(
                target instanceof Player,
                ServerConfig.PVP_MENTAL_ABILITIES.get(),
                target.getMaxHealth())) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.psychiatrist.resisted",
                    target.getDisplayName()));
            return false;
        }
        if (!SpiritualityCost.tryConsume(data, MIND_SHOCK_COST)) {
            return insufficient(player, MIND_SHOCK_COST);
        }

        boolean interrupted = target instanceof Mob mob && mob.getTarget() != null;
        target.addEffect(new MobEffectInstance(
                MobEffects.CONFUSION, 120, 0, false, true));
        target.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN, 120, 1, false, true));
        target.addEffect(new MobEffectInstance(
                MobEffects.WEAKNESS, 180, 1, false, true));
        if (target instanceof Mob mob) {
            mob.setTarget(null);
            mob.getNavigation().stop();
        }
        data.psychShockCooldownEndTick =
                AbilityCooldowns.start(now, MIND_SHOCK_COOLDOWN);
        player.serverLevel().sendParticles(ParticleTypes.WITCH,
                target.getX(), target.getY() + target.getBbHeight() * 0.7d,
                target.getZ(), 20, 0.35d, 0.5d, 0.35d, 0.02d);
        player.level().playSound(null, target.blockPosition(),
                SoundEvents.EVOKER_CAST_SPELL,
                SoundSource.PLAYERS, 0.7f, 1.5f);
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.psychiatrist.mind_shock",
                target.getDisplayName()));
        if (interrupted) {
            ActingEventHandler.trigger(
                    player, ActingEvent.PSYCHIATRIST7_INTERRUPT, target);
        }
        return true;
    }

    private static boolean flameSpear(ServerPlayer player,
                                      PlayerMysteryData data) {
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.pyroSpearCooldownEndTick, now)) {
            return cooldown(player, data.pyroSpearCooldownEndTick, now);
        }
        LivingEntity target = AbilityTargeting.findLookTarget(player, 20d);
        if (target == null) return noTarget(player);
        if (target instanceof ServerPlayer targetPlayer
                && !player.canHarmPlayer(targetPlayer)) {
            return false;
        }
        if (!SpiritualityCost.tryConsume(data, FLAME_SPEAR_COST)) {
            return insufficient(player, FLAME_SPEAR_COST);
        }

        float displayedDamage = M2Sequence7AbilityLogic.burningTargetDamage(
                6f, target.isOnFire());
        target.hurt(player.damageSources().playerAttack(player), 6f);
        target.setSecondsOnFire(5);
        data.pyroSpearCooldownEndTick =
                AbilityCooldowns.start(now, FLAME_SPEAR_COOLDOWN);
        player.serverLevel().sendParticles(ParticleTypes.FLAME,
                target.getX(), target.getY() + target.getBbHeight() * 0.5d,
                target.getZ(), 24, 0.25d, 0.4d, 0.25d, 0.05d);
        player.level().playSound(null, target.blockPosition(),
                SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.8f, 1.2f);
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.pyromaniac.flame_spear",
                target.getDisplayName(), displayedDamage));
        if (player.distanceToSqr(target) >= 144d) {
            ActingEventHandler.trigger(
                    player, ActingEvent.PYROMANIAC7_LONG_SHOT, target);
        }
        return true;
    }

    private static boolean fireRing(ServerPlayer player,
                                    PlayerMysteryData data) {
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.pyroRingCooldownEndTick, now)) {
            return cooldown(player, data.pyroRingCooldownEndTick, now);
        }
        AABB area = player.getBoundingBox().inflate(5d);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(
                LivingEntity.class, area,
                target -> target.isAlive()
                        && target != player
                        && (!(target instanceof ServerPlayer targetPlayer)
                            || player.canHarmPlayer(targetPlayer)));
        if (targets.isEmpty()) return noTarget(player);
        if (!SpiritualityCost.tryConsume(data, FIRE_RING_COST)) {
            return insufficient(player, FIRE_RING_COST);
        }

        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().playerAttack(player), 4f);
            target.setSecondsOnFire(4);
            target.knockback(0.8d,
                    player.getX() - target.getX(),
                    player.getZ() - target.getZ());
        }
        data.pyroRingCooldownEndTick =
                AbilityCooldowns.start(now, FIRE_RING_COOLDOWN);
        player.serverLevel().sendParticles(ParticleTypes.FLAME,
                player.getX(), player.getY() + 0.4d, player.getZ(),
                64, 2.2d, 0.2d, 2.2d, 0.04d);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1f, 0.8f);
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.pyromaniac.fire_ring",
                targets.size()));
        if (M2Sequence7AbilityLogic.crowdActingReady(targets.size())) {
            ActingEventHandler.trigger(
                    player, ActingEvent.PYROMANIAC7_FIRE_RING, null);
        }
        return true;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer player)
                || player.tickCount % 20 != 0) {
            return;
        }
        PlayerMysteryData data = MysteryCapability.get(player);
        if (isPsychiatrist(data)) {
            tickPsychologicalCloak(player, data);
        } else if (isPyromaniac(data)) {
            tickFireAffinity(player, data);
        }
    }

    private static void tickPsychologicalCloak(ServerPlayer player,
                                               PlayerMysteryData data) {
        if (!player.isShiftKeyDown()) {
            data.actingCounters.put(CLOAK_TICKS, 0);
            return;
        }
        if (!SpiritualityCost.tryConsume(data, PSYCHOLOGICAL_CLOAK_DRAIN)) {
            data.actingCounters.put(CLOAK_TICKS, 0);
            return;
        }
        player.level().getEntitiesOfClass(
                Mob.class, player.getBoundingBox().inflate(8d),
                mob -> mob.getTarget() == player).forEach(mob -> {
                    mob.setTarget(null);
                    mob.getNavigation().stop();
                });
        int ticks = data.actingCounters.merge(CLOAK_TICKS, 20, Integer::sum);
        if (M2Sequence7AbilityLogic.sustainedActingReady(ticks)) {
            data.actingCounters.put(CLOAK_TICKS, 0);
            ActingEventHandler.trigger(
                    player, ActingEvent.PSYCHIATRIST7_CLOAK, null);
        }
    }

    private static void tickFireAffinity(ServerPlayer player,
                                         PlayerMysteryData data) {
        player.addEffect(new MobEffectInstance(
                MobEffects.FIRE_RESISTANCE, 40, 0, false, false, true));
        if (!player.isOnFire() && !player.isInLava()) {
            data.actingCounters.put(FIRE_TICKS, 0);
            return;
        }
        int ticks = data.actingCounters.merge(FIRE_TICKS, 20, Integer::sum);
        if (M2Sequence7AbilityLogic.fireAffinityActingReady(ticks)) {
            data.actingCounters.put(FIRE_TICKS, 0);
            ActingEventHandler.trigger(
                    player, ActingEvent.PYROMANIAC7_WALK_THROUGH_FIRE, null);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer attacker) {
            PlayerMysteryData attackerData = MysteryCapability.get(attacker);
            if (isPyromaniac(attackerData)) {
                event.setAmount(M2Sequence7AbilityLogic.burningTargetDamage(
                        event.getAmount(), event.getEntity().isOnFire()));
            }
        }
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        PlayerMysteryData data = MysteryCapability.get(player);
        if (isPsychiatrist(data)) {
            data.actingCounters.put(CLOAK_TICKS, 0);
        }
        if (isPyromaniac(data)
                && event.getSource().is(DamageTypeTags.IS_FIRE)) {
            event.setAmount(event.getAmount() * 0.25f);
        }
    }

    private static boolean isPsychiatrist(PlayerMysteryData data) {
        return SpectatorPotionItem.SPECTATOR_PATHWAY.equals(data.pathway)
                && data.sequence == 7;
    }

    private static boolean isPyromaniac(PlayerMysteryData data) {
        return HunterPotionItem.HUNTER_PATHWAY.equals(data.pathway)
                && data.sequence == 7;
    }

    private static boolean noTarget(ServerPlayer player) {
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.ability.no_target"));
        return false;
    }

    private static boolean insufficient(ServerPlayer player, float cost) {
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.ability.insufficient_spirit", cost));
        return false;
    }

    private static boolean cooldown(ServerPlayer player,
                                    long cooldownEnd,
                                    long now) {
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.ability.cooldown",
                Math.max(1L,
                        AbilityCooldowns.remaining(cooldownEnd, now) / 20L)));
        return false;
    }
}
