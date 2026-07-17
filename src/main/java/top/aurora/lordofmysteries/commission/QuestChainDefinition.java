package top.aurora.lordofmysteries.commission;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import top.aurora.lordofmysteries.content.ContentMetadata;

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
        ContentMetadata.parse(json);
        ResourceLocation id = resourceLocation(
                GsonHelper.getAsString(json, "id", fallbackId.toString()), "id");
        String titleKey = GsonHelper.getAsString(json, "title_key");
        requireText(titleKey, "title_key");
        JsonArray stepsJson = GsonHelper.getAsJsonArray(json, "steps");
        if (stepsJson.isEmpty()) throw new JsonParseException("quest chain has no steps");
        List<Step> steps = new ArrayList<>();
        Set<String> stepIds = new HashSet<>();
        for (int index = 0; index < stepsJson.size(); index++) {
            JsonObject stepJson = GsonHelper.convertToJsonObject(
                    stepsJson.get(index), "steps[" + index + "]");
            JsonObject objectiveJson = GsonHelper.getAsJsonObject(stepJson, "objective");
            String type = GsonHelper.getAsString(objectiveJson, "type");
            if (!SUPPORTED_OBJECTIVE_TYPES.contains(type)) {
                throw new JsonParseException("unsupported quest objective type: " + type);
            }
            String target = GsonHelper.getAsString(objectiveJson, "target", "");
            int count = GsonHelper.getAsInt(objectiveJson, "count", 1);
            if (count < 1) throw new JsonParseException("objective count must be positive");
            String stepId = GsonHelper.getAsString(stepJson, "id");
            String guidanceKey = GsonHelper.getAsString(stepJson, "guidance_key");
            requireText(stepId, "steps[" + index + "].id");
            requireText(guidanceKey, "steps[" + index + "].guidance_key");
            if (!stepIds.add(stepId)) {
                throw new JsonParseException("duplicate quest step id: " + stepId);
            }
            steps.add(new Step(stepId, guidanceKey,
                    new Objective(type, target, count)));
        }
        JsonObject coop = GsonHelper.getAsJsonObject(json, "coop", new JsonObject());
        String failPolicy = GsonHelper.getAsString(json, "fail_policy", "step_retry");
        if (!Set.of("step_retry", "chain_retry", "abandon").contains(failPolicy)) {
            throw new JsonParseException("unsupported fail policy: " + failPolicy);
        }
        boolean sharedProgress = GsonHelper.getAsBoolean(
                coop, "shared_progress", false);
        int maximumPartySize = GsonHelper.getAsInt(coop, "max_party", 1);
        if (sharedProgress && (maximumPartySize < 2
                || maximumPartySize > QuestPartyPolicy.MAXIMUM_PERSISTENT_PARTY_SIZE)) {
            throw new JsonParseException("shared max_party must be between 2 and "
                    + QuestPartyPolicy.MAXIMUM_PERSISTENT_PARTY_SIZE);
        }
        if (!sharedProgress && maximumPartySize != 1) {
            throw new JsonParseException("non-shared quest max_party must be 1");
        }
        return new QuestChainDefinition(id, titleKey, steps, failPolicy,
                sharedProgress, maximumPartySize);
    }

    private static ResourceLocation resourceLocation(String value, String field) {
        ResourceLocation id = ResourceLocation.tryParse(value);
        if (id == null) throw new JsonParseException("invalid " + field + ": " + value);
        return id;
    }

    private static void requireText(String value, String field) {
        if (value.isBlank()) throw new JsonParseException(field + " must not be blank");
    }

    public record Step(String id, String guidanceKey, Objective objective) {}

    public record Objective(String type, String target, int count) {}
}
