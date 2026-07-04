#!/usr/bin/env python3

import argparse
import json
import re
import sys
from datetime import datetime
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
STATUS_PATH = ROOT / "project-status.json"
ROADMAP_PATH = ROOT / "roadmap.json"
README_PATH = ROOT / "README.md"
ROADMAP_DOC_PATH = ROOT / "ROADMAP.md"
PROJECT_META_PATH = ROOT / "docs" / "assets" / "project-meta.js"
ROADMAP_DATA_PATH = ROOT / "docs" / "assets" / "roadmap-data.js"
WIKI_PATHS = [
    ROOT / "wiki" / "Home.md",
    ROOT / "wiki" / "Development-Status.md",
    ROOT / "wiki" / "Versioning-and-Releases.md",
]


def load_status():
    status = json.loads(STATUS_PATH.read_text(encoding="utf-8"))
    required = {
        "schema_version",
        "version",
        "stage",
        "milestone",
        "minecraft_version",
        "forge_version",
        "java_version",
        "last_updated",
        "last_updated_utc",
    }
    missing = sorted(required - status.keys())
    if missing:
        raise ValueError(f"project-status.json 缺少字段: {', '.join(missing)}")
    if status["schema_version"] != 1:
        raise ValueError("不支持的 project-status.json schema_version")
    version_pattern = re.compile(
        rf"^\d+\.\d+\.\d+(?:-[0-9A-Za-z.-]+)?-{re.escape(status['minecraft_version'])}$"
    )
    if not version_pattern.fullmatch(status["version"]):
        raise ValueError("version 必须使用 <SemVer>-<Minecraft 版本> 格式")
    local_time = datetime.fromisoformat(status["last_updated"])
    utc_time = datetime.fromisoformat(status["last_updated_utc"].replace("Z", "+00:00"))
    if local_time.tzinfo is None or utc_time.tzinfo is None:
        raise ValueError("更新时间必须包含时区")
    if local_time.astimezone(utc_time.tzinfo) != utc_time:
        raise ValueError("last_updated 与 last_updated_utc 不一致")
    gradle_properties = {}
    for line in (ROOT / "gradle.properties").read_text(encoding="utf-8").splitlines():
        if "=" in line and not line.lstrip().startswith("#"):
            key, value = line.split("=", 1)
            gradle_properties[key.strip()] = value.strip()
    expected_properties = {
        "minecraft_version": status["minecraft_version"],
        "forge_version": status["forge_version"],
    }
    for key, expected in expected_properties.items():
        if gradle_properties.get(key) != expected:
            raise ValueError(
                f"{key} 与 gradle.properties 不一致: "
                f"{expected!r} != {gradle_properties.get(key)!r}"
            )
    return status, local_time


def load_roadmap(status):
    roadmap = json.loads(ROADMAP_PATH.read_text(encoding="utf-8"))
    required = {
        "schema_version",
        "design_version",
        "current_milestone",
        "gate_policy",
        "milestones",
        "scope_targets",
    }
    missing = sorted(required - roadmap.keys())
    if missing:
        raise ValueError(f"roadmap.json 缺少字段: {', '.join(missing)}")
    if roadmap["schema_version"] != 1:
        raise ValueError("不支持的 roadmap.json schema_version")
    milestones = roadmap["milestones"]
    expected_ids = [f"M{i}" for i in range(8)]
    actual_ids = [milestone.get("id") for milestone in milestones]
    if actual_ids != expected_ids:
        raise ValueError(f"roadmap 里程碑必须按 M0-M7 排列: {actual_ids}")
    active = [m["id"] for m in milestones if m.get("state") == "active"]
    if active != [roadmap["current_milestone"]]:
        raise ValueError("roadmap 必须只有 current_milestone 处于 active")
    if status["milestone"] != roadmap["current_milestone"]:
        raise ValueError("project-status.json milestone 与 roadmap.json 不一致")
    for milestone in milestones:
        for field in ("title", "phase", "state", "summary", "acceptance", "points"):
            if field not in milestone:
                raise ValueError(f"{milestone['id']} 缺少字段: {field}")
        if milestone["state"] not in {"done", "active", "planned", "future"}:
            raise ValueError(f"{milestone['id']} state 无效")
        if not milestone["points"]:
            raise ValueError(f"{milestone['id']} points 不能为空")
    return roadmap


