package top.aurora.lordofmysteries.knowledge;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

class M1StarterRouteContractTest {

    private static final Path CONTRACT = Path.of(
            "docs/m1-playability-contract.json");
    private static final Path STARTER_LOOT = Path.of(
            "src/main/resources/data/lord_of_mysteries/loot_tables/chests/"
                    + "starter_investigator_supplies.json");

    @Test
    void starterCacheContainsEveryGuaranteedSupply() throws IOException {
        JsonObject contract = read(CONTRACT);
        Set<String> lootItems = new HashSet<>();
        collectItems(read(STARTER_LOOT), lootItems);

        for (JsonElement item : contract.getAsJsonArray("starter_cache_items")) {
            assertTrue(lootItems.contains(item.getAsString()), item.getAsString());
        }
    }

    @Test
    void sequenceSevenStillRequiresTheFieldHunt() throws IOException {
        JsonObject contract = read(CONTRACT);
        Set<String> lootItems = new HashSet<>();
        collectItems(read(STARTER_LOOT), lootItems);
        String fieldItem = contract.getAsJsonArray("route")
                .get(2).getAsJsonObject().get("field_acquired").getAsString();

        assertFalse(lootItems.contains(fieldItem));
    }

    private static JsonObject read(Path path) throws IOException {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }

    private static void collectItems(JsonElement element, Set<String> items) {
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("type") && object.has("name")
                    && "minecraft:item".equals(object.get("type").getAsString())) {
                items.add(object.get("name").getAsString());
            }
            object.entrySet().forEach(entry -> collectItems(entry.getValue(), items));
        } else if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(value -> collectItems(value, items));
        }
    }
}
