package top.aurora.lordofmysteries.commission;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Team;

import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerMysteryData;

public final class QuestPartyService {

    private QuestPartyService() {}

    public static List<ServerPlayer> participants(ServerPlayer player,
                                                  QuestChainDefinition chain) {
        Team team = player.getTeam();
        if (team == null || !QuestPartyPolicy.teamEligible(
                chain.sharedProgress(), chain.maximumPartySize(),
                team.getPlayers().size())) return List.of(player);
        List<ServerPlayer> eligible = player.getServer().getPlayerList().getPlayers().stream()
                .filter(candidate -> Objects.equals(team, candidate.getTeam()))
                .filter(candidate -> sameChain(candidate, chain))
                .sorted(Comparator.comparing(candidate -> candidate.getUUID().toString()))
                .toList();
        return eligible.isEmpty() ? List.of(player) : eligible;
    }

    public static boolean isCoordinator(ServerPlayer player,
                                        QuestChainDefinition chain) {
        PlayerMysteryData playerData = MysteryCapability.get(player);
        String objectiveType = playerData.activeQuestStep >= 0
                && playerData.activeQuestStep < chain.steps().size()
                ? chain.steps().get(playerData.activeQuestStep).objective().type() : "";
        List<ServerPlayer> sameStep = participants(player, chain).stream()
                .filter(candidate -> {
                    PlayerMysteryData candidateData = MysteryCapability.get(candidate);
                    return candidateData.activeQuestStep == playerData.activeQuestStep
                            && (!"escort".equals(objectiveType)
                            || !candidateData.escortedReporterUuid.isBlank());
                })
                .toList();
        return player.getUUID().equals(QuestPartyPolicy.coordinator(
                sameStep.stream().map(ServerPlayer::getUUID).toList()));
    }

    public static void assignReporter(ServerPlayer player,
                                      QuestChainDefinition chain,
                                      String reporterUuid) {
        for (ServerPlayer participant : participants(player, chain)) {
            PlayerMysteryData data = MysteryCapability.get(participant);
            if (data.activeQuestStep < chain.steps().size()
                    && "escort".equals(chain.steps().get(
                            data.activeQuestStep).objective().type())) {
                data.escortedReporterUuid = reporterUuid;
            }
        }
        persist(player, chain, true);
    }

    public static String partyKey(ServerPlayer player, QuestChainDefinition chain) {
        return sharedPartyKey(player, chain).orElse("player:" + player.getUUID());
    }

    public static void setDefenseState(ServerPlayer player,
                                       QuestChainDefinition chain,
                                       boolean waveSpawned,
                                       long nextTick) {
        for (ServerPlayer participant : participants(player, chain)) {
            PlayerMysteryData data = MysteryCapability.get(participant);
            data.questDefenseWaveSpawned = waveSpawned;
            data.questDefenseNextTick = nextTick;
        }
        persist(player, chain, true);
    }

    public static void setResolutionState(ServerPlayer player,
                                          QuestChainDefinition chain,
                                          String route, boolean ready) {
        for (ServerPlayer participant : participants(player, chain)) {
            PlayerMysteryData data = MysteryCapability.get(participant);
            data.questResolutionRoute = route;
            data.questResolutionReady = ready;
        }
        persist(player, chain, true);
    }

    public static void registerActive(ServerPlayer player,
                                      QuestChainDefinition chain) {
        persist(player, chain, true);
    }

    public static void persistActive(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        ResourceLocation chainId = ResourceLocation.tryParse(data.activeQuestChainId);
        QuestChainDefinition chain = chainId == null
                ? null : QuestChainDefinitionManager.get(chainId);
        if (chain != null) persist(player, chain, false);
    }

    public static void persistProgress(ServerPlayer player,
                                       QuestChainDefinition chain) {
        persist(player, chain, false);
    }

    public static boolean reconcile(ServerPlayer player) {
        Team team = player.getTeam();
        ServerLevel level = overworld(player);
        if (team == null || level == null) return false;
        String key = teamKey(team);
        QuestPartySavedData savedData = QuestPartySavedData.get(level);
        QuestPartySnapshot snapshot = savedData.snapshot(key).orElse(null);
        if (snapshot == null || !snapshot.hasMember(player.getUUID())
                || snapshot.hasSettled(player.getUUID())) return false;
        QuestChainDefinition chain = chain(snapshot);
        if (chain == null || !QuestPartyPolicy.teamEligible(
                chain.sharedProgress(), chain.maximumPartySize(),
                team.getPlayers().size())) return false;
        PlayerMysteryData data = MysteryCapability.get(player);
        if (!data.activeCommissionId.isBlank() && !snapshot.matches(data)) return false;
        boolean caughtUp = snapshot.applyTo(data, player.getUUID());
        boolean advancedLedger = snapshot.mergeProgress(
                data, player.getUUID(), level.getGameTime());
        if (caughtUp || advancedLedger) savedData.put(key, snapshot);
        if (caughtUp) {
            CommissionService.restoreCommissionPaper(player);
            player.sendSystemMessage(Component.translatable(
                    "command.lord_of_mysteries.party.catchup")
                    .withStyle(ChatFormatting.AQUA));
        }
        return caughtUp;
    }

