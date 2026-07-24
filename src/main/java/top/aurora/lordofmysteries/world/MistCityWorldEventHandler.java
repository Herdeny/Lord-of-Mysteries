package top.aurora.lordofmysteries.world;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.commission.MysticalExposurePolicy;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerDataSection;
import top.aurora.lordofmysteries.player.PlayerMysteryData;

@Mod.EventBusSubscriber(
        modid = ProjectMystery.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class MistCityWorldEventHandler {

    private MistCityWorldEventHandler() {}

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        int tick = event.getServer().getTickCount();
        if (tick % 100 != 0) return;
        ServerLevel level = event.getServer().getLevel(Level.OVERWORLD);
        if (level == null) return;
        long day = Math.floorDiv(level.getDayTime(), 24_000L);
        MistCityWorldEvent worldEvent =
                MistCityWorldEventPolicy.eventForDay(level.getSeed(), day);
        MistCityWorldEventSavedData saved =
                MistCityWorldEventSavedData.get(level);
        if (saved.update(day, worldEvent)) {
            event.getServer().getPlayerList().getPlayers().forEach(player ->
                    player.sendSystemMessage(Component.translatable(
                                    "message.lord_of_mysteries.world_event.changed",
                                    Component.translatable(
                                            worldEvent.translationKey()))
                            .withStyle(worldEvent == MistCityWorldEvent.CLEAR
                                    ? ChatFormatting.GRAY
                                    : ChatFormatting.LIGHT_PURPLE)));
        }
        applyVisibility(level, worldEvent);
        if (tick % 1_200 == 0) {
            applyMinuteEffects(level, worldEvent);
        }
    }

    private static void applyVisibility(
            ServerLevel level, MistCityWorldEvent worldEvent) {
        for (ServerPlayer player : level.players()) {
            if (player.isCreative() || player.isSpectator()) continue;
            if (MistCityWorldEventPlayerEffects.obscuresOutdoorVision(
                    worldEvent,
                    level.canSeeSky(player.blockPosition()))) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.DARKNESS, 140, 0,
                        true, false, true));
            }
        }
    }

    private static void applyMinuteEffects(
            ServerLevel level, MistCityWorldEvent worldEvent) {
        for (ServerPlayer player : level.players()) {
            if (player.isCreative() || player.isSpectator()) continue;
            PlayerMysteryData data = MysteryCapability.get(player);
            MistCityWorldEventPlayerEffects.MinuteEffect effect =
                    MistCityWorldEventPlayerEffects.minuteEffect(
                            worldEvent,
                            data.pathway != null,
                            isSheltered(level, player));
            if (!effect.active()) continue;
            data.insanityPressure = Math.min(
                    100f,
                    data.insanityPressure + effect.pressureIncrease());
            data.mysticalExposure = MysticalExposurePolicy.adjust(
                    data.mysticalExposure,
                    effect.exposureIncrease());
            data.markDirty(PlayerDataSection.CORE);
            if (effect.exposureIncrease() > 0f) {
                data.markDirty(PlayerDataSection.SOCIAL);
            }
            player.sendSystemMessage(Component.translatable(
                            "message.lord_of_mysteries.world_event.minute_effect",
                            Component.translatable(
                                    worldEvent.translationKey()),
                            effect.pressureIncrease(),
                            effect.exposureIncrease())
                    .withStyle(ChatFormatting.DARK_PURPLE));
        }
    }

    private static boolean isSheltered(
            ServerLevel level, ServerPlayer player) {
        int radius = MistCityDistrictLayout.maximumHorizontalRadius();
        return MistCityOutpostSavedData.get(level).outpost()
                .map(outpost -> outpost.distSqr(
                        player.blockPosition()) <= (long) radius * radius)
                .orElse(false);
    }
}
