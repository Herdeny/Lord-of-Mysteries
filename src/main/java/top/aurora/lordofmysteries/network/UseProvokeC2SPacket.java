package top.aurora.lordofmysteries.network;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import top.aurora.lordofmysteries.ability.HunterAbilityHandler;

public record UseProvokeC2SPacket() {

    public static void encode(UseProvokeC2SPacket packet, FriendlyByteBuf buffer) {}

    public static UseProvokeC2SPacket decode(FriendlyByteBuf buffer) {
        return new UseProvokeC2SPacket();
    }

    public static void handle(UseProvokeC2SPacket packet,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer sender = context.getSender();
        if (sender != null) HunterAbilityHandler.provoke(sender);
        context.setPacketHandled(true);
    }
}
