package top.aurora.lordofmysteries.dream;

public enum DreamScenario {
    MISSING_GUEST("missing_guest"),
    REPEATING_THEATRE("repeating_theatre"),
    DROWNED_ARCHIVE("drowned_archive"),
    COLORLESS_STREET("colorless_street");

    private final String id;

    DreamScenario(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String translationKey() {
        return "dream_scenario.lord_of_mysteries." + id;
    }

    public static DreamScenario fromId(String id) {
        if (id == null) return null;
        for (DreamScenario scenario : values()) {
            if (scenario.id.equalsIgnoreCase(id)) return scenario;
        }
        return null;
    }
}
