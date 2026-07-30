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

public final class ScribeGolemEntity extends Husk {

    public ScribeGolemEntity(
            EntityType<? extends ScribeGolemEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(
                    MobEffects.DIG_SLOWDOWN, 160, 1));
            living.addEffect(new MobEffectInstance(
                    MobEffects.GLOWING, 160, 0));
            if (level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.ENCHANT,
                        living.getX(), living.getY() + 0.9d, living.getZ(),
                        20, 0.45d, 0.65d, 0.45d, 0.02d);
            }
        }
        return hit;
    }
}
