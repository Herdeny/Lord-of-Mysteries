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
    PSYCHIATRIST7_PACIFY_CROWD("spectator", 7,
            "psychiatrist7_pacify_crowd", 15f, 3600L),
    PSYCHIATRIST7_DEESCALATE("spectator", 7,
            "psychiatrist7_deescalate", 12f, 4800L),
    PSYCHIATRIST7_INTERRUPT("spectator", 7,
            "psychiatrist7_interrupt", 10f, 2400L),
    PSYCHIATRIST7_CLOAK("spectator", 7,
            "psychiatrist7_cloak", 15f, 6000L),
    PYROMANIAC7_LONG_SHOT("hunter", 7,
            "pyromaniac7_long_shot", 12f, 2400L),
    PYROMANIAC7_FIRE_RING("hunter", 7,
            "pyromaniac7_fire_ring", 15f, 3600L),
    PYROMANIAC7_WALK_THROUGH_FIRE("hunter", 7,
            "pyromaniac7_walk_through_fire", 15f, 6000L),
    THIEF9_FIRST_STEAL("thief", 9,
            "thief9_first_steal", 10f, 2400L),
    THIEF9_GHOST("thief", 9,
            "thief9_ghost", 15f, 6000L),
    THIEF9_PICK_TARGET("thief", 9,
            "thief9_pick_target", 12f, 4800L),
    SWINDLER8_BIG_CON("thief", 8,
            "swindler8_big_con", 18f, 6000L),
    SWINDLER8_SWAP_ESCAPE("thief", 8,
            "swindler8_swap_escape", 15f, 4800L),
    SWINDLER8_HONEST_DAY("thief", 8,
            "swindler8_honest_day", 8f, 24000L),
    CRYPTOLOGIST7_ANCIENT_READ("thief", 7,
            "cryptologist7_ancient_read", 18f, 6000L),
    CRYPTOLOGIST7_PERFECT_CRIME("thief", 7,
            "cryptologist7_perfect_crime", 20f, 6000L),
    CRYPTOLOGIST7_SHARE_SECRET("thief", 7,
            "cryptologist7_share_secret", 10f, 4800L),
    APPRENTICE9_BOOKWORM("apprentice", 9,
            "apprentice9_bookworm", 10f, 2400L),
    APPRENTICE9_TEACH("apprentice", 9,
            "apprentice9_teach", 12f, 4800L),
    APPRENTICE9_FIELD_NOTE("apprentice", 9,
            "apprentice9_field_note", 8f, 6000L),
    TRICKMASTER8_COMBO_DISCOVERY("apprentice", 8,
            "trickmaster8_combo_discovery", 18f, 6000L),
    TRICKMASTER8_DOOR_TACTICS("apprentice", 8,
            "trickmaster8_door_tactics", 15f, 4800L),
    TRICKMASTER8_SHOW_OFF("apprentice", 8,
            "trickmaster8_show_off", 8f, 3600L),
    ASTROLOGER7_ACCURATE_FORECAST("apprentice", 7,
            "astrologer7_accurate_forecast", 18f, 12000L),
    ASTROLOGER7_WARD_SAVE("apprentice", 7,
            "astrologer7_ward_save", 15f, 6000L),
    ASTROLOGER7_STAR_ATLAS("apprentice", 7,
            "astrologer7_star_atlas", 12f, 24000L),
    FACELESS6_MAINTAIN_COVER("seer", 6,
            "faceless6_maintain_cover", 14f, 6000L),
    FACELESS6_RESTRAIN_WITHOUT_IDENTITY("seer", 6,
            "faceless6_restrain_without_identity", 18f, 6000L),
    MARIONETTIST5_REVEAL_THREADS("seer", 5,
            "marionettist5_reveal_threads", 15f, 6000L),
    MARIONETTIST5_RESTRAIN_CRISIS("seer", 5,
            "marionettist5_restrain_crisis", 20f, 9000L),
    HYPNOTIST6_DEESCALATE_HOSTILE("spectator", 6,
            "hypnotist6_deescalate_hostile", 18f, 6000L),
    HYPNOTIST6_BARRIER_ALLY("spectator", 6,
            "hypnotist6_barrier_ally", 16f, 9000L),
    DREAMWALKER5_LUCID_RECOVERY("spectator", 5,
            "dreamwalker5_lucid_recovery", 15f, 12000L),
    DREAMWALKER5_LULL_THREAT("spectator", 5,
            "dreamwalker5_lull_threat", 18f, 6000L),
    CONSPIRER6_PREPARE_BATTLEFIELD("hunter", 6,
            "conspirer6_prepare_battlefield", 16f, 12000L),
    CONSPIRER6_TURN_ENEMIES("hunter", 6,
            "conspirer6_turn_enemies", 20f, 9000L),
    REAPER5_SWEEP_FLAMES("hunter", 5,
            "reaper5_sweep_flames", 18f, 6000L),
    REAPER5_HARVEST_PREY("hunter", 5,
            "reaper5_harvest_prey", 20f, 9000L),
    PROMETHEUS6_STEAL_POWER("thief", 6,
            "prometheus6_steal_power", 20f, 12000L),
    PROMETHEUS6_RECOVER_UNOWNED("thief", 6,
            "prometheus6_recover_unowned", 12f, 6000L),
    DREAM_STEALER5_RETRIEVE_DREAM("thief", 5,
            "dream_stealer5_retrieve_dream", 18f, 12000L),
    DREAM_STEALER5_ESCAPE_REALITY("thief", 5,
            "dream_stealer5_escape_reality", 15f, 6000L),
    SCRIBE6_COPY_KNOWLEDGE("apprentice", 6,
            "scribe6_copy_knowledge", 18f, 12000L),
    SCRIBE6_ARCHIVE_FOCUS("apprentice", 6,
            "scribe6_archive_focus", 12f, 9000L),
    TRAVELER5_CROSS_DISTANCE("apprentice", 5,
            "traveler5_cross_distance", 16f, 6000L),
    TRAVELER5_RETURN_OUTPOST("apprentice", 5,
            "traveler5_return_outpost", 20f, 24000L);

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
