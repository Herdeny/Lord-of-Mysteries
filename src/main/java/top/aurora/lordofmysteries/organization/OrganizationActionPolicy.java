package top.aurora.lordofmysteries.organization;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.resources.ResourceLocation;

public final class OrganizationActionPolicy {

    public static final int DAILY_ACTION_COUNT = 3;

    private OrganizationActionPolicy() {}

    public static List<PlannedAction> generate(
            long worldSeed, long day, float exposure,
            Map<ResourceLocation, OrganizationDefinition> definitions) {
        if (day < 0L || definitions == null || definitions.isEmpty()) {
            return List.of();
        }
        List<OrganizationDefinition> organizations =
                definitions.values().stream()
                        .sorted(Comparator.comparing(
                                value -> value.id().toString()))
                        .toList();
        int count = Math.min(DAILY_ACTION_COUNT, organizations.size());
        List<PlannedAction> actions = new ArrayList<>(count);
        Set<ResourceLocation> selected = new HashSet<>();
        long base = mix(worldSeed ^ day * 0x9E3779B97F4A7C15L);
        for (int slot = 0; slot < count; slot++) {
            int index = Math.floorMod(
                    (int) mix(base + slot * 0x632BE59BD9B4E019L),
                    organizations.size());
            while (!selected.add(organizations.get(index).id())) {
                index = (index + 1) % organizations.size();
            }
            OrganizationDefinition organization = organizations.get(index);
            OrganizationActionType type = selectType(
                    organization, mix(base ^ organization.id().hashCode()
                            ^ slot * 0x94D049BB133111EBL));
            int risk = Math.min(
                    5,
                    type.baseRisk()
                            + (exposure >= 40f ? 1 : 0)
                            + (exposure >= 70f ? 1 : 0));
            actions.add(new PlannedAction(
                    slot + 1, organization.id(), type, risk));
        }
        return List.copyOf(actions);
    }

    private static OrganizationActionType selectType(
            OrganizationDefinition definition, long seed) {
        List<OrganizationActionType> types =
                new ArrayList<>(EnumSet.copyOf(
                        definition.strategyWeights().keySet()));
        types.sort(Comparator.comparing(OrganizationActionType::id));
        double total = types.stream()
                .mapToDouble(type ->
                        definition.strategyWeights().get(type))
                .sum();
        double cursor = unsignedUnit(seed) * total;
        for (OrganizationActionType type : types) {
            cursor -= definition.strategyWeights().get(type);
            if (cursor <= 0d) return type;
        }
        return types.get(types.size() - 1);
    }

    private static double unsignedUnit(long value) {
        long bits = mix(value) >>> 11;
        return bits * 0x1.0p-53;
    }

    private static long mix(long value) {
        value ^= value >>> 30;
        value *= 0xBF58476D1CE4E5B9L;
        value ^= value >>> 27;
        value *= 0x94D049BB133111EBL;
        return value ^ value >>> 31;
    }

    public record PlannedAction(
            int slot,
            ResourceLocation organization,
            OrganizationActionType type,
            int risk) {

        public PlannedAction {
            if (slot < 1 || organization == null || type == null
                    || risk < 1 || risk > 5) {
                throw new IllegalArgumentException(
                        "invalid planned organization action");
            }
        }
    }
}
