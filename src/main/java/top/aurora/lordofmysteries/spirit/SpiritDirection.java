package top.aurora.lordofmysteries.spirit;

import java.util.Locale;

public enum SpiritDirection {
    NORTH("north", 0, -1),
    EAST("east", 1, 0),
    SOUTH("south", 0, 1),
    WEST("west", -1, 0);

    private final String id;
    private final int stepX;
    private final int stepZ;

    SpiritDirection(String id, int stepX, int stepZ) {
        this.id = id;
        this.stepX = stepX;
        this.stepZ = stepZ;
    }

    public String id() {
        return id;
    }

    public int stepX() {
        return stepX;
    }

    public int stepZ() {
        return stepZ;
    }

    public String translationKey() {
        return "spirit_direction.lord_of_mysteries." + id;
    }

    public static SpiritDirection fromId(String id) {
        if (id == null) return null;
        String normalized = id.trim().toLowerCase(Locale.ROOT);
        for (SpiritDirection direction : values()) {
            if (direction.id.equals(normalized)) return direction;
        }
        return null;
    }
}
