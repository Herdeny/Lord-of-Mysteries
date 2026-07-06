package top.aurora.lordofmysteries.knowledge;

import java.util.List;
import java.util.UUID;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import top.aurora.lordofmysteries.acting.ActingEvent;
import top.aurora.lordofmysteries.acting.ActingEventHandler;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.potion.M2PathwayPotionItem;

public final class KnowledgeCopyItem extends Item {

    private static final String KNOWLEDGE_TAG = "KnowledgeId";
    private static final String AUTHOR_TAG = "Author";

    public KnowledgeCopyItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static ItemStack create(Item item, ResourceLocation knowledgeId,
                                   UUID author) {
        ItemStack stack = new ItemStack(item);
        stack.getOrCreateTag().putString(KNOWLEDGE_TAG, knowledgeId.toString());
        stack.getOrCreateTag().putUUID(AUTHOR_TAG, author);
        return stack;
    }

    @Nullable
    public static ResourceLocation knowledgeId(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains(KNOWLEDGE_TAG)) return null;
        return ResourceLocation.tryParse(stack.getTag().getString(KNOWLEDGE_TAG));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player,
                                                   InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        ResourceLocation knowledgeId = knowledgeId(stack);
        if (knowledgeId == null) {
            serverPlayer.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.knowledge_copy.invalid")
                    .withStyle(ChatFormatting.RED));
            return InteractionResultHolder.fail(stack);
        }

        PlayerMysteryData data = MysteryCapability.get(serverPlayer);
        if (!data.knownKnowledge.add(knowledgeId)) {
            serverPlayer.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.knowledge_copy.known")
                    .withStyle(ChatFormatting.GRAY));
            return InteractionResultHolder.fail(stack);
        }

        if (stack.getTag().hasUUID(AUTHOR_TAG)) {
            UUID authorId = stack.getTag().getUUID(AUTHOR_TAG);
            ServerPlayer author = serverPlayer.server.getPlayerList().getPlayer(authorId);
            if (author != null && !authorId.equals(serverPlayer.getUUID())) {
                PlayerMysteryData authorData = MysteryCapability.get(author);
                if (M2PathwayPotionItem.Pathway.APPRENTICE.id()
                        .equals(authorData.pathway)
                        && authorData.sequence == 9) {
                    ActingEventHandler.trigger(
                            author, ActingEvent.APPRENTICE9_TEACH, serverPlayer);
                }
            }
        }

        serverPlayer.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.knowledge_copy.learned",
                Component.translatable(
                        KnowledgeText.translationKey(knowledgeId.toString())))
                .withStyle(ChatFormatting.GOLD));
        if (!serverPlayer.getAbilities().instabuild) stack.shrink(1);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        ResourceLocation id = knowledgeId(stack);
        if (id == null) {
            tooltip.add(Component.translatable(
                    "tooltip.lord_of_mysteries.knowledge_copy.empty")
                    .withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        tooltip.add(Component.translatable(
                "tooltip.lord_of_mysteries.knowledge_copy",
                Component.translatable(KnowledgeText.translationKey(id.toString())))
                .withStyle(ChatFormatting.GRAY));
    }
}
