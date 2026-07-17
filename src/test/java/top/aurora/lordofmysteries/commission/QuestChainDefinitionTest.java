package top.aurora.lordofmysteries.commission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonParser;
import com.google.gson.JsonParseException;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import net.minecraft.resources.ResourceLocation;

class QuestChainDefinitionTest {

    @Test
    void parsesObjectivesAndCoopPolicy() {
        QuestChainDefinition chain = QuestChainDefinition.parse(
                withMetadata("""
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
                        """), id("fallback"));

        assertEquals(2, chain.steps().size());
        assertEquals(3, chain.steps().get(1).objective().count());
        assertTrue(chain.sharedProgress());
        assertEquals(4, chain.maximumPartySize());
    }

    @Test
    void rejectsObjectiveTypesOutsideTheFrozenV08Registry() {
        assertThrows(JsonParseException.class, () -> QuestChainDefinition.parse(
                withMetadata("""
                        {
                          "title_key": "quest.test.title",
                          "steps": [{
                            "id": "bad",
                            "guidance_key": "quest.test.bad",
                            "objective": {"type": "hardcoded_magic"}
                          }]
                        }
                        """), id("quest/test")));
    }

    @Test
    void rejectsSharedPartySizeBeyondPersistentLedgerLimit() {
        assertThrows(JsonParseException.class, () -> QuestChainDefinition.parse(
                withMetadata("""
                        {
                          "title_key": "quest.test.title",
                          "steps": [{
                            "id": "visit",
                            "guidance_key": "quest.test.visit",
                            "objective": {"type": "enter_structure", "target": "camp"}
                          }],
                          "coop": {"shared_progress": true, "max_party": 8}
                        }
                        """), id("quest/test")));
    }

    @Test
    void rejectsDuplicateStepIdsAndNonPositiveCounts() {
        assertThrows(JsonParseException.class, () -> QuestChainDefinition.parse(
                withMetadata("""
                        {
                          "title_key": "quest.test.title",
                          "steps": [
                            {"id": "same", "guidance_key": "quest.test.a",
                             "objective": {"type": "encounter", "count": 1}},
                            {"id": "same", "guidance_key": "quest.test.b",
                             "objective": {"type": "encounter", "count": 1}}
                          ]
                        }
                        """), id("quest/test")));
        assertThrows(JsonParseException.class, () -> QuestChainDefinition.parse(
                withMetadata("""
                        {
                          "title_key": "quest.test.title",
                          "steps": [{
                            "id": "bad_count",
                            "guidance_key": "quest.test.bad",
                            "objective": {"type": "encounter", "count": 0}
                          }]
                        }
                        """), id("quest/test")));
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
