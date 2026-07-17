package top.aurora.lordofmysteries.commission;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import top.aurora.lordofmysteries.network.InvestigationBoardS2CPacket;
import top.aurora.lordofmysteries.network.PMNetwork;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.registry.ModBlocks;

public final class InvestigationBoardService {

    private static final int HORIZONTAL_RANGE = 6;
    private static final int VERTICAL_RANGE = 4;

    private InvestigationBoardService() {}

    public static int openFromBoard(ServerPlayer player) {
        sendView(player);
        return 1;
    }

    public static int openNearby(ServerPlayer player) {
        if (!isNearBoard(player)) {
            player.sendSystemMessage(Component.translatable(
                    "screen.lord_of_mysteries.investigation_board.nearby_required"));
            return 0;
        }
        sendView(player);
        return 1;
    }

    public static void refresh(ServerPlayer player) {
        if (isNearBoard(player)) sendView(player);
    }

    public static boolean isNearBoard(ServerPlayer player) {
        BlockPos center = player.blockPosition();
        for (BlockPos position : BlockPos.betweenClosed(
                center.offset(-HORIZONTAL_RANGE, -VERTICAL_RANGE, -HORIZONTAL_RANGE),
                center.offset(HORIZONTAL_RANGE, VERTICAL_RANGE, HORIZONTAL_RANGE))) {
            if (player.level().getBlockState(position).is(ModBlocks.COMMISSION_BOARD.get())) {
                return true;
            }
        }
        return false;
    }

    private static void sendView(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        InvestigationBoardView view = InvestigationBoardView.from(
                data, CommissionDefinitionManager.all(), player.level().getGameTime());
        PMNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new InvestigationBoardS2CPacket(view));
    }
}
