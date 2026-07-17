package top.aurora.lordofmysteries.commission;

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

public final class SealedFormulaDossierItem extends Item {

    public SealedFormulaDossierItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player,
                                                   InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return FormulaAppraisalService.inspect(serverPlayer, stack) > 0
                ? InteractionResultHolder.success(stack)
                : InteractionResultHolder.fail(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(FormulaAppraisalService.isAppraised(stack)
                        ? "tooltip.lord_of_mysteries.sealed_formula_dossier.appraised"
                        : "tooltip.lord_of_mysteries.sealed_formula_dossier.sealed")
                .withStyle(ChatFormatting.GRAY));
    }
}
