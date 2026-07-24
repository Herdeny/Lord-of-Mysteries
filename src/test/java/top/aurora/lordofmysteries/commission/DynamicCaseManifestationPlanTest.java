package top.aurora.lordofmysteries.commission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class DynamicCaseManifestationPlanTest {

    @Test
    void layoutIsStableAndKeepsRolesSeparate() {
        DynamicCaseProfile profile =
                DynamicCaseGenerator.generateForDay(77219L, 4L);

        DynamicCaseManifestationPlan first =
                DynamicCaseManifestationPlan.forProfile(profile);
        DynamicCaseManifestationPlan second =
                DynamicCaseManifestationPlan.forProfile(profile);

        assertEquals(first, second);
        assertNotEquals(first.subject(), first.affected());
        assertNotEquals(first.subject(), first.evidence());
        assertNotEquals(first.routine(), first.subject());
        assertNotEquals(first.routine(), first.affected());
        assertNotEquals(first.routine(), first.evidence());
        assertNotEquals(first.affected(), first.evidence());
    }

    @Test
    void everyRoleStaysInsideTheInvestigationSite() {
        for (long day = 0; day < 64; day++) {
            DynamicCaseManifestationPlan plan =
                    DynamicCaseManifestationPlan.forProfile(
                            DynamicCaseGenerator.generateForDay(8128L, day));
            for (DynamicCaseManifestationPlan.Offset offset
                    : List.of(plan.subject(), plan.routine(),
                            plan.affected(), plan.evidence())) {
                assertTrue(Math.abs(offset.x()) <= 3);
                assertTrue(Math.abs(offset.z()) <= 3);
            }
        }
    }
}
