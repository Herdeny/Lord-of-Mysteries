package top.aurora.lordofmysteries.knowledge;

import java.util.List;

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

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerMysteryData;

public final class FormulaFragmentItem extends Item {

    private static final List<ResourceLocation> RUMORS = List.of(
            knowledge("m2/pathway_thief_rumor"),
            knowledge("m2/pathway_apprentice_rumor"),
            knowledge("m2/commission_system"),
            knowledge("m2/mist_city"));

    public FormulaFragmentItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player,
                                                   InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        PlayerMysteryData data = MysteryCapability.get(serverPlayer);
        ResourceLocation discovered = RUMORS.stream()
                .filter(id -> !data.knownKnowledge.contains(id))
                .findFirst()
                .orElse(null);
        if (discovered == null) {
            serverPlayer.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.formula_fragment.exhausted")
                    .withStyle(ChatFormatting.GRAY));
            return InteractionResultHolder.fail(stack);
        }

        data.knownKnowledge.add(discovered);
        if (!serverPlayer.getAbilities().instabuild) stack.shrink(1);
        serverPlayer.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.formula_fragment.deciphered",
                Component.translatable(KnowledgeText.translationKey(discovered.toString())))
                .withStyle(ChatFormatting.GOLD));
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(
                "tooltip.lord_of_mysteries.formula_fragment")
                .withStyle(ChatFormatting.GRAY));
    }

    private static ResourceLocation knowledge(String path) {
        return ResourceLocation.fromNamespaceAndPath(
                ProjectMystery.MOD_ID, "knowledge/" + path);
    }
}
