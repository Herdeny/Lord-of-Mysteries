package top.aurora.lordofmysteries.acting;

public enum ActingEvent {
    DIVINATION_SUCCESS("seer9_divination_success", 12f, 1200L),
    ABSTAIN_DIVINATION("seer9_abstain_divination", 8f, 0L),
    INTERPRET_AMBIGUOUS("seer9_interpret_ambiguous", 15f, 2400L),
    HELP_PLAYER_ESCAPE("seer9_help_player_escape", 18f, 4800L),
    OVER_DIVINATION_PENALTY("seer9_over_divination_penalty", -5f, 0L);

    private final String id;
    private final float baseDigestion;
    private final long noveltyDecayTicks;

    ActingEvent(String id, float baseDigestion, long noveltyDecayTicks) {
        this.id = id;
        this.baseDigestion = baseDigestion;
        this.noveltyDecayTicks = noveltyDecayTicks;
    }

    public String id() {
        return id;
    }

    public float baseDigestion() {
        return baseDigestion;
    }

    public long noveltyDecayTicks() {
        return noveltyDecayTicks;
    }
}
