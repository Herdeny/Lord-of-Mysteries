package top.aurora.lordofmysteries.commission;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerMysteryData;

public final class CityServiceDeskService {

    private CityServiceDeskService() {}

    public static int interactDetectiveClerk(ServerPlayer player) {
        if (player.isShiftKeyDown() && player.isSprinting()) {
            return CityLifeService.workFromServiceNpc(
                    player, CityEconomyPolicy.Job.AGENCY);
        }
        if (player.isShiftKeyDown()) return buyFieldKit(player);
        player.sendSystemMessage(Component.translatable(
                        "message.lord_of_mysteries.city_service.detective")
                .withStyle(ChatFormatting.GRAY));
        return InvestigationBoardService.openNearby(player);
    }

    public static int interactConstable(ServerPlayer player) {
        if (player.isShiftKeyDown() && player.isSprinting()) {
            return CityLifeService.workFromServiceNpc(
                    player, CityEconomyPolicy.Job.PATROL);
        }
        if (player.isShiftKeyDown()) return requestSafeRoom(player);
        PlayerMysteryData data = MysteryCapability.get(player);
        player.sendSystemMessage(Component.translatable(
                        "message.lord_of_mysteries.city_service.constable",
                        CommissionCurrency.format(data.moneyPence),
                        Math.round(data.insanityPressure),
                        Math.round(data.pollution))
                .withStyle(ChatFormatting.DARK_AQUA));
        if (!data.activeCommissionId.isBlank()) {
            CommissionService.showStatus(player);
        }
        return 1;
    }

    public static int showDirectory(ServerPlayer player) {
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.city.directory")
                .withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.city.press")
                .withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.city.detective",
                        CommissionCurrency.format(CityServiceDeskLogic.FIELD_KIT_COST))
                .withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.city.constabulary",
                        CommissionCurrency.format(CityServiceDeskLogic.SAFE_ROOM_COST))
                .withStyle(ChatFormatting.GRAY));
        return 1;
    }

    private static int buyFieldKit(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (CityServiceDeskLogic.purchase(
                data.moneyPence, CityServiceDeskLogic.FIELD_KIT_COST)
                != CityServiceDeskLogic.TransactionStatus.SUCCESS) {
            player.sendSystemMessage(Component.translatable(
                            "message.lord_of_mysteries.city_service.insufficient",
                            CommissionCurrency.format(
                                    CityServiceDeskLogic.FIELD_KIT_COST),
                            CommissionCurrency.format(data.moneyPence))
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        data.moneyPence -= CityServiceDeskLogic.FIELD_KIT_COST;
        giveItem(player, new ItemStack(Items.TORCH, 8));
        giveItem(player, new ItemStack(Items.PAPER, 4));
        giveItem(player, new ItemStack(Items.BREAD, 2));
        player.sendSystemMessage(Component.translatable(
                        "message.lord_of_mysteries.city_service.field_kit",
                        CommissionCurrency.format(
                                CityServiceDeskLogic.FIELD_KIT_COST),
                        CommissionCurrency.format(data.moneyPence))
                .withStyle(ChatFormatting.GOLD));
        return 1;
    }

    private static int requestSafeRoom(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        CityServiceDeskLogic.SafeRoomResult result =
                CityServiceDeskLogic.requestSafeRoom(
                        data.moneyPence, data.insanityPressure, data.pollution);
        if (result.status()
                == CityServiceDeskLogic.TransactionStatus.NOT_NEEDED) {
            player.sendSystemMessage(Component.translatable(
                            "message.lord_of_mysteries.city_service.safe_room_not_needed")
                    .withStyle(ChatFormatting.GRAY));
            return 0;
        }
        if (result.status()
                == CityServiceDeskLogic.TransactionStatus.INSUFFICIENT_FUNDS) {
            player.sendSystemMessage(Component.translatable(
                            "message.lord_of_mysteries.city_service.insufficient",
                            CommissionCurrency.format(
                                    CityServiceDeskLogic.SAFE_ROOM_COST),
                            CommissionCurrency.format(data.moneyPence))
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        data.moneyPence = result.balance();
        data.insanityPressure = result.pressure();
        data.pollution = result.pollution();
        player.sendSystemMessage(Component.translatable(
                        "message.lord_of_mysteries.city_service.safe_room",
                        Math.round(result.recoveredPressure()),
                        Math.round(result.recoveredPollution()),
                        CommissionCurrency.format(data.moneyPence))
                .withStyle(ChatFormatting.AQUA));
        return 1;
    }

    private static void giveItem(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) player.drop(stack, false);
        player.containerMenu.broadcastChanges();
    }
}
