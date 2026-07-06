package top.aurora.lordofmysteries.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.Level;

public final class ApprenticeBreakdownEntity extends EnderMan {

    public ApprenticeBreakdownEntity(
            EntityType<? extends ApprenticeBreakdownEntity> type,
            Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) return;
        if (tickCount % 10 == 0 && level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    getX(), getY() + getBbHeight() * 0.6d, getZ(),
                    12, 0.45d, 0.8d, 0.45d, 0.05d);
        }
        LivingEntity target = getTarget();
        if (target != null && tickCount % 80 == 0
                && distanceToSqr(target) <= 144d) {
            target.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN, 80, 1, false, false));
            target.addEffect(new MobEffectInstance(
                    MobEffects.CONFUSION, 100, 0, false, false));
        }
    }
}
