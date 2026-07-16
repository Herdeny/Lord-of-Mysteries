package top.aurora.lordofmysteries.knowledge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class M1ProgressAdvisorTest {

    @Test
    void coversEveryVerticalSliceStage() {
        assertEquals(M1ProgressAdvisor.Stage.FIND_CAMP,
                M1ProgressAdvisor.evaluate(false, null, -1, 0f, false));
        assertEquals(M1ProgressAdvisor.Stage.BREW_SEER,
                M1ProgressAdvisor.evaluate(true, null, -1, 0f, false));
        assertEquals(M1ProgressAdvisor.Stage.DIGEST_SEER,
                M1ProgressAdvisor.evaluate(true, "lord_of_mysteries:seer", 9, 99f, false));
        assertEquals(M1ProgressAdvisor.Stage.BREW_CLOWN,
                M1ProgressAdvisor.evaluate(true, "lord_of_mysteries:seer", 9, 100f, false));
        assertEquals(M1ProgressAdvisor.Stage.DIGEST_CLOWN,
                M1ProgressAdvisor.evaluate(true, "lord_of_mysteries:seer", 8, 80f, false));
        assertEquals(M1ProgressAdvisor.Stage.HUNT_SERPENT,
                M1ProgressAdvisor.evaluate(true, "lord_of_mysteries:seer", 8, 100f, false));
        assertEquals(M1ProgressAdvisor.Stage.BREW_MAGICIAN,
                M1ProgressAdvisor.evaluate(true, "lord_of_mysteries:seer", 8, 100f, true));
        assertEquals(M1ProgressAdvisor.Stage.COMPLETE,
                M1ProgressAdvisor.evaluate(true, "lord_of_mysteries:seer", 7, 0f, false));
        assertEquals(M1ProgressAdvisor.Stage.OTHER_PATHWAY,
                M1ProgressAdvisor.evaluate(true, "lord_of_mysteries:hunter", 9, 0f, false));
    }
}
