package top.aurora.lordofmysteries.network;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import top.aurora.lordofmysteries.core.config.ServerConfig;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerMysteryData;

public record RequestMysteryStatusC2SPacket() {

    public static void encode(RequestMysteryStatusC2SPacket packet, FriendlyByteBuf buffer) {}

    public static RequestMysteryStatusC2SPacket decode(FriendlyByteBuf buffer) {
        return new RequestMysteryStatusC2SPacket();
    }

    public static void handle(RequestMysteryStatusC2SPacket packet,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer sender = context.getSender();
        if (sender != null && PMNetwork.acceptC2S(
                sender, NetworkProtocol.REQUEST_STATUS, 10L)) {
            PlayerMysteryData data = MysteryCapability.get(sender);
            PlayerMysteryStatusS2CPacket response = PlayerMysteryStatusS2CPacket.from(
                    data, ServerConfig.SHOW_EXACT_DIGESTION.get());
            PMNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sender), response);
        }
        context.setPacketHandled(true);
    }
}
