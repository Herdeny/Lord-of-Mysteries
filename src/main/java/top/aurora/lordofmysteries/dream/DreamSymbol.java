package top.aurora.lordofmysteries.dream;

public enum DreamSymbol {
    EMPTY_CHAIR("empty_chair", DreamAction.COMFORT),
    REPEATING_DOOR("repeating_door", DreamAction.ANCHOR),
    BACKWARD_CLOCK("backward_clock", DreamAction.INTERPRET),
    FACELESS_PORTRAIT("faceless_portrait", DreamAction.CONFRONT),
    DROWNED_LEDGER("drowned_ledger", DreamAction.INTERPRET),
    SILENT_BELL("silent_bell", DreamAction.ANCHOR),
    BORROWED_VOICE("borrowed_voice", DreamAction.COMFORT),
    FALSE_SUNRISE("false_sunrise", DreamAction.CONFRONT);

    private final String id;
    private final DreamAction preferredAction;

    DreamSymbol(String id, DreamAction preferredAction) {
        this.id = id;
        this.preferredAction = preferredAction;
    }

    public String id() {
        return id;
    }

    public DreamAction preferredAction() {
        return preferredAction;
    }

    public String translationKey() {
        return "dream_symbol.lord_of_mysteries." + id;
    }

    public static DreamSymbol fromId(String id) {
        if (id == null) return null;
        for (DreamSymbol symbol : values()) {
            if (symbol.id.equalsIgnoreCase(id)) return symbol;
        }
        return null;
    }
}
