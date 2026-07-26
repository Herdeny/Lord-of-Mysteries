package top.aurora.lordofmysteries.ability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class TravelMarkerServiceTest {

    @Test
    void validVanillaLodestoneTagRestoresMarker() {
        CompoundTag tag = markerTag(
                "minecraft:the_nether", new BlockPos(-24, 67, 103));

        TravelMarkerService.MarkerData marker =
                TravelMarkerService.parseMarkerTag(tag).orElseThrow();

        assertEquals(ResourceLocation.fromNamespaceAndPath(
                "minecraft", "the_nether"), marker.dimension());
        assertEquals(new BlockPos(-24, 67, 103), marker.position());
    }

    @Test
    void malformedDimensionAndIncompletePositionAreRejected() {
        CompoundTag invalidDimension = markerTag(
                "not a dimension", BlockPos.ZERO);
        assertTrue(TravelMarkerService.readMarkerTag(
                invalidDimension).isEmpty());

        CompoundTag incomplete = markerTag(
                "minecraft:overworld", BlockPos.ZERO);
        incomplete.getCompound("LodestonePos").remove("Y");
        assertTrue(TravelMarkerService.parseMarkerTag(incomplete).isEmpty());
    }

    @Test
    void missingVanillaMarkerFieldsAreRejected() {
        assertTrue(TravelMarkerService.parseMarkerTag(
                new CompoundTag()).isEmpty());

        CompoundTag positionOnly = markerTag(
                "minecraft:overworld", BlockPos.ZERO);
        positionOnly.remove("LodestoneDimension");
        assertTrue(TravelMarkerService.parseMarkerTag(positionOnly).isEmpty());
    }

    @Test
    void markerNameUsesNamespacedMetadataWithoutChangingVanillaBinding() {
        CompoundTag tag = markerTag(
                "minecraft:overworld", new BlockPos(4, 70, -8));
        tag.putString(
                TravelMarkerService.MARKER_NAME_TAG,
                "  \u00a7bNorthern   Relay ");

        assertEquals(
                "Northern Relay",
                TravelMarkerService.readMarkerName(tag));
        assertTrue(TravelMarkerService.parseMarkerTag(tag).isPresent());
    }

    @Test
    void missingMarkerNameRemainsSafeAndUnnamed() {
        assertEquals(
                "",
                TravelMarkerService.readMarkerName(markerTag(
                        "minecraft:overworld", BlockPos.ZERO)));
        assertEquals("", TravelMarkerService.readMarkerName(null));
    }

    private static CompoundTag markerTag(
            String dimension, BlockPos position) {
        CompoundTag positionTag = new CompoundTag();
        positionTag.putInt("X", position.getX());
        positionTag.putInt("Y", position.getY());
        positionTag.putInt("Z", position.getZ());
        CompoundTag tag = new CompoundTag();
        tag.put("LodestonePos", positionTag);
        tag.putString("LodestoneDimension", dimension);
        return tag;
    }
}
