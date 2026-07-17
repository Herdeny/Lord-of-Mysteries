#!/usr/bin/env python3

import json
import re
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
CONTRACT = ROOT / "docs" / "m2-investigation-contract.json"
DATA = ROOT / "src" / "main" / "resources" / "data" / "lord_of_mysteries"
LANG = ROOT / "src" / "main" / "resources" / "assets" / "lord_of_mysteries" / "lang"
COMMANDS = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "command" / "ProjectMysteryCommands.java"
PLAYER_DATA = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "player" / "PlayerMysteryData.java"
SITE_GENERATOR = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "world" / "InvestigationSiteGenerator.java"
ITEMS = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "registry" / "ModItems.java"
COMMISSION_SERVICE = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "commission" / "CommissionService.java"
PARTY_SERVICE = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "commission" / "QuestPartyService.java"
PARTY_SNAPSHOT = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "commission" / "QuestPartySnapshot.java"
PARTY_SAVED_DATA = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "commission" / "QuestPartySavedData.java"
PROGRESS_HANDLER = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "commission" / "QuestProgressHandler.java"


def load(path):
    return json.loads(path.read_text(encoding="utf-8"))


def require(condition, message):
    if not condition:
        raise SystemExit(f"M2 investigation contract failed: {message}")


def by_id(paths):
    payloads = [load(path) for path in paths]
    return {payload["id"]: payload for payload in payloads}


def main():
    contract = load(CONTRACT)
    require(contract.get("schema_version") == 1, "unsupported contract schema")

    commissions = by_id(sorted((DATA / "commissions").glob("*.json")))
    quests = by_id(sorted((DATA / "quests").glob("*.json")))
    require(len(commissions) == contract["data_counts"]["commissions"],
            "commission count drifted")
    require(len(quests) == contract["data_counts"]["quest_chains"],
            "quest chain count drifted")

    missing = commissions["lord_of_mysteries:commission/missing_investigation_squad"]
    require(set(missing["solutions"]) == set(
        contract["legacy_missing_squad"]["solutions"]),
        "missing squad rescue solutions drifted")
    missing_quest = quests[missing["quest_chain"]]
    missing_steps = [step["id"] for step in missing_quest["steps"]]
    require(len(missing_steps) == contract["legacy_missing_squad"]["step_count"],
            "legacy missing squad step count changed")
    prefix = contract["legacy_missing_squad"]["prefix"]
    require(missing_steps[:len(prefix)] == prefix,
            "legacy missing squad prefix changed")

    formula = contract["counterfeit_formula"]
    formula_commission = commissions[formula["commission"]]
    require(formula_commission["quest_chain"] == formula["quest"],
            "counterfeit formula quest link drifted")
    require(formula_commission.get("prerequisites") == [formula["prerequisite"]],
            "counterfeit formula prerequisite drifted")
    formula_steps = [step["id"] for step in quests[formula["quest"]]["steps"]]
    require(formula_steps == formula["steps"],
            "counterfeit formula step order drifted")

    command_source = COMMANDS.read_text(encoding="utf-8")
    for token in contract["command_tokens"]:
        require(f'literal("{token}")' in command_source,
                f"missing command token {token}")

    player_source = PLAYER_DATA.read_text(encoding="utf-8")
    schema_match = re.search(r"CURRENT_SCHEMA_VERSION\s*=\s*(\d+)", player_source)
    require(schema_match is not None
            and int(schema_match.group(1)) == contract["capability_schema"],
            "capability schema drifted")

    site_source = SITE_GENERATOR.read_text(encoding="utf-8")
    require("occultist_hut" in site_source and "OCCULTIST_HUT" in site_source,
            "occultist hut is not wired to world generation")
    require((DATA / "loot_tables" / "chests" / "occultist_hut.json").exists(),
            "occultist hut loot table is missing")
    require("sealed_formula_dossier" in ITEMS.read_text(encoding="utf-8"),
            "sealed formula dossier is not registered")

    party = contract["party_recovery"]
    party_service = PARTY_SERVICE.read_text(encoding="utf-8")
    party_snapshot = PARTY_SNAPSHOT.read_text(encoding="utf-8")
    party_saved_data = PARTY_SAVED_DATA.read_text(encoding="utf-8")
    progress_handler = PROGRESS_HANDLER.read_text(encoding="utf-8")
    commission_service = COMMISSION_SERVICE.read_text(encoding="utf-8")
    require(party["saved_data"] in party_saved_data,
            "party saved-data identity drifted")
    for state_key in party["required_state"]:
        require(f'"{state_key}"' in party_snapshot,
                f"party snapshot misses {state_key}")
    require("QuestPartySavedData.get" in party_service
            and "joinAndSync" in party_service
            and "reconcile" in party_service,
            "party persistence service is incomplete")
    require("QuestPartySavedData.get" in command_source
            and "party_storage=" in command_source,
            "dedicated-server diagnostics do not verify party storage")
    require("PlayerLoggedInEvent" in progress_handler
            and "PlayerLoggedOutEvent" in progress_handler,
            "party login/logout recovery hooks are missing")
    require("markSettled" in commission_service,
            "individual party settlement tracking is missing")
    require(party["maximum_party"] == 4,
            "party recovery contract maximum changed")

    for locale in ("zh_cn", "en_us"):
        translations = load(LANG / f"{locale}.json")
        missing_keys = set(contract["translation_keys"]) - translations.keys()
        require(not missing_keys,
                f"{locale} misses translations {sorted(missing_keys)}")

    print(
        "M2 investigation contract checked: "
        f"{len(commissions)} commissions, {len(quests)} quest chains, "
        "occultist hut, formula appraisal, three rescue routes, "
        "and persistent party recovery"
    )


if __name__ == "__main__":
    main()
