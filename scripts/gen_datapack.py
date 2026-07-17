#!/usr/bin/env python3

import argparse
import csv
import json
import re
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
PATHWAYS_CSV = ROOT / "docs" / "pathways_master.csv"
RECIPES_CSV = ROOT / "docs" / "recipes_master.csv"
GENERATED_ROOT = (
    ROOT / "src" / "generated" / "resources" / "data" / "lord_of_mysteries"
)
MAIN_DATA_ROOT = ROOT / "src" / "main" / "resources" / "data" / "lord_of_mysteries"
COMMISSIONS_ROOT = MAIN_DATA_ROOT / "commissions"
QUESTS_ROOT = MAIN_DATA_ROOT / "quests"
LANG_ROOT = (
    ROOT / "src" / "main" / "resources" / "assets" / "lord_of_mysteries" / "lang"
)
ID_PATTERN = re.compile(r"^[a-z0-9_.-]+$")
RESOURCE_PATTERN = re.compile(r"^[a-z0-9_.-]+:[a-z0-9_./-]+$")
QUEST_OBJECTIVE_TYPES = {
    "talk_npc",
    "enter_structure",
    "pickup",
    "encounter",
    "reach_sequence",
    "rescue",
    "survive_waves",
    "deliver",
    "brew_quality",
    "divine_success",
    "ritual_outcome",
    "escort",
    "collect_set",
    "reputation_reach",
    "custom_callback",
}


def read_rows(path):
    with path.open(encoding="utf-8-sig", newline="") as stream:
        return list(csv.DictReader(stream))


def require_id(value, field):
    if not ID_PATTERN.fullmatch(value):
        raise ValueError(f"{field} 不是合法 ID: {value!r}")
    return value


def require_resource(value, field):
    if not RESOURCE_PATTERN.fullmatch(value):
        raise ValueError(f"{field} 不是合法资源位置: {value!r}")
    return value


def split_values(value):
    return [part.strip() for part in value.split(";") if part.strip()]


def render_pathways(rows):
    expected = {}
    seen = set()
    statuses = {"implemented", "m2_foundation", "planned", "future"}
    for row in rows:
        pathway_id = require_id(row["pathway_id"].strip(), "pathway_id")
        if pathway_id in seen:
            raise ValueError(f"重复 pathway_id: {pathway_id}")
        seen.add(pathway_id)
        status = row["status"].strip()
        if status not in statuses:
            raise ValueError(f"{pathway_id} status 无效: {status}")
        sequences = [int(value) for value in split_values(
            row["implemented_sequences"])]
        if any(sequence < 0 or sequence > 9 for sequence in sequences):
            raise ValueError(f"{pathway_id} implemented_sequences 超出 0-9")
        payload = {
            "id": f"lord_of_mysteries:{pathway_id}",
            "type": "pathway",
            "schema_version": 4,
            "canon_status": "adaptation",
            "source_tier": "B-tech",
            "source_refs": ["GDD:v0.9/57", "REPO:docs/pathways_master.csv"],
            "spoiler_level": 1,
            "knowledge_gate": "lord_of_mysteries:knowledge/pathway_catalog",
            "links": {
                "requires": [],
                "produces": [],
                "used_by": [],
                "countered_by": [],
            },
            "implementation_state": (
                "playable"
                if status in {"implemented", "m2_foundation"}
                else "planned"
            ),
            "group": require_id(row["group"].strip(), "group"),
            "display_name_key": row["display_name_key"].strip(),
            "description_key": row["description_key"].strip(),
            "color": row["color"].strip(),
            "status": status,
            "implemented_sequences": sequences,
        }
        expected[GENERATED_ROOT / "pathway_catalog" / f"{pathway_id}.json"] = payload
    return expected


def parse_ingredient(value):
    raw = value.strip()
    resource, separator, count_text = raw.rpartition("*")
    if not separator:
        return require_resource(raw, "ingredient"), 1
    count = int(count_text)
    if count < 1 or count > 9:
        raise ValueError(f"ingredient count 必须在 1-9: {raw}")
    return require_resource(resource, "ingredient"), count


