#!/usr/bin/env python3

import json
import re
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
CONTRACT = ROOT / "docs" / "m4-foundation-contract.json"
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
        raise SystemExit(f"M4 foundation contract failed: {message}")


def definitions(directory):
    values = [load(path) for path in sorted((DATA / directory).glob("*.json"))]
    ids = [value.get("id") for value in values]
    require(len(ids) == len(set(ids)), f"{directory} contains duplicate ids")
    return values


def main():
    contract = load(CONTRACT)
    require(contract.get("schema_version") == 1,
            "unsupported contract schema")

    organizations = definitions("organizations")
    organization_contract = contract["organizations"]
    require(len(organizations) == organization_contract["count"],
            "organization definition count drifted")
    require(sum(value.get("kind") == "church" for value in organizations)
            == organization_contract["churches"],
            "church count drifted")
    require(sum(value.get("kind") == "secret" for value in organizations)
            == organization_contract["secret_organizations"],
            "secret organization count drifted")
    organization_ids = {value["id"] for value in organizations}
    for value in organizations:
        require(value.get("schema_version") == 4
                and value.get("implementation_state") == "playable",
                f"{value['id']} metadata is not playable schema 4")
        for plane in organization_contract["data_planes"]:
            require(plane in value and bool(value[plane]),
                    f"{value['id']} misses organization plane {plane}")
        relations = value["relations"]
        allies = set(relations.get("allies", []))
        enemies = set(relations.get("enemies", []))
        require(not allies.intersection(enemies)
                and value["id"] not in allies
                and value["id"] not in enemies,
                f"{value['id']} has conflicting relations")
        require(all(target in organization_ids for target in allies | enemies),
                f"{value['id']} references an unknown organization")
        strategies = value["strategy_weights"]
        require(len([weight for weight in strategies.values() if weight > 0]) >= 2,
                f"{value['id']} has fewer than two active strategies")
        require(set(strategies).issubset(
                    organization_contract["action_types"]),
                f"{value['id']} uses an unknown strategy")

    action_enum = source(
        JAVA / "organization" / "OrganizationActionType.java")
    action_policy = source(
        JAVA / "organization" / "OrganizationActionPolicy.java")
    action_saved = source(
        JAVA / "organization" / "OrganizationActionSavedData.java")
    action_service = source(
        JAVA / "organization" / "OrganizationActionService.java")
    commands = source(JAVA / "command" / "ProjectMysteryCommands.java")
    require(
        f"DAILY_ACTION_COUNT = {organization_contract['daily_action_count']}"
        in action_policy,
        "daily organization action count drifted")
    require(organization_contract["persistent_data_name"] in action_saved,
            "organization SavedData name drifted")
    for action_type in organization_contract["action_types"]:
        require(f'"{action_type}"' in action_enum,
                f"organization action {action_type} is missing")
    require("level.getSeed()" in action_service
            and "actionExposure(level)" in action_service,
            "daily actions are not seeded by world and exposure")
    require("Map<UUID, Assignment>" in action_saved
            and "assignments.containsKey(player)" in action_saved,
            "player-isolated assignment guard is missing")
    for token in organization_contract["commands"]:
        require(f'literal("{token}")' in commands,
                f"organization command {token} is missing")

    artifacts = definitions("artifacts")
    artifact_contract = contract["artifacts"]
    require(len(artifacts) == artifact_contract["implemented"],
            "artifact definition count drifted")
    require(artifact_contract["implemented"] < artifact_contract["m4_target"],
            "contract must not claim the 24-artifact target is complete")
    expected_artifacts = {
        f"lord_of_mysteries:{path}"
        for path in artifact_contract["definitions"]
    }
    artifact_ids = {value["id"] for value in artifacts}
    require(artifact_ids == expected_artifacts,
            "implemented artifact ids drifted")
    for value in artifacts:
        require(value.get("schema_version") == 4
                and value.get("implementation_state") == "playable",
                f"{value['id']} metadata is not playable schema 4")
        require(value.get("item") == value["id"],
                f"{value['id']} item binding drifted")
        require(value.get("custody_organization") in organization_ids,
                f"{value['id']} has an unknown custodian")
        require(1 <= value.get("danger_level", 0) <= 5
                and 1 <= value.get("safe_uses", 0) <= 64
                and value.get("loan_days", 0) >= 1
                and value.get("leak_threshold", 0)
                >= value.get("danger_level", 0),
                f"{value['id']} risk limits are invalid")
        item_path = value["item"].split(":", 1)[1]
        require((ASSETS / "models" / "item" / f"{item_path}.json").exists(),
                f"{value['id']} item model is missing")

    custody_state = source(
        JAVA / "artifact" / "ArtifactCustodyState.java")
    custody_saved = source(
        JAVA / "artifact" / "ArtifactCustodySavedData.java")
    custody_service = source(
        JAVA / "artifact" / "SealedArtifactService.java")
    require(artifact_contract["persistent_data_name"] in custody_saved,
            "artifact SavedData name drifted")
    for state in artifact_contract["states"]:
        require(state in custody_state,
                f"artifact custody state {state} is missing")
    for field in artifact_contract["tracks"]:
        require(f'"{field}"' in custody_saved,
                f"artifact custody field {field} is not persisted")
    require("activeForDefinition(definition.id()) != null" in custody_saved,
            "world-unique issue guard is missing")
    require(f"TRUSTED_REPUTATION = {artifact_contract['trusted_reputation']}"
            in custody_service,
            "artifact reputation threshold drifted")
    require("expireOverdue" in custody_saved
            and "expireOverdue" in custody_service,
            "offline overdue leakage is missing")
    require("uses > safeUses" in custody_saved
            and "contamination >= leakThreshold" in custody_saved,
            "safe-use or contamination leak trigger is missing")
    require("HOLDER_CHANGED" in custody_saved
            and "countInstances(player, instance) > 1" in custody_service
            and "DUPLICATE" in custody_saved,
            "holder-change or duplicate protection is missing")
    require("adoptLegacy" in custody_service,
            "legacy matchbox adoption is missing")
    require("retireAbused" in custody_saved
            and "retireAbused" in custody_service,
            "operator abuse retirement is missing")
    require("record.responsible().equals(player.getUUID())" in custody_service
            and "record.holder().equals(player.getUUID())" in custody_service
            and "visibleRecords" in custody_service,
            "normal artifact status can expose another player's custody record")
    require("player.hasPermissions(2)" in custody_service,
            "orphaned artifact diagnostics are not permission-gated")
    require("grants no permissions" in source(
                ASSETS / "lang" / "en_us.json")
            and "不授予任何权限" in source(
                ASSETS / "lang" / "zh_cn.json"),
            "Guest Mask permission boundary is not documented in game")
    for token in artifact_contract["commands"]:
        require(f'literal("{token}")' in commands,
                f"artifact command {token} is missing")

    translations = {
        locale: load(ASSETS / "lang" / f"{locale}.json")
        for locale in ("zh_cn", "en_us")
    }
    for value in organizations:
        for locale, language in translations.items():
            require(value["title_key"] in language,
                    f"{locale} misses {value['title_key']}")
    for value in artifacts:
        for locale, language in translations.items():
            for key in (
                    f"item.lord_of_mysteries.{value['item'].split(':', 1)[1]}",
                    value["effect_key"],
                    value["cost_key"]):
                require(key in language,
                        f"{locale} misses {key}")

    pages = source(ROOT / "docs" / "assets" / "catalog-data.js")
    pages_contract = contract["pages"]
    require(
        f'"organizationDefinitions": {pages_contract["organization_entries"]}'
        in pages
        and f'"artifactDefinitions": {pages_contract["artifact_entries"]}'
        in pages,
        "Pages catalog metadata drifted")
    for entry_id in organization_ids | artifact_ids:
        require(f'"id": "{entry_id}"' in pages,
                f"Pages catalog misses {entry_id}")

    validation = contract["validation"]
    test_sources = "\n".join(
        source(path) for path in TESTS.rglob("*.java"))
    require(test_sources.count("@Test")
            == validation["junit_tests"],
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
        "M4 foundation contract checked: "
        f"{len(organizations)} organizations, "
        f"{len(artifact_contract['action_types']) if 'action_types' in artifact_contract else len(organization_contract['action_types'])} "
        "autonomous action types, "
        f"{len(artifacts)}/{artifact_contract['m4_target']} sealed artifacts, "
        "persistent custody, offline leakage, recovery, duplicate quarantine, "
        "operator retirement, bilingual resources, and synchronized Pages"
    )


if __name__ == "__main__":
    main()
