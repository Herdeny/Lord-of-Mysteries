package top.aurora.lordofmysteries.network;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import top.aurora.lordofmysteries.ability.M2FoundationAbilityHandler;

public record UseM2FoundationAbilityC2SPacket(
        M2FoundationAbilityHandler.AbilitySlot slot) {

    public static void encode(UseM2FoundationAbilityC2SPacket packet,
                              FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.slot());
    }

    public static UseM2FoundationAbilityC2SPacket decode(FriendlyByteBuf buffer) {
        return new UseM2FoundationAbilityC2SPacket(
                buffer.readEnum(M2FoundationAbilityHandler.AbilitySlot.class));
    }

    public static void handle(UseM2FoundationAbilityC2SPacket packet,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer sender = context.getSender();
        if (sender != null && PMNetwork.acceptC2S(
                sender, NetworkProtocol.USE_M2_FOUNDATION_ABILITY)) {
            M2FoundationAbilityHandler.use(sender, packet.slot());
        }
        context.setPacketHandled(true);
    }
}
