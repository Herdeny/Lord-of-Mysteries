package top.aurora.lordofmysteries.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
    }

    @Override
    protected boolean isSunSensitive() {
        return false;
    }
}
