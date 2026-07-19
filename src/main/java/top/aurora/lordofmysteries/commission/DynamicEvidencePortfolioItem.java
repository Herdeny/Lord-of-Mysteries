package top.aurora.lordofmysteries.commission;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public final class DynamicEvidencePortfolioItem extends Item {

    public DynamicEvidencePortfolioItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        if (player.level().isClientSide()) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }
        int result = DynamicCaseService.collectSceneEvidence(
                serverPlayer, context.getClickedPos(), context.getItemInHand());
        return result > 0 ? InteractionResult.CONSUME : InteractionResult.FAIL;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            DynamicCaseService.show(serverPlayer);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag) {
        if (!isBound(stack)) {
            tooltip.add(Component.translatable(
                            "tooltip.lord_of_mysteries.dynamic_evidence_portfolio.unbound")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }
        tooltip.add(Component.translatable(
                        "tooltip.lord_of_mysteries.dynamic_evidence_portfolio.case",
                        instanceId(stack))
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable(
                        "tooltip.lord_of_mysteries.dynamic_evidence_portfolio.stage",
                        Component.translatable(
                                "tooltip.lord_of_mysteries.dynamic_evidence_portfolio.stage."
                                        + stage(stack)))
                .withStyle(ChatFormatting.GRAY));
        String theme = DynamicEvidencePortfolioData.evidenceThemeId(stack.getTag());
        tooltip.add(Component.translatable(
                        "tooltip.lord_of_mysteries.dynamic_evidence_portfolio.theme",
                        Component.translatable(
                                "dynamic_case.lord_of_mysteries.evidence_theme."
                                        + theme))
                .withStyle(ChatFormatting.AQUA));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stage(stack) >= 3 || super.isFoil(stack);
    }

    public static void bind(
            ItemStack stack, DynamicCaseProfile profile, int collectedStage) {
        DynamicEvidencePortfolioData.write(
                stack.getOrCreateTag(), profile, collectedStage);
    }

    public static boolean matches(
            ItemStack stack, DynamicCaseProfile profile) {
        return stack.getItem() instanceof DynamicEvidencePortfolioItem
                && DynamicEvidencePortfolioData.matches(stack.getTag(), profile);
    }

    public static boolean isBound(ItemStack stack) {
        return DynamicEvidencePortfolioData.isBound(stack.getTag());
    }

    public static String instanceId(ItemStack stack) {
        return DynamicEvidencePortfolioData.instanceId(stack.getTag());
    }

    public static int stage(ItemStack stack) {
        return DynamicEvidencePortfolioData.stage(stack.getTag());
    }
}
