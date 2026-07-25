package top.aurora.lordofmysteries.ability;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.player.PlayerFeedback;
import top.aurora.lordofmysteries.player.PlayerMysteryData;

public final class TravelMarkerService {

    private static final String PASSENGER_COOLDOWN =
            ProjectMystery.MOD_ID + ":travel_relay_cooldown";
    private static final long LEADER_COOLDOWN_TICKS = 36_000L;
    private static final long PASSENGER_COOLDOWN_TICKS = 1_200L;
    private static final int[][] DESTINATION_OFFSETS = {
            {0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1},
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1},
            {2, 0}, {-2, 0}, {0, 2}, {0, -2}
    };

    private TravelMarkerService() {}

    public record Marker(ResourceKey<Level> dimension, BlockPos position) {}

    record MarkerData(ResourceLocation dimension, BlockPos position) {}

    private record Origin(
            ServerLevel level, Vec3 position, float yaw, float pitch) {}

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
                "message.lord_of_mysteries.travel.guide.consent",
                M3TravelNetworkLogic.MAX_PASSENGERS)
                .withStyle(ChatFormatting.GRAY));
        return active ? 1 : 0;
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
        List<ServerPlayer> travelers = new ArrayList<>();
        travelers.add(leader);
        travelers.addAll(passengers);
        Optional<Map<ServerPlayer, Vec3>> destinations = findDestinations(
                destinationLevel, travelers, destinationMarker.position());
        if (destinations.isEmpty()) {
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
        Map<ServerPlayer, Origin> origins = new LinkedHashMap<>();
        for (ServerPlayer traveler : travelers) {
            origins.put(traveler, new Origin(
                    traveler.serverLevel(), traveler.position(),
                    traveler.getYRot(), traveler.getXRot()));
        }
        List<ServerPlayer> moved = new ArrayList<>();
        for (ServerPlayer traveler : travelers) {
            if (!teleport(traveler, destinationLevel,
                    destinations.get().get(traveler))) {
                rollback(moved, origins);
                SpiritualityCost.refund(data, cost);
                PlayerFeedback.send(leader, Component.translatable(
                        "message.lord_of_mysteries.travel.teleport_failed"));
                return false;
            }
            moved.add(traveler);
        }

        data.apprenticeWardCooldownEndTick =
                AbilityCooldowns.start(now, LEADER_COOLDOWN_TICKS);
        for (ServerPlayer passenger : passengers) {
            passenger.getPersistentData().putLong(
                    PASSENGER_COOLDOWN,
                    AbilityCooldowns.start(now, PASSENGER_COOLDOWN_TICKS));
            passenger.addEffect(new MobEffectInstance(
                    MobEffects.DAMAGE_RESISTANCE,
                    100, 2, false, false, true));
            PlayerFeedback.send(passenger, Component.translatable(
                    "message.lord_of_mysteries.travel.passenger_arrival",
                    leader.getDisplayName()));
        }
        leader.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_RESISTANCE,
                100, 2, false, false, true));
        PlayerFeedback.send(leader, Component.translatable(
                "message.lord_of_mysteries.travel.success",
                destinationMarker.dimension().location().toString(),
                destinationMarker.position().getX(),
                destinationMarker.position().getY(),
                destinationMarker.position().getZ(),
                passengers.size(),
                Math.round(cost))
                .withStyle(ChatFormatting.AQUA));
        return true;
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

    private static Optional<Map<ServerPlayer, Vec3>> findDestinations(
            ServerLevel level, List<ServerPlayer> travelers,
            BlockPos marker) {
        Map<ServerPlayer, Vec3> destinations = new LinkedHashMap<>();
        List<Vec3> occupied = new ArrayList<>();
        for (ServerPlayer traveler : travelers) {
            Vec3 destination = findDestination(
                    level, traveler, marker, occupied);
            if (destination == null) return Optional.empty();
            destinations.put(traveler, destination);
            occupied.add(destination);
        }
        return Optional.of(destinations);
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

    private static boolean teleport(
            ServerPlayer player, ServerLevel destinationLevel,
            Vec3 destination) {
        ServerLevel sourceLevel = player.serverLevel();
        if (sourceLevel == destinationLevel) {
            player.teleportTo(
                    destination.x, destination.y, destination.z);
            return true;
        }
        return player.teleportTo(
                destinationLevel,
                destination.x, destination.y, destination.z,
                Set.<RelativeMovement>of(),
                player.getYRot(), player.getXRot());
    }

    private static void rollback(
            List<ServerPlayer> moved,
            Map<ServerPlayer, Origin> origins) {
        for (ServerPlayer player : moved) {
            Origin origin = origins.get(player);
            if (origin == null) continue;
            if (player.serverLevel() == origin.level()) {
                player.teleportTo(
                        origin.position().x,
                        origin.position().y,
                        origin.position().z);
            } else {
                player.teleportTo(
                        origin.level(),
                        origin.position().x,
                        origin.position().y,
                        origin.position().z,
                        Set.<RelativeMovement>of(),
                        origin.yaw(), origin.pitch());
            }
        }
    }
}
