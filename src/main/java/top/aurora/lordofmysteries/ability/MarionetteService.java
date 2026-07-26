package top.aurora.lordofmysteries.ability;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.acting.ActingEvent;
import top.aurora.lordofmysteries.acting.ActingEventHandler;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerFeedback;
import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.potion.SeerPotionItem;

@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class MarionetteService {

    public static final String OWNER_TAG =
            ProjectMystery.MOD_ID + ":marionette_owner";
    private static final String CREATED_AT_TAG =
            ProjectMystery.MOD_ID + ":marionette_created_at";
    private static final int[][] RECALL_OFFSETS = {
            {2, 0}, {-2, 0}, {0, 2}, {0, -2},
            {2, 2}, {2, -2}, {-2, 2}, {-2, -2},
            {3, 0}, {-3, 0}, {0, 3}, {0, -3}
    };

    private MarionetteService() {}

    public static boolean tryCreateFromLook(
            ServerPlayer owner, PlayerMysteryData data) {
        long now = owner.level().getGameTime();
        if (!AbilityCooldowns.ready(
                data.marionetteCreationCooldownEndTick, now)) {
            PlayerFeedback.send(owner, Component.translatable(
                    "message.lord_of_mysteries.marionette.cooldown",
                    Math.max(1L, AbilityCooldowns.remaining(
                            data.marionetteCreationCooldownEndTick, now) / 20L))
                    .withStyle(ChatFormatting.YELLOW));
            return false;
        }
        LivingEntity looked = AbilityTargeting.findLookTarget(owner, 14d);
        if (!(looked instanceof Mob target)) {
            return feedback(owner, "invalid_target", ChatFormatting.RED);
        }
        CreationResult result = create(owner, data, target);
        return switch (result) {
            case SUCCESS -> true;
            case ALREADY_OWNED -> feedback(
                    owner, "already_owned", ChatFormatting.YELLOW);
            case OWNED_BY_ANOTHER -> feedback(
                    owner, "owned_by_another", ChatFormatting.RED);
            case ROSTER_FULL -> feedback(
                    owner, "roster_full", ChatFormatting.RED,
                    MarionettePolicy.MAX_MARIONETTES);
            case INSUFFICIENT_SPIRITUALITY -> feedback(
                    owner, "insufficient", ChatFormatting.RED,
                    Math.round(MarionettePolicy.CREATION_COST));
            case INVALID_TARGET -> feedback(
                    owner, "invalid_target", ChatFormatting.RED);
        };
    }

    public static CreationResult create(
            ServerPlayer owner,
            PlayerMysteryData data,
            Mob target) {
        if (owner == null || data == null || target == null
                || !target.isAlive()
                || !SeerPotionItem.SEER_PATHWAY.equals(data.pathway)
                || data.sequence != 5) {
            return CreationResult.INVALID_TARGET;
        }
        List<UUID> roster = MarionettePolicy.normalizeRoster(
                data.marionetteRoster);
        data.marionetteRoster = new ArrayList<>(roster);
        UUID currentOwner = ownerOf(target).orElse(null);
        if (owner.getUUID().equals(currentOwner)) {
            return CreationResult.ALREADY_OWNED;
        }
        if (currentOwner != null) {
            return CreationResult.OWNED_BY_ANOTHER;
        }
        boolean hostile =
                target.getType().getCategory() == MobCategory.MONSTER;
        if (!MarionettePolicy.canCreate(
                false,
                hostile,
                false,
                target.getMaxHealth(),
                target.getHealth(),
                roster.size())) {
            return roster.size() >= MarionettePolicy.MAX_MARIONETTES
                    ? CreationResult.ROSTER_FULL
                    : CreationResult.INVALID_TARGET;
        }
        if (!SpiritualityCost.tryConsume(
                data, MarionettePolicy.CREATION_COST)) {
            return CreationResult.INSUFFICIENT_SPIRITUALITY;
        }

        long now = owner.level().getGameTime();
        target.getPersistentData().putUUID(OWNER_TAG, owner.getUUID());
        target.getPersistentData().putLong(CREATED_AT_TAG, now);
        target.setPersistenceRequired();
        target.setTarget(null);
        target.getNavigation().stop();
        target.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_RESISTANCE, 100, 1, false, true));
        data.marionetteRoster.add(target.getUUID());
        data.marionetteCreationCooldownEndTick =
                AbilityCooldowns.start(
                        now, MarionettePolicy.CREATION_COOLDOWN_TICKS);

        owner.serverLevel().sendParticles(
                ParticleTypes.ENCHANT,
                target.getX(),
                target.getY() + target.getBbHeight() * 0.6d,
                target.getZ(),
                48, 0.55d, 0.8d, 0.55d, 0.08d);
        owner.serverLevel().playSound(
                null,
                target.blockPosition(),
                SoundEvents.EVOKER_CAST_SPELL,
                SoundSource.PLAYERS,
                0.8f,
                0.75f);
        PlayerFeedback.send(owner, Component.translatable(
                "message.lord_of_mysteries.marionette.created",
                target.getDisplayName(),
                data.marionetteRoster.size(),
                MarionettePolicy.MAX_MARIONETTES)
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        ActingEventHandler.trigger(
                owner,
                ActingEvent.MARIONETTIST5_RESTRAIN_CRISIS,
                target);
        return CreationResult.SUCCESS;
    }

    public static int sendGuide(ServerPlayer owner) {
        PlayerMysteryData data = MysteryCapability.get(owner);
        normalize(data);
        PlayerFeedback.send(owner, Component.translatable(
                "message.lord_of_mysteries.marionette.guide",
                data.marionetteRoster.size(),
                MarionettePolicy.MAX_MARIONETTES)
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        PlayerFeedback.send(owner, Component.translatable(
                "message.lord_of_mysteries.marionette.controls")
                .withStyle(ChatFormatting.GRAY));
        for (int index = 0; index < data.marionetteRoster.size(); index++) {
            UUID id = data.marionetteRoster.get(index);
            Optional<Mob> loaded = findLoaded(owner.getServer(), id);
            if (loaded.isPresent()) {
                Mob mob = loaded.get();
                PlayerFeedback.send(owner, Component.translatable(
                        "message.lord_of_mysteries.marionette.entry.loaded",
                        index + 1,
                        mob.getDisplayName(),
                        Math.round(mob.getHealth()),
                        Math.round(mob.getMaxHealth()),
                        mob.level().dimension().location().toString())
                        .withStyle(ChatFormatting.AQUA));
            } else {
                PlayerFeedback.send(owner, Component.translatable(
                        "message.lord_of_mysteries.marionette.entry.unloaded",
                        index + 1,
                        id.toString())
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
        }
        return data.marionetteRoster.size();
    }

    public static int release(ServerPlayer owner, int slot) {
        PlayerMysteryData data = MysteryCapability.get(owner);
        normalize(data);
        if (slot < 1 || slot > data.marionetteRoster.size()) {
            return feedbackCode(
                    owner, "invalid_slot", ChatFormatting.RED, slot);
        }
        UUID id = data.marionetteRoster.remove(slot - 1);
        findLoaded(owner.getServer(), id).ifPresent(
                MarionetteService::clearOwnership);
        PlayerFeedback.send(owner, Component.translatable(
                "message.lord_of_mysteries.marionette.released",
                slot, id.toString())
                .withStyle(ChatFormatting.YELLOW));
        return 1;
    }

    public static int releaseAll(ServerPlayer owner) {
        PlayerMysteryData data = MysteryCapability.get(owner);
        normalize(data);
        int released = data.marionetteRoster.size();
        data.marionetteRoster.forEach(id ->
                findLoaded(owner.getServer(), id).ifPresent(
                        MarionetteService::clearOwnership));
        data.marionetteRoster.clear();
        PlayerFeedback.send(owner, Component.translatable(
                "message.lord_of_mysteries.marionette.released_all",
                released).withStyle(ChatFormatting.YELLOW));
        return released;
    }

    public static int recall(ServerPlayer owner) {
        PlayerMysteryData data = MysteryCapability.get(owner);
        normalize(data);
        int recalled = 0;
        int offsetIndex = 0;
        for (UUID id : data.marionetteRoster) {
            Optional<Mob> loaded = findLoaded(owner.getServer(), id);
            if (loaded.isEmpty()) continue;
            Mob mob = loaded.get();
            if (mob.level() != owner.serverLevel()
                    || !owner.getUUID().equals(
                    ownerOf(mob).orElse(null))) {
                continue;
            }
            Vec3 destination = findRecallDestination(
                    owner.serverLevel(), owner, mob, offsetIndex);
            offsetIndex++;
            if (destination == null) continue;
            mob.teleportTo(
                    destination.x, destination.y, destination.z);
            mob.setTarget(null);
            mob.getNavigation().stop();
            recalled++;
        }
        PlayerFeedback.send(owner, Component.translatable(
                "message.lord_of_mysteries.marionette.recalled",
                recalled, data.marionetteRoster.size())
                .withStyle(recalled > 0
                        ? ChatFormatting.AQUA : ChatFormatting.YELLOW));
        return recalled;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null || server.getTickCount() % 10 != 0) return;
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof Mob mob && isMarionette(mob)) {
                    maintain(server, mob);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        Entity attacker = event.getSource().getEntity();
        LivingEntity victim = event.getEntity();
        Optional<UUID> attackerOwner = ownerOf(attacker);
        Optional<UUID> victimOwner = ownerOf(victim);
        if (!MarionettePolicy.canDamage(
                attackerOwner.orElse(null),
                attacker == null ? null : attacker.getUUID(),
                victim instanceof Player,
                victimOwner.orElse(null))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        Optional<UUID> ownerId = ownerOf(event.getEntity());
        if (ownerId.isEmpty()) return;
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        ServerPlayer owner = server.getPlayerList().getPlayer(ownerId.get());
        if (owner == null) return;
        PlayerMysteryData data = MysteryCapability.get(owner);
        data.marionetteRoster.remove(event.getEntity().getUUID());
    }

    public static boolean isMarionette(Entity entity) {
        return ownerOf(entity).isPresent();
    }

    public static Optional<UUID> ownerOf(Entity entity) {
        if (entity == null
                || !entity.getPersistentData().hasUUID(OWNER_TAG)) {
            return Optional.empty();
        }
        return Optional.of(entity.getPersistentData().getUUID(OWNER_TAG));
    }

    private static void maintain(MinecraftServer server, Mob mob) {
        UUID ownerId = ownerOf(mob).orElse(null);
        if (ownerId == null) return;
        ServerPlayer owner = server.getPlayerList().getPlayer(ownerId);
        if (owner == null) {
            mob.setTarget(null);
            mob.getNavigation().stop();
            return;
        }
        PlayerMysteryData data = MysteryCapability.get(owner);
        normalize(data);
        if (!data.marionetteRoster.contains(mob.getUUID())) {
            clearOwnership(mob);
            return;
        }
        LivingEntity currentTarget = mob.getTarget();
        if (currentTarget instanceof Player
                || ownerOf(currentTarget).isPresent()) {
            mob.setTarget(null);
            currentTarget = null;
        }
        if (mob.level() != owner.serverLevel()) {
            mob.setTarget(null);
            mob.getNavigation().stop();
            return;
        }

        LivingEntity commandedTarget = combatTarget(owner, mob);
        if (commandedTarget != null) {
            mob.setTarget(commandedTarget);
            return;
        }
        if (currentTarget == null
                && mob.distanceToSqr(owner) > 64d) {
            mob.getNavigation().moveTo(owner, 1.1d);
        }
    }

    private static LivingEntity combatTarget(
            ServerPlayer owner, Mob mob) {
        LivingEntity candidate = owner.getLastHurtMob();
        if (!validCombatTarget(owner, mob, candidate)) {
            candidate = owner.getLastHurtByMob();
        }
        return validCombatTarget(owner, mob, candidate)
                ? candidate : null;
    }

    private static boolean validCombatTarget(
            ServerPlayer owner,
            Mob mob,
            LivingEntity candidate) {
        return candidate != null
                && candidate.isAlive()
                && !(candidate instanceof Player)
                && ownerOf(candidate).isEmpty()
                && candidate.level() == mob.level()
                && mob.distanceToSqr(candidate) <= 576d;
    }

    private static Optional<Mob> findLoaded(
            MinecraftServer server, UUID id) {
        if (server == null || id == null) return Optional.empty();
        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(id);
            if (entity instanceof Mob mob) return Optional.of(mob);
        }
        return Optional.empty();
    }

    private static void clearOwnership(Mob mob) {
        mob.getPersistentData().remove(OWNER_TAG);
        mob.getPersistentData().remove(CREATED_AT_TAG);
        mob.setTarget(null);
        mob.getNavigation().stop();
    }

    private static Vec3 findRecallDestination(
            ServerLevel level,
            ServerPlayer owner,
            Mob mob,
            int startingOffset) {
        for (int attempt = 0; attempt < RECALL_OFFSETS.length; attempt++) {
            int[] offset = RECALL_OFFSETS[
                    (startingOffset + attempt) % RECALL_OFFSETS.length];
            BlockPos floor = owner.blockPosition().offset(
                    offset[0], -1, offset[1]);
            BlockPos feet = floor.above();
            if (!level.getWorldBorder().isWithinBounds(feet)
                    || !level.getBlockState(floor).isFaceSturdy(
                    level, floor, Direction.UP)) {
                continue;
            }
            Vec3 destination = Vec3.atBottomCenterOf(feet);
            AABB bounds = mob.getDimensions(mob.getPose())
                    .makeBoundingBox(destination);
            if (level.noCollision(mob, bounds)) {
                return destination;
            }
        }
        return null;
    }

    private static void normalize(PlayerMysteryData data) {
        data.marionetteRoster = new ArrayList<>(
                MarionettePolicy.normalizeRoster(
                        data.marionetteRoster));
    }

    private static boolean feedback(
            ServerPlayer player,
            String key,
            ChatFormatting formatting,
            Object... args) {
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.marionette." + key,
                args).withStyle(formatting));
        return false;
    }

    private static int feedbackCode(
            ServerPlayer player,
            String key,
            ChatFormatting formatting,
            Object... args) {
        feedback(player, key, formatting, args);
        return 0;
    }

    public enum CreationResult {
        SUCCESS,
        INVALID_TARGET,
        ALREADY_OWNED,
        OWNED_BY_ANOTHER,
        ROSTER_FULL,
        INSUFFICIENT_SPIRITUALITY
    }
}
