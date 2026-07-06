package top.aurora.lordofmysteries.acting;

public enum ActingEvent {
    DIVINATION_SUCCESS("seer", 9, "seer9_divination_success", 12f, 1200L),
    ABSTAIN_DIVINATION("seer", 9, "seer9_abstain_divination", 8f, 0L),
    INTERPRET_AMBIGUOUS("seer", 9, "seer9_interpret_ambiguous", 15f, 2400L),
    HELP_PLAYER_ESCAPE("seer", 9, "seer9_help_player_escape", 18f, 4800L),
    OVER_DIVINATION_PENALTY("seer", 9, "seer9_over_divination_penalty", -5f, 0L),
    CLOWN8_PERFORMANCE("seer", 8, "clown8_performance", 12f, 3600L),
    CLOWN8_SMILE_MASK("seer", 8, "clown8_smile_mask", 15f, 4800L),
    CLOWN8_DODGE_MASTER("seer", 8, "clown8_dodge_master", 10f, 3600L),
    MAGICIAN7_GRAND_ESCAPE("seer", 7, "magician7_grand_escape", 20f, 6000L),
    MAGICIAN7_FLASHY_ENTRY("seer", 7, "magician7_flashy_entry", 12f, 3600L),
    MAGICIAN7_DECEIVE_MOB("seer", 7, "magician7_deceive_mob", 15f, 4800L),
    SPECTATOR9_PREDICT_FIVE("spectator", 9,
            "spectator9_predict_5attacks", 10f, 2400L),
    SPECTATOR9_OBSERVE_WITHOUT_FIGHT("spectator", 9,
            "spectator9_observe_without_fight", 15f, 6000L),
    SPECTATOR8_SURFACE_READ("spectator", 8,
            "spectator8_surface_read", 12f, 2400L),
    SPECTATOR8_MENTAL_SUGGESTION("spectator", 8,
            "spectator8_mental_suggestion", 15f, 4800L),
    HUNTER9_TRACK_AND_KILL("hunter", 9,
            "hunter9_track_and_kill", 15f, 4800L),
    HUNTER9_TRAP_SETUP("hunter", 9,
            "hunter9_trap_setup", 12f, 3600L),
    HUNTER8_PROVOKE_THREE("hunter", 8,
            "hunter8_provoke_three", 15f, 3600L),
    HUNTER8_ENRAGE("hunter", 8,
            "hunter8_enrage", 12f, 2400L),
    HUNTER8_BATTLE_WILL("hunter", 8,
            "hunter8_battle_will", 15f, 4800L),
    THIEF9_FIRST_STEAL("thief", 9,
            "thief9_first_steal", 10f, 2400L),
    THIEF9_GHOST("thief", 9,
            "thief9_ghost", 15f, 6000L),
    THIEF9_PICK_TARGET("thief", 9,
            "thief9_pick_target", 12f, 4800L),
    APPRENTICE9_BOOKWORM("apprentice", 9,
            "apprentice9_bookworm", 10f, 2400L),
    APPRENTICE9_TEACH("apprentice", 9,
            "apprentice9_teach", 12f, 4800L),
    APPRENTICE9_FIELD_NOTE("apprentice", 9,
            "apprentice9_field_note", 8f, 6000L);

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
