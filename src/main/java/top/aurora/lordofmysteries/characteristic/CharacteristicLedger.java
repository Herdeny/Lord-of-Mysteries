package top.aurora.lordofmysteries.characteristic;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.resources.ResourceLocation;

import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.potion.PotionQuality;

public final class CharacteristicLedger {

    private CharacteristicLedger() {}

    public static void recordPotionAdvancement(PlayerMysteryData data,
                                               ResourceLocation pathway,
                                               int sequence,
                                               PotionQuality quality) {
        float purity = purity(quality);
        for (int index = 0; index < data.characteristicBundles.size(); index++) {
            CharacteristicBundle bundle = data.characteristicBundles.get(index);
            if (bundle.pathway().equals(pathway)) {
                data.characteristicBundles.set(index,
                        bundle.advance(sequence, purity, quality.id()));
                return;
            }
        }
        data.characteristicBundles.add(CharacteristicBundle.fromPotion(
                pathway, sequence, purity, quality.id()));
    }

    public static List<CharacteristicBundle> migrateLegacy(
            ResourceLocation pathway, int sequence, String qualityId) {
        if (pathway == null || sequence < 0 || sequence > 9) return List.of();
        PotionQuality quality = PotionQuality.fromId(qualityId);
        return List.of(CharacteristicBundle.fromPotion(
                pathway, sequence, purity(quality), "legacy-" + quality.id()));
    }

    public static List<CharacteristicBundle> copy(
            List<CharacteristicBundle> bundles) {
        return new ArrayList<>(bundles);
    }

    private static float purity(PotionQuality quality) {
        return switch (quality) {
            case PERFECT -> 1f;
            case COMPLETE -> 0.95f;
            case FLAWED -> 0.75f;
            case CONTAMINATED -> 0.5f;
        };
    }
}
