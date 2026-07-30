package top.aurora.lordofmysteries.knowledge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class M3TeamCompositionAdvisorTest {

    @Test
    void allFivePathwaysCoverEveryLaunchRole() {
        M3TeamCompositionAdvisor.Result result =
                M3TeamCompositionAdvisor.evaluate(List.of(
                        "seer",
                        "spectator",
                        "hunter",
                        "thief",
                        "apprentice"));

        assertTrue(result.complete());
        assertEquals(5, result.distinctPathways());
        assertEquals(
                M3TeamCompositionAdvisor.Role.values().length,
                result.covered().size());
    }

    @Test
    void singlePathwayReportsActionableGapsWithoutInflatingDiversity() {
        M3TeamCompositionAdvisor.Result result =
                M3TeamCompositionAdvisor.evaluate(List.of(
                        "hunter", "hunter", "unknown"));

        assertFalse(result.complete());
        assertEquals(1, result.distinctPathways());
        assertTrue(result.covered().contains(
                M3TeamCompositionAdvisor.Role.COMBAT));
        assertTrue(result.missing().contains(
                M3TeamCompositionAdvisor.Role.INVESTIGATION));
        assertFalse(result.missing().contains(
                M3TeamCompositionAdvisor.Role.SURVIVAL));
    }

    @Test
    void emptyRosterFailsClosedWithEveryRoleMissing() {
        M3TeamCompositionAdvisor.Result result =
                M3TeamCompositionAdvisor.evaluate(null);

        assertEquals(0, result.distinctPathways());
        assertTrue(result.covered().isEmpty());
        assertEquals(
                M3TeamCompositionAdvisor.Role.values().length,
                result.missing().size());
    }
}
