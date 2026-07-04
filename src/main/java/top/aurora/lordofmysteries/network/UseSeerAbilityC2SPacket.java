package top.aurora.lordofmysteries.network;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import top.aurora.lordofmysteries.ability.SeerAbilityHandler;

public record UseSeerAbilityC2SPacket(SeerAbilityHandler.Ability ability) {

    public static void encode(UseSeerAbilityC2SPacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.ability());
    }

    public static UseSeerAbilityC2SPacket decode(FriendlyByteBuf buffer) {
        return new UseSeerAbilityC2SPacket(
                buffer.readEnum(SeerAbilityHandler.Ability.class));
    }

    public static void handle(UseSeerAbilityC2SPacket packet,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer sender = context.getSender();
        if (sender != null) SeerAbilityHandler.use(sender, packet.ability());
        context.setPacketHandled(true);
    }
}
