package top.aurora.lordofmysteries.network;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import top.aurora.lordofmysteries.commission.CommissionService;
import top.aurora.lordofmysteries.commission.InvestigationBoardService;

public record InvestigationBoardActionC2SPacket(Action action, String commissionId) {

    public enum Action {
        ACCEPT,
        ABANDON
    }

    public static void encode(InvestigationBoardActionC2SPacket packet,
                              FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.action());
        buffer.writeUtf(packet.commissionId());
    }

    public static InvestigationBoardActionC2SPacket decode(FriendlyByteBuf buffer) {
        return new InvestigationBoardActionC2SPacket(
                buffer.readEnum(Action.class), buffer.readUtf(256));
    }

    public static void handle(InvestigationBoardActionC2SPacket packet,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer sender = context.getSender();
        if (sender != null
                && PMNetwork.acceptC2S(sender, NetworkProtocol.INVESTIGATION_BOARD_ACTION, 10L)
                && InvestigationBoardService.isNearBoard(sender)) {
            if (packet.action() == Action.ACCEPT) {
                ResourceLocation commissionId = ResourceLocation.tryParse(packet.commissionId());
                if (commissionId != null) CommissionService.accept(sender, commissionId);
            } else if (packet.action() == Action.ABANDON) {
                CommissionService.abandon(sender);
            }
            InvestigationBoardService.refresh(sender);
        }
        context.setPacketHandled(true);
    }
}
