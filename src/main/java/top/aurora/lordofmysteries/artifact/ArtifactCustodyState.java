package top.aurora.lordofmysteries.artifact;

import java.util.Locale;

public enum ArtifactCustodyState {
    BORROWED,
    LEAKED,
    RECOVERED,
    RETURNED,
    ABUSED;

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static ArtifactCustodyState fromId(String id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "artifact custody state is required");
        }
        try {
            return valueOf(id.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "unknown artifact custody state " + id,
                    exception);
        }
    }

    public boolean active() {
        return this != RETURNED;
    }
}
