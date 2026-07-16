package top.aurora.lordofmysteries.commission;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public record QuestChainDefinition(
        ResourceLocation id,
        String titleKey,
        List<Step> steps,
        String failPolicy,
        boolean sharedProgress,
        int maximumPartySize) {

    public static final Set<String> SUPPORTED_OBJECTIVE_TYPES = Set.of(
            "talk_npc", "enter_structure", "pickup", "encounter",
            "reach_sequence", "rescue", "survive_waves", "deliver",
            "brew_quality", "divine_success", "ritual_outcome", "escort",
            "collect_set", "reputation_reach", "custom_callback");

    public QuestChainDefinition {
        steps = List.copyOf(steps);
    }

    public static QuestChainDefinition parse(JsonObject json, ResourceLocation fallbackId) {
        ResourceLocation id = resourceLocation(
                GsonHelper.getAsString(json, "id", fallbackId.toString()), "id");
        String titleKey = GsonHelper.getAsString(json, "title_key");
        JsonArray stepsJson = GsonHelper.getAsJsonArray(json, "steps");
        if (stepsJson.isEmpty()) throw new JsonParseException("quest chain has no steps");
        List<Step> steps = new ArrayList<>();
        for (int index = 0; index < stepsJson.size(); index++) {
            JsonObject stepJson = GsonHelper.convertToJsonObject(
                    stepsJson.get(index), "steps[" + index + "]");
            JsonObject objectiveJson = GsonHelper.getAsJsonObject(stepJson, "objective");
            String type = GsonHelper.getAsString(objectiveJson, "type");
            if (!SUPPORTED_OBJECTIVE_TYPES.contains(type)) {
                throw new JsonParseException("unsupported quest objective type: " + type);
            }
            String target = GsonHelper.getAsString(objectiveJson, "target", "");
            int count = Math.max(1, GsonHelper.getAsInt(objectiveJson, "count", 1));
            steps.add(new Step(
                    GsonHelper.getAsString(stepJson, "id"),
                    GsonHelper.getAsString(stepJson, "guidance_key"),
                    new Objective(type, target, count)));
        }
        JsonObject coop = GsonHelper.getAsJsonObject(json, "coop", new JsonObject());
        return new QuestChainDefinition(id, titleKey, steps,
                GsonHelper.getAsString(json, "fail_policy", "step_retry"),
                GsonHelper.getAsBoolean(coop, "shared_progress", false),
                Math.max(1, GsonHelper.getAsInt(coop, "max_party", 1)));
    }

    private static ResourceLocation resourceLocation(String value, String field) {
        ResourceLocation id = ResourceLocation.tryParse(value);
        if (id == null) throw new JsonParseException("invalid " + field + ": " + value);
        return id;
    }

    public record Step(String id, String guidanceKey, Objective objective) {}

    public record Objective(String type, String target, int count) {}
}
