package top.aurora.lordofmysteries.commission;

import java.util.List;

import org.jetbrains.annotations.Nullable;

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

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.knowledge.KnowledgeText;
import top.aurora.lordofmysteries.player.MysteryCapability;

public final class CaseClueItem extends Item {

    private final ResourceLocation knowledgeId;
    private final String tooltipKey;

    public CaseClueItem(String knowledgePath, String tooltipKey, Properties properties) {
        super(properties.stacksTo(1));
        this.knowledgeId = ResourceLocation.fromNamespaceAndPath(
                ProjectMystery.MOD_ID, "knowledge/" + knowledgePath);
        this.tooltipKey = tooltipKey;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player,
                                                   InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer
                && MysteryCapability.get(serverPlayer).knownKnowledge.add(knowledgeId)) {
            serverPlayer.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.knowledge.discovered",
                    Component.translatable(KnowledgeText.translationKey(
                            knowledgeId.toString())))
                    .withStyle(ChatFormatting.GOLD));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(tooltipKey).withStyle(ChatFormatting.GRAY));
    }
}
