package top.aurora.lordofmysteries.spirit;

import java.util.ArrayList;
import java.util.Set;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.ability.TravelMarkerService;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.OccultActivityService;
import top.aurora.lordofmysteries.player.PlayerDataSection;
import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.registry.ModItems;

@Mod.EventBusSubscriber(
        modid = ProjectMystery.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SpiritExpeditionService {

    public static final ResourceKey<Level> SPIRIT_WORLD = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(
                    ProjectMystery.MOD_ID, "spirit_world"));
    private static final int STABILIZE_SALT_COST = 1;
    private static final int STABILIZE_CANDLE_COST = 1;

    private SpiritExpeditionService() {}

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || event.getServer().getTickCount() % 20 != 0) {
            return;
        }
        for (ServerPlayer player : event.getServer()
                .getPlayerList().getPlayers()) {
            tickPlayer(player);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        SpiritExpeditionSavedData.Expedition expedition = saved(player).get(
                player.getUUID());
        if (expedition == null) {
            if (SPIRIT_WORLD.equals(player.level().dimension())) {
                recoverWithoutRecord(player);
            }
            return;
        }
        if (SpiritExpeditionPolicy.timedOut(
                player.level().getGameTime(), expedition.expiresAt())) {
            exitInternal(player, ExitReason.TIMEOUT);
            return;
        }
        ServerLevel spirit = player.getServer().getLevel(SPIRIT_WORLD);
        if (spirit == null) {
            exitInternal(player, ExitReason.INTERRUPTED);
            return;
        }
        Vec3 destination = SpiritExpeditionWorldBuilder.build(
                spirit, expedition.currentPad(),
                expedition.projection(), expedition.weather());
        if (!SPIRIT_WORLD.equals(player.level().dimension())) {
            if (destination == null || !teleport(player, spirit, destination)) {
                exitInternal(player, ExitReason.INTERRUPTED);
                return;
            }
            send(player, "message.lord_of_mysteries.spirit.resumed",
                    ChatFormatting.AQUA);
        }
        ensureEncounterEntity(player, expedition);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        SpiritExpeditionSavedData.Expedition expedition = saved(player).get(
                player.getUUID());
        if (expedition == null
                && !SPIRIT_WORLD.equals(player.level().dimension())) {
            return;
        }
        event.setCanceled(true);
        player.setHealth(Math.max(1f, player.getMaxHealth() * 0.25f));
        player.clearFire();
        PlayerMysteryData data = MysteryCapability.get(player);
        data.pollution = Math.min(100f, data.pollution + 3f);
        data.markDirty(PlayerDataSection.CORE);
        if (expedition == null) {
            recoverWithoutRecord(player);
        } else {
            exitInternal(player, ExitReason.DREAM_DEATH);
        }
    }

    public static int showGuide(ServerPlayer player) {
        send(player, "message.lord_of_mysteries.spirit.guide.title",
                ChatFormatting.GOLD);
        send(player, "message.lord_of_mysteries.spirit.guide.enter",
                ChatFormatting.GRAY);
        send(player, "message.lord_of_mysteries.spirit.guide.route",
                ChatFormatting.GRAY);
        send(player, "message.lord_of_mysteries.spirit.guide.safety",
                ChatFormatting.GRAY);
        return 1;
    }

    public static int start(ServerPlayer player, String projectionId) {
        SpiritProjection projection = SpiritProjection.fromId(projectionId);
        if (projection == null) {
            send(player, "message.lord_of_mysteries.spirit.invalid_projection",
                    ChatFormatting.RED);
            return 0;
        }
        if (!player.isAlive() || player.isSpectator()
                || SPIRIT_WORLD.equals(player.level().dimension())) {
            send(player, "message.lord_of_mysteries.spirit.enter_denied",
                    ChatFormatting.RED);
            return 0;
        }
        if (!OccultActivityService.isAvailable(player)) {
            OccultActivityService.sendDenied(player);
            return 0;
        }
        SpiritExpeditionSavedData data = saved(player);
        if (data.isReadOnlyFutureSchema()) {
            send(player, "message.lord_of_mysteries.spirit.enter_failed",
                    ChatFormatting.RED);
            return 0;
        }
        if (data.get(player.getUUID()) != null) {
            send(player, "message.lord_of_mysteries.spirit.already_active",
                    ChatFormatting.RED);
            return 0;
        }
        ItemStack routeLantern = find(player, ModItems.SPIRIT_ROUTE_LANTERN.get());
        if (routeLantern == null
                || find(player, ModItems.SPIRIT_COMPASS.get()) == null) {
            send(player, "message.lord_of_mysteries.spirit.missing_kit",
                    ChatFormatting.RED);
            return 0;
        }
        ServerLevel spirit = player.getServer().getLevel(SPIRIT_WORLD);
        if (spirit == null) {
            send(player, "message.lord_of_mysteries.spirit.dimension_missing",
                    ChatFormatting.RED);
            return 0;
        }
        ServerLevel originLevel = player.serverLevel();
        long now = originLevel.getGameTime();
        long routeSeed = SpiritExpeditionPolicy.mix(
                originLevel.getSeed() ^ player.getUUID().getMostSignificantBits(),
                player.getUUID().getLeastSignificantBits()
                        ^ player.blockPosition().asLong() ^ now);
        SpiritExpeditionSavedData.Expedition expedition = data.start(
                player.getUUID(), originLevel.dimension().location(),
                player.blockPosition(), projection, routeSeed, now);
        if (expedition == null) return 0;
        Vec3 destination = SpiritExpeditionWorldBuilder.build(
                spirit, expedition.currentPad(), projection,
                expedition.weather());
        if (destination == null || !teleport(player, spirit, destination)) {
            data.remove(player.getUUID());
            send(player, "message.lord_of_mysteries.spirit.enter_failed",
                    ChatFormatting.RED);
            return 0;
        }
        if (!player.getAbilities().instabuild) {
            routeLantern.hurtAndBreak(1, player, broken -> {});
        }
        applyWeather(player, expedition.weather());
        player.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_RESISTANCE, 100, 1, false, false, true));
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.spirit.entered",
                Component.translatable(projection.translationKey()),
                Component.translatable(expedition.weather().translationKey()),
                SpiritExpeditionPolicy.DURATION_TICKS / 1200L)
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        return 1;
    }

    public static int status(ServerPlayer player) {
        SpiritExpeditionSavedData.Expedition expedition = active(player);
        if (expedition == null) return 0;
        long remainingTicks = Math.max(
                0L, expedition.expiresAt() - player.level().getGameTime());
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.spirit.status",
                expedition.step(), SpiritExpeditionPolicy.ROUTE_LEGS,
                expedition.stability(), expedition.drift(),
                expedition.rewardScore(),
                Math.max(0L, remainingTicks / 20L),
                Component.translatable(expedition.weather().translationKey()))
                .withStyle(ChatFormatting.AQUA));
        if (expedition.pendingEncounter() != null) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.spirit.encounter.pending",
                    Component.translatable(
                            expedition.pendingEncounter().translationKey()))
                    .withStyle(ChatFormatting.YELLOW));
        }
        return 1;
    }

    public static int compass(ServerPlayer player) {
        SpiritExpeditionSavedData.Expedition expedition = active(player);
        if (expedition == null) return 0;
        if (expedition.pendingEncounter() != null) {
            send(player, "message.lord_of_mysteries.spirit.encounter.blocked",
                    ChatFormatting.RED);
            return 0;
        }
        if (expedition.extractionReady()) {
            send(player, "message.lord_of_mysteries.spirit.extraction_ready",
                    ChatFormatting.GREEN);
            return 1;
        }
        SpiritDirection direction = SpiritExpeditionPolicy.expectedDirection(
                expedition.routeSeed(), expedition.step(), expedition.tide(),
                expedition.projection(), expedition.weather());
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.spirit.compass",
                Component.translatable(direction.translationKey()),
                Component.translatable(expedition.weather().hintKey()))
                .withStyle(ChatFormatting.GOLD));
        return 1;
    }

    public static int navigate(ServerPlayer player, String directionId) {
        SpiritDirection direction = SpiritDirection.fromId(directionId);
        if (direction == null) {
            send(player, "message.lord_of_mysteries.spirit.invalid_direction",
                    ChatFormatting.RED);
            return 0;
        }
        SpiritExpeditionSavedData data = saved(player);
        SpiritExpeditionSavedData.Expedition expedition = active(player);
        if (expedition == null) return 0;
        if (expedition.pendingEncounter() != null) {
            send(player, "message.lord_of_mysteries.spirit.encounter.blocked",
                    ChatFormatting.RED);
            return 0;
        }
        if (expedition.extractionReady()) {
            send(player, "message.lord_of_mysteries.spirit.extraction_ready",
                    ChatFormatting.GREEN);
            return 0;
        }
        SpiritExpeditionPolicy.NavigationOutcome outcome =
                SpiritExpeditionPolicy.navigate(
                        expedition.routeSeed(), expedition.step(),
                        expedition.tide(), expedition.projection(),
                        expedition.weather(), direction);
        BlockPos destinationPos = SpiritExpeditionWorldBuilder.nextPad(
                expedition, direction);
        Vec3 destination = SpiritExpeditionWorldBuilder.build(
                player.serverLevel(), destinationPos,
                expedition.projection(), expedition.weather());
        if (destination == null) {
            send(player, "message.lord_of_mysteries.spirit.route_unsafe",
                    ChatFormatting.RED);
            return 0;
        }
        player.teleportTo(destination.x, destination.y, destination.z);
        SpiritExpeditionSavedData.Expedition changed = data.update(
                player.getUUID(), current -> current.afterNavigation(
                        outcome, destinationPos, player.level().getGameTime()));
        if (changed == null) return 0;
        player.sendSystemMessage(Component.translatable(
                outcome.correct()
                        ? "message.lord_of_mysteries.spirit.route.correct"
                        : "message.lord_of_mysteries.spirit.route.drift",
                Component.translatable(
                        outcome.expectedDirection().translationKey()),
                changed.stability(), changed.drift())
                .withStyle(outcome.correct()
                        ? ChatFormatting.GREEN : ChatFormatting.RED));
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.spirit.encounter.appeared",
                Component.translatable(outcome.encounter().translationKey()))
                .withStyle(ChatFormatting.YELLOW));
        spawnEncounterEntity(player, changed);
        applyWeather(player, changed.weather());
        if (changed.failed()) {
            exitInternal(player, ExitReason.DRIFT);
        }
        return 1;
    }

    public static int resolveEncounter(ServerPlayer player, String actionId) {
        SpiritEncounterAction action = SpiritEncounterAction.fromId(actionId);
        if (action == null) {
            send(player, "message.lord_of_mysteries.spirit.invalid_action",
                    ChatFormatting.RED);
            return 0;
        }
        SpiritExpeditionSavedData data = saved(player);
        SpiritExpeditionSavedData.Expedition expedition = active(player);
        if (expedition == null) return 0;
        SpiritEncounter encounter = expedition.pendingEncounter();
        if (encounter == null) {
            send(player, "message.lord_of_mysteries.spirit.no_encounter",
                    ChatFormatting.RED);
            return 0;
        }
        SpiritExpeditionPolicy.EncounterOutcome outcome =
                SpiritExpeditionPolicy.resolve(encounter, action);
        SpiritExpeditionSavedData.Expedition changed = data.update(
                player.getUUID(), current -> current.afterEncounter(
                        outcome, player.level().getGameTime()));
        if (changed == null) return 0;
        removeEncounterEntities(player.getServer(), player.getUUID());
        player.sendSystemMessage(Component.translatable(
                outcome.correct()
                        ? "message.lord_of_mysteries.spirit.encounter.success"
                        : "message.lord_of_mysteries.spirit.encounter.failure",
                Component.translatable(encounter.translationKey()),
                Component.translatable(action.translationKey()),
                changed.stability(), changed.drift())
                .withStyle(outcome.correct()
                        ? ChatFormatting.GREEN : ChatFormatting.RED));
        if (changed.failed()) {
            exitInternal(player, ExitReason.DRIFT);
        } else if (changed.extractionReady()) {
            send(player, "message.lord_of_mysteries.spirit.extraction_ready",
                    ChatFormatting.GREEN);
        }
        return 1;
    }

    public static int stabilize(ServerPlayer player) {
        SpiritExpeditionSavedData data = saved(player);
        SpiritExpeditionSavedData.Expedition expedition = active(player);
        if (expedition == null) return 0;
        if (expedition.stability() >= SpiritExpeditionPolicy.MAX_STABILITY
                && expedition.drift() == 0) {
            send(player, "message.lord_of_mysteries.spirit.stable",
                    ChatFormatting.YELLOW);
            return 0;
        }
        if (!player.getAbilities().instabuild
                && (player.getInventory().countItem(ModItems.SPIRIT_SALT.get())
                        < STABILIZE_SALT_COST
                || player.getInventory().countItem(ModItems.WHITE_CANDLE.get())
                        < STABILIZE_CANDLE_COST)) {
            send(player, "message.lord_of_mysteries.spirit.stabilize_missing",
                    ChatFormatting.RED);
            return 0;
        }
        SpiritExpeditionSavedData.Expedition changed = data.update(
                player.getUUID(), current -> current.stabilized(
                        player.level().getGameTime()));
        if (changed == null) return 0;
        if (!player.getAbilities().instabuild) {
            consume(player, ModItems.SPIRIT_SALT.get(), STABILIZE_SALT_COST);
            consume(player, ModItems.WHITE_CANDLE.get(), STABILIZE_CANDLE_COST);
        }
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.spirit.stabilized",
                changed.stability(), changed.drift())
                .withStyle(ChatFormatting.GREEN));
        return 1;
    }

    public static int exit(ServerPlayer player, boolean emergencyCharm) {
        SpiritExpeditionSavedData.Expedition expedition = saved(player).get(
                player.getUUID());
        if (expedition == null) {
            if (SPIRIT_WORLD.equals(player.level().dimension())) {
                return recoverWithoutRecord(player) ? 1 : 0;
            }
            send(player, "message.lord_of_mysteries.spirit.not_active",
                    ChatFormatting.RED);
            return 0;
        }
        if (!SPIRIT_WORLD.equals(player.level().dimension())) {
            return exitInternal(player, ExitReason.INTERRUPTED) ? 1 : 0;
        }
        ExitReason reason = expedition.extractionReady()
                && expedition.pendingEncounter() == null
                ? ExitReason.COMPLETE
                : emergencyCharm ? ExitReason.EMERGENCY : ExitReason.EARLY;
        return exitInternal(player, reason) ? 1 : 0;
    }

    public static int recover(ServerPlayer player) {
        if (saved(player).get(player.getUUID()) != null) {
            return exitInternal(player, ExitReason.INTERRUPTED) ? 1 : 0;
        }
        return SPIRIT_WORLD.equals(player.level().dimension())
                && recoverWithoutRecord(player) ? 1 : 0;
    }

    private static void tickPlayer(ServerPlayer player) {
        SpiritExpeditionSavedData.Expedition expedition = saved(player).get(
                player.getUUID());
        if (expedition == null) {
            if (SPIRIT_WORLD.equals(player.level().dimension())) {
                recoverWithoutRecord(player);
            }
            return;
        }
        long now = player.level().getGameTime();
        if (SpiritExpeditionPolicy.timedOut(now, expedition.expiresAt())) {
            exitInternal(player, ExitReason.TIMEOUT);
            return;
        }
        if (!SPIRIT_WORLD.equals(player.level().dimension())) {
            exitInternal(player, ExitReason.INTERRUPTED);
            return;
        }
        if (player.getY() < 20d) {
            Vec3 safe = SpiritExpeditionWorldBuilder.build(
                    player.serverLevel(), expedition.currentPad(),
                    expedition.projection(), expedition.weather());
            if (safe != null) player.teleportTo(safe.x, safe.y, safe.z);
        }
        applyWeather(player, expedition.weather());
    }

    private static SpiritExpeditionSavedData.Expedition active(
            ServerPlayer player) {
        SpiritExpeditionSavedData.Expedition expedition = saved(player).get(
                player.getUUID());
        if (expedition == null) {
            send(player, "message.lord_of_mysteries.spirit.not_active",
                    ChatFormatting.RED);
            return null;
        }
        if (!SPIRIT_WORLD.equals(player.level().dimension())) {
            send(player, "message.lord_of_mysteries.spirit.wrong_dimension",
                    ChatFormatting.RED);
            return null;
        }
        return expedition;
    }

    private static boolean exitInternal(
            ServerPlayer player, ExitReason reason) {
        SpiritExpeditionSavedData data = saved(player);
        SpiritExpeditionSavedData.Expedition expedition = data.get(
                player.getUUID());
        if (expedition == null) return recoverWithoutRecord(player);
        ReturnTarget target = returnTarget(player, expedition);
        if (target == null || !teleport(player, target.level(), target.position())) {
            send(player, "message.lord_of_mysteries.spirit.exit_failed",
                    ChatFormatting.RED);
            return false;
        }
        data.remove(player.getUUID());
        removeEncounterEntities(player.getServer(), player.getUUID());
        if (reason == ExitReason.COMPLETE) giveRewards(player, expedition);
        if (reason != ExitReason.COMPLETE) applyFailurePressure(player, reason);
        player.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_RESISTANCE, 100, 1, false, false, true));
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.spirit.exit." + reason.id,
                expedition.step(), expedition.rewardScore())
                .withStyle(reason == ExitReason.COMPLETE
                        ? ChatFormatting.GREEN : ChatFormatting.YELLOW));
        return true;
    }

    private static ReturnTarget returnTarget(
            ServerPlayer player,
            SpiritExpeditionSavedData.Expedition expedition) {
        MinecraftServer server = player.getServer();
        ResourceKey<Level> originKey = ResourceKey.create(
                Registries.DIMENSION, expedition.originDimension());
        ServerLevel origin = server.getLevel(originKey);
        if (origin != null
                && origin.isInWorldBounds(expedition.origin())
                && origin.getWorldBorder().isWithinBounds(
                        expedition.origin())) {
            origin.getChunkAt(expedition.origin());
            Vec3 safe = TravelMarkerService.findDoorArrival(
                    origin, player, expedition.origin().below());
            if (safe != null) return new ReturnTarget(origin, safe);
        }
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) return null;
        BlockPos spawn = overworld.getSharedSpawnPos();
        overworld.getChunkAt(spawn);
        Vec3 safe = TravelMarkerService.findDoorArrival(
                overworld, player, spawn.below());
        if (safe == null) {
            BlockPos surface = overworld.getHeightmapPos(
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawn);
            safe = Vec3.atBottomCenterOf(surface.above());
        }
        return new ReturnTarget(overworld, safe);
    }

    private static boolean recoverWithoutRecord(ServerPlayer player) {
        ServerLevel overworld = player.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) return false;
        BlockPos spawn = overworld.getSharedSpawnPos();
        overworld.getChunkAt(spawn);
        BlockPos surface = overworld.getHeightmapPos(
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawn);
        boolean moved = teleport(
                player, overworld, Vec3.atBottomCenterOf(surface.above()));
        if (moved) {
            send(player, "message.lord_of_mysteries.spirit.recovered",
                    ChatFormatting.YELLOW);
        }
        return moved;
    }

    private static void giveRewards(
            ServerPlayer player,
            SpiritExpeditionSavedData.Expedition expedition) {
        int residue = Math.max(2, 2 + expedition.rewardScore() / 3);
        give(player, new ItemStack(ModItems.SPIRIT_ROUTE_RESIDUE.get(), residue));
        give(player, new ItemStack(ModItems.MEMORY_FRAGMENT.get(),
                Math.max(1, expedition.resolvedEncounters() / 2)));
        if (expedition.rewardScore() >= 15) {
            give(player, new ItemStack(ModItems.SPIRIT_ORCHID_PETALS.get()));
        }
    }

    private static void applyFailurePressure(
            ServerPlayer player, ExitReason reason) {
        float pressure = switch (reason) {
            case DREAM_DEATH -> 12f;
            case DRIFT -> 9f;
            case TIMEOUT -> 6f;
            case INTERRUPTED -> 4f;
            case EMERGENCY -> 3f;
            case EARLY -> 1f;
            case COMPLETE -> 0f;
        };
        PlayerMysteryData data = MysteryCapability.get(player);
        data.insanityPressure = Math.min(
                100f, data.insanityPressure + pressure);
        data.markDirty(PlayerDataSection.CORE);
    }

    private static void applyWeather(
            ServerPlayer player, SpiritWeather weather) {
        MobEffectInstance effect = switch (weather) {
            case SPIRIT_MIST -> new MobEffectInstance(
                    MobEffects.BLINDNESS, 60, 0, false, false, true);
            case STARLESS_NIGHT -> new MobEffectInstance(
                    MobEffects.DARKNESS, 60, 0, false, false, true);
            case WHISPERING_RAIN -> new MobEffectInstance(
                    MobEffects.CONFUSION, 80, 0, false, false, true);
            case SPIRITUAL_STORM -> new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN, 60, 0, false, false, true);
            case MEMORY_SNOW -> new MobEffectInstance(
                    MobEffects.DIG_SLOWDOWN, 60, 0, false, false, true);
            case DOORLIGHT_AURORA -> new MobEffectInstance(
                    MobEffects.GLOWING, 60, 0, false, false, true);
        };
        player.addEffect(effect);
    }

    private static void ensureEncounterEntity(
            ServerPlayer player,
            SpiritExpeditionSavedData.Expedition expedition) {
        if (expedition.pendingEncounter() == null
                || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        SpiritEcologyKind expected = SpiritEcologyKind.fromId(
                expedition.pendingEncounter().id());
        BlockPos pad = expedition.currentPad();
        boolean present = false;
        var stale = new ArrayList<SpiritEcologyEntity>();
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof SpiritEcologyEntity ecology
                    && ecology.belongsTo(player.getUUID())) {
                if (ecology.ecologyKind() == expected
                        && ecology.distanceToSqr(
                                pad.getX() + 0.5d,
                                pad.getY() + 1d,
                                pad.getZ() + 0.5d) <= 4096d) {
                    present = true;
                } else {
                    stale.add(ecology);
                }
            }
        }
        stale.forEach(Entity::discard);
        if (!present) spawnEncounterEntity(player, expedition);
    }

    private static void spawnEncounterEntity(
            ServerPlayer player,
            SpiritExpeditionSavedData.Expedition expedition) {
        if (expedition == null || expedition.pendingEncounter() == null
                || !(player.level() instanceof ServerLevel level)
                || !SPIRIT_WORLD.equals(level.dimension())) {
            return;
        }
        removeEncounterEntities(player.getServer(), player.getUUID());
        SpiritEcologyKind kind = SpiritEcologyKind.fromId(
                expedition.pendingEncounter().id());
        if (kind == null) return;
        SpiritEcologyEntity entity = kind.entityType().create(level);
        if (entity == null) return;
        entity.bind(player.getUUID(), kind);
        BlockPos pad = expedition.currentPad();
        entity.moveTo(
                pad.getX() + 2.5d, pad.getY() + 1d,
                pad.getZ() + 0.5d, 180f, 0f);
        entity.restrictTo(pad.above(), 5);
        level.addFreshEntity(entity);
    }

    private static void removeEncounterEntities(
            MinecraftServer server, java.util.UUID owner) {
        ServerLevel spirit = server.getLevel(SPIRIT_WORLD);
        if (spirit == null) return;
        var owned = new ArrayList<SpiritEcologyEntity>();
        for (Entity entity : spirit.getAllEntities()) {
            if (entity instanceof SpiritEcologyEntity ecology
                    && ecology.belongsTo(owner)) {
                owned.add(ecology);
            }
        }
        owned.forEach(Entity::discard);
    }

    private static boolean teleport(
            ServerPlayer player, ServerLevel level, Vec3 destination) {
        if (player.serverLevel() == level) {
            player.teleportTo(destination.x, destination.y, destination.z);
            return true;
        }
        return player.teleportTo(
                level, destination.x, destination.y, destination.z,
                Set.<RelativeMovement>of(),
                player.getYRot(), player.getXRot());
    }

    private static ItemStack find(ServerPlayer player, Item item) {
        for (int slot = 0;
             slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(item)) return stack;
        }
        return null;
    }

    private static void consume(
            ServerPlayer player, Item item, int amount) {
        int remaining = amount;
        for (int slot = 0;
             slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.is(item)) continue;
            int consumed = Math.min(remaining, stack.getCount());
            stack.shrink(consumed);
            remaining -= consumed;
            if (remaining == 0) return;
        }
    }

    private static void give(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) player.drop(stack, false);
    }

    private static SpiritExpeditionSavedData saved(ServerPlayer player) {
        return SpiritExpeditionSavedData.get(player.serverLevel());
    }

    private static void send(
            ServerPlayer player, String key, ChatFormatting formatting) {
        player.sendSystemMessage(Component.translatable(key)
                .withStyle(formatting));
    }

    private enum ExitReason {
        COMPLETE("complete"),
        EARLY("early"),
        EMERGENCY("emergency"),
        TIMEOUT("timeout"),
        DRIFT("drift"),
        DREAM_DEATH("dream_death"),
        INTERRUPTED("interrupted");

        private final String id;

        ExitReason(String id) {
            this.id = id;
        }
    }

    private record ReturnTarget(ServerLevel level, Vec3 position) {}
}
