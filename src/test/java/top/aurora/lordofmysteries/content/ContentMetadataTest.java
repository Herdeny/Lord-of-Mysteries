package top.aurora.lordofmysteries.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

class ContentMetadataTest {

    @Test
    void parsesV09SchemaAndTypedLinks() {
        ContentMetadata metadata = ContentMetadata.parse(
                JsonParser.parseString("""
                        {
                          "schema_version": 4,
                          "canon_status": "original",
                          "source_tier": "D",
                          "source_refs": ["GDD:v0.9/73"],
                          "spoiler_level": 2,
                          "knowledge_gate": "lord_of_mysteries:knowledge/test",
                          "links": {
                            "requires": ["lord_of_mysteries:item/spirit_herb"],
                            "produces": [],
                            "used_by": [],
                            "countered_by": []
                          },
                          "implementation_state": "verified"
                        }
                        """).getAsJsonObject());

        assertEquals(4, metadata.schemaVersion());
        assertEquals("original", metadata.canonStatus());
        assertEquals(2, metadata.spoilerLevel());
        assertEquals(1, metadata.links().requires().size());
    }

    @Test
    void rejectsSourcePolicyAndDuplicateReferences() {
        assertThrows(JsonParseException.class, () -> ContentMetadata.parse(
                JsonParser.parseString("""
                        {
                          "schema_version": 4,
                          "canon_status": "original",
                          "source_tier": "A",
                          "source_refs": ["GDD:v0.9/73"],
                          "spoiler_level": 0,
                          "knowledge_gate": "lord_of_mysteries:knowledge/test",
                          "links": {},
                          "implementation_state": "planned"
                        }
                        """).getAsJsonObject()));
        assertThrows(JsonParseException.class, () -> ContentMetadata.parse(
                JsonParser.parseString("""
                        {
                          "schema_version": 4,
                          "canon_status": "adaptation",
                          "source_tier": "B-tech",
                          "source_refs": ["GDD:v0.9/73", "GDD:v0.9/73"],
                          "spoiler_level": 0,
                          "knowledge_gate": "lord_of_mysteries:knowledge/test",
                          "links": {},
                          "implementation_state": "planned"
                        }
                        """).getAsJsonObject()));
    }
}
