package top.aurora.lordofmysteries.commission;

import java.util.Locale;

public enum DynamicCaseResponseBranch {
    PRIORITY(4L),
    ROUTINE(3L),
    RECONCILIATION(4L);

    private final long durationDays;

    DynamicCaseResponseBranch(long durationDays) {
        this.durationDays = durationDays;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public long durationDays() {
        return durationDays;
    }

    public String translationKey() {
        return "dynamic_case.lord_of_mysteries.response_branch." + id();
    }

    public static DynamicCaseResponseBranch fromId(String value) {
        if (value == null) return null;
        for (DynamicCaseResponseBranch branch : values()) {
            if (branch.id().equals(value)) return branch;
        }
        return null;
    }
}
