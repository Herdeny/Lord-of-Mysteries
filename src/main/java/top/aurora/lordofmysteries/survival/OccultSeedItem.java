package top.aurora.lordofmysteries.survival;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;

public final class OccultSeedItem extends ItemNameBlockItem {
    public OccultSeedItem(Block block, Properties properties) { super(block, properties); }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!context.getLevel().isClientSide()
                && (context.getPlayer() == null || !SurvivalInteraction.allowed(context.getPlayer(), context.getClickedPos()))) {
            return InteractionResult.FAIL;
        }
        return super.useOn(context);
    }
}
