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
    spirit_ecology_eggs = unique_matches(
        REGISTRY_ROOT / "ModItems.java",
        r'\bspiritEcologyEgg\(\s*"([a-z0-9_]+)"',
    )
    for registry_id in spirit_ecology_eggs:
        spawn_egg_id = f"{registry_id}_spawn_egg"
        if spawn_egg_id not in items:
            items.append(spawn_egg_id)
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
        r'\bENTITIES\.register\(\s*"([a-z0-9_]+)"|\bspiritEcology\(\s*"([a-z0-9_]+)"',
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
        return "原生分部模型、原创材质与动画；旅行门为双面门框"
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
        "image": (
            f"assets/item-icons/{registry_id}.png"
            if (ASSET_ROOT / "textures/item" / f"{registry_id}.png").exists()
            else None
        ),
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
            ["事故类型", definition["incident_profile"]],
            ["效果", zh_cn[effect_key]],
            ["代价", zh_cn[cost_key]],
        ],
        "long": (
            "该封印物使用世界级保管台账记录唯一实例、责任人、当前持有人、"
            "最后维度与坐标、污染、使用次数、借出日、到期日和事故。"
            "逾期、超出安全使用次数、污染达到阈值、换手或复制会进入泄漏、"
            "回收或滥用处理；六类事故使用不同稳定材料，来客面具不会绕过任何权限。"
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
        "summary": f"{kind}。每周独立制定战略，每天自主行动，并使用玩家隔离的接取与结算状态。",
        "tags": ["组织", kind, "周战略", "自主行动", "M4"],
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
            "七个数据面。服务端按世界种子、战略周、游戏日和全服神秘暴露确定"
            "每周重点与每日三项行动；无人参与的行动仍会自主结算，玩家不能伪造奖励。"
        ),
    }


def spirit_weather_entry(definition, zh_cn, en_us):
    weather_id = definition["id"]
    key = f"spirit_weather.{MOD_ID}.{weather_id}"
    hint_key = f"{key}.hint"
    if key not in zh_cn or key not in en_us or hint_key not in zh_cn:
        raise ValueError(f"spirit weather misses translations: {weather_id}")
    return {
        "type": "world",
        "id": f"{MOD_ID}:spirit_weather/{weather_id}",
        "name": zh_cn[key],
        "en": en_us[key],
        "summary": zh_cn[hint_key],
        "tags": ["灵界", "异常天气", f"风险{definition['risk']}", "M5", "playable"],
        "details": [
            ["风险", str(definition["risk"])],
            ["运行时效果", definition["runtime_effect"]],
            ["导航规则", definition["navigation_rule"]],
            ["低配降级", definition["degradation"]],
        ],
        "long": (
            "该天气已进入服务端权威的 M5 灵界远征状态机。视觉或声音效果可以按"
            "性能设置降级，但罗盘方向、稳定度、漂移、遭遇与安全撤离规则保持一致。"
        ),
    }


def spirit_encounter_entry(definition, zh_cn, en_us):
    encounter_id = definition["id"]
    key = f"spirit_encounter.{MOD_ID}.{encounter_id}"
    action_key = (
        f"spirit_action.{MOD_ID}.{definition['preferred_action']}"
    )
    if key not in zh_cn or key not in en_us or action_key not in zh_cn:
        raise ValueError(f"spirit encounter misses translations: {encounter_id}")
    research_keys = {
        part: f"spirit_research.{MOD_ID}.{encounter_id}.{part}"
        for part in ("clue", "success", "failure")
    }
    if any(value not in zh_cn or value not in en_us for value in research_keys.values()):
        raise ValueError(f"spirit research misses translations: {encounter_id}")
    return {
        "type": "entity",
        "id": f"{MOD_ID}:spirit_encounter/{encounter_id}",
        "name": zh_cn[key] + " · 遭遇档案",
        "en": en_us[key] + " encounter",
        "summary": (
            f"灵界航路生态遭遇；推荐以“{zh_cn[action_key]}”处理，错误应对会增加漂移。"
        ),
        "tags": ["灵界", "生态遭遇", f"风险{definition['risk']}", "M5", "航路档案"],
        "details": [
            ["生态角色", definition["ecology_role"]],
            ["推荐应对", zh_cn[action_key]],
            ["风险", str(definition["risk"])],
            ["收益类型", definition["reward"]],
            ["实现形态", "真实实体、原生模型、服务端权威遭遇"],
            ["观察线索", zh_cn[research_keys["clue"]]],
            ["成功反馈", zh_cn[research_keys["success"]]],
            ["错误反馈", zh_cn[research_keys["failure"]]],
            ["操作入口", "/pm spirit study · /pm spirit journal"],
        ],
        "long": (
            "这是已实体化的灵界生态，不是未来占位档案。先持调查员残缺手稿右键或执行"
            "/pm spirit study 记录线索，再决定观察、回避、帮助或交易。手账按玩家和世界"
            "保存已观察/成功应对进度，不发放重复奖励。实体必须属于本人当前有效遭遇；"
            "旧实体、跨维度、旁观和过期状态不能结算。"
        ),
    }



