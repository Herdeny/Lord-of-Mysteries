package top.aurora.lordofmysteries.knowledge;

public final class GuideDirection {

    private GuideDirection() {}

    public static String fromDelta(double deltaX, double deltaZ) {
        if (deltaX == 0d && deltaZ == 0d) return "here";
        double angle = Math.atan2(deltaZ, deltaX);
        int sector = Math.floorMod((int) Math.round(angle / (Math.PI / 4d)), 8);
        return switch (sector) {
            case 0 -> "east";
            case 1 -> "southeast";
            case 2 -> "south";
            case 3 -> "southwest";
            case 4 -> "west";
            case 5 -> "northwest";
            case 6 -> "north";
            default -> "northeast";
        };
    }
}
