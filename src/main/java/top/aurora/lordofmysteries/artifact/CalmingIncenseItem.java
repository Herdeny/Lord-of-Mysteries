package top.aurora.lordofmysteries.artifact;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerMysteryData;

public final class CalmingIncenseItem extends Item {

    public CalmingIncenseItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        PlayerMysteryData data = MysteryCapability.get(serverPlayer);
        if (!OccultConsumableRules.canUseCalmingIncense(data.insanityPressure)) {
            serverPlayer.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.incense.not_needed"));
            return InteractionResultHolder.fail(stack);
        }

        OccultConsumableRules.Result result = OccultConsumableRules.applyCalmingIncense(
                data.insanityPressure, data.pollution);
        data.insanityPressure = result.pressure();
        data.pollution = result.pollution();
        if (!serverPlayer.getAbilities().instabuild) stack.shrink(1);
        serverPlayer.getCooldowns().addCooldown(this, 100);
        serverPlayer.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.incense.used",
                Math.round(result.pressure()), Math.round(result.pollution()))
                .withStyle(ChatFormatting.AQUA));
        ServerLevel serverLevel = serverPlayer.serverLevel();
        serverLevel.sendParticles(serverPlayer, ParticleTypes.CAMPFIRE_COSY_SMOKE, true,
                serverPlayer.getX(), serverPlayer.getY() + 0.8d, serverPlayer.getZ(),
                10, 0.4d, 0.5d, 0.4d, 0.01d);
        serverLevel.playSound(null, serverPlayer.blockPosition(),
                SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.55f, 1.35f);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip,
                                TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.lord_of_mysteries.calming_incense")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.lord_of_mysteries.calming_incense.cost")
                .withStyle(ChatFormatting.DARK_PURPLE));
    }
}
