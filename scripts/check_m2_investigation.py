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
BOARD_BLOCK = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "commission" / "CommissionBoardBlock.java"
BOARD_SERVICE = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "commission" / "InvestigationBoardService.java"
BOARD_STATE = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "commission" / "CommissionBoardState.java"
EVIDENCE_VIEW = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "commission" / "CaseEvidenceView.java"
EVIDENCE_STATE = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "commission" / "EvidenceState.java"
ANALYSIS_STAGE = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "commission" / "CaseAnalysisStage.java"
RELATION_KIND = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "commission" / "EvidenceRelationKind.java"
ANALYSIS_SERVICE = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "commission" / "CaseAnalysisService.java"
DEBRIEF_RECORD = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "commission" / "CaseDebriefRecord.java"
DEBRIEF_SERVICE = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "commission" / "CaseDebriefService.java"
CASE_GRADE = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "commission" / "CaseGrade.java"
DEBRIEF_FOCUS = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "commission" / "CaseDebriefFocus.java"
HYPOTHESIS_RECORD = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "commission" / "CaseHypothesisRecord.java"
HYPOTHESIS_RULES = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "commission" / "CaseHypothesisRules.java"
HYPOTHESIS_SERVICE = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "commission" / "CaseHypothesisService.java"
HYPOTHESIS_VIEW = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "commission" / "CaseHypothesisView.java"
HYPOTHESIS_STANCE = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "commission" / "CaseHypothesisStance.java"
HYPOTHESIS_STATUS = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "commission" / "CaseHypothesisStatus.java"
RECOVERY_POLICY = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "commission" / "CaseRecoveryPolicy.java"
FORMULA_SERVICE = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "commission" / "FormulaAppraisalService.java"
NEWS_LOGIC = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "commission" / "CityNewsLogic.java"
NEWS_SERVICE = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "commission" / "CityNewsService.java"
NEWS_ITEM = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "commission" / "MistCityNewspaperItem.java"
CITY_LIFE = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "commission" / "CityLifeService.java"
CITY_DESK_LOGIC = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "commission" / "CityServiceDeskLogic.java"
CITY_DESK_SERVICE = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "commission" / "CityServiceDeskService.java"
NPC_HANDLER = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "commission" / "InvestigationNpcHandler.java"
OUTPOST_GENERATOR = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "world" / "MistCityOutpostGenerator.java"
OUTPOST_SAVED_DATA = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "world" / "MistCityOutpostSavedData.java"
BOARD_SCREEN = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "client" / "InvestigationBoardScreen.java"
BOARD_PACKET = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "network" / "InvestigationBoardS2CPacket.java"
NETWORK_PROTOCOL = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "network" / "NetworkProtocol.java"
NETWORK = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "network" / "PMNetwork.java"
DYNAMIC_CASE_PROFILE = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "commission" / "DynamicCaseProfile.java"
DYNAMIC_CASE_GENERATOR = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "commission" / "DynamicCaseGenerator.java"
DYNAMIC_CASE_SERVICE = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "commission" / "DynamicCaseService.java"
DYNAMIC_PORTFOLIO = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "commission" / "DynamicEvidencePortfolioItem.java"
DYNAMIC_PORTFOLIO_DATA = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "commission" / "DynamicEvidencePortfolioData.java"
DYNAMIC_PORTFOLIO_MODEL = ROOT / "src" / "main" / "resources" / "assets" / "lord_of_mysteries" / "models" / "item" / "dynamic_evidence_portfolio.json"
DYNAMIC_EVIDENCE = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "commission" / "DynamicCaseEvidenceItem.java"
DYNAMIC_EVIDENCE_DATA = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "commission" / "DynamicCaseEvidenceData.java"
DYNAMIC_MANIFESTATION = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "commission" / "DynamicCaseManifestationService.java"
DYNAMIC_SCHEDULE = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "commission" / "DynamicCaseSchedulePolicy.java"
DYNAMIC_FEEDBACK = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "commission" / "DynamicCaseFeedbackPolicy.java"


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

    dynamic = contract["dynamic_case_rotation"]
    dynamic_commission = commissions[dynamic["commission"]]
    require(dynamic_commission["quest_chain"] == dynamic["quest"],
            "dynamic case quest link drifted")
    require(dynamic_commission.get("prerequisites")
            == [dynamic["prerequisite"]],
            "dynamic case prerequisite drifted")
    require(dynamic_commission.get("repeatable") == dynamic["repeatable"]
            and dynamic_commission.get("cooldown_hours")
            == dynamic["cooldown_hours"],
            "dynamic case repeatability drifted")
    dynamic_steps = [step["id"]
                     for step in quests[dynamic["quest"]]["steps"]]
    require(dynamic_steps == dynamic["steps"],
            "dynamic case step order drifted")
    dynamic_profile = DYNAMIC_CASE_PROFILE.read_text(encoding="utf-8")
    dynamic_generator = DYNAMIC_CASE_GENERATOR.read_text(encoding="utf-8")
    dynamic_service = DYNAMIC_CASE_SERVICE.read_text(encoding="utf-8")
    commission_source = COMMISSION_SERVICE.read_text(encoding="utf-8")
    for archetype in dynamic["archetypes"]:
        require(archetype in dynamic_profile,
                f"dynamic case archetype {archetype} is missing")
    for slot in dynamic["slots"]:
        parts = slot.split("_")
        profile_slot = parts[0] + "".join(
            part.title() for part in parts[1:])
        require(profile_slot in dynamic_profile,
                f"dynamic case slot {slot} is missing")
    for route in dynamic["evidence_routes"]:
        require(route in dynamic_service,
                f"dynamic case evidence route {route} is missing")
    for conclusion in dynamic["conclusions"]:
        require(conclusion in dynamic_profile,
                f"dynamic case conclusion {conclusion} is missing")
    require(f'DESK_RECONSTRUCTION_COST = {dynamic["desk_cost_pence"]}L'
            in dynamic_service,
            "dynamic case desk cost drifted")
    require(f'DESK_RECONSTRUCTION_PRESSURE = '
            f'{dynamic["desk_pressure_fallback"]}f' in dynamic_service,
            "dynamic case desk pressure fallback drifted")
    require(f'WRONG_CONCLUSION_PRESSURE = '
            f'{dynamic["wrong_conclusion_pressure"]}f' in dynamic_service,
            "dynamic case wrong-conclusion pressure drifted")
    for state in dynamic["recovery_states"]:
        require(f'"{state}"' in dynamic_service,
                f"dynamic case recovery state {state} is missing")
    interactions = dynamic["world_interactions"]
    portfolio_id = interactions["portfolio_item"].split(":", 1)[1]
    portfolio_source = DYNAMIC_PORTFOLIO.read_text(encoding="utf-8")
    portfolio_data_source = DYNAMIC_PORTFOLIO_DATA.read_text(encoding="utf-8")
    npc_source = NPC_HANDLER.read_text(encoding="utf-8")
    newspaper_source = NEWS_ITEM.read_text(encoding="utf-8")
    party_source = PARTY_SERVICE.read_text(encoding="utf-8")
    evidence_source = DYNAMIC_EVIDENCE.read_text(encoding="utf-8")
    evidence_data_source = DYNAMIC_EVIDENCE_DATA.read_text(encoding="utf-8")
    manifestation_source = DYNAMIC_MANIFESTATION.read_text(encoding="utf-8")
    schedule_source = DYNAMIC_SCHEDULE.read_text(encoding="utf-8")
    feedback_source = DYNAMIC_FEEDBACK.read_text(encoding="utf-8")
    require(portfolio_id in ITEMS.read_text(encoding="utf-8")
            and DYNAMIC_PORTFOLIO_MODEL.exists(),
            "dynamic evidence portfolio registry or model is missing")
    require(interactions["scene_use"] != "right_click_site_block"
            or ("useOn" in portfolio_source
                and "collectSceneEvidence" in portfolio_source
                and "caseLocationTarget" in dynamic_service),
            "dynamic scene evidence is not a physical site interaction")
    require(interactions.get("highlighted_scene_use")
            != "right_click_evidence_display"
            or ("isEvidenceDisplay" in manifestation_source
                and "DynamicCaseService.collectSceneEvidence" in npc_source),
            "dynamic scene display is not directly collectable")
    require(interactions["witness_use"] != "right_click_tagged_npc"
            or ("tryInterviewWitness" in dynamic_service
                and "DynamicCaseService.tryInterviewWitness" in npc_source),
            "dynamic witness evidence is not wired to tagged NPC interaction")
    require(interactions["records_use"] != "right_click_newspaper"
            or ("tryReviewRecords" in dynamic_service
                and "DynamicCaseService.tryReviewRecords" in newspaper_source),
            "dynamic record evidence is not wired to newspaper interaction")
    require(interactions["recovery"] != "board_restore_current_stage"
            or ("recoverPortfolio" in dynamic_service
                and "DynamicCaseService.recoverPortfolio" in commission_source
                and "collectedStage" in portfolio_data_source),
            "dynamic portfolio recovery does not preserve its stage")
    require(not interactions["party_sync"]
            or ("synchronizePortfolios" in dynamic_service
                and "QuestPartyService.participants" in dynamic_service
                and party_source.count("DynamicCaseService.issuePortfolio") >= 2),
            "dynamic portfolio progress is not synchronized to the party")
    require(not interactions["returned_on_settle_or_abandon"]
            or commission_source.count("DynamicCaseService.returnPortfolio") >= 2,
            "dynamic portfolio is not returned on settlement and abandonment")
    require(interactions["portfolio_item"]
            in quests[dynamic["quest"]]["links"]["produces"],
            "dynamic quest content graph does not produce the portfolio")
    manifestations = dynamic["manifestations"]
    for tag_key in ["subject_tag", "affected_tag", "evidence_tag"]:
        require(f'"{manifestations[tag_key]}"' in manifestation_source,
                f"dynamic manifestation {tag_key} is missing")
    require(not manifestations["spawn_only_at_generated_site"]
            or "manifestationTarget" in manifestation_source,
            "dynamic manifestations may spawn before their site exists")
    require(not manifestations["cleanup_inactive_loaded_scenes"]
            or "cleanupInactive" in manifestation_source,
            "inactive dynamic manifestations are not cleaned up")
    require(not manifestations["old_instance_cannot_progress"]
            or "INSTANCE_DATA" in manifestation_source,
            "dynamic manifestations are not bound to a case instance")
    require(not manifestations["subject_moves_with_schedule"]
            or ("SCHEDULE_DATA" in manifestation_source
                and "scheduleState.observationOpen()" in manifestation_source
                and "plan.routine()" in manifestation_source),
            "dynamic subject does not move with its schedule")
    weekly = dynamic["weekly_rotation"]
    require(f"Math.floorDiv(safeDay, {weekly['days_per_week']}L)"
            in dynamic_generator,
            "dynamic case week duration drifted")
    for organization in weekly["organizations"]:
        require(organization in dynamic_profile,
                f"dynamic weekly organization {organization} is missing")
    for period in weekly["schedule_periods"]:
        require(period in dynamic_profile,
                f"dynamic schedule period {period} is missing")
    for relationship in weekly["relationship_nodes"]:
        require(relationship in dynamic_profile,
                f"dynamic relationship {relationship} is missing")
    require(f"TICKS_PER_PERIOD = "
            f"{weekly['observation_window_ticks']:,}L".replace(",", "_")
            in schedule_source,
            "dynamic observation-window duration drifted")
    require(weekly["observation_is_hard_gate"]
            or "fieldRequirementMet" not in schedule_source,
            "dynamic observation window became a hard quest gate")
    evidence_view_source = EVIDENCE_VIEW.read_text(encoding="utf-8")
    for relation_id in weekly["stable_relation_ids"]:
        require(f'"{relation_id}"' in evidence_view_source,
                f"dynamic schedule relation {relation_id} is missing")
    for grade, adjustment in weekly["grade_feedback"].items():
        require(f"case {grade} -> {adjustment}" in feedback_source,
                f"dynamic organization feedback for grade {grade} drifted")
    require(not weekly["persists_in_org_reputation"]
            or ("applyOrganizationFeedback" in commission_source
                and "orgReputation.put" in dynamic_service
                and "PlayerDataSection.SOCIAL" in dynamic_service),
            "dynamic organization feedback is not persisted")
    item_registry = ITEMS.read_text(encoding="utf-8")
    for evidence_id in dynamic["sealed_evidence_items"]:
        path = evidence_id.split(":", 1)[1]
        model = ROOT / "src" / "main" / "resources" / "assets" \
            / "lord_of_mysteries" / "models" / "item" / f"{path}.json"
        require(path in item_registry and model.exists(),
                f"sealed dynamic evidence {evidence_id} is missing")
        require(evidence_id in quests[dynamic["quest"]]["links"]["produces"],
                f"sealed dynamic evidence {evidence_id} is not linked")
    evidence_policy = dynamic["sealed_evidence_policy"]
    require(not evidence_policy["instance_bound_nbt"]
            or ("dynamic_case_instance" in evidence_data_source
                and "dynamic_case_evidence_theme" in evidence_data_source),
            "sealed evidence is not bound to its case instance and theme")
    require(not evidence_policy["issued_after_scene"]
            or "synchronizeEvidenceSamples" in dynamic_service,
            "sealed evidence is not issued after scene collection")
    require(not evidence_policy["party_sync"]
            or "QuestPartyService.participants" in dynamic_service,
            "sealed evidence is not synchronized to online participants")
    require(not evidence_policy["late_join_restore"]
            or "hasEvidenceSample" in dynamic_service,
            "late-joining participants cannot restore sealed evidence")
    require(not evidence_policy["board_recovery"]
            or "giveEvidenceSample" in dynamic_service,
            "sealed evidence cannot be recovered at the board")
    require(not evidence_policy["returned_on_settle_or_abandon"]
            or "DynamicCaseEvidenceItem.matches" in dynamic_service,
            "sealed evidence is not returned with the portfolio")
    require(not dynamic["stable_from_world_seed_and_accept_tick"]
            or ("generateForDay" in dynamic_generator
                and "worldSeed" in dynamic_generator
                and "commissionAcceptedTick" in dynamic_service),
            "dynamic case identity is not deterministic across restarts")
    require(not dynamic["preserves_capability_schema"]
            or contract["capability_schema"] == 19,
            "dynamic case unexpectedly changed capability schema")
    require(not dynamic["preserves_packet_count"]
            or contract["investigation_board"]["packet_count"] == 14,
            "dynamic case unexpectedly changed packet count")

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
            and "reconcile" in party_service
            and "retainMembership" in party_service
            and "completedCommissions" in party_service,
            "party persistence service is incomplete")
    require("QuestPartySavedData.get" in command_source
            and "party_storage=" in command_source
            and "active_parties=" in command_source
            and "party_members=" in command_source,
            "dedicated-server diagnostics do not verify party storage")
    require("PlayerLoggedInEvent" in progress_handler
            and "PlayerLoggedOutEvent" in progress_handler,
            "party login/logout recovery hooks are missing")
    require("markSettled" in commission_service,
            "individual party settlement tracking is missing")
    require("markSettled(UUID member" in party_saved_data
            and "retainMembership(UUID member" in party_saved_data,
            "party membership lifecycle guards are missing")
    require(party["maximum_party"] == 4,
            "party recovery contract maximum changed")

    board = contract["investigation_board"]
    board_block = BOARD_BLOCK.read_text(encoding="utf-8")
    board_service = BOARD_SERVICE.read_text(encoding="utf-8")
    board_state = BOARD_STATE.read_text(encoding="utf-8")
    board_screen = BOARD_SCREEN.read_text(encoding="utf-8")
    network_protocol = NETWORK_PROTOCOL.read_text(encoding="utf-8")
    network = NETWORK.read_text(encoding="utf-8")
    protocol_match = re.search(r'VERSION\s*=\s*"(\d+)"', network_protocol)
    packet_count_match = re.search(r"PACKET_COUNT\s*=\s*(\d+)", network_protocol)
    require(protocol_match is not None
            and int(protocol_match.group(1)) == board["network_protocol"],
            "investigation board protocol drifted")
    require(packet_count_match is not None
            and int(packet_count_match.group(1)) == board["packet_count"],
            "investigation board packet count drifted")
    require("InvestigationBoardService.openFromBoard" in board_block,
            "commission board does not open the investigation interface")
    require(not board["requires_proximity"]
            or ("isNearBoard" in board_service and "COMMISSION_BOARD" in board_service),
            "investigation board actions are not proximity-gated")
    require("InvestigationBoardS2CPacket" in network
            and "InvestigationBoardActionC2SPacket" in network,
            "investigation board packets are not registered")
    for state in board["states"]:
        require(state in board_state,
                f"investigation board state {state} is missing")
    require("state.name().toLowerCase" in board_screen,
            "investigation board state labels are not localized")
    require(f'literal("{board["command"]}")' in command_source,
            "investigation board command is missing")

    evidence = contract["evidence_archive"]
    evidence_view = EVIDENCE_VIEW.read_text(encoding="utf-8")
    evidence_state = EVIDENCE_STATE.read_text(encoding="utf-8")
    formula_service = FORMULA_SERVICE.read_text(encoding="utf-8")
    board_packet = BOARD_PACKET.read_text(encoding="utf-8")
    for case_id in evidence["cases"]:
        require(f'"{case_id}"' in evidence_view,
                f"evidence archive misses case {case_id}")
    for state in evidence["states"]:
        require(state in evidence_state,
                f"evidence state {state} is missing")
    for clue in evidence["formula_clues"]:
        require(f'"{clue}"' in evidence_view,
                f"formula evidence clue {clue} is missing")
    require(not evidence["server_authoritative"]
            or ("DossierEvidence" in formula_service
                and "CaseEvidenceView" in board_packet
                and "renderEvidenceEntry" in board_screen),
            "server-authoritative evidence archive is incomplete")

    reasoning = contract["evidence_reasoning"]
    analysis_stage = ANALYSIS_STAGE.read_text(encoding="utf-8")
    relation_kind = RELATION_KIND.read_text(encoding="utf-8")
    analysis_service = ANALYSIS_SERVICE.read_text(encoding="utf-8")
    recovery_policy = RECOVERY_POLICY.read_text(encoding="utf-8")
    for stage in reasoning["stages"]:
        require(stage in analysis_stage,
                f"case analysis stage {stage} is missing")
    for kind in reasoning["relation_kinds"]:
        require(kind in relation_kind,
                f"evidence relation kind {kind} is missing")
    for token in reasoning["commands"]:
        require(f'literal("{token}")' in command_source,
                f"case reasoning command {token} is missing")
    require(not reasoning["shows_confidence"]
            or ("confidence" in evidence_view
                and "analysis.confidence" in board_screen),
            "case analysis confidence is not displayed")
    require(not reasoning["shows_next_action"]
            or ("nextActionKey" in evidence_view
                and "analysis.next_action" in board_screen),
            "case analysis next action is not displayed")
    require("relations" in board_packet
            and "renderAnalysisRelation" in board_screen
            and "showAnalysis" in analysis_service
            and "showArchive" in analysis_service,
            "evidence relation analysis is incomplete")
    require(reasoning["undiscovered_relations_sent"]
            or "relation.state() != EvidenceState.MISSING" in evidence_view,
            "undiscovered evidence relations must not enter the client snapshot")
    require("restoreCommissionPaper" in recovery_policy
            and "restoreFormulaDossier" in recovery_policy
            and "recoveredDossierAppraised" in recovery_policy
            and "createRecoveryDossier" in commission_service
            and "recoverCaseItems" in commission_service,
            "case item recovery policy is incomplete")
    require(not reasoning["recovery_requires_board"]
            or "InvestigationBoardService.isNearBoard" in commission_service,
            "case recovery is not investigation-board gated")
    require(not reasoning["preserves_packet_count"]
            or board["packet_count"] == 14,
            "case reasoning unexpectedly changed packet count")

    debrief = contract["case_debrief"]
    debrief_record = DEBRIEF_RECORD.read_text(encoding="utf-8")
    debrief_service = DEBRIEF_SERVICE.read_text(encoding="utf-8")
    case_grade = CASE_GRADE.read_text(encoding="utf-8")
    debrief_focus = DEBRIEF_FOCUS.read_text(encoding="utf-8")
    require(sum(debrief["score_components"].values()) == 100,
            "case debrief component weights must total 100")
    for component, maximum in debrief["score_components"].items():
        require(f"{component}Score" in debrief_record
                and str(maximum) in debrief_record,
                f"case debrief component {component} drifted")
    for grade in debrief["grades"]:
        require(grade in case_grade,
                f"case grade {grade} is missing")
    for grade, minimum in debrief["grade_minimums"].items():
        require(grade in case_grade
                and (grade == "D" or str(minimum) in case_grade),
                f"case grade threshold {grade} drifted")
    for focus in debrief["focuses"]:
        require(focus in debrief_focus,
                f"case debrief focus {focus} is missing")
    require(f'"{debrief["archive_key"]}"' in player_source
            and "caseDebriefs" in player_source
            and "CaseDebriefRecord.load" in player_source,
            "persistent case debrief archive is incomplete")
    require("CaseDebriefService.evaluate" in commission_service
            and "caseDebriefs.put" in commission_service
            and "CaseDebriefService.sendSummary" in commission_service,
            "case settlement does not create and display a debrief")
    require("evidenceScore" in debrief_service
            and "procedureScore" in debrief_service
            and "safetyScore" in debrief_service
            and "efficiencyScore" in debrief_service,
            "case debrief scoring dimensions are incomplete")
    require(not debrief["captures_failed_formula_verdicts"]
            or ("failedAttempts" in formula_service
                and "failedVerdictAttempts" in debrief_service),
            "formula verdict failures are not included in the debrief")
    require(not debrief["captures_unresolved_hypothesis_strain"]
            or "unresolvedReasoningStrain" in debrief_service,
            "unresolved hypothesis strain is not included in the debrief")
    require(debrief["mutates_rewards"]
            or ("moneyPence" not in debrief_service
                and "orgReputation" not in debrief_service
                and "giveItem" not in debrief_service),
            "case debrief unexpectedly mutates settlement rewards")
    require(f'literal("{debrief["command"]}")' in command_source
            and "showDebrief" in analysis_service,
            "case debrief command or archive reader is missing")

    hypothesis = contract["case_hypothesis"]
    hypothesis_record = HYPOTHESIS_RECORD.read_text(encoding="utf-8")
    hypothesis_rules = HYPOTHESIS_RULES.read_text(encoding="utf-8")
    hypothesis_service = HYPOTHESIS_SERVICE.read_text(encoding="utf-8")
    hypothesis_view = HYPOTHESIS_VIEW.read_text(encoding="utf-8")
    hypothesis_stance = HYPOTHESIS_STANCE.read_text(encoding="utf-8")
    hypothesis_status = HYPOTHESIS_STATUS.read_text(encoding="utf-8")
    require(f'MAX_NOTE_LENGTH = {hypothesis["maximum_note_length"]}'
            in hypothesis_record,
            "hypothesis note limit drifted")
    require(f'MAX_STRAIN = {hypothesis["maximum_strain"]}'
            in hypothesis_record,
            "hypothesis strain limit drifted")
    require(f'WRONG_TEST_PRESSURE = {hypothesis["wrong_test_pressure"]}'
            in hypothesis_rules,
            "wrong hypothesis pressure cost drifted")
    require(f'TEST_COOLDOWN_TICKS = {hypothesis["test_cooldown_ticks"]}L'
            in hypothesis_rules,
            "hypothesis test cooldown drifted")
    require(f'RECONSIDER_COOLDOWN_TICKS = '
            f'{hypothesis["reconsider_cooldown_ticks"]}L' in hypothesis_rules,
            "hypothesis reconsider cooldown drifted")
    for stance in hypothesis["stances"]:
        require(stance in hypothesis_stance,
                f"hypothesis stance {stance} is missing")
    for status in hypothesis["statuses"]:
        require(status in hypothesis_status,
                f"hypothesis status {status} is missing")
    for token in hypothesis["commands"]:
        require(f'literal("{token}")' in command_source,
                f"hypothesis command {token} is missing")
    require(not hypothesis["persistent"]
            or (f'"{hypothesis["archive_key"]}"' in player_source
                and "caseHypotheses" in player_source
                and "CaseHypothesisRecord.load" in player_source),
            "persistent hypothesis workspace is incomplete")
    require(not hypothesis["requires_revealed_relation"]
            or ("active.evidence().relations().stream" in hypothesis_service
                and ".id().equals" in hypothesis_service),
            "hypothesis proposals are not limited to revealed relations")
    require(not hypothesis["requires_board_for_mutation"]
            or "InvestigationBoardService.isNearBoard" in hypothesis_service,
            "hypothesis mutations are not investigation-board gated")
    require(not hypothesis["recoverable"]
            or ("reconsider" in hypothesis_rules
                and "record.unresolvedStrain() - 1" in hypothesis_rules
                and "TEST_HYPOTHESIS" in board_screen
                and "RECONSIDER_HYPOTHESIS" in board_screen),
            "hypothesis strain cannot be recovered from the board")
    require("CaseHypothesisView" in board_packet
            and "renderCustomHypothesis" in board_screen
            and "CaseHypothesisView" in hypothesis_view,
            "hypothesis state is not visible on the investigation board")
    require(f'Math.max(0, unresolvedReasoningStrain) * '
            f'{hypothesis["debrief_penalty_per_strain"]}' in debrief_service,
            "hypothesis strain debrief penalty drifted")
    require(hypothesis["mutates_rewards"]
            or ("moneyPence" not in hypothesis_service
                and "orgReputation" not in hypothesis_service
                and "giveItem" not in hypothesis_service),
            "hypothesis testing unexpectedly mutates case rewards")

    newspaper = contract["daily_newspaper"]
    news_logic = NEWS_LOGIC.read_text(encoding="utf-8")
    news_service = NEWS_SERVICE.read_text(encoding="utf-8")
    news_item = NEWS_ITEM.read_text(encoding="utf-8")
    city_life = CITY_LIFE.read_text(encoding="utf-8")
    headline_match = re.search(r"HEADLINE_COUNT\s*=\s*(\d+)", news_logic)
    require(headline_match is not None
            and int(headline_match.group(1)) == newspaper["headline_count"],
            "daily newspaper headline count drifted")
    require(news_logic.count("newspaper.case.")
            == newspaper["case_bulletins"],
            "daily newspaper case bulletin count drifted")
    require("CityNewsService.read" in news_item
            and "newspaper_rumors" in news_service,
            "daily newspaper is not wired to city intelligence")
    require(not newspaper["granted_by_press_shift"]
            or "ModItems.NEWSPAPER" in city_life,
            "press shift does not grant the daily newspaper")
    require(newspaper["knowledge"].split(":", 1)[1] in news_service,
            "daily newspaper knowledge id drifted")

    city_desks = contract["city_service_desks"]
    city_desk_logic = CITY_DESK_LOGIC.read_text(encoding="utf-8")
    city_desk_service = CITY_DESK_SERVICE.read_text(encoding="utf-8")
    npc_handler = NPC_HANDLER.read_text(encoding="utf-8")
    outpost_generator = OUTPOST_GENERATOR.read_text(encoding="utf-8")
    outpost_saved_data = OUTPOST_SAVED_DATA.read_text(encoding="utf-8")
    service_version_match = re.search(
        r"CURRENT_SERVICE_VERSION\s*=\s*(\d+)", outpost_saved_data)
    require(service_version_match is not None
            and int(service_version_match.group(1))
            == city_desks["world_service_version"],
            "city service world version drifted")
    require(f'FIELD_KIT_COST = {city_desks["field_kit_cost"]}L'
            in city_desk_logic,
            "detective field kit price drifted")
    require(f'SAFE_ROOM_COST = {city_desks["safe_room_cost"]}L'
            in city_desk_logic,
            "constabulary safe-room price drifted")
    for desk in city_desks["desks"]:
        require(desk in (npc_handler + city_desk_service).lower(),
                f"city service desk {desk} is missing")
    require("InvestigationBoardService.openNearby" in city_desk_service
            and "requestSafeRoom" in city_desk_service,
            "city service interactions are incomplete")
    require(not city_desks["upgrades_existing_outposts"]
            or ("serviceVersion" in outpost_generator
                and "generateServiceBooths" in outpost_generator
                and '"service_version"' in outpost_saved_data),
            "legacy outposts do not receive service booths")
    require(f'literal("{city_desks["command"]}")' in command_source,
            "city service directory command is missing")

    for locale in ("zh_cn", "en_us"):
        translations = load(LANG / f"{locale}.json")
        missing_keys = set(contract["translation_keys"]) - translations.keys()
        require(not missing_keys,
                f"{locale} misses translations {sorted(missing_keys)}")

    print(
        "M2 investigation contract checked: "
        f"{len(commissions)} commissions, {len(quests)} quest chains, "
        "occultist hut, formula appraisal, three rescue routes, "
        "persistent party recovery, the server-authoritative evidence archive, "
        "case reasoning, recoverable player hypotheses, board-gated item "
        "recovery, persistent case debriefs, "
        "the deterministic daily newspaper, versioned city service desks, "
        "and the recoverable eight-slot dynamic case rotation with physical "
        "scene, witness, and records interactions"
    )


if __name__ == "__main__":
    main()
