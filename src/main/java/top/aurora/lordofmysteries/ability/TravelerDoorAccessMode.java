package top.aurora.lordofmysteries.ability;

import java.util.Locale;
import java.util.UUID;

public enum TravelerDoorAccessMode {
    PRIVATE,
    PARTY,
    ORGANIZATION,
    PUBLIC;

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public boolean allows(
            UUID owner,
            String ownerTeam,
            UUID candidate,
            String candidateTeam) {
        if (owner == null || candidate == null) return false;
        if (owner.equals(candidate)) return true;
        return switch (this) {
            case PRIVATE -> false;
            case PARTY -> !normalizedTeam(ownerTeam).isEmpty()
                    && normalizedTeam(ownerTeam).equals(
                            normalizedTeam(candidateTeam));
            case ORGANIZATION -> false;
            case PUBLIC -> true;
        };
    }

    public static TravelerDoorAccessMode fromId(String value) {
        if (value == null) return PARTY;
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return PARTY;
        }
    }

    public static String normalizedTeam(String value) {
        if (value == null) return "";
        String normalized = value.trim();
        return normalized.length() <= 64 ? normalized : "";
    }
}
