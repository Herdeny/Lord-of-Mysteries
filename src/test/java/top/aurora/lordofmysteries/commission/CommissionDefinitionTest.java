package top.aurora.lordofmysteries.commission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import net.minecraft.resources.ResourceLocation;

class CommissionDefinitionTest {

    @Test
    void parsesDataDrivenCommissionAndReward() {
        CommissionDefinition definition = CommissionDefinition.parse(
                withMetadata("""
                        {
                          "id": "lord_of_mysteries:commission/test",
                          "title_key": "commission.test.title",
                          "summary_key": "commission.test.summary",
                          "board": ["mist_city_outpost"],
                          "level_range": [0, 2],
                          "solutions": ["tracking", "divination"],
                          "reward": {
                            "pence": 36,
                            "reputation": {"lord_of_mysteries:organization/test": 4}
                          },
                          "quest_chain": "lord_of_mysteries:quest/test",
                          "prerequisites": ["lord_of_mysteries:commission/intro"],
                          "cooldown_hours": 2
                        }
                        """), id("test"));

        assertEquals(id("commission/test"), definition.id());
        assertEquals(36L, definition.reward().pence());
        assertEquals(2000L, definition.cooldownTicks());
        assertEquals(2, definition.solutions().size());
        assertEquals(List.of(id("commission/intro")), definition.prerequisites());
        assertFalse(definition.repeatable());
    }

    @Test
    void rejectsSingleSolutionCommission() {
        assertThrows(JsonParseException.class, () -> CommissionDefinition.parse(
                withMetadata("""
                        {
                          "title_key": "title",
                          "summary_key": "summary",
                          "board": ["board"],
                          "level_range": [0, 1],
                          "solutions": ["combat"],
                          "reward": {},
                          "quest_chain": "lord_of_mysteries:quest/test"
                        }
                        """), id("commission/test")));
    }

    @Test
    void rejectsDuplicateSolutionsAndCooldownOverflow() {
        assertThrows(JsonParseException.class, () -> CommissionDefinition.parse(
                withMetadata("""
                        {
                          "title_key": "title",
                          "summary_key": "summary",
                          "board": ["board"],
                          "level_range": [0, 1],
                          "solutions": ["combat", "combat"],
                          "reward": {},
                          "quest_chain": "lord_of_mysteries:quest/test"
                        }
                        """), id("commission/test")));
        assertThrows(JsonParseException.class, () -> CommissionDefinition.parse(
                withMetadata("""
                        {
                          "title_key": "title",
                          "summary_key": "summary",
                          "board": ["board"],
                          "level_range": [0, 1],
                          "solutions": ["combat", "divination"],
                          "reward": {},
                          "quest_chain": "lord_of_mysteries:quest/test",
                          "cooldown_hours": 9223372036854776
                        }
                        """), id("commission/test")));
    }

    private static JsonObject withMetadata(String json) {
        JsonObject object = JsonParser.parseString(json).getAsJsonObject();
        object.addProperty("schema_version", 4);
        object.addProperty("canon_status", "original");
        object.addProperty("source_tier", "D");
        object.add("source_refs", JsonParser.parseString("[\"TEST:v0.9\"]"));
        object.addProperty("spoiler_level", 0);
        object.addProperty("knowledge_gate", "lord_of_mysteries:knowledge/test");
        object.add("links", JsonParser.parseString("{}"));
        object.addProperty("implementation_state", "verified");
        return object;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("lord_of_mysteries", path);
    }
}