    public static int joinAndSync(ServerPlayer player) {
        Team team = player.getTeam();
        ServerLevel level = overworld(player);
        if (team == null || level == null) {
            return message(player, "command.lord_of_mysteries.party.no_team");
        }
        String key = teamKey(team);
        QuestPartySavedData savedData = QuestPartySavedData.get(level);
        QuestPartySnapshot snapshot = savedData.snapshot(key).orElse(null);
        if (snapshot == null) {
            return message(player, "command.lord_of_mysteries.party.none");
        }
        QuestChainDefinition chain = chain(snapshot);
        if (chain == null) {
            return message(player, "command.lord_of_mysteries.party.data_unavailable");
        }
        if (!QuestPartyPolicy.teamEligible(chain.sharedProgress(),
                chain.maximumPartySize(), team.getPlayers().size())) {
            return message(player, "command.lord_of_mysteries.party.invalid_size",
                    team.getPlayers().size(), chain.maximumPartySize());
        }
        PlayerMysteryData data = MysteryCapability.get(player);
        if (!data.activeCommissionId.isBlank() && !snapshot.matches(data)) {
            return message(player, "command.lord_of_mysteries.party.conflict");
        }
        if (snapshot.hasSettled(player.getUUID())) {
            return message(player, "command.lord_of_mysteries.party.already_settled");
        }
        CommissionDefinition definition = commission(snapshot);
        if (definition == null || !data.completedCommissions.containsAll(
                definition.prerequisites())) {
            return message(player, "command.lord_of_mysteries.party.prerequisite_missing");
        }
        boolean alreadyMember = snapshot.hasMember(player.getUUID());
        if (!alreadyMember && !snapshot.addMember(
                player.getUUID(), chain.maximumPartySize())) {
            return message(player, "command.lord_of_mysteries.party.full",
                    chain.maximumPartySize());
        }
        boolean caughtUp = snapshot.applyTo(data, player.getUUID());
        snapshot.mergeProgress(data, player.getUUID(), level.getGameTime());
        savedData.put(key, snapshot);
        CommissionService.restoreCommissionPaper(player);
        if (alreadyMember && !caughtUp) {
            return message(player, "command.lord_of_mysteries.party.already_synced");
        }
        player.sendSystemMessage(Component.translatable(
                "command.lord_of_mysteries.party.sync_success",
                Component.translatable(definition.titleKey()))
                .withStyle(ChatFormatting.GREEN));
        return 1;
    }

