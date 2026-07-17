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

import top.aurora.lordofmysteries.knowledge.KnowledgeText;
import top.aurora.lordofmysteries.knowledge.M1TrialTracker;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerMysteryData;

public final class ActingIdentityCardItem extends Item {

    private static final String BOUND = "identity_bound";
    private static final String PATHWAY = "identity_pathway";
    private static final String SEQUENCE = "identity_sequence";
    private static final String PROFILE = "identity_profile";
    private static final int PROFILE_COUNT = 4;

    public ActingIdentityCardItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }
        PlayerMysteryData data = MysteryCapability.get(player);
        if (!data.isExtraordinary()) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.identity_card.commoner")
                    .withStyle(ChatFormatting.GRAY));
            return InteractionResultHolder.fail(stack);
        }
        if (player.isShiftKeyDown()) {
            int nextProfile = (stack.getOrCreateTag().getInt(PROFILE) + 1)
                    % PROFILE_COUNT;
            stack.getOrCreateTag().putInt(PROFILE, nextProfile);
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.identity_card.profile",
                    Component.translatable(profileKey(nextProfile)))
                    .withStyle(ChatFormatting.GOLD));
            return InteractionResultHolder.success(stack);
        }

        stack.getOrCreateTag().putBoolean(BOUND, true);
        stack.getOrCreateTag().putString(PATHWAY, data.pathway.toString());
        stack.getOrCreateTag().putInt(SEQUENCE, data.sequence);
        if (player instanceof ServerPlayer serverPlayer) {
            M1TrialTracker.recordIdentityAnchor(serverPlayer);
        }
        showCard(player, stack, data);
        return InteractionResultHolder.success(stack);
    }

    private static void showCard(Player player, ItemStack stack,
                                 PlayerMysteryData data) {
        int profile = Math.floorMod(stack.getOrCreateTag().getInt(PROFILE),
                PROFILE_COUNT);
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.identity_card.bound",
                Component.translatable(KnowledgeText.pathwayTranslationKey(
                        data.pathway.toString())),
                data.sequence,
                Component.translatable(profileKey(profile)))
                .withStyle(ChatFormatting.AQUA));
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.identity_card.promise." + profile)
                .withStyle(ChatFormatting.DARK_AQUA));
    }

    private static String profileKey(int profile) {
        return "identity.lord_of_mysteries.profile." + profile;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        if (stack.getOrCreateTag().getBoolean(BOUND)) {
            tooltip.add(Component.translatable(
                    "tooltip.lord_of_mysteries.identity_card.bound",
                    stack.getOrCreateTag().getInt(SEQUENCE))
                    .withStyle(ChatFormatting.AQUA));
        } else {
            tooltip.add(Component.translatable(
                    "tooltip.lord_of_mysteries.identity_card.unbound")
                    .withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.translatable(
                "tooltip.lord_of_mysteries.identity_card.profile_hint")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
