package top.aurora.lordofmysteries.commission;

import java.util.Locale;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public record DynamicCaseContactEvent(
        String instanceId,
        DynamicCaseProfile.Subject contact,
        DynamicCaseProfile.Organization organization,
        Kind kind,
        String detail,
        long caseDay,
        int standingDelta) {

    private static final int MAX_INSTANCE_ID_LENGTH = 64;

    public DynamicCaseContactEvent {
        instanceId = instanceId == null ? "" : instanceId.strip();
        detail = detail == null ? "" : detail.strip().toLowerCase(Locale.ROOT);
        if (instanceId.isBlank()
                || instanceId.length() > MAX_INSTANCE_ID_LENGTH
                || contact == null || organization == null || kind == null
                || caseDay < 0L
                || standingDelta < DynamicCaseRelationshipPolicy.MIN_STANDING
                || standingDelta > DynamicCaseRelationshipPolicy.MAX_STANDING
                || !kind.isValidDetail(detail)) {
            throw new IllegalArgumentException(
                    "dynamic case contact event is invalid");
        }
    }

    public static DynamicCaseContactEvent caseClosed(
            DynamicCaseHistoryEntry entry) {
        if (entry == null) {
            throw new IllegalArgumentException(
                    "dynamic case history entry is required");
        }
        return new DynamicCaseContactEvent(
                entry.instanceId(),
                entry.subject(),
                entry.organization(),
                Kind.CASE_CLOSED,
                entry.grade().name(),
                entry.caseDay(),
                DynamicCaseRelationshipPolicy.adjustment(entry.grade()));
    }

    public static DynamicCaseContactEvent response(
            DynamicCaseResponseTask task,
            Kind kind,
            long caseDay,
            int standingDelta) {
        if (task == null || kind == null || !kind.isResponse()) {
            throw new IllegalArgumentException(
                    "dynamic case response event is required");
        }
        return new DynamicCaseContactEvent(
                task.instanceId(),
                task.contact(),
                task.organization(),
                kind,
                task.branch().id(),
                caseDay,
                standingDelta);
    }

    public String identityKey() {
        return instanceId + (kind == Kind.CASE_CLOSED
                ? ":case" : ":response");
    }

    public String eventTranslationKey() {
        return "dynamic_case.lord_of_mysteries.contact_event." + kind.id();
    }

    public String detailTranslationKey() {
        if (kind == Kind.CASE_CLOSED) {
            return "dynamic_case.lord_of_mysteries.contact_grade." + detail;
        }
        return DynamicCaseResponseBranch.fromId(detail).translationKey();
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("instance_id", instanceId);
        tag.putString("contact", contact.id());
        tag.putString("organization", organization.id());
        tag.putString("kind", kind.id());
        tag.putString("detail", detail);
        tag.putLong("case_day", caseDay);
        tag.putInt("standing_delta", standingDelta);
        return tag;
    }

    public static DynamicCaseContactEvent load(CompoundTag tag) {
        if (!hasRequiredShape(tag)) {
            throw new IllegalArgumentException(
                    "dynamic case contact event is incomplete");
        }
        return new DynamicCaseContactEvent(
                tag.getString("instance_id"),
                DynamicCaseProfile.Subject.fromId(tag.getString("contact")),
                organizationFromId(tag.getString("organization")),
                Kind.fromId(tag.getString("kind")),
                tag.getString("detail"),
                tag.getLong("case_day"),
                tag.getInt("standing_delta"));
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
                && tag.contains("contact", Tag.TAG_STRING)
                && tag.contains("organization", Tag.TAG_STRING)
                && tag.contains("kind", Tag.TAG_STRING)
                && tag.contains("detail", Tag.TAG_STRING)
                && tag.contains("case_day", Tag.TAG_LONG)
                && tag.contains("standing_delta", Tag.TAG_INT);
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

    public enum Kind {
        CASE_CLOSED,
        RESPONSE_COMPLETED,
        RESPONSE_ABANDONED,
        RESPONSE_EXPIRED;

        public String id() {
            return name().toLowerCase(Locale.ROOT);
        }

        public boolean isResponse() {
            return this != CASE_CLOSED;
        }

        private boolean isValidDetail(String value) {
            if (value == null || value.isBlank()) return false;
            if (this == CASE_CLOSED) {
                for (CaseGrade grade : CaseGrade.values()) {
                    if (grade.name().equalsIgnoreCase(value)) return true;
                }
                return false;
            }
            return DynamicCaseResponseBranch.fromId(value) != null;
        }

        public static Kind fromId(String value) {
            if (value == null) return null;
            for (Kind kind : values()) {
                if (kind.id().equals(value)) return kind;
            }
            return null;
        }
    }
}
