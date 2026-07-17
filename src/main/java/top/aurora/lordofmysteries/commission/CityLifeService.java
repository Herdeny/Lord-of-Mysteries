package top.aurora.lordofmysteries.commission;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import top.aurora.lordofmysteries.knowledge.M1TrialTracker;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.registry.ModItems;

public final class CityLifeService {

    public static final int PAPER_COST = 3;
    public static final long SHIFT_REWARD_PENCE = 24L;

    private CityLifeService() {}

    public static boolean tryWorkPressShift(ServerPlayer player) {
        if (!player.isShiftKeyDown()) return false;
        PlayerMysteryData data = MysteryCapability.get(player);
        long day = player.level().getDayTime() / 24000L;
        if (data.lastCityWorkDay == day) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.city_life.already_worked")
                    .withStyle(ChatFormatting.GRAY));
            return true;
        }
        if (player.getInventory().countItem(Items.PAPER) < PAPER_COST) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.city_life.need_paper",
                    PAPER_COST).withStyle(ChatFormatting.YELLOW));
            return true;
        }
        consumePaper(player, PAPER_COST);
        data.lastCityWorkDay = day;
        data.cityWorkShifts++;
        data.moneyPence += SHIFT_REWARD_PENCE;
        ItemStack meal = new ItemStack(Items.BREAD, 2);
        if (!player.getInventory().add(meal)) player.drop(meal, false);
        ItemStack newspaper = new ItemStack(ModItems.NEWSPAPER.get());
        if (!player.getInventory().add(newspaper)) player.drop(newspaper, false);
        M1TrialTracker.recordStreetLife(player);
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.city_life.shift_complete",
                CommissionCurrency.format(SHIFT_REWARD_PENCE),
                CommissionCurrency.format(data.moneyPence))
                .withStyle(ChatFormatting.GOLD));
        return true;
    }

    public static int showStatus(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        long day = player.level().getDayTime() / 24000L;
        player.sendSystemMessage(Component.translatable(
                "command.lord_of_mysteries.life.status",
                CommissionCurrency.format(data.moneyPence),
                data.cityWorkShifts,
                data.lastCityWorkDay == day
                        ? Component.translatable(
                                "command.lord_of_mysteries.life.done")
                        : Component.translatable(
                                "command.lord_of_mysteries.life.available"))
                .withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.translatable(
                "command.lord_of_mysteries.life.hint", PAPER_COST)
                .withStyle(ChatFormatting.GRAY));
        return 1;
    }

    private static void consumePaper(ServerPlayer player, int amount) {
        int remaining = amount;
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.is(Items.PAPER)) continue;
            int consumed = Math.min(remaining, stack.getCount());
            stack.shrink(consumed);
            remaining -= consumed;
            if (remaining == 0) break;
        }
        player.containerMenu.broadcastChanges();
    }
}
