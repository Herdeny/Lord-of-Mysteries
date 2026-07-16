package top.aurora.lordofmysteries.commission;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Team;

import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerMysteryData;

public final class QuestPartyService {

    private QuestPartyService() {}

    public static List<ServerPlayer> participants(ServerPlayer player,
                                                  QuestChainDefinition chain) {
        Team team = player.getTeam();
        if (!chain.sharedProgress() || team == null) return List.of(player);
        List<ServerPlayer> eligible = player.getServer().getPlayerList().getPlayers().stream()
                .filter(candidate -> Objects.equals(team, candidate.getTeam()))
                .filter(candidate -> sameChain(candidate, chain))
                .sorted(Comparator.comparing(candidate -> candidate.getUUID().toString()))
                .toList();
        if (!QuestPartyPolicy.sharingAllowed(true, chain.maximumPartySize(),
                eligible.size())) return List.of(player);
        return eligible;
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
    }

    public static String partyKey(ServerPlayer player, QuestChainDefinition chain) {
        List<ServerPlayer> shared = participants(player, chain);
        if (shared.size() > 1 && player.getTeam() != null) {
            return "team:" + player.getTeam().getName();
        }
        return "player:" + player.getUUID();
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
    }

    private static boolean sameChain(ServerPlayer player,
                                     QuestChainDefinition chain) {
        return chain.id().toString().equals(
                MysteryCapability.get(player).activeQuestChainId);
    }
}
