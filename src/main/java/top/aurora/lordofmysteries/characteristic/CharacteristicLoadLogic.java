package top.aurora.lordofmysteries.characteristic;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;

import top.aurora.lordofmysteries.player.PlayerMysteryData;

public final class CharacteristicLoadLogic {

    public static final float DIGESTION_PENALTY_PER_LAYER = 0.12f;
    public static final float MINIMUM_DIGESTION_MULTIPLIER = 0.4f;
    public static final float SPIRITUALITY_REWARD_PER_LAYER = 1.5f;
    public static final float STRAINED_PURITY_MULTIPLIER = 0.65f;

    private CharacteristicLoadLogic() {}

    public static int extraLoad(PlayerMysteryData data) {
        if (data.pathway == null || data.sequence < 0) return 0;
        return data.characteristicBundles.stream()
                .filter(bundle -> bundle.pathway().equals(data.pathway))
                .findFirst()
                .map(bundle -> extraLoad(bundle, data.sequence))
                .orElse(0);
    }

    public static int extraLoad(CharacteristicBundle bundle,
                                int currentSequence) {
        if (currentSequence < 0 || currentSequence > 9) return 0;
        int load = 0;
        for (CharacteristicBundle.Layer layer : bundle.layers()) {
            int retainedAllowance = layer.sequence() >= currentSequence ? 1 : 0;
            load += Math.max(0, layer.count() - retainedAllowance);
        }
        return load;
    }

    public static float digestionMultiplier(int extraLoad) {
        return Math.max(MINIMUM_DIGESTION_MULTIPLIER,
                1f - Math.max(0, extraLoad) * DIGESTION_PENALTY_PER_LAYER);
    }

    public static float spiritualityReward(int extraLoad) {
        return Math.min(6f,
                Math.max(0, extraLoad) * SPIRITUALITY_REWARD_PER_LAYER);
    }

    public static float actingPressureGain(int extraLoad, float novelty) {
        return Math.min(2f, Math.max(0, extraLoad) * 0.25f)
                * Math.max(0.25f, Math.min(1f, novelty));
    }

    public static float extractionStability(
            float pollution,
            float pressure,
            float imprintDominance,
            int extraLoad,
            int supporters) {
        float stability = 1f
                - clamp(pollution, 0f, 100f) * 0.0028f
                - clamp(pressure, 0f, 100f) * 0.0028f
                - clamp(imprintDominance, 0f, 1f) * 0.25f
                - Math.max(0, extraLoad - 1) * 0.08f
                + Math.min(3, Math.max(0, supporters)) * 0.1f;
        return clamp(stability, 0f, 1f);
    }

    public static Outcome resolve(float stability) {
        if (stability >= 0.75f) return Outcome.STABLE_SUCCESS;
        if (stability >= 0.45f) return Outcome.STRAINED_SUCCESS;
        return Outcome.FAILURE;
    }

    public static AbsorptionResult absorb(
            CharacteristicBundle current,
            CharacteristicBundle incoming,
            int currentSequence) {
        if (!current.pathway().equals(incoming.pathway())) {
            return AbsorptionResult.failure(AbsorptionStatus.PATHWAY_MISMATCH);
        }
        if (CharacteristicProcessingLogic.totalUnits(incoming) != 1) {
            return AbsorptionResult.failure(AbsorptionStatus.MULTI_UNIT);
        }
        CharacteristicBundle.Layer incomingLayer = incoming.layers().get(0);
        if (incomingLayer.sequence() < currentSequence) {
            return AbsorptionResult.failure(AbsorptionStatus.TOO_POTENT);
        }
        if (current.layers().stream().noneMatch(layer ->
                layer.sequence() == incomingLayer.sequence())) {
            return AbsorptionResult.failure(
                    AbsorptionStatus.MISSING_BASE_LAYER);
        }
        CharacteristicProcessingLogic.MergeResult merged =
                CharacteristicProcessingLogic.merge(current, incoming);
        if (!merged.success()) {
            return AbsorptionResult.failure(switch (merged.status()) {
                case DUPLICATE_SOURCE ->
                        AbsorptionStatus.DUPLICATE_SOURCE;
                case LAYER_CAPACITY ->
                        AbsorptionStatus.LAYER_CAPACITY;
                default -> AbsorptionStatus.INVALID;
            });
        }
        int before = extraLoad(current, currentSequence);
        int after = extraLoad(merged.merged(), currentSequence);
        if (after != before + 1) {
            return AbsorptionResult.failure(AbsorptionStatus.INVALID);
        }
        return new AbsorptionResult(
                AbsorptionStatus.SUCCESS, merged.merged(), after);
    }

