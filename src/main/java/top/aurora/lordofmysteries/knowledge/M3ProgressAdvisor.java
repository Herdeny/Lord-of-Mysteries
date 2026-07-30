package top.aurora.lordofmysteries.knowledge;

import java.util.Map;

public final class M3ProgressAdvisor {

    private static final Map<String, Materials> MATERIALS = Map.of(
            "seer", new Materials(
                    "shapeshifter_blood",
                    "marionette_vine_core"),
            "spectator", new Materials(
                    "cradle_moth_eye",
                    "sleeping_giant_eyelash"),
            "hunter", new Materials(
                    "twin_serpent_tongue",
                    "battlefield_iron_rose"),
            "thief", new Materials(
                    "ability_leech_core",
                    "dream_scale_fragment"),
            "apprentice", new Materials(
                    "scribe_golem_finger_bone",
                    "spatial_rift_crystal"));

    private M3ProgressAdvisor() {}

    public static Advice evaluate(
            String pathway, int sequence, float digestion,
            boolean sequenceFiveRitualKnown) {
        Materials materials = MATERIALS.get(pathway);
        if (materials == null || sequence < 5 || sequence > 9) {
            return null;
        }
        if (sequence >= 8) {
            return new Advice(Stage.FOUNDATION_PATHWAY, "");
        }
        if (sequence == 7) {
            return digestion < 100f
                    ? new Advice(Stage.DIGEST_SEQUENCE_SEVEN, "")
                    : new Advice(
                            Stage.GATHER_SEQUENCE_SIX,
                            materials.sequenceSix());
        }
        if (sequence == 6) {
            if (digestion < 100f) {
                return new Advice(Stage.DIGEST_SEQUENCE_SIX, "");
            }
            return sequenceFiveRitualKnown
                    ? new Advice(
                            Stage.GATHER_SEQUENCE_FIVE,
                            materials.sequenceFive())
                    : new Advice(
                            Stage.PERFORM_SEQUENCE_FIVE_RITUAL,
                            materials.sequenceFive());
        }
        return new Advice(Stage.MASTER_SEQUENCE_FIVE, "");
    }

    public static boolean supports(String pathway) {
        return MATERIALS.containsKey(pathway);
    }

    public enum Stage {
        FOUNDATION_PATHWAY("foundation_pathway"),
        DIGEST_SEQUENCE_SEVEN("digest_sequence_seven"),
        GATHER_SEQUENCE_SIX("gather_sequence_six"),
        DIGEST_SEQUENCE_SIX("digest_sequence_six"),
        PERFORM_SEQUENCE_FIVE_RITUAL("perform_sequence_five_ritual"),
        GATHER_SEQUENCE_FIVE("gather_sequence_five"),
        MASTER_SEQUENCE_FIVE("master_sequence_five");

        private final String translationSuffix;

        Stage(String translationSuffix) {
            this.translationSuffix = translationSuffix;
        }

        public String translationSuffix() {
            return translationSuffix;
        }
    }

    public record Advice(Stage stage, String materialItemPath) {}

    private record Materials(String sequenceSix, String sequenceFive) {}
}
