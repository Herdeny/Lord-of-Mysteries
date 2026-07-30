package top.aurora.lordofmysteries.entity;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import net.minecraftforge.network.NetworkHooks;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.ability.M3TravelNetworkLogic;
import top.aurora.lordofmysteries.ability.TravelMarkerService;
import top.aurora.lordofmysteries.ability.TravelerDoorAccessMode;
import top.aurora.lordofmysteries.ability.TravelerDoorOrganizationPolicy;
import top.aurora.lordofmysteries.ability.TravelerDoorPolicy;
import top.aurora.lordofmysteries.compat.TravelerDoorTerritoryEvent;
import top.aurora.lordofmysteries.compat.TravelerDoorTerritoryService;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerFeedback;
import top.aurora.lordofmysteries.player.PlayerMysteryData;

public final class TravelerDoorEntity extends Entity {

    private static final String TRANSIT_COOLDOWN =
            ProjectMystery.MOD_ID + ":traveler_door_transit_cooldown";
    private static final String DENIED_FEEDBACK_COOLDOWN =
            ProjectMystery.MOD_ID + ":traveler_door_denied_feedback";

    private UUID owner;
    private String ownerTeam = "";
    private TravelerDoorAccessMode accessMode =
            TravelerDoorAccessMode.PARTY;
    private String organizationId = "";
    private int ownerOrganizationReputation;
    private String doorName = "";
    private Set<UUID> blockedPlayers = new HashSet<>();
    private ResourceKey<Level> targetDimension = Level.OVERWORLD;
    private BlockPos targetAnchor = BlockPos.ZERO;
    private int remainingTicks;
    private boolean configured;

    public TravelerDoorEntity(
            EntityType<? extends TravelerDoorEntity> type,
            Level level) {
        super(type, level);
        noPhysics = true;
        setInvulnerable(true);
    }

