package top.aurora.lordofmysteries.survival;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import top.aurora.lordofmysteries.player.OccultActivityService;

public final class SurvivalInteraction {
    private SurvivalInteraction() {}

    public static boolean allowed(Player player, BlockPos position) {
        return player instanceof ServerPlayer serverPlayer && player.isAlive()
                && !player.isSpectator() && player.mayBuild()
                && player.distanceToSqr(Vec3.atCenterOf(position)) <= 36d
                && player.level().mayInteract(player, position)
                && OccultActivityService.isAvailable(serverPlayer);
    }
}
