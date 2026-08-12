package top.aurora.lordofmysteries.artifact;

import java.util.List;
import java.util.Locale;

import com.google.gson.JsonParseException;

import net.minecraft.resources.ResourceLocation;

import top.aurora.lordofmysteries.ProjectMystery;

public enum ArtifactIncidentProfile {
    FIRE("fire", List.of(
            modRequirement("calming_incense", 1),
            modRequirement("pure_water", 1))),
    ECHO("echo", List.of(
            modRequirement("calming_incense", 2),
            vanillaRequirement("string", 4))),
    COGNITION("cognition", List.of(
            modRequirement("knowledge_copy", 1),
            modRequirement("pure_water", 1))),
    STORM("storm", List.of(
            vanillaRequirement("copper_ingot", 4),
            modRequirement("pure_water", 1))),
    HUNGER("hunger", List.of(
            vanillaRequirement("bread", 8),
            modRequirement("calming_incense", 1))),
    VOID("void", List.of(
            vanillaRequirement("amethyst_shard", 4),
            modRequirement("pure_water", 2)));

    private final String id;
    private final List<Requirement> requirements;

    ArtifactIncidentProfile(String id, List<Requirement> requirements) {
        this.id = id;
        this.requirements = List.copyOf(requirements);
    }

    public String id() {
        return id;
    }

    public List<Requirement> requirements() {
        return requirements;
    }

    public String translationKey() {
        return "artifact_incident.lord_of_mysteries." + id;
    }

    public static ArtifactIncidentProfile fromId(String id) {
        if (id == null) {
            throw new JsonParseException("artifact incident profile is required");
        }
        String normalized = id.trim().toLowerCase(Locale.ROOT);
        for (ArtifactIncidentProfile profile : values()) {
            if (profile.id.equals(normalized)) return profile;
        }
        throw new JsonParseException(
                "unknown artifact incident profile " + id);
    }

    private static Requirement modRequirement(String path, int count) {
        return new Requirement(ResourceLocation.fromNamespaceAndPath(
                ProjectMystery.MOD_ID, path), count);
    }

    private static Requirement vanillaRequirement(String path, int count) {
        return new Requirement(ResourceLocation.withDefaultNamespace(path), count);
    }

    public record Requirement(ResourceLocation item, int count) {

        public Requirement {
            if (item == null || count < 1 || count > 64) {
                throw new IllegalArgumentException(
                        "invalid artifact containment requirement");
            }
        }
    }
}