def render_recipes(rows):
    expected = {}
    seen = set()
    for row in rows:
        recipe_id = require_id(row["recipe_id"].strip(), "recipe_id")
        if recipe_id in seen:
            raise ValueError(f"重复 recipe_id: {recipe_id}")
        seen.add(recipe_id)
        carrier = row["carrier"].strip()
        if carrier != "bench":
            raise ValueError(f"{recipe_id} 当前生成器仅支持 bench")
        ingredients = []
        for raw in split_values(row["ingredients"]):
            resource, count = parse_ingredient(raw)
            ingredients.extend({"item": resource} for _ in range(count))
        if not ingredients or len(ingredients) > 9:
            raise ValueError(f"{recipe_id} 工作台材料数量必须在 1-9")
        result_count = int(row["result_count"])
        if result_count < 1 or result_count > 64:
            raise ValueError(f"{recipe_id} result_count 必须在 1-64")
        if row["unlock"].strip() not in {"free", "knowledge", "quest"}:
            raise ValueError(f"{recipe_id} unlock 无效")
        if row["jei_visible"].strip().lower() not in {"true", "false"}:
            raise ValueError(f"{recipe_id} jei_visible 必须是 true/false")
        payload = {
            "type": "minecraft:crafting_shapeless",
            "ingredients": ingredients,
            "result": {
                "item": require_resource(row["result_id"].strip(), "result_id"),
                "count": result_count,
            },
        }
        expected[GENERATED_ROOT / "recipes" / f"{recipe_id}.json"] = payload
    return expected


def encode(payload):
    return json.dumps(payload, ensure_ascii=False, indent=2) + "\n"


def load_json_files(root):
    payloads = {}
    for path in sorted(root.glob("*.json")):
        with path.open(encoding="utf-8") as stream:
            payloads[path] = json.load(stream)
    return payloads


def load_language_keys():
    languages = {}
    for locale in ("zh_cn", "en_us"):
        path = LANG_ROOT / f"{locale}.json"
        with path.open(encoding="utf-8") as stream:
            payload = json.load(stream)
        if not isinstance(payload, dict):
            raise ValueError(f"{path.name} 必须是 JSON object")
        languages[locale] = set(payload)
    return languages


def require_translation(key, field, languages):
    if not key:
        raise ValueError(f"{field} 缺少翻译 key")
    missing = [locale for locale, keys in languages.items() if key not in keys]
    if missing:
        raise ValueError(f"{field} 翻译缺失 {missing}: {key}")


def validate_quests(languages):
    payloads = load_json_files(QUESTS_ROOT)
    ids = set()
    for path, payload in payloads.items():
        quest_id = require_resource(payload["id"], f"{path.name}.id")
        if quest_id in ids:
            raise ValueError(f"重复 quest id: {quest_id}")
        ids.add(quest_id)
        require_translation(payload.get("title_key", ""),
                            f"{quest_id}.title_key", languages)
        steps = payload.get("steps", [])
        if not steps:
            raise ValueError(f"{quest_id} 必须至少有一个 step")
        step_ids = set()
        for step in steps:
            step_id = require_id(step["id"], f"{quest_id}.step.id")
            if step_id in step_ids:
                raise ValueError(f"{quest_id} 重复 step id: {step_id}")
            step_ids.add(step_id)
            require_translation(step.get("guidance_key", ""),
                                f"{quest_id}/{step_id}.guidance_key", languages)
            objective = step.get("objective", {})
            objective_type = objective.get("type")
            if objective_type not in QUEST_OBJECTIVE_TYPES:
                raise ValueError(
                    f"{quest_id}/{step_id} objective type 无效: {objective_type}"
                )
            count = int(objective.get("count", 1))
            if count < 1:
                raise ValueError(f"{quest_id}/{step_id} count 必须大于 0")
            target = objective.get("target", "")
            if objective_type != "encounter" and not target:
                raise ValueError(f"{quest_id}/{step_id} target 不能为空")
            if objective_type == "pickup":
                require_resource(target, f"{quest_id}/{step_id}.target")
        if payload.get("fail_policy", "step_retry") not in {
            "step_retry", "chain_retry", "abandon"
        }:
            raise ValueError(f"{quest_id} fail_policy 无效")
        coop = payload.get("coop", {})
        shared = bool(coop.get("shared_progress", False))
        maximum_party = int(coop.get("max_party", 1))
        if maximum_party < 1 or maximum_party > 4:
            raise ValueError(f"{quest_id} max_party 必须在 1-4")
        if not shared and maximum_party != 1:
            raise ValueError(f"{quest_id} 未开启共享进度时 max_party 必须为 1")
    return ids, len(payloads)


