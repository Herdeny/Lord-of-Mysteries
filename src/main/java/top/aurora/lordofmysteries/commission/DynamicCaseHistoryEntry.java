package top.aurora.lordofmysteries.commission;

import java.util.Locale;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public record DynamicCaseHistoryEntry(
        long caseDay,
        long caseWeek,
        String instanceId,
        DynamicCaseProfile.Archetype archetype,
        DynamicCaseProfile.Subject subject,
        DynamicCaseProfile.Organization organization,
        DynamicCaseProfile.CaseLocation location,
        CaseGrade grade,
        int score,
        long completedTick,
        int reputationAdjustment,
        FollowUpStatus followUpStatus) {

    private static final int MAX_INSTANCE_ID_LENGTH = 64;
    private static final int MAX_REPUTATION_ADJUSTMENT = 16;

    public DynamicCaseHistoryEntry {
        instanceId = instanceId == null ? "" : instanceId.strip();
        if (caseDay < 0L || caseWeek != Math.floorDiv(caseDay, 7L)
                || instanceId.isBlank()
                || instanceId.length() > MAX_INSTANCE_ID_LENGTH
                || archetype == null || subject == null
                || organization == null || location == null
                || grade == null || score < 0 || score > 100
                || grade != CaseGrade.fromScore(score)
                || completedTick < 0L
                || Math.abs(reputationAdjustment)
                        > MAX_REPUTATION_ADJUSTMENT
                || followUpStatus == null) {
            throw new IllegalArgumentException(
                    "dynamic case history entry is invalid");
        }
    }

    public DynamicCaseHistoryEntry withFollowUpStatus(
            FollowUpStatus status) {
        return new DynamicCaseHistoryEntry(
                caseDay, caseWeek, instanceId, archetype, subject,
                organization, location, grade, score, completedTick,
                reputationAdjustment, status);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("case_day", caseDay);
        tag.putLong("case_week", caseWeek);
        tag.putString("instance_id", instanceId);
        tag.putString("archetype", archetype.id());
        tag.putString("subject", subject.id());
        tag.putString("organization", organization.id());
        tag.putString("location", location.id());
        tag.putString("grade", grade.name());
        tag.putInt("score", score);
        tag.putLong("completed_tick", completedTick);
        tag.putInt("reputation_adjustment", reputationAdjustment);
        tag.putString("follow_up_status", followUpStatus.id());
        return tag;
    }

    public static DynamicCaseHistoryEntry load(CompoundTag tag) {
        if (!hasRequiredShape(tag)) {
            throw new IllegalArgumentException(
                    "dynamic case history tag is incomplete");
        }
        DynamicCaseProfile.Archetype archetype = optionFromId(
                DynamicCaseProfile.Archetype.values(),
                tag.getString("archetype"));
        DynamicCaseProfile.Subject subject = optionFromId(
                DynamicCaseProfile.Subject.values(),
                tag.getString("subject"));
        DynamicCaseProfile.Organization organization = optionFromId(
                DynamicCaseProfile.Organization.values(),
                tag.getString("organization"));
        DynamicCaseProfile.CaseLocation location = optionFromId(
                DynamicCaseProfile.CaseLocation.values(),
                tag.getString("location"));
        CaseGrade grade = gradeFromId(tag.getString("grade"));
        FollowUpStatus status = FollowUpStatus.fromId(
                tag.getString("follow_up_status"));
        return new DynamicCaseHistoryEntry(
                tag.getLong("case_day"),
                tag.getLong("case_week"),
                tag.getString("instance_id"),
                archetype,
                subject,
                organization,
                location,
                grade,
                tag.getInt("score"),
                tag.getLong("completed_tick"),
                tag.getInt("reputation_adjustment"),
                status);
    }

    public static boolean isValid(CompoundTag tag) {
        if (!hasRequiredShape(tag)) return false;
        try {
            load(tag);
            return true;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    private static boolean hasRequiredShape(CompoundTag tag) {
        return tag != null
                && tag.contains("case_day", Tag.TAG_LONG)
                && tag.contains("case_week", Tag.TAG_LONG)
                && tag.contains("instance_id", Tag.TAG_STRING)
                && tag.contains("archetype", Tag.TAG_STRING)
                && tag.contains("subject", Tag.TAG_STRING)
                && tag.contains("organization", Tag.TAG_STRING)
                && tag.contains("location", Tag.TAG_STRING)
                && tag.contains("grade", Tag.TAG_STRING)
                && tag.contains("score", Tag.TAG_INT)
                && tag.contains("completed_tick", Tag.TAG_LONG)
                && tag.contains("reputation_adjustment", Tag.TAG_INT)
                && tag.contains("follow_up_status", Tag.TAG_STRING);
    }

    private static CaseGrade gradeFromId(String value) {
        if (value == null) return null;
        try {
            return CaseGrade.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static <T extends Enum<T> & DynamicCaseProfile.SlotOption>
            T optionFromId(T[] values, String value) {
        if (value == null) return null;
        for (T option : values) {
            if (option.id().equals(value)) return option;
        }
        return null;
    }

    public enum FollowUpStatus {
        PENDING,
        CLAIMED,
        EXPIRED;

        public String id() {
            return name().toLowerCase(Locale.ROOT);
        }

        public static FollowUpStatus fromId(String value) {
            if (value == null) return null;
            for (FollowUpStatus status : values()) {
                if (status.id().equals(value)) return status;
            }
            return null;
        }
    }
}
