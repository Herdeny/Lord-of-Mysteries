package top.aurora.lordofmysteries.commission;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class DynamicCaseSiteLayoutPolicy {

    static final int MAX_VISIBLE_INSTANCES = 25;
    private static final int GRID_RADIUS = 2;
    private static final int LANE_SPACING = 5;
    private static final List<Offset> LANES = createLanes();

    private DynamicCaseSiteLayoutPolicy() {
    }

    static Map<String, Offset> assign(
            Collection<DynamicCaseProfile> profiles) {
        List<DynamicCaseProfile> ordered = profiles.stream()
                .sorted(java.util.Comparator.comparing(
                        DynamicCaseProfile::instanceId))
                .toList();
        Map<String, Offset> assignments = new LinkedHashMap<>();
        boolean[] occupied = new boolean[LANES.size()];
        for (DynamicCaseProfile profile : ordered) {
            if (assignments.size() >= MAX_VISIBLE_INSTANCES) break;
            int preferred = Math.floorMod(
                    profile.instanceId().hashCode(), LANES.size());
            for (int probe = 0; probe < LANES.size(); probe++) {
                int lane = (preferred + probe) % LANES.size();
                if (occupied[lane]) continue;
                occupied[lane] = true;
                assignments.put(profile.instanceId(), LANES.get(lane));
                break;
            }
        }
        return assignments;
    }

    private static List<Offset> createLanes() {
        java.util.ArrayList<Offset> lanes =
                new java.util.ArrayList<>(MAX_VISIBLE_INSTANCES);
        for (int z = -GRID_RADIUS; z <= GRID_RADIUS; z++) {
            for (int x = -GRID_RADIUS; x <= GRID_RADIUS; x++) {
                lanes.add(new Offset(x * LANE_SPACING, z * LANE_SPACING));
            }
        }
        return List.copyOf(lanes);
    }

    record Offset(int x, int z) {
        Offset {
            int maximum = GRID_RADIUS * LANE_SPACING;
            if (Math.abs(x) > maximum || Math.abs(z) > maximum
                    || x % LANE_SPACING != 0 || z % LANE_SPACING != 0) {
                throw new IllegalArgumentException(
                        "dynamic case site lane is invalid");
            }
        }
    }
}
