import argparse
import gzip
import json
import struct
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MOD = "lord_of_mysteries"
DATA = ROOT / "src/main/resources/data"
ASSETS = ROOT / "src/main/resources/assets" / MOD
MANIFEST = ROOT / "docs/survival-content.json"


def cube(start, end, texture):
    return {"from": start, "to": end, "faces": {
        face: {"texture": texture} for face in ("down", "up", "north", "south", "west", "east")}}


def loot(item, count=1, conditions=None, functions=None):
    entry = {"type": "minecraft:item", "name": item if ":" in item else f"{MOD}:{item}"}
    if functions:
        entry["functions"] = functions
    if count != 1:
        entry.setdefault("functions", []).append({"function": "minecraft:set_count", "count": count})
    pool = {"rolls": 1, "entries": [entry]}
    if conditions:
        pool["conditions"] = conditions
    return pool


def integer(value):
    return (3, value)


def string(value):
    return (8, value)


def list_tag(kind, values):
    return (9, (kind, values))


def compound(value):
    return (10, value)


def nbt_payload(kind, value):
    if kind == 2:
        return struct.pack(">h", value)
    if kind == 3:
        return struct.pack(">i", value)
    if kind == 8:
        encoded = value.encode("utf-8")
        return struct.pack(">H", len(encoded)) + encoded
    if kind == 9:
        element_kind, values = value
        return bytes([element_kind]) + struct.pack(">i", len(values)) + b"".join(
            nbt_payload(element_kind, entry) for entry in values)
    if kind == 10:
        return b"".join(bytes([entry[0]]) + nbt_payload(8, name)
                        + nbt_payload(*entry) for name, entry in value.items()) + b"\x00"
    raise ValueError(f"Unsupported NBT type {kind}")


def structure(hostile):
    palette = []
    blocks = {}

    def place(position, name, properties=None, entity=None):
        state = {"Name": string(name if ":" in name else "minecraft:" + name)}
        if properties:
            state["Properties"] = compound({key: string(value) for key, value in properties.items()})
        if state not in palette:
            palette.append(state)
        block = {"pos": list_tag(3, list(position)), "state": integer(palette.index(state))}
        if entity:
            block["nbt"] = compound(entity)
        blocks[position] = block

    for horizontal in range(11):
        for depth in range(11):
            for height in range(7):
                boundary = horizontal in (0, 10) or depth in (0, 10) or height in (0, 6)
                stone = "mossy_stone_bricks" if (horizontal + depth + height) % 4 == 0 else "stone_bricks"
                place((horizontal, height, depth), stone if boundary else "air")
    for step in range(6):
        place((2, step + 1, step + 1), "stone_brick_stairs", {"facing": "south", "half": "bottom", "shape": "straight", "waterlogged": "false"})
        for clear in range(step + 2, 8):
            place((2, clear, step + 1), "air")
    for horizontal, depth in [(1, 6), (3, 6)]:
        place((horizontal, 7, depth), "chiseled_stone_bricks")
        place((horizontal, 8, depth), "lantern", {"hanging": "false", "waterlogged": "false"})
    for horizontal, depth in [(8, 2), (8, 8)]:
        place((horizontal, 1, depth), "chest", {"facing": "west", "type": "single", "waterlogged": "false"},
              {"id": string("minecraft:chest"), "LootTable": string(f"{MOD}:chests/" + ("occult_cellar" if hostile else "herbalist_ruin"))})
    place((5, 1, 7), f"{MOD}:mystic_mortar")
    if hostile:
        place((5, 1, 4), "spawner", entity={"id": string("minecraft:mob_spawner"),
              "Delay": (2, 200), "SpawnData": compound({"entity": compound({"id": string("minecraft:zombie")})})})
        for position in [(4, 1, 3), (6, 1, 3), (6, 2, 6)]:
            place(position, "cobweb")
    else:
        for horizontal in (4, 6):
            for depth in (3, 4, 5):
                place((horizontal, 0, depth), "farmland", {"moisture": "7"})
                place((horizontal, 1, depth), f"{MOD}:" + ("spirit_herb_crop" if horizontal == 4 else "heather_crop"), {"age": "7"})
        place((5, 0, 4), "water", {"level": "0"})
        place((5, 4, 4), "lantern", {"hanging": "true", "waterlogged": "false"})
        place((5, 5, 4), "chain", {"axis": "y", "waterlogged": "false"})
    root = {"DataVersion": integer(3465), "size": list_tag(3, [11, 9, 11]),
            "palette": list_tag(10, palette), "blocks": list_tag(10, list(blocks.values())),
            "entities": list_tag(10, [])}
    return gzip.compress(bytes([10]) + nbt_payload(8, "") + nbt_payload(10, root), mtime=0)