def status_block(status, local_time, prefix="> "):
    offset = local_time.strftime("%z")
    offset_text = f"UTC{offset[:3]}:{offset[3:]}"
    lines = [
        f"当前版本：**`{status['version']}`**",
        f"开发阶段：**{status['stage']}**（{status['milestone']}）",
        (
            f"技术基线：Minecraft **{status['minecraft_version']}** · "
            f"Forge **{status['forge_version']}** · Java **{status['java_version']}**"
        ),
        (
            f"最后更新：**{local_time.strftime('%Y-%m-%d %H:%M:%S')} "
            f"{offset_text}**（`{status['last_updated_utc']}`）"
        ),
    ]
    return "\n".join(prefix + line for line in lines)


def roadmap_table(roadmap):
    labels = {
        "done": "已完成",
        "active": "进行中",
        "planned": "规划",
        "future": "远期",
    }
    lines = [
        f"> 设计基线：**{roadmap['design_version']}** · "
        f"当前里程碑：**{roadmap['current_milestone']}**",
        "",
        "| 里程碑 | 阶段 | 状态 | 目标 |",
        "|---|---|---|---|",
    ]
    for milestone in roadmap["milestones"]:
        lines.append(
            f"| {milestone['id']} | {milestone['phase']} | "
            f"{labels[milestone['state']]} | {milestone['summary']} |"
        )
    lines.extend([
        "",
        f"> 门禁规则：{roadmap['gate_policy']}",
    ])
    return "\n".join(lines)


def render_roadmap_document(roadmap):
    labels = {
        "done": "已完成",
        "active": "进行中",
        "planned": "规划",
        "future": "远期",
    }
    lines = [
        "# Project Mystery 路线图",
        "",
        "<!-- Generated by scripts/sync_project_metadata.py from roadmap.json. -->",
        "",
        f"- 设计基线：**{roadmap['design_version']}**",
        f"- 当前里程碑：**{roadmap['current_milestone']}**",
        f"- 门禁规则：{roadmap['gate_policy']}",
        "",
        "## 阶段总览",
        "",
        roadmap_table(roadmap),
    ]
    for milestone in roadmap["milestones"]:
        duration = milestone["duration_weeks"]
        duration_text = f"{duration} 周" if duration else "未排期"
        lines.extend([
            "",
            f"## {milestone['id']} · {milestone['title']}",
            "",
            f"- 阶段：{milestone['phase']}",
            f"- 状态：{labels[milestone['state']]}",
            f"- 计划周期：{duration_text}",
            f"- 验收：{milestone['acceptance']}",
            "",
        ])
        lines.extend(f"- {point}" for point in milestone["points"])
    lines.extend([
        "",
        "## 内容规模目标",
        "",
        "| 类别 | MVP | EP1 | EP2 |",
        "|---|---:|---:|---:|",
    ])
    keys = [
        ("playable_sequences", "可玩序列"),
        ("abilities", "能力"),
        ("acting_events", "扮演事件"),
        ("potion_recipes", "魔药配方"),
        ("artifacts", "封印物"),
        ("structures", "结构"),
        ("world_events", "世界事件"),
        ("diary_pages", "日记残页"),
    ]
    scopes = roadmap["scope_targets"]
    for key, label in keys:
        lines.append(
            f"| {label} | {scopes['MVP'][key]} | "
            f"{scopes['EP1'][key]} | {scopes['EP2'][key]} |"
        )
    lines.extend([
        "",
        "路线数据的唯一来源是 `roadmap.json`。修改后运行：",
        "",
        "```bash",
        "python scripts/sync_project_metadata.py",
        "python scripts/sync_project_metadata.py --check",
        "```",
        "",
    ])
    return "\n".join(lines)


