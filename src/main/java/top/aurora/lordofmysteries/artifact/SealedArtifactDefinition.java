package top.aurora.lordofmysteries.artifact;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import top.aurora.lordofmysteries.content.ContentMetadata;

public record SealedArtifactDefinition(
        ResourceLocation id,
        ResourceLocation item,
        ResourceLocation custodyOrganization,
        String titleKey,
        int dangerLevel,
        int safeUses,
        int loanDays,
        int leakThreshold,
        String effectKey,
        String costKey,
        String knowledgeGate,
        List<String> containment) {

    public SealedArtifactDefinition {
        containment = List.copyOf(containment);
    }

    public static SealedArtifactDefinition parse(
            JsonObject json, ResourceLocation fallbackId) {
        ContentMetadata.parse(json);
        ResourceLocation id = location(
                GsonHelper.getAsString(json, "id", fallbackId.toString()),
                "id");
        ResourceLocation item = location(
                GsonHelper.getAsString(json, "item"), "item");
        ResourceLocation organization = location(
                GsonHelper.getAsString(json, "custody_organization"),
                "custody_organization");
        String titleKey = requiredText(json, "title_key");
        int dangerLevel = GsonHelper.getAsInt(json, "danger_level");
        int safeUses = GsonHelper.getAsInt(json, "safe_uses");
        int loanDays = GsonHelper.getAsInt(json, "loan_days");
        int leakThreshold = GsonHelper.getAsInt(json, "leak_threshold");
        if (dangerLevel < 1 || dangerLevel > 5
                || safeUses < 1 || safeUses > 64
                || loanDays < 1 || loanDays > 30
                || leakThreshold < dangerLevel) {
            throw new JsonParseException(
                    "invalid sealed artifact risk limits");
        }
        String effectKey = requiredText(json, "effect_key");
        String costKey = requiredText(json, "cost_key");
        String knowledgeGate = requiredText(json, "knowledge_gate");
        JsonArray containmentJson =
                GsonHelper.getAsJsonArray(json, "containment");
        List<String> containment = new ArrayList<>();
        containmentJson.forEach(
                element -> containment.add(element.getAsString()));
        if (containment.isEmpty()
                || containment.stream().anyMatch(String::isBlank)
                || new HashSet<>(containment).size()
                        != containment.size()) {
            throw new JsonParseException(
                    "containment must be non-empty and unique");
        }
        return new SealedArtifactDefinition(
                id, item, organization, titleKey, dangerLevel,
                safeUses, loanDays, leakThreshold, effectKey, costKey,
                knowledgeGate, containment);
    }

    private static String requiredText(JsonObject json, String field) {
        String value = GsonHelper.getAsString(json, field);
        if (value.isBlank()) {
            throw new JsonParseException(field + " must not be blank");
        }
        return value;
    }

    private static ResourceLocation location(
            String value, String field) {
        ResourceLocation id = ResourceLocation.tryParse(value);
        if (id == null) {
            throw new JsonParseException(
                    "invalid " + field + ": " + value);
        }
        return id;
    }
}
