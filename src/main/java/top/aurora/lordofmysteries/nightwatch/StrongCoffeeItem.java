package top.aurora.lordofmysteries.nightwatch;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public final class StrongCoffeeItem extends Item {
    public StrongCoffeeItem(Properties properties) {
        super(properties.stacksTo(16).craftRemainder(Items.GLASS_BOTTLE));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && (!(player instanceof ServerPlayer server)
                || !NightwatchService.canClearFatigue(server))) return InteractionResultHolder.fail(stack);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity consumer) {
        if (!(consumer instanceof ServerPlayer player) || !NightwatchService.clearFatigue(player)) return stack;
        if (player.getAbilities().instabuild) return stack;
        stack.shrink(1);
        ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
        if (stack.isEmpty()) return bottle;
        if (!player.getInventory().add(bottle)) player.drop(bottle, false);
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack) { return 32; }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.DRINK; }
}