def survival_entries(entries, zh_cn, en_us):
    config = json.loads((ROOT / "docs/survival-content.json").read_text(encoding="utf-8"))
    by_id = {entry["id"]: entry for entry in entries}

    def name(identifier):
        vanilla = {"minecraft:string": "线", "minecraft:charcoal": "木炭",
                   "minecraft:honey_bottle": "蜂蜜瓶", "minecraft:honeycomb": "蜜脾",
                   "minecraft:glow_ink_sac": "荧光墨囊", "minecraft:paper": "纸", "minecraft:feather": "羽毛", "minecraft:ink_sac": "墨囊",
                   "minecraft:bone_meal": "骨粉", "minecraft:amethyst_shard": "紫水晶碎片",
                   "minecraft:iron_nugget": "铁粒", "minecraft:quartz": "下界石英", "minecraft:brown_mushroom": "棕色蘑菇"}
        if identifier in vanilla:
            return vanilla[identifier]
        return zh_cn.get(f"item.{identifier.replace(':', '.')}",
                         zh_cn.get(f"block.{identifier.replace(':', '.')}", identifier))

    def enrich(identifier, summary, details):
        entry = by_id[f"{MOD_ID}:{identifier}"]
        entry["summary"] = summary
        entry["tags"] += ["生存资源", config["version"]]
        entry["details"] += details
        entry["survivalDetails"] = details
        entry["long"] += " 新内容只在新生成区块出现；旧合成保留，无需重置存档。完整说明见 Wiki 的 Survival-Resource-Loop。"

    for ore in config["ores"]:
        for prefix in ("", "deepslate_"):
            enrich(prefix + ore["id"] + "_ore", f"可开采的{name(MOD_ID + ':' + ore['drop'])}来源，连接洞穴采矿与神秘学消耗品。",
                   [["分布", f"主世界 Y={ore['min_y']}..{ore['max_y']}；每区块{ore['attempts']}次矿脉尝试，不保证实际数量"],
                    ["工具", "石镐及以上；时运增加材料，精准采集保留矿石"],
                    ["加工", "熔炉/高炉：每块矿石得到1份材料；熔炼不叠加时运"]])
    for plant in config["plants"]:
        for identifier in (plant["id"], plant["id"] + "_crop", "wild_" + plant["id"]):
            enrich(identifier, "野外采集后直接播种，兼作魔药与研钵材料；建立可持续药圃。",
                   [["生态", "、".join(plant["biomes"])], ["种植", "材料本身即种子；耕地、光照、水分按原版作物规则"],
                    ["成熟", "年龄7；右键得到2份并留根，破坏成熟作物得到3份（其中1份可重种）"],
                    ["保护", "骨粉有效；年龄6不可收获；重复或冲突交互不重复发货"],
                    ["生活循环", "65%概率堆肥；农民/流浪商人可能出售种苗，老手农民可能收购草药"]])
    enrich("mystic_mortar", "双手投料、右键加工的神秘学研钵，提供14种基础消耗品与守夜材料加工路线。",
           [["制作", "5石砖+1紫水晶碎片；工作台配方可解锁"], ["使用", "主手原料、副手催化材料；每次右键处理一批"],
            ["查询", "/pm survival recipes"], ["安全", "缺料、顺序错误、权限/活动冲突不扣料；返还容器；满包在玩家位置掉落"]])
    enrich("spirit_salt", "矿产、研钵、牧师和开放水域钓鱼可获得的基础神秘学耗材。",
           [["钓鱼", "真实鱼竿、开放水域；保留原版渔获，20%概率额外1灵盐"], ["交易", "学徒牧师交易池：1绿宝石换3灵盐，最多8次，按原版补货"]])
    for recipe in config["mortar"]:
        result_id = f"{MOD_ID}:{recipe['result']}"
        entries.append({
            "type": "recipe", "id": f"{MOD_ID}:recipe/mortar_{recipe['id']}",
            "name": name(result_id) + " · 研钵配方", "en": recipe["id"],
            "summary": f"{name(recipe['main'])} ×{recipe['main_count']} + {name(recipe['reagent'])} ×{recipe['reagent_count']} → {name(result_id)} ×{recipe['count']}",
            "tags": ["研钵", "生存资源", config["version"]],
            "details": [["主手", f"{name(recipe['main'])} ×{recipe['main_count']}"], ["副手", f"{name(recipe['reagent'])} ×{recipe['reagent_count']}"],
                        ["产物", f"{name(result_id)} ×{recipe['count']}"], ["来源", f"recipes/mortar_{recipe['id']}.json"]],
            "long": "不是无消耗法术；保持双手顺序，对研钵右键。支持数据包覆盖，游戏内命令显示实际已加载配方。蜂蜜瓶配方返还玻璃瓶。旧工作台和坩埚链路不受替代。"})
    for ruin in config["structures"]:
        entries.append({
            "type": "structure", "id": f"{MOD_ID}:structure/{ruin['id']}",
            "name": "旧仪式地窖" if ruin["hostile"] else "草药师遗址", "en": ruin["id"],
            "summary": "带僵尸刷怪笼、研钵与两箱补给的地下遗迹。" if ruin["hostile"] else "带药圃、研钵与两箱补给的草药师遗迹。",
            "tags": ["原生世界生成", "探索", config["version"]],
            "details": [["地形", "平原、向日葵平原、森林、桦木森林、针叶林的新区块"],
                        ["分布", f"随机间距 {ruin['spacing']} 区块 / 最小分隔参数 {ruin['separation']} 区块；不是每隔固定距离必有"],
                        ["奖励", "每箱保证灵性草2、石楠2、灵盐3，另加2–4次低阶随机补给"],
                        ["定位", f"/locate structure {MOD_ID}:{ruin['id']}（管理员命令）"]],
            "long": "从石砖与提灯入口探索，备好食物、镐和照明。箱子只在首次打开生成物品；不掉完整非凡特性，不跳过知识或晋升门槛。不回填旧区块，也不覆盖既有建筑。"})
    return entries


