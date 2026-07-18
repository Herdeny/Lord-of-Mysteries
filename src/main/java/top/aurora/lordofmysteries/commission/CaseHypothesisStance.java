package top.aurora.lordofmysteries.commission;

import java.util.Locale;

public enum CaseHypothesisStance {
    SUPPORTS,
    CONTRADICTS,
    LEADS_TO;

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public boolean matches(EvidenceRelationKind relationKind) {
        return relationKind != null && name().equals(relationKind.name());
    }

    public static CaseHypothesisStance fromId(String id) {
        if (id == null) return null;
        for (CaseHypothesisStance stance : values()) {
            if (stance.id().equals(id)) return stance;
        }
        return null;
    }
}
