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

    relay = contract["traveler_spatial_relay"]
    travel_logic = source(
        JAVA / "ability" / "M3TravelNetworkLogic.java")
    travel_service = source(
        JAVA / "ability" / "TravelMarkerService.java")
    door_access = source(
        JAVA / "ability" / "TravelerDoorAccessMode.java")
    door_policy = source(
        JAVA / "ability" / "TravelerDoorPolicy.java")
    door_entity = source(
        JAVA / "entity" / "TravelerDoorEntity.java")
    territory_event = source(
        JAVA / "compat" / "TravelerDoorTerritoryEvent.java")
    territory_service = source(
        JAVA / "compat" / "TravelerDoorTerritoryService.java")
    entity_registry = source(
        JAVA / "registry" / "ModEntities.java")
    require(f'literal("{relay["command"]}")' in commands
            and "TravelMarkerService.sendGuide" in commands,
            "traveler spatial relay guide command is missing")
    require(all(f'literal("{part}")' in commands
                for part in relay["access_command"])
            and ".setAccessMode(" in commands,
            "traveler door access command is missing")
    require(
        f"BASE_SPIRITUALITY_COST = {relay['leader_cost']}f"
        in travel_logic
        and f"PASSENGER_SPIRITUALITY_COST = "
        f"{relay['passenger_cost']}f" in travel_logic
        and f"MAX_PASSENGERS = {relay['max_passengers']}" in travel_logic,
        "traveler spatial relay cost or passenger cap drifted")
    require(
        f"CONSENT_RADIUS = {relay['consent_radius']}d" in travel_logic,
        "traveler relay consent radius drifted")
    require(
        f"DOOR_DURATION_TICKS = {relay['door_duration_ticks']}"
        in travel_logic
        and f"TRANSIT_COOLDOWN_TICKS = "
        f"{relay['transit_cooldown_ticks']}L" in travel_logic,
        "traveler door lifetime or transit cooldown drifted")
    leader_cooldown = re.search(
        r"LEADER_COOLDOWN_TICKS\s*=\s*([\d_]+)L", travel_service)
    passenger_cooldown = re.search(
        r"PASSENGER_COOLDOWN_TICKS\s*=\s*([\d_]+)L", travel_service)
    require(
        leader_cooldown
        and int(leader_cooldown.group(1).replace("_", ""))
        == relay["leader_cooldown_ticks"]
        and passenger_cooldown
        and int(passenger_cooldown.group(1).replace("_", ""))
        == relay["passenger_cooldown_ticks"],
        "traveler relay cooldowns drifted")
    require(
        relay["marker_item"] == "minecraft:compass"
        and "Items.COMPASS" in travel_service
        and relay["marker_block"] == "minecraft:lodestone"
        and "Blocks.LODESTONE" in travel_service
        and "level.isInWorldBounds(position)" in travel_service
        and travel_service.index("level.isInWorldBounds(position)")
        < travel_service.index("level.getChunkAt(position)"),
        "traveler relay no longer uses physical vanilla markers")
    require(
        not relay["opening_support_requires_sneaking"]
        or "candidate.isShiftKeyDown()"
        in travel_service,
        "traveler relay opening support consent is missing")
    require(
        not relay["same_source_dimension"]
        or "candidate.serverLevel() == leader.serverLevel()"
        in travel_service,
        "traveler relay can pull passengers from another source dimension")
    require(
        not relay["matching_marker"] or "hasMatchingMarker"
        in travel_service,
        "traveler relay no longer requires a matching marker")
    require(
        not relay["failure_preserves_resources"]
        or ("findDoorArrival" in travel_service
            and "SpiritualityCost.tryConsume" in travel_service
            and travel_service.index("findDoorArrival")
            < travel_service.index("SpiritualityCost.tryConsume")
            and "SpiritualityCost.refund(data, cost)" in travel_service
            and "sourceDoor.discard()" in travel_service),
        "traveler relay failure no longer preserves resources")
    require(
        relay["entity"] == "traveler_door"
        and '"traveler_door"' in entity_registry
        and "TravelerDoorEntity::new" in entity_registry
        and "addFreshEntity(sourceDoor)" in travel_service
        and "addFreshEntity(destinationDoor)" in travel_service,
        "traveler relay no longer opens two real door entities")
    require(
        not relay["crossing_is_final_consent"]
        or ("getEntitiesOfClass(" in door_entity
            and "tryTransit(player)" in door_entity),
        "traveler door crossing is no longer explicit consent")
    require(
        sorted(mode.lower() for mode in relay["access_modes"])
        == ["party", "private", "public"]
        and all(mode.upper() in door_access
                for mode in relay["access_modes"])
        and f"TravelerDoorAccessMode.{relay['default_access_mode'].upper()}"
        in door_entity
        and (not relay["owner_always_allowed"]
             or "owner.equals(candidate)" in door_access),
        "traveler door access modes drifted")
    require(
        f'"{relay["marker_name_tag"]}"' in travel_service
        and f"MAX_NAME_LENGTH = {relay['name_max_length']}"
        in door_policy
        and 'literal("name")' in commands
        and ".setMarkerName(" in commands
        and ".clearMarkerName(" in commands
        and '"door_name"' in door_entity,
        "traveler door marker naming is missing")
    require(
        f'"{relay["blacklist_nbt_key"]}"' in player
        and "traveler_door_safety_controls" in fixer
        and f"MAX_BLOCKED_PLAYERS = "
        f"{relay['blacklist_max_players']}" in door_policy
        and all(f'literal("{token}")' in commands
                for token in ("block", "unblock", "blocked"))
        and (not relay["blacklist_overrides_access"]
             or ("blockedPlayers.contains(candidate)" in door_policy
                 and door_policy.index("blockedPlayers.contains(candidate)")
                 < door_policy.index(".allows(owner")))
        and (not relay["active_blacklist_updates"]
             or ("updateActiveDoors" in travel_service
                 and "door.block(candidate)" in travel_service
                 and "door.unblock(candidate)" in travel_service)),
        "traveler door blacklist controls drifted")
    require(
        relay["territory_event"] in territory_event
        and all(action in territory_event
                for action in relay["territory_actions"])
        and "MinecraftForge.EVENT_BUS.post" in territory_service
        and (not relay["vanilla_spawn_protection"]
             or "isUnderSpawnProtection" in territory_service)
        and travel_service.index("TravelerDoorTerritoryService.allows")
        < travel_service.index("SpiritualityCost.tryConsume")
        and "TRANSIT_DESTINATION" in door_entity,
        "traveler door territory compatibility hook is missing")
    require(
        not relay["one_pair_per_owner"]
        or ("discardPreviousDoors" in travel_service
            and "door.ownedBy(owner.getUUID())" in travel_service),
        "traveler door owner pair replacement is missing")
    require(
        not relay["persistent"]
        or ("readAdditionalSaveData" in door_entity
            and "addAdditionalSaveData" in door_entity
            and all(f'"{field}"' in door_entity
                    for field in relay["nbt_fields"])),
        "traveler door persistence fields drifted")
    require(
        not relay["cross_dimension"]
        or ("getLevel(targetDimension)" in door_entity
            and "player.teleportTo(" in door_entity),
        "traveler door cross-dimension transit is missing")
    require(
        not relay["safe_arrival_required"]
        or ("findDoorArrival" in door_entity
            and "destination == null" in door_entity
            and "isWithinBounds(targetAnchor)" in door_entity),
        "traveler door safe-arrival rejection is missing")

    marionettes = contract["persistent_marionettes"]
    marionette_policy = source(
        JAVA / "ability" / "MarionettePolicy.java")
    marionette_service = source(
        JAVA / "ability" / "MarionetteService.java")
    marionette_scroll = source(
        JAVA / "ability" / "MarionetteScrollItem.java")
    marionette_storage = source(
        JAVA / "ability" / "MarionetteStoragePolicy.java")
    marionette_item_registry = source(
        JAVA / "registry" / "ModItems.java")
    gametest_source = source(
        JAVA / "gametest" / "PlayerPersistenceGameTests.java")
    require(
        f'literal("{marionettes["command"]}")' in commands
        and "MarionetteService.sendGuide" in commands
        and "MarionetteService.recall" in commands
        and "MarionetteService.release" in commands,
        "marionette roster commands are missing")
    require(
        f"MAX_MARIONETTES = {marionettes['maximum_roster']}"
        in marionette_policy
        and f"MAX_TARGET_HEALTH = "
        f"{marionettes['maximum_target_health']}f" in marionette_policy
        and f"MAX_CAPTURE_HEALTH_RATIO = "
        f"{marionettes['maximum_capture_health_ratio']}f"
        in marionette_policy
        and f"CREATION_COST = {marionettes['creation_cost']}f"
        in marionette_policy
        and f"CREATION_COOLDOWN_TICKS = "
        f"{marionettes['creation_cooldown_ticks']:_}L"
        in marionette_policy,
        "marionette creation limits drifted")
    require(
        not marionettes["requires_hostile_non_player"]
        or ("!playerTarget" in marionette_policy
            and "hostileTarget" in marionette_policy
            and "MobCategory.MONSTER" in marionette_service),
        "marionette creation can target players or non-hostiles")
    require(
        not marionettes["foreign_ownership_rejected"]
        or ("OWNED_BY_ANOTHER" in marionette_service
            and "currentOwner != null" in marionette_service),
        "foreign marionette ownership is no longer rejected")
    require(
        not marionettes["player_damage_forbidden"]
        or ("MarionettePolicy.canDamage" in marionette_service
            and "victim instanceof Player" in marionette_service
            and "event.setCanceled(true)" in marionette_service
            and "victimPlayer || victimOwner != null" in marionette_policy),
        "marionettes can damage players")
    require(
        not marionettes["owner_friendly_fire_forbidden"]
        or ("MarionettePolicy.canDamage" in marionette_service
            and "!victimOwner.equals(attackerId)" in marionette_policy),
        "marionette owner or squad friendly fire is not blocked")
    require(
        not marionettes["all_marionette_friendly_fire_forbidden"]
        or ("attackerOwner != null" in marionette_policy
            and "victimOwner != null" in marionette_policy
            and "ownerOf(candidate).isEmpty()" in marionette_service),
        "marionettes can attack another player's marionettes")
    require(
        f'"{marionettes["roster_nbt_key"]}"' in player
        and f'"{marionettes["cooldown_nbt_key"]}"' in player
        and marionettes["migration_fix"] in fixer
        and "MarionettePolicy.normalizeRoster" in sanitizer,
        "marionette roster persistence, migration or repair is missing")
    require(
        marionettes["owner_tag"].split(":", 1)[-1]
        in marionette_service
        and (not marionettes["persistent_entity_tag"]
             or ("getPersistentData().putUUID" in marionette_service
                 and "setPersistenceRequired()" in marionette_service)),
        "marionette entity ownership is not persistent")
    require(
        not marionettes["loaded_same_dimension_recall"]
        or ("mob.level() != owner.serverLevel()" in marionette_service
            and "findRecallDestination" in marionette_service
            and "mob.teleportTo(" in marionette_service),
        "same-dimension marionette recall is missing")
    require(
        not marionettes["never_force_loads_chunks"]
        or ("level.getEntity(id)" in marionette_service
            and "getChunkAt" not in marionette_service),
        "marionette management can force-load chunks")
    require(
        not marionettes["explicit_release"]
        or ("data.marionetteRoster.remove" in marionette_service
            and "data.marionetteStorageRecords.remove" in marionette_service
            and "clearOwnership" in marionette_service),
        "marionette ownership cannot be explicitly revoked")
    require(
        f'"{marionettes["storage_item"]}"' in marionette_item_registry
        and f'"{marionettes["storage_records_nbt_key"]}"' in player
        and marionettes["storage_migration_fix"] in fixer
        and "MarionetteStoragePolicy.normalizeRecords" in sanitizer,
        "marionette storage item, ledger migration or repair is missing")
    require(
        f"STORAGE_COST = {marionettes['storage_cost']}f"
        in marionette_storage
        and f"ITEM_COOLDOWN_TICKS = "
        f"{marionettes['storage_cooldown_ticks']}" in marionette_storage,
        "marionette storage costs drifted")
    require(
        not marionettes["server_authoritative_storage_payload"]
        or ("MarionetteStoragePolicy.payload(record)" in marionette_scroll
            and "entity_payload" in marionette_storage
            and "entity_payload" not in source(
                ROOT / "src" / "main" / "resources" / "assets"
                / "lord_of_mysteries" / "models" / "item"
                / "marionette_scroll.json")),
        "marionette payload is no longer server authoritative")
    require(
        not marionettes["one_time_storage_token"]
        or ("tokenMatches" in marionette_scroll
            and "marionetteStorageRecords.remove" in marionette_scroll),
        "marionette storage token is not one-time")
    require(
        not marionettes["owner_bound_storage"]
        or ("owner.getUUID().equals(voucher.ownerId())"
            in marionette_scroll),
        "marionette storage is no longer owner-bound")
    require(
        not marionettes["copied_voucher_rejected"]
        or ("INVALID_TOKEN" in marionette_scroll
            and "copied voucher" in gametest_source.lower()),
        "copied marionette vouchers are no longer rejected")
    require(
        not marionettes["cross_dimension_deploy"]
        or ("owner.serverLevel()" in marionette_scroll
            and "EntityType.loadEntityRecursive" in marionette_scroll),
        "marionette storage cannot deploy into the owner's current dimension")
    require(
        not marionettes["safe_deployment_required"]
        or ("findDeploymentDestination" in marionette_scroll
            and "level.noCollision" in marionette_scroll
            and "isWithinBounds" in marionette_scroll),
        "marionette deployment safe-position checks are missing")

    processing = contract["characteristic_processing"]
    processing_logic = source(
        JAVA / "characteristic" / "CharacteristicProcessingLogic.java")
    processing_service = source(
        JAVA / "characteristic" / "CharacteristicProcessingService.java")
    load_logic = source(
        JAVA / "characteristic" / "CharacteristicLoadLogic.java")
    load_service = source(
        JAVA / "characteristic" / "CharacteristicLoadService.java")
    broken_characteristic = source(
        JAVA / "characteristic" / "BrokenCharacteristicItem.java")
    separator_block = source(
        JAVA / "characteristic" / "CharacteristicSeparatorBlock.java")
    provenance_saved = source(
        JAVA / "characteristic" / "CharacteristicProvenanceSavedData.java")
    provenance_events = source(
        JAVA / "characteristic" / "CharacteristicProvenanceEvents.java")
    acting_handler = source(JAVA / "acting" / "ActingEventHandler.java")
    status_packet = source(
        JAVA / "network" / "PlayerMysteryStatusS2CPacket.java")
    block_registry = source(JAVA / "registry" / "ModBlocks.java")
    item_registry = source(JAVA / "registry" / "ModItems.java")
    require(f'literal("{processing["command"]}")' in commands
            and "CharacteristicProcessingService.sendGuide" in commands,
            "characteristic processing guide command is missing")
    require(
        f'BLOCKS.register("{processing["separator"]}"' in block_registry
        and f'BLOCKS.register("{processing["washing_altar"]}"'
        in block_registry,
        "characteristic processing workstations are missing")
    require(
        f'"{processing["probe"]}"' in item_registry
        and f'"{processing["seal_wax"]}"' in item_registry
        and f'"{processing["washing_incense"]}"' in item_registry,
        "characteristic processing tools are missing")
    require(
        f"MERGE_CORRUPTION = {processing['merge_corruption']}f"
        in processing_logic
        and f"CLEANSE_CORRUPTION_REDUCTION = "
        f"{processing['cleanse_corruption_reduction']}f"
        in processing_logic
        and f"CLEANSE_DOMINANCE_REDUCTION = "
        f"{processing['cleanse_dominance_reduction']}f"
        in processing_logic
        and f"CLEANSE_PURITY_LOSS = "
        f"{processing['cleanse_purity_loss']}f"
        in processing_logic,
        "characteristic processing costs drifted")
    require(
        not processing["rejects_duplicate_source"]
        or ("first.sourceHash().equals(second.sourceHash())"
            in processing_logic and "DUPLICATE_SOURCE" in processing_logic),
        "duplicate characteristic sources are no longer rejected")
    require(
        not processing["sealed_blocks_processing"]
        or processing_service.count("isSealed(") >= 4,
        "sealed characteristics no longer block processing")
    require(
        not processing["failure_preserves_resources"]
        or ("StackResult.failure" in processing_service
            and "ItemStack.EMPTY" in processing_service),
        "failed characteristic operations no longer preserve resources")
    require(
        f'"{processing["identity_salt_circle"]}"' in item_registry,
        "identity salt circle is missing")
    require(
        not processing["extra_load_visible"]
        or ("extraCharacteristicLoad" in status_packet
            and "CharacteristicLoadLogic.extraLoad(data)" in status_packet
            and "CharacteristicLoadService.sendStatus(player)"
            in processing_service),
        "extra characteristic load is no longer visible")
    require(
        not processing["direct_absorption_single_unit"]
        or ("CharacteristicProcessingLogic.totalUnits(incoming) != 1"
            in load_logic
            and "CharacteristicLoadService.absorb(player, stack)"
            in broken_characteristic),
        "direct absorption no longer requires one conserved unit")
    require(
        f"DIGESTION_PENALTY_PER_LAYER = "
        f"{processing['digestion_penalty_per_layer']:.2f}f" in load_logic
        and f"MINIMUM_DIGESTION_MULTIPLIER = "
        f"{processing['minimum_digestion_multiplier']:.1f}f" in load_logic
        and f"SPIRITUALITY_REWARD_PER_LAYER = "
        f"{processing['spirituality_reward_per_layer']:.1f}f" in load_logic
        and "CharacteristicLoadLogic.digestionMultiplier(extraLoad)"
        in acting_handler
        and "CharacteristicLoadLogic.spiritualityReward(extraLoad)"
        in acting_handler,
        "extra-load acting tradeoff drifted")
    require(
        f"SPIRIT_SALT_COST = "
        f"{processing['extraction_spirit_salt_cost']}" in load_service
        and f"Math.min({processing['extraction_supporter_cap']}, "
        in load_service
        and "player.isShiftKeyDown()" in separator_block
        and "CharacteristicLoadService.interact(" in separator_block,
        "extra-load extraction ritual requirements drifted")
    require(
        not processing["extraction_preserves_pathway_and_sequence"]
        or ("data.characteristicBundles.set(currentIndex, result.retained())"
            in load_service
            and not re.search(r"data\.pathway\s*=(?!=)", load_service)
            and not re.search(r"data\.sequence\s*=(?!=)", load_service)),
        "extraction can overwrite pathway or sequence")
    require(
        not processing["failure_preserves_extra_load"]
        or ("case FAILURE" in load_service
            and "source.layers()" in load_logic
            and "return new ExtractionResult(outcome, agitated, null)"
            in load_logic),
        "failed extraction no longer preserves the extra layer")
    provenance = processing["server_global_provenance"]
    require(
        f'"{provenance["data_name"]}"' in provenance_saved
        and f"DATA_VERSION = {provenance['data_version']}"
        in provenance_saved
        and (not provenance["overworld_storage"]
             or "getServer().overworld()" in provenance_saved)
        and (not provenance["hashed_actor_only"]
             or ('fingerprint(actor.toString())' in provenance_saved
                 and 'putString("actor_hash"' in provenance_saved
                 and 'putString("actor_uuid"' not in provenance_saved)),
        "server-global characteristic provenance storage drifted")
    operation_sources = processing_service + load_service
    for operation in provenance["operations"]:
        require(f'"{operation}"' in operation_sources,
                f"provenance operation {operation} is not audited")
    require(
        not provenance["replay_preserves_items_and_materials"]
        or ("PROVENANCE_REPLAY" in processing_service
            and processing_service.index(".consume(")
            < processing_service.index("case ACCEPTED -> result")
            and load_service.index("if (!audit(")
            < load_service.index("stack.shrink(1)")
            and load_service.index('if (!audit(\n                player,\n'
                                   '                "player_extract"')
            < load_service.index("consumeMaterials(player)")),
        "provenance replay can consume player resources")
    for command_token in provenance["operator_command"].split():
        require(f'literal("{command_token}")' in commands,
                f"provenance operator command {command_token} is missing")
    require(
        '"characteristic_provenance_nonce"' in player
        and "characteristic_provenance_nonce" in fixer
        and "CharacteristicLedger.ensurePlayerProvenance" in provenance_events,
        "legacy player characteristic sources are not rekeyed on login")

    rituals = contract["sequence_five_rituals"]
    ritual_service = source(
        JAVA / "ritual" / "SequenceFiveAdvancementRitual.java")
    ritual_logic = source(
        JAVA / "ritual" / "SequenceFiveRitualLogic.java")
    altar = source(JAVA / "ritual" / "RitualAltarBlock.java")
    potion_rules = source(JAVA / "potion" / "PotionAdvancementRules.java")
    require(rituals["target_sequence"] == 5
            and "targetSequence == 5" in potion_rules
            and "RITUAL_REQUIRED" in potion_rules,
            "sequence 5 no longer requires a completed ritual")
    require(f'ModItems.{rituals["altar"].upper()}.get()'
            in source(JAVA / "registry" / "ModItems.java")
            or "RITUAL_ALTAR_ITEM" in source(
                    JAVA / "registry" / "ModItems.java"),
            "ritual altar item is missing")
    require("SequenceFiveAdvancementRitual.interact" in altar
            and "player.isShiftKeyDown()" in altar,
            "ritual altar no longer exposes inspect/commit interaction")
    require(f'literal("{rituals["guide_command"]}")' in commands
            and "SequenceFiveAdvancementRitual.showGuide" in commands,
            "sequence-5 ritual guide command is missing")
    require(not rituals["solo_supported"]
            or "supporters" in ritual_logic
            and "supporters" in ritual_service,
            "solo ritual stability no longer supports optional helpers")
    require(f"Math.min({rituals['supporter_cap']}"
            in ritual_service,
            "ritual supporter cap drifted")
    require(
        f"clamp(worldEventBonus, 0f, "
        f"{rituals['world_event_bonus']:.2f}f)" in ritual_logic,
        "ritual-resonance bonus cap drifted")
    require(not rituals["failure_consumes_materials"]
            or "consumeCosts(player, costs(type))" in ritual_service,
            "failed committed rituals no longer consume prepared materials")
    require(not rituals["failure_adds_pressure_and_pollution"]
            or ("insanityPressure + 12f" in ritual_service
                and "pollution + 6f" in ritual_service
                and "insanityPressure + 20f" in ritual_service
                and "pollution + 15f" in ritual_service),
            "ritual failure consequences drifted")
    for pathway, terms in rituals["pathways"].items():
        require(f'{terms["type"]}("{pathway}")' in ritual_service,
                f"{pathway} ritual type is missing")
        require(f'"{rituals["knowledge_prefix"]}"' in ritual_service
                or rituals["knowledge_prefix"] in ritual_service,
                "ritual completion proof prefix drifted")
        require(terms["support_focus"].upper() in ritual_service,
                f"{pathway} ritual support focus is missing")

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
        f"shared daily limits, schema-{contract['capability_schema']} "
        "exposure persistence and repair, "
        "six deterministic restart-persistent world events, event-sensitive "
        "economy, newspaper and diagnostics visibility, spirituality and "
        "ritual modifiers, five launch pathways at sequences 6-5, five "
        "dedicated sequence-5 rituals with solo and supporter paths, "
        "a persistent consent-gated bidirectional traveler door with "
        "private, party and public access, marker names, a persistent "
        "blocklist and territory protection hooks, a three-slot persistent "
        "marionette roster with safe recall and explicit release, "
        "server-authoritative marionette storage with owner-bound one-time "
        "tokens, copied-voucher rejection and safe cross-dimensional deployment, "
        "conserved characteristic "
        "splitting, sealing and washing, visible extra-load absorption and "
        "safe extraction, server-global provenance replay protection, "
        f"{validation['game_tests']} GameTests, and "
        "dedicated restart validation"
    )


if __name__ == "__main__":
    main()
