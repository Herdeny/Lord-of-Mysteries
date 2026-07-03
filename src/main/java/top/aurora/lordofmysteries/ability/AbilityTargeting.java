package top.aurora.lordofmysteries.ability;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public final class AbilityTargeting {

    private AbilityTargeting() {}

    public static LivingEntity findLookTarget(ServerPlayer player, double range) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.scale(range));
        AABB search = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(
                player,
                start,
                end,
                search,
                entity -> entity instanceof LivingEntity living
                        && living.isAlive()
                        && living.isPickable()
                        && entity != player,
                range * range);
        return hit != null && hit.getEntity() instanceof LivingEntity living ? living : null;
    }
}

