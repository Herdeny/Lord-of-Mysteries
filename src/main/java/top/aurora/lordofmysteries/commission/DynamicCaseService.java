package top.aurora.lordofmysteries.commission;

import java.util.Optional;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerDataSection;
import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.registry.ModItems;
import top.aurora.lordofmysteries.world.InvestigationSiteGenerator;
import top.aurora.lordofmysteries.world.InvestigationSiteSavedData;
import top.aurora.lordofmysteries.world.MistCityOutpostSavedData;

public final class DynamicCaseService {

    public static final long DESK_RECONSTRUCTION_COST = 6L;
    public static final float DESK_RECONSTRUCTION_PRESSURE = 3f;
    public static final float WRONG_CONCLUSION_PRESSURE = 6f;
    private static final double FIELD_RANGE = 28d;
    private static final double WITNESS_RANGE = 10d;
    private static final String RECONSIDER_ROUTE = "reconsider";
    private static final String RECOVERED_ROUTE = "recovered";

    private DynamicCaseService() {}

    public enum InvestigationRoute {
        FIELD,
        DESK
    }

    public static DynamicCaseProfile profileFor(
            ServerPlayer player, PlayerMysteryData data) {
        if (!isActive(data)) return null;
        ServerLevel overworld = player.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) return null;
        return DynamicCaseGenerator.generate(
                overworld.getSeed(), data.commissionAcceptedTick);
    }

    public static int show(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        ServerLevel overworld = player.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) return 0;
        boolean active = isActive(data);
        long tick = active ? data.commissionAcceptedTick
                : overworld.getGameTime();
        DynamicCaseProfile profile = DynamicCaseGenerator.generate(
                overworld.getSeed(), tick);
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.dynamic_case.title",
                        profile.instanceId())
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        sendSlot(player, "archetype", profile.archetype());
        sendSlot(player, "subject", profile.subject());
        sendSlot(player, "organization", profile.organization());
        sendSlot(player, "relationship", profile.relationship());
        sendSlot(player, "schedule", profile.schedule());
        sendSlot(player, "motive", active && data.activeQuestStep > 1
                ? profile.motive() : null);
        sendSlot(player, "method", active && data.activeQuestStep > 2
                ? profile.method() : null);
        sendSlot(player, "location", profile.location());
        sendSlot(player, "anomaly", active && data.activeQuestStep > 0
                ? profile.anomaly() : null);
        sendSlot(player, "cover_up", active
                && (data.activeQuestStep >= 4
                        || RECOVERED_ROUTE.equals(data.questResolutionRoute))
                ? profile.coverUp() : null);
        sendSlot(player, "victim_impact", profile.victimImpact());
        sendSlot(player, "evidence_theme", profile.evidenceTheme());
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.dynamic_case.weekly_rotation",
                        profile.caseWeek() + 1L,
                        Component.translatable(profile.organization()
                                .translationKey("organization")))
                .withStyle(ChatFormatting.DARK_AQUA));
        DynamicCaseWeeklyDirective directive =
                DynamicCaseWeeklyDirective.select(
                        overworld.getSeed(), profile.caseWeek(),
                        profile.organization());
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.dynamic_case.directive",
                        Component.translatable(directive.translationKey()))
                .withStyle(ChatFormatting.AQUA));
        expireOrganizationResponse(
                player, data, currentCaseDay(player), true);
        sendContinuityState(player, data);
        sendScheduleState(player, profile);
        if (!active) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.dynamic_case.preview")
                    .withStyle(ChatFormatting.GOLD));
            return 1;
        }
        sendNextStep(player, data, profile);
        return 1;
    }

    public static int investigate(
            ServerPlayer player, InvestigationRoute route) {
        PlayerMysteryData data = MysteryCapability.get(player);
        DynamicCaseProfile profile = profileFor(player, data);
        if (profile == null) return noActive(player);
        int step = data.activeQuestStep;
        if (step < 0 || step > 2) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.dynamic_case.investigate.wrong_stage")
                    .withStyle(ChatFormatting.YELLOW));
            sendNextStep(player, data, profile);
            return 0;
        }
        if (route == InvestigationRoute.FIELD) {
            if (!fieldRequirementMet(player, profile, step)) {
                player.sendSystemMessage(Component.translatable(
                                "command.lord_of_mysteries.dynamic_case.field_unavailable."
                                        + step)
                        .withStyle(ChatFormatting.RED));
                player.sendSystemMessage(Component.translatable(
                                "command.lord_of_mysteries.dynamic_case.desk_fallback")
                        .withStyle(ChatFormatting.GRAY));
                return 0;
            }
        } else if (!applyDeskRecovery(player, data)) {
            return 0;
        }
        String target = switch (step) {
            case 0 -> "dynamic_scene";
            case 1 -> "dynamic_witness";
            default -> "dynamic_records";
        };
        if (!CommissionService.recordObjective(
                player, "custom_callback", target, 1)) {
            return 0;
        }
        if (step == 0) {
            synchronizeEvidenceSamples(player, profile);
        }
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.dynamic_case.evidence_recorded",
                        Component.translatable(
                                "command.lord_of_mysteries.dynamic_case.route."
                                        + route.name().toLowerCase(
                                                java.util.Locale.ROOT)),
                        Component.translatable(clueKey(profile, step)))
                .withStyle(ChatFormatting.GREEN));
        data = MysteryCapability.get(player);
        synchronizePortfolios(player, profile, data.activeQuestStep);
        sendNextStep(player, data, profile);
        InvestigationBoardService.refresh(player);
        return 1;
    }

    public static int collectSceneEvidence(
            ServerPlayer player, BlockPos clickedPosition, ItemStack portfolio) {
        return collectSceneEvidence(
                player, clickedPosition, portfolio, null);
    }

    static int collectSceneEvidence(
            ServerPlayer player,
            BlockPos clickedPosition,
            ItemStack portfolio,
            String evidenceInstanceId) {
        PlayerMysteryData data = MysteryCapability.get(player);
        DynamicCaseProfile profile = profileFor(player, data);
        if (profile == null) return noActive(player);
        if (data.activeQuestStep != 0) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.dynamic_case.investigate.wrong_stage")
                    .withStyle(ChatFormatting.YELLOW));
            return 0;
        }
        if (!DynamicEvidencePortfolioItem.matches(portfolio, profile)) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.dynamic_case.portfolio.outdated")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        if (evidenceInstanceId != null
                && !profile.instanceId().equals(evidenceInstanceId)) {
            player.sendSystemMessage(Component.translatable(
                            "message.lord_of_mysteries.dynamic_case.evidence.outdated")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        if (player.level().dimension() != Level.OVERWORLD) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.dynamic_case.field_unavailable.0")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        ServerLevel overworld = player.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) return 0;
        Optional<BlockPos> target = caseLocationTarget(
                overworld, profile.location());
        if (target.isEmpty()
                || target.get().distSqr(clickedPosition)
                        > FIELD_RANGE * FIELD_RANGE) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.dynamic_case.field_unavailable.0")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        return investigate(player, InvestigationRoute.FIELD);
    }

    public static boolean tryInterviewWitness(
            ServerPlayer player, Villager villager) {
        PlayerMysteryData data = MysteryCapability.get(player);
        DynamicCaseProfile profile = profileFor(player, data);
        if (profile == null || data.activeQuestStep != 1
                || !villager.getTags().contains(
                        expectedWitnessTag(profile.archetype()))) {
            return false;
        }
        return investigate(player, InvestigationRoute.FIELD) > 0;
    }

    public static boolean tryReviewRecords(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (!isActive(data) || data.activeQuestStep != 2) return false;
        return investigate(player, InvestigationRoute.FIELD) > 0;
    }

    public static boolean issuePortfolio(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        DynamicCaseProfile profile = profileFor(player, data);
        if (profile == null) return false;
        DynamicCaseArtifactPolicy.RecoveryPlan plan =
                DynamicCaseArtifactPolicy.plan(
                        data.activeQuestStep,
                        hasPortfolio(player, profile),
                        hasEvidenceSample(player, profile));
        boolean issued = false;
        if (plan.restorePortfolio()) {
            issued |= givePortfolio(
                    player, profile, data.activeQuestStep,
                    "command.lord_of_mysteries.dynamic_case.portfolio.issued");
        }
        if (plan.restoreEvidenceSample()) {
            issued |= giveEvidenceSample(
                    player, profile,
                    "command.lord_of_mysteries.dynamic_case.sample.issued");
        }
        return issued;
    }

    public static int recoverPortfolio(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        DynamicCaseProfile profile = profileFor(player, data);
        if (profile == null) return 0;
        DynamicCaseArtifactPolicy.RecoveryPlan plan =
                DynamicCaseArtifactPolicy.plan(
                        data.activeQuestStep,
                        hasPortfolio(player, profile),
                        hasEvidenceSample(player, profile));
        int restored = 0;
        if (plan.restorePortfolio() && givePortfolio(
                player, profile, data.activeQuestStep,
                "command.lord_of_mysteries.dynamic_case.portfolio.recovered")) {
            restored++;
        }
        if (plan.restoreEvidenceSample() && giveEvidenceSample(
                player, profile,
                "command.lord_of_mysteries.dynamic_case.sample.recovered")) {
            restored++;
        }
        return restored;
    }

    public static void returnPortfolio(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        DynamicCaseProfile profile = profileFor(player, data);
        if (profile == null) return;
        boolean removed = false;
        for (int index = 0;
                index < player.getInventory().getContainerSize(); index++) {
            ItemStack stack = player.getInventory().getItem(index);
            if (DynamicEvidencePortfolioItem.matches(stack, profile)
                    || DynamicCaseEvidenceItem.matches(stack, profile)) {
                stack.shrink(stack.getCount());
                removed = true;
            }
        }
        if (removed) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.dynamic_case.portfolio.returned")
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    public static int conclude(
            ServerPlayer player, DynamicCaseProfile.Conclusion conclusion) {
        PlayerMysteryData data = MysteryCapability.get(player);
        DynamicCaseProfile profile = profileFor(player, data);
        if (profile == null) return noActive(player);
        if (!CommissionService.isCurrentObjective(
                player, "custom_callback", "dynamic_conclusion")) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.dynamic_case.conclusion.not_ready")
                    .withStyle(ChatFormatting.YELLOW));
            return 0;
        }
        if (RECONSIDER_ROUTE.equals(data.questResolutionRoute)) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.dynamic_case.conclusion.recovery_required")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        QuestChainDefinition chain = activeChain(data);
        if (chain == null) return 0;
        if (profile.conclusion() != conclusion) {
            data.insanityPressure = Math.min(100f,
                    data.insanityPressure + WRONG_CONCLUSION_PRESSURE);
            data.markDirty(PlayerDataSection.CORE);
            QuestPartyService.setResolutionState(
                    player, chain, RECONSIDER_ROUTE, false);
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.dynamic_case.conclusion.wrong",
                            Math.round(WRONG_CONCLUSION_PRESSURE))
                    .withStyle(ChatFormatting.RED));
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.dynamic_case.conclusion.recover")
                    .withStyle(ChatFormatting.GOLD));
            InvestigationBoardService.refresh(player);
            return 0;
        }
        QuestPartyService.setResolutionState(
                player, chain, conclusion.id(), true);
        CommissionService.recordObjective(
                player, "custom_callback", "dynamic_conclusion", 1);
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.dynamic_case.conclusion.correct",
                        Component.translatable(conclusion.translationKey("conclusion")))
                .withStyle(ChatFormatting.GREEN));
        InvestigationBoardService.refresh(player);
        return 1;
    }

    public static int recoverConclusion(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        DynamicCaseProfile profile = profileFor(player, data);
        if (profile == null) return noActive(player);
        if (!RECONSIDER_ROUTE.equals(data.questResolutionRoute)) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.dynamic_case.recover.not_needed")
                    .withStyle(ChatFormatting.GRAY));
            return 0;
        }
        if (!InvestigationBoardService.isNearBoard(player)) {
            player.sendSystemMessage(Component.translatable(
                            "screen.lord_of_mysteries.investigation_board.nearby_required")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        QuestChainDefinition chain = activeChain(data);
        if (chain == null) return 0;
        QuestPartyService.setResolutionState(player, chain, RECOVERED_ROUTE, false);
        data.insanityPressure = Math.max(0f, data.insanityPressure - 2f);
        data.markDirty(PlayerDataSection.CORE);
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.dynamic_case.recover.complete",
                        Component.translatable(
                                profile.coverUp().translationKey("cover_up")),
                        Component.translatable(
                                profile.anomaly().translationKey("anomaly")))
                .withStyle(ChatFormatting.AQUA));
        InvestigationBoardService.refresh(player);
        return 1;
    }

    public static boolean isActive(PlayerMysteryData data) {
        return data != null && CommissionService.DYNAMIC_CASE.toString()
                .equals(data.activeCommissionId);
    }

    public static boolean isResolutionState(String route) {
        return route != null && (DynamicCaseProfile.Conclusion.fromId(route) != null
                || RECONSIDER_ROUTE.equals(route)
                || RECOVERED_ROUTE.equals(route));
    }

    public static DynamicCaseFeedbackPolicy.Feedback applyOrganizationFeedback(
            ServerPlayer player,
            PlayerMysteryData data,
            DynamicCaseProfile profile,
            CaseDebriefRecord debrief) {
        ResourceLocation organizationId = ResourceLocation.fromNamespaceAndPath(
                ProjectMystery.MOD_ID, profile.organization().reputationPath());
        int current = data.orgReputation.getOrDefault(organizationId, 0);
        DynamicCaseFeedbackPolicy.Feedback feedback =
                DynamicCaseFeedbackPolicy.evaluate(
                        profile.organization(), debrief.grade(), current);
        data.orgReputation.put(organizationId, feedback.updatedReputation());
        data.markDirty(PlayerDataSection.SOCIAL);
        String tone = feedback.adjustment() > 0 ? "positive"
                : feedback.adjustment() < 0 ? "negative" : "neutral";
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.dynamic_case.feedback." + tone,
                        Component.translatable(profile.organization()
                                .translationKey("organization")),
                        debrief.grade().name(),
                        String.format(java.util.Locale.ROOT, "%+d",
                                feedback.adjustment()),
                        feedback.updatedReputation())
                .withStyle(feedback.adjustment() > 0
                        ? ChatFormatting.GREEN
                        : feedback.adjustment() < 0
                                ? ChatFormatting.RED : ChatFormatting.YELLOW));
        return feedback;
    }

    public static void recordCompletion(
            PlayerMysteryData data,
            DynamicCaseProfile profile,
            CaseDebriefRecord debrief,
            DynamicCaseFeedbackPolicy.Feedback feedback) {
        if (data == null || profile == null || debrief == null
                || feedback == null) {
            throw new IllegalArgumentException(
                    "dynamic case completion data is required");
        }
        DynamicCaseContinuityPolicy.record(
                data.dynamicCaseHistory,
                new DynamicCaseHistoryEntry(
                        profile.caseDay(),
                        profile.caseWeek(),
                        profile.instanceId(),
                        profile.archetype(),
                        profile.subject(),
                        profile.organization(),
                        profile.location(),
                        debrief.grade(),
                        debrief.score(),
                        debrief.completedTick(),
                        feedback.adjustment(),
                        DynamicCaseHistoryEntry.FollowUpStatus.PENDING));
        DynamicCaseRelationshipPolicy.recordCaseResult(
                data.dynamicCaseContactStandings,
                profile.subject(),
                debrief.grade());
        data.markDirty(PlayerDataSection.SOCIAL);
    }

    public static void announceFollowUp(
            ServerPlayer player, PlayerMysteryData data) {
        DynamicCaseContinuityPolicy.latestPending(data.dynamicCaseHistory)
                .ifPresent(entry -> {
                    DynamicCaseContinuityPolicy.Reward reward =
                            DynamicCaseContinuityPolicy.reward(entry.grade());
                    player.sendSystemMessage(Component.translatable(
                                    "command.lord_of_mysteries.dynamic_case.follow_up.available",
                                    Component.translatable(
                                            reward.response().translationKey()),
                                    Component.translatable(entry.organization()
                                            .translationKey("organization")))
                            .withStyle(ChatFormatting.GOLD));
                    sendContactStanding(
                            player, data, entry.subject());
                });
    }

    public static int showHistory(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (data.dynamicCaseHistory.isEmpty()) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.dynamic_case.history.empty")
                    .withStyle(ChatFormatting.GRAY));
            return 0;
        }
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.dynamic_case.history.title",
                        data.dynamicCaseHistory.size())
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        int first = Math.max(0, data.dynamicCaseHistory.size() - 5);
        for (int index = data.dynamicCaseHistory.size() - 1;
                index >= first; index--) {
            DynamicCaseHistoryEntry entry =
                    data.dynamicCaseHistory.get(index);
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.dynamic_case.history.entry",
                            entry.instanceId(),
                            entry.caseWeek() + 1L,
                            Component.translatable(entry.organization()
                                    .translationKey("organization")),
                            Component.translatable(entry.archetype()
                                    .translationKey("archetype")),
                            Component.translatable(entry.location()
                                    .translationKey("location")),
                            entry.grade().name(),
                            entry.score(),
                            Component.translatable(
                                    "dynamic_case.lord_of_mysteries.follow_up_status."
                                            + entry.followUpStatus().id()))
                    .withStyle(entry.followUpStatus()
                            == DynamicCaseHistoryEntry.FollowUpStatus.PENDING
                                    ? ChatFormatting.GOLD
                                    : ChatFormatting.GRAY));
        }
        return 1;
    }

    public static int showContacts(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.dynamic_case.contacts.title")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        for (DynamicCaseProfile.Subject contact
                : DynamicCaseProfile.Subject.values()) {
            sendContactStanding(player, data, contact);
        }
        return 1;
    }

    public static int claimFollowUp(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (!data.activeCommissionId.isBlank()) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.dynamic_case.follow_up.active_case")
                    .withStyle(ChatFormatting.YELLOW));
            return 0;
        }
        long currentDay = currentCaseDay(player);
        expireOrganizationResponse(player, data, currentDay, true);
        if (data.organizationResponseTask != null) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.dynamic_case.response.active")
                    .withStyle(ChatFormatting.YELLOW));
            return 0;
        }
        if (!InvestigationBoardService.isNearBoard(player)) {
            player.sendSystemMessage(Component.translatable(
                            "screen.lord_of_mysteries.investigation_board.nearby_required")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        Optional<DynamicCaseHistoryEntry> pending =
                DynamicCaseContinuityPolicy.latestPending(
                        data.dynamicCaseHistory);
        if (pending.isEmpty()) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.dynamic_case.follow_up.none")
                    .withStyle(ChatFormatting.GRAY));
            return 0;
        }

        DynamicCaseHistoryEntry entry = pending.get();
        ServerLevel overworld = player.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) return 0;
        DynamicCaseContinuityPolicy.Reward reward =
                DynamicCaseContinuityPolicy.reward(entry.grade());
        data.moneyPence = saturatingAdd(data.moneyPence, reward.moneyPence());
        ResourceLocation organizationId =
                ResourceLocation.fromNamespaceAndPath(
                        ProjectMystery.MOD_ID,
                        entry.organization().reputationPath());
        data.orgReputation.put(
                organizationId,
                saturatingAdd(
                        data.orgReputation.getOrDefault(organizationId, 0),
                        reward.reputation()));
        float previousPressure = data.insanityPressure;
        data.insanityPressure = Math.max(
                0f, data.insanityPressure - reward.pressureRecovery());
        for (int index = data.dynamicCaseHistory.size() - 1;
                index >= 0; index--) {
            DynamicCaseHistoryEntry candidate =
                    data.dynamicCaseHistory.get(index);
            if (candidate.instanceId().equals(entry.instanceId())) {
                data.dynamicCaseHistory.set(index,
                        candidate.withFollowUpStatus(
                                DynamicCaseHistoryEntry.FollowUpStatus.CLAIMED));
                break;
            }
        }
        DynamicCaseWeeklyDirective directive =
                DynamicCaseWeeklyDirective.select(
                        overworld.getSeed(),
                        entry.caseWeek(),
                        entry.organization());
        data.organizationResponseTask =
                DynamicCaseResponsePolicy.assign(
                        entry, directive, currentDay);
        data.markDirty(PlayerDataSection.SOCIAL);
        if (Float.compare(previousPressure, data.insanityPressure) != 0) {
            data.markDirty(PlayerDataSection.CORE);
        }
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.dynamic_case.follow_up.claimed",
                        Component.translatable(
                                reward.response().translationKey()),
                        Component.translatable(entry.organization()
                                .translationKey("organization")),
                        CommissionCurrency.format(reward.moneyPence()),
                        reward.reputation(),
                        Math.round(previousPressure - data.insanityPressure))
                .withStyle(ChatFormatting.GREEN));
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.dynamic_case.response.assigned",
                        Component.translatable(directive.translationKey()),
                        Component.translatable(entry.organization()
                                .translationKey("organization")),
                        Component.translatable(entry.subject()
                                .translationKey("subject")),
                        data.organizationResponseTask.expiresDay() + 1L)
                .withStyle(ChatFormatting.GOLD));
        InvestigationBoardService.refresh(player);
        return 1;
    }

    public static int showOrganizationResponse(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        expireOrganizationResponse(
                player, data, currentCaseDay(player), true);
        DynamicCaseResponseTask task = data.organizationResponseTask;
        if (task == null) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.dynamic_case.response.none")
                    .withStyle(ChatFormatting.GRAY));
            return 0;
        }
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.dynamic_case.response.status",
                        Component.translatable(task.directive().translationKey()),
                        Component.translatable(task.organization()
                                .translationKey("organization")),
                        Component.translatable(task.contact()
                                .translationKey("subject")),
                        Component.translatable(
                                "dynamic_case.lord_of_mysteries.response_stage."
                                        + task.stage().id()),
                        task.expiresDay() + 1L)
                .withStyle(task.stage()
                        == DynamicCaseResponseTask.Stage.ASSIGNED
                                ? ChatFormatting.GOLD
                                : ChatFormatting.AQUA));
        sendContactStanding(player, data, task.contact());
        player.sendSystemMessage(Component.translatable(
                        task.stage()
                                == DynamicCaseResponseTask.Stage.ASSIGNED
                                        ? "command.lord_of_mysteries.dynamic_case.response.next_briefing"
                                        : "command.lord_of_mysteries.dynamic_case.response.next_submit")
                .withStyle(ChatFormatting.GRAY));
        return 1;
    }

    public static boolean tryBriefOrganizationResponse(
            ServerPlayer player,
            DynamicCaseProfile.Organization organization) {
        PlayerMysteryData data = MysteryCapability.get(player);
        expireOrganizationResponse(
                player, data, currentCaseDay(player), true);
        DynamicCaseResponseTask task = data.organizationResponseTask;
        if (task == null || task.organization() != organization) {
            return false;
        }
        if (task.stage() == DynamicCaseResponseTask.Stage.BRIEFED) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.dynamic_case.response.already_briefed")
                    .withStyle(ChatFormatting.GRAY));
            return true;
        }
        data.organizationResponseTask = task.withStage(
                DynamicCaseResponseTask.Stage.BRIEFED);
        data.markDirty(PlayerDataSection.SOCIAL);
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.dynamic_case.response.briefed",
                        Component.translatable(task.directive().translationKey()),
                        Component.translatable(task.contact()
                                .translationKey("subject")))
                .withStyle(ChatFormatting.AQUA));
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.dynamic_case.response.next_submit")
                .withStyle(ChatFormatting.GRAY));
        return true;
    }

    public static int submitOrganizationResponse(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        expireOrganizationResponse(
                player, data, currentCaseDay(player), true);
        DynamicCaseResponseTask task = data.organizationResponseTask;
        if (task == null) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.dynamic_case.response.none")
                    .withStyle(ChatFormatting.GRAY));
            return 0;
        }
        if (task.stage() != DynamicCaseResponseTask.Stage.BRIEFED) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.dynamic_case.response.briefing_required")
                    .withStyle(ChatFormatting.YELLOW));
            return 0;
        }
        if (!InvestigationBoardService.isNearBoard(player)) {
            player.sendSystemMessage(Component.translatable(
                            "screen.lord_of_mysteries.investigation_board.nearby_required")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        DynamicCaseResponsePolicy.Reward reward =
                DynamicCaseResponsePolicy.reward(task.directive());
        data.moneyPence = saturatingAdd(
                data.moneyPence, reward.moneyPence());
        ResourceLocation organizationId = organizationId(task.organization());
        data.orgReputation.put(
                organizationId,
                saturatingAdd(
                        data.orgReputation.getOrDefault(organizationId, 0),
                        reward.reputation()));
        int standing = DynamicCaseRelationshipPolicy.adjust(
                data.dynamicCaseContactStandings,
                task.contact(),
                reward.contactStanding());
        data.organizationResponseTask = null;
        data.markDirty(PlayerDataSection.SOCIAL);
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.dynamic_case.response.completed",
                        Component.translatable(task.directive().translationKey()),
                        CommissionCurrency.format(reward.moneyPence()),
                        reward.reputation(),
                        reward.contactStanding())
                .withStyle(ChatFormatting.GREEN));
        sendContactStanding(player, task.contact(), standing);
        InvestigationBoardService.refresh(player);
        return 1;
    }

    public static int abandonOrganizationResponse(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        expireOrganizationResponse(
                player, data, currentCaseDay(player), true);
        DynamicCaseResponseTask task = data.organizationResponseTask;
        if (task == null) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.dynamic_case.response.none")
                    .withStyle(ChatFormatting.GRAY));
            return 0;
        }
        if (!InvestigationBoardService.isNearBoard(player)) {
            player.sendSystemMessage(Component.translatable(
                            "screen.lord_of_mysteries.investigation_board.nearby_required")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        int standing = DynamicCaseRelationshipPolicy.adjust(
                data.dynamicCaseContactStandings,
                task.contact(), -1);
        data.organizationResponseTask = null;
        data.markDirty(PlayerDataSection.SOCIAL);
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.dynamic_case.response.abandoned",
                        Component.translatable(task.contact()
                                .translationKey("subject")))
                .withStyle(ChatFormatting.YELLOW));
        sendContactStanding(player, task.contact(), standing);
        InvestigationBoardService.refresh(player);
        return 1;
    }

    private static boolean applyDeskRecovery(
            ServerPlayer player, PlayerMysteryData data) {
        if (!InvestigationBoardService.isNearBoard(player)) {
            player.sendSystemMessage(Component.translatable(
                            "screen.lord_of_mysteries.investigation_board.nearby_required")
                    .withStyle(ChatFormatting.RED));
            return false;
        }
        if (data.moneyPence >= DESK_RECONSTRUCTION_COST) {
            data.moneyPence -= DESK_RECONSTRUCTION_COST;
            data.markDirty(PlayerDataSection.SOCIAL);
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.dynamic_case.desk_paid",
                            CommissionCurrency.format(DESK_RECONSTRUCTION_COST))
                    .withStyle(ChatFormatting.YELLOW));
        } else {
            data.insanityPressure = Math.min(100f,
                    data.insanityPressure + DESK_RECONSTRUCTION_PRESSURE);
            data.markDirty(PlayerDataSection.CORE);
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.dynamic_case.desk_pressure",
                            Math.round(DESK_RECONSTRUCTION_PRESSURE))
                    .withStyle(ChatFormatting.YELLOW));
        }
        return true;
    }

    private static boolean fieldRequirementMet(
            ServerPlayer player, DynamicCaseProfile profile, int step) {
        return switch (step) {
            case 0 -> nearCaseLocation(player, profile.location());
            case 1 -> nearWitness(player, profile.archetype());
            case 2 -> hasNewspaper(player);
            default -> false;
        };
    }

    private static boolean nearCaseLocation(
            ServerPlayer player, DynamicCaseProfile.CaseLocation location) {
        if (player.level().dimension() != Level.OVERWORLD) return false;
        return caseLocationTarget(player.serverLevel(), location)
                .filter(position -> position.distToCenterSqr(player.position())
                        <= FIELD_RANGE * FIELD_RANGE)
                .isPresent();
    }

    static Optional<BlockPos> caseLocationTarget(
            ServerLevel level, DynamicCaseProfile.CaseLocation location) {
        return switch (location) {
            case MIST_CITY_OUTPOST -> MistCityOutpostSavedData.get(level).outpost();
            case ABANDONED_CHURCH -> Optional.of(
                    InvestigationSiteSavedData.get(level).church()
                            .orElseGet(() -> InvestigationSiteGenerator.churchTarget(level)));
            case CULTIST_CAMP -> Optional.of(
                    InvestigationSiteSavedData.get(level).cultistCamp()
                            .orElseGet(() -> InvestigationSiteGenerator.cultistCampTarget(level)));
            case OCCULTIST_HUT -> Optional.of(
                    InvestigationSiteSavedData.get(level).occultistHut()
                            .orElseGet(() -> InvestigationSiteGenerator.occultistHutTarget(level)));
        };
    }

    private static boolean nearWitness(
            ServerPlayer player, DynamicCaseProfile.Archetype archetype) {
        String tag = expectedWitnessTag(archetype);
        return !player.level().getEntitiesOfClass(
                Villager.class,
                player.getBoundingBox().inflate(WITNESS_RANGE),
                villager -> villager.isAlive() && villager.getTags().contains(tag))
                .isEmpty();
    }

    static String expectedWitnessTag(
            DynamicCaseProfile.Archetype archetype) {
        return switch (archetype) {
            case MISSING_PERSON -> InvestigationNpcHandler.PRESS_CLERK_TAG;
            case ANOMALOUS_ITEM -> InvestigationNpcHandler.OCCULT_APPRAISER_TAG;
            case OCCULT_CRIME -> InvestigationNpcHandler.NIGHTHAWK_CONTACT_TAG;
        };
    }

    private static boolean hasNewspaper(ServerPlayer player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(ModItems.NEWSPAPER.get())) return true;
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (stack.is(ModItems.NEWSPAPER.get())) return true;
        }
        return false;
    }

    private static QuestChainDefinition activeChain(PlayerMysteryData data) {
        ResourceLocation id = ResourceLocation.tryParse(data.activeQuestChainId);
        return id == null ? null : QuestChainDefinitionManager.get(id);
    }

    private static void synchronizePortfolios(
            ServerPlayer player, DynamicCaseProfile profile, int collectedStage) {
        QuestChainDefinition chain = activeChain(MysteryCapability.get(player));
        if (chain == null) return;
        for (ServerPlayer participant : QuestPartyService.participants(player, chain)) {
            for (int index = 0;
                    index < participant.getInventory().getContainerSize(); index++) {
                ItemStack stack = participant.getInventory().getItem(index);
                if (DynamicEvidencePortfolioItem.matches(stack, profile)) {
                    DynamicEvidencePortfolioItem.bind(
                            stack, profile, collectedStage);
                }
            }
        }
    }

    private static void synchronizeEvidenceSamples(
            ServerPlayer player, DynamicCaseProfile profile) {
        QuestChainDefinition chain = activeChain(MysteryCapability.get(player));
        if (chain == null) return;
        for (ServerPlayer participant : QuestPartyService.participants(player, chain)) {
            if (!hasEvidenceSample(participant, profile)) {
                giveEvidenceSample(participant, profile,
                        "command.lord_of_mysteries.dynamic_case.sample.collected");
            }
        }
    }

    private static boolean hasPortfolio(
            ServerPlayer player, DynamicCaseProfile profile) {
        for (int index = 0;
                index < player.getInventory().getContainerSize(); index++) {
            if (DynamicEvidencePortfolioItem.matches(
                    player.getInventory().getItem(index), profile)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasEvidenceSample(
            ServerPlayer player, DynamicCaseProfile profile) {
        for (int index = 0;
                index < player.getInventory().getContainerSize(); index++) {
            if (DynamicCaseEvidenceItem.matches(
                    player.getInventory().getItem(index), profile)) {
                return true;
            }
        }
        return false;
    }

    private static boolean givePortfolio(
            ServerPlayer player,
            DynamicCaseProfile profile,
            int collectedStage,
            String messageKey) {
        ItemStack portfolio = new ItemStack(
                ModItems.DYNAMIC_EVIDENCE_PORTFOLIO.get());
        DynamicEvidencePortfolioItem.bind(
                portfolio, profile, collectedStage);
        if (!QuestItemDelivery.give(player, portfolio)) return false;
        player.sendSystemMessage(Component.translatable(
                        messageKey, profile.instanceId())
                .withStyle(ChatFormatting.AQUA));
        return true;
    }

    private static boolean giveEvidenceSample(
            ServerPlayer player,
            DynamicCaseProfile profile,
            String messageKey) {
        ItemStack sample = DynamicCaseEvidenceItem.create(profile);
        if (!QuestItemDelivery.give(player, sample)) return false;
        player.sendSystemMessage(Component.translatable(
                        messageKey,
                        Component.translatable(profile.evidenceTheme()
                                .translationKey("evidence_theme")),
                        profile.instanceId())
                .withStyle(ChatFormatting.AQUA));
        return true;
    }

    private static void sendSlot(
            ServerPlayer player, String slot, DynamicCaseProfile.SlotOption value) {
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.dynamic_case.slot." + slot,
                        value == null
                                ? Component.translatable(
                                        "command.lord_of_mysteries.dynamic_case.slot.hidden")
                                : Component.translatable(value.translationKey(slot)))
                .withStyle(ChatFormatting.GRAY));
    }

    private static void sendNextStep(
            ServerPlayer player,
            PlayerMysteryData data,
            DynamicCaseProfile profile) {
        String key;
        Object[] arguments = new Object[0];
        if (data.activeQuestStep == 0) {
            key = "command.lord_of_mysteries.dynamic_case.next.scene";
            arguments = new Object[]{Component.translatable(
                    profile.location().translationKey("location"))};
        } else if (data.activeQuestStep == 1) {
            key = "command.lord_of_mysteries.dynamic_case.next.witness";
            arguments = new Object[]{Component.translatable(
                    witnessKey(profile.archetype()))};
        } else if (data.activeQuestStep == 2) {
            key = "command.lord_of_mysteries.dynamic_case.next.records";
        } else if (data.activeQuestStep == 3
                && RECONSIDER_ROUTE.equals(data.questResolutionRoute)) {
            key = "command.lord_of_mysteries.dynamic_case.next.recover";
        } else if (data.activeQuestStep == 3) {
            key = "command.lord_of_mysteries.dynamic_case.next.conclude";
        } else {
            key = "command.lord_of_mysteries.dynamic_case.next.return";
        }
        player.sendSystemMessage(Component.translatable(key, arguments)
                .withStyle(ChatFormatting.GOLD));
    }

    private static void sendScheduleState(
            ServerPlayer player, DynamicCaseProfile profile) {
        ServerLevel overworld =
                player.getServer().getLevel(Level.OVERWORLD);
        DynamicCaseSchedulePolicy.State state =
                DynamicCaseSchedulePolicy.state(
                        profile, overworld == null
                                ? player.level().getDayTime()
                                : overworld.getDayTime());
        String key = state.observationOpen()
                ? "command.lord_of_mysteries.dynamic_case.time.open"
                : "command.lord_of_mysteries.dynamic_case.time.wait";
        Object[] arguments = state.observationOpen()
                ? new Object[]{
                        Component.translatable(state.currentPeriod()
                                .translationKey("day_period")),
                        Component.translatable(profile.schedule()
                                .translationKey("schedule"))}
                : new Object[]{
                        Component.translatable(state.currentPeriod()
                                .translationKey("day_period")),
                        Component.translatable(profile.schedule()
                                .translationKey("schedule")),
                        state.minutesUntilOpen()};
        player.sendSystemMessage(Component.translatable(key, arguments)
                .withStyle(state.observationOpen()
                        ? ChatFormatting.GREEN : ChatFormatting.GRAY));
    }

    private static void sendContinuityState(
            ServerPlayer player, PlayerMysteryData data) {
        if (data.dynamicCaseHistory.isEmpty()) return;
        DynamicCaseHistoryEntry latest = data.dynamicCaseHistory.get(
                data.dynamicCaseHistory.size() - 1);
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.dynamic_case.continuity.previous",
                        latest.instanceId(),
                        latest.grade().name(),
                        Component.translatable(latest.organization()
                                .translationKey("organization")))
                .withStyle(ChatFormatting.GRAY));
        if (latest.followUpStatus()
                == DynamicCaseHistoryEntry.FollowUpStatus.PENDING) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.dynamic_case.follow_up.reminder")
                    .withStyle(ChatFormatting.GOLD));
        }
        if (data.organizationResponseTask != null) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.dynamic_case.response.reminder",
                            Component.translatable(
                                    data.organizationResponseTask.directive()
                                            .translationKey()))
                    .withStyle(ChatFormatting.AQUA));
        }
    }

    private static void sendContactStanding(
            ServerPlayer player,
            PlayerMysteryData data,
            DynamicCaseProfile.Subject contact) {
        sendContactStanding(
                player, contact,
                data.dynamicCaseContactStandings.getOrDefault(contact, 0));
    }

    private static void sendContactStanding(
            ServerPlayer player,
            DynamicCaseProfile.Subject contact,
            int standing) {
        DynamicCaseRelationshipPolicy.Attitude attitude =
                DynamicCaseRelationshipPolicy.attitude(standing);
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.dynamic_case.contacts.entry",
                        Component.translatable(
                                contact.translationKey("subject")),
                        standing,
                        Component.translatable(attitude.translationKey()))
                .withStyle(standing > 2
                        ? ChatFormatting.GREEN
                        : standing < -2
                                ? ChatFormatting.RED : ChatFormatting.GRAY));
    }

    private static void expireOrganizationResponse(
            ServerPlayer player,
            PlayerMysteryData data,
            long currentDay,
            boolean announce) {
        DynamicCaseResponseTask task = data.organizationResponseTask;
        if (!DynamicCaseResponsePolicy.isExpired(task, currentDay)) return;
        int standing = DynamicCaseRelationshipPolicy.adjust(
                data.dynamicCaseContactStandings,
                task.contact(), -1);
        data.organizationResponseTask = null;
        data.markDirty(PlayerDataSection.SOCIAL);
        if (!announce) return;
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.dynamic_case.response.expired",
                        Component.translatable(task.contact()
                                .translationKey("subject")))
                .withStyle(ChatFormatting.RED));
        sendContactStanding(player, task.contact(), standing);
    }

    private static long currentCaseDay(ServerPlayer player) {
        ServerLevel overworld = player.getServer().getLevel(Level.OVERWORLD);
        long gameTime = overworld == null
                ? player.level().getGameTime()
                : overworld.getGameTime();
        return Math.floorDiv(
                Math.max(0L, gameTime),
                DynamicCaseGenerator.TICKS_PER_CASE_DAY);
    }

    private static ResourceLocation organizationId(
            DynamicCaseProfile.Organization organization) {
        return ResourceLocation.fromNamespaceAndPath(
                ProjectMystery.MOD_ID,
                organization.reputationPath());
    }

    private static String clueKey(DynamicCaseProfile profile, int step) {
        return switch (step) {
            case 0 -> profile.anomaly().translationKey("anomaly");
            case 1 -> profile.motive().translationKey("motive");
            default -> profile.method().translationKey("method");
        };
    }

    private static String witnessKey(DynamicCaseProfile.Archetype archetype) {
        return switch (archetype) {
            case MISSING_PERSON -> "entity.lord_of_mysteries.press_clerk";
            case ANOMALOUS_ITEM -> "entity.lord_of_mysteries.occult_appraiser";
            case OCCULT_CRIME -> "entity.lord_of_mysteries.nighthawk_contact";
        };
    }

    private static int noActive(ServerPlayer player) {
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.dynamic_case.no_active")
                .withStyle(ChatFormatting.GRAY));
        return 0;
    }

    private static long saturatingAdd(long current, long amount) {
        if (amount <= 0L) return current;
        return current > Long.MAX_VALUE - amount
                ? Long.MAX_VALUE : current + amount;
    }

    private static int saturatingAdd(int current, int amount) {
        if (amount <= 0) return current;
        return current > Integer.MAX_VALUE - amount
                ? Integer.MAX_VALUE : current + amount;
    }
}
