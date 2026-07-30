package top.aurora.lordofmysteries.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class RiftlingEntity extends Endermite {

    public RiftlingEntity(
            EntityType<? extends RiftlingEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !level().isClientSide() && isAlive()
                && getRandom().nextFloat() < 0.4f) {
            phaseStep();
        }
        return hurt;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 40, 0));
        }
        return hit;
    }

    private void phaseStep() {
        Vec3 origin = position();
        for (int attempt = 0; attempt < 8; attempt++) {
            double x = getX() + (getRandom().nextDouble() - 0.5d) * 12d;
            double y = getY() + getRandom().nextInt(5) - 2;
            double z = getZ() + (getRandom().nextDouble() - 0.5d) * 12d;
            Vec3 destination = new Vec3(x, y, z);
            Vec3 movement = destination.subtract(position());
            if (!level().noCollision(this, getBoundingBox().move(movement))) {
                continue;
            }
            teleportTo(x, y, z);
            if (level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.PORTAL,
                        origin.x, origin.y + 0.3d, origin.z,
                        18, 0.35d, 0.35d, 0.35d, 0.04d);
                serverLevel.sendParticles(
                        ParticleTypes.REVERSE_PORTAL,
                        x, y + 0.3d, z,
                        18, 0.35d, 0.35d, 0.35d, 0.04d);
            }
            return;
        }
    }
}
