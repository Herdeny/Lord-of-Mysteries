package top.aurora.lordofmysteries.content;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public record ContentMetadata(
        int schemaVersion,
        String canonStatus,
        String sourceTier,
        List<String> sourceRefs,
        int spoilerLevel,
        ResourceLocation knowledgeGate,
        Links links,
        String implementationState) {

    public static final int CURRENT_SCHEMA_VERSION = 4;
    private static final Set<String> CANON_STATUSES = Set.of(
            "canon", "adaptation", "original", "placeholder");
    private static final Set<String> SOURCE_TIERS = Set.of(
            "S", "A", "B-tech", "C", "D");
    private static final Set<String> IMPLEMENTATION_STATES = Set.of(
            "planned", "data_ready", "code_ready", "asset_ready",
            "playable", "verified");

    public ContentMetadata {
        sourceRefs = List.copyOf(sourceRefs);
    }

    public static ContentMetadata parse(JsonObject json) {
        int schemaVersion = GsonHelper.getAsInt(json, "schema_version", -1);
        if (schemaVersion != CURRENT_SCHEMA_VERSION) {
            throw new JsonParseException("schema_version must be "
                    + CURRENT_SCHEMA_VERSION);
        }
        String canonStatus = requiredChoice(
                json, "canon_status", CANON_STATUSES);
        String sourceTier = requiredChoice(
                json, "source_tier", SOURCE_TIERS);
        List<String> sourceRefs = strings(
                GsonHelper.getAsJsonArray(json, "source_refs"), "source_refs");
        int spoilerLevel = GsonHelper.getAsInt(json, "spoiler_level", -1);
        if (spoilerLevel < 0 || spoilerLevel > 4) {
            throw new JsonParseException("spoiler_level must be between 0 and 4");
        }
        ResourceLocation knowledgeGate = resourceLocation(
                GsonHelper.getAsString(json, "knowledge_gate"),
                "knowledge_gate");
        Links links = Links.parse(GsonHelper.getAsJsonObject(json, "links"));
        String implementationState = requiredChoice(
                json, "implementation_state", IMPLEMENTATION_STATES);
        validateSource(canonStatus, sourceTier);
        return new ContentMetadata(schemaVersion, canonStatus, sourceTier,
                sourceRefs, spoilerLevel, knowledgeGate, links,
                implementationState);
    }

    private static String requiredChoice(JsonObject json, String field,
                                         Set<String> choices) {
        String value = GsonHelper.getAsString(json, field, "");
        if (!choices.contains(value)) {
            throw new JsonParseException(field + " must be one of " + choices);
        }
        return value;
    }

    private static List<String> strings(JsonArray array, String field) {
        List<String> values = new ArrayList<>();
        array.forEach(element -> values.add(element.getAsString()));
        if (values.isEmpty() || values.stream().anyMatch(String::isBlank)
                || new HashSet<>(values).size() != values.size()) {
            throw new JsonParseException(field + " must be non-empty and unique");
        }
        return values;
    }

    private static List<ResourceLocation> resources(JsonObject json,
                                                    String field) {
        JsonArray array = GsonHelper.getAsJsonArray(json, field, new JsonArray());
        List<ResourceLocation> values = new ArrayList<>();
        array.forEach(element -> values.add(resourceLocation(
                element.getAsString(), "links." + field)));
        if (new HashSet<>(values).size() != values.size()) {
            throw new JsonParseException("links." + field
                    + " must not contain duplicates");
        }
        return values;
    }

    private static ResourceLocation resourceLocation(String value,
                                                     String field) {
        ResourceLocation id = ResourceLocation.tryParse(value);
        if (id == null) throw new JsonParseException("invalid " + field + ": " + value);
        return id;
    }

    private static void validateSource(String canonStatus, String sourceTier) {
        if ("original".equals(canonStatus) && !"D".equals(sourceTier)) {
            throw new JsonParseException("original content must use source_tier D");
        }
        if ("canon".equals(canonStatus)
                && ("C".equals(sourceTier) || "D".equals(sourceTier))) {
            throw new JsonParseException("canon content requires source tier S or A");
        }
    }

    public record Links(
            List<ResourceLocation> requires,
            List<ResourceLocation> produces,
            List<ResourceLocation> usedBy,
            List<ResourceLocation> counteredBy) {

        public Links {
            requires = List.copyOf(requires);
            produces = List.copyOf(produces);
            usedBy = List.copyOf(usedBy);
            counteredBy = List.copyOf(counteredBy);
        }

        private static Links parse(JsonObject json) {
            return new Links(
                    resources(json, "requires"),
                    resources(json, "produces"),
                    resources(json, "used_by"),
                    resources(json, "countered_by"));
        }
    }
}
