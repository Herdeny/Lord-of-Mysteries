package top.aurora.lordofmysteries.acting;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import top.aurora.lordofmysteries.core.config.ServerConfig;
import top.aurora.lordofmysteries.knowledge.M1TrialTracker;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerFeedback;
import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.potion.PotionQuality;

public final class ActingEventHandler {

    private ActingEventHandler() {}

    public static float trigger(ServerPlayer player, ActingEvent event, @Nullable Entity target) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (data.pathway == null
                || !event.pathway().equals(data.pathway.getPath())
                || data.sequence != event.sequence()) {
            return 0f;
        }

        long now = player.level().getGameTime();
        String historyKey = event.id();
        if (target != null) historyKey += ":" + target.getUUID();
        long last = data.actingHistory.getOrDefault(historyKey, 0L);

        float novelty = ActingCalculator.novelty(last, now, event.noveltyDecayTicks());
        float gain = ActingCalculator.gain(
                event.baseDigestion(),
                1f,
                novelty,
                ActingCalculator.risk(data.insanityPressure),
                PotionQuality.fromId(data.potionQuality).digestionMultiplier(),
                ServerConfig.DIGESTION_MULTIPLIER.get().floatValue());
        data.digestion = Math.max(0f, Math.min(100f, data.digestion + gain));
        data.actingHistory.put(historyKey, now);
        ActingIdentityService.recordPractice(data, novelty, gain);
        if (gain > 0f) M1TrialTracker.recordActing(player);

        if (ServerConfig.SHOW_EXACT_DIGESTION.get()) {
            PlayerFeedback.send(player, Component.translatable(
                    "message.lord_of_mysteries.acting.exact",
                    String.format(java.util.Locale.ROOT, "%+.1f", gain),
                    String.format(java.util.Locale.ROOT, "%.1f", data.digestion))
                    .withStyle(gain >= 0f ? ChatFormatting.AQUA : ChatFormatting.RED));
        } else {
            String key = gain < 0f
                    ? "message.lord_of_mysteries.acting.penalty"
                    : data.digestion >= 100f
                    ? "message.lord_of_mysteries.acting.complete"
                    : gain >= 10f
                    ? "message.lord_of_mysteries.acting.strong"
                    : "message.lord_of_mysteries.acting.faint";
            PlayerFeedback.send(player,
                    Component.translatable(key).withStyle(ChatFormatting.DARK_AQUA));
        }
        return gain;
    }
}
