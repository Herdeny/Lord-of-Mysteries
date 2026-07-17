package top.aurora.lordofmysteries.acting;

import java.util.List;

import org.jetbrains.annotations.Nullable;

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

import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerMysteryData;

public final class ActingReflectionJournalItem extends Item {

    private static final String ENTRIES = "reflection_entries";

    public ActingReflectionJournalItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.pass(stack);
        }
        ActingIdentityService.ReflectionResult result =
                ActingIdentityService.reflect(serverPlayer);
        if (result == ActingIdentityService.ReflectionResult.SUCCESS) {
            stack.getOrCreateTag().putInt(ENTRIES,
                    stack.getOrCreateTag().getInt(ENTRIES) + 1);
        }
        PlayerMysteryData data = MysteryCapability.get(player);
        String suffix = switch (result) {
            case SUCCESS -> "success";
            case COMMONER -> "commoner";
            case ALREADY_REFLECTED -> "cooldown";
        };
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.reflection_journal." + suffix,
                String.format(java.util.Locale.ROOT, "%.1f",
                        data.principleInsight),
                String.format(java.util.Locale.ROOT, "%.1f",
                        data.roleOveridentification))
                .withStyle(result == ActingIdentityService.ReflectionResult.SUCCESS
                        ? ChatFormatting.AQUA : ChatFormatting.GRAY));
        return result == ActingIdentityService.ReflectionResult.SUCCESS
                ? InteractionResultHolder.success(stack)
                : InteractionResultHolder.fail(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(
                "tooltip.lord_of_mysteries.reflection_journal",
                stack.getOrCreateTag().getInt(ENTRIES))
                .withStyle(ChatFormatting.GRAY));
    }
}
