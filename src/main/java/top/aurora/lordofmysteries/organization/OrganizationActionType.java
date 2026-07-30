package top.aurora.lordofmysteries.organization;

import java.util.Locale;

public enum OrganizationActionType {
    NIGHT_PATROL("night_patrol", 1),
    ARTIFACT_TRANSFER("artifact_transfer", 2),
    HERESY_REVIEW("heresy_review", 2),
    DISASTER_RELIEF("disaster_relief", 1),
    HIGH_COUNCIL("high_council", 3),
    SECRET_RECRUITMENT("secret_recruitment", 2);

    private final String id;
    private final int baseRisk;

    OrganizationActionType(String id, int baseRisk) {
        this.id = id;
        this.baseRisk = baseRisk;
    }

    public String id() {
        return id;
    }

    public int baseRisk() {
        return baseRisk;
    }

    public String translationKey() {
        return "organization_action.lord_of_mysteries." + id;
    }

    public static OrganizationActionType fromId(String id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "organization action type is required");
        }
        String normalized = id.trim().toLowerCase(Locale.ROOT);
        for (OrganizationActionType type : values()) {
            if (type.id.equals(normalized)) return type;
        }
        throw new IllegalArgumentException(
                "unknown organization action type " + id);
    }
}
