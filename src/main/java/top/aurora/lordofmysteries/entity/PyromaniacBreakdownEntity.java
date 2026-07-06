package top.aurora.lordofmysteries.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;

public final class PyromaniacBreakdownEntity extends Zombie {

    public PyromaniacBreakdownEntity(
            EntityType<? extends PyromaniacBreakdownEntity> type,
            Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        super.tick();
        clearFire();
        if (!level().isClientSide()
                && tickCount % 8 == 0
                && level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.FLAME,
                    getX(), getY() + getBbHeight() * 0.5d, getZ(),
                    10, 0.35d, 0.5d, 0.35d, 0.03d);
        }
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity living) {
            living.setSecondsOnFire(5);
        }
        return hit;
    }

    @Override
    protected boolean isSunSensitive() {
        return false;
    }
}
