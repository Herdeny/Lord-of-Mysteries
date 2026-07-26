package top.aurora.lordofmysteries.compat;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.common.MinecraftForge;

public final class TravelerDoorTerritoryService {

    private TravelerDoorTerritoryService() {}

    public static boolean allows(
            ServerPlayer actor,
            UUID owner,
            ServerLevel level,
            BlockPos position,
            String doorName,
            TravelerDoorTerritoryEvent.Action action) {
        if (actor == null || owner == null || level == null
                || position == null || action == null) {
            return false;
        }
        if (actor.getServer().isUnderSpawnProtection(
                level, position, actor)) {
            return false;
        }
        return !MinecraftForge.EVENT_BUS.post(
                new TravelerDoorTerritoryEvent(
                        actor,
                        owner,
                        level,
                        position,
                        doorName == null ? "" : doorName,
                        action));
    }
}
