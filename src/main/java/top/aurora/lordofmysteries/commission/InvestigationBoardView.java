package top.aurora.lordofmysteries.commission;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;

import top.aurora.lordofmysteries.player.PlayerMysteryData;

public record InvestigationBoardView(
        long balancePence,
        String activeCommissionId,
        String activeTitleKey,
        String activeGuidanceKey,
        int activeStep,
        int activeStepCount,
        int activeProgress,
        int activeTarget,
        List<Entry> entries,
        CaseEvidenceView evidence) {

    public InvestigationBoardView {
        entries = List.copyOf(entries);
    }

    public static InvestigationBoardView from(
            PlayerMysteryData data,
            Map<ResourceLocation, CommissionDefinition> definitions,
            long gameTime,
            CaseEvidenceView evidence) {
        String activeTitleKey = "";
        String activeGuidanceKey = "";
        int activeStep = 0;
        int activeStepCount = 0;
        int activeProgress = 0;
        int activeTarget = 0;
        ResourceLocation activeId = ResourceLocation.tryParse(data.activeCommissionId);
        CommissionDefinition activeDefinition = activeId == null
                ? null : definitions.get(activeId);
        QuestChainDefinition activeChain = activeDefinition == null
                ? null : QuestChainDefinitionManager.get(activeDefinition.questChain());
        if (activeDefinition != null && activeChain != null
                && data.activeQuestStep >= 0
                && data.activeQuestStep < activeChain.steps().size()) {
            QuestChainDefinition.Step step = activeChain.steps().get(data.activeQuestStep);
            activeTitleKey = activeDefinition.titleKey();
            activeGuidanceKey = step.guidanceKey();
            activeStep = data.activeQuestStep + 1;
            activeStepCount = activeChain.steps().size();
            activeProgress = data.questObjectiveProgress;
            activeTarget = step.objective().count();
        }
        List<Entry> entries = definitions.values().stream()
                .sorted(Comparator.comparing(definition -> definition.id().toString()))
                .map(definition -> new Entry(
                        definition.id().toString(),
                        definition.titleKey(),
                        definition.summaryKey(),
                        definition.reward().pence(),
                        CommissionService.availability(data, definition, gameTime)))
                .toList();
        return new InvestigationBoardView(
                data.moneyPence,
                data.activeCommissionId,
                activeTitleKey,
                activeGuidanceKey,
                activeStep,
                activeStepCount,
                activeProgress,
                activeTarget,
                entries,
                evidence);
    }

    public record Entry(
            String id,
            String titleKey,
            String summaryKey,
            long rewardPence,
            CommissionBoardState state) {}
}
