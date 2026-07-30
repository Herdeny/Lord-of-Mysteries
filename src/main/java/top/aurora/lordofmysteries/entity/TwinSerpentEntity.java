package top.aurora.lordofmysteries.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.level.Level;

public final class TwinSerpentEntity extends CaveSpider {

    public TwinSerpentEntity(
            EntityType<? extends TwinSerpentEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
            living.setSecondsOnFire(3);
            if (level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.FLAME,
                        living.getX(), living.getY() + 0.6d, living.getZ(),
                        12, 0.3d, 0.35d, 0.3d, 0.02d);
            }
        }
        return hit;
    }
}
