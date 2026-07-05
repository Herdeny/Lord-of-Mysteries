package top.aurora.lordofmysteries.player;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.knowledge.M1TrialTracker;

@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RestRecoveryHandler {

    private static final ResourceLocation SAFE_REST = ResourceLocation.fromNamespaceAndPath(
            ProjectMystery.MOD_ID, "knowledge/safe_rest");

    private RestRecoveryHandler() {}

    @SubscribeEvent
    public static void onWakeUp(PlayerWakeUpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || event.wakeImmediately() || !event.updateLevel()) return;

        PlayerMysteryData data = MysteryCapability.get(player);
        long currentDay = player.level().getDayTime() / 24000L;
        if (!RestRecoveryRules.canRecover(
                currentDay, data.lastRestRecoveryDay, data.insanityPressure)) return;

        float before = data.insanityPressure;
        data.insanityPressure = RestRecoveryRules.pressureAfterRest(before);
        data.lastRestRecoveryDay = currentDay;
        data.knownKnowledge.add(SAFE_REST);
        M1TrialTracker.recordRest(player);
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.rest.recovered",
                Math.round(before - data.insanityPressure),
                Math.round(data.insanityPressure))
                .withStyle(ChatFormatting.AQUA));
    }
}
