package top.aurora.lordofmysteries.commission;

import java.util.Comparator;
import java.util.Optional;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.registry.ModItems;
import top.aurora.lordofmysteries.world.AbandonedCampGenerator;
import top.aurora.lordofmysteries.world.CampGenerationSavedData;
import top.aurora.lordofmysteries.world.MistCityOutpostSavedData;

public final class CommissionService {

    public static final ResourceLocation LOST_CAT = id("commission/lost_cat");
    public static final ResourceLocation MISSING_SQUAD =
            id("commission/missing_investigation_squad");

    private CommissionService() {}

    public static int interactBoard(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (!data.activeCommissionId.isBlank()) {
            recordObjective(player, "talk_npc", "nighthawk_contact", 1);
            if (isReadyToSettle(data)) return settle(player);
            return showStatus(player);
        }
        ResourceLocation recommended = !data.completedCommissions.contains(LOST_CAT)
                ? LOST_CAT : MISSING_SQUAD;
        if (data.completedCommissions.contains(recommended)) return list(player);
        int accepted = accept(player, recommended);
        if (accepted > 0) {
            recordObjective(player, "enter_structure", "mist_city_outpost", 1);
        }
        return accepted;
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
                                : Component.empty())
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
        giveCommissionPaper(player, definition, now);
        player.sendSystemMessage(Component.translatable(
                "command.lord_of_mysteries.commission.accepted",
                Component.translatable(definition.titleKey()))
                .withStyle(ChatFormatting.GOLD));
        recordObjective(player, "talk_npc", "press_clerk", 1);
        showCurrentStep(player, data, chain);
        return 1;
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
        return 1;
    }

    public static int abandon(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (data.activeCommissionId.isBlank()) return 0;
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
        ResourceLocation commissionId = ResourceLocation.tryParse(data.activeCommissionId);
        CommissionDefinition definition = commissionId == null
                ? null : CommissionDefinitionManager.get(commissionId);
        if (definition == null || !isReadyToSettle(data)) return 0;
        data.moneyPence += definition.reward().pence();
        definition.reward().reputation().forEach((organization, amount) ->
                data.orgReputation.merge(organization, amount, Integer::sum));
        data.completedCommissions.add(definition.id());
        data.commissionCooldowns.put(definition.id(),
                player.level().getGameTime() + definition.cooldownTicks());
        if (MISSING_SQUAD.equals(definition.id())) {
            giveItem(player, new ItemStack(ModItems.BURNT_LIST.get()));
            data.knownKnowledge.add(id("knowledge/m2/missing_squad_chain"));
        } else if (LOST_CAT.equals(definition.id())) {
            giveItem(player, new ItemStack(ModItems.NEWSPAPER.get()));
        }
        player.sendSystemMessage(Component.translatable(
                "command.lord_of_mysteries.commission.settled",
                Component.translatable(definition.titleKey()),
                CommissionCurrency.format(definition.reward().pence()))
                .withStyle(ChatFormatting.GREEN));
        clearActive(data);
        return 1;
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
