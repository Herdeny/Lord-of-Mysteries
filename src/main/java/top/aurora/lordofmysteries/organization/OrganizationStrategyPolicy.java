package top.aurora.lordofmysteries.organization;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;

public final class OrganizationStrategyPolicy {

    public static final int WEEK_LENGTH_DAYS = 7;

    private OrganizationStrategyPolicy() {}

    public static Map<ResourceLocation, Directive> generate(
            long worldSeed, long week, float exposure,
            Map<ResourceLocation, OrganizationDefinition> definitions) {
        if (week < 0L || definitions == null || definitions.isEmpty()) {
            return Map.of();
        }
        Map<ResourceLocation, Directive> directives =
                new LinkedHashMap<>();
        definitions.values().stream()
                .sorted(Comparator.comparing(
                        value -> value.id().toString()))
                .forEach(definition -> {
                    long seed = mix(worldSeed
                            ^ week * 0x9E3779B97F4A7C15L
                            ^ definition.id().hashCode());
                    OrganizationActionType focus = selectType(
                            definition, seed);
                    int risk = Math.min(5,
                            focus.baseRisk()
                                    + (exposure >= 40f ? 1 : 0)
                                    + (exposure >= 70f ? 1 : 0));
                    directives.put(definition.id(), new Directive(
                            definition.id(), focus, risk, 0, 0));
                });
        return Map.copyOf(directives);
    }

    public static boolean autonomousSuccess(
            long worldSeed, long day,
            OrganizationActionPolicy.PlannedAction action,
            Directive directive) {
        if (action == null || day < 0L) return false;
        double chance = 0.68d - action.risk() * 0.08d;
        if (directive != null) {
            if (directive.focus() == action.type()) chance += 0.15d;
            chance += Math.max(-0.12d, Math.min(
                    0.12d,
                    (directive.successes() - directive.failures())
                            * 0.02d));
        }
        chance = Math.max(0.15d, Math.min(0.85d, chance));
        long seed = mix(worldSeed
                ^ day * 0x632BE59BD9B4E019L
                ^ action.organization().hashCode()
                ^ action.slot() * 0x94D049BB133111EBL);
        return unsignedUnit(seed) < chance;
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

    public record Directive(
            ResourceLocation organization,
            OrganizationActionType focus,
            int risk,
            int successes,
            int failures) {

        public Directive {
            if (organization == null || focus == null
                    || risk < 1 || risk > 5
                    || successes < 0 || failures < 0) {
                throw new IllegalArgumentException(
                        "invalid organization weekly directive");
            }
        }

        public Directive withOutcome(boolean success) {
            return new Directive(
                    organization, focus, risk,
                    success ? saturatingIncrement(successes) : successes,
                    success ? failures : saturatingIncrement(failures));
        }

        private static int saturatingIncrement(int value) {
            return value == Integer.MAX_VALUE ? value : value + 1;
        }
    }
}