def validate_commissions(quest_ids, languages):
    payloads = load_json_files(COMMISSIONS_ROOT)
    ids = set()
    for path, payload in payloads.items():
        commission_id = require_resource(payload["id"], f"{path.name}.id")
        if commission_id in ids:
            raise ValueError(f"重复 commission id: {commission_id}")
        ids.add(commission_id)
    for path, payload in payloads.items():
        commission_id = require_resource(payload["id"], f"{path.name}.id")
        require_translation(payload.get("title_key", ""),
                            f"{commission_id}.title_key", languages)
        require_translation(payload.get("summary_key", ""),
                            f"{commission_id}.summary_key", languages)
        boards = payload.get("board", [])
        if not boards or len(boards) != len(set(boards)):
            raise ValueError(f"{commission_id} board 必须非空且不能重复")
        solutions = payload.get("solutions", [])
        if len(solutions) < 2:
            raise ValueError(f"{commission_id} 至少需要两种解法")
        if len(solutions) != len(set(solutions)) or any(not value for value in solutions):
            raise ValueError(f"{commission_id} solutions 不能重复或为空")
        level_range = payload.get("level_range", [])
        if len(level_range) != 2:
            raise ValueError(f"{commission_id} level_range 必须有两个值")
        minimum_level, maximum_level = map(int, level_range)
        if minimum_level < 0 or maximum_level > 9 or minimum_level > maximum_level:
            raise ValueError(f"{commission_id} level_range 必须按 0-9 升序")
        reward = payload.get("reward", {})
        if int(reward.get("pence", 0)) < 0:
            raise ValueError(f"{commission_id} reward.pence 不能为负数")
        for organization, amount in reward.get("reputation", {}).items():
            require_resource(organization, f"{commission_id}.reward.reputation")
            int(amount)
        quest_id = require_resource(
            payload["quest_chain"], f"{commission_id}.quest_chain"
        )
        if quest_id not in quest_ids:
            raise ValueError(f"{commission_id} 引用了不存在的任务链: {quest_id}")
        prerequisites = payload.get("prerequisites", [])
        if len(prerequisites) != len(set(prerequisites)):
            raise ValueError(f"{commission_id} prerequisites 不能重复")
        for prerequisite in prerequisites:
            prerequisite_id = require_resource(
                prerequisite, f"{commission_id}.prerequisites"
            )
            if prerequisite_id == commission_id:
                raise ValueError(f"{commission_id} 不能依赖自身")
            if prerequisite_id not in ids:
                raise ValueError(
                    f"{commission_id} 引用了不存在的前置委托: {prerequisite_id}"
                )
        if int(payload.get("cooldown_hours", 0)) < 0:
            raise ValueError(f"{commission_id} cooldown_hours 不能为负数")
    return len(payloads)


def expected_files():
    pathways = read_rows(PATHWAYS_CSV)
    recipes = read_rows(RECIPES_CSV)
    expected = {}
    expected.update(render_pathways(pathways))
    expected.update(render_recipes(recipes))
    manifest = {
        "design_version": "v0.9",
        "content_schema_version": 4,
        "generator": "scripts/gen_datapack.py",
        "sources": [
            "docs/pathways_master.csv",
            "docs/recipes_master.csv",
            "docs/Project_Mystery_v0_9_manifest.json",
            "docs/master/m0_content_catalog.json",
            "docs/master/m0_runtime_catalog.json",
        ],
        "pathway_count": len(pathways),
        "generated_recipe_count": len(recipes),
    }
    expected[
        GENERATED_ROOT / "project_mystery" / "generated_manifest.json"
    ] = manifest
    return expected


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    try:
        expected = expected_files()
        languages = load_language_keys()
        quest_ids, quest_count = validate_quests(languages)
        commission_count = validate_commissions(quest_ids, languages)
    except (OSError, ValueError, KeyError, csv.Error, json.JSONDecodeError) as error:
        print(f"datapack generation error: {error}", file=sys.stderr)
        return 1

    stale = []
    for path, payload in expected.items():
        content = encode(payload)
        current = path.read_text(encoding="utf-8") if path.exists() else ""
        if current == content:
            continue
        stale.append(path.relative_to(ROOT))
        if not args.check:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(content, encoding="utf-8", newline="\n")

    if args.check and stale:
        print("以下生成文件过期或缺失：", file=sys.stderr)
        for path in stale:
            print(f"- {path}", file=sys.stderr)
        print("请运行: python scripts/gen_datapack.py", file=sys.stderr)
        return 1

    action = "checked" if args.check else "generated"
    print(
        f"project datapack {action}: {len(expected)} generated files, "
        f"{commission_count} commissions, {quest_count} quest chains"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
