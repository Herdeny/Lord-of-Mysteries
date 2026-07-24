package top.aurora.lordofmysteries.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.acting.ActingIdentityService;
import top.aurora.lordofmysteries.knowledge.InvestigatorCompassItem;
import top.aurora.lordofmysteries.knowledge.InvestigatorNotesItem;
import top.aurora.lordofmysteries.knowledge.GuideJournalProgress;
import top.aurora.lordofmysteries.knowledge.KnowledgeText;
import top.aurora.lordofmysteries.knowledge.M1Readiness;
import top.aurora.lordofmysteries.knowledge.M1TrialContinuity;
import top.aurora.lordofmysteries.knowledge.M1TrialProgress;
import top.aurora.lordofmysteries.knowledge.M1TrialTimeline;
import top.aurora.lordofmysteries.knowledge.M1TrialTimer;
import top.aurora.lordofmysteries.knowledge.M1TrialTracker;
import top.aurora.lordofmysteries.knowledge.PlayerGuideHandler;
import top.aurora.lordofmysteries.network.NetworkProtocol;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.potion.SeerPotionItem;
import top.aurora.lordofmysteries.registry.ModItems;
import top.aurora.lordofmysteries.commission.CaseAnalysisService;
import top.aurora.lordofmysteries.commission.CaseHypothesisService;
import top.aurora.lordofmysteries.commission.CaseHypothesisStance;
import top.aurora.lordofmysteries.commission.CommissionDefinitionManager;
import top.aurora.lordofmysteries.commission.CommissionService;
import top.aurora.lordofmysteries.commission.CityLifeService;
import top.aurora.lordofmysteries.commission.CityEconomyPolicy;
import top.aurora.lordofmysteries.commission.CityServiceDeskService;
import top.aurora.lordofmysteries.commission.DynamicCaseProfile;
import top.aurora.lordofmysteries.commission.DynamicCaseService;
import top.aurora.lordofmysteries.commission.FormulaAppraisalService;
import top.aurora.lordofmysteries.commission.InvestigationBoardService;
import top.aurora.lordofmysteries.commission.QuestChainDefinitionManager;
import top.aurora.lordofmysteries.commission.QuestPartyService;
import top.aurora.lordofmysteries.commission.QuestPartySavedData;
import top.aurora.lordofmysteries.world.AbandonedCampGenerator;
import top.aurora.lordofmysteries.world.CampGenerationSavedData;
import top.aurora.lordofmysteries.world.MistCityOutpostGenerator;
import top.aurora.lordofmysteries.world.MistCityOutpostSavedData;
import top.aurora.lordofmysteries.world.MistCityWorldEvent;
import top.aurora.lordofmysteries.world.MistCityWorldEventPolicy;
import top.aurora.lordofmysteries.world.MistCityWorldEventSavedData;
import top.aurora.lordofmysteries.world.InvestigationSiteGenerator;
import top.aurora.lordofmysteries.world.InvestigationSiteSavedData;

