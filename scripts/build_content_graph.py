#!/usr/bin/env python3

import csv
import json
import re
import sys
from collections import defaultdict
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
MAIN_DATA = ROOT / "src/main/resources/data/lord_of_mysteries"
GENERATED_DATA = ROOT / "src/generated/resources/data/lord_of_mysteries"
ASSETS = ROOT / "src/main/resources/assets/lord_of_mysteries"
MASTER_CATALOGS = (
    ROOT / "docs/master/m0_content_catalog.json",
    ROOT / "docs/master/m0_runtime_catalog.json",
)
REPORT_ROOT = ROOT / "build/reports/project_mystery"
RESOURCE = re.compile(r"^[a-z0-9_.-]+:[a-z0-9_./-]+$")
METADATA_FIELDS = {
    "schema_version", "canon_status", "source_tier", "source_refs",
    "spoiler_level", "knowledge_gate", "links", "implementation_state",
}
LINK_FIELDS = ("requires", "produces", "used_by", "countered_by")
CANON = {"canon", "adaptation", "original", "placeholder"}
TIERS = {"S", "A", "B-tech", "C", "D"}
STATES = {
    "planned", "data_ready", "code_ready", "asset_ready", "playable",
    "verified",
}
RUNTIME_STATES = {"code_ready", "asset_ready", "playable", "verified"}


def read_json(path):
    with path.open(encoding="utf-8") as stream:
        return json.load(stream)


