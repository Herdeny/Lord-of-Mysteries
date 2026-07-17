package top.aurora.lordofmysteries.acting;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.registry.ModItems;

public final class IdentityKitService {

    private IdentityKitService() {}

    public static void onPotionAdvancement(ServerPlayer player,
                                           PlayerMysteryData data,
                                           boolean firstPotion) {
        ActingIdentityService.recordAdvancement(data);
        if (!firstPotion) return;
        boolean cardGranted = giveIfMissing(
                player, ModItems.ACTING_IDENTITY_CARD.get());
        boolean journalGranted = giveIfMissing(
                player, ModItems.ACTING_REFLECTION_JOURNAL.get());
        if (cardGranted || journalGranted) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.identity_kit.granted"));
        }
    }

    private static boolean giveIfMissing(ServerPlayer player, Item item) {
        boolean present = player.getInventory().items.stream()
                .anyMatch(stack -> stack.is(item));
        if (present) return false;
        ItemStack stack = new ItemStack(item);
        if (!player.getInventory().add(stack)) player.drop(stack, false);
        return true;
    }
}
