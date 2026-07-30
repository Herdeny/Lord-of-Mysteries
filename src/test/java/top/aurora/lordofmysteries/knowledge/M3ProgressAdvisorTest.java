package top.aurora.lordofmysteries.knowledge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class M3ProgressAdvisorTest {

    @Test
    void routesAllFivePathwaysToDedicatedSequenceSixMaterials() {
        assertMaterial("seer", "shapeshifter_blood");
        assertMaterial("spectator", "cradle_moth_eye");
        assertMaterial("hunter", "twin_serpent_tongue");
        assertMaterial("thief", "ability_leech_core");
        assertMaterial("apprentice", "scribe_golem_finger_bone");
    }

    @Test
    void sequenceFiveRitualPrecedesFinalIngredientAdvice() {
        M3ProgressAdvisor.Advice blocked =
                M3ProgressAdvisor.evaluate(
                        "apprentice", 6, 100f, false);
        assertEquals(
                M3ProgressAdvisor.Stage
                        .PERFORM_SEQUENCE_FIVE_RITUAL,
                blocked.stage());
        assertEquals("spatial_rift_crystal",
                blocked.materialItemPath());

        M3ProgressAdvisor.Advice ready =
                M3ProgressAdvisor.evaluate(
                        "apprentice", 6, 100f, true);
        assertEquals(
                M3ProgressAdvisor.Stage.GATHER_SEQUENCE_FIVE,
                ready.stage());
        assertEquals("spatial_rift_crystal",
                ready.materialItemPath());
    }

    @Test
    void coversFoundationDigestionAndSequenceFiveMastery() {
        assertEquals(
                M3ProgressAdvisor.Stage.FOUNDATION_PATHWAY,
                M3ProgressAdvisor.evaluate(
                        "hunter", 9, 0f, false).stage());
        assertEquals(
                M3ProgressAdvisor.Stage.DIGEST_SEQUENCE_SEVEN,
                M3ProgressAdvisor.evaluate(
                        "hunter", 7, 99.9f, false).stage());
        assertEquals(
                M3ProgressAdvisor.Stage.DIGEST_SEQUENCE_SIX,
                M3ProgressAdvisor.evaluate(
                        "hunter", 6, 40f, false).stage());
        assertEquals(
                M3ProgressAdvisor.Stage.MASTER_SEQUENCE_FIVE,
                M3ProgressAdvisor.evaluate(
                        "hunter", 5, 0f, true).stage());
    }

    @Test
    void rejectsUnsupportedOrOutOfScopeCharacters() {
        assertNull(M3ProgressAdvisor.evaluate(
                "sailor", 7, 100f, false));
        assertNull(M3ProgressAdvisor.evaluate(
                "seer", 4, 100f, true));
        assertNull(M3ProgressAdvisor.evaluate(
                "seer", -1, 0f, false));
    }

    private static void assertMaterial(
            String pathway, String expectedMaterial) {
        M3ProgressAdvisor.Advice advice =
                M3ProgressAdvisor.evaluate(
                        pathway, 7, 100f, false);
        assertEquals(
                M3ProgressAdvisor.Stage.GATHER_SEQUENCE_SIX,
                advice.stage());
        assertEquals(expectedMaterial, advice.materialItemPath());
    }
}
