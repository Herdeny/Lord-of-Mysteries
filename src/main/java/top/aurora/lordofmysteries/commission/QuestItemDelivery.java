package top.aurora.lordofmysteries.commission;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

final class QuestItemDelivery {

    private QuestItemDelivery() {
    }

    static boolean give(ServerPlayer player, ItemStack stack) {
        if (player.getInventory().add(stack)) {
            player.containerMenu.broadcastChanges();
            return true;
        }
        player.sendSystemMessage(Component.translatable(
                        "message.lord_of_mysteries.quest_item.inventory_full",
                        stack.getHoverName())
                .withStyle(ChatFormatting.RED));
        return false;
    }
}
