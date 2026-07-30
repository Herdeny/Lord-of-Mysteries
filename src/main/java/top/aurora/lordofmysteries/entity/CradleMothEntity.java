package top.aurora.lordofmysteries.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.level.Level;

public final class CradleMothEntity extends Vex {

    public CradleMothEntity(
            EntityType<? extends CradleMothEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(
                    MobEffects.BLINDNESS, 80, 0));
            living.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN, 120, 1));
            if (level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.PORTAL,
                        living.getX(), living.getY() + 0.8d, living.getZ(),
                        18, 0.4d, 0.5d, 0.4d, 0.02d);
            }
        }
        return hit;
    }
}
