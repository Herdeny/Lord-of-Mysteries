package top.aurora.lordofmysteries.commission;

import java.util.Locale;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class CaseDebriefService {

    private static final long DEFAULT_TARGET_TICKS = 72_000L;

    private CaseDebriefService() {}

    public static CaseDebriefRecord evaluate(
            ResourceLocation commissionId,
            CaseEvidenceView evidence,
            long acceptedTick,
            long completedTick,
            String resolutionRoute,
            float insanityPressure,
            float pollution,
            int failedVerdictAttempts) {
        long durationTicks = Math.max(0L, completedTick - Math.max(0L, acceptedTick));
        int evidenceScore = evidence.total() == 0 ? 0
                : Math.round(evidence.discovered() * 40f / evidence.total());
        int procedureScore = (evidence.conclusionReady() ? 30 : 15)
                - Math.min(15, Math.max(0, failedVerdictAttempts) * 5);
        int safetyScore = 20 - riskPenalty(insanityPressure)
                - riskPenalty(pollution);
        int efficiencyScore = efficiencyScore(
                durationTicks, targetTicks(commissionId));
        return new CaseDebriefRecord(
                evidenceScore,
                procedureScore,
                safetyScore,
                efficiencyScore,
                durationTicks,
                completedTick,
                resolutionRoute);
    }

    public static void sendSummary(
            ServerPlayer player,
            String caseTitleKey,
            CaseDebriefRecord record) {
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.case.debrief.title",
                        Component.translatable(caseTitleKey))
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.case.debrief.summary",
                        record.score(), record.grade().name())
                .withStyle(gradeColor(record.grade())));
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.case.debrief.components",
                        record.evidenceScore(), record.procedureScore(),
                        record.safetyScore(), record.efficiencyScore())
                .withStyle(ChatFormatting.GRAY));
        long totalSeconds = record.durationTicks() / 20L;
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.case.debrief.duration",
                        totalSeconds / 60L, totalSeconds % 60L)
                .append(Component.literal(" · "))
                .append(Component.translatable(
                        "command.lord_of_mysteries.case.debrief.route",
                        routeName(record.resolutionRoute())))
                .withStyle(ChatFormatting.DARK_GRAY));
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.case.debrief.focus",
                        Component.translatable(
                                "command.lord_of_mysteries.case.debrief.focus."
                                        + record.improvementFocus().name()
                                        .toLowerCase(Locale.ROOT)))
                .withStyle(ChatFormatting.GOLD));
    }

    private static Component routeName(String route) {
        if (route == null || route.isBlank()) {
            return Component.translatable(
                    "command.lord_of_mysteries.case.debrief.route.none");
        }
        return Component.translatable(
                "message.lord_of_mysteries.quest.approach." + route);
    }

    private static int riskPenalty(float value) {
        float safeValue = Float.isFinite(value)
                ? Math.max(0f, Math.min(100f, value)) : 100f;
        return Math.min(10, Math.round(safeValue / 10f));
    }

    private static int efficiencyScore(long durationTicks, long targetTicks) {
        if (durationTicks <= targetTicks) return 10;
        if (durationTicks <= targetTicks * 3L / 2L) return 8;
        if (durationTicks <= targetTicks * 2L) return 5;
        return 2;
    }

    private static long targetTicks(ResourceLocation commissionId) {
        if (CommissionService.LOST_CAT.equals(commissionId)) return 24_000L;
        if (CommissionService.COUNTERFEIT_FORMULA.equals(commissionId)) {
            return 36_000L;
        }
        return DEFAULT_TARGET_TICKS;
    }

    private static ChatFormatting gradeColor(CaseGrade grade) {
        return switch (grade) {
            case S -> ChatFormatting.GOLD;
            case A -> ChatFormatting.GREEN;
            case B -> ChatFormatting.AQUA;
            case C -> ChatFormatting.YELLOW;
            case D -> ChatFormatting.RED;
        };
    }
}
