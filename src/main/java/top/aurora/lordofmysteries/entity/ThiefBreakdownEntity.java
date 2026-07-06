package top.aurora.lordofmysteries.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;

public final class ThiefBreakdownEntity extends Zombie {

    public ThiefBreakdownEntity(EntityType<? extends ThiefBreakdownEntity> type,
                                Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) return;
        if (tickCount % 10 == 0 && level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SMOKE,
                    getX(), getY() + getBbHeight() * 0.5d, getZ(),
                    10, 0.35d, 0.45d, 0.35d, 0.01d);
        }
        if (tickCount % 40 == 0) {
            addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SPEED, 60, 1, false, false));
        }
        LivingEntity target = getTarget();
        if (target != null && tickCount % 100 == 0
                && distanceToSqr(target) <= 36d) {
            target.addEffect(new MobEffectInstance(
                    MobEffects.BLINDNESS, 60, 0, false, false));
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                && getRandom().nextFloat() < 0.15f) {
            if (level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SMOKE,
                        getX(), getY() + 0.8d, getZ(),
                        14, 0.4d, 0.6d, 0.4d, 0.05d);
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
