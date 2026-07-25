package top.aurora.lordofmysteries.characteristic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import net.minecraft.resources.ResourceLocation;

import top.aurora.lordofmysteries.player.PlayerMysteryData;

class CharacteristicLoadLogicTest {

    private static final ResourceLocation SEER =
            ResourceLocation.fromNamespaceAndPath(
                    "lord_of_mysteries", "seer");
    private static final ResourceLocation HUNTER =
            ResourceLocation.fromNamespaceAndPath(
                    "lord_of_mysteries", "hunter");

    @Test
    void normalLayeredAdvancementHasNoExtraLoad() {
        PlayerMysteryData data = playerData(normalProgression());

        assertEquals(0, CharacteristicLoadLogic.extraLoad(data));
    }

    @Test
    void repeatedAndTooPotentLayersCountAsExtraLoad() {
        CharacteristicBundle loaded = bundle(
                SEER,
                6,
                List.of(
                        new CharacteristicBundle.Layer(9, 2, 0.9f),
                        new CharacteristicBundle.Layer(8, 1, 0.9f),
                        new CharacteristicBundle.Layer(7, 1, 0.9f),
                        new CharacteristicBundle.Layer(6, 1, 0.8f)),
                "repeated");

        assertEquals(2, CharacteristicLoadLogic.extraLoad(loaded, 7));
    }

    @Test
    void absorptionAddsExactlyOneConservedExtraLayer() {
        CharacteristicBundle current = normalProgression();
        CharacteristicBundle incoming = CharacteristicBundle.fromPotion(
                SEER, 7, 0.8f, "external-layer");

        CharacteristicLoadLogic.AbsorptionResult result =
                CharacteristicLoadLogic.absorb(current, incoming, 7);

        assertTrue(result.success());
        assertEquals(1, result.extraLoad());
        assertEquals(
                CharacteristicProcessingLogic.totalUnits(current) + 1,
                CharacteristicProcessingLogic.totalUnits(result.merged()));
    }

    @Test
    void absorptionRejectsUnsafeOrAmbiguousPayloads() {
        CharacteristicBundle current = normalProgression();
        CharacteristicBundle wrongPath = CharacteristicBundle.fromPotion(
                HUNTER, 7, 0.9f, "wrong-path");
        CharacteristicBundle multiUnit = bundle(
                SEER,
                7,
                List.of(new CharacteristicBundle.Layer(7, 2, 0.9f)),
                "multi");
        CharacteristicBundle tooPotent = CharacteristicBundle.fromPotion(
                SEER, 6, 0.9f, "too-potent");
        CharacteristicBundle currentWithoutLayer =
                CharacteristicBundle.fromPotion(
                        SEER, 7, 0.9f, "current-without-layer");
        CharacteristicBundle missingLayer = CharacteristicBundle.fromPotion(
                SEER, 8, 0.9f, "missing");

        assertEquals(
                CharacteristicLoadLogic.AbsorptionStatus.PATHWAY_MISMATCH,
                CharacteristicLoadLogic.absorb(
                        current, wrongPath, 7).status());
        assertEquals(
                CharacteristicLoadLogic.AbsorptionStatus.MULTI_UNIT,
                CharacteristicLoadLogic.absorb(
                        current, multiUnit, 7).status());
        assertEquals(
                CharacteristicLoadLogic.AbsorptionStatus.TOO_POTENT,
                CharacteristicLoadLogic.absorb(
                        current, tooPotent, 7).status());
        assertEquals(
                CharacteristicLoadLogic.AbsorptionStatus.MISSING_BASE_LAYER,
                CharacteristicLoadLogic.absorb(
                        currentWithoutLayer, missingLayer, 7).status());
    }

    @Test
    void directDuplicateSourceIsRejected() {
        CharacteristicBundle current = CharacteristicBundle.fromPotion(
                SEER, 9, 0.95f, "same-source");

        CharacteristicLoadLogic.AbsorptionResult result =
                CharacteristicLoadLogic.absorb(current, current, 9);

        assertFalse(result.success());
        assertEquals(
                CharacteristicLoadLogic.AbsorptionStatus.DUPLICATE_SOURCE,
                result.status());
    }

    @Test
    void extraLoadTradesDigestionForSpiritualityAndPressure() {
        assertEquals(1f,
                CharacteristicLoadLogic.digestionMultiplier(0), 0.0001f);
        assertEquals(0.76f,
                CharacteristicLoadLogic.digestionMultiplier(2), 0.0001f);
        assertEquals(0.4f,
                CharacteristicLoadLogic.digestionMultiplier(20), 0.0001f);
        assertEquals(3f,
                CharacteristicLoadLogic.spiritualityReward(2), 0.0001f);
        assertEquals(6f,
                CharacteristicLoadLogic.spiritualityReward(20), 0.0001f);
        assertTrue(
                CharacteristicLoadLogic.actingPressureGain(2, 1f) > 0f);
    }

