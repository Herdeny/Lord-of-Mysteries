package top.aurora.lordofmysteries.knowledge;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.potion.SeerPotionItem;

public final class InvestigatorNotesItem extends Item {

    public InvestigatorNotesItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        PlayerMysteryData data = MysteryCapability.get(serverPlayer);
        serverPlayer.sendSystemMessage(Component.translatable(
                "guide.lord_of_mysteries.title").withStyle(ChatFormatting.LIGHT_PURPLE));
        if (!data.isExtraordinary()) {
            send(serverPlayer, "guide.lord_of_mysteries.commoner.1");
            send(serverPlayer, "guide.lord_of_mysteries.commoner.2");
            send(serverPlayer, "guide.lord_of_mysteries.commoner.3");
        } else if (SeerPotionItem.SEER_PATHWAY.equals(data.pathway)) {
            send(serverPlayer, "guide.lord_of_mysteries.seer." + data.sequence);
            send(serverPlayer, "guide.lord_of_mysteries.seer.controls");
            if (data.digestion >= 100f && data.sequence > 7) {
                send(serverPlayer, "guide.lord_of_mysteries.seer.ready",
                        data.sequence - 1);
            } else if (data.sequence == 7) {
                send(serverPlayer, "guide.lord_of_mysteries.seer.m1");
            } else {
                send(serverPlayer, "guide.lord_of_mysteries.digestion",
                        Math.round(data.digestion));
            }
        } else {
            send(serverPlayer, "guide.lord_of_mysteries.other_pathway");
        }
        send(serverPlayer, "guide.lord_of_mysteries.status_key");
        return InteractionResultHolder.success(stack);
    }

    private static void send(ServerPlayer player, String key, Object... args) {
        player.sendSystemMessage(Component.translatable(key, args)
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip,
                                TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.lord_of_mysteries.investigator_notes")
                .withStyle(ChatFormatting.GRAY));
    }
}
