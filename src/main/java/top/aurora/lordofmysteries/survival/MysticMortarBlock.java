package top.aurora.lordofmysteries.survival;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.items.ItemHandlerHelper;
import top.aurora.lordofmysteries.registry.ModSurvival;

public final class MysticMortarBlock extends Block {
    private static final VoxelShape SHAPE = Shapes.or(box(2, 0, 2, 14, 3, 14),
            box(2, 3, 2, 4, 10, 14), box(12, 3, 2, 14, 10, 14),
            box(4, 3, 2, 12, 10, 4), box(4, 3, 12, 12, 10, 14));

    public MysticMortarBlock(Properties properties) { super(properties); }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos position,
                               CollisionContext context) { return SHAPE; }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos position, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!SurvivalInteraction.allowed(player, position) || !level.getBlockState(position).is(this)) {
            player.displayClientMessage(Component.translatable("message.lord_of_mysteries.survival.denied"), true);
            return InteractionResult.FAIL;
        }
        SimpleContainer input = new SimpleContainer(player.getMainHandItem().copy(), player.getOffhandItem().copy());
        var selected = level.getRecipeManager().getAllRecipesFor(ModSurvival.MORTAR_TYPE.get()).stream()
                .filter(recipe -> recipe.matches(input, level))
                .min(java.util.Comparator.comparing(recipe -> recipe.getId().toString()));
        if (selected.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.lord_of_mysteries.mortar.help"), false);
            return InteractionResult.CONSUME;
        }
        MortarRecipe recipe = selected.get();
        ItemStack output = recipe.assemble(input, level.registryAccess());
        if (!player.getAbilities().instabuild) {
            ItemStack mainRemainder = remainder(player.getMainHandItem(), recipe.mainCount());
            ItemStack reagentRemainder = remainder(player.getOffhandItem(), recipe.reagentCount());
            player.getMainHandItem().shrink(recipe.mainCount());
            player.getOffhandItem().shrink(recipe.reagentCount());
            ItemHandlerHelper.giveItemToPlayer(player, mainRemainder);
            ItemHandlerHelper.giveItemToPlayer(player, reagentRemainder);
        }
        ItemHandlerHelper.giveItemToPlayer(player, output);
        level.playSound(null, position, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 0.5f, 1.2f);
        return InteractionResult.CONSUME;
    }

    private static ItemStack remainder(ItemStack ingredient, int count) {
        ItemStack remainder = ingredient.getCraftingRemainingItem();
        if (!remainder.isEmpty()) remainder.setCount(count);
        return remainder;
    }
}
