package top.aurora.lordofmysteries.dream;

public enum DreamAction {
    INTERPRET("interpret"),
    ANCHOR("anchor"),
    COMFORT("comfort"),
    CONFRONT("confront");

    private final String id;

    DreamAction(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String translationKey() {
        return "dream_action.lord_of_mysteries." + id;
    }

    public static DreamAction fromId(String id) {
        if (id == null) return null;
        for (DreamAction action : values()) {
            if (action.id.equalsIgnoreCase(id)) return action;
        }
        return null;
    }
}
