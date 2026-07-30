package top.aurora.lordofmysteries.organization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import net.minecraft.resources.ResourceLocation;

class OrganizationDefinitionTest {

    @Test
    void parsesAllSevenOrganizationDataPlanes() {
        OrganizationDefinition definition = OrganizationDefinition.parse(
                validDefinition(), id("fallback"));

        assertEquals(id("organization/test_watch"), definition.id());
        assertEquals(OrganizationDefinition.Kind.CHURCH, definition.kind());
        assertEquals(2, definition.covertUnits().size());
        assertEquals(2, definition.doctrines().size());
        assertEquals(2, definition.resources().size());
        assertEquals(2, definition.territories().size());
        assertEquals(1, definition.allies().size());
        assertEquals(1, definition.enemies().size());
        assertEquals(3, definition.strategyWeights().size());
    }

    @Test
    void rejectsUnknownStrategiesAndConflictingRelations() {
        JsonObject unknownStrategy = validDefinition();
        unknownStrategy.getAsJsonObject("strategy_weights")
                .addProperty("unverified_action", 1);
        assertThrows(JsonParseException.class, () ->
                OrganizationDefinition.parse(
                        unknownStrategy, id("fallback")));

        JsonObject conflict = validDefinition();
        conflict.getAsJsonObject("relations")
                .add("enemies", JsonParser.parseString(
                        "[\"lord_of_mysteries:organization/ally\"]"));
        assertThrows(JsonParseException.class, () ->
                OrganizationDefinition.parse(conflict, id("fallback")));
    }

    @Test
    void rejectsIncompleteDataPlanesAndWeakStrategies() {
        JsonObject noTerritory = validDefinition();
        noTerritory.add("territories", JsonParser.parseString("[]"));
        assertThrows(JsonParseException.class, () ->
                OrganizationDefinition.parse(
                        noTerritory, id("fallback")));

        JsonObject oneStrategy = validDefinition();
        oneStrategy.add("strategy_weights", JsonParser.parseString(
                "{\"night_patrol\": 1}"));
        assertThrows(JsonParseException.class, () ->
                OrganizationDefinition.parse(
                        oneStrategy, id("fallback")));
    }

    static JsonObject validDefinition() {
        JsonObject object = JsonParser.parseString("""
                {
                  "id": "lord_of_mysteries:organization/test_watch",
                  "kind": "church",
                  "title_key": "organization.test",
                  "public_front": "public clinic",
                  "covert_units": ["watch", "archive"],
                  "doctrines": ["contain", "verify"],
                  "resources": ["vault", "records"],
                  "territories": ["north", "docks"],
                  "relations": {
                    "allies": ["lord_of_mysteries:organization/ally"],
                    "enemies": ["lord_of_mysteries:organization/enemy"]
                  },
                  "strategy_weights": {
                    "night_patrol": 4,
                    "artifact_transfer": 2,
                    "disaster_relief": 1
                  }
                }
                """).getAsJsonObject();
        object.addProperty("schema_version", 4);
        object.addProperty("canon_status", "original");
        object.addProperty("source_tier", "D");
        object.add("source_refs",
                JsonParser.parseString("[\"TEST:v0.9\"]"));
        object.addProperty("spoiler_level", 0);
        object.addProperty(
                "knowledge_gate",
                "lord_of_mysteries:knowledge/test");
        object.add("links", JsonParser.parseString("{}"));
        object.addProperty("implementation_state", "verified");
        return object;
    }

    static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(
                "lord_of_mysteries", path);
    }
}
