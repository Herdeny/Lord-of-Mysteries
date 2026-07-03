package top.aurora.lordofmysteries.ability;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;

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
import top.aurora.lordofmysteries.potion.SpectatorPotionItem;

@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SpectatorAbilityHandler {

    public static final float EMOTION_DRAIN_PER_SECOND = 0.5f;
    public static final long BEHAVIOR_PREDICTION_COOLDOWN = 160L;
    public static final float SURFACE_READ_COST = 18f;
    public static final long SURFACE_READ_COOLDOWN = 600L;
    public static final float MENTAL_SUGGESTION_COST = 25f;
    public static final long MENTAL_SUGGESTION_COOLDOWN = 800L;

    private SpectatorAbilityHandler() {}

    public static Boolean toggleEmotionRead(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (!hasSpectatorAbility(data, 9)) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.spectator.unavailable"));
            return null;
        }
        if (data.emotionReadActive) {
            data.emotionReadActive = false;
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.spectator.emotion_off"));
            return Boolean.FALSE;
        }
        if (data.spirituality < 1f) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.ability.insufficient_spirit",
                    EMOTION_DRAIN_PER_SECOND));
            return Boolean.FALSE;
        }
        data.emotionReadActive = true;
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.spectator.emotion_on"));
        return Boolean.TRUE;
    }

    public static boolean readSurface(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (!hasSpectatorAbility(data, 8)) return unavailable(player);

        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.surfaceReadCooldownEndTick, now)) {
            return cooldown(player, data.surfaceReadCooldownEndTick, now);
        }
        LivingEntity target = AbilityTargeting.findLookTarget(player, 16d);
        if (target == null) return noTarget(player);
        if (!SpiritualityCost.tryConsume(data, SURFACE_READ_COST)) {
            return insufficient(player, SURFACE_READ_COST);
        }

        data.surfaceReadCooldownEndTick = AbilityCooldowns.start(now, SURFACE_READ_COOLDOWN);
        String state = surfaceState(target);
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.spectator.surface_result",
                target.getDisplayName(),
                Component.translatable("emotion.lord_of_mysteries." + state))
                .withStyle(ChatFormatting.AQUA));
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.5f, 1.4f);
        if (data.sequence == 8) {
            ActingEventHandler.trigger(player, ActingEvent.SPECTATOR8_SURFACE_READ, target);
        }
        return true;
    }

    public static boolean castMentalSuggestion(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (!hasSpectatorAbility(data, 8)) return unavailable(player);

        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.mentalSuggestionCooldownEndTick, now)) {
            return cooldown(player, data.mentalSuggestionCooldownEndTick, now);
        }
        LivingEntity target = AbilityTargeting.findLookTarget(player, 12d);
        if (target == null) return noTarget(player);
        if (target instanceof ServerPlayer && !ServerConfig.PVP_MENTAL_ABILITIES.get()) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.spectator.pvp_disabled"));
            return false;
        }
        if (!(target instanceof Player) && target.getMaxHealth() > 80f) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.spectator.target_resisted",
                    target.getDisplayName()));
            return false;
        }
        if (!SpiritualityCost.tryConsume(data, MENTAL_SUGGESTION_COST)) {
            return insufficient(player, MENTAL_SUGGESTION_COST);
        }

        int duration = suggestionDuration(target);
        data.mentalSuggestionCooldownEndTick =
                AbilityCooldowns.start(now, MENTAL_SUGGESTION_COOLDOWN);
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                duration, 2, false, true));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,
                duration, 1, false, true));
        if (target instanceof Mob mob) {
            mob.setTarget(null);
            mob.getNavigation().stop();
        }
        if (target instanceof ServerPlayer targetPlayer) {
            targetPlayer.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.spectator.suggestion_received",
                    player.getDisplayName(),
                    duration / 20));
        }

        ServerLevel level = player.serverLevel();
        level.sendParticles(ParticleTypes.WITCH,
                target.getX(), target.getY() + target.getBbHeight() * 0.7, target.getZ(),
                18, 0.35, 0.5, 0.35, 0.02);
        level.playSound(null, target.blockPosition(),
                SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 0.6f, 1.3f);
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.spectator.suggestion_applied",
                target.getDisplayName(),
                duration / 20));
        if (data.sequence == 8) {
            ActingEventHandler.trigger(player, ActingEvent.SPECTATOR8_MENTAL_SUGGESTION, target);
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
        if (!data.emotionReadActive) return;
        if (!hasSpectatorAbility(data, 9)
                || !SpiritualityCost.tryConsume(data, EMOTION_DRAIN_PER_SECOND)) {
            data.emotionReadActive = false;
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.spectator.emotion_exhausted"));
            return;
        }

        LivingEntity target = AbilityTargeting.findLookTarget(player, 16d);
        if (target == null) {
            player.displayClientMessage(Component.translatable(
                    "message.lord_of_mysteries.spectator.no_emotion"), true);
            return;
        }
        SpectatorEmotionLogic.Emotion emotion = emotionOf(target, player);
        player.displayClientMessage(Component.translatable(
                "message.lord_of_mysteries.spectator.emotion_result",
                target.getDisplayName(),
                Component.translatable("emotion.lord_of_mysteries." + emotion.id())), true);

        if (data.sequence == 9) {
            trackObservation(player, data, target);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer observer) {
            resetObservationAfterCombat(observer, event.getEntity());
        }
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !(event.getSource().getEntity() instanceof LivingEntity)) {
            return;
        }
        PlayerMysteryData data = MysteryCapability.get(player);
        if (!hasSpectatorAbility(data, 9)) return;
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.behaviorPredictionCooldownEndTick, now)) return;

        data.behaviorPredictionCooldownEndTick =
                AbilityCooldowns.start(now, BEHAVIOR_PREDICTION_COOLDOWN);
        event.setAmount(event.getAmount() * 0.6f);
        player.displayClientMessage(Component.translatable(
                "message.lord_of_mysteries.spectator.predicted_attack"), true);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.NOTE_BLOCK_HAT.value(), SoundSource.PLAYERS, 0.5f, 1.8f);

        if (data.sequence == 9) {
            int predictions = data.actingCounters.merge("spectator9:prediction_streak", 1, Integer::sum);
            if (predictions >= 5) {
                data.actingCounters.put("spectator9:prediction_streak", 0);
                ActingEventHandler.trigger(player, ActingEvent.SPECTATOR9_PREDICT_FIVE, null);
            }
        }
    }

    private static void resetObservationAfterCombat(ServerPlayer observer,
                                                    LivingEntity target) {
        PlayerMysteryData data = MysteryCapability.get(observer);
        if (!hasSpectatorAbility(data, 9)) return;
        data.actingHistory.remove("spectator9:observation_start:" + target.getUUID());
    }

    private static void trackObservation(ServerPlayer player, PlayerMysteryData data,
                                         LivingEntity target) {
        long now = player.level().getGameTime();
        String key = "spectator9:observation_start:" + target.getUUID();
        long started = data.actingHistory.getOrDefault(key, 0L);
        if (started == 0L) {
            data.actingHistory.put(key, now);
        } else if (now - started >= 600L) {
            data.actingHistory.put(key, now);
            ActingEventHandler.trigger(
                    player, ActingEvent.SPECTATOR9_OBSERVE_WITHOUT_FIGHT, target);
        }
    }

    private static SpectatorEmotionLogic.Emotion emotionOf(LivingEntity target,
                                                            ServerPlayer observer) {
        boolean aggressive = target instanceof Mob mob && mob.getTarget() != null;
        float healthRatio = target.getMaxHealth() <= 0f
                ? 1f : target.getHealth() / target.getMaxHealth();
        boolean attentive = !(target instanceof Enemy)
                && target.distanceToSqr(observer) <= 36d;
        return SpectatorEmotionLogic.classify(aggressive, healthRatio, attentive);
    }

    private static String surfaceState(LivingEntity target) {
        if (target instanceof Mob mob && mob.getTarget() != null) return "anger";
        if (target.getHealth() <= target.getMaxHealth() * 0.25f) return "fear";
        if (target instanceof Villager) return "curiosity";
        if (target instanceof Enemy) return "hostility";
        return "calm";
    }

    private static int suggestionDuration(LivingEntity target) {
        if (target instanceof Player player && player.isShiftKeyDown()) return 60;
        if (target instanceof ServerPlayer player) {
            PlayerMysteryData targetData = MysteryCapability.get(player);
            if (hasSpectatorAbility(targetData, 9)) return 80;
        }
        return 160;
    }

    private static boolean hasSpectatorAbility(PlayerMysteryData data, int requiredSequence) {
        return SpectatorPotionItem.SPECTATOR_PATHWAY.equals(data.pathway)
                && data.sequence >= 0
                && data.sequence <= requiredSequence;
    }

    private static boolean unavailable(ServerPlayer player) {
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.spectator.unavailable"));
        return false;
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

    private static boolean cooldown(ServerPlayer player, long cooldownEnd, long now) {
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.ability.cooldown",
                Math.max(1L, AbilityCooldowns.remaining(cooldownEnd, now) / 20L)));
        return false;
    }
}
