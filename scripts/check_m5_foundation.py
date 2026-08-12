#!/usr/bin/env python3

import json
import re
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
CONTRACT = ROOT / "docs" / "m5-foundation-contract.json"
JAVA = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries"
TESTS = ROOT / "src" / "test" / "java" / "top" / "aurora" / "lordofmysteries"
DATA = ROOT / "src" / "main" / "resources" / "data" / "lord_of_mysteries"
ASSETS = ROOT / "src" / "main" / "resources" / "assets" / "lord_of_mysteries"


def load(path):
    return json.loads(path.read_text(encoding="utf-8"))


def source(path):
    return path.read_text(encoding="utf-8")


def require(condition, message):
    if not condition:
        raise SystemExit(f"M5 foundation contract failed: {message}")


def enum_ids(path):
    return set(re.findall(r'\("([a-z0-9_]+)"(?:,|\))', source(path)))


def main():
    contract = load(CONTRACT)
    require(contract.get("schema_version") == 1,
            "unsupported contract schema")
    expedition = contract["expedition"]
    spirit = JAVA / "spirit"
    policy = source(spirit / "SpiritExpeditionPolicy.java")
    normalized_policy = policy.replace("_", "")
    saved = source(spirit / "SpiritExpeditionSavedData.java")
    service = source(spirit / "SpiritExpeditionService.java")
    builder = source(spirit / "SpiritExpeditionWorldBuilder.java")
    commands = source(JAVA / "command" / "ProjectMysteryCommands.java")
    items = source(JAVA / "registry" / "ModItems.java")

    constants = {
        "DURATION_TICKS": expedition["duration_ticks"],
        "ROUTE_LEGS": expedition["route_legs"],
        "STARTING_STABILITY": expedition["starting_stability"],
        "MAX_STABILITY": expedition["max_stability"],
        "MAX_DRIFT": expedition["max_drift"],
    }
    for name, value in constants.items():
        require(re.search(
                    rf"{name.replace('_', '')}\s*=\s*{value}L?\s*;",
                    normalized_policy),
                f"{name} drifted")
    require(expedition["persistent_data_name"] in saved,
            "SavedData name drifted")
    require(f'SCHEMA_VERSION = {expedition["schema_version"]}' in saved,
            "SavedData schema drifted")
    require('Map<UUID, Expedition>' in saved
            and 'putIfAbsent' in saved
            and 'allocateLane' in saved,
            "player isolation or duplicate protection is missing")
    require('"orphaned_entries"' in saved
            and 'orphanedEntries.add' in saved,
            "malformed SavedData quarantine is missing")

    enum_contracts = {
        "SpiritProjection.java": set(expedition["projections"]),
        "SpiritDirection.java": set(expedition["directions"]),
        "SpiritWeather.java": set(expedition["weather"]),
        "SpiritEncounter.java": set(expedition["encounters"]),
    }
    for filename, expected in enum_contracts.items():
        require(enum_ids(spirit / filename) == expected,
                f"{filename} ids drifted")
    encounter_source = source(spirit / "SpiritEncounter.java")
    for encounter, action in expedition["encounters"].items():
        require(re.search(
                    rf'\("{encounter}",\s*SpiritEncounterAction\.{action.upper()}',
                    encounter_source),
                f"{encounter} response drifted")

    dimension = load(DATA / "dimension" / "spirit_world.json")
    dimension_type = load(DATA / "dimension_type" / "spirit_world.json")
    require(dimension.get("type") == expedition["dimension"],
            "Spirit World dimension type binding drifted")
    require(dimension.get("generator", {}).get("type") == "minecraft:flat"
            and dimension.get("generator", {}).get("settings", {}).get("biome")
            == "minecraft:the_void",
            "Spirit World must remain a semantic projection, not copied chunks")
    require(not dimension_type.get("natural")
            and not dimension_type.get("bed_works")
            and not dimension_type.get("respawn_anchor_works"),
            "Spirit World respawn safety drifted")
    require("isInWorldBounds" in builder
            and "getWorldBorder().isWithinBounds" in builder
            and "getChunkAt" in builder,
            "route node destination validation is incomplete")

    for command in expedition["commands"]:
        require(f'literal("{command}")' in commands,
                f"command {command} is missing")
    pages = source(ROOT / "docs" / "assets" / "catalog-data.js")
    translations = {
        locale: load(ASSETS / "lang" / f"{locale}.json")
        for locale in ("zh_cn", "en_us")
    }
    for item in expedition["items"]:
        require(f'"{item}"' in items,
                f"item registration {item} is missing")
        require((ASSETS / "models" / "item" / f"{item}.json").exists(),
                f"item model {item} is missing")
        key = f"item.lord_of_mysteries.{item}"
        for locale, language in translations.items():
            require(key in language, f"{locale} misses {key}")
        require(f'"id": "lord_of_mysteries:{item}"' in pages,
                f"Pages catalog misses {item}")
    for recipe in expedition["recipes"]:
        path = DATA / "recipes" / f"{recipe}.json"
        require(path.exists(), f"recipe {recipe} is missing")
        require(load(path).get("result", {}).get("item")
                == f"lord_of_mysteries:{recipe}",
                f"recipe result {recipe} drifted")
    for recipe in expedition["reward_recycling_recipes"]:
        require((DATA / "recipes" / f"{recipe}.json").exists(),
                f"reward recycling recipe {recipe} is missing")
    weather_profiles = {
        value["id"]: value
        for value in (
            load(path) for path in sorted(
                (DATA / "spirit_weather").glob("*.json")))
    }
    encounter_profiles = {
        value["id"]: value
        for value in (
            load(path) for path in sorted(
                (DATA / "spirit_encounters").glob("*.json")))
    }
    require(set(weather_profiles) == set(expedition["weather"]),
            "weather data profiles drifted")
    require(set(encounter_profiles) == set(expedition["encounters"]),
            "encounter data profiles drifted")
    for weather, profile in weather_profiles.items():
        require(profile.get("implementation_state") == "playable"
                and 1 <= profile.get("risk", 0) <= 3
                and profile.get("runtime_effect")
                and profile.get("navigation_rule")
                and profile.get("degradation"),
                f"weather profile {weather} is incomplete")
    for encounter, preferred in expedition["encounters"].items():
        profile = encounter_profiles[encounter]
        require(profile.get("implementation_state")
                == "playable_route_profile"
                and profile.get("preferred_action") == preferred
                and 1 <= profile.get("risk", 0) <= 3
                and profile.get("ecology_role")
                and profile.get("reward"),
                f"encounter profile {encounter} is incomplete")

    safety = contract["safety"]
    anchors = {
        "server_authoritative": "ServerPlayer",
        "player_isolated_lanes": "allocateLane",
        "destination_validated_before_state_change":
            "destination == null || !teleport",
        "entry_failure_has_no_cost": "data.remove(player.getUUID())",
        "stabilize_failure_has_no_cost": "stabilize_missing",
        "exit_removes_state_after_teleport": "data.remove(player.getUUID())",
        "timeout_safe_return": "ExitReason.TIMEOUT",
        "login_resume": "onLogin",
        "void_recovery": "player.getY() < 20d",
        "spirit_death_is_not_real_death": "event.setCanceled(true)",
        "orphaned_nbt_quarantine": "orphanedEntries.add",
        "overflow_saturation": "saturatedAdd",
        "incorrect_navigation_does_not_advance":
            "step + (outcome.correct() ? 1 : 0)",
        "future_schema_read_only": "futureSnapshot != null",
        "capacity_exhaustion_safe_failure": "return -1",
        "origin_validated_before_chunk_load":
            "origin.isInWorldBounds(expedition.origin())",
        "wrong_dimension_cannot_complete":
            "exitInternal(player, ExitReason.INTERRUPTED)",
    }
    combined = "\n".join((policy, saved, service, builder))
    for rule, enabled in safety.items():
        require(not enabled or anchors[rule] in combined,
                f"safety rule {rule} is missing")

    validation = contract["validation"]
    require(
        f'"spiritWeatherDefinitions": {validation["pages_weather_entries"]}'
        in pages,
        "Pages weather metadata drifted")
    require(
        f'"spiritEncounterDefinitions": {validation["pages_encounter_entries"]}'
        in pages,
        "Pages encounter metadata drifted")
    for profile_id in expedition["weather"]:
        require(
            f'"id": "lord_of_mysteries:spirit_weather/{profile_id}"'
            in pages,
            f"Pages misses weather {profile_id}")
    for profile_id in expedition["encounters"]:
        require(
            f'"id": "lord_of_mysteries:spirit_encounter/{profile_id}"'
            in pages,
            f"Pages misses encounter {profile_id}")
    test_sources = "\n".join(
        source(path) for path in TESTS.rglob("*.java"))
    require(test_sources.count("@Test") == validation["junit_tests"],
            "JUnit test count drifted")
    for test_class in validation["behavior_test_classes"]:
        require(f"class {test_class}" in test_sources,
                f"behavior test {test_class} is missing")
    game_tests = source(
        JAVA / "gametest" / "PlayerPersistenceGameTests.java")
    require(game_tests.count("@GameTest") == validation["game_tests"],
            "GameTest count drifted")
    for method in validation["game_test_methods"]:
        require(f"void {method}" in game_tests,
                f"GameTest {method} is missing")

    print(
        "M5 foundation contract checked: "
        f"{len(expedition['projections'])} projections, "
        f"{len(expedition['weather'])} weather profiles, "
        f"{len(expedition['encounters'])} encounter profiles, "
        f"{expedition['route_legs']} route legs, persistent recovery, "
        "bilingual resources, and synchronized Pages"
    )


if __name__ == "__main__":
    main()
