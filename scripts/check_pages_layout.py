#!/usr/bin/env python3
"""Validate the GitHub Pages structure, content baseline, and interactions."""

from __future__ import annotations

import json
import sys
from html.parser import HTMLParser
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
HTML_PATH = ROOT / "docs" / "index.html"
CSS_PATH = ROOT / "docs" / "assets" / "wiki.css"
JS_PATH = ROOT / "docs" / "assets" / "wiki.js"
WIKI_DATA_PATH = ROOT / "docs" / "assets" / "wiki-data.js"
CATALOG_DATA_PATH = ROOT / "docs" / "assets" / "catalog-data.js"
STATUS_PATH = ROOT / "project-status.json"


class SiteParser(HTMLParser):
    def __init__(self) -> None:
        super().__init__()
        self.ids: list[str] = []
        self.sections: list[tuple[str, str]] = []
        self.scripts: list[str] = []
        self.stylesheets: list[str] = []
        self.tags: dict[str, int] = {}
        self.language = ""
        self.has_viewport = False

    def handle_starttag(
            self, tag: str,
            attrs: list[tuple[str, str | None]]) -> None:
        attributes = dict(attrs)
        self.tags[tag] = self.tags.get(tag, 0) + 1
        element_id = attributes.get("id")
        if element_id:
            self.ids.append(element_id)
        if tag == "html":
            self.language = attributes.get("lang", "")
        elif tag == "meta" and attributes.get("name") == "viewport":
            self.has_viewport = True
        elif tag == "section" and element_id:
            self.sections.append((element_id, attributes.get("class", "")))
        elif tag == "script" and attributes.get("src"):
            self.scripts.append(attributes["src"] or "")
        elif tag == "link" and attributes.get("rel") == "stylesheet":
            self.stylesheets.append(attributes.get("href", "") or "")


def require(condition: bool, message: str, errors: list[str]) -> None:
    if not condition:
        errors.append(message)


def contains_all(text: str, values: tuple[str, ...]) -> bool:
    return all(value in text for value in values)


