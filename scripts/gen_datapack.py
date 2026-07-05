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
ID_PATTERN = re.compile(r"^[a-z0-9_.-]+$")
RESOURCE_PATTERN = re.compile(r"^[a-z0-9_.-]+:[a-z0-9_./-]+$")


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


def expected_files():
    pathways = read_rows(PATHWAYS_CSV)
    recipes = read_rows(RECIPES_CSV)
    expected = {}
    expected.update(render_pathways(pathways))
    expected.update(render_recipes(recipes))
    manifest = {
        "design_version": "v0.8",
        "generator": "scripts/gen_datapack.py",
        "sources": [
            "docs/pathways_master.csv",
            "docs/recipes_master.csv",
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
    except (OSError, ValueError, KeyError, csv.Error) as error:
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
    print(f"project datapack {action}: {len(expected)} files")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