    @Test
    void preparationAndSupportersChangeExtractionOutcome() {
        float clean = CharacteristicLoadLogic.extractionStability(
                0f, 0f, 0f, 1, 0);
        float strained = CharacteristicLoadLogic.extractionStability(
                60f, 60f, 0.5f, 2, 0);
        float supported = CharacteristicLoadLogic.extractionStability(
                60f, 60f, 0.5f, 2, 3);
        float failed = CharacteristicLoadLogic.extractionStability(
                100f, 100f, 1f, 3, 0);

        assertEquals(CharacteristicLoadLogic.Outcome.STABLE_SUCCESS,
                CharacteristicLoadLogic.resolve(clean));
        assertEquals(CharacteristicLoadLogic.Outcome.STRAINED_SUCCESS,
                CharacteristicLoadLogic.resolve(strained));
        assertEquals(CharacteristicLoadLogic.Outcome.STABLE_SUCCESS,
                CharacteristicLoadLogic.resolve(supported));
        assertEquals(CharacteristicLoadLogic.Outcome.FAILURE,
                CharacteristicLoadLogic.resolve(failed));
    }

    @Test
    void stableExtractionRemovesOnlyOneExtraUnit() {
        CharacteristicBundle loaded = loadedProgression();
        int before = CharacteristicProcessingLogic.totalUnits(loaded);

        CharacteristicLoadLogic.ExtractionResult result =
                CharacteristicLoadLogic.extract(
                        loaded, 7,
                        CharacteristicLoadLogic.Outcome.STABLE_SUCCESS);

        assertEquals(0,
                CharacteristicLoadLogic.extraLoad(result.retained(), 7));
        assertEquals(before,
                CharacteristicProcessingLogic.totalUnits(result.retained())
                        + CharacteristicProcessingLogic.totalUnits(
                        result.extracted()));
        assertEquals(7, result.retained().highestSequence());
        assertEquals(7, result.extracted().highestSequence());
    }

    @Test
    void strainedExtractionLowersPurityAndFailurePreservesLoad() {
        CharacteristicBundle loaded = loadedProgression();
        CharacteristicLoadLogic.ExtractionResult strained =
                CharacteristicLoadLogic.extract(
                        loaded, 7,
                        CharacteristicLoadLogic.Outcome.STRAINED_SUCCESS);
        CharacteristicLoadLogic.ExtractionResult failed =
                CharacteristicLoadLogic.extract(
                        loaded, 7,
                        CharacteristicLoadLogic.Outcome.FAILURE);

        assertTrue(strained.extracted().layers().get(0).purity() < 0.8f);
        assertEquals(1,
                CharacteristicLoadLogic.extraLoad(failed.retained(), 7));
        assertNull(failed.extracted());
        assertTrue(failed.retained().corruption() > loaded.corruption());
        assertTrue(failed.retained().imprint().dominance()
                > loaded.imprint().dominance());
    }

    private static PlayerMysteryData playerData(
            CharacteristicBundle bundle) {
        PlayerMysteryData data = new PlayerMysteryData();
        data.pathway = SEER;
        data.sequence = 7;
        data.characteristicBundles.add(bundle);
        return data;
    }

    private static CharacteristicBundle normalProgression() {
        return CharacteristicBundle.fromPotion(
                SEER, 9, 0.95f, "complete")
                .advance(8, 0.9f, "rough")
                .advance(7, 0.85f, "flawed");
    }

    private static CharacteristicBundle loadedProgression() {
        CharacteristicLoadLogic.AbsorptionResult result =
                CharacteristicLoadLogic.absorb(
                        normalProgression(),
                        CharacteristicBundle.fromPotion(
                                SEER, 7, 0.8f, "external"),
                        7);
        return result.merged();
    }

    private static CharacteristicBundle bundle(
            ResourceLocation pathway,
            int highestSequence,
            List<CharacteristicBundle.Layer> layers,
            String source) {
        return new CharacteristicBundle(
                pathway,
                highestSequence,
                layers,
                new CharacteristicBundle.Imprint(
                        highestSequence,
                        "unease",
                        200L,
                        0,
                        0.2f,
                        List.of()),
                10f,
                (source + "-".repeat(64)).substring(0, 64));
    }
}
