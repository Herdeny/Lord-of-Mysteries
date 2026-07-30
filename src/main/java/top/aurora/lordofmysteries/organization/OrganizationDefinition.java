package top.aurora.lordofmysteries.organization;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import top.aurora.lordofmysteries.content.ContentMetadata;

public record OrganizationDefinition(
        ResourceLocation id,
        Kind kind,
        String titleKey,
        String publicFront,
        List<String> covertUnits,
        List<String> doctrines,
        List<String> resources,
        List<String> territories,
        List<ResourceLocation> allies,
        List<ResourceLocation> enemies,
        Map<OrganizationActionType, Double> strategyWeights) {

    public OrganizationDefinition {
        covertUnits = List.copyOf(covertUnits);
        doctrines = List.copyOf(doctrines);
        resources = List.copyOf(resources);
        territories = List.copyOf(territories);
        allies = List.copyOf(allies);
        enemies = List.copyOf(enemies);
        strategyWeights = Map.copyOf(strategyWeights);
    }

    public static OrganizationDefinition parse(
            JsonObject json, ResourceLocation fallbackId) {
        ContentMetadata.parse(json);
        ResourceLocation id = resourceLocation(
                GsonHelper.getAsString(json, "id", fallbackId.toString()), "id");
        Kind kind = Kind.fromId(GsonHelper.getAsString(json, "kind"));
        String titleKey = requiredText(json, "title_key");
        String publicFront = requiredText(json, "public_front");
        List<String> covertUnits = distinctStrings(json, "covert_units");
        List<String> doctrines = distinctStrings(json, "doctrines");
        List<String> resources = distinctStrings(json, "resources");
        List<String> territories = distinctStrings(json, "territories");
        JsonObject relations = GsonHelper.getAsJsonObject(json, "relations");
        List<ResourceLocation> allies = locations(relations, "allies");
        List<ResourceLocation> enemies = locations(relations, "enemies");
        Set<ResourceLocation> overlap = new HashSet<>(allies);
        overlap.retainAll(enemies);
        if (!overlap.isEmpty()) {
            throw new JsonParseException(
                    "organization allies and enemies must not overlap");
        }
        if (allies.contains(id) || enemies.contains(id)) {
            throw new JsonParseException(
                    "organization cannot relate to itself");
        }
        JsonObject weights = GsonHelper.getAsJsonObject(
                json, "strategy_weights");
        Map<OrganizationActionType, Double> strategy =
                new EnumMap<>(OrganizationActionType.class);
        for (String key : weights.keySet()) {
            OrganizationActionType type;
            try {
                type = OrganizationActionType.fromId(key);
            } catch (IllegalArgumentException exception) {
                throw new JsonParseException(
                        "unknown organization strategy " + key,
                        exception);
            }
            double weight = weights.get(key).getAsDouble();
            if (!Double.isFinite(weight) || weight < 0d || weight > 10d) {
                throw new JsonParseException(
                        "organization strategy weight must be within 0-10");
            }
            if (weight > 0d) strategy.put(type, weight);
        }
        if (strategy.size() < 2) {
            throw new JsonParseException(
                    "organization requires at least two action strategies");
        }
        return new OrganizationDefinition(
                id, kind, titleKey, publicFront, covertUnits, doctrines,
                resources, territories, allies, enemies, strategy);
    }

    private static String requiredText(JsonObject json, String field) {
        String value = GsonHelper.getAsString(json, field);
        if (value.isBlank()) {
            throw new JsonParseException(field + " must not be blank");
        }
        return value;
    }

    private static List<String> distinctStrings(
            JsonObject json, String field) {
        JsonArray array = GsonHelper.getAsJsonArray(json, field);
        List<String> values = new ArrayList<>();
        array.forEach(element -> values.add(element.getAsString()));
        if (values.isEmpty() || values.stream().anyMatch(String::isBlank)
                || new HashSet<>(values).size() != values.size()) {
            throw new JsonParseException(
                    field + " must be non-empty and unique");
        }
        return values;
    }

    private static List<ResourceLocation> locations(
            JsonObject json, String field) {
        JsonArray array = GsonHelper.getAsJsonArray(
                json, field, new JsonArray());
        List<ResourceLocation> values = new ArrayList<>();
        for (var element : array) {
            values.add(resourceLocation(element.getAsString(), field));
        }
        if (new HashSet<>(values).size() != values.size()) {
            throw new JsonParseException(field + " must be unique");
        }
        return values;
    }

    private static ResourceLocation resourceLocation(
            String value, String field) {
        ResourceLocation id = ResourceLocation.tryParse(value);
        if (id == null) {
            throw new JsonParseException(
                    "invalid " + field + ": " + value);
        }
        return id;
    }

    public enum Kind {
        CHURCH("church"),
        SECRET("secret");

        private final String id;

        Kind(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }

        public static Kind fromId(String id) {
            for (Kind value : values()) {
                if (value.id.equals(id)) return value;
            }
            throw new JsonParseException(
                    "unknown organization kind " + id);
        }
    }
}
