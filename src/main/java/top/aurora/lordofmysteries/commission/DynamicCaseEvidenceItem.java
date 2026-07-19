package top.aurora.lordofmysteries.commission;

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

import top.aurora.lordofmysteries.registry.ModItems;

public final class DynamicCaseEvidenceItem extends Item {

    private final DynamicCaseProfile.EvidenceTheme evidenceTheme;

    public DynamicCaseEvidenceItem(
            DynamicCaseProfile.EvidenceTheme evidenceTheme,
            Properties properties) {
        super(properties.stacksTo(1));
        this.evidenceTheme = evidenceTheme;
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
        if (!DynamicCaseEvidenceData.isBound(stack.getTag())) {
            tooltip.add(Component.translatable(
                            "tooltip.lord_of_mysteries.dynamic_case_evidence.unbound")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }
        tooltip.add(Component.translatable(
                        "tooltip.lord_of_mysteries.dynamic_case_evidence.case",
                        DynamicCaseEvidenceData.instanceId(stack.getTag()))
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable(
                        "tooltip.lord_of_mysteries.dynamic_case_evidence.theme",
                        Component.translatable(evidenceTheme.translationKey(
                                "evidence_theme")))
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable(
                        "tooltip.lord_of_mysteries.dynamic_case_evidence.sealed")
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return DynamicCaseEvidenceData.isBound(stack.getTag())
                || super.isFoil(stack);
    }

    public static ItemStack create(DynamicCaseProfile profile) {
        ItemStack stack = new ItemStack(itemFor(profile.evidenceTheme()));
        DynamicCaseEvidenceData.write(stack.getOrCreateTag(), profile);
        return stack;
    }

    public static boolean matches(
            ItemStack stack, DynamicCaseProfile profile) {
        return stack.getItem() instanceof DynamicCaseEvidenceItem item
                && item.evidenceTheme == profile.evidenceTheme()
                && DynamicCaseEvidenceData.matches(stack.getTag(), profile);
    }

    private static Item itemFor(DynamicCaseProfile.EvidenceTheme theme) {
        return switch (theme) {
            case BRASS_TOKEN -> ModItems.DYNAMIC_CASE_BRASS_TOKEN.get();
            case INK_TRACE -> ModItems.DYNAMIC_CASE_INK_TRACE.get();
            case CANDLE_WAX -> ModItems.DYNAMIC_CASE_CANDLE_WAX.get();
            case TORN_SCHEDULE -> ModItems.DYNAMIC_CASE_TORN_SCHEDULE.get();
        };
    }
}
