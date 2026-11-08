package top.aurora.lordofmysteries.survival;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.items.ItemHandlerHelper;

public final class OccultCropBlock extends CropBlock {
    private final Supplier<Item> harvest;

    public OccultCropBlock(Properties properties, Supplier<Item> harvest) {
        super(properties);
        this.harvest = harvest;
    }

    @Override
    protected ItemLike getBaseSeedId() { return harvest.get(); }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos position,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND || !isMaxAge(state)) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!SurvivalInteraction.allowed(player, position)
                || !level.getBlockState(position).equals(state)) return InteractionResult.FAIL;
        if (!level.setBlock(position, getStateForAge(0), 3)) return InteractionResult.FAIL;
        if (!player.getAbilities().instabuild) {
            ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(harvest.get(), 2));
        }
        level.playSound(null, position, SoundEvents.CROP_BREAK, SoundSource.BLOCKS, 0.6f, 1.1f);
        return InteractionResult.CONSUME;
    }
}
