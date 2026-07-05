package top.aurora.lordofmysteries.knowledge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class KnowledgeTextTest {

    @Test
    void convertsKnowledgeIdsToTranslationKeys() {
        assertEquals("knowledge.lord_of_mysteries.bestiary.spirit_wisp",
                KnowledgeText.translationKey(
                        "lord_of_mysteries:knowledge/bestiary/spirit_wisp"));
        assertEquals("knowledge.lord_of_mysteries.safe_rest",
                KnowledgeText.translationKey("knowledge/safe_rest"));
    }

    @Test
    void convertsPathwayIdsToTranslationKeys() {
        assertEquals("pathway.lord_of_mysteries.seer",
                KnowledgeText.pathwayTranslationKey("lord_of_mysteries:seer"));
        assertEquals("pathway.lord_of_mysteries.commoner",
                KnowledgeText.pathwayTranslationKey(""));
    }
}
