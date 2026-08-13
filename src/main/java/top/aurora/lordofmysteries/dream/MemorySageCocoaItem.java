package top.aurora.lordofmysteries.dream;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerDataSection;
import top.aurora.lordofmysteries.player.PlayerMysteryData;

public final class MemorySageCocoaItem extends Item {

    public MemorySageCocoaItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(
            ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide() && entity instanceof ServerPlayer player) {
            PlayerMysteryData data = MysteryCapability.get(player);
            data.insanityPressure = Math.max(0f, data.insanityPressure - 8f);
            data.mentalTraumaEndTick = Math.max(
                    level.getGameTime(), data.mentalTraumaEndTick - 1_200L);
            data.markDirty(PlayerDataSection.CORE);
        }
        return result;
    }
}