def main() -> int:
    html = HTML_PATH.read_text(encoding="utf-8")
    css = CSS_PATH.read_text(encoding="utf-8")
    javascript = JS_PATH.read_text(encoding="utf-8")
    wiki_data = WIKI_DATA_PATH.read_text(encoding="utf-8")
    catalog_data = CATALOG_DATA_PATH.read_text(encoding="utf-8")
    status = json.loads(STATUS_PATH.read_text(encoding="utf-8"))
    asset_version = status["version"].split("-", 1)[0]
    parser = SiteParser()
    parser.feed(html)
    errors: list[str] = []

    required_ids = {
        "main-content", "start", "mechanics", "pathways", "sequences",
        "sequence-tabs", "seq-ladder", "catalog", "search",
        "filter-group", "cards", "load-more", "roadmap", "resources",
        "modal",
    }
    require(parser.language == "zh-CN", "HTML lang must be zh-CN", errors)
    require(parser.has_viewport, "Pages is missing the viewport meta tag", errors)
    require(parser.tags.get("main", 0) == 1, "Pages must contain one main", errors)
    require(parser.tags.get("h1", 0) == 1, "Pages must contain one h1", errors)
    require(
        len(parser.ids) == len(set(parser.ids)),
        "Pages contains duplicate DOM ids", errors)
    require(
        required_ids.issubset(parser.ids),
        "Pages is missing a required interaction id", errors)
    require(
        all("reveal" not in classes.split() for _, classes in parser.sections),
        "Content sections must not depend on reveal for visibility", errors)

    local_assets = parser.scripts + parser.stylesheets
    require(
        all(asset.startswith("assets/") for asset in local_assets),
        "Scripts and stylesheets must be local docs/assets files", errors)
    require(
        all(f"?v={asset_version}" in asset for asset in local_assets),
        f"Static asset cache versions must be {asset_version}", errors)

    require(
        contains_all(
            html,
            ("三槽持久秘偶编队", "/pm marionette", "三种战术",
             "离线休眠", "/pm travel", "/pm m3 team")),
        "Pages must retain the implemented M3 multiplayer entry points", errors)
    require(
        contains_all(
            html,
            ("M4 首批闭环", "7 / 24 封印物", "/pm m4",
             "组织行动与封印物保管", "12份组织定义", "7份封印物定义")),
        "Pages must expose the current M4 playable loop and truthful scope", errors)
    require(
        contains_all(
            html,
            ("精确玩家皮肤/模型/声线", "具体领地 Mod 适配归入 M7",
             "完整目标仍为24件逐件验证封印物")),
        "Pages must retain the M3 and M4 release boundaries", errors)
    require(
        "持久秘偶、完整梦境" not in html,
        "Pages must not list persistent marionettes as unfinished", errors)

    require(
        contains_all(
            wiki_data,
            ("Capability schema 30", "439 JUnit · 24 GameTest",
             "389 JSON · 1741 双语键", "115 物品 · 7 方块 · 16 实体",
             "147 节点 · 197 关系", "237 条图鉴")),
        "Dynamic Pages cards do not match the current validation baseline", errors)
    require(
        contains_all(
            wiki_data,
            ("M4 组织行动与封印物保管", "/pm organization",
             "/pm artifact", "7 / 24", "普通玩家只能查看自己负责或持有")),
        "Dynamic Pages cards are missing the M4 authority and privacy model", errors)
    require(
        contains_all(
            catalog_data,
            ('"organizationDefinitions": 12', '"artifactDefinitions": 7',
             '"registeredItems": 115',
             '"id": "lord_of_mysteries:organization/church_night_watch"',
             '"id": "lord_of_mysteries:artifact_3_091_kindly_umbrella"')),
        "Generated Pages catalog is missing M4 definitions or registrations", errors)

    stale_markers = (
        "427 JUnit · 22 GameTest",
        "365 JSON · 1631 双语键",
        "109 物品 · 7 方块 · 16 实体",
        "126 节点 · 188 关系",
        "218 条图鉴",
        "Capability schema 29",
    )
    require(
        not any(marker in wiki_data for marker in stale_markers),
        "Dynamic Pages cards still contain a previous current baseline", errors)

    require(
        "[hidden] { display: none !important; }" in css,
        "CSS must preserve native hidden semantics", errors)
    require(
        "scroll-margin-top: 0" in css,
        "Sections must not double-apply the top anchor offset", errors)
    require(
        "@media (max-width: 820px)" in css
        and "@media (max-width: 560px)" in css,
        "Pages is missing tablet or phone breakpoints", errors)
    require(
        ".filter-group" in css and "overflow-x: auto" in css,
        "Mobile catalog filters must support horizontal scrolling", errors)
    require(
        ".reveal { opacity: 0" not in css,
        "CSS must not hide content behind reveal animations", errors)

    require(
        "var PAGE_SIZE = 24;" in javascript,
        "Catalog page size must remain 24", errors)
    require(
        "visibleLimit += PAGE_SIZE" in javascript,
        "Catalog is missing incremental loading", errors)
    require(
        "function restoreHashTarget()" in javascript
        and 'style.scrollBehavior = "auto"' in javascript,
        "Dynamic rendering must restore URL anchors without long scrolling",
        errors)
    require(
        "modalTrigger.focus()" in javascript,
        "Closing the details modal must restore focus", errors)
    require(
        "aria-selected" in javascript and "aria-pressed" in javascript,
        "Tabs and catalog filters must expose accessible state", errors)

    if errors:
        print("GitHub Pages layout contract failed:", file=sys.stderr)
        for error in errors:
            print(f"- {error}", file=sys.stderr)
        return 1

    print(
        "GitHub Pages layout contract passed: "
        f"{len(parser.sections)} sections, {len(parser.ids)} unique ids, "
        f"asset cache v{asset_version}, M4 overview, catalog batch 24."
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
