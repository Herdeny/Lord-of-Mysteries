package top.aurora.lordofmysteries.commission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class DynamicCaseWeeklyDirectiveTest {

    @Test
    void directiveSelectionIsStableAndOrganizationScoped() {
        for (DynamicCaseProfile.Organization organization
                : DynamicCaseProfile.Organization.values()) {
            DynamicCaseWeeklyDirective first =
                    DynamicCaseWeeklyDirective.select(
                            918273645L, 12L, organization);
            DynamicCaseWeeklyDirective second =
                    DynamicCaseWeeklyDirective.select(
                            918273645L, 12L, organization);

            assertEquals(first, second);
            assertEquals(organization, first.organization());
        }
    }

    @Test
    void weeklyPoolCanReachBothDirectivesForEveryOrganization() {
        for (DynamicCaseProfile.Organization organization
                : DynamicCaseProfile.Organization.values()) {
            Set<DynamicCaseWeeklyDirective> observed = new HashSet<>();
            for (long week = 0L; week < 32L; week++) {
                observed.add(DynamicCaseWeeklyDirective.select(
                        48271L, week, organization));
            }
            assertEquals(2, observed.size());
            assertTrue(observed.stream().allMatch(directive ->
                    directive.organization() == organization));
        }
    }
}
