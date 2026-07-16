package top.aurora.lordofmysteries.commission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonParser;
import com.google.gson.JsonParseException;
import org.junit.jupiter.api.Test;

import net.minecraft.resources.ResourceLocation;

class QuestChainDefinitionTest {

    @Test
    void parsesObjectivesAndCoopPolicy() {
        QuestChainDefinition chain = QuestChainDefinition.parse(
                JsonParser.parseString("""
                        {
                          "id": "lord_of_mysteries:quest/test",
                          "title_key": "quest.test.title",
                          "steps": [
                            {
                              "id": "visit",
                              "guidance_key": "quest.test.visit",
                              "objective": {"type": "enter_structure", "target": "camp"}
                            },
                            {
                              "id": "hunt",
                              "guidance_key": "quest.test.hunt",
                              "objective": {"type": "encounter", "count": 3}
                            }
                          ],
                          "coop": {"shared_progress": true, "max_party": 4}
                        }
                        """).getAsJsonObject(), id("fallback"));

        assertEquals(2, chain.steps().size());
        assertEquals(3, chain.steps().get(1).objective().count());
        assertTrue(chain.sharedProgress());
        assertEquals(4, chain.maximumPartySize());
    }

    @Test
    void rejectsObjectiveTypesOutsideTheFrozenV08Registry() {
        assertThrows(JsonParseException.class, () -> QuestChainDefinition.parse(
                JsonParser.parseString("""
                        {
                          "title_key": "quest.test.title",
                          "steps": [{
                            "id": "bad",
                            "guidance_key": "quest.test.bad",
                            "objective": {"type": "hardcoded_magic"}
                          }]
                        }
                        """).getAsJsonObject(), id("quest/test")));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("lord_of_mysteries", path);
    }
}
