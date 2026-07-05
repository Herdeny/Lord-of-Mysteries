package top.aurora.lordofmysteries.player;

import java.util.Locale;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import top.aurora.lordofmysteries.core.config.ServerConfig;
import top.aurora.lordofmysteries.entity.SeerBreakdownEntity;
import top.aurora.lordofmysteries.registry.ModEntities;

public final class InsanityEventHandler {

    private static final long BREAKDOWN_COOLDOWN = 12000L;
    private static final long MENTAL_TRAUMA_DURATION = 1728000L;

    private InsanityEventHandler() {}

    public static void triggerMildEvent(ServerPlayer player, PlayerMysteryData data) {
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.AMBIENT, 0.35f, 0.55f);
        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0, false, false));
        player.sendSystemMessage(Component.translatable(
                whisperKey(player, "mild")).withStyle(ChatFormatting.DARK_GRAY));
    }

    public static void triggerModerateEvent(ServerPlayer player, PlayerMysteryData data) {
        data.insanityPressure = Math.min(100f, data.insanityPressure + 5f);
        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 160, 0, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0, false, false));
        player.sendSystemMessage(Component.translatable(
                whisperKey(player, "moderate")).withStyle(ChatFormatting.DARK_PURPLE));
    }

    public static void triggerSevereEvent(ServerPlayer player, PlayerMysteryData data) {
        data.insanityPressure = Math.min(100f, data.insanityPressure + 10f);
        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 300, 1, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 1, false, false));
        player.sendSystemMessage(Component.translatable(
                whisperKey(player, "severe")).withStyle(ChatFormatting.RED));
    }

    public static void triggerBreakdown(ServerPlayer player, PlayerMysteryData data) {
        long now = player.level().getGameTime();
        if (now < data.breakdownCooldownEndTick) return;
        data.breakdownCooldownEndTick = now + BREAKDOWN_COOLDOWN;

        String mode = ServerConfig.BREAKDOWN_MODE.get().toLowerCase(Locale.ROOT);
        switch (mode) {
            case "death" -> {
                player.sendSystemMessage(Component.translatable(
                        "message.lord_of_mysteries.insanity.breakdown_death").withStyle(ChatFormatting.DARK_RED));
                player.hurt(player.damageSources().magic(), Float.MAX_VALUE);
            }
            case "permanent" -> permanentBreakdown(player, data);
            default -> recoverableBreakdown(player, data, now);
        }
    }

    private static void recoverableBreakdown(ServerPlayer player, PlayerMysteryData data, long now) {
        data.pollution = 70f;
        data.insanityPressure = 60f;
        data.spiritVisionActive = false;
        data.mentalTraumaEndTick = now + MENTAL_TRAUMA_DURATION;

        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 600, 10, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 600, 4, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 300, 0, false, false));
        spawnBreakdownBody(player);
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.insanity.breakdown_recoverable")
                .withStyle(ChatFormatting.DARK_RED));
    }

    private static void permanentBreakdown(ServerPlayer player, PlayerMysteryData data) {
        data.pathway = null;
        data.sequence = -1;
        data.spirituality = 0f;
        data.spiritualityMax = 100f;
        data.digestion = 0f;
        data.pollution = 50f;
        data.insanityPressure = 50f;
        data.spiritVisionActive = false;
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.insanity.breakdown_permanent")
                .withStyle(ChatFormatting.DARK_RED));
    }

    private static void spawnBreakdownBody(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return;
        SeerBreakdownEntity body = ModEntities.SEER_BREAKDOWN.get().create(level);
        if (body == null) return;
        body.moveTo(player.getX() + 1.5, player.getY(), player.getZ() + 1.5,
                player.getYRot(), player.getXRot());
        body.setCustomName(Component.translatable("entity.lord_of_mysteries.seer_breakdown"));
        body.setCustomNameVisible(true);
        body.setGlowingTag(true);
        body.setTarget(player);
        level.addFreshEntity(body);
    }

    private static String whisperKey(ServerPlayer player, String severity) {
        return "message.lord_of_mysteries.insanity." + severity + "."
                + player.getRandom().nextInt(4);
    }
}
