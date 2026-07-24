package top.aurora.lordofmysteries.commission;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.registry.ModItems;
import top.aurora.lordofmysteries.world.AbandonedCampGenerator;
import top.aurora.lordofmysteries.world.CampGenerationSavedData;
import top.aurora.lordofmysteries.world.InvestigationSiteSavedData;
import top.aurora.lordofmysteries.world.MistCityOutpostSavedData;

public final class CommissionService {

    public static final ResourceLocation LOST_CAT = id("commission/lost_cat");
    public static final ResourceLocation MISSING_SQUAD =
            id("commission/missing_investigation_squad");
    public static final ResourceLocation COUNTERFEIT_FORMULA =
            id("commission/counterfeit_formula");
    public static final ResourceLocation DYNAMIC_CASE =
            id("commission/dynamic_case_rotation");
    private static final String RESCUE_GUARD_TAG = "lom_rescue_guard";
    private static final String RESCUE_PARTY_TAG = "lom_rescue_party";

    private CommissionService() {}

    public static int interactBoard(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (!data.activeCommissionId.isBlank()) {
            if (DYNAMIC_CASE.toString().equals(data.activeCommissionId)
                    && isReadyToSettle(data)) {
                return settle(player);
            }
            if (LOST_CAT.toString().equals(data.activeCommissionId)) {
                recordObjective(player, "talk_npc", "nighthawk_contact", 1);
                if (isReadyToSettle(data)) return settle(player);
            }
            return showStatus(player);
        }
        ResourceLocation recommended = recommendedCommission(data);
        if (recommended == null) return list(player);
        CommissionDefinition recommendation = CommissionDefinitionManager.get(
                recommended);
        if (recommendation == null
                || (!recommendation.repeatable()
                        && data.completedCommissions.contains(recommended))) {
            return list(player);
        }
        int accepted = accept(player, recommended);
        if (accepted > 0) {
            recordObjective(player, "enter_structure", "mist_city_outpost", 1);
        }
        return accepted;
    }

