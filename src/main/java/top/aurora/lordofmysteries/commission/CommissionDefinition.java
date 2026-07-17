package top.aurora.lordofmysteries.commission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public record CommissionDefinition(
        ResourceLocation id,
        String titleKey,
        String summaryKey,
        List<String> boards,
        int minimumLevel,
        int maximumLevel,
        List<String> solutions,
        Reward reward,
        ResourceLocation questChain,
        List<ResourceLocation> prerequisites,
        long cooldownTicks,
        boolean repeatable) {

    public CommissionDefinition {
        boards = List.copyOf(boards);
        solutions = List.copyOf(solutions);
        prerequisites = List.copyOf(prerequisites);
    }

    public static CommissionDefinition parse(JsonObject json, ResourceLocation fallbackId) {
        ResourceLocation id = resourceLocation(
                GsonHelper.getAsString(json, "id", fallbackId.toString()), "id");
        String titleKey = GsonHelper.getAsString(json, "title_key");
        String summaryKey = GsonHelper.getAsString(json, "summary_key");
        List<String> boards = strings(GsonHelper.getAsJsonArray(json, "board"));
        JsonArray range = GsonHelper.getAsJsonArray(json, "level_range");
        if (range.size() != 2) {
            throw new JsonParseException("level_range must contain exactly two values");
        }
        int minimumLevel = range.get(0).getAsInt();
        int maximumLevel = range.get(1).getAsInt();
        if (minimumLevel > maximumLevel) {
            throw new JsonParseException("level_range minimum exceeds maximum");
        }
        List<String> solutions = strings(GsonHelper.getAsJsonArray(json, "solutions"));
        if (solutions.size() < 2) {
            throw new JsonParseException("commissions require at least two solutions");
        }
        JsonObject rewardJson = GsonHelper.getAsJsonObject(json, "reward");
        long pence = Math.max(0L, GsonHelper.getAsLong(rewardJson, "pence", 0L));
        Map<ResourceLocation, Integer> reputation = new HashMap<>();
        JsonObject reputationJson = GsonHelper.getAsJsonObject(
                rewardJson, "reputation", new JsonObject());
        for (String key : reputationJson.keySet()) {
            reputation.put(resourceLocation(key, "reward.reputation"),
                    reputationJson.get(key).getAsInt());
        }
        ResourceLocation questChain = resourceLocation(
                GsonHelper.getAsString(json, "quest_chain"), "quest_chain");
        List<ResourceLocation> prerequisites = new ArrayList<>();
        if (json.has("prerequisites")) {
            for (String value : strings(GsonHelper.getAsJsonArray(
                    json, "prerequisites"))) {
                prerequisites.add(resourceLocation(value, "prerequisites"));
            }
        }
        long cooldownHours = Math.max(0L, GsonHelper.getAsLong(json, "cooldown_hours", 0L));
        boolean repeatable = GsonHelper.getAsBoolean(json, "repeatable", false);
        return new CommissionDefinition(id, titleKey, summaryKey, boards,
                minimumLevel, maximumLevel, solutions,
                new Reward(pence, Map.copyOf(reputation)), questChain,
                prerequisites, cooldownHours * 1000L, repeatable);
    }

    private static List<String> strings(JsonArray array) {
        List<String> values = new ArrayList<>();
        array.forEach(element -> values.add(element.getAsString()));
        return values;
    }

    private static ResourceLocation resourceLocation(String value, String field) {
        ResourceLocation id = ResourceLocation.tryParse(value);
        if (id == null) throw new JsonParseException("invalid " + field + ": " + value);
        return id;
    }

    public record Reward(long pence, Map<ResourceLocation, Integer> reputation) {}
}
