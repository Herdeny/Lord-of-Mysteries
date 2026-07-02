package top.aurora.lordofmysteries.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import top.aurora.lordofmysteries.registry.ModBlockEntities;

public final class RitualAltarBlock extends BaseEntityBlock {

    public RitualAltarBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof RitualAltarBlockEntity altar)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        ItemStack held = player.getItemInHand(hand);
        if (!held.isEmpty() && altar.insert(held, player)) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.ritual.material_added"));
            return InteractionResult.CONSUME;
        }
        if (held.isEmpty() && altar.state() == RitualStateMachine.State.COMPLETE) {
            ItemStack artifact = altar.takeArtifact();
            if (!player.addItem(artifact)) player.drop(artifact, false);
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.ritual.sealed"));
            return InteractionResult.CONSUME;
        }
        if (held.isEmpty() && level instanceof ServerLevel serverLevel && altar.start(serverLevel)) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.ritual.started"));
            return InteractionResult.CONSUME;
        }
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.ritual.requirements"));
        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!oldState.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof RitualAltarBlockEntity altar) {
            for (ItemStack stack : altar.itemsForDrop()) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
            }
        }
        super.onRemove(oldState, level, pos, newState, moving);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RitualAltarBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, ModBlockEntities.RITUAL_ALTAR.get(),
                RitualAltarBlockEntity::serverTick);
    }
}
