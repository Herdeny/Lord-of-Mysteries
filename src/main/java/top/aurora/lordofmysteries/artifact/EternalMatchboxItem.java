package top.aurora.lordofmysteries.artifact;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.entity.SeerBreakdownEntity;
import top.aurora.lordofmysteries.entity.SpiritWispEntity;
import top.aurora.lordofmysteries.entity.ThiefBreakdownEntity;
import top.aurora.lordofmysteries.entity.ApprenticeBreakdownEntity;
import top.aurora.lordofmysteries.entity.PsychiatristBreakdownEntity;
import top.aurora.lordofmysteries.entity.PyromaniacBreakdownEntity;

public final class EternalMatchboxItem extends Item {

    public static final float PRESSURE_PER_USE = 15f;

    public EternalMatchboxItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        if (isSealed(context.getItemInHand())) {
            if (!level.isClientSide()) {
                player.sendSystemMessage(Component.translatable(
                        "message.lord_of_mysteries.artifact.sealed"));
            }
            return InteractionResult.FAIL;
        }
        BlockPos firePos = context.getClickedPos().relative(context.getClickedFace());
        if (!level.isEmptyBlock(firePos)
                || !Blocks.SOUL_FIRE.defaultBlockState().canSurvive(level, firePos)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            level.setBlock(firePos, Blocks.SOUL_FIRE.defaultBlockState(), 11);
            payCost(player);
            context.getItemInHand().hurtAndBreak(1, player,
                    broken -> broken.broadcastBreakEvent(context.getHand()));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player,
                                                   LivingEntity target, InteractionHand hand) {
        if (isSealed(stack)) {
            if (!player.level().isClientSide()) {
                player.sendSystemMessage(Component.translatable(
                        "message.lord_of_mysteries.artifact.sealed"));
            }
            return InteractionResult.FAIL;
        }
        if (!player.level().isClientSide()) {
            float damage = target instanceof SeerBreakdownEntity
                    || target instanceof ThiefBreakdownEntity
                    || target instanceof ApprenticeBreakdownEntity
                    || target instanceof PsychiatristBreakdownEntity
                    || target instanceof PyromaniacBreakdownEntity
                    || target instanceof SpiritWispEntity ? 12f : 6f;
            target.hurt(player.damageSources().magic(), damage);
            target.setSecondsOnFire(5);
            payCost(player);
            stack.hurtAndBreak(1, player, broken -> broken.broadcastBreakEvent(hand));
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide());
    }

    private void payCost(Player player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        data.insanityPressure = Math.min(100f, data.insanityPressure + PRESSURE_PER_USE);
        player.getCooldowns().addCooldown(this, 100);
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.artifact.matchbox_cost")
                .withStyle(ChatFormatting.DARK_RED));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip,
                                TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.lord_of_mysteries.artifact.danger", 5)
                .withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("tooltip.lord_of_mysteries.artifact.matchbox")
                .withStyle(ChatFormatting.GRAY));
        if (isSealed(stack)) {
            tooltip.add(Component.translatable("tooltip.lord_of_mysteries.artifact.sealed")
                    .withStyle(ChatFormatting.AQUA));
        }
    }

    private static boolean isSealed(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean("sealed");
    }
}
