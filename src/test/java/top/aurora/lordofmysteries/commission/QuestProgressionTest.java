package top.aurora.lordofmysteries.commission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

import net.minecraft.resources.ResourceLocation;

class QuestProgressionTest {

    private static final QuestChainDefinition CHAIN = new QuestChainDefinition(
            ResourceLocation.fromNamespaceAndPath("lord_of_mysteries", "quest/test"),
            "quest.test.title",
            List.of(
                    new QuestChainDefinition.Step("camp", "quest.test.camp",
                            new QuestChainDefinition.Objective("enter_structure", "camp", 1)),
                    new QuestChainDefinition.Step("hunt", "quest.test.hunt",
                            new QuestChainDefinition.Objective("encounter", "", 3))),
            "step_retry", true, 4);

    @Test
    void ignoresWrongObjectiveWithoutChangingProgress() {
        QuestProgression.Result result = QuestProgression.record(
                CHAIN, 0, 0, "enter_structure", "outpost", 1);
        assertFalse(result.matched());
        assertEquals(0, result.stepIndex());
    }

    @Test
    void advancesWhenStepRequirementIsMet() {
        QuestProgression.Result result = QuestProgression.record(
                CHAIN, 0, 0, "enter_structure", "camp", 1);
        assertTrue(result.stepCompleted());
        assertEquals(1, result.stepIndex());
        assertFalse(result.chainCompleted());
    }

    @Test
    void accumulatesCountAndCompletesChain() {
        QuestProgression.Result partial = QuestProgression.record(
                CHAIN, 1, 1, "encounter", "any_entity", 1);
        assertEquals(2, partial.progress());
        assertFalse(partial.stepCompleted());

        QuestProgression.Result complete = QuestProgression.record(
                CHAIN, 1, partial.progress(), "encounter", "another_entity", 1);
        assertTrue(complete.chainCompleted());
        assertEquals(2, complete.stepIndex());
    }
}