    public static int talkPressClerk(ServerPlayer player) {
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.npc.press_clerk")
                .withStyle(ChatFormatting.GRAY));
        return recordObjective(player, "talk_npc", "press_clerk", 1) ? 1 : 0;
    }

    public static int interactContact(ServerPlayer player) {
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.npc.nighthawk_contact")
                .withStyle(ChatFormatting.DARK_PURPLE));
        PlayerMysteryData data = MysteryCapability.get(player);
        recordObjective(player, "talk_npc", "nighthawk_contact", 1);
        return isReadyToSettle(data) ? settle(player) : showStatus(player);
    }

    public static int interactOccultAppraiser(ServerPlayer player) {
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.npc.occult_appraiser")
                .withStyle(ChatFormatting.DARK_PURPLE));
        PlayerMysteryData data = MysteryCapability.get(player);
        if (!COUNTERFEIT_FORMULA.toString().equals(data.activeCommissionId)) {
            return 0;
        }
        recordObjective(player, "talk_npc", "occult_appraiser", 1);
        data = MysteryCapability.get(player);
        if (data.activeQuestStep >= 2 && data.activeQuestStep <= 4
                && !FormulaAppraisalService.hasDossier(player)) {
            giveItem(player, FormulaAppraisalService.createDossier(player));
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.formula.dossier_received")
                    .withStyle(ChatFormatting.GOLD));
        }
        if (isCurrentObjective(player, "pickup",
                "lord_of_mysteries:sealed_formula_dossier")) {
            recordObjective(player, "pickup",
                    "lord_of_mysteries:sealed_formula_dossier", 1);
        }
        data = MysteryCapability.get(player);
        if (isReadyToSettle(data)) {
            return settle(player);
        }
        return showStatus(player);
    }

    public static int chooseRescueApproach(ServerPlayer player, String route) {
        if (!isCurrentObjective(player, "rescue", "missing_reporter")) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.quest.approach.wrong_stage")
                    .withStyle(ChatFormatting.GRAY));
            return 0;
        }
        PlayerMysteryData data = MysteryCapability.get(player);
        if (!data.questResolutionRoute.isBlank()) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.quest.approach.locked",
                    Component.translatable(
                            "message.lord_of_mysteries.quest.approach."
                                    + data.questResolutionRoute))
                    .withStyle(ChatFormatting.YELLOW));
            return 0;
        }
        QuestChainDefinition chain = activeChain(data);
        ServerLevel level = player.getServer().getLevel(Level.OVERWORLD);
        if (chain == null || level == null) return 0;
        boolean atCamp = InvestigationSiteSavedData.get(level).cultistCamp()
                .filter(position -> near(player, position, 32d)).isPresent();
        if (!atCamp) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.quest.approach.not_at_camp")
                    .withStyle(ChatFormatting.YELLOW));
            return 0;
        }
        switch (route) {
            case "assault" -> {
                QuestPartyService.setResolutionState(player, chain, route, false);
                spawnRescueGuards(level, player, chain);
            }
            case "stealth" -> {
                if (!RescueApproachPolicy.stealthAllowed(
                        data.pathway == null ? "" : data.pathway.toString(),
                        data.sequence)) {
                    player.sendSystemMessage(Component.translatable(
                            "message.lord_of_mysteries.quest.approach.stealth_locked")
                            .withStyle(ChatFormatting.RED));
                    return 0;
                }
                QuestPartyService.setResolutionState(player, chain, route, true);
                for (ServerPlayer participant : QuestPartyService.participants(player, chain)) {
                    participant.addEffect(new MobEffectInstance(
                            MobEffects.INVISIBILITY, 1200, 0, false, false));
                    participant.addEffect(new MobEffectInstance(
                            MobEffects.MOVEMENT_SPEED, 600, 0, false, false));
                }
            }
            case "divination" -> {
                if (!RescueApproachPolicy.divinationAllowed(
                        data.pathway == null ? "" : data.pathway.toString(),
                        data.sequence, data.spirituality)) {
                    player.sendSystemMessage(Component.translatable(
                            "message.lord_of_mysteries.quest.approach.divination_locked")
                            .withStyle(ChatFormatting.RED));
                    return 0;
                }
                data.spirituality -= 12f;
                QuestPartyService.setResolutionState(player, chain, route, true);
            }
            default -> {
                return 0;
            }
        }
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.quest.approach.selected",
                Component.translatable(
                        "message.lord_of_mysteries.quest.approach." + route))
                .withStyle(ChatFormatting.GOLD));
        return 1;
    }

    public static int rescueReporter(ServerPlayer player, Villager reporter) {
        if (!isCurrentObjective(player, "rescue", "missing_reporter")) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.npc.reporter_not_ready")
                    .withStyle(ChatFormatting.GRAY));
            return 0;
        }
        PlayerMysteryData data = MysteryCapability.get(player);
        if (data.questResolutionRoute.isBlank()) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.quest.approach.required")
                    .withStyle(ChatFormatting.YELLOW));
            return 0;
        }
        QuestChainDefinition chain = activeChain(data);
        if (chain == null) return 0;
        if ("assault".equals(data.questResolutionRoute)) {
            int remaining = remainingRescueGuards(player.serverLevel(), player, chain);
            if (remaining > 0) {
                player.sendSystemMessage(Component.translatable(
                        "message.lord_of_mysteries.quest.approach.guards_remaining",
                        remaining).withStyle(ChatFormatting.RED));
                return 0;
            }
            QuestPartyService.setResolutionState(
                    player, chain, data.questResolutionRoute, true);
        }
        if (!MysteryCapability.get(player).questResolutionReady) return 0;
        if (!recordObjective(player, "rescue", "missing_reporter", 1)) return 0;
        data = MysteryCapability.get(player);
        data.escortedReporterUuid = reporter.getUUID().toString();
        chain = activeChain(data);
        if (chain != null) {
            QuestPartyService.assignReporter(
                    player, chain, reporter.getUUID().toString());
        }
        InvestigationNpcHandler.beginEscort(reporter, player);
        if (!hasItem(player, ModItems.PRESS_CARD.get())) {
            giveItem(player, new ItemStack(ModItems.PRESS_CARD.get()));
        }
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.npc.reporter_rescued")
                .withStyle(ChatFormatting.GREEN));
        data.knownKnowledge.add(id(
                "knowledge/m2/rescue_route_" + data.questResolutionRoute));
        return 1;
    }

    public static int list(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        player.sendSystemMessage(Component.translatable(
                "command.lord_of_mysteries.commission.list.title")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        if (CommissionDefinitionManager.all().isEmpty()) {
            player.sendSystemMessage(Component.translatable(
                    "command.lord_of_mysteries.commission.data_unavailable")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        CommissionDefinitionManager.all().values().stream()
                .sorted(Comparator.comparing(definition -> definition.id().toString()))
                .forEach(definition -> player.sendSystemMessage(Component.literal("• ")
                        .append(Component.translatable(definition.titleKey()))
                        .append(Component.literal(" — "))
                        .append(Component.translatable(definition.summaryKey()))
                        .append(data.completedCommissions.contains(definition.id())
                                ? Component.translatable(
                                        "command.lord_of_mysteries.commission.completed_suffix")
                                : requirementsMet(data, definition)
                                ? Component.empty()
                                : Component.translatable(
                                        "command.lord_of_mysteries.commission.locked_suffix"))
                        .withStyle(ChatFormatting.GRAY)));
        player.sendSystemMessage(Component.translatable(
                "command.lord_of_mysteries.commission.list.hint")
                .withStyle(ChatFormatting.DARK_GRAY));
        return CommissionDefinitionManager.all().size();
    }

    public static int accept(ServerPlayer player, String value) {
        return accept(player, normalize(value));
    }

    public static int accept(ServerPlayer player, ResourceLocation commissionId) {
        CommissionDefinition definition = CommissionDefinitionManager.get(commissionId);
        if (definition == null) {
            player.sendSystemMessage(Component.translatable(
                    "command.lord_of_mysteries.commission.unknown", commissionId)
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        QuestChainDefinition chain = QuestChainDefinitionManager.get(definition.questChain());
        if (chain == null) {
            player.sendSystemMessage(Component.translatable(
                    "command.lord_of_mysteries.commission.chain_missing")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        PlayerMysteryData data = MysteryCapability.get(player);
        if (!data.activeCommissionId.isBlank()) {
            player.sendSystemMessage(Component.translatable(
                    "command.lord_of_mysteries.commission.already_active")
                    .withStyle(ChatFormatting.YELLOW));
            return 0;
        }
        if (!definition.repeatable() && data.completedCommissions.contains(commissionId)) {
            player.sendSystemMessage(Component.translatable(
                    "command.lord_of_mysteries.commission.already_completed")
                    .withStyle(ChatFormatting.YELLOW));
            return 0;
        }
        if (!requirementsMet(data, definition)) {
            player.sendSystemMessage(Component.translatable(
                    "command.lord_of_mysteries.commission.prerequisite_missing")
                    .withStyle(ChatFormatting.YELLOW));
            return 0;
        }
        long now = player.level().getGameTime();
        if (data.commissionCooldowns.getOrDefault(commissionId, 0L) > now) {
            player.sendSystemMessage(Component.translatable(
                    "command.lord_of_mysteries.commission.cooldown")
                    .withStyle(ChatFormatting.YELLOW));
            return 0;
        }

        data.activeCommissionId = commissionId.toString();
        data.activeQuestChainId = definition.questChain().toString();
        data.activeQuestStep = 0;
        data.questObjectiveProgress = 0;
        data.commissionAcceptedTick = now;
        if (DYNAMIC_CASE.equals(commissionId)) {
            data.caseHypotheses.remove(DYNAMIC_CASE);
        }
        QuestPartyService.registerActive(player, chain);
        giveCommissionPaper(player, definition, data.commissionAcceptedTick);
        player.sendSystemMessage(Component.translatable(
                "command.lord_of_mysteries.commission.accepted",
                Component.translatable(definition.titleKey()))
                .withStyle(ChatFormatting.GOLD));
        showCurrentStep(player, data, chain);
        if (DYNAMIC_CASE.equals(definition.id())) {
            DynamicCaseService.issuePortfolio(player);
            DynamicCaseService.show(player);
        }
        return 1;
    }

    public static CommissionBoardState availability(
            PlayerMysteryData data, CommissionDefinition definition, long gameTime) {
        if (definition.id().toString().equals(data.activeCommissionId)) {
            return CommissionBoardState.ACTIVE;
        }
        if (!data.activeCommissionId.isBlank()) {
            return CommissionBoardState.LOCKED;
        }
        if (!definition.repeatable()
                && data.completedCommissions.contains(definition.id())) {
            return CommissionBoardState.COMPLETED;
        }
        if (!requirementsMet(data, definition)) {
            return CommissionBoardState.LOCKED;
        }
        if (data.commissionCooldowns.getOrDefault(definition.id(), 0L) > gameTime) {
            return CommissionBoardState.COOLDOWN;
        }
        return CommissionBoardState.AVAILABLE;
    }

    public static int showStatus(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        player.sendSystemMessage(Component.translatable(
                "command.lord_of_mysteries.commission.balance",
                CommissionCurrency.format(data.moneyPence))
                .withStyle(ChatFormatting.GOLD));
        if (data.activeCommissionId.isBlank()) {
            player.sendSystemMessage(Component.translatable(
                    "command.lord_of_mysteries.commission.none")
                    .withStyle(ChatFormatting.GRAY));
            return 0;
        }
        ResourceLocation commissionId = ResourceLocation.tryParse(data.activeCommissionId);
        CommissionDefinition definition = commissionId == null
                ? null : CommissionDefinitionManager.get(commissionId);
        QuestChainDefinition chain = activeChain(data);
        if (definition == null || chain == null) {
            player.sendSystemMessage(Component.translatable(
                    "command.lord_of_mysteries.commission.data_unavailable")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        player.sendSystemMessage(Component.translatable(
                "command.lord_of_mysteries.commission.status.title",
                Component.translatable(definition.titleKey()))
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        showCurrentStep(player, data, chain);
        if (DYNAMIC_CASE.equals(definition.id())) {
            DynamicCaseService.show(player);
        }
        return 1;
    }

    public static int abandon(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (data.activeCommissionId.isBlank()) return 0;
        if (DYNAMIC_CASE.equals(ResourceLocation.tryParse(
                data.activeCommissionId))) {
            DynamicCaseService.returnPortfolio(player);
        }
        QuestPartyService.leave(player);
        clearActive(data);
        player.sendSystemMessage(Component.translatable(
                "command.lord_of_mysteries.commission.abandoned")
                .withStyle(ChatFormatting.YELLOW));
        return 1;
    }

    public static void tick(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        QuestChainDefinition chain = activeChain(data);
        if (chain == null || data.activeQuestStep < 0
                || data.activeQuestStep >= chain.steps().size()) return;
        QuestChainDefinition.Objective objective =
                chain.steps().get(data.activeQuestStep).objective();
        ServerLevel overworld = player.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) return;
        switch (objective.type()) {
            case "enter_structure" -> trackStructure(player, overworld, objective);
            case "custom_callback" -> trackCustomObjective(player, overworld, objective);
            case "pickup" -> trackEvidence(player, overworld, objective);
            case "escort" -> {
                if (QuestPartyService.isCoordinator(player, chain)) {
                    trackEscort(player, overworld, objective);
                }
            }
            case "survive_waves" -> {
                if (QuestPartyService.isCoordinator(player, chain)) {
                    trackNightDefense(player, overworld, objective);
                }
            }
            case "reach_sequence" -> {
                if (data.isExtraordinary() && data.sequence <= 9) {
                    recordObjective(player, objective.type(), objective.target(), 1);
                }
            }
            default -> {
            }
        }
    }

    public static void recordOccultKill(ServerPlayer player, ResourceLocation entityId) {
        recordObjective(player, "encounter", entityId.toString(), 1);
    }

    public static boolean recordObjective(ServerPlayer player, String type,
                                          String target, int amount) {
        PlayerMysteryData data = MysteryCapability.get(player);
        QuestChainDefinition chain = activeChain(data);
        if (chain == null) return false;
        List<ServerPlayer> participants = QuestPartyService.participants(player, chain);
        boolean matched = recordObjectiveForPlayer(player, type, target, amount);
        if (!matched) return false;
        for (ServerPlayer participant : participants) {
            if (participant != player) {
                recordObjectiveForPlayer(participant, type, target, amount);
            }
        }
        QuestPartyService.persistProgress(player, chain);
        return true;
    }

    private static boolean recordObjectiveForPlayer(ServerPlayer player, String type,
                                                    String target, int amount) {
        PlayerMysteryData data = MysteryCapability.get(player);
        QuestChainDefinition chain = activeChain(data);
        if (chain == null) return false;
        QuestProgression.Result result = QuestProgression.record(
                chain, data.activeQuestStep, data.questObjectiveProgress,
                type, target, amount);
        if (!result.matched()) return false;
        int completedStep = data.activeQuestStep;
        data.activeQuestStep = result.stepIndex();
        data.questObjectiveProgress = result.progress();
        if (result.stepCompleted()) {
            player.sendSystemMessage(Component.translatable(
                    "command.lord_of_mysteries.quest.step_complete",
                    completedStep + 1, chain.steps().size())
                    .withStyle(ChatFormatting.GREEN));
            if (result.chainCompleted()) {
                player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.quest.return_to_board")
                        .withStyle(ChatFormatting.GOLD));
            } else {
                showCurrentStep(player, data, chain);
            }
        } else {
            QuestChainDefinition.Objective objective =
                    chain.steps().get(data.activeQuestStep).objective();
            player.sendSystemMessage(Component.translatable(
                    "command.lord_of_mysteries.quest.progress",
                    data.questObjectiveProgress, objective.count())
                    .withStyle(ChatFormatting.GRAY));
        }
        return true;
    }

    private static int settle(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        QuestChainDefinition chain = activeChain(data);
        ResourceLocation commissionId = ResourceLocation.tryParse(data.activeCommissionId);
        CommissionDefinition definition = commissionId == null
                ? null : CommissionDefinitionManager.get(commissionId);
        if (definition == null || !isReadyToSettle(data)) return 0;
        long completedTick = player.level().getGameTime();
        DynamicCaseProfile dynamicProfile =
                DynamicCaseService.profileFor(player, data);
        CaseEvidenceView evidence = CaseEvidenceView.from(
                data, FormulaAppraisalService.evidence(player),
                dynamicProfile);
        CaseDebriefRecord debrief = CaseDebriefService.evaluate(
                definition.id(), evidence, data.commissionAcceptedTick,
                completedTick, data.questResolutionRoute,
                data.insanityPressure, data.pollution,
                FormulaAppraisalService.failedAttempts(player),
                CaseHypothesisService.unresolvedStrain(
                        data, definition.id()));
        data.caseDebriefs.put(definition.id(), debrief);
        data.moneyPence += definition.reward().pence();
        definition.reward().reputation().forEach((organization, amount) ->
                data.orgReputation.merge(organization, amount, Integer::sum));
        if (DYNAMIC_CASE.equals(definition.id()) && dynamicProfile != null) {
            DynamicCaseService.applyOrganizationFeedback(
                    player, data, dynamicProfile, debrief);
        }
        data.completedCommissions.add(definition.id());
        data.commissionCooldowns.put(definition.id(),
                player.level().getGameTime() + definition.cooldownTicks());
        if (MISSING_SQUAD.equals(definition.id())) {
            giveItem(player, new ItemStack(ModItems.BURNT_LIST.get()));
            data.knownKnowledge.add(id("knowledge/m2/missing_squad_chain"));
        } else if (LOST_CAT.equals(definition.id())) {
            giveItem(player, new ItemStack(ModItems.NEWSPAPER.get()));
        } else if (COUNTERFEIT_FORMULA.equals(definition.id())) {
            FormulaAppraisalService.takeDossier(player);
            giveItem(player, new ItemStack(ModItems.FORMULA_FRAGMENT.get(), 2));
        } else if (DYNAMIC_CASE.equals(definition.id())) {
            data.knownKnowledge.add(id("knowledge/m2/dynamic_case_rotation"));
            DynamicCaseService.returnPortfolio(player);
        }
        player.sendSystemMessage(Component.translatable(
                "command.lord_of_mysteries.commission.settled",
                Component.translatable(definition.titleKey()),
                CommissionCurrency.format(definition.reward().pence()))
                .withStyle(ChatFormatting.GREEN));
        CaseDebriefService.sendSummary(player, definition.titleKey(), debrief);
        if (chain != null) QuestPartyService.markSettled(player, chain);
        clearActive(data);
        return 1;
    }

    public static boolean restoreCommissionPaper(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (data.activeCommissionId.isBlank()
                || hasCommissionPaper(player, data.activeCommissionId)) return false;
        ResourceLocation commissionId = ResourceLocation.tryParse(data.activeCommissionId);
        CommissionDefinition definition = commissionId == null
                ? null : CommissionDefinitionManager.get(commissionId);
        if (definition != null) {
            giveCommissionPaper(player, definition, data.commissionAcceptedTick);
            return true;
        }
        return false;
    }

    public static int recoverCaseItems(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (data.activeCommissionId.isBlank()) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.case.recover.no_active")
                    .withStyle(ChatFormatting.GRAY));
            return 0;
        }
        if (DynamicCaseService.isActive(data)
                && "reconsider".equals(data.questResolutionRoute)) {
            return DynamicCaseService.recoverConclusion(player);
        }
        if (!InvestigationBoardService.isNearBoard(player)) {
            player.sendSystemMessage(Component.translatable(
                            "screen.lord_of_mysteries.investigation_board.nearby_required")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        if (DynamicCaseService.isActive(data)) {
            int restoredPortfolio = DynamicCaseService.recoverPortfolio(player);
            if (restoredPortfolio > 0) return restoredPortfolio;
        }
        CaseRecoveryPolicy.RecoveryPlan plan = CaseRecoveryPolicy.plan(
                data.activeCommissionId,
                data.activeQuestStep,
                hasCommissionPaper(player, data.activeCommissionId),
                FormulaAppraisalService.hasDossier(player));
        int restored = 0;
        if (plan.restoreCommissionPaper() && restoreCommissionPaper(player)) {
            restored++;
        }
        if (plan.restoreFormulaDossier()) {
            giveItem(player, FormulaAppraisalService.createRecoveryDossier(
                    player, plan.recoveredDossierAppraised()));
            restored++;
        }
        if (restored == 0) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.case.recover.complete")
                    .withStyle(ChatFormatting.GRAY));
            return 0;
        }
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.case.recover.restored", restored)
                .withStyle(ChatFormatting.GREEN));
        return restored;
    }

    private static boolean hasCommissionPaper(ServerPlayer player,
                                              String commissionId) {
        for (ItemStack stack : player.getInventory().items) {
            if (isCommissionPaper(stack, commissionId)) return true;
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (isCommissionPaper(stack, commissionId)) return true;
        }
        for (int slot = 0; slot < player.getEnderChestInventory().getContainerSize(); slot++) {
            if (isCommissionPaper(player.getEnderChestInventory().getItem(slot),
                    commissionId)) return true;
        }
        return false;
    }

    private static boolean isCommissionPaper(ItemStack stack, String commissionId) {
        return stack.is(ModItems.COMMISSION_PAPER.get()) && stack.hasTag()
                && commissionId.equals(stack.getTag().getString("commission_id"));
    }

    private static void trackLostCat(ServerPlayer player, ServerLevel level,
                                     QuestChainDefinition.Objective objective) {
        nearestCamp(level, player).filter(position -> near(player, position, 24d))
                .ifPresent(position -> {
                    Cat cat = AbandonedCampGenerator.ensureLostCat(level, position);
                    if (player.distanceToSqr(cat) <= 8d * 8d) {
                        recordObjective(player, objective.type(), objective.target(), 1);
                    }
                });
    }

    private static void trackStructure(ServerPlayer player, ServerLevel level,
                                       QuestChainDefinition.Objective objective) {
        if ("mist_city_outpost".equals(objective.target())) {
            MistCityOutpostSavedData.get(level).outpost()
                    .filter(position -> near(player, position, 18d))
                    .ifPresent(position -> recordObjective(
                            player, objective.type(), objective.target(), 1));
        } else if ("investigator_camp".equals(objective.target())) {
            nearestCamp(level, player)
                    .filter(position -> near(player, position, 24d))
                    .ifPresent(position -> recordObjective(
                            player, objective.type(), objective.target(), 1));
        } else if ("abandoned_church".equals(objective.target())) {
            InvestigationSiteSavedData.get(level).church()
                    .filter(position -> near(player, position, 24d))
                    .ifPresent(position -> recordObjective(
                            player, objective.type(), objective.target(), 1));
        } else if ("cultist_camp".equals(objective.target())) {
            InvestigationSiteSavedData.get(level).cultistCamp()
                    .filter(position -> near(player, position, 28d))
                    .ifPresent(position -> recordObjective(
                            player, objective.type(), objective.target(), 1));
        } else if ("occultist_hut".equals(objective.target())) {
            InvestigationSiteSavedData.get(level).occultistHut()
                    .filter(position -> near(player, position, 18d))
                    .ifPresent(position -> recordObjective(
                            player, objective.type(), objective.target(), 1));
        }
    }

    private static void trackEscort(ServerPlayer player, ServerLevel level,
                                    QuestChainDefinition.Objective objective) {
        if (!"missing_reporter_to_outpost".equals(objective.target())) return;
        PlayerMysteryData data = MysteryCapability.get(player);
        UUID reporterId = parseUuid(data.escortedReporterUuid);
        if (reporterId == null) return;
        Entity entity = level.getEntity(reporterId);
        if (!(entity instanceof Villager reporter) || !reporter.isAlive()) return;
        double distance = player.distanceToSqr(reporter);
        if (distance > 32d * 32d) {
            reporter.teleportTo(player.getX() + 1d, player.getY(), player.getZ() + 1d);
        } else if (distance > 3d * 3d) {
            reporter.getNavigation().moveTo(player, 1.05d);
        }
        MistCityOutpostSavedData.get(level).outpost().ifPresent(outpost -> {
            if (near(player, outpost, 18d)
                    && outpost.distToCenterSqr(reporter.position()) <= 18d * 18d
                    && recordObjective(player, objective.type(), objective.target(), 1)) {
                InvestigationNpcHandler.finishEscort(reporter, outpost);
                player.sendSystemMessage(Component.translatable(
                        "message.lord_of_mysteries.quest.escort_complete")
                        .withStyle(ChatFormatting.GREEN));
            }
        });
    }

    private static void trackNightDefense(ServerPlayer player, ServerLevel level,
                                          QuestChainDefinition.Objective objective) {
        if (!"mist_city_outpost".equals(objective.target())) return;
        MistCityOutpostSavedData.get(level).outpost().ifPresent(outpost -> {
            if (!near(player, outpost, 32d)) return;
            PlayerMysteryData data = MysteryCapability.get(player);
            long now = level.getGameTime();
            QuestChainDefinition chain = activeChain(data);
            if (chain == null) return;
            String partyKey = QuestPartyService.partyKey(player, chain);
            List<Mob> attackers = defenseAttackers(level, outpost, partyKey);
            if (!attackers.isEmpty() && !data.questDefenseWaveSpawned) {
                QuestPartyService.setDefenseState(player, chain, true, 0L);
            }
            NightDefenseLogic.Action action = NightDefenseLogic.decide(
                    level.isNight(), data.questDefenseWaveSpawned,
                    !attackers.isEmpty(), now, data.questDefenseNextTick);
            switch (action) {
                case WAIT_FOR_NIGHT -> {
                    if (now >= data.questDefenseNextTick) {
                        player.sendSystemMessage(Component.translatable(
                                "message.lord_of_mysteries.quest.wait_for_night")
                                .withStyle(ChatFormatting.DARK_GRAY));
                        QuestPartyService.setDefenseState(
                                player, chain, false, now + 200L);
                    }
                }
                case SPAWN_WAVE -> {
                    int wave = data.questObjectiveProgress + 1;
                    spawnDefenseWave(level, player, outpost, wave, partyKey);
                    QuestPartyService.setDefenseState(player, chain, true, 0L);
                    player.sendSystemMessage(Component.translatable(
                            "message.lord_of_mysteries.quest.wave_started",
                            wave, objective.count()).withStyle(ChatFormatting.RED));
                }
                case COMPLETE_WAVE -> {
                    int completedWave = data.questObjectiveProgress + 1;
                    QuestPartyService.setDefenseState(player, chain, false, 0L);
                    player.sendSystemMessage(Component.translatable(
                            "message.lord_of_mysteries.quest.wave_cleared",
                            completedWave, objective.count()).withStyle(ChatFormatting.GREEN));
                    recordObjective(player, objective.type(), objective.target(), 1);
                    long nextTick = isCurrentObjective(
                            player, objective.type(), objective.target()) ? now + 100L : 0L;
                    QuestPartyService.setDefenseState(
                            player, chain, false, nextTick);
                }
                case WAIT -> {
                }
            }
        });
    }

    private static List<Mob> defenseAttackers(ServerLevel level, BlockPos outpost,
                                              String partyKey) {
        return level.getEntitiesOfClass(Mob.class, new AABB(outpost).inflate(48d),
                mob -> mob.isAlive()
                        && partyKey.equals(mob.getPersistentData().getString(
                                "lom_defense_party")));
    }

    private static void spawnDefenseWave(ServerLevel level, ServerPlayer player,
                                         BlockPos outpost, int wave,
                                         String partyKey) {
        RandomSource random = RandomSource.create(
                level.getSeed() ^ player.getUUID().getMostSignificantBits() ^ wave);
        int count = 2 + wave;
        for (int index = 0; index < count; index++) {
            double angle = Math.PI * 2d * index / count + random.nextDouble() * 0.35d;
            int radius = 10 + random.nextInt(5);
            int x = outpost.getX() + (int) Math.round(Math.cos(angle) * radius);
            int z = outpost.getZ() + (int) Math.round(Math.sin(angle) * radius);
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            Mob attacker = (index + wave) % 3 == 0
                    ? EntityType.SKELETON.create(level) : EntityType.ZOMBIE.create(level);
            if (attacker == null) continue;
            attacker.moveTo(x + 0.5d, y, z + 0.5d, random.nextFloat() * 360f, 0f);
            attacker.setTarget(player);
            attacker.setPersistenceRequired();
            attacker.getPersistentData().putString("lom_defense_party", partyKey);
            level.addFreshEntity(attacker);
        }
    }

    private static void trackCustomObjective(ServerPlayer player, ServerLevel level,
                                             QuestChainDefinition.Objective objective) {
        if ("lost_cat".equals(objective.target())) {
            trackLostCat(player, level, objective);
        }
    }

    private static void trackEvidence(ServerPlayer player, ServerLevel level,
                                      QuestChainDefinition.Objective objective) {
        if ("lord_of_mysteries:sealed_formula_dossier".equals(objective.target())) {
            if (FormulaAppraisalService.hasDossier(player)) {
                recordObjective(player, objective.type(), objective.target(), 1);
            }
            return;
        }
        if (hasItem(player, ModItems.BLOODSTAINED_NOTEBOOK.get())) {
            recordObjective(player, objective.type(), objective.target(), 1);
            return;
        }
        nearestCamp(level, player).filter(position -> near(player, position, 12d))
                .ifPresent(position -> {
                    giveItem(player, new ItemStack(ModItems.BLOODSTAINED_NOTEBOOK.get()));
                    player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.quest.evidence_found")
                            .withStyle(ChatFormatting.DARK_RED));
                });
    }

    private static void showCurrentStep(ServerPlayer player, PlayerMysteryData data,
                                        QuestChainDefinition chain) {
        if (data.activeQuestStep >= chain.steps().size()) {
            player.sendSystemMessage(Component.translatable(
                    "command.lord_of_mysteries.quest.return_to_board")
                    .withStyle(ChatFormatting.GOLD));
            return;
        }
        QuestChainDefinition.Step step = chain.steps().get(data.activeQuestStep);
        player.sendSystemMessage(Component.literal("[" + (data.activeQuestStep + 1)
                        + "/" + chain.steps().size() + "] ")
                .append(Component.translatable(step.guidanceKey()))
                .withStyle(ChatFormatting.AQUA));
        if (step.objective().count() > 1) {
            player.sendSystemMessage(Component.translatable(
                    "command.lord_of_mysteries.quest.progress",
                    data.questObjectiveProgress, step.objective().count())
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        if (!data.questResolutionRoute.isBlank()) {
            player.sendSystemMessage(Component.translatable(
                    "command.lord_of_mysteries.quest.approach_status",
                    Component.translatable(
                            "message.lord_of_mysteries.quest.approach."
                                    + data.questResolutionRoute),
                    Component.translatable(data.questResolutionReady
                            ? "command.lord_of_mysteries.quest.approach_ready"
                            : "command.lord_of_mysteries.quest.approach_pending"))
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static boolean isReadyToSettle(PlayerMysteryData data) {
        QuestChainDefinition chain = activeChain(data);
        return chain != null && data.activeQuestStep >= chain.steps().size();
    }

    private static QuestChainDefinition activeChain(PlayerMysteryData data) {
        if (data.activeQuestChainId.isBlank()) return null;
        ResourceLocation id = ResourceLocation.tryParse(data.activeQuestChainId);
        return id == null ? null : QuestChainDefinitionManager.get(id);
    }

    public static boolean isCurrentObjective(ServerPlayer player, String type,
                                             String target) {
        PlayerMysteryData data = MysteryCapability.get(player);
        QuestChainDefinition chain = activeChain(data);
        if (chain == null || data.activeQuestStep < 0
                || data.activeQuestStep >= chain.steps().size()) return false;
        QuestChainDefinition.Objective objective =
                chain.steps().get(data.activeQuestStep).objective();
        return objective.type().equals(type) && objective.target().equals(target);
    }

    private static UUID parseUuid(String value) {
        if (value.isBlank()) return null;
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static Optional<BlockPos> nearestCamp(ServerLevel level, ServerPlayer player) {
        return CampGenerationSavedData.get(level).nearestCamp(player.blockPosition());
    }

    private static boolean near(ServerPlayer player, BlockPos position, double radius) {
        return player.level().dimension() == Level.OVERWORLD
                && position.distToCenterSqr(player.position()) <= radius * radius;
    }

    private static boolean hasItem(ServerPlayer player, Item item) {
        return player.getInventory().items.stream().anyMatch(stack -> stack.is(item));
    }

    private static void giveCommissionPaper(ServerPlayer player,
                                            CommissionDefinition definition, long acceptedTick) {
        ItemStack paper = new ItemStack(ModItems.COMMISSION_PAPER.get());
        paper.getOrCreateTag().putString("commission_id", definition.id().toString());
        paper.getOrCreateTag().putLong("accepted_tick", acceptedTick);
        paper.setHoverName(Component.translatable(definition.titleKey()));
        giveItem(player, paper);
    }

    private static void giveItem(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) player.drop(stack, false);
    }

    private static void clearActive(PlayerMysteryData data) {
        data.activeCommissionId = "";
        data.activeQuestChainId = "";
        data.activeQuestStep = -1;
        data.questObjectiveProgress = 0;
        data.commissionAcceptedTick = 0L;
        data.escortedReporterUuid = "";
        data.questDefenseWaveSpawned = false;
        data.questDefenseNextTick = 0L;
        data.questResolutionRoute = "";
        data.questResolutionReady = false;
    }

    private static ResourceLocation recommendedCommission(PlayerMysteryData data) {
        if (!data.completedCommissions.contains(LOST_CAT)) return LOST_CAT;
        if (!data.completedCommissions.contains(MISSING_SQUAD)) return MISSING_SQUAD;
        if (!data.completedCommissions.contains(COUNTERFEIT_FORMULA)) {
            return COUNTERFEIT_FORMULA;
        }
        return DYNAMIC_CASE;
    }

    private static boolean requirementsMet(PlayerMysteryData data,
                                           CommissionDefinition definition) {
        return data.completedCommissions.containsAll(definition.prerequisites());
    }

    private static int remainingRescueGuards(ServerLevel level, ServerPlayer player,
                                             QuestChainDefinition chain) {
        Optional<BlockPos> camp = InvestigationSiteSavedData.get(level).cultistCamp();
        if (camp.isEmpty()) return 0;
        String partyKey = QuestPartyService.partyKey(player, chain);
        return level.getEntitiesOfClass(Mob.class, new AABB(camp.get()).inflate(96d),
                mob -> mob.isAlive() && mob.getTags().contains(RESCUE_GUARD_TAG)
                        && partyKey.equals(mob.getPersistentData().getString(
                                RESCUE_PARTY_TAG))).size();
    }

    private static void spawnRescueGuards(ServerLevel level, ServerPlayer player,
                                          QuestChainDefinition chain) {
        Optional<BlockPos> camp = InvestigationSiteSavedData.get(level).cultistCamp();
        if (camp.isEmpty() || remainingRescueGuards(level, player, chain) > 0) return;
        String partyKey = QuestPartyService.partyKey(player, chain);
        int[][] offsets = {{-7, -4}, {-7, 4}, {5, -6}, {6, 5}};
        for (int index = 0; index < offsets.length; index++) {
            BlockPos base = camp.get().offset(offsets[index][0], 0, offsets[index][1]);
            int y = level.getHeight(
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, base.getX(), base.getZ());
            Mob guard = index == offsets.length - 1
                    ? EntityType.SKELETON.create(level) : EntityType.ZOMBIE.create(level);
            if (guard == null) continue;
            guard.moveTo(base.getX() + 0.5d, y, base.getZ() + 0.5d, 0f, 0f);
            guard.setTarget(player);
            guard.setPersistenceRequired();
            guard.addTag(RESCUE_GUARD_TAG);
            guard.getPersistentData().putString(RESCUE_PARTY_TAG, partyKey);
            level.addFreshEntity(guard);
        }
    }

    private static ResourceLocation normalize(String value) {
        if (value.contains(":")) {
            ResourceLocation parsed = ResourceLocation.tryParse(value);
            return parsed == null ? id("commission/invalid") : parsed;
        }
        String path = value.startsWith("commission/") ? value : "commission/" + value;
        return id(path);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ProjectMystery.MOD_ID, path);
    }
}
