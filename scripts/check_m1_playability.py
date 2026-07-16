#!/usr/bin/env python3

import json
import re
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
CONTRACT = ROOT / "docs" / "m1-playability-contract.json"
DATA = ROOT / "src" / "main" / "resources" / "data" / "lord_of_mysteries"
ASSETS = ROOT / "src" / "main" / "resources" / "assets" / "lord_of_mysteries"
STARTER_LOOT = DATA / "loot_tables" / "chests" / "starter_investigator_supplies.json"
COMMANDS = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "command" / "ProjectMysteryCommands.java"
GENERATOR = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "world" / "AbandonedCampGenerator.java"
TIMELINE = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "knowledge" / "M1TrialTimeline.java"


def load(path):
    with path.open(encoding="utf-8") as handle:
        return json.load(handle)


def collect_item_names(value):
    names = set()
    if isinstance(value, dict):
        if value.get("type") == "minecraft:item" and isinstance(value.get("name"), str):
            names.add(value["name"])
        for nested in value.values():
            names.update(collect_item_names(nested))
    elif isinstance(value, list):
        for nested in value:
            names.update(collect_item_names(nested))
    return names


def collect_guaranteed_items(loot_table):
    guaranteed = set()
    for pool in loot_table.get("pools", []):
        entries = pool.get("entries", [])
        if (pool.get("rolls") != 1 or pool.get("conditions") or len(entries) != 1):
            continue
        entry = entries[0]
        if entry.get("type") == "minecraft:item" and not entry.get("conditions"):
            guaranteed.add(entry.get("name"))
    return guaranteed


def timeline_targets():
    source = TIMELINE.read_text(encoding="utf-8")
    names = {
        "camp": "CAMP_TARGET_TICKS",
        "sequence_9": "SEQUENCE_9_TARGET_TICKS",
        "sequence_8": "SEQUENCE_8_TARGET_TICKS",
        "sequence_7": "SEQUENCE_7_TARGET_TICKS",
    }
    targets = {}
    for contract_name, constant_name in names.items():
        match = re.search(rf"{constant_name}\s*=\s*(\d+)L", source)
        require(match is not None, f"missing Java timeline constant {constant_name}")
        targets[contract_name] = int(match.group(1))
    return targets


def require(condition, message):
    if not condition:
        raise SystemExit(f"M1 playability contract failed: {message}")


def main():
    contract = load(CONTRACT)
    require(contract.get("schema_version") == 1, "unsupported contract schema")

    target_values = list(contract["target_minutes"].values())
    require(target_values == sorted(target_values), "timing targets must be ordered")
    require(target_values[-1] <= 60, "vertical slice target exceeds one hour")
    expected_ticks = {
        name: minutes * 60 * 20
        for name, minutes in contract["target_minutes"].items()
    }
    require(timeline_targets() == expected_ticks,
            "Java timeline targets drifted from the playability contract")

    starter_loot = load(STARTER_LOOT)
    starter_items = collect_item_names(starter_loot)
    guaranteed_items = collect_guaranteed_items(starter_loot)
    required_starter = set(contract["starter_cache_items"])
    require(required_starter <= guaranteed_items,
            "starter cache does not guarantee "
            f"{sorted(required_starter - guaranteed_items)}")

    for stage in contract["route"]:
        potion_path = DATA / "potions" / f"{stage['potion']}.json"
        potion = load(potion_path)
        required_inputs = {
            ingredient["item"]
            for ingredient in potion["ingredients"]
            if ingredient.get("required", False)
        }
        require(required_inputs == set(stage["required_inputs"]),
                f"{stage['potion']} required inputs drifted")
        require(potion["target_sequence"] == stage["sequence"],
                f"{stage['potion']} targets the wrong sequence")

    sequence_seven = contract["route"][-1]
    require(sequence_seven["field_acquired"] not in starter_items,
            "starter cache must not bypass the sequence 7 hunt")

    command_source = COMMANDS.read_text(encoding="utf-8")
    for command in contract["commands"]:
        require(f'literal("{command}")' in command_source,
                f"missing /pm command token {command}")

    generator_source = GENERATOR.read_text(encoding="utf-8")
    require("starter_investigator_supplies" in generator_source,
            "starter loot table is not wired to camp generation")

    for language in ("en_us", "zh_cn"):
        translations = load(ASSETS / "lang" / f"{language}.json")
        missing = set(contract["translation_keys"]) - translations.keys()
        require(not missing, f"{language} misses translations {sorted(missing)}")

    advancement = DATA / "advancements" / "reach_investigator_camp.json"
    require(advancement.exists(), "camp arrival advancement is missing")
    print(
        "M1 playability contract checked: "
        f"{len(contract['route'])} sequences, "
        f"{len(required_starter)} guaranteed supplies, "
        f"{len(contract['commands'])} command tokens"
    )


if __name__ == "__main__":
    main()
