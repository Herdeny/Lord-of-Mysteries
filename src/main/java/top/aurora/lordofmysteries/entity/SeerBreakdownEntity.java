package top.aurora.lordofmysteries.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;

public final class SeerBreakdownEntity extends Zombie {

    public SeerBreakdownEntity(EntityType<? extends SeerBreakdownEntity> type, Level level) {
        super(type, level);
        setCanBreakDoors(true);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) return;
        if (tickCount % 10 == 0 && level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    getX(), getY() + getBbHeight() * 0.6, getZ(),
                    8, 0.3, 0.6, 0.3, 0.02);
        }
        LivingEntity target = getTarget();
        if (target != null && tickCount % 100 == 0 && distanceToSqr(target) <= 256.0) {
            target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 80, 0, false, false));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0, false, false));
        }
        if (tickCount % 100 == 0) {
            level().getEntitiesOfClass(ServerPlayer.class, getBoundingBox().inflate(24d),
                    player -> player.isAlive()).forEach(player ->
                    player.sendSystemMessage(Component.translatable(
                            "message.lord_of_mysteries.breakdown.coordinate_leak",
                            player.getName(),
                            player.getBlockX(), player.getBlockY(), player.getBlockZ())));
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                && getRandom().nextFloat() < 0.2f) {
            if (level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.PORTAL,
                        getX(), getY() + 0.8d, getZ(),
                        18, 0.4d, 0.7d, 0.4d, 0.08d);
            }
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    protected boolean isSunSensitive() {
        return false;
    }
}
