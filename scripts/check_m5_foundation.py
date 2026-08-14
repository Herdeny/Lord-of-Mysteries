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
    entities = source(JAVA / "registry" / "ModEntities.java")
    artifact_service = source(
        JAVA / "artifact" / "SealedArtifactService.java")
    dream = JAVA / "dream"
    dream_policy = source(dream / "SharedDreamPolicy.java")
    normalized_dream_policy = dream_policy.replace("_", "")
    dream_saved = source(dream / "SharedDreamSavedData.java")
    dream_service = source(dream / "SharedDreamService.java")
    dream_builder = source(dream / "SharedDreamWorldBuilder.java")
    ecology_entity = source(spirit / "SpiritEcologyEntity.java")
    activity_policy = source(JAVA / "player" / "OccultActivityPolicy.java")
    activity_service = source(JAVA / "player" / "OccultActivityService.java")
    travel_service = source(JAVA / "ability" / "TravelMarkerService.java")
    door_entity = source(JAVA / "entity" / "TravelerDoorEntity.java")
    ritual_altar = source(JAVA / "ritual" / "RitualAltarBlockEntity.java")
    sequence_five_ritual = source(
        JAVA / "ritual" / "SequenceFiveAdvancementRitual.java")
    marionette_scroll = source(
        JAVA / "ability" / "MarionetteScrollItem.java")

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

    ecology = contract["physical_ecology"]
    require(enum_ids(spirit / "SpiritEcologyKind.java")
            == set(ecology["entities"]),
            "physical ecology ids drifted")
    for entity in ecology["entities"]:
        require(f'spiritEcology("{entity}")' in entities,
                f"entity registration {entity} is missing")
        require(f'spiritEcologyEgg("{entity}"' in items,
                f"spawn egg {entity} is missing")
        require((ASSETS / "models" / "item"
                 / f"{entity}_spawn_egg.json").exists(),
                f"spawn egg model {entity} is missing")
        for locale, language in translations.items():
            require(f"entity.lord_of_mysteries.{entity}" in language,
                    f"{locale} misses ecology entity {entity}")
            require(f"item.lord_of_mysteries.{entity}_spawn_egg" in language,
                    f"{locale} misses ecology spawn egg {entity}")
        require(f'"id": "lord_of_mysteries:{entity}"' in pages,
                f"Pages catalog misses entity {entity}")
        require(f'"id": "lord_of_mysteries:{entity}_spawn_egg"' in pages,
                f"Pages catalog misses spawn egg {entity}")
    require("belongsTo" in ecology_entity
            and "resolveEncounter" in ecology_entity
            and "routeOwner == null" in ecology_entity,
            "owned and creative ecology behavior is incomplete")
    require("spawnEncounterEntity" in service
            and "removeEncounterEntities" in service
            and "restrictTo" in service,
            "route ecology lifecycle is incomplete")

    shared_dream = contract["shared_dream"]
    dream_constants = {
        "MAX_PARTICIPANTS": shared_dream["max_participants"],
        "TRUSTED_REPUTATION": shared_dream["trusted_reputation"],
        "SYMBOL_STEPS": shared_dream["symbol_steps"],
        "STARTING_COHERENCE": shared_dream["starting_coherence"],
        "MAX_COHERENCE": shared_dream["max_coherence"],
        "MAX_TRAUMA": shared_dream["max_trauma"],
        "INVITE_TTL_TICKS": shared_dream["invite_ttl_ticks"],
        "SESSION_TTL_TICKS": shared_dream["session_ttl_ticks"],
    }
    for name, value in dream_constants.items():
        require(re.search(
                    rf"{name.replace('_', '')}\s*=\s*{value}L?\s*;",
                    normalized_dream_policy),
                f"shared dream {name} drifted")
    require(shared_dream["persistent_data_name"] in dream_saved,
            "shared dream SavedData name drifted")
    require(f'SCHEMA_VERSION = {shared_dream["schema_version"]}'
            in dream_saved,
            "shared dream SavedData schema drifted")
    dream_enums = {
        "DreamScenario.java": set(shared_dream["scenarios"]),
        "DreamSymbol.java": set(shared_dream["symbols"]),
        "DreamAction.java": set(shared_dream["actions"]),
    }
    for filename, expected in dream_enums.items():
        require(enum_ids(dream / filename) == expected,
                f"{filename} ids drifted")
    dream_dimension = load(DATA / "dimension" / "shared_dream.json")
    dream_dimension_type = load(
        DATA / "dimension_type" / "shared_dream.json")
    require(dream_dimension.get("type") == shared_dream["dimension"],
            "shared dream dimension type binding drifted")
    require(dream_dimension.get("generator", {}).get("type")
            == "minecraft:flat"
            and dream_dimension.get("generator", {}).get(
                "settings", {}).get("biome") == "minecraft:the_void",
            "shared dream must remain a semantic scene")
    require(not dream_dimension_type.get("natural")
            and not dream_dimension_type.get("bed_works")
            and not dream_dimension_type.get("respawn_anchor_works"),
            "shared dream respawn safety drifted")
    require("isInWorldBounds" in dream_builder
            and "getWorldBorder().isWithinBounds" in dream_builder
            and "getChunkAt" in dream_builder,
            "shared dream destination validation is incomplete")
    for command in shared_dream["commands"]:
        require(f'literal("{command}")' in commands,
                f"shared dream command {command} is missing")
    for item in (shared_dream["ritual_items"]
                 + shared_dream["reward_and_recovery_items"]):
        require(f'"{item}"' in items,
                f"shared dream item {item} is missing")
        require((ASSETS / "models" / "item" / f"{item}.json").exists(),
                f"shared dream model {item} is missing")
        for locale, language in translations.items():
            require(f"item.lord_of_mysteries.{item}" in language,
                    f"{locale} misses shared dream item {item}")
        require(f'"id": "lord_of_mysteries:{item}"' in pages,
                f"Pages catalog misses shared dream item {item}")
    for recipe in shared_dream["recipes"]:
        require((DATA / "recipes" / f"{recipe}.json").exists(),
                f"shared dream recipe {recipe} is missing")
    require("hasUsableSleepingBell" in artifact_service
            and "useSleepingBellAsDreamAnchor" in artifact_service
            and "OrganizationActionSavedData" in dream_service
            and "OrganizationActionType.HERESY_REVIEW" in dream_service
            and "OrganizationActionType.HIGH_COUNCIL" in dream_service,
            "artifact or organization shared dream integration is incomplete")

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
        "dream_explicit_consent": "allAccepted()",
        "dream_decline_and_timeout_have_no_cost":
            "DreamCloseReason.DECLINED",
        "dream_no_teleport_before_all_accept": "!session.allAccepted()",
        "dream_disconnect_fail_closed": "DreamCloseReason.DISCONNECTED",
        "dream_wrong_dimension_fail_closed":
            "DreamCloseReason.WRONG_DIMENSION",
        "dream_death_is_not_real_death": "DreamCloseReason.DREAM_DEATH",
        "dream_team_change_fail_closed": "DreamCloseReason.TEAM_CHANGED",
        "dream_organization_access_fail_closed":
            "DreamCloseReason.ORGANIZATION_ACCESS_LOST",
        "dream_per_player_origin_recovery":
            "Map<UUID, SharedDreamSavedData.Origin> origins",
        "dream_duplicate_recovery_refused":
            "recoveries.containsKey(player)",
        "dream_future_schema_read_only": "futureSnapshot != null",
        "dream_malformed_session_quarantine": "orphanedEntries.add",
        "dream_materials_consumed_after_activation": "consumeRitualKit(host)",
        "cross_system_activity_mutual_exclusion":
            "OccultActivityPolicy.classify",
        "protected_dimensions_reject_traveler_doors":
            "isProtectedDimension",
        "ritual_leader_eligibility_rechecked":
            "leaderPlayer.distanceToSqr",
        "marionette_activity_conflicts_preserve_state":
            "DeployResult.ACTIVITY_CONFLICT",
        "player_activity_diagnostics": 'literal("activity")',
    }
    combined = "\n".join((
        policy, saved, service, builder,
        dream_policy, dream_saved, dream_service, dream_builder,
        activity_policy, activity_service, commands, travel_service,
        door_entity, ritual_altar, sequence_five_ritual,
        marionette_scroll,
    ))
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
    require(
        f'"registeredItems": {validation["pages_registered_items"]}'
        in pages,
        "Pages item metadata drifted")
    require(
        f'"registeredEntities": {validation["pages_registered_entities"]}'
        in pages,
        "Pages entity metadata drifted")
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
        f"{len(ecology['entities'])} physical ecology entities, "
        f"{len(shared_dream['scenarios'])} shared dream scenarios, "
        f"{expedition['route_legs']} route legs, persistent recovery, "
        "explicit consent, bilingual resources, and synchronized Pages"
    )


if __name__ == "__main__":
    main()
