package top.aurora.lordofmysteries.dream;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
import top.aurora.lordofmysteries.artifact.SealedArtifactService;
import top.aurora.lordofmysteries.organization.OrganizationActionSavedData;
import top.aurora.lordofmysteries.organization.OrganizationActionType;
import top.aurora.lordofmysteries.organization.OrganizationDefinitionManager;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerDataSection;
import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.registry.ModItems;
import top.aurora.lordofmysteries.spirit.SpiritExpeditionSavedData;
import top.aurora.lordofmysteries.spirit.SpiritExpeditionService;

@Mod.EventBusSubscriber(
        modid = ProjectMystery.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SharedDreamService {

    public static final ResourceKey<Level> DREAM_WORLD = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(
                    ProjectMystery.MOD_ID, "shared_dream"));
    private static final ResourceLocation SHARED_DREAM_KNOWLEDGE =
            ResourceLocation.fromNamespaceAndPath(
                    ProjectMystery.MOD_ID, "knowledge/shared_dream");
    private static final double INVITE_DISTANCE_SQUARED = 24d * 24d;
    private static final int RESTART_LOGIN_GRACE_TICKS = 1_200;

    private SharedDreamService() {}

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || event.getServer().getTickCount() % 20 != 0) {
            return;
        }
        ServerLevel overworld = event.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) return;
        SharedDreamSavedData data = SharedDreamSavedData.get(overworld);
        long now = overworld.getGameTime();
        for (SharedDreamSavedData.DreamSession session : data.sessions()) {
            if (session.state() == SharedDreamSavedData.DreamState.LOBBY) {
                if (SharedDreamPolicy.invitationExpired(
                        now, session.expiresAt())) {
                    closeLobby(event.getServer(), data, session,
                            DreamCloseReason.INVITE_TIMEOUT);
                }
                continue;
            }
            if (session.state() == SharedDreamSavedData.DreamState.COMPLETE) {
                closeActive(event.getServer(), data, session,
                        DreamCloseReason.COMPLETE);
                continue;
            }
            if (session.state() == SharedDreamSavedData.DreamState.FAILED) {
                closeActive(event.getServer(), data, session,
                        DreamCloseReason.INTERRUPTED);
                continue;
            }
            if (SharedDreamPolicy.invitationExpired(
                    now, session.sessionExpiresAt())) {
                closeActive(event.getServer(), data, session,
                        DreamCloseReason.SESSION_TIMEOUT);
                continue;
            }
            DreamCloseReason invalid = validateActive(
                    event.getServer(), session,
                    event.getServer().getTickCount()
                            >= RESTART_LOGIN_GRACE_TICKS);
            if (invalid != null) {
                closeActive(event.getServer(), data, session, invalid);
            }
        }
        for (ServerPlayer player : event.getServer()
                .getPlayerList().getPlayers()) {
            SharedDreamSavedData.Recovery recovery = data.recovery(
                    player.getUUID());
            if (recovery != null) settleRecovery(player, data, recovery);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        SharedDreamSavedData data = saved(player);
        SharedDreamSavedData.Recovery recovery = data.recovery(
                player.getUUID());
        if (recovery != null) {
            settleRecovery(player, data, recovery);
            return;
        }
        SharedDreamSavedData.DreamSession session = data.forPlayer(
                player.getUUID());
        if (session != null
                && session.state() == SharedDreamSavedData.DreamState.ACTIVE) {
            ServerLevel dream = player.getServer().getLevel(DREAM_WORLD);
            Vec3 destination = dream == null ? null
                    : SharedDreamWorldBuilder.build(
                            dream, session.lane(), session.scenario());
            if (destination == null
                    || !teleport(player, dream, destination)) {
                closeActive(player.getServer(), data, session,
                        DreamCloseReason.INTERRUPTED);
            } else {
                applyDreamEffects(player);
            }
            return;
        }
        if (DREAM_WORLD.equals(player.level().dimension())) {
            recoverWithoutRecord(player);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        SharedDreamSavedData data = saved(player);
        SharedDreamSavedData.DreamSession session = data.forPlayer(
                player.getUUID());
        if (session == null) return;
        if (session.state() == SharedDreamSavedData.DreamState.LOBBY) {
            closeLobby(player.getServer(), data, session,
                    DreamCloseReason.DISCONNECTED);
        } else {
            closeActive(player.getServer(), data, session,
                    DreamCloseReason.DISCONNECTED);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        SharedDreamSavedData data = saved(player);
        SharedDreamSavedData.DreamSession session = data.forPlayer(
                player.getUUID());
        if (session == null || session.state()
                != SharedDreamSavedData.DreamState.ACTIVE) {
            return;
        }
        event.setCanceled(true);
        player.setHealth(Math.max(1f, player.getMaxHealth() * 0.25f));
        player.clearFire();
        closeActive(player.getServer(), data, session,
                DreamCloseReason.DREAM_DEATH);
    }

    public static int showGuide(ServerPlayer player) {
        send(player, "message.lord_of_mysteries.dream.guide.title",
                ChatFormatting.LIGHT_PURPLE);
        send(player, "message.lord_of_mysteries.dream.guide.consent",
                ChatFormatting.GRAY);
        send(player, "message.lord_of_mysteries.dream.guide.ritual",
                ChatFormatting.GRAY);
        send(player, "message.lord_of_mysteries.dream.guide.actions",
                ChatFormatting.GRAY);
        return 1;
    }

    public static int invite(
            ServerPlayer host, ServerPlayer target,
            String rawOrganization) {
        ResourceLocation organization = normalizeOrganization(rawOrganization);
        if (organization == null
                || OrganizationDefinitionManager.get(organization) == null) {
            send(host, "message.lord_of_mysteries.dream.invalid_organization",
                    ChatFormatting.RED);
            return 0;
        }
        if (!canInvite(host, target, organization)) return 0;
        SharedDreamSavedData data = saved(host);
        SharedDreamSavedData.DreamSession existing = data.forPlayer(
                host.getUUID());
        long now = host.getServer().overworld().getGameTime();
        SharedDreamSavedData.DreamSession session;
        if (existing == null) {
            long seed = SharedDreamPolicy.mix(
                    host.getServer().overworld().getSeed()
                            ^ host.getUUID().getMostSignificantBits(),
                    target.getUUID().getLeastSignificantBits() ^ now);
            session = data.createLobby(
                    host.getUUID(), target.getUUID(), organization,
                    teamName(host), seed, now);
        } else if (existing.state()
                == SharedDreamSavedData.DreamState.LOBBY
                && existing.host().equals(host.getUUID())
                && existing.organization().equals(organization)
                && existing.teamName().equals(teamName(host))) {
            session = data.invite(host.getUUID(), target.getUUID(), now);
        } else {
            session = null;
        }
        if (session == null) {
            send(host, "message.lord_of_mysteries.dream.invite_failed",
                    ChatFormatting.RED);
            return 0;
        }
        host.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.dream.invited",
                target.getDisplayName(), organization.getPath())
                .withStyle(ChatFormatting.AQUA));
        target.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.dream.invitation",
                host.getDisplayName(), organization.getPath(),
                SharedDreamPolicy.INVITE_TTL_TICKS / 20L)
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        return 1;
    }

    public static int accept(ServerPlayer player) {
        SharedDreamSavedData data = saved(player);
        SharedDreamSavedData.DreamSession session = data.forPlayer(
                player.getUUID());
        if (session == null || !accessValid(player, session.organization())
                || !teamName(player).equals(session.teamName())) {
            send(player, "message.lord_of_mysteries.dream.accept_failed",
                    ChatFormatting.RED);
            return 0;
        }
        SharedDreamSavedData.DreamSession accepted = data.accept(
                player.getUUID(), player.getServer().overworld().getGameTime());
        if (accepted == null) {
            send(player, "message.lord_of_mysteries.dream.accept_failed",
                    ChatFormatting.RED);
            return 0;
        }
        notifyParticipants(player.getServer(), accepted,
                Component.translatable(
                        "message.lord_of_mysteries.dream.accepted",
                        player.getDisplayName())
                        .withStyle(ChatFormatting.GREEN));
        return 1;
    }

    public static int decline(ServerPlayer player) {
        SharedDreamSavedData data = saved(player);
        SharedDreamSavedData.DreamSession session = data.forPlayer(
                player.getUUID());
        if (session == null || session.state()
                != SharedDreamSavedData.DreamState.LOBBY) {
            send(player, "message.lord_of_mysteries.dream.not_invited",
                    ChatFormatting.RED);
            return 0;
        }
        closeLobby(player.getServer(), data, session,
                DreamCloseReason.DECLINED);
        return 1;
    }

    public static int start(ServerPlayer host) {
        SharedDreamSavedData data = saved(host);
        SharedDreamSavedData.DreamSession session = data.forPlayer(
                host.getUUID());
        if (session == null || session.state()
                != SharedDreamSavedData.DreamState.LOBBY
                || !session.host().equals(host.getUUID())
                || !session.allAccepted() || !hasRitualKit(host)) {
            send(host, "message.lord_of_mysteries.dream.start_failed",
                    ChatFormatting.RED);
            return 0;
        }
        Map<UUID, ServerPlayer> players = onlineParticipants(
                host.getServer(), session);
        if (players.size() != session.participants().size()
                || players.values().stream().anyMatch(player ->
                !validAtStart(player, host, session))) {
            send(host, "message.lord_of_mysteries.dream.start_failed",
                    ChatFormatting.RED);
            return 0;
        }
        ServerLevel dream = host.getServer().getLevel(DREAM_WORLD);
        Vec3 destination = dream == null ? null
                : SharedDreamWorldBuilder.build(
                        dream, session.lane(), session.scenario());
        if (destination == null) {
            send(host, "message.lord_of_mysteries.dream.dimension_missing",
                    ChatFormatting.RED);
            return 0;
        }
        Map<UUID, SharedDreamSavedData.Origin> origins = new HashMap<>();
        players.forEach((id, player) -> origins.put(id,
                new SharedDreamSavedData.Origin(
                        player.level().dimension().location(),
                        player.blockPosition())));
        for (ServerPlayer player : players.values()) {
            if (!teleport(player, dream, destination)) {
                rollback(players, origins);
                data.close(session.id());
                send(host, "message.lord_of_mysteries.dream.start_failed",
                        ChatFormatting.RED);
                return 0;
            }
        }
        SharedDreamSavedData.DreamSession active = data.activate(
                host.getUUID(), origins,
                host.getServer().overworld().getGameTime());
        if (active == null) {
            rollback(players, origins);
            data.close(session.id());
            return 0;
        }
        consumeRitualKit(host);
        players.values().forEach(player -> {
            applyDreamEffects(player);
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.dream.started",
                    Component.translatable(active.scenario().translationKey()),
                    active.participants().size())
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            showCurrentSymbol(player, active);
        });
        return 1;
    }

    public static int status(ServerPlayer player) {
        SharedDreamSavedData data = saved(player);
        SharedDreamSavedData.Recovery recovery = data.recovery(player.getUUID());
        if (recovery != null) {
            send(player, "message.lord_of_mysteries.dream.recovery_pending",
                    ChatFormatting.YELLOW);
            return 1;
        }
        SharedDreamSavedData.DreamSession session = data.forPlayer(
                player.getUUID());
        if (session == null) {
            send(player, "message.lord_of_mysteries.dream.not_active",
                    ChatFormatting.RED);
            return 0;
        }
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.dream.status",
                session.state().id(), session.participants().size(),
                session.step(), SharedDreamPolicy.SYMBOL_STEPS,
                session.coherence(), session.trauma(), session.clueScore())
                .withStyle(ChatFormatting.AQUA));
        if (session.state() == SharedDreamSavedData.DreamState.ACTIVE) {
            showCurrentSymbol(player, session);
        }
        return 1;
    }

    public static int act(ServerPlayer player, String rawAction) {
        DreamAction action = DreamAction.fromId(rawAction);
        SharedDreamSavedData data = saved(player);
        SharedDreamSavedData.DreamSession before = data.forPlayer(
                player.getUUID());
        if (action == null || before == null || before.state()
                != SharedDreamSavedData.DreamState.ACTIVE
                || !DREAM_WORLD.equals(player.level().dimension())) {
            send(player, "message.lord_of_mysteries.dream.action_failed",
                    ChatFormatting.RED);
            return 0;
        }
        SharedDreamSavedData.VoteResult result = data.vote(
                player.getUUID(), action,
                player.getServer().overworld().getGameTime());
        if (result == null) {
            send(player, "message.lord_of_mysteries.dream.already_voted",
                    ChatFormatting.RED);
            return 0;
        }
        notifyParticipants(player.getServer(), result.session(),
                Component.translatable(
                        "message.lord_of_mysteries.dream.vote_recorded",
                        player.getDisplayName(),
                        Component.translatable(action.translationKey()),
                        result.session().votes().size(),
                        result.session().participants().size())
                        .withStyle(ChatFormatting.GRAY));
        if (!result.resolved()) return 1;
        Component resolution = Component.translatable(
                result.outcome().correct()
                        ? "message.lord_of_mysteries.dream.symbol_resolved"
                        : "message.lord_of_mysteries.dream.symbol_failed",
                Component.translatable(before.pendingSymbol().translationKey()),
                Component.translatable(result.selected().translationKey()),
                result.session().coherence(), result.session().trauma())
                .withStyle(result.outcome().correct()
                        ? ChatFormatting.GREEN : ChatFormatting.RED);
        notifyParticipants(player.getServer(), result.session(), resolution);
        if (result.session().state()
                == SharedDreamSavedData.DreamState.COMPLETE) {
            closeActive(player.getServer(), data, result.session(),
                    DreamCloseReason.COMPLETE);
        } else if (result.session().state()
                == SharedDreamSavedData.DreamState.FAILED) {
            closeActive(player.getServer(), data, result.session(),
                    DreamCloseReason.INTERRUPTED);
        } else {
            onlineParticipants(player.getServer(), result.session()).values()
                    .forEach(member -> showCurrentSymbol(
                            member, result.session()));
        }
        return 1;
    }

    public static int leave(ServerPlayer player) {
        SharedDreamSavedData data = saved(player);
        SharedDreamSavedData.DreamSession session = data.forPlayer(
                player.getUUID());
        if (session == null) return recover(player);
        if (session.state() == SharedDreamSavedData.DreamState.LOBBY) {
            closeLobby(player.getServer(), data, session,
                    DreamCloseReason.VOLUNTARY);
        } else {
            closeActive(player.getServer(), data, session,
                    DreamCloseReason.VOLUNTARY);
        }
        return 1;
    }

    public static int recover(ServerPlayer player) {
        SharedDreamSavedData data = saved(player);
        SharedDreamSavedData.Recovery recovery = data.recovery(player.getUUID());
        if (recovery != null) return settleRecovery(player, data, recovery) ? 1 : 0;
        return DREAM_WORLD.equals(player.level().dimension())
                && recoverWithoutRecord(player) ? 1 : 0;
    }

    private static boolean canInvite(
            ServerPlayer host, ServerPlayer target,
            ResourceLocation organization) {
        if (host == target || !host.isAlive() || !target.isAlive()
                || host.isSpectator() || target.isSpectator()
                || host.level() != target.level()
                || host.distanceToSqr(target) > INVITE_DISTANCE_SQUARED
                || DREAM_WORLD.equals(host.level().dimension())
                || SpiritExpeditionService.SPIRIT_WORLD.equals(
                        host.level().dimension())
                || !teamName(host).equals(teamName(target))
                || !accessValid(host, organization)
                || !accessValid(target, organization)
                || SpiritExpeditionSavedData.get(host.serverLevel()).get(
                        host.getUUID()) != null
                || SpiritExpeditionSavedData.get(target.serverLevel()).get(
                        target.getUUID()) != null
                || !hasRitualKit(host)) {
            send(host, "message.lord_of_mysteries.dream.invite_denied",
                    ChatFormatting.RED);
            return false;
        }
        return true;
    }

    private static boolean validAtStart(
            ServerPlayer player, ServerPlayer host,
            SharedDreamSavedData.DreamSession session) {
        return player.isAlive() && !player.isSpectator()
                && player.level() == host.level()
                && player.distanceToSqr(host) <= INVITE_DISTANCE_SQUARED
                && !DREAM_WORLD.equals(player.level().dimension())
                && !SpiritExpeditionService.SPIRIT_WORLD.equals(
                        player.level().dimension())
                && teamName(player).equals(session.teamName())
                && accessValid(player, session.organization())
                && SpiritExpeditionSavedData.get(player.serverLevel()).get(
                        player.getUUID()) == null;
    }

    private static DreamCloseReason validateActive(
            MinecraftServer server,
            SharedDreamSavedData.DreamSession session,
            boolean enforceOffline) {
        for (UUID id : session.participants().keySet()) {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player == null) {
                if (enforceOffline) return DreamCloseReason.DISCONNECTED;
                continue;
            }
            if (!player.isAlive()) return DreamCloseReason.DREAM_DEATH;
            if (!DREAM_WORLD.equals(player.level().dimension())) {
                return DreamCloseReason.WRONG_DIMENSION;
            }
            if (!teamName(player).equals(session.teamName())) {
                return DreamCloseReason.TEAM_CHANGED;
            }
            if (!accessValid(player, session.organization())) {
                return DreamCloseReason.ORGANIZATION_ACCESS_LOST;
            }
        }
        return null;
    }

    private static void closeLobby(
            MinecraftServer server, SharedDreamSavedData data,
            SharedDreamSavedData.DreamSession session,
            DreamCloseReason reason) {
        if (data.close(session.id()) == null) return;
        notifyParticipants(server, session, Component.translatable(
                "message.lord_of_mysteries.dream.closed." + reason.id())
                .withStyle(ChatFormatting.YELLOW));
    }

    private static void closeActive(
            MinecraftServer server, SharedDreamSavedData data,
            SharedDreamSavedData.DreamSession session,
            DreamCloseReason reason) {
        SharedDreamSavedData.DreamSession removed = data.close(session.id());
        if (removed == null) return;
        long now = server.overworld().getGameTime();
        boolean completed = reason == DreamCloseReason.COMPLETE;
        removed.participants().forEach((player, participant) -> {
            if (participant.origin() != null) {
                data.queueRecovery(
                        player, removed.id(), participant.origin(), reason,
                        removed.clueScore(), removed.organization(),
                        completed, now);
            }
        });
        for (ServerPlayer player : onlineParticipants(server, removed).values()) {
            SharedDreamSavedData.Recovery recovery = data.recovery(
                    player.getUUID());
            if (recovery != null) settleRecovery(player, data, recovery);
        }
    }

    private static boolean settleRecovery(
            ServerPlayer player, SharedDreamSavedData data,
            SharedDreamSavedData.Recovery recovery) {
        ReturnTarget target = returnTarget(player, recovery.origin());
        if (target == null
                || !teleport(player, target.level(), target.position())) {
            send(player, "message.lord_of_mysteries.dream.return_failed",
                    ChatFormatting.RED);
            return false;
        }
        if (data.clearRecovery(player.getUUID()) == null) return false;
        applySettlement(player, recovery);
        player.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_RESISTANCE, 100, 1,
                false, false, true));
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.dream.closed."
                        + recovery.reason().id(), recovery.clueScore())
                .withStyle(recovery.completed()
                        ? ChatFormatting.GREEN : ChatFormatting.YELLOW));
        return true;
    }

    private static void applySettlement(
            ServerPlayer player, SharedDreamSavedData.Recovery recovery) {
        PlayerMysteryData playerData = MysteryCapability.get(player);
        if (recovery.completed()) {
            give(player, new ItemStack(ModItems.DREAM_MEMORY.get(),
                    Math.max(1, recovery.clueScore() / 4)));
            give(player, new ItemStack(ModItems.MEMORY_FRAGMENT.get(), 2));
            playerData.orgReputation.merge(
                    recovery.organization(), 1, Integer::sum);
            playerData.knownKnowledge.add(SHARED_DREAM_KNOWLEDGE);
            playerData.knownKnowledge.add(ResourceLocation.fromNamespaceAndPath(
                    ProjectMystery.MOD_ID,
                    "knowledge/dream/" + recovery.sessionId()));
            playerData.insanityPressure = Math.max(
                    0f, playerData.insanityPressure - 4f);
            OrganizationActionSavedData actions =
                    OrganizationActionSavedData.get(
                            player.getServer().overworld());
            var assigned = actions.assignedAction(player.getUUID());
            if (assigned != null
                    && assigned.organization().equals(recovery.organization())
                    && (assigned.type() == OrganizationActionType.HERESY_REVIEW
                    || assigned.type() == OrganizationActionType.HIGH_COUNCIL)) {
                actions.addProgress(player.getUUID(),
                        Math.max(1, recovery.clueScore()));
            }
            playerData.markDirty(PlayerDataSection.KNOWLEDGE);
            playerData.markDirty(PlayerDataSection.SOCIAL);
        } else {
            int trauma = recovery.reason().traumatic() ? 10 : 4;
            playerData.insanityPressure = Math.min(
                    100f, playerData.insanityPressure + trauma);
            playerData.mentalTraumaEndTick = Math.max(
                    playerData.mentalTraumaEndTick,
                    player.level().getGameTime() + trauma * 120L);
            give(player, new ItemStack(ModItems.DREAM_TRAUMA_SHARD.get()));
        }
        playerData.markDirty(PlayerDataSection.CORE);
    }

    private static ReturnTarget returnTarget(
            ServerPlayer player, SharedDreamSavedData.Origin origin) {
        ResourceKey<Level> originKey = ResourceKey.create(
                Registries.DIMENSION, origin.dimension());
        ServerLevel level = player.getServer().getLevel(originKey);
        if (level != null && level.isInWorldBounds(origin.position())
                && level.getWorldBorder().isWithinBounds(origin.position())) {
            level.getChunkAt(origin.position());
            Vec3 safe = TravelMarkerService.findDoorArrival(
                    level, player, origin.position().below());
            if (safe != null) return new ReturnTarget(level, safe);
        }
        ServerLevel overworld = player.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) return null;
        BlockPos spawn = overworld.getSharedSpawnPos();
        if (!overworld.isInWorldBounds(spawn)
                || !overworld.getWorldBorder().isWithinBounds(spawn)) {
            return null;
        }
        overworld.getChunkAt(spawn);
        BlockPos surface = overworld.getHeightmapPos(
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawn);
        return new ReturnTarget(
                overworld, Vec3.atBottomCenterOf(surface.above()));
    }

    private static boolean recoverWithoutRecord(ServerPlayer player) {
        ServerLevel overworld = player.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) return false;
        BlockPos spawn = overworld.getSharedSpawnPos();
        if (!overworld.isInWorldBounds(spawn)
                || !overworld.getWorldBorder().isWithinBounds(spawn)) {
            return false;
        }
        overworld.getChunkAt(spawn);
        BlockPos surface = overworld.getHeightmapPos(
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawn);
        boolean moved = teleport(player, overworld,
                Vec3.atBottomCenterOf(surface.above()));
        if (moved) send(player,
                "message.lord_of_mysteries.dream.recovered",
                ChatFormatting.YELLOW);
        return moved;
    }

    private static void showCurrentSymbol(
            ServerPlayer player,
            SharedDreamSavedData.DreamSession session) {
        if (session.pendingSymbol() == null) return;
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.dream.symbol",
                Component.translatable(session.pendingSymbol().translationKey()),
                session.step() + 1, SharedDreamPolicy.SYMBOL_STEPS)
                .withStyle(ChatFormatting.GOLD));
    }

    private static void notifyParticipants(
            MinecraftServer server,
            SharedDreamSavedData.DreamSession session,
            Component message) {
        session.participants().keySet().stream()
                .map(id -> server.getPlayerList().getPlayer(id))
                .filter(java.util.Objects::nonNull)
                .forEach(player -> player.sendSystemMessage(message));
    }

    private static Map<UUID, ServerPlayer> onlineParticipants(
            MinecraftServer server,
            SharedDreamSavedData.DreamSession session) {
        Map<UUID, ServerPlayer> result = new HashMap<>();
        session.participants().keySet().forEach(id -> {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player != null) result.put(id, player);
        });
        return result;
    }

    private static boolean accessValid(
            ServerPlayer player, ResourceLocation organization) {
        return OrganizationDefinitionManager.get(organization) != null
                && MysteryCapability.get(player).orgReputation.getOrDefault(
                        organization, 0)
                >= SharedDreamPolicy.TRUSTED_REPUTATION;
    }

    private static String teamName(ServerPlayer player) {
        return player.getTeam() == null ? "" : player.getTeam().getName();
    }

    private static ResourceLocation normalizeOrganization(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String value = raw.trim().toLowerCase(Locale.ROOT);
        if (!value.contains(":")) {
            value = ProjectMystery.MOD_ID + ":organization/" + value;
        }
        ResourceLocation id = ResourceLocation.tryParse(value);
        return id != null && id.getPath().startsWith("organization/")
                ? id : null;
    }

    private static boolean hasRitualKit(ServerPlayer player) {
        return find(player, ModItems.DREAM_ENTRY_RIBBON.get()) != null
                && (find(player, ModItems.DREAM_ANCHOR_CLOCK.get()) != null
                || SealedArtifactService.hasUsableSleepingBell(player))
                && find(player, ModItems.WITNESS_STONE.get()) != null;
    }

    private static void consumeRitualKit(ServerPlayer player) {
        if (player.getAbilities().instabuild) return;
        consume(player, ModItems.DREAM_ENTRY_RIBBON.get(), 1);
        if (find(player, ModItems.DREAM_ANCHOR_CLOCK.get()) != null) {
            damage(player, ModItems.DREAM_ANCHOR_CLOCK.get());
        } else {
            SealedArtifactService.useSleepingBellAsDreamAnchor(player);
        }
        damage(player, ModItems.WITNESS_STONE.get());
    }

    private static void rollback(
            Map<UUID, ServerPlayer> players,
            Map<UUID, SharedDreamSavedData.Origin> origins) {
        players.forEach((id, player) -> {
            SharedDreamSavedData.Origin origin = origins.get(id);
            if (origin == null) return;
            ServerLevel level = player.getServer().getLevel(
                    ResourceKey.create(Registries.DIMENSION,
                            origin.dimension()));
            if (level != null) teleport(player, level,
                    Vec3.atBottomCenterOf(origin.position()));
        });
    }

    private static void applyDreamEffects(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(
                MobEffects.NIGHT_VISION, 260, 0,
                false, false, true));
        player.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_RESISTANCE, 100, 1,
                false, false, true));
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

    private static void damage(ServerPlayer player, Item item) {
        ItemStack stack = find(player, item);
        if (stack != null) stack.hurtAndBreak(1, player, broken -> {});
    }

    private static void give(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) player.drop(stack, false);
    }

    private static SharedDreamSavedData saved(ServerPlayer player) {
        return SharedDreamSavedData.get(player.serverLevel());
    }

    private static void send(
            ServerPlayer player, String key, ChatFormatting formatting) {
        player.sendSystemMessage(Component.translatable(key)
                .withStyle(formatting));
    }

    private record ReturnTarget(ServerLevel level, Vec3 position) {}
}
