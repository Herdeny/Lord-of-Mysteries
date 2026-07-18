package top.aurora.lordofmysteries.commission;

import java.util.Locale;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerDataSection;
import top.aurora.lordofmysteries.player.PlayerMysteryData;

public final class CaseHypothesisService {

    private CaseHypothesisService() {}

    public static int show(ServerPlayer player) {
        ActiveCase active = activeCase(player);
        if (active == null) return 0;
        CaseHypothesisRecord record = active.record();
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.case.hypothesis.title",
                        Component.translatable(active.evidence().caseTitleKey()))
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        if (record.hasDraft()) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.case.hypothesis.current",
                            record.relationId(),
                            Component.translatable(stanceKey(record.stance())),
                            record.note())
                    .withStyle(statusColor(record.status())));
        } else {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.case.hypothesis.none")
                    .withStyle(ChatFormatting.GRAY));
        }
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.case.hypothesis.counters",
                        record.unresolvedStrain(), record.failedTests(),
                        record.successfulTests())
                .withStyle(record.unresolvedStrain() > 0
                        ? ChatFormatting.YELLOW : ChatFormatting.DARK_GRAY));
        for (CaseEvidenceView.Relation relation : active.evidence().relations()) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.case.hypothesis.relation",
                            relation.id(),
                            Component.translatable(relation.titleKey()))
                    .withStyle(ChatFormatting.DARK_AQUA));
        }
        return 1;
    }

    public static int propose(
            ServerPlayer player,
            CaseHypothesisStance stance,
            String relationId,
            String note) {
        ActiveCase active = activeCase(player);
        if (active == null || !requireBoard(player)) return 0;
        String normalizedRelation = relationId == null ? ""
                : relationId.strip().toLowerCase(Locale.ROOT);
        CaseEvidenceView.Relation relation = active.evidence().relations().stream()
                .filter(candidate -> candidate.id().equals(normalizedRelation))
                .findFirst().orElse(null);
        if (relation == null) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.case.hypothesis.relation_unknown",
                            normalizedRelation)
                    .withStyle(ChatFormatting.RED));
            return show(player);
        }
        String sanitizedNote = CaseHypothesisRecord.sanitizeNote(note);
        if (sanitizedNote.isBlank()) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.case.hypothesis.note_required")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        CaseHypothesisRecord updated = active.record().propose(
                relation.id(), stance, sanitizedNote);
        write(active.data(), active.caseId(), updated);
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.case.hypothesis.proposed",
                        relation.id(), Component.translatable(stanceKey(stance)),
                        sanitizedNote)
                .withStyle(ChatFormatting.AQUA));
        InvestigationBoardService.refresh(player);
        return 1;
    }

    public static int test(ServerPlayer player) {
        ActiveCase active = activeCase(player);
        if (active == null || !requireBoard(player)) return 0;
        CaseHypothesisRecord record = active.record();
        if (!record.hasDraft()) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.case.hypothesis.no_draft")
                    .withStyle(ChatFormatting.GRAY));
            return 0;
        }
        long gameTime = player.level().getGameTime();
        long remaining = CaseHypothesisRules.testCooldownRemaining(
                record, gameTime);
        if (remaining > 0L) {
            sendCooldown(player, remaining);
            return 0;
        }
        CaseEvidenceView.Relation relation = active.evidence().relations().stream()
                .filter(candidate -> candidate.id().equals(record.relationId()))
                .findFirst().orElse(null);
        if (relation == null) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.case.hypothesis.relation_stale")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        int previousStrain = record.unresolvedStrain();
        CaseHypothesisRules.TestResult result = CaseHypothesisRules.test(
                record, relation.kind(), gameTime);
        write(active.data(), active.caseId(), result.record());
        if (result.pressureCost() > 0) {
            active.data().insanityPressure = Math.min(100f,
                    active.data().insanityPressure + result.pressureCost());
            active.data().markDirty(PlayerDataSection.CORE);
        }
        if (result.supported()) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.case.hypothesis.supported",
                            record.relationId(),
                            previousStrain - result.record().unresolvedStrain())
                    .withStyle(ChatFormatting.GREEN));
        } else {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.case.hypothesis.rejected",
                            record.relationId(), result.pressureCost(),
                            result.record().unresolvedStrain())
                    .withStyle(ChatFormatting.RED));
        }
        InvestigationBoardService.refresh(player);
        return result.supported() ? 1 : 0;
    }

    public static int reconsider(ServerPlayer player) {
        ActiveCase active = activeCase(player);
        if (active == null || !requireBoard(player)) return 0;
        CaseHypothesisRecord record = active.record();
        if (record.unresolvedStrain() <= 0) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.case.hypothesis.no_strain")
                    .withStyle(ChatFormatting.GRAY));
            return 0;
        }
        long gameTime = player.level().getGameTime();
        long remaining = CaseHypothesisRules.reconsiderCooldownRemaining(
                record, gameTime);
        if (remaining > 0L) {
            sendCooldown(player, remaining);
            return 0;
        }
        CaseHypothesisRecord updated = CaseHypothesisRules.reconsider(
                record, gameTime);
        write(active.data(), active.caseId(), updated);
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.case.hypothesis.reconsidered",
                        updated.unresolvedStrain())
                .withStyle(ChatFormatting.GREEN));
        InvestigationBoardService.refresh(player);
        return 1;
    }

    public static int clear(ServerPlayer player) {
        ActiveCase active = activeCase(player);
        if (active == null || !requireBoard(player)) return 0;
        if (!active.record().hasDraft()) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.case.hypothesis.no_draft")
                    .withStyle(ChatFormatting.GRAY));
            return 0;
        }
        write(active.data(), active.caseId(), active.record().clearDraft());
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.case.hypothesis.cleared")
                .withStyle(ChatFormatting.GRAY));
        InvestigationBoardService.refresh(player);
        return 1;
    }

    public static int unresolvedStrain(
            PlayerMysteryData data, ResourceLocation caseId) {
        if (data == null || caseId == null) return 0;
        CaseHypothesisRecord record = data.caseHypotheses.get(caseId);
        return record == null ? 0 : record.unresolvedStrain();
    }

    private static ActiveCase activeCase(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        ResourceLocation caseId = ResourceLocation.tryParse(
                data.activeCommissionId);
        CaseEvidenceView evidence = CaseEvidenceView.from(
                data, FormulaAppraisalService.evidence(player),
                DynamicCaseService.profileFor(player, data));
        if (caseId == null || evidence.commissionId().isBlank()) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.case.hypothesis.no_active")
                    .withStyle(ChatFormatting.GRAY));
            return null;
        }
        return new ActiveCase(data, caseId, evidence,
                data.caseHypotheses.getOrDefault(
                        caseId, CaseHypothesisRecord.EMPTY));
    }

    private static boolean requireBoard(ServerPlayer player) {
        if (InvestigationBoardService.isNearBoard(player)) return true;
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.case.hypothesis.board_required")
                .withStyle(ChatFormatting.RED));
        return false;
    }

    private static void write(
            PlayerMysteryData data,
            ResourceLocation caseId,
            CaseHypothesisRecord record) {
        data.caseHypotheses.put(caseId, record);
        data.markDirty(PlayerDataSection.SOCIAL);
    }

    private static void sendCooldown(ServerPlayer player, long ticks) {
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.case.hypothesis.cooldown",
                        Math.max(1L, (ticks + 19L) / 20L))
                .withStyle(ChatFormatting.YELLOW));
    }

    private static String stanceKey(CaseHypothesisStance stance) {
        return "screen.lord_of_mysteries.hypothesis.stance." + stance.id();
    }

    private static ChatFormatting statusColor(CaseHypothesisStatus status) {
        return switch (status) {
            case DRAFT -> ChatFormatting.AQUA;
            case SUPPORTED -> ChatFormatting.GREEN;
            case REJECTED -> ChatFormatting.RED;
        };
    }

    private record ActiveCase(
            PlayerMysteryData data,
            ResourceLocation caseId,
            CaseEvidenceView evidence,
            CaseHypothesisRecord record) {}
}
