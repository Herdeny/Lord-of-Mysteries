package top.aurora.lordofmysteries.artifact;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

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
        assertEquals(ArtifactIncidentProfile.ECHO,
                definition.incidentProfile());
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

        JsonObject unknownIncident = validDefinition();
        unknownIncident.addProperty("incident_profile", "unknown");
        assertThrows(JsonParseException.class, () ->
                SealedArtifactDefinition.parse(
                        unknownIncident, id("fallback")));
    }

    @Test
    void m4CatalogContainsTwentyFourUniqueManagedArtifacts() {
        var kinds = Arrays.asList(ManagedArtifactKind.values());
        assertEquals(24, kinds.size());
        assertEquals(24, new HashSet<>(kinds.stream()
                .map(ManagedArtifactKind::id)
                .toList()).size());
    }

    @Test
    void everyIncidentProfileHasConcreteContainmentMaterials() {
        assertEquals(6, ArtifactIncidentProfile.values().length);
        for (ArtifactIncidentProfile profile
                : ArtifactIncidentProfile.values()) {
            assertEquals(2, profile.requirements().size());
            assertEquals(2, new HashSet<>(profile.requirements().stream()
                    .map(ArtifactIncidentProfile.Requirement::item)
                    .toList()).size());
        }
    }

    @Test
    void extendedArtifactTimingAndPlayerSafetyPoliciesRejectUnsafeUse() {
        assertTrue(ExtendedSealedArtifactEffects.rewindSnapshotDue(0L, 1L));
        assertFalse(ExtendedSealedArtifactEffects.rewindSnapshotDue(100L, 199L));
        assertTrue(ExtendedSealedArtifactEffects.rewindSnapshotDue(100L, 200L));
        assertTrue(ExtendedSealedArtifactEffects.rewindSnapshotDue(200L, 100L));

        Vec3 target = new Vec3(8d, 64d, 8d);
        assertTrue(ExtendedSealedArtifactEffects.stormTargetClear(
                target, java.util.List.of(new Vec3(13d, 64d, 8d))));
        assertFalse(ExtendedSealedArtifactEffects.stormTargetClear(
                target, java.util.List.of(new Vec3(10d, 64d, 8d))));
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
                  "incident_profile": "echo",
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
