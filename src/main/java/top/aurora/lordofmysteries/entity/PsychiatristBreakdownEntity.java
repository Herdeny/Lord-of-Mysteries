package top.aurora.lordofmysteries.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;

public final class PsychiatristBreakdownEntity extends Zombie {

    public PsychiatristBreakdownEntity(
            EntityType<? extends PsychiatristBreakdownEntity> type,
            Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) return;
        if (tickCount % 10 == 0 && level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.WITCH,
                    getX(), getY() + getBbHeight() * 0.65d, getZ(),
                    12, 0.4d, 0.7d, 0.4d, 0.02d);
        }
        LivingEntity target = getTarget();
        if (target != null && tickCount % 80 == 0
                && distanceToSqr(target) <= 100d) {
            target.addEffect(new MobEffectInstance(
                    MobEffects.CONFUSION, 120, 0, false, false));
            target.addEffect(new MobEffectInstance(
                    MobEffects.WEAKNESS, 120, 1, false, false));
        }
    }

    @Override
    protected boolean isSunSensitive() {
        return false;
    }
}
