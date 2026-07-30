package top.aurora.lordofmysteries.ability;

import java.util.Locale;

public enum MarionetteTacticalMode {
    FOLLOW,
    GUARD,
    PASSIVE;

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public boolean allowsCombat() {
        return this != PASSIVE;
    }

    public static boolean isValidId(String value) {
        if (value == null) return false;
        for (MarionetteTacticalMode mode : values()) {
            if (mode.id().equals(value.trim().toLowerCase(
                    Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    public static MarionetteTacticalMode fromId(String value) {
        if (value == null) return FOLLOW;
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return FOLLOW;
        }
    }
}
