package top.aurora.lordofmysteries.commission;

import java.util.Locale;

public final class MysticalExposurePolicy {

    private MysticalExposurePolicy() {}

    public static float adjust(float current, float delta) {
        float safeCurrent = Float.isFinite(current) ? current : 0f;
        float safeDelta = Float.isFinite(delta) ? delta : 0f;
        return Math.max(0f, Math.min(100f, safeCurrent + safeDelta));
    }

    public static int caseDelta(CaseGrade grade) {
        if (grade == null) {
            throw new IllegalArgumentException("case grade is required");
        }
        return switch (grade) {
            case S -> -4;
            case A -> -2;
            case B -> 0;
            case C -> 2;
            case D -> 6;
        };
    }

    public static Band band(float exposure) {
        float normalized = adjust(exposure, 0f);
        if (normalized < 20f) return Band.HIDDEN;
        if (normalized < 45f) return Band.RUMORED;
        if (normalized < 70f) return Band.NOTICED;
        if (normalized < 90f) return Band.WATCHED;
        return Band.EXPOSED;
    }

    public enum Band {
        HIDDEN,
        RUMORED,
        NOTICED,
        WATCHED,
        EXPOSED;

        public String id() {
            return name().toLowerCase(Locale.ROOT);
        }

        public String translationKey() {
            return "exposure.lord_of_mysteries.band." + id();
        }
    }
}