@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ProjectMysteryCommands {

    private ProjectMysteryCommands() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("pm")
                .then(Commands.literal("guide")
                        .executes(context -> {
                            InvestigatorNotesItem.showGuide(
                                    context.getSource().getPlayerOrException());
                            return 1;
                        })
                        .then(Commands.literal("next").executes(context ->
                                PlayerGuideHandler.showNextStep(
                                        context.getSource().getPlayerOrException())))
                        .then(Commands.literal("recover").executes(context ->
                                PlayerGuideHandler.restoreStarterKit(
                                        context.getSource().getPlayerOrException(), true))))
                .then(Commands.literal("next").executes(context ->
                        PlayerGuideHandler.showNextStep(
                                context.getSource().getPlayerOrException())))
                .then(Commands.literal("recover").executes(context ->
                        PlayerGuideHandler.restoreStarterKit(
                                context.getSource().getPlayerOrException(), true)))
                .then(Commands.literal("handbook")
                        .executes(context -> InvestigatorNotesItem.showHandbookOverview(
                                context.getSource().getPlayerOrException()))
                        .then(Commands.argument("chapter", IntegerArgumentType.integer(
                                        1, GuideJournalProgress.CHAPTER_COUNT))
                                .executes(context -> InvestigatorNotesItem.showHandbookChapter(
                                        context.getSource().getPlayerOrException(),
                                        IntegerArgumentType.getInteger(context, "chapter")))))
                .then(Commands.literal("status").executes(context ->
                        showStatus(context.getSource().getPlayerOrException())))
                .then(Commands.literal("reflect").executes(context ->
                        reflect(context.getSource().getPlayerOrException())))
                .then(Commands.literal("camp").executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    ItemStack compass = findCompass(player);
                    InvestigatorCompassItem.reportCamp(player, compass);
                    return 1;
                }))
                .then(Commands.literal("mistcity").executes(context -> {
                    MistCityOutpostGenerator.reportOutpost(
                            context.getSource().getPlayerOrException());
                    return 1;
                }))
                .then(Commands.literal("life")
                        .executes(context -> CityLifeService.showStatus(
                                context.getSource().getPlayerOrException()))
                        .then(Commands.literal("work")
                                .then(Commands.literal("press")
                                        .executes(context ->
                                                CityLifeService.workAtDistrict(
                                                        context.getSource()
                                                                .getPlayerOrException(),
                                                        CityEconomyPolicy.Job.PRESS)))
                                .then(Commands.literal("agency")
                                        .executes(context ->
                                                CityLifeService.workAtDistrict(
                                                        context.getSource()
                                                                .getPlayerOrException(),
                                                        CityEconomyPolicy.Job.AGENCY)))
                                .then(Commands.literal("patrol")
                                        .executes(context ->
                                                CityLifeService.workAtDistrict(
                                                        context.getSource()
                                                                .getPlayerOrException(),
                                                        CityEconomyPolicy.Job.PATROL)))))
                .then(Commands.literal("city").executes(context ->
                        CityServiceDeskService.showDirectory(
                                context.getSource().getPlayerOrException())))
                .then(Commands.literal("case")
                        .executes(context -> {
                            InvestigationSiteGenerator.reportSites(
                                    context.getSource().getPlayerOrException());
                            return 1;
                        })
                        .then(Commands.literal("sites").executes(context -> {
                            InvestigationSiteGenerator.reportSites(
                                    context.getSource().getPlayerOrException());
                            return 1;
                        }))
                        .then(Commands.literal("analyze").executes(context ->
                                CaseAnalysisService.showAnalysis(
                                        context.getSource().getPlayerOrException())))
                        .then(Commands.literal("archive").executes(context ->
                                CaseAnalysisService.showArchive(
                                        context.getSource().getPlayerOrException())))
                        .then(Commands.literal("debrief")
                                .executes(context -> CaseAnalysisService.showArchive(
                                        context.getSource().getPlayerOrException()))
                                .then(Commands.literal("lost_cat").executes(context ->
                                        CaseAnalysisService.showDebrief(
                                                context.getSource().getPlayerOrException(),
                                                CommissionService.LOST_CAT)))
                                .then(Commands.literal("missing_squad").executes(context ->
                                        CaseAnalysisService.showDebrief(
                                                context.getSource().getPlayerOrException(),
                                                CommissionService.MISSING_SQUAD)))
                                .then(Commands.literal("counterfeit_formula").executes(context ->
                                        CaseAnalysisService.showDebrief(
                                                context.getSource().getPlayerOrException(),
                                                CommissionService.COUNTERFEIT_FORMULA)))
                                .then(Commands.literal("dynamic_case").executes(context ->
                                        CaseAnalysisService.showDebrief(
                                                context.getSource().getPlayerOrException(),
                                                CommissionService.DYNAMIC_CASE))))
                        .then(Commands.literal("hypothesis")
                                .executes(context -> CaseHypothesisService.show(
                                        context.getSource().getPlayerOrException()))
                                .then(Commands.literal("propose")
                                        .then(Commands.literal("supports")
                                                .then(Commands.argument(
                                                                "relation", StringArgumentType.word())
                                                        .then(Commands.argument(
                                                                        "note", StringArgumentType.greedyString())
                                                                .executes(context ->
                                                                        CaseHypothesisService.propose(
                                                                                context.getSource().getPlayerOrException(),
                                                                                CaseHypothesisStance.SUPPORTS,
                                                                                StringArgumentType.getString(context, "relation"),
                                                                                StringArgumentType.getString(context, "note"))))))
                                        .then(Commands.literal("contradicts")
                                                .then(Commands.argument(
                                                                "relation", StringArgumentType.word())
                                                        .then(Commands.argument(
                                                                        "note", StringArgumentType.greedyString())
                                                                .executes(context ->
                                                                        CaseHypothesisService.propose(
                                                                                context.getSource().getPlayerOrException(),
                                                                                CaseHypothesisStance.CONTRADICTS,
                                                                                StringArgumentType.getString(context, "relation"),
                                                                                StringArgumentType.getString(context, "note"))))))
                                        .then(Commands.literal("leads_to")
                                                .then(Commands.argument(
                                                                "relation", StringArgumentType.word())
                                                        .then(Commands.argument(
                                                                        "note", StringArgumentType.greedyString())
                                                                .executes(context ->
                                                                        CaseHypothesisService.propose(
                                                                                context.getSource().getPlayerOrException(),
                                                                                CaseHypothesisStance.LEADS_TO,
                                                                                StringArgumentType.getString(context, "relation"),
                                                                                StringArgumentType.getString(context, "note")))))))
                                .then(Commands.literal("test").executes(context ->
                                        CaseHypothesisService.test(
                                                context.getSource().getPlayerOrException())))
                                .then(Commands.literal("reconsider").executes(context ->
                                        CaseHypothesisService.reconsider(
                                                context.getSource().getPlayerOrException())))
                                .then(Commands.literal("clear").executes(context ->
                                        CaseHypothesisService.clear(
                                                context.getSource().getPlayerOrException()))))
                        .then(Commands.literal("recover").executes(context ->
                                CommissionService.recoverCaseItems(
                                        context.getSource().getPlayerOrException())))
                        .then(Commands.literal("rotation")
                                .executes(context -> DynamicCaseService.show(
                                        context.getSource().getPlayerOrException()))
                                .then(Commands.literal("history").executes(context ->
                                        DynamicCaseService.showHistory(
                                                context.getSource().getPlayerOrException())))
                                .then(Commands.literal("contacts")
                                        .executes(context ->
                                                DynamicCaseService.showContacts(
                                                        context.getSource().getPlayerOrException()))
                                        .then(Commands.literal("history").executes(context ->
                                                DynamicCaseService.showContactHistory(
                                                        context.getSource().getPlayerOrException()))))
                                .then(Commands.literal("followup").executes(context ->
                                        DynamicCaseService.claimFollowUp(
                                                context.getSource().getPlayerOrException())))
                                .then(Commands.literal("response")
                                        .executes(context ->
                                                DynamicCaseService.showOrganizationResponse(
                                                        context.getSource().getPlayerOrException()))
                                        .then(Commands.literal("submit").executes(context ->
                                                DynamicCaseService.submitOrganizationResponse(
                                                        context.getSource().getPlayerOrException())))
                                        .then(Commands.literal("abandon").executes(context ->
                                                DynamicCaseService.abandonOrganizationResponse(
                                                        context.getSource().getPlayerOrException()))))
                                .then(Commands.literal("investigate")
                                        .then(Commands.literal("field").executes(context ->
                                                DynamicCaseService.investigate(
                                                        context.getSource().getPlayerOrException(),
                                                        DynamicCaseService.InvestigationRoute.FIELD)))
                                        .then(Commands.literal("desk").executes(context ->
                                                DynamicCaseService.investigate(
                                                        context.getSource().getPlayerOrException(),
                                                        DynamicCaseService.InvestigationRoute.DESK))))
                                .then(Commands.literal("conclude")
                                        .then(Commands.literal("human_concealment")
                                                .executes(context ->
                                                        DynamicCaseService.conclude(
                                                                context.getSource().getPlayerOrException(),
                                                                DynamicCaseProfile.Conclusion.HUMAN_CONCEALMENT)))
                                        .then(Commands.literal("extraordinary_distortion")
                                                .executes(context ->
                                                        DynamicCaseService.conclude(
                                                                context.getSource().getPlayerOrException(),
                                                                DynamicCaseProfile.Conclusion.EXTRAORDINARY_DISTORTION)))
                                        .then(Commands.literal("ritual_diversion")
                                                .executes(context ->
                                                        DynamicCaseService.conclude(
                                                                context.getSource().getPlayerOrException(),
                                                                DynamicCaseProfile.Conclusion.RITUAL_DIVERSION))))
                                .then(Commands.literal("recover").executes(context ->
                                        DynamicCaseService.recoverConclusion(
                                                context.getSource().getPlayerOrException())))))
                .then(Commands.literal("commission")
                        .executes(context -> CommissionService.showStatus(
                                context.getSource().getPlayerOrException()))
                        .then(Commands.literal("board").executes(context ->
                                InvestigationBoardService.openNearby(
                                        context.getSource().getPlayerOrException())))
                        .then(Commands.literal("list").executes(context ->
                                CommissionService.list(
                                        context.getSource().getPlayerOrException())))
                        .then(Commands.literal("status").executes(context ->
                                CommissionService.showStatus(
                                        context.getSource().getPlayerOrException())))
                        .then(Commands.literal("accept")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .executes(context -> CommissionService.accept(
                                                context.getSource().getPlayerOrException(),
                                                StringArgumentType.getString(context, "id")))))
                        .then(Commands.literal("abandon").executes(context ->
                                CommissionService.abandon(
                                        context.getSource().getPlayerOrException())))
                        .then(Commands.literal("approach")
                                .then(Commands.literal("assault").executes(context ->
                                        CommissionService.chooseRescueApproach(
                                                context.getSource().getPlayerOrException(),
                                                "assault")))
                                .then(Commands.literal("stealth").executes(context ->
                                        CommissionService.chooseRescueApproach(
                                                context.getSource().getPlayerOrException(),
                                                "stealth")))
                                .then(Commands.literal("divination").executes(context ->
                                        CommissionService.chooseRescueApproach(
                                                context.getSource().getPlayerOrException(),
                                                "divination")))))
                .then(Commands.literal("formula")
                        .executes(context -> FormulaAppraisalService.inspectHeld(
                                context.getSource().getPlayerOrException()))
                        .then(Commands.literal("inspect").executes(context ->
                                FormulaAppraisalService.inspectHeld(
                                        context.getSource().getPlayerOrException())))
                        .then(Commands.literal("verdict")
                                .then(Commands.literal("authentic").executes(context ->
                                        FormulaAppraisalService.submitVerdict(
                                                context.getSource().getPlayerOrException(),
                                                true)))
                                .then(Commands.literal("forged").executes(context ->
                                        FormulaAppraisalService.submitVerdict(
                                                context.getSource().getPlayerOrException(),
                                                false)))))
                .then(Commands.literal("party")
                        .executes(context -> QuestPartyService.showStatus(
                                context.getSource().getPlayerOrException()))
                        .then(Commands.literal("sync").executes(context ->
                                QuestPartyService.joinAndSync(
                                        context.getSource().getPlayerOrException()))))
                .then(Commands.literal("rules").executes(context ->
                        showLines(context.getSource().getPlayerOrException(), "rules", 5)))
                .then(Commands.literal("items").executes(context ->
                        showLines(context.getSource().getPlayerOrException(), "items", 9)))
                .then(Commands.literal("bestiary").executes(context ->
                        showLines(context.getSource().getPlayerOrException(), "bestiary", 4)))
                .then(Commands.literal("journal").executes(context ->
                        showJournal(context.getSource().getPlayerOrException())))
                .then(Commands.literal("m1check").executes(context ->
                        showM1Check(context.getSource().getPlayerOrException())))
                .then(Commands.literal("doctor").executes(context ->
                        showDiagnostics(context.getSource().getPlayerOrException())))
                .then(Commands.literal("servercheck")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> showServerDiagnostics(context.getSource())))
                .then(Commands.literal("trial")
                        .then(Commands.literal("start").executes(context ->
                                startTrial(context.getSource().getPlayerOrException())))
                        .then(Commands.literal("resume").executes(context ->
                                startTrial(context.getSource().getPlayerOrException())))
                        .then(Commands.literal("status").executes(context ->
                                showTrial(context.getSource().getPlayerOrException())))
                        .then(Commands.literal("verify").executes(context ->
                                verifyTrial(context.getSource().getPlayerOrException())))
                        .then(Commands.literal("report").executes(context ->
                                showTrialReport(context.getSource().getPlayerOrException())))
                        .then(Commands.literal("stop").executes(context ->
                                stopTrial(context.getSource().getPlayerOrException())))
                        .then(Commands.literal("reset").executes(context ->
                                resetTrial(context.getSource().getPlayerOrException())))));
    }

    private static int showStatus(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        player.sendSystemMessage(Component.translatable(
                "command.lord_of_mysteries.status",
                Component.translatable(KnowledgeText.pathwayTranslationKey(
                        data.pathway == null ? "" : data.pathway.toString())),
                data.sequence,
                Math.round(data.spirituality),
                Math.round(data.spiritualityMax),
                Math.round(data.digestion),
                Math.round(data.pollution),
                Math.round(data.insanityPressure))
                .withStyle(ChatFormatting.AQUA));
        return 1;
    }

    private static int reflect(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        ActingIdentityService.ReflectionResult result =
                ActingIdentityService.reflect(player);
        String suffix = switch (result) {
            case SUCCESS -> "success";
            case COMMONER -> "commoner";
            case ALREADY_REFLECTED -> "cooldown";
        };
        player.sendSystemMessage(Component.translatable(
                "command.lord_of_mysteries.reflect." + suffix,
                String.format(java.util.Locale.ROOT, "%.1f", data.principleInsight),
                String.format(java.util.Locale.ROOT, "%.1f", data.roleOveridentification))
                .withStyle(result == ActingIdentityService.ReflectionResult.SUCCESS
                        ? ChatFormatting.AQUA : ChatFormatting.GRAY));
        return result == ActingIdentityService.ReflectionResult.SUCCESS ? 1 : 0;
    }

    private static int showM1Check(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (SeerPotionItem.SEER_PATHWAY.equals(data.pathway)
                && data.sequence <= 7) {
            String suffix = !data.identityAnchored ? "identity"
                    : data.actingReflectionCount <= 0 ? "reflection"
                    : data.cityWorkShifts <= 0 ? "street_life" : "complete";
            player.sendSystemMessage(Component.translatable(
                    "command.lord_of_mysteries.m1check." + suffix)
                    .withStyle(suffix.equals("complete")
                            ? ChatFormatting.GREEN : ChatFormatting.LIGHT_PURPLE));
            return 1;
        }
        M1Readiness.Stage stage = M1Readiness.evaluate(
                data.pathway == null ? null : data.pathway.toString(),
                data.sequence, data.digestion);
        player.sendSystemMessage(Component.translatable(
                "command.lord_of_mysteries.m1check." + stage.name().toLowerCase())
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        return 1;
    }

    private static int startTrial(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (data.m1TrialActive) {
            player.sendSystemMessage(Component.translatable(
                    "command.lord_of_mysteries.trial.already_active")
                    .withStyle(ChatFormatting.YELLOW));
            return 0;
        }
        boolean resumed = hasTrialRecord(data);
        if (!resumed) clearTrial(data);
        data.m1TrialActive = true;
        data.m1TrialStartTick = player.level().getGameTime();
        if (SeerPotionItem.SEER_PATHWAY.equals(data.pathway)) {
            data.m1TrialBestSequence = data.sequence;
        }
        M1TrialTracker.refresh(player, data);
        player.sendSystemMessage(Component.translatable(
                resumed
                        ? "command.lord_of_mysteries.trial.resumed"
                        : "command.lord_of_mysteries.trial.started")
                .withStyle(ChatFormatting.GOLD));
        showTrial(player);
        return 1;
    }

    private static int stopTrial(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (!data.m1TrialActive) {
            player.sendSystemMessage(Component.translatable(
                    "command.lord_of_mysteries.trial.not_active")
                    .withStyle(ChatFormatting.YELLOW));
            return 0;
        }
        M1TrialTracker.refresh(player, data);
        data.m1TrialElapsedTicks = trialElapsed(player, data);
        data.m1TrialStartTick = -1L;
        data.m1TrialActive = false;
        player.sendSystemMessage(Component.translatable(
                "command.lord_of_mysteries.trial.stopped")
                .withStyle(ChatFormatting.YELLOW));
        showTrial(player);
        return 1;
    }

    private static int resetTrial(ServerPlayer player) {
        clearTrial(MysteryCapability.get(player));
        player.sendSystemMessage(Component.translatable(
                "command.lord_of_mysteries.trial.reset")
                .withStyle(ChatFormatting.GRAY));
        return 1;
    }

    private static int showTrial(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (data.m1TrialActive) M1TrialTracker.refresh(player, data);
        long elapsed = trialElapsed(player, data);
        M1TrialProgress.Result result = M1TrialProgress.evaluate(
                elapsed,
                data.m1TrialCampVisited,
                data.m1TrialBestSequence,
                data.m1TrialOccultKills,
                data.m1TrialActingEvents,
                data.m1TrialMaxPressure,
                data.m1TrialMaxPollution,
                data.identityAnchored,
                data.actingReflectionCount > 0,
                data.cityWorkShifts > 0);
        player.sendSystemMessage(Component.translatable(
                "command.lord_of_mysteries.trial.title",
                M1TrialProgress.formatDuration(elapsed),
                Component.translatable(data.m1TrialActive
                        ? "command.lord_of_mysteries.trial.active"
                        : "command.lord_of_mysteries.trial.inactive"))
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        sendTrialGoal(player, result.durationComplete(), "duration",
                M1TrialProgress.formatDuration(elapsed));
        sendTrialGoal(player, result.campVisited(), "camp");
        sendTrialGoal(player, result.sequenceComplete(), "sequence",
                data.m1TrialBestSequence < 0 ? "-" : data.m1TrialBestSequence);
        sendTrialGoal(player, result.killsComplete(), "kills",
                data.m1TrialOccultKills, M1TrialProgress.REQUIRED_OCCULT_KILLS);
        sendTrialGoal(player, result.actingComplete(), "acting",
                data.m1TrialActingEvents, M1TrialProgress.REQUIRED_ACTING_EVENTS);
        sendTrialGoal(player, result.riskObserved(), "risk",
                Math.round(data.m1TrialMaxPressure), Math.round(data.m1TrialMaxPollution));
        sendTrialGoal(player, result.identityAnchored(), "identity");
        sendTrialGoal(player, result.reflectionCompleted(), "reflection");
        sendTrialGoal(player, result.streetLifeCompleted(), "street_life",
                data.cityWorkShifts);
        player.sendSystemMessage(Component.translatable(
                "command.lord_of_mysteries.trial.stats",
                data.m1TrialDeaths,
                data.m1TrialRestRecoveries,
                data.m1TrialCharmsConsumed)
                .withStyle(ChatFormatting.DARK_GRAY));
        player.sendSystemMessage(Component.translatable(
                result.passed()
                        ? "command.lord_of_mysteries.trial.passed"
                        : "command.lord_of_mysteries.trial.pending",
                result.completedGoals())
                .withStyle(result.passed() ? ChatFormatting.GREEN : ChatFormatting.YELLOW));
        M1TrialContinuity.Result continuity = M1TrialContinuity.evaluate(
                data.m1TrialReconnects,
                data.m1TrialServerRestarts,
                data.m1TrialDimensionChanges,
                data.m1TrialDeathRecoveries);
        sendTrialGoal(player, continuity.reconnectComplete(), "reconnect",
                data.m1TrialReconnects, M1TrialContinuity.REQUIRED_RECONNECTS);
        sendTrialGoal(player, continuity.restartComplete(), "restart",
                data.m1TrialServerRestarts, M1TrialContinuity.REQUIRED_SERVER_RESTARTS);
        sendTrialGoal(player, continuity.dimensionComplete(), "dimension",
                data.m1TrialDimensionChanges,
                M1TrialContinuity.REQUIRED_DIMENSION_CHANGES);
        sendTrialGoal(player, continuity.deathComplete(), "death_recovery",
                data.m1TrialDeathRecoveries,
                M1TrialContinuity.REQUIRED_DEATH_RECOVERIES);
        player.sendSystemMessage(Component.translatable(
                continuity.passed()
                        ? "command.lord_of_mysteries.trial.continuity_passed"
                        : "command.lord_of_mysteries.trial.continuity_pending",
                continuity.completedGoals())
                .withStyle(continuity.passed()
                        ? ChatFormatting.GREEN : ChatFormatting.YELLOW));
        return Math.max(1, result.completedGoals());
    }

    private static int verifyTrial(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (data.m1TrialActive) M1TrialTracker.refresh(player, data);
        long elapsed = trialElapsed(player, data);
        M1TrialProgress.Result core = M1TrialProgress.evaluate(
                elapsed, data.m1TrialCampVisited, data.m1TrialBestSequence,
                data.m1TrialOccultKills, data.m1TrialActingEvents,
                data.m1TrialMaxPressure, data.m1TrialMaxPollution,
                data.identityAnchored, data.actingReflectionCount > 0,
                data.cityWorkShifts > 0);
        M1TrialContinuity.Result continuity = M1TrialContinuity.evaluate(
                data.m1TrialReconnects, data.m1TrialServerRestarts,
                data.m1TrialDimensionChanges, data.m1TrialDeathRecoveries);
        showTrial(player);
        boolean verified = core.passed() && continuity.passed();
        player.sendSystemMessage(Component.translatable(verified
                        ? "command.lord_of_mysteries.trial.verify_passed"
                        : "command.lord_of_mysteries.trial.verify_pending")
                .withStyle(verified ? ChatFormatting.GREEN : ChatFormatting.RED));
        return verified ? 1 : 0;
    }

    private static int showTrialReport(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (data.m1TrialActive) M1TrialTracker.refresh(player, data);
        M1TrialTimeline.Result result = M1TrialTimeline.evaluate(
                data.m1TrialCampReachedTick,
                data.m1TrialSequence9Tick,
                data.m1TrialSequence8Tick,
                data.m1TrialSequence7Tick,
                data.m1TrialIdentityAnchoredTick,
                data.m1TrialReflectionCompletedTick,
                data.m1TrialStreetLifeCompletedTick);
        player.sendSystemMessage(Component.translatable(
                "command.lord_of_mysteries.trial.report.title")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        sendTrialMilestone(player, "camp", result.camp());
        sendTrialMilestone(player, "sequence9", result.sequence9());
        sendTrialMilestone(player, "sequence8", result.sequence8());
        sendTrialMilestone(player, "sequence7", result.sequence7());
        sendTrialMilestone(player, "identity", result.identity());
        sendTrialMilestone(player, "reflection", result.reflection());
        sendTrialMilestone(player, "street_life", result.streetLife());
        player.sendSystemMessage(Component.translatable(
                result.onSchedule()
                        ? "command.lord_of_mysteries.trial.report.on_schedule"
                        : "command.lord_of_mysteries.trial.report.needs_review",
                result.recordedMilestones(), result.onTimeMilestones())
                .withStyle(result.onSchedule()
                        ? ChatFormatting.GREEN : ChatFormatting.YELLOW));
        player.sendSystemMessage(Component.translatable(
                "command.lord_of_mysteries.trial.report.first_events",
                formatOptionalDuration(data.m1TrialFirstOccultKillTick),
                formatOptionalDuration(data.m1TrialFirstActingTick),
                formatOptionalDuration(data.m1TrialRiskReachedTick))
                .withStyle(ChatFormatting.DARK_GRAY));
        return result.recordedMilestones();
    }

    private static void sendTrialMilestone(
            ServerPlayer player, String milestone,
            M1TrialTimeline.Milestone result) {
        Component status = Component.translatable(result.recorded()
                ? result.onTime()
                ? "command.lord_of_mysteries.trial.report.status.on_time"
                : "command.lord_of_mysteries.trial.report.status.late"
                : "command.lord_of_mysteries.trial.report.status.pending");
        player.sendSystemMessage(Component.translatable(
                "command.lord_of_mysteries.trial.report.line",
                Component.translatable(
                        "command.lord_of_mysteries.trial.report." + milestone),
                formatOptionalDuration(result.actualTick()),
                M1TrialProgress.formatDuration(result.targetTick()),
                status).withStyle(result.onTime()
                        ? ChatFormatting.GREEN : ChatFormatting.GRAY));
    }

    private static String formatOptionalDuration(long tick) {
        return tick < 0L ? "--:--:--" : M1TrialProgress.formatDuration(tick);
    }

    private static void sendTrialGoal(ServerPlayer player, boolean complete,
                                      String goal, Object... args) {
        player.sendSystemMessage(Component.literal(complete ? "✔ " : "· ")
                .append(Component.translatable(
                        "command.lord_of_mysteries.trial.goal." + goal, args))
                .withStyle(complete ? ChatFormatting.GREEN : ChatFormatting.GRAY));
    }

    private static long trialElapsed(ServerPlayer player, PlayerMysteryData data) {
        return M1TrialTimer.elapsed(data.m1TrialElapsedTicks,
                data.m1TrialActive, data.m1TrialStartTick,
                player.level().getGameTime());
    }

    private static boolean hasTrialRecord(PlayerMysteryData data) {
        return data.m1TrialElapsedTicks > 0L || data.m1TrialCampVisited
                || data.m1TrialBestSequence >= 0 || data.m1TrialOccultKills > 0
                || data.m1TrialDeaths > 0 || data.m1TrialRestRecoveries > 0
                || data.m1TrialCharmsConsumed > 0 || data.m1TrialActingEvents > 0
                || data.m1TrialMaxPressure > 0f || data.m1TrialMaxPollution > 0f
                || data.m1TrialReconnects > 0 || data.m1TrialServerRestarts > 0
                || data.m1TrialDimensionChanges > 0
                || data.m1TrialDeathRecoveries > 0
                || data.m1TrialCampReachedTick >= 0L
                || data.m1TrialSequence9Tick >= 0L
                || data.m1TrialSequence8Tick >= 0L
                || data.m1TrialSequence7Tick >= 0L
                || data.m1TrialIdentityAnchoredTick >= 0L
                || data.m1TrialReflectionCompletedTick >= 0L
                || data.m1TrialStreetLifeCompletedTick >= 0L;
    }

    private static void clearTrial(PlayerMysteryData data) {
        data.m1TrialActive = false;
        data.m1TrialStartTick = -1L;
        data.m1TrialElapsedTicks = 0L;
        data.m1TrialCampVisited = false;
        data.m1TrialBestSequence = -1;
        data.m1TrialOccultKills = 0;
        data.m1TrialDeaths = 0;
        data.m1TrialRestRecoveries = 0;
        data.m1TrialCharmsConsumed = 0;
        data.m1TrialActingEvents = 0;
        data.m1TrialMaxPressure = 0f;
        data.m1TrialMaxPollution = 0f;
        data.m1TrialReconnects = 0;
        data.m1TrialServerRestarts = 0;
        data.m1TrialDimensionChanges = 0;
        data.m1TrialDeathRecoveries = 0;
        data.m1TrialPendingReconnect = false;
        data.m1TrialSessionId = "";
        data.m1TrialCampReachedTick = -1L;
        data.m1TrialSequence9Tick = -1L;
        data.m1TrialSequence8Tick = -1L;
        data.m1TrialSequence7Tick = -1L;
        data.m1TrialFirstOccultKillTick = -1L;
        data.m1TrialFirstActingTick = -1L;
        data.m1TrialRiskReachedTick = -1L;
        data.m1TrialIdentityAnchoredTick = -1L;
        data.m1TrialReflectionCompletedTick = -1L;
        data.m1TrialStreetLifeCompletedTick = -1L;
    }

    private static int showServerDiagnostics(CommandSourceStack source) {
        int commissions = CommissionDefinitionManager.all().size();
        int quests = QuestChainDefinitionManager.all().size();
        ServerLevel overworld = source.getServer().getLevel(Level.OVERWORLD);
        boolean worldReady = overworld != null;
        boolean partyStorageReady = overworld != null;
        QuestPartySavedData partyStorage = overworld == null
                ? null : QuestPartySavedData.get(overworld);
        MistCityWorldEventSavedData eventStorage = overworld == null
                ? null : MistCityWorldEventSavedData.get(overworld);
        long currentDay = overworld == null
                ? 0L : Math.floorDiv(overworld.getDayTime(), 24_000L);
        MistCityWorldEvent worldEvent = overworld == null
                ? MistCityWorldEvent.CLEAR
                : MistCityWorldEventPolicy.eventForDay(
                        overworld.getSeed(), currentDay);
        if (eventStorage != null) {
            eventStorage.update(currentDay, worldEvent);
        }
        MistCityOutpostSavedData outpostStorage = overworld == null
                ? null : MistCityOutpostSavedData.get(overworld);
        boolean healthy = commissions > 0 && quests > 0 && worldReady
                && partyStorageReady && eventStorage != null
                && NetworkProtocol.PACKET_COUNT > 0;
        String marker = "PROJECT_MYSTERY_SERVERCHECK_"
                + (healthy ? "OK" : "FAILED")
                + " commissions=" + commissions
                + " quests=" + quests
                + " protocol=" + NetworkProtocol.VERSION
                + " packets=" + NetworkProtocol.PACKET_COUNT
                + " overworld=" + worldReady
                + " party_storage=" + partyStorageReady
                + " active_parties=" + (partyStorage == null
                        ? 0 : partyStorage.activePartyCount())
                + " party_members=" + (partyStorage == null
                        ? 0 : partyStorage.activeMemberCount())
                + " world_event=" + worldEvent.id()
                + " event_day=" + currentDay
                + " city_service_version=" + (outpostStorage == null
                        ? 0 : outpostStorage.serviceVersion());
        if (healthy) {
            source.sendSuccess(() -> Component.literal(marker), false);
            return 1;
        }
        source.sendFailure(Component.literal(marker));
        return 0;
    }

    private static int showDiagnostics(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        int repairs = data.sanitize();
        int errors = 0;
        player.sendSystemMessage(Component.translatable(
                "command.lord_of_mysteries.doctor.title")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        player.sendSystemMessage(Component.translatable(
                repairs == 0
                        ? "command.lord_of_mysteries.doctor.player_ok"
                        : "command.lord_of_mysteries.doctor.player_repaired",
                repairs).withStyle(repairs == 0
                        ? ChatFormatting.GREEN : ChatFormatting.YELLOW));
        player.sendSystemMessage(Component.translatable(
                "command.lord_of_mysteries.doctor.data",
                CommissionDefinitionManager.all().size(),
                QuestChainDefinitionManager.all().size())
                .withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.translatable(
                "command.lord_of_mysteries.doctor.protocol",
                NetworkProtocol.VERSION, NetworkProtocol.PACKET_COUNT)
                .withStyle(ChatFormatting.GRAY));

        if (!data.activeQuestChainId.isBlank()) {
            ResourceLocation questId = ResourceLocation.tryParse(data.activeQuestChainId);
            ResourceLocation commissionId = ResourceLocation.tryParse(data.activeCommissionId);
            boolean valid = questId != null && commissionId != null
                    && QuestChainDefinitionManager.get(questId) != null
                    && CommissionDefinitionManager.get(commissionId) != null;
            if (!valid) errors++;
            player.sendSystemMessage(Component.translatable(valid
                            ? "command.lord_of_mysteries.doctor.active_ok"
                            : "command.lord_of_mysteries.doctor.active_invalid")
                    .withStyle(valid ? ChatFormatting.GREEN : ChatFormatting.RED));
        } else {
            player.sendSystemMessage(Component.translatable(
                    "command.lord_of_mysteries.doctor.active_none")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }

        ServerLevel overworld = player.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) {
            errors++;
        } else {
            BlockPos camp = CampGenerationSavedData.get(overworld)
                    .nearestCamp(player.blockPosition())
                    .orElseGet(() -> AbandonedCampGenerator.starterCampTarget(overworld));
            BlockPos outpost = MistCityOutpostSavedData.get(overworld).outpost()
                    .orElseGet(() -> MistCityOutpostGenerator.starterOutpostTarget(overworld));
            InvestigationSiteSavedData sites = InvestigationSiteSavedData.get(overworld);
            BlockPos church = sites.church()
                    .orElseGet(() -> InvestigationSiteGenerator.churchTarget(overworld));
            BlockPos cultistCamp = sites.cultistCamp()
                    .orElseGet(() -> InvestigationSiteGenerator.cultistCampTarget(overworld));
            BlockPos occultistHut = sites.occultistHut()
                    .orElseGet(() -> InvestigationSiteGenerator.occultistHutTarget(overworld));
            player.sendSystemMessage(Component.translatable(
                    "command.lord_of_mysteries.doctor.world",
                    camp.getX(), camp.getZ(), outpost.getX(), outpost.getZ(),
                    church.getX(), church.getZ(), cultistCamp.getX(), cultistCamp.getZ(),
                    occultistHut.getX(), occultistHut.getZ())
                    .withStyle(ChatFormatting.GRAY));
        }
        player.sendSystemMessage(Component.translatable(errors == 0
                        ? "command.lord_of_mysteries.doctor.passed"
                        : "command.lord_of_mysteries.doctor.failed",
                errors).withStyle(errors == 0
                        ? ChatFormatting.GREEN : ChatFormatting.RED));
        return errors == 0 ? 1 : 0;
    }

    private static ItemStack findCompass(ServerPlayer player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(ModItems.INVESTIGATOR_COMPASS.get())) return stack;
        }
        return new ItemStack(ModItems.INVESTIGATOR_COMPASS.get());
    }

    private static int showLines(ServerPlayer player, String section, int count) {
        player.sendSystemMessage(Component.translatable(
                "command.lord_of_mysteries." + section + ".title")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        for (int line = 1; line <= count; line++) {
            player.sendSystemMessage(Component.translatable(
                    "command.lord_of_mysteries." + section + "." + line)
                    .withStyle(ChatFormatting.GRAY));
        }
        return count;
    }

    private static int showJournal(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        player.sendSystemMessage(Component.translatable(
                "command.lord_of_mysteries.journal.title")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        if (data.knownKnowledge.isEmpty()) {
            player.sendSystemMessage(Component.translatable(
                    "command.lord_of_mysteries.journal.empty")
                    .withStyle(ChatFormatting.GRAY));
            return 0;
        }
        data.knownKnowledge.stream()
                .map(Object::toString)
                .sorted()
                .limit(20)
                .forEach(id -> player.sendSystemMessage(Component.literal("• ")
                        .append(Component.translatable(KnowledgeText.translationKey(id)))
                        .withStyle(ChatFormatting.GRAY)));
        return data.knownKnowledge.size();
    }
}
