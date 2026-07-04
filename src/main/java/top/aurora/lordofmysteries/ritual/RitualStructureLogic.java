package top.aurora.lordofmysteries.ritual;

import java.util.List;

public final class RitualStructureLogic {

    private RitualStructureLogic() {}

    public static List<Offset> circleOffsets(int radius) {
        if (radius < 2) throw new IllegalArgumentException("radius must be at least 2");
        int diagonal = radius - 1;
        return List.of(
                new Offset(radius, 0),
                new Offset(-radius, 0),
                new Offset(0, radius),
                new Offset(0, -radius),
                new Offset(diagonal, diagonal),
                new Offset(diagonal, -diagonal),
                new Offset(-diagonal, diagonal),
                new Offset(-diagonal, -diagonal));
    }

    public static float completion(int found, int required) {
        if (required <= 0) return 0f;
        return Math.max(0f, Math.min(1f, found / (float) required));
    }

    public record Offset(int x, int z) {}
}
