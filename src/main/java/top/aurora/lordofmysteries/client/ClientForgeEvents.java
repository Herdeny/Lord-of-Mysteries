package top.aurora.lordofmysteries.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.network.PMNetwork;
import top.aurora.lordofmysteries.network.ToggleSpiritVisionC2SPacket;
import top.aurora.lordofmysteries.network.UseSimpleDivinationC2SPacket;
import top.aurora.lordofmysteries.network.RequestMysteryStatusC2SPacket;
import top.aurora.lordofmysteries.network.ToggleEmotionReadC2SPacket;
import top.aurora.lordofmysteries.network.UseMentalSuggestionC2SPacket;
import top.aurora.lordofmysteries.network.UseSurfaceReadC2SPacket;
import top.aurora.lordofmysteries.network.UseProvokeC2SPacket;
import top.aurora.lordofmysteries.network.UseEnrageC2SPacket;
import top.aurora.lordofmysteries.network.UseSeerAbilityC2SPacket;
import top.aurora.lordofmysteries.network.UseM2FoundationAbilityC2SPacket;
import top.aurora.lordofmysteries.ability.SeerAbilityHandler;
import top.aurora.lordofmysteries.ability.M2FoundationAbilityHandler;

/**
 * 客户端游戏总线事件（Forge 1.20.1）。
 *
 * <p>轮询 KeyMapping.consumeClick()，把按键翻译成网络包发送到服务端。
 * 不在客户端直接扣灵性/改状态——服务端才是权威。
 */
@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientForgeEvents {

    private ClientForgeEvents() {}

    /**
     * KeyMapping 的按键事件在 {@link InputEvent.Key} 上被抛出；
     * 使用 {@code consumeClick} 是 Forge 推荐的「按一次触发一次」写法，
     * 会自动处理按键长按只算一次的问题。
     */
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        while (PMKeyBindings.TOGGLE_SPIRIT_VISION.consumeClick()) {
            PMNetwork.CHANNEL.sendToServer(new ToggleSpiritVisionC2SPacket());
        }
        while (PMKeyBindings.USE_DIVINATION.consumeClick()) {
            PMNetwork.CHANNEL.sendToServer(new UseSimpleDivinationC2SPacket());
        }
        while (PMKeyBindings.OPEN_STATUS.consumeClick()) {
            PMNetwork.CHANNEL.sendToServer(new RequestMysteryStatusC2SPacket());
        }
        while (PMKeyBindings.TOGGLE_EMOTION_READ.consumeClick()) {
            PMNetwork.CHANNEL.sendToServer(new ToggleEmotionReadC2SPacket());
        }
        while (PMKeyBindings.USE_SURFACE_READ.consumeClick()) {
            PMNetwork.CHANNEL.sendToServer(new UseSurfaceReadC2SPacket());
        }
        while (PMKeyBindings.USE_MENTAL_SUGGESTION.consumeClick()) {
            PMNetwork.CHANNEL.sendToServer(new UseMentalSuggestionC2SPacket());
        }
        while (PMKeyBindings.USE_PROVOKE.consumeClick()) {
            PMNetwork.CHANNEL.sendToServer(new UseProvokeC2SPacket());
        }
        while (PMKeyBindings.USE_ENRAGE.consumeClick()) {
            PMNetwork.CHANNEL.sendToServer(new UseEnrageC2SPacket());
        }
        while (PMKeyBindings.USE_CARD_BLADE.consumeClick()) {
            sendSeer(SeerAbilityHandler.Ability.CARD_BLADE);
        }
        while (PMKeyBindings.USE_FLAME_LEAP.consumeClick()) {
            sendSeer(SeerAbilityHandler.Ability.FLAME_LEAP);
        }
        while (PMKeyBindings.ARM_PAPER_SUBSTITUTE.consumeClick()) {
            sendSeer(SeerAbilityHandler.Ability.PAPER_SUBSTITUTE);
        }
        while (PMKeyBindings.USE_AIR_BULLET.consumeClick()) {
            sendSeer(SeerAbilityHandler.Ability.AIR_BULLET);
        }
        while (PMKeyBindings.USE_STAGE_ILLUSION.consumeClick()) {
            sendSeer(SeerAbilityHandler.Ability.STAGE_ILLUSION);
        }
        while (PMKeyBindings.USE_M2_PRIMARY.consumeClick()) {
            sendM2(M2FoundationAbilityHandler.AbilitySlot.PRIMARY);
        }
        while (PMKeyBindings.USE_M2_SECONDARY.consumeClick()) {
            sendM2(M2FoundationAbilityHandler.AbilitySlot.SECONDARY);
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientMysteryState.clear();
    }

    private static void sendSeer(SeerAbilityHandler.Ability ability) {
        PMNetwork.CHANNEL.sendToServer(new UseSeerAbilityC2SPacket(ability));
    }

    private static void sendM2(M2FoundationAbilityHandler.AbilitySlot slot) {
        PMNetwork.CHANNEL.sendToServer(new UseM2FoundationAbilityC2SPacket(slot));
    }
}
