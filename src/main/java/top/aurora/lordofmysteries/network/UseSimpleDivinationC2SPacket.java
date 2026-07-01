package top.aurora.lordofmysteries.network;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.network.NetworkEvent;

import top.aurora.lordofmysteries.ability.SimpleDivinationHandler;

/**
 * 客户端 → 服务端：使用简易占卜。
 *
 * <p>无 payload，结果通过聊天/HUD 反馈。M2 若引入界面式结果面板，可扩展 S2C 结果包。
 */
public record UseSimpleDivinationC2SPacket() {

    public static void encode(UseSimpleDivinationC2SPacket pkt, FriendlyByteBuf buf) {}

    public static UseSimpleDivinationC2SPacket decode(FriendlyByteBuf buf) {
        return new UseSimpleDivinationC2SPacket();
    }

    public static void handle(UseSimpleDivinationC2SPacket pkt, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ServerPlayer sender = ctx.getSender();
        if (sender != null) {
            SimpleDivinationHandler.cast(sender);
        }
        ctx.setPacketHandled(true);
    }
}
