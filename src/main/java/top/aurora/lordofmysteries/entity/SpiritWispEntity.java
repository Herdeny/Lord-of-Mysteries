package top.aurora.lordofmysteries.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.level.Level;

import top.aurora.lordofmysteries.ability.SpiritualityCost;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerMysteryData;

public final class SpiritWispEntity extends Vex {

    public SpiritWispEntity(EntityType<? extends SpiritWispEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel serverLevel) || tickCount % 80 != 0) return;

        for (ServerPlayer player : serverLevel.getEntitiesOfClass(
                ServerPlayer.class, getBoundingBox().inflate(6d),
                target -> target.isAlive() && !target.isCreative() && !target.isSpectator())) {
            PlayerMysteryData data = MysteryCapability.get(player);
            float drained = data.isExtraordinary()
                    ? SpiritualityCost.forceConsume(data, 2f) : 0f;
            if (drained < 2f) {
                data.insanityPressure = Math.min(100f, data.insanityPressure + 1f);
            }
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.spirit_wisp.drain")
                    .withStyle(ChatFormatting.DARK_AQUA));
        }

        serverLevel.sendParticles(ParticleTypes.SCULK_SOUL,
                getX(), getY() + 0.4d, getZ(),
                8, 0.35d, 0.35d, 0.35d, 0.02d);
    }
}
