package top.aurora.lordofmysteries.network;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import top.aurora.lordofmysteries.ability.HunterAbilityHandler;

public record UseEnrageC2SPacket() {

    public static void encode(UseEnrageC2SPacket packet, FriendlyByteBuf buffer) {}

    public static UseEnrageC2SPacket decode(FriendlyByteBuf buffer) {
        return new UseEnrageC2SPacket();
    }

    public static void handle(UseEnrageC2SPacket packet,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer sender = context.getSender();
        if (sender != null && PMNetwork.acceptC2S(
                sender, NetworkProtocol.USE_ENRAGE)) {
            HunterAbilityHandler.enrage(sender);
        }
        context.setPacketHandled(true);
    }
}
