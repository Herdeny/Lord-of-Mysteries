package top.aurora.lordofmysteries.characteristic;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public final class CharacteristicProvenanceSavedData extends SavedData {

    public static final int DATA_VERSION = 1;
    private static final String DATA_NAME =
            "lord_of_mysteries_characteristic_provenance";
    private static final Pattern OPERATION_PATTERN =
            Pattern.compile("[a-z0-9_]{1,40}");
    private static final Pattern HASH_PATTERN =
            Pattern.compile("[0-9a-f]{64}");
    private static final int MAX_OUTPUTS = 4;
    private final Map<String, Entry> consumedSources = new LinkedHashMap<>();

    public static CharacteristicProvenanceSavedData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(
                CharacteristicProvenanceSavedData::load,
                CharacteristicProvenanceSavedData::new,
                DATA_NAME);
    }

    public static CharacteristicProvenanceSavedData load(CompoundTag tag) {
        CharacteristicProvenanceSavedData data =
                new CharacteristicProvenanceSavedData();
        ListTag entries = tag.getList("consumed_sources", Tag.TAG_COMPOUND);
        for (int index = 0; index < entries.size(); index++) {
            Entry entry = Entry.load(entries.getCompound(index));
            if (entry != null) {
                data.consumedSources.putIfAbsent(
                        entry.sourceFingerprint(), entry);
            }
        }
        return data;
    }

    public ConsumptionResult consume(
            String operation,
            UUID actor,
            long gameTime,
            List<String> inputSources,
            List<String> outputSources) {
        if (!validOperation(operation)
                || actor == null
                || gameTime < 0L
                || inputSources == null
                || inputSources.isEmpty()
                || outputSources == null
                || outputSources.size() > MAX_OUTPUTS) {
            return ConsumptionResult.INVALID;
        }
        List<String> inputFingerprints = fingerprints(inputSources);
        List<String> outputFingerprints = fingerprints(outputSources);
        if (inputFingerprints.size() != inputSources.size()
                || outputFingerprints.size() != outputSources.size()) {
            return ConsumptionResult.INVALID;
        }
        Set<String> uniqueInputs = new LinkedHashSet<>(inputFingerprints);
        if (uniqueInputs.size() != inputFingerprints.size()) {
            return ConsumptionResult.REPLAY;
        }
        if (uniqueInputs.stream().anyMatch(consumedSources::containsKey)) {
            return ConsumptionResult.REPLAY;
        }

        String actorHash = fingerprint(actor.toString());
        List<String> immutableOutputs = List.copyOf(outputFingerprints);
        for (String input : inputFingerprints) {
            consumedSources.put(input, new Entry(
                    input,
                    operation,
                    actorHash,
                    gameTime,
                    immutableOutputs));
        }
        setDirty();
        return ConsumptionResult.ACCEPTED;
    }

    public boolean isConsumed(String source) {
        return source != null
                && consumedSources.containsKey(fingerprint(source));
    }

    public int consumedSourceCount() {
        return consumedSources.size();
    }

    public long operationCount(String operation) {
        return consumedSources.values().stream()
                .filter(entry -> entry.operation().equals(operation))
                .map(Entry::operationKey)
                .distinct()
                .count();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("data_version", DATA_VERSION);
        ListTag entries = new ListTag();
        consumedSources.values().forEach(entry -> entries.add(entry.save()));
        tag.put("consumed_sources", entries);
        return tag;
    }

    private static boolean validOperation(String operation) {
        return operation != null
                && OPERATION_PATTERN.matcher(operation).matches();
    }

    private static List<String> fingerprints(List<String> sources) {
        List<String> result = new ArrayList<>(sources.size());
        for (String source : sources) {
            if (source == null || source.isBlank() || source.length() > 512) {
                return List.of();
            }
            result.add(fingerprint(source));
        }
        return result;
    }

    private static String fingerprint(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(
                    value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is unavailable", exception);
        }
    }

    public enum ConsumptionResult {
        ACCEPTED,
        REPLAY,
        INVALID
    }

    public record Entry(
            String sourceFingerprint,
            String operation,
            String actorHash,
            long gameTime,
            List<String> outputFingerprints) {

        public Entry {
            outputFingerprints = List.copyOf(outputFingerprints);
        }

        private CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putString("source", sourceFingerprint);
            tag.putString("operation", operation);
            tag.putString("actor_hash", actorHash);
            tag.putLong("game_time", gameTime);
            ListTag outputs = new ListTag();
            outputFingerprints.forEach(
                    output -> outputs.add(StringTag.valueOf(output)));
            tag.put("outputs", outputs);
            return tag;
        }

        private static Entry load(CompoundTag tag) {
            String source = tag.getString("source");
            String operation = tag.getString("operation");
            String actorHash = tag.getString("actor_hash");
            long gameTime = tag.getLong("game_time");
            ListTag rawOutputs = tag.getList("outputs", Tag.TAG_STRING);
            if (!HASH_PATTERN.matcher(source).matches()
                    || !validOperation(operation)
                    || !HASH_PATTERN.matcher(actorHash).matches()
                    || gameTime < 0L
                    || rawOutputs.size() > MAX_OUTPUTS) {
                return null;
            }
            List<String> outputs = new ArrayList<>(rawOutputs.size());
            for (int index = 0; index < rawOutputs.size(); index++) {
                String output = rawOutputs.getString(index);
                if (!HASH_PATTERN.matcher(output).matches()) return null;
                outputs.add(output);
            }
            return new Entry(source, operation, actorHash, gameTime, outputs);
        }

        private String operationKey() {
            return operation + "|" + actorHash + "|" + gameTime
                    + "|" + outputFingerprints;
        }
    }
}