def replace_block(text, name, content):
    start = f"<!-- {name}:start -->"
    end = f"<!-- {name}:end -->"
    pattern = re.compile(
        rf"{re.escape(start)}.*?{re.escape(end)}",
        flags=re.DOTALL,
    )
    replacement = f"{start}\n{content}\n{end}"
    updated, count = pattern.subn(replacement, text)
    if count != 1:
        raise ValueError(f"{name} 标记块数量应为 1，实际为 {count}")
    return updated


def render_project_meta(status, local_time):
    offset = local_time.strftime("%z")
    display = (
        f"{local_time.strftime('%Y-%m-%d %H:%M:%S')} "
        f"UTC{offset[:3]}:{offset[3:]}"
    )
    payload = {
        "version": status["version"],
        "stage": status["stage"],
        "milestone": status["milestone"],
        "mc": f"Minecraft Java {status['minecraft_version']}",
        "loader": f"Forge {status['forge_version']}",
        "java": status["java_version"],
        "lastUpdated": status["last_updated"],
        "lastUpdatedUtc": status["last_updated_utc"],
        "lastUpdatedDisplay": display,
    }
    encoded = json.dumps(payload, ensure_ascii=False, indent=2)
    return (
        "/* Generated by scripts/sync_project_metadata.py. */\n"
        f"window.LOM_PROJECT_META = {encoded};\n"
    )


def render_roadmap_data(roadmap):
    payload = {
        "designVersion": roadmap["design_version"],
        "currentMilestone": roadmap["current_milestone"],
        "gatePolicy": roadmap["gate_policy"],
        "milestones": roadmap["milestones"],
        "scopeTargets": roadmap["scope_targets"],
    }
    encoded = json.dumps(payload, ensure_ascii=False, indent=2)
    return (
        "/* Generated by scripts/sync_project_metadata.py. */\n"
        f"window.LOM_ROADMAP_META = {encoded};\n"
    )


def expected_files(status, local_time, roadmap):
    expected = {}
    readme = README_PATH.read_text(encoding="utf-8")
    readme = replace_block(
        readme,
        "project-status",
        status_block(status, local_time, prefix="> - "),
    )
    expected[README_PATH] = replace_block(
        readme,
        "roadmap",
        roadmap_table(roadmap),
    )
    wiki_block = status_block(status, local_time, prefix="- ")
    for path in WIKI_PATHS:
        text = path.read_text(encoding="utf-8")
        text = replace_block(text, "project-status", wiki_block)
        if path.name == "Development-Status.md":
            text = replace_block(text, "roadmap", roadmap_table(roadmap))
        expected[path] = text
    expected[PROJECT_META_PATH] = render_project_meta(status, local_time)
    expected[ROADMAP_DATA_PATH] = render_roadmap_data(roadmap)
    expected[ROADMAP_DOC_PATH] = render_roadmap_document(roadmap)
    return expected


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--check",
        action="store_true",
        help="只检查生成内容是否同步，不写入文件",
    )
    args = parser.parse_args()

    try:
        status, local_time = load_status()
        roadmap = load_roadmap(status)
        expected = expected_files(status, local_time, roadmap)
    except (OSError, ValueError, json.JSONDecodeError) as error:
        print(f"metadata sync error: {error}", file=sys.stderr)
        return 1

    stale = []
    for path, content in expected.items():
        current = path.read_text(encoding="utf-8") if path.exists() else ""
        if current == content:
            continue
        stale.append(path.relative_to(ROOT))
        if not args.check:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(content, encoding="utf-8", newline="\n")

    if args.check and stale:
        print("以下文件与 project-status.json / roadmap.json 不同步：", file=sys.stderr)
        for path in stale:
            print(f"- {path}", file=sys.stderr)
        print("请运行: python scripts/sync_project_metadata.py", file=sys.stderr)
        return 1

    action = "checked" if args.check else "updated"
    print(f"project metadata {action}: {len(expected)} files")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
