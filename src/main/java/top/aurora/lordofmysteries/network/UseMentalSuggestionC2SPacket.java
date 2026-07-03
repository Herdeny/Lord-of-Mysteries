package top.aurora.lordofmysteries.network;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import top.aurora.lordofmysteries.ability.SpectatorAbilityHandler;

public record UseMentalSuggestionC2SPacket() {

    public static void encode(UseMentalSuggestionC2SPacket packet, FriendlyByteBuf buffer) {}

    public static UseMentalSuggestionC2SPacket decode(FriendlyByteBuf buffer) {
        return new UseMentalSuggestionC2SPacket();
    }

    public static void handle(UseMentalSuggestionC2SPacket packet,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer sender = context.getSender();
        if (sender != null) SpectatorAbilityHandler.castMentalSuggestion(sender);
        context.setPacketHandled(true);
    }
}

