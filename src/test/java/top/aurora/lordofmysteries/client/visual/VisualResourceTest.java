package top.aurora.lordofmysteries.client.visual;

import com.google.gson.JsonParser;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VisualResourceTest {
    private static final Path ASSETS = Path.of("src/main/resources/assets/lord_of_mysteries");

    @Test
    void SharedMaterialBudgetAndArticulatedBirdFaceStayConsistent() throws Exception {
        var atlas = ImageIO.read(ASSETS.resolve("textures/entity/occult_atlas.png").toFile());
        assertEquals(512, atlas.getWidth());
        assertEquals(512, atlas.getHeight());
        var root = OccultCreatureModel.createLayer(CreatureVisual.NIGHT_OWL).bakeRoot();
        var head = root.getChild("root").getChild("head");
        for (String child : new String[]{"beak", "face_disc", "owl_eyes", "ear_left", "ear_right"}) {
            assertTrue(head.hasChild(child), child + " must follow head rotation");
        }
        var model = new OccultCreatureModel<>(root, CreatureVisual.NIGHT_OWL);
        model.applyPose(0, 0, 20, 45, 30, 0, false, true, false, false);
        assertTrue(head.yRot > 0 && head.xRot > 0);
        model.applyPose(0, 0, 0, Float.NaN, Float.POSITIVE_INFINITY, 0, false, true, false, true);
        assertTrue(Float.isFinite(head.yRot) && Float.isFinite(head.xRot));
    }

    @Test
    void EveryAtlasInventoryEntryHasVisibleIndependentRuntimeSprite() throws Exception {
        var manifest = JsonParser.parseString(Files.readString(Path.of("docs/art-source/manifest.json"))).getAsJsonObject();
        var hashes = new HashSet<Integer>();
        int count = 0;
        for (var sheet : manifest.getAsJsonArray("item_atlases")) {
            for (var value : sheet.getAsJsonObject().getAsJsonArray("ids")) {
                String id = value.getAsString();
                BufferedImage image = ImageIO.read(ASSETS.resolve("textures/item/" + id + ".png").toFile());
                assertNotNull(image, id);
                assertEquals(64, image.getWidth(), id);
                assertEquals(64, image.getHeight(), id);
                int visible = 0;
                int transparent = 0;
                int[] pixels = image.getRGB(0, 0, 64, 64, null, 0, 64);
                for (int pixel : pixels) {
                    if ((pixel >>> 24) > 64) visible++;
                    if ((pixel >>> 24) == 0) transparent++;
                }
                assertTrue(visible > 80, id + " is empty or unreadable");
                assertTrue(transparent > 400, id + " has no inventory padding");
                assertTrue(hashes.add(java.util.Arrays.hashCode(pixels)), id + " duplicates another sprite");
                var model = JsonParser.parseString(Files.readString(ASSETS.resolve("models/item/" + id + ".json"))).getAsJsonObject();
                assertEquals("lord_of_mysteries:item/" + id,
                        model.getAsJsonObject("textures").get("layer0").getAsString());
                assertArrayEquals(Files.readAllBytes(ASSETS.resolve("textures/item/" + id + ".png")),
                        Files.readAllBytes(Path.of("docs/assets/item-icons/" + id + ".png")), id);
                count++;
            }
        }
        assertEquals(133, count);
    }

    @Test
    void EveryParticleSpriteHasTransparentSilhouetteAndValidAtlasReference() throws Exception {
        var manifest = JsonParser.parseString(Files.readString(Path.of("docs/art-source/manifest.json"))).getAsJsonObject();
        for (var value : manifest.getAsJsonObject("particle_atlas").getAsJsonArray("ids")) {
            String id = value.getAsString();
            var definition = JsonParser.parseString(Files.readString(ASSETS.resolve("particles/" + id + ".json"))).getAsJsonObject();
            assertEquals("lord_of_mysteries:" + id, definition.getAsJsonArray("textures").get(0).getAsString());
            BufferedImage image = ImageIO.read(ASSETS.resolve("textures/particle/" + id + ".png").toFile());
            assertEquals(32, image.getWidth());
            int opaque = 0;
            int transparent = 0;
            for (int pixel : image.getRGB(0, 0, 32, 32, null, 0, 32)) {
                if ((pixel >>> 24) > 64) opaque++;
                if ((pixel >>> 24) == 0) transparent++;
            }
            assertTrue(opaque > 10, id + " is empty");
            assertTrue(transparent > 100, id + " is a solid square");
        }
    }

    @Test
    void EveryCreatureAndDoorLayerBakesWithBoundedGeometry() {
        for (CreatureVisual visual : CreatureVisual.values()) {
            var root = OccultCreatureModel.createLayer(visual).bakeRoot();
            assertTrue(root.hasChild("root"), visual.name());
            long parts = root.getAllParts().count();
            assertTrue(parts > 5 && parts <= 40, visual + " geometry budget: " + parts);
        }
        var door = TravelerDoorRenderer.createLayer().bakeRoot();
        assertTrue(door.hasChild("frame"));
        assertTrue(door.hasChild("veil"));
    }

    @Test
    void WorkstationCollisionMatchesGeometryIncludingOpenCavities() throws Exception {
        for (String id : new String[] {"crucible", "ritual_altar", "commission_board",
                "characteristic_separator", "imprint_washing_altar"}) {
            var model = JsonParser.parseString(Files.readString(
                    ASSETS.resolve("models/block/" + id + ".json"))).getAsJsonObject();
            var expected = net.minecraft.world.phys.shapes.Shapes.empty();
            for (var element : model.getAsJsonArray("elements")) {
                var from = element.getAsJsonObject().getAsJsonArray("from");
                var to = element.getAsJsonObject().getAsJsonArray("to");
                expected = net.minecraft.world.phys.shapes.Shapes.or(expected,
                        net.minecraft.world.phys.shapes.Shapes.box(
                                from.get(0).getAsDouble() / 16, from.get(1).getAsDouble() / 16, from.get(2).getAsDouble() / 16,
                                to.get(0).getAsDouble() / 16, to.get(1).getAsDouble() / 16, to.get(2).getAsDouble() / 16));
            }
            var actual = (net.minecraft.world.phys.shapes.VoxelShape)
                    top.aurora.lordofmysteries.core.WorkstationShapes.class
                            .getField(id.toUpperCase(java.util.Locale.ROOT)).get(null);
            assertFalse(net.minecraft.world.phys.shapes.Shapes.joinIsNotEmpty(
                    expected, actual, net.minecraft.world.phys.shapes.BooleanOp.NOT_SAME), id);
        }
        var crucible = top.aurora.lordofmysteries.core.WorkstationShapes.CRUCIBLE;
        assertFalse(crucible.toAabbs().stream().anyMatch(box -> box.contains(0.5, 0.5, 0.5)));
        assertTrue(crucible.toAabbs().stream().anyMatch(box -> box.contains(0.5, 0.25, 0.5)));
    }
}
