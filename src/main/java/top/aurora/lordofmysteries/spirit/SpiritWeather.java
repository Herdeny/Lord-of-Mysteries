package top.aurora.lordofmysteries.spirit;

import java.util.Locale;

public enum SpiritWeather {
    SPIRIT_MIST("spirit_mist", 1),
    STARLESS_NIGHT("starless_night", 2),
    WHISPERING_RAIN("whispering_rain", 2),
    SPIRITUAL_STORM("spiritual_storm", 3),
    MEMORY_SNOW("memory_snow", 2),
    DOORLIGHT_AURORA("doorlight_aurora", 1);

    private final String id;
    private final int risk;

    SpiritWeather(String id, int risk) {
        this.id = id;
        this.risk = risk;
    }

    public String id() {
        return id;
    }

    public int risk() {
        return risk;
    }

    public String translationKey() {
        return "spirit_weather.lord_of_mysteries." + id;
    }

    public String hintKey() {
        return translationKey() + ".hint";
    }

    public static SpiritWeather fromId(String id) {
        if (id == null) return null;
        String normalized = id.trim().toLowerCase(Locale.ROOT);
        for (SpiritWeather weather : values()) {
            if (weather.id.equals(normalized)) return weather;
        }
        return null;
    }
}
