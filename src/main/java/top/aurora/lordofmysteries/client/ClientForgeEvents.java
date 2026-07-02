package top.aurora.lordofmysteries.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.network.PMNetwork;
import top.aurora.lordofmysteries.network.ToggleSpiritVisionC2SPacket;
import top.aurora.lordofmysteries.network.UseSimpleDivinationC2SPacket;
import top.aurora.lordofmysteries.network.RequestMysteryStatusC2SPacket;

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
    }
}
