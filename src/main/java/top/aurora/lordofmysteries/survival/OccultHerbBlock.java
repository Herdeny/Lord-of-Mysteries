package top.aurora.lordofmysteries.survival;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class OccultHerbBlock extends BushBlock {
    private final Supplier<Item> harvest;
    private static final VoxelShape SHAPE = box(2, 0, 2, 14, 13, 14);

    public OccultHerbBlock(Properties properties, Supplier<Item> harvest) {
        super(properties);
        this.harvest = harvest;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos position, BlockState state) {
        return new ItemStack(harvest.get());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos position,
                               CollisionContext context) { return SHAPE; }
}
