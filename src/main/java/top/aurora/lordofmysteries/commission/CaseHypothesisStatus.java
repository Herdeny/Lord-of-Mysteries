package top.aurora.lordofmysteries.commission;

import java.util.Locale;

public enum CaseHypothesisStatus {
    DRAFT,
    SUPPORTED,
    REJECTED;

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static CaseHypothesisStatus fromId(String id) {
        if (id == null) return null;
        for (CaseHypothesisStatus status : values()) {
            if (status.id().equals(id)) return status;
        }
        return null;
    }
}
