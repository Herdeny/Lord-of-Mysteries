package top.aurora.lordofmysteries.spirit;

public enum SpiritEncounter {
    LANTERN_MOTH_SWARM("lantern_moth_swarm", SpiritEncounterAction.OBSERVE, 1),
    MEMORY_LEECH("memory_leech", SpiritEncounterAction.AVOID, 3),
    PRAYER_ECHO("prayer_echo", SpiritEncounterAction.AID, 1),
    SPIRIT_FERRYMAN("spirit_ferryman", SpiritEncounterAction.BARGAIN, 2),
    COLOR_EATER("color_eater", SpiritEncounterAction.AVOID, 2),
    DOOR_WISP("door_wisp", SpiritEncounterAction.OBSERVE, 1),
    WHISPER_CROW("whisper_crow", SpiritEncounterAction.BARGAIN, 2),
    COMPASS_BIRD("compass_bird", SpiritEncounterAction.AID, 1),
    ARCHIVE_SPIDER("archive_spider", SpiritEncounterAction.OBSERVE, 2),
    GASLIGHT_SPECTER("gaslight_specter", SpiritEncounterAction.AVOID, 3),
    THEATRE_MASKLING("theatre_maskling", SpiritEncounterAction.BARGAIN, 2),
    GRAVE_LANTERN("grave_lantern", SpiritEncounterAction.AID, 2);

    private final String id;
    private final SpiritEncounterAction preferredAction;
    private final int risk;

    SpiritEncounter(
            String id, SpiritEncounterAction preferredAction, int risk) {
        this.id = id;
        this.preferredAction = preferredAction;
        this.risk = risk;
    }

    public String id() {
        return id;
    }

    public SpiritEncounterAction preferredAction() {
        return preferredAction;
    }

    public int risk() {
        return risk;
    }

    public String translationKey() {
        return "spirit_encounter.lord_of_mysteries." + id;
    }
}
