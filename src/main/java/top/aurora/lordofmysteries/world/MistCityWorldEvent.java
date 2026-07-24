package top.aurora.lordofmysteries.world;

import java.util.Locale;

public enum MistCityWorldEvent {
    CLEAR,
    DENSE_FOG,
    SPIRITUAL_SURGE,
    BLOOD_MOON,
    EVIL_GAZE,
    WITCH_HUNT_NIGHT,
    RITUAL_RESONANCE;

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String translationKey() {
        return "world_event.lord_of_mysteries." + id();
    }

    public static MistCityWorldEvent fromId(String value) {
        if (value == null) return CLEAR;
        for (MistCityWorldEvent event : values()) {
            if (event.id().equals(value)) return event;
        }
        return CLEAR;
    }
}