def nightwatch_entries(entries):
    specifications = {
        "night_owl_optic_nerve": ("不眠者序列9的坩埚核心材料。", [["获取", "夜枭固定掉落1；新手牧师交易池8绿宝石/份、最多3次"], ["下一步", "坩埚第1项 → 守夜辅料 → 可选灰烬粉末"]]),
        "coffee_beans": ("营火和农民提供的浓黑咖啡原料。", [["获取", "农民新手候选1绿宝石/4豆、最多8次；营火烘烤可可30秒"], ["适配", "烘烤可可是MC替代获取，不是植物学或原著设定"]]),
        "strong_coffee": ("清除不眠者当日白昼倦怠，保留守夜层数。", [["合成", "2咖啡豆+糖+水桶+玻璃瓶；返空桶"], ["饮用", "32tick；无倦怠不消耗；生存返玻璃瓶；满包掉落"], ["加工", "研钵主手咖啡、副手暗影蘑菇，各1 → 守夜辅料，返玻璃瓶"]]),
        "shadow_mushroom": ("低阶夜巡材料，也可通过研钵稳定获得。", [["获取", "牧师新手候选1绿宝石/2菇；研钵棕色蘑菇+黑蜡；不眠残影掉落"], ["用途", "加工守夜辅料"]]),
        "nightwatch_extract": ("把咖啡与暗影蘑菇预处理成适配三槽坩埚的材料。", [["加工", "研钵浓黑咖啡1+暗影蘑菇1，返玻璃瓶"], ["投料", "夜枭视络之后投入，灰烬粉末之前"]]),
        "sleepless_potion_9": ("普通人的第二批途径入口：不眠者序列9。", [["门槛", "未选择途径、存活、非旁观、无独占活动"], ["灵性", "114上限；Y夜视0.5/秒；U状态"], ["守夜", "完整夜晚最多5层；夜间移速+4%/层、攻击+6%/层，昼移速-10%/层"], ["说明", "/pm nightwatch；后续序列8–5未实现"]]),
        "night_owl": ("暗色耳羽、面盘、分层翼羽的夜间敌对飞行生物。", [["生态", "暗森林/针叶林/原始松木针叶林；原版黑暗怪物条件"], ["战斗", "16生命、3基础攻击、24格跟踪；非穿墙飞行"], ["掉落", "夜枭视络1、羽毛0–2"]]),
        "sleepless_shade": ("不眠者失控后的兜帽与新月残影，延续特性掉落链。", [["来源", "不眠者失控转化；不会水中转成原版溺尸"], ["表现", "独立兜帽/新月饰纹/触须；30生命、5基础攻击"], ["边界", "原生分部动画；声音复用原版幻翼，不声称原作音效"]]),
    }
    for entry in entries:
        spec = specifications.get(entry["id"].split(":")[-1])
        if spec:
            entry["summary"] = spec[0]
            entry["details"] = spec[1]
            entry["tags"] += ["不眠者", "守夜", "M6", "0.9.40"]
            entry["long"] = "本轮是M6不眠者序列9纵切，不是四途径全部完成。完整入门、风险、多人和存档边界见Wiki的M6-Nightwatch。所有技能由服务端校验，粒子不造成额外伤害。"
    return entries