    public static ExtractionResult extract(
            CharacteristicBundle source,
            int currentSequence,
            Outcome outcome) {
        CharacteristicBundle.Layer selected = source.layers().stream()
                .filter(layer -> layer.count()
                        > (layer.sequence() >= currentSequence ? 1 : 0))
                .min(Comparator.comparingInt(
                        CharacteristicBundle.Layer::sequence))
                .orElse(null);
        if (selected == null) {
            return new ExtractionResult(
                    Outcome.NO_EXTRA_LOAD, source, null);
        }
        if (outcome == Outcome.FAILURE) {
            CharacteristicBundle.Imprint imprint = source.imprint();
            CharacteristicBundle agitated = new CharacteristicBundle(
                    source.pathway(),
                    source.highestSequence(),
                    source.layers(),
                    new CharacteristicBundle.Imprint(
                            imprint.formerOwnerSequence(),
                            imprint.dominantEmotion(),
                            imprint.ageTicks(),
                            imprint.cleansingCount(),
                            Math.min(1f, imprint.dominance() + 0.1f),
                            imprint.whisperPool()),
                    Math.min(100f, source.corruption() + 12f),
                    derive("load-extraction-failure", source.sourceHash()));
            return new ExtractionResult(outcome, agitated, null);
        }

        List<CharacteristicBundle.Layer> retainedLayers =
                new ArrayList<>(source.layers());
        retainedLayers.remove(selected);
        if (selected.count() > 1) {
            retainedLayers.add(new CharacteristicBundle.Layer(
                    selected.sequence(), selected.count() - 1,
                    selected.purity()));
        }
        retainedLayers.sort(Comparator.comparingInt(
                CharacteristicBundle.Layer::sequence).reversed());
        if (retainedLayers.isEmpty()) {
            return new ExtractionResult(
                    Outcome.NO_EXTRA_LOAD, source, null);
        }

        float purityMultiplier = outcome == Outcome.STRAINED_SUCCESS
                ? STRAINED_PURITY_MULTIPLIER : 1f;
        float extractedCorruption = source.corruption()
                + (outcome == Outcome.STRAINED_SUCCESS ? 15f : 0f);
        CharacteristicBundle.Imprint imprint = source.imprint();
        CharacteristicBundle.Imprint extractedImprint =
                outcome == Outcome.STRAINED_SUCCESS
                        ? new CharacteristicBundle.Imprint(
                        imprint.formerOwnerSequence(),
                        imprint.dominantEmotion(),
                        imprint.ageTicks(),
                        imprint.cleansingCount(),
                        Math.min(1f, imprint.dominance() + 0.15f),
                        imprint.whisperPool())
                        : imprint;
        CharacteristicBundle extracted = new CharacteristicBundle(
                source.pathway(),
                selected.sequence(),
                List.of(new CharacteristicBundle.Layer(
                        selected.sequence(), 1,
                        Math.max(0.1f,
                                selected.purity() * purityMultiplier))),
                extractedImprint,
                Math.min(100f, extractedCorruption),
                derive("load-extracted", source.sourceHash(),
                        Integer.toString(selected.sequence())));
        int retainedHighest = retainedLayers.stream()
                .mapToInt(CharacteristicBundle.Layer::sequence)
                .min()
                .orElseThrow();
        CharacteristicBundle retained = new CharacteristicBundle(
                source.pathway(),
                retainedHighest,
                retainedLayers,
                source.imprint(),
                source.corruption(),
                derive("load-retained", source.sourceHash(),
                        Integer.toString(selected.sequence())));
        return new ExtractionResult(outcome, retained, extracted);
    }

    private static String derive(String operation, String... sources) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(operation.getBytes(StandardCharsets.UTF_8));
            for (String source : sources) {
                digest.update((byte) 0);
                digest.update(source.getBytes(StandardCharsets.UTF_8));
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static float clamp(float value, float minimum, float maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    public enum Outcome {
        STABLE_SUCCESS,
        STRAINED_SUCCESS,
        FAILURE,
        NO_EXTRA_LOAD;

        public boolean extracted() {
            return this == STABLE_SUCCESS || this == STRAINED_SUCCESS;
        }
    }

    public enum AbsorptionStatus {
        SUCCESS,
        PATHWAY_MISMATCH,
        MULTI_UNIT,
        TOO_POTENT,
        MISSING_BASE_LAYER,
        DUPLICATE_SOURCE,
        LAYER_CAPACITY,
        INVALID
    }

    public record AbsorptionResult(
            AbsorptionStatus status,
            CharacteristicBundle merged,
            int extraLoad) {

        public static AbsorptionResult failure(AbsorptionStatus status) {
            return new AbsorptionResult(status, null, 0);
        }

        public boolean success() {
            return status == AbsorptionStatus.SUCCESS;
        }
    }

    public record ExtractionResult(
            Outcome outcome,
            CharacteristicBundle retained,
            CharacteristicBundle extracted) {}
}
