package top.aurora.lordofmysteries.artifact;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class ManagedSealedArtifactItem extends Item {

    private final ManagedArtifactKind kind;

    public ManagedSealedArtifactItem(
            ManagedArtifactKind kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level, Player player, InteractionHand hand) {
        return SealedArtifactService.use(level, player, hand, kind);
    }

    @Override
    public InteractionResult interactLivingEntity(
            ItemStack stack, Player player,
            LivingEntity target, InteractionHand hand) {
        return SealedArtifactService.interact(
                stack, player, target, hand, kind);
    }

    @Override
    public void inventoryTick(
            ItemStack stack, Level level, Entity entity,
            int slot, boolean selected) {
        SealedArtifactService.observeInventory(
                stack, level, entity, kind);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag) {
        tooltip.add(Component.translatable(
                        "tooltip.lord_of_mysteries.artifact.managed")
                .withStyle(ChatFormatting.DARK_AQUA));
        tooltip.add(Component.translatable(
                        "tooltip.lord_of_mysteries.artifact."
                                + kind.path() + ".effect")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(
                        "tooltip.lord_of_mysteries.artifact."
                                + kind.path() + ".cost")
                .withStyle(ChatFormatting.RED));
        if (stack.hasTag() && stack.getTag().hasUUID(
                SealedArtifactService.INSTANCE_TAG)) {
            tooltip.add(Component.translatable(
                            "tooltip.lord_of_mysteries.artifact.custody_bound")
                    .withStyle(ChatFormatting.GOLD));
        }
        if (stack.hasTag() && stack.getTag().getBoolean(
                SealedArtifactService.QUARANTINED_TAG)) {
            tooltip.add(Component.translatable(
                            "tooltip.lord_of_mysteries.artifact.quarantined")
                    .withStyle(ChatFormatting.DARK_RED));
        }
    }
}
