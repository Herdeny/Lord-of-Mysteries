package top.aurora.lordofmysteries.commission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import net.minecraft.resources.ResourceLocation;

class QuestPlaythroughContractTest {

    private static final Path QUEST_ROOT = Path.of(
            "src/main/resources/data/lord_of_mysteries/quests");

    @Test
    void everyPublishedQuestCanAdvanceFromFirstToLastStep() throws IOException {
        List<Path> questFiles;
        try (Stream<Path> paths = Files.list(QUEST_ROOT)) {
            questFiles = paths.filter(candidate ->
                    candidate.toString().endsWith(".json")).toList();
        }
        for (Path path : questFiles) {
            QuestChainDefinition chain = load(path);
            int stepIndex = 0;
            int progress = 0;
            for (QuestChainDefinition.Step step : chain.steps()) {
                QuestProgression.Result result = null;
                for (int count = 0; count < step.objective().count(); count++) {
                    String target = step.objective().target().isBlank()
                            ? "runtime_target" : step.objective().target();
                    result = QuestProgression.record(chain, stepIndex, progress,
                            step.objective().type(), target, 1);
                    assertTrue(result.matched(), path + " failed at " + step.id());
                    stepIndex = result.stepIndex();
                    progress = result.progress();
                }
                assertTrue(result != null && result.stepCompleted(),
                        path + " did not complete " + step.id());
            }
            assertEquals(chain.steps().size(), stepIndex,
                    path + " did not reach settlement state");
        }
    }

    @Test
    void missingSquadKeepsLegacySevenStepPrefix() throws IOException {
        QuestChainDefinition chain = load(QUEST_ROOT.resolve(
                "chain1_missing_squad_prologue.json"));
        assertEquals(List.of(
                        "s1_accept", "s2_press_desk", "s3_last_camp", "s4_evidence",
                        "s5_advance", "s6_clear_threats", "s7_settle"),
                chain.steps().subList(0, 7).stream()
                        .map(QuestChainDefinition.Step::id).toList());
        assertEquals(13, chain.steps().size());
        assertTrue(chain.sharedProgress());
        assertEquals(4, chain.maximumPartySize());
    }

    private static QuestChainDefinition load(Path path) throws IOException {
        return QuestChainDefinition.parse(
                JsonParser.parseString(Files.readString(path)).getAsJsonObject(),
                ResourceLocation.fromNamespaceAndPath(
                        "lord_of_mysteries", "quest/" + path.getFileName()));
    }
}