def render():
    items, blocks, entities = load_registry_ids()
    zh_cn = load_language("zh_cn")
    en_us = load_language("en_us")
    organizations = load_definitions("organizations")
    artifacts = load_definitions("artifacts")
    spirit_weather = load_definitions("spirit_weather")
    spirit_encounters = load_definitions("spirit_encounters")

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
    entries.extend(
        spirit_weather_entry(definition, zh_cn, en_us)
        for definition in spirit_weather.values()
    )
    entries.extend(
        spirit_encounter_entry(definition, zh_cn, en_us)
        for definition in spirit_encounters.values()
    )
    entries = nightwatch_entries(survival_entries(entries, zh_cn, en_us))
    entries.sort(key=lambda entry: (entry["type"], entry["id"]))

    metadata = {
        "registeredItems": len(items),
        "registeredBlocks": len(blocks),
        "registeredEntities": len(entities),
        "organizationDefinitions": len(organizations),
        "artifactDefinitions": len(artifacts),
        "spiritWeatherDefinitions": len(spirit_weather),
        "spiritEncounterDefinitions": len(spirit_encounters),
        "uniqueRegistryEntries": len(entries),
        "source": "Forge registries + organization/artifact/spirit + survival manifest + zh_cn/en_us",
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
        "  var generatedById = {};\n"
        "  generatedEntries.forEach(function (entry) { generatedById[entry.id] = entry; });\n"
        "  manualEntries.forEach(function (entry) {\n"
        "    if (generatedById[entry.id]) entry.image = generatedById[entry.id].image;\n"
        "    if (generatedById[entry.id] && generatedById[entry.id].survivalDetails) {\n"
        "      entry.details = (entry.details || []).concat(generatedById[entry.id].survivalDetails);\n"
        "      entry.tags = (entry.tags || []).concat(['生存资源', '0.9.40']);\n"
        "    }\n"
        "  });\n"
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
