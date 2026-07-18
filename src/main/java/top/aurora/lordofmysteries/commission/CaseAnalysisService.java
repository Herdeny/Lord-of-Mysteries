package top.aurora.lordofmysteries.commission;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerMysteryData;

public final class CaseAnalysisService {

    private static final List<ResourceLocation> CASE_ORDER = List.of(
            CommissionService.LOST_CAT,
            CommissionService.MISSING_SQUAD,
            CommissionService.COUNTERFEIT_FORMULA);

    private CaseAnalysisService() {}

    public static int showAnalysis(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        CaseEvidenceView evidence = CaseEvidenceView.from(
                data, FormulaAppraisalService.evidence(player));
        if (evidence.commissionId().isBlank()) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.case.analysis.no_active")
                    .withStyle(ChatFormatting.GRAY));
            return 0;
        }
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.case.analysis.title",
                        Component.translatable(evidence.caseTitleKey()))
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.case.analysis.summary",
                        evidence.confidence(), evidence.discovered(), evidence.total(),
                        evidence.confirmed(), evidence.suspicious(), evidence.missing())
                .withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.translatable(
                        "screen.lord_of_mysteries.analysis.stage."
                                + evidence.analysisStage().name().toLowerCase(
                                        java.util.Locale.ROOT))
                .withStyle(stageColor(evidence.analysisStage())));
        player.sendSystemMessage(Component.translatable(
                        "screen.lord_of_mysteries.analysis.theory",
                        Component.translatable(evidence.theoryKey()))
                .withStyle(ChatFormatting.AQUA));
        player.sendSystemMessage(Component.translatable(
                        "screen.lord_of_mysteries.analysis.next_action",
                        Component.translatable(evidence.nextActionKey()))
                .withStyle(ChatFormatting.GOLD));
        for (CaseEvidenceView.Relation relation : evidence.relations()) {
            if (relation.state() == EvidenceState.MISSING) continue;
            player.sendSystemMessage(Component.literal("• ")
                    .append(Component.translatable(
                            "screen.lord_of_mysteries.analysis.relation_kind."
                                    + relation.kind().name().toLowerCase(
                                            java.util.Locale.ROOT)))
                    .append(Component.literal(" · "))
                    .append(Component.translatable(relation.titleKey()))
                    .withStyle(relation.state() == EvidenceState.SUSPICIOUS
                            ? ChatFormatting.RED : ChatFormatting.DARK_AQUA));
        }
        return 1;
    }

    public static int showArchive(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.case.archive.title")
                .withStyle(ChatFormatting.GOLD));
        int completed = 0;
        for (ResourceLocation caseId : CASE_ORDER) {
            CommissionDefinition definition = CommissionDefinitionManager.get(caseId);
            if (definition == null || !data.completedCommissions.contains(caseId)) continue;
            completed++;
            player.sendSystemMessage(Component.literal("• ")
                    .append(Component.translatable(definition.titleKey()))
                    .append(Component.literal(" · "))
                    .append(Component.translatable(
                            "command.lord_of_mysteries.case.archive.closed"))
                    .withStyle(ChatFormatting.GREEN));
        }
        if (completed == 0) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.case.archive.empty")
                    .withStyle(ChatFormatting.GRAY));
        }
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.case.archive.progress",
                        completed, CASE_ORDER.size())
                .withStyle(ChatFormatting.DARK_GRAY));
        return 1;
    }

    private static ChatFormatting stageColor(CaseAnalysisStage stage) {
        return switch (stage) {
            case READY -> ChatFormatting.GREEN;
            case CORRELATING -> ChatFormatting.AQUA;
            case COLLECTING -> ChatFormatting.YELLOW;
            case NO_CASE -> ChatFormatting.GRAY;
        };
    }
}
