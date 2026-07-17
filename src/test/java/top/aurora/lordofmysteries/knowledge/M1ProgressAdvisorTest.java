package top.aurora.lordofmysteries.knowledge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class M1ProgressAdvisorTest {

    @Test
    void coversEveryVerticalSliceStage() {
        assertEquals(M1ProgressAdvisor.Stage.FIND_CAMP,
                M1ProgressAdvisor.evaluate(false, null, -1, 0f, false,
                        false, false, false));
        assertEquals(M1ProgressAdvisor.Stage.BREW_SEER,
                M1ProgressAdvisor.evaluate(true, null, -1, 0f, false,
                        false, false, false));
        assertEquals(M1ProgressAdvisor.Stage.DIGEST_SEER,
                M1ProgressAdvisor.evaluate(true, "lord_of_mysteries:seer", 9,
                        99f, false, false, false, false));
        assertEquals(M1ProgressAdvisor.Stage.BREW_CLOWN,
                M1ProgressAdvisor.evaluate(true, "lord_of_mysteries:seer", 9,
                        100f, false, false, false, false));
        assertEquals(M1ProgressAdvisor.Stage.DIGEST_CLOWN,
                M1ProgressAdvisor.evaluate(true, "lord_of_mysteries:seer", 8,
                        80f, false, false, false, false));
        assertEquals(M1ProgressAdvisor.Stage.HUNT_SERPENT,
                M1ProgressAdvisor.evaluate(true, "lord_of_mysteries:seer", 8,
                        100f, false, false, false, false));
        assertEquals(M1ProgressAdvisor.Stage.BREW_MAGICIAN,
                M1ProgressAdvisor.evaluate(true, "lord_of_mysteries:seer", 8,
                        100f, true, false, false, false));
        assertEquals(M1ProgressAdvisor.Stage.BIND_IDENTITY,
                M1ProgressAdvisor.evaluate(true, "lord_of_mysteries:seer", 7,
                        0f, false, false, false, false));
        assertEquals(M1ProgressAdvisor.Stage.REFLECT,
                M1ProgressAdvisor.evaluate(true, "lord_of_mysteries:seer", 7,
                        0f, false, true, false, false));
        assertEquals(M1ProgressAdvisor.Stage.CITY_LIFE,
                M1ProgressAdvisor.evaluate(true, "lord_of_mysteries:seer", 7,
                        0f, false, true, true, false));
        assertEquals(M1ProgressAdvisor.Stage.COMPLETE,
                M1ProgressAdvisor.evaluate(true, "lord_of_mysteries:seer", 7,
                        0f, false, true, true, true));
        assertEquals(M1ProgressAdvisor.Stage.OTHER_PATHWAY,
                M1ProgressAdvisor.evaluate(true, "lord_of_mysteries:hunter", 9,
                        0f, false, false, false, false));
    }
}
