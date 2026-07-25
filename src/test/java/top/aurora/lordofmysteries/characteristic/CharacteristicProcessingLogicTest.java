package top.aurora.lordofmysteries.characteristic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import net.minecraft.resources.ResourceLocation;

class CharacteristicProcessingLogicTest {

    private static final ResourceLocation SEER =
            ResourceLocation.fromNamespaceAndPath("lord_of_mysteries", "seer");
    private static final ResourceLocation HUNTER =
            ResourceLocation.fromNamespaceAndPath("lord_of_mysteries", "hunter");

    @Test
    void splitPreservesEveryCharacteristicUnit() {
        CharacteristicBundle source = layered(SEER, "source-a");

        CharacteristicProcessingLogic.SplitResult result =
                CharacteristicProcessingLogic.splitHighestLayer(source);

        assertTrue(result.success());
        assertEquals(CharacteristicProcessingLogic.totalUnits(source),
                CharacteristicProcessingLogic.totalUnits(result.extracted())
                        + CharacteristicProcessingLogic.totalUnits(
                        result.remainder()));
        assertEquals(7, result.extracted().highestSequence());
        assertEquals(8, result.remainder().highestSequence());
        assertNotEquals(result.extracted().sourceHash(),
                result.remainder().sourceHash());
    }

    @Test
    void splitRejectsTheLastRemainingUnit() {
        CharacteristicBundle source = CharacteristicBundle.fromPotion(
                SEER, 9, 0.95f, "single");

        CharacteristicProcessingLogic.SplitResult result =
                CharacteristicProcessingLogic.splitHighestLayer(source);

        assertEquals(CharacteristicProcessingLogic.Status.SINGLE_UNIT,
                result.status());
    }

    @Test
    void mergeCombinesCountsAndWeightedPurity() {
        CharacteristicBundle first = single(
                SEER, 8, 2, 1f, 10f, 0.4f, "source-a");
        CharacteristicBundle second = single(
                SEER, 8, 1, 0.4f, 20f, 0.2f, "source-b");

        CharacteristicProcessingLogic.MergeResult result =
                CharacteristicProcessingLogic.merge(first, second);

        assertTrue(result.success());
        assertEquals(3, result.merged().layers().get(0).count());
        assertEquals(0.8f, result.merged().layers().get(0).purity(),
                0.0001f);
        assertEquals(18.333334f, result.merged().corruption(), 0.0001f);
    }

    @Test
    void mergeRejectsDuplicatedSourceHash() {
        CharacteristicBundle source = layered(SEER, "same-source");

        CharacteristicProcessingLogic.MergeResult result =
                CharacteristicProcessingLogic.merge(source, source);

        assertEquals(CharacteristicProcessingLogic.Status.DUPLICATE_SOURCE,
                result.status());
    }

    @Test
    void mergeRejectsDifferentPathwaysWithoutChangingEitherBundle() {
        CharacteristicBundle first = layered(SEER, "seer-source");
        CharacteristicBundle second = layered(HUNTER, "hunter-source");

        CharacteristicProcessingLogic.MergeResult result =
                CharacteristicProcessingLogic.merge(first, second);

        assertEquals(CharacteristicProcessingLogic.Status.PATHWAY_MISMATCH,
                result.status());
        assertEquals(3, CharacteristicProcessingLogic.totalUnits(first));
        assertEquals(3, CharacteristicProcessingLogic.totalUnits(second));
    }

    @Test
    void mergeIsDeterministicRegardlessOfHandOrder() {
        CharacteristicBundle first = layered(SEER, "source-a");
        CharacteristicBundle second = single(
                SEER, 8, 1, 0.7f, 35f, 0.8f, "source-b");

        CharacteristicBundle forward =
                CharacteristicProcessingLogic.merge(first, second).merged();
        CharacteristicBundle reverse =
                CharacteristicProcessingLogic.merge(second, first).merged();

        assertEquals(forward.save(), reverse.save());
    }

    @Test
    void cleansingTradesPurityForLowerCorruptionAndImprint() {
        CharacteristicBundle source = layered(SEER, "source-a");

        CharacteristicProcessingLogic.CleanseResult result =
                CharacteristicProcessingLogic.cleanse(source);

        assertTrue(result.success());
        assertEquals(35f, result.cleansed().corruption(), 0.0001f);
        assertEquals(0.4f, result.cleansed().imprint().dominance(),
                0.0001f);
        assertEquals(1, result.cleansed().imprint().cleansingCount());
        assertEquals(
                CharacteristicProcessingLogic.averagePurity(source) - 0.03f,
                CharacteristicProcessingLogic.averagePurity(result.cleansed()),
                0.0001f);
        assertNotEquals(source.sourceHash(), result.cleansed().sourceHash());
    }

    @Test
    void cleansingRejectsACompletelyQuietBundle() {
        CharacteristicBundle source = CharacteristicBundle.fromPotion(
                SEER, 9, 1f, "quiet");

        CharacteristicProcessingLogic.CleanseResult result =
                CharacteristicProcessingLogic.cleanse(source);

        assertEquals(CharacteristicProcessingLogic.Status.ALREADY_CLEAN,
                result.status());
    }

    private static CharacteristicBundle layered(
            ResourceLocation pathway, String sourceHash) {
        return new CharacteristicBundle(
                pathway,
                7,
                List.of(
                        new CharacteristicBundle.Layer(9, 1, 0.95f),
                        new CharacteristicBundle.Layer(8, 1, 0.9f),
                        new CharacteristicBundle.Layer(7, 1, 0.85f)),
                new CharacteristicBundle.Imprint(
                        7, "despair", 84000L, 0, 0.6f,
                        List.of("whisper.one", "whisper.two")),
                60f,
                paddedHash(sourceHash));
    }

    private static CharacteristicBundle single(
            ResourceLocation pathway,
            int sequence,
            int count,
            float purity,
            float corruption,
            float dominance,
            String sourceHash) {
        return new CharacteristicBundle(
                pathway,
                sequence,
                List.of(new CharacteristicBundle.Layer(
                        sequence, count, purity)),
                new CharacteristicBundle.Imprint(
                        sequence, "unease", 200L, 0, dominance, List.of()),
                corruption,
                paddedHash(sourceHash));
    }

    private static String paddedHash(String value) {
        return (value + "-".repeat(64)).substring(0, 64);
    }
}
