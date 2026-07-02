package top.aurora.lordofmysteries.acting;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import top.aurora.lordofmysteries.core.config.ServerConfig;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.potion.PotionQuality;
import top.aurora.lordofmysteries.potion.SeerPotionItem;

public final class ActingEventHandler {

    private ActingEventHandler() {}

    public static float trigger(ServerPlayer player, ActingEvent event, @Nullable Entity target) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (!SeerPotionItem.SEER_PATHWAY.equals(data.pathway) || data.sequence != 9) return 0f;

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

        if (ServerConfig.SHOW_EXACT_DIGESTION.get()) {
            player.sendSystemMessage(Component.translatable(
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
            player.sendSystemMessage(Component.translatable(key).withStyle(ChatFormatting.DARK_AQUA));
        }
        return gain;
    }
}
