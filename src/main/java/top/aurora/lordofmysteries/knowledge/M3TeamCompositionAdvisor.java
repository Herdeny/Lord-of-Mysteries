package top.aurora.lordofmysteries.knowledge;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class M3TeamCompositionAdvisor {

    private static final Map<String, Set<Role>> PATHWAY_ROLES = Map.of(
            "seer", Set.of(Role.INVESTIGATION, Role.CONTROL),
            "spectator", Set.of(Role.CONTROL, Role.SUPPORT),
            "hunter", Set.of(Role.COMBAT, Role.SURVIVAL),
            "thief", Set.of(Role.INFILTRATION, Role.RESOURCES),
            "apprentice", Set.of(Role.MOBILITY, Role.SUPPORT));

    private M3TeamCompositionAdvisor() {}

    public static Result evaluate(Collection<String> pathways) {
        EnumSet<Role> covered = EnumSet.noneOf(Role.class);
        java.util.HashSet<String> distinctPathways =
                new java.util.HashSet<>();
        if (pathways != null) {
            for (String pathway : pathways) {
                Set<Role> roles = PATHWAY_ROLES.get(pathway);
                if (roles == null) continue;
                distinctPathways.add(pathway);
                covered.addAll(roles);
            }
        }
        EnumSet<Role> missing = EnumSet.allOf(Role.class);
        missing.removeAll(covered);
        return new Result(
                Set.copyOf(covered),
                Set.copyOf(missing),
                distinctPathways.size());
    }

    public enum Role {
        INVESTIGATION("investigation"),
        CONTROL("control"),
        SUPPORT("support"),
        COMBAT("combat"),
        SURVIVAL("survival"),
        INFILTRATION("infiltration"),
        RESOURCES("resources"),
        MOBILITY("mobility");

        private final String translationSuffix;

        Role(String translationSuffix) {
            this.translationSuffix = translationSuffix;
        }

        public String translationSuffix() {
            return translationSuffix;
        }
    }

    public record Result(
            Set<Role> covered,
            Set<Role> missing,
            int distinctPathways) {

        public boolean complete() {
            return missing.isEmpty();
        }
    }
}
