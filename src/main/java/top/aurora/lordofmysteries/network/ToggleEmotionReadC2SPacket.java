package top.aurora.lordofmysteries.network;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import top.aurora.lordofmysteries.ability.SpectatorAbilityHandler;

public record ToggleEmotionReadC2SPacket() {

    public static void encode(ToggleEmotionReadC2SPacket packet, FriendlyByteBuf buffer) {}

    public static ToggleEmotionReadC2SPacket decode(FriendlyByteBuf buffer) {
        return new ToggleEmotionReadC2SPacket();
    }

    public static void handle(ToggleEmotionReadC2SPacket packet,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer sender = context.getSender();
        if (sender != null && PMNetwork.acceptC2S(
                sender, NetworkProtocol.TOGGLE_EMOTION_READ)) {
            SpectatorAbilityHandler.toggleEmotionRead(sender);
        }
        context.setPacketHandled(true);
    }
}

