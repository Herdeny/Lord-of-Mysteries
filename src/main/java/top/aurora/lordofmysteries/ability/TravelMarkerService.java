package top.aurora.lordofmysteries.ability;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.compat.TravelerDoorTerritoryEvent;
import top.aurora.lordofmysteries.compat.TravelerDoorTerritoryService;
import top.aurora.lordofmysteries.entity.TravelerDoorEntity;
import top.aurora.lordofmysteries.player.PlayerFeedback;
import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.registry.ModEntities;

public final class TravelMarkerService {

    private static final String PASSENGER_COOLDOWN =
            ProjectMystery.MOD_ID + ":travel_relay_cooldown";
    static final String MARKER_NAME_TAG =
            "ProjectMysteryTravelMarkerName";
    private static final long LEADER_COOLDOWN_TICKS = 36_000L;
    private static final long PASSENGER_COOLDOWN_TICKS = 1_200L;
    private static final int[][] DESTINATION_OFFSETS = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1},
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1},
            {2, 0}, {-2, 0}, {0, 2}, {0, -2}, {0, 0}
    };

    private TravelMarkerService() {}

    public record Marker(ResourceKey<Level> dimension, BlockPos position) {}

    record MarkerData(ResourceLocation dimension, BlockPos position) {}

    public static boolean hasCompassInHands(ServerPlayer player) {
        return player.getMainHandItem().is(Items.COMPASS)
                || player.getOffhandItem().is(Items.COMPASS);
    }

    public static Optional<Marker> markerInHands(ServerPlayer player) {
        Optional<Marker> mainHand = readMarker(player.getMainHandItem());
        return mainHand.isPresent()
                ? mainHand
                : readMarker(player.getOffhandItem());
    }

    static String readMarkerName(CompoundTag tag) {
        return tag == null
                ? ""
                : TravelerDoorPolicy.normalizeName(
                        tag.getString(MARKER_NAME_TAG));
    }

    public static String markerNameInHands(ServerPlayer player) {
        ItemStack stack = markerStackInHands(player);
        return stack.isEmpty() || stack.getTag() == null
                ? "" : readMarkerName(stack.getTag());
    }

    public static Optional<Marker> readMarker(ItemStack stack) {
        if (!stack.is(Items.COMPASS) || !stack.hasTag()) {
            return Optional.empty();
        }
        CompoundTag tag = stack.getTag();
        return tag == null ? Optional.empty() : readMarkerTag(tag);
    }

    static Optional<Marker> readMarkerTag(CompoundTag tag) {
        return parseMarkerTag(tag).map(value -> new Marker(
                ResourceKey.create(Registries.DIMENSION, value.dimension()),
                value.position()));
    }

    static Optional<MarkerData> parseMarkerTag(CompoundTag tag) {
        if (!tag.contains("LodestonePos", Tag.TAG_COMPOUND)
                || !tag.contains("LodestoneDimension", Tag.TAG_STRING)) {
            return Optional.empty();
        }
        ResourceLocation dimensionId = ResourceLocation.tryParse(
                tag.getString("LodestoneDimension"));
        if (dimensionId == null) return Optional.empty();
        CompoundTag positionTag = tag.getCompound("LodestonePos");
        if (!positionTag.contains("X", Tag.TAG_INT)
                || !positionTag.contains("Y", Tag.TAG_INT)
                || !positionTag.contains("Z", Tag.TAG_INT)) {
            return Optional.empty();
        }
        BlockPos position = new BlockPos(
                positionTag.getInt("X"),
                positionTag.getInt("Y"),
                positionTag.getInt("Z"));
        return Optional.of(new MarkerData(dimensionId, position));
    }

    public static int sendGuide(ServerPlayer player) {
        Optional<Marker> marker = markerInHands(player);
        if (marker.isEmpty()) {
            PlayerFeedback.send(player, Component.translatable(
                    "message.lord_of_mysteries.travel.guide.empty")
                    .withStyle(ChatFormatting.GRAY));
            return 0;
        }
        Marker value = marker.get();
        ServerLevel destination = player.getServer().getLevel(
                value.dimension());
        boolean active = destination != null
                && activeMarker(destination, value.position());
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.travel.guide.marker",
                value.dimension().location().toString(),
                value.position().getX(),
                value.position().getY(),
                value.position().getZ(),
                Component.translatable(active
                        ? "message.lord_of_mysteries.travel.guide.active"
                        : "message.lord_of_mysteries.travel.guide.inactive"))
                .withStyle(active
                        ? ChatFormatting.AQUA
                        : ChatFormatting.RED));
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.travel.guide.name",
                doorLabel(markerNameInHands(player)))
                .withStyle(ChatFormatting.GRAY));
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.travel.guide.consent",
                M3TravelNetworkLogic.MAX_PASSENGERS)
                .withStyle(ChatFormatting.GRAY));
        PlayerMysteryData data =
                top.aurora.lordofmysteries.player.MysteryCapability.get(
                        player);
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.travel.guide.access",
                Component.translatable(
                        "message.lord_of_mysteries.travel.access."
                                + accessMode(data).id()),
                M3TravelNetworkLogic.DOOR_DURATION_TICKS / 20)
                .withStyle(ChatFormatting.GRAY));
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.travel.guide.blocked",
                data.travelerDoorBlacklist.size(),
                TravelerDoorPolicy.MAX_BLOCKED_PLAYERS)
                .withStyle(ChatFormatting.GRAY));
        return active ? 1 : 0;
    }

    public static int setMarkerName(
            ServerPlayer player, String requestedName) {
        ItemStack stack = markerStackInHands(player);
        if (stack.isEmpty()) {
            PlayerFeedback.send(player, Component.translatable(
                    "message.lord_of_mysteries.travel.marker_required"));
            return 0;
        }
        String name = TravelerDoorPolicy.normalizeName(requestedName);
        if (name.isEmpty()) {
            PlayerFeedback.send(player, Component.translatable(
                    "message.lord_of_mysteries.travel.name.invalid")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        stack.getOrCreateTag().putString(MARKER_NAME_TAG, name);
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.travel.name.updated",
                Component.literal(name))
                .withStyle(ChatFormatting.AQUA));
        return 1;
    }

    public static int clearMarkerName(ServerPlayer player) {
        ItemStack stack = markerStackInHands(player);
        if (stack.isEmpty()) {
            PlayerFeedback.send(player, Component.translatable(
                    "message.lord_of_mysteries.travel.marker_required"));
            return 0;
        }
        CompoundTag tag = stack.getTag();
        if (tag != null) tag.remove(MARKER_NAME_TAG);
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.travel.name.cleared")
                .withStyle(ChatFormatting.AQUA));
        return 1;
    }

    public static int setAccessMode(
            ServerPlayer player, String requestedMode) {
        TravelerDoorAccessMode mode =
                TravelerDoorAccessMode.fromId(requestedMode);
        PlayerMysteryData data =
                top.aurora.lordofmysteries.player.MysteryCapability.get(
                        player);
        data.travelerDoorAccessMode = mode.id();
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.travel.access.updated",
                Component.translatable(
                        "message.lord_of_mysteries.travel.access."
                                + mode.id()))
                .withStyle(ChatFormatting.AQUA));
        return 1;
    }

    public static int blockPlayer(
            ServerPlayer owner, ServerPlayer candidate) {
        if (owner.getUUID().equals(candidate.getUUID())) {
            PlayerFeedback.send(owner, Component.translatable(
                    "message.lord_of_mysteries.travel.block.self")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        PlayerMysteryData data =
                top.aurora.lordofmysteries.player.MysteryCapability.get(
                        owner);
        if (data.travelerDoorBlacklist.contains(candidate.getUUID())) {
            PlayerFeedback.send(owner, Component.translatable(
                    "message.lord_of_mysteries.travel.block.already",
                    candidate.getDisplayName())
                    .withStyle(ChatFormatting.YELLOW));
            return 0;
        }
        if (data.travelerDoorBlacklist.size()
                >= TravelerDoorPolicy.MAX_BLOCKED_PLAYERS) {
            PlayerFeedback.send(owner, Component.translatable(
                    "message.lord_of_mysteries.travel.block.full",
                    TravelerDoorPolicy.MAX_BLOCKED_PLAYERS)
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        data.travelerDoorBlacklist.add(candidate.getUUID());
        updateActiveDoors(owner, candidate.getUUID(), true);
        PlayerFeedback.send(owner, Component.translatable(
                "message.lord_of_mysteries.travel.block.added",
                candidate.getDisplayName())
                .withStyle(ChatFormatting.AQUA));
        return 1;
    }

    public static int unblockPlayer(
            ServerPlayer owner, ServerPlayer candidate) {
        PlayerMysteryData data =
                top.aurora.lordofmysteries.player.MysteryCapability.get(
                        owner);
        if (!data.travelerDoorBlacklist.remove(candidate.getUUID())) {
            PlayerFeedback.send(owner, Component.translatable(
                    "message.lord_of_mysteries.travel.block.missing",
                    candidate.getDisplayName())
                    .withStyle(ChatFormatting.YELLOW));
            return 0;
        }
        updateActiveDoors(owner, candidate.getUUID(), false);
        PlayerFeedback.send(owner, Component.translatable(
                "message.lord_of_mysteries.travel.block.removed",
                candidate.getDisplayName())
                .withStyle(ChatFormatting.AQUA));
        return 1;
    }

    public static int showBlockedPlayers(ServerPlayer owner) {
        PlayerMysteryData data =
                top.aurora.lordofmysteries.player.MysteryCapability.get(
                        owner);
        Set<UUID> blocked = TravelerDoorPolicy.normalizeBlacklist(
                data.travelerDoorBlacklist);
        PlayerFeedback.send(owner, Component.translatable(
                "message.lord_of_mysteries.travel.block.list",
                blocked.size(),
                TravelerDoorPolicy.MAX_BLOCKED_PLAYERS)
                .withStyle(ChatFormatting.GRAY));
        blocked.forEach(value -> PlayerFeedback.send(
                owner,
                Component.literal(value.toString())
                        .withStyle(ChatFormatting.DARK_GRAY)));
        return blocked.size();
    }

    public static boolean relayToHeldMarker(
            ServerPlayer leader,
            PlayerMysteryData data,
            List<ServerPlayer> sourcePlayers) {
        long now = leader.level().getGameTime();
        if (!AbilityCooldowns.ready(
                data.apprenticeWardCooldownEndTick, now)) {
            PlayerFeedback.send(leader, Component.translatable(
                    "message.lord_of_mysteries.travel.cooldown",
                    Math.max(1L, AbilityCooldowns.remaining(
                            data.apprenticeWardCooldownEndTick, now) / 20L)));
            return false;
        }
        Optional<Marker> marker = markerInHands(leader);
        if (marker.isEmpty()) {
            PlayerFeedback.send(leader, Component.translatable(
                    "message.lord_of_mysteries.travel.marker_required"));
            return false;
        }
        Marker destinationMarker = marker.get();
        ServerLevel destinationLevel = leader.getServer().getLevel(
                destinationMarker.dimension());
        if (destinationLevel == null) {
            PlayerFeedback.send(leader, Component.translatable(
                    "message.lord_of_mysteries.travel.dimension_unavailable",
                    destinationMarker.dimension().location().toString()));
            return false;
        }
        if (!activeMarker(destinationLevel, destinationMarker.position())) {
            PlayerFeedback.send(leader, Component.translatable(
                    "message.lord_of_mysteries.travel.marker_inactive"));
            return false;
        }
        BlockPos sourceAnchor = findSourceAnchor(leader);
        if (sourceAnchor == null
                || !doorSpaceClear(
                        destinationLevel, destinationMarker.position())) {
            PlayerFeedback.send(leader, Component.translatable(
                    "message.lord_of_mysteries.travel.destination_unsafe"));
            return false;
        }
        if (leader.serverLevel() == destinationLevel
                && sourceAnchor.distSqr(destinationMarker.position()) < 9d) {
            PlayerFeedback.send(leader, Component.translatable(
                    "message.lord_of_mysteries.travel.marker_too_close"));
            return false;
        }
        String doorName = markerNameInHands(leader);
        if (!TravelerDoorTerritoryService.allows(
                        leader,
                        leader.getUUID(),
                        leader.serverLevel(),
                        sourceAnchor.above(),
                        doorName,
                        TravelerDoorTerritoryEvent.Action.OPEN_SOURCE)
                || !TravelerDoorTerritoryService.allows(
                        leader,
                        leader.getUUID(),
                        destinationLevel,
                        destinationMarker.position().above(),
                        doorName,
                        TravelerDoorTerritoryEvent.Action.OPEN_DESTINATION)) {
            PlayerFeedback.send(leader, Component.translatable(
                    "message.lord_of_mysteries.travel.territory_denied")
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        List<ServerPlayer> passengers = sourcePlayers.stream()
                .filter(candidate -> M3TravelNetworkLogic.canJoinRelay(
                        candidate == leader,
                        candidate.serverLevel() == leader.serverLevel(),
                        candidate.isAlive(),
                        candidate.isSpectator(),
                        candidate.isSleeping(),
                        candidate.isShiftKeyDown(),
                        hasMatchingMarker(candidate, destinationMarker),
                        passengerCooldownReady(candidate, now),
                        candidate.distanceToSqr(leader)))
                .sorted(Comparator.comparing(
                        candidate -> candidate.getUUID().toString()))
                .limit(M3TravelNetworkLogic.MAX_PASSENGERS)
                .toList();
        if (findDoorArrival(
                destinationLevel,
                leader,
                destinationMarker.position()) == null
                || findDoorArrival(
                leader.serverLevel(),
                leader,
                sourceAnchor) == null) {
            PlayerFeedback.send(leader, Component.translatable(
                    "message.lord_of_mysteries.travel.destination_unsafe"));
            return false;
        }

        float cost = M3TravelNetworkLogic.relayCost(passengers.size());
        if (!SpiritualityCost.tryConsume(data, cost)) {
            PlayerFeedback.send(leader, Component.translatable(
                    "message.lord_of_mysteries.ability.insufficient_spirit",
                    cost));
            return false;
        }
        TravelerDoorAccessMode access = accessMode(data);
        String team = leader.getTeam() == null
                ? "" : leader.getTeam().getName();
        TravelerDoorEntity sourceDoor = createDoor(
                leader.serverLevel(),
                leader.getUUID(),
                team,
                access,
                doorName,
                data.travelerDoorBlacklist,
                destinationMarker.dimension(),
                destinationMarker.position(),
                sourceAnchor);
        TravelerDoorEntity destinationDoor = createDoor(
                destinationLevel,
                leader.getUUID(),
                team,
                access,
                doorName,
                data.travelerDoorBlacklist,
                leader.serverLevel().dimension(),
                sourceAnchor,
                destinationMarker.position());
        if (sourceDoor == null || destinationDoor == null
                || !leader.serverLevel().addFreshEntity(sourceDoor)) {
            SpiritualityCost.refund(data, cost);
            return false;
        }
        if (!destinationLevel.addFreshEntity(destinationDoor)) {
            sourceDoor.discard();
            SpiritualityCost.refund(data, cost);
            return false;
        }
        discardPreviousDoors(
                leader,
                sourceDoor.getUUID(),
                destinationDoor.getUUID());

        data.apprenticeWardCooldownEndTick =
                AbilityCooldowns.start(now, LEADER_COOLDOWN_TICKS);
        for (ServerPlayer passenger : passengers) {
            PlayerFeedback.send(passenger, Component.translatable(
                    "message.lord_of_mysteries.travel.door.invited",
                    leader.getDisplayName(),
                    M3TravelNetworkLogic.DOOR_DURATION_TICKS / 20));
        }
        PlayerFeedback.send(leader, Component.translatable(
                "message.lord_of_mysteries.travel.door.opened",
                doorLabel(doorName),
                destinationMarker.dimension().location().toString(),
                destinationMarker.position().getX(),
                destinationMarker.position().getY(),
                destinationMarker.position().getZ(),
                passengers.size(),
                Math.round(cost),
                M3TravelNetworkLogic.DOOR_DURATION_TICKS / 20,
                Component.translatable(
                        "message.lord_of_mysteries.travel.access."
                                + access.id()))
                .withStyle(ChatFormatting.AQUA));
        return true;
    }

    public static void recordDoorTransit(
            ServerPlayer player, UUID owner) {
        if (owner != null && !owner.equals(player.getUUID())) {
            long now = player.serverLevel().getGameTime();
            player.getPersistentData().putLong(
                    PASSENGER_COOLDOWN,
                    AbilityCooldowns.start(
                            now, PASSENGER_COOLDOWN_TICKS));
        }
    }

    private static boolean activeMarker(
            ServerLevel level, BlockPos position) {
        if (!level.isInWorldBounds(position)
                || !level.getWorldBorder().isWithinBounds(position)) {
            return false;
        }
        level.getChunkAt(position);
        return level.getBlockState(position).is(Blocks.LODESTONE);
    }

    private static boolean passengerCooldownReady(
            ServerPlayer player, long now) {
        return AbilityCooldowns.ready(
                player.getPersistentData().getLong(PASSENGER_COOLDOWN), now);
    }

    private static boolean hasMatchingMarker(
            ServerPlayer player, Marker expected) {
        return markerInHands(player)
                .map(expected::equals)
                .orElse(false);
    }

    public static Vec3 findDoorArrival(
            ServerLevel level, Entity entity, BlockPos marker) {
        return findDestination(level, entity, marker, List.of());
    }

    private static Vec3 findDestination(
            ServerLevel level, Entity entity, BlockPos marker,
            List<Vec3> occupied) {
        for (int[] offset : DESTINATION_OFFSETS) {
            for (int yOffset = 1; yOffset <= 4; yOffset++) {
                BlockPos feet = marker.offset(
                        offset[0], yOffset, offset[1]);
                if (!level.getWorldBorder().isWithinBounds(feet)
                        || !level.getBlockState(feet.below()).isFaceSturdy(
                        level, feet.below(),
                        net.minecraft.core.Direction.UP)) {
                    continue;
                }
                Vec3 candidate = Vec3.atBottomCenterOf(feet);
                if (occupied.stream().anyMatch(
                        placed -> placed.distanceToSqr(candidate) < 1d)) {
                    continue;
                }
                Vec3 move = candidate.subtract(entity.position());
                if (level.noCollision(
                        entity, entity.getBoundingBox().move(move))) {
                    return candidate;
                }
            }
        }
        return null;
    }

    private static BlockPos findSourceAnchor(ServerPlayer player) {
        BlockPos desiredFeet = player.blockPosition()
                .relative(player.getDirection(), 2);
        for (int[] offset : DESTINATION_OFFSETS) {
            for (int yOffset : new int[] {0, -1, 1, -2}) {
                BlockPos floor = desiredFeet.offset(
                        offset[0], yOffset - 1, offset[1]);
                if (doorSpaceClear(player.serverLevel(), floor)) {
                    return floor;
                }
            }
        }
        return null;
    }

    private static boolean doorSpaceClear(
            ServerLevel level, BlockPos floor) {
        if (!level.isInWorldBounds(floor)
                || !level.getWorldBorder().isWithinBounds(floor)
                || !level.getBlockState(floor).isFaceSturdy(
                        level, floor, net.minecraft.core.Direction.UP)) {
            return false;
        }
        BlockPos feet = floor.above();
        return level.getBlockState(feet)
                .getCollisionShape(level, feet).isEmpty()
                && level.getBlockState(feet.above())
                .getCollisionShape(level, feet.above()).isEmpty();
    }

    private static TravelerDoorEntity createDoor(
            ServerLevel level,
            UUID owner,
            String ownerTeam,
            TravelerDoorAccessMode access,
            String doorName,
            Collection<UUID> blockedPlayers,
            ResourceKey<Level> targetDimension,
            BlockPos targetAnchor,
            BlockPos localAnchor) {
        TravelerDoorEntity door = ModEntities.TRAVELER_DOOR.get().create(level);
        if (door == null) return null;
        door.configure(
                owner,
                ownerTeam,
                access,
                doorName,
                blockedPlayers,
                targetDimension,
                targetAnchor,
                M3TravelNetworkLogic.DOOR_DURATION_TICKS);
        Vec3 position = Vec3.atBottomCenterOf(localAnchor.above());
        door.moveTo(position.x, position.y, position.z);
        return door;
    }

    private static void discardPreviousDoors(
            ServerPlayer owner, UUID sourceDoor, UUID destinationDoor) {
        for (ServerLevel level : owner.getServer().getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof TravelerDoorEntity door
                        && door.ownedBy(owner.getUUID())
                        && !door.getUUID().equals(sourceDoor)
                        && !door.getUUID().equals(destinationDoor)) {
                    door.discard();
                }
            }
        }
    }

    private static void updateActiveDoors(
            ServerPlayer owner, UUID candidate, boolean blocked) {
        for (ServerLevel level : owner.getServer().getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof TravelerDoorEntity door
                        && door.ownedBy(owner.getUUID())) {
                    if (blocked) {
                        door.block(candidate);
                    } else {
                        door.unblock(candidate);
                    }
                }
            }
        }
    }

    private static ItemStack markerStackInHands(ServerPlayer player) {
        if (readMarker(player.getMainHandItem()).isPresent()) {
            return player.getMainHandItem();
        }
        if (readMarker(player.getOffhandItem()).isPresent()) {
            return player.getOffhandItem();
        }
        return ItemStack.EMPTY;
    }

    private static Component doorLabel(String doorName) {
        return doorName == null || doorName.isEmpty()
                ? Component.translatable(
                        "message.lord_of_mysteries.travel.name.unnamed")
                : Component.literal(doorName);
    }

    private static TravelerDoorAccessMode accessMode(
            PlayerMysteryData data) {
        return TravelerDoorAccessMode.fromId(
                data.travelerDoorAccessMode);
    }
}
