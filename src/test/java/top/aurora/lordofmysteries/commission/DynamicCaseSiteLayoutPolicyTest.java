package top.aurora.lordofmysteries.commission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class DynamicCaseSiteLayoutPolicyTest {

    @Test
    void concurrentCasesReceiveStableUniqueLanes() {
        List<DynamicCaseProfile> profiles = profiles(20);
        Map<String, DynamicCaseSiteLayoutPolicy.Offset> first =
                DynamicCaseSiteLayoutPolicy.assign(profiles);
        Collections.reverse(profiles);
        Map<String, DynamicCaseSiteLayoutPolicy.Offset> second =
                DynamicCaseSiteLayoutPolicy.assign(profiles);

        assertEquals(first, second);
        assertEquals(20, first.size());
        assertEquals(first.size(), new HashSet<>(first.values()).size());
        assertTrue(first.values().stream().allMatch(offset ->
                Math.abs(offset.x()) <= 10 && Math.abs(offset.z()) <= 10));
    }

    @Test
    void crowdedSiteCapsPhysicalScenesWithoutOverlapping() {
        List<DynamicCaseProfile> profiles =
                profiles(DynamicCaseSiteLayoutPolicy.MAX_VISIBLE_INSTANCES + 3);
        Map<String, DynamicCaseSiteLayoutPolicy.Offset> assignments =
                DynamicCaseSiteLayoutPolicy.assign(profiles);

        assertEquals(DynamicCaseSiteLayoutPolicy.MAX_VISIBLE_INSTANCES,
                assignments.size());
        assertEquals(assignments.size(),
                new HashSet<>(assignments.values()).size());
        assertEquals(3L, profiles.stream()
                .filter(profile -> !assignments.containsKey(
                        profile.instanceId()))
                .count());
    }

    private static List<DynamicCaseProfile> profiles(int count) {
        List<DynamicCaseProfile> profiles = new ArrayList<>();
        for (long day = 0; day < count; day++) {
            profiles.add(DynamicCaseGenerator.generateForDay(77219L, day));
        }
        return profiles;
    }
}
