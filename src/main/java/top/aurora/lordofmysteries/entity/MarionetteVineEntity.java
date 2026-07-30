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

public final class MarionetteVineEntity extends CaveSpider {

    public MarionetteVineEntity(
            EntityType<? extends MarionetteVineEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
            living.addEffect(new MobEffectInstance(
                    MobEffects.WEAKNESS, 100, 0));
            if (level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.SPORE_BLOSSOM_AIR,
                        living.getX(), living.getY() + 0.8d, living.getZ(),
                        14, 0.35d, 0.45d, 0.35d, 0.01d);
            }
        }
        return hit;
    }
}
