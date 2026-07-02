package top.aurora.lordofmysteries.potion;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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

public final class CrucibleBlock extends BaseEntityBlock {

    public CrucibleBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof CrucibleBlockEntity crucible)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        ItemStack held = player.getItemInHand(hand);
        if (crucible.canInsert(held) && crucible.insert(held, player)) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.crucible.ingredient_added", crucible.ingredientCount()));
            return InteractionResult.CONSUME;
        }
        if (held.isEmpty() && crucible.hasResult()) {
            ItemStack result = crucible.takeResult();
            if (!player.addItem(result)) player.drop(result, false);
            player.sendSystemMessage(Component.translatable("message.lord_of_mysteries.crucible.result"));
            return InteractionResult.CONSUME;
        }
        if (held.isEmpty() && crucible.startBrewing()) {
            player.sendSystemMessage(Component.translatable("message.lord_of_mysteries.crucible.started"));
            return InteractionResult.CONSUME;
        }
        if (crucible.isBrewing()) {
            int seconds = Math.max(0,
                    (CrucibleBlockEntity.BREWING_TIME - crucible.brewingTicks()) / 20);
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.crucible.progress",
                    seconds, Math.round(crucible.temperature())));
            return InteractionResult.CONSUME;
        }
        player.sendSystemMessage(Component.translatable("message.lord_of_mysteries.crucible.help"));
        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!oldState.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof CrucibleBlockEntity crucible) {
            for (ItemStack stack : crucible.itemsForDrop()) {
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
        return new CrucibleBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, ModBlockEntities.CRUCIBLE.get(), CrucibleBlockEntity::serverTick);
    }
}
