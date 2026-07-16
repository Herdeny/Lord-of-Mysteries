package top.aurora.lordofmysteries.network;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.network.NetworkEvent;

import top.aurora.lordofmysteries.ability.SpiritVisionHandler;

/**
 * 客户端 → 服务端：切换灵视开关。
 *
 * <p>本包无 payload——按下按键就意味着「切换」，不携带目标状态，避免客户端伪造。
 */
public record ToggleSpiritVisionC2SPacket() {

    public static void encode(ToggleSpiritVisionC2SPacket pkt, FriendlyByteBuf buf) {
        // 空 payload
    }

    public static ToggleSpiritVisionC2SPacket decode(FriendlyByteBuf buf) {
        return new ToggleSpiritVisionC2SPacket();
    }

    public static void handle(ToggleSpiritVisionC2SPacket pkt, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        // consumerMainThread 已经保证在主线程；不需要 enqueueWork。
        ServerPlayer sender = ctx.getSender();
        if (sender != null && PMNetwork.acceptC2S(
                sender, NetworkProtocol.TOGGLE_SPIRIT_VISION)) {
            SpiritVisionHandler.toggle(sender);
        }
        ctx.setPacketHandled(true);
    }
}
