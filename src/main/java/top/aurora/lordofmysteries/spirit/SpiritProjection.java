package top.aurora.lordofmysteries.spirit;

import java.util.Locale;

public enum SpiritProjection {
    CHURCH("church"),
    CEMETERY("cemetery"),
    THEATRE("theatre"),
    HARBOR("harbor"),
    MANOR("manor");

    private final String id;

    SpiritProjection(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String translationKey() {
        return "spirit_projection.lord_of_mysteries." + id;
    }

    public static SpiritProjection fromId(String id) {
        if (id == null) return null;
        String normalized = id.trim().toLowerCase(Locale.ROOT);
        for (SpiritProjection projection : values()) {
            if (projection.id.equals(normalized)) return projection;
        }
        return null;
    }
}
