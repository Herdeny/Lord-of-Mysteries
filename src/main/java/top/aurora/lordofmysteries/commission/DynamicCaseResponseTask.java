package top.aurora.lordofmysteries.commission;

import java.util.Locale;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public record DynamicCaseResponseTask(
        String instanceId,
        DynamicCaseProfile.Organization organization,
        DynamicCaseProfile.Subject contact,
        DynamicCaseWeeklyDirective directive,
        long assignedDay,
        long expiresDay,
        Stage stage,
        DynamicCaseResponseBranch branch) {

    private static final int MAX_INSTANCE_ID_LENGTH = 64;

    public DynamicCaseResponseTask {
        instanceId = instanceId == null ? "" : instanceId.strip();
        if (instanceId.isBlank()
                || instanceId.length() > MAX_INSTANCE_ID_LENGTH
                || organization == null || contact == null
                || directive == null || directive.organization() != organization
                || assignedDay < 0L
                || expiresDay != assignedDay
                        + (branch == null ? -1L : branch.durationDays())
                || stage == null || branch == null) {
            throw new IllegalArgumentException(
                    "dynamic case organization response task is invalid");
        }
    }

    public DynamicCaseResponseTask(
            String instanceId,
            DynamicCaseProfile.Organization organization,
            DynamicCaseProfile.Subject contact,
            DynamicCaseWeeklyDirective directive,
            long assignedDay,
            long expiresDay,
            Stage stage) {
        this(instanceId, organization, contact, directive,
                assignedDay, expiresDay, stage,
                DynamicCaseResponseBranch.ROUTINE);
    }

    public DynamicCaseResponseTask withStage(Stage nextStage) {
        return new DynamicCaseResponseTask(
                instanceId, organization, contact, directive,
                assignedDay, expiresDay, nextStage, branch);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("instance_id", instanceId);
        tag.putString("organization", organization.id());
        tag.putString("contact", contact.id());
        tag.putString("directive", directive.id());
        tag.putLong("assigned_day", assignedDay);
        tag.putLong("expires_day", expiresDay);
        tag.putString("stage", stage.id());
        tag.putString("branch", branch.id());
        return tag;
    }

    public static DynamicCaseResponseTask load(CompoundTag tag) {
        if (!hasRequiredShape(tag)) {
            throw new IllegalArgumentException(
                    "dynamic case organization response task is incomplete");
        }
        return new DynamicCaseResponseTask(
                tag.getString("instance_id"),
                organizationFromId(tag.getString("organization")),
                DynamicCaseProfile.Subject.fromId(tag.getString("contact")),
                DynamicCaseWeeklyDirective.fromId(
                        tag.getString("directive")),
                tag.getLong("assigned_day"),
                tag.getLong("expires_day"),
                Stage.fromId(tag.getString("stage")),
                DynamicCaseResponseBranch.fromId(
                        tag.getString("branch")));
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
                && tag.contains("instance_id", Tag.TAG_STRING)
                && tag.contains("organization", Tag.TAG_STRING)
                && tag.contains("contact", Tag.TAG_STRING)
                && tag.contains("directive", Tag.TAG_STRING)
                && tag.contains("assigned_day", Tag.TAG_LONG)
                && tag.contains("expires_day", Tag.TAG_LONG)
                && tag.contains("stage", Tag.TAG_STRING)
                && tag.contains("branch", Tag.TAG_STRING);
    }

    private static DynamicCaseProfile.Organization organizationFromId(
            String value) {
        if (value == null) return null;
        for (DynamicCaseProfile.Organization organization
                : DynamicCaseProfile.Organization.values()) {
            if (organization.id().equals(value)) return organization;
        }
        return null;
    }

    public enum Stage {
        ASSIGNED,
        BRIEFED;

        public String id() {
            return name().toLowerCase(Locale.ROOT);
        }

        public static Stage fromId(String value) {
            if (value == null) return null;
            for (Stage stage : values()) {
                if (stage.id().equals(value)) return stage;
            }
            return null;
        }
    }
}
