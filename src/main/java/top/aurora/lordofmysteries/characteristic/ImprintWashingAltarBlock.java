package top.aurora.lordofmysteries.characteristic;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import top.aurora.lordofmysteries.registry.ModItems;

public final class ImprintWashingAltarBlock extends Block {

    public ImprintWashingAltarBlock(Properties properties) {
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
        ItemStack characteristic = player.getMainHandItem();
        ItemStack incense = player.getOffhandItem();
        if (CharacteristicConservationService.readStack(
                characteristic).isEmpty()
                || !incense.is(ModItems.IMPRINT_WASHING_INCENSE.get())) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.characteristic.washing.help"));
            return InteractionResult.CONSUME;
        }
        CharacteristicProcessingService.StackResult result =
                CharacteristicProcessingService.cleanse(characteristic);
        if (!result.success()) {
            player.sendSystemMessage(
                    CharacteristicProcessingService.statusMessage(
                            result.status()));
            return InteractionResult.CONSUME;
        }
        player.setItemInHand(InteractionHand.MAIN_HAND, result.primary());
        if (!player.getAbilities().instabuild) incense.shrink(1);
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.characteristic.washing.success"));
        return InteractionResult.CONSUME;
    }
}
