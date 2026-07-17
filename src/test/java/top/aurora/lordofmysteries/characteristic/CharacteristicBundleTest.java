package top.aurora.lordofmysteries.characteristic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import top.aurora.lordofmysteries.player.PlayerMysteryData;

class CharacteristicBundleTest {

    private static final ResourceLocation SEER =
            ResourceLocation.fromNamespaceAndPath("lord_of_mysteries", "seer");

    @Test
    void preservesLayeredAdvancementAcrossNbt() {
        CharacteristicBundle bundle = CharacteristicBundle.fromPotion(
                SEER, 9, 0.95f, "complete")
                .advance(8, 1f, "perfect")
                .advance(7, 0.75f, "flawed");

        CharacteristicBundle restored = CharacteristicBundle.load(bundle.save());

        assertEquals(SEER, restored.pathway());
        assertEquals(7, restored.highestSequence());
        assertEquals(3, restored.layers().size());
        assertEquals(0.75f, restored.layers().get(2).purity());
        assertEquals(64, restored.sourceHash().length());
    }

    @Test
    void rejectsSkippedOrRepeatedAdvancement() {
        CharacteristicBundle bundle = CharacteristicBundle.fromPotion(
                SEER, 9, 0.95f, "complete");
        assertThrows(IllegalArgumentException.class,
                () -> bundle.advance(9, 1f, "perfect"));
    }

    @Test
    void migratesSchemaFifteenExtraordinarySave() {
        PlayerMysteryData source = new PlayerMysteryData();
        source.pathway = SEER;
        source.sequence = 8;
        source.potionQuality = "flawed";
        CompoundTag legacyTag = source.save();
        legacyTag.putInt("schema_version", 15);
        legacyTag.remove("characteristic_bundles");

        PlayerMysteryData restored = new PlayerMysteryData();
        restored.load(legacyTag);

        assertEquals(PlayerMysteryData.CURRENT_SCHEMA_VERSION,
                restored.schemaVersion);
        assertEquals(1, restored.characteristicBundles.size());
        assertEquals(8, restored.characteristicBundles.get(0).highestSequence());
        assertEquals(0.75f,
                restored.characteristicBundles.get(0).layers().get(0).purity());
    }
}