    public void configure(
            UUID owner,
            String ownerTeam,
            TravelerDoorAccessMode accessMode,
            String organizationId,
            int ownerOrganizationReputation,
            String doorName,
            Collection<UUID> blockedPlayers,
            ResourceKey<Level> targetDimension,
            BlockPos targetAnchor,
            int remainingTicks) {
        this.owner = owner;
        this.ownerTeam = TravelerDoorAccessMode.normalizedTeam(ownerTeam);
        this.accessMode = accessMode == null
                ? TravelerDoorAccessMode.PARTY : accessMode;
        this.organizationId =
                TravelerDoorOrganizationPolicy.normalizeId(organizationId);
        this.ownerOrganizationReputation = ownerOrganizationReputation;
        this.doorName = TravelerDoorPolicy.normalizeName(doorName);
        this.blockedPlayers = new HashSet<>(
                TravelerDoorPolicy.normalizeBlacklist(blockedPlayers));
        this.targetDimension = targetDimension;
        this.targetAnchor = targetAnchor == null
                ? BlockPos.ZERO : targetAnchor.immutable();
        this.remainingTicks = Math.min(
                M3TravelNetworkLogic.DOOR_DURATION_TICKS,
                Math.max(0, remainingTicks));
        configured = owner != null
                && targetDimension != null
                && remainingTicks > 0
                && (this.accessMode
                        != TravelerDoorAccessMode.ORGANIZATION
                        || TravelerDoorOrganizationPolicy.allows(
                                this.organizationId,
                                this.ownerOrganizationReputation,
                                this.ownerOrganizationReputation));
        applyDisplayName();
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            if (tickCount % 2 == 0) {
                level().addParticle(
                        ParticleTypes.REVERSE_PORTAL,
                        getX() + (random.nextDouble() - 0.5d) * 0.8d,
                        getY() + random.nextDouble() * 2.2d,
                        getZ() + (random.nextDouble() - 0.5d) * 0.8d,
                        0d, 0.02d, 0d);
            }
            return;
        }
        if (!configured || remainingTicks-- <= 0) {
            discard();
            return;
        }
        if (!(level() instanceof ServerLevel serverLevel)) return;
        if (tickCount % 5 == 0) {
            serverLevel.sendParticles(
                    ParticleTypes.REVERSE_PORTAL,
                    getX(), getY() + 1.1d, getZ(),
                    16, 0.45d, 0.9d, 0.45d, 0.05d);
        }
        if (tickCount < 10) return;
        for (ServerPlayer player : serverLevel.getEntitiesOfClass(
                ServerPlayer.class,
                getBoundingBox().inflate(0.2d, 0d, 0.2d),
                candidate -> candidate.isAlive()
                        && !candidate.isSpectator())) {
            tryTransit(player);
        }
    }

    public TransitResult tryTransit(ServerPlayer player) {
        if (!configured || player == null || !player.isAlive()
                || player.isSpectator()) {
            return TransitResult.INVALID;
        }
        long now = player.serverLevel().getGameTime();
        if (player.getPersistentData().getLong(TRANSIT_COOLDOWN) > now) {
            return TransitResult.COOLDOWN;
        }
        String candidateTeam = player.getTeam() == null
                ? "" : player.getTeam().getName();
        PlayerMysteryData candidateData = MysteryCapability.get(player);
        int candidateOrganizationReputation =
                TravelerDoorOrganizationPolicy.reputation(
                        candidateData.orgReputation,
                        organizationId);
        if (!owner.equals(player.getUUID())
                && blockedPlayers.contains(player.getUUID())) {
            sendBlocked(player, now);
            return TransitResult.BLOCKED;
        }
        if (!TravelerDoorPolicy.allows(
                owner,
                ownerTeam,
                accessMode,
                organizationId,
                ownerOrganizationReputation,
                blockedPlayers,
                player.getUUID(),
                candidateTeam,
                candidateOrganizationReputation)) {
            sendDenied(player, now);
            return TransitResult.DENIED;
        }
        ServerLevel destinationLevel =
                player.getServer().getLevel(targetDimension);
        if (destinationLevel == null
                || !destinationLevel.isInWorldBounds(targetAnchor)
                || !destinationLevel.getWorldBorder()
                        .isWithinBounds(targetAnchor)) {
            sendFailure(player);
            return TransitResult.UNAVAILABLE;
        }
        if (!TravelerDoorTerritoryService.allows(
                player,
                owner,
                destinationLevel,
                targetAnchor.above(),
                doorName,
                TravelerDoorTerritoryEvent.Action.TRANSIT_DESTINATION)) {
            sendTerritoryDenied(player, now);
            return TransitResult.TERRITORY_DENIED;
        }
        destinationLevel.getChunkAt(targetAnchor);
        Vec3 destination = TravelMarkerService.findDoorArrival(
                destinationLevel, player, targetAnchor);
        if (destination == null) {
            sendFailure(player);
            return TransitResult.UNSAFE;
        }
        boolean moved = player.serverLevel() == destinationLevel
                ? teleportSameLevel(player, destination)
                : player.teleportTo(
                        destinationLevel,
                        destination.x,
                        destination.y,
                        destination.z,
                        Set.<RelativeMovement>of(),
                        player.getYRot(),
                        player.getXRot());
        if (!moved) {
            sendFailure(player);
            return TransitResult.FAILED;
        }
        player.getPersistentData().putLong(
                TRANSIT_COOLDOWN,
                player.serverLevel().getGameTime()
                        + M3TravelNetworkLogic.TRANSIT_COOLDOWN_TICKS);
        TravelMarkerService.recordDoorTransit(player, owner);
        player.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_RESISTANCE,
                60, 1, false, false, true));
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.travel.door.transit")
                .withStyle(ChatFormatting.AQUA));
        return TransitResult.SUCCESS;
    }

    private static boolean teleportSameLevel(
            ServerPlayer player, Vec3 destination) {
        player.teleportTo(destination.x, destination.y, destination.z);
        return true;
    }

    private void sendDenied(ServerPlayer player, long now) {
        long nextFeedback = player.getPersistentData().getLong(
                DENIED_FEEDBACK_COOLDOWN);
        if (nextFeedback > now) return;
        player.getPersistentData().putLong(
                DENIED_FEEDBACK_COOLDOWN, now + 40L);
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.travel.door.denied",
                Component.translatable(
                        "message.lord_of_mysteries.travel.access."
                                + accessMode.id()))
                .withStyle(ChatFormatting.RED));
    }

    private void sendBlocked(ServerPlayer player, long now) {
        long nextFeedback = player.getPersistentData().getLong(
                DENIED_FEEDBACK_COOLDOWN);
        if (nextFeedback > now) return;
        player.getPersistentData().putLong(
                DENIED_FEEDBACK_COOLDOWN, now + 40L);
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.travel.door.blocked")
                .withStyle(ChatFormatting.RED));
    }

    private static void sendTerritoryDenied(
            ServerPlayer player, long now) {
        long nextFeedback = player.getPersistentData().getLong(
                DENIED_FEEDBACK_COOLDOWN);
        if (nextFeedback > now) return;
        player.getPersistentData().putLong(
                DENIED_FEEDBACK_COOLDOWN, now + 40L);
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.travel.territory_denied")
                .withStyle(ChatFormatting.RED));
    }

    private static void sendFailure(ServerPlayer player) {
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.travel.door.destination_failed")
                .withStyle(ChatFormatting.RED));
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        owner = tag.hasUUID("owner") ? tag.getUUID("owner") : null;
        ownerTeam = TravelerDoorAccessMode.normalizedTeam(
                tag.getString("owner_team"));
        accessMode = TravelerDoorAccessMode.fromId(
                tag.getString("access_mode"));
        organizationId =
                TravelerDoorOrganizationPolicy.normalizeId(
                        tag.getString("organization_id"));
        ownerOrganizationReputation =
                tag.getInt("owner_organization_reputation");
        doorName = TravelerDoorPolicy.normalizeName(
                tag.getString("door_name"));
        blockedPlayers = new HashSet<>();
        ListTag blocked = tag.getList(
                "blocked_players", Tag.TAG_STRING);
        for (int i = 0; i < blocked.size(); i++) {
            try {
                blockedPlayers.add(UUID.fromString(blocked.getString(i)));
            } catch (IllegalArgumentException exception) {
                continue;
            }
        }
        blockedPlayers = new HashSet<>(
                TravelerDoorPolicy.normalizeBlacklist(blockedPlayers));
        ResourceLocation dimension = ResourceLocation.tryParse(
                tag.getString("target_dimension"));
        targetDimension = dimension == null
                ? null : ResourceKey.create(Registries.DIMENSION, dimension);
        targetAnchor = tag.contains("target_anchor", Tag.TAG_COMPOUND)
                ? net.minecraft.nbt.NbtUtils.readBlockPos(
                        tag.getCompound("target_anchor"))
                : BlockPos.ZERO;
        remainingTicks = Math.min(
                M3TravelNetworkLogic.DOOR_DURATION_TICKS,
                Math.max(0, tag.getInt("remaining_ticks")));
        configured = owner != null
                && targetDimension != null
                && remainingTicks > 0
                && (accessMode
                        != TravelerDoorAccessMode.ORGANIZATION
                        || TravelerDoorOrganizationPolicy.allows(
                                organizationId,
                                ownerOrganizationReputation,
                                ownerOrganizationReputation));
        applyDisplayName();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (owner != null) tag.putUUID("owner", owner);
        tag.putString("owner_team", ownerTeam);
        tag.putString("access_mode", accessMode.id());
        tag.putString("organization_id", organizationId);
        tag.putInt(
                "owner_organization_reputation",
                ownerOrganizationReputation);
        tag.putString("door_name", doorName);
        ListTag blocked = new ListTag();
        TravelerDoorPolicy.normalizeBlacklist(blockedPlayers)
                .forEach(value -> blocked.add(
                        StringTag.valueOf(value.toString())));
        tag.put("blocked_players", blocked);
        if (targetDimension != null) {
            tag.putString(
                    "target_dimension",
                    targetDimension.location().toString());
        }
        tag.put(
                "target_anchor",
                net.minecraft.nbt.NbtUtils.writeBlockPos(targetAnchor));
        tag.putInt("remaining_ticks", remainingTicks);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    public boolean ownedBy(UUID candidate) {
        return owner != null && owner.equals(candidate);
    }

    public UUID owner() {
        return owner;
    }

    public TravelerDoorAccessMode accessMode() {
        return accessMode;
    }

    public String organizationId() {
        return organizationId;
    }

    public String doorName() {
        return doorName;
    }

    public Set<UUID> blockedPlayers() {
        return Set.copyOf(blockedPlayers);
    }

    public void block(UUID candidate) {
        if (candidate == null || candidate.equals(owner)) return;
        blockedPlayers.add(candidate);
        blockedPlayers = new HashSet<>(
                TravelerDoorPolicy.normalizeBlacklist(blockedPlayers));
    }

    public void unblock(UUID candidate) {
        if (candidate != null) blockedPlayers.remove(candidate);
    }

    public ResourceKey<Level> targetDimension() {
        return targetDimension;
    }

    public BlockPos targetAnchor() {
        return targetAnchor;
    }

    public int remainingTicks() {
        return remainingTicks;
    }

    private void applyDisplayName() {
        if (doorName.isEmpty()) {
            setCustomName(null);
            setCustomNameVisible(false);
            return;
        }
        setCustomName(Component.literal(doorName));
        setCustomNameVisible(true);
    }

    public enum TransitResult {
        SUCCESS,
        BLOCKED,
        DENIED,
        TERRITORY_DENIED,
        COOLDOWN,
        UNSAFE,
        UNAVAILABLE,
        FAILED,
        INVALID
    }
}
