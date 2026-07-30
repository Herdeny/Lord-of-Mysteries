#!/usr/bin/env python3

import argparse
import json
import re
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
REGISTRY_ROOT = ROOT / "src" / "main" / "java" / "top" / "aurora" / "lordofmysteries" / "registry"
ASSET_ROOT = ROOT / "src" / "main" / "resources" / "assets" / "lord_of_mysteries"
DATA_ROOT = ROOT / "src" / "main" / "resources" / "data" / "lord_of_mysteries"
OUTPUT_PATH = ROOT / "docs" / "assets" / "catalog-data.js"
MOD_ID = "lord_of_mysteries"


def unique_matches(path, pattern):
    text = path.read_text(encoding="utf-8")
    values = []
    for match in re.finditer(pattern, text):
        value = next(group for group in match.groups() if group is not None)
        if value not in values:
            values.append(value)
    return values


def load_registry_ids():
    items = unique_matches(
        REGISTRY_ROOT / "ModItems.java",
        r'\bsimple\(\s*"([a-z0-9_]+)"\s*\)|\bITEMS\.register\(\s*"([a-z0-9_]+)"',
    )
    managed_artifacts = unique_matches(
        ROOT / "src" / "main" / "java" / "top" / "aurora"
        / "lordofmysteries" / "artifact" / "ManagedArtifactKind.java",
        r'\b[A-Z][A-Z0-9_]*\(\s*"([a-z0-9_]+)"\s*\)',
    )
    for registry_id in managed_artifacts:
        if registry_id not in items:
            items.append(registry_id)
    blocks = unique_matches(
        REGISTRY_ROOT / "ModBlocks.java",
        r'\bBLOCKS\.register\(\s*"([a-z0-9_]+)"',
    )
    entities = unique_matches(
        REGISTRY_ROOT / "ModEntities.java",
        r'\bENTITIES\.register\(\s*"([a-z0-9_]+)"',
    )
    if not items or not blocks or not entities:
        raise ValueError("无法从 Forge 注册类读取完整物品、方块和实体 ID")
    return items, blocks, entities


def load_language(language):
    path = ASSET_ROOT / "lang" / f"{language}.json"
    return json.loads(path.read_text(encoding="utf-8"))


def load_definitions(directory):
    definitions = {}
    for path in sorted((DATA_ROOT / directory).glob("*.json")):
        definition = json.loads(path.read_text(encoding="utf-8"))
        definition_id = definition.get("id")
        if not definition_id or definition_id in definitions:
            raise ValueError(f"{directory} contains an invalid or duplicate id: {path.name}")
        definitions[definition_id] = definition
    return definitions


def resource_state(kind, registry_id):
    if kind == "entity":
        texture = ASSET_ROOT / "textures" / "entity" / f"{registry_id}.png"
        return "实体纹理已提供" if texture.exists() else "使用代码渲染或共用纹理"
    model_kind = "block" if kind == "block" else "item"
    model = ASSET_ROOT / "models" / model_kind / f"{registry_id}.json"
    if model.exists():
        return f"{model_kind} 模型已提供"
    if kind == "item" and registry_id.endswith("_spawn_egg"):
        return "Forge 生成蛋内置模型"
    return "由方块物品或代码模型提供"


def entry_for(kind, registry_id, zh_cn, en_us):
    translation_kind = "item" if kind == "item" else kind
    translation_key = f"{translation_kind}.{MOD_ID}.{registry_id}"
    if translation_key not in zh_cn or translation_key not in en_us:
        raise ValueError(f"注册内容缺少中英双语名称: {translation_key}")

    display_type = kind
    if kind == "item" and re.search(r"_potion_[0-9]+$", registry_id):
        display_type = "potion"
    summaries = {
        "item": "已在 Forge 物品注册表中实现、具备中英双语名称的可获取物品。",
        "potion": "已注册并可由当前数据配方或玩法链路使用的序列魔药。",
        "block": "已在 Forge 方块注册表中实现并纳入资源完整性校验的可放置方块。",
        "entity": "已在 Forge 实体注册表中实现并具备服务端行为的生物或失控体。",
    }
    source_files = {
        "item": "ModItems.java",
        "block": "ModBlocks.java",
        "entity": "ModEntities.java",
    }
    return {
        "type": display_type,
        "id": f"{MOD_ID}:{registry_id}",
        "name": zh_cn[translation_key],
        "en": en_us[translation_key],
        "summary": summaries[display_type],
        "tags": ["已注册", "中英双语", "自动同步"],
        "details": [
            ["注册表", f"Forge {kind} registry"],
            ["注册 ID", f"{MOD_ID}:{registry_id}"],
            ["代码来源", source_files[kind]],
            ["资源状态", resource_state(kind, registry_id)],
        ],
        "long": (
            "此条目由 <code>scripts/gen_pages_catalog.py</code> 从实际 Forge 注册类和"
            "语言资源自动生成。若注册内容、ID 或翻译发生变化，Pages 图鉴会在构建门禁中"
            "要求同步更新。已有人工详细说明的同 ID 条目会优先保留。"
        ),
    }