def write_json(path, payload):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(
        json.dumps(payload, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
        newline="\n",
    )


def write_csv(path, headers, rows):
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8", newline="") as stream:
        writer = csv.DictWriter(stream, fieldnames=headers)
        writer.writeheader()
        writer.writerows(rows)


def validate_metadata(node, origin, errors):
    missing = sorted(METADATA_FIELDS - set(node))
    if missing:
        errors.append(f"{origin}: missing metadata fields {missing}")
        return
    if node["schema_version"] != 4:
        errors.append(f"{origin}: schema_version must be 4")
    if node["canon_status"] not in CANON:
        errors.append(f"{origin}: invalid canon_status")
    if node["source_tier"] not in TIERS:
        errors.append(f"{origin}: invalid source_tier")
    refs = node["source_refs"]
    if not isinstance(refs, list) or not refs or len(refs) != len(set(refs)):
        errors.append(f"{origin}: source_refs must be non-empty and unique")
    spoiler = node["spoiler_level"]
    if not isinstance(spoiler, int) or spoiler < 0 or spoiler > 4:
        errors.append(f"{origin}: spoiler_level must be within 0-4")
    if not RESOURCE.fullmatch(node["knowledge_gate"]):
        errors.append(f"{origin}: invalid knowledge_gate")
    if node["implementation_state"] not in STATES:
        errors.append(f"{origin}: invalid implementation_state")
    if node["canon_status"] == "original" and node["source_tier"] != "D":
        errors.append(f"{origin}: original content must use source_tier D")
    if node["canon_status"] == "canon" and node["source_tier"] in {"C", "D"}:
        errors.append(f"{origin}: canon content requires source tier S or A")
    links = node["links"]
    if not isinstance(links, dict):
        errors.append(f"{origin}: links must be an object")
        return
    for field in LINK_FIELDS:
        values = links.get(field, [])
        if not isinstance(values, list) or len(values) != len(set(values)):
            errors.append(f"{origin}: links.{field} must be a unique list")
            continue
        for value in values:
            if not isinstance(value, str) or not RESOURCE.fullmatch(value):
                errors.append(f"{origin}: invalid links.{field} value {value!r}")


def load_nodes(errors):
    nodes = []
    for catalog_path in MASTER_CATALOGS:
        catalog = read_json(catalog_path)
        if (catalog.get("schema_version") != 4
                or catalog.get("design_version") != "v0.9"):
            errors.append(
                f"{catalog_path.relative_to(ROOT)}: invalid catalog header")
        defaults = catalog.get("defaults", {})
        for raw_entry in catalog.get("entries", []):
            entry = dict(defaults)
            entry.update(raw_entry)
            default_links = defaults.get("links", {})
            entry["links"] = dict(default_links)
            entry["links"].update(raw_entry.get("links", {}))
            nodes.append((entry, str(catalog_path.relative_to(ROOT))))
    for directory in (
            MAIN_DATA / "commissions", MAIN_DATA / "quests",
            MAIN_DATA / "sequences"):
        for path in sorted(directory.glob("*.json")):
            nodes.append((read_json(path), str(path.relative_to(ROOT))))
    for path in sorted((GENERATED_DATA / "pathway_catalog").glob("*.json")):
        nodes.append((read_json(path), str(path.relative_to(ROOT))))
    by_id = {}
    origins = {}
    for node, origin in nodes:
        node_id = node.get("id", "")
        if not isinstance(node_id, str) or not RESOURCE.fullmatch(node_id):
            errors.append(f"{origin}: invalid or missing id")
            continue
        if node_id in by_id:
            errors.append(f"duplicate content id {node_id}: {origins[node_id]}, {origin}")
            continue
        validate_metadata(node, origin, errors)
        by_id[node_id] = node
        origins[node_id] = origin
    return by_id, origins


def runtime_ids():
    ids = set()
    registries = {
        "ModItems.java": ("ITEMS", "item"),
        "ModBlocks.java": ("BLOCKS", "block"),
        "ModEntities.java": ("ENTITIES", "entity"),
    }
    registry_root = ROOT / "src/main/java/top/aurora/lordofmysteries/registry"
    for filename, (registry, _) in registries.items():
        text = (registry_root / filename).read_text(encoding="utf-8")
        if registry == "ITEMS":
            names = set(re.findall(r'simple\("([a-z0-9_.-]+)"\)', text))
        else:
            names = set()
        names.update(re.findall(
            rf'{registry}\.register\(\s*"([a-z0-9_.-]+)"', text))
        ids.update(f"lord_of_mysteries:{name}" for name in names)
    with (ROOT / "docs/recipes_master.csv").open(
            encoding="utf-8-sig", newline="") as stream:
        ids.update(
            f"lord_of_mysteries:{row['recipe_id']}" for row in csv.DictReader(stream)
        )
    return ids


def java_source():
    return "\n".join(
        path.read_text(encoding="utf-8")
        for path in (ROOT / "src/main/java").rglob("*.java")
    )


def validate_runtime(nodes, runtime, java, errors):
    for node_id, node in nodes.items():
        state = node["implementation_state"]
        if state not in RUNTIME_STATES:
            continue
        content_type = node.get("type", "definition")
        if content_type in {"item", "recipe", "block", "entity"}:
            if node_id not in runtime:
                errors.append(f"{node_id}: {state} but no runtime registration/data")
        marker = node.get("runtime_marker")
        if content_type == "status" and (not marker or marker not in java):
            errors.append(f"{node_id}: status runtime_marker is not implemented")
        if content_type == "pathway" and not node.get("implemented_sequences"):
            errors.append(f"{node_id}: playable pathway has no sequences")
        if content_type == "sequence":
            potion_id = node.get("potion_id", "")
            if potion_id not in runtime:
                errors.append(f"{node_id}: sequence potion is not registered: {potion_id}")
            if not node.get("abilities") or not node.get("acting_events"):
                errors.append(f"{node_id}: sequence abilities and acting_events are required")
            for acting_event in node.get("acting_events", []):
                if acting_event.split(":", 1)[-1] not in java:
                    errors.append(f"{node_id}: acting event is not implemented: {acting_event}")


def detect_requires_cycles(nodes, errors):
    visiting = set()
    visited = set()

    def visit(node_id, trail):
        if node_id in visiting:
            cycle = trail[trail.index(node_id):] + [node_id]
            errors.append("requires cycle: " + " -> ".join(cycle))
            return
        if node_id in visited:
            return
        visiting.add(node_id)
        for target in nodes[node_id]["links"].get("requires", []):
            if target in nodes:
                visit(target, trail + [target])
        visiting.remove(node_id)
        visited.add(node_id)

    for node_id in nodes:
        visit(node_id, [node_id])


def language_report(nodes):
    languages = {
        locale: read_json(ASSETS / "lang" / f"{locale}.json")
        for locale in ("zh_cn", "en_us")
    }
    rows = []
    for node_id, node in nodes.items():
        keys = list(node.get("translation_keys", []))
        if node.get("type") == "item" and node_id.startswith("lord_of_mysteries:"):
            keys.append("item.lord_of_mysteries." + node_id.split(":", 1)[1])
        keys.extend(
            node.get(field, "")
            for field in ("display_name_key", "description_key", "title_key", "summary_key")
            if node.get(field)
        )
        for step in node.get("steps", []):
            if step.get("guidance_key"):
                keys.append(step["guidance_key"])
        for key in sorted(set(keys)):
            rows.append({
                "id": node_id,
                "translation_key": key,
                "zh_cn": "present" if key in languages["zh_cn"] else "missing",
                "en_us": "present" if key in languages["en_us"] else "missing",
            })
    return rows


def asset_report(nodes):
    rows = []
    for node_id, node in nodes.items():
        if node.get("type") != "item":
            continue
        path = node_id.split(":", 1)[1]
        model = ASSETS / "models/item" / f"{path}.json"
        rows.append({
            "id": node_id,
            "asset": str(model.relative_to(ROOT)),
            "status": "present" if model.exists() else "missing",
            "implementation_state": node["implementation_state"],
        })
    return rows


def build_reports(nodes, origins, runtime, errors):
    edges = []
    inbound = defaultdict(int)
    outbound = defaultdict(int)
    spoiler_rows = []
    valid = set(nodes) | runtime
    for node_id, node in nodes.items():
        gate = node["knowledge_gate"]
        if gate not in nodes:
            errors.append(f"{node_id}: unknown knowledge_gate {gate}")
        for relation in LINK_FIELDS:
            for target in node["links"].get(relation, []):
                if not target.startswith("minecraft:") and target not in valid:
                    errors.append(f"{node_id}: unknown {relation} target {target}")
                edges.append({"source": node_id, "relation": relation, "target": target})
                outbound[node_id] += 1
                if target in nodes:
                    inbound[target] += 1
                    if node["spoiler_level"] > nodes[target]["spoiler_level"]:
                        spoiler_rows.append({
                            "source": node_id,
                            "source_level": node["spoiler_level"],
                            "relation": relation,
                            "target": target,
                            "target_level": nodes[target]["spoiler_level"],
                            "gate": gate,
                        })
    orphan_rows = [
        {
            "id": node_id,
            "type": node.get("type", "definition"),
            "implementation_state": node["implementation_state"],
            "origin": origins[node_id],
        }
        for node_id, node in nodes.items()
        if inbound[node_id] == 0 and outbound[node_id] == 0
    ]
    localization = language_report(nodes)
    assets = asset_report(nodes)
    for row in localization:
        if "missing" in {row["zh_cn"], row["en_us"]}:
            errors.append(
                f"{row['id']}: missing translation {row['translation_key']}"
            )
    for row in assets:
        if row["status"] == "missing" and row["implementation_state"] in RUNTIME_STATES:
            errors.append(f"{row['id']}: missing item model {row['asset']}")
    write_json(REPORT_ROOT / "content_graph.json", {
        "design_version": "v0.9",
        "schema_version": 4,
        "nodes": [
            {
                "id": node_id,
                "type": node.get("type", "definition"),
                "implementation_state": node["implementation_state"],
                "spoiler_level": node["spoiler_level"],
                "origin": origins[node_id],
            }
            for node_id, node in sorted(nodes.items())
        ],
        "edges": edges,
    })
    write_csv(REPORT_ROOT / "orphan_report.csv",
              ["id", "type", "implementation_state", "origin"], orphan_rows)
    write_csv(REPORT_ROOT / "spoiler_leak_report.csv",
              ["source", "source_level", "relation", "target", "target_level", "gate"],
              spoiler_rows)
    write_csv(REPORT_ROOT / "localization_report.csv",
              ["id", "translation_key", "zh_cn", "en_us"], localization)
    write_csv(REPORT_ROOT / "asset_report.csv",
              ["id", "asset", "status", "implementation_state"], assets)
    write_csv(REPORT_ROOT / "compat_report.csv",
              ["integration", "mode", "state"], [
                  {"integration": "JEI", "mode": "read_only", "state": "planned"},
                  {"integration": "EMI", "mode": "read_only", "state": "planned"},
                  {"integration": "Jade", "mode": "read_only", "state": "planned"},
              ])
    return len(edges), len(orphan_rows), len(spoiler_rows)


def main():
    errors = []
    try:
        nodes, origins = load_nodes(errors)
        runtime = runtime_ids()
        validate_runtime(nodes, runtime, java_source(), errors)
        detect_requires_cycles(nodes, errors)
        edge_count, orphan_count, spoiler_count = build_reports(
            nodes, origins, runtime, errors)
    except (OSError, ValueError, KeyError, TypeError, json.JSONDecodeError) as error:
        errors.append(str(error))
        nodes = {}
        edge_count = orphan_count = spoiler_count = 0
    validation = {
        "design_version": "v0.9",
        "schema_version": 4,
        "node_count": len(nodes),
        "edge_count": edge_count,
        "orphan_count": orphan_count,
        "spoiler_transition_count": spoiler_count,
        "errors": errors,
        "status": "passed" if not errors else "failed",
    }
    write_json(REPORT_ROOT / "pm-content-validation.json", validation)
    if errors:
        for error in errors:
            print(f"content graph error: {error}", file=sys.stderr)
        return 1
    print(
        f"v0.9 content graph checked: {len(nodes)} nodes, {edge_count} edges, "
        f"{orphan_count} orphans, {spoiler_count} gated spoiler transitions"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
