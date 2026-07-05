package top.aurora.lordofmysteries.knowledge;

import java.util.List;
import java.util.Optional;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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

import top.aurora.lordofmysteries.world.AbandonedCampGenerator;
import top.aurora.lordofmysteries.world.CampGenerationSavedData;

public final class InvestigatorCompassItem extends Item {

    public InvestigatorCompassItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) reportCamp(serverPlayer, stack);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    public static void reportCamp(ServerPlayer player, ItemStack stack) {
        ServerLevel level = player.getServer().getLevel(Level.OVERWORLD);
        if (level == null) return;
        Optional<BlockPos> generated =
                CampGenerationSavedData.get(level).nearestCamp(player.blockPosition());
        BlockPos target = generated.orElseGet(() ->
                AbandonedCampGenerator.starterCampTarget(level));
        double deltaX = target.getX() - player.getX();
        double deltaZ = target.getZ() - player.getZ();
        int distance = (int) Math.round(Math.sqrt(deltaX * deltaX + deltaZ * deltaZ));
        String direction = GuideDirection.fromDelta(deltaX, deltaZ);

        stack.getOrCreateTag().putInt("camp_x", target.getX());
        stack.getOrCreateTag().putInt("camp_z", target.getZ());
        player.sendSystemMessage(Component.translatable(
                generated.isPresent()
                        ? "message.lord_of_mysteries.compass.camp"
                        : "message.lord_of_mysteries.compass.starter",
                Component.translatable("direction.lord_of_mysteries." + direction),
                distance, target.getX(), target.getZ()).withStyle(ChatFormatting.GOLD));
        level.sendParticles(player, ParticleTypes.HAPPY_VILLAGER, true,
                player.getX(), player.getY() + 1.2d, player.getZ(),
                12, 0.45d, 0.5d, 0.45d, 0.05d);
        level.playSound(null, player.blockPosition(),
                SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS, 0.7f, 1.2f);
        player.getCooldowns().addCooldown(stack.getItem(), 20);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains("camp_x");
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip,
                                TooltipFlag flag) {
        tooltip.add(Component.translatable(
                "tooltip.lord_of_mysteries.investigator_compass")
                .withStyle(ChatFormatting.GRAY));
        if (stack.hasTag() && stack.getTag().contains("camp_x")) {
            tooltip.add(Component.translatable(
                    "tooltip.lord_of_mysteries.investigator_compass.bound",
                    stack.getTag().getInt("camp_x"), stack.getTag().getInt("camp_z"))
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
