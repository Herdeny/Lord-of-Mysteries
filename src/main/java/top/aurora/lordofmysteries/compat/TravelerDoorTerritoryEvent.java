package top.aurora.lordofmysteries.compat;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public final class TravelerDoorTerritoryEvent extends Event {

    private final ServerPlayer actor;
    private final UUID owner;
    private final ServerLevel level;
    private final BlockPos position;
    private final String doorName;
    private final Action action;

    public TravelerDoorTerritoryEvent(
            ServerPlayer actor,
            UUID owner,
            ServerLevel level,
            BlockPos position,
            String doorName,
            Action action) {
        this.actor = actor;
        this.owner = owner;
        this.level = level;
        this.position = position.immutable();
        this.doorName = doorName;
        this.action = action;
    }

    public ServerPlayer actor() {
        return actor;
    }

    public UUID owner() {
        return owner;
    }

    public ServerLevel level() {
        return level;
    }

    public BlockPos position() {
        return position;
    }

    public String doorName() {
        return doorName;
    }

    public Action action() {
        return action;
    }

    public enum Action {
        OPEN_SOURCE,
        OPEN_DESTINATION,
        TRANSIT_DESTINATION
    }
}
