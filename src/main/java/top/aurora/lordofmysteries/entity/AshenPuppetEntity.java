package top.aurora.lordofmysteries.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.level.Level;

public final class AshenPuppetEntity extends Husk {

    public AshenPuppetEntity(EntityType<? extends AshenPuppetEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0));
            if (level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.ASH,
                        getX(), getY() + 1d, getZ(),
                        12, 0.4d, 0.6d, 0.4d, 0.02d);
            }
        }
        return hit;
    }
}
