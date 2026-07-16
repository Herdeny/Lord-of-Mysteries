package top.aurora.lordofmysteries.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.knowledge.InvestigatorCompassItem;
import top.aurora.lordofmysteries.knowledge.InvestigatorNotesItem;
import top.aurora.lordofmysteries.knowledge.GuideJournalProgress;
import top.aurora.lordofmysteries.knowledge.KnowledgeText;
import top.aurora.lordofmysteries.knowledge.M1Readiness;
import top.aurora.lordofmysteries.knowledge.M1TrialProgress;
import top.aurora.lordofmysteries.knowledge.M1TrialTracker;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.potion.SeerPotionItem;
import top.aurora.lordofmysteries.registry.ModItems;
import top.aurora.lordofmysteries.commission.CommissionService;
import top.aurora.lordofmysteries.world.MistCityOutpostGenerator;
import top.aurora.lordofmysteries.world.InvestigationSiteGenerator;

@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ProjectMysteryCommands {

    private ProjectMysteryCommands() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("pm")
                .then(Commands.literal("guide").executes(context -> {
                    InvestigatorNotesItem.showGuide(context.getSource().getPlayerOrException());
                    return 1;
                }))
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
                .then(Commands.literal("case").executes(context -> {
                    InvestigationSiteGenerator.reportSites(
                            context.getSource().getPlayerOrException());
                    return 1;
                }))
                .then(Commands.literal("commission")
                        .executes(context -> CommissionService.showStatus(
                                context.getSource().getPlayerOrException()))
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
                .then(Commands.literal("trial")
                        .then(Commands.literal("start").executes(context ->
                                startTrial(context.getSource().getPlayerOrException())))
                        .then(Commands.literal("status").executes(context ->
                                showTrial(context.getSource().getPlayerOrException())))
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

    private static int showM1Check(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
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
        clearTrial(data);
        data.m1TrialActive = true;
        data.m1TrialStartTick = player.level().getGameTime();
        if (SeerPotionItem.SEER_PATHWAY.equals(data.pathway)) {
            data.m1TrialBestSequence = data.sequence;
        }
        M1TrialTracker.refresh(player, data);
        player.sendSystemMessage(Component.translatable(
                "command.lord_of_mysteries.trial.started")
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
        data.m1TrialStartTick = 0L;
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
                data.m1TrialMaxPollution);
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
        return Math.max(1, result.completedGoals());
    }

    private static void sendTrialGoal(ServerPlayer player, boolean complete,
                                      String goal, Object... args) {
        player.sendSystemMessage(Component.literal(complete ? "✔ " : "· ")
                .append(Component.translatable(
                        "command.lord_of_mysteries.trial.goal." + goal, args))
                .withStyle(complete ? ChatFormatting.GREEN : ChatFormatting.GRAY));
    }

    private static long trialElapsed(ServerPlayer player, PlayerMysteryData data) {
        if (!data.m1TrialActive) return data.m1TrialElapsedTicks;
        return data.m1TrialElapsedTicks + Math.max(
                0L, player.level().getGameTime() - data.m1TrialStartTick);
    }

    private static void clearTrial(PlayerMysteryData data) {
        data.m1TrialActive = false;
        data.m1TrialStartTick = 0L;
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