def artifact_entry(entry, definition, organizations, zh_cn, en_us):
    effect_key = definition["effect_key"]
    cost_key = definition["cost_key"]
    custody_id = definition["custody_organization"]
    custody = organizations.get(custody_id)
    if effect_key not in zh_cn or effect_key not in en_us:
        raise ValueError(f"artifact definition misses translated effect: {definition['id']}")
    if cost_key not in zh_cn or cost_key not in en_us:
        raise ValueError(f"artifact definition misses translated cost: {definition['id']}")
    if custody is None:
        raise ValueError(f"artifact definition references unknown custodian: {definition['id']}")
    custody_key = custody["title_key"]
    if custody_key not in zh_cn or custody_key not in en_us:
        raise ValueError(f"artifact custodian misses translations: {custody_id}")
    return {
        **entry,
        "type": "artifact",
        "summary": zh_cn[effect_key],
        "tags": [
            "封印物",
            f"危险等级{definition['danger_level']}",
            "保管台账",
            "M4",
        ],
        "details": [
            ["登记编号", definition["id"]],
            ["保管组织", zh_cn[custody_key]],
            ["危险等级", str(definition["danger_level"])],
            ["安全使用", f"{definition['safe_uses']} 次"],
            ["借用期限", f"{definition['loan_days']} 天"],
            ["泄漏阈值", str(definition["leak_threshold"])],
            ["效果", zh_cn[effect_key]],
            ["代价", zh_cn[cost_key]],
        ],
        "long": (
            "该封印物使用世界级保管台账记录唯一实例、责任人、当前持有人、"
            "最后维度与坐标、污染、使用次数、借出日、到期日和事故。"
            "逾期、超出安全使用次数、污染达到阈值、换手或复制会进入泄漏、"
            "回收或滥用处理；来客面具不会绕过任何权限。"
        ),
    }


def organization_entry(definition, zh_cn, en_us):
    title_key = definition["title_key"]
    if title_key not in zh_cn or title_key not in en_us:
        raise ValueError(f"organization misses translations: {definition['id']}")
    kind = "教会" if definition["kind"] == "church" else "隐秘组织"
    strategies = sorted(
        definition["strategy_weights"].items(),
        key=lambda value: (-value[1], value[0]),
    )
    return {
        "type": "org",
        "id": definition["id"],
        "name": zh_cn[title_key],
        "en": en_us[title_key],
        "summary": f"{kind}。每天按数据权重自主生成行动，并使用玩家独立的接取与结算状态。",
        "tags": ["组织", kind, "自主行动", "M4"],
        "details": [
            ["类型", kind],
            ["公开身份", definition["public_front"]],
            ["隐秘单位", "、".join(definition["covert_units"])],
            ["教义", "、".join(definition["doctrines"])],
            ["资源", "、".join(definition["resources"])],
            ["领地", "、".join(definition["territories"])],
            ["盟友", str(len(definition["relations"].get("allies", [])))],
            ["敌对", str(len(definition["relations"].get("enemies", [])))],
            ["行动权重", "、".join(f"{key} {weight:g}" for key, weight in strategies)],
        ],
        "long": (
            "组织定义覆盖公开身份、隐秘单位、教义、资源、领地、关系和行动策略"
            "七个数据面。服务端按世界种子、游戏日和全服神秘暴露确定每日三项"
            "行动；玩家只选择是否参与，不能决定组织是否行动或伪造奖励。"
        ),
    }
def render():
    items, blocks, entities = load_registry_ids()
    zh_cn = load_language("zh_cn")
    en_us = load_language("en_us")
    organizations = load_definitions("organizations")
    artifacts = load_definitions("artifacts")

    block_ids = set(blocks)
    entries = [
        entry_for("item", registry_id, zh_cn, en_us)
        for registry_id in items
        if registry_id not in block_ids
    ]
    entries.extend(entry_for("block", registry_id, zh_cn, en_us) for registry_id in blocks)
    entries.extend(entry_for("entity", registry_id, zh_cn, en_us) for registry_id in entities)
    entries = [
        artifact_entry(
            entry,
            artifacts[entry["id"]],
            organizations,
            zh_cn,
            en_us,
        ) if entry["id"] in artifacts else entry
        for entry in entries
    ]
    entries.extend(
        organization_entry(definition, zh_cn, en_us)
        for definition in organizations.values()
    )
    entries.sort(key=lambda entry: (entry["type"], entry["id"]))

    metadata = {
        "registeredItems": len(items),
        "registeredBlocks": len(blocks),
        "registeredEntities": len(entities),
        "organizationDefinitions": len(organizations),
        "artifactDefinitions": len(artifacts),
        "uniqueRegistryEntries": len(entries),
        "source": "Forge registries + organization/artifact data + zh_cn/en_us",
    }
    encoded_entries = json.dumps(entries, ensure_ascii=False, indent=2)
    encoded_metadata = json.dumps(metadata, ensure_ascii=False, indent=2)
    return (
        "/* Generated by scripts/gen_pages_catalog.py. Do not edit manually. */\n"
        "window.LOM = window.LOM || {};\n"
        "(function (D) {\n"
        f"  var generatedEntries = {encoded_entries};\n"
        "  var manualEntries = Array.isArray(D.entries) ? D.entries : [];\n"
        "  var manualIds = {};\n"
        "  manualEntries.forEach(function (entry) { manualIds[entry.id] = true; });\n"
        "  D.entries = manualEntries.concat(generatedEntries.filter(function (entry) {\n"
        "    return !manualIds[entry.id];\n"
        "  }));\n"
        f"  D.catalogMeta = {encoded_metadata};\n"
        "})(window.LOM);\n"
    )


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true", help="只检查 Pages 图鉴是否最新")
    args = parser.parse_args()

    try:
        expected = render()
    except (OSError, ValueError, json.JSONDecodeError) as error:
        print(f"pages catalog error: {error}", file=sys.stderr)
        return 1

    current = OUTPUT_PATH.read_text(encoding="utf-8") if OUTPUT_PATH.exists() else ""
    if args.check:
        if current != expected:
            print(
                "docs/assets/catalog-data.js 与实际 Forge 注册内容不同步；"
                "请运行 python scripts/gen_pages_catalog.py",
                file=sys.stderr,
            )
            return 1
        print("pages catalog checked")
        return 0

    OUTPUT_PATH.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT_PATH.write_text(expected, encoding="utf-8", newline="\n")
    print("pages catalog updated")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