def generate():
    config = json.loads(MANIFEST.read_text(encoding="utf-8"))
    output = {}

    def put(path, value):
        output[path] = (json.dumps(value, ensure_ascii=False, indent=2) + "\n").encode("utf-8")

    def data(path, value):
        put(DATA / MOD / path, value)

    def model(name, elements, textures, render=None):
        value = {"textures": textures, "elements": elements}
        if render:
            value["render_type"] = render
        put(ASSETS / "models/block" / f"{name}.json", value)

    def block(name, elements, textures):
        model(name, elements, textures)
        put(ASSETS / "blockstates" / f"{name}.json", {"variants": {"": {"model": f"{MOD}:block/{name}"}}})

    mineable = []
    for ore in config["ores"]:
        targets = []
        for deep in (False, True):
            name = ("deepslate_" if deep else "") + ore["id"] + "_ore"
            mineable.append(f"{MOD}:{name}")
            host = "minecraft:block/deepslate" if deep else "minecraft:block/stone"
            vein = "minecraft:block/calcite" if ore["id"] == "saltstone" else "minecraft:block/raw_iron_block"
            elements = [cube([0.05, 0.05, 0.05], [15.95, 15.95, 15.95], "#host")]
            for start, end in [([3, 5, 0], [7, 9, 16]), ([11, 10, 0], [14, 13, 16]),
                               ([0, 3, 4], [16, 6, 8]), ([5, 0, 10], [9, 16, 13])]:
                elements.append(cube(start, end, "#vein"))
            block(name, elements, {"host": host, "vein": vein, "particle": host})
            put(ASSETS / "models/item" / f"{name}.json", {"parent": f"{MOD}:block/{name}"})
            silk = {"condition": "minecraft:match_tool", "predicate": {"enchantments": [
                {"enchantment": "minecraft:silk_touch", "levels": {"min": 1}}]}}
            drops = loot(ore["drop"], functions=[{"function": "minecraft:apply_bonus",
                "enchantment": "minecraft:fortune", "formula": "minecraft:ore_drops"},
                {"function": "minecraft:explosion_decay"}])["entries"][0]
            data(f"loot_tables/blocks/{name}.json", {"type": "minecraft:block", "pools": [{
                "rolls": 1, "entries": [{"type": "minecraft:alternatives", "children": [
                    {"type": "minecraft:item", "name": f"{MOD}:{name}", "conditions": [silk]}, drops]}]}]})
            for method, duration in [("smelting", 200), ("blasting", 100)]:
                data(f"recipes/survival_{name}_{method}.json", {"type": f"minecraft:{method}",
                     "ingredient": {"item": f"{MOD}:{name}"}, "result": f"{MOD}:{ore['drop']}",
                     "experience": 0.3, "cookingtime": duration})
            targets.append({"target": {"predicate_type": "minecraft:tag_match",
                "tag": "minecraft:deepslate_ore_replaceables" if deep else "minecraft:stone_ore_replaceables"},
                "state": {"Name": f"{MOD}:{name}"}})
        data(f"worldgen/configured_feature/{ore['id']}.json", {"type": "minecraft:ore",
            "config": {"size": ore["size"], "discard_chance_on_air_exposure": 0.0, "targets": targets}})
        data(f"worldgen/placed_feature/{ore['id']}.json", {"feature": f"{MOD}:{ore['id']}", "placement": [
            {"type": "minecraft:count", "count": ore["attempts"]}, {"type": "minecraft:in_square"},
            {"type": "minecraft:height_range", "height": {"type": "minecraft:uniform",
                "min_inclusive": {"absolute": ore["min_y"]}, "max_inclusive": {"absolute": ore["max_y"]}}},
            {"type": "minecraft:biome"}]})
        data(f"forge/biome_modifier/{ore['id']}.json", {"type": "forge:add_features",
            "biomes": "#minecraft:is_overworld", "features": [f"{MOD}:{ore['id']}"], "step": "underground_ores"})
        for category in ("blocks", "items"):
            put(DATA / "forge/tags" / category / "ores" / f"{ore['id']}.json",
                {"replace": False, "values": [f"{MOD}:{ore['id']}_ore", f"{MOD}:deepslate_{ore['id']}_ore"]})
        put(DATA / MOD / "tags/items/reagents" / (ore["drop"] + ".json"),
            {"replace": False, "values": [f"{MOD}:{ore['drop']}"]})

    for plant in config["plants"]:
        name = plant["id"]
        for stage in range(4):
            elements = []
            height = (stage + 1) * 3 + 1
            for angle in (-45, 45):
                element = cube([2, 0, 8], [14, height, 8], "#plant")
                element["faces"] = {face: {"texture": "#plant", "uv": [0, 0, 16, 16]} for face in ("north", "south")}
                element["rotation"] = {"origin": [8, 0, 8], "axis": "y", "angle": angle, "rescale": True}
                elements.append(element)
            model(name + "_crop" + ("" if stage == 3 else f"_stage{stage}"), elements,
                  {"plant": f"{MOD}:item/{name}", "particle": f"{MOD}:item/{name}"}, "minecraft:cutout")
        put(ASSETS / "blockstates" / f"{name}_crop.json", {"variants": {
            f"age={age}": {"model": f"{MOD}:block/{name}_crop" + ("" if age == 7 else f"_stage{min(2, age // 2)}")}
            for age in range(8)}})
        model("wild_" + name, elements, {"plant": f"{MOD}:item/{name}", "particle": f"{MOD}:item/{name}"}, "minecraft:cutout")
        put(ASSETS / "blockstates" / f"wild_{name}.json", {"variants": {"": {"model": f"{MOD}:block/wild_{name}"}}})
        data(f"loot_tables/blocks/wild_{name}.json", {"type": "minecraft:block", "pools": [loot(name, conditions=[
            {"condition": "minecraft:survives_explosion"}])]})
        data(f"loot_tables/blocks/{name}_crop.json", {"type": "minecraft:block", "pools": [
            loot(name, conditions=[{"condition": "minecraft:survives_explosion"}]),
            loot(name, count=2, conditions=[{"condition": "minecraft:block_state_property",
                "block": f"{MOD}:{name}_crop", "properties": {"age": "7"}}],
                 functions=[{"function": "minecraft:explosion_decay"}])]})
        data(f"tags/worldgen/biome/has_{name}.json", {"replace": False, "values": plant["biomes"]})
        data(f"worldgen/configured_feature/wild_{name}.json", {"type": "minecraft:random_patch", "config": {
            "tries": 20, "xz_spread": 5, "y_spread": 2, "feature": {"feature": {"type": "minecraft:simple_block",
                "config": {"to_place": {"type": "minecraft:simple_state_provider", "state": {"Name": f"{MOD}:wild_{name}"}}}},
                "placement": [{"type": "minecraft:block_predicate_filter", "predicate": {"type": "minecraft:all_of",
                    "predicates": [{"type": "minecraft:matching_blocks", "blocks": ["minecraft:air"]},
                                   {"type": "minecraft:would_survive", "state": {"Name": f"{MOD}:wild_{name}"}}]}}]}}})
        data(f"worldgen/placed_feature/wild_{name}.json", {"feature": f"{MOD}:wild_{name}", "placement": [
            {"type": "minecraft:rarity_filter", "chance": plant["rarity"]}, {"type": "minecraft:in_square"},
            {"type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG"}, {"type": "minecraft:biome"}]})
        data(f"forge/biome_modifier/wild_{name}.json", {"type": "forge:add_features",
            "biomes": f"#{MOD}:has_{name}", "features": [f"{MOD}:wild_{name}"], "step": "vegetal_decoration"})

    for registry in ("blocks", "items"):
        put(DATA / f"forge/tags/{registry}/ores.json", {"replace": False,
            "values": [f"#forge:ores/{ore['id']}" for ore in config["ores"]]})

    mortar = [cube(start, end, "#stone") for start, end in [
        ([2, 0, 2], [14, 3, 14]), ([2, 3, 2], [4, 10, 14]), ([12, 3, 2], [14, 10, 14]),
        ([4, 3, 2], [12, 10, 4]), ([4, 3, 12], [12, 10, 14])]]
    block("mystic_mortar", mortar, {"stone": "minecraft:block/polished_deepslate", "particle": "minecraft:block/polished_deepslate"})
    put(ASSETS / "models/item/mystic_mortar.json", {"parent": f"{MOD}:block/mystic_mortar"})
    data("loot_tables/blocks/mystic_mortar.json", {"type": "minecraft:block", "pools": [loot("mystic_mortar",
        conditions=[{"condition": "minecraft:survives_explosion"}])]})
    mineable.append(f"{MOD}:mystic_mortar")
    mineable.extend(f"{MOD}:{name}" for name in ["crucible", "ritual_altar", "characteristic_separator", "imprint_washing_altar"])
    for tag in ("mineable/pickaxe", "needs_stone_tool"):
        put(DATA / f"minecraft/tags/blocks/{tag}.json", {"replace": False, "values": mineable})
    data("recipes/mystic_mortar.json", {"type": "minecraft:crafting_shaped", "pattern": ["S S", "SAS", " S "],
         "key": {"S": {"item": "minecraft:stone_bricks"}, "A": {"item": "minecraft:amethyst_shard"}},
         "result": {"item": f"{MOD}:mystic_mortar"}})
    for recipe in config["mortar"]:
        data(f"recipes/mortar_{recipe['id']}.json", {"type": f"{MOD}:mystic_mortar",
             "main": {"item": recipe["main"]}, "main_count": recipe["main_count"],
             "reagent": {"item": recipe["reagent"]}, "reagent_count": recipe["reagent_count"],
             "result": {"item": f"{MOD}:{recipe['result']}", "count": recipe["count"]}})
    data("recipes/campfire_herb_ash.json", {"type": "minecraft:campfire_cooking",
         "ingredient": {"item": f"{MOD}:spirit_herb"}, "result": f"{MOD}:ash_powder", "experience": 0.1, "cookingtime": 300})
    data("advancements/recipes/mystic_mortar.json", {"parent": "minecraft:recipes/root", "criteria": {
        "has_amethyst": {"trigger": "minecraft:inventory_changed", "conditions": {"items": [{"items": ["minecraft:amethyst_shard"]}]}}},
        "requirements": [["has_amethyst"]], "rewards": {"recipes": [f"{MOD}:mystic_mortar"]}})

    modifiers = []
    for drop in config["mob_drops"]:
        name = "survival_" + drop["entity"]
        modifiers.append(f"{MOD}:{name}")
        data(f"loot_modifiers/{name}.json", {"type": f"{MOD}:survival_supplies", "conditions": [
            {"condition": "forge:loot_table_id", "loot_table_id": f"minecraft:entities/{drop['entity']}"},
            {"condition": "minecraft:killed_by_player"}, {"condition": "minecraft:random_chance", "chance": drop["chance"]}],
            "item": f"{MOD}:{drop['item']}", "count": 1})
    for chest in config["chest_supplies"]:
        name = "survival_" + chest["table"]
        modifiers.append(f"{MOD}:{name}")
        data(f"loot_modifiers/{name}.json", {"type": f"{MOD}:survival_supplies", "conditions": [
            {"condition": "forge:loot_table_id", "loot_table_id": f"minecraft:chests/{chest['table']}"}],
            "item": f"{MOD}:{chest['item']}", "count": chest["count"]})
    fishing = config["fishing"]
    modifiers.append(f"{MOD}:survival_fishing")
    data("loot_modifiers/survival_fishing.json", {"type": f"{MOD}:survival_supplies", "conditions": [
        {"condition": "forge:loot_table_id", "loot_table_id": "minecraft:gameplay/fishing"},
        {"condition": "minecraft:entity_properties", "entity": "this", "predicate": {
            "type": "minecraft:fishing_bobber", "type_specific": {"type": "fishing_hook", "in_open_water": fishing["open_water"]}}},
        {"condition": "minecraft:match_tool", "predicate": {"items": ["minecraft:fishing_rod"]}},
        {"condition": "minecraft:random_chance", "chance": fishing["chance"]}],
        "item": f"{MOD}:{fishing['item']}", "count": fishing["count"]})
    put(DATA / "forge/loot_modifiers/global_loot_modifiers.json", {"replace": False, "entries": modifiers})

    for discovery in config["discoveries"]:
        display = {"icon": {"item": f"{MOD}:{discovery['item']}"},
                   "title": {"translate": f"advancement.{MOD}.survival.{discovery['id']}.title"},
                   "description": {"translate": f"advancement.{MOD}.survival.{discovery['id']}.description"},
                   "frame": "task", "show_toast": True, "announce_to_chat": False, "hidden": False}
        if "loot_table" in discovery:
            criterion = {"trigger": "minecraft:player_generates_container_loot", "conditions": {
                "loot_table": f"{MOD}:chests/{discovery['loot_table']}"}}
        else:
            criterion = {"trigger": "minecraft:inventory_changed", "conditions": {
                "items": [{"items": [f"{MOD}:{discovery['item']}"]}]}}
        advancement = {"display": display, "criteria": {"discover": criterion}, "requirements": [["discover"]]}
        if "parent" in discovery:
            advancement["parent"] = f"{MOD}:survival/{discovery['parent']}"
        else:
            display["background"] = "minecraft:textures/block/deepslate_tiles.png"
        data(f"advancements/survival/{discovery['id']}.json", advancement)

    for ruin in config["structures"]:
        name = ruin["id"]
        data(f"tags/worldgen/biome/has_structure/{name}.json", {"replace": False,
             "values": ["minecraft:plains", "minecraft:sunflower_plains", "minecraft:forest", "minecraft:birch_forest", "minecraft:taiga"]})
        data(f"worldgen/structure/{name}.json", {"type": "minecraft:jigsaw", "biomes": f"#{MOD}:has_structure/{name}",
             "step": "surface_structures", "spawn_overrides": {}, "terrain_adaptation": "beard_thin",
             "start_pool": f"{MOD}:{name}", "size": 1, "start_height": {"absolute": -6},
             "project_start_to_heightmap": "WORLD_SURFACE_WG", "max_distance_from_center": 32,
             "use_expansion_hack": False})
        data(f"worldgen/structure_set/{name}.json", {"structures": [{"structure": f"{MOD}:{name}", "weight": 1}],
             "placement": {"type": "minecraft:random_spread", "spacing": ruin["spacing"],
                           "separation": ruin["separation"], "salt": ruin["salt"]}})
        data(f"worldgen/template_pool/{name}.json", {"name": f"{MOD}:{name}", "fallback": "minecraft:empty",
             "elements": [{"weight": 1, "element": {"element_type": "minecraft:single_pool_element",
                "location": f"{MOD}:{name}", "processors": "minecraft:empty", "projection": "rigid"}}]})
        output[DATA / MOD / "structures" / f"{name}.nbt"] = structure(ruin["hostile"])
        entries = [loot("spirit_herb", 2), loot("heather", 2), loot("spirit_salt", 3)]
        extras = ["minecraft:bread", "minecraft:iron_ingot", f"{MOD}:silver_filings", f"{MOD}:white_candle",
                  f"{MOD}:pure_water", f"{MOD}:calming_incense"]
        if ruin["hostile"]:
            extras += [f"{MOD}:divination_crystal", f"{MOD}:black_wax", f"{MOD}:ashen_thread"]
        entries.append({"rolls": {"type": "minecraft:uniform", "min": 2, "max": 4},
                        "entries": [loot(item)["entries"][0] for item in extras]})
        data(f"loot_tables/chests/{name}.json", {"type": "minecraft:chest", "pools": entries})
    return output


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true")
    arguments = parser.parse_args()
    expected = generate()
    drift = []
    for path, payload in expected.items():
        if arguments.check:
            if not path.is_file() or path.read_bytes() != payload:
                drift.append(str(path.relative_to(ROOT)))
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_bytes(payload)
    if drift:
        raise SystemExit("Survival generated content drift:\n" + "\n".join(drift))
    print(f"Survival content {'checked' if arguments.check else 'generated'}: {len(expected)} files")


if __name__ == "__main__":
    main()
