package top.aurora.lordofmysteries.entity;

import java.util.Comparator;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.level.Level;

import top.aurora.lordofmysteries.ability.SpiritualityCost;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerMysteryData;

public final class AbilityLeechEntity extends Silverfish {

    public AbilityLeechEntity(
            EntityType<? extends AbilityLeechEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (!hit || !(target instanceof ServerPlayer player)) return hit;

        PlayerMysteryData data = MysteryCapability.get(player);
        if (data.isExtraordinary()) {
            SpiritualityCost.forceConsume(data, 6f);
        }
        MobEffectInstance stolen = player.getActiveEffects().stream()
                .filter(effect -> effect.getEffect().isBeneficial())
                .max(Comparator.comparingInt(MobEffectInstance::getDuration))
                .orElse(null);
        if (stolen != null) {
            player.removeEffect(stolen.getEffect());
            addEffect(new MobEffectInstance(
                    stolen.getEffect(),
                    Math.min(400, stolen.getDuration()),
                    stolen.getAmplifier()));
        }
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.WITCH,
                    player.getX(), player.getY() + 0.8d, player.getZ(),
                    18, 0.35d, 0.5d, 0.35d, 0.02d);
        }
        return true;
    }
}