    public static int showStatus(ServerPlayer player) {
        Team team = player.getTeam();
        ServerLevel level = overworld(player);
        if (team == null || level == null) {
            return message(player, "command.lord_of_mysteries.party.no_team");
        }
        QuestPartySnapshot snapshot = QuestPartySavedData.get(level)
                .snapshot(teamKey(team)).orElse(null);
        if (snapshot == null) {
            return message(player, "command.lord_of_mysteries.party.none");
        }
        QuestChainDefinition chain = chain(snapshot);
        CommissionDefinition definition = commission(snapshot);
        if (chain == null || definition == null) {
            return message(player, "command.lord_of_mysteries.party.data_unavailable");
        }
        if (!QuestPartyPolicy.teamEligible(chain.sharedProgress(),
                chain.maximumPartySize(), team.getPlayers().size())) {
            return message(player, "command.lord_of_mysteries.party.invalid_size",
                    team.getPlayers().size(), chain.maximumPartySize());
        }
        int pending = snapshot.members().size() - snapshot.settledMembers().size();
        long online = snapshot.members().stream()
                .filter(member -> !snapshot.hasSettled(member))
                .filter(member -> player.getServer().getPlayerList().getPlayer(member) != null)
                .count();
        int displayStep = Math.min(snapshot.questStep() + 1, chain.steps().size());
        long ageSeconds = Math.max(0L,
                (level.getGameTime() - snapshot.updatedTick()) / 20L);
        player.sendSystemMessage(Component.translatable(
                "command.lord_of_mysteries.party.status",
                team.getName(), Component.translatable(definition.titleKey()))
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        player.sendSystemMessage(Component.translatable(
                "command.lord_of_mysteries.party.progress",
                displayStep, chain.steps().size(), snapshot.objectiveProgress())
                .withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.translatable(
                "command.lord_of_mysteries.party.members",
                online, pending, snapshot.members().size(), chain.maximumPartySize())
                .withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.translatable(
                "command.lord_of_mysteries.party.updated", ageSeconds)
                .withStyle(ChatFormatting.DARK_GRAY));
        if (!snapshot.hasMember(player.getUUID())) {
            player.sendSystemMessage(Component.translatable(
                    "command.lord_of_mysteries.party.join_hint")
                    .withStyle(ChatFormatting.YELLOW));
        }
        return 1;
    }

    public static void markSettled(ServerPlayer player,
                                   QuestChainDefinition chain) {
        sharedPartyKey(player, chain).ifPresent(key -> {
            ServerLevel level = overworld(player);
            if (level == null) return;
            QuestPartySavedData savedData = QuestPartySavedData.get(level);
            savedData.snapshot(key).ifPresent(snapshot -> {
                if (!snapshot.markSettled(player.getUUID())) return;
                if (snapshot.isFinished()) savedData.remove(key);
                else savedData.put(key, snapshot);
            });
        });
    }

    public static void leave(ServerPlayer player) {
        Team team = player.getTeam();
        ServerLevel level = overworld(player);
        if (team == null || level == null) return;
        String key = teamKey(team);
        QuestPartySavedData savedData = QuestPartySavedData.get(level);
        savedData.snapshot(key).ifPresent(snapshot -> {
            if (!snapshot.removeMember(player.getUUID())) return;
            if (snapshot.isFinished()) savedData.remove(key);
            else savedData.put(key, snapshot);
        });
    }

    private static void persist(ServerPlayer player, QuestChainDefinition chain,
                                boolean authoritative) {
        Optional<String> key = sharedPartyKey(player, chain);
        ServerLevel level = overworld(player);
        if (key.isEmpty() || level == null) return;
        PlayerMysteryData playerData = MysteryCapability.get(player);
        if (!chain.id().toString().equals(playerData.activeQuestChainId)
                || playerData.activeCommissionId.isBlank()) return;
        QuestPartySavedData savedData = QuestPartySavedData.get(level);
        QuestPartySnapshot snapshot = savedData.snapshot(key.get()).orElse(null);
        if (snapshot == null) {
            snapshot = QuestPartySnapshot.create(
                    playerData, player.getUUID(), level.getGameTime());
        } else if (!snapshot.matches(playerData)) {
            return;
        }
        boolean changed = snapshot.mergeProgress(
                playerData, player.getUUID(), level.getGameTime());
        for (ServerPlayer participant : participants(player, chain)) {
            PlayerMysteryData participantData = MysteryCapability.get(participant);
            changed |= snapshot.mergeProgress(participantData,
                    participant.getUUID(), level.getGameTime());
        }
        if (authoritative) {
            changed |= snapshot.updateAuthoritative(playerData, level.getGameTime());
        }
        if (changed || savedData.snapshot(key.get()).isEmpty()) {
            savedData.put(key.get(), snapshot);
        }
    }

    private static Optional<String> sharedPartyKey(ServerPlayer player,
                                                   QuestChainDefinition chain) {
        Team team = player.getTeam();
        if (team == null || !QuestPartyPolicy.teamEligible(
                chain.sharedProgress(), chain.maximumPartySize(),
                team.getPlayers().size())) return Optional.empty();
        return Optional.of(teamKey(team));
    }

    private static String teamKey(Team team) {
        return "team:" + team.getName();
    }

    private static ServerLevel overworld(ServerPlayer player) {
        return player.getServer().getLevel(Level.OVERWORLD);
    }

    private static QuestChainDefinition chain(QuestPartySnapshot snapshot) {
        ResourceLocation id = ResourceLocation.tryParse(snapshot.questChainId());
        return id == null ? null : QuestChainDefinitionManager.get(id);
    }

    private static CommissionDefinition commission(QuestPartySnapshot snapshot) {
        ResourceLocation id = ResourceLocation.tryParse(snapshot.commissionId());
        return id == null ? null : CommissionDefinitionManager.get(id);
    }

    private static int message(ServerPlayer player, String key, Object... arguments) {
        player.sendSystemMessage(Component.translatable(key, arguments)
                .withStyle(ChatFormatting.YELLOW));
        return 0;
    }

    private static boolean sameChain(ServerPlayer player,
                                     QuestChainDefinition chain) {
        return chain.id().toString().equals(
                MysteryCapability.get(player).activeQuestChainId);
    }
}
