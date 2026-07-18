package top.aurora.lordofmysteries.commission;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public record CaseDebriefRecord(
        int evidenceScore,
        int procedureScore,
        int safetyScore,
        int efficiencyScore,
        long durationTicks,
        long completedTick,
        String resolutionRoute) {

    private static final int MAX_ROUTE_LENGTH = 32;

    public CaseDebriefRecord {
        evidenceScore = clamp(evidenceScore, 0, 40);
        procedureScore = clamp(procedureScore, 0, 30);
        safetyScore = clamp(safetyScore, 0, 20);
        efficiencyScore = clamp(efficiencyScore, 0, 10);
        durationTicks = Math.max(0L, durationTicks);
        completedTick = Math.max(0L, completedTick);
        resolutionRoute = sanitizeRoute(resolutionRoute);
    }

    public int score() {
        return evidenceScore + procedureScore + safetyScore + efficiencyScore;
    }

    public CaseGrade grade() {
        return CaseGrade.fromScore(score());
    }

    public CaseDebriefFocus improvementFocus() {
        int evidenceLoss = 40 - evidenceScore;
        int procedureLoss = 30 - procedureScore;
        int safetyLoss = 20 - safetyScore;
        int efficiencyLoss = 10 - efficiencyScore;
        int greatestLoss = Math.max(Math.max(evidenceLoss, procedureLoss),
                Math.max(safetyLoss, efficiencyLoss));
        if (evidenceLoss == greatestLoss) return CaseDebriefFocus.EVIDENCE;
        if (procedureLoss == greatestLoss) return CaseDebriefFocus.PROCEDURE;
        if (safetyLoss == greatestLoss) return CaseDebriefFocus.SAFETY;
        return CaseDebriefFocus.EFFICIENCY;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("evidence_score", evidenceScore);
        tag.putInt("procedure_score", procedureScore);
        tag.putInt("safety_score", safetyScore);
        tag.putInt("efficiency_score", efficiencyScore);
        tag.putLong("duration_ticks", durationTicks);
        tag.putLong("completed_tick", completedTick);
        tag.putString("resolution_route", resolutionRoute);
        return tag;
    }

    public static CaseDebriefRecord load(CompoundTag tag) {
        if (tag == null) throw new IllegalArgumentException("tag is required");
        return new CaseDebriefRecord(
                tag.getInt("evidence_score"),
                tag.getInt("procedure_score"),
                tag.getInt("safety_score"),
                tag.getInt("efficiency_score"),
                tag.getLong("duration_ticks"),
                tag.getLong("completed_tick"),
                tag.getString("resolution_route"));
    }

    public static boolean isValid(CompoundTag tag) {
        return tag != null
                && tag.contains("evidence_score", Tag.TAG_INT)
                && tag.contains("procedure_score", Tag.TAG_INT)
                && tag.contains("safety_score", Tag.TAG_INT)
                && tag.contains("efficiency_score", Tag.TAG_INT)
                && tag.contains("duration_ticks", Tag.TAG_LONG)
                && tag.contains("completed_tick", Tag.TAG_LONG)
                && tag.contains("resolution_route", Tag.TAG_STRING);
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static String sanitizeRoute(String route) {
        if (route == null) return "";
        String sanitized = route.strip();
        if (sanitized.length() > MAX_ROUTE_LENGTH) return "";
        return sanitized.equals("assault")
                || sanitized.equals("stealth")
                || sanitized.equals("divination")
                || DynamicCaseProfile.Conclusion.fromId(sanitized) != null
                        ? sanitized : "";
    }
}
