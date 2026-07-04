package top.aurora.lordofmysteries.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class ShapeshifterSerpentEntity extends CaveSpider {

    private boolean revealed;

    public ShapeshifterSerpentEntity(
            EntityType<? extends ShapeshifterSerpentEntity> type, Level level) {
        super(type, level);
        setInvisible(true);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide() || tickCount % 10 != 0) return;

        Player nearby = level().getNearestPlayer(this, 7d);
        boolean shouldReveal = nearby != null && nearby.isAlive() && !nearby.isCreative();
        if (shouldReveal && !revealed) {
            revealed = true;
            setInvisible(false);
            setTarget(nearby);
            if (level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.POOF,
                        getX(), getY() + 0.4d, getZ(),
                        18, 0.35d, 0.25d, 0.35d, 0.03d);
            }
        } else if (!shouldReveal && getTarget() == null) {
            revealed = false;
            setInvisible(true);
        }
    }
}
