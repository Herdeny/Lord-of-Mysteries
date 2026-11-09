package top.aurora.lordofmysteries.survival;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SurvivalContentTest {
    private static final Path DATA = Path.of("src/main/resources/data/lord_of_mysteries");
    private static final Path ASSETS = Path.of("src/main/resources/assets/lord_of_mysteries");
    private JsonObject read(Path path) throws Exception {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }

    @Test
    void orePlacementHasFiniteOverworldBoundsAndBothHostVariants() throws Exception {
        for (String id : new String[]{"saltstone", "argent"}) {
            var feature = read(DATA.resolve("worldgen/configured_feature/" + id + ".json")).getAsJsonObject("config");
            assertEquals(2, feature.getAsJsonArray("targets").size());
            assertTrue(feature.get("size").getAsInt() <= 6);
            var placement = read(DATA.resolve("worldgen/placed_feature/" + id + ".json")).getAsJsonArray("placement");
            assertTrue(placement.get(0).getAsJsonObject().get("count").getAsInt() <= 6);
            var height = placement.get(2).getAsJsonObject().getAsJsonObject("height");
            assertTrue(height.getAsJsonObject("min_inclusive").get("absolute").getAsInt() >= -64);
            assertTrue(height.getAsJsonObject("max_inclusive").get("absolute").getAsInt() <= 80);
            assertEquals("#minecraft:is_overworld",
                    read(DATA.resolve("forge/biome_modifier/" + id + ".json")).get("biomes").getAsString());
        }
    }

    @Test
    void oreDropsPreserveSilkTouchFortuneAndExplosionSemantics() throws Exception {
        for (String id : new String[]{"saltstone_ore", "deepslate_saltstone_ore", "argent_ore", "deepslate_argent_ore"}) {
            String json = Files.readString(DATA.resolve("loot_tables/blocks/" + id + ".json"));
            assertTrue(json.contains("minecraft:alternatives"));
            assertTrue(json.contains("minecraft:silk_touch"));
            assertTrue(json.contains("minecraft:fortune"));
            assertTrue(json.contains("minecraft:explosion_decay"));
            assertTrue(json.contains("lord_of_mysteries:" + id));
            assertTrue(Files.exists(DATA.resolve("recipes/survival_" + id + "_smelting.json")));
            assertTrue(Files.exists(DATA.resolve("recipes/survival_" + id + "_blasting.json")));
        }
    }

    @Test
    void cropStatesRequireFullMaturityAndKeepOriginalMaterialIdentity() throws Exception {
        for (String name : new String[]{"spirit_herb", "heather"}) {
            var variants = read(ASSETS.resolve("blockstates/" + name + "_crop.json")).getAsJsonObject("variants");
            assertEquals(8, variants.size());
            assertNotEquals(variants.get("age=6"), variants.get("age=7"));
            var pools = read(DATA.resolve("loot_tables/blocks/" + name + "_crop.json")).getAsJsonArray("pools");
            assertEquals("7", pools.get(1).getAsJsonObject().getAsJsonArray("conditions")
                    .get(0).getAsJsonObject().getAsJsonObject("properties").get("age").getAsString());
            assertEquals("lord_of_mysteries:" + name, pools.get(0).getAsJsonObject()
                    .getAsJsonArray("entries").get(0).getAsJsonObject().get("name").getAsString());
            assertTrue(Files.exists(DATA.resolve("recipes/" + name + ".json")));
        }
    }

    @Test
    void mobLootIsAdditivePlayerGatedAndNotGuaranteed() throws Exception {
        var list = read(DATA.getParent().resolve("forge/loot_modifiers/global_loot_modifiers.json"));
        assertFalse(list.get("replace").getAsBoolean());
        assertEquals(9, list.getAsJsonArray("entries").size());
        for (String entity : new String[]{"zombie", "husk", "spider", "witch"}) {
            var modifier = read(DATA.resolve("loot_modifiers/survival_" + entity + ".json"));
            var conditions = modifier.getAsJsonArray("conditions");
            assertEquals("minecraft:entities/" + entity, conditions.get(0).getAsJsonObject().get("loot_table_id").getAsString());
            assertEquals("minecraft:killed_by_player", conditions.get(1).getAsJsonObject().get("condition").getAsString());
            assertTrue(conditions.get(2).getAsJsonObject().get("chance").getAsDouble() <= 0.2);
            assertEquals(1, modifier.get("count").getAsInt());
        }
    }

    @Test
    void nativeRuinsHaveBoundedCoordinatesUniqueBlocksAndSeededLootTables() throws Exception {
        for (String name : new String[]{"occult_cellar", "herbalist_ruin"}) {
            var nbt = NbtIo.readCompressed(DATA.resolve("structures/" + name + ".nbt").toFile());
            var size = nbt.getList("size", Tag.TAG_INT);
            assertEquals(11, size.getInt(0));
            assertEquals(9, size.getInt(1));
            var palette = nbt.getList("palette", Tag.TAG_COMPOUND);
            var positions = new HashSet<String>();
            int chests = 0;
            for (var raw : nbt.getList("blocks", Tag.TAG_COMPOUND)) {
                var block = (net.minecraft.nbt.CompoundTag) raw;
                var pos = block.getList("pos", Tag.TAG_INT);
                assertTrue(positions.add(pos.toString()));
                for (int axis = 0; axis < 3; axis++) assertTrue(pos.getInt(axis) >= 0 && pos.getInt(axis) < size.getInt(axis));
                assertTrue(block.getInt("state") >= 0 && block.getInt("state") < palette.size());
                var entity = block.getCompound("nbt");
                if (entity.contains("LootTable")) {
                    assertEquals("lord_of_mysteries:chests/" + name, entity.getString("LootTable"));
                    assertFalse(entity.contains("Items"));
                    chests++;
                }
            }
            assertEquals(2, chests);
        }
    }

    @Test
    void ruinSpacingAndBiomeFiltersAvoidOceanWideGeneration() throws Exception {
        for (String name : new String[]{"occult_cellar", "herbalist_ruin"}) {
            var placement = read(DATA.resolve("worldgen/structure_set/" + name + ".json")).getAsJsonObject("placement");
            assertTrue(placement.get("spacing").getAsInt() >= 32);
            assertEquals(1, read(DATA.resolve("worldgen/structure/" + name + ".json")).get("size").getAsInt());
            assertTrue(placement.get("separation").getAsInt() < placement.get("spacing").getAsInt());
            String biomes = Files.readString(DATA.resolve("tags/worldgen/biome/has_structure/" + name + ".json"));
            assertFalse(biomes.contains("ocean"));
            assertFalse(biomes.contains("river"));
            assertFalse(biomes.contains("is_overworld"));
        }
    }

    @Test
    void mortarRecipesHaveBoundedCostsAndNoProgressionBypassOutputs() throws Exception {
        var manifest = read(Path.of("docs/survival-content.json"));
        var recipes = manifest.getAsJsonArray("mortar");
        assertEquals(14, recipes.size());
        for (var raw : recipes) {
            var spec = raw.getAsJsonObject();
            var actual = read(DATA.resolve("recipes/mortar_" + spec.get("id").getAsString() + ".json"));
            assertEquals(spec.get("main_count"), actual.get("main_count"));
            assertEquals(spec.get("reagent_count"), actual.get("reagent_count"));
            assertTrue(actual.get("main_count").getAsInt() > 0);
            String result = actual.getAsJsonObject("result").get("item").getAsString();
            assertFalse(result.contains("potion"));
            assertFalse(result.contains("characteristic"));
            assertFalse(result.contains("formula"));
            assertTrue(actual.getAsJsonObject("result").get("count").getAsInt() <= 4);
        }
    }

    @Test
    void fishingBonusRequiresRootTableOpenWaterAndRealRod() throws Exception {
        var modifier = read(DATA.resolve("loot_modifiers/survival_fishing.json"));
        var conditions = modifier.getAsJsonArray("conditions");
        assertEquals("minecraft:gameplay/fishing", conditions.get(0).getAsJsonObject().get("loot_table_id").getAsString());
        var predicate = conditions.get(1).getAsJsonObject().getAsJsonObject("predicate");
        assertEquals("minecraft:fishing_bobber", predicate.get("type").getAsString());
        assertTrue(predicate.getAsJsonObject("type_specific").get("in_open_water").getAsBoolean());
        assertEquals("minecraft:fishing_rod", conditions.get(2).getAsJsonObject().getAsJsonObject("predicate")
                .getAsJsonArray("items").get(0).getAsString());
        assertEquals(0.2, conditions.get(3).getAsJsonObject().get("chance").getAsDouble(), 0.00001);
        assertEquals(1, modifier.get("count").getAsInt());
    }

    @Test
    void discoveryTreeHasTenReachableNodesWithoutRepeatableRewards() throws Exception {
        var nodes = read(Path.of("docs/survival-content.json")).getAsJsonArray("discoveries");
        var seen = new HashSet<String>();
        assertEquals(10, nodes.size());
        for (var raw : nodes) {
            var node = raw.getAsJsonObject();
            String id = node.get("id").getAsString();
            if (node.has("parent")) assertTrue(seen.contains(node.get("parent").getAsString()));
            assertTrue(seen.add(id));
            var advancement = read(DATA.resolve("advancements/survival/" + id + ".json"));
            assertFalse(advancement.has("rewards"));
            assertFalse(advancement.getAsJsonObject("display").get("announce_to_chat").getAsBoolean());
            if (node.has("loot_table")) {
                assertEquals("minecraft:player_generates_container_loot", advancement.getAsJsonObject("criteria")
                        .getAsJsonObject("discover").get("trigger").getAsString());
            }
        }
    }

    @Test
    void discoveryTranslationsExplainAcquisitionInBothLanguages() throws Exception {
        var nodes = read(Path.of("docs/survival-content.json")).getAsJsonArray("discoveries");
        for (String language : new String[]{"zh_cn", "en_us"}) {
            var translated = read(ASSETS.resolve("lang/" + language + ".json"));
            for (var raw : nodes) {
                var node = raw.getAsJsonObject();
                String key = "advancement.lord_of_mysteries.survival." + node.get("id").getAsString();
                assertEquals(node.get(language.equals("zh_cn") ? "title" : "en"), translated.get(key + ".title"));
                assertEquals(node.get(language.equals("zh_cn") ? "description" : "description_en"), translated.get(key + ".description"));
            }
        }
    }

    @Test
    void mortarAlternativesDoNotShadowExistingHandPairs() throws Exception {
        var pairs = new HashSet<String>();
        for (var raw : read(Path.of("docs/survival-content.json")).getAsJsonArray("mortar")) {
            var recipe = raw.getAsJsonObject();
            assertTrue(pairs.add(recipe.get("main").getAsString() + "|" + recipe.get("reagent").getAsString()));
        }
    }

    @Test
    void mortarGeometryRetainsBowlCavityAndOreFacesAvoidHostZFight() throws Exception {
        var mortar = read(ASSETS.resolve("models/block/mystic_mortar.json")).getAsJsonArray("elements");
        assertEquals(5, mortar.size());
        for (var raw : mortar) {
            var element = raw.getAsJsonObject();
            var start = element.getAsJsonArray("from");
            var end = element.getAsJsonArray("to");
            boolean coversCenter = true;
            for (int axis = 0; axis < 3; axis++) coversCenter &= start.get(axis).getAsDouble() < 8 && end.get(axis).getAsDouble() > 8;
            assertFalse(coversCenter);
        }
        var host = read(ASSETS.resolve("models/block/saltstone_ore.json")).getAsJsonArray("elements").get(0).getAsJsonObject();
        assertTrue(host.getAsJsonArray("from").get(0).getAsDouble() > 0);
        assertTrue(host.getAsJsonArray("to").get(0).getAsDouble() < 16);
    }
}
