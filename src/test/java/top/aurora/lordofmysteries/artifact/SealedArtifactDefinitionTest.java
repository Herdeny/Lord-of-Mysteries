package top.aurora.lordofmysteries.artifact;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import net.minecraft.resources.ResourceLocation;

class SealedArtifactDefinitionTest {

    @Test
    void parsesCustodyRiskAndContainmentContract() {
        SealedArtifactDefinition definition =
                SealedArtifactDefinition.parse(
                        validDefinition(), id("fallback"));

        assertEquals(id("artifact_test"), definition.id());
        assertEquals(id("artifact_test"), definition.item());
        assertEquals(id("organization/test_watch"),
                definition.custodyOrganization());
        assertEquals(3, definition.dangerLevel());
        assertEquals(5, definition.safeUses());
        assertEquals(2, definition.loanDays());
        assertEquals(15, definition.leakThreshold());
        assertEquals(2, definition.containment().size());
    }

    @Test
    void rejectsUnsafeLimitsAndDuplicateContainment() {
        JsonObject weakThreshold = validDefinition();
        weakThreshold.addProperty("leak_threshold", 2);
        assertThrows(JsonParseException.class, () ->
                SealedArtifactDefinition.parse(
                        weakThreshold, id("fallback")));

        JsonObject duplicateContainment = validDefinition();
        duplicateContainment.add(
                "containment",
                JsonParser.parseString("[\"seal\", \"seal\"]"));
        assertThrows(JsonParseException.class, () ->
                SealedArtifactDefinition.parse(
                        duplicateContainment, id("fallback")));
    }

    static JsonObject validDefinition() {
        JsonObject object = JsonParser.parseString("""
                {
                  "id": "lord_of_mysteries:artifact_test",
                  "item": "lord_of_mysteries:artifact_test",
                  "custody_organization": "lord_of_mysteries:organization/test_watch",
                  "title_key": "item.test",
                  "danger_level": 3,
                  "safe_uses": 5,
                  "loan_days": 2,
                  "leak_threshold": 15,
                  "effect_key": "artifact.test.effect",
                  "cost_key": "artifact.test.cost",
                  "knowledge_gate": "lord_of_mysteries:knowledge/test",
                  "containment": ["seal", "audit"]
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
