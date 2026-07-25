package top.aurora.lordofmysteries.characteristic;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class CharacteristicProcessingLogic {

    public static final float MERGE_CORRUPTION = 5f;
    public static final float CLEANSE_CORRUPTION_REDUCTION = 25f;
    public static final float CLEANSE_DOMINANCE_REDUCTION = 0.2f;
    public static final float CLEANSE_PURITY_LOSS = 0.03f;

    private CharacteristicProcessingLogic() {}

    public static SplitResult splitHighestLayer(CharacteristicBundle bundle) {
        if (totalUnits(bundle) <= 1) {
            return new SplitResult(Status.SINGLE_UNIT, null, null);
        }
        CharacteristicBundle.Layer highest = bundle.layers().stream()
                .min(Comparator.comparingInt(CharacteristicBundle.Layer::sequence))
                .orElseThrow();
        List<CharacteristicBundle.Layer> remainderLayers =
                new ArrayList<>(bundle.layers());
        remainderLayers.remove(highest);
        if (highest.count() > 1) {
            remainderLayers.add(new CharacteristicBundle.Layer(
                    highest.sequence(), highest.count() - 1, highest.purity()));
        }
        remainderLayers.sort(Comparator.comparingInt(
                CharacteristicBundle.Layer::sequence).reversed());
        CharacteristicBundle extracted = new CharacteristicBundle(
                bundle.pathway(),
                highest.sequence(),
                List.of(new CharacteristicBundle.Layer(
                        highest.sequence(), 1, highest.purity())),
                bundle.imprint(),
                bundle.corruption(),
                derive("split-extracted", bundle.sourceHash(),
                        Integer.toString(highest.sequence())));
        int remainderHighest = remainderLayers.stream()
                .mapToInt(CharacteristicBundle.Layer::sequence)
                .min()
                .orElseThrow();
        CharacteristicBundle remainder = new CharacteristicBundle(
                bundle.pathway(),
                remainderHighest,
                remainderLayers,
                bundle.imprint(),
                bundle.corruption(),
                derive("split-remainder", bundle.sourceHash(),
                        Integer.toString(highest.sequence())));
        return new SplitResult(Status.SUCCESS, extracted, remainder);
    }

    public static MergeResult merge(CharacteristicBundle first,
                                    CharacteristicBundle second) {
        if (!first.pathway().equals(second.pathway())) {
            return new MergeResult(Status.PATHWAY_MISMATCH, null);
        }
        if (first.sourceHash().equals(second.sourceHash())) {
            return new MergeResult(Status.DUPLICATE_SOURCE, null);
        }
        Map<Integer, LayerAccumulator> accumulators = new TreeMap<>(
                Comparator.reverseOrder());
        addLayers(accumulators, first.layers());
        addLayers(accumulators, second.layers());
        if (accumulators.values().stream().anyMatch(
                accumulator -> accumulator.count > 64)) {
            return new MergeResult(Status.LAYER_CAPACITY, null);
        }
        List<CharacteristicBundle.Layer> layers = accumulators.entrySet()
                .stream()
                .map(entry -> entry.getValue().toLayer(entry.getKey()))
                .toList();
        int firstUnits = totalUnits(first);
        int secondUnits = totalUnits(second);
        int totalUnits = firstUnits + secondUnits;
        float corruption = clamp(
                (first.corruption() * firstUnits
                        + second.corruption() * secondUnits) / totalUnits
                        + MERGE_CORRUPTION,
                0f, 100f);
        CharacteristicBundle.Imprint imprint = mergeImprint(
                first, second, firstUnits, secondUnits);
        List<String> hashes = new ArrayList<>(List.of(
                first.sourceHash(), second.sourceHash()));
        hashes.sort(String::compareTo);
        CharacteristicBundle merged = new CharacteristicBundle(
                first.pathway(),
                Math.min(first.highestSequence(), second.highestSequence()),
                layers,
                imprint,
                corruption,
                derive("merge", hashes.get(0), hashes.get(1)));
        return new MergeResult(Status.SUCCESS, merged);
    }

    public static CleanseResult cleanse(CharacteristicBundle bundle) {
        if (bundle.corruption() <= 0f
                && bundle.imprint().dominance() <= 0f) {
            return new CleanseResult(Status.ALREADY_CLEAN, null);
        }
        List<CharacteristicBundle.Layer> layers = bundle.layers().stream()
                .map(layer -> new CharacteristicBundle.Layer(
                        layer.sequence(),
                        layer.count(),
                        Math.max(0.1f,
                                layer.purity() - CLEANSE_PURITY_LOSS)))
                .toList();
        CharacteristicBundle.Imprint imprint = bundle.imprint();
        CharacteristicBundle.Imprint cleansedImprint =
                new CharacteristicBundle.Imprint(
                        imprint.formerOwnerSequence(),
                        imprint.dominantEmotion(),
                        imprint.ageTicks(),
                        imprint.cleansingCount() + 1,
                        Math.max(0f, imprint.dominance()
                                - CLEANSE_DOMINANCE_REDUCTION),
                        imprint.whisperPool());
        CharacteristicBundle cleansed = new CharacteristicBundle(
                bundle.pathway(),
                bundle.highestSequence(),
                layers,
                cleansedImprint,
                Math.max(0f, bundle.corruption()
                        - CLEANSE_CORRUPTION_REDUCTION),
                derive("cleanse", bundle.sourceHash(),
                        Integer.toString(cleansedImprint.cleansingCount())));
        return new CleanseResult(Status.SUCCESS, cleansed);
    }

    public static int totalUnits(CharacteristicBundle bundle) {
        return bundle.layers().stream()
                .mapToInt(CharacteristicBundle.Layer::count)
                .sum();
    }

    public static float averagePurity(CharacteristicBundle bundle) {
        int units = totalUnits(bundle);
        if (units == 0) return 0f;
        float weighted = 0f;
        for (CharacteristicBundle.Layer layer : bundle.layers()) {
            weighted += layer.purity() * layer.count();
        }
        return weighted / units;
    }

    private static void addLayers(
            Map<Integer, LayerAccumulator> accumulators,
            List<CharacteristicBundle.Layer> layers) {
        for (CharacteristicBundle.Layer layer : layers) {
            accumulators.computeIfAbsent(
                    layer.sequence(), ignored -> new LayerAccumulator())
                    .add(layer);
        }
    }

    private static CharacteristicBundle.Imprint mergeImprint(
            CharacteristicBundle first,
            CharacteristicBundle second,
            int firstUnits,
            int secondUnits) {
        CharacteristicBundle dominant = dominantBundle(first, second);
        CharacteristicBundle.Imprint firstImprint = first.imprint();
        CharacteristicBundle.Imprint secondImprint = second.imprint();
        int formerOwnerSequence = lowerKnownSequence(
                firstImprint.formerOwnerSequence(),
                secondImprint.formerOwnerSequence());
        LinkedHashSet<String> whispers = new LinkedHashSet<>();
        firstImprint.whisperPool().stream().sorted().forEach(whispers::add);
        secondImprint.whisperPool().stream().sorted().forEach(whispers::add);
        float dominance = clamp(
                (firstImprint.dominance() * firstUnits
                        + secondImprint.dominance() * secondUnits)
                        / (firstUnits + secondUnits)
                        + 0.05f,
                0f, 1f);
        return new CharacteristicBundle.Imprint(
                formerOwnerSequence,
                dominant.imprint().dominantEmotion(),
                Math.max(firstImprint.ageTicks(), secondImprint.ageTicks()),
                Math.max(firstImprint.cleansingCount(),
                        secondImprint.cleansingCount()),
                dominance,
                whispers.stream().limit(16).toList());
    }

    private static CharacteristicBundle dominantBundle(
            CharacteristicBundle first, CharacteristicBundle second) {
        int dominance = Float.compare(
                first.imprint().dominance(),
                second.imprint().dominance());
        if (dominance > 0) return first;
        if (dominance < 0) return second;
        return first.sourceHash().compareTo(second.sourceHash()) <= 0
                ? first : second;
    }

    private static int lowerKnownSequence(int first, int second) {
        if (first < 0) return second;
        if (second < 0) return first;
        return Math.min(first, second);
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

    public enum Status {
        SUCCESS,
        SINGLE_UNIT,
        PATHWAY_MISMATCH,
        DUPLICATE_SOURCE,
        LAYER_CAPACITY,
        ALREADY_CLEAN
    }

    public record SplitResult(
            Status status,
            CharacteristicBundle extracted,
            CharacteristicBundle remainder) {

        public boolean success() {
            return status == Status.SUCCESS;
        }
    }

    public record MergeResult(
            Status status,
            CharacteristicBundle merged) {

        public boolean success() {
            return status == Status.SUCCESS;
        }
    }

    public record CleanseResult(
            Status status,
            CharacteristicBundle cleansed) {

        public boolean success() {
            return status == Status.SUCCESS;
        }
    }

    private static final class LayerAccumulator {

        private int count;
        private float weightedPurity;

        private void add(CharacteristicBundle.Layer layer) {
            count += layer.count();
            weightedPurity += layer.purity() * layer.count();
        }

        private CharacteristicBundle.Layer toLayer(int sequence) {
            return new CharacteristicBundle.Layer(
                    sequence, count, weightedPurity / count);
        }
    }
}
