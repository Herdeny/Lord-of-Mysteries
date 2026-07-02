package top.aurora.lordofmysteries.potion;

import java.util.Locale;

public enum PotionQuality {
    PERFECT("perfect", 1.2f, 5f, 0f),
    COMPLETE("complete", 1.0f, 10f, 0f),
    FLAWED("flawed", 0.7f, 20f, 5f),
    CONTAMINATED("contaminated", 0.4f, 35f, 15f);

    private final String id;
    private final float digestionMultiplier;
    private final float initialPressure;
    private final float initialPollution;

    PotionQuality(String id, float digestionMultiplier, float initialPressure, float initialPollution) {
        this.id = id;
        this.digestionMultiplier = digestionMultiplier;
        this.initialPressure = initialPressure;
        this.initialPollution = initialPollution;
    }

    public String id() {
        return id;
    }

    public float digestionMultiplier() {
        return digestionMultiplier;
    }

    public float initialPressure() {
        return initialPressure;
    }

    public float initialPollution() {
        return initialPollution;
    }

    public static PotionQuality fromId(String id) {
        if (id == null || id.isBlank()) return COMPLETE;
        String normalized = id.toLowerCase(Locale.ROOT);
        for (PotionQuality quality : values()) {
            if (quality.id.equals(normalized)) return quality;
        }
        return COMPLETE;
    }
}
