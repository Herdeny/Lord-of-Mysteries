package top.aurora.lordofmysteries.network;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import top.aurora.lordofmysteries.ability.SpectatorAbilityHandler;

public record UseSurfaceReadC2SPacket() {

    public static void encode(UseSurfaceReadC2SPacket packet, FriendlyByteBuf buffer) {}

    public static UseSurfaceReadC2SPacket decode(FriendlyByteBuf buffer) {
        return new UseSurfaceReadC2SPacket();
    }

    public static void handle(UseSurfaceReadC2SPacket packet,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer sender = context.getSender();
        if (sender != null && PMNetwork.acceptC2S(
                sender, NetworkProtocol.USE_SURFACE_READ)) {
            SpectatorAbilityHandler.readSurface(sender);
        }
        context.setPacketHandled(true);
    }
}

