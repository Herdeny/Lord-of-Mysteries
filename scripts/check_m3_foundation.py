#!/usr/bin/env python3

import json
import re
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
CONTRACT = ROOT / "docs" / "m3-foundation-contract.json"
JAVA = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries"
TESTS = ROOT / "src" / "test" / "java" / "top" / "aurora" / "lordofmysteries"
LANG = ROOT / "src" / "main" / "resources" / "assets" / "lord_of_mysteries" / "lang"
DATA = ROOT / "src" / "main" / "resources" / "data" / "lord_of_mysteries"


def load(path):
    return json.loads(path.read_text(encoding="utf-8"))


def source(path):
    return path.read_text(encoding="utf-8")


def require(condition, message):
    if not condition:
        raise SystemExit(f"M3 foundation contract failed: {message}")


def main():
    contract = load(CONTRACT)
    require(contract.get("schema_version") == 1,
            "unsupported contract schema")

    player = source(JAVA / "player" / "PlayerMysteryData.java")
    fixer = source(JAVA / "player" / "PlayerMysteryDataFixer.java")
    sanitizer = source(JAVA / "player" / "PlayerMysteryDataSanitizer.java")
    schema = re.search(r"CURRENT_SCHEMA_VERSION\s*=\s*(\d+)", player)
    require(schema and int(schema.group(1)) == contract["capability_schema"],
            "capability schema drifted")

    economy = contract["economy"]
    policy = source(JAVA / "commission" / "CityEconomyPolicy.java")
    city_life = source(JAVA / "commission" / "CityLifeService.java")
    commands = source(JAVA / "command" / "ProjectMysteryCommands.java")
    for job, terms in economy["jobs"].items():
        signature = (
            f"case {job} -> new ShiftTerms("
            f"{terms['reward_pence']}L, {terms['paper_cost']}, "
            f"{terms['pressure_increase']}f, "
            f"{terms['exposure_reduction']}f)")
        require(signature in policy, f"{job} base terms drifted")
        require(f"case {job}" in city_life,
                f"{job} is not connected to city work")
    require(not economy["shared_daily_limit"]
            or ("canWork(data.lastCityWorkDay, day)" in city_life
                and "data.lastCityWorkDay = day" in city_life),
            "city jobs no longer share one daily limit")
    require(not economy["requires_matching_district"]
            or ("isNearDistrict" in city_life
                and "MistCityDistrictLayout.servicePosition" in city_life),
            "city jobs no longer require their physical districts")
    require(not economy["event_sensitive_rewards"]
            or ("terms(job, worldEvent)" in city_life
                and "case WITCH_HUNT_NIGHT" in policy),
            "city job rewards are no longer event-sensitive")
    for key in economy["persistent_counters"]:
        require(f'"{key}"' in player,
                f"persistent economy counter {key} is missing")
    for token in economy["command"]:
        require(f'literal("{token}")' in commands,
                f"city economy command {token} is missing")

    exposure = contract["exposure"]
    exposure_policy = source(
        JAVA / "commission" / "MysticalExposurePolicy.java")
    require(f'"{exposure["nbt_key"]}"' in player,
            "mystical exposure is not persisted")
    require(exposure["migration_fix"] in fixer
            and str(contract["capability_schema"]) in fixer,
            "mystical exposure migration is missing")
    require("MysticalExposurePolicy.adjust" in sanitizer,
            "mystical exposure is not sanitized")
    require(f"Math.max({exposure['minimum']}f, Math.min("
            f"{exposure['maximum']}f" in exposure_policy,
            "mystical exposure bounds drifted")
    for grade, delta in exposure["grade_deltas"].items():
        require(f"case {grade} -> {delta}" in exposure_policy,
                f"case exposure delta {grade} drifted")
    for band in exposure["bands"]:
        require(band in exposure_policy,
                f"exposure band {band} is missing")

    events = contract["world_events"]
    event_enum = source(JAVA / "world" / "MistCityWorldEvent.java")
    event_policy = source(JAVA / "world" / "MistCityWorldEventPolicy.java")
    event_saved = source(
        JAVA / "world" / "MistCityWorldEventSavedData.java")
    event_handler = source(
        JAVA / "world" / "MistCityWorldEventHandler.java")
    event_modifiers = source(
        JAVA / "world" / "MistCityWorldEventModifiers.java")
    event_player_effects = source(
        JAVA / "world" / "MistCityWorldEventPlayerEffects.java")
    news_service = source(JAVA / "commission" / "CityNewsService.java")
    news_logic = source(JAVA / "commission" / "CityNewsLogic.java")
    diagnostics = source(JAVA / "command" / "ProjectMysteryCommands.java")
    require(f"CYCLE_DAYS = {events['cycle_days']}" in event_policy,
            "world event cycle duration drifted")
    for event in events["events"]:
        require(event in event_enum and event in event_policy,
                f"world event {event} is not scheduled")
    require(not events["persistent"]
            or (f'"{events["data_name"]}"' in event_saved
                and '"current_day"' in event_saved
                and '"current_event"' in event_saved
                and "setDirty()" in event_saved),
            "world event state is not restart-persistent")
    require(not events["broadcasts_changes"]
            or ("world_event.changed" in event_handler
                and "getPlayerList().getPlayers()" in event_handler),
            "world event changes are not broadcast")
    require(not events["shown_in_newspaper"]
            or ("worldEvent.translationKey()" in news_logic
                and "issue.worldEventKey()" in news_service),
            "newspaper no longer shows world events")
    require(not events["shown_in_server_diagnostics"]
            or '" world_event="' in diagnostics,
            "server diagnostics no longer show world events")
    modifiers = events["modifiers"]
    require(not modifiers["economy_all_events"]
            or all(f"case {event}" in policy for event in events["events"]),
            "not every world event modifies city economy")
    require(f"? {modifiers['spiritual_surge_regen_multiplier']}f"
            in event_modifiers,
            "spiritual surge regeneration modifier drifted")
    require(f"? {modifiers['ritual_resonance_completion_bonus']:.2f}f"
            in event_modifiers,
            "ritual resonance completion modifier drifted")
    require(not modifiers["dense_fog_outdoor_darkness"]
            or ("obscuresOutdoorVision" in event_player_effects
                and "MobEffects.DARKNESS" in event_handler
                and "level.canSeeSky" in event_handler),
            "dense fog no longer obscures outdoor vision")
    for key, event, token in (
            ("evil_gaze_pressure_per_minute", "EVIL_GAZE", "0.2f"),
            ("blood_moon_pressure_per_minute", "BLOOD_MOON", "0.4f")):
        require(f"case {event} -> new MinuteEffect({token}, 0f)"
                in event_player_effects
                and modifiers[key] == float(token[:-1]),
                f"{event} minute pressure drifted")
    require(
        f"new MinuteEffect("
        f"{modifiers['witch_hunt_pressure_per_minute']}f, "
        f"{modifiers['witch_hunt_exposure_per_minute']}f)"
        in event_player_effects,
        "witch-hunt minute consequences drifted")
    require(not modifiers["witch_hunt_commoner_exemption"]
            or "if (!beyonder) return MinuteEffect.NONE"
            in event_player_effects,
            "witch-hunt commoner exemption is missing")
    require(not modifiers["witch_hunt_outpost_shelter"]
            or ("isSheltered" in event_handler
                and "MistCityOutpostSavedData.get" in event_handler
                and "sheltered" in event_player_effects),
            "witch-hunt outpost shelter is missing")

    launch = contract["launch_pathways"]
    require(launch["sequences"] == [6, 5],
            "M3 launch sequence scope drifted")
    items = source(JAVA / "registry" / "ModItems.java")
    crucible = source(JAVA / "potion" / "CrucibleRecipeLogic.java")
    handler = source(JAVA / "ability" / "M3LaunchAbilityHandler.java")
    ability_router = source(
        JAVA / "ability" / "M2FoundationAbilityHandler.java")
    logic = source(JAVA / "ability" / "M3LaunchAbilityLogic.java")
    acting = source(JAVA / "acting" / "ActingEvent.java")
    require(all(slot in ability_router
                for slot in launch["server_authoritative_slots"])
            and "M3LaunchAbilityHandler.use(player, slot)" in ability_router,
            "M3 ability slots are not server-routed")
    require(not launch["player_control_forbidden"]
            or ("target instanceof Player" in handler
                and "canControl" in handler
                and "!playerTarget" in logic),
            "M3 control abilities no longer exclude players")
    potion_count = 0
    acting_count = 0
    for pathway, terms in launch["pathways"].items():
        for sequence in launch["sequences"]:
            potion_name = f"{pathway.upper()}_POTION_{sequence}"
            brewed_name = f"{pathway.upper()}_{sequence}"
            require(potion_name in items,
                    f"{pathway} sequence {sequence} potion is not registered")
            require(brewed_name in crucible,
                    f"{pathway} sequence {sequence} has no crucible result")
            potion_count += 1
            definition = load(
                DATA / "sequences" / f"{pathway}_{sequence}.json")
            require(definition["implementation_state"] == "code_ready",
                    f"{pathway} sequence {sequence} state is overstated")
            require(definition["spirituality_max"]
                    == terms["spirituality"][str(sequence)],
                    f"{pathway} sequence {sequence} spirituality drifted")
            pressure = definition.get(
                "potion_pressure", definition.get("initial_pressure"))
            require(pressure == terms["pressure"][str(sequence)],
                    f"{pathway} sequence {sequence} pressure drifted")
            expected_abilities = {
                f"lord_of_mysteries:{ability}"
                for ability in terms["abilities"][str(sequence)]
            }
            require(set(definition["abilities"]) == expected_abilities,
                    f"{pathway} sequence {sequence} ability data drifted")
            require(len(definition["acting_events"]) == 2,
                    f"{pathway} sequence {sequence} needs two acting routes")
            for event in definition["acting_events"]:
                require(event.split(":", 1)[-1] in acting,
                        f"acting event {event} is not executable")
                acting_count += 1
    require(potion_count == launch["potion_count"],
            "M3 potion count drifted")
    require(acting_count == launch["acting_event_count"],
            "M3 acting event count drifted")

    validation = contract["validation"]
    game_tests = source(
        JAVA / "gametest" / "PlayerPersistenceGameTests.java")
    require(game_tests.count("@GameTest") == validation["game_tests"],
            "GameTest count drifted")
    restart_script = source(ROOT / "scripts" / "run_server_restart_matrix.py")
    require(not validation["requires_restart_matrix"]
            or ("run_once(args.timeout, 1)" in restart_script
                and "run_once(args.timeout, 2)" in restart_script
                and "first_seed != second_seed" in restart_script),
            "dedicated server restart matrix is missing")
    test_sources = "\n".join(
        source(path) for path in TESTS.rglob("*.java"))
    for test_class in validation["behavior_test_classes"]:
        require(f"class {test_class}" in test_sources,
                f"behavior test {test_class} is missing")

    for locale in ("zh_cn", "en_us"):
        translations = load(LANG / f"{locale}.json")
        missing = set(contract["translation_keys"]) - translations.keys()
        require(not missing,
                f"{locale} misses translations {sorted(missing)}")

    print(
        "M3 foundation contract checked: three physical city jobs with "
        "shared daily limits, schema-23 exposure persistence and repair, "
        "six deterministic restart-persistent world events, event-sensitive "
        "economy, newspaper and diagnostics visibility, spirituality and "
        "ritual modifiers, five launch pathways at sequences 6-5, "
        "nine GameTests, and dedicated restart validation"
    )


if __name__ == "__main__":
    main()
