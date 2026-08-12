package top.aurora.lordofmysteries.spirit;

import java.util.Locale;

public enum SpiritEncounterAction {
    OBSERVE("observe"),
    AID("aid"),
    AVOID("avoid"),
    BARGAIN("bargain");

    private final String id;

    SpiritEncounterAction(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String translationKey() {
        return "spirit_action.lord_of_mysteries." + id;
    }

    public static SpiritEncounterAction fromId(String id) {
        if (id == null) return null;
        String normalized = id.trim().toLowerCase(Locale.ROOT);
        for (SpiritEncounterAction action : values()) {
            if (action.id.equals(normalized)) return action;
        }
        return null;
    }
}
