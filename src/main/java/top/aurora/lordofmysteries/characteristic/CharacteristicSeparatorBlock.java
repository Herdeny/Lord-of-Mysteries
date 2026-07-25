package top.aurora.lordofmysteries.characteristic;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import top.aurora.lordofmysteries.registry.ModItems;

public final class CharacteristicSeparatorBlock extends Block {

    public CharacteristicSeparatorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.CONSUME;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.CONSUME;
        }
        ItemStack primary = player.getMainHandItem();
        ItemStack catalyst = player.getOffhandItem();
        if (primary.is(ModItems.IDENTITY_SALT_CIRCLE.get())) {
            return CharacteristicLoadService.interact(
                    serverPlayer.serverLevel(),
                    pos,
                    serverPlayer,
                    player.isShiftKeyDown());
        }
        if (CharacteristicConservationService.readStack(primary).isEmpty()) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.characteristic.separator.help"));
            return InteractionResult.CONSUME;
        }
        if (CharacteristicConservationService.readStack(catalyst).isPresent()) {
            applyMerge(serverPlayer, primary, catalyst);
            return InteractionResult.CONSUME;
        }
        if (catalyst.is(ModItems.IMPRINT_PROBE.get())) {
            if (!player.isShiftKeyDown()) {
                CharacteristicProcessingService.sendInspection(
                        serverPlayer, primary);
                return InteractionResult.CONSUME;
            }
            if (CharacteristicProcessingService.isSealed(primary)) {
                applyUnseal(serverPlayer, primary, catalyst);
            } else {
                applySplit(serverPlayer, primary, catalyst);
            }
            return InteractionResult.CONSUME;
        }
        if (catalyst.is(ModItems.MEMORY_SEAL_WAX.get())) {
            applySeal(serverPlayer, primary, catalyst);
            return InteractionResult.CONSUME;
        }
        CharacteristicProcessingService.sendInspection(serverPlayer, primary);
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.characteristic.separator.help"));
        return InteractionResult.CONSUME;
    }

    private static void applyMerge(
            ServerPlayer player,
            ItemStack primary,
            ItemStack secondary) {
        CharacteristicProcessingService.StackResult result =
                CharacteristicProcessingService.merge(primary, secondary);
        if (!result.success()) {
            player.sendSystemMessage(
                    CharacteristicProcessingService.statusMessage(
                            result.status()));
            return;
        }
        player.setItemInHand(InteractionHand.MAIN_HAND, result.primary());
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.characteristic.separator.merged"));
    }

    private static void applySplit(
            ServerPlayer player,
            ItemStack primary,
            ItemStack probe) {
        CharacteristicProcessingService.StackResult result =
                CharacteristicProcessingService.split(primary);
        if (!result.success()) {
            player.sendSystemMessage(
                    CharacteristicProcessingService.statusMessage(
                            result.status()));
            return;
        }
        player.setItemInHand(InteractionHand.MAIN_HAND, result.primary());
        if (!player.addItem(result.secondary())) {
            player.drop(result.secondary(), false);
        }
        damageProbe(player, probe);
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.characteristic.separator.split"));
    }

    private static void applySeal(
            ServerPlayer player,
            ItemStack primary,
            ItemStack wax) {
        CharacteristicProcessingService.StackResult result =
                CharacteristicProcessingService.seal(primary);
        if (!result.success()) {
            player.sendSystemMessage(
                    CharacteristicProcessingService.statusMessage(
                            result.status()));
            return;
        }
        player.setItemInHand(InteractionHand.MAIN_HAND, result.primary());
        if (!player.getAbilities().instabuild) wax.shrink(1);
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.characteristic.separator.sealed"));
    }

    private static void applyUnseal(
            ServerPlayer player,
            ItemStack primary,
            ItemStack probe) {
        CharacteristicProcessingService.StackResult result =
                CharacteristicProcessingService.unseal(primary);
        if (!result.success()) {
            player.sendSystemMessage(
                    CharacteristicProcessingService.statusMessage(
                            result.status()));
            return;
        }
        player.setItemInHand(InteractionHand.MAIN_HAND, result.primary());
        damageProbe(player, probe);
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.characteristic.separator.unsealed"));
    }

    private static void damageProbe(
            ServerPlayer player, ItemStack probe) {
        probe.hurtAndBreak(1, player, current ->
                current.broadcastBreakEvent(InteractionHand.OFF_HAND));
    }
}
