package top.aurora.lordofmysteries.commission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import net.minecraft.resources.ResourceLocation;

class CommissionDefinitionTest {

    @Test
    void parsesDataDrivenCommissionAndReward() {
        CommissionDefinition definition = CommissionDefinition.parse(
                JsonParser.parseString("""
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
                          "cooldown_hours": 2
                        }
                        """).getAsJsonObject(), id("test"));

        assertEquals(id("commission/test"), definition.id());
        assertEquals(36L, definition.reward().pence());
        assertEquals(2000L, definition.cooldownTicks());
        assertEquals(2, definition.solutions().size());
        assertFalse(definition.repeatable());
    }

    @Test
    void rejectsSingleSolutionCommission() {
        assertThrows(JsonParseException.class, () -> CommissionDefinition.parse(
                JsonParser.parseString("""
                        {
                          "title_key": "title",
                          "summary_key": "summary",
                          "board": ["board"],
                          "level_range": [0, 1],
                          "solutions": ["combat"],
                          "reward": {},
                          "quest_chain": "lord_of_mysteries:quest/test"
                        }
                        """).getAsJsonObject(), id("commission/test")));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("lord_of_mysteries", path);
    }
}
