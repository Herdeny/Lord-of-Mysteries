package top.aurora.lordofmysteries.characteristic;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

public record CharacteristicBundle(
        ResourceLocation pathway,
        int highestSequence,
        List<Layer> layers,
        Imprint imprint,
        float corruption,
        String sourceHash) {

    public CharacteristicBundle {
        if (pathway == null) throw new IllegalArgumentException("pathway is required");
        if (highestSequence < 0 || highestSequence > 9) {
            throw new IllegalArgumentException("highestSequence must be within 0-9");
        }
        layers = List.copyOf(layers);
        if (layers.isEmpty()) throw new IllegalArgumentException("layers must not be empty");
        if (layers.stream().noneMatch(layer -> layer.sequence() == highestSequence)) {
            throw new IllegalArgumentException("highestSequence must exist in layers");
        }
        if (imprint == null) throw new IllegalArgumentException("imprint is required");
        if (!Float.isFinite(corruption) || corruption < 0f || corruption > 100f) {
            throw new IllegalArgumentException("corruption must be within 0-100");
        }
        if (sourceHash == null || sourceHash.isBlank()) {
            throw new IllegalArgumentException("sourceHash is required");
        }
    }

    public static CharacteristicBundle fromPotion(ResourceLocation pathway,
                                                   int sequence,
                                                   float purity,
                                                   String qualityId) {
        Layer layer = new Layer(sequence, 1, purity);
        return new CharacteristicBundle(pathway, sequence, List.of(layer),
                Imprint.fresh(), 0f,
                hash(pathway + "|" + sequence + "|" + qualityId + "|potion"));
    }

    public CharacteristicBundle advance(int sequence, float purity,
                                        String qualityId) {
        if (sequence >= highestSequence) {
            throw new IllegalArgumentException(
                    "advancement sequence must be lower than current sequence");
        }
        List<Layer> advancedLayers = new ArrayList<>(layers);
        advancedLayers.add(new Layer(sequence, 1, purity));
        advancedLayers.sort(Comparator.comparingInt(Layer::sequence).reversed());
        return new CharacteristicBundle(pathway, sequence, advancedLayers,
                imprint, corruption,
                hash(sourceHash + "|" + sequence + "|" + qualityId));
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("pathway", pathway.toString());
        tag.putInt("highest_sequence", highestSequence);
        ListTag layerTags = new ListTag();
        for (Layer layer : layers) layerTags.add(layer.save());
        tag.put("layers", layerTags);
        tag.put("imprint", imprint.save());
        tag.putFloat("corruption", corruption);
        tag.putString("source_hash", sourceHash);
        return tag;
    }

    public static CharacteristicBundle load(CompoundTag tag) {
        ResourceLocation pathway = ResourceLocation.tryParse(tag.getString("pathway"));
        if (pathway == null) throw new IllegalArgumentException("invalid pathway");
        List<Layer> layers = new ArrayList<>();
        ListTag layerTags = tag.getList("layers", Tag.TAG_COMPOUND);
        for (int index = 0; index < layerTags.size(); index++) {
            layers.add(Layer.load(layerTags.getCompound(index)));
        }
        return new CharacteristicBundle(pathway,
                tag.getInt("highest_sequence"), layers,
                Imprint.load(tag.getCompound("imprint")),
                tag.getFloat("corruption"), tag.getString("source_hash"));
    }

    private static String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(
                    value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    public record Layer(int sequence, int count, float purity) {
        public Layer {
            if (sequence < 0 || sequence > 9) {
                throw new IllegalArgumentException("layer sequence must be within 0-9");
            }
            if (count < 1 || count > 64) {
                throw new IllegalArgumentException("layer count must be within 1-64");
            }
            if (!Float.isFinite(purity) || purity < 0f || purity > 1f) {
                throw new IllegalArgumentException("layer purity must be within 0-1");
            }
        }

        private CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("sequence", sequence);
            tag.putInt("count", count);
            tag.putFloat("purity", purity);
            return tag;
        }

        private static Layer load(CompoundTag tag) {
            return new Layer(tag.getInt("sequence"), tag.getInt("count"),
                    tag.getFloat("purity"));
        }
    }

    public record Imprint(
            int formerOwnerSequence,
            String dominantEmotion,
            long ageTicks,
            int cleansingCount,
            float dominance,
            List<String> whisperPool) {

        public Imprint {
            if (formerOwnerSequence < -1 || formerOwnerSequence > 9) {
                throw new IllegalArgumentException(
                        "formerOwnerSequence must be within -1-9");
            }
            if (dominantEmotion == null || dominantEmotion.isBlank()) {
                throw new IllegalArgumentException("dominantEmotion is required");
            }
            if (ageTicks < 0L || cleansingCount < 0) {
                throw new IllegalArgumentException("imprint counters cannot be negative");
            }
            if (!Float.isFinite(dominance) || dominance < 0f || dominance > 1f) {
                throw new IllegalArgumentException("dominance must be within 0-1");
            }
            whisperPool = List.copyOf(whisperPool);
        }

        public static Imprint fresh() {
            return new Imprint(-1, "neutral", 0L, 0, 0f, List.of());
        }

        private CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("former_owner_sequence", formerOwnerSequence);
            tag.putString("dominant_emotion", dominantEmotion);
            tag.putLong("age_ticks", ageTicks);
            tag.putInt("cleansing_count", cleansingCount);
            tag.putFloat("dominance", dominance);
            ListTag whispers = new ListTag();
            for (String whisper : whisperPool) whispers.add(StringTag.valueOf(whisper));
            tag.put("whisper_pool", whispers);
            return tag;
        }

        private static Imprint load(CompoundTag tag) {
            List<String> whispers = new ArrayList<>();
            ListTag whisperTags = tag.getList("whisper_pool", Tag.TAG_STRING);
            for (int index = 0; index < whisperTags.size(); index++) {
                String whisper = whisperTags.getString(index);
                if (!whisper.isBlank()) whispers.add(whisper);
            }
            return new Imprint(tag.contains("former_owner_sequence")
                    ? tag.getInt("former_owner_sequence") : -1,
                    tag.contains("dominant_emotion")
                            ? tag.getString("dominant_emotion") : "neutral",
                    tag.getLong("age_ticks"), tag.getInt("cleansing_count"),
                    tag.getFloat("dominance"), whispers);
        }
    }
}
