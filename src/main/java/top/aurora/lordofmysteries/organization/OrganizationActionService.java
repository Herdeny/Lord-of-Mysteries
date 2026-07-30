package top.aurora.lordofmysteries.organization;

import java.util.Comparator;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.artifact.SealedArtifactService;
import top.aurora.lordofmysteries.commission.CommissionCurrency;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerDataSection;
import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.registry.ModItems;
import top.aurora.lordofmysteries.world.MistCityOutpostSavedData;
import top.aurora.lordofmysteries.world.MistCityWorldEvent;
import top.aurora.lordofmysteries.world.MistCityWorldEventPolicy;

@Mod.EventBusSubscriber(
        modid = ProjectMystery.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class OrganizationActionService {

    public static final int PATROL_KILL_TARGET = 3;
    public static final int RELIEF_BREAD_COST = 8;
    private static final double SUBMIT_DISTANCE_SQUARED = 100d;
    private static final double PATROL_DISTANCE_SQUARED = 4096d;

    private OrganizationActionService() {}

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || event.getServer().getTickCount() % 100 != 0) {
            return;
        }
        ServerLevel level = event.getServer().getLevel(Level.OVERWORLD);
        if (level == null || OrganizationDefinitionManager.all().isEmpty()) {
            return;
        }
        long day = currentDay(level);
        float exposure = actionExposure(level);
        OrganizationActionSavedData saved =
                OrganizationActionSavedData.get(level);
        if (!saved.refresh(
                level.getSeed(), day, exposure,
                OrganizationDefinitionManager.all())) {
            return;
        }
        event.getServer().getPlayerList().getPlayers().forEach(player ->
                player.sendSystemMessage(Component.translatable(
                                "message.lord_of_mysteries.organization.actions_changed",
                                saved.actions().size())
                        .withStyle(ChatFormatting.GOLD)));
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity()
                instanceof ServerPlayer player)
                || event.getEntity().getType().getCategory()
                        != MobCategory.MONSTER
                || !nearOutpost(player, PATROL_DISTANCE_SQUARED)) {
            return;
        }
        OrganizationActionSavedData saved =
                OrganizationActionSavedData.get(
                        player.getServer().overworld());
        OrganizationActionPolicy.PlannedAction action =
                saved.assignedAction(player.getUUID());
        if (action == null
                || action.type()
                        != OrganizationActionType.NIGHT_PATROL) {
            return;
        }
        int progress = saved.addProgress(player.getUUID(), 1);
        player.sendSystemMessage(Component.translatable(
                        "message.lord_of_mysteries.organization.patrol_progress",
                        progress, PATROL_KILL_TARGET)
                .withStyle(ChatFormatting.GRAY));
    }

    public static int showGuide(ServerPlayer player) {
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.m4.guide")
                .withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.m4.boundary")
                .withStyle(ChatFormatting.GRAY));
        showOrganizations(player);
        return showActions(player);
    }

    public static int showOrganizations(ServerPlayer player) {
        var definitions = OrganizationDefinitionManager.all()
                .values().stream()
                .sorted(Comparator.comparing(
                        value -> value.id().toString()))
                .toList();
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.organization.catalog",
                        definitions.size())
                .withStyle(ChatFormatting.GOLD));
        PlayerMysteryData data = MysteryCapability.get(player);
        for (OrganizationDefinition definition : definitions) {
            int reputation = data.orgReputation.getOrDefault(
                    definition.id(), 0);
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.organization.entry",
                            definition.id().getPath(),
                            Component.translatable(definition.titleKey()),
                            definition.kind().id(),
                            reputation)
                    .withStyle(ChatFormatting.GRAY));
        }
        return definitions.size();
    }

    public static int showActions(ServerPlayer player) {
        OrganizationActionSavedData saved = storage(player);
        ensureCurrent(player, saved);
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.organization.actions",
                        saved.currentDay(), saved.actions().size())
                .withStyle(ChatFormatting.GOLD));
        for (OrganizationActionPolicy.PlannedAction action
                : saved.actions()) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.organization.action_entry",
                            action.slot(),
                            organizationName(action.organization()),
                            Component.translatable(
                                    action.type().translationKey()),
                            action.risk())
                    .withStyle(ChatFormatting.GRAY));
        }
        OrganizationActionSavedData.Assignment assignment =
                saved.assignment(player.getUUID());
        OrganizationActionPolicy.PlannedAction assigned =
                saved.assignedAction(player.getUUID());
        if (assignment != null && assigned != null) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.organization.assigned",
                            assigned.slot(),
                            Component.translatable(
                                    assigned.type().translationKey()),
                            assignment.progress())
                    .withStyle(ChatFormatting.AQUA));
        }
        return saved.actions().size();
    }

    public static int claim(ServerPlayer player, int slot) {
        OrganizationActionSavedData saved = storage(player);
        ensureCurrent(player, saved);
        if (saved.assignment(player.getUUID()) != null) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.organization.already_assigned")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        OrganizationActionPolicy.PlannedAction action =
                saved.action(slot);
        if (action == null
                || !saved.assign(
                        player.getUUID(), slot,
                        player.serverLevel().getGameTime())) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.organization.claim_denied")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.organization.claimed",
                        Component.translatable(
                                action.type().translationKey()),
                        organizationName(action.organization()))
                .withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.translatable(
                        objectiveKey(action.type()))
                .withStyle(ChatFormatting.GRAY));
        return 1;
    }

    public static int abandon(ServerPlayer player) {
        if (!storage(player).abandon(player.getUUID())) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.organization.no_assignment")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.organization.abandoned")
                .withStyle(ChatFormatting.YELLOW));
        return 1;
    }

    public static int submit(ServerPlayer player) {
        OrganizationActionSavedData saved = storage(player);
        ensureCurrent(player, saved);
        OrganizationActionPolicy.PlannedAction action =
                saved.assignedAction(player.getUUID());
        OrganizationActionSavedData.Assignment assignment =
                saved.assignment(player.getUUID());
        if (action == null || assignment == null) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.organization.no_assignment")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        if (!nearOutpost(player, SUBMIT_DISTANCE_SQUARED)) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.organization.not_at_desk")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        if (!objectiveComplete(player, action, assignment)) {
            player.sendSystemMessage(Component.translatable(
                            objectiveKey(action.type()))
                    .withStyle(ChatFormatting.YELLOW));
            return 0;
        }
        if (!saved.complete(player.getUUID())) return 0;
        PlayerMysteryData data = MysteryCapability.get(player);
        int reputation = Math.max(1, action.risk());
        long reward = 12L + action.risk() * 12L;
        data.orgReputation.merge(
                action.organization(), reputation, Integer::sum);
        data.moneyPence = saturatingAdd(data.moneyPence, reward);
        data.markDirty(PlayerDataSection.SOCIAL);
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.organization.completed",
                        organizationName(action.organization()),
                        reputation,
                        CommissionCurrency.format(reward))
                .withStyle(ChatFormatting.GOLD));
        return 1;
    }

    private static boolean objectiveComplete(
            ServerPlayer player,
            OrganizationActionPolicy.PlannedAction action,
            OrganizationActionSavedData.Assignment assignment) {
        return switch (action.type()) {
            case NIGHT_PATROL ->
                    assignment.progress() >= PATROL_KILL_TARGET;
            case ARTIFACT_TRANSFER ->
                    SealedArtifactService.returnFirstHeld(
                            player, true) > 0;
            case HERESY_REVIEW ->
                    player.getInventory().countItem(
                            ModItems.DYNAMIC_EVIDENCE_PORTFOLIO.get()) > 0;
            case DISASTER_RELIEF ->
                    consume(player, Items.BREAD, RELIEF_BREAD_COST);
            case HIGH_COUNCIL ->
                    MysteryCapability.get(player).sequence <= 5
                            && MysteryCapability.get(player)
                            .orgReputation.getOrDefault(
                                    action.organization(), 0) >= 8;
            case SECRET_RECRUITMENT ->
                    MysteryCapability.get(player)
                            .orgReputation.getOrDefault(
                                    action.organization(), 0) >= 4
                            && player.getInventory().countItem(
                                    ModItems.KNOWLEDGE_COPY.get()) > 0;
        };
    }

    private static Component organizationName(ResourceLocation id) {
        OrganizationDefinition definition =
                OrganizationDefinitionManager.get(id);
        return definition == null
                ? Component.literal(id.getPath())
                : Component.translatable(definition.titleKey());
    }

    private static String objectiveKey(OrganizationActionType type) {
        return "command.lord_of_mysteries.organization.objective."
                + type.id();
    }

    private static OrganizationActionSavedData storage(
            ServerPlayer player) {
        return OrganizationActionSavedData.get(
                player.getServer().overworld());
    }

    private static void ensureCurrent(
            ServerPlayer player,
            OrganizationActionSavedData saved) {
        ServerLevel level = player.getServer().overworld();
        saved.refresh(
                level.getSeed(), currentDay(level),
                actionExposure(level),
                OrganizationDefinitionManager.all());
    }

    private static long currentDay(ServerLevel level) {
        return Math.max(0L, Math.floorDiv(
                level.getDayTime(), 24_000L));
    }

    private static float actionExposure(ServerLevel level) {
        float playerExposure = 0f;
        for (ServerPlayer player : level.players()) {
            playerExposure = Math.max(
                    playerExposure,
                    MysteryCapability.get(player).mysticalExposure);
        }
        MistCityWorldEvent event =
                MistCityWorldEventPolicy.eventForDay(
                        level.getSeed(), currentDay(level));
        float worldExposure = event == MistCityWorldEvent.CLEAR
                ? 10f : 45f;
        return Math.max(playerExposure, worldExposure);
    }

    private static boolean nearOutpost(
            ServerPlayer player, double distanceSquared) {
        ServerLevel level = player.getServer().overworld();
        if (player.level() != level) return false;
        BlockPos outpost = MistCityOutpostSavedData.get(level)
                .outpost().orElse(null);
        return outpost != null
                && outpost.distSqr(player.blockPosition())
                <= distanceSquared;
    }

    private static boolean consume(
            ServerPlayer player, Item item, int amount) {
        if (player.getInventory().countItem(item) < amount) {
            return false;
        }
        int remaining = amount;
        for (int slot = 0;
             slot < player.getInventory().getContainerSize()
                     && remaining > 0;
             slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.is(item)) continue;
            int consumed = Math.min(remaining, stack.getCount());
            stack.shrink(consumed);
            remaining -= consumed;
        }
        player.containerMenu.broadcastChanges();
        return true;
    }

    private static long saturatingAdd(long value, long increase) {
        if (increase > 0L && value > Long.MAX_VALUE - increase) {
            return Long.MAX_VALUE;
        }
        return value + increase;
    }
}
