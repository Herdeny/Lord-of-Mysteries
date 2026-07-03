package top.aurora.lordofmysteries.acting;

public enum ActingEvent {
    DIVINATION_SUCCESS("seer", 9, "seer9_divination_success", 12f, 1200L),
    ABSTAIN_DIVINATION("seer", 9, "seer9_abstain_divination", 8f, 0L),
    INTERPRET_AMBIGUOUS("seer", 9, "seer9_interpret_ambiguous", 15f, 2400L),
    HELP_PLAYER_ESCAPE("seer", 9, "seer9_help_player_escape", 18f, 4800L),
    OVER_DIVINATION_PENALTY("seer", 9, "seer9_over_divination_penalty", -5f, 0L),
    SPECTATOR9_PREDICT_FIVE("spectator", 9,
            "spectator9_predict_5attacks", 10f, 2400L),
    SPECTATOR9_OBSERVE_WITHOUT_FIGHT("spectator", 9,
            "spectator9_observe_without_fight", 15f, 6000L),
    SPECTATOR8_SURFACE_READ("spectator", 8,
            "spectator8_surface_read", 12f, 2400L),
    SPECTATOR8_MENTAL_SUGGESTION("spectator", 8,
            "spectator8_mental_suggestion", 15f, 4800L);

    private final String pathway;
    private final int sequence;
    private final String id;
    private final float baseDigestion;
    private final long noveltyDecayTicks;

    ActingEvent(String pathway, int sequence, String id,
                float baseDigestion, long noveltyDecayTicks) {
        this.pathway = pathway;
        this.sequence = sequence;
        this.id = id;
        this.baseDigestion = baseDigestion;
        this.noveltyDecayTicks = noveltyDecayTicks;
    }

    public String pathway() {
        return pathway;
    }

    public int sequence() {
        return sequence;
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
