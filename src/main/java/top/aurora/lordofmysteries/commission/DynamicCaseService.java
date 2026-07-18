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
                : player.level().getGameTime();
        DynamicCaseProfile profile = DynamicCaseGenerator.generate(
                overworld.getSeed(), tick);
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.dynamic_case.title",
                        profile.instanceId())
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        sendSlot(player, "archetype", profile.archetype());
        sendSlot(player, "subject", profile.subject());
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
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.dynamic_case.evidence_recorded",
                        Component.translatable(
                                "command.lord_of_mysteries.dynamic_case.route."
                                        + route.name().toLowerCase(
                                                java.util.Locale.ROOT)),
                        Component.translatable(clueKey(profile, step)))
                .withStyle(ChatFormatting.GREEN));
        data = MysteryCapability.get(player);
        sendNextStep(player, data, profile);
        InvestigationBoardService.refresh(player);
        return 1;
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
        ServerLevel level = player.serverLevel();
        Optional<BlockPos> target = switch (location) {
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
        return target.filter(position -> position.distToCenterSqr(player.position())
                <= FIELD_RANGE * FIELD_RANGE).isPresent();
    }

    private static boolean nearWitness(
            ServerPlayer player, DynamicCaseProfile.Archetype archetype) {
        String tag = switch (archetype) {
            case MISSING_PERSON -> InvestigationNpcHandler.PRESS_CLERK_TAG;
            case ANOMALOUS_ITEM -> InvestigationNpcHandler.OCCULT_APPRAISER_TAG;
            case OCCULT_CRIME -> InvestigationNpcHandler.NIGHTHAWK_CONTACT_TAG;
        };
        return !player.level().getEntitiesOfClass(
                Villager.class,
                player.getBoundingBox().inflate(WITNESS_RANGE),
                villager -> villager.isAlive() && villager.getTags().contains(tag))
                .isEmpty();
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
}
