#!/usr/bin/env python3
"""Validate GitHub Pages structure and interaction contracts without a browser."""

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

    def handle_starttag(self, tag: str, attrs: list[tuple[str, str | None]]) -> None:
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


def main() -> int:
    html = HTML_PATH.read_text(encoding="utf-8")
    css = CSS_PATH.read_text(encoding="utf-8")
    javascript = JS_PATH.read_text(encoding="utf-8")
    wiki_data = WIKI_DATA_PATH.read_text(encoding="utf-8")
    status = json.loads(STATUS_PATH.read_text(encoding="utf-8"))
    asset_version = status["version"].split("-", 1)[0]
    parser = SiteParser()
    parser.feed(html)
    errors: list[str] = []

    required_ids = {
        "main-content",
        "start",
        "mechanics",
        "pathways",
        "sequences",
        "sequence-tabs",
        "seq-ladder",
        "catalog",
        "search",
        "filter-group",
        "cards",
        "load-more",
        "roadmap",
        "resources",
        "modal",
    }
    require(parser.language == "zh-CN", "HTML lang 必须是 zh-CN", errors)
    require(parser.has_viewport, "Pages 缺少 viewport meta", errors)
    require(parser.tags.get("main", 0) == 1, "Pages 必须只有一个 main", errors)
    require(parser.tags.get("h1", 0) == 1, "Pages 必须只有一个 h1", errors)
    require(len(parser.ids) == len(set(parser.ids)), "Pages 存在重复 id", errors)
    require(required_ids.issubset(parser.ids), "Pages 缺少关键交互 id", errors)
    require(all("reveal" not in classes.split() for _, classes in parser.sections),
            "内容 section 不得依赖 reveal 才可见", errors)

    local_assets = parser.scripts + parser.stylesheets
    require(all(asset.startswith("assets/") for asset in local_assets),
            "脚本和样式必须使用 docs/assets 本地资源", errors)
    require(all(f"?v={asset_version}" in asset for asset in local_assets),
            f"静态资源缓存版本必须同步为 {asset_version}", errors)
    require("三槽持久秘偶编队" in html and "/pm marionette" in html,
            "Pages M3 入口与进度摘要必须展示已实现的持久秘偶编队", errors)
    require("端点运维" in html and "/pm travel" in html,
            "Pages M3 入口与路线摘要必须展示旅行家端点运维", errors)
    require("持久秘偶、完整梦境" not in html,
            "Pages 不得把已实现的持久秘偶继续列为未完成", errors)
    require("秘偶收纳卷轴" in html and "跨维" in html,
            "Pages M3 入口必须展示已实现的秘偶跨维收纳", errors)
    require("精确玩家皮肤/模型/声线" in html
            and "具体领地 Mod 适配器、组织门权限" in html,
            "Pages 必须保留真实 M3 发布边界，避免误报完成", errors)
    require("Capability schema 29" in wiki_data
            and "416 JUnit · 20 GameTest" in wiki_data
            and "365 JSON · 1608 双语键" in wiki_data
            and "109 物品 · 7 方块 · 16 实体" in wiki_data
            and "217 条图鉴" in wiki_data,
            "Pages 动态机制卡的 schema、测试和资源基线未同步", errors)
    require("权威收纳" in wiki_data
            and "一次性 token" in wiki_data
            and "跨维安全部署" in wiki_data,
            "Pages 动态 M3 卡必须展示秘偶权威收纳闭环", errors)
    require("404 JUnit · 18 GameTest" not in wiki_data
            and "397 JUnit" not in wiki_data
            and "17 GameTest" not in wiki_data
            and "17 Forge GameTest" not in wiki_data
            and "schema 28" not in wiki_data
            and "schema 27" not in wiki_data
            and "324 JSON" not in wiki_data
            and "322 JSON" not in wiki_data
            and "1521 双语键" not in wiki_data
            and "1500 双语键" not in wiki_data
            and "92 物品" not in wiki_data
            and "91 物品" not in wiki_data
            and "191 条图鉴" not in wiki_data
            and "190 条图鉴" not in wiki_data
            and "秘偶收纳/跨维控制" not in wiki_data
            and "收纳卷轴、跨维指挥" not in wiki_data,
            "Pages 动态图鉴仍含上一版本的当前状态文案", errors)
    require("无面人八槽形体记录" in wiki_data
            and "/pm faceless" in wiki_data
            and "不保存来源实体/玩家UUID" in wiki_data,
            "Pages 图鉴必须展示无面人形体、命令与隐私边界", errors)
    require("M3 专属材料生态" in wiki_data
            and "10 材料 · 7 生物 · 10 保底配方" in wiki_data
            and "/pm bestiary" in wiki_data,
            "Pages 图鉴必须展示最终材料、生物生态与玩家入口", errors)

    require("[hidden] { display: none !important; }" in css,
            "CSS 必须保护 hidden 属性不被布局规则覆盖", errors)
    require("scroll-margin-top: 0" in css,
            "锚点 section 不得与 html scroll-padding 重复偏移", errors)
    require("@media (max-width: 820px)" in css and "@media (max-width: 560px)" in css,
            "Pages 缺少平板或手机响应式断点", errors)
    require(".filter-group" in css and "overflow-x: auto" in css,
            "移动端分类筛选必须支持横向滚动", errors)
    require(".reveal { opacity: 0" not in css,
            "禁止恢复会令锚点直达空白的透明 reveal 样式", errors)

    require("var PAGE_SIZE = 24;" in javascript,
            "图鉴首批数量必须固定为 24", errors)
    require("visibleLimit += PAGE_SIZE" in javascript,
            "图鉴缺少分批加载逻辑", errors)
    require("function restoreHashTarget()" in javascript,
            "动态渲染后必须恢复 URL 锚点", errors)
    require("style.scrollBehavior = \"auto\"" in javascript,
            "锚点恢复必须绕过长距离平滑滚动", errors)
    require("modalTrigger.focus()" in javascript,
            "详情弹窗关闭后必须恢复触发器焦点", errors)
    require("aria-selected" in javascript and "aria-pressed" in javascript,
            "途径切换与图鉴筛选必须暴露可访问状态", errors)

    if errors:
        print("GitHub Pages layout contract failed:", file=sys.stderr)
        for error in errors:
            print(f"- {error}", file=sys.stderr)
        return 1

    print(
        "GitHub Pages layout contract passed: "
        f"{len(parser.sections)} sections, {len(parser.ids)} unique ids, "
        f"asset cache v{asset_version}, current M3 overview, catalog batch 24."
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
