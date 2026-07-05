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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import top.aurora.lordofmysteries.ability.SpiritualityCost;
import top.aurora.lordofmysteries.entity.ShapeshifterSerpentEntity;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerMysteryData;

public final class SpiritLanternItem extends Item {

    private static final float SPIRIT_COST = 3f;

    public SpiritLanternItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        PlayerMysteryData data = MysteryCapability.get(serverPlayer);
        boolean paidWithSpirit = data.isExtraordinary()
                && SpiritualityCost.tryConsume(data, SPIRIT_COST);
        if (!paidWithSpirit) {
            data.insanityPressure = Math.min(100f, data.insanityPressure + 2f);
        }

        List<Monster> revealed = level.getEntitiesOfClass(Monster.class,
                serverPlayer.getBoundingBox().inflate(16d),
                Monster::isAlive);
        for (Monster monster : revealed) {
            monster.addEffect(new MobEffectInstance(
                    MobEffects.GLOWING, 200, 0, false, false, true));
            if (monster instanceof ShapeshifterSerpentEntity serpent) {
                serpent.revealFor(200);
            }
        }

        stack.hurtAndBreak(1, serverPlayer, broken -> broken.broadcastBreakEvent(hand));
        serverPlayer.getCooldowns().addCooldown(this, 100);
        serverPlayer.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.lantern.revealed",
                revealed.size(),
                paidWithSpirit
                        ? Component.translatable("message.lord_of_mysteries.lantern.spirit_cost")
                        : Component.translatable("message.lord_of_mysteries.lantern.pressure_cost"))
                .withStyle(ChatFormatting.GOLD));
        ServerLevel serverLevel = serverPlayer.serverLevel();
        serverLevel.sendParticles(serverPlayer, ParticleTypes.SOUL_FIRE_FLAME, true,
                serverPlayer.getX(), serverPlayer.getY() + 1d, serverPlayer.getZ(),
                24, 1.2d, 0.7d, 1.2d, 0.02d);
        serverLevel.playSound(null, serverPlayer.blockPosition(),
                SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 0.8f, 1.1f);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip,
                                TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.lord_of_mysteries.spirit_lantern")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.lord_of_mysteries.spirit_lantern.cost")
                .withStyle(ChatFormatting.DARK_AQUA));
    }
}
