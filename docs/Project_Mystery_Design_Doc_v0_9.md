# Project Mystery — 《诡秘之主》Minecraft Mod 开发文档 v0.9

> **版本**：0.9（原著一致性审计 · 全途径 9–5 设计闭环 · 生态/物品/配方/状态/天气/活动/兼容/工程路线全面扩展）  
> **技术基线**：Minecraft Java Edition **1.20.1** · **Forge 47.3.x** · **Java 17** · **GeckoLib 4.x**  
> **项目代号**：Project Mystery（仓库：`Herdeny/Lord-of-Mysteries`）  
> **更新日期**：2026-07-17（v0.9）  
> **继承规则**：本文件完整嵌入 v0.8 原文，未删除、缩写或覆盖其任何段落；v0.9 新增内容从第 57 章开始。发生冲突时，按“第 57.4 节版本优先级”解释，旧文本仍保留用于审计。  
> **内容声明**：原著专有内容仅用于内部原型与学习交流；公开发布前仍须取得授权或完成原创化映射。新增文案全部原创，不摘录小说正文。  
> **研究口径**：原著/官方资料与高质量维基用于事实核对；论坛与社区讨论仅作灵感，不作为正典证据；Forge、GeckoLib、Curios 等官方文档用于工程约束。

---

## v0.9 导航

- **v0.8 完整基线**：下方“BEGIN v0.8 BASELINE”至“END v0.8 BASELINE”，内容逐字保留。
- **J 原著一致性与内容图谱**：57–59
- **K 全途径与高序列扩展**：60–62
- **L 魔药、特性、扮演与神秘学闭环**：63–65
- **M 组织、世界、生态与生产内容**：66–72
- **N 事件、活动、指引与兼容**：73–76
- **O 工程、路线图、测试与总账**：77–81

---

<!-- BEGIN v0.8 BASELINE: SHA-256 recorded in chapter 81 -->
# Project Mystery — 《诡秘之主》Minecraft Mod 开发文档 v0.8

> **版本**:0.8(全量落地版 · 物品/配方/数据包/模型/粒子/技能/生物/文案/指引/任务全细化 · 服务器整合 · 宏大叙事)
> **技术基线**:Minecraft Java Edition **1.20.1** · **Forge 47.3.x** · **Java 17** · **GeckoLib 4.x**(已决策采纳)
> **项目代号**:Project Mystery(仓库:`Herdeny/Lord-of-Mysteries`)
> **更新日期**:2026-07-05(v0.8)
> **文档定位**:唯一权威 GDD + TDD。v0.8 为自足文档,不依赖回看历史版本。
> **核心理念**:玩家不是选择职业,而是在未知信息中承担风险,通过扮演消化力量,沿神之途径逐步攀升——并为攀升付出代价。

---

# 目录

- **A 总纲**:1 授权与决策 / 2 概述与哲学 / 3 核心循环 / 4 范围路线图
- **B 角色系统**:5 核心属性(完整代码) / 6 非凡特性(完整代码) / 7 途径与序列(22 条总册+9 条详设) / 8 扮演法与消化
- **C 神秘学玩法**:9 魔药(25 配方+完整代码) / 10 灵视与灵体世界 / 11 占卜(七法+完整代码) / 12 仪式魔法与尊名呼名(完整代码) / 13 封印物(40 件) / 14 罗塞尔日记与知识手册
- **D 世界与社会**:15 灰雾与塔罗会(完整代码) / 16 七大教会与隐秘组织 / 17 金镑经济与雾都生活 / 18 世界事件(15 种) / 19 世界生成与结构 / 20 生物图鉴与 Boss / 21 任务链(3 条全步骤)
- **E 呈现与体验**:22 疯狂与恐怖呈现 / 23 UI 与可访问性 / 24 多人生态 / 25 成就树 / 26 指令与权限
- **F 工程**:27 技术架构(完整网络层) / 28 目录结构 / 29 JSON Schema 全集 / 30 配置全集 / 31 本地化 / 32 音效音乐 / 33 兼容性 / 34 开发者 API
- **G 管理**:35 里程碑 / 36 测试验收 / 37 风险 / 38 决策记录 / 39 下一步
- **H 扩充(v0.7)**:40 非凡材料图鉴(80+ 材料+GLM 代码) / 41 序列 3-1 后期玩法·唯一性·神位战争(毕业账本+完整代码) / 42 雾都生活扩充(报社/大学/剧院/悬赏+完整代码)
  - 另:7.11 第三批六途径详设 · 7.12 第四批七途径概设 · 7.13 相邻途径转换(完整代码) · 9.6-9.7 魔药 75 配方与知识经济 · 13.6 封印物 72 件 · 14.4 亵渎石板 · 14.5 日记 72 页 · 18.3 世界事件 25 种 · 21.4-21.8 任务链 8 条 · 25.1 成就 60 项 · 26.1 指令 26 条
- **I 全量落地(v0.8)**:43 全物品注册总表(180+ 物品) / 44 合成配方附表(全量,含数据包 JSON) / 45 数据包全量文件规范与整仓样例 / 46 GeckoLib 模型清单全册(第三/四批 + 全失控体,49 模型) / 47 粒子特效总表(34 种 + 完整代码) / 48 技能能力全表(15 途径 × 序列 9-5,150+ 条) / 49 生物图鉴扩充(18 新生物 + 完整实体代码) / 50 文案库(呓语/日记/传闻/对白/成就全文案规范与样本池) / 51 新手指引与引导系统(神秘学手账 + 教学成就链) / 52 指令树/任务数据结构全细化 / 53 极光之恋服务器整合方案(小雨 AI 桥接/QQ/直播联动/赛季运营) / 54 Wiki 覆盖对照表 / 55 宏大叙事与纪元史诗(第五纪元编年史 + 赛季叙事弧)

---

# 0B. v0.7 → v0.8 变更摘要(本次全量落地)

| # | 变更 | 说明 |
|---|------|------|
| 1 | **全物品注册总表** | 180+ 物品六域归册(工具/材料/魔药/仪式/文书/货币杂项),每件含注册 ID、稀有度、堆叠、tooltip key、获取路线 ≥2 条;附 `PMItems` 注册类完整代码(43 章) |
| 2 | **合成配方全量附表** | 工作台配方 60+ 条(shaped/shapeless 全 JSON)、坩埚魔药配方数据包 JSON 全格式、切石/锻造/营火扩展位;配方汇总 CSV 规范(44 章) |
| 3 | **数据包全量文件** | `data/pm/` 整仓目录树 + 13 类数据驱动 JSON 的**完整文件样例**(非片段):pathway/sequence/ability/potion_recipe/ritual/honorific/artifact/diary_page/whisper/world_event/commission/advancement/loot_modifier + tags 与 worldgen(45 章) |
| 4 | **GeckoLib 模型清单全册** | 21 → **49 模型**:第三批 6 神话形态+6 失控体、第四批 7 概念稿、新生物 12、Boss 2、0 级具象 4;骨骼树规范、动画状态机全表、`.geo.json`/`.animation.json` 完整样例(46 章) |
| 5 | **粒子特效总表** | 34 种自定义粒子归册(灵性/污染/仪式/途径专属/事件),`PMParticles` 注册 + 自定义粒子类完整代码 + 使用点位映射(47 章) |
| 6 | **技能能力全表** | 15 条可玩途径 × 序列 9-5 的 150+ 能力统一入册:类型/消耗/冷却/执行器 ID/参数/粒子/音效/升级曲线(48 章) |
| 7 | **生物图鉴扩充** | 罗盘鸟/光萤群/梦魇兽/知识妖/摆渡人/神孽等 18 种新生物全规格(属性/AI 目标树/掉落/生成规则),含 1 个完整实体类代码(49 章) |
| 8 | **文案库建库** | 呓语池 40 条、日记样章 6 页全文、传闻池 30 条、NPC 对白模板、成就文案 60 条全量、写作规范与禁词表(50 章) |
| 9 | **新手指引系统** | 「神秘学手账」引导书(自研翻页 UI)、教学成就链 12 步、情境 Toast 提示 24 条、每途径首小时路书(51 章) |
| 10 | **指令树/任务结构细化** | 26 条指令全参数树+权限级+示例;任务链 JSON 数据结构+链一完整数据文件(52 章) |
| 11 | **极光之恋服务器整合** | 小雨(Aurora Rain)AI 管理桥接(WebSocket→OpenClaw)、QQ 群播报、直播间(30313460)事件联动、赛季运营日历、备份与反作弊清单(53 章) |
| 12 | **Wiki 覆盖对照表** | fandom 维基全部大类 → 文档章节映射核对,缺口标注与补齐策略(54 章) |
| 13 | **宏大叙事扩写** | 第五纪元编年史(原创化)、三大终局幻影、赛季叙事弧模板、玩家可改写的\"活历史\"账本(55 章) |

---

# 0. v0.5 → v0.6 变更摘要

| # | 变更 | 说明 |
|---|------|------|
| 1 | **决策落定** | GeckoLib 4.x 正式采纳;`docs/ip_mapping.csv` 原创化映射表立即启动(M2 前完成词表) |
| 2 | **22 途径全序列名入册** | 全部 22 条途径的序列 9-0 名称完整登记,数据包占位 JSON 全量生成 |
| 3 | **首发五途径补齐序列 4** | 每条途径的半神序列与神话生物形态详设(诡法师/操纵师/铁血骑士/寄生者/秘法师) |
| 4 | **第二批四途径升级为详设** | 水手/不眠者/收尸人/歌颂者从"概设 9-7"升级为"详设 9-5",与首发同规格 |
| 5 | **新增:罗塞尔日记系统** | 散落世界的日记残页 = 顶级知识来源 + 收集玩法 + 世界观叙事载体 |
| 6 | **新增:金镑经济与雾都生活** | 金镑/苏勒/便士三级货币、侦探事务所委托、报纸传闻、当铺、蒸汽机车快旅 |
| 7 | **新增:会客法(通灵)** | 与亡灵/灵体对话的正式系统,收尸人途径核心,占卜家可用降级版 |
| 8 | **新增:尊名呼名系统** | 仪式呼唤存在需拼合"三句式尊名",错误呼名 = 灾变;玩家可注册自己的灰雾尊名接收祈求 |
| 9 | **封印物 32 → 40 件** | 新增 8 件教会圣物(高声望租借制,用完要还,逾期触发教会追索) |
| 10 | **占卜七法** | 新增灵数占卜与纸人问事,占卜法总数 5 → 7 |
| 11 | **完整代码交付** | 核心 14 个类给出可直接落库的完整实现(非片段):数据、网络、能力、坩埚、仪式、占卜、灰雾市场、傀儡、事件调度、配置等 |
| 12 | **成就树/指令/API 三章新增** | 34 项成就、17 条管理指令、Addon 开发者事件 API |

---

# 0A. v0.6 → v0.7 变更摘要(本次扩充)

| # | 变更 | 说明 |
|---|------|------|
| 1 | **可玩途径 9 → 15** | 第三批六途径(战士/秘祈人/阅读者/刺客/耕种者/药师)升级详设 9-5,各带差异化机制、晋升仪式与扮演事件(7.11) |
| 2 | **剩余七途径概设** | 律师/仲裁人/罪犯/囚犯/窥秘人/通识者/怪物 给出 9-7 概设;母树双途径定位"堕落路线"(7.12) |
| 3 | **相邻途径转换系统** | 序列 4/3 可转换同组途径,保留 2 个旧能力形成杂糅 Build;完整 AdjacentPathwayManager 代码(7.13) |
| 4 | **魔药 25 → 75 配方** | 26-60 号入正文表,61-75 入附表;新增配方残页拼合、真伪鉴定、首学头条的"知识经济"闭环(9.6/9.7) |
| 5 | **封印物 40 → 72 件** | 32 件新增(代表 16 件入册),0 级封印物全服限 3 激活+「封印共鸣」惩罚事件(13.6) |
| 6 | **亵渎石板残片** | 7 板 21 残片收集线 = 序列 3 钥匙,支持灰雾三人"共研"(14.4) |
| 7 | **罗塞尔日记 36 → 72 页** | 12 主题 ×3 页,30% 假线索页,附原创文案示例与规范(14.5) |
| 8 | **世界事件 15 → 25 种** | 血月/灵界裂隙/大失眠/竞技之夜/法庭开庭等,新增 seasonal 与 player_driven 触发器(18.3) |
| 9 | **任务链 3 → 8 条** | 新增链四~八,含堕落路线专属线与 30h+ 石板主线(21.4-21.8) |
| 10 | **序列 3-1 有限可玩** | 圣者试炼三幕副本、唯一性赛季政治、神位战争终局;序列 0 保持不可玩(41 章) |
| 11 | **毕业时间账本** | 单人 400-600h / 结社 250h+,三红线防加速、三底线防劝退(41.5) |
| 12 | **非凡材料图鉴** | 80+ 材料六分类、条件掉落 GLM 完整代码、神秘作物嫁接树(40 章) |
| 13 | **雾都生活扩充** | 报社周刊(完整代码)/大学/剧院/悬赏公会 + 七教会声望矩阵(42 章) |
| 14 | **成就 34 → 60、指令 17 → 26** | 25.1 / 26.1 |
| 15 | **新增完整代码 4 类** | AdjacentPathwayManager / MysticMaterialLootModifier / UniquenessManager / NewspaperGenerator,均可直接落库 |

---

# 1. 文档目的与授权声明

## 1.1 文档地位

本文档是 Project Mystery 的唯一权威规格,兼任**游戏设计文档(GDD)**与**技术实现蓝图(TDD)**。数值表、材料名、任务文本作为附属表格独立迭代,结构性变更必须回写本文档。文档按 A-G 七个分部组织,每个分部可独立分配给协作者(Herdeny 侧建议:B/C 分部代码,小倪侧:D/E 内容 + F 架构)。

## 1.2 授权声明(不可省略)

《诡秘之主》为爱潜水的乌贼所著、阅文集团旗下作品。当前所有专有名词(途径名、序列名、组织名、地名、神名)**仅用于内部原型开发与学习交流**。公开发布前必须满足以下之一:

1. 取得 IP 方书面授权;
2. 完成**原创化映射**(已决策启动):维护 `docs/ip_mapping.csv`(原作名称 → 原创名称 → lang key),所有专有名词经由 `lang` 文件与数据包 ID 双层映射,可一键切换为原创世界观(工作代号「雾都异闻」)。

**工程约束(强制)**:代码与数据包 ID 一律使用英文中性命名(`seer`、`gray_fog`、`nighthawks`、`backlund_town`),显示名称全部走本地化文件。任何 PR 中出现硬编码中文显示名 = 直接打回。

**内容红线**:本 Mod 不复制原作小说文本。日记残页、呓语、任务文案全部为**原创撰写的风格化文本**(致敬氛围,不摘抄原文);知识手册中的"传闻"条目使用自写的百科式描述。

## 1.3 v0.6 已锁定决策(原第 24 节待决策项结论)

| # | 决策项 | 结论 | 理由 |
|---|--------|------|------|
| D1 | 项目性质 | 先按**免费公开同人**准备,双轨维护原创化词表 | 商业化需授权,免费同人风险最低 |
| D2 | 原创化映射启动 | **立即**,M2 前完成 22 途径+7 教会+主要地名词表 | 越晚成本越高 |
| D3 | 序列 5 晋升仪式 | 单人可行,多人参与每人 +5% 完成度(上限 +20%) | 不逼社交,奖励社交 |
| D4 | 拍卖货币 | 自定义**金镑**体系(金镑/苏勒/便士),兑换村民绿宝石 | 见第 17 章 |
| D5 | 灰雾首席权力 | 保留拍卖抽成 10%,祈求回应权**默认全席位开放**,首席仅获优先接单权 | 削弱寡头,保留争夺动机 |
| D6 | 神话形态模型 | **GeckoLib 4.x** | 骨骼动画质量远超原生 JSON |
| D7 | 猎巫夜与萌新 | 未入途径玩家完全豁免 | 保护萌新 |
| D8 | 22 途径占位 | MVP 即放出,以「传闻」灰字条目呈现 | 世界观完整性优先 |

---

# 2. 产品概述与设计哲学

## 2.1 一句话定义

**在蒸汽与机械的时代,于未知中承担风险、通过扮演消化力量、沿神之途径逐步攀升的 Minecraft 生存冒险 Mod。**

## 2.2 时代基调

原作世界观 = 维多利亚蒸汽朋克 × 克苏鲁式不可名状 × SCP 式封印物管理 × 侦探推理。Mod 的美术、音效、结构生成围绕四个关键词:

- **雾**:物理雾效、信息迷雾(占卜不给确定答案)、身份迷雾(灰雾中匿名);
- **烛光**:安全感来源——光照、教堂、仪式蜡烛、深红星光是玩家的心理锚点;
- **低语**:危险来源——污染、封印物、邪神注视都以「听觉入侵」为首要反馈;
- **绅士的体面**:哪怕在猎杀邪教徒,也要付账单、看报纸、赶火车——雾都生活系统(第 17 章)让恐怖有了对照组,恐怖才成立。

## 2.3 设计支柱(10 条)

| # | 支柱 | 设计含义 | 反例(禁止) |
|---|------|----------|-----------|
| 1 | 知识即资源 | 正确配方、仪式、禁忌是力量前提;错误知识会杀人 | 全配方 JEI 直接可查 |
| 2 | 力量有代价 | 能力越强,污染、疯狂、失控风险越高 | 无代价的技能树 |
| 3 | 行为塑造成长 | 魔药不自动消化,必须扮演角色准则 | 挂机刷经验 |
| 4 | 信息不完全可靠 | 占卜、梦境、灵视是线索不是答案,可被干扰 | 占卜=雷达 |
| 5 | 原版生存仍重要 | 探索、建造、天气、月相、地形参与神秘学 | 架空原版玩法 |
| 6 | 灰雾是核心社交空间 | 匿名塔罗会=多人服最大差异化 | 灰雾做成快递柜 |
| 7 | 组织与身份 | 教会与隐秘组织提供任务、庇护、审查 | 组织=商店皮肤 |
| 8 | 特性守恒 | 非凡特性不灭,途径力量在世界中循环 | 特性凭空产出 |
| 9 | 高处不胜寒 | 序列越高,理智侵蚀越强,「人」越难保留 | 高序列=纯数值膨胀 |
| 10 | **生活是恐怖的锚**(新) | 经济、报纸、火车、委托构成"正常世界",神秘学是撕开它的裂缝 | 全程高压恐怖 |

## 2.4 不做什么

- 不做单纯技能树或刷怪升级;不让 JEI 公开隐秘答案(仅已解锁);
- 不做大型线性剧情战役(以调查任务链代替);不把灰雾做成传送门或作弊仓库;
- 序列 3 及以上(圣者/天使/真神)不做玩家可达内容,仅作世界观 NPC/事件/封印物来源;
- 不复刻原作主角团剧情——玩家是这个世界里**自己的故事**,原作要素以系统、地点、组织、传闻形式存在。

## 2.5 目标玩家画像

| 画像 | 诉求 | 对应内容 |
|------|------|----------|
| 原著粉 | 还原度、彩蛋、成为非凡者的幻想 | 22 途径、尊名、日记残页、教会生态 |
| 硬核生存玩家 | 深度机制、风险管理 | 消化/污染/特性经济、仪式检定 |
| 多人服社交玩家 | 身份博弈、政治、交易 | 塔罗会匿名、卧底、护送劫镖 |
| 恐怖游戏爱好者 | 氛围、Jump scare 可控的心理恐怖 | 低语、幻觉、猎巫夜、可访问性开关 |
| 建筑/RP 玩家 | 场景、道具、扮演素材 | 雾都镇区、维多利亚方块集、报纸与委托 RP 工具 |

---

# 3. 核心体验与循环

## 3.1 主循环

```
探索与调查 → 发现手稿/材料/特性/线索
  → 验证知识(灵视/占卜/组织鉴定/日记残页交叉印证)
  → 服药(呓语时刻 + 稳定性检定)获得力量
  → 依照扮演准则行动,消化魔药
  → 应对污染、低语、追踪、审查与失控
  → 收集下一序列配方 + 主材料(或等价非凡特性)
  → 晋升(序列 5 起魔药+仪式双重检定)
  → 接触更危险的知识、组织与事件 → 循环
```

## 3.2 玩家阶段总览

| 阶段 | 玩家目标 | 主要压力 | 归属 |
|------|----------|----------|------|
| 普通人 | 谋生(委托/职业)、发现神秘事件、第一份手稿 | 信息匮乏、贫穷、怪物 | MVP |
| 序列 9 | 基础能力、学扮演法、加入或躲避组织 | 灵性不足、反噬 | MVP |
| 序列 8 | 扮演成流派、组织外围任务 | 消化停滞、污染 | MVP |
| 序列 7 | 中型仪式、进灰雾、领塔罗席位 | 敌对非凡者、失控事件 | MVP |
| 序列 6 | 大型仪式、猎取特性、组织中坚、卷入猎巫 | 特性同化、审查 | EP1 |
| 序列 5 | 首次双重晋升、领域雏形、灰雾话语权 | 仪式灾变、高位注视 | EP1 |
| 序列 4 | 神话生物形态、一方传说、半神事件 | 理智侵蚀、真神级事件 | EP2 |

## 3.3 一小时体验切片(垂直切片验收基准)

0-10min:雾都镇区醒来,报社接第一个侦探委托(找走失的猫→发现猫在废弃营地);10-25min:营地探索,拿到占卜家手稿+材料清单,首遇游荡灵体(打不过,跑);25-40min:收集材料、坩埚酿药、灵视鉴定配方可信度;40-50min:服药呓语时刻、首次灵视看世界(颜色光环震撼点)、完成 2 个扮演事件;50-60min:夜幕降临,污染 25 触发第一次低语,教堂钟声指引避难→值夜者 NPC 接触,MVP 主线开启。

## 3.4 死亡与非凡特性循环

任何序列 ≥ 9 的非凡者(玩家/NPC/生物)死亡时按概率凝聚**非凡特性物品**(第 6 章)。特性是高序列魔药主材料的唯一稳定来源,机制上把 PvE 狩猎、PvP、组织战争与晋升经济打通——**你的失败是他人的材料,反之亦然**。

---

# 4. 范围与路线图

## 4.1 首发内容(MVP 1.0)

- **途径**:占卜家、观众、猎人、偷盗者、学徒(序列 9-7 可玩,9-5 数据就绪);
- **机制**:灵性、魔药、扮演消化、污染、失控、特性(基础)、灵视、占卜三法、仪式、封印物 3/2 级;
- **社会**:金镑经济、侦探事务所、报纸、值夜者任务线、极光会敌对、心理炼金会服务;
- **内容量**:~80 材料、~45 知识条目、24 封印物、10 类非凡生物、5 失控体、1 阶段 Boss、日记残页第一辑(12 页);
- **任务**:调查链一「失踪调查小队」;
- 全内容数据包驱动。

## 4.2 路线图

| 阶段 | 主要内容 | 优先级 |
|------|----------|--------|
| EP1(M4) | 五途径序列 6-5 实装、晋升仪式、特性完整版、灰雾源堡+塔罗会、七教会声望网、猎巫、任务链二/三、封印物至 40、占卜七法、会客法 | 高 |
| EP2(M5) | 序列 4 半神+神话形态(GeckoLib)、第二批四途径(水手/不眠者/收尸人/歌颂者)序列 9-5、组织战争、日记第二辑 | 高 |
| EP3(M6) | 22 途径框架社区共创、1 级封印物全实装、贝克兰德大城市、灵界维度(探索型)、真神级事件 | 中 |
| 长线 | 序列 3 圣者 NPC 剧目、跨服灰雾(Velocity/Bungee 桥接调研) | 低 |

## 4.3 内容规模总账(v0.7 目标)

| 类别 | MVP | EP1 | EP2 | EP3 | EP4(毕业线) | 累计 |
|------|-----|-----|-----|-----|--------------|------|
| 可玩序列 | 15 | 25 | 45 | 75 | 90(含序列 3-1) | 90 |
| 能力 | 45 | 80 | 150+ | 240+ | 300+ | 300+ |
| 扮演事件 | 60 | 110 | 200+ | 320+ | 400+ | 400+ |
| 魔药配方 | 15 | 25 | 45 | 75 | 75+石板配方 | 82 |
| 封印物 | 24 | 40 | 40 | 72 | 72 | 72 |
| 结构 | 8 | 12 | 14+ | 20+ | 22+ | 22+ |
| 世界事件 | 6 | 15 | 15+ | 25 | 25+终局 | 26+ |
| 日记残页 | 12 | 24 | 36 | 72 | 72+石板 21 残片 | 93 |
| 任务链 | 1 | 3 | 3 | 6 | 8 | 8 |
| 神秘材料 | 20 | 40 | 55 | 80+ | 80+ | 80+ |
| 成就 / 指令 | 12/8 | 24/12 | 34/17 | 50/22 | 60/26 | 60/26 |

---

# 5. 核心属性系统(Forge 1.20.1 · Capability 完整实现)

## 5.1 属性总表

| 属性 | 字段 | 范围 | 作用 |
|------|------|------|------|
| 途径 | pathway | ResourceLocation/null | 决定能力池、扮演准则、失控体 |
| 序列 | sequence | 9→4(-1=普通人) | 力量层级 |
| 灵性 | spirituality | 0-上限 | 能力资源,类比"蓝" |
| 灵性上限 | spiritualityMax | 途径×序列决定 | — |
| 消化度 | digestion | 0-100% | 晋升门槛,扮演法唯一来源 |
| 污染 | pollution | 0-100 | 长期腐蚀,难降 |
| 失控压力 | insanityPressure | 0-100 | 短期波动,可疏解 |
| 理智 | sanity | 0-100 | 序列 ≤5 启用,神话形态消耗 |
| 知识 | knownKnowledge | Set | 解锁配方/仪式/手册条目 |
| 扮演历史 | actingHistory | Map | 新颖度衰减依据 |
| 声望 | orgReputation | Map | 组织关系 |
| 同化特性 | assimilatedCharacteristics | List | 晋升消耗记录,影响失控掉落 |
| 金镑余额 | pennyBalance | long(以便士记账) | 经济系统(第 17 章) |
| 尊名 | tarotTitle | String | 灰雾匿名身份 |
| 通缉度 | wantedLevel | 0-5 | 猎巫/悬赏系统 |

## 5.2 PlayerMysteryData 完整实现

```java
package top.aurora.projectmystery.player;

import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import java.util.*;

/**
 * 玩家神秘学数据主体。所有写操作走受控 setter(clamp + 脏标记),
 * 网络同步由 MysteryNetwork 按脏区增量下发。
 */
public class PlayerMysteryData {
    public static final int SCHEMA_VERSION = 3;   // v0.6 = 3

    // ---- 途径与序列 ----
    private ResourceLocation pathway = null;
    private int sequence = -1;

    // ---- 核心数值 ----
    private float spirituality = 0f;
    private float spiritualityMax = 100f;
    private float digestion = 0f;
    private float pollution = 0f;
    private float insanityPressure = 0f;
    private float sanity = 100f;

    // ---- 当前魔药上下文 ----
    private ResourceLocation currentPotion = null;
    private int currentPotionQuality = 1;          // ordinal of PotionQuality

    // ---- 知识 / 扮演 / 声望 ----
    private final Set<ResourceLocation> knownKnowledge = new HashSet<>();
    private final Map<String, Long> actingHistory = new HashMap<>();
    private final Map<String, Integer> targetRepeat = new HashMap<>();
    private final Map<ResourceLocation, Integer> orgReputation = new HashMap<>();

    // ---- 特性 ----
    private final List<ResourceLocation> assimilatedCharacteristics = new ArrayList<>();

    // ---- 经济 / 社会 ----
    private long pennyBalance = 0L;                 // 1 金镑=20 苏勒=240 便士
    private int wantedLevel = 0;
    private long wantedExpireGameTime = 0L;

    // ---- 灰雾 ----
    private String tarotTitle = "";
    private int tarotSeat = -1;                     // -1 = 未入座
    private long grayFogCooldownUntil = 0L;

    // ---- 神话形态(EP2) ----
    private boolean mythicalFormUnlocked = false;
    private boolean mythicalFormActive = false;

    // ---- 状态旗标 ----
    private boolean spiritVisionActive = false;
    private long traumaUntil = 0L;                  // 精神创伤截止
    private boolean dirty = true;

    // ================== 受控访问器(节选完整版) ==================
    public ResourceLocation getPathway() { return pathway; }
    public int getSequence() { return sequence; }

    public void enterPathway(ResourceLocation pathwayId) {
        this.pathway = pathwayId; this.sequence = 9;
        this.spiritualityMax = PathwayRegistry.spiritualityMax(pathwayId, 9);
        this.spirituality = this.spiritualityMax * 0.5f;
        this.digestion = 0f; markDirty();
    }

    public void advanceSequence() {
        if (sequence <= 4) return;                  // 序列 3+ 不可玩
        this.sequence--;
        this.spiritualityMax = PathwayRegistry.spiritualityMax(pathway, sequence);
        this.digestion = 0f;
        this.currentPotion = null; markDirty();
    }

    public float getSpirituality() { return spirituality; }
    public float getSpiritualityMax() { return spiritualityMax; }
    public boolean trySpendSpirituality(float amount) {
        if (spirituality < amount) return false;
        spirituality -= amount; markDirty(); return true;
    }
    public void addSpirituality(float amount) {
        spirituality = Math.min(spiritualityMax, Math.max(0f, spirituality + amount)); markDirty();
    }

    public float getDigestion() { return digestion; }
    public void addDigestion(float amount) {
        digestion = Math.min(100f, Math.max(0f, digestion + amount)); markDirty();
    }

    public float getPollution() { return pollution; }
    public void addPollution(float amount) {
        pollution = Math.min(100f, Math.max(0f, pollution + amount)); markDirty();
    }

    public float getInsanityPressure() { return insanityPressure; }
    public void addInsanityPressure(float amount) {
        insanityPressure = Math.min(100f, Math.max(0f, insanityPressure + amount)); markDirty();
    }

    public float getSanity() { return sanity; }
    public void addSanity(float amount) {
        sanity = Math.min(100f, Math.max(0f, sanity + amount)); markDirty();
    }

    public boolean knowsKnowledge(ResourceLocation id) { return knownKnowledge.contains(id); }
    public boolean unlockKnowledge(ResourceLocation id) {
        boolean added = knownKnowledge.add(id); if (added) markDirty(); return added;
    }
    public Set<ResourceLocation> knowledgeView() { return Collections.unmodifiableSet(knownKnowledge); }

    public Map<String, Long> getActingHistory() { return actingHistory; }
    public int getTargetRepeatCount(String key) { return targetRepeat.getOrDefault(key, 0); }
    public void bumpTargetRepeat(String key, long now) {
        targetRepeat.merge(key, 1, Integer::sum); markDirty();
    }

    public int getReputation(ResourceLocation org) { return orgReputation.getOrDefault(org, 0); }
    public void addReputation(ResourceLocation org, int delta) {
        orgReputation.merge(org, delta, Integer::sum); markDirty();
    }

    public List<ResourceLocation> getAssimilatedCharacteristics() { return assimilatedCharacteristics; }
    public void recordAssimilation(ResourceLocation charId) {
        assimilatedCharacteristics.add(charId); markDirty();
    }

    public long getPennyBalance() { return pennyBalance; }
    public boolean trySpendPennies(long amount) {
        if (pennyBalance < amount) return false;
        pennyBalance -= amount; markDirty(); return true;
    }
    public void addPennies(long amount) { pennyBalance = Math.max(0, pennyBalance + amount); markDirty(); }

    public int getWantedLevel() { return wantedLevel; }
    public void setWanted(int level, long expireAt) {
        wantedLevel = Math.max(0, Math.min(5, level)); wantedExpireGameTime = expireAt; markDirty();
    }
    public void tickWantedDecay(long now) {
        if (wantedLevel > 0 && now >= wantedExpireGameTime) { wantedLevel = 0; markDirty(); }
    }

    public String getTarotTitle() { return tarotTitle; }
    public int getTarotSeat() { return tarotSeat; }
    public void claimSeat(int seat, String title) { tarotSeat = seat; tarotTitle = title; markDirty(); }

    public ResourceLocation getCurrentPotion() { return currentPotion; }
    public int getCurrentPotionQualityOrdinal() { return currentPotionQuality; }
    public void setCurrentPotion(ResourceLocation potion, int qualityOrdinal) {
        this.currentPotion = potion; this.currentPotionQuality = qualityOrdinal; markDirty();
    }

    public boolean isMythicalFormUnlocked() { return mythicalFormUnlocked; }
    public boolean isMythicalFormActive() { return mythicalFormActive; }
    public void setMythicalFormUnlocked(boolean v) { mythicalFormUnlocked = v; markDirty(); }
    public void setMythicalFormActive(boolean v) { mythicalFormActive = v; markDirty(); }

    public boolean isSpiritVisionActive() { return spiritVisionActive; }
    public void setSpiritVisionActive(boolean v) { spiritVisionActive = v; markDirty(); }

    public boolean hasTrauma(long now) { return now < traumaUntil; }
    public void applyTrauma(long until) { traumaUntil = until; markDirty(); }

    public void markDirty() { dirty = true; }
    public boolean consumeDirty() { boolean d = dirty; dirty = false; return d; }

    // ================== 死亡惩罚 ==================
    public void applyDeathPenalty(String mode) {
        switch (mode) {
            case "keep" -> { /* 无惩罚 */ }
            case "drop_potion_progress" -> { digestion = 0f; insanityPressure = 0f; }
            case "drop_sequence" -> {
                if (sequence >= 0 && sequence < 9) {
                    sequence++;
                    spiritualityMax = PathwayRegistry.spiritualityMax(pathway, sequence);
                }
                digestion = 0f; insanityPressure = 0f;
            }
        }
        spirituality = spiritualityMax * 0.25f;
        markDirty();
    }

    // ================== NBT 序列化 ==================
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("SchemaVersion", SCHEMA_VERSION);
        if (pathway != null) tag.putString("Pathway", pathway.toString());
        tag.putInt("Sequence", sequence);
        tag.putFloat("Spirituality", spirituality);
        tag.putFloat("SpiritualityMax", spiritualityMax);
        tag.putFloat("Digestion", digestion);
        tag.putFloat("Pollution", pollution);
        tag.putFloat("InsanityPressure", insanityPressure);
        tag.putFloat("Sanity", sanity);
        if (currentPotion != null) tag.putString("CurrentPotion", currentPotion.toString());
        tag.putInt("PotionQuality", currentPotionQuality);

        ListTag kl = new ListTag();
        knownKnowledge.forEach(id -> kl.add(StringTag.valueOf(id.toString())));
        tag.put("Knowledge", kl);

        CompoundTag at = new CompoundTag();
        actingHistory.forEach(at::putLong);
        tag.put("ActingHistory", at);

        CompoundTag tr = new CompoundTag();
        targetRepeat.forEach(tr::putInt);
        tag.put("TargetRepeat", tr);

        CompoundTag rt = new CompoundTag();
        orgReputation.forEach((k, v) -> rt.putInt(k.toString(), v));
        tag.put("OrgReputation", rt);

        ListTag cl = new ListTag();
        assimilatedCharacteristics.forEach(id -> cl.add(StringTag.valueOf(id.toString())));
        tag.put("Characteristics", cl);

        tag.putLong("PennyBalance", pennyBalance);
        tag.putInt("WantedLevel", wantedLevel);
        tag.putLong("WantedExpire", wantedExpireGameTime);
        tag.putString("TarotTitle", tarotTitle);
        tag.putInt("TarotSeat", tarotSeat);
        tag.putLong("GrayFogCooldown", grayFogCooldownUntil);
        tag.putBoolean("MythUnlocked", mythicalFormUnlocked);
        tag.putLong("TraumaUntil", traumaUntil);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        int version = tag.getInt("SchemaVersion");
        if (version < 3) MysteryDataMigrations.migrate(tag, version);   // v1/v2 → v3

        pathway = tag.contains("Pathway") ? ResourceLocation.tryParse(tag.getString("Pathway")) : null;
        sequence = tag.contains("Sequence") ? tag.getInt("Sequence") : -1;
        spirituality = tag.getFloat("Spirituality");
        spiritualityMax = Math.max(1f, tag.getFloat("SpiritualityMax"));
        digestion = tag.getFloat("Digestion");
        pollution = tag.getFloat("Pollution");
        insanityPressure = tag.getFloat("InsanityPressure");
        sanity = tag.contains("Sanity") ? tag.getFloat("Sanity") : 100f;
        currentPotion = tag.contains("CurrentPotion")
                ? ResourceLocation.tryParse(tag.getString("CurrentPotion")) : null;
        currentPotionQuality = tag.getInt("PotionQuality");

        knownKnowledge.clear();
        tag.getList("Knowledge", Tag.TAG_STRING)
           .forEach(t -> knownKnowledge.add(ResourceLocation.tryParse(t.getAsString())));

        actingHistory.clear();
        CompoundTag at = tag.getCompound("ActingHistory");
        at.getAllKeys().forEach(k -> actingHistory.put(k, at.getLong(k)));

        targetRepeat.clear();
        CompoundTag tr = tag.getCompound("TargetRepeat");
        tr.getAllKeys().forEach(k -> targetRepeat.put(k, tr.getInt(k)));

        orgReputation.clear();
        CompoundTag rt = tag.getCompound("OrgReputation");
        rt.getAllKeys().forEach(k -> orgReputation.put(ResourceLocation.tryParse(k), rt.getInt(k)));

        assimilatedCharacteristics.clear();
        tag.getList("Characteristics", Tag.TAG_STRING)
           .forEach(t -> assimilatedCharacteristics.add(ResourceLocation.tryParse(t.getAsString())));

        pennyBalance = tag.getLong("PennyBalance");
        wantedLevel = tag.getInt("WantedLevel");
        wantedExpireGameTime = tag.getLong("WantedExpire");
        tarotTitle = tag.getString("TarotTitle");
        tarotSeat = tag.contains("TarotSeat") ? tag.getInt("TarotSeat") : -1;
        grayFogCooldownUntil = tag.getLong("GrayFogCooldown");
        mythicalFormUnlocked = tag.getBoolean("MythUnlocked");
        traumaUntil = tag.getLong("TraumaUntil");
        markDirty();
    }
}
```

## 5.3 Capability 注册、Provider 与事件挂载(完整)

```java
package top.aurora.projectmystery.player;

public class MysteryCapabilities {
    public static final Capability<PlayerMysteryData> MYSTERY_DATA =
        CapabilityManager.get(new CapabilityToken<>() {});

    @SubscribeEvent  // MOD 总线
    public static void register(RegisterCapabilitiesEvent event) {
        event.register(PlayerMysteryData.class);
    }
}

public class PlayerMysteryDataProvider implements ICapabilitySerializable<CompoundTag> {
    public static final ResourceLocation ID =
        new ResourceLocation(ProjectMystery.MOD_ID, "mystery_data");
    private final PlayerMysteryData backend = new PlayerMysteryData();
    private final LazyOptional<PlayerMysteryData> optional = LazyOptional.of(() -> backend);

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == MysteryCapabilities.MYSTERY_DATA ? optional.cast() : LazyOptional.empty();
    }
    @Override public CompoundTag serializeNBT() { return backend.serializeNBT(); }
    @Override public void deserializeNBT(CompoundTag nbt) { backend.deserializeNBT(nbt); }
    public void invalidate() { optional.invalidate(); }
}

@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID)
public class CapabilityEvents {

    @SubscribeEvent
    public static void onAttach(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player)
            event.addCapability(PlayerMysteryDataProvider.ID, new PlayerMysteryDataProvider());
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        event.getOriginal().getCapability(MysteryCapabilities.MYSTERY_DATA).ifPresent(oldData ->
            event.getEntity().getCapability(MysteryCapabilities.MYSTERY_DATA).ifPresent(newData -> {
                newData.deserializeNBT(oldData.serializeNBT());
                if (event.isWasDeath())
                    newData.applyDeathPenalty(ServerConfig.DEATH_PENALTY_MODE.get());
            }));
        event.getOriginal().invalidateCaps();
    }

    @SubscribeEvent public static void onJoin(PlayerEvent.PlayerLoggedInEvent e) { sync(e.getEntity()); }
    @SubscribeEvent public static void onRespawn(PlayerEvent.PlayerRespawnEvent e) { sync(e.getEntity()); }
    @SubscribeEvent public static void onDim(PlayerEvent.PlayerChangedDimensionEvent e) { sync(e.getEntity()); }

    private static void sync(Player p) { if (p instanceof ServerPlayer sp) MysteryNetwork.sendFullSync(sp); }
}

public final class MysteryDataHelper {
    public static PlayerMysteryData get(Player player) {
        return player.getCapability(MysteryCapabilities.MYSTERY_DATA)
                     .orElseThrow(() -> new IllegalStateException("MysteryData missing on " + player.getName()));
    }
    public static void ifPresent(Player player, Consumer<PlayerMysteryData> action) {
        player.getCapability(MysteryCapabilities.MYSTERY_DATA).ifPresent(action);
    }
}
```

## 5.4 灵性数值规则(序列 9-4)

| 序列 | 上限区间(随途径浮动) | 自然恢复 | 灵视消耗/s | 备注 |
|------|----------------------|----------|------------|------|
| 9 | 100-122 | 1.0/20s | 0.8-1.2 | |
| 8 | 130-150 | 1.2/18s | 0.6-1.0 | |
| 7 | 155-180 | 1.5/15s | 0.4-0.8 | 可入灰雾 |
| 6 | 190-230 | 2.0/12s | 0.3-0.6 | 大型仪式主持 |
| 5 | 250-310 | 2.5/10s | 0.2-0.4 | 灵性外放/领域雏形 |
| 4 | 340-420 | 3.0/8s | 0.1-0.3 | 半神,神话形态 |

**恢复条件**:非战斗(5s 内未受击)+ 光照 ≥7 + 压力 <30。**环境系数**:教堂庇护区 ×1.5,猎人户外 ×1.1,偷盗者阴影 ×1.3,不眠者夜间 ×1.4(EP2),灰雾内 ×0(不恢复)。

```java
@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID)
public class SpiritualityRegenHandler {
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) return;
        Player player = event.player;
        if (player.tickCount % 20 != 0) return;

        MysteryDataHelper.ifPresent(player, data -> {
            if (data.getPathway() == null) return;
            data.tickWantedDecay(player.level().getGameTime());

            boolean inCombat = player.tickCount - player.getLastHurtByMobTimestamp() < 100;
            boolean lit = player.level().getMaxLocalRawBrightness(player.blockPosition()) >= 7;
            if (inCombat || !lit || data.getInsanityPressure() >= 30f) return;

            float regen = PathwayRegistry.regenRate(data.getPathway(), data.getSequence())
                        * EnvironmentModifiers.compute(player, data)
                        * ServerConfig.SPIRIT_REGEN_MULT.get().floatValue();
            data.addSpirituality(regen);
            if (data.consumeDirty()) MysteryNetwork.sendCoreStats((ServerPlayer) player, data);
        });
    }
}
```

## 5.5 污染与失控压力

**双轨模型**:压力(短期,可通过睡眠 -20、教堂祈祷 -15/日、心理炼金会治疗 -40 付费疏解)与污染(长期,仅净化仪式/教会赦免/特定封印物可降,且每次净化上限 -25)。压力 100 或污染 100 均触发失控。

| 数值区间 | 状态 | 客户端效果 | 检定频率 |
|----------|------|-----------|----------|
| 0-24 | 稳定 | 无 | 无 |
| 25-49 | 轻度 | 偶发低语、边缘轻粒子 | 5min |
| 50-74 | 危险 | 幻象生物、能力 10% 失效 | 2min |
| 75-99 | 临界 | 屏幕暗角、负面状态、周期检定 | 30s |
| 100 | 失控 | 失控结局 | 立即 |

**失控结局三模式**(server 配置 `breakdown_mode`):

| 模式 | 效果 |
|------|------|
| recoverable(默认) | 倒地 30s + 生成本途径专属失控体 + 掉破碎特性 + 24h 精神创伤 |
| permanent | 序列 +1(降级)+ 部分知识封锁 + 掉破碎特性 |
| death | 死亡 + 掉落非凡物品 + 凝聚完整特性供他人拾取 |

> **特性守恒在失败路径的体现**:无论哪种模式,失控必产出特性物品——他人的失败是你的材料。

---

# 6. 非凡特性系统(完整实现)

## 6.1 规则总纲

- 每位非凡者体内蕴含与**当前序列**对应的非凡特性;
- **特性不灭定律**:非凡者死亡后,特性以概率凝聚为实体物品(结晶/残骸/器官形态,材质按途径染色),或逸散进入区块「灵性沉积池」,数个游戏日后在附近以特性凝集体精英怪重现;
- 特性可作为**同途径同序列魔药主材料的替代品**(纯度 ≥0.6);
- 特性可在封印仪式中**炼入封印物**(效果 ×(1+纯度×0.5),危险等级 +1);
- **同化风险**:携带非本途径特性、或高出自身 ≥2 序列的特性,每分钟侵蚀检定 → +污染 +低语。

## 6.2 掉落规则

| 死亡来源 | 完整特性 | 破碎特性 | 逸散沉积 |
|----------|----------|----------|----------|
| 非凡生物(自然生成) | 25% | 40% | 35% |
| 失控体 | 60% | 30% | 10% |
| 玩家(death 模式) | 80% | 15% | 5% |
| 阶段 Boss / 半神 | 100% 保底 | — | — |

破碎特性 ×3(同途径同序列)+ 净化仪式 → 完整特性(纯度 0.7)。被污染特性(`Corrupted`)酿药必出「污染」品质。

## 6.3 完整代码

```java
package top.aurora.projectmystery.characteristic;

/** 非凡特性物品:NBT { Pathway, Sequence, Purity, Corrupted, SourceUUID } */
public class BeyonderCharacteristicItem extends Item {

    public BeyonderCharacteristicItem(Properties props) { super(props.stacksTo(1).rarity(Rarity.EPIC)); }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity holder, int slot, boolean selected) {
        if (level.isClientSide() || level.getGameTime() % 1200 != 0) return;   // 每分钟
        if (!(holder instanceof ServerPlayer player)) return;
        if (!ServerConfig.ASSIMILATION_ENABLED.get()) return;

        CompoundTag tag = stack.getOrCreateTag();
        ResourceLocation cp = ResourceLocation.tryParse(tag.getString("Pathway"));
        int cs = tag.getInt("Sequence");

        MysteryDataHelper.ifPresent(player, data -> {
            boolean same = cp != null && cp.equals(data.getPathway());
            int gap = data.getSequence() - cs;                    // 正=特性更高阶
            if (same && gap < 2) return;                          // 安全持有
            float erosion = (same ? 0.5f : 1.5f) + Math.max(0, gap);
            data.addPollution(erosion * ServerConfig.POLLUTION_MULT.get().floatValue());
            if (level.random.nextFloat() < 0.15f)
                MysteryNetwork.sendInsanity((ServerPlayer) player, InsanityFx.WHISPER, 0.4f);
        });
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level lvl, List<Component> tip, TooltipFlag flag) {
        CompoundTag tag = stack.getOrCreateTag();
        tip.add(Component.translatable("pm.char.pathway",
                Component.translatable("pathway." + tag.getString("Pathway").replace(':', '.'))));
        tip.add(Component.translatable("pm.char.sequence", tag.getInt("Sequence")));
        tip.add(Component.translatable("pm.char.purity",
                String.format("%.0f%%", tag.getFloat("Purity") * 100)));
        if (tag.getBoolean("Corrupted"))
            tip.add(Component.translatable("pm.char.corrupted").withStyle(ChatFormatting.DARK_RED));
    }
}

@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID)
public class CharacteristicDropHandler {

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity dead = event.getEntity();
        if (dead.level().isClientSide() || !ServerConfig.CHAR_DROP_ENABLED.get()) return;

        BeyonderProfile profile = BeyonderProfileHelper.resolve(dead);
        if (profile == null) return;

        RandomSource rand = dead.level().random;
        DropTier tier = DropTier.roll(dead, profile, rand);
        ServerLevel level = (ServerLevel) dead.level();
        switch (tier) {
            case FULL -> drop(level, dead, CharacteristicFactory.full(
                    profile.pathway(), profile.sequence(), 1.0f, dead.getUUID()));
            case FRAGMENT -> drop(level, dead, CharacteristicFactory.fragment(
                    profile.pathway(), profile.sequence()));
            case DISSIPATE -> SpiritSedimentManager.get(level)
                    .deposit(dead.chunkPosition(), profile);
        }
    }

    private static void drop(ServerLevel level, LivingEntity dead, ItemStack stack) {
        Containers.dropItemStack(level, dead.getX(), dead.getY(), dead.getZ(), stack);
        level.playSound(null, dead.blockPosition(),
                ModSounds.CHARACTERISTIC_CONDENSE.get(), SoundSource.NEUTRAL, 0.8f, 0.9f);
    }
}

/** 区块级灵性沉积池:SavedData 持久化,仅加载区块 tick */
public class SpiritSedimentManager extends SavedData {
    private static final String KEY = "pm_spirit_sediment";
    private final Map<Long, Integer> sediment = new HashMap<>();     // ChunkPos.toLong → 份数

    public static SpiritSedimentManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                SpiritSedimentManager::load, SpiritSedimentManager::new, KEY);
    }

    public void deposit(ChunkPos pos, BeyonderProfile profile) {
        long key = pos.toLong();
        int total = sediment.merge(key, 1, Integer::sum);
        setDirty();
        if (total >= 3) WorldEventScheduler.queueCondensateSpawn(pos, profile);   // 特性凝集体
    }

    public int query(ChunkPos pos) { return sediment.getOrDefault(pos.toLong(), 0); }
    public void drain(ChunkPos pos) { sediment.remove(pos.toLong()); setDirty(); }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag map = new CompoundTag();
        sediment.forEach((k, v) -> map.putInt(Long.toString(k), v));
        tag.put("Sediment", map);
        return tag;
    }
    public static SpiritSedimentManager load(CompoundTag tag) {
        SpiritSedimentManager m = new SpiritSedimentManager();
        CompoundTag map = tag.getCompound("Sediment");
        map.getAllKeys().forEach(k -> m.sediment.put(Long.parseLong(k), map.getInt(k)));
        return m;
    }
}
```

## 6.4 灵视中的特性感知

灵视激活时,16 格内的特性物品/凝集体向玩家方向渲染**金色细线**(客户端仅收到方向向量,不收坐标)——鼓励狩猎循环而不做成免费雷达。

---

# 7. 途径与序列设计(全 22 条入册 · 9 条详设)

## 7.1 二十二条途径总册(序列 9 → 0 全名)

**分组按上级归属**;实装:✅ 首发详设(9-4) | 🔷 EP2 详设(9-5) | ⬜ 占位(JSON 注册+传闻条目)。

| 组 | 途径 | 序列 9 → 0 | 实装 |
|----|------|------------|------|
| 源堡 | 占卜家 | 占卜家 → 小丑 → 魔术师 → 无面人 → 秘偶大师 → 诡法师 → 古代学者 → 奇迹师 → 诡秘侍者 → **愚者** | ✅ |
| 源堡 | 学徒 | 学徒 → 戏法大师 → 占星人 → 记录官 → 旅行家 → 秘法师 → 漫游者 → 旅法师 → 星之匙 → **门** | ✅ |
| 源堡 | 偷盗者 | 偷盗者 → 诈骗师 → 解密学者 → 盗火人 → 窃梦家 → 寄生者 → 欺瞒导师 → 命运木马 → 时之虫 → **错误** | ✅ |
| 混沌海 | 观众 | 观众 → 读心者 → 心理医生 → 催眠师 → 梦境行者 → 操纵师 → 织梦人 → 洞察者 → 童话作家 → **空想家** | ✅ |
| 混沌海 | 秘祈人 | 秘祈人 → 倾听者 → 隐修士 → 蔷薇主教 → 牧羊人 → 黑骑士 → 三首圣堂 → 秽语长老 → 暗天使 → **倒吊人** | ⬜ |
| 混沌海 | 歌颂者 | 歌颂者 → 祈光人 → 太阳神官 → 公证人 → 光之祭司 → 无暗者 → 正义导师 → 逐光者 → 纯白天使 → **太阳** | 🔷 |
| 混沌海 | 水手 | 水手 → 暴怒之民 → 航海家 → 风眷者 → 海洋歌者 → 灾难主祭 → 海王 → 天灾 → 雷神 → **暴君** | 🔷 |
| 混沌海 | 阅读者 | 阅读者 → 推理学员 → 侦探 → 博学者 → 秘术导师 → 预言家 → 洞悉者 → 智天使 → 全知之眼 → **白塔** | ⬜ |
| 黑夜 | 不眠者 | 不眠者 → 午夜诗人 → 梦魇 → 安魂师 → 灵巫 → 守夜人 → 恐惧主教 → 隐秘之仆 → 厄难骑士 → **黑暗** | 🔷 |
| 黑夜 | 收尸人 | 收尸人 → 掘墓人 → 通灵者 → 死灵导师 → 看门人 → 不死者 → 摆渡人 → 死亡执政官 → 苍白皇帝 → **死神** | 🔷 |
| 黑夜 | 战士 | 战士 → 格斗家 → 武器大师 → 黎明骑士 → 守护者 → 猎魔者 → 银骑士 → 荣耀者 → 神明之手 → **黄昏巨人** | ⬜ |
| 灾祸 | 猎人 | 猎人 → 挑衅者 → 纵火家 → 阴谋家 → 收割者 → 铁血骑士 → 战争主教 → 天气术士 → 征服者 → **红祭司** | ✅ |
| 灾祸 | 刺客 | 刺客 → 教唆者 → 女巫 → 欢愉 → 痛苦 → 绝望 → 不老 → 灾难 → 末日 → **原初魔女** | ⬜ |
| 母神 | 耕种者 | 耕种者 → 医师 → 丰收祭司 → 生物学家 → 德鲁伊 → 古代炼金师 → 抬棺人 → 荒芜主母 → 自然行者 → **母亲** | ⬜ |
| 母神 | 药师 | 药师 → 驯兽师 → 吸血鬼 → 魔药教授 → 深红学者 → 巫王 → 召唤大师 → 创生者 → 美神 → **月亮** | ⬜ |
| 秩序 | 律师 | 律师 → 野蛮人 → 贿赂者 → 腐化男爵 → 混乱导师 → 堕落伯爵 → 狂乱法师 → 熵之公爵 → 弑序亲王 → **黑皇帝** | ⬜ |
| 秩序 | 仲裁人 | 仲裁人 → 治安官 → 审讯者 → 法官 → 惩戒骑士 → 律令法师 → 混乱猎手 → 平衡者 → 秩序之手 → **审判者** | ⬜ |
| 母树 | 罪犯 | 罪犯 → 折翼天使 → 连环杀手 → 恶魔 → 欲望使徒 → 魔鬼 → 呓语者 → 鲜血大公 → 污秽君王 → **深渊** | ⬜ |
| 母树 | 囚犯 | 囚犯 → 疯子 → 狼人 → 活尸 → 怨魂 → 木偶 → 沉默门徒 → 古代邪物 → 神孽 → **被缚者** | ⬜ |
| 知识妖 | 窥秘人 | 窥秘人 → 格斗学者 → 巫师 → 卷轴教授 → 星象师 → 神秘学家 → 预言大师 → 贤者 → 知识皇帝 → **隐者** | ⬜ |
| 知识妖 | 通识者 | 通识者 → 考古学家 → 鉴定师 → 机械专家 → 天文学家 → 炼金术士 → 奥秘学者 → 知识导师 → 启蒙者 → **完美者** | ⬜ |
| 命运 | 怪物 | 怪物 → 机器 → 幸运儿 → 灾祸教士 → 赢家 → 厄运法师 → 混乱行者 → 先知 → 水银之蛇 → **命运之轮** | ⬜ |

> 占位途径在知识手册中呈现为「传闻」条目(灰字,自写百科式短描述+来源出处如"某本烧焦的笔记提到…"),数据包 `pathways/*.json` 全部注册,`implemented_sequences=[]`。社区可通过数据包直接填充(第 34 章 API)。

## 7.2 序列职级通用规则

| 序列段 | 称谓 | 晋升要求 | 解锁 |
|--------|------|----------|------|
| 9-6 | 低/中序列 | 消化 100% + 魔药 + 稳定性检定 | — |
| 5 | 中高序列 | 消化 100% + **魔药 + 晋升仪式**双重检定 | 灵性外放、领域雏形 |
| 4 | 半神 | 魔药 + 大型仪式 + 途径试炼 | **神话生物形态**(GeckoLib) |
| 3-0 | 圣者/天使/真神 | 不可玩 | 世界观 NPC/事件/圣物来源 |

**呓语时刻**:服药瞬间 3-8s——屏幕灰化、低语字幕(数据包 `whispers/` 池,全部原创文案)、不可移动;污染 ≥50 时低语引用玩家最近行为(context_aware 条目),并追加一次隐藏检定。

**扮演准则(角色卡)**:每个序列在知识手册显示一句原创「准则」,是该序列所有扮演事件的母题。例:占卜家「不去占卜与神有关的事,不去占卜自己的命运」→ 违反(对神名/自身占卜)直接 +15 压力——**准则不只是提示,也是可违反的规则**。

## 7.3 占卜家途径(→ 愚者)✅ 序列 9-4 全详设

**定位**:信息之王 → 欺诈与傀儡 → 历史与奇迹。全 Mod 情报中枢,PvE 弱开局、多人服后期权力顶点。

### 序列 9 占卜家

| 属性 | 值 | | 属性 | 值 |
|---|---|---|---|---|
| 灵性上限 | 122 | | 服药压力 | +10 |
| 主材料 | 夜蚀花种子 | | 消化时长(典型) | 2-3h |

| 能力 | 类型 | 消耗 | 冷却 | 说明 |
|------|------|------|------|------|
| 灵视 | 切换 | 1.0/s | — | 灵体光环、情绪色、污染可视、特性金线 |
| 危险直觉 | 被动 | 0 | 30s | 3s 内将受攻击时听觉+屏幕边缘提示 |
| 简易占卜(是/否) | 主动 | 15 | 60s | 可信度分级回答(第 11 章) |
| 灵摆占卜 | 主动+道具 | 10 | 30s | 方位/距离型问题 |

扮演事件:seer9_first_reading(为他人占卜一次 +10/新颖)、seer9_verify_prophecy(占卜结果被现实验证 +15)、seer9_ritual_observe(旁观完整仪式 +8)、seer9_never_self(连续 3 日不占自身命运 +12,准则事件)。

### 序列 8 小丑

| 灵性上限 144 | 服药压力 +12 | 主材料 深灰灵体之泪 |
|---|---|---|

| 能力 | 类型 | 消耗 | 冷却 | 说明 |
|------|------|------|------|------|
| 纸牌飞刃 | 主动 | 4/张 | 0.5s | 纸变利刃投掷,3 连发,可附灵性燃烧 |
| 完美平衡 | 被动 | 0 | — | 免疫摔倒/冰面打滑/击退 -50%,可走 1 格宽绳索 |
| 表情操控 | 主动 | 8 | 10s | 对 NPC 说谎成功率 +40%,交易价格 -10% |
| 直觉预判 | 被动 | 0 | 8s | 15% 概率完全闪避近战(替身残影) |

扮演事件:clown8_performance(向 ≥2 名观众表演杂耍动作序列 +12)、clown8_smile_mask(压力 ≥50 时完成一场战斗 +15,"痛苦也要微笑")、clown8_dodge_master(单场闪避 5 次 +10)。

### 序列 7 魔术师

| 灵性上限 175 | 服药压力 +15 | 主材料 幻形蛇的毒腺 |
|---|---|---|

| 能力 | 类型 | 消耗 | 冷却 | 说明 |
|------|------|------|------|------|
| 火焰跳跃 | 主动 | 18 | 6s | 化作火光闪现 8 格,落点小范围燃烧 |
| 替身纸人 | 主动 | 25 | 45s | 预置纸人,受致命伤时与纸人换位(30s 内有效) |
| 空气子弹 | 主动 | 6 | 1s | 无形弹道,中距离骚扰 |
| 舞台幻术 | 主动 | 20 | 30s | 5×5 区域投影幻象地形/生物 10s(可骗 AI 寻路) |

扮演事件:magician7_grand_escape(用替身逃脱致命一击 +20)、magician7_flashy_entry(火焰跳跃进入战斗并 5s 内命中 +12)、magician7_deceive_mob(幻术使 ≥3 只敌对生物走错路 +15)。

### 序列 6 无面人

| 灵性上限 212 | 服药压力 +22 | 主材料 变形怪之血(或特性替代) |
|---|---|---|

| 能力 | 类型 | 消耗 | 冷却 | 说明 |
|------|------|------|------|------|
| 完全变形 | 持续 | 25+2/s | 60s | 变为已记录形体(生物/玩家),姓名牌同步,灵视可破 |
| 形体记录 | 主动 | 10 | 10s | 注视 3s 记录,8 槽 |
| 毛发操控 | 主动 | 15 | 20s | 6 格发丝鞭:缴械/绊倒/取物 |
| 声线模仿 | 被动 | 0 | — | 以被伪装者名义发消息(服务器可关,默认带杂音标记) |

扮演事件:faceless6_perfect_infiltration(伪装下在敌营 8 格停留 60s +18)、faceless6_identity_puzzle(3 形体完成同一任务节点 +22)、faceless6_no_true_face(整日不以真容示人 +15)、faceless6_rescue_in_disguise(伪装救人不暴露 +20)。

### 序列 5 秘偶大师(仪式晋升起点)

| 灵性上限 265 | 服药压力 +30 | 晋升 魔药+「提线仪式」(月下/5 白蜡烛/傀儡丝) |
|---|---|---|

| 能力 | 类型 | 消耗 | 冷却 | 说明 |
|------|------|------|------|------|
| 灵体之线 | 切换 | 3/s | — | 见万物灰白丝线+生物情绪线抖动(预判) |
| 制偶 | 主动 | 60 | 120s | HP<20% 非 Boss 生物 → 傀儡(保留攻击 AI,阵营归主),上限 3 |
| 傀儡收纳 | 主动 | 5 | 2s | 收入傀儡卷轴,可携带可交易(灰雾硬通货) |
| 傀儡操纵 | 引导 | 8/s | 30s | 第一人称附身傀儡,本体静止暴露 |

扮演事件:marionettist5_army_of_one(3 傀儡协同赢下战斗且本体无伤 +25)、marionettist5_puppet_sacrifice(傀儡代死 +20)、marionettist5_collect_rare(收录新生物类型 +15/类)、marionettist5_hidden_master(全程只以傀儡露面完成组织任务 +30)。

### 序列 4 诡法师(半神 · EP2)

- **晋升试炼**:在灰雾中主持一场 ≥5 人塔罗会 + 击败「历史投影」(个人 Boss 战:与自己过往行为数据生成的镜像作战);
- **神话形态「雾影操偶主」**(GeckoLib):灰雾质感多臂人形,背后浮现巨大提线轮盘;
- 形态能力:①「万物提线」——将 16 格内至多 5 只 ≤ 序列 6 敌人临时傀儡化 15s;②「历史回溯」——8s 内自身状态(HP/位置/灵性)回滚到 10s 前(每形态一次);
- 常态新增:傀儡上限 6、可跨维度遥控傀儡、「诡秘替身」(每 72h 一次免死,以一具傀儡湮灭为代价);
- 代价:形态中 sanity -0.8/s;每次变身后 24h「神性余韵」(普通人恐惧逃离,目击计数 +1)。

## 7.4 观众途径(→ 空想家)✅ 序列 9-4 全详设

**定位**:精神领域专家、团队大脑与"读心侦探"。

### 序列 9 观众

| 灵性上限 118 | 服药压力 +10 | 主材料 冥想菇孢子粉 |
|---|---|---|

| 能力 | 类型 | 消耗 | 冷却 | 说明 |
|------|------|------|------|------|
| 情绪观察 | 切换 | 0.8/s | — | 生物头顶情绪图标(激怒/恐惧/平静/异常),异常=被操控或伪装 |
| 行为预测 | 被动 | 0 | 12s | 被锁定目标的下一动作提示(前摇箭头) |
| 冷静自持 | 被动 | 0 | — | 压力增长 -15% |
| 微表情审讯 | 主动 | 12 | 20s | 对话 NPC 时判断"它在说谎吗"(可信度分级) |

扮演事件:spectator9_silent_watch(潜行观察一只生物完整行为循环 60s +10)、spectator9_read_liar(识破一次 NPC 谎言并据此完成委托 +15)、spectator9_no_first_move(遭遇战中先观察 10s 再出手 +8)。

### 序列 8 读心者

| 灵性上限 140 | 服药压力 +13 | 主材料 灰烬人偶的丝线 |
|---|---|---|

| 能力 | 类型 | 消耗 | 冷却 | 说明 |
|------|------|------|------|------|
| 表层读心 | 主动 | 15 | 15s | 读取目标"表层念头"(NPC:任务线索/交易底价;玩家:其最近 5s 输入意图摘要,可被心灵屏障挡) |
| 心理暗示 | 主动 | 20 | 40s | 使目标短暂改变仇恨对象/移动意图 5s |
| 精神刺针 | 主动 | 10 | 3s | 小额真实伤害+0.5s 迟滞(精神系基础攻击) |

扮演事件:mindreader8_bargain(读底价后砍价成功 +12)、mindreader8_prevent_ambush(读出伏击意图并化解 +18)、mindreader8_ethics(读到隐私后不加利用——由后续行为判定 +10,心理炼金会伦理线联动)。

### 序列 7 心理医生

| 灵性上限 168 | 服药压力 +16 | 主材料 安眠花蜜蜡 |
|---|---|---|

| 能力 | 类型 | 消耗 | 冷却 | 说明 |
|------|------|------|------|------|
| 精神安抚 | 主动 | 20 | 30s | 目标玩家压力 -12 / 移除恐慌类 debuff;对敌对生物=解除激怒 |
| 狂乱诱导 | 主动 | 25 | 45s | 目标生物无差别攻击最近单位 8s(对玩家为屏幕扭曲+操作反转 3s,PvP 减半) |
| 心理治疗(仪式化) | 长交互 | 40 | 每人每日 | 为他人 -25 压力并移除精神创伤(收费服务玩法) |
| 群体镇定 | 主动 | 30 | 90s | 8 格半径友方压力增长暂停 30s |

扮演事件:psychiatrist7_clinic(单日治疗 3 名不同玩家 +20)、psychiatrist7_talk_down(用安抚终止一次他人失控前兆(压力 ≥90→<75)+25)、psychiatrist7_madness_weapon(狂乱诱导致两敌互杀 +12)。

### 序列 6 催眠师

| 灵性上限 205 | 服药压力 +24 | 主材料 摇篮蛾的复眼 |
|---|---|---|

| 能力 | 类型 | 消耗 | 冷却 | 说明 |
|------|------|------|------|------|
| 催眠 | 主动 | 30 | 45s | 低抗性生物沉睡 15s(受击即醒);玩家渐暗+减速 40%,Shift 挣脱 |
| 记忆修改 | 主动 | 40 | 300s | 清除目标仇恨与威胁记录;村民交易冷却重置(每日一次) |
| 心灵屏障 | 被动 | 0 | — | 低语类污染 -30%;被读心/占卜时获得感知 |
| 暗示种子 | 主动 | 25 | 120s | 埋延迟指令(条件触发式),10min 内生效 |

扮演事件:hypnotist6_bloodless(不流血化解 ≥3 敌遭遇 +22)、hypnotist6_erase_pursuit(记忆修改解除队友被追杀 +18)、hypnotist6_barrier_endure(屏障下穿越污染 ≥50 区域 +12)。

### 序列 5 梦境行者

| 灵性上限 258 | 服药压力 +28 | 晋升 魔药+「入梦仪式」(床边,参与者共享梦境试炼) |
|---|---|---|

| 能力 | 类型 | 消耗 | 冷却 | 说明 |
|------|------|------|------|------|
| 入梦 | 主动 | 50 | 一夜一次 | 进入个人梦境层(袖珍维度:去过的区块碎片+记忆扭曲拼贴);梦中可安全整理手册、演练占卜 |
| 拖入梦境 | 主动 | 45 | 600s | 将沉睡生物/同意玩家拖入梦境 1v1「梦境对决」(精神比拼小游戏,胜者予败者 60s 现实 debuff) |
| 梦境信标 | 主动 | 20 | — | 梦中置信标,醒后 30min 内可对该现实坐标远程施放一次安抚/狂乱 |
| 清醒梦 | 被动 | 0 | — | 睡眠不再跳过失控检定;夜间事件预知梦提示 |

扮演事件:dreamwalker5_dream_rescue(梦中解除队友精神创伤 +28)、dreamwalker5_nightmare_hunt(击败入侵自己梦境的梦魇残片 +20)、dreamwalker5_never_force(放弃可乘之危的拖入机会 +10)。

### 序列 4 操纵师(半神 · EP2)

- **试炼**:同时维持 3 名 NPC 的"心象剧场"(保护其精神世界抵御污染潮 5min);
- **神话形态「千面心影」**:半透明多重叠影人形,周身漂浮眼状光斑;
- 形态能力:①「心象领域」12 格——领域内敌方全员情绪可视+行动前摇预告,友方免疫精神控制;②「傀心一指」——将一名 ≤ 序列 6 敌人完全操纵 8s(玩家为强制第三人称观战);
- 常态新增:表层读心升级为"深层读忆"(可读取 NPC 记忆碎片=隐藏任务线索)、暗示种子可存 3 枚;
- 代价:同诡法师(sanity 流失+神性余韵)。

## 7.5 猎人途径(→ 红祭司)✅ 序列 9-4 全详设

**定位**:战斗与狩猎顶点,火焰、阴谋与战争的序曲。

### 序列 9 猎人

| 灵性上限 110 | 服药压力 +8 | 主材料 红睛巨狼的心脏 |
|---|---|---|

| 能力 | 类型 | 消耗 | 冷却 | 说明 |
|------|------|------|------|------|
| 追踪嗅觉 | 切换 | 0.6/s | — | 高亮 32 格内目标类型足迹(生物/玩家可选),雨天精度减半 |
| 陷阱大师 | 被动 | 0 | — | 陷阱类方块放置速度 ×2,自设陷阱伤害 +30%,可拆敌方陷阱回收 |
| 野外感知 | 被动 | 0 | — | 视野边缘威胁方向提示;浓雾/夜晚视距惩罚 -50% |
| 强弓术 | 被动 | 0 | — | 弓弩满蓄时间 -25%,爆头(命中头部判定)+40% |

扮演事件:hunter9_clean_kill(单发/单击击杀满血目标 +12)、hunter9_trap_kill(陷阱击杀 +10)、hunter9_track_success(循足迹找到 ≥200 格外目标 +15)、hunter9_live_off_land(一整日只吃狩猎/采集所得 +8)。

### 序列 8 挑衅者

| 灵性上限 132 | 服药压力 +12 | 主材料 好斗蜥王的舌 |
|---|---|---|

| 能力 | 类型 | 消耗 | 冷却 | 说明 |
|------|------|------|------|------|
| 挑衅 | 主动 | 10 | 15s | 强制 12 格内单体仇恨自己 10s;对玩家=其屏幕出现红色挑衅字样+对你伤害 -10%(激怒惩罚) |
| 群体激怒 | 主动 | 22 | 60s | 8 格内敌对生物互相仇恨概率 +50% 持续 12s |
| 战意沸腾 | 被动 | 0 | — | 每受击一次攻击 +2%(至 +20%),脱战衰减 |
| 言语之刺 | 主动 | 8 | 10s | NPC 对话选项"激将"解锁(部分委托的非战斗解法) |

扮演事件:provoker8_tank_master(挑衅承伤保队友零伤过遭遇 +18)、provoker8_infight(激怒致敌互杀 ≥2 +12)、provoker8_word_win(用激将完成委托 +15)。

### 序列 7 纵火家

| 灵性上限 170 | 服药压力 +16 | 主材料 焰髓石粉 |
|---|---|---|

| 能力 | 类型 | 消耗 | 冷却 | 说明 |
|------|------|------|------|------|
| 灼热镰击 | 主动 | 14 | 4s | 近战火焰弧斩 3 格,点燃 4s |
| 火鸦 | 主动 | 20 | 25s | 召唤 2 只追踪火鸦(可被击落),命中爆燃 |
| 爆燃陷阱 | 主动 | 18 | 20s | 隐形火雷,触发 2.5 格爆燃(不毁方块,可配置) |
| 火焰亲和 I | 被动 | 0 | — | 火伤 -40%,岩浆减速 -50% |

扮演事件:pyro7_chain_ignite(单场点燃 ≥4 目标 +15)、pyro7_controlled_burn(战斗后无非目标建筑损毁 +12)、pyro7_phoenix_moment(HP<20% 时用火焰能力反杀 +20)。

### 序列 6 阴谋家

| 灵性上限 218 | 服药压力 +20 | 主材料 双头蛇的信子(或特性替代) |
|---|---|---|

| 能力 | 类型 | 消耗 | 冷却 | 说明 |
|------|------|------|------|------|
| 战场布局 | 主动 | 30 | 180s | 24×24「猎场」120s:己方陷阱伤害 +50%/触发 +1 格,敌方移动留高亮轨迹 |
| 弱点洞悉 | 被动 | 0 | 20s | 对同一目标第 3 击起 15% 概率 1.5× 弱点伤害+部位提示 |
| 借刀杀人 | 主动 | 25 | 60s | 使两敌互仇 20s(Boss 免疫) |
| 火焰亲和 II | 被动 | 0 | — | 火伤 -60%,火系消耗 -20% |

扮演事件:conspirer6_flawless_ambush(猎场全歼 ≥4 敌无伤 +25)、conspirer6_enemy_infighting(借刀致杀 +15)、conspirer6_patient_stalk(侦察 ≥2min 再动手 +12)。

### 序列 5 收割者

| 灵性上限 272 | 服药压力 +26 | 晋升 魔药+「战火仪式」(燃烧献祭坛,献祭 5 件战利品) |
|---|---|---|

| 能力 | 类型 | 消耗 | 冷却 | 说明 |
|------|------|------|------|------|
| 焚天之镰 | 主动 | 35 | 25s | 火焰巨镰 5 格扇形,对燃烧目标 ×1.6 |
| 收割印记 | 主动 | 20 | 15s | 标记 30s;标记目标死亡返 15 灵性并刷新镰刀冷却(连锁清怪核心) |
| 战争领域(雏形) | 切换 | 10/s | — | 8 格:友方攻速 +15%,敌方火抗 -30%;开启期间压力 +0.5/s |
| 烈焰重生 | 预备 | 60 | 72h | 致死时原地爆燃复活 50% HP,+20 污染 |

扮演事件:reaper5_harvest_chain(印记连锁 ≥5 杀 +30)、reaper5_protect_weak(领域护普通人 NPC 全存活 +20)、reaper5_controlled_burn_II(大量用火零误伤建筑 +15)。

### 序列 4 铁血骑士(半神 · EP2)

- **试炼**:独力守住雾都镇区一次极光会大规模袭击(波次防御战);
- **神话形态「燃铁战像」**:熔岩纹理重甲巨人,肩部烟囱喷焰(蒸汽朋克意象);
- 形态能力:①「军势领域」16 格——友方伤害 +25%/恐惧免疫,敌方持续微燃;②「铁血冲锋」——直线 12 格冲撞,途径敌人击飞+重甲碾压伤害;
- 常态新增:收割印记可叠 3 目标、免疫燃烧、近战附带灵性火;
- 代价:同前;另形态中无法使用任何"阴谋/陷阱"类能力(神话侧写:堂堂之阵)。

## 7.6 偷盗者途径(→ 错误)✅ 序列 9-4 全详设

**定位**:偷盗人心、窃取法术、欺诈规则——从偷东西开始,到偷能力、偷梦、偷"存在"。

### 序列 9 偷盗者

| 灵性上限 108 | 服药压力 +10 | 主材料 影栖鼬的爪 |
|---|---|---|

| 能力 | 类型 | 消耗 | 冷却 | 说明 |
|------|------|------|------|------|
| 妙手空空 | 主动 | 12 | 8s | 3 格内偷取生物掉落表 1 件/玩家非手持快捷栏 1 件(PvP 可配),被发现率与目标警觉相关 |
| 潜影步 | 切换 | 0.8/s | — | 潜行速度 = 走路速度,脚步声消除 |
| 锐眼 | 被动 | 0 | — | 高亮 16 格内容器/上锁物/暗门 |
| 应急脱身 | 主动 | 15 | 30s | 向后翻滚 4 格+0.5s 无敌帧 |

扮演事件:marauder9_first_steal(成功行窃 +10)、marauder9_ghost(整场潜入未触发任何警觉 +15)、marauder9_pick_target(偷取"有价值"标记物品 +12)。

### 序列 8 诈骗师

| 灵性上限 130 | 服药压力 +14 | 主材料 镜面蟹的壳 |
|---|---|---|

| 能力 | 类型 | 消耗 | 冷却 | 说明 |
|------|------|------|------|------|
| 偷天换日 | 主动 | 22 | 90s | 与 8 格内目标位置互换(视线内) |
| 伪造文书 | 长交互 | 20+材料 | 60s | 伪造委托单/通行证/交易券(NPC 鉴定有失败率,失败=通缉 +1) |
| 话术 | 被动 | 0 | — | NPC 交易价格 -15%,谎言检测对自己 -30% |
| 金蝉脱壳 | 被动 | 0 | 120s | 被抓捕判定时 30% 概率留下衣物假身逃脱 |

扮演事件:swindler8_big_con(伪造文书通过一次教会检查 +18)、swindler8_swap_escape(换位逃脱追杀 +15)、swindler8_honest_day(整日不行骗不行窃 +8——"骗子最难的表演是诚实")。

### 序列 7 解密学者

| 灵性上限 158 | 服药压力 +15 | 主材料 古碑苔的孢囊 |
|---|---|---|

| 能力 | 类型 | 消耗 | 冷却 | 说明 |
|------|------|------|------|------|
| 符文解析 | 主动 | 20 | 30s | 解读仪式残迹/封印阵列 → 知识条目碎片 |
| 开锁大师 | 主动 | 15 | 20s | 无钥匙开锁(留「被撬」标记,物主灵视可查) |
| 痕迹抹除 | 主动 | 20 | 60s | 清除 30s 内足迹/气味/灵性残留 |
| 密码直觉 | 被动 | 0 | — | 谜题类结构(失落图书馆等)提示 +1 档 |

扮演事件:cryptologist7_ancient_read(解析遗迹符文获得完整知识 +18)、cryptologist7_perfect_crime(开锁行窃+痕迹抹除全套完成 +20)、cryptologist7_share_secret(将解密所得分享给他人 +10)。

### 序列 6 盗火人

| 灵性上限 198 | 服药压力 +26 | 主材料 窃能水蛭之核(或特性替代) |
|---|---|---|

| 能力 | 类型 | 消耗 | 冷却 | 风险 | 说明 |
|------|------|------|------|------|------|
| 窃取非凡 | 主动 | 45 | 240s | 25% 反噬+15 压力 | 临时窃取目标一个低于自身序列的能力 60s(目标该能力同步进冷却——零和偷窃) |
| 火种收藏 | 被动 | 0 | — | — | 窃取过的能力入「火种册」,再窃同款消耗 -40% |
| 顺手牵羊 II | 被动 | 0 | — | — | 妙手空空升级:可偷 NPC 钥匙类关键道具 |
| 无主之物 | 被动 | 0 | — | — | 拾取他人掉落不触发盗窃标记概率 +50% |

扮演事件:prometheus6_steal_and_win(用窃得能力赢下战斗 +25)、prometheus6_return_stolen(归还贵重赃物 +15)、prometheus6_flame_collection(火种册新录 3 能力 +18)。

### 序列 5 窃梦家

| 灵性上限 246 | 服药压力 +32 | 晋升 魔药+「窃梦仪式」(沉睡生物旁,窃其梦境残片为仪式核心) |
|---|---|---|

| 能力 | 类型 | 消耗 | 冷却 | 说明 |
|------|------|------|------|------|
| 窃梦 | 主动 | 40 | 120s | 抽取沉睡生物「梦境残片」:可食(随机灵性/知识碎片/幻觉)、可作仪式材料、可黑市出售 |
| 潜入梦境 | 主动 | 50 | 600s | 潜入沉睡玩家梦境层行窃(偷梦境信标/演练记录)——与梦境行者天然猫鼠 |
| 现实错位 | 主动 | 35 | 45s | 1.5s 错位:无敌帧+穿 ≤2 格薄墙 |
| 谎言织网 | 被动 | 0 | — | 被占卜/读心 50% 概率反馈伪造信息 |

扮演事件:dreamthief5_grand_heist(组织据点保险库无伤窃取 +30)、dreamthief5_dream_duel_escape(从梦境对决脱逃 +25)、dreamthief5_never_kill_sleeper(窃梦不伤沉睡者 ×5 +15)。

### 序列 4 寄生者(半神 · EP2)

- **试炼**:在不被识破的前提下,以他人身份在雾都生活 3 个游戏日(委托/交易/礼拜全套);
- **神话形态「千影寄主」**:本体半透明,周身环绕多个"面孔剪影";
- 形态能力:①「寄生转移」——被致命一击时转移到 24 格内任一生物身旁并夺取其 30% HP;②「影从」——复制一名敌方玩家的当前装备属性幻影为己战斗 20s;
- 常态新增:窃取非凡可存 2 个能力槽、现实错位可穿 4 格;
- 代价:同前;另每次寄生转移随机遗忘一条知识(可重学)。

## 7.7 学徒途径(→ 门)✅ 序列 9-4 全详设

**定位**:知识、星占与空间。后勤枢纽、配方引擎、全队的"任意门"。

### 序列 9 学徒

| 灵性上限 115 | 服药压力 +9 | 主材料 星辉苔藓 |
|---|---|---|

| 能力 | 类型 | 消耗 | 冷却 | 说明 |
|------|------|------|------|------|
| 快速学习 | 被动 | 0 | — | 阅读手稿/书籍速度 +50%,知识碎片合成需求 -1 |
| 灵性视野 | 切换 | 0.9/s | — | 灵视变体:侧重仪式残迹/知识物品高亮 |
| 稳定灵墨 | 主动 | 10 | 15s | 抄录一页手稿(制作可交易的知识副本,3 次后原本"磨损") |
| 小空间戏法 | 主动 | 8 | 10s | 隔 2 格取放小物品 |

扮演事件:apprentice9_bookworm(单日读满 3 份新手稿 +10)、apprentice9_teach(将知识副本交予他人并被使用 +12)、apprentice9_field_note(在 3 种不同群系记录见闻 +8)。

### 序列 8 戏法大师

| 灵性上限 138 | 服药压力 +12 | 主材料 幻光鸟羽 |
|---|---|---|

| 能力 | 类型 | 消耗 | 冷却 | 说明 |
|------|------|------|------|------|
| 空间戏法 | 主动 | 18 | 15s | 3 格内物品/小实体"戏法"至手中或 6 格内空位 |
| 知识链接 | 主动 | 20 | 180s | 两条已知知识危险组合 → 概率产出新配方线索(失败 +5 压力) |
| 镜面小门 | 主动 | 25 | 40s | 放置一对 1×2 镜门(相距 ≤16 格),10s 内可穿行 |
| 快速学习 II | 被动 | 0 | — | — |

扮演事件:trickmaster8_combo_discovery(知识链接成功 +18)、trickmaster8_door_tactics(镜门完成一次战术迂回击杀/救援 +15)、trickmaster8_show_off(向 ≥2 名观众演示空间戏法 +8)。

### 序列 7 占星人

| 灵性上限 165 | 服药压力 +14 | 主材料 陨星尘 |
|---|---|---|

| 能力 | 类型 | 消耗 | 冷却 | 说明 |
|------|------|------|------|------|
| 星象占卜 | 主动 | 25 | 300s | 夜间露天:未来一日世界事件预告(精度随月相:满月 +1 档/新月 -1 档) |
| 星光庇护 | 主动 | 30 | 120s | 5 格光幕 60s:污染增长 -50%,灵体不可穿越 |
| 星图定位 | 主动 | 15 | 60s | 标记一次占卜结果的大致方位为地图光点 |
| 记录狂热 | 被动 | 0 | — | 自动记录旁观的仪式/配方线索 |

扮演事件:astrologer7_accurate_forecast(星象预告被验证 +18)、astrologer7_ward_save(光幕保护他人渡过灵体袭击 +15)、astrologer7_star_atlas(收集 5 种月相下的观测记录 +12)。

### 序列 6 记录官

| 灵性上限 200 | 服药压力 +18 | 主材料 誊写魔像的指骨(或特性替代) |
|---|---|---|

| 能力 | 类型 | 消耗 | 冷却 | 说明 |
|------|------|------|------|------|
| 卷轴铭刻 | 长交互 | 30+材料 | 60s | 将自身一个主动能力铭刻为单次卷轴(任何人可用,威力 70%)——能力证券化 |
| 完美复录 | 主动 | 25 | 120s | 复制书/地图/手稿(含隐藏信息) |
| 档案宫殿 | 被动 | 0 | — | 手册容量无限+分类检索;阅读再 +50% |
| 空间标记 | 主动 | 15 | 30s | 记录 3 个坐标书签(供序列 5) |

扮演事件:scribe6_scroll_economy(卷轴被他人使用 +15/次,≤3/日)、scribe6_archive_complete(补全任务链全部知识 +25)、scribe6_faithful_record(完整记录他人仪式 +18)。

### 序列 5 旅行家

| 灵性上限 250 | 服药压力 +24 | 晋升 魔药+「启门仪式」(两个空间标记之间,含空间封印物碎片) |
|---|---|---|

| 能力 | 类型 | 消耗 | 冷却 | 说明 |
|------|------|------|------|------|
| 旅行家之门 | 主动 | 20/8格 | 6s | 视线内 ≤12 格闪现,可穿薄墙;连发每次 +5 消耗 |
| 远门 | 引导 8s | 80 | 1800s | 开启至任一空间标记的双向门 20s,队友可行 |
| 灵界一瞥 | 主动 | 15 | 30s | 透视 8 格方块后实体轮廓 3s |
| 门扉守则 | 被动 | 0 | — | 传送类反噬伤害 -80% |

扮演事件:traveler5_rescue_portal(远门救援濒死队友 +30)、traveler5_uncharted(踏足未加载区块 +12/次 ≤2/日)、traveler5_no_shortcut(护送全程不传送 +15)。

### 序列 4 秘法师(半神 · EP2)

- **试炼**:在「陵寝开启」事件遗迹中,90s 内连续穿越 7 道空间乱流封锁;
- **神话形态「星门行者」**:星空质感人形,身后悬浮巨大石质门框;
- 形态能力:①「折跃领域」14 格——友方获得 1 次免费短传送/敌方传送与投射物 30% 被"折歪";②「万象之门」——开启至任意已探索维度锚点的团队门(含灰雾);
- 常态新增:空间标记 6 枚、旅行家之门可带 1 名队友;
- 代价:同前;另每次万象之门后 10min 内无法再入灰雾(空间疲劳)。

## 7.8 首发五途径序列 4 神话形态汇总

| 途径 | 序列 4 | 神话形态 | 领域(12-16 格) | 大招 |
|------|--------|----------|----------------|------|
| 占卜家 | 诡法师 | 雾影操偶主 | — | 万物提线 / 历史回溯 |
| 观众 | 操纵师 | 千面心影 | 心象领域 | 傀心一指 |
| 猎人 | 铁血骑士 | 燃铁战像 | 军势领域 | 铁血冲锋 |
| 偷盗者 | 寄生者 | 千影寄主 | — | 寄生转移 / 影从 |
| 学徒 | 秘法师 | 星门行者 | 折跃领域 | 万象之门 |

通用规则:形态激活 sanity -0.8/s,归零即失控;半神被击杀 100% 掉完整特性并触发「半神陨落回响」;神性余韵 24h(普通人 NPC 恐惧,目击 ≥3 次触发猎巫)。

## 7.9 第二批途径详设(EP2 · 序列 9-5)🔷

> 与首发同规格,序列 4 留至 EP2 末段。以下每途径给出定位、差异化机制与 9-5 全能力表(扮演事件各列 2-3 条代表项,全量入附表)。

### 7.9.1 水手途径(→ 暴君)

**定位**:海洋、风暴与雷霆的战士。**差异化机制——天候共鸣**:雷雨天全能力消耗 -30%/伤害 +25%;沙漠等干旱群系反向惩罚 -20%。船只作为"移动据点"参与玩法。

| 序列 | 名称 | 灵性上限 | 能力(消耗/冷却) |
|------|------|----------|------------------|
| 9 | 水手 | 112 | 稳浪步(被动:船上/水中移动+30%,水下呼吸+60s)/ 怒涛拳(10/3s:近战附水压击退)/ 观云识天(被动:提前 1 游戏时辰预告天气) |
| 8 | 暴怒之民 | 136 | 狂怒(20/60s:15s 攻+25% 受伤+10%,结束后虚弱 5s)/ 水性通达(被动:水下挖掘不减速)/ 浪涌(15/12s:前方 3 格水浪推击) |
| 7 | 航海家 | 172 | 唤风(25/30s:己船加速/敌船减速 30s;陆上=顺风疾跑 10s)/ 海图直觉(被动:海洋结构 64 格罗盘感应)/ 静水领悟(被动:雨中灵性恢复 ×1.5) |
| 6 | 风眷者 | 224 | 风之壁(30/45s:格挡投射物幕墙 8s)/ 乘风(22/20s:三段跳+滑翔)/ 引雷针(35/90s:雷雨天呼落一道真实闪电于标记点) |
| 5 | 海洋歌者 | 280 | 鲸歌(40/120s:16 格海洋生物友好化+可短暂骑乘)/ 风暴之种(50/300s:局部小型雷暴云 30s,范围内持续雷击敌人)/ 潮汐领域雏形(12/s 切换:8 格水场,友方水上行走)/ **晋升「咏潮仪式」**:满月涨潮的海岸线,以风暴中获取的雷击木为核心 |

代表扮演事件:sailor9_storm_sail(雷暴中航行 500 格 +15)、wrath8_last_stand(HP<30% 狂怒反杀 +18)、navigator7_new_route(发现并标记新海洋结构 +15)、windblessed6_thunder_call(引雷击杀 +20)、oceansinger5_whale_ride(骑乘大型海洋生物完成一次跨海 +25)。

### 7.9.2 不眠者途径(→ 黑暗)

**定位**:黑夜猎手与恐惧散播者。**差异化机制——不眠**:主动拒绝睡眠积累「守夜层数」(每夜 +1,上限 5),层数提高夜间全属性;但白日进入「倦怠」(移速 -10%×层数,可用浓咖啡/教会晨祷清除惩罚保留增益)。全数值曲线昼夜反转:夜间为白昼的 1.3-1.6 倍。

| 序列 | 名称 | 灵性上限 | 能力 |
|------|------|----------|------|
| 9 | 不眠者 | 114 | 夜视(切换 0.5/s:真·夜视+黑暗中轮廓强化)/ 惊醒(被动:睡眠中受袭立即满状态起身+1s 无敌)/ 静夜潜行(被动:夜间脚步声 -70%) |
| 8 | 午夜诗人 | 140 | 低吟(18/25s:8 格敌方恐惧值 +2,恐惧满 5 层逃跑)/ 韵律感知(被动:黑暗中"声呐"式轮廓,闭眼可视)/ 安眠诗(15/60s:使目标生物入睡) |
| 7 | 梦魇 | 176 | 噩梦缠身(28/45s:沉睡目标做噩梦——醒后恐惧满层;对玩家=幻象追逐 8s)/ 恐惧收割(被动:击杀恐惧状态目标返 12 灵性)/ 影中疾行(20/15s:阴影间 6 格瞬移) |
| 6 | 安魂师 | 228 | 安魂曲(35/120s:净化 8 格内亡灵系生物,每净化 1 只 +消化)/ 守夜人之火(25/60s:不灭黑焰火堆,夜间庇护圈=教堂庇护效果)/ 恐惧具象(40/90s:将目标最深恐惧投影为幻影兽助战 20s) |
| 5 | 灵巫 | 284 | 沟通亡魂(会客法完整版,见 10.4)/ 灵魂鞭挞(30/8s:无视护甲的灵魂直伤)/ 黑夜领域雏形(12/s:10 格黑暗降临,己方隐身敌方致盲)/ **晋升「守夜仪式」**:连续 3 夜不眠后的第 4 夜零点,于守夜人之火旁完成 |

代表扮演事件:sleepless9_all_night(整夜清醒并击退 ≥3 夜袭 +12)、poet8_fear_rout(恐惧驱逃 ≥3 敌 +15)、nightmare7_terror_hunt(收割恐惧目标 ×5 +18)、requiem6_purify_graveyard(净化整片墓地亡灵 +25)、shaman5_last_words(用会客法替亡者完成遗愿委托 +30)。

### 7.9.3 收尸人途径(→ 死神)

**定位**:亡灵、安魂与灵界摆渡。**差异化机制——会客法**(第 10.4 节)本途径独占完整版:与死亡玩家残留灵体对话、承接"亡者委托"。与不眠者共享黑夜系但方向相反:不眠者散播恐惧,收尸人抚平死亡。

| 序列 | 名称 | 灵性上限 | 能力 |
|------|------|----------|------|
| 9 | 收尸人 | 110 | 死亡视觉(切换 0.7/s:尸体/骸骨/死亡地点高亮,可见"死因残影"3s 回放)/ 妥善安葬(长交互:埋葬生物尸骸 → 小额消化+该区域亡灵刷新率 -20%)/ 尸僵抗性(被动:凋零/中毒 -40%) |
| 8 | 掘墓人 | 134 | 墓土掌握(12/10s:操纵泥土快速挖穴/立小土墙)/ 遗物感应(被动:32 格墓地/遗骸结构感应)/ 亡者之息(被动:亡灵系生物默认中立,除非先攻击) |
| 7 | 通灵者 | 174 | 会客法·初阶(40/日:与 1 具新鲜尸体/残留灵体问 3 个问题)/ 灵体牵引(20/30s:拉拽/固定一只灵体 5s)/ 渡魂(25/60s:超度徘徊灵体 → 消化+偶得"遗愿"支线) |
| 6 | 死灵导师 | 226 | 亡骨仆从(45/180s:以骸骨材料召唤 2 具骷髅仆从,可装备武器)/ 生死界感(被动:队友濒死(HP<15%)全图感应)/ 防腐圣油(制作:尸体保鲜=会客法窗口延长) |
| 5 | 看门人 | 282 | 门扉之钥(50/600s:开启 10s「灵界之门」,放逐一只 ≤序列 6 敌人入灵界 30s/或己方短暂遁入躲避)/ 亡者军列(仆从上限 4+可召唤"英灵残影"精英 1 具 60s)/ 死寂领域雏形(12/s:10 格内敌方治疗无效+亡灵友军强化)/ **晋升「摆渡仪式」**:在渡魂 ≥13 名灵体后,于午夜墓园完成 |

代表扮演事件:corpse9_proper_burial(安葬 ×5 +10)、gravedigger8_tomb_keeper(修缮墓地结构 +12)、spiritualist7_last_message(把亡者遗言带给指定 NPC +20)、necroteacher6_bone_guard(仆从保护普通人 NPC 存活 +18)、gatekeeper5_ferry(放逐强敌保全队 +28)。

### 7.9.4 歌颂者途径(→ 太阳)

**定位**:光明、净化与秩序,团队光环与对污染系特攻。**差异化机制——正午共鸣**:游戏时间 5500-6500 tick(正午前后)全能力免费一次;**公证契约**:序列 6 起可为玩家间交易/委托做"公证",违约方自动受罚(系统层担保,催生玩家间大宗交易)。

| 序列 | 名称 | 灵性上限 | 能力 |
|------|------|----------|------|
| 9 | 歌颂者 | 116 | 颂光术(10/5s:光矢,对亡灵/污染系 ×1.5)/ 光明祷歌(20/60s:8 格友方小治疗+移除失明)/ 晨光感知(被动:日出时全额回满灵性) |
| 8 | 祈光人 | 142 | 圣光印(15/20s:目标烙印 20s,受光系伤 +30%)/ 驱暗(22/45s:6 格净化黑暗类地形效果与隐身)/ 白日行者(被动:白天移速 +10%) |
| 7 | 太阳神官 | 178 | 净化之光(30/90s:移除目标全部负面/对污染生物真伤)/ 光之壁垒(28/60s:5 格光墙 10s)/ 小型圣域(35/180s:圣化 8 格地面 60s,亡灵不可进入) |
| 6 | 公证人 | 230 | 公证契约(20/次:两名玩家交易/委托担保,违约自动 debuff+赔付)/ 誓言之力(被动:履约中的自己全属性 +8%)/ 烈日审判(40/120s:天降光柱 3 格,白天伤害翻倍) |
| 5 | 光之祭司 | 286 | 黎明重现(60/600s:强制局部"日出"30s——夜间变白昼光照,亡灵潮立即退散)/ 恒光领域雏形(12/s:12 格友方持续微恢复+污染增长冻结)/ 圣光洗礼(50/300s:为一名玩家 -20 污染,每人每周一次)/ **晋升「迎日仪式」**:正午,连续公证 7 单无违约后,于教堂圣坛完成 |

代表扮演事件:bard9_dawn_hymn(日出时向 ≥2 玩家演奏/致辞 +10)、lightprayer8_purge_dark(驱散黑暗地形 ×3 +12)、priest7_cleanse_ally(净化队友重负面 +18)、notary6_fair_witness(公证大额交易履约 +20)、lightpriest5_false_dawn(黎明重现扭转一场夜战 +30)。

## 7.10 数据驱动落库(PathwayRegistry + Codec)

```java
package top.aurora.projectmystery.pathway;

public record PathwayDefinition(
        ResourceLocation id, String displayKey, int color, String group,
        List<Integer> implementedSequences,
        float spiritualityBase, float spiritualityPerSeq,
        ResourceLocation actingProfile, ResourceLocation breakdownEntity,
        List<String> sequenceNameKeys) {

    public static final Codec<PathwayDefinition> CODEC = RecordCodecBuilder.create(i -> i.group(
        ResourceLocation.CODEC.fieldOf("id").forGetter(PathwayDefinition::id),
        Codec.STRING.fieldOf("display_name_key").forGetter(PathwayDefinition::displayKey),
        Codec.INT.fieldOf("color").forGetter(PathwayDefinition::color),
        Codec.STRING.fieldOf("group").forGetter(PathwayDefinition::group),
        Codec.INT.listOf().fieldOf("implemented_sequences").forGetter(PathwayDefinition::implementedSequences),
        Codec.FLOAT.fieldOf("spirituality_base").forGetter(PathwayDefinition::spiritualityBase),
        Codec.FLOAT.fieldOf("spirituality_per_sequence").forGetter(PathwayDefinition::spiritualityPerSeq),
        ResourceLocation.CODEC.fieldOf("acting_profile").forGetter(PathwayDefinition::actingProfile),
        ResourceLocation.CODEC.fieldOf("breakdown_entity").forGetter(PathwayDefinition::breakdownEntity),
        Codec.STRING.listOf().fieldOf("sequence_names_keys").forGetter(PathwayDefinition::sequenceNameKeys)
    ).apply(i, PathwayDefinition::new));
}

public class PathwayRegistry {
    private static final Map<ResourceLocation, PathwayDefinition> MAP = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, Map<Integer, SequenceDefinition>> SEQ =
            new ConcurrentHashMap<>();

    public static void register(ResourceLocation id, PathwayDefinition def) { MAP.put(id, def); }
    public static void registerSequence(SequenceDefinition def) {
        SEQ.computeIfAbsent(def.pathway(), k -> new ConcurrentHashMap<>()).put(def.sequence(), def);
    }
    public static void clear() { MAP.clear(); SEQ.clear(); }

    public static float spiritualityMax(ResourceLocation pathway, int sequence) {
        PathwayDefinition d = MAP.get(pathway);
        if (d == null) return 100f;
        float base = d.spiritualityBase() + d.spiritualityPerSeq() * (9 - sequence);
        SequenceDefinition s = sequence(pathway, sequence);
        return s != null ? base + s.spiritualityBonus() : base;
    }

    public static float regenRate(ResourceLocation pathway, int sequence) {
        return switch (sequence) {
            case 9 -> 1.0f; case 8 -> 1.2f; case 7 -> 1.5f;
            case 6 -> 2.0f; case 5 -> 2.5f; default -> 3.0f;
        };
    }

    @Nullable public static SequenceDefinition sequence(ResourceLocation pathway, int seq) {
        Map<Integer, SequenceDefinition> m = SEQ.get(pathway);
        return m == null ? null : m.get(seq);
    }
    public static boolean isImplemented(ResourceLocation pathway, int seq) {
        PathwayDefinition d = MAP.get(pathway);
        return d != null && d.implementedSequences().contains(seq);
    }
    public static Collection<PathwayDefinition> all() { return MAP.values(); }
}
```

---

## 7.11 第三批途径详设(v0.7 新增 · EP3 · 序列 9-5)🔶

> 六条途径升级为详设,与第二批同规格。至此 **15/22 条途径可玩**,覆盖全部七大教会对应途径 + 灾祸/母神双系,玩家开荒可选方向从 9 条扩至 15 条。

### 7.11.1 战士途径(→ 黄昏巨人)

**定位**:正面近战之王、团队护盾、黎明系圣光战士。**差异化机制——荣耀誓约**:玩家可在知识手册立下誓约(如"不偷袭""不遗弃队友""正面迎敌"),持誓期间全属性 +6%;违誓立刻 +20 压力并失去增益 3 日。**黎明共鸣**:游戏时间 23000-1000 tick(黎明前后)能力消耗 -30%。

| 序列 | 名称 | 灵性上限 | 能力(消耗/冷却) |
|------|------|----------|------------------|
| 9 | 战士 | 108 | 武技精通(被动:近战武器伤害 +15%、攻速 +10%)/ 铁壁格挡(8/6s:1.5s 完美格挡,成功反弹 30% 伤害)/ 战场嗅觉(被动:16 格敌意生物数量直觉提示) |
| 8 | 格斗家 | 132 | 连击(被动:连续命中同目标第 3 击必暴击)/ 摔投(15/12s:抓取并摔掷一只 ≤2 格生物,落地眩晕 2s)/ 淬体(被动:摔落伤害 -50%,击退抗性 +40%) |
| 7 | 武器大师 | 170 | 万兵通晓(被动:任意武器均享精通加成,含远程)/ 武装切换(5/2s:快捷栏武器瞬时换装+1s 换装无敌帧)/ 卸武(20/25s:击落目标手持物 5s) |
| 6 | 黎明骑士 | 222 | 黎明之光(25/60s:8 格净化亡灵/污染系,友方小治疗)/ 圣光斩(20/10s:剑刃附光,对黑暗系 ×1.6)/ 无畏(被动:恐惧/黑暗致盲类效果免疫) |
| 5 | 守护者 | 278 | 圣盾守护(35/90s:为一名友方套 8 点吸收盾并转移其 30% 承伤到自己 15s)/ 大地践踏(40/60s:6 格震地击飞+缓速)/ 壁垒领域雏形(12/s:8 格内友方受伤 -15%、击退免疫)/ **晋升「守誓仪式」**:持同一誓约 ≥14 日无违誓,黎明时分于教堂圣坛,以守护他人时破损的甲胄为核心 |

代表扮演事件:warrior9_fair_duel(正面击败等阶敌人不偷袭 +12)、fighter8_barehand(徒手击败武装敌人 +15)、weaponmaster7_versatile(单场战斗使用 ≥3 类武器击杀 +15)、dawnknight6_purge_horde(单场净化 ≥8 只亡灵 +20)、guardian5_shield_save(圣盾使队友免于致死一击 +30)。

### 7.11.2 秘祈人途径(→ 倒吊人)

**定位**:高风险高知识型——倾听不可名状者的低语换取情报与力量,是"污染管理玩法"的极致体现。**差异化机制——倾听层数**:主动开启「倾听」积累层数(每分钟 +1,上限 10),层数提高占卜可信度与灵性回复,但同步提高呓语频率与污染增速;层数 ≥7 时可能被"低语存在"反向注视(随机负面事件池)。本途径污染上限比其他途径高 30%,失控阈值同步放宽——**与污染共舞**。

| 序列 | 名称 | 灵性上限 | 能力 |
|------|------|----------|------|
| 9 | 秘祈人 | 118 | 秘祝(12/20s:低声祷词,自身下一次检定 +10%)/ 危险直觉(被动:高危结构/封印物 24 格心悸预警)/ 邪祀知识(被动:可辨识邪教祭坛与其归属) |
| 8 | 倾听者 | 144 | 倾听(切换:见差异化机制)/ 窃听风声(25/60s:标记一名 NPC/玩家,3 分钟内其"公开行为摘要"以传闻形式送达)/ 静默祷告(15/30s:压力 -3,倾听层数不清空) |
| 7 | 隐修士 | 180 | 隐修庇护(30/120s:自身 20s 内不被灵视/占卜/追踪锁定)/ 低语转嫁(35/90s:将自身 10 点污染暂存入手持容器 10 分钟,超时未净化则双倍返还)/ 苦修(被动:饥饿值 ≤6 时灵性回复 ×1.5) |
| 6 | 蔷薇主教 | 232 | 血肉遁形(45/180s:潜入一只 ≥2 格生物体内 30s,期间无敌不可动,钻出时宿主重伤——对玩家需其 HP<30%)/ 血肉修复(30/60s:牺牲 4 点自身 HP 治疗目标 8 点)/ 荆棘之环(25/45s:6 格荆棘缓速+流血) |
| 5 | 牧羊人 | 288 | 羊群引导(50/240s:魅惑 ≤3 只中立/敌意生物为"羊群"跟随作战 60s)/ 献祭祷告(60/600s:献祭一只羊群成员,全队灵性回满)/ 迷雾领域雏形(12/s:10 格浓雾,己方视野正常敌方致盲)/ **晋升「牧首仪式」**:倾听层数满 10 的状态下,于邪教祭坛遗址完成而不失控 |

代表扮演事件:pryer9_omen_heed(听从危险直觉撤离并证实危险 +12)、listener8_secret_keep(获知秘密 24h 不外泄 +15)、monk7_pollution_juggle(污染 ≥60 状态存活一整日 +18)、bishop6_flesh_hide(血肉遁形躲过一次必死攻击 +22)、shepherd5_flock_war(羊群协战击败精英 +28)。

### 7.11.3 阅读者途径(→ 白塔)

**定位**:知识、推理与"学习复制"——全 Mod 唯一能**模仿他途径能力**的途径,后期百科型全能。**差异化机制——藏书楼**:玩家维护个人"藏书阁"方块群,每收录 1 本原创书籍/日记残页/知识条目,永久 +0.2% 灵性上限(上限 +15%);**推理链**:任务线索在手册中可拖拽连线,连出正确推理链直接跳过任务步骤并获额外消化。

| 序列 | 名称 | 灵性上限 | 能力 |
|------|------|----------|------|
| 9 | 阅读者 | 106 | 过目不忘(被动:阅读过的配方/仪式图永久收录手册)/ 速读(被动:经验/知识类获取 +20%)/ 学识检定(10/10s:对可疑物品做一次"这是什么"鉴定,可信度 B) |
| 8 | 推理学员 | 130 | 推理链(见差异化机制)/ 细节之眼(切换 0.5/s:痕迹高亮——脚印、血迹、翻动过的容器)/ 弱点分析(15/20s:标记目标,对其伤害 +12% 持续 15s) |
| 7 | 侦探 | 168 | 现场重现(35/120s:在死亡/战斗地点回放 10s 灰色残影)/ 伪装识破(被动:无面人/易容类伪装在 8 格内自动显形)/ 心证(20/60s:审讯 NPC 时获得一次"真话判定") |
| 6 | 博学者 | 220 | 能力摹写(50/300s:观察到其他玩家/生物释放能力后,可摹写其 ≤序列 7 能力一次,威力 70%)/ 万象笔记(被动:摹写过的能力入册,每册每日可重放 1 次)/ 语言通晓(被动:所有 NPC 方言/密文可读) |
| 5 | 秘术导师 | 276 | 授业(30/次:向另一玩家"讲授"自己已握知识条目,双方各 +消化)/ 知识风暴(45/90s:8 格敌方随机遗忘 1 个主动技能 10s)/ 智识领域雏形(12/s:10 格内己方冷却 -20%)/ **晋升「立论仪式」**:藏书阁收录 ≥60 册,写就一本原创"论文"书与他人签阅后,于大学讲堂完成 |

代表扮演事件:reader9_bookworm(单日收录 ≥3 册 +10)、student8_deduce(推理链跳过任务步骤 +15)、detective7_cold_case(完成一单侦探事务所悬案 +20)、polymath6_mimic_win(摹写能力击败原持有者 +25)、mentor5_teach(授业 ≥3 名不同玩家 +25)。

### 7.11.4 刺客途径(→ 原初魔女)

**定位**:诅咒、媚惑与影杀,PVP 与单体爆发极致。**差异化机制——痛苦汲取**:自身或 12 格内生物承受伤害时积累「痛楚」资源(独立于灵性),痛楚驱动本途径高阶能力;**魅魔面相**:序列 7 起外观可切换"魅影形态"(NPC 好感交互加成/教会声望减损)。

| 序列 | 名称 | 灵性上限 | 能力 |
|------|------|----------|------|
| 9 | 刺客 | 110 | 背刺(被动:背后攻击 ×1.8)/ 悄声步(切换 0.3/s:潜行不触发绊线/压力板)/ 淬毒(制作:武器附毒 5 击) |
| 8 | 教唆者 | 134 | 挑拨(20/45s:两只敌意生物互相攻击 15s)/ 谎言之舌(被动:NPC 交涉类检定 +15%)/ 心弦拨动(15/30s:目标玩家屏幕心跳音效+准星轻微漂移 8s) |
| 7 | 女巫 | 172 | 诅咒之痕(25/40s:目标受"厄运"——15s 内检定/暴击率恶化)/ 魅影(见差异化机制)/ 影袭(痛楚 20/12s:瞬步至目标背后并附加背刺判定) |
| 6 | 欢愉 | 224 | 极乐之雾(35/90s:6 格敌方陷入恍惚,攻击欲望 -80% 8s)/ 痛楚转移(痛楚 30/60s:将自身当前 30% 已损 HP 转嫁给被诅咒目标)/ 妒火(被动:被 ≥2 敌围攻时闪避 +20%) |
| 5 | 痛苦 | 280 | 苦痛具象(痛楚 60/180s:凝聚"苦痛之刺"投射,无视护甲,伤害随痛楚储量成长)/ 荆冠仪式(50/300s:自伤 30% HP,60s 内全能力零冷却——高风险爆发)/ 哀恸领域雏形(12/s:10 格敌方持续微量真伤+治疗效果减半)/ **晋升「蚀心仪式」**:携满层痛楚,于午夜风暴中的高塔顶端完成 |

代表扮演事件:assassin9_silent_kill(无声击杀不惊动第二目标 +12)、instigator8_civil_war(挑拨致两派 NPC 冲突 +15)、witch7_hex_hunt(诅咒状态下击杀 ×3 +18)、bliss6_pacify(极乐之雾无伤化解围攻 +20)、pain5_thorn_king(苦痛之刺单发 ≥30 伤害 +30)。

### 7.11.5 耕种者途径(→ 母亲)

**定位**:生命、丰饶与自然改造——种田玩法神秘学化,基地建设/后勤核心。**差异化机制——生命亲和**:作物生长速度、动物繁殖率随序列提升获得半径加成(4→16 格);**丰饶税**:每次大规模催熟/创生都会抽取"地力",地力耗尽的区块产出减半 3 日——教玩家轮作与敬畏。

| 序列 | 名称 | 灵性上限 | 能力 |
|------|------|----------|------|
| 9 | 耕种者 | 104 | 绿手指(被动:半径 4 格作物生长 +25%)/ 沃土感知(切换:地力/湿度/光照可视化)/ 育种(制作:两种作物杂交出强化种子) |
| 8 | 医师 | 128 | 草药包扎(12/8s:治疗 6 点+移除流血)/ 诊断(10/10s:读取目标全部状态效果与病因)/ 防疫(被动:中毒/凋零持续时间 -50%) |
| 7 | 丰收祭司 | 166 | 丰饶祝祷(30/300s:半径 12 格作物瞬间推进一个生长期,消耗地力)/ 收获狂欢(25/120s:8 格友方饱食度回满+再生 I 10s)/ 谷灵守护(被动:己方农田免袭击生物践踏) |
| 6 | 生物学家 | 218 | 生命剖析(15/20s:对生物弱点标记,全队对其伤害 +15%)/ 嫁接术(制作:培育"神秘作物"——魔药辅材的可再生来源,见第 40 章)/ 驯化(35/90s:中立生物永久驯服概率检定) |
| 5 | 德鲁伊 | 274 | 荆棘巨藤(40/60s:召唤藤蔓缠绕 6 格敌人 5s)/ 兽群之友(50/240s:召集 8 格内已驯化动物协战 60s)/ 生命领域雏形(12/s:10 格友方持续再生+作物加速)/ **晋升「春祭仪式」**:主持一场 ≥4 名玩家参与的丰收祭(各自献上亲手培育作物),于满月夜巨树下完成 |

代表扮演事件:farmer9_first_harvest(收获自育强化作物 +10)、physician8_field_medic(战斗中治疗队友 ≥5 次 +15)、priest7_feast(以自产食材宴请 ≥3 玩家 +15)、biologist6_new_species(嫁接出新神秘作物 +22)、druid5_forest_ward(兽群击退入侵者保卫村庄 +28)。

### 7.11.6 药师途径(→ 月亮)

**定位**:魔药炼制大师、血族与生物创生——**全服的"药剂供应链"角色**,魔药系统玩法的乘数。**差异化机制——炼金精通**:炼制任意魔药品质检定 +1 档,失败品可回收 50% 材料;**血月共鸣**:血月事件(第 18 章)期间全能力免费,但污染增速 ×2;序列 7 起获得"渴血"负面(每 3 日需摄入一次生物精华,否则灵性回复停滞)。

| 序列 | 名称 | 灵性上限 | 能力 |
|------|------|----------|------|
| 9 | 药师 | 112 | 药理直觉(被动:见 7.11.6 机制;可鉴定魔药品质)/ 复方(制作:两瓶低阶药水合并强化)/ 抗性底子(被动:自饮魔药副作用 -30%) |
| 8 | 驯兽师 | 136 | 兽语(10/15s:读取动物情绪与需求)/ 战宠契约(30/次:驯服生物升格"战宠",可下达攻击/守护指令并佩戴护具)/ 兽群嗅觉(被动:借战宠视野) |
| 7 | 吸血鬼 | 174 | 血之汲取(20/8s:近战偷取 3 HP)/ 蝠翼滑翔(15/20s:短距滑翔+夜间加速)/ 血雾遁(30/60s:化雾位移 6 格,免疫穿越伤害)/ 渴血(负面,见机制) |
| 6 | 魔药教授 | 226 | 催化(30/次:坩埚炼制时间 -50%,本次品质检定再 +1 档)/ 配方逆向(45/日:分析一瓶未知魔药,概率解锁其配方残页)/ 毒理免疫(被动:一切中毒免疫) |
| 5 | 深红学者 | 282 | 血肉造物(60/600s:以生物材料+血液创生一只"深红仆兽"常驻,可自定义部件加点)/ 血契(40/次:与另一玩家立血契,双方共享 10% 伤害与治疗 24h)/ 猩红领域雏形(12/s:10 格敌方吸血弱化己方吸血强化)/ **晋升「血月仪式」**:血月之夜,以自炼至纯品质魔药为核心完成 |

代表扮演事件:apothecary9_first_pure(炼出纯粹品质魔药 +12)、tamer8_pet_save(战宠救主 +15)、vampire7_night_hunt(血月夜狩猎 ×5 +18)、professor6_recipe_break(逆向解锁配方 +22)、scholar5_lifework(深红仆兽存活 ≥7 日 +28)。

### 7.11.7 第三批小结与首发对照

| 途径 | 一句话卖点 | 服务器生态位 |
|------|-----------|-------------|
| 战士 | 誓约驱动的正面无双 | 主坦/前排 |
| 秘祈人 | 与污染共舞的情报贩子 | 高风险情报位 |
| 阅读者 | 能"抄作业"的百科全书 | 万金油/军师 |
| 刺客 | 痛楚经济的影杀爆发 | PVP 刺客位 |
| 耕种者 | 神秘学种田流 | 后勤/基建 |
| 药师 | 全服魔药供应链 | 经济/辅助核心 |

## 7.12 第四批七途径概设(v0.7 新增 · EP4 · 序列 9-7)⬜→🔶

> 剩余七条途径给出 9-7 概设(每序列 2 能力+1 事件),EP4 升级详设。其中母树双途径定位为**"堕落路线"**:能力强度上浮 15%,但扮演事件多为反社会行为(教会声望持续为负、猎巫夜必然成为目标)——给"想当反派"的玩家一条真实可玩的路。

| 途径 | 序列 9 | 序列 8 | 序列 7 | 差异化机制概要 |
|------|--------|--------|--------|----------------|
| 律师(→黑皇帝) | 律师:雄辩(交涉检定+20%)/条款之眼(识破合同陷阱) | 野蛮人:规则破坏(对"结构方块"伤害×3)/狂性(HP 越低攻越高) | 贿赂者:金钱开路(金镑代替声望通过检查)/腐化之言(NPC 短暂倒戈) | **腐化点数**:破坏秩序类行为积累,兑换黑市特权 |
| 仲裁人(→审判者) | 仲裁人:明察(谎言检定)/秩序之言(停止一场 NPC 斗殴) | 治安官:逮捕术(束缚目标 3s)/巡逻直觉(通缉目标 32 格感应) | 审讯者:威压审讯(必得一条真话)/铁面(魅惑/恐惧免疫) | **法典系统**:登记服务器"法条",执法他人违法行为得消化 |
| 罪犯(→深渊) | 罪犯:黑话(黑市价格 -15%)/销赃(赃物洗白) | 折翼天使:堕落之翼(短距滑翔)/罪业感知(见他人"罪值") | 连环杀手:猎杀名单(标记目标,对其全增伤)/无痕(不留脚印血迹) | **罪值经济**:作恶得力量,罪值越高失控越近 |
| 囚犯(→被缚者) | 囚犯:枷锁适应(负重/缓速惩罚减半)/越狱直觉(囚禁类效果时长减半) | 疯子:谵妄之力(压力 ≥60 时反而全属性+15%)/疯言(对敌施加混乱) | 狼人:月下变形(夜间兽形:速度攻击大增,不能用工具)/嗜血回复 | **束缚与释放**:主动给自己上"戒律枷锁"换取爆发窗口 |
| 窥秘人(→隐者) | 窥秘人:窥视(远程观察标记点 10s)/秘纹识别 | 格斗学者:知行合一(每学 1 条知识,徒手伤害+1%)/预判格挡 | 巫师:元素飞弹(三系可选)/法术书(自制卷轴) | **卷轴经济**:能力可封装为一次性卷轴出售 |
| 通识者(→完美者) | 通识者:万物图鉴(鉴定一切原版/Mod 物品)/杂学(随机小增益) | 考古学家:遗迹感应/文物修复(损毁封印物部分复原) | 鉴定师:真伪之眼(识破一切伪造品)/估价(物品市场价浮窗) | **鉴定服务**:全服唯一能鉴定封印物真伪的职业位 |
| 怪物(→命运之轮) | 怪物:厄运缠身(周期性随机小负面)/因祸得福(受负面后下次检定+15%) | 机器:精密计算(弹道/坠落预测线)/情绪关闭(压力增长冻结 60s) | 幸运儿:幸运一日(每日一次检定必成)/灾祸转嫁(将自身负面转给 8 格随机生物) | **命运摆锤**:幸运与厄运交替周期,玩法=在正确的相位做正确的事 |

## 7.13 相邻途径与转换系统(v0.7 新增 · 完整代码)

**世界观规则落地**:同组途径互为"相邻途径"。玩家在**序列 4 或序列 3** 时可执行一次「途径转换」——服用相邻途径同序列魔药不视为失控级错误,成功后获得**杂糅能力**(保留旧途径 2 个已选能力槽)。这是毕业延长与 Build 多样性的核心机制之一:同一角色可以"愚者系三途径大满贯"。

**相邻组注册**(数据包 `pathway_groups/*.json`):源堡[占卜家/学徒/偷盗者]、混沌海[观众/秘祈人/歌颂者/水手/阅读者]、黑夜[不眠者/收尸人/战士]、灾祸[猎人/刺客]、母神[耕种者/药师]、秩序[律师/仲裁人]、母树[罪犯/囚犯]、知识妖[窥秘人/通识者]、命运[怪物](特殊:仅可与"镜像自身"转换=重置能力加点)。

**规则表**:

| 项 | 规则 |
|----|------|
| 触发条件 | 当前序列 ∈ {4,3} 且消化 100%,持有相邻途径同序列魔药 |
| 检定 | 基础成功率 70% + 首席祝福 10% + 仪式加成 ≤15%;失败=+35 污染并保留原途径 |
| 杂糅能力 | 保留旧途径玩家自选 2 个能力(序列 ≤6);新途径能力全解锁 |
| 代价 | 转换后消化清零、7 日"特性紊乱"(随机呓语频率 ×2) |
| 次数 | 每角色一生 2 次(与组内途径数量无关) |
| 神话形态 | 使用新途径形态,但外观混入旧途径元素(GeckoLib 材质变体) |

```java
package top.aurora.projectmystery.pathway.adjacent;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import top.aurora.projectmystery.capability.PlayerMysteryData;
import top.aurora.projectmystery.capability.MysteryCapabilities;
import top.aurora.projectmystery.pathway.PathwayRegistry;
import top.aurora.projectmystery.pathway.PathwayDefinition;
import top.aurora.projectmystery.item.PotionOfSequenceItem;
import top.aurora.projectmystery.network.PMNetwork;
import top.aurora.projectmystery.network.S2CPathwaySwitchFxPacket;

import java.util.*;

/**
 * 相邻途径转换管理器(7.13)。
 * 数据驱动:pathway_groups/*.json 定义组;本类只做规则执行。
 * 入口:PotionOfSequenceItem#finishUsingItem 检测到"相邻途径同序列魔药"时调用 attemptSwitch。
 */
public final class AdjacentPathwayManager {

    public static final int MAX_SWITCHES_PER_LIFE = 2;
    public static final int RETAINED_ABILITY_SLOTS = 2;
    public static final float BASE_SUCCESS = 0.70f;
    public static final float CHAIRMAN_BLESS_BONUS = 0.10f;
    public static final float RITUAL_BONUS_CAP = 0.15f;
    public static final int FAIL_POLLUTION = 35;
    public static final int TURMOIL_DAYS = 7;

    /** groupId -> pathwayIds,数据包重载时由 PathwayGroupLoader 填充 */
    private static final Map<ResourceLocation, List<ResourceLocation>> GROUPS = new HashMap<>();
    private static final Map<ResourceLocation, ResourceLocation> PATHWAY_TO_GROUP = new HashMap<>();

    private AdjacentPathwayManager() {}

    public static void reload(Map<ResourceLocation, List<ResourceLocation>> groups) {
        GROUPS.clear();
        PATHWAY_TO_GROUP.clear();
        GROUPS.putAll(groups);
        groups.forEach((g, list) -> list.forEach(p -> PATHWAY_TO_GROUP.put(p, g)));
    }

    public static boolean areAdjacent(ResourceLocation a, ResourceLocation b) {
        if (a.equals(b)) return false;
        ResourceLocation ga = PATHWAY_TO_GROUP.get(a);
        return ga != null && ga.equals(PATHWAY_TO_GROUP.get(b));
    }

    public static Optional<List<ResourceLocation>> groupOf(ResourceLocation pathway) {
        ResourceLocation g = PATHWAY_TO_GROUP.get(pathway);
        return g == null ? Optional.empty() : Optional.of(GROUPS.get(g));
    }

    /** 供物品/GUI 侧预检:能否发起转换(不消耗任何东西)。 */
    public static SwitchCheck check(ServerPlayer player, ResourceLocation targetPathway, int targetSequence) {
        PlayerMysteryData data = player.getCapability(MysteryCapabilities.MYSTERY_DATA)
                .orElseThrow(() -> new IllegalStateException("Missing mystery data"));

        if (data.getPathway() == null) return SwitchCheck.deny("no_pathway");
        if (!areAdjacent(data.getPathway(), targetPathway)) return SwitchCheck.deny("not_adjacent");
        int seq = data.getSequence();
        if (seq != 4 && seq != 3) return SwitchCheck.deny("sequence_window");        // 仅序列 4/3
        if (targetSequence != seq) return SwitchCheck.deny("potion_sequence_mismatch");
        if (data.getDigestion() < 1.0f) return SwitchCheck.deny("not_digested");
        if (data.getSwitchCount() >= MAX_SWITCHES_PER_LIFE) return SwitchCheck.deny("no_switches_left");
        return SwitchCheck.allow();
    }

    /**
     * 执行转换。ritualBonus 来自仪式状态机(12 章),chairmanBlessed 来自灰雾席位(15 章)。
     * @return true=成功转换
     */
    public static boolean attemptSwitch(ServerPlayer player, ItemStack potion,
                                        ResourceLocation targetPathway,
                                        float ritualBonus, boolean chairmanBlessed) {
        int targetSeq = PotionOfSequenceItem.getSequence(potion);
        SwitchCheck check = check(player, targetPathway, targetSeq);
        if (!check.allowed()) {
            player.sendSystemMessage(Component.translatable("pm.switch.deny." + check.reason()));
            return false;
        }
        PlayerMysteryData data = player.getCapability(MysteryCapabilities.MYSTERY_DATA).orElseThrow();

        float chance = BASE_SUCCESS
                + (chairmanBlessed ? CHAIRMAN_BLESS_BONUS : 0f)
                + Math.min(ritualBonus, RITUAL_BONUS_CAP);
        boolean success = player.getRandom().nextFloat() < chance;

        if (!success) {
            data.addPollution(FAIL_POLLUTION);
            data.markDirtyAndSync(player);
            player.sendSystemMessage(Component.translatable("pm.switch.fail"));
            PMNetwork.sendTo(player, S2CPathwaySwitchFxPacket.failure());
            return false;
        }

        // ---- 成功:保留 2 个旧途径能力(序列<=6),写入 hybridAbilities ----
        ResourceLocation oldPathway = data.getPathway();
        List<ResourceLocation> retained = pickRetainedAbilities(player, data);
        CompoundTag hybrid = new CompoundTag();
        hybrid.putString("old_pathway", oldPathway.toString());
        for (int i = 0; i < retained.size(); i++) hybrid.putString("ability_" + i, retained.get(i).toString());
        data.setHybridTag(hybrid);

        data.setPathway(targetPathway);
        data.setSequence(targetSeq);
        data.setDigestion(0f);                                    // 消化清零
        data.incrementSwitchCount();
        data.applyStatusEffect("pm:trait_turmoil", TURMOIL_DAYS * 24000L); // 7 游戏日紊乱
        data.markDirtyAndSync(player);

        PathwayDefinition def = PathwayRegistry.get(targetPathway);
        player.sendSystemMessage(Component.translatable("pm.switch.success",
                Component.translatable(def.displayKey())));
        PMNetwork.sendTo(player, S2CPathwaySwitchFxPacket.success(oldPathway, targetPathway));
        return true;
    }

    /** 玩家事先在手册 GUI 中勾选保留槽;未勾选则默认取使用频率最高的 2 个低阶能力。 */
    private static List<ResourceLocation> pickRetainedAbilities(ServerPlayer player, PlayerMysteryData data) {
        List<ResourceLocation> chosen = data.getRetainChoices();
        if (chosen.size() == RETAINED_ABILITY_SLOTS) return chosen;
        return data.getAbilityUsageStats().entrySet().stream()
                .filter(e -> data.abilitySequence(e.getKey()) >= 6)   // 序列 9-6 的能力才可保留
                .sorted(Map.Entry.<ResourceLocation, Integer>comparingByValue().reversed())
                .limit(RETAINED_ABILITY_SLOTS)
                .map(Map.Entry::getKey).toList();
    }

    public record SwitchCheck(boolean allowed, String reason) {
        static SwitchCheck allow() { return new SwitchCheck(true, ""); }
        static SwitchCheck deny(String r) { return new SwitchCheck(false, r); }
    }
}
```

**配套改动清单**:`PlayerMysteryData` 增加字段 `switchCount:int`、`hybridTag:CompoundTag`、`retainChoices:List<RL>`、`abilityUsageStats:Map<RL,int>`(能力每次释放 +1,已有网络同步走 5.3 的 dirty 机制);`PotionOfSequenceItem` 在 `finishUsingItem` 分支:同途径→晋升逻辑(8 章),相邻途径→本节,非相邻→失控逻辑(5.5)。GUI:知识手册新增「传承页」勾选保留能力。

---

# 8. 扮演法与消化系统

## 8.1 设计原则

- 扮演法是**唯一**消化来源(不因时间自然增长);
- 每序列 3-5 个事件,覆盖战斗/非战斗/社交/准则四类,保证不同玩法风格都有路径;
- **新颖度衰减**对抗刷取:同事件重复收益按时间线性恢复,同目标重复指数衰减;
- **准则事件**(违反角色准则=惩罚,坚守=奖励)让"扮演"有两个方向的张力;
- 反馈**含蓄**:「你感觉对角色的理解更深了」而非「+18.4 消化」(调试项可开精确值)。

## 8.2 消化公式

```
实际消化 = 基础收益 × 质量系数(0.7-1.2) × 新颖度(0.1-1.0)
         × 风险系数(1.0-1.5, 随当前压力) × 魔药品质系数 × 服务器倍率
```

## 8.3 完整实现

```java
package top.aurora.projectmystery.acting;

public record ActingEventData(
        String id, ResourceLocation pathway, int sequence,
        float baseDigestion, long noveltyDecayTicks,
        int targetCooldownMaxCount, float targetCooldownMultiplier,
        boolean riskBonus, boolean allowLowerSequence, boolean principleEvent) {
    public static final Codec<ActingEventData> CODEC = /* RecordCodecBuilder,字段一一对应 */ null;
}

public class ActingEventHandler {

    public static void trigger(ServerPlayer player, String eventId, @Nullable Entity target) {
        MysteryDataHelper.ifPresent(player, data -> {
            ActingEventData ev = ActingEventRegistry.get(eventId);
            if (ev == null || !ev.pathway().equals(data.getPathway())) return;
            if (ev.sequence() != data.getSequence() && !ev.allowLowerSequence()) return;

            long now = player.serverLevel().getGameTime();
            float novelty = novelty(ev, data.getActingHistory().getOrDefault(eventId, 0L), now);

            if (target != null) {
                String key = eventId + ":" + target.getUUID();
                if (data.getTargetRepeatCount(key) >= ev.targetCooldownMaxCount())
                    novelty *= ev.targetCooldownMultiplier();
                data.bumpTargetRepeat(key, now);
            }

            float risk = ev.riskBonus() ? 1.0f + data.getInsanityPressure() / 200f : 1.0f;
            float quality = PotionQuality.byOrdinal(data.getCurrentPotionQualityOrdinal())
                                         .digestionMultiplier();
            float lowSeqPenalty = (ev.sequence() != data.getSequence()) ? 0.3f : 1.0f;

            float gain = ev.baseDigestion() * novelty * risk * quality * lowSeqPenalty
                       * ServerConfig.DIGESTION_MULT.get().floatValue();

            data.addDigestion(gain);
            data.getActingHistory().put(eventId, now);
            MysteryNetwork.sendDigestHint(player, DigestHintLevel.fromGain(gain));

            if (data.getDigestion() >= 100f)
                MysteryNetwork.sendToast(player, "pm.hint.digestion_complete");
        });
    }

    /** 违反准则:直接惩罚(准则事件的另一面) */
    public static void violatePrinciple(ServerPlayer player, String principleId) {
        MysteryDataHelper.ifPresent(player, data -> {
            data.addInsanityPressure(15f);
            data.addDigestion(-5f);
            MysteryNetwork.sendInsanity(player, InsanityFx.PRINCIPLE_BROKEN, 0.6f);
        });
    }

    private static float novelty(ActingEventData ev, long last, long now) {
        if (last == 0) return 1.0f;
        long elapsed = now - last;
        return elapsed >= ev.noveltyDecayTicks() ? 1.0f
             : Math.max(0.1f, (float) elapsed / ev.noveltyDecayTicks());
    }
}
```

## 8.4 事件触发接线一览(工程速查)

| 事件类别 | Forge 钩子 | 示例 |
|----------|-----------|------|
| 击杀类 | LivingDeathEvent | reaper5_harvest_chain |
| 交互类 | PlayerInteractEvent / 自定义 UI 回调 | scribe6_scroll_economy |
| 状态类 | TickEvent.PlayerTickEvent 条件采样 | faceless6_no_true_face |
| 位移类 | EntityTeleportEvent / ChunkWatchEvent | traveler5_uncharted |
| 社交类 | ServerChatEvent / 交易回调 | notary6_fair_witness |
| 准则类 | 对应能力入口埋点 | seer9_never_self(占卜目标校验) |

---

# 9. 魔药系统(25 配方 + 完整代码)

## 9.1 制作流程

配方知识(可信/可疑/伪造)→ 主材料(稀有材料或同途径同序列特性替代)→ 辅材+容器 → 坩埚投料(顺序+温度+时间)→ 品质结算 → 服用(呓语时刻+稳定性检定)→ 消化期。

## 9.2 首发 25 配方总表(五途径 × 序列 9-5)

> 材料全为 Mod 原创物产;「辅材」通用池:纯净水、月光草、灰烬粉、灵性盐、蒸馏酒精、石楠花瓣、银屑、黑蜡。每配方 = 主材 ×1 + 辅材 ×3-4 + 容器(玻璃瓶/银杯)。

| 途径 | 序列 | 魔药名 | 主材料 | 关键辅材 | 火候 |
|------|------|--------|--------|----------|------|
| 占卜家 | 9 | 占卜家魔药 | 夜蚀花种子 | 月光草+灵性盐 | 文火 60s |
| 占卜家 | 8 | 小丑魔药 | 深灰灵体之泪 | 石楠+酒精 | 文火 90s |
| 占卜家 | 7 | 魔术师魔药 | 幻形蛇毒腺 | 灰烬粉+银屑 | 武火 45s |
| 占卜家 | 6 | 无面人魔药 | 变形怪之血 | 银屑+黑蜡 | 交替 120s |
| 占卜家 | 5 | 秘偶大师魔药 | 提线魔藤之芯 | 黑蜡+灵性盐×2 | 文火 180s |
| 观众 | 9 | 观众魔药 | 冥想菇孢子粉 | 纯净水+月光草 | 文火 60s |
| 观众 | 8 | 读心者魔药 | 灰烬人偶丝线 | 灵性盐+石楠 | 文火 75s |
| 观众 | 7 | 心理医生魔药 | 安眠花蜜蜡 | 酒精+月光草 | 文火 100s |
| 观众 | 6 | 催眠师魔药 | 摇篮蛾复眼 | 黑蜡+石楠 | 交替 110s |
| 观众 | 5 | 梦境行者魔药 | 睡巨人的睫毛 | 月光草×2+银屑 | 文火 200s |
| 猎人 | 9 | 猎人魔药 | 红睛巨狼心脏 | 灰烬粉+酒精 | 武火 40s |
| 猎人 | 8 | 挑衅者魔药 | 好斗蜥王之舌 | 灰烬粉+灵性盐 | 武火 55s |
| 猎人 | 7 | 纵火家魔药 | 焰髓石粉 | 灰烬粉×2+银屑 | 武火 70s |
| 猎人 | 6 | 阴谋家魔药 | 双头蛇信子 | 黑蜡+酒精 | 交替 100s |
| 猎人 | 5 | 收割者魔药 | 战场铁蔷薇 | 灰烬粉×2+黑蜡 | 武火 150s |
| 偷盗者 | 9 | 偷盗者魔药 | 影栖鼬之爪 | 黑蜡+酒精 | 文火 50s |
| 偷盗者 | 8 | 诈骗师魔药 | 镜面蟹之壳 | 银屑+石楠 | 文火 80s |
| 偷盗者 | 7 | 解密学者魔药 | 古碑苔孢囊 | 灵性盐+纯净水 | 文火 95s |
| 偷盗者 | 6 | 盗火人魔药 | 窃能水蛭之核 | 灰烬粉+黑蜡 | 交替 115s |
| 偷盗者 | 5 | 窃梦家魔药 | 梦境残片×3 | 月光草+黑蜡 | 文火 190s |
| 学徒 | 9 | 学徒魔药 | 星辉苔藓 | 纯净水+灵性盐 | 文火 55s |
| 学徒 | 8 | 戏法大师魔药 | 幻光鸟羽 | 银屑+月光草 | 文火 85s |
| 学徒 | 7 | 占星人魔药 | 陨星尘 | 银屑×2+纯净水 | 文火 105s |
| 学徒 | 6 | 记录官魔药 | 誊写魔像指骨 | 灵性盐+黑蜡 | 交替 125s |
| 学徒 | 5 | 旅行家魔药 | 空间裂隙结晶 | 银屑×2+灵性盐 | 文火 210s |

**火候**:文火=普通火焰热源;武火=岩浆/灵魂火;交替=酿造中途需切换热源一次(方块下方热源变更检测)——手感玩法。

## 9.3 品质与稳定性

| 品质 | 消化倍率 | 初始压力 | 稳定性检定 |
|------|----------|----------|-----------|
| 完美 | ×1.2 | 基础值 -5 | 低难度 |
| 完整 | ×1.0 | 基础值 | 中 |
| 瑕疵 | ×0.7 | 基础值 +10 | 高 |
| 污染 | ×0.4 | 基础值 +25 | 极高 |
| 失效 | ×0 | 0 | —(纯浪费) |

**稳定性检定**(服药后):`成功率 = 92% - 品质惩罚 - 当前污染×0.2% + 教堂内服用 +5%`;失败=压力再 +20 并进入 60s「灵性紊乱」(能力消耗 +50%)。

## 9.4 坩埚完整实现

```java
package top.aurora.projectmystery.potion;

public class CrucibleBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_OUTPUT = 0;
    private final ItemStackHandler inventory = new ItemStackHandler(7);
    private final LazyOptional<IItemHandler> invOpt = LazyOptional.of(() -> inventory);

    private final List<ItemStack> addedOrder = new ArrayList<>();
    private HeatType currentHeat = HeatType.NONE;
    private HeatType previousHeat = HeatType.NONE;
    private boolean heatSwitched = false;               // 「交替火候」检测
    private int brewingTime = 0;
    private CrucibleState state = CrucibleState.IDLE;

    public enum CrucibleState { IDLE, BREWING, READY, FAILED }
    public enum HeatType { NONE, GENTLE, FIERCE }       // 文火/武火

    public CrucibleBlockEntity(BlockPos pos, BlockState bs) {
        super(ModBlockEntities.CRUCIBLE.get(), pos, bs);
    }

    /** 玩家右键投料入口(由 CrucibleBlock#use 调用) */
    public InteractionResult addIngredient(Player player, ItemStack held) {
        if (state == CrucibleState.READY || state == CrucibleState.FAILED)
            return InteractionResult.PASS;               // 先取出成品
        if (addedOrder.size() >= 6) return InteractionResult.FAIL;

        ItemStack one = held.split(1);
        addedOrder.add(one);
        if (state == CrucibleState.IDLE) { state = CrucibleState.BREWING; brewingTime = 0; }
        level.playSound(null, worldPosition, ModSounds.CRUCIBLE_PLOP.get(),
                SoundSource.BLOCKS, 0.7f, 0.9f + level.random.nextFloat() * 0.2f);
        setChanged();
        return InteractionResult.CONSUME;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState bs, CrucibleBlockEntity be) {
        if (be.state != CrucibleState.BREWING) return;
        be.brewingTime++;

        HeatType heat = detectHeat(level, pos.below());
        if (heat != be.currentHeat) {
            if (be.currentHeat != HeatType.NONE && heat != HeatType.NONE) be.heatSwitched = true;
            be.previousHeat = be.currentHeat;
            be.currentHeat = heat;
        }
        if (heat == HeatType.NONE) { be.brewingTime--; return; }   // 无热源暂停

        if (be.brewingTime % 20 == 0) be.emitBrewParticles(level, pos);

        PotionRecipe recipe = PotionRecipeRegistry.findBestMatch(be.addedOrder);
        int required = recipe != null ? recipe.brewingTimeTicks() : 1200;
        if (be.brewingTime >= required) be.resolve(recipe);
        be.setChanged();
    }

    private void resolve(@Nullable PotionRecipe recipe) {
        if (recipe == null) { fail(); return; }
        MainIngredientResult main = recipe.resolveMainIngredient(addedOrder);
        if (main == MainIngredientResult.MISSING) { fail(); return; }

        PotionQuality quality = QualityCalculator.calc(recipe, addedOrder,
                currentHeat, heatSwitched, main);
        inventory.setStackInSlot(SLOT_OUTPUT, PotionItemFactory.create(recipe, quality));
        state = CrucibleState.READY;
        addedOrder.clear(); heatSwitched = false;
        level.playSound(null, worldPosition, ModSounds.CRUCIBLE_DONE.get(),
                SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    private void fail() {
        inventory.setStackInSlot(SLOT_OUTPUT, new ItemStack(ModItems.CONTAMINATED_MIXTURE.get()));
        state = CrucibleState.FAILED;
        addedOrder.clear();
        PollutionCloud.spawn((ServerLevel) level, worldPosition, 3, 5f, 100);  // 半径3,+5/s,5s
    }

    private static HeatType detectHeat(Level level, BlockPos below) {
        BlockState bs = level.getBlockState(below);
        if (bs.is(BlockTags.FIRE) || bs.is(Blocks.CAMPFIRE)) return HeatType.GENTLE;
        if (bs.is(Blocks.LAVA) || bs.is(Blocks.SOUL_FIRE) || bs.is(Blocks.SOUL_CAMPFIRE))
            return HeatType.FIERCE;
        return HeatType.NONE;
    }

    private void emitBrewParticles(Level level, BlockPos pos) {
        PotionRecipe r = PotionRecipeRegistry.findBestMatch(addedOrder);
        int color = r != null ? QualityCalculator.previewColor(r, addedOrder, currentHeat)
                              : 0x5A5A5A;                 // 灰=未知配方(玩家可读的"火候"反馈)
        ((ServerLevel) level).sendParticles(new DustParticleOptions(
                Vec3.fromRGB24(color).toVector3f(), 1.0f),
                pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 4, 0.2, 0.1, 0.2, 0.0);
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == ForgeCapabilities.ITEM_HANDLER ? invOpt.cast() : super.getCapability(cap, side);
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", inventory.serializeNBT());
        ListTag order = new ListTag();
        addedOrder.forEach(s -> order.add(s.save(new CompoundTag())));
        tag.put("Order", order);
        tag.putInt("BrewTime", brewingTime);
        tag.putString("State", state.name());
        tag.putBoolean("HeatSwitched", heatSwitched);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        inventory.deserializeNBT(tag.getCompound("Inventory"));
        addedOrder.clear();
        tag.getList("Order", Tag.TAG_COMPOUND)
           .forEach(t -> addedOrder.add(ItemStack.of((CompoundTag) t)));
        brewingTime = tag.getInt("BrewTime");
        state = CrucibleState.valueOf(tag.getString("State"));
        heatSwitched = tag.getBoolean("HeatSwitched");
    }
}
```

## 9.5 服药与呓语时刻

```java
public class BeyonderPotionItem extends Item {
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity user) {
        if (level.isClientSide() || !(user instanceof ServerPlayer player)) return stack;
        PotionRecipe recipe = PotionItemFactory.recipeOf(stack);
        PotionQuality quality = PotionItemFactory.qualityOf(stack);

        MysteryDataHelper.ifPresent(player, data -> {
            // 途径/序列校验:错途径服药 = 剧毒;跳序列 = 必失控
            AdvanceCheck check = AdvanceCheck.evaluate(data, recipe);
            switch (check) {
                case WRONG_PATHWAY -> { data.addPollution(40); player.hurt(
                        player.damageSources().magic(), 12f); return; }
                case SKIP_SEQUENCE -> { InsanityEventHandler.triggerBreakdown(player, data); return; }
                case NOT_DIGESTED -> { MysteryNetwork.sendToast(player, "pm.hint.not_digested"); return; }
                case NEED_RITUAL -> { MysteryNetwork.sendToast(player, "pm.hint.need_ritual"); return; }
                case OK -> { }
            }
            WhisperMoment.begin(player, data, recipe, quality);    // 3-8s 呓语 → 回调结算
        });
        return ItemStack.EMPTY;
    }
}

public class WhisperMoment {
    /** 冻结移动、下发呓语字幕,结束后回调 StabilityCheck */
    public static void begin(ServerPlayer player, PlayerMysteryData data,
                             PotionRecipe recipe, PotionQuality quality) {
        int ticks = 60 + player.getRandom().nextInt(100);
        player.addEffect(new MobEffectInstance(ModEffects.WHISPER_TRANCE.get(), ticks, 0, false, false));
        List<String> pool = WhisperRegistry.pick(recipe.pathway(), data, 3);
        MysteryNetwork.sendWhispers(player, pool, ticks);
        TickScheduler.schedule(player.serverLevel(), ticks, () ->
                StabilityCheck.resolve(player, data, recipe, quality));
    }
}
```

---

## 9.6 魔药配方扩充(v0.7:26-60 号 · 覆盖第二/三批途径序列 9-5)

> 命名规则同 9.2;主材=非凡特性或指定神秘材料(第 40 章图鉴),辅材 2-4 种;序列 5 需配套仪式。所有材料获取途径必须至少 2 条(掉落/结构/交易/种植),避免单点卡死。

| # | 配方 | 序列 | 主材 | 辅材 | 获取渠道示例 |
|---|------|------|------|------|-------------|
| 26 | 水手 | 9 | 深海鱼脊髓 | 海盐结晶、风干海藻、朗姆基液 | 渔获稀有掉落/港口黑市 |
| 27 | 暴怒之民 | 8 | 溺亡者残念(灵体掉落) | 风暴海水、鲨齿粉、辣根 | 雷暴海域灵体/沉船结构 |
| 28 | 航海家 | 7 | 罗盘鸟眼(新增生物) | 磁石粉、六分仪碎片、清泉 | 海岛巢穴/侦探所悬赏 |
| 29 | 风眷者 | 6 | 风之精随(风暴事件掉落) | 雷击木芯、白羽、云母粉 | 风暴之种事件/高山结构 |
| 30 | 海洋歌者 | 5 | 鲸落龙涎 | 月光珍珠、深渊水苔、歌石 | 鲸类稀有掉落/海底神殿 |
| 31 | 不眠者 | 9 | 夜枭视络 | 黑咖啡浓缩、暗影蘑菇、烛灰 | 夜间猎杀/教会配给 |
| 32 | 午夜诗人 | 8 | 梦呓结晶(睡眠村民附近夜采) | 鸦羽、墨囊、安神草 | 村庄夜间/墓园结构 |
| 33 | 梦魇 | 7 | 噩梦马鬃(梦魇兽) | 梦境残片、黑曜石粉、罂粟提取 | 梦魇兽/梦境副本 |
| 34 | 安魂师 | 6 | 安眠花蕊(神秘作物) | 圣水、灵薄雾滴、白烛芯 | 耕种者培育/教堂花园 |
| 35 | 灵巫 | 5 | 缚灵之牙 | 灵界苔、银粉、祖灵香 | 灵体精英掉落/摆渡人交易 |
| 36 | 收尸人 | 9 | 陈年墓土精粹 | 防腐油、白骨粉、裹尸布纤维 | 墓园结构/侦探所委托 |
| 37 | 掘墓人 | 8 | 亡者遗愿残响 | 铁锹锈末、柏木片、黑土 | 渡魂玩法产出 |
| 38 | 通灵者 | 7 | 灵媒之瞳(灵体 Boss) | 通灵烛、镜面银、灰雾凝露 | 灵潮事件/灰雾市场 |
| 39 | 死灵导师 | 6 | 完整古骸(考古) | 缚魂锁链环、暗影精华、尸香花 | 遗迹发掘/黑市 |
| 40 | 看门人 | 5 | 门扉残响(灵界之门事件) | 界碑石屑、摆渡船木、寂静之铃 | 灵界裂隙事件 |
| 41 | 歌颂者 | 9 | 晨光凝露 | 圣咏乐谱残页、金盏花、蜂蜜 | 日出高山采集/教会 |
| 42 | 祈光人 | 8 | 圣光萤心 | 白蜡、镀金粉、澄净水 | 光萤群(新增生物)/圣物租借任务 |
| 43 | 太阳神官 | 7 | 无暗结晶 | 日轮花、圣油、火绒 | 正午沙漠结构/教会声望兑换 |
| 44 | 公证人 | 6 | 誓约金印坯 | 契约羊皮纸、火漆、金粉 | 公证玩法产出/银行 |
| 45 | 光之祭司 | 5 | 恒昼之心 | 圣徒遗骨粉、七色棱镜、颂歌之火 | 光明大教堂副本 |
| 46 | 战士 | 9 | 斗兽腺体 | 铁屑、烈酒、牛皮胶 | 竞技场/猎人交易 |
| 47 | 格斗家 | 8 | 拳师指骨 | 绷带、镁粉、蛇胆 | 地下拳场结构 |
| 48 | 武器大师 | 7 | 百兵通灵铁 | 淬火油、各系武器碎片×3、砥石粉 | 武器献祭仪式产出 |
| 49 | 黎明骑士 | 6 | 黎明圣徽残片 | 圣水、雄鹰羽、朝露 | 教会任务线/猎巫夜守卫奖励 |
| 50 | 守护者 | 5 | 不破之盾核 | 圣骑士甲片、誓约金印、龙血草 | 守誓玩法+副本 |
| 51 | 秘祈人 | 9 | 低语苔孢子 | 蜡封耳塞、祷文纸灰、井水 | 邪教祭坛遗址 |
| 52 | 倾听者 | 8 | 回声螺 | 银线、鼓膜草、静谧粉尘 | 海蚀洞结构/灰雾市场 |
| 53 | 隐修士 | 7 | 苦修者念珠芯 | 粗麻纤维、断食者唾液结晶、岩盐 | 隐修院结构 |
| 54 | 蔷薇主教 | 6 | 血肉蔷薇球茎(神秘作物) | 鲜血精粹、荆棘、麝香 | 培育/极乐教团据点 |
| 55 | 牧羊人 | 5 | 群心之铃 | 羔羊毛、迷雾凝露、牧杖木屑 | 迷雾牧场事件 |
| 56 | 阅读者 | 9 | 智慧之烛泪 | 书页灰、墨水、薄荷 | 大学图书馆/日记残页兑换 |
| 57 | 推理学员 | 8 | 逻辑丝线 | 放大镜片粉、烟斗灰、咖啡因结晶 | 侦探所声望兑换 |
| 58 | 侦探 | 7 | 真相之眼水晶 | 指纹粉、密文纸、银盐 | 悬案任务链奖励 |
| 59 | 博学者 | 6 | 万识脑髓(知识妖遗落) | 记忆水银、七种书籍灰烬、星图纸 | 知识妖遭遇事件 |
| 60 | 秘术导师 | 5 | 立论之核(原创论文书成书时凝结) | 讲坛木屑、学位绶带丝、贤者之灰 | 立论玩法产出 |

> 刺客(61-65)、耕种者(66-70)、药师(71-75)配方入附表 `docs/recipes_ep3.csv`(同上格式,主材依次为:影蜥腺/挑拨者之舌/女巫指甲/极乐花蜜/苦痛之刺核;沃土之心/医者仁心草/丰饶麦穗王/嵌合胚芽/德鲁伊橡实;药鼎残渣精粹/兽王毛髓/血族之牙/教授手稿灰/深红之血)。**60+15=75 配方为 v0.7 总账**。

## 9.7 配方获取的"知识经济"闭环(v0.7 强化)

1. **配方本身是道具**:`recipe_scroll` 物品,右键学习后进手册;可交易、可在灰雾拍卖、可被"偷盗者"窃取(未学习状态下)。
2. **真伪系统**:黑市流通配方 30% 为伪造(材料表有 1-2 处错误→炼出即失败+污染)。**公证人(歌颂者 6)与鉴定师(通识者 7)可鉴伪**——把世界观里"配方流通靠公证"的设定做成职业生态位。
3. **残页拼合**:高阶配方(序列 ≤6)拆成 2-3 张残页散布在不同渠道(结构宝箱/任务/拍卖),集齐拼合才可学习,拼合时可选公证防伪。
4. **首学奖励**:全服第一个学会某配方的玩家获成就+报纸头条(42 章)+灰雾声望,鼓励探索竞速。

---

# 10. 灵视、灵体世界与会客法

## 10.1 灵视渲染(客户端完整)

```java
@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID, value = Dist.CLIENT)
public class SpiritVisionRenderer {

    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Post<?, ?> event) {
        ClientMysteryData data = ClientMysteryData.get();
        if (!data.spiritVisionActive()) return;
        SpiritAura aura = data.auraOf(event.getEntity().getId());   // 全部来自 S2C 包
        if (aura == null) return;
        OutlineRenderer.draw(event.getPoseStack(), event.getMultiBufferSource(),
                event.getEntity(), aura.color(), aura.opacity());
        if (data.canSeeEmotionThreads())
            ThreadRenderer.emotionJitter(event.getPoseStack(),
                    event.getMultiBufferSource(), event.getEntity(), aura.jitter());
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        ClientMysteryData data = ClientMysteryData.get();
        if (!data.spiritVisionActive()) return;
        for (Vec3 dir : data.characteristicDirections())            // 金色特性指向线(仅方向)
            GoldThreadRenderer.draw(event.getPoseStack(), dir);
        for (BlockPos pos : data.ritualResidues())                  // 仪式残迹涟漪
            ResidueRenderer.ripple(event.getPoseStack(), pos, event.getPartialTick());
    }
}
```

**颜色编码**:绿=友好/黄=中立/红=敌意/紫=受污染异常/白高亮=高危(扭曲呈现,不给数据)/灰半透=灵体(仅灵视可见)/金线=特性方向。服务端每 10 tick 扫 32 格 AABB,差量同步;灵视关闭即零开销。

## 10.2 灵体生物

| 灵体 | 生成 | 行为 | 掉落 |
|------|------|------|------|
| 游荡灵体 | 夜间/污染区 | 缓慢漂移,接触 +压力,不主动攻击 | 灵性盐 |
| 怨念残影 | 死亡地点 | 主动攻击,免疫物理(需灵性攻击/银器) | 破碎特性(低概率) |
| 徘徊者 | 墓地/战场遗址 | 中立,可被渡魂/会客 | 遗愿支线 |
| 梦魇残片 | 梦境层 | 入侵者,击败得梦境残片 | 梦境残片 |
| 灵界水母 | 灵界潮汐事件 | 发光漂浮,触手麻痹 | 灵性凝胶 |

**灵体规则**:普通武器 25% 效率;附灵性武器(灵性盐+银锭合成附魔"通灵")全额;所有灵体在教堂庇护区/星光庇护内不可入。

## 10.3 灵性墙与庇护区

教堂结构自带 12 格半径庇护区(方块实体"圣徽"驱动):灵体不可入、压力增长 -50%、灵性恢复 ×1.5、猎巫夜临时豁免(进门=投案自守,出门恢复)。玩家可用「祝圣仪式」为自宅制作小型圣徽(6 格,材料含教会声望兑换的圣水)。

## 10.4 会客法(通灵对话系统)

**定义**:与新鲜尸体残留信息/徘徊灵体建立灵界会话。收尸人途径完整版,占卜家序列 7+ 可用降级版(问题数减半,失败率翻倍)。

**机制**:
- 目标:死亡 <1 游戏日的生物尸骸标记 / 徘徊者灵体 / **死亡玩家的残留灵体**(死亡点驻留 10min,本人下线也有效);
- 会话 UI:烛光对话框,可问 3 个问题(初阶)/5 个(灵巫);问题从**语义槽位**选取:「你是谁」「你怎么死的」「凶手/凶物是什么样子」「你的遗物在哪」「你有什么未了之愿」——回答由死亡记录数据生成(死因、击杀者轮廓、掉落物位置),真实但**残缺**(随机缺词,死亡越久缺越多);
- 死亡玩家残留灵体:允许死者本人以灵体视角打 1 条 ≤50 字符聊天(多人服名场面:"快捡我的包!在岩浆边!");
- 风险:每次会客 +8 压力;对"死于失控"的目标会客,20% 概率被反噬(+20 压力+幻象);
- 「遗愿」支线:徘徊者的未了之愿生成微型委托(送信/寻物/复仇),完成=渡魂+消化+偶得遗物。

```java
public class SeanceSession {
    public static Optional<SeanceSession> open(ServerPlayer medium, DeathRecord record) {
        PlayerMysteryData data = MysteryDataHelper.get(medium);
        int questions = data.getPathway().equals(Pathways.CORPSE_COLLECTOR)
                ? (data.getSequence() <= 5 ? 5 : 3)
                : (data.getSequence() <= 7 ? 1 : 0);   // 占卜家降级版
        if (questions == 0 || !data.trySpendSpirituality(40f)) return Optional.empty();
        data.addInsanityPressure(8f);
        return Optional.of(new SeanceSession(medium.getUUID(), record, questions));
    }

    public Component ask(SeanceQuestion q) {
        float decay = record.ageDecay();                // 死亡越久信息越残缺
        return SeanceAnswerBuilder.build(record, q, decay, random);
    }
}
```

---

# 11. 占卜系统(七法 + 完整代码)

## 11.1 七种占卜法

| 占卜法 | 解锁 | 消耗 | 冷却 | 擅长 | 特点与风险 |
|--------|------|------|------|------|-----------|
| 简易占卜(是/否) | 占卜家 9 | 15 | 60s | 通用 | 连续追问同一问题精度递减 |
| 灵摆占卜 | 占卜家 9+灵摆 | 10 | 30s | 方位/距离 | 持摆手不能持武器 |
| 纸牌占卜 | 占卜家 8 | 25 | 300s | 事件类型 | 抽 3 张暗示未来事件;误读=心理暗示 debuff |
| **灵数占卜**(新) | 占卜家 8 | 12 | 45s | 数量/时间 | 只回答数字类问题("还有几只?""几点来?"),答案 ±20% 浮动 |
| **纸人问事**(新) | 占卜家 7+替罪纸人 | 30+纸人 | 180s | 危险试探 | 派纸人替身走向指定坐标,纸人"死亡方式"回传=前方危险类型;纸人消耗品 |
| 梦境占卜 | 观众 5 / 占卜家 7 | 40+睡眠 | 一夜一次 | 剧情线索 | 血月夜使用可能被「注视」 |
| 星象占卜 | 学徒 7 | 25 | 300s | 世界事件 | 夜间露天;月相影响精度 |

## 11.2 可信度分级(玩家可见的唯一"精度"表达)

| 可信度 | 文案示例(原创) | 底层含义 |
|--------|----------------|----------|
| 清晰 | 「烛火笔直,答案确凿。」 | 真实结果 |
| 尚可 | 「烛影微晃,大致如此。」 | 真实,细节 ±扰动 |
| 模糊 | 「雾气弥漫,难以分辨。」 | 50% 真实/50% 泛化 |
| 混乱 | 「烛火疯狂摇曳——有什么在干扰。」 | 结果随机或反向;若为高位干扰则必反向 |

## 11.3 完整实现

```java
package top.aurora.projectmystery.divination;

public class DivinationSystem {

    public static DivinationResult perform(ServerPlayer player, DivinationType type,
                                           DivinationQuestion question) {
        PlayerMysteryData data = MysteryDataHelper.get(player);
        DivinationMethod method = DivinationMethodRegistry.get(type);
        if (!method.canUse(player, data))
            return DivinationResult.rejected("pm.div.cannot_use");
        if (!data.trySpendSpirituality(method.cost()))
            return DivinationResult.rejected("pm.div.no_spirituality");

        // 准则校验:占卜"与神有关"或"自身命运" = 违反准则
        if (question.touchesDivine() || question.aboutSelfFate()) {
            ActingEventHandler.violatePrinciple(player, "seer_principle_no_divine");
            return DivinationResult.backfired();
        }

        ServerLevel level = player.serverLevel();
        float base = method.baseClearness(data.getSequence(), data.getSpirituality());
        float interference = interference(level, player, question);
        float score = base - interference + (float) level.random.nextGaussian() * 0.1f;

        // 重复追问衰减
        int repeats = DivinationHistory.count(player, question.hash(), level.getGameTime());
        score -= repeats * 0.12f;

        ConfidenceLevel conf = ConfidenceLevel.fromScore(score);
        Object truth = method.computeTruth(question, level);
        Object shown = Distorter.distort(truth, conf, level.random);

        if (conf == ConfidenceLevel.CHAOTIC && level.random.nextFloat() < 0.3f)
            data.addInsanityPressure(10f);              // 强行窥探的反噬
        DivinationHistory.record(player, question.hash(), level.getGameTime());
        return new DivinationResult(conf, shown, method.presentationKey());
    }

    private static float interference(ServerLevel level, ServerPlayer player,
                                      DivinationQuestion q) {
        float v = 0f;
        if (WorldEventScheduler.isActive(level, WorldEvents.EVIL_GAZE)) v += 0.35f;
        if (GrayFog.isInside(player)) v -= 0.25f;                       // 灰雾中占卜增益
        if (q.targetPlayer() != null) {
            PlayerMysteryData t = MysteryDataHelper.get(q.targetPlayer());
            if (AbilityRegistry.hasPassive(t, Abilities.WEB_OF_LIES)) v += 0.30f;  // 谎言织网
            if (t.getSequence() >= 0 && t.getSequence() <= 5) v += 0.20f;          // 高序列难测
        }
        v += MoonPhaseModifier.forType(level, q.type());
        return v;
    }
}
```

## 11.4 纸人问事(危险试探小系统)

派出 `PaperProxyEntity`(纸人实体)沿寻路走向目标坐标(≤64 格);它会真实触发陷阱/激怒生物/踩入污染场;死亡时向占卜者回传死亡摘要(「纸人在途中被火焰吞噬」「纸人抵达后被某种巨大的东西撕碎」——按 DamageSource 映射的原创文案池)。抵达存活则回传"路径安全"并原地化灰。

---

# 12. 仪式魔法与尊名呼名(完整代码)

## 12.1 仪式解剖

一场仪式 = **场地**(多方块阵列:粉笔阵纹+蜡烛+祭坛核心)+ **材料**(投入阵中)+ **环境**(时间/月相/天气/维度/高度)+ **主持者**(序列门槛)+ **咏唱**(呼名交互)。

## 12.2 尊名呼名系统(v0.6 新增核心)

呼唤一位存在需要**三句式尊名**(原创拼装文法):`[称谓句] + [权柄句] + [象征句]`。例如愚者的尊名在数据包中登记为三个 lang key 的组合;玩家从知识条目/日记残页中分别收集三句,**集齐才能正确呼名**。

- 呼名正确:仪式进入正常 RESOLVING;
- 呼名残缺(缺 1 句):完成度 -30%;
- 呼名错误(拼错组合/呼了不该呼的):**灾变直通**——被呼错的存在"看了过来"(高阶恶灵+被注视标记);
- **玩家尊名**:塔罗席位玩家可在灰雾登记自己的三句式尊名(从预置词库拼装,防脏话);其他玩家在现实中以其尊名举行祈求仪式 → 该玩家收到弹窗可跨维度回应(第 15 章)。

## 12.3 仪式状态机(完整)

```java
package top.aurora.projectmystery.ritual;

public class RitualInstance {
    public enum Phase { IDLE, ASSEMBLED, PRIMED, INVOKING, RESOLVING, FAILED, CANCELLED }

    private final RitualDefinition def;
    private final BlockPos center;
    private final UUID leader;
    private final Set<UUID> participants = new HashSet<>();
    private Phase phase = Phase.IDLE;
    private float completion = 0f;
    private int invokeTicks = 0;
    private boolean potionDrunk = false;                 // 晋升仪式:时机窗口
    private HonorificNameCheck nameCheck = HonorificNameCheck.PENDING;

    public void tick(ServerLevel level) {
        switch (phase) {
            case ASSEMBLED -> {
                if (!MultiBlockDetector.stillValid(level, center, def.pattern()))
                    { cancel(level, "pm.ritual.structure_broken"); return; }
                if (MaterialTracker.allPresent(level, center, def.materials())
                        && EnvironmentCheck.pass(level, center, def.environment()))
                    phase = Phase.PRIMED;
            }
            case PRIMED -> { /* 等待主持者点燃核心蜡烛(交互推进) */ }
            case INVOKING -> {
                invokeTicks++;
                completion = baseCompletion(level);
                RitualFx.channel(level, center, completion, invokeTicks);

                // 晋升仪式:呼名后第 100-160 tick 为"饮药窗口"
                if (def.isAdvancement() && !potionDrunk
                        && invokeTicks > 160) { resolveFail(level, 0.4f); return; }
                if (invokeTicks >= def.invokeDuration()) phase = Phase.RESOLVING;
            }
            case RESOLVING -> resolve(level);
            default -> { }
        }
    }

    /** 主持者提交呼名(客户端拼装三句 → C2S 校验) */
    public void submitHonorificName(ServerLevel level, List<ResourceLocation> lines) {
        nameCheck = HonorificRegistry.check(def.invokeTarget(), lines);
        switch (nameCheck) {
            case CORRECT -> { phase = Phase.INVOKING; invokeTicks = 0; }
            case PARTIAL -> { phase = Phase.INVOKING; invokeTicks = 0; completion -= 0.30f; }
            case WRONG -> catastrophe(level, true);      // 呼错名 = 直通灾变
        }
    }

    /** 晋升仪式中的饮药回调(BeyonderPotionItem 在 INVOKING 阶段转发到这里) */
    public boolean notifyPotionDrunk(ServerPlayer player, int qualityOrdinal) {
        if (phase != Phase.INVOKING || invokeTicks < 100 || invokeTicks > 160) return false;
        potionDrunk = true; return true;
    }

    private float baseCompletion(ServerLevel level) {
        float c = 0.6f;
        c += MaterialTracker.qualityBonus(level, center, def.materials());        // ≤0.15
        c += EnvironmentCheck.bonus(level, center, def.environment());            // ≤0.10
        c += Math.min(0.20f, (participants.size() - 1) * 0.05f);                  // D3:多人加成
        if (WorldEventScheduler.isActive(level, WorldEvents.RITUAL_RESONANCE)) c += 0.10f;
        if (nameCheck == HonorificNameCheck.PARTIAL) c -= 0.30f;
        return Math.min(1.0f, c);
    }

    private void resolve(ServerLevel level) {
        float roll = completion + (level.random.nextFloat() - 0.5f) * 0.1f;
        if (roll >= 0.95f)      def.results().grantPerfect(level, center, participants);
        else if (roll >= 0.80f) def.results().grantSuccess(level, center, participants);
        else if (roll >= 0.50f) resolveFail(level, roll);
        else if (roll >= 0.20f) severeFail(level);
        else                    catastrophe(level, false);
        MaterialTracker.consume(level, center, def.materials());
        phase = roll >= 0.80f ? Phase.IDLE : Phase.FAILED;
    }

    private void resolveFail(ServerLevel level, float roll) {
        PollutionCloud.spawn(level, center, 3, 4f, 100);
        participants.forEach(u -> withPlayer(level, u, p ->
                MysteryDataHelper.get(p).addInsanityPressure(10f)));
    }
    private void severeFail(ServerLevel level) {
        level.addFreshEntity(ModEntities.LESSER_WRAITH.get().create(level));
        participants.forEach(u -> withPlayer(level, u, p ->
                MysteryDataHelper.get(p).addPollution(15f)));
    }
    private void catastrophe(ServerLevel level, boolean wrongName) {
        RitualFx.explode(level, center);
        level.addFreshEntity(ModEntities.GREATER_WRAITH.get().create(level));
        participants.forEach(u -> withPlayer(level, u, p -> {
            PlayerMysteryData d = MysteryDataHelper.get(p);
            d.addPollution(25f);
            GazeMark.apply(p, 72 * 60 * 20);             // 被注视 72 游戏时
        }));
        if (wrongName) WorldEventScheduler.forceSchedule(level, WorldEvents.EVIL_GAZE);
        phase = Phase.FAILED;
    }

    public void cancel(ServerLevel level, String reasonKey) {
        MaterialTracker.refund(level, center, def.materials());
        phase = Phase.CANCELLED;
    }
}
```

**工程约束**:结构检测事件驱动(方块放置/破坏触发局部重检+结构哈希缓存),禁止每 tick 全量;区块卸载 → cancel 归还材料(防复制)。

## 12.4 常用仪式清单(MVP+EP1,18 种)

| 仪式 | 序列门槛 | 环境 | 用途 |
|------|----------|------|------|
| 净化仪式 | 9 | 白天 | 污染 -25(每周期上限) |
| 镇魂仪式 | 9 | 夜晚 | 区域灵体清退 |
| 封印仪式(3/2级) | 8 | — | 封印物制作/再封印 |
| 灰雾仪式 | 7 | 新月/满月 | 进入灰雾 |
| 祝圣仪式 | 7+教会声望 | 白天 | 自宅小型圣徽 |
| 破碎特性净化 | 7 | 满月 | 3 破碎 → 1 完整(0.7 纯度) |
| 祈求仪式 | 7 | 烛阵 | 呼名祈求存在/塔罗玩家 |
| 提线/入梦/战火/窃梦/启门仪式 | 6→5 | 各途径专属 | 五途径序列 5 晋升 |
| 咏潮/守夜/摆渡/迎日仪式 | 6→5 | 各途径专属 | EP2 四途径序列 5 晋升 |
| 特性炼入仪式 | 6 | 雷雨 | 特性 → 封印物强化 |
| 招灵仪式 | 6 | 午夜 | 强制召来徘徊者会客 |
| 大型净化仪式 | 5 | 正午+教堂 | 团队污染 -15 |

---

# 13. 封印物系统(40 件 · 分级管控)

## 13.1 危险分级与管控映射

| 等级 | 定义 | 机制映射 |
|------|------|----------|
| 3 级 | 负面轻微可控 | 自由持有;组织登记免审查 |
| 2 级 | 显著负面需管控 | 值夜者巡逻灵视发现未登记 → 审查任务 |
| 1 级 | 准神话级,强同化 | 每日同化检定;教会获知位置 → 回收行动事件 |
| 0 级 | 真神级 | 不可持有;仅剧情实体/事件(EP3+) |

## 13.2 封印物总表(40 件)

**3 级(12 件)**:永燃火柴盒 / 幸运骨片 / 低语石球 / 归零罗盘 / 游荡者地图 / 反诅之铃 / 无声风衣 / 不湿之伞(雨雪不沾,代价:晴天缓慢积攒静电自伤)/ 醒神鼻烟壶(清除昏睡,代价:+3 压力/次)/ 诚实之秤(鉴定物品真伪,代价:对持有者的谎言也会"响")/ 引路烛(指向最近教堂,代价:同时暴露自己位置给该教堂)/ 拾遗手套(3 格磁吸掉落物,代价:偶尔"多捡"不该捡的东西)

**2 级(16 件)**:无面怀表 / 替罪纸人 / 静默铃铛 / 泣血匕首 / 窃听者之耳 / 倒吊人的绳索 / 凝视之眼 / 精神锚点 / 暗影披风 / 回响号角 / 泣血羽毛笔 / 灰雾之门(怀表)/ 傀儡丝手套 / 饕餮锅 / 星图罗盘 / 沉默之匣

**1 级(8 件)**:哭泣铜镜 / 记忆窃取镜 / 蚀骨之链 / 死亡之书 / 虚空棋盘 / 悲鸣提琴 / 借命怀炉 / 门扉残页

**0 级(4 件,事件实体)**:不作为物品存在——以「灾祸具象」形式作为 EP3 世界事件核心:沉没王座(海域事件)/ 无面剧场(维度裂隙)/ 熄灯钟(全服黑暗事件)/ 白骨门扉(灵界通道)。

## 13.3 教会圣物(v0.6 新增 8 件 · 租借制)

高声望(TRUSTED+)可向教会**租借**圣物,租期 3 游戏日,**逾期触发追索**(教会精英小队上门+声望暴跌):

| 圣物 | 教会 | 效果 | 租金 |
|------|------|------|------|
| 黑夜圣徽 | 黑夜女神 | 随身 6 格移动庇护区 | 2 金镑/期 |
| 风暴之哨 | 风暴之主 | 呼唤一次真实雷击 | 3 金镑 |
| 齿轮心脏 | 蒸汽机械 | 红石/机械装置效率 ×2 光环 | 2 金镑 |
| 不灭晨曦烛 | 永恒烈阳 | 夜间携带=正午光照判定 | 3 金镑 |
| 智慧之匣 | 知识智慧 | 存 1 条知识,可无损转让 | 2 金镑 |
| 丰饶之种 | 大地母神 | 作物即时成熟 3×3,每日 3 次 | 2 金镑 |
| 断锋 | 战神 | 武器耐久不减 72h | 3 金镑 |
| (轮换圣物) | 随赛季 | 服务器活动位 | — |

## 13.4 封印物 JSON(schema 同 v0.5,新增 rental 字段)

```json
{
  "id": "project_mystery:wailing_violin",
  "grade": 1,
  "effect": { "type": "project_mystery:mass_weep", "radius": 24, "duration_ticks": 400 },
  "cost": { "type": "project_mystery:forget_knowledge", "count": 1 },
  "assimilation": { "daily_check": true, "whisper_pool": "project_mystery:violin_whispers" },
  "seal_method": { "type": "project_mystery:ritual_seal", "ritual_id": "project_mystery:seven_string_silence" },
  "rental": null
}
```

## 13.5 特性炼入与 1 级制作

封印仪式放入特性 → 效果 ×(1+纯度×0.5),危险 +1,同化检定 ×2。**1 级封印物唯一制作途径** = 序列 ≤6 特性为核心炼制(EP1 末解锁)。

---

## 13.6 封印物扩充(v0.7:41-72 号 · 32 件新增)

> 全部为原创设计的封印物(致敬"编号管控"体裁,不复用原作具体封印物设定)。分级沿用 13.1;每件 = 主动收益 + 常驻代价 + 管控要求,**收益与代价必须同数量级**。列出代表 16 件,其余 16 件同格式入 `data/pm/relics/*.json`。

| 编号 | 名称 | 级别 | 收益 | 代价 | 获取 |
|------|------|------|------|------|------|
| 2-081 | 逆走怀表 | 2 | 每日一次:回溯自身 5s 前位置与 HP | 持有者每日随机 1 小时"时感错乱"(UI 时间乱码) | 钟表匠遗宅结构 |
| 2-082 | 无弦提琴 | 2 | 演奏:16 格敌方缓速 40% 10s | 每次演奏后 24h 内说话字幕随机变成琴谱符号(禁言 debuff) | 剧院地下室(42 章) |
| 2-083 | 饕餮餐叉 | 2 | 食物饱食度效果 ×2 | 每 3 日必须进食一次"活物"否则饥饿锁 6 | 极乐教团据点 |
| 2-084 | 亡者名录 | 2 | 记录 32 格内死亡事件与死因 | 每记录 10 条,书中随机浮现持有者名字一次(压力 +8) | 收尸人任务线 |
| 2-085 | 缄默怀刃 | 2 | 攻击不产生任何声音与仇恨扩散 | 持有者自身脚步声对灵体 ×3 放大 | 黑市拍卖 |
| 1-021 | 千面之镜 | 1 | 照镜复制 8 格内一名玩家外观 30 分钟 | 每次使用后 5% 概率"镜中人"事件(敌对分身) | 无面人系任务链 |
| 1-022 | 风暴之锚 | 1 | 掷出:锚点 12 格强制雷暴 60s | 持有期间永久湿身(着火免疫但雷击必中) | 沉没旗舰副本 |
| 1-023 | 白噪音烛台 | 1 | 点燃:8 格内一切占卜/窥视/倾听无效 | 烛火照明范围内玩家无法听到任何环境预警音 | 隐修院密室 |
| 1-024 | 悔罪者铁鞋 | 1 | 移速 -15%,但每走 1000 格自动 -1 污染 | 无法脱下直至污染归零 | 教会苦修奖励 |
| 1-025 | 博弈骰盅 | 1 | 每日 3 次:任意检定改为掷骰(1-100 均匀分布) | 每次掷出 ≤10,随机资产(物品/货币)消失一件 | 赌场结构 |
| 1-026 | 摆渡人船票 | 1 | 死亡时 50% 概率原地灵体复活(保留背包) | 生效后 7 日内会客法对自己永远"占线" | 灵界裂隙 |
| 0-006 | 缚日晷影 | 0 | 强制世界时间冻结正午 300s(全服) | 使用者此后每个夜晚必做噩梦(睡眠不再跳夜) | 神孽巢穴 Boss |
| 0-007 | 群山之息 | 0 | 60s 内免疫一切物理伤害 | 结束后石化 30s(可被攻击) | 巨人陨落遗迹 |
| 0-008 | 缪斯断笔 | 0 | 书写一条"预言",7 日内高概率自我实现(事件调度器加权) | 每条预言同时生成一条镜像"厄言"作用于持有者 | 作家沙龙任务链终点 |
| 0-009 | 门扉钥匙坯 | 0 | 在任意门框上开启一次 10s 双向传送门(绑定曾到访点) | 每次使用,随机一扇"别的门"也被打开(入侵事件) | 星门行者试炼 |
| 0-010 | 静默王冠 | 0 | 佩戴:领域类能力范围 +50% | 全程无法使用聊天/表情/告示牌(真·缄默) | 神位战争(41 章)首赛季奖励 |

**管控玩法升级**:0 级封印物全服同时激活数 ≤3(服务器配置);激活第 4 件时触发全服广播事件「封印共鸣」——所有激活中封印物代价 ×2,直到有玩家封存一件。让顶级封印物成为**服务器政治问题**而非个人收藏。

# 14. 罗塞尔日记与知识手册(v0.6 新增)

## 14.1 罗塞尔日记残页

世界观设定:一位数百年前的传奇皇帝留下过加密日记,残页散落世界——这是本 Mod 的**顶级知识来源+收集玩法+叙事载体**。

- **形态**:36 页(MVP 12 / EP1 +12 / EP2 +12),每页为独立物品,内容为**原创撰写**的日记体短文(吐槽+线索混排的风格化文本,不摘抄任何原作文字);
- **加密**:拾取时显示乱码,需「解密」——占卜家纸牌占卜/解密学者符文解析/知识智慧教会付费翻译(3 苏勒/页)三选一;
- **价值**:每页含 1 条硬信息:某高阶配方的主材料线索/某仪式环境要求/某尊名三句之一/某 1 级封印物弱点/某结构坐标线索;
- **收集奖励**:每集齐 6 页解锁手册隐藏章节+1 个成就;36 页全收集 = 「皇帝的注视」称号+专属灰雾席位装饰;
- **流通**:残页可交易,灰雾拍卖热门标的;完美复录(记录官)可复制但副本标记「抄本」(不计收集进度——保护收集价值)。

## 14.2 知识手册(Codex)

- 玩家的核心 UI(默认 G 键):途径志 / 材料志 / 仪式志 / 封印物志 / 生物志 / 日记集 / 传闻(22 途径占位灰字);
- 条目三态:**未知**(不显示)/ **传闻**(灰字模糊描述+信息来源)/ **已验证**(全数据);
- 验证途径:亲身实践 / 灵视观察 / 占卜 / 组织购买 / 日记解密;错误知识(伪造手稿习得)在手册中与真知识**外观一致**——直到被实践证伪(标红划线,+1 成就"吃一堑");
- 记录官的档案宫殿被动 = 手册全文检索+无限容量。

## 14.3 知识数据结构

```java
public record KnowledgeEntry(
        ResourceLocation id, KnowledgeCategory category,
        String titleKey, String rumorKey, String verifiedKey,
        List<ResourceLocation> revealsRecipes,
        List<ResourceLocation> revealsRituals,
        @Nullable ResourceLocation honorificLine,       // 尊名三句之一
        int blackMarketValuePennies) {}
```

---

## 14.4 亵渎石板残片系统(v0.7 新增)

**定位**:比罗塞尔日记更高一级的"世界真相"收集线,也是**序列 3 之路的钥匙**(41 章)。全服共 **7 块石板、每块 3 残片、共 21 残片**,分别对应七组相邻途径的"造物奥秘"。

| 石板 | 主题(原创描述) | 残片来源 | 集齐效果 |
|------|----------------|----------|----------|
| I 雾之板 | 源堡组的欺诈与隐喻 | 灰雾首席赛季奖励/诡秘侍者幻影事件/0 级封印物副产 | 该组途径序列 4→3 试炼解锁 |
| II 潮之板 | 混沌海组的全知悖论 | 深海神殿 Boss/风暴之种稀有/公证 100 单里程碑 | 同上 |
| III 夜之板 | 黑夜组的恐惧与安眠 | 猎巫夜完美防守/灵界裂隙深层/守夜 30 层 | 同上 |
| IV 灾之板 | 灾祸组的阴阳两面 | 神孽巢穴/苦痛试炼/战争主教幻影 | 同上 |
| V 生之板 | 母神组的创生代价 | 血月 Boss/巨树之心/嵌合体图鉴全收集 | 同上 |
| VI 序之板 | 秩序组的律法与腐化 | 法典系统里程碑/黑市大亨/审判庭遗址 | 同上 |
| VII 智之板 | 知识妖+命运组的记录与摆锤 | 藏书阁 200 册/命运摆锤满周期/知识皇帝幻影 | 同上 |

规则:残片不可交易(灵魂绑定获取者),但**可在灰雾"共研"**——3 名各持 1 残片的玩家可共同解读,三人都算集齐(鼓励结社)。每块石板解读后进入手册"真相"页,呈现一段原创的、克制的世界观揭示文本(禁止摘抄原作)。

## 14.5 罗塞尔日记扩充(36 → 72 页)与文案规范

新增 36 页按 12 主题 ×3 页组织:蒸汽议会见闻/大学轶事/远航日志/猎巫夜亲历/灰雾初探/封印物事故记录/教会外交/黑市浮世绘/半神目击/占卜失灵之日/最后的晚餐(未完线索)/致未来的读者。每页给出:①一段 60-120 字原创日记体正文(诙谐+局部吐槽+夹带一条真知识);②1 条可解锁的手册知识;③1 条误导信息(30% 页面含假线索,由公证/推理链甄别)。

**示例(原创文案,直接入 lang 文件)**:

> `pm.diary.p41`:"今天蒸汽议会又为路灯该用煤气还是奥术光辉吵了四个钟头。我提议两个都装,他们居然鼓掌了。备忘:给'那位夜里的朋友'的供品,黑麦面包比蜂蜜蛋糕有效——别问我怎么知道的。"(真知识:黑夜系仪式供品修正 +5%)

> `pm.diary.p57`:"占星人说我命宫里有一扇门。我说那敢情好,冬天记得关上。他没笑。占星人从来不笑,这可能才是他们最大的非凡特性。"(假线索页:所附"星门坐标"指向一处陷阱结构)

---

# 15. 灰雾与塔罗会(完整代码)

## 15.1 空间与进入

- 独立小维度 `project_mystery:gray_fog`:无限灰雾平面之上的青铜与石砌巨殿,长桌+22 席高背椅+穹顶星图;
- 进入:序列 ≤7 且知晓「灰雾仪式」,新月/满月之夜举行(EP1 后席位玩家可随时应召);肉身留在原地进入**假寐**(可被攻击!进灰雾前要找安全屋——风险设计);
- 会期:现实每 7 游戏日一届「塔罗聚会」,届时全体席位玩家收到邀请弹窗。

## 15.2 匿名机制

灰雾内一切玩家皮肤替换为**裹雾人影**(途径色轮廓),名牌=塔罗称号,聊天走独立频道(签名混淆);声音/皮肤/披风全部屏蔽。**破译他人身份只能靠"说话内容与行为习惯"**——多人服社交推理玩法的地基。

## 15.3 席位与权力(D5 落地)

- 22 席对应大阿卡纳;入座条件:序列 ≤7 + 首席(或无首席时系统)批准;
- **全席位**均可被祈求仪式呼名(玩家尊名注册后);首席额外获得:拍卖 10% 抽成、优先接单权、踢席发起权(需 2/3 投票);
- 首席产生:席位玩家匿名投票,每现实周一届;
- 席位可被挑战:决斗(灰雾竞技场,不掉落)或投票弹劾。

## 15.4 交易市场(完整代码)

```java
package top.aurora.projectmystery.grayfog;

/** 挂单市场:全服 SavedData(overworld 持久化),支持一口价与拍卖 */
public class GrayFogTradeMarket extends SavedData {
    private static final String KEY = "pm_grayfog_market";
    private final Map<UUID, MarketListing> listings = new LinkedHashMap<>();

    public record MarketListing(UUID listingId, UUID sellerId, String sellerTarotTitle,
                                ItemStack stack, long priceOrStartBid, boolean auction,
                                long auctionEndGameTime, @Nullable UUID highestBidder,
                                long highestBid) {}

    public static GrayFogTradeMarket get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                GrayFogTradeMarket::load, GrayFogTradeMarket::new, KEY);
    }

    public UUID list(ServerPlayer seller, ItemStack stack, long price, boolean auction, long durationTicks) {
        PlayerMysteryData d = MysteryDataHelper.get(seller);
        UUID id = UUID.randomUUID();
        listings.put(id, new MarketListing(id, seller.getUUID(), d.getTarotTitle(),
                stack.copy(), price, auction,
                seller.serverLevel().getGameTime() + durationTicks, null, 0));
        setDirty();
        return id;
    }

    public TransactionResult buyNow(ServerPlayer buyer, UUID listingId) {
        MarketListing l = listings.get(listingId);
        if (l == null || l.auction()) return TransactionResult.NOT_FOUND;
        PlayerMysteryData bd = MysteryDataHelper.get(buyer);
        long fee = l.priceOrStartBid() / 10;                       // D5:首席抽成 10%
        if (!bd.trySpendPennies(l.priceOrStartBid())) return TransactionResult.NO_FUNDS;

        EscrowService.deliver(buyer, l.stack());                   // 匿名邮包投递
        EscrowService.payout(l.sellerId(), l.priceOrStartBid() - fee);
        EscrowService.payout(TarotSeatManager.chiefUUID(), fee);
        listings.remove(listingId); setDirty();
        return TransactionResult.OK;
    }

    public TransactionResult bid(ServerPlayer bidder, UUID listingId, long amount) {
        MarketListing l = listings.get(listingId);
        if (l == null || !l.auction()) return TransactionResult.NOT_FOUND;
        if (amount <= l.highestBid() || amount < l.priceOrStartBid()) return TransactionResult.BID_TOO_LOW;
        if (!MysteryDataHelper.get(bidder).trySpendPennies(amount)) return TransactionResult.NO_FUNDS;

        if (l.highestBidder() != null) EscrowService.payout(l.highestBidder(), l.highestBid()); // 退还前高价
        listings.put(listingId, new MarketListing(l.listingId(), l.sellerId(), l.sellerTarotTitle(),
                l.stack(), l.priceOrStartBid(), true, l.auctionEndGameTime(),
                bidder.getUUID(), amount));
        setDirty();
        return TransactionResult.OK;
    }

    /** 每分钟由调度器扫尾拍卖 */
    public void settleExpired(MinecraftServer server, long now) {
        Iterator<MarketListing> it = listings.values().iterator();
        while (it.hasNext()) {
            MarketListing l = it.next();
            if (!l.auction() || now < l.auctionEndGameTime()) continue;
            if (l.highestBidder() != null) {
                long fee = l.highestBid() / 10;
                EscrowService.deliver(l.highestBidder(), l.stack());
                EscrowService.payout(l.sellerId(), l.highestBid() - fee);
                EscrowService.payout(TarotSeatManager.chiefUUID(), fee);
            } else EscrowService.returnItem(l.sellerId(), l.stack());
            it.remove(); setDirty();
        }
    }

    @Override public CompoundTag save(CompoundTag tag) { /* listing 列表序列化,略常规 */ return tag; }
    public static GrayFogTradeMarket load(CompoundTag tag) { /* 反序列化 */ return new GrayFogTradeMarket(); }
}
```

**离线保障**:EscrowService 把物品/货币写入玩家离线邮箱(SavedData),上线弹「灰雾邮包」领取——挂单玩家无需在线。

## 15.5 祈求与回应

现实中玩家以某席位的注册尊名举行祈求仪式(第 12 章)→ 该席位玩家收到弹窗(内容+悬赏)→ 可选择回应:跨维度投送 1 个物品/1 条消息/1 次远程占卜。回应成功双方各 +少量消化(「扮演神秘存在」与「虔信者」的双向扮演)。祈求悬赏进托管,回应后自动结算。

---

# 16. 七大教会与隐秘组织

## 16.1 七大正统教会

| 教会 | 神祇 | 对应途径组 | 玩家服务 | 特色审查 |
|------|------|-----------|----------|----------|
| 黑夜教会 | 黑夜女神 | 不眠者/收尸人 | 安眠祝福、压力疏解、值夜者任务线 | 缉捕滥用恐惧者 |
| 风暴教会 | 风暴之主 | 水手/歌颂者(部分) | 航海祝福、雷雨预报 | 严打海上劫掠 |
| 蒸汽与机械教会 | 蒸汽机械之神 | 战士/仲裁人(部分) | 机械图纸、红石祝福 | 管制危险造物 |
| 永恒烈阳教会 | 永恒烈阳 | 歌颂者 | 净化、驱邪、正午祝福 | 零容忍亡灵系 |
| 知识与智慧教会 | 知识智慧之神 | 阅读者/学徒(部分) | 手稿翻译、知识鉴定、图书馆 | 查缴禁书 |
| 大地母神教会 | 大地母神 | 耕种者/药师 | 作物祝福、治疗 | 反对亵渎自然 |
| 战神教会 | 战神 | 战士/猎人(部分) | 武器祝福、佣兵委托 | 决斗合法化登记 |

**声望五档**:敌对(-100~-50 猎杀)/警惕(-49~-1 拒入)/中立(0-99)/信任(100-299 内部委托+圣物租借)/核心(300+ 高阶知识+圣物优先)。跨教会声望联动:烈阳与黑夜互不加分,机械与母神轻微互斥——**信仰选边**是长线身份玩法。

## 16.2 值夜者任务线(MVP 主线,黑夜教会)

招募试炼(灵性测试+一次协同缉捕)→ 外围(巡夜/护送/收缴 3 级封印物 ×6 委托)→ 正式(小队编制,2 级封印物押运、失控体处置)→ 资深(参与猎巫夜指挥、1 级封印物回收行动)。津贴:周薪 6 苏勒起,按阶晋升。**卧底玩法**:极光会玩家可假意受招,双阵营声望互为暗面——被识破(占卜/读心/行为暴露)= 全服通缉事件。

## 16.3 隐秘组织(6+)

| 组织 | 性质 | 玩家交互 |
|------|------|----------|
| 极光会 | 邪教,狂信 | 敌对阵营可加入:献祭玩法、禁忌知识黑市、教会通缉 |
| 心理炼金会 | 精神操控研究 | 中立商人:压力治疗、精神系手稿、读心伦理支线 |
| 玫瑰学派 | 血肉与欲望仪式 | 敌对 NPC 势力:据点结构+Boss 前哨 |
| 秘密教团 | 古老隐修 | 隐藏声望:日记残页高价收购方 |
| 摩西密码学会 | 密码与档案 | 解密服务/伪造鉴定,解密学者友好 |
| 拾荒者行会 | 灰色中介 | 赃物销售、悬赏中介、地下情报 |

## 16.4 猎巫夜(世界事件联动)

触发:区域失控 ≥3 次/月 或 半神目击 ≥3 次。持续 3 游戏日:教会巡逻 ×3、灵视检查站、未登记非凡者被举报 → 通缉;玩家选择:躲(潜行/伪装/离城)、自首(登记+小额罚金+洗白)、对抗(通缉 +2)。**D7:未入途径玩家完全豁免。**

---

# 17. 金镑经济与雾都生活(v0.6 新增)

## 17.1 三级货币(D4)

**1 金镑 = 20 苏勒 = 240 便士**(内部以便士 long 记账)。铸币实体物品(可掉落/存箱)+ 银行户头(教会金库存取,死亡不掉)。兑换:银行提供绿宝石 ↔ 便士挂牌价(服务器可配,默认 1 绿宝石=6 便士),打通原版村民经济。

**收入参照**:普通委托 2-8 苏勒 / 值夜者周薪 6 苏勒 / 一份魔药辅材 3-10 便士 / 序列 9 主材料黑市价 1-3 金镑 / 序列 5 特性拍卖起价 50 金镑。

## 17.2 侦探事务所(委托板)

雾都镇区核心玩法建筑。委托板每日刷新程序化委托(模板+参数):

| 委托类型 | 例 | 报酬 | 神秘学含量 |
|----------|----|------|-----------|
| 寻物/寻宠 | 找回走失的猫 | 2 苏勒 | 低(新手引导) |
| 跟踪调查 | 记录某 NPC 三个地点行程 | 4 苏勒 | 中(潜行/伪装) |
| 笔迹鉴定 | 鉴别遗嘱真伪 | 5 苏勒 | 中(灵视/占卜) |
| 灵异排查 | 民宅闹鬼调查 | 8 苏勒 | 高(灵体/会客法) |
| 失踪人口 | 顺藤摸到邪教据点 | 12 苏勒+ | 高(小型任务链) |

委托解法**多路径**(战斗/社交/神秘学至少两条),完成度分档结算;高完成度积累「事务所口碑」解锁高价委托。

## 17.3 报纸与传闻

每游戏周发行《雾都纪事》(自动生成:世界事件回顾+委托广告+匿名传闻栏)。**传闻栏是系统向玩家投放线索的官方渠道**:日记残页目击、结构坐标模糊指向、猎巫预告。玩家可付费刊登启事(寻物/招募/拍卖预告)——多人服公告板玩法。

## 17.4 当铺与黑市

当铺:抵押物品借贷(3 游戏日赎回,逾期没收上架);黑市(拾荒者行会):销赃(赃物标记洗白,-30% 价)、违禁品、伪造文书材料。

## 17.5 蒸汽机车

镇区间铁路快旅(付费 2 便士/百格,坐车加载屏动画+车厢内景)。**列车事件**:低概率触发车厢遭遇(扒手/查票/灵异车厢——一节"不存在的 13 号车厢"微副本)。

---

# 18. 世界事件系统(15 种 + 调度器代码)

## 18.1 事件总表

| # | 事件 | 触发 | 时长 | 效果 | 阶段 |
|---|------|------|------|------|------|
| 1 | 灵潮汹涌 | 周期+随机 | 1 夜 | 灵体生成 ×3,灵性恢复 ×1.5,占卜 +1 档 | MVP |
| 2 | 血月 | 月相替换(低概率) | 1 夜 | 失控检定 ×2,特性掉率 +15%,梦占危险 | MVP |
| 3 | 浓雾蔽日 | 天气链 | 2 游戏日 | 视距 8 格,潜行/偷盗增益,雾中遭遇表 | MVP |
| 4 | 邪神的注视 | 高危仪式失败/呼错名 | 12h | 全服占卜混乱、压力 +0.2/min、被注视者遭狩猎 | MVP |
| 5 | 猎巫夜 | 见 16.4 | 3 游戏日 | 审查与庇护 | MVP |
| 6 | 仪式共鸣 | 随机 | 6h | 全部仪式完成度 +10% | MVP |
| 7 | 灰雾涨潮 | 新月 | 1 夜 | 灰雾仪式门槛降至序列 9(萌新之夜) | EP1 |
| 8 | 特性风暴 | 沉积池溢出 | 1h | 区域特性凝集体成群,狩猎盛宴 | EP1 |
| 9 | 极光夜袭 | 声望敌对+定居点规模 | 波次 | 极光会袭击玩家聚居地 | EP1 |
| 10 | 封印物暴走 | 1 级封印物持有超时 | — | 封印物自主行动小事件 | EP1 |
| 11 | 教会巡礼 | 周期 | 1 游戏日 | 主教巡游:限时圣物租借+赦免(污染 -10) | EP1 |
| 12 | 灵界潮汐 | 随机(海岸/墓地) | 3h | 灵界生物涌入,门扉残页线索 | EP2 |
| 13 | 半神陨落回响 | 玩家/NPC 半神死亡 | 24h | 全服公告(匿名)、区域异象、特性争夺战 | EP2 |
| 14 | 陵寝开启 | 稀有结构激活 | 限时 | 高难副本窗口:古代非凡者陵墓 | EP2 |
| 15 | 灾祸具象 | 0 级封印物事件 | 服务器级 | 全服协作 Boss 事件(EP3,四选一轮换) |  EP3 |

## 18.2 调度器(完整)

```java
public class WorldEventScheduler extends SavedData {
    private static final String KEY = "pm_world_events";
    private final Map<ResourceLocation, ActiveEvent> active = new HashMap<>();
    private final List<QueuedSpawn> condensateQueue = new ArrayList<>();
    private long nextRollTime = 0;

    public record ActiveEvent(ResourceLocation id, long startedAt, long endsAt, CompoundTag ctx) {}

    public static WorldEventScheduler get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage()
                .computeIfAbsent(WorldEventScheduler::load, WorldEventScheduler::new, KEY);
    }

    /** 主入口:ServerTickEvent 每 100 tick 调用 */
    public void tick(MinecraftServer server) {
        long now = server.overworld().getGameTime();
        active.values().removeIf(e -> {
            if (now < e.endsAt()) return false;
            WorldEventRegistry.get(e.id()).onEnd(server, e.ctx()); setDirty(); return true;
        });
        if (now >= nextRollTime) {
            nextRollTime = now + 24000;                            // 每游戏日掷一次
            WorldEventRegistry.rollable(server).stream()
                .filter(def -> server.overworld().random.nextFloat() < def.dailyChance())
                .limit(1)
                .forEach(def -> start(server, def, new CompoundTag()));
            setDirty();
        }
        drainCondensateQueue(server);
    }

    public void start(MinecraftServer server, WorldEventDefinition def, CompoundTag ctx) {
        long now = server.overworld().getGameTime();
        active.put(def.id(), new ActiveEvent(def.id(), now, now + def.durationTicks(), ctx));
        def.onStart(server, ctx);
        Broadcast.eventBanner(server, def.bannerKey());            // 含蓄公告(报纸口吻)
        setDirty();
    }

    public static boolean isActive(ServerLevel level, ResourceLocation id) {
        return get(level).active.containsKey(id);
    }
    public static void forceSchedule(ServerLevel level, ResourceLocation id) {
        get(level).start(level.getServer(), WorldEventRegistry.get(id), new CompoundTag());
    }
    public static void queueCondensateSpawn(ChunkPos pos, BeyonderProfile profile) { /* 入队 */ }
    private void drainCondensateQueue(MinecraftServer server) { /* 加载区块内生成凝集体 */ }

    @Override public CompoundTag save(CompoundTag tag) { /* active 序列化 */ return tag; }
    public static WorldEventScheduler load(CompoundTag tag) { return new WorldEventScheduler(); }
}
```

---

## 18.3 世界事件扩充(v0.7:16-25 号)

| # | 事件 | 触发 | 内容 | 主要受益途径 |
|---|------|------|------|-------------|
| 16 | 血月 | 月相满月 ×8% 概率 | 夜怪强化 ×1.5、药师系能力免费、血族 NPC 出没、血肉蔷薇可采 | 药师/刺客 |
| 17 | 灵界裂隙 | 区域渡魂/死亡密度阈值 | 裂隙结构生成:灵体涌出,深层有摆渡人商人与门扉残响 | 收尸人/不眠者 |
| 18 | 风暴之种 | 雷暴天+海洋 | 移动超级雷暴胞:中心风眷者材料,外圈船只高危 | 水手 |
| 19 | 恒昼之兆 | 歌颂者玩家集体祷告达标 | 全服 1 游戏日白昼延长 30%,亡灵潮取消一次 | 歌颂者/全体 |
| 20 | 大失眠 | 服务器 7 日内睡眠率 <20% | 全服进入 3 夜"不得安眠":床失效、梦魇兽游荡、守夜层数双倍 | 不眠者 |
| 21 | 知识妖过境 | 随机稀有 | 一只不可攻击的"记录存在"漫游 30 分钟,跟随者随机获得/遗失 1 条知识 | 阅读者/窥秘人 |
| 22 | 竞技之夜 | 周常(周六夜) | 地下拳场开放:非凡能力禁用的纯格斗锦标赛,冠军得拳师指骨 | 战士 |
| 23 | 丰饶巡礼 | 春季首个满月 | 大地母神使者巡游耕地:达标农田获"沃土之心",践踏者受诅咒 | 耕种者 |
| 24 | 法庭开庭 | 玩家举报积累阈值 | NPC 巡回法庭抵达:玩家可起诉/辩护(交涉小游戏),胜诉方获法典点 | 律师/仲裁人 |
| 25 | 摆锤之日 | 命运途径玩家 ≥3 人 | 全服 24h 幸运/厄运双倍摆动,整点反转,报纸提前一日刊出"运势预告" | 怪物/全体 |

调度器兼容:全部走 18.2 的 `WorldEventScheduler` 权重池,新增字段 `seasonal`(23)与 `player_driven`(19/20/24/25)两类触发器,JSON 示例入 `data/pm/events/`。

# 19. 世界生成与结构(14 种)

| 结构 | 群系 | 内容 | 阶段 |
|------|------|------|------|
| 雾都镇区 | 平原/河畔 | 维多利亚小镇:事务所/教堂/报社/当铺/车站/贫民巷,主城枢纽 | MVP |
| 废弃教堂 | 森林/丘陵 | 入门副本:手稿+3 级封印物+怨念残影 | MVP |
| 神秘学家小屋 | 针叶林/沼泽 | 坩埚+材料+可疑手稿(伪造知识来源之一) | MVP |
| 邪教营地 | 荒野 | 极光会前哨:仪式阵+俘虏救援 | MVP |
| 古代祭坛 | 山巅/沙漠 | 大型仪式场地(环境加成 +10%) | MVP |
| 灵性泉眼 | 稀有 | 灵性恢复 ×3 区域+高价材料刷新 | MVP |
| 墓园 | 镇区外围 | 收尸人玩法核心:徘徊者/遗愿/掘墓委托 | MVP |
| 铁路与车站 | 连接镇区 | 蒸汽机车线(锚定多镇区) | MVP |
| 值夜者哨所 | 镇区/要道 | 组织据点:委托板+押运起点 | EP1 |
| 失落图书馆 | 地下 | 谜题副本:知识与禁书,摩西学会线索 | EP1 |
| 玫瑰庄园 | 黑森林 | 中型副本:玫瑰学派据点+前哨 Boss | EP1 |
| 沉船残骸 | 海底 | 水手途径材料+风暴教会委托 | EP2 |
| 古代陵寝 | 深地 | 「陵寝开启」限时副本本体 | EP2 |
| 贝克兰德式大城 | 稀有超大 | EP3 城市玩法:多区规划/雾巷犯罪/上流舞会 | EP3 |

**镇区生成规范**:Jigsaw 拼装,保证功能建筑齐全;首个镇区距出生点 ≤600 格(定位器:新手指南针物品)。

---

# 20. 生物图鉴与 Boss

## 20.1 常规非凡生物(10 类)

红睛巨狼(狼群夜猎,首领掉心脏)/ 幻形蛇(伪装成物品的拟态怪)/ 影栖鼬(偷玩家物品逃跑,追杀夺回)/ 摇篮蛾(催眠鳞粉云)/ 双头蛇(两头分别喷毒/缠绕)/ 窃能水蛭(吸取灵性,粘附需击落)/ 誊写魔像(失落图书馆守卫,复写玩家攻击模式)/ 好斗蜥王(挑衅气场,强制仇恨)/ 灰烬人偶(死亡爆丝线缠绕)/ 变形怪(Boss 级稀有:先侦察玩家再拟态其队友)。

## 20.2 失控体(5 种,对应首发途径)

| 途径 | 失控体 | 核心机制 |
|------|--------|----------|
| 占卜家 | 疯言先知 | 持续播报周围玩家真实坐标(信息污染),预判闪避 |
| 观众 | 窥心魇 | 复制受害者恐惧幻象群,本体隐形需灵视 |
| 猎人 | 燃烬兽 | 死亡冲锋+地面持续火化,击杀留焦土场 |
| 偷盗者 | 空手影 | 持续偷取玩家快捷栏并丢弃诱离,窃能吸蓝 |
| 学徒 | 乱典魔像 | 随机传送战+投掷"知识乱页"(致幻投射物) |

失控体特性掉率 60%;由玩家失控生成的个体命名为「(玩家名)的影子」——社死+叙事。

## 20.3 阶段 Boss:玫瑰之影(EP1)

玫瑰庄园地窖,三阶段:P1 血蔷薇藤场地战(藤蔓分区+小怪)→ P2 献祭仪式打断战(限时拆 4 个仪式烛台,失败进入强化)→ P3 半失控形态(序列 6 级数值+恐惧光环,场地坍缩)。掉落:序列 6 特性(保底)+ 2 级封印物(泣血匕首)+ 日记残页 ×1。**动态难度**:按在场玩家数与平均序列缩放 HP/伤害(表驱动)。

## 20.4 GeckoLib 清单(D6 落地)

神话形态 ×9(首发 5+EP2 4)、失控体 ×5、变形怪、阶段 Boss ×2、0 级具象 ×4 = **21 个骨骼动画模型**;通用动画状态机:idle/walk/attack1/attack2/skill/rage/death;模型规格约束:≤4k 面/骨骼 ≤40/贴图 ≤256²(服务器渲染压力控制)。

---

# 21. 任务链(3 条全步骤)

## 21.1 链一「失踪的调查小队」(MVP,6-10h)

1. 报社委托:失踪记者最后出现在废弃教堂 → 2. 教堂:染血笔记(第一份手稿)+怨念残影初遇 → 3. 灵视追踪血迹至神秘学家小屋:发现两份配方(一真一伪,鉴定教学)→ 4. 服药成为序列 9(任一首发途径,分支对白)→ 5. 顺线索至邪教营地:救出幸存记者(潜入/强攻/占卜三解)→ 6. 护送回镇触发夜袭防守战 → 7. 结算:值夜者接触+组织招募开启+链二钥匙物品「烧焦的名单」。

## 21.2 链二「消失的收藏家」(EP1,10-15h)

黑市封印物收藏家失踪 → 宅邸密室(锐眼/解密学者机关)→ 发现其被 1 级封印物「记忆窃取镜」同化 → 三方争夺战:玩家/值夜者/极光会同时抵达(动态阵营战)→ 选择:上缴教会(声望+圣物租借权)/黑市出售(50 金镑+通缉风险)/自持(同化挑战+隐藏成就)。多结局影响链三开局阵营态度。

## 21.3 链三「灰雾的邀请」(EP1 末,15h+)

连续 3 夜同一预知梦(梦境层剧本演出)→ 按梦中线索集齐灰雾仪式三要素(尊名一句藏于日记残页 #17)→ 首次进入灰雾:围绕 22 席的"面试"(答题+一次匿名投票)→ 领取席位 → 首个塔罗委托:跨玩家协作押运一件 2 级封印物穿越猎巫夜的城市(护送+反劫+身份保密三重玩法)→ 结算:席位坐实,尊名注册开放。

---

## 21.4 任务链四「白日之下无新事」(EP3 · 歌颂者/公证主题 · 8-12h)

1. 教堂晨祷时收到匿名委托:连续三期报纸的分类广告藏有同一串灵数密码 → 2. 灵数占卜(11 章)解码得出当铺地址 → 3. 当铺赎回一枚"誓约金印坯",发现印面被篡改 → 4. 公证人 NPC 鉴定:全城流通的婚约/地契公证印有 1/7 是伪造 → 5. 玩家选择:上报教会(声望+,黑市敌对)或私下追查(黑市线) → 6. 顺印坯工坊查到废弃铸币厂 → 7. Boss:伪誓者(会反转玩家 buff/debuff 的精英) → 8. 结局分支:销毁母印(全服伪造配方比例 -10%)/私藏母印(个人伪造特权+通缉风险)。奖励:配方 44 残页、公证声望、成就「谁来公证公证人」。

## 21.5 任务链五「不眠夜航」(EP3 · 水手/不眠者双线 · 10-14h)

雾都港一艘客轮夜航后全员"醒着失踪"(身体在,神智不在)。水手线:出海追溯航线→风暴之种内寻获船钟;不眠者线:入梦幸存者→梦境副本收集 4 段梦境残片。双线在"灯塔看守人"处汇合:看守人 30 年未眠,守夜层数溢出成为半失控体(可战/可渡:收尸人渡魂或安魂师净化可无伤解决)。奖励:噩梦马鬃、配方 33/30 各一残页、灯塔据点使用权(自带守夜人之火)。

## 21.6 任务链六「豺狼当道」(EP3 · 堕落路线专属 · 12h)

母树双途径玩家专属暗线:黑市中间人被连环灭口 → 玩家顶替其位 → 在 5 单黑市委托中平衡"罪值收益"与"教会注意度" → 最终发现幕后是一名潜伏的欲望使徒 NPC 企图献祭整条街区 → 选择:黑吃黑(吞其力量,罪值暴涨+获稀有特性)/告密洗白(罪值清零,教会保护性收编,转正线)。**是堕落玩家的"回头路"与"深渊路"分岔点**。

## 21.7 任务链七「二十一片真相」(EP4 · 亵渎石板主线 · 30h+)

围绕 14.4 的 21 残片设计的超长收集主线:每块石板配 1 个专属副本/事件/社交里程碑,7 块石板各有一段结算剧情;集齐任意 1 块解锁个人序列 3 试炼(41 章),集齐 7 块触发全服终局事件「群星回响」(神位战争赛季开启)。设计上**单人全收集预期 150h+,结社分工可压缩到 60h**——把毕业时间的大头放在这条链与 41 章。

## 21.8 任务链八「极光来信」(彩蛋链 · 4h)

低魔温情线:一位常年给"不存在的笔友"写信的邮差 NPC 委托玩家投递 7 封信,收件人遍布 7 教会辖区;每封信送达都揭示邮差与一位早已离世友人的往事;终点在雪原极光下,用会客法完成最后一次"面对面"。奖励:纪念怀表(非封印物,纯外观+小夜灯功能)、成就「有些信永远不会迟到」。(制作组签名彩蛋位,可埋 Tarot Club 成员名。)

---

# 22. 疯狂、幻觉与恐怖呈现

- **听觉优先**:低语(方向性音频,压力越高越近)→ 视觉边缘(余光粒子/一闪而过的人影)→ 正面幻觉(假实体:攻击判定穿透即散)→ 空间幻觉(假门假路,撞上即碎)四级递进;
- **假实体规则**:客户端幻觉实体不进服务端碰撞/仇恨,防误伤判定;PvP 场景幻觉不生成(竞技公平);
- **UI 侵蚀**:压力 ≥75 时手册文字偶发"错字化"、坐标 HUD 抖动 ±3——信息系统本身被腐蚀(占卜家玩家最恐惧的事);
- **恐怖分级开关**(客户端):完整/标准(默认,无 jump scare 音爆)/舒缓(仅 UI 提示);服务器可强制下限。

# 23. UI/UX 与可访问性

- HUD:灵性条(途径色)/消化环(围绕准星,含蓄发光)/压力以屏幕暗角+心跳声表达(无数字);
- 知识手册 G 键;能力轮盘 R 键(≤8 槽,拖拽绑定);仪式模式:手持粉笔进入阵纹辅助网格;
- 可访问性:色觉方案 ×3(灵视光环形状差异化:友=圆/敌=棱/异常=波纹)、低语字幕开关、光敏模式(禁频闪)、恐怖分级;
- 全部交互支持手柄式焦点导航(为未来 Controllable 兼容留接口)。

# 24. 多人与服务器生态

- **身份三层**:真名(账号)/ 公开身份(镇区档案,可伪造)/ 塔罗称号(灰雾)——三层互相隐藏构成社交推理空间;
- 阵营战争(EP2):教会联盟 vs 极光会据点攻防赛季;领地保护接口预留(FTB Chunks 兼容);
- 反滥用:偷盗/读心/操纵类能力全部提供服务器粒度开关+受害者保护冷却(同一受害者 10min 内免疫同一玩家同种侵犯);
- 经济防通胀:委托报酬全服日预算池、拍卖抽成回收、圣物租金回收。

# 25. 成就树(34 项选列)

入门:「第一口苦涩」(首次服药)/「看见了」(首次灵视)/「它在动」(首见灵体)。进阶:「墨守准则」(准则事件 ×10)/「吃一堑」(证伪一条假知识)/「无面之友」(以 3 种形态与同一 NPC 对话)/「纸人替我去」(纸人问事 ×5)/「完美一炉」(完美品质魔药)。高阶:「双重检定」(首次仪式晋升)/「提线人生」(傀儡 ×3 协同胜利)/「皇帝的注视」(日记 36 页)/「第 23 席」(围观满员塔罗会)。隐藏:「呼错了名字」(触发尊名灾变并存活)/「半神陨落」(击杀半神)/「体面人」(全年零通缉)。

## 25.1 成就扩充(v0.7:35-60 号 · 26 项新增)

| # | 成就 | 条件 | 类型 |
|---|------|------|------|
| 35 | 逆流者 | 完成一次相邻途径转换 | 挑战 |
| 36 | 三位一体 | 同一角色历经同组 3 条途径(源堡组限定) | 史诗 |
| 37 | 谁来公证公证人 | 完成任务链四并销毁母印 | 剧情 |
| 38 | 守夜三十 | 守夜层数生涯累计 30 | 途径 |
| 39 | 摆渡百魂 | 渡魂 100 名灵体 | 途径 |
| 40 | 誓约如铁 | 单一誓约持续 30 游戏日无违 | 途径 |
| 41 | 与污染共舞 | 污染 ≥70 状态存活 3 游戏日且未失控 | 挑战 |
| 42 | 抄家式学习 | 摹写 10 种不同能力 | 途径 |
| 43 | 荆棘之冠 | 荆冠仪式期间击败精英 | 途径 |
| 44 | 老农的浪漫 | 培育全部 12 种神秘作物 | 收集 |
| 45 | 血月炼金夜 | 血月期间炼出纯粹品质魔药 | 挑战 |
| 46 | 头版头条 | 登上报纸头条 3 次 | 社交 |
| 47 | 图书馆之魂 | 藏书阁收录 200 册 | 收集 |
| 48 | 全知的代价 | 集齐一块亵渎石板 | 史诗 |
| 49 | 群星回响 | 参与全服「群星回响」事件 | 史诗 |
| 50 | 圣者之上 | 晋升序列 3 | 史诗 |
| 51 | 唯一性 | 持有任意一份唯一性 ≥7 日 | 史诗 |
| 52 | 半神狩猎者 | 击杀一名神话形态玩家/NPC | 挑战 |
| 53 | 封印共鸣见证者 | 经历一次封印共鸣事件 | 世界 |
| 54 | 有些信永远不会迟到 | 完成任务链八 | 剧情 |
| 55 | 冠军之夜 | 竞技之夜夺冠 | 世界 |
| 56 | 法典缔造者 | 起草的法条被服务器采纳 | 社交 |
| 57 | 深渊回头 | 堕落路线洗白转正 | 剧情 |
| 58 | 深渊到底 | 堕落路线吞噬结局 | 剧情 |
| 59 | 大满贯藏家 | 持有过全部 5 个级别的封印物 | 收集 |
| 60 | 毕业典礼 | 序列 1(见 41 章封顶规则)+ 全 8 任务链 + 任意 20 成就 | 终局 |

# 26. 指令与权限(17 条)

```
/pm pathway set <玩家> <途径> <序列>     /pm pathway clear <玩家>
/pm stat get|set <玩家> <字段> [值]      /pm digest add <玩家> <值>
/pm knowledge grant|revoke <玩家> <条目> /pm knowledge list <玩家>
/pm pollution|pressure set <玩家> <值>   /pm breakdown trigger <玩家>
/pm ritual debug <on|off>               /pm event start|stop <事件ID>
/pm grayfog invite <玩家>               /pm seat assign <玩家> <0-21>
/pm money give <玩家> <便士>            /pm wanted set <玩家> <0-5>
/pm diary give <玩家> <页码>            /pm artifact spawn <ID>
/pm dump <玩家>                          (完整数据快照到日志,排障用)
```
权限级:2(OP);/pm dump 供 Herdeny 联调排障。

---

## 26.1 指令扩充(v0.7:18-26 号)

| # | 指令 | 权限 | 说明 |
|---|------|------|------|
| 18 | `/pm pathway switch <player> <pathway>` | 3 | 管理员强制途径转换(跳过检定) |
| 19 | `/pm slate grant <player> <slate> <fragment>` | 3 | 发放石板残片 |
| 20 | `/pm uniqueness locate <pathway>` | 4 | 查询某途径唯一性当前持有者/坐标 |
| 21 | `/pm trial start <player> [seq]` | 3 | 手动开启序列 3 试炼 |
| 22 | `/pm event fire <id>` | 3 | 立即触发指定世界事件(16-25 兼容) |
| 23 | `/pm news publish <headline_key>` | 2 | 手动投稿报纸头条 |
| 24 | `/pm law list|enact <id>|repeal <id>` | 2 | 法典系统管理 |
| 25 | `/pm material drops <material>` | 2 | 查询某神秘材料全部掉落来源(调试) |
| 26 | `/pm graduation report [player]` | 2 | 输出玩家毕业进度审计(41.5 用) |

---

# 27. 技术架构(F 分部)

## 27.1 模块分层

```
pm-core(Capability/网络/注册/配置)
 ├─ pm-pathway(途径/序列/能力执行器)
 ├─ pm-occult(魔药/仪式/占卜/灵视/会客法)
 ├─ pm-world(结构/事件/生物/失控体/Boss)
 ├─ pm-society(组织/声望/经济/委托/报纸/猎巫)
 ├─ pm-grayfog(维度/塔罗会/市场/祈求)
 └─ pm-client(渲染/HUD/手册/音频/GeckoLib 绑定)
```
单 jar 多 package;公共 API 收敛在 `top.aurora.projectmystery.api`(第 34 章)。

## 27.2 网络层(SimpleChannel 完整注册)

```java
public class MysteryNetwork {
    private static final String PROTOCOL = "6";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ProjectMystery.MOD_ID, "main"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);
    private static int id = 0;

    public static void register() {
        // S2C
        CHANNEL.messageBuilder(S2CCoreStatsPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
               .decoder(S2CCoreStatsPacket::decode).encoder(S2CCoreStatsPacket::encode)
               .consumerMainThread(S2CCoreStatsPacket::handle).add();
        CHANNEL.messageBuilder(S2CFullSyncPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
               .decoder(S2CFullSyncPacket::decode).encoder(S2CFullSyncPacket::encode)
               .consumerMainThread(S2CFullSyncPacket::handle).add();
        registerS2C(S2CSpiritAuraPacket.class);      // 灵视光环差量
        registerS2C(S2CInsanityFxPacket.class);      // 幻觉/低语指令
        registerS2C(S2CWhisperPacket.class);         // 呓语时刻字幕
        registerS2C(S2CDigestHintPacket.class);      // 消化含蓄提示
        registerS2C(S2CRitualFxPacket.class);        // 仪式表现
        registerS2C(S2CGrayFogUiPacket.class);       // 灰雾市场/席位 UI 数据
        registerS2C(S2CPrayerPopupPacket.class);     // 祈求弹窗
        registerS2C(S2CNewspaperPacket.class);       // 报纸内容
        // C2S
        registerC2S(C2SAbilityActivatePacket.class); // 能力施放(服务端全量校验)
        registerC2S(C2SAbilityBindPacket.class);
        registerC2S(C2SDivinationAskPacket.class);
        registerC2S(C2SRitualNamePacket.class);      // 尊名提交
        registerC2S(C2SMarketActionPacket.class);    // 挂单/购买/竞拍
        registerC2S(C2SSeanceAskPacket.class);       // 会客法问题
        registerC2S(C2SCodexActionPacket.class);
    }
    /* registerS2C/registerC2S 泛型辅助:反射约定 decode/encode/handle,略 */
}
```

**安全原则**:一切判定服务端执行;C2S 仅传意图(能力 ID/目标 ID/UI 动作),服务端校验冷却/消耗/视线/距离;灵视数据只发"玩家有权看见"的差量;读心/占卜结果按接收者单独计算,杜绝抓包全知。

## 27.3 能力执行器示例(完整,火焰跳跃)

```java
public class FlameLeapAbility implements ActiveAbility {
    public static final ResourceLocation ID = rl("flame_leap");
    @Override public float cost() { return 18f; }
    @Override public int cooldownTicks() { return 120; }
    @Override public boolean requires(PlayerMysteryData d) {
        return Pathways.MAGICIAN_LINE.equals(d.getPathway()) && d.getSequence() <= 7;
    }
    @Override
    public ActivationResult activate(ServerPlayer player, PlayerMysteryData data, AbilityContext ctx) {
        Vec3 look = player.getLookAngle();
        Vec3 target = RaycastUtil.safeBlink(player, look, 8.0);
        if (target == null) return ActivationResult.fail("pm.ability.no_path");

        ServerLevel level = player.serverLevel();
        FxUtil.flameBurst(level, player.position());
        player.teleportTo(target.x, target.y, target.z);
        FxUtil.flameBurst(level, target);
        level.getEntitiesOfClass(LivingEntity.class, AABB.ofSize(target, 3, 2, 3),
                e -> e != player).forEach(e -> e.setSecondsOnFire(2));
        level.playSound(null, BlockPos.containing(target),
                ModSounds.FLAME_LEAP.get(), SoundSource.PLAYERS, 0.9f, 1.0f);
        ActingEventHandler.trigger(player, "magician7_flashy_entry_arm", null); // 预备事件
        return ActivationResult.success();
    }
}
```

## 27.4 性能预算

灵视扫描 ≤0.3ms/玩家/10tick(AABB+类型过滤);结构检测事件驱动+哈希缓存;失控体/幻觉粒子客户端预算 ≤2000 粒子;市场/事件 SavedData 写盘节流(脏标记+30s 合批);GeckoLib 实体同屏 ≤8(生成器约束)。

# 28. 目录结构

```
src/main/java/top/aurora/projectmystery/
  api/  player/  pathway/  ability/  acting/  potion/  ritual/  divination/
  characteristic/  artifact/  knowledge/  grayfog/  society/  economy/
  world/{structure,event,entity}  client/{hud,codex,render,audio}  network/  config/  command/
src/main/resources/data/project_mystery/
  pathways/  sequences/  abilities/  acting_events/  potion_recipes/  rituals/
  honorifics/  artifacts/  knowledge/  whispers/  diary_pages/  commissions/
  world_events/  structures/  loot_tables/  advancements/
src/main/resources/assets/project_mystery/
  lang/{en_us,zh_cn}.json  textures/  geo/  animations/  sounds/
docs/ip_mapping.csv
```

# 29. JSON Schema 速览(数据包驱动清单)

pathways(见 7.10)/ sequences(能力列表+灵性加成+准则 key)/ abilities(类型/消耗/冷却/执行器 ID+参数)/ acting_events(见 8.3)/ potion_recipes(主材+辅材+火候+时长)/ rituals(pattern+材料+环境+呼名目标+结果表)/ honorifics(三句 lang key 组合)/ artifacts(见 13.4)/ knowledge(见 14.3)/ whispers(条件加权文案池)/ diary_pages(页码+加密态+硬信息引用)/ commissions(委托模板+参数域+多解法结算)/ world_events(触发+时长+回调 ID)。全部走 `SimpleJsonResourceReloadListener`,`/reload` 热更新。

# 30. 服务器配置全集(pm-server.toml 关键项)

```toml
[general]  death_penalty_mode="drop_potion_progress"  breakdown_mode="recoverable"
[rates]    digestion_mult=1.0  pollution_mult=1.0  spirit_regen_mult=1.0
[pvp]      steal_from_players=true  mind_read_players=true  victim_protection_min=10
[grayfog]  auction_fee_percent=10  meeting_interval_days=7  newmoon_lowbar=true
[economy]  emerald_to_penny=6  daily_commission_budget_pounds=40
[events]   witch_hunt_enabled=true  bloodmoon_chance=0.05
[horror]   server_min_horror_level="standard"  hallucination_in_pvp=false
[content]  implemented_pathways=["seer","spectator","hunter","marauder","apprentice"]
```

# 31. 本地化规范

zh_cn 为内容母本,en_us 同步骨架;key 规范 `pm.<域>.<条目>.<字段>`;**呓语/日记/传闻等文案全部原创**,写作基调:克制、书面、维多利亚译制腔;禁用网络流行语;ip_mapping.csv 三列(canonical_cn, original_cn, lang_key)驱动一键原创化构建变体(Gradle flavor:`-Pworldbuild=original`)。

# 32. 音效与音乐

低语声库(方向性 3D,男女声耳语分层 ×12)/ 坩埚沸腾三态(文/武/交替各异)/ 呓语时刻低频嗡鸣 / 灰雾大殿混响预设 / 蒸汽机车汽笛 / 教堂钟声(庇护区进入提示)/ 血月环境乐 / 神话形态变身主题短乐句 ×9(每途径 8s 动机)。BGM 策略:平时无 BGM(原版音景),事件与副本才进乐——恐怖靠留白。

# 33. 兼容性

JEI:仅展示「已验证」配方(知识系统接管解锁);Curios:封印物 3/2 级可入饰品槽(charm/necklace);GeckoLib 4.x 硬依赖(EP2 起);FTB Chunks/Quests 软兼容(领地保护回调+任务触发器导出);Create:蒸汽机车车站预留连接器接口(远期)。冲突声明:与其他大型 Mana 系 Mod 的灵性条 HUD 位置可配置避让。

# 34. 开发者 API(Addon 生态,D8 配套)

```java
// top.aurora.projectmystery.api
public interface ProjectMysteryApi {
    PathwayBuilder newPathway(ResourceLocation id);          // 社区补全 22 途径
    void registerAbilityExecutor(ResourceLocation id, ActiveAbility executor);
    void registerDivinationMethod(DivinationMethod method);
    void registerWorldEvent(WorldEventDefinition def);
    IEventBus mysteryEventBus();                             // 自定义事件总线
}
// 可订阅事件:BeyonderAdvanceEvent(Pre/Post) / ActingTriggerEvent(可取消/改值)
// PotionBrewedEvent / RitualResolveEvent / BreakdownEvent / CharacteristicDropEvent
// GrayFogTradeEvent / PrayerEvent —— 全部在 api 包内以稳定签名发布,遵循语义化版本
```

---

# 35. 里程碑(G 分部)

| 里程碑 | 内容 | 验收 |
|--------|------|------|
| M0(2 周) | 工程骨架:Capability/网络/配置/手册空壳 | /pm dump 全字段可读写同步 |
| M1(4 周) | 占卜家 9-7 纵切:魔药+扮演+灵视+失控 | 一小时切片(3.3)可玩 |
| M2(4 周) | 五途径 9-7、雾都镇区、委托、链一、ip_mapping 词表完成 | MVP 内测 |
| M3(3 周) | 封印物 24、世界事件 6、值夜者线、经济基础 | **MVP 公测** |
| M4(6 周) | 序列 6-5、仪式晋升、灰雾+塔罗会、特性完全体、猎巫、链二/三 | EP1 |
| M5(8 周) | 序列 4+GeckoLib 神话形态、EP2 四途径、组织战争、日记二辑 | EP2 |
| M6 | 灵界维度、1 级封印物全实装、大城市 | EP3 |
| M7 | 22 途径社区共创计划+API 冻结 1.0 | 生态 |

# 36. 测试与验收要点

数值:每序列 DPS/生存/功能三维雷达对标表(同序列差异 ≤15%);消化速率审计(目标:活跃 3-4h/序列段,挂机 0);经济:金镑日产出/回收报表指令 `/pm economy report`;网络:灵视 20 人同屏包量 ≤50KB/s/人;回归:Schema V2→V3 存档迁移用例 ×12;恐怖分级三档截图对照走查;多人专项:匿名破译渗透测试(内部红队扮演卧底一周)。

# 37. 风险登记册(Top8)

| 风险 | 等级 | 缓解 |
|------|------|------|
| IP 侵权 | 高 | D1/D2 双轨,发布前法务自查 |
| 范围蔓延(45 序列+150 能力) | 高 | 里程碑硬闸门:M3 前禁止实装 EP1 内容 |
| 扮演系统被刷 | 中 | 新颖度+目标衰减+服务器审计指令 |
| 恐怖内容劝退 | 中 | 分级三档+光敏模式 |
| 偷盗/读心引发多人纠纷 | 中 | 全量开关+受害者保护+日志留痕 |
| GeckoLib 21 模型工期 | 中 | 复用动画状态机+外包分包表 |
| 经济通胀 | 低 | 预算池+多重回收 |
| SavedData 膨胀 | 低 | 市场/事件条目上限+过期清理 |

# 38. 决策记录

D1-D8 见 1.3(全部落定)。**v0.8 新落定**:**D9 数据包全部脚本生成**——人手只维护 CSV 主表与样例 JSON,`scripts/gen_datapack.py` 渲染全量 1100+ 文件,JSON 永远是构建产物(45 章);**D12 AI 桥接只读红线**——pm-bridge 不向任何 AI 开放写玩家数据的指令(53.1)。**仍待决策**:D10 灵界维度是探索型还是仅事件型;D11 序列 4 是否引入"神性折磨"日常 debuff(拟真 vs 好玩);D13 跨服灰雾技术路线(Velocity 插件桥 or 数据库中台)。

# 39. 下一步(本周可执行)

1. Herdeny:M0 骨架仓库初始化(按 28 章目录),PlayerMysteryData/网络层直接取第 5/27 章代码落库;
2. 小倪:`docs/ip_mapping.csv` 词表首版(22 途径+7 教会+15 地名)+ 日记残页第一辑 12 页文案(原创);
3. 双方:数据包 schema 冻结评审(第 29 章清单过一遍,定字段名);
4. 建立数值表(Google Sheets → CSV → datagen)流水线,25 配方与灵性表先行;
5. M1 纵切目标日期倒排,占卜家 9-7 三个序列的能力执行器分工。

---

**文档结束 · Project Mystery v0.6**
*「知识即资源,力量有代价;雾未散,门未开。」——本文档全部设定与文案为原创撰写,仅设定名词沿用原作译名,发布前按 D2 完成映射。*

---

# 40. 非凡材料图鉴(v0.7 新增 · 80+ 材料 · 完整掉落修改器代码)

## 40.1 材料分类学

| 类 | 数量 | 来源模式 | 例 |
|----|------|----------|-----|
| A 生物精粹 | 24 | 特定生物条件击杀(时间/天气/方式修正) | 夜枭视络、深海鱼脊髓、噩梦马鬃 |
| B 环境凝结 | 16 | 特定时空采集(月相/正午/雷暴/群系) | 晨光凝露、风之精随、月光珍珠 |
| C 神秘作物 | 12 | 耕种者培育/嫁接链 | 安眠花蕊、血肉蔷薇球茎、丰饶麦穗王 |
| D 结构造物 | 14 | 探索结构宝箱/机关 | 六分仪碎片、拳师指骨、真相之眼水晶 |
| E 玩法结晶 | 10 | 系统行为副产(公证/渡魂/守夜/立论/献祭) | 誓约金印坯、亡者遗愿残响、立论之核 |
| F 事件掉落 | 8+ | 世界事件限定 | 门扉残响、无暗结晶、沃土之心 |

**设计铁律**:①任一配方主材 ≥2 获取渠道;②E 类材料**只能**由对应玩法产出且可交易——把"玩法"变成"产能",催生服务器分工经济;③A 类全部走 GLM(Global Loot Modifier),不改原版战利品表,兼容其他 Mod。

## 40.2 条件掉落修改器(完整实现)

```java
package top.aurora.projectmystery.loot;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * 神秘材料条件掉落(40.2)。
 * 数据包驱动:data/pm/loot_modifiers/*.json 声明目标实体、材料、基础概率与条件位掩码。
 * 条件:NIGHT_ONLY / THUNDER / FULL_MOON / NOON / KILLED_BY_ABILITY / BLOOD_MOON_EVENT。
 * 幸运附魔与"幸运儿"途径加成在 rollBonus 中统一结算。
 */
public class MysticMaterialLootModifier extends LootModifier {

    public static final int NIGHT_ONLY = 1, THUNDER = 1 << 1, FULL_MOON = 1 << 2,
            NOON = 1 << 3, KILLED_BY_ABILITY = 1 << 4, BLOOD_MOON = 1 << 5;

    public static final Supplier<Codec<MysticMaterialLootModifier>> CODEC = () ->
            RecordCodecBuilder.create(inst -> codecStart(inst).and(inst.group(
                    ResourceLocation.CODEC.fieldOf("target_entity").forGetter(m -> m.targetEntity),
                    ResourceLocation.CODEC.fieldOf("material").forGetter(m -> m.material),
                    Codec.FLOAT.fieldOf("base_chance").forGetter(m -> m.baseChance),
                    Codec.INT.optionalFieldOf("conditions", 0).forGetter(m -> m.conditions),
                    Codec.INT.optionalFieldOf("max_count", 1).forGetter(m -> m.maxCount)
            )).apply(inst, MysticMaterialLootModifier::new));

    private final ResourceLocation targetEntity;
    private final ResourceLocation material;
    private final float baseChance;
    private final int conditions;
    private final int maxCount;

    public MysticMaterialLootModifier(LootItemCondition[] conditionsIn, ResourceLocation targetEntity,
                                      ResourceLocation material, float baseChance, int conditions, int maxCount) {
        super(conditionsIn);
        this.targetEntity = targetEntity;
        this.material = material;
        this.baseChance = baseChance;
        this.conditions = conditions;
        this.maxCount = maxCount;
    }

    @Override
    protected @NotNull it.unimi.dsi.fastutil.objects.ObjectArrayList<ItemStack> doApply(
            it.unimi.dsi.fastutil.objects.ObjectArrayList<ItemStack> loot, LootContext ctx) {

        Entity victim = ctx.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (victim == null) return loot;
        ResourceLocation type = ForgeRegistries.ENTITY_TYPES.getKey(victim.getType());
        if (!targetEntity.equals(type)) return loot;

        var level = ctx.getLevel();
        long dayTime = level.getDayTime() % 24000L;

        if (has(NIGHT_ONLY) && (dayTime < 13000 || dayTime > 23000)) return loot;
        if (has(NOON) && (dayTime < 5500 || dayTime > 6500)) return loot;
        if (has(THUNDER) && !level.isThundering()) return loot;
        if (has(FULL_MOON) && level.getMoonPhase() != 0) return loot;
        if (has(BLOOD_MOON) && !top.aurora.projectmystery.event.WorldEventScheduler
                .isActive(level, new ResourceLocation("pm", "blood_moon"))) return loot;
        if (has(KILLED_BY_ABILITY) && !wasKilledByAbility(victim)) return loot;

        float chance = baseChance * (1f + rollBonus(ctx));
        int count = 0;
        for (int i = 0; i < maxCount; i++) if (ctx.getRandom().nextFloat() < chance) count++;
        if (count > 0) {
            var item = ForgeRegistries.ITEMS.getValue(material);
            if (item != null) loot.add(new ItemStack(item, count));
        }
        return loot;
    }

    private boolean has(int flag) { return (conditions & flag) != 0; }

    private boolean wasKilledByAbility(Entity victim) {
        return victim.getPersistentData().getBoolean("pm_killed_by_ability"); // AbilityExecutor 落锤时打标
    }

    private float rollBonus(LootContext ctx) {
        float bonus = ctx.getLuck() * 0.05f;
        if (ctx.getParamOrNull(LootContextParams.KILLER_ENTITY) instanceof LivingEntity killer) {
            bonus += top.aurora.projectmystery.pathway.LuckHooks.materialFindBonus(killer); // 幸运儿等
        }
        return Math.min(bonus, 0.5f);
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() { return CODEC.get(); }
}
```

注册:`DeferredRegister<Codec<? extends IGlobalLootModifier>>` 于 `ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS`;数据包 JSON 示例(夜枭视络):

```json
{
  "type": "pm:mystic_material",
  "conditions": [],
  "target_entity": "pm:night_owl",
  "material": "pm:night_owl_retina",
  "base_chance": 0.35,
  "conditions_mask": 17,
  "max_count": 1
}
```

## 40.3 神秘作物嫁接树(C 类 12 种)

`基础种子 →(生物学家嫁接)→ 二级 →(满足环境条件)→ 神秘作物`。示例链:小麦+发光浆果→微光麦→月相满月夜收割→**安眠花蕊**;甜浆果+玫瑰丛→棘心莓→以鲜血精粹灌溉→**血肉蔷薇球茎**。全 12 条链入 `data/pm/grafting/*.json`(fields: base_a, base_b, intermediate, condition, result, growth_days)。作物生长周期刻意拉长(3-7 游戏日)+地力约束——**农业型玩家的长线内容**。

---

# 41. 序列 3-1 后期玩法:圣者试炼、唯一性与神位战争(v0.7 新增 · 毕业延长核心)

> v0.6 将 3-0 定为"不可玩"。v0.7 修订:**序列 3(圣者)与序列 2/1 有限可玩,序列 0 永久不可玩**(保留为世界观天花板与服务器事件)。本章同时给出全 Mod 的**毕业时间设计账本**。

## 41.1 序列 3「圣者试炼」

前置:序列 4 消化 100% + 对应组亵渎石板集齐(14.4)+ 序列 3 魔药(配方仅出自石板解读)。试炼为**个人副本**(独立维度实例),三幕结构:

1. **幕一·途径之影**:与"自己所有低序列形态的残影"车轮战——检验玩家对本途径全套能力的理解(残影使用玩家历史数据:常用能力/常用走位)。
2. **幕二·准则之问**:非战斗幕。3 个道德困境场景(数据包驱动,每途径专属),选择无对错但必须**符合扮演准则**——违背准则即失败。占卜家问"知而不言",战士问"誓约与生命孰重",收尸人问"该不该唤醒安眠者"。
3. **幕三·失控之渊**:污染强制拉满至 99 的状态下 Boss 战(本途径失控体的"完全形态"),战斗中污染每 10s -1,坚持到污染 <60 即胜——主题:**在疯狂中保持自我**。

胜利:晋升序列 3,解锁"圣者领域"(领域雏形完全体,16-24 格)与 1 个途径终极能力;失败:退出副本,7 日冷却,魔药不消耗(试炼可反复挑战,但幕二选择每次刷新)。

## 41.2 唯一性系统(序列 2/1 门槛)

**规则**:每条途径全服**只存在一份「唯一性」**(不可合成、Boss/事件产出或从持有者身上夺取)。持有唯一性 + 序列 3 满消化 = 可晋升序列 2;序列 1 需同组"半数以上唯一性认可"(持有或结盟)。这把 2/1 从"刷材料"变成**服务器政治与赛季博弈**:

| 项 | 规则 |
|----|------|
| 产出 | 每赛季(现实 8 周)每途径 1 份,出自「群星回响」终局事件的途径试炼塔 |
| 夺取 | 击杀持有者掉落(受 3 日新手保护);或灰雾"神位决斗"(双方同意的规则化 PVP) |
| 持有代价 | 全服坐标每日一次向同组序列 ≥4 玩家模糊广播(半径 500 格级);污染增速 +20% |
| 离线保护 | 离线 >48h 唯一性自动"沉眠"回试炼塔(防屯号) |
| 赛季重置 | 赛季末序列 2/1 玩家进入"荣誉殿堂"(名字+雕像),角色转为 NPC 化传奇(可选新角色继承 10% 消化速度加成) |

## 41.3 UniquenessManager(完整实现)

```java
package top.aurora.projectmystery.endgame;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.aurora.projectmystery.ProjectMystery;
import top.aurora.projectmystery.item.UniquenessItem;
import top.aurora.projectmystery.pathway.PathwayRegistry;

import javax.annotation.Nullable;
import java.util.*;

/**
 * 唯一性登记簿(41.2/41.3):全服每途径一份的世界级 SavedData。
 * 负责:持有登记、每日模糊广播、离线沉眠、死亡掉落、防复制(NBT 序列号校验)。
 */
@Mod.EventBusSubscriber(modid = ProjectMystery.MODID)
public class UniquenessManager extends SavedData {

    private static final String DATA_NAME = "pm_uniqueness";
    private static final long OFFLINE_SLEEP_TICKS = 48L * 60 * 60 * 20; // 现实 48h(以 tick 估算,服务器不停机场景)
    public static final int BROADCAST_BLUR_RADIUS = 500;

    /** pathwayId -> record */
    private final Map<ResourceLocation, Entry> entries = new HashMap<>();

    public static UniquenessManager get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(
                UniquenessManager::load, UniquenessManager::new, DATA_NAME);
    }

    /* ------------------- 发放与登记 ------------------- */

    /** 试炼塔产出时调用:生成带序列号的唯一性物品。若该途径已存在未沉眠唯一性则拒绝。 */
    public Optional<ItemStack> issue(MinecraftServer server, ResourceLocation pathway, UUID firstOwner) {
        Entry e = entries.get(pathway);
        if (e != null && !e.dormant) return Optional.empty();
        long serial = new Random().nextLong();
        ItemStack stack = UniquenessItem.create(pathway, serial);
        entries.put(pathway, new Entry(firstOwner, serial, false, server.overworld().getGameTime()));
        setDirty();
        broadcast(server, Component.translatable("pm.uniqueness.issued",
                Component.translatable(PathwayRegistry.get(pathway).displayKey())));
        return Optional.of(stack);
    }

    public boolean verify(ResourceLocation pathway, long serial) {
        Entry e = entries.get(pathway);
        return e != null && e.serial == serial && !e.dormant;   // 复制品序列号必然对不上或已沉眠
    }

    public void transfer(MinecraftServer server, ResourceLocation pathway, UUID newOwner) {
        Entry e = entries.get(pathway);
        if (e == null) return;
        e.owner = newOwner;
        e.dormant = false;
        e.lastSeenTick = server.overworld().getGameTime();
        setDirty();
    }

    /* ------------------- 事件钩子 ------------------- */

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        UniquenessManager mgr = get(player.server);
        for (ItemStack stack : collectUniqueness(player)) {
            ResourceLocation pathway = UniquenessItem.pathwayOf(stack);
            if (mgr.isProtected(player)) continue;              // 3 日新手保护
            player.getInventory().removeItem(stack);
            ItemEntity drop = new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), stack);
            drop.setUnlimitedLifetime();                        // 不消失,直到被拾取或沉眠回收
            drop.setGlowingTag(true);
            player.level().addFreshEntity(drop);
            mgr.broadcast(player.server, Component.translatable("pm.uniqueness.dropped",
                    Component.translatable(PathwayRegistry.get(pathway).displayKey())));
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        get(player.server).entries.values().stream()
                .filter(e -> player.getUUID().equals(e.owner))
                .forEach(e -> { e.lastSeenTick = player.level().getGameTime(); });
    }

    /** 每日 0 点由 WorldEventScheduler 调用:模糊坐标广播 + 离线沉眠检查。 */
    public void dailyTick(MinecraftServer server) {
        long now = server.overworld().getGameTime();
        for (var it = entries.entrySet().iterator(); it.hasNext(); ) {
            var kv = it.next();
            Entry e = kv.getValue();
            ServerPlayer owner = e.owner == null ? null : server.getPlayerList().getPlayer(e.owner);
            if (owner == null) {
                if (!e.dormant && now - e.lastSeenTick > OFFLINE_SLEEP_TICKS) {
                    e.dormant = true;                            // 沉眠:回试炼塔重出
                    setDirty();
                    broadcast(server, Component.translatable("pm.uniqueness.dormant",
                            Component.translatable(PathwayRegistry.get(kv.getKey()).displayKey())));
                }
                continue;
            }
            BlockPos blurred = blur(owner.blockPosition());
            broadcastToGroupHunters(server, kv.getKey(), Component.translatable(
                    "pm.uniqueness.whisper", blurred.getX(), blurred.getZ()));
        }
    }

    /* ------------------- 内部 ------------------- */

    private BlockPos blur(BlockPos real) {
        Random r = new Random();
        return new BlockPos(
                (real.getX() / BROADCAST_BLUR_RADIUS) * BROADCAST_BLUR_RADIUS + r.nextInt(BROADCAST_BLUR_RADIUS),
                0,
                (real.getZ() / BROADCAST_BLUR_RADIUS) * BROADCAST_BLUR_RADIUS + r.nextInt(BROADCAST_BLUR_RADIUS));
    }

    private boolean isProtected(ServerPlayer p) {
        return p.getPersistentData().getLong("pm_uniq_protect_until") > p.level().getGameTime();
    }

    private static List<ItemStack> collectUniqueness(ServerPlayer p) {
        List<ItemStack> list = new ArrayList<>();
        p.getInventory().items.forEach(s -> { if (s.getItem() instanceof UniquenessItem) list.add(s); });
        return list;
    }

    private void broadcast(MinecraftServer server, Component msg) {
        server.getPlayerList().broadcastSystemMessage(msg, false);
    }

    private void broadcastToGroupHunters(MinecraftServer server, ResourceLocation pathway, Component msg) {
        server.getPlayerList().getPlayers().stream()
                .filter(p -> top.aurora.projectmystery.pathway.PathwayQueries.sameGroupAndSeqAtLeast(p, pathway, 4))
                .forEach(p -> p.sendSystemMessage(msg));
    }

    /* ------------------- SavedData ------------------- */

    public static UniquenessManager load(CompoundTag tag) {
        UniquenessManager mgr = new UniquenessManager();
        CompoundTag list = tag.getCompound("entries");
        for (String key : list.getAllKeys()) {
            CompoundTag et = list.getCompound(key);
            Entry e = new Entry(et.hasUUID("owner") ? et.getUUID("owner") : null,
                    et.getLong("serial"), et.getBoolean("dormant"), et.getLong("lastSeen"));
            mgr.entries.put(new ResourceLocation(key), e);
        }
        return mgr;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag list = new CompoundTag();
        entries.forEach((k, e) -> {
            CompoundTag et = new CompoundTag();
            if (e.owner != null) et.putUUID("owner", e.owner);
            et.putLong("serial", e.serial);
            et.putBoolean("dormant", e.dormant);
            et.putLong("lastSeen", e.lastSeenTick);
            list.put(k.toString(), et);
        });
        tag.put("entries", list);
        return tag;
    }

    private static final class Entry {
        @Nullable UUID owner;
        final long serial;
        boolean dormant;
        long lastSeenTick;
        Entry(@Nullable UUID owner, long serial, boolean dormant, long lastSeenTick) {
            this.owner = owner; this.serial = serial; this.dormant = dormant; this.lastSeenTick = lastSeenTick;
        }
    }
}
```

## 41.4 神位战争(赛季终局 · 序列 0 的"不可玩"呈现)

「群星回响」后 7 日为神位战争窗口:各组唯一性持有者可发起**登神仪式**(超大型仪式,10 分钟持续引导,全服坐标公开)。规则:**仪式必然在 99% 时失败**(序列 0 永不可达),但按坚持时长结算赛季排名与"荣誉殿堂"席位;其他玩家可攻打仪式(掠夺唯一性)或护驾(护驾方分享排名分)。设计意图:序列 0 是灯塔不是终点——**"毕业"被定义为序列 1+殿堂留名**,而非成神。

## 41.5 毕业时间账本(节奏总控)

| 阶段 | 内容 | 预期时长(单人/结社) |
|------|------|---------------------|
| 序列 9→7 | 教学+扮演入门 | 15h / 10h |
| 序列 7→5 | 仪式、配方残页、任务链 1-3 | 40h / 25h |
| 序列 5→4 | 大仪式+途径试炼+半神材料 | 50h / 30h |
| 序列 4 期 | 相邻转换 Build、任务链 4-6、封印物收集 | 60h / 40h |
| 序列 4→3 | 石板 21 残片+圣者试炼(任务链 7) | 150h / 60h |
| 序列 3→2 | 唯一性获取与保有(赛季制) | ≥1 赛季(8 周) |
| 序列 2→1 | 同组唯一性政治 | 1-2 赛季 |
| **毕业** | 成就 60「毕业典礼」 | **总计 400-600h(单人)/ 250h+(结社)** |

三条防加速红线:①消化只能由扮演事件产出(无氪无刷);②E 类材料绑定玩法产能;③唯一性赛季化。三条防劝退底线:①每阶段 ≥3 条可选方向(战斗/社交/收集/种田/经济);②结社共研机制让社交显著提速;③序列 5 即有完整 Build 乐趣,毕业是马拉松不是门票。

---

# 42. 雾都生活扩充:报社、大学、剧院与悬赏(v0.7 新增 · 完整代码)

## 42.1 四大新据点(挂接 17 章经济)

| 据点 | 结构 | 玩法 | 关联系统 |
|------|------|------|----------|
| 《雾都纪闻》报社 | 城镇印刷所 | 每游戏周自动出刊:头条(全服大事)+运势(占卜家投稿抽成)+分类广告(玩家付费刊登)+悬赏页 | 18/21/41 事件源;42.2 代码 |
| 雾都大学 | 大型讲堂+图书馆 | 阅读者立论仪式场地;NPC 公开课(听课得知识条目,每周 3 门轮换);藏书阁公共翼(玩家捐书换声望) | 7.11.3 |
| 皇家剧院 | 剧院结构 | 每满月上演 NPC 剧目(观看得压力 -10);玩家可包场表演(表情动作编排小系统),观众打赏金镑 | 22 章压力管理出口 |
| 悬赏公会 | 事务所隔壁 | 玩家互挂悬赏(需公证防滥用);失控体/通缉 NPC 官方悬赏;赏金猎人排行榜 | 16/24 事件、PVP 规范出口 |

## 42.2 NewspaperGenerator(完整实现)

```java
package top.aurora.projectmystery.city.news;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import top.aurora.projectmystery.economy.CurrencyManager;
import top.aurora.projectmystery.event.WorldEventLog;
import top.aurora.projectmystery.endgame.UniquenessManager;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 《雾都纪闻》周刊生成器(42.2)。
 * 每游戏周一 06:00 由调度器调用 publish():
 *   1) 汇总 WorldEventLog 本周大事 → 头条与要闻(模板化原创文案,lang key 池)
 *   2) 收集玩家付费广告队列(金镑结算,10% 归灰雾首席金库)
 *   3) 占卜家投稿"运势栏"(可信度标注,错误运势会被读者 NPC 吐槽 → 投稿者小额声望损失)
 *   4) 生成 written_book 并投递到全部邮箱方块 + 报童 NPC 库存
 */
public final class NewspaperGenerator {

    private static final int MAX_HEADLINES = 3;
    private static final int MAX_ADS = 8;
    private static final int AD_PRICE_SOULS = 5;               // 5 苏勒/条
    private static final Random RNG = new Random();

    /** 模板池:同一事件类型 3-5 个标题模板随机,避免八股。全部 lang key,文案原创。 */
    private static final Map<ResourceLocation, List<String>> HEADLINE_TEMPLATES = Map.of(
            new ResourceLocation("pm", "witch_hunt"), List.of(
                    "pm.news.headline.witchhunt.1", "pm.news.headline.witchhunt.2", "pm.news.headline.witchhunt.3"),
            new ResourceLocation("pm", "blood_moon"), List.of(
                    "pm.news.headline.bloodmoon.1", "pm.news.headline.bloodmoon.2"),
            new ResourceLocation("pm", "seal_resonance"), List.of(
                    "pm.news.headline.resonance.1", "pm.news.headline.resonance.2"),
            new ResourceLocation("pm", "uniqueness_transfer"), List.of(
                    "pm.news.headline.uniqueness.1", "pm.news.headline.uniqueness.2", "pm.news.headline.uniqueness.3"),
            new ResourceLocation("pm", "first_recipe"), List.of(
                    "pm.news.headline.firstrecipe.1", "pm.news.headline.firstrecipe.2"));

    private final Deque<Advert> adQueue = new ArrayDeque<>();
    private final List<FortuneSubmission> fortunes = new ArrayList<>();

    /* ---------------- 对外接口 ---------------- */

    public boolean submitAdvert(UUID player, String textKeyOrRaw, MinecraftServer server) {
        if (adQueue.size() >= MAX_ADS * 3) return false;                   // 队列上限:3 期
        if (!CurrencyManager.charge(server, player, 0, AD_PRICE_SOULS, 0)) return false;
        CurrencyManager.tithe(server, AD_PRICE_SOULS / 10);                // 10% 入首席金库
        adQueue.add(new Advert(player, sanitize(textKeyOrRaw)));
        return true;
    }

    public void submitFortune(UUID seer, String fortuneText, float credibility) {
        fortunes.add(new FortuneSubmission(seer, sanitize(fortuneText), credibility));
    }

    /** 周一 06:00 调度入口 */
    public ItemStack publish(MinecraftServer server) {
        List<Component> pages = new ArrayList<>();
        pages.add(frontPage(server));
        pages.add(fortunePage());
        pages.add(adsPage());
        pages.add(bountyPage(server));

        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        CompoundTag tag = book.getOrCreateTag();
        tag.putString("title", Component.translatable("pm.news.title").getString()
                + " · " + issueNumber(server));
        tag.putString("author", "The Foggy Chronicle");
        ListTag pageList = new ListTag();
        pages.forEach(p -> pageList.add(StringTag.valueOf(Component.Serializer.toJson(p))));
        tag.put("pages", pageList);

        MailboxDelivery.deliverToAll(server, book.copy());                 // 邮箱方块投递
        fortunes.clear();
        return book;
    }

    /* ---------------- 版面 ---------------- */

    private Component frontPage(MinecraftServer server) {
        var events = WorldEventLog.thisWeek(server).stream()
                .sorted(Comparator.comparingInt(WorldEventLog.Entry::importance).reversed())
                .limit(MAX_HEADLINES).toList();
        var page = Component.empty().copy();
        if (events.isEmpty()) {
            page.append(Component.translatable("pm.news.headline.quiet_week"));  // "本周雾都风平浪静(存疑)"
        }
        for (var e : events) {
            List<String> pool = HEADLINE_TEMPLATES.getOrDefault(e.type(),
                    List.of("pm.news.headline.generic"));
            page.append(Component.translatable(pool.get(RNG.nextInt(pool.size())), e.args()))
                .append(Component.literal("\n\n"));
        }
        return page;
    }

    private Component fortunePage() {
        var page = Component.translatable("pm.news.fortune.header").copy().append("\n");
        fortunes.stream().sorted(Comparator.comparingDouble(f -> -f.credibility))
                .limit(3)
                .forEach(f -> page.append(Component.literal("§7" + f.text + " §8("
                        + credibilityStars(f.credibility) + ")\n")));
        return page;
    }

    private Component adsPage() {
        var page = Component.translatable("pm.news.ads.header").copy().append("\n");
        for (int i = 0; i < MAX_ADS && !adQueue.isEmpty(); i++) {
            page.append(Component.literal("§o" + adQueue.poll().text + "§r\n"));
        }
        return page;
    }

    private Component bountyPage(MinecraftServer server) {
        var page = Component.translatable("pm.news.bounty.header").copy().append("\n");
        top.aurora.projectmystery.city.bounty.BountyBoard.get(server).topBounties(5)
                .forEach(b -> page.append(Component.translatable("pm.news.bounty.line",
                        b.targetName(), b.rewardPounds()).append("\n")));
        UniquenessManager.get(server); // 触发懒加载,确保悬赏页读取唯一性动态时数据就绪
        return page;
    }

    /* ---------------- 工具 ---------------- */

    private static String sanitize(String raw) {
        String s = raw.replaceAll("[§\\x00-\\x1F]", "").trim();
        return s.length() > 120 ? s.substring(0, 120) + "…" : s;
    }

    private static String credibilityStars(float c) {
        int n = Math.round(c * 5);
        return "★".repeat(Math.max(1, n)) + "☆".repeat(5 - Math.max(1, n));
    }

    private static String issueNumber(MinecraftServer server) {
        long weeks = server.overworld().getGameTime() / (24000L * 7);
        return "No." + (weeks + 1);
    }

    private record Advert(UUID owner, String text) {}
    private record FortuneSubmission(UUID seer, String text, float credibility) {}
}
```

配套:`WorldEventLog`(事件调度器写入的环形日志,字段 type/importance/args/gameDay)、`MailboxDelivery`(邮箱方块实体遍历投递)、`BountyBoard`(SavedData,悬赏发布走公证防滥用)。头条模板 lang 文案全部原创撰写,示例:`pm.news.headline.uniqueness.1 = "【号外】'%s'之器易主!昨夜北区目击雾中追逐,绅士们请系好怀表。"`

## 42.3 声望矩阵(七教会 × 玩家)

七教会独立声望轨(-100~+100):任务/公证/净化/捐赠加声望,堕落行为/持有违禁封印物/猎巫夜袭击减声望。声望档位解锁:圣物租借(13.3)、教会配给材料、避难所使用、序列 5 仪式场地借用。**互斥设计**:部分教会两两存在外交张力(数据包 `church_relations.json`),对立教会声望联动(+10 某家 → -3 其对家)——身份选择有重量,但不锁死(可用大额捐赠修复)。

---

---

# 43. 全物品注册总表(v0.8 新增 · 180+ 物品 · I 分部)

> **本章定位**:物品域的唯一权威清单。所有注册 ID 使用英文中性命名(工程约束 1.2),显示名走 `zh_cn.json`。每件物品**获取路线 ≥2 条**(防单点卡死,同 9.6 原则)。稀有度:C 常见 / U 罕见 / R 稀有 / E 史诗 / L 传说。

## 43.1 物品域划分

| 域 | 前缀约定 | 数量 | 说明 |
|----|---------|------|------|
| 工具与器具 | `tool_` / 直名 | 22 | 占卜器具、仪式器具、坩埚配件、侦查工具 |
| 神秘材料 | 直名(对齐 40 章图鉴) | 84 | A-F 六类,详表见 43.3(与 40 章一一对应落 ID) |
| 魔药与成品 | `potion_` / `elixir_` | 12 | 魔药瓶、失败副产物、稀释剂 |
| 仪式与文书 | `scroll_` / `page_` / `charm_` | 28 | 配方卷轴、日记残页、石板残片、符咒、契约 |
| 货币与经济 | `coin_` / `note_` | 9 | 三级货币、银行券、当票、车票 |
| 杂项与彩蛋 | misc | 25+ | 纪念品、装饰、剧情道具 |

## 43.2 工具与器具(22 件全表)

| 注册 ID | 显示名 | 稀有度 | 堆叠 | 功能 | 获取路线 |
|---------|--------|--------|------|------|---------|
| `spirit_pendulum` | 灵摆 | C | 1 | 占卜法①载体;右键开始灵摆占卜 | 工作台合成 / 侦探所购买(2 苏勒) |
| `dowsing_rod` | 占卜杖 | U | 1 | 占卜法②寻物寻人 | 合成 / 神秘学家小屋宝箱 |
| `tarot_deck` | 塔罗牌组 | U | 1 | 占卜法③;耐久 78 次 | 合成(需 22 张牌面纸) / 灰雾市场 |
| `dream_incense` | 梦境熏香 | U | 16 | 占卜法④催化;睡前使用进入引导梦 | 合成 / 教会配给 |
| `star_chart` | 星象图 | R | 1 | 占卜法⑤;仅夜间露天可用 | 大学天文台兑换 / 遗迹 |
| `numerology_slate` | 灵数石板 | U | 1 | 占卜法⑥;输入问题得数列 | 合成 / 当铺 |
| `paper_figurine` | 纸人 | C | 64 | 占卜法⑦纸人问事耗材 | 合成(纸+朱砂墨) / 杂货店 |
| `ritual_dagger` | 仪式银匕 | U | 1 | 划定仪式阵、采集灵体材料必需 | 合成(银锭) / 值夜者配发 |
| `ritual_chalk` | 仪式粉笔 | C | 64 | 绘制仪式图案(12.1) | 合成(骨粉+染料) / 商店 |
| `silver_censer` | 银香炉 | U | 1 | 仪式环境净化,+5% 成功率 | 合成 / 教堂声望兑换 |
| `crucible_stirrer` | 坩埚搅拌杖 | C | 1 | 炼药交替火候手动微调(9.4) | 合成 |
| `thermometer_brass` | 黄铜测温计 | U | 1 | 显示坩埚实时温度数值(否则只有粒子暗示) | 合成 / 大学购买 |
| `spirit_goggles` | 灵视护目镜 | R | 1 | 非灵视途径玩家获得 30s 临时灵视,冷却 5min | 1 级封印物副产 / 灰雾拍卖 |
| `magnifier` | 放大镜 | C | 1 | 侦查:高亮 8 格内痕迹方块(脚印/血迹) | 合成 / 侦探所 |
| `fingerprint_kit` | 指纹粉盒 | U | 8 | 委托玩法:提取容器/门上的最后交互者线索 | 侦探所声望兑换 |
| `sealing_wax_kit` | 火漆封蜡盒 | C | 16 | 公证/契约玩法耗材(17/42 章) | 合成 / 文具店 |
| `notary_seal` | 公证金印 | R | 1 | 公证人职业器具;为文书盖真伪凭证 | 配方 44 产物 / 教会授予 |
| `grave_shovel` | 收尸人铁铲 | U | 1 | 挖掘墓土 3 倍速;可发掘"古骸"稀有产物 | 合成 / 收尸人任务线 |
| `ferry_bell` | 寂静之铃 | R | 1 | 会客法增幅:占线概率 -20% | 摆渡人交易 / 灵界裂隙 |
| `wax_earplugs` | 蜡封耳塞 | C | 16 | 佩戴期间低语免疫但环境音全失(风险交换) | 合成 / 隐修院 |
| `pocket_watch` | 怀表 | C | 1 | 显示现实时间+游戏内时段;雾都绅士标配 | 合成 / 钟表店 |
| `memorial_watch` | 纪念怀表 | L | 1 | 21.8 彩蛋链奖励;小夜灯+特殊滴答音效 | 仅任务链八 |

## 43.3 神秘材料落 ID 对照(40 章图鉴 → 物品注册,84 件)

> 40 章按 A-F 六类给出材料学定义,本节落成注册 ID。命名规则:`<来源>_<形态>`。以下列出全部 84 条 ID(显示名见 lang 附件 `docs/items_lang_zh.csv`),按类归组:

- **A 生物掉落(28)**:`deepfish_marrow` 深海鱼脊髓 / `drowned_regret` 溺亡者残念 / `compass_bird_eye` 罗盘鸟眼 / `storm_essence` 风之精随 / `whalefall_ambergris` 鲸落龙涎 / `owl_visual_nerve` 夜枭视络 / `nightmare_mane` 噩梦马鬃 / `spirit_binding_fang` 缚灵之牙 / `medium_pupil` 灵媒之瞳 / `ancient_remains` 完整古骸 / `door_echo` 门扉残响 / `glowfly_heart` 圣光萤心 / `beast_gland` 斗兽腺体 / `boxer_knuckle` 拳师指骨 / `polyweapon_iron` 百兵通灵铁 / `echo_conch` 回声螺 / `sage_cerebrum` 万识脑髓 / `shadow_lizard_gland` 影蜥腺 / `provoker_tongue` 挑拨者之舌 / `witch_fingernail` 女巫指甲 / `pain_thorn_core` 苦痛之刺核 / `beast_king_marrow` 兽王毛髓 / `vampire_fang` 血族之牙 / `crimson_blood` 深红之血 / `wraith_residue` 怨念残渣 / `mirror_shard_alive` 活性镜片 / `chimera_embryo` 嵌合胚芽 / `abyss_ink` 深渊墨汁
- **B 事件/环境产物(16)**:`storm_seawater` 风暴海水 / `spirit_tide_dew` 灰雾凝露 / `moonlight_pearl` 月光珍珠 / `abyss_moss` 深渊水苔 / `limbo_mist_drop` 灵薄雾滴 / `spirit_realm_moss` 灵界苔 / `boundary_stone_dust` 界碑石屑 / `dawn_dew` 晨光凝露 / `noon_crystal` 无暗结晶 / `eternal_day_heart` 恒昼之心 / `bloodmoon_clot` 血月凝块 / `aurora_shard` 极光碎屑 / `fog_condensate` 浓雾凝液 / `star_echo_dust` 群星回响尘 / `quake_core` 地鸣核 / `mirage_glass` 蜃景玻璃
- **C 神秘作物(12)**:`sedative_pistil` 安眠花蕊 / `flesh_rose_bulb` 血肉蔷薇球茎 / `mercy_herb` 医者仁心草 / `fertile_heart` 沃土之心 / `plenty_wheat_king` 丰饶麦穗王 / `druid_acorn` 德鲁伊橡实 / `sun_wheel_flower` 日轮花 / `dream_poppy` 梦罂粟 / `whisper_moss_spore` 低语苔孢子 / `gallows_mushroom` 绞架蘑菇 / `moon_gourd` 月轮瓜 / `ghost_pepper_soul` 缚魂椒
- **D 加工中间体(14)**:`sea_salt_crystal` 海盐结晶 / `rum_base` 朗姆基液 / `holy_water` 圣水 / `silver_dust` 银粉 / `gilded_powder` 镀金粉 / `quench_oil` 淬火油 / `contract_vellum` 契约羊皮纸 / `memory_mercury` 记忆水银 / `sacred_oil` 圣油 / `embalming_oil` 防腐油 / `caffeine_crystal` 咖啡因结晶 / `logic_thread` 逻辑丝线 / `truth_crystal` 真相之眼水晶 / `thesis_core` 立论之核
- **E 结构/考古产物(8)**:`sextant_fragment` 六分仪碎片 / `saint_bone_dust` 圣徒遗骨粉 / `prism_seven` 七色棱镜 / `mono_ash` 隐修念珠芯 / `mint_die_blank` 誓约金印坯 / `shipwreck_bell` 沉船之钟 / `giant_petrified_breath` 群山之息矿屑 / `stargate_key_blank` 门扉钥匙坯料
- **F 特性衍生(6)**:`characteristic_shard_9~4` 非凡特性(六档,NBT 记途径与序列,见 6 章)

## 43.4 魔药与成品(12)

| ID | 显示名 | 说明 |
|----|--------|------|
| `mystery_potion` | 魔药(通用瓶) | NBT:pathway/sequence/quality;9.4 坩埚产物 |
| `failed_sludge` | 炼制失败淤浆 | 失败产物;可蒸馏回收 1 辅材 |
| `diluted_potion` | 稀释魔药 | 药师(71-75)玩法:效果 -50%、消化 +30% 版本 |
| `purification_draught` | 净化剂 | -10 污染,每日限 2 | 
| `sedative_tonic` | 镇定剂 | 压力 -15,有成瘾计数(≥5 次/周→反噬) |
| `spirit_tonic` | 灵性补剂 | 灵性恢复速度 ×2,持续 5min |
| `antidote_universal` | 万用解毒瓶 | 清除 C 类作物毒素 |
| `placebo_vial` | 安慰剂 | 黑市伪造魔药;喝下才知道(30% 伪造经济一环) |
| `characteristic_solution` | 特性溶液 | 特性入药中间态(6.3) |
| `ritual_catalyst` | 仪式催化瓶 | 仪式成功率 +8%,一次性 |
| `dream_draught` | 入梦药剂 | 强制进入梦境层(不眠者副本钥匙) |
| `blood_substitute` | 代血浆 | 血族之牙配方辅材;教会医院有售 |

## 43.5 仪式与文书(28)· 货币(9)· 杂项(25)

**文书类**:`recipe_scroll`(配方卷轴,NBT 记配方 ID+真伪种子)/ `recipe_fragment`(配方残页,2-3 合 1)/ `diary_page`(罗塞尔日记页 ×72,NBT 页码)/ `blasphemy_slate_shard`(亵渎石板残片 ×21)/ `commission_paper` 委托书 / `notarized_deed` 公证文书 / `forged_deed` 伪造文书 / `wanted_poster` 悬赏令 / `newspaper` 报纸(周刊,42 章生成器产物)/ `letter_sealed` 火漆信件 / `charm_paper` 纸符(仪式消耗)/ `honorific_note` 尊名笺(记录三句式)/ `prayer_slip` 祈愿签 / `church_permit` 教会通行证 / `black_market_token` 黑市信物 / `pawn_ticket` 当票 / `train_ticket` 车票(区段 NBT)/ `theater_ticket` 剧院票 / `university_diploma` 学位证书 / `press_card` 记者证 / `bounty_contract` 悬赏契约 / `seance_invitation` 会客邀请函 / `tarot_summon_note` 塔罗会召集令 / `apology_letter` 道歉信(扮演事件道具) / `blank_manuscript` 空白手稿 / `finished_thesis` 成书论文(秘术导师配方 60 主材来源)/ `map_fragment` 藏宝图残片 / `ip_easter_scroll` 「雾都异闻」制作组卷轴(彩蛋)。

**货币**:`coin_penny` 便士 / `coin_soli` 苏勒 / `coin_pound` 金镑(1 镑=20 苏勒=240 便士,D4)/ `note_pound_5` 五镑券 / `note_pound_20` 二十镑券 / `bank_cheque` 支票(NBT 面额+签名)/ `counterfeit_coin` 伪币(黑市,银行识别率 85%)/ `church_scrip` 教会代金券 / `grayfog_credit` 灰雾信用点(仅拍卖内流通,防现实倒卖)。

**杂项精选**:`gunpla_display`? ——否决,不入正典(风格违和);实际入册:`brass_goggles` 黄铜护目镜(装饰)/ `top_hat` 大礼帽 / `monocle` 单片眼镜 / `walking_cane` 手杖(可藏剑,合成)/ `music_box` 八音盒(播放 32 章动机乐句)/ `chess_set` 棋盘 / `absinthe_bottle` 苦艾酒 / `pipe_smoking` 烟斗 / `umbrella_black` 黑伞(缓降 20%)/ `candle_pack` 蜡烛组 / `oil_lamp` 油灯(手持光源,灵体不敌视,火把会)/ `camera_obscura` 暗箱相机(拍摄生成地图画)/ `gramophone` 留声机 / `perfume_vial` 香水(掩盖收尸人尸臭 debuff)/ `winter_coat` 冬大衣(雪原保暖)/ `sewing_kit` 针线包(修复布甲)/ `pocket_mirror` 随身镜(检查身后 = 反跟踪小玩法)/ `aurora_postcard` 极光明信片(21.8)/ `tarot_club_badge` 塔罗会徽章(席位凭证)等 25 件。

## 43.6 PMItems 注册类(完整代码)

```java
package top.aurora.projectmystery.registry;

import net.minecraft.world.item.*;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import top.aurora.projectmystery.ProjectMystery;
import top.aurora.projectmystery.item.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 物品统一注册(v0.8 · 43 章)。
 * 约定:
 *  1. 简单材料走 registerSimple(批量);带逻辑物品单独 register。
 *  2. 稀有度经由 PMRarity 常量集中管理,tooltip 由 PMItemBase 统一注入 lang key
 *     `pm.item.<id>.tip`(缺失 key 时不渲染,不报错)。
 *  3. 全部挂到创造标签 PMCreativeTabs(工具/材料/文书/货币四页签)。
 */
public final class PMItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ProjectMystery.MODID);

    /** 简单材料清册:ID → 稀有度(用于批量注册,84 种神秘材料 + 杂项)。 */
    private static final Map<String, Rarity> SIMPLE = new LinkedHashMap<>();
    static {
        // —— A 生物掉落(节选,全 28 条按 43.3 顺序登记)——
        SIMPLE.put("deepfish_marrow", Rarity.COMMON);
        SIMPLE.put("drowned_regret", Rarity.UNCOMMON);
        SIMPLE.put("compass_bird_eye", Rarity.UNCOMMON);
        SIMPLE.put("storm_essence", Rarity.RARE);
        SIMPLE.put("whalefall_ambergris", Rarity.RARE);
        SIMPLE.put("owl_visual_nerve", Rarity.COMMON);
        SIMPLE.put("nightmare_mane", Rarity.RARE);
        SIMPLE.put("spirit_binding_fang", Rarity.RARE);
        SIMPLE.put("medium_pupil", Rarity.RARE);
        SIMPLE.put("ancient_remains", Rarity.RARE);
        SIMPLE.put("door_echo", Rarity.EPIC);
        SIMPLE.put("glowfly_heart", Rarity.UNCOMMON);
        SIMPLE.put("beast_gland", Rarity.COMMON);
        SIMPLE.put("boxer_knuckle", Rarity.UNCOMMON);
        SIMPLE.put("polyweapon_iron", Rarity.RARE);
        SIMPLE.put("echo_conch", Rarity.UNCOMMON);
        SIMPLE.put("sage_cerebrum", Rarity.EPIC);
        // ……(其余 A 类 11 条、B 类 16 条、C 类 12 条、D 类 14 条、E 类 8 条
        //     按 43.3 完整清单同格式登记;CI 校验 SIMPLE.size()==78)
        // —— 杂项(节选)——
        SIMPLE.put("top_hat", Rarity.COMMON);
        SIMPLE.put("monocle", Rarity.COMMON);
        SIMPLE.put("aurora_postcard", Rarity.EPIC);
    }

    // —— 带逻辑物品(独立类)——
    public static final RegistryObject<Item> SPIRIT_PENDULUM =
            ITEMS.register("spirit_pendulum", () -> new SpiritPendulumItem(props(1, Rarity.COMMON)));
    public static final RegistryObject<Item> TAROT_DECK =
            ITEMS.register("tarot_deck", () -> new TarotDeckItem(props(1, Rarity.UNCOMMON).durability(78)));
    public static final RegistryObject<Item> RITUAL_CHALK =
            ITEMS.register("ritual_chalk", () -> new RitualChalkItem(props(64, Rarity.COMMON)));
    public static final RegistryObject<Item> MYSTERY_POTION =
            ITEMS.register("mystery_potion", () -> new MysteryPotionItem(props(1, Rarity.RARE)));
    public static final RegistryObject<Item> RECIPE_SCROLL =
            ITEMS.register("recipe_scroll", () -> new RecipeScrollItem(props(16, Rarity.UNCOMMON)));
    public static final RegistryObject<Item> RECIPE_FRAGMENT =
            ITEMS.register("recipe_fragment", () -> new RecipeFragmentItem(props(16, Rarity.UNCOMMON)));
    public static final RegistryObject<Item> DIARY_PAGE =
            ITEMS.register("diary_page", () -> new DiaryPageItem(props(16, Rarity.RARE)));
    public static final RegistryObject<Item> BLASPHEMY_SLATE_SHARD =
            ITEMS.register("blasphemy_slate_shard", () -> new SlateShardItem(props(1, Rarity.EPIC)));
    public static final RegistryObject<Item> NEWSPAPER =
            ITEMS.register("newspaper", () -> new NewspaperItem(props(16, Rarity.COMMON)));
    public static final RegistryObject<Item> COIN_PENNY =
            ITEMS.register("coin_penny", () -> new CurrencyItem(props(64, Rarity.COMMON), 1));
    public static final RegistryObject<Item> COIN_SOLI =
            ITEMS.register("coin_soli", () -> new CurrencyItem(props(64, Rarity.COMMON), 12));
    public static final RegistryObject<Item> COIN_POUND =
            ITEMS.register("coin_pound", () -> new CurrencyItem(props(64, Rarity.UNCOMMON), 240));
    public static final RegistryObject<Item> CHARACTERISTIC_SHARD =
            ITEMS.register("characteristic_shard", () -> new CharacteristicItem(props(1, Rarity.EPIC)));
    public static final RegistryObject<Item> SPIRIT_GOGGLES =
            ITEMS.register("spirit_goggles", () -> new SpiritGogglesItem(props(1, Rarity.RARE)));
    public static final RegistryObject<Item> GUIDE_JOURNAL =
            ITEMS.register("guide_journal", () -> new GuideJournalItem(props(1, Rarity.COMMON))); // 51 章

    /** 批量注册简单材料。 */
    public static void bootstrapSimple() {
        SIMPLE.forEach((id, rarity) ->
                ITEMS.register(id, () -> new PMItemBase(props(64, rarity))));
    }

    private static Item.Properties props(int stack, Rarity rarity) {
        return new Item.Properties().stacksTo(stack).rarity(rarity);
    }

    private PMItems() {}
}
```

> **落库指引**:`bootstrapSimple()` 必须在主类构造器中、`ITEMS.register(modEventBus)` **之前**调用(DeferredRegister 惰性求值,注册顺序即清册顺序)。CI 增加一条校验:`SIMPLE` 清册与 `docs/items_lang_zh.csv` 行数一致,防止 lang 漏配。

---

# 44. 合成配方附表(v0.8 新增 · 全量)

> **本章定位**:所有可合成物品的配方唯一清单。三种载体:①原版工作台(`data/pm/recipes/*.json`,可被 JEI 直读);②坩埚魔药(自定义 `data/pm/potion_recipes/*.json`,JEI 插件桥接展示,但**知识系统接管解锁**——未验证配方在 JEI 中只显示剪影);③仪式产物(不进 JEI,只进知识手册)。

## 44.1 工作台配方总表(60 条)

> 格式:`产物 ×数量 ← 配方(S=shaped 有序 / SL=shapeless 无序)`。材料用注册 ID;`#` 开头为标签(45.7)。

**占卜与仪式器具(14)**
1. `spirit_pendulum` ← S:线(顶)+ 铁锭(中)+ 紫水晶碎片(底)
2. `dowsing_rod` ← S:V 形 2 木棍 + 1 `silver_dust`
3. `tarot_deck` ← SL:22× 纸 + 1× `abyss_ink` + 1× 皮革
4. `dream_incense` ×4 ← SL:`dream_poppy` + 甘蔗 + 线
5. `numerology_slate` ← S:3 石板(顶行)+ `silver_dust`(中心)+ 2 木棍(两侧)
6. `paper_figurine` ×8 ← SL:4× 纸 + 1× 红色染料
7. `ritual_dagger` ← S:银锭(顶)+ 银锭(中)+ 木棍(底)【银锭 = `#forge:ingots/silver`,无银 Mod 时配方文件条件回退为铁锭+`silver_dust`】
8. `ritual_chalk` ×6 ← SL:2× 骨粉 + 1× 白色染料 + 1× 黏土球
9. `silver_censer` ← S:银锭 ×3(底行)+ 铁栏杆(中心)+ 链(顶)
10. `crucible_stirrer` ← S:木棍 ×2 斜排 + 铁粒(顶端)
11. `thermometer_brass` ← S:玻璃板(顶)+ 红石(中)+ 铜锭(底)
12. `magnifier` ← S:玻璃板(中心)+ 铁粒环 ×4 + 木棍(右下)
13. `sealing_wax_kit` ×4 ← SL:蜜脾 + 红色染料 + 纸
14. `wax_earplugs` ×2 ← SL:蜜脾 + 线

**生活与杂项(18)**:`pocket_watch`(金锭×4 环+红石中心)/ `walking_cane`(木棍×2+铁粒顶)/ `umbrella_black`(黑羊毛×3 顶行+木棍中柱×2)/ `oil_lamp`(玻璃+铁锭+线+`sacred_oil` 回退为煤)/ `candle_pack` ×3(蜜脾+线)/ `music_box`(木板×4+金粒×4+红石中心)/ `camera_obscura`(木板环+玻璃板+`silver_dust`)/ `sewing_kit`(铁粒+线×2+皮革)/ `pocket_mirror`(玻璃板+`silver_dust`+木板)/ `winter_coat`(皮革×6+羊毛×2+线)/ `top_hat`(黑羊毛×5 帽形)/ `monocle`(玻璃板+金粒环)/ `perfume_vial`(玻璃瓶+任意花×2+`dawn_dew`)/ `chess_set`(黑白羊毛+木板)/ `gramophone`(木板×4+铁锭+音符盒芯)/ `brass_goggles`(铜锭×2+玻璃板×2+皮革)/ `pipe_smoking`(木板+木棍)/ `absinthe_bottle`(玻璃瓶+`mercy_herb`+小麦——酿造玩法入口,饮后压力 -5 醉酒 60s)。

**加工中间体(D 类 14 条全部可合成/可炼)**:`sea_salt_crystal`(水瓶+营火慢烤,营火配方)/ `rum_base`(甘蔗×3+水瓶,SL)/ `holy_water`(水瓶+`saint_bone_dust`,教堂祭坛右键替代路线)/ `silver_dust` ×2(银锭研磨,切石机)/ `gilded_powder`(金锭研磨,切石机)/ `quench_oil`(桶装水+煤×2+`beast_gland`)/ `contract_vellum`(皮革切石)/ `memory_mercury`(`mirror_shard_alive`+水瓶+`silver_dust`)/ `sacred_oil`(`holy_water`+蜜脾)/ `embalming_oil`(`quench_oil`+`sea_salt_crystal`+线)/ `caffeine_crystal`(可可豆×4 切石)/ `logic_thread`(线×3+`caffeine_crystal`)/ `truth_crystal` 不可合成(悬案链奖励,防经济破坏)/ `thesis_core` 不可合成(立论玩法产出)。

**文书与货币(14)**:`blank_manuscript` ×3(纸×3+皮革)/ `charm_paper` ×4(纸+朱砂=红染料+`silver_dust`)/ `honorific_note`(纸+`abyss_ink`)/ `prayer_slip` ×8(纸+`gilded_powder`)/ `letter_sealed`(纸+`sealing_wax_kit`)/ 货币兑换走银行 NPC 与灰雾兑换器,不走工作台(防刷);`counterfeit_coin` 走黑市专用"私铸台"方块(罪值 +2/次)。

## 44.2 工作台配方 JSON 全格式样例(可直接落库)

`data/pm/recipes/spirit_pendulum.json`(shaped 完整文件):

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "key": {
    "S": { "item": "minecraft:string" },
    "I": { "item": "minecraft:iron_ingot" },
    "A": { "item": "minecraft:amethyst_shard" }
  },
  "pattern": [
    " S ",
    " I ",
    " A "
  ],
  "result": { "item": "pm:spirit_pendulum", "count": 1 },
  "show_notification": true
}
```

`data/pm/recipes/tarot_deck.json`(shapeless 完整文件,含条件回退):

```json
{
  "type": "minecraft:crafting_shapeless",
  "category": "misc",
  "ingredients": [
    { "item": "minecraft:paper" }, { "item": "minecraft:paper" },
    { "item": "minecraft:paper" }, { "item": "minecraft:paper" },
    { "item": "pm:abyss_ink" },
    { "item": "minecraft:leather" }
  ],
  "result": { "item": "pm:tarot_deck", "count": 1 }
}
```

`data/pm/recipes/ritual_dagger.json`(带 Forge 条件回退的完整文件——有银则用银,无银用铁+银粉):

```json
{
  "type": "forge:conditional",
  "recipes": [
    {
      "conditions": [ { "type": "forge:not", "value": { "type": "forge:tag_empty", "tag": "forge:ingots/silver" } } ],
      "recipe": {
        "type": "minecraft:crafting_shaped",
        "key": { "S": { "tag": "forge:ingots/silver" }, "T": { "item": "minecraft:stick" } },
        "pattern": [ "S", "S", "T" ],
        "result": { "item": "pm:ritual_dagger" }
      }
    },
    {
      "conditions": [ { "type": "forge:tag_empty", "tag": "forge:ingots/silver" } ],
      "recipe": {
        "type": "minecraft:crafting_shapeless",
        "ingredients": [
          { "item": "minecraft:iron_ingot" }, { "item": "minecraft:iron_ingot" },
          { "item": "pm:silver_dust" }, { "item": "minecraft:stick" }
        ],
        "result": { "item": "pm:ritual_dagger" }
      }
    }
  ]
}
```

## 44.3 坩埚魔药配方数据包格式(全量落库规范)

75 条配方(9.2/9.6)全部落为 `data/pm/potion_recipes/<pathway>_<seq>.json`。完整文件样例(占卜家 · 序列 9「占卜家」):

```json
{
  "id": "pm:seer_9",
  "pathway": "pm:seer",
  "sequence": 9,
  "main_ingredient": { "item": "pm:medium_pupil_lesser", "consume": 1 },
  "aux_ingredients": [
    { "item": "pm:silver_dust", "count": 2 },
    { "item": "minecraft:fermented_spider_eye", "count": 1 },
    { "item": "pm:holy_water", "count": 1 }
  ],
  "liquid_base": "minecraft:water",
  "heat_profile": {
    "mode": "alternating",
    "target_celsius": 88,
    "tolerance": 6,
    "phase_seconds": [40, 25, 40],
    "phases": ["gentle", "fierce", "gentle"]
  },
  "brew_seconds": 105,
  "ritual_required": false,
  "failure": { "pollution_cloud_radius": 3, "sludge_count": 1 },
  "unlock": { "knowledge_id": "pm:recipe_seer_9", "verified_required": true }
}
```

> **序列 ≤5 配方**追加字段 `"ritual_required": true, "ritual_id": "pm:ritual/advance_<pathway>_5"`,坩埚产物为「未激活魔药」,需在仪式窗口内服用(12.3 状态机 `POTION_WINDOW`)。

## 44.4 配方汇总 CSV 规范(docs/recipes_master.csv)

列:`recipe_id, carrier(bench/crucible/ritual/campfire/stonecutter), result_id, result_count, ingredients(分号分隔 id*count), unlock(free/knowledge/quest), jei_visible(bool), notes`。CI 校验:①每个注册物品至少出现在一条配方或一个 loot 表;②`unlock=knowledge` 的配方必须存在对应 `knowledge_id`;③坩埚配方主材必须在 43.3 清册内。

## 44.5 营火/切石/锻造扩展位

- **营火**:`sea_salt_crystal`(水瓶 300s)/ 烤 `deepfish_marrow`→ 失去药用(新手教训点,tooltip 警告)。
- **切石机**:研磨类中间体统一走切石(见 44.1),体验定位"雾都药剂师的研钵"。
- **锻造台**:`walking_cane` + `ritual_dagger` → `cane_sword` 藏剑手杖(攻击 6,潜行时外观不可辨识 = 猎巫夜隐蔽携带玩法)。

---

# 45. 数据包全量文件规范与整仓样例(v0.8 新增)

> **本章定位**:`data/pm/` 的完整目录树 + 13 类自定义数据文件的**完整样例**(每类一份可直接落库的全文件,其余同格式批量生成)。全部经 `SimpleJsonResourceReloadListener` 加载,`/reload` 热更新;每类都有 Codec 校验,格式错误在日志输出**具体路径+字段**而非静默吞掉。

## 45.1 目录树全景

```
data/pm/
├─ recipes/                    # 44.1 工作台/营火/切石/锻造 60+ 文件
├─ potion_recipes/             # 44.3 坩埚 75 文件(seer_9.json ... pharmacist_5.json)
├─ pathways/                   # 22 文件(7.10 Codec)
├─ sequences/                  # 22×6=132 文件(可玩途径)+ 占位
├─ abilities/                  # 48 章 150+ 文件
├─ acting_events/              # 8.3,每途径 8-12 条,共 ~150 文件
├─ rituals/                    # 12.4 18 种 + 晋升仪式 15 条
├─ honorifics/                 # 尊名三句式组合,~40 文件
├─ artifacts/                  # 13 章 72 文件(0/1/2/3 级子目录)
├─ knowledge/                  # 14.3 知识条目 ~300 文件
├─ diary_pages/                # 72 文件
├─ slate_shards/               # 21 文件(14.4)
├─ whispers/                   # 呓语池,按途径+通用分 16 文件
├─ world_events/               # 25 文件(18 章)
├─ commissions/                # 委托模板 ~60 文件(17.2/42)
├─ quests/                     # 任务链 8 目录(52.3 结构)
├─ loot_modifiers/             # 40.2 GLM 声明,~35 文件 + global_loot_modifiers.json
├─ advancements/pm/            # 60 成就 + 教学链 12(51 章)
├─ tags/items/                 # 45.7 材料标签
├─ worldgen/
│  ├─ structure/               # 19 章 14 结构 nbt 引用
│  ├─ structure_set/
│  ├─ template_pool/
│  ├─ processor_list/
│  └─ biome_modifier/          # 生物生成注入(49 章)
└─ damage_type/                # 灵性伤害/污染反噬/仪式灾变 3 种
```

## 45.2 pathway 完整文件(`data/pm/pathways/seer.json`)

```json
{
  "id": "pm:seer",
  "group": "fool",
  "adjacent": ["pm:marauder", "pm:apprentice"],
  "batch": 1,
  "playable_to_sequence": 4,
  "color": "#8A7CC2",
  "icon": "pm:textures/pathway/seer.png",
  "acting_principle_key": "pm.pathway.seer.principle",
  "sequences": [
    "pm:seer_9", "pm:seer_8", "pm:seer_7",
    "pm:seer_6", "pm:seer_5", "pm:seer_4"
  ],
  "lost_control_entity": "pm:lost_seer",
  "mythical_form": { "entity": "pm:myth_form_seer", "trigger_sequence": 4 }
}
```

## 45.3 sequence 完整文件(`data/pm/sequences/seer_9.json`)

```json
{
  "id": "pm:seer_9",
  "pathway": "pm:seer",
  "sequence": 9,
  "name_key": "pm.seq.seer_9",
  "spirituality_bonus": 20,
  "sanity_cap_bonus": 0,
  "passive_abilities": ["pm:spirit_vision_basic", "pm:danger_intuition"],
  "active_abilities": ["pm:pendulum_divination", "pm:dowsing"],
  "acting_events_pool": "pm:acting/seer_9",
  "digestion_hours_baseline": 6,
  "advance_requires": { "digestion": 1.0, "potion": "pm:seer_8", "ritual": null }
}
```

## 45.4 ability 完整文件(`data/pm/abilities/flame_leap.json`,对应 27.3 执行器)

```json
{
  "id": "pm:flame_leap",
  "type": "active",
  "executor": "pm:flame_leap",
  "cost": { "spirituality": 12 },
  "cooldown_ticks": 160,
  "params": { "power": 1.15, "ignite_seconds": 2, "landing_damage": 4.0 },
  "particles": { "cast": "pm:ember_burst", "trail": "minecraft:flame" },
  "sound": "pm:ability.flame_leap",
  "unlock_hint_key": "pm.ability.flame_leap.hint",
  "upgrade": { "per_sequence_power": 0.1, "max_sequence_scaling": 5 }
}
```

## 45.5 ritual / honorific / artifact / diary_page / whisper 完整文件

`data/pm/rituals/advance_seer_5.json`(晋升仪式):

```json
{
  "id": "pm:advance_seer_5",
  "pattern": "pm:patterns/circle_seven_candles",
  "materials": [
    { "item": "pm:silver_censer", "consume": false },
    { "item": "pm:holy_water", "count": 2 },
    { "item": "pm:whisper_moss_spore", "count": 1 },
    { "item": "pm:charm_paper", "count": 4 }
  ],
  "environment": { "time": "night", "moon_phase_any": [0, 4], "roofed": true, "min_y": -64, "max_y": 100 },
  "honorific_target": "pm:honorifics/fool_veiled",
  "potion_window_seconds": 60,
  "outcomes": {
    "critical_success": { "weight_base": 10, "effects": ["pm:advance", "pm:pollution:-5"] },
    "success":          { "weight_base": 55, "effects": ["pm:advance"] },
    "flawed":           { "weight_base": 20, "effects": ["pm:advance", "pm:pollution:+10", "pm:whisper_burst"] },
    "failure":          { "weight_base": 12, "effects": ["pm:refund_none", "pm:sanity:-15"] },
    "catastrophe":      { "weight_base": 3,  "effects": ["pm:summon:pm:wraith_elite", "pm:pollution:+25"] }
  },
  "multiplayer_bonus": { "per_participant": 0.05, "cap": 0.20 }
}
```

`data/pm/honorifics/fool_veiled.json`:

```json
{
  "id": "pm:fool_veiled",
  "lines": ["pm.honorific.fool.1", "pm.honorific.fool.2", "pm.honorific.fool.3"],
  "strictness": "exact_order",
  "wrong_call_catastrophe": "pm:events/honorific_backlash",
  "player_registrable": true,
  "register_requires": { "grayfog_seat": true, "sequence_max": 6 }
}
```

`data/pm/artifacts/2/2_081_reverse_watch.json`(逆走怀表,13.6):

```json
{
  "id": "pm:relic_2_081",
  "grade": 2,
  "active": { "executor": "pm:rewind_self", "params": { "seconds": 5 }, "daily_uses": 1 },
  "curse": { "executor": "pm:time_scramble_ui", "params": { "daily_random_hours": 1 } },
  "containment": { "storage": "silver_box", "carry_pollution_per_day": 1 },
  "rental": null,
  "resonance_weight": 2,
  "source": "pm:structures/clockmaker_manor"
}
```

`data/pm/diary_pages/page_17.json`:

```json
{
  "id": "pm:diary_17",
  "page_no": 17,
  "encrypted": true,
  "cipher": "hermes_substitution",
  "text_key": "pm.diary.17.body",
  "hard_info": [ { "type": "honorific_line", "ref": "pm:fool_veiled#2" } ],
  "fake": false,
  "loot_hint": ["pm:structures/collector_manor", "grayfog_auction"]
}
```

`data/pm/whispers/seer.json`(池文件,加权文案):

```json
{
  "pool": "pm:whispers/seer",
  "entries": [
    { "key": "pm.whisper.seer.1", "weight": 10, "min_pollution": 0 },
    { "key": "pm.whisper.seer.2", "weight": 8,  "min_pollution": 20 },
    { "key": "pm.whisper.seer.3", "weight": 5,  "min_pollution": 40, "sanity_damage": 2 },
    { "key": "pm.whisper.generic.9", "weight": 6, "min_pollution": 0 }
  ]
}
```

## 45.6 world_event / commission / advancement / loot_modifier 完整文件

`data/pm/world_events/blood_moon.json`:

```json
{
  "id": "pm:blood_moon",
  "triggers": [ { "type": "random_night", "chance": 0.05 }, { "type": "command" } ],
  "duration_ticks": 12000,
  "mutex_group": "night_major",
  "callbacks": { "start": "pm:cb/bloodmoon_start", "tick": "pm:cb/bloodmoon_tick", "end": "pm:cb/bloodmoon_end" },
  "effects": { "spawn_mult": { "pm:wraith": 3.0 }, "pollution_ambient_per_min": 1, "sky_color": "#7A1020" },
  "loot_bonus_tag": "pm:bloodmoon_bonus",
  "broadcast_key": "pm.event.blood_moon.start"
}
```

`data/pm/commissions/missing_person.json`(委托模板,参数域随机化):

```json
{
  "id": "pm:commission/missing_person",
  "board": ["detective_agency", "church_notice"],
  "level_range": [1, 3],
  "params": {
    "npc_name": { "pool": "pm:namepool/citizen" },
    "district": { "pool": "pm:districts" },
    "twist": { "weighted": { "mundane": 50, "beyonder_involved": 30, "cult": 20 } }
  },
  "solutions": ["divination", "witness_interview", "trace_highlight"],
  "reward": { "pounds": [1, 4], "reputation": { "detective_agency": 5 }, "bonus_if_no_kill": { "pounds": 1 } },
  "cooldown_hours": 20
}
```

`data/pm/advancements/pm/first_potion.json`(完整成就文件):

```json
{
  "parent": "pm:root",
  "display": {
    "icon": { "item": "pm:mystery_potion" },
    "title": { "translate": "pm.adv.first_potion.title" },
    "description": { "translate": "pm.adv.first_potion.desc" },
    "frame": "task", "show_toast": true, "announce_to_chat": true, "hidden": false
  },
  "criteria": { "brewed": { "trigger": "pm:potion_brewed", "conditions": { "quality_min": "flawed" } } },
  "rewards": { "experience": 50 }
}
```

`data/pm/loot_modifiers/deepfish_marrow.json` + 总册(40.2 GLM 声明落库):

```json
{
  "type": "pm:conditional_mystic_drop",
  "conditions": [ { "condition": "minecraft:random_chance", "chance": 0.08 } ],
  "target_entity_tag": "pm:deep_fish",
  "drop": { "item": "pm:deepfish_marrow", "count_range": [1, 1] },
  "requirements": { "killer_min_sequence": 9, "moon_phase_any": null, "dimension": "minecraft:overworld" },
  "luck_scaling": 0.01
}
```

`data/pm/loot_modifiers/global_loot_modifiers.json`:

```json
{ "replace": false, "entries": [
  "pm:deepfish_marrow", "pm:owl_visual_nerve", "pm:beast_gland",
  "pm:drowned_regret", "pm:nightmare_mane", "pm:whalefall_ambergris"
] }
```

## 45.7 标签全集(`data/pm/tags/items/`)

`mystic_material_a.json`(生物掉落类)/ `mystic_material_b.json` … `_f.json` 六类;`crucible_main.json`(可作主材)/ `crucible_aux.json`;`relic_grade_0~3.json`;`currency.json`;`deep_fish.json`(实体标签,GLM 用);`bloodmoon_bonus.json`。样例:

```json
{ "replace": false, "values": [
  "pm:deepfish_marrow", "pm:owl_visual_nerve", "pm:beast_gland", "pm:boxer_knuckle",
  { "id": "pm:compass_bird_eye", "required": false }
] }
```

## 45.8 damage_type 与 worldgen 注入

`data/pm/damage_type/spiritual.json`:

```json
{ "message_id": "pm.spiritual", "scaling": "never", "exhaustion": 0.0, "effects": "freezing" }
```

`data/pm/worldgen/biome_modifier/spawn_compass_bird.json`(罗盘鸟注入海岛群系):

```json
{
  "type": "forge:add_spawns",
  "biomes": "#minecraft:is_beach",
  "spawners": { "type": "pm:compass_bird", "weight": 8, "minCount": 1, "maxCount": 2 }
}
```

> **交付账**:数据包全量 ≈ **1,100+ JSON 文件**。生成策略:以本章 13 类完整样例为模板,`scripts/gen_datapack.py`(仓库内 Python 脚本)从 `docs/*.csv` 主表批量渲染(Jinja2),**人手只维护 CSV 与样例,JSON 永远是构建产物**——这是 v0.8 最重要的工程决策,写入 38 章决策记录 D9。

---

# 46. GeckoLib 模型清单全册(v0.8 新增 · 21 → 49 模型)

> 20.4 只覆盖首发+EP2。本章补全**第三批/第四批途径、全部新生物与 Boss**,并给出骨骼树规范与 `.geo.json`/`.animation.json` 完整样例。规格约束不变:≤4k 面 / 骨骼 ≤40 / 贴图 ≤256²;Boss 与 0 级具象放宽到 ≤8k 面 / 512² 贴图(单场景仅 1 只)。

## 46.1 模型总账(49 个)

| 组 | 数量 | 清单 |
|----|------|------|
| 首发神话形态(已有) | 5 | 诡法师/操纵师/铁血骑士/寄生者/秘法师 |
| EP2 神话形态(已有) | 4 | 海洋歌者上位相/灵巫上位相/看门人上位相/光之祭司上位相 |
| **第三批神话形态(新增)** | 6 | 见 46.2 |
| **第三批失控体(新增)** | 6 | 见 46.2 |
| **第四批概念稿(新增)** | 7 | 见 46.3(EP4 才建模,先出三视图) |
| 首发失控体(已有) | 5 | — |
| 变形怪/阶段 Boss(已有) | 3 | 变形怪/玫瑰之影/伪誓者 |
| **新生物(新增)** | 12 | 罗盘鸟/光萤群/梦魇兽/知识妖/摆渡人/神孽(幼)/风暴之种核心/灯塔看守人/镜中人/欲望使徒/迷雾牧场羊群体/伪币铸师 |
| 0 级具象(已有) | 4 | — |
| **合计** | **49** | 动画状态机统一:idle/walk/attack1/attack2/skill/rage/death(+特殊态见备注) |

## 46.2 第三批六途径:神话形态 + 失控体 双清单

| 途径 | 神话形态(序列 4 预置,EP3 实装序列 5 时先出失控体) | 骨骼要点 | 失控体 | 失控体行为动画特殊态 |
|------|------------------------------------------------|---------|--------|--------------------|
| 战士(→黑皇帝支线组) | `myth_form_warrior` 百战余烬:半透明战甲叠影,背后浮空 6 柄武器环 | 武器环独立 6 骨骼,attack2 = 武器轮转斩 | `lost_warrior` 暴走斗兽:肌肉撕裂外露、拖行断刃 | `berserk`(狂暴变速)、`weapon_pickup`(拾取地面武器换 attack 模组) |
| 秘祈人(→黑夜) | `myth_form_secret_suppliant` 低语檐影:兜帽内无脸,周身 8 条祷文飘带 | 飘带用 GeckoLib bone 链式惯性;skill = 飘带缠绕禁锢 | `lost_suppliant` 聋祷者:双耳位置生出喇叭状肉质增生 | `listen`(定身侦听,玩家发声=锁定)、`silence_field` |
| 阅读者(→黑皇帝) | `myth_form_reader` 万卷回廊:躯干为翻页书堆叠,眼镜悬浮 | 书页 4 组循环 UV 动画;skill = 掷出光字锁链 | `lost_reader` 蠹智者:头部膨大、书页嵌入皮肤 | `analyze`(读玩家=复制其最近 1 个主动技)、`page_storm` |
| 刺客(→死神支线) | `myth_form_assassin` 无影裁决:身体边缘持续溶入阴影粒子 | 半透明渲染层+影分身 1 具(共享动画控制器) | `lost_assassin` 缝影怪:影子与本体错位 0.5s | `shadow_swap`(与影子换位)、`backstab` |
| 耕种者(→母树) | `myth_form_cultivator` 荒岁枝冠:下半身根系、肩生果枝,随季节换贴图 ×4 | 季节贴图热切换(动画控制器变量) | `lost_cultivator` 增殖藤傀:藤蔓不断从关节涌出 | `root_grasp`(地面藤蔓 AoE)、`bloom_burst` |
| 药师(→母树) | `myth_form_pharmacist` 千剂垂囊:悬浮药瓶轨道 12 个,躯干半液态 | 药瓶轨道 = 环形 bone 阵列,skill = 随机药雨(buff/debuff 混合) | `lost_pharmacist` 淤药漏体:体表不断渗出彩色淤浆 | `potion_lob`(抛射失败淤浆)、`self_dose`(自疗+属性随机化) |

## 46.3 第四批七途径(概念稿规格)

律师(言灵锁链缠身的法袍虚影)/ 仲裁人(天平为首、双面法冠)/ 罪犯(欲望烟雾具象、七宗徽记轮转)/ 囚犯(拖拽星空镣铐、身后铁窗光门)/ 窥秘人(独眼星轨环)/ 通识者(棱镜多面体头颅)/ 怪物(命运纺线缠绕的不定形)。每个交付:三视图 + 配色板 + 关键帧草稿 3 张,入 `docs/concepts/ep4/`;**建模留到 EP4,避免美术资源提前折旧**。

## 46.4 骨骼树规范(以 `myth_form_warrior` 为例)

```
root
├─ body ─ chest ─ head(眼部发光层 emissive)
│        ├─ arm_l ─ forearm_l ─ hand_l
│        └─ arm_r ─ forearm_r ─ hand_r
├─ leg_l / leg_r
└─ weapon_ring(独立旋转组)
   ├─ w1 … w6(六柄武器,各 1 骨,idle 缓转 4°/t,attack2 时序抽出)
```

命名硬约束:全小写下划线;左右后缀 `_l/_r`;发光层骨骼以 `glow_` 前缀(渲染器按前缀走 emissive RenderType);**骨骼名 = 动画通道名**,CI 用 `scripts/validate_geo.py` 校验 geo 与 animation 的骨骼集合一致。

## 46.5 `.geo.json` 完整样例(简化演示模型 · 罗盘鸟)

`assets/pm/geo/compass_bird.geo.json`:

```json
{
  "format_version": "1.12.0",
  "minecraft:geometry": [
    {
      "description": {
        "identifier": "geometry.pm.compass_bird",
        "texture_width": 64, "texture_height": 64,
        "visible_bounds_width": 2, "visible_bounds_height": 1.5, "visible_bounds_offset": [0, 0.5, 0]
      },
      "bones": [
        { "name": "root", "pivot": [0, 0, 0] },
        { "name": "body", "parent": "root", "pivot": [0, 6, 0],
          "cubes": [ { "origin": [-3, 4, -4], "size": [6, 5, 8], "uv": [0, 0] } ] },
        { "name": "head", "parent": "body", "pivot": [0, 9, -4],
          "cubes": [ { "origin": [-2, 8, -8], "size": [4, 4, 4], "uv": [28, 0] } ] },
        { "name": "glow_eye_compass", "parent": "head", "pivot": [0, 10, -8],
          "cubes": [ { "origin": [-1, 9, -8.2], "size": [2, 2, 0], "uv": [44, 0] } ] },
        { "name": "wing_l", "parent": "body", "pivot": [3, 8, 0],
          "cubes": [ { "origin": [3, 3, -3], "size": [1, 5, 7], "uv": [0, 13] } ] },
        { "name": "wing_r", "parent": "body", "pivot": [-3, 8, 0], "mirror": true,
          "cubes": [ { "origin": [-4, 3, -3], "size": [1, 5, 7], "uv": [0, 13] } ] },
        { "name": "tail", "parent": "body", "pivot": [0, 6, 4],
          "cubes": [ { "origin": [-2, 5, 4], "size": [4, 2, 5], "uv": [22, 13] } ] }
      ]
    }
  ]
}
```

## 46.6 `.animation.json` 完整样例

`assets/pm/animations/compass_bird.animation.json`:

```json
{
  "format_version": "1.8.0",
  "animations": {
    "animation.pm.compass_bird.idle": {
      "loop": true, "animation_length": 2.0,
      "bones": {
        "wing_l": { "rotation": { "0.0": [0, 0, 0], "1.0": [0, 0, -12], "2.0": [0, 0, 0] } },
        "wing_r": { "rotation": { "0.0": [0, 0, 0], "1.0": [0, 0, 12], "2.0": [0, 0, 0] } },
        "glow_eye_compass": { "rotation": { "0.0": [0, 0, 0], "2.0": [0, 0, 360] } }
      }
    },
    "animation.pm.compass_bird.fly": {
      "loop": true, "animation_length": 0.6,
      "bones": {
        "wing_l": { "rotation": { "0.0": [0, 0, 35], "0.3": [0, 0, -45], "0.6": [0, 0, 35] } },
        "wing_r": { "rotation": { "0.0": [0, 0, -35], "0.3": [0, 0, 45], "0.6": [0, 0, -35] } },
        "tail":   { "rotation": { "0.0": [10, 0, 0], "0.3": [-5, 0, 0], "0.6": [10, 0, 0] } }
      }
    },
    "animation.pm.compass_bird.point": {
      "loop": false, "animation_length": 1.2,
      "bones": { "head": { "rotation": { "0.0": [0, 0, 0], "0.4": [0, "q.pm_point_yaw", 0], "1.2": [0, "q.pm_point_yaw", 0] } } }
    }
  }
}
```

> `q.pm_point_yaw` 为自定义 Molang 查询:罗盘鸟被驯服后 `point` 动画朝向最近未探索结构(49.2 玩法),客户端 GeoAnimatable 侧注入查询值。

## 46.7 美术生产排期与资产账

| 里程碑 | 交付 | 工时估算(单人美术) |
|--------|------|-------------------|
| M3(EP2) | EP2 4 神话形态精修 + 新生物 6(罗盘鸟/光萤群/梦魇兽/摆渡人/知识妖/灯塔看守人) | 6 周 |
| M4(EP3) | 第三批 6 失控体 + 6 神话形态 + 新生物 6 | 10 周 |
| M5(EP4) | 第四批 7 建模 + 0 级具象精修 | 8 周 |

风险对策(挂 37 章):美术是唯一单点,启用**社区共创通道**——`docs/model_style_guide.pdf` + 提交模板,验收标准即 46.4 规范;通过者进 credits 与 21.8 彩蛋名单。

---

# 47. 粒子特效总表(v0.8 新增 · 34 种 + 完整代码)

> 原则:①粒子是**信息**不是装饰——玩家应能从粒子读出状态(污染高低/仪式阶段/火候文武);②服务器只广播 `ParticleOptions`,采样与插值全在客户端;③密度全部走 `pm-client.toml` 三档(low/standard/rich)。

## 47.1 粒子注册总表

| 注册 ID | 视觉 | 用途点位 | 密度档差异 |
|---------|------|---------|-----------|
| `spirit_wisp` | 淡青拖尾光点 | 灵视下灵体轮廓、灵性恢复 | low 减半 |
| `pollution_mote` | 暗紫下沉尘 | 污染 ≥40 常驻身周;失败药云 | low 仅药云 |
| `sanity_crack` | 屏幕边缘裂纹(GUI 粒子) | 理智 <30 | 不可关(信息粒子) |
| `ritual_rune` | 金色符文浮升 | 仪式阶段推进,每阶段颜色偏移 | — |
| `ritual_smoke_column` | 垂直烟柱 | 仪式成功(白)/灾变(黑红) | — |
| `honorific_echo` | 同心圆声波环 | 尊名念诵每句判定通过 | — |
| `crucible_gentle` | 细密小气泡 | 文火 | rich 加雾面 |
| `crucible_fierce` | 大气泡+溅射 | 武火 | — |
| `crucible_overheat` | 红色蒸汽 | 超温预警(测温计之外的免费信息) | 不可关 |
| `ember_burst` | 火星爆发 | 火焰跳跃等火系能力 | — |
| `shadow_flicker` | 黑色残影碎片 | 刺客系能力、缝影怪 | — |
| `page_glyph` | 漂浮字符 | 阅读者系、知识妖 | — |
| `divination_thread` | 银色丝线指向 | 占卜结果方向指引(渐隐 10s) | 不可关 |
| `dream_bubble` | 虹彩泡 | 梦境层环境 | rich 折射 |
| `grayfog_veil` | 灰白涡旋 | 灰雾出入、匿名化过场 | — |
| `blood_moon_ash` | 红黑飘灰 | 血月全屏环境 | low 减 70% |
| `aurora_ribbon` | 极光带(天空渲染辅助) | 极光事件、21.8 终点 | rich 双层 |
| `spirit_tide_wave` | 地表青雾漫延 | 灵界潮汐 | — |
| `characteristic_glow` | 特性掉落金脉光 | 非凡特性未拾取时 | 不可关 |
| `wanted_stamp` | 红色封蜡印(GUI) | 通缉状态获得 | — |
| 其余 14 种 | `candle_soul`/`incense_trail`/`mirror_ripple`/`storm_core_spark`/`root_burst`/`potion_rain`/`silence_field_edge`/`listen_pulse`/`weapon_ring_trace`/`petal_rose_shadow`/`counterfeit_glint`/`train_steam`/`newspaper_flip`/`seat_anoint`(灰雾就座) | 对应 46 章模型技能与 18 章事件 | 按组继承 |

## 47.2 PMParticles 注册 + 自定义粒子完整代码

```java
package top.aurora.projectmystery.registry;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import top.aurora.projectmystery.ProjectMystery;

public final class PMParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, ProjectMystery.MODID);

    public static final RegistryObject<SimpleParticleType> SPIRIT_WISP =
            PARTICLES.register("spirit_wisp", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> POLLUTION_MOTE =
            PARTICLES.register("pollution_mote", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> RITUAL_RUNE =
            PARTICLES.register("ritual_rune", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> DIVINATION_THREAD =
            PARTICLES.register("divination_thread", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> CRUCIBLE_GENTLE =
            PARTICLES.register("crucible_gentle", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> CRUCIBLE_FIERCE =
            PARTICLES.register("crucible_fierce", () -> new SimpleParticleType(false));
    // ……其余 28 种同格式注册,清册顺序与 47.1 一致

    private PMParticles() {}
}
```

客户端粒子类(灵性光点,含拖尾与呼吸透明度,完整实现):

```java
package top.aurora.projectmystery.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * spirit_wisp:淡青色缓升光点。
 * 行为:轻微布朗漂移 + 朝"呼吸曲线"变化的透明度;寿命 30-50t。
 */
public class SpiritWispParticle extends TextureSheetParticle {

    protected SpiritWispParticle(ClientLevel level, double x, double y, double z,
                                 double vx, double vy, double vz, SpriteSet sprites) {
        super(level, x, y, z, vx, vy, vz);
        this.pickSprite(sprites);
        this.lifetime = 30 + this.random.nextInt(21);
        this.gravity = -0.02F;                       // 缓慢上浮
        this.quadSize = 0.06F + this.random.nextFloat() * 0.04F;
        this.rCol = 0.55F; this.gCol = 0.9F; this.bCol = 0.95F;
        this.xd = vx * 0.4; this.yd = vy * 0.4 + 0.01; this.zd = vz * 0.4;
        this.hasPhysics = false;
    }

    @Override
    public void tick() {
        super.tick();
        // 布朗漂移
        this.xd += (this.random.nextDouble() - 0.5) * 0.002;
        this.zd += (this.random.nextDouble() - 0.5) * 0.002;
        // 呼吸透明度:sin 包络,首尾淡入淡出
        float t = (float) this.age / (float) this.lifetime;
        this.alpha = (float) (Math.sin(Math.PI * t) * 0.85);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        public Provider(SpriteSet sprites) { this.sprites = sprites; }
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z, double vx, double vy, double vz) {
            return new SpiritWispParticle(level, x, y, z, vx, vy, vz, sprites);
        }
    }
}
```

注册挂载(客户端事件,完整):

```java
@Mod.EventBusSubscriber(modid = ProjectMystery.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class PMClientParticles {
    @SubscribeEvent
    public static void onRegisterProviders(RegisterParticleProvidersEvent e) {
        e.registerSpriteSet(PMParticles.SPIRIT_WISP.get(), SpiritWispParticle.Provider::new);
        e.registerSpriteSet(PMParticles.POLLUTION_MOTE.get(), PollutionMoteParticle.Provider::new);
        e.registerSpriteSet(PMParticles.RITUAL_RUNE.get(), RitualRuneParticle.Provider::new);
        e.registerSpriteSet(PMParticles.DIVINATION_THREAD.get(), DivinationThreadParticle.Provider::new);
        // ……其余同格式
    }
}
```

---

# 48. 技能能力全表(v0.8 新增 · 15 途径 × 序列 9-5 · 150+ 条)

> 每条能力 = 一行注册:`名称 | 类型(P 被动/A 主动/T 切换) | 灵性消耗 | 冷却 | 执行器 ID | 关键参数`。执行器复用度优先(同执行器不同参数 = 不同能力),v0.8 统计:150 能力 → **仅 62 个执行器**。首发五途径 9-4 详表已在 7.3-7.7,此处收录**第二批+第三批 10 条途径的序列 9-5 全表(100 条)**,并给出执行器复用映射。

## 48.1 水手 → 海洋歌者(→ 暴风)

| 序列 | 能力 | 型 | 耗/冷 | 执行器 | 参数要点 |
|------|------|----|-------|--------|---------|
| 9 水手 | 平衡大师 | P | — | `pm:sure_footing` | 船上/冰面不滑,击退 -30% |
| 9 | 大力水手 | P | — | `pm:attr_mod` | 水中挖掘 ×2,力量 +1 |
| 9 | 浪涌拳 | A | 8/6s | `pm:melee_wave` | 近战附水系击退,水中伤害 +40% |
| 8 暴怒之民 | 狂怒引擎 | T | 3/s | `pm:rage_mode` | 攻速 +30%/防 -20%,受击回灵 |
| 8 | 威吓咆哮 | A | 15/45s | `pm:fear_shout` | 8 格恐惧 3s(生物),玩家屏幕抖动 |
| 7 航海家 | 风向直觉 | P | — | `pm:weather_sense` | 天气变化提前 60s 提示 |
| 7 | 定向浪标 | A | 10/20s | `pm:waypoint_mark` | 标记 1 点,全队 HUD 罗盘指向 |
| 7 | 水波行走 | T | 2/s | `pm:water_walk` | 水面行走 |
| 6 风眷者 | 风之庇护 | P | — | `pm:fall_reduce` | 摔落 -80%,箭矢偏折 15% |
| 6 | 唤风 | A | 20/30s | `pm:gust_push` | 锥形击飞+熄火 |
| 6 | 乘风 | A | 25/60s | `pm:wind_dash` | 三段空中冲刺 |
| 5 海洋歌者 | 深海之歌 | A | 35/90s | `pm:aoe_channel` | 吟唱 5s:16 格敌减攻 30%+友回复 |
| 5 | 召唤海雾 | A | 30/120s | `pm:fog_field` | 24 格浓雾 60s(弹道命中率↓) |
| 5 | 鲸落引力 | A | 40/150s | `pm:gravity_well` | 12 格拉拽向心 3s |

## 48.2 不眠者 → 灵巫(→ 黑夜)

9 不眠者:守夜精神(P,夜间不困+夜视微光)/ 疲劳转移(A,10/30s,`pm:debuff_transfer`)/ 浅眠警戒(P,睡眠中受击立醒+格挡首击);8 午夜诗人:哀恸之诗(A,18/40s,8 格精神伤害+压力转嫁)/ 暗影潜行(T,2/s,`pm:stealth_shadow`)/ 灵感迸发(P,夜间经验 +25%);7 梦魇:入梦(A,30/300s,`pm:dream_dive`,对睡眠者读取记忆碎片/植入噩梦二选一)/ 梦境编织(A,25/90s,制造幻象分身)/ 噩梦缠身(P,击杀掉落梦境残片);6 安魂师:安抚(A,15/20s,清恐惧/狂暴)/ 群体安眠(A,30/120s,和平生物强制入睡+玩家跳夜投票加权)/ 灵性护壁(T,4/s,`pm:spirit_barrier`);5 灵巫:操灵术(A,35/60s,`pm:spirit_command`,驱使 ≤2 灵体作战 60s)/ 缚灵领域(A,40/180s,16 格灵体减速 50%+可视化)/ 生命感知(P,32 格心跳雷达)。

## 48.3 收尸人 → 看门人(→ 死神)

9 收尸人:死者安眠(P,尸体 8 格内压力不增)/ 验尸(A,5/10s,`pm:inspect_corpse`,读取死因/死亡时间/凶手线索)/ 尸臭耐受(P);8 掘墓人:渡魂(A,20/60s,送residual 灵体安息,产 `亡者遗愿残响`)/ 墓土亲和(P,挖掘墓土必掉精粹)/ 缚尸(A,25/90s,短暂唤起 1 具骸骨卫士 30s);7 通灵者:会客法·完全版(A,30/仪式,10.4 主控)/ 灵体对话(P,灵体不主动敌视)/ 阴阳眼(T,3/s,常驻灵视+可视灵体情绪色);6 死灵导师:白骨军团(A,45/240s,3 具精英骸骨 120s)/ 死亡宣告(A,30/90s,标记目标:其死亡时灵魂必被捕获)/ 亡者知识(P,渡魂时抽取 1 条知识条目);5 看门人:开门(A,50/600s,`pm:spirit_gate`,开启灵界短途门 10s)/ 界域巡守(P,灵界内全属性 +20%)/ 摆渡(A,40/仪式,护送玩家灵体回尸体点=远程复活服务,**玩家服务经济位**)。

## 48.4 歌颂者 → 光之祭司(→ 太阳)

9 歌颂者:晨祷(P,日出时全 buff 刷新)/ 圣光弹(A,8/4s,`pm:projectile_holy`,对亡灵 ×2)/ 颂歌(A,12/30s,8 格小回复);8 祈光人:光盾(A,18/25s,吸收 8 伤)/ 驱暗(A,15/20s,驱散 12 格黑暗类 debuff+隐身显形)/ 圣光灼目(A,20/45s,锥形致盲);7 太阳神官:正午审判(A,30/90s,标记处天降光柱)/ 无暗领域(T,5/s,10 格内怪物生成禁止)/ 光合(P,日光下缓回灵);6 公证人:誓约(A,25/仪式,`pm:oath_contract`,双人契约:违约者受神罚 debuff——**玩家间信用系统的机制底座**)/ 鉴伪(A,10/10s,鉴定文书/配方/货币真伪)/ 圣印(P,公证文书带防篡改校验);5 光之祭司:恒昼祷言(A,50/600s,强制局部白昼 120s)/ 圣光洗礼(A,40/180s,16 格大治疗+复燃倒地队友 1 次)/ 阳炎之躯(T,6/s,近身灼烧光环)。

## 48.5 第三批六途径(战士/秘祈人/阅读者/刺客/耕种者/药师)

**战士**:9 强壮体格(P)/破甲斩(A,8/6s)/战吼(A,12/30s);8 格斗家:连击计数(P,连击 ≥3 增伤)/擒拿(A,15/20s,缴械 3s)/铁布衫(T,3/s);7 武器大师:百兵精通(P,所有武器攻速 +15%)/武器投掷(A,10/8s,可召回)/剑气(A,20/25s);6 黎明骑士:骑士誓约(P,守护半径内友伤转移 20%)/黎明冲锋(A,25/40s)/圣光淬刃(A,20/60s);5 守护者:不动如山(T,8/s,嘲讽+减伤 50%)/盾墙(A,35/120s,展开 5×3 光盾墙)/守誓反击(P,格挡成功必反击)。
**秘祈人**:9 隐秘祷告(P,祈祷动作回灵)/窃听(A,8/15s,穿墙听 16 格对话气泡)/低语标记(A,12/30s);8 倾听者:万籁俱寂(A,20/60s,8 格消音领域)/心音辨谎(P,NPC 对话选项显示"心率")/回声定位(A,10/20s);7 隐修士:苦修(T,禁食换灵性上限 +30%)/静默结界(A,25/90s)/断念(A,15/30s,自清 1 项精神 debuff);6 蔷薇主教:血肉赐福(A,30/60s,牺牲 HP 强化队友)/荆棘缠绕(A,25/45s)/秘祭(P,仪式成功率 +10%);5 牧羊人:群心链接(A,40/180s,5 人小队共享 20% 伤害池)/迷雾牧歌(A,35/120s,驯服敌对生物 60s)/牧杖审判(A,30/90s)。
**阅读者**:9 速读(P,知识条目阅读耗时 -50%)/过目不忘(P,地图探索永久显示)/纸上谈兵(A,10/20s,复制看到的原版附魔书效果 30s);8 推理学员:演绎(A,15/30s,`pm:deduction_link`,连接 2 条线索生成新线索)/弱点洞察(A,12/15s,显示目标弱点部位)/逻辑护壁(P,精神伤害 -20%);7 侦探:犯罪重演(A,25/120s,重演 32 格内 10min 内死亡场景)/易容窥探(A,20/60s)/直觉(P,伪造物品红色微光);6 博学者:知识就是力量(P,每 50 知识条目全属性 +1)/万象辞典(A,30/90s,强制显形一切隐身/伪装)/引经据典(A,25/60s,随机复刻一条已读能力 10s);5 秘术导师:立论(A,50/仪式,原创论文成书→产 `thesis_core`)/学识领域(A,40/180s,16 格队友冷却 -25%)/真理之眼(T,6/s,全息灵视+数值透视)。
**刺客**(堕落倾向中性):9 影步(A,8/10s)/淬毒(P)/弱点直击(P,背刺 ×1.5);8:影分身(A,20/60s)/毒雾(A,18/45s)/无声(P);7:影杀(A,30/90s,处决 <30% HP 目标)/换影(A,25/40s,与影分身换位)/血契(P,击杀回灵);6:暗影领主(T,5/s)/夜幕(A,35/150s,24 格黑夜领域)/死亡印记(A,30/60s);5:寂灭一闪(A,50/300s,超远突进斩)/影之国度(A,45/240s,拉目标进影空间 1v1 15s)/无我(P,静止 5s 后完全隐形)。
**耕种者**:9 绿手指(P,作物生长 +25%)/催芽(A,8/20s)/嫁接(A,10/仪式,40.3 嫁接树入口);8:光合治疗(A,15/30s)/藤蔓束缚(A,18/40s)/大地情报(P,踩踏感知 16 格地下矿藏模糊方位);7:森之囁语(A,20/60s,动员 3 只野生动物助战)/结界树(A,30/180s,种植临时庇护树=移动灵性墙)/丰收(A,25/90s,催熟 8 格作物);6:荒岁诅咒(A,35/120s,16 格敌方"饥馑":持续掉饱食+攻减)/根须行走(T,4/s,地下潜行)/生命嫁接(A,30/90s,转移自身 HP 给作物→果实变治疗果);5:百果之宴(A,50/600s,摆宴:8 人份随机 buff 果实)/荒野召唤(A,45/240s,召唤树人卫士)/季节掌控(A,40/300s,局部更改季节贴图与作物逻辑 300s)。
**药师**:9 药理直觉(P,tooltip 显示魔药隐藏属性)/精准称量(P,炼药容错 ±1℃)/急救(A,8/10s);8:毒理(P,毒不侵)/复方(A,15/仪式,合并 2 魔药效果各 70%)/麻醉针(A,12/20s);7:临床(A,20/40s,移除目标 1 项 debuff 转为小瓶收集)/药浴(A,25/120s,坩埚变浴:入浴者消化 +20%)/耐药性管理(P,镇定剂成瘾阈值 +3);6:流行病学(A,30/90s,读取 32 格所有单位 buff/debuff 全景)/万灵药坯(A,35/仪式,产 `diluted_potion` 高级版)/以毒攻毒(P,受毒时反向回复);5:药王鼎(A,50/仪式,召唤大坩埚:群体供药站 300s)/生死一线(A,45/300s,锁定目标 HP 不低于 1 点持续 5s)/丹心(P,自身魔药副作用 -50%)。

## 48.6 执行器复用映射(工程账)

`pm:attr_mod`(属性修饰,21 能力复用)/ `pm:projectile_generic`(9)/ `pm:aoe_channel`(7)/ `pm:field_effect`(领域类 11)/ `pm:stealth_*`(4)/ `pm:summon_*`(6)/ `pm:contract_*`(誓约/血契/群心 3)/ `pm:transform_*`(神话形态 15)……全部执行器实现放 `ability/executor/` 包,按 27.3 模板;**新增执行器必须先查复用表**,写入 PR 模板检查项。

---

# 49. 生物图鉴扩充(v0.8 新增 · 18 种新生物 · 完整实体代码)

> 20 章覆盖常规非凡生物 10 类+失控体+Boss。本章补全 v0.7/v0.8 文本中出现但未立册的全部生物,做到**文中每个名词都有实体规格**。

## 49.1 新生物总表

| ID | 名称 | HP/攻 | 敌意 | 生成 | 核心行为 | 掉落 |
|----|------|-------|------|------|---------|------|
| `compass_bird` | 罗盘鸟 | 12/0 | 被动 | 海滩/海岛,群 1-2 | 可驯服(海盐结晶);`point` 动画指向最近未探索结构 | `compass_bird_eye` 8%(GLM) |
| `glowfly_swarm` | 光萤群 | 8/0 | 被动 | 夜间花田/教堂花园 | 单实体渲染为粒子群;瓶捕获得移动光源 | `glowfly_heart` 15% |
| `nightmare_steed` | 梦魇兽 | 45/7 | 敌对(仅夜/梦境层) | 梦境副本/血月地表 | 冲锋践踏;被骑乘(驯服后)可入他人梦境边缘 | `nightmare_mane` 25% |
| `knowledge_imp` | 知识妖 | 30/3 | 中立 | 图书馆结构/大学 | 偷玩家手册知识条目并逃跑;击杀返还+概率掉脑髓 | `sage_cerebrum` 5% |
| `ferryman` | 摆渡人 | 200/— | 不可攻击 | 灵界渡口/灵界裂隙 | 交易 NPC:灵界摆渡、`ferry_bell` 出售、渡魂委托 | — |
| `god_spawn_juvenile` | 神孽(幼) | 300/12 | 敌对 | 神孽巢穴(19 章结构) | 三阶段:潜伏拟态方块→触须收割→畸变冲撞 | `0-006 缚日晷影`(首杀) |
| `storm_seed_core` | 风暴之种核心 | 120/— | 事件体 | 风暴之种事件中心 | 悬浮核心,吸引雷击;破坏=事件提前终结+掉落 | `storm_essence` 100% |
| `lighthouse_keeper` | 灯塔看守人 | 180/10 | 剧情体 | 任务链五 | 半失控体:可战/可渡双解(21.5) | 配方残页 |
| `mirror_walker` | 镜中人 | 60/8 | 敌对 | 千面之镜副产/镜厅结构 | 复制目标玩家装备外观与 1 个主动技 | `mirror_shard_alive` 40% |
| `desire_apostle` | 欲望使徒 | 250/14 | Boss 级 | 任务链六终幕 | 三欲望阶段(贪/怒/惧),读玩家背包价值调整台词 | 稀有特性 |
| `mist_flock` | 迷雾牧场羊群 | 20/0 | 被动 | 迷雾牧场事件 | 剪毛得 `迷雾凝露`;跟随牧羊人玩家 | 群心之铃材料 |
| `counterfeiter` | 伪币铸师 | 40/5 | 敌对(黑市中立) | 废弃铸币厂 | 撒伪币逃跑(捡=罪值+1 的陷阱) | `mint_die_blank` 10% |
| `wraith` | 怨念残影 | 35/6 | 敌对(仅灵视可见) | 凶宅/战场遗址 | 穿墙;油灯光照下停滞 | `wraith_residue` 30% |
| `spirit_eel` | 灵界鳗 | 25/4 | 中立 | 灵界水域 | 缠绕减速;群体发光引路(跟随=捷径) | `spirit_realm_moss` |
| `deep_fish` | 深海鱼(标签族) | 原版鱼扩展 | — | 深海 | 原版鱼挂 `pm:deep_fish` 标签走 GLM | `deepfish_marrow` |
| `whale_ancient` | 古鲸 | 400/0 | 被动 | 深海稀有 | 死亡自然事件"鲸落":沉底生成材料床 | `whalefall_ambergris` |
| `theater_phantom` | 剧院魅影 | 90/9 | 中立 | 剧院地下(42 章) | 音乐对抗玩法:演奏正确旋律安抚 | `无弦提琴` 线索 |
| `owl_dusk` | 暮枭 | 16/2 | 中立 | 夜间针叶林 | 俯冲骚扰高污染玩家(污染 ≥60 才敌对——**行走的污染检测器**) | `owl_visual_nerve` 20% |

## 49.2 完整实体类样例:罗盘鸟(可直接落库)

```java
package top.aurora.projectmystery.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.animal.ShoulderRidingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.aurora.projectmystery.registry.PMItems;
import top.aurora.projectmystery.world.StructureLocator;

import javax.annotation.Nullable;

/**
 * 罗盘鸟:被动飞行生物,可用海盐结晶驯服。
 * 驯服后每 60s 播放 point 动画,头部朝向最近的未探索 PM 结构;
 * 主人潜行+右键 → 聊天栏输出模糊方位("东北方,很远/不远/就在附近")。
 */
public class CompassBirdEntity extends ShoulderRidingEntity implements FlyingAnimal, GeoEntity {

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.pm.compass_bird.idle");
    private static final RawAnimation FLY  = RawAnimation.begin().thenLoop("animation.pm.compass_bird.fly");
    private static final RawAnimation POINT = RawAnimation.begin().thenPlay("animation.pm.compass_bird.point");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int pointCooldown = 0;
    @Nullable private BlockPos lastPointed;

    public CompassBirdEntity(EntityType<? extends ShoulderRidingEntity> type, Level level) {
        super(type, level);
        this.moveControl = new net.minecraft.world.entity.ai.control.FlyingMoveControl(this, 10, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 12.0D)
                .add(Attributes.FLYING_SPEED, 0.5D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new PanicGoal(this, 1.4D));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 1.0D, 6.0F, 2.0F, true));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.1D,
                net.minecraft.world.item.crafting.Ingredient.of(PMItems.ITEMS.getEntries().stream()
                        .filter(r -> r.getId().getPath().equals("sea_salt_crystal"))
                        .findFirst().orElseThrow().get()), false));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomFlyingGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        // 驯服:海盐结晶
        if (!this.isTame() && stack.is(itemBySaltTag(stack))) {
            if (!player.getAbilities().instabuild) stack.shrink(1);
            if (!this.level().isClientSide) {
                if (this.random.nextInt(3) == 0) {
                    this.tame(player);
                    this.level().broadcastEntityEvent(this, (byte) 7);   // 爱心
                } else {
                    this.level().broadcastEntityEvent(this, (byte) 6);   // 烟雾
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        // 主人潜行右键:报告方位
        if (this.isTame() && this.isOwnedBy(player) && player.isShiftKeyDown() && !this.level().isClientSide) {
            BlockPos target = StructureLocator.nearestUnexplored((net.minecraft.server.level.ServerLevel) this.level(),
                    this.blockPosition(), 3000, player.getUUID());
            if (target != null) {
                this.lastPointed = target;
                player.sendSystemMessage(StructureLocator.vagueDirectionMessage(this.blockPosition(), target));
                this.pointCooldown = 1200;
            } else {
                player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("pm.msg.compass_bird.nothing"));
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    private boolean itemBySaltTag(ItemStack s) {
        return s.getItem().builtInRegistryHolder().key().location().getPath().equals("sea_salt_crystal");
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (pointCooldown > 0) pointCooldown--;
    }

    // —— GeckoLib ——
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 4, state -> {
            if (this.pointCooldown > 1180) return state.setAndContinue(POINT); // 触发后 1s 播 point
            if (this.isFlying()) return state.setAndContinue(FLY);
            return state.setAndContinue(IDLE);
        }));
    }

    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
    @Override public boolean isFlying() { return !this.onGround(); }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (lastPointed != null) tag.putLong("PmLastPointed", lastPointed.asLong());
    }
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("PmLastPointed")) lastPointed = BlockPos.of(tag.getLong("PmLastPointed"));
    }

    @Nullable @Override
    public AgeableMob getBreedOffspring(net.minecraft.server.level.ServerLevel level, AgeableMob other) {
        return null; // 不繁殖:数量由生成规则控制
    }
}
```

## 49.3 生成规则与世界注入

全部生物走 `worldgen/biome_modifier`(45.8 格式)+ 结构内 spawner 双通道;敌对新生物**全部尊重"未入途径玩家豁免"(D7)**:`finalizeSpawn` 中检查 32 格内是否仅有普通玩家,是则转为巡逻不索敌。事件体(风暴之种核心等)由 18 章调度器直接 spawn,不入自然生成池。

---

# 50. 文案库(v0.8 新增 · 全量文案规范与样本池)

> 31 章定基调:克制、书面、维多利亚译制腔、禁网络流行语。本章给出**可直接入 lang 的样本池**与写作模板;全部原创(内容红线 1.2)。

## 50.1 呓语池(40 条选列 20,`pm.whisper.*`)

通用低污染:「今晚的雾,比昨晚厚了一寸。」「有人在数你的脚步声。一、二、一、二。」「钟停在三点。可你没有钟。」「窗外的路灯记得你的名字。」
通用高污染:「把眼睛借我用一晚,好吗?」「你影子的方向,和别人不一样。」「墙的那边也是墙。墙的那边也是墙。墙的那边——」「他们把海装进了你的耳朵。」
占卜家系:「不要用占卜结果去占卜占卜本身。」「银线断了七根,还剩一根系着谁?」
收尸人系:「土是暖的。土一直是暖的。」「有位客人想预约明天的葬礼——他自己的。」
歌颂者系:「太阳落下时,替它看着黑夜。」
刺客系:「你上一次呼吸,是什么时候的事?」
药师系:「按时服药。按时,服药。按,时,服,药。」
阅读者系:「这一页你读过了。这一页你读过了。这一页你——撕掉它。」

**写作模板**:①具体的日常意象 ②一处不对劲 ③不解释。禁止:直接惊吓词("鬼""死定了")、二人称威胁、玩梗。

## 50.2 罗塞尔日记样章(72 页中的 6 页全文示例)

- **#03(明文,假线索页)**:「四月十一日。我又梦见那扇门了。这次我数了门环——十三个。醒来后我让人查遍了皇宫,没有一扇门有十三个门环。也许它不在皇宫。也许它不在任何地方。(旁注,另一种笔迹:陛下数错了。)」
- **#17(密文,硬信息:尊名第二句)**:解密后:「……如果有一天你必须呼唤那位蒙灰的先生,记住第二句是『不属于这个时代的引导者』。念错一个字,就当我没写过这页。」
- **#29(明文)**:「今天教了他们做蒸汽机。他们管锅炉压力表叫『铁的心跳』。我笑了很久,然后在没人的时候,哭了一小会儿。」
- **#41(密文,硬信息:配方 59 残页坐标暗示)**:「知识妖只偷它读得懂的东西。所以我把最重要的那页,写成了菜谱。」
- **#56(明文,假线索页)**:「西边的灯塔下埋着我的备用日记。(旁注:这页是我写来骗小偷的。真话率:百分之五十。)」
- **#72(终页,群星回响钥匙)**:「如果你集齐了七块石板,替我向『他们』问一句话:值得吗?——不管答案是什么,别信。」

**规范**:每页 ≤120 字;30% 假线索页必须**页内自洽**(有旁注或笔误暗示);硬信息页解密难度与信息价值正相关。

## 50.3 传闻池(报纸/酒馆 NPC,30 条选列 10)

「贝克镇当铺收了一面照不出人影的镜子,老板三天没开门了。」「北郊墓园夜里有人唱歌,守墓人说那调子他祖父哼过。」「大学有位教授宣称雾有重量,被同行笑了;昨天他称出来了。」「码头工人说 7 号仓库的货自己换了位置。」「剧院包厢 B 常年被同一位先生预订,可售票员从没见过他进场。」「银行新来的出纳能一眼挑出伪币,他说伪币『闻起来是甜的』。」「有个孩子用粉笔在桥洞画了个圈,第二天圈里的雨是干的。」「侦探社挂出告示:高价收购『没有寄件人的信』。」「铁路夜班车多报了一位乘客,查票时少了一位。」「教堂的钟自己响了半声。只有半声。」

**用途**:42 章 NewspaperGenerator 素材池 + 委托钩子(每条传闻挂 0-1 个可追查委托 ID,追查率约 40%——**并非所有传闻都有后续**,保持世界的不可穷尽感)。

## 50.4 NPC 对白模板与成就文案

**对白模板**(委托板/商店/教会三类,各含 greeting/business/farewell/reputation 四档 × 3 变体,共 108 条入 `pm.npc.*`):示例(侦探所接待,声望高档 greeting):「是您啊。老板念叨过,说这个城市欠您几次道谢——他让我把好案子都留给您挑。」
**成就文案**(60 条全量入 `pm.adv.*`,标题追求双关与克制,示例):`first_potion`「第一口深渊」/ `first_acting`「戏中人」/ `pollution_50`「你看,雾在看你」/ `grayfog_seat`「愚者们的圆桌」/ `notary_first`「谁来公证公证人」/ `chain8_done`「有些信永远不会迟到」/ `uniqueness_won`「独一无二,代价另计」/ `all_diary`「陛下的读者」/ `slate_all`「二十一片真相」/ `season_god_war`「群星终于回响」。

---

# 51. 新手指引与引导系统(v0.8 新增)

> 恐怖神秘题材的最大流失点是"看不懂"。引导哲学:**教操作,不剧透;给方向,不给答案**。三层引导:手账(主动查阅)→ 教学成就链(里程碑牵引)→ 情境 Toast(即时提示)。

## 51.1 「神秘学手账」(guide_journal)

出生获得;自研翻页 UI(非 Patchouli 依赖,减少硬依赖数)。章节随进度**自动解锁**(内容驱动:首见坩埚才出现炼药章)。目录:
1. 这座城市(背景与安全守则:夜里别听,别回头,教堂是安全的)
2. 成为非凡者(配方→坩埚→服药→消化,四步图解)
3. 扮演法(准则查看方式、消化进度条在哪、"演过头"的代价)
4. 灵性与污染(HUD 图解:灵性条/污染刻度/理智裂纹粒子的含义)
5. 占卜入门(七法对比表,新手推荐灵摆)
6. 仪式安全手册(灾变案例三则——**用故事教规则**)
7. 钱与生计(三级货币、委托板位置、当铺规则)
8. 灰雾(仅在收到邀请后解锁,只有一句:「带上你的尊名,和你的怀疑。」)
9. 附录:途径速览(已实装 15 条,每条一句定位+起步序列 9 能力预览)

## 51.2 教学成就链(12 步,`advancements/pm/tutorial/`)

拾取手账 → 完成一次灵摆占卜 → 找到第一张配方(任务链一步 3 保底)→ 点燃坩埚 → 炼出任意品质魔药 → 服药成为序列 9 → 触发第一次扮演事件 → 消化度 50% → 完成第一单委托 → 经历第一次呓语时刻(自动)→ 进入教堂庇护区 → 阅读第一页罗塞尔日记。每步 toast + 手账对应章节高亮;**全链完成奖励**:`spirit_goggles` ×1(给非灵视途径玩家的补偿性好奇心工具)。

## 51.3 情境 Toast(24 条选列)

污染首破 20:「雾比刚才近了一些。(污染会引来注视——净化剂、教堂、苦修都能压低它)」/ 首次听到低语:「你听到的不一定是真的。但『你听到了』这件事是真的。」/ 首见封印物:「收益写在正面,代价写在背面。先看背面。」/ 首次进入血月:「今晚,请待在光里。」/ 首次委托失败:「侦探社不追究失败,只追究谎报。」/ 首次会客占线:「『那边』也有忙的时候。」

## 51.4 每途径首小时路书(节选二)

**占卜家路书**:出生镇 → 委托板接「寻猫」(教灵摆)→ 猫在钟楼(教爬塔+灵视微光)→ 报酬 6 便士 → 杂货店买纸人 ×2 → 睡前纸人问事(教风险:纸人 10% 烧毁)→ 次日链一开局。
**战士路书**:出生镇 → 竞技场报名(教连击)→ 三连胜奖励 `beast_gland` → 坩埚教学 → 序列 9 → 竞技之夜事件预告(给出中期目标)。

---

# 52. 指令树与任务数据结构全细化(v0.8 新增)

## 52.1 26 条指令全参数树(节选核心 12 条,其余同格式入 `docs/commands.md`)

```
/pm pathway set <player> <pathway> <sequence>        权限4  例:/pm pathway set Bill pm:seer 7
/pm pathway clear <player>                           权限4
/pm digestion set <player> <0.0-1.0>                 权限3
/pm pollution add|set <player> <int>                 权限3
/pm sanity set <player> <int>                        权限3
/pm characteristic give <player> <pathway> <seq>     权限4
/pm ritual debug <start|outcome force <tier>>        权限4  # 测试仪式五结局
/pm event start|stop <event_id> [duration]           权限3  例:/pm event start pm:blood_moon
/pm grayfog seat <assign|revoke> <player> <0-21>     权限4
/pm economy give <player> <pounds> [soli] [pence]    权限3
/pm quest <start|skipstep|reset> <player> <chain_id> 权限3
/pm uniqueness <status|force_transfer <from> <to>>   权限4  # 41 章赛季管理
/pm knowledge grant <player> <knowledge_id>          权限3
/pm relic resonance <status|reset>                   权限4  # 13.6 封印共鸣
/pm mimic debug spawn <entity_id>                    权限4
/pm season <status|advance|archive>                  权限4  # 55 章赛季弧
……(/pm whisper test、/pm diary give、/pm slate give、/pm npc reputation、
    /pm newspaper publish、/pm train schedule、/pm bounty post、/pm acting trigger、
    /pm commission refresh、/pm backup marker、/pm bridge status —— 共 26 条)
```

统一约定:所有写操作指令产生**审计日志**(`logs/pm-admin.log`,JSON 行格式:时间/执行者/指令/目标/前后值),供 53 章小雨审计管线消费。

## 52.2 任务链 JSON 数据结构(quests/)

```json
{
  "id": "pm:chain1_missing_squad",
  "title_key": "pm.quest.chain1.title",
  "steps": [
    {
      "id": "s1_accept",
      "objective": { "type": "talk_npc", "npc": "pm:npc/press_clerk" },
      "on_complete": { "give": [{ "item": "pm:commission_paper", "nbt": { "case": "missing_reporter" } }] }
    },
    {
      "id": "s2_church",
      "objective": { "type": "enter_structure", "structure": "pm:abandoned_church" },
      "sub": [
        { "type": "pickup", "item": "pm:blank_manuscript", "nbt_case": "bloodstained" },
        { "type": "encounter", "entity": "pm:wraith", "kill_optional": true }
      ]
    },
    {
      "id": "s3_hut",
      "objective": { "type": "enter_structure", "structure": "pm:mystic_hut" },
      "branch_teaching": { "real_recipe": 1, "fake_recipe": 1, "identify_tutorial": true }
    },
    { "id": "s4_advance", "objective": { "type": "reach_sequence", "sequence": 9, "any_pathway": true } },
    { "id": "s5_camp", "objective": { "type": "rescue", "npc": "pm:npc/survivor_reporter" },
      "solutions": ["stealth", "assault", "divination_bypass"] },
    { "id": "s6_defense", "objective": { "type": "survive_waves", "waves": 3, "protect": "pm:npc/survivor_reporter" } },
    { "id": "s7_settle", "objective": { "type": "talk_npc", "npc": "pm:npc/nighthawk_contact" },
      "on_complete": { "give": [{ "item": "pm:map_fragment", "nbt": { "key": "burnt_list" } }],
                        "unlock_chain": "pm:chain2_collector" } }
  ],
  "fail_policy": "step_retry",
  "coop": { "shared_progress": true, "max_party": 4 }
}
```

> 目标类型枚举(QuestObjectiveType):talk_npc / enter_structure / pickup / encounter / reach_sequence / rescue / survive_waves / deliver / brew_quality / divine_success / ritual_outcome / escort / collect_set / reputation_reach / custom_callback —— 15 种,覆盖 8 条链全部步骤;新增类型需过 Codec 注册,禁止硬编码链逻辑。

---

# 53. 极光之恋服务器整合方案(v0.8 新增 · 运营侧)

> 本章面向 Bill 自己的 1.20.1 服务器「极光之恋」的部署与运营,与 Mod 本体解耦(全部走 34 章 API + 审计日志,**不进 Mod 仓库**,放独立仓库 `aurora-pm-ops`)。

## 53.1 小雨(Aurora Rain)AI 管理桥接

- **架构**:Forge 侧轻量伴生 Mod `pm-bridge`(仅服务端)开 WebSocket 服务(默认 `ws://127.0.0.1:18795`,LAN 绑定,token 鉴权)→ OpenClaw 网关(小落/小雨实例)订阅事件流。**注意与现有 18789/18792 端口错开;Windows 侧沿用既有 PowerShell 端口转发脚本追加 18795。**
- **事件流(出站)**:玩家晋升/失控/死亡掉特性、封印共鸣、0 级封印物激活、灰雾拍卖高价成交、世界事件起止、任务链关键节点、审计日志(52.1)全量。JSON Lines,字段与 API 事件一一对应。
- **指令流(入站,白名单)**:小雨仅可执行只读查询 + 三条运营指令(`/pm newspaper publish`、`/pm event start`(限 seasonal 白名单)、`/pm bounty post`)。**写玩家数据的指令一律不开放给 AI**——这是硬红线,写入 pm-bridge 配置默认值。
- **人格分工**:小雨(INFJ,公正/奖罚分明)负责播报、仲裁申诉工单、周报;小落(INFP)负责创意侧(报纸副刊文案生成,走 50.3 传闻池模板约束,**生成后入人工审核队列再发布**,防止 AI 文案破坏文风红线)。

## 53.2 QQ 群与直播联动

- **QQ 播报**(经 OpenClaw 既有通道):血月/猎巫夜开始、首学配方头条、神位战争赛季节点 → 群公告;玩家 @小雨 查询自己的途径/消化/声望(只读)。
- **直播联动(房间 30313460,可选开关)**:小兮直播时可开启「观众之夜」事件——直播间弹幕投票选择当晚世界事件(从 seasonal 白名单三选一);礼物触发彩蛋:向在线玩家投放「极光明信片」。**平衡红线**:联动只投放外观/事件,不投放数值与材料。live-bot.py 已有的弹幕监听直接复用,新增一个 intent → pm-bridge 指令的映射表。
- **B 站周报**:小雨汇总审计日志与报纸头条,自动生成动态图文草稿(人工过审后发布)。

## 53.3 赛季运营日历(与 55 章叙事弧咬合)

| 周 | 运营动作 |
|----|---------|
| 第 1-2 周 | 开荒期:教学链数据观测(漏斗:手账→首药→首扮演),流失点热修 |
| 第 3-6 周 | 成长期:每周六晚固定世界事件(投票制);首学头条竞速榜 |
| 第 7-10 周 | 中盘:亵渎石板残片限时投放(每周 2 片进入世界);灰雾席位选举 |
| 第 11-13 周 | 终局:唯一性争夺 → 神位战争周末决战 → 赛季结算庆典(21.8 彩蛋链常驻不清档) |
| 赛季间歇 | 存档封存入「编年史」(55.3);平衡补丁窗口 |

## 53.4 服务器保障清单

自动备份(小落 cron:每 6h 增量 + 每日全量,异地一份)/ 反作弊:审计日志异常模式检测(小雨规则:深夜高频 `/pm economy give` 告警)/ 崩溃回滚预案:PlayerMysteryData schema 版本化(5.2 已有)+ 备份点指令 `/pm backup marker` / 压测基准:27.4 性能预算 + 50 人同时在线灰雾拍卖场景专项测试。

---

# 54. Wiki 覆盖对照表(v0.8 核对)

> 对照 lordofthemysteries.fandom.com 中文维基的全部一级分类,逐项确认文档覆盖位置与原创化处理策略。

| 维基大类 | 文档章节 | 覆盖度 | 备注 |
|---------|---------|--------|------|
| 22 条途径与全序列名 | 7.1-7.13 | ✅ 全量 | 15 可玩 + 7 概设;相邻转换=原作"途径相邻"设定的玩法化 |
| 魔药与配方体系 | 9 章 + 44 章 | ✅ 75 配方 | 配方=知识的经济观还原(9.7) |
| 扮演法/消化/失控 | 8 章、5.5 | ✅ | 失控体逐途径(20.2/46.2) |
| 非凡特性与不灭定律 | 6 章 | ✅ | |
| 灵界/灵视/占卜/仪式/尊名 | 10-12 章 | ✅ | 七法占卜为原作五法的适配扩展 |
| 封印物(编号管控体裁) | 13 章 | ✅ 72 件 | 全原创设计,致敬体裁不复用具体设定 |
| 塔罗会/灰雾/22 席 | 15 章 | ✅ | |
| 七正统教会/执法机构/隐秘组织 | 16 章 | ✅ | |
| 历史五纪/旧日/外神背景 | **55 章(v0.8 补齐)** | ✅ 原创化 | 此前唯一大缺口,本版补齐 |
| 罗塞尔与日记 | 14 章 | ✅ 72 页 | 全原创文案(50.2 规范) |
| 货币/雾都风物/报纸 | 17/42 章 | ✅ | |
| 唯一性/神位/神战 | 41 章 | ✅ | 序列 0 不可玩=对原作"成神即非人"的机制表达 |
| 亵渎石板 | 14.4 | ✅ | 21 残片 |
| 值夜者/心理炼金会等组织生态 | 16.2/16.3 | ✅ | 双重身份玩法 |
| 角色人物志 | 不直接收录 | ⚠️ 有意排除 | 内容红线:不复刻原作人物剧情,仅以"传闻"致敬 |

---

# 55. 宏大叙事与纪元史诗(v0.8 新增)

## 55.1 第五纪元编年史(原创化版,玩家可考据的"深历史")

游戏内历史采用原创化五纪框架(ip_mapping 词表同步):**熔铸之纪**(诸神未分,巨人与龙的黄昏)→ **失语之纪**(第一场大静默,文字诞生于禁忌)→ **弑约之纪**(旧日陨落,七柱信仰确立)→ **蒸汽之纪**(罗塞尔式人物「异乡的皇帝」带来机械与新词)→ **当前·迷雾之纪**。每一纪对应一层考古内容:结构(19 章)按纪元分层埋藏,亵渎石板 7 块各记一段被篡改的纪元史——**同一事件在教会正史、日记、石板中有三个版本**,考据本身就是玩法。玩家知识手册设「编年史」页签,按证据等级(正史/孤证/矛盾)标注,集齐一纪的三源证据解锁隐藏成就与灰雾谈资对白。

## 55.2 三大终局幻影(长线悬念钩子)

世界始终悬挂三个不解答的问题,所有内容向它们汇聚但永不官方定论:①**门后是什么**(学徒途径终点,每赛季神战结算时给一帧 2 秒的门缝演出,逐赛季多一帧);②**雾从哪里来**(污染系统的世界观根源,石板 #7 给出与教会说法相反的暗示);③**「他们」是否在回信**(祈愿签系统偶发 0.1% 的"被回应"事件,内容由运营手写,全服编号存档)。设计准则:**悬念是服务器的公共财产**——解答权保留给玩家社区的集体考据,官方只加线索不加结论。

## 55.3 赛季叙事弧模板与「活历史」账本

每赛季 = 一段可被玩家改写的历史:开季事件(53.3)→ 中盘抉择(全服投票或阵营战决定一项世界状态,如"母树据点是否被焚毁")→ 终局神战 → **编年史结算**:小雨把本季大事(首学者/唯一性得主/投票结果/神战胜负)写入下赛季世界生成的实体内容——上季冠军的雕像出现在广场、被焚毁的据点留下焦土结构、报纸档案馆可查旧刊。**玩家的名字进入世界的地质层**,这是"可以玩很久很久"的最终答案:内容会耗尽,历史不会。

---

# 56. v0.8 收束:总账与下一步

**内容总账(v0.8)**:途径 22(15 可玩)/ 魔药配方 75 / 封印物 72 / 物品 180+ / 工作台配方 60+ / 数据包 JSON ≈1100(脚本生成)/ GeckoLib 模型 49 / 粒子 34 / 能力 150+(执行器 62)/ 生物 18 新增 / 呓语 40 / 日记 72 / 传闻 30 / 成就 60 / 指令 26 / 任务链 8 / 世界事件 25 / 结构 14 / 毕业 400-600h(单人)。

**本周可执行(在 39 章基础上追加)**:
1. 建 `scripts/gen_datapack.py` + `docs/recipes_master.csv` 骨架(45 章 D9 决策落地,最高优先级——它决定后续一切内容的生产速度);
2. `PMItems.bootstrapSimple()` 落库 + lang CSV 首批 84 材料(43 章);
3. 罗盘鸟全套(实体 49.2 + geo/anim 46.5-46.6 + biome_modifier 45.8)作为**新生物生产管线的垂直切片**;
4. 教学成就链 12 步(51.2)进 MVP 验收标准(3.3);
5. `pm-bridge` 仓库初始化(53.1),先只做事件出站流。

**分工建议**:Herdeny——1/2/3 的代码位;小倪——CSV 主表、50 章文案池扩写、53 章 ops 仓库与小雨接线。

> v0.8 是"全量落地版":从这一版起,文档的每一个名词都有注册 ID、每一个系统都有数据文件格式、每一个模型都有排期。剩下的不是设计,是生产。
<!-- END v0.8 BASELINE -->

---

# J. 原著一致性与内容图谱（v0.9 新增）

# 57. v0.9 更新治理、来源分级与冲突处理

## 57.1 本次增量目标

v0.9 不把“更多内容”理解为无边界堆表，而是把 v0.8 已经建立的物品、技能、任务、组织、世界事件和服务器运营，重新连接成可验证、可游玩、可持续生产的系统网。新增工作分为六条主线：

1. **原著一致性**：补齐非凡特性中的精神烙印、额外特性、相邻途径转换、高序列神话生物形态、锚与祈祷响应等约束。
2. **全途径可设计**：把 v0.8 中仍为概设的 7 条途径扩展为序列 9–5 的完整设计规格，使 22 条途径都达到“可进入制作排期”的粒度。
3. **内容生产化**：所有新增生物、材料、物品、配方、状态、粒子、天气、事件、任务，都有注册 ID、来源、用途、前置知识、产出与互链。
4. **生活—神秘—高序列三层闭环**：普通人的职业与消费为神秘活动提供资金；神秘调查提供晋升知识；高序列行为改变世界状态并反向影响普通生活。
5. **兼容而不绑死**：对 JEI/EMI、Curios、Create、Farmer’s Delight、Immersive Engineering、AE2、Botania、FTB 系列、Jade/WTHIT 等采用软依赖适配器。
6. **可落地路线图**：区分“代码骨架完成、内容规格完成、资产完成、可玩验收完成”，每个里程碑给出依赖和退出标准。

## 57.2 来源等级

| 等级 | 来源 | 可用于何种结论 | 文档标记 |
|---|---|---|---|
| S | 小说正文、作者/版权方公开设定 | 正典事实与名词 | `canon_status=canon` |
| A | 高质量、可交叉核对的专题维基 | 结构化事实索引；仍需避免照抄文案 | `source_tier=A` |
| B | Minecraft/Forge/GeckoLib/Curios 官方文档与官方仓库 | API、生命周期、版本约束 | `source_tier=B-tech` |
| C | Mod 制作论坛、Issue、Reddit/贴吧等社区讨论 | 玩法灵感、踩坑案例、玩家需求 | `canon_status=adaptation` |
| D | 本项目原创设计 | 系统、数值、文案、道具与事件 | `canon_status=original` |

**强制规则**：C/D 级内容不得以“原著明确如此”描述；A 级资料遇到互相冲突时，记录争议并回到 S 级复核。任何数据文件都可以附 `source_refs`，但运行时不需要加载外部网页。

## 57.3 数据条目统一元数据

所有 `pathway/sequence/ability/item/entity/ritual/event/quest` 定义新增以下通用字段：

```json
{
  "id": "pm:example_entry",
  "schema_version": 4,
  "canon_status": "canon|adaptation|original|placeholder",
  "source_tier": "S|A|B-tech|C|D",
  "source_refs": ["LOM-WIKI:Pathways", "FORGE:Registries"],
  "spoiler_level": 0,
  "knowledge_gate": "pm:knowledge/example",
  "links": {
    "requires": ["pm:item/example_material"],
    "produces": ["pm:effect/example"],
    "used_by": ["pm:quest/example_case"],
    "countered_by": ["pm:ritual/example_cleansing"]
  },
  "implementation_state": "planned|data_ready|code_ready|asset_ready|playable|verified"
}
```

`links` 不是 Wiki 装饰字段，而是构建时校验的内容依赖图。不存在的 ID、循环前置、无来源物品、无解除手段的负面状态，均使 CI 失败。

## 57.4 版本优先级与 v0.8 冲突收束

| 冲突 | v0.8 保留文本 | v0.9 解释与最终规则 |
|---|---|---|
| 序列 3–0 不可玩 vs 第 41 章序列 3–1 有限可玩 | 两处都保留 | **默认战役上限仍为序列 4**；服务器可开启 `endgame_saint_mode`，使序列 3–1 进入赛季制、限额、不可永久复制的“实验终局”。序列 0 永不作为常规玩家状态。 |
| 途径转换从序列 4 开始 | v0.8 通用规则 | 大多数相邻途径在序列 4节点处理；“源堡三途径”等特殊组使用数据字段 `switch_entry_sequence` 覆盖，默认从序列 3开始。 |
| 魔药消化被简化为行为积分 | 原有扮演事件仍有效 | 积分只是可观察进度；底层新增精神烙印、准则理解度、重复衰减、扮演过度与身份锚，避免刷行为。 |
| 非凡特性一件对应一个序列 | 原物品继续兼容 | 新 NBT/Codec 将特性表示为“当前最高序列 + 内含低序列层”；旧存档迁移为单层，并由迁移日志注明。 |
| 物品/能力总数等同已实现 | v0.8 总账保留 | v0.9 总账同时给出 `documented/data_ready/playable/verified` 四列，禁止把设计表直接当作完成量。 |

## 57.5 不删减保证与审计方式

- v0.8 原文被包在显式注释边界内；生成脚本对基线计算 SHA-256。
- v0.9 只在基线之前增加封面、在基线之后增加章节。
- 发布前 CI 从 v0.9 文件中截取基线区段并与 `Project_Mystery_Design_Doc_v0_8.md` 做字节级比较。
- 任何将旧段落“顺手修正”的 PR 都必须改为：保留旧段落，并在新章节加入“勘误/覆盖说明”。

# 58. 原著机制一致性审计与补齐矩阵

## 58.1 关键结论

| 原著机制 | v0.8 状态 | v0.9 增量实现 | 影响章节 |
|---|---|---|---|
| 22 条标准途径、序列 9→0 | 已有总册 | 加入路径组、相邻转换入口和来源标记；7 条概设扩为 9–5 详设 | 60–61 |
| 正确配方、扮演法、晋升仪式降低失控风险 | 已有基本循环 | 配方可信度、替代材料、精神烙印、仪式资格与失败恢复统一进同一检定 | 63–65 |
| 特性含有前序特性，额外特性增加负担 | 仅单层特性 | `CharacteristicBundle` 多层模型、额外特性负担、析出与净化玩法 | 63 |
| 特性精神烙印与前主人有关 | 缺失 | 保存来源序列、死亡情绪、存续时间、净化次数，决定低语与消化难度 | 63.3 |
| 扮演是消化精神烙印，不等于失去自我 | 有事件积分 | “准则理解—实践—反思—身份锚”四段；过度扮演触发人格侵蚀 | 64 |
| 高序列神话形态蕴含知识，直视危险 | 有模型与理智消耗 | 引入“知识灼伤”、观察许可、团队分担、混合形态部件 | 61.4、72 |
| 序列 3 可在一定范围回应祈祷，需要锚 | 尊名与祈祷已有 | 祈祷队列、锚稳定度、回应预算、假祈祷与劫持、服务器反滥用 | 61.5、65.6 |
| 锚可以是信众，也可由重要人生痕迹补充 | 未成系统 | 信众锚、关系锚、地点锚、纪念物锚四类；高序列玩家必须维护“人性账本” | 61.6 |
| 相邻途径可转换，但会形成混合与风险 | 有转换管理器 | 加入入口序列、兼容表、旧能力保留上限、神话形态混合、额外特性负担 | 61.2 |
| 灵界/星界与现实分层 | 有灰雾/灵界设想 | 增加灵界航路、坐标漂移、信标、天气泄漏和结构映射 | 67.5 |
| 神秘对普通社会隐蔽 | 有组织/生活 | 引入“神秘暴露度”、目击者处理、报纸掩盖、教会封锁与城市恐慌 | 66.5、73.2 |

## 58.2 正典、改编与原创三栏展示

知识手账每个条目显示三种来源徽标，默认隐藏剧透细节：

- **卷轴徽标（正典）**：条目的世界观基础来自原著，可显示“基础事实”，但不显示小说原句。
- **齿轮徽标（玩法改编）**：为 Minecraft 可玩性做出的数值、交互和时长设计。
- **墨滴徽标（项目原创）**：原创生物个体、封印物、委托、报纸和日记文案。

例如“神话形态”条目：正典栏说明高序列神性形态和直视危险；改编栏说明客户端后处理、知识灼伤条和团队观察机制；原创栏记录本 Mod 的模型骨骼、音效与成就。

## 58.3 剧透等级

| `spoiler_level` | 玩家侧显示 | 解锁条件 |
|---|---|---|
| 0 | 基础玩法与安全提示 | 初始可见 |
| 1 | 低序列名称、常见组织 | 获得第一份神秘学手稿 |
| 2 | 中序列能力与晋升仪式提示 | 到达序列 7 或获得可靠资料 |
| 3 | 相邻途径、高序列与纪元史 | 序列 5、石板研究或管理员开启 |
| 4 | 终局、唯一性、外神/非标准途径 | 赛季终局或明确选择“显示深度剧透” |

服务器可以设置 `spoiler_policy=progressive|open|strict`。客户端本地隐藏不等于服务器不下发：严格模式下高剧透条目仅在解锁后同步，防止资源包/网络包直接泄露。

# 59. 内容关系图、注册规范与互链验收

## 59.1 六层内容图

```
世界层：维度/群系/结构/天气/纪元遗迹
  ↓ 生成与事件
生态层：生物/植物/矿物/灵界生命/失控体
  ↓ 掉落与采集
生产层：工具/工位/加工法/配方/品质/封印
  ↓ 消耗与装备
角色层：途径/序列/能力/状态/扮演/特性
  ↓ 身份与关系
社会层：教会/组织/家族/NPC/经济/通缉/报纸
  ↓ 线索与选择
叙事层：知识/日记/任务/调查板/世界事件/赛季历史
```

每个新增条目至少拥有一条“进入玩法”的入边和一条“离开玩法”的出边。例如材料不能只存在于掉落表中；它至少应连接一张配方、一个委托或一条研究记录。

## 59.2 ID 与标签约定

- 显示名继续全部走本地化；注册 ID 使用中性英文。
- 新增总标签：`pm:mystical_materials`、`pm:ritual_catalysts`、`pm:spirit_world_drops`、`pm:characteristic_containers`、`pm:knowledge_media`、`pm:anchor_objects`、`pm:disguise_items`。
- 跨 Mod 标签优先 `forge:`，项目专属语义才用 `pm:`。
- 兼容配方不得直接引用其他 Mod 的具体物品 ID，除非适配器确认该物品存在；优先通过标签或条件配方。
- 所有迁移过的 ID 写入 `data/pm/migrations/id_aliases.json`，并由 `MissingMappingsEvent` 或数据迁移器处理。

## 59.3 内容链接校验器

构建阶段执行以下检查：

1. 所有 `requires/produces/used_by/countered_by` 指向存在的 ID。
2. 每个负面状态至少有一个解除或缓解路径；“不可解除”必须显式声明并给出倒计时/死亡/事件终点。
3. 每件材料至少被一种配方、研究或委托消费。
4. 每种生物至少有生成规则、掉落表、图鉴条目和一种生态行为。
5. 每条任务至少有失败/放弃恢复，不允许永久锁档。
6. 每个高价值产物至少有两个来源，其中一个不能依赖 PvP。
7. 每项兼容内容在对应 Mod 缺失时必须静默禁用，不得导致数据包加载失败。
8. 所有玩法原创条目都含 `canon_status=adaptation|original`。

## 59.4 运行时关系查询

神秘学手账新增“关系页”：点击任意条目可查看“从哪里来、可以做什么、谁会需要、什么能克制”。JEI/EMI 只负责已解锁的加工图；知识手账负责世界观与线索；Jade/WTHIT 负责场景内轻量提示，三者不互相替代。


# K. 全途径与高序列扩展（v0.9 新增）

# 60. 第四批七途径：序列 9–5 全详设

本章把 v0.8 第 7.12 节的七条概设提升为与前 15 条途径相同的制作规格。能力名称与效果采用“正典基础 + Minecraft 改编”的双层表达；具体数值由平衡表管理，不在本章锁死。每条途径至少具备信息、战斗、生活/组织和风险管理四类能力，避免只有 PvP 价值。

## 60.1 律师途径（黑皇帝组）

- **注册 ID**：`pm:lawyer`
- **核心循环**：“语言—交易—扭曲”闭环。玩家通过建立有利规则、诱导对手接受条件，再扭曲规则边界取得优势。所有能力都受「契约见证」与「混乱债务」约束。
- **专属资源**：`argument（论证点）/ disorder_debt（混乱债务）`
- **正典口径**：序列名称与途径归属按资料核对；技能交互、数值、物品和仪式条件属于玩法改编。

| 序列 | 名称 | 能力组（每项均需独立数据文件） | 扮演事件样例 | 关键物品 | 边界与风险 |
|---|---|---|---|---|---|
| 9 | 律师 | **条款洞察**：标出交易、委托和 NPC 对话中的模糊条款；不直接给最优答案。<br>**有利陈述**：在证据充足时提高说服/交易成功率；伪造证据会积累通缉。<br>**口头约定**：双方确认后生成短时契约，违约者获得“失信”状态。<br>**辩护**：消耗论证点，为目标暂时削弱通缉增长或组织处罚。 | • 替他人解释一份真实条款<br>• 在不撒谎的情况下争取更好条件<br>• 发现并主动指出对自己不利的漏洞 | 墨水契据、铜制印章 | 把“会说话”误当无条件魅惑；无证据陈述反噬。 |
| 8 | 野蛮人 | **强制谈判**：近距离打断施法/交互并进入短暂对峙界面。<br>**蛮横索取**：以混乱债务换取一次强制交换；目标可付代价拒绝。<br>**抗规训**：降低减速、沉默、缴械持续时间。<br>**破门而入**：对门、锁、脆弱结界造成额外结构伤害。 | • 在公开场合打破不合理规则<br>• 承受惩罚仍坚持诉求<br>• 以力量保护弱势委托人 | 裂纹法槌、粗制护腕 | PvP 强制交互必须尊重服务器同意设置。 |
| 7 | 贿赂者 | **价值标记**：看见 NPC/玩家当前最在意的资源类别，而非具体背包。<br>**利益输送**：向 NPC、组织代理或事件节点支付资源换取偏转。<br>**代价转移**：把一项即将到来的小型负面效果转为金钱/耐久损失。<br>**收买证人**：降低目击者报告神秘事件的概率；高警觉者可能反向举报。 | • 用合适而非昂贵的筹码解决冲突<br>• 识别无法被收买的人<br>• 主动偿还一次不正当收益 | 空白汇票、镀银钱夹 | 不能把所有关系简化成货币；连续贿赂会形成证据链。 |
| 6 | 腐化男爵 | **腐化领域**：在小范围内放大贪婪、争执和交易偏差。<br>**规则扭曲**：把一个数值条件暂时向有利方向偏移，但不得改变事件本质。<br>**腐化赠礼**：给予带隐性代价的增益；接受者能通过鉴定发现。<br>**债务追索**：远程触发已签契约的违约惩罚。 | • 让一套表面公正的制度暴露偏差<br>• 以规则而非暴力获胜<br>• 承担自己制造的长期后果 | 黑铁爵印、腐化账簿 | 领域叠加会提高城市恐慌与教会关注。 |
| 5 | 混乱导师 | **秩序裂解**：暂时禁用区域内一种非核心规则，如红石时序、门禁或队伍共享。<br>**悖论命令**：发布两难命令，目标选择其一并承担对应代价。<br>**混乱教义**：为队伍建立可变战术规则，每次变更消耗锚稳定度。<br>**扭曲判定**：把一次失败改为“有代价的成功”，代价由系统从安全池选择。 | • 设计一场不靠击杀的秩序崩解<br>• 让敌方规则互相冲突<br>• 在混乱结束后重建可运行秩序 | 逆序法典、双面权杖 | 序列 5 仪式要求在不造成永久毁档的前提下推翻一套稳定规则并建立替代规则。 |

## 60.2 仲裁人途径（审判者组）

- **注册 ID**：`pm:arbiter`
- **核心循环**：“调查—宣布—执行”闭环。权威来自证据、见证和管辖范围；无证据滥用权威会生成「僭越」并削弱后续命令。
- **专属资源**：`authority（权威）/ jurisdiction（管辖）`
- **正典口径**：序列名称与途径归属按资料核对；技能交互、数值、物品和仪式条件属于玩法改编。

| 序列 | 名称 | 能力组（每项均需独立数据文件） | 扮演事件样例 | 关键物品 | 边界与风险 |
|---|---|---|---|---|---|
| 9 | 仲裁人 | **争议感知**：发现交易、领地和委托中的冲突点。<br>**临时裁定**：对低风险争议给出可执行裁定，双方接受后获得履约奖励。<br>**证词记录**：把 NPC/玩家陈述写入可验证记录。<br>**止争**：短暂降低附近中立生物和 NPC 的敌意。 | • 公平解决一次双方都不满意的争议<br>• 在朋友与规则冲突时保持一致标准<br>• 承认并修正错误裁定 | 仲裁徽章、证词册 | 不能凭序列身份自动获得真相。 |
| 8 | 治安官 | **警戒线**：布置区域边界，越界触发提示与证据记录。<br>**追踪令**：基于证据追踪目标留下的行动痕迹。<br>**缴械警告**：先警告、后判定；无视警告的目标更易被缴械。<br>**巡逻加护**：在已登记聚落内提高移动与感知。 | • 完成连续三夜巡逻<br>• 不使用私刑抓捕嫌疑人<br>• 保护一个曾反对自己的居民 | 铜星徽、警戒绳 | 治安官不是免费雷达；追踪只显示线索节点。 |
| 7 | 审讯者 | **矛盾标记**：比较多份证词，标出相互矛盾处。<br>**精神压迫**：对有明确罪证者施加压力；对无辜目标会反噬。<br>**沉默令**：短时禁止目标使用聊天触发型仪式/吟唱能力。<br>**证据回响**：在案发地重现模糊声音与动作轮廓。 | • 用证据而非恐惧获得供述<br>• 面对沉默仍完成调查<br>• 释放一个证据不足的嫌疑人 | 审讯灯、回声蜡筒 | 不提供现实中的审讯技巧；仅为游戏化证据系统。 |
| 6 | 法官 | **法庭领域**：把区域转为临时法庭，登记参与者与行为。<br>**禁止**：声明一种具体行为，重复越界者受递增惩罚。<br>**许可**：授权某种原本受限的交互，降低误伤与组织处罚。<br>**判决执行**：根据已确认罪证施加禁足、缴械或赔偿。 | • 公开审理重大案件<br>• 让权力接受旁听与复核<br>• 执行对己方不利的判决 | 黑木法台、银边法袍 | 模糊或过宽的禁止自动无效。 |
| 5 | 惩戒骑士 | **律令冲锋**：沿已宣布的执法目标高速突进。<br>**群体禁令**：在大型事件中发布一条可读、可反制的区域规则。<br>**罪证铠甲**：证据越完整，防御越高；错判时铠甲崩解。<br>**秩序锚定**：短暂稳定被混乱、扭曲或幻觉影响的区域。 | • 在混乱事件中维持一条可验证秩序<br>• 亲自承担错误命令的惩罚<br>• 保护裁决程序而非个人威望 | 惩戒长枪、法则锁链 | 仪式要求在全服可见的混乱事件中维持秩序并接受一次公开复核。 |

## 60.3 罪犯途径（深渊组）

- **注册 ID**：`pm:criminal`
- **核心循环**：“欲望—堕落—恶意预感”。该路线定位为高风险反派/救赎双轨；服务器可禁用主动伤害型扮演，所有成长都提供 PvE、调查或自我约束替代。
- **专属资源**：`desire（欲望）/ depravity（堕落度）`
- **正典口径**：序列名称与途径归属按资料核对；技能交互、数值、物品和仪式条件属于玩法改编。

| 序列 | 名称 | 能力组（每项均需独立数据文件） | 扮演事件样例 | 关键物品 | 边界与风险 |
|---|---|---|---|---|---|
| 9 | 罪犯 | **恶意感知**：感知附近针对自己的明确敌意。<br>**痕迹抹除**：降低普通追踪痕迹，不影响高阶占卜。<br>**趁隙攻击**：对处于恐惧/混乱的敌对生物增伤。<br>**罪迹**：每次主动伤害中立目标积累不可隐藏的罪迹。 | • 在危险环境中识别真实恶意<br>• 拒绝一次有利但越界的选择<br>• 追查另一名罪犯留下的痕迹 | 暗红手套、污迹短刃 | 不要求或奖励现实式犯罪；核心是风险与自控。 |
| 8 | 折翼天使 | **坠落滑翔**：从高处展开暗影膜滑翔，受光照限制。<br>**诱惑抵抗**：看见自身欲望条并主动压制一次冲动。<br>**不祥外貌**：恐吓敌对生物，降低普通 NPC 好感。<br>**堕落羽片**：投出羽片造成短时虚弱和污染暴露。 | • 在光照压制下完成任务<br>• 帮助一个害怕自己的 NPC<br>• 主动净化一件污染物 | 焦黑羽片、裂翼披肩 | 外形变化必须支持隐私/外观关闭。 |
| 7 | 连环杀手 | **模式分析**：连续击败同类敌对生物后看见其弱点，但重复收益快速衰减。<br>**猎场布置**：为 Boss/怪物布置标记陷阱。<br>**恐惧连锁**：敌对生物死亡时向同阵营传播恐惧。<br>**罪行签名**：主动留下可追踪签名，换取更高战利品与更高通缉。 | • 追踪并终止一只连续作恶的精英怪<br>• 故意留下线索接受追捕挑战<br>• 打破自己的重复猎杀模式 | 签名刀鞘、猎场图纸 | 名称来自途径序列，但玩法避免鼓励伤害玩家或平民。 |
| 6 | 恶魔 | **危险预感**：对真正致命的近期危险获得强烈预警。<br>**熔硫体魄**：获得火焰与毒抗性，接触圣光时受抑制。<br>**恶意诅咒**：对已伤害自己的敌对目标施加可净化诅咒。<br>**深渊投影**：短暂显露魔化肢体，造成范围击退与恐惧。 | • 顺从预感避开一次灾难<br>• 在有能力报复时选择克制<br>• 击败来自深渊裂隙的同类 | 硫磺核心、深渊角质 | 预感有冷却和噪声，不可用于透视所有危险。 |
| 5 | 欲望使徒 | **欲望弦线**：观察目标当前占优势的情绪欲望类别。<br>**情绪引爆**：放大已有情绪，不凭空制造；可被心理防护反制。<br>**欲望替身**：把一次控制效果转移到预先储存的欲望容器。<br>**堕落契约**：与自愿目标交换增益和长期代价。 | • 识别并拒绝自己最强烈的欲望<br>• 利用欲望阻止而非制造灾难<br>• 让一个堕落契约在知情同意下完成 | 七情瓶、欲望面具 | 仪式要求在强烈欲望环境中保持自我并完成目标，禁止以非自愿玩家为仪式对象。 |

## 60.4 囚犯途径（被缚者组）

- **注册 ID**：`pm:prisoner`
- **核心循环**：“克制—诅咒—释放”。玩家在月相、伤害、饥饿与情绪刺激下积累变形压力，主动束缚可换取稳定，失控释放则提供力量但产生长期代价。
- **专属资源**：`restraint（克制）/ transformation_pressure（变形压力）`
- **正典口径**：序列名称与途径归属按资料核对；技能交互、数值、物品和仪式条件属于玩法改编。

| 序列 | 名称 | 能力组（每项均需独立数据文件） | 扮演事件样例 | 关键物品 | 边界与风险 |
|---|---|---|---|---|---|
| 9 | 囚犯 | **自我束缚**：装备限制器降低属性，却持续减缓污染与压力。<br>**忍耐**：在受控负面状态下逐步获得抗性。<br>**锁链熟练**：锁链工具可牵引敌对生物或固定物品。<br>**越狱直觉**：更快发现牢门、封印与结构薄弱点。 | • 自愿完成一段受限旅程<br>• 不借助暴力逃离囚笼<br>• 帮助他人解除束缚 | 束缚腕环、旧锁链 | 受限玩法必须随时可退出，退出只损失成长机会。 |
| 8 | 疯子 | **痛觉转化**：把部分伤害转为变形压力。<br>**混乱步法**：受控制时获得不可预测短位移。<br>**狂笑震慑**：对敌对生物造成短时失衡。<br>**清醒窗口**：在压力临界时主动获得数秒完全清醒并重置技能。 | • 在高压力下完成精确操作<br>• 记录并理解一次冲动<br>• 主动寻求治疗而非放任失控 | 裂笑面具、镇静针盒 | 疯狂表现可在可访问性设置中改为抽象图标。 |
| 7 | 狼人 | **月相变身**：夜间或月光下切换狼人形态。<br>**嗅迹**：追踪受伤敌对生物的气味节点。<br>**撕裂**：叠加可包扎的流血状态。<br>**银惧**：受到银制/圣化装备额外压制。 | • 在满月不失控地保护聚落<br>• 用嗅迹找回失踪者<br>• 主动结束一次有利变身 | 月痕皮、银扣项圈 | 变身受月相与克制条双重控制。 |
| 6 | 活尸 | **尸化体魄**：提高护甲、击退抗性和水下存活，降低食物收益。<br>**死亡迟缓**：致命伤时进入短暂迟缓倒地，可被仪式救回。<br>**寒尸之触**：近战施加减速与灵性恢复抑制。<br>**腐败耐受**：免疫普通中毒，圣水会造成短时虚弱。 | • 背负队友穿越致命环境<br>• 在濒死状态保护目标<br>• 维持生活习惯对抗尸化 | 冷尸心、缝合外套 | 尸化不是纯增益，社交与恢复均有代价。 |
| 5 | 怨魂 | **灵体化**：短时穿越薄墙与物理攻击，受灵性武器克制。<br>**镜面跳跃**：在已标记镜面间移动。<br>**附身**：仅可附身允许的 NPC/生物模板；玩家必须明确同意。<br>**怨念尖啸**：释放积累怨念造成恐惧和灯光熄灭。 | • 不借附身完成一次调查<br>• 释放一段旧怨而非报复<br>• 从镜面迷宫中带回他人 | 怨镜碎片、幽纱披风 | 仪式要求在强烈怨念场所保持克制并完成和解或封印。 |

## 60.5 窥秘人途径（隐者组）

- **注册 ID**：`pm:mystery_pryer`
- **核心循环**：“窥见—记录—承受知识”。能力越强，知识负荷越高；玩家要通过分类、封印、教学和遗忘仪式管理知识，而不是无成本施法。
- **专属资源**：`occult_load（神秘负荷）/ prepared_scrolls（预备卷轴）`
- **正典口径**：序列名称与途径归属按资料核对；技能交互、数值、物品和仪式条件属于玩法改编。

| 序列 | 名称 | 能力组（每项均需独立数据文件） | 扮演事件样例 | 关键物品 | 边界与风险 |
|---|---|---|---|---|---|
| 9 | 窥秘人 | **神秘感应**：发现附近未识别的仪式、符号和神秘物品。<br>**象征抄录**：复制低阶符号到笔记，但不会自动理解。<br>**危险阅读**：临时阅读高剧透条目并承担知识负荷。<br>**知识封签**：把条目封存，降低低语但暂时不可用。 | • 正确分类三种未知材料<br>• 拒绝阅读一份明显危险资料<br>• 把知识教给真正需要的人 | 窥秘镜片、封签本 | 知识解锁与玩家真实查 Wiki 分离，服务器只控制游戏内效果。 |
| 8 | 格斗学者 | **术式步法**：按预备符号组合获得短时战斗姿态。<br>**知识反击**：识别敌对能力类别后获得一次针对性格挡。<br>**符号拳印**：近战留下可被卷轴触发的标记。<br>**身体记忆**：重复训练降低卷轴施放失败率。 | • 用已知克制击败敌人<br>• 在实战中验证一条理论<br>• 承认并修正错误分类 | 符文绷带、演武札 | 不做单纯数值武僧，核心是知识转化为动作。 |
| 7 | 巫师 | **元素术式**：从已学卷轴选择火、霜、风、光等基础术式。<br>**诅咒解除**：按对应关系解除低中阶诅咒。<br>**使魔契约**：与神秘生物建立有限、可终止的协作。<br>**快速施术**：牺牲稳定度缩短一次卷轴读条。 | • 完成一套正确对应关系的施法<br>• 照料使魔而非消耗它<br>• 为他人解除未知诅咒 | 术式杖、使魔铃 | 术式由数据驱动，不能硬编码为固定魔法列表。 |
| 6 | 卷轴教授 | **卷轴编纂**：把已掌握能力制作成有限次数卷轴。<br>**课堂领域**：队友在范围内学习速度提高，施法失败率降低。<br>**反制注释**：为卷轴添加一个明确反制条件。<br>**知识借阅**：临时共享知识条目，归还后保留摘要不保留配方。 | • 编写可被他人成功使用的卷轴<br>• 公开一个对己方不利的反制条件<br>• 修复一份错误教材 | 教授羽笔、注释台 | 复制能力受序列、知识许可和版权映射三重约束。 |
| 5 | 星象师 | **星图投影**：根据时间、天气与方位提供概率性事件预报。<br>**星辉仪式**：在露天星光下提高仪式稳定性。<br>**轨迹偏转**：小幅改变投射物或移动实体的路径。<br>**天象封印**：把一次异常天气封入星盘，之后释放或研究。 | • 连续记录一轮月相和天气<br>• 准确预警而不夸大确定性<br>• 在不利天象中完成仪式 | 黄铜星盘、深蓝观测袍 | 仪式要求完成长期观测，并在罕见天象中主持公开预测。 |

## 60.6 通识者途径（完美者组）

- **注册 ID**：`pm:savant`
- **核心循环**：“理解—制造—迭代”。能力不凭空创造科技；玩家需要扫描、拆解、绘图、原型、测试和维护。与 Create/IE/AE2 的兼容均通过蓝图和标签实现。
- **专属资源**：`insight（洞见）/ prototype_stability（原型稳定度）`
- **正典口径**：序列名称与途径归属按资料核对；技能交互、数值、物品和仪式条件属于玩法改编。

| 序列 | 名称 | 能力组（每项均需独立数据文件） | 扮演事件样例 | 关键物品 | 边界与风险 |
|---|---|---|---|---|---|
| 9 | 通识者 | **快速理解**：首次观察原版工位或机器时生成结构摘要。<br>**基础修理**：以材料修复工具并保留部分附魔。<br>**测量**：读取速度、应力、能耗或红石状态。<br>**草图**：把小型结构保存为不可自动放置的蓝图。 | • 修复陌生设备<br>• 解释一项机制给他人<br>• 用更少资源完成同一功能 | 测量尺、工程草图 | 不能绕过其他 Mod 的进度或配方锁。 |
| 8 | 考古学家 | **年代判读**：估计遗物所属纪元与可信度。<br>**精细发掘**：使用刷具/支架降低遗物破损。<br>**遗址拼图**：把散落结构线索组合成坐标范围。<br>**旧物共鸣**：读取遗物的一段模糊使用痕迹。 | • 完整发掘一处遗址<br>• 归还具有归属的文物<br>• 识别一件现代伪造品 | 考古刷组、样本箱 | 遗物叙事全部原创，避免复刻小说段落。 |
| 7 | 鉴定师 | **材质鉴定**：识别品质、污染和伪装。<br>**价值区间**：给出市场区间而非固定价格。<br>**弱点标注**：为装备/构造体标记维护弱点。<br>**真伪印章**：对物品签发可追溯鉴定报告。 | • 识破高价值赝品<br>• 为普通物品给出公正估价<br>• 公开撤销一次错误鉴定 | 放大镜组、鉴定印章 | 市场价格仍由供需与服务器配置决定。 |
| 6 | 机械专家 | **模块化构装**：制作可替换模块的机械仆从。<br>**远程维护**：在视线内修复登记机器。<br>**过载**：短时提高机器效率并积累磨损。<br>**故障预判**：在故障前显示原因类别和剩余安全时间。 | • 让原型稳定运行一个游戏日<br>• 在过载前主动停机维护<br>• 用机械解决非战斗问题 | 齿轮工具箱、机械核心 | 实体数量、区块加载与路径 AI 受严格性能预算。 |
| 5 | 天文学家 | **精密天文台**：建立多方块观测结构。<br>**轨道计算**：预测月相、流星、异常天气与灵界潮。<br>**星光聚焦**：把天光转为仪式能量或设备校准。<br>**远距通信**：在已校准天线之间传递低带宽数据。 | • 建立公开天文台<br>• 完成一次跨区域联合观测<br>• 用预测避免灾害而非牟利 | 精密望远镜、星轨计算机 | 仪式要求在极端天象中保持观测链完整，并公开一份可验证预测。 |

## 60.7 怪物途径（命运之轮组）

- **注册 ID**：`pm:monster`
- **核心循环**：“预兆—概率—代价守恒”。不是随意改骰子，而是把好运、厄运与未知结果在时间和对象间转移。服务器保留完整审计，防止经济复制。
- **专属资源**：`fortune（幸运势）/ entropy_debt（熵债）`
- **正典口径**：序列名称与途径归属按资料核对；技能交互、数值、物品和仪式条件属于玩法改编。

| 序列 | 名称 | 能力组（每项均需独立数据文件） | 扮演事件样例 | 关键物品 | 边界与风险 |
|---|---|---|---|---|---|
| 9 | 怪物 | **危险预兆**：以身体反应提示近期异常，但不说明来源。<br>**命运观察**：查看事件的概率档位：极低/低/中/高。<br>**被动避祸**：极低概率自动规避环境伤害，消耗幸运势。<br>**不祥吸引**：稀有事件更容易靠近，同时危险事件权重也提高。 | • 记录并验证三个预兆<br>• 接受一次无法解释的坏运<br>• 提醒他人而不夸大预言 | 预兆硬币、命纹绷带 | UI 不显示精确随机数。 |
| 8 | 机器 | **情绪平稳**：降低恐惧与冲动影响，代价是社交增益下降。<br>**概率记录**：自动记录近期随机结果用于玩家分析。<br>**机械步调**：固定节奏操作减少失误波动。<br>**命运校准**：把极端好运/坏运拉回平均一次。 | • 保持稳定完成高压任务<br>• 用记录推翻主观印象<br>• 在同伴失控时提供稳定锚 | 节拍器、概率日志 | “机器”是行为方式，不改变玩家为机械种族。 |
| 7 | 幸运儿 | **幸运偏置**：提高一次普通战利品或躲避结果，随后增加熵债。<br>**巧合援助**：在开放世界生成合情合理的小型帮助。<br>**厄运延期**：延后一个可延期负面事件。<br>**分享好运**：把幸运势给予队友，自己承担部分熵债。 | • 把好运用于帮助他人<br>• 在好运后主动偿还熵债<br>• 不依赖幸运完成一次挑战 | 四叶胸针、幸运骰盅 | 禁止作用于真实付费抽奖或服务器经济结算。 |
| 6 | 灾祸教士 | **灾祸标记**：标记一个地点，使积累的熵债优先在那里结算。<br>**小型灾变**：释放可预告、可疏散的环境灾害。<br>**厄运传导**：把自身厄运转移到准备好的替代物。<br>**灾后祈福**：灾祸结束后提高修复和救援效率。 | • 提前预警自己召来的灾祸<br>• 组织灾后救援<br>• 让灾祸落在无人区域 | 灾祸香炉、替灾人偶 | 事件必须有倒计时、范围提示和管理员取消能力。 |
| 5 | 赢家 | **胜势积累**：通过连续正确决策而非纯随机获得胜势。<br>**关键一掷**：消耗全部胜势，把一次中等概率结果提升一档。<br>**失败保险**：为重大行动预设可接受失败分支。<br>**命运反转**：在熵债完全结算后把一次灾难性结果改为重伤/重大损失。 | • 在信息不足时做出并承担选择<br>• 预先设计失败方案<br>• 赢得挑战后把收益分给参与者 | 胜利筹码、命运轮盘 | 仪式要求在公开竞赛中连续获胜，同时不操纵或伤害无辜参与者。 |

## 60.8 七途径制作验收

| 验收项 | 最低标准 |
|---|---|
| 数据 | 每个序列 1 个 sequence JSON、至少 4 个 ability JSON、6 个 acting_event、1 个失控表、1 套本地化。 |
| 玩法 | 每个序列至少 1 个非战斗能力、1 个团队或社会能力、1 个明确反制。 |
| 资产 | 序列 9–7 使用原版骨骼/物品动画；序列 6–5 至少 1 套独立施法动画与 4 种粒子组合。 |
| 平衡 | 同序列 10 分钟战斗测试、30 分钟调查测试、2 小时生存测试均不得出现唯一最优循环。 |
| 多人 | 强制、附身、审讯、契约、情绪操控等能力遵守 `consent_sensitive` 标记和服务器策略。 |
| 正典 | 所有声称为正典的序列名、途径归属和高层规则进入 `docs/canon_audit.csv`；原创能力不得标 canon。 |

# 61. 高序列、相邻途径、神话形态、祈祷与锚 v2

## 61.1 序列层级的统一运行规则

| 序列段 | 运行方式 | 玩家常规可达 | 服务器资源约束 | 世界反馈 |
|---|---|---:|---|---|
| 9–8 | 个人能力与生活职业 | 是 | 普通 Capability 数据 | 零散目击、低级组织关注 |
| 7–6 | 中序列调查、仪式、组织行动 | 是 | 区域事件与能力限流 | 城市封锁、报纸掩盖、猎巫 |
| 5 | 领域雏形、晋升仪式 | 是 | 领域同时在线数量限制 | 区域天气、组织政治、祈祷雏形 |
| 4 | 半神、神话形态 | 默认终局 | 神话形态渲染预算、区域唯一事件 | 城市/群系级影响，教会最高警戒 |
| 3 | 圣者高位与有限祈祷响应 | 可选赛季模式 | 每阵营/途径限额，离线代理严格限制 | 维度级事件、锚政治 |
| 2–1 | 天使/唯一性竞争 | 可选叙事化模式 | 全服唯一性、赛季重置或历史封存 | 纪元事件与世界法则临时改变 |
| 0 | 真神 | 不作为普通玩家状态 | 仅剧情投影、管理员事件或结局镜头 | 世界状态结算，不允许永久超管能力 |

## 61.2 相邻途径转换矩阵 v2

`adjacent_groups/*.json` 新增字段：

```json
{
  "id": "pm:group_lord_of_mysteries",
  "members": ["pm:seer", "pm:apprentice", "pm:marauder"],
  "default_switch_entry_sequence": 3,
  "entries": [
    {
      "from": "pm:seer",
      "to": "pm:apprentice",
      "min_sequence": 3,
      "retained_ability_slots": 2,
      "mythical_blend": ["thread_tentacle", "star_door_rune"]
    }
  ],
  "incompatibility_cost": {
    "pollution": 18,
    "extra_characteristic_load": 1,
    "identity_fracture": 12
  }
}
```

转换流程不是“点按钮洗职业”，而是：取得目标途径配方与特性 → 完成原途径消化 → 进行相邻资格审查 → 准备转换仪式 → 选择保留能力 → 承担混合特性与神话形态变化 → 进入 3–5 小时稳定期。失败不会直接删档，而是从“序列不升、部分材料损坏、额外特性增加、产生混合失控体”中按风险选择结果。

## 61.3 混合 Build 规则

- 旧途径最多保留 2 个能力槽，且必须低于当前序列至少 1 级。
- 保留能力拥有 `foreign_pathway_cost_multiplier=1.35`，并产生对应异色粒子，便于 PvP 识别。
- 不能保留被目标途径明确排斥的核心权柄能力。
- 每个混合能力增加 1 点“特性负担”；负担达到阈值后，玩家必须通过仪式析出额外特性、接受永久副作用或放弃能力。
- 神话形态由部件标签合成，不允许每种组合都制作独立模型；GeckoLib 通过可隐藏骨骼和材质层组合。

## 61.4 神话形态：知识灼伤与安全观察

神话形态不只是大招皮肤。激活后同时运行四个通道：

1. **形态通道**：骨骼、材质、发光层、局部透明与体积粒子。
2. **权柄通道**：领域能力、被动规则和环境影响。
3. **知识通道**：附近观察者获得 `mythic_knowledge_burn`，强度取决于双方序列差、观察时间、遮蔽物和灵视状态。
4. **人性通道**：使用者持续消耗理智与锚稳定度；脱离形态后进入恢复期。

观察保护手段包括：关闭灵视、背对目标、佩戴教会认证滤镜、处于仪式保护圈、由高序列队友分担、观看低保真投影。客户端“减少闪烁/恐怖呈现”只替换视觉，不取消服务器伤害判定。

### 知识灼伤状态阶梯

| 层数 | 表现 | 游戏效果 | 处理 |
|---:|---|---|---|
| 1 | 余像、耳鸣 | 手账出现无法解读符号 | 离开视线 10 秒 |
| 2 | 概念重叠 | 技能栏短时错位、占卜噪声提高 | 遮眼、祈祷或镇静仪式 |
| 3 | 知识侵入 | 随机解锁“破碎知识”，同时污染上升 | 教会治疗/记忆封存 |
| 4 | 临界崩解 | 强制跪倒、持续失控检定 | 团队救援或紧急断视 |

## 61.5 祈祷队列与回应预算

序列 3 及以上、或获得特殊灰雾权限的存在，可注册尊名并接收祈祷。为避免离线外挂与骚扰，服务器使用异步**事件队列**而不是让玩家永久在线监听：

- 祈祷包含发起者、尊名解析结果、地点模糊区、请求类型、供品摘要、可信度与污染标记。
- 接收者在线时通过灰雾/神国界面查看；离线时只允许预先配置的安全回应，如“记录、拒绝、发送固定祝福”。
- 每个游戏日有 `response_budget`，强回应消耗更多锚与灵性。
- 错误尊名、相似尊名和被污染祭坛可能把请求路由给未知存在；新手教学先用无害的模拟仪式演示。
- 任何祈祷都不能读取玩家私聊或现实账号信息。

## 61.6 锚系统：从粉丝数改为“人性网络”

| 锚类型 | 来源 | 稳定优势 | 风险 | 可玩任务 |
|---|---|---|---|---|
| 信仰锚 | 教会、信徒、公开仪式 | 数量大、恢复快 | 教义偏移、组织政治、伪信徒 | 布道、救援、维护教堂、回应祈祷 |
| 关系锚 | 队友、家族、长期 NPC 关系 | 人性恢复高 | 人员死亡或背叛冲击大 | 共同行动、纪念日、承诺与和解 |
| 地点锚 | 家园、城市、纪念地、墓园 | 稳定且适合单人 | 地点被毁或污染 | 修复、守护、重建历史地标 |
| 物件锚 | 日记、照片、礼物、旧装备 | 可携带、适合备份 | 被窃、损毁、伪造 | 维护、寻回、鉴定真伪 |
| 事迹锚 | 重要人生经历与公开记忆 | 不占实体槽位 | 被历史扭曲或遗忘 | 编年史、报纸、口述历史、雕像 |

锚稳定度不是普通“蓝条”。达到低阈值时，玩家的行为建议、低语和神话形态外观会逐渐趋向途径本能；恢复需要完成与自身身份有关的生活行为，而不只是吃药。

## 61.7 唯一性与高序列的服务器公平

- 唯一性物品在全服只有一个激活实例；复制品自动变为“历史投影”，只能用于研究或剧情。
- 持有者 14 天不上线时，不直接没收；进入“沉睡保管”，组织可通过公开事件竞争托管权。
- 唯一性不可放进末影箱、AE2 无损复制网络或跨维度复制容器；兼容层统一通过 `pm:uniqueness_blacklisted_storage`。
- 终局能力只改变明确白名单内的世界规则，绝不授予管理指令、任意 NBT 修改或读取服务器文件。
- 赛季结束后，持有记录写入活历史，物品可转为纪念版，下一赛季重新进入世界循环。

# 62. 《宿命之环》与非标准途径的可选扩展框架

## 62.1 默认策略

v0.9 不把非标准途径直接塞进主线，也不在未完成逐项核对前承诺全部能力。它们以独立实验包 `pm_coi_expansion` 存在，默认关闭，并受四项开关控制：

```toml
[content]
enable_circle_of_inevitability_expansion = false
enable_boon_system = false
show_nonstandard_pathway_spoilers = false
allow_outer_deity_events = false
```

专题维基已把非标准途径和第二部内容纳入分类，因此 v0.9 预留注册、UI、污染与任务接口；具体名称、序列、能力和故事文本必须在单独的版权/正典审计后进入数据包。

## 62.2 “恩赐”与魔药体系的差异化玩法

| 维度 | 魔药途径 | 恩赐/非标准途径扩展 |
|---|---|---|
| 力量来源 | 配方、特性、消化、仪式 | 与高位存在建立联系并接受授予 |
| 主要风险 | 精神烙印、额外特性、失控 | 控制权、污染、身份依附、恩赐撤回 |
| 成长资源 | 材料、知识、特性 | 仪式关系、献祭、契约、阵营任务 |
| 死亡循环 | 凝聚特性或沉积 | 部分力量回收、留下污染痕迹或契约债务 |
| 反制 | 净化、析出、相邻转换 | 切断联系、伪装信号、转移契约、组织庇护 |

## 62.3 外神事件设计边界

- 高位存在只通过天象、梦境、污染区、代理人和事件投影呈现，不直接生成可被普通武器击杀的“血条 Boss”。
- 玩家可以阻止一次降临、封闭锚点、救出被影响者、破坏仪式或改变事件结果，但不能用刷怪塔量产高位存在掉落。
- 非标准内容与原版主线完全可拆卸；关闭数据包后，旧物品迁移为无能力纪念物，不崩档。
- 服务器可把非标准途径设为 NPC/事件专用，保留世界观威胁而不开放玩家成长。

# L. 魔药、特性、扮演与神秘学闭环（v0.9 新增）

# 63. 魔药与非凡特性 v2：多层特性、精神烙印、替代材料与析出

## 63.1 `CharacteristicBundle` 数据模型

一份高序列特性不再只是 `{Pathway, Sequence, Purity}`。新模型显式保存其中包含的低序列层：

```json
{
  "pathway": "pm:seer",
  "highest_sequence": 5,
  "layers": [
    {"sequence": 9, "count": 1, "purity": 0.98},
    {"sequence": 8, "count": 1, "purity": 0.96},
    {"sequence": 7, "count": 1, "purity": 0.94},
    {"sequence": 6, "count": 1, "purity": 0.92},
    {"sequence": 5, "count": 1, "purity": 0.90}
  ],
  "imprint": {
    "former_owner_sequence": 5,
    "death_emotion": "despair",
    "age_ticks": 84000,
    "cleansing_count": 0,
    "dominance": 0.64,
    "whisper_pool": "pm:imprint/seer_despair"
  },
  "corruption": 0.12,
  "source_uuid_hash": "server_salted_hash"
}
```

`source_uuid_hash` 只用于同一服务器去重和历史审计，不保存或显示真实 UUID。旧 v0.8 特性迁移为单层束，纯度不变，精神烙印使用中性默认值。

## 63.2 额外特性负担

玩家体内每个序列允许的标准层数为 1。多出的同途径低/同序列特性不会直接提供翻倍能力，而会形成：

- 灵性上限小幅提高；
- 能力失控权重提高；
- 精神烙印低语叠加；
- 析出难度提高；
- 死亡时凝聚更复杂、价值更高的特性束。

额外特性负担以 `extra_load` 计数，不做隐藏惩罚。玩家可通过高阶析出仪式、死亡重生配置、组织净化服务、特定封印物或转途径处理；任何路线都应付出资源或风险，但不强制删档。

## 63.3 精神烙印强度

烙印强度由以下因素组成：

```
imprint = base(sequence)
        × owner_sequence_factor
        × recency_factor
        × death_emotion_factor
        × possession_duration_factor
        × (1 - cleansing_reduction)
```

- 前主人序列越高、持有越久，烙印越强。
- 刚凝聚的特性最活跃，随世界时间逐步沉静，但不会自然消失。
- 极端死亡情绪改变低语池与幻觉主题，不等同提高全部数值。
- 净化能削弱烙印，同时可能损失纯度；完美净化需要珍贵材料和可靠知识。
- 同一来源的多份碎片重组时，烙印可能恢复，防止“打碎洗白”成为零代价方案。

## 63.4 配方可靠度与替代材料

魔药配方新增四层结构：核心特性/主材料、辅助材料、处理方式、禁忌。辅助材料允许在同一对应关系标签内替代，但会改变品质：

| 变化 | 结果 |
|---|---|
| 同标签、相近灵性 | 品质小幅波动，正常可饮用 |
| 同标签但污染更高 | 污染品质，额外低语与副作用 |
| 对应关系相反 | 凝固、爆炸、生成异常药渣或错误魔药 |
| 核心特性序列错误 | 无法晋升，可能触发混合特性或严重失控 |
| 辅料数量轻微偏差 | 影响稳定性而非必然失败 |
| 处理顺序错误 | 产生可鉴定的颜色、气味或灵性异常线索 |

这既保留“正确配方重要”，也让世界生成和整合包中的材料替换具有可玩空间。

## 63.5 魔药制作的五阶段交互

1. **鉴材**：灵视、鉴定师、组织实验室或手账对材料定性。
2. **预处理**：研磨、蒸馏、晾晒、月光浸泡、火焰炙烤、灵界冷却等。
3. **调配**：坩埚按阶段接受材料；界面显示可观察现象，不直接显示答案。
4. **封存**：选择容器、封蜡、标签和保质条件；错误容器会缓慢污染。
5. **服用**：环境、锚、消化度、额外特性和仪式条件共同结算。

## 63.6 新增加工工位

| 工位 ID | 用途 | 与现有系统连接 |
|---|---|---|
| `pm:mystic_mortar` | 研磨组织、晶体、干燥植物 | 材料品质、粉末粒度、配方替代 |
| `pm:spirit_distiller` | 蒸馏精油、灵性溶剂、纯露 | Create/IE 可提供热与动力兼容 |
| `pm:moonlight_basin` | 月相浸泡、沉淀污染 | 天气/月相、药师/耕种者玩法 |
| `pm:sealed_drying_rack` | 避光、避风或特殊气氛干燥 | 群系、天气与建筑 |
| `pm:characteristic_separator` | 高阶析出额外特性 | 高序列任务、教会服务、风险事件 |
| `pm:imprint_washing_altar` | 弱化精神烙印 | 仪式、锚、封印物与组织声望 |
| `pm:sample_cabinet` | 保存带温湿度要求的样本 | 雾都生活、考古、大学研究 |

## 63.7 品质与可观察反馈

魔药不显示简单星级，而通过瓶内层次、颜色边缘、气泡节奏、灵视光晕、气味文案和容器温度给出线索。完全鉴定后才显示品质标签：`stable / rough / contaminated / mismatched / counterfeit / overconcentrated`。

## 63.8 析出仪式

析出不是普通工作台拆解。完整流程需要：目标进入保护阵、提供身份锚、选择要析出的层、准备对应关系材料、承受 3 波烙印投影。结果可能为：

- 成功：生成特性层物品，玩家保留当前序列但失去额外负担与相应冗余增益；
- 部分成功：特性析出但纯度下降，玩家获得精神创伤；
- 失败可恢复：生成失控投影，仪式中断，材料损失；
- 严重失败：序列能力暂时封锁或产生混合失控体，不直接删除角色。

# 64. 扮演法 v2：准则理解、身份锚、过度扮演与反刷

## 64.1 四段消化模型

| 阶段 | 含义 | 玩家行为 | 系统结果 |
|---|---|---|---|
| 理解 | 理解序列名称背后的原则 | 阅读可靠资料、观察前辈、实验并反思 | 解锁准则候选，不直接加大量消化 |
| 实践 | 在真实情境中按准则行动 | 调查、生活、战斗、社交 | 获得情境化消化 |
| 验证 | 行为产生可观察结果 | 预言验证、裁决履行、治疗成功 | 乘数提高，减少“表演打卡” |
| 区分 | 记住自己只是在扮演 | 写日志、维持关系、拒绝过度角色化 | 降低人格侵蚀，稳定精神烙印 |

## 64.2 准则不是唯一答案

每个序列提供 3–5 条“准则候选”，不同玩家可通过自己的经历形成个人化原则。系统记录行为语义标签，不要求复制固定台词。服务器管理员可以添加自定义扮演事件，但不能直接设置“执行命令即加 100%”。

## 64.3 反刷规则

- 同目标、同地点、同事件模板在 24 小时内收益递减。
- 没有风险、选择或外部结果的重复动作只提供极低“练习值”。
- 多人互刷由目标重复、账号关系、事件结果和时间间隔联合检测，只降低收益，不自动封禁。
- 世界事件、真实委托、陌生目标、承担代价和产生长期影响提高新颖度。
- 玩家可以在手账看到“为什么收益降低”的可解释提示。

## 64.4 过度扮演

新增 `role_overidentification`（角色过度认同）0–100：

| 区间 | 表现 | 处理 |
|---|---|---|
| 0–24 | 健康区分 | 无 |
| 25–49 | 对非准则行为产生轻微压力 | 写个人日志、与锚角色互动 |
| 50–74 | 对话选项与低语偏向序列人格 | 完成“脱离角色”的生活任务 |
| 75–99 | 某些能力自动触发倾向、身份记忆模糊 | 心理治疗、教会仪式、团队陪伴 |
| 100 | 人格侵蚀事件 | 可恢复剧情，不强制永久失控 |

“扮演过度”不惩罚角色扮演玩家；它只针对连续选择极端准则、拒绝所有身份锚并在高压力下继续服药的系统状态。

## 64.5 身份卡与个人日志

玩家第一次服药后创建“我是谁”卡片，可选填：普通职业、重要关系、家园、承诺、纪念物、害怕失去的事。所有字段都可以使用预设，不要求输入真实个人信息。身份卡在高序列时提供人性恢复任务，并与锚系统连接。

## 64.6 扮演事件 JSON v2

```json
{
  "id": "pm:acting/seer/verified_reading",
  "pathway": "pm:seer",
  "sequence": 9,
  "semantic_tags": ["divination", "for_others", "verified_result"],
  "requirements": {
    "target_is_self": false,
    "evidence_event": "pm:divination_result_verified",
    "min_uncertainty": 0.35
  },
  "rewards": {
    "digestion": [6.0, 14.0],
    "principle_insight": 2.0
  },
  "novelty": {
    "target_cooldown_ticks": 72000,
    "location_cooldown_ticks": 24000,
    "repeat_multiplier": 0.35
  },
  "overacting_delta": -1.0
}
```

# 65. 神秘学知识树、仪式、占卜、梦境与灵界航路

## 65.1 知识不是线性科技树

知识图谱分为“对应关系、操作方法、禁忌、历史线索、组织解释、实践验证”六种节点。同一魔药配方可能由不同来源给出，玩家需要交叉印证；错误资料不会全部标红，而会在实践中留下可分析证据。

## 65.2 对应关系系统

| 维度 | 标签例 | 玩法用途 |
|---|---|---|
| 颜色 | `crimson/black/silver/gold` | 蜡烛、布幔、宝石、粒子与仪式倾向 |
| 金属 | `silver/brass/iron/gold` | 对灵体、秩序、机械、太阳等倾向 |
| 植物 | `night/moon/sun/death/dream` | 魔药辅料、熏香、药剂和祭坛装饰 |
| 天象 | `full_moon/new_moon/storm/equinox` | 仪式窗口、事件权重和材料品质 |
| 方位 | `north/east/south/west/zenith` | 祭坛布局、占卜解释与结构朝向 |
| 声音 | `bell/whisper/chant/silence` | 吟唱、反制、组织风格和环境音 |
| 几何 | `circle/triangle/spiral/door/eye` | 阵式验证、模型纹理和 UI 图标 |

## 65.3 仪式编排器

仪式由六个槽组成：**目标、对象、场地、时间、材料、文字/动作**。玩家可以使用模板，也能在研究模式中组合；系统按对应关系、知识可信度和风险计算结果。高风险组合在确认前显示“你无法判断后果”，而不是直接禁止。

### 仪式失败类型

- 无响应：目标不匹配、条件不足；
- 偏转：尊名解析到相似存在；
- 污染回流：供品/祭坛被污染；
- 过量响应：规格太高、保护不足；
- 欺骗响应：未知存在伪装；
- 结构崩解：阵式被破坏、天气骤变或参与者离场。

每种失败都应生成可调查的残留物：烧蚀纹、异常灰、逆流蜡、低语录音、灵界坐标碎片等。

## 65.4 占卜可信度模型

占卜输出由真实线索、提问质量、目标位格、干扰、距离、时间、施术者状态和工具质量共同决定。结果分为“清晰、象征、破碎、矛盾、被遮蔽、危险反窥”。界面绝不显示精确概率，只通过摆幅、牌面完整度、梦境连贯度、声音和文字措辞表达。

## 65.5 梦境玩法

- 睡眠时有概率进入个人梦境；组织任务可进入共享梦境；高污染区会生成侵入梦。
- 梦境地图由现实地点的语义部件重组，不直接复制完整区块，降低存储。
- 玩家通过象征物、重复场景、人物缺席和色彩变化收集线索。
- 梦境死亡通常造成压力/创伤而非现实死亡；某些高风险事件可通过配置升级。
- 观众、不眠者、收尸人、怪物等途径拥有不同梦境交互，不垄断所有梦境内容。

## 65.6 灵界航路

灵界不是普通第二个下界。其位置由“现实锚点 + 象征方向 + 潮汐”决定：

- 城市教堂、墓园、剧院、港口、古宅在灵界有不同投影。
- 航路随灵界天气漂移；玩家需使用灵界罗盘、灯塔、摆渡人契约或星图。
- 迷失不会直接传送到随机死亡点，而会进入“漂流事件”：遇见灵界生物、遗失时间、听见祈祷、发现历史残片。
- 大规模传送有服务器冷却和目的地白名单，防止替代所有交通系统。

## 65.7 符咒、护符与一次性神秘物品

新增统一 `charm` 配方类型：载体 + 对应材料 + 铭文 + 注入方式 + 激活词/动作。符咒有灵性容量、保质期和泄漏风险；已知配方可在 JEI/EMI 显示，但激活条件和禁忌仍由手账管理。


# M. 组织、世界、生态与生产内容（v0.9 新增）

# 66. 教会、隐秘组织、家族与城市神秘暴露系统

## 66.1 组织不再只是声望商店

每个组织由七个数据面组成：教义/目标、公开身份、隐秘部门、资源、辖区、敌友关系、行动策略。服务器每日根据世界状态生成组织行动，而不是等待玩家接任务。

```json
{
  "id": "pm:church_example",
  "public_front": "pm:public/church",
  "covert_units": ["pm:unit/investigation_team"],
  "doctrines": ["protect_civilians", "contain_mystery"],
  "resources": ["sanctuary", "sealed_artifact_vault", "informants"],
  "territories": ["#pm:districts/urban"],
  "strategy_weights": {
    "investigate": 1.0,
    "contain": 0.9,
    "recruit": 0.5,
    "cover_up": 0.8,
    "open_conflict": 0.1
  }
}
```

## 66.2 正神教会行动模板

专题资料显示不同教会拥有各自的非凡行动单位。v0.9 将这一结构抽象为可配置单位，而非把七个教会做成换皮：

| 行动类型 | 触发 | 玩家可参与 | 失败后果 | 可被其他系统读取 |
|---|---|---|---|---|
| 夜间巡查 | 城市暴露度上升、灵体报告 | 跟随、提供线索、误导、救援 | 失踪、封锁区扩大 | 报纸、通缉、任务板 |
| 封印物转运 | 教会仓库容量或事件需求 | 护送、鉴定、劫持、替换 | 封印泄漏、组织战争 | 世界事件、拍卖、Boss |
| 异端审查 | 错误尊名、邪教据点、公开神迹 | 作证、辩护、潜入、撤离 | 通缉、组织声望变化 | 律师/仲裁人途径 |
| 灾后救助 | 风暴、瘟疫、灵界裂隙 | 治疗、建造、物资运输 | 城市生活指数下降 | 经济、锚、扮演 |
| 高位会议 | 序列 5+、唯一性线索 | 代表阵营、交换情报、投票 | 阵营决策改变 | 赛季历史、全服规则 |
| 秘密招募 | 玩家表现与资料可信 | 接受、拒绝、卧底、推荐他人 | 机会窗口关闭或遭监视 | 组织身份、任务链 |

## 66.3 隐秘组织的细胞网络

隐秘组织采用“总部未知—区域负责人—行动小组—外围联系人”四层。玩家打掉一个据点只会破坏局部网络，并可能让其他细胞改变密码、转移仪式或制造假线索。组织网络保存在 SavedData 中，只保存抽象节点，不让每个 NPC 永久加载。

### 细胞节点状态

`dormant`（潜伏）→ `recruiting`（招募）→ `preparing`（筹备）→ `active`（行动）→ `exposed`（暴露）→ `dispersed`（瓦解）/`escaped`（转移）。

每个状态有不同场景：酒馆暗号、大学社团、慈善机构、货运公司、地下祭坛、梦境聚会、报社匿名投稿。组织不会永远穿统一制服站在地牢里。

## 66.4 天使家族与历史家系

第四纪元家系以“历史遗址、血脉传承、封印物所有权、相邻途径知识和政治债务”进入游戏，而不直接复刻小说角色剧情。每个家系生成：

- 1 个公开或伪装姓氏；
- 1–3 个祖宅/墓园/档案结构；
- 一组家徽纹样与旧式装备；
- 至少一条真实传承和一条伪造传承；
- 与某途径、封印物或纪元事件的关系；
- 当前分支：复兴、衰败、隐居、被替代、血脉污染。

玩家可成为受雇调查者、继承争议见证人、家族客卿或敌对组织代理，但不会自动成为“原著家族成员”。

## 66.5 神秘暴露度与掩盖

每个聚落维护 `mystery_exposure` 0–100：

| 暴露 | 社会表现 | 组织行为 | 玩家影响 |
|---:|---|---|---|
| 0–19 | 神秘被当作传闻 | 低调巡逻 | 普通生活稳定、委托少 |
| 20–39 | 失踪/怪谈增加 | 调查、报纸掩盖 | 神秘委托增多，物价小幅波动 |
| 40–59 | 居民主动避夜 | 宵禁、封锁、招募 | 夜间商店关闭、教会任务增加 |
| 60–79 | 恐慌与组织冲突 | 清剿、撤离、封印物调用 | 城市功能受损，敌对事件频发 |
| 80–100 | 公开灾难 | 高位介入或弃城 | 进入区域主线，结果永久写入历史 |

降低暴露度不等于“删除证人”：可通过救援、解释为普通事故、修复建筑、治疗创伤、报社发布合理报道、教会封锁和时间衰减。暴力处理目击者会带来罪迹、组织追查和锚损失。

## 66.6 NPC 记忆与关系

NPC 只记录与玩法有关的摘要：玩家是否守约、是否救援、是否撒谎被发现、所属组织、公开通缉、曾共同经历的事件。不会保存玩家真实聊天全文。关系变化以事件 ID 记录，可在“关系账本”查看原因，便于调试和角色扮演。

# 67. 世界生成、地点、结构、天气与纪元考古

## 67.1 世界空间层级

| 层级 | 示例 | 生成方式 | 主要玩法 |
|---|---|---|---|
| 大区 | 王国、海域、荒原、极地 | 数据包区域标签，不强制替换世界地图 | 组织势力、物价、长途事件 |
| 城市/聚落 | 雾都城区、港口、工业镇、大学城 | Jigsaw + 地区主题池 | 生活、委托、教会与秘密组织 |
| 街区 | 上流住宅、东区、码头、工厂、墓园 | 道路和地块模板 | 神秘暴露、巡逻、报纸事件 |
| 地标 | 教堂、报社、剧院、车站、警局 | 可重复但带变体 | 服务、任务和锚 |
| 隐秘地点 | 地下祭坛、梦境入口、封印库 | 条件生成/事件临时实例 | 调查、仪式、Boss |
| 异界投影 | 灵界航站、灰雾席位、历史投影 | 独立实例或轻量维度 | 高序列、祈祷、纪元线索 |

## 67.2 新增结构目录（v0.9 追加 32 类）

| 类别 | 结构 | 主要内容 | 互链 |
|---|---|---|---|
| 城市公共 | 区警局、法庭、慈善医院、公共浴室、图书馆、邮局 | 普通职业、调查、社会事件 | 仲裁人/律师、报纸、通缉 |
| 工业交通 | 煤气厂、机械车间、货运站、河闸、灯塔、废弃隧道 | 机械零件、事故、走私 | 通识者、Create/IE、海上事件 |
| 文化生活 | 小剧院、音乐厅、画廊、私人俱乐部、廉价旅馆、咖啡馆 | 表演、社交、谣言、身份锚 | 小丑、观众、报社、经济 |
| 宗教神秘 | 小教堂、乡村圣所、异端礼拜室、封印物中转库、祈祷地下室 | 组织服务、仪式与封印 | 七教会、尊名、封印物 |
| 学术考古 | 大学实验室、旧天文台、博物馆库房、考古营地、纪元墓穴 | 鉴定、研究、历史线索 | 阅读者、通识者、窥秘人 |
| 荒野生态 | 灵性湿地、夜蚀花谷、巨兽骨场、月光洞穴、风暴悬崖 | 神秘材料、生物生态 | 耕种者、药师、水手 |
| 灵界 | 漂流市集、无声渡口、记忆森林、颜色荒漠、梦门回廊、祈祷回声塔 | 航路、交易、迷失事件 | 灵界、祈祷、梦境、摆渡人 |

结构数量是模板类别，不代表全部资产已经完成。每类至少准备 3 个布局种子和 2 个状态变体（正常/废弃/污染/事件中任选），防止世界重复。

## 67.3 城市建筑状态

同一结构根据历史与暴露度拥有状态层：`prosperous / ordinary / poor / abandoned / burned / quarantined / occult_occupied / restored`。状态通过处理器替换方块、战利品、NPC、涂鸦、照明和环境音，无需复制整套 NBT。

## 67.4 纪元考古

遗址不直接讲完整答案，而由五类证据组成：建筑工艺、材料年代、铭文碎片、物品使用痕迹、后世改建。玩家可在大学/博物馆建立时间线，允许多个假说并存；新证据会提高或降低假说可信度。考古学家、阅读者、占卜家、通识者各有不同证据优势。

## 67.5 灵界映射与坐标安全

现实结构通过 `spirit_projection_tag` 映射到灵界语义结构，而非一比一坐标复制。服务器只同步玩家附近航路节点；传送目标由服务器校验，禁止客户端提交任意坐标。退出灵界时优先回到进入锚点，锚点失效才进入安全漂流事件。

## 67.6 天气与神秘天象总览

| ID | 名称 | 触发/区域 | 视觉与音效 | 玩法影响 | 反制/利用 |
|---|---|---|---|---|---|
| `pm:thick_industrial_fog` | 工业浓雾 | 城市、低风、污染 | 低能见度、煤气灯光晕 | 追踪困难、灵体更活跃 | 风系能力、路灯网络、口罩 |
| `pm:spirit_mist` | 灵性雾 | 灵界泄漏 | 彩边雾滴、远处低语 | 灵视增强但污染上升 | 关闭灵视、盐灯、教会结界 |
| `pm:blood_moon_tide` | 绯月潮 | 月相/事件 | 月光变色、影子延长 | 药师/囚犯/梦境事件增强 | 室内庇护、月光盆收集 |
| `pm:starless_night` | 无星之夜 | 高位遮蔽 | 星图消失、环境声压低 | 占星与远距仪式失灵 | 备用锚、机械导航 |
| `pm:whispering_rain` | 低语雨 | 污染区 | 雨滴含字幕碎片 | 呓语、错误线索、作物变异 | 过滤雨水、隔音符咒 |
| `pm:ash_fall` | 灰烬飘落 | 大型仪式后 | 温灰粒子、钟声迟滞 | 残留物采集、呼吸负面 | 面具、净化街区 |
| `pm:spiritual_storm` | 灵性风暴 | 海上/灵界 | 多层云、闪电符号 | 航路漂移、能力过载 | 灯塔、风眷者、避风港 |
| `pm:memory_snow` | 记忆雪 | 寒地灵界投影 | 雪片显现短场景 | 收集他人历史碎片，可能误导 | 鉴定、火焰融化、封存样本 |
| `pm:silent_thunder` | 无声雷暴 | 高位事件 | 闪电无声，延迟冲击 | 红石/机械异常、灵性震荡 | 接地阵、停机维护 |
| `pm:golden_dawn` | 金色黎明 | 太阳相关事件 | 暖光、合唱远音 | 亡灵削弱、净化效率提高 | 收集晨露、公开祈祷 |
| `pm:nightmare_front` | 梦魇锋面 | 城市压力高 | 云层似眼睑 | 睡眠进入共享噩梦 | 不眠者巡逻、梦境治疗 |
| `pm:fate_hail` | 命运冰雹 | 熵债集中 | 冰粒落点异常 | 随机设备故障/意外好运 | 灾祸标记、保险与修复 |
| `pm:doorlight_aurora` | 门光极光 | 空间异常 | 天空出现门形光带 | 旅行失误、稀有航路开放 | 星图校准、学徒能力 |
| `pm:time_drift_dust` | 时漂尘 | 古代遗址 | 尘粒逆落 | 作物/机器局部加速或减速 | 时间锚、区域封锁 |
| `pm:sea_rage` | 海怒 | 海洋歌者/灾难事件 | 巨浪、风啸、蓝白电弧 | 船只、港口和海怪活动 | 灯塔、避风、海员任务 |
| `pm:grave_calm` | 墓园静滞 | 大规模死亡后 | 风停、虫鸣消失 | 亡灵交谈窗口、恢复受阻 | 安魂仪式、摆渡人任务 |
| `pm:knowledge_flare` | 知识耀斑 | 研究失控 | 空中符号、书页翻动声 | 随机知识显现与灼伤 | 封签、关闭书库、抄录 |
| `pm:anchor_eclipse` | 锚蚀 | 高序列锚危机 | 熟悉建筑短暂褪色 | 锚恢复降低、历史被扭曲 | 纪念活动、关系任务、修复地标 |

天气事件必须遵守：提前可读征兆、可配置视觉强度、服务器上限、明确结束条件、至少一种规避与一种利用方式。


# 68. 生物、植物与神秘生态 v2

v0.9 将生物从“战斗掉落点”提升为生态节点：它们会迁徙、争夺资源、响应天气、影响结构状态，并被组织和委托系统观察。下表中的正典标记只说明世界观类别基础；绝大多数具体个体、名称、AI 和掉落属于本项目原创或玩法改编。

## 68.1 新增/重构生物目录

| 注册 ID | 显示名 | 生态类别 | 生成环境 | 核心 AI/交互 | 主要掉落 | 系统互链 | 口径 |
|---|---|---|---|---|---|---|---|
| pm:lantern_moth_swarm | 灯蛾群 | 灵界小型群体 | 灵界渡口、旧灯塔 | 围绕安全光源盘旋；光源熄灭时逃散 | 灯蛾粉、微光翅 | 符咒、灵界导航、灯油 | original |
| pm:memory_leech | 记忆水蛭 | 灵体寄生 | 记忆森林、噩梦水域 | 吸附后偷走最近一条任务提示；可用镜子发现 | 记忆黏液、失真片段 | 记忆封存、梦境治疗 | original |
| pm:prayer_echo | 祈祷回声 | 非实体回声 | 祈祷塔、废弃圣所 | 重复无人回应的祈祷；接近时生成线索或陷阱 | 回声结晶 | 尊名研究、锚事件 | adaptation |
| pm:spirit_ferryman | 灵界摆渡人 | 中立智慧灵体 | 无声渡口 | 接受正确代价后提供航路；欺骗会把玩家送入漂流 | 渡票、旧桨碎片 | 灵界交通、收尸人任务 | adaptation |
| pm:color_eater | 噬色兽 | 灵界捕食者 | 颜色荒漠 | 吸走方块与粒子的颜色并伪装自身 | 无色腺体、褪色皮 | 幻术颜料、净化滤镜 | original |
| pm:door_wisp | 门隙精 | 空间灵体 | 门光极光、废弃传送结构 | 在门框间跳跃，偷走钥匙并留下错误入口 | 折叠尘、门纹膜 | 学徒材料、空间陷阱 | original |
| pm:whisper_crow | 低语鸦 | 城市神秘鸟类 | 墓园、屋顶、报社附近 | 模仿听过的短句；可能携带真实或伪造传闻 | 黑羽、录音喉骨 | 报纸线索、占卜干扰 | original |
| pm:compass_bird | 罗盘鸟 | 中立导航生物 | 风暴悬崖、灵界边缘 | 头冠指向稳定锚；灵性风暴中集群迁徙 | 罗盘羽、磁性喙片 | 灵界罗盘、教学任务 | original |
| pm:clockwork_beetle | 发条甲虫 | 机械构造体 | 机械车间、遗址 | 修补附近低级机器；被污染后反向拆解 | 微型齿轮、弹簧壳 | 通识者、Create/IE 适配 | original |
| pm:archive_spider | 档案蛛 | 知识妖化生物 | 图书馆、档案室 | 用文字丝网封存书页；受惊会打乱书架索引 | 文字丝、墨囊 | 卷轴、知识封签 | original |
| pm:ink_hound | 墨犬 | 追踪构造体 | 警局、秘密组织据点 | 追踪文书、契约和签名气味 | 活墨、纸质牙 | 律师/仲裁人调查 | original |
| pm:gutter_gremlin | 沟渠怪 | 城市异变 | 下水道、贫民区 | 偷食废料并藏起小物件；群体会堵塞管网 | 酸性胆汁、锈币 | 城市维护、炼金溶剂 | original |
| pm:gaslight_specter | 煤气灯幽影 | 城市灵体 | 浓雾街区 | 只在灯影间移动，靠近时降低照明而不破坏灯 | 灯芯灰、影油 | 幻术、夜巡事件 | original |
| pm:theatre_maskling | 剧院面灵 | 情绪灵体 | 剧院、舞会、梦境 | 复制观众情绪并换面具；可触发群体喜剧或恐惧 | 情绪漆、面具碎片 | 小丑/观众扮演、舞台活动 | original |
| pm:ledger_mimic | 账簿拟态 | 物品拟态怪 | 当铺、仓库、地下交易所 | 伪装成账簿，记录附近交易并咬住盗贼 | 会计皮、假印章 | 经济调查、诈骗任务 | original |
| pm:grave_lantern | 墓灯灵 | 中立亡灵 | 墓园、战场遗址 | 为迷失亡魂引路；被攻击后召集怨魂 | 墓灯芯、安魂灰 | 收尸人、安魂仪式 | adaptation |
| pm:bone_messenger | 骨信使 | 亡灵构造体 | 墓穴、秘密通道 | 携带无法普通打开的骨筒信件 | 骨筒、死者印泥 | 组织线索、会客法 | original |
| pm:pale_horse | 苍白驮马 | 亡灵坐骑 | 墓园静滞、荒原 | 只接受完成安魂任务的骑手 | 苍白鬃毛、冷铁马蹄 | 摆渡/护送任务 | adaptation |
| pm:corpse_bloom_host | 尸花宿主 | 植物寄生亡灵 | 湿地、乱葬地 | 死亡后花朵继续释放孢子，需分阶段处理 | 尸花瓣、腐灵孢子 | 耕种者/药师、瘟疫事件 | original |
| pm:mirror_wraith | 镜怨 | 怨魂 | 旧宅、剧院后台 | 在镜面间移动并复制玩家轮廓 | 怨镜银膜、冷凝泪 | 囚犯途径、镜面迷宫 | adaptation |
| pm:storm_fin | 风暴鳍兽 | 海洋灵性生物 | 深海、灵性风暴 | 沿雷电航线跃出海面，攻击金属船体 | 导电鳍、蓝血 | 水手材料、避雷设备 | original |
| pm:mist_whale | 雾鲸 | 大型中立海兽 | 远洋浓雾 | 背部形成临时雾岛；鸣声可扰乱航向 | 雾囊膜、回声鲸骨 | 海上航路、仪式号角 | original |
| pm:reef_oracle | 礁卜兽 | 海洋智慧异兽 | 珊瑚遗迹 | 用贝壳排列给出象征性预兆 | 卜壳、潮汐眼 | 占卜、港口传闻 | original |
| pm:brine_corpse | 盐尸 | 海上亡灵 | 沉船、盐沼 | 吸收水分并在甲板留下盐线 | 灵盐、潮湿裹布 | 安魂、海上瘟疫 | original |
| pm:thunder_ray | 雷翼鳐 | 空海生物 | 风暴海域 | 借雷云滑翔，释放链式电弧 | 雷膜、蓄电腺 | 风眷者、机械蓄能 | original |
| pm:moon_hare | 月纹兔 | 神秘草食兽 | 月光洞穴、夜间草原 | 满月采食发光植物，受惊时留下假影 | 月纹皮、银露 | 药师、月相材料 | original |
| pm:harvest_stag | 丰收鹿 | 中立大型兽 | 神秘农田、森林边缘 | 提升附近作物品质；污染时转为荒芜角兽 | 丰收角屑、生命血滴 | 耕种者、季节活动 | original |
| pm:plague_gnat_cloud | 疫蚋云 | 群体害虫 | 湿地、污染农场 | 传播可追踪病原；火、烟和净化草药可驱散 | 疫翅、病原样本 | 医师研究、瘟疫事件 | original |
| pm:graftling | 嫁接灵 | 植物构造体 | 神秘温室 | 照料相邻作物并随机交换一项性状 | 嫁接芽、活性树液 | 嫁接树、生态农场 | original |
| pm:nightmare_hound | 梦魇猎犬 | 梦境捕食者 | 共享噩梦、城市高压力区 | 追踪恐惧较高目标，现实中只留下爪印 | 梦毛、恐惧唾液 | 观众/不眠者、梦境治疗 | adaptation |
| pm:logic_imp | 逻辑小鬼 | 知识异常体 | 研究事故、图书馆 | 提出悖论并锁住容器；回答需证据而非猜谜 | 悖论角、逻辑核 | 阅读者、知识事件 | original |
| pm:omen_snake | 兆蛇 | 命运生物 | 灾祸前沿、遗址 | 鳞片图案预示事件类别；捕杀会改变预兆 | 命鳞、空白蛇蜕 | 怪物途径、天气预测 | original |
| pm:misfortune_cat | 厄运猫 | 城市中立生物 | 旅馆、码头、市场 | 把小厄运吸到自己周围；喂养可暂时保护店铺 | 逆毛、黑爪套 | 命运活动、城市生活 | original |
| pm:law_golem | 律令石像 | 秩序构造体 | 法庭遗址、封印库 | 按刻写规则行动；规则冲突时停机或暴走 | 律文石片、秩序核心 | 仲裁人/律师、高阶封印 | original |
| pm:bribe_sprite | 贿赂精 | 欲望小灵 | 地下交易所 | 接受物品后改动小型机关；越喂越贪 | 欲望尘、空钱袋 | 律师任务、交易陷阱 | original |
| pm:desire_larva | 欲望幼体 | 深渊污染生物 | 堕落仪式残留 | 依附强烈情绪成长，净化后可转为无害情绪灵 | 欲望囊、污染黏液 | 罪犯途径、心理治疗 | adaptation |
| pm:chain_beast | 缚链兽 | 诅咒生物 | 牢狱遗址、满月事件 | 越挣扎越强；冷静站定会让锁链松弛 | 活锁链、月痕骨 | 囚犯途径、克制教学 | original |
| pm:mythic_afterimage | 神话残像 | 高序列事件投影 | 神话形态使用地点 | 不掉落完整特性，只重放知识灼伤片段 | 知识灰、形态鳞影 | 高序列调查、模型复用 | adaptation |
| pm:characteristic_condensate | 特性凝集体 | 精英异变体 | 灵性沉积区 | 吸收附近生物特征，死亡凝聚对应特性束 | 完整/破碎特性 | 特性守恒、区块生态 | adaptation |
| pm:mixed_breakdown_entity | 混合失控体 | Boss 模板 | 转途径失败/额外特性过载 | 组合两途径能力与可隐藏骨骼部件 | 混合特性、转途径残留 | 高序列风险、团队副本 | adaptation |

## 68.2 神秘植物与作物

| 注册 ID | 名称 | 生长条件 | 产物 | 用途 | 口径 |
|---|---|---|---|---|---|
| pm:night_eclipse_flower | 夜蚀花 | 暗处/新月 | 种子、花粉 | 占卜家材料、暗视熏香 | adaptation |
| pm:spirit_moss | 灵性苔 | 墓园石、灵界锚 | 湿苔、孢子 | 灵体诱饵、样本保湿 | original |
| pm:whisper_reed | 低语芦 | 灵性湿地 | 芦芯、露水 | 录音笔芯、低语过滤 | original |
| pm:moon_dew_lily | 月露百合 | 满月水面 | 花瓣、月露 | 月光盆、药师材料 | original |
| pm:sunthread_grass | 日丝草 | 金色黎明 | 纤维、晨露 | 圣化绷带、光符咒 | original |
| pm:grave_bell | 墓铃花 | 墓园静滞 | 铃形花、根 | 安魂、亡灵引导 | original |
| pm:dream_poppy | 梦罂粟 | 梦境泄漏地 | 梦粉、种荚 | 睡眠药、梦境钥匙 | original |
| pm:mirror_ivy | 镜藤 | 旧宅镜墙 | 银叶、反光汁 | 镜面跳跃、幻术涂层 | original |
| pm:storm_kelp | 风暴藻 | 雷暴海岸 | 导电叶、盐胶 | 蓄电、海洋魔药 | original |
| pm:mist_fern | 雾蕨 | 浓雾林地 | 雾叶、根茎 | 雾瓶、隐匿布料 | original |
| pm:ash_rose | 灰烬玫瑰 | 仪式火场 | 灰瓣、炭刺 | 净化灰、纪念物锚 | original |
| pm:memory_sage | 记忆鼠尾草 | 记忆雪融区 | 叶、香脂 | 记忆固定、心理治疗 | original |
| pm:inkcap_mushroom | 墨帽菇 | 档案室地下 | 墨液、菌褶 | 活墨、文书伪装 | original |
| pm:brassroot | 黄铜根 | 机械遗址 | 金属根、树液 | 机械润滑、星盘部件 | original |
| pm:chainvine | 锁链藤 | 牢狱遗址 | 链节藤、刺 | 束缚器、陷阱 | original |
| pm:desire_orchid | 欲念兰 | 情绪污染地 | 香囊、花蕊 | 情绪鉴定、欲望容器 | original |
| pm:law_lotus | 律纹莲 | 法庭水池 | 刻纹瓣、莲心 | 秩序仪式、证词封存 | original |
| pm:fortune_clover | 命运苜蓿 | 灾祸后恢复区 | 叶、根瘤 | 幸运符、熵债研究 | original |
| pm:time_thistle | 时漂蓟 | 时间尘天气 | 刺、绒毛 | 保存期限调整、考古定年 | original |
| pm:door_blossom | 门扉花 | 空间薄弱点 | 花膜、折叠花粉 | 短距门符、空间标记 | original |
| pm:corpse_bloom | 尸花 | 乱葬湿地 | 尸瓣、孢子 | 毒物、死灵材料、瘟疫研究 | adaptation |
| pm:spirit_orchid | 灵界兰 | 灵界市集边缘 | 灵兰瓣、透明根 | 灵视药、灵界稳定 | original |
| pm:nightwatch_tea | 守夜茶 | 寒冷高地 | 茶叶 | 不眠者饮品、疲劳管理 | original |
| pm:seafarer_anise | 航海茴香 | 海崖 | 种子、油 | 防晕船、风向仪式 | original |
| pm:giant_blood_moss | 巨血苔 | 巨兽骨场 | 红苔块 | 战士药膏、力量材料 | adaptation |
| pm:silverveil_cotton | 银幕棉 | 月光农田 | 银纤维 | 灵体防护衣、Curios 槽饰品 | original |
| pm:paper_reed | 纸芦 | 河岸农场 | 纸浆、韧纤维 | 符纸、报纸、卷轴 | original |
| pm:sealed_wax_tree | 封蜡树 | 温室/暖地 | 树脂、蜡果 | 封印、药瓶封口 | original |
| pm:echo_bamboo | 回声竹 | 风谷 | 空心节、叶 | 仪式乐器、录音筒 | original |
| pm:purity_saltwort | 净盐草 | 盐沼 | 盐叶、根灰 | 净化盐、海上保存 | original |
| pm:nightmare_cocoa | 梦魇可可 | 梦境温室 | 豆、壳 | 梦境诱导食品、剧院饮品 | original |
| pm:anchor_oak | 锚忆橡 | 玩家纪念地 | 年轮片、叶 | 地点锚、纪念牌 | original |
| pm:star_chart_lichen | 星图地衣 | 天文台石墙 | 发光片 | 星图墨、夜间指示 | original |
| pm:truth_fern | 证言蕨 | 法庭/警局庭院 | 卷叶 | 证词药剂、谎言副作用缓解 | original |
| pm:calamity_berry | 灾果 | 灾祸标记区 | 果实、核 | 灾祸诱饵、熵债容器 | original |
| pm:veil_pepper | 帷幕椒 | 浓雾温室 | 辛果 | 抗低语食物、隐匿烟雾 | original |

## 68.3 生态规则

1. **天气驱动**：至少 60% 的神秘生物拥有天气或月相权重；天气结束后逐步迁出，不瞬间消失。
2. **食物网**：灵性植物→小型灵体/草食异兽→捕食者/失控体；过度捕杀会改变材料供应和事件权重。
3. **非战斗交互**：驯养、观察、记录、救治、引导、交易、驱逐、封印至少覆盖每类生态。
4. **特性守恒**：只有明确拥有非凡特性的实体进入特性掉落；普通神秘动物不必都掉特性。
5. **生成上限**：群体生物使用单实体 swarm；大型生物按区域预算；灵界生物离开锚区后进入休眠。
6. **世界影响**：生物活动可留下巢、足迹、食痕、脱落物和居民传闻，先有线索再有遭遇。

## 68.4 失控体组合器

失控体由“途径骨架模板 + 序列部件 + 死亡情绪 + 环境污染 + 额外特性”组合。数据定义只选择白名单能力，禁止把任意玩家技能全量复制给 AI。组合器输出模型部件、能力组、掉落、低语池和弱点；同一途径的失控体因此能有可读差异，而不是永远同一种怪。

# 69. 全物品增量注册表 v0.9

本章在 v0.8 的 180+ 物品基础上新增 **104** 个明确注册条目，并把第 68 章的 36 种植物产物与 40 种生物掉落纳入标签系统。新增数仅代表设计条目；实现状态见第 81 章。

## 69.1 新增物品表

| 类别 | 注册 ID | 显示名 | 物品样式/材质 | 核心用途 | 主要来源 | 互链 | 稀有度 |
|---|---|---|---|---|---|---|---|
| 工具 | pm:spirit_lens_mk2 | 可调灵视镜 | 黄铜镜框、三片可旋滤镜 | 切换污染/灵体/情绪观察层，长期观察会积累知识灼伤 | 通识者工位+银幕棉 | 灵视、神话形态观察 | uncommon |
| 工具 | pm:portable_divination_board | 折叠占卜板 | 胡桃木板、嵌银方位环 | 野外进行灵摆/灵数/纸牌占卜，稳定度低于固定祭坛 | 侦探事务所/制作 | 占卜、委托 | uncommon |
| 工具 | pm:evidence_camera | 灵性照相机 | 皮腔相机、镁光灯、刻纹镜头 | 记录异常轮廓；照片可作证据但可能被高位干扰 | 通识者/报社 | 证据、报纸、任务 | rare |
| 工具 | pm:echo_phonograph | 回声留声筒 | 黄铜喇叭、蜡筒 | 录下仪式余音和灵体短句；回放可能复触发低阶效果 | 剧院/机械工位 | 会客法、低语分析 | rare |
| 工具 | pm:ritual_compass | 仪式罗盘 | 银针、五色宝石位 | 检查祭坛方位和缺失节点，不显示正确答案 | 大学/制作 | 仪式编排器 | uncommon |
| 工具 | pm:sealed_tweezers | 封印镊 | 黑铁镊、封蜡握柄 | 安全移动微小污染材料，耐久耗尽会泄漏 | 教会供应 | 样本处理 | common |
| 工具 | pm:occult_caliper | 神秘卡尺 | 黄铜精密尺 | 测量骨片、符号和机械部件，提升鉴定数据质量 | 通识者制作 | 考古、鉴定 | common |
| 工具 | pm:spirit_chalk_case | 灵性粉笔盒 | 木盒、六色粉笔 | 快速绘制低阶保护线；雨水会破坏 | 教会/工作台 | 仪式、防护 | common |
| 工具 | pm:portable_sample_press | 便携样本压片器 | 钢夹、玻璃片 | 把植物/组织制成显微样本和档案条目 | 大学实验室 | 药师、耕种者 | uncommon |
| 工具 | pm:silver_thread_spool | 银丝线轴 | 细银线、黑木轴 | 布置警戒线、灵体感应和小型封印 | 制作/教会 | Curios、陷阱 | uncommon |
| 工具 | pm:dream_anchor_clock | 梦锚闹钟 | 机械钟、记忆鼠尾草囊 | 设定梦境退出条件；损坏会延迟醒来 | 心理炼金会/机械专家 | 梦境、安全 | rare |
| 工具 | pm:weather_observer | 便携天象仪 | 折叠三脚架、星图盘 | 记录月相、风、灵性天气并生成观测数据 | 天文台 | 天气、星象师 | uncommon |
| 工具 | pm:imprint_probe | 烙印探针 | 水晶针、绝缘柄 | 估计特性精神烙印强弱；使用者承受少量低语 | 高阶实验室 | 特性、净化 | epic |
| 工具 | pm:anchor_resonator | 锚共鸣仪 | 木盒、照片槽、音叉 | 比较地点/物件锚稳定度，不显示私人文本 | 序列5任务 | 锚、人性 | epic |
| 工具 | pm:spirit_route_marker | 灵界路标器 | 罗盘羽、门纹膜 | 在灵界放置短期航路标记，退出后衰减 | 灵界市集 | 航路、团队 | rare |
| 工具 | pm:mystic_lockpick | 神秘锁具组 | 银针、骨片、微齿轮 | 处理仪式锁/机械锁；失败会留下证据 | 偷盗者/通识者 | 潜入、通缉 | rare |
| 穿戴 | pm:filtered_monocle | 过滤单片镜 | 黄铜、烟色玻璃 | 降低神话知识灼伤，牺牲灵视清晰度 | 教会/通识者 | Curios:face | rare |
| 穿戴 | pm:nightwatch_coat | 守夜长外套 | 深色羊毛、银线内衬 | 夜间保温并降低低语雨影响 | 不眠者任务 | Curios:back/armor | uncommon |
| 穿戴 | pm:ritualist_gloves | 仪式师手套 | 白布、五指不同符号 | 减少放错材料概率，污染后需清洗 | 仪式工位 | Curios:hands | uncommon |
| 穿戴 | pm:spirit_silk_veil | 灵丝面纱 | 银幕棉、灵界兰 | 遮蔽灵体视线并降低附身概率 | 药师/织造 | Curios:face | rare |
| 穿戴 | pm:oath_brooch | 誓约胸针 | 银胸针、可写契据 | 记录一项自愿承诺，履约恢复锚，违约获失信 | 律师/仲裁人 | Curios:charm | rare |
| 穿戴 | pm:ferryman_token | 摆渡人信物 | 旧木牌、渡票绳 | 减少一次灵界漂流惩罚 | 摆渡任务 | Curios:charm | rare |
| 穿戴 | pm:moonrestraint_collar | 月制束环 | 银扣、锁链藤 | 压低狼人/怨魂变形压力，降低速度 | 囚犯路线 | Curios:necklace | rare |
| 穿戴 | pm:sunthread_bandage | 日丝绷带 | 日丝草纤维 | 可装备式缓慢净化伤口，遇夜效率低 | 歌颂者/医师 | Curios:hands | uncommon |
| 穿戴 | pm:brassroot_goggles | 黄铜根护目镜 | 黄铜根、皮革 | 机械过载时显示故障类型 | 机械专家 | Curios:head | uncommon |
| 穿戴 | pm:fortune_knot | 命运结 | 命运苜蓿、红线 | 储存少量幸运势，满载后必须结算熵债 | 怪物路线 | Curios:charm | rare |
| 穿戴 | pm:mourners_locket | 悼念吊坠 | 银盒、纪念纸片 | 作为物件锚，安魂任务效率提高 | 墓园/制作 | Curios:necklace | uncommon |
| 穿戴 | pm:gas_mask_occult | 神秘滤毒面具 | 皮革、净盐草、活性炭 | 抵御工业雾、孢子和低语雨一部分效果 | 工业城/制作 | Curios:face | uncommon |
| 穿戴 | pm:starcloak_lining | 星纹斗篷内衬 | 星图地衣、深蓝布 | 露天仪式稳定度提升，室内无效 | 星象师 | Curios:back | rare |
| 穿戴 | pm:law_chain_belt | 律令链带 | 律文石、银链 | 提高管辖区域内命令稳定，僭越时反噬更强 | 仲裁人路线 | Curios:belt | rare |
| 穿戴 | pm:desire_mask | 欲望面具 | 情绪漆、镜藤汁 | 隐藏自身情绪类别，但逐渐积累欲望压力 | 罪犯/观众路线 | Curios:face | epic |
| 穿戴 | pm:archaeologist_satchel | 考古挎包 | 帆布、样本格 | 存放遗物样本并保持出处信息 | 大学任务 | Curios:back | uncommon |
| 仪式 | pm:five_color_candle_set | 五色仪式蜡烛组 | 五色蜡、金属烛芯 | 提供可替换的颜色/金属对应槽 | 蜡树+矿物 | 仪式编排 | common |
| 仪式 | pm:silent_bell | 无声铃 | 黑银合金、回声竹 | 摇动时吸收附近声音用于安魂/隐匿 | 教会封印库 | 安魂、潜入 | rare |
| 仪式 | pm:prayer_receiver_plate | 祈祷承接盘 | 银盘、尊名槽 | 服务器端登记安全祈祷队列 | 序列3研究 | 祈祷、尊名 | epic |
| 仪式 | pm:identity_salt_circle | 身份盐环 | 净盐、记忆香草 | 在析出/服药时稳定身份锚 | 教会/心理炼金 | 特性、扮演 | rare |
| 仪式 | pm:spirit_route_lantern | 灵界航灯 | 墓灯芯、罗盘羽 | 为灵界小队提供共享锚点 | 摆渡任务 | 灵界航路 | rare |
| 仪式 | pm:counterfeit_honorific_shard | 伪尊名碎片 | 污染陶片 | 教学错误呼名与欺骗响应，不可直接安全使用 | 异端据点 | 尊名风险 | rare |
| 仪式 | pm:memory_seal_wax | 记忆封蜡 | 蜡树脂、记忆鼠尾草 | 封存一个知识节点或梦境片段 | 窥秘人/观众 | 知识、梦境 | uncommon |
| 仪式 | pm:imprint_washing_incense | 烙印洗涤香 | 灰烬玫瑰、灵界兰 | 析出/净化时降低烙印但损失纯度 | 高阶配方 | 特性 | epic |
| 仪式 | pm:anchor_oak_tablet | 锚忆木牌 | 锚忆橡木、刻字位 | 登记地点锚和纪念事件 | 生活任务 | 锚、历史 | uncommon |
| 仪式 | pm:storm_grounding_spike | 灵暴接地钉 | 黑铁、风暴藻 | 在灵性风暴中保护小区域和机器 | 水手/机械 | 天气、兼容 | uncommon |
| 仪式 | pm:dream_entry_ribbon | 入梦缎带 | 梦罂粟纤维、银线 | 把自愿参与者连接到共享梦境 | 心理炼金 | 梦境、团队 | rare |
| 仪式 | pm:witness_stone | 见证石 | 律文石、活墨 | 记录仪式参与者和同意状态 | 仲裁人/律师 | 多人安全 | rare |
| 仪式 | pm:mythic_viewing_screen | 神话观测幕 | 银幕棉、烟玻璃 | 将神话形态转为低保真投影 | 序列4研究 | 知识灼伤、UI | epic |
| 仪式 | pm:calamity_substitute_doll | 替灾人偶 | 纸芦、灾果核 | 承接一项小型厄运，装满后必须安全销毁 | 怪物路线 | 熵债、事件 | rare |
| 仪式 | pm:spirit_contract_parchment | 灵体契约羊皮纸 | 纸芦、灵墨 | 记录与灵体/使魔的可终止协作条款 | 窥秘人/收尸人 | 契约、AI | rare |
| 仪式 | pm:epoch_resonance_frame | 纪元共鸣框 | 古木、年代样本 | 组合考古证据并投影模糊历史层 | 考古主线 | 历史、任务 | epic |
| 文书 | pm:case_board | 调查板 | 木板、线、图钉 | 把证据节点和假说连接，支持多人协作 | 侦探事务所 | 任务、证据 | common |
| 文书 | pm:verified_recipe_copy | 核验配方抄本 | 防水纸、三重印章 | 可被组织验证的魔药/仪式配方版本 | 教会/大学 | 知识经济 | rare |
| 文书 | pm:forged_recipe_copy | 伪造配方抄本 | 旧纸、假印章 | 带可发现错误的危险资料 | 隐秘组织/战利品 | 调查、魔药 | uncommon |
| 文书 | pm:sealed_testimony | 封存证词 | 证词册、见证蜡 | 保存 NPC/玩家确认的证词摘要 | 警局/法庭 | 仲裁人、案件 | uncommon |
| 文书 | pm:organization_dossier | 组织卷宗 | 档案夹、密码页 | 记录已知细胞、关系和可信度 | 调查任务 | 组织网络 | rare |
| 文书 | pm:spirit_route_chart | 灵界航图 | 星图墨、门纹膜 | 记录动态航路节点和最后更新时间 | 灵界市集 | 航路、交易 | rare |
| 文书 | pm:weather_almanac | 神秘天象历 | 装订纸、观测数据 | 预测月相和已知异常天气窗口 | 天文台 | 天气、活动 | uncommon |
| 文书 | pm:acting_reflection_journal | 扮演反思日志 | 皮面本、身份卡页 | 记录准则理解和“我不是角色”反思 | 第一次服药 | 扮演、锚 | common |
| 文书 | pm:anchor_ledger | 锚账本 | 木纹封面、关系页 | 显示锚类别、稳定变化原因和修复任务 | 序列5解锁 | 高序列、人性 | rare |
| 文书 | pm:artifact_custody_form | 封印物交接单 | 多联纸、编号封签 | 记录租借、保管、异常和归还 | 教会仓库 | 封印物、追索 | common |
| 文书 | pm:archaeology_field_log | 考古野外记录 | 防潮本、坐标格 | 记录地层、出处和假说，不直接给答案 | 大学 | 考古、时间线 | common |
| 文书 | pm:mythic_observation_report | 神话观测报告 | 烧蚀纸、保护封套 | 保存低保真观察结果和灼伤风险 | 序列4事件 | 研究、知识 | epic |
| 文书 | pm:newspaper_plate | 报纸铅版 | 铅字板、墨 | 锁定一期报纸版面，可被盗换 | 报社 | 生活、掩盖 | uncommon |
| 文书 | pm:bounty_warrant | 悬赏令 | 官印、目标摘要 | 生成合法追踪任务和奖励托管 | 悬赏公会 | 通缉、任务 | common |
| 文书 | pm:railway_timetable | 铁路时刻表 | 印刷纸 | 显示服务器交通班次和延误事件 | 车站 | 交通、天气 | common |
| 文书 | pm:theatre_program | 剧院节目单 | 彩印纸 | 触发表演活动、角色扮演和传闻 | 剧院 | 生活、扮演 | common |
| 消耗品 | pm:nightwatch_tea_cup | 守夜茶 | 深色茶汤、星图杯 | 降低疲劳，连续饮用导致灵性恢复下降 | 守夜茶叶 | 生活、不眠者 | common |
| 消耗品 | pm:veil_pepper_stew | 帷幕椒炖菜 | 深红汤、雾气 | 短时抗低语，口渴增加 | 帷幕椒+肉/蔬菜 | Farmer’s Delight | common |
| 消耗品 | pm:moon_dew_tonic | 月露补剂 | 银蓝瓶 | 月夜恢复灵性，白天效果减半 | 月露百合 | 药师、月相 | uncommon |
| 消耗品 | pm:sunthread_poultice | 日丝敷料 | 金色纤维包 | 治疗流血/腐败，黑夜恢复慢 | 日丝草 | 医师、歌颂者 | common |
| 消耗品 | pm:memory_sage_cocoa | 忆香可可 | 棕色杯、银叶 | 梦境后恢复记忆线索，增加饱和度 | 记忆鼠尾草+可可 | 梦境、生活 | uncommon |
| 消耗品 | pm:purity_salt_biscuit | 净盐饼干 | 浅色硬饼 | 小幅抗污染但提高口渴 | 净盐草+面粉 | 旅行补给 | common |
| 消耗品 | pm:seafarer_anise_spirit | 航海茴香饮 | 琥珀瓶 | 抗晕船；服务器可改为无酒精版本 | 航海茴香 | 海上活动 | common |
| 消耗品 | pm:calm_mint_draught | 镇静薄荷剂 | 淡绿小瓶 | 降低压力，短时降低反应速度 | 普通薄荷+梦粉 | 心理治疗 | common |
| 消耗品 | pm:spirit_vision_drops | 灵视眼滴 | 透明滴管瓶 | 短时增强灵视但提高灼伤风险 | 灵界兰+溶剂 | 调查 | uncommon |
| 消耗品 | pm:anti_spore_fumigator | 驱孢烟罐 | 铁罐、白烟 | 驱散疫蚋/尸花孢子，室内需通风 | 净盐草+炭 | 瘟疫事件 | common |
| 消耗品 | pm:ritual_chalk_refill | 仪式粉笔补充包 | 纸包粉末 | 补充粉笔盒耐久 | 矿粉+对应植物 | 仪式 | common |
| 消耗品 | pm:sealed_lamp_oil | 封印灯油 | 黑玻璃瓶 | 灵界航灯燃料，防风但会吸引灯蛾 | 影油+植物油 | 灵界 | uncommon |
| 消耗品 | pm:evidence_flash_powder | 证据闪光粉 | 纸袋镁粉 | 照相机一次闪光；可能惊扰灵体 | 工业材料 | 调查 | common |
| 消耗品 | pm:imprint_sedative | 烙印镇静剂 | 紫灰安瓿 | 临时降低低语，不减少真正烙印 | 高阶药师 | 特性 | rare |
| 消耗品 | pm:anchor_memory_candle | 忆锚蜡烛 | 暖白蜡烛 | 在家园/纪念地恢复锚，重复使用递减 | 锚忆木+蜡 | 锚、生活 | uncommon |
| 消耗品 | pm:stormglass_charge | 风暴玻璃充能瓶 | 蓝白玻璃 | 收集一次灵暴电荷用于工位/符咒 | 风暴藻+玻璃 | 天气、机械 | rare |
| 部件 | pm:occult_bearing | 神秘轴承 | 黄铜、银线 | Create 转动工位与灵性密封之间的接口 | Create 适配器 | 蒸馏/研磨 | uncommon |
| 部件 | pm:sealed_fluid_tank | 封印流体罐 | 厚玻璃、封蜡树脂 | 存放神秘液体并保存污染/来源 NBT | Create/IE | 流体、魔药 | rare |
| 部件 | pm:spirit_energy_coil | 灵性线圈 | 风暴藻、铜、星图地衣 | 把机械能/FE 转为受限仪式辅助能，不生成灵性 | IE/通识者 | 能量兼容 | rare |
| 部件 | pm:ae2_knowledge_pattern | 知识样板 | 认证抄本、空白样板 | AE2 只自动化已解锁且允许的中间步骤 | AE2 适配器 | 自动化、安全 | epic |
| 部件 | pm:botania_correspondence_lens | 对应透镜 | 植物材料、魔力透镜 | 用魔力触发对应关系检测，不替代魔药核心特性 | Botania 适配器 | 植物/仪式 | rare |
| 部件 | pm:farmers_delight_mystic_knife | 神秘厨刀 | 银钢、木柄 | 处理食材和低风险植物，不处理特性 | Farmer’s Delight | 料理、材料 | uncommon |
| 部件 | pm:curios_seal_socket | 封印饰品插座 | 银环、封签 | 为封印物提供 Curios 槽位并保存危险等级 | Curios | 装备、封印物 | rare |
| 部件 | pm:jade_occult_probe | 神秘探针模块 | 观察镜片、红石 | 让 Jade/WTHIT 显示已知信息与未知占位 | Jade/WTHIT | 指引、剧透 | uncommon |
| 部件 | pm:ftb_claim_ritual_beacon | 领地仪式信标 | 见证石、信标件 | 将大型仪式与领地权限/同意检查连接 | FTB Chunks/Teams | 多人安全 | rare |
| 部件 | pm:emi_hidden_recipe_token | 隐秘配方令牌 | 知识印章 | EMI/JEI 解锁事件触发刷新，不泄露未学配方 | JEI/EMI | 知识门控 | uncommon |
| 部件 | pm:geckolib_morph_core | 形态动画核心 | 特性残影、机械核心 | 仅作模型状态同步，不提供能力 | GeckoLib | 神话形态 | epic |
| 部件 | pm:server_history_archive_drive | 服务器历史档案盘 | 齿轮、纸带 | 导出活历史摘要，不含聊天与私人数据 | 服务器运营 | 赛季、备份 | rare |
| 封印物 | pm:artifact_3_091_kindly_umbrella | 3-091“好心的伞” | 黑伞、内衬有微笑纹 | 雨中自动遮挡异常天气；持续使用会替玩家答应陌生请求 | 委托链/封印库 | 天气、契约 | rare |
| 封印物 | pm:artifact_3_144_last_ticket | 3-144“末班票” | 褪色车票 | 错过交通时召来一次幽灵班次；目的地可能偏移 | 车站事件 | 交通、灵界 | rare |
| 封印物 | pm:artifact_3_207_honest_mirror | 3-207“诚实镜” | 小圆镜、裂纹 | 显示一个当前伪装；同时暴露持有者一项秘密状态 | 法庭遗址 | 伪装、调查 | rare |
| 封印物 | pm:artifact_2_031_sleeping_bell | 2-031“沉睡钟” | 无指针座钟 | 让区域入睡并进入共享梦；敲响者最后醒来 | 心理炼金任务 | 梦境、Boss | epic |
| 封印物 | pm:artifact_2_074_empty_ledger | 2-074“空账簿” | 厚黑账本 | 可抹去一笔游戏内债务；另一笔随机债务会被加重 | 地下拍卖 | 经济、律师 | epic |
| 封印物 | pm:artifact_2_118_weather_vane | 2-118“反向风标” | 铜制风标 | 把一种异常天气从此地转移到标记地点；必须提前放置警告 | 灯塔任务 | 天气、命运 | epic |
| 封印物 | pm:artifact_2_166_guest_mask | 2-166“客人的面具” | 无五官白面具 | 暂时获得目标组织的外围身份；会吸收该身份的敌人关系 | 剧院地下 | 潜入、组织 | epic |
| 封印物 | pm:artifact_2_203_merciful_chain | 2-203“仁慈锁链” | 银黑链条 | 束缚失控体并阻止死亡；使用者承受其部分压力 | 教会圣物 | 救援、囚犯 | epic |
| 封印物 | pm:artifact_1_012_city_whistle | 1-012“城市哨” | 黄铜警哨 | 吹响后城市所有已登记警戒线短时联动；也会暴露吹哨者位置 | 高位组织事件 | 城市、仲裁人 | epic |
| 封印物 | pm:artifact_1_048_memory_snowglobe | 1-048“旧日雪景” | 玻璃雪球 | 重演地点一次重大历史片段；观察者可能被困在错误版本 | 纪元主线 | 考古、梦境 | epic |
| 封印物 | pm:artifact_1_077_unfinished_door | 1-077“未完成的门” | 无门扇框架 | 通向与目的地象征相近的位置；每次使用缺失一个返回条件 | 灵界副本 | 旅行、风险 | epic |
| 封印物 | pm:artifact_1_109_publication_press | 1-109“必须出版” | 小型铅字机 | 每天强制出版一条它认为重要的消息，真假混合且无法销毁当期 | 报社主线 | 报纸、暴露 | epic |

## 69.2 物品样式统一规范

- **普通生活物品**：低饱和纸张、木、铁、布；轮廓清楚，避免所有物品都发光。
- **神秘材料**：只在边缘或裂隙出现 1–3 像素异常色；灵视开启后才显示第二层纹理。
- **途径物品**：共享几何语言而非固定颜色，例如门/眼/锁链/齿轮/波浪/书页等符号。
- **封印物**：外表首先像可信的日常物品，危险通过细微不协调、动画和 tooltip 逐步揭示。
- **高序列物品**：不靠彩虹光效表达稀有；使用多层材质、不可完全理解的符号、低频动画和环境反应。
- **原创化切换**：纹理文件不包含中文专名；显示名、编号、描述和符号覆盖层可由资源包替换。

## 69.3 NBT/数据组件约束（1.20.1 兼容写法）

v0.9 仍以 1.20.1 Forge 为基线，因此动态数据保存在受控 NBT/Capability 中；未来迁移到新版 Data Components 时通过适配层转换。所有可堆叠物品必须确保决定行为的 NBT 一致，否则强制不可堆叠，避免污染/来源信息被合并丢失。

# 70. 合成表、加工链与知识门控 v2

v0.9 新增 **80** 条代表性配方规格；与 v0.8 的工作台、坩埚和数据包配方合并后，目标为 180+ 可追踪配方。表中“门控”是显示/执行前置，不是简单隐藏：玩家可以拿到未知产物，但无法稳定复制。

## 70.1 新增配方总表

| 类型 | 配方 ID | 输入/条件摘要 | 输出 | 知识/条件门控 | 主要系统 |
|---|---|---|---|---|---|
| crafting | spirit_lens_mk2 | 烟色玻璃×3+黄铜锭×2+银线+灵界兰 | 可调灵视镜 | knowledge:spirit_optics | 通识者/教会 |
| crafting | portable_divination_board | 木板×4+银粒×4+纸芦纸 | 折叠占卜板 | knowledge:divination_tools | 占卜家 |
| crafting | sealed_tweezers | 铁锭+封蜡树脂+银粒 | 封印镊 | none | 通用 |
| crafting | spirit_chalk_case | 木板×3+神秘粉笔×6 | 灵性粉笔盒 | knowledge:ritual_basics | 仪式 |
| crafting | silver_thread_spool | 银粒×4+木棍 | 银丝线轴 | knowledge:silver_thread | 仪式/Curios |
| crafting | weather_observer | 望远镜+黄铜锭×2+星图纸 | 便携天象仪 | knowledge:astronomy | 星象师 |
| crafting | filtered_monocle | 可调灵视镜+烟色玻璃+银幕棉 | 过滤单片镜 | knowledge:mythic_viewing | 高序列 |
| tailoring | nightwatch_coat | 深色羊毛×6+银线×2+皮革 | 守夜长外套 | knowledge:nightwatch_uniform | 不眠者 |
| tailoring | spirit_silk_veil | 银幕棉×4+灵界兰×2+银线 | 灵丝面纱 | knowledge:spirit_warding | 药师 |
| jewelry | oath_brooch | 银锭+见证石片+封蜡 | 誓约胸针 | knowledge:contract_ritual | 律师/仲裁人 |
| jewelry | fortune_knot | 命运苜蓿×4+红染线+银粒 | 命运结 | knowledge:fortune_storage | 怪物 |
| crafting | archaeologist_satchel | 皮革×4+帆布×3+样本盒 | 考古挎包 | none | 考古 |
| candle_making | five_color_candle_set | 蜡×5+五类颜料+五类金属粒 | 五色仪式蜡烛组 | knowledge:correspondence_colors | 仪式 |
| ritual | silent_bell | 回声竹节+黑银合金+无声仪式 | 无声铃 | knowledge:silence_ritual | 教会 |
| ritual | identity_salt_circle | 净盐×8+记忆鼠尾草×4+身份卡 | 身份盐环 | knowledge:identity_anchor | 析出/晋升 |
| ritual | spirit_route_lantern | 灯笼+墓灯芯+罗盘羽+渡票 | 灵界航灯 | knowledge:spirit_route | 灵界 |
| distilling | memory_seal_wax | 封蜡树脂+记忆鼠尾草油+银粉 | 记忆封蜡 | knowledge:memory_seal | 知识/梦境 |
| distilling | imprint_washing_incense | 灰烬玫瑰×3+灵界兰×2+净盐 | 烙印洗涤香 | knowledge:imprint_cleansing | 特性 |
| engraving | anchor_oak_tablet | 锚忆橡木板+银线+记忆墨 | 锚忆木牌 | knowledge:anchor_objects | 锚 |
| smithing | storm_grounding_spike | 黑铁锭×3+风暴藻胶+铜线 | 灵暴接地钉 | knowledge:spiritual_storm | 天气/机械 |
| tailoring | dream_entry_ribbon | 梦罂粟纤维×4+银线×2 | 入梦缎带 | knowledge:shared_dream | 梦境 |
| engraving | witness_stone | 律文石+活墨+银粒 | 见证石 | knowledge:witness_ritual | 多人安全 |
| tailoring | mythic_viewing_screen | 银幕棉×8+烟色玻璃×2+过滤镜 | 神话观测幕 | knowledge:mythic_viewing | 高序列 |
| crafting | calamity_substitute_doll | 纸芦纸×4+灾果核+红线 | 替灾人偶 | knowledge:misfortune_transfer | 怪物 |
| engraving | spirit_contract_parchment | 羊皮纸+灵墨+银线 | 灵体契约羊皮纸 | knowledge:spirit_contract | 使魔/灵体 |
| crafting | case_board | 木板×6+线×4+铁粒 | 调查板 | none | 调查 |
| printing | verified_recipe_copy | 已知配方+防水纸+三类组织印章 | 核验配方抄本 | organization_reputation | 知识经济 |
| printing | forged_recipe_copy | 任意配方残页+旧纸+假印章 | 伪造配方抄本 | knowledge:forgery | 调查/风险 |
| printing | organization_dossier | 档案夹+证据卡×3+密码页 | 组织卷宗 | knowledge:organization_analysis | 组织 |
| printing | spirit_route_chart | 星图纸+灵界观测数据+门纹粉 | 灵界航图 | knowledge:spirit_route | 灵界 |
| printing | weather_almanac | 纸×8+一轮月相观测+装订线 | 神秘天象历 | knowledge:astronomy | 天气 |
| printing | acting_reflection_journal | 皮革+纸×6+身份卡 | 扮演反思日志 | first_potion | 扮演 |
| printing | anchor_ledger | 锚忆木板+纸×8+关系印章 | 锚账本 | sequence<=5 | 锚 |
| printing | artifact_custody_form | 纸×3+编号封签+组织印章 | 封印物交接单 | organization_member | 封印物 |
| printing | archaeology_field_log | 防水纸×8+炭笔+线 | 考古野外记录 | none | 考古 |
| printing | newspaper_plate | 铅锭×4+活墨+报纸版面 | 报纸铅版 | press_access | 报社 |
| cooking | nightwatch_tea_cup | 守夜茶叶+水+糖（可选） | 守夜茶 | none | 生活 |
| cooking | veil_pepper_stew | 帷幕椒+蔬菜+肉/豆类+碗 | 帷幕椒炖菜 | none | Farmer’s Delight |
| distilling | moon_dew_tonic | 月露+月纹兔毛（替代标签）+水 | 月露补剂 | knowledge:moon_tonic | 药师 |
| herbalism | sunthread_poultice | 日丝草纤维+净水+绷带 | 日丝敷料 | knowledge:field_medicine | 医师 |
| cooking | memory_sage_cocoa | 可可+记忆鼠尾草+奶/替代奶 | 忆香可可 | knowledge:dream_recovery | 梦境 |
| cooking | purity_salt_biscuit | 面粉+净盐草灰+油脂 | 净盐饼干 | none | 旅行 |
| distilling | calm_mint_draught | 薄荷+梦粉微量+纯水 | 镇静薄荷剂 | knowledge:sedative | 心理治疗 |
| distilling | spirit_vision_drops | 灵界兰+盐水+过滤玻璃粉 | 灵视眼滴 | knowledge:spirit_vision | 调查 |
| crafting | anti_spore_fumigator | 铁罐+净盐草+木炭+纸芯 | 驱孢烟罐 | knowledge:spore_control | 瘟疫 |
| distilling | sealed_lamp_oil | 植物油+影油+封蜡树脂 | 封印灯油 | knowledge:spirit_lamp | 灵界 |
| chemistry | evidence_flash_powder | 镁粉标签+纸袋+稳定盐 | 证据闪光粉 | knowledge:photography | 调查 |
| distilling | imprint_sedative | 镇静剂+烙印灰+记忆鼠尾草 | 烙印镇静剂 | knowledge:imprint_medicine | 特性 |
| candle_making | anchor_memory_candle | 蜡+锚忆木屑+纪念物拓印 | 忆锚蜡烛 | knowledge:anchor_objects | 锚 |
| weather_capture | stormglass_charge | 风暴玻璃瓶+灵暴接地钉+风暴天气 | 风暴玻璃充能瓶 | knowledge:storm_capture | 天气 |
| mechanical | occult_bearing | 黄铜机件+银线+密封油 | 神秘轴承 | compat:create | Create |
| mechanical | sealed_fluid_tank | 厚玻璃×4+铁框+封蜡树脂 | 封印流体罐 | knowledge:mystic_storage | Create/IE |
| mechanical | spirit_energy_coil | 铜线×4+风暴藻+星图地衣 | 灵性线圈 | compat:ie | IE |
| ae2_inscriber | ae2_knowledge_pattern | 空白样板+核验配方抄本+知识印章 | 知识样板 | compat:ae2 | AE2 |
| botania_mana_infusion | botania_correspondence_lens | 魔力透镜+对应材料标签+银线 | 对应透镜 | compat:botania | Botania |
| smithing | farmers_delight_mystic_knife | 厨刀+银钢锭+封印镊 | 神秘厨刀 | compat:farmers_delight | 料理 |
| jewelry | curios_seal_socket | 银环+封签+见证石片 | 封印饰品插座 | compat:curios | Curios |
| mechanical | jade_occult_probe | 红石比较器+灵视镜片+黄铜壳 | 神秘探针模块 | compat:jade | Jade/WTHIT |
| ritual | ftb_claim_ritual_beacon | 见证石×4+信标部件+领地许可 | 领地仪式信标 | compat:ftb_chunks | FTB |
| printing | emi_hidden_recipe_token | 知识印章+纸+认证墨 | 隐秘配方令牌 | compat:jei_emi | JEI/EMI |
| mechanical | geckolib_morph_core | 机械核心+神话残像灰+特性标签 | 形态动画核心 | sequence<=4 | GeckoLib |
| mechanical | server_history_archive_drive | 纸带×8+黄铜齿轮+档案印章 | 服务器历史档案盘 | admin_craft_only | 运营 |
| mortar | night_eclipse_powder | 夜蚀花×2 | 夜蚀花粉 | knowledge:material_processing | 魔药 |
| mortar | grave_bell_powder | 墓铃花×2 | 墓铃根粉 | knowledge:material_processing | 安魂 |
| distilling | storm_kelp_oil | 风暴藻×3+溶剂 | 导电藻油 | knowledge:distillation | 机械/水手 |
| drying | spirit_orchid_dry | 灵界兰+避光环境 | 干燥灵兰瓣 | knowledge:drying | 魔药 |
| moonlight_soak | moon_dew_concentrate | 月露×4+满月一夜 | 浓缩月露 | knowledge:moon_processing | 药师 |
| campfire | purity_salt_ash | 净盐草+低火 | 净盐草灰 | none | 净化 |
| smoking | veil_pepper_flake | 帷幕椒+烟熏 | 帷幕椒片 | none | 料理/符咒 |
| grafting | silverveil_seed | 棉花+月露百合+银粉 | 银幕棉种 | knowledge:mystic_grafting | 耕种者 |
| grafting | anchor_oak_sapling | 橡树苗+记忆鼠尾草+纪念拓印 | 锚忆橡树苗 | knowledge:mystic_grafting | 锚 |
| grafting | brassroot_cutting | 树根+黄铜粉+机械润滑 | 黄铜根插条 | knowledge:mystic_grafting | 机械 |
| ritual | characteristic_layer_separation | 特性束+身份盐环+洗涤香+对应材料 | 析出的特性层 | knowledge:characteristic_separation | 高序列 |
| ritual | imprint_cleansing | 特性束+洗涤香+地点锚+三名见证者 | 低烙印特性束 | knowledge:imprint_cleansing | 高序列 |
| ritual | spirit_route_opening | 灵界航灯+航图+渡票+稳定锚 | 临时灵界航路 | knowledge:spirit_route | 灵界 |
| ritual | shared_dream_entry | 入梦缎带+梦锚闹钟+见证石+梦粉 | 共享梦境实例 | knowledge:shared_dream | 梦境 |
| ritual | mythic_safe_observation | 神话观测幕+过滤镜+保护圈+序列4目标同意 | 低保真观测记录 | knowledge:mythic_viewing | 高序列 |
| ritual | weather_sealing | 天象仪+星盘+对应容器+异常天气核心 | 封存天气瓶 | knowledge:weather_sealing | 天气 |
| ritual | anchor_restoration | 锚忆蜡烛+纪念物+关系见证+生活行动 | 锚稳定恢复 | knowledge:anchor_restoration | 锚 |
| ritual | false_honorific_training | 伪尊名碎片+教学祭坛+安全封锁 | 错误响应模拟事件 | tutorial:honorific | 指引 |

## 70.2 配方类型注册

| 类型 ID | 界面/方块 | 服务器校验重点 | 自动化策略 |
|---|---|---|---|
| pm:mortar | 神秘研钵 | 材料标签、粒度、污染 | Create 机械研磨可替代低风险步骤 |
| pm:distilling | 灵性蒸馏器 | 温度曲线、溶剂、冷凝器 | IE/Create 提供热和动力，但配方知识仍校验 |
| pm:moonlight_soak | 月光盆 | 月相、露天、天气、浸泡时间 | 不可离线瞬间完成；区块休眠保存进度 |
| pm:drying | 封闭干燥架 | 光照、湿度、风、容器 | 可由环境控制设备加速至上限 |
| pm:grafting | 嫁接台 | 亲本性状、季节、失败变异 | 不允许 AE2 无条件复制种苗 |
| pm:printing | 印刷机 | 版面、纸、墨、权限、知识来源 | Create 动力可批量印刷已批准内容 |
| pm:engraving | 铭文台 | 符号顺序、载体、工具精度 | 机械臂只能执行已保存模板 |
| pm:ritual | 仪式编排器 | 参与者、同意、场地、时间、材料、尊名 | 禁止完全无人值守高风险仪式 |
| pm:weather_capture | 天象封存装置 | 真实天气、容器容量、接地 | 只捕获事件能量，不复制天气核心 |
| pm:characteristic_separation | 特性析出阵 | 特性束、身份锚、见证、风险结算 | 不可自动化/不可批量 |

## 70.3 完整配方 JSON 示例

```json
{
  "type": "pm:distilling",
  "id": "pm:imprint_washing_incense",
  "schema_version": 2,
  "canon_status": "adaptation",
  "knowledge_gate": "pm:knowledge/imprint_cleansing",
  "ingredients": [
    {"tag": "pm:plants/ash_rose", "count": 3},
    {"tag": "pm:plants/spirit_orchid", "count": 2},
    {"tag": "pm:purification_salts", "count": 1}
  ],
  "solvent": {"fluid": "minecraft:water", "amount": 250},
  "temperature": {"min": 340, "max": 380, "unit": "K"},
  "duration": 600,
  "result": {"item": "pm:imprint_washing_incense", "count": 2},
  "failure_results": [
    {"when": "temperature_high", "item": "pm:burnt_occult_residue"},
    {"when": "ingredient_polluted", "item": "pm:corrupted_incense"}
  ],
  "compat": {"create_heating": true, "immersive_engineering_heat": true}
}
```

## 70.4 JEI/EMI 展示规则

- 未解锁配方仅显示“未知知识条目”和可能的研究来源，不显示输入格。
- 已获得但未核验的配方显示玩家当前掌握版本，并用“可信度未知”提示，不自动纠错。
- 服务器只把已解锁配方同步给客户端；客户端查询不能越权读取服务器全表。
- 配方解锁后触发 JEI/EMI runtime refresh；没有对应 Mod 时不加载适配类。
- 仪式配方不在 JEI 中展示尊名全文和高剧透结果，只链接到手账。

# 71. 状态 Buff/Debuff 全表 v0.9

新增/重构 **64** 个状态。所有负面状态都标明处理路线；不可瞬间解除的状态也必须有可预期的恢复过程。

| ID | 名称 | 类别 | 效果摘要 | 解除/缓解 | 主要粒子 |
|---|---|---|---|---|---|
| pm:whisper_exposure | 低语暴露 | 精神 | 偶发错误字幕/声音；高层提高占卜噪声 | 离开源头、隔音符咒、祈祷 | 灰紫文字粒 |
| pm:knowledge_burn | 知识灼伤 | 高序列 | 技能栏错位、破碎知识、失控压力 | 断视、过滤镜、保护仪式 | 几何灼痕 |
| pm:imprint_agitation | 烙印躁动 | 特性 | 额外低语、消化检定变难 | 镇静剂、净化、身份锚 | 暗色脉冲 |
| pm:extra_characteristic_load | 额外特性负担 | 特性 | 灵性小增但污染/失控权重提高 | 析出、净化、剧情处理 | 多层光环 |
| pm:role_overidentification | 过度扮演 | 扮演 | 行为建议偏向序列人格 | 日志、生活任务、关系锚 | 面具残影 |
| pm:identity_fracture | 身份裂隙 | 精神 | 锚恢复降低、梦境出现替代自我 | 心理治疗、纪念物、团队陪伴 | 裂纹边框 |
| pm:anchor_stable | 锚稳定 | 高序列 | 降低神话形态人性消耗 | 维护锚 | 暖白细线 |
| pm:anchor_erosion | 锚蚀 | 高序列 | 锚效果下降、熟悉地点褪色 | 纪念活动、修复地标 | 褪色方块尘 |
| pm:spiritually_exhausted | 灵性枯竭 | 资源 | 能力禁用、自然恢复延迟 | 休息、光照、药剂 | 透明雾 |
| pm:ritual_focus | 仪式专注 | 仪式 | 提高稳定，移动/受击会打断 | 完成或取消仪式 | 环形符文 |
| pm:ritual_backlash | 仪式反噬 | 仪式 | 随机对应关系负面与材料灼伤 | 对症处理、组织救援 | 逆流火花 |
| pm:honorific_misdirection | 尊名误导 | 仪式 | 祈祷路由不稳定 | 停止仪式、核验知识 | 错位字形 |
| pm:prayer_overload | 祈祷过载 | 高序列 | 回应预算恢复下降、低语增加 | 拒绝队列、维护锚 | 密集星点 |
| pm:spirit_route_lost | 灵界迷航 | 灵界 | 坐标漂移、地图不可用 | 航灯、摆渡人、漂流事件 | 方向箭碎片 |
| pm:spirit_tide | 灵潮充盈 | 天气 | 灵性能力增强、污染风险提高 | 等待天气结束 | 彩边雾滴 |
| pm:industrial_smog | 工业烟雾 | 环境 | 视野/呼吸/食物恢复下降 | 面具、室内净化 | 黑灰烟 |
| pm:whispering_rain_soaked | 低语雨浸染 | 天气 | 随机传闻和错误提示 | 换衣、烘干、净化 | 字幕雨滴 |
| pm:memory_snow_touched | 记忆雪触碰 | 天气 | 看见历史片段，可能混淆任务时间 | 封存样本、取暖 | 场景雪片 |
| pm:fate_debt | 熵债 | 命运 | 未来事件坏运权重提高 | 自然结算、替灾物、救援行动 | 黑白骰点 |
| pm:winning_momentum | 胜势 | 命运 | 正确决策累积，可用于关键判定 | 消耗/失败清零 | 金色轨迹 |
| pm:misfortune_delayed | 厄运延期 | 命运 | 负面事件倒计时延后但更可见 | 准备保险、主动结算 | 悬停裂片 |
| pm:desire_pressure | 欲望压力 | 深渊 | 情绪操控易反噬 | 自我约束、心理防护 | 红色丝线 |
| pm:depravity_trace | 罪迹 | 深渊 | 组织追踪和恶意生物感知提高 | 承担后果、救赎任务 | 暗红脚印 |
| pm:malice_marked | 恶意标记 | 深渊 | 受到来源诅咒更强 | 净化、离开区域 | 黑红眼点 |
| pm:restraint | 克制 | 被缚者 | 降低变形压力但限制属性 | 主动解除 | 银链环 |
| pm:transformation_pressure | 变形压力 | 被缚者 | 达到阈值触发形态/失控 | 束环、镇静、月相管理 | 皮下脉动 |
| pm:werewolf_form | 狼人形态 | 被缚者 | 速度/近战/嗅迹增强，银弱点 | 主动结束、日出 | 月纹毛屑 |
| pm:wraith_form | 怨魂形态 | 被缚者 | 穿越薄墙、物理抗性，灵性弱点 | 计时结束、安魂 | 镜面雾 |
| pm:corpse_stasis | 尸化迟缓 | 死亡 | 濒死倒地，可被救回 | 救援仪式、超时 | 冷白气 |
| pm:sanctuary | 庇护 | 教会 | 灵性恢复与低语抗性提高 | 离开区域 | 暖光尘 |
| pm:excommunicated | 组织逐离 | 社会 | 教会服务关闭、追索概率提高 | 赎罪/复核任务 | 破碎徽记 |
| pm:wanted | 通缉 | 社会 | 巡逻与悬赏追踪 | 时间、投案、辩护 | 红印章 |
| pm:credible_witness | 可信证人 | 调查 | 证词权重提高 | 撒谎被发现后失去 | 蓝墨印 |
| pm:perjury | 伪证 | 调查 | 仲裁/律师能力反噬、信誉下降 | 公开更正、赔偿 | 裂字 |
| pm:contract_bound | 契约约束 | 社会 | 履约奖励、违约惩罚 | 完成、双方解除、裁定 | 银线结 |
| pm:breach_of_contract | 失信 | 社会 | 交易和组织信任下降 | 履约补偿、仲裁 | 断线 |
| pm:jurisdiction | 管辖 | 秩序 | 区域命令获得证据加成 | 离开/撤销 | 方形边界 |
| pm:usurpation | 僭越 | 秩序 | 无证据命令反噬 | 公开复核、承担惩罚 | 倒置徽章 |
| pm:corruption_field | 腐化领域 | 秩序 | 交易偏差和争执上升 | 秩序锚定、离开 | 暗金币尘 |
| pm:disorder_debt | 混乱债务 | 秩序 | 规则扭曲越多后果越重 | 重建秩序、偿还 | 扭曲网格 |
| pm:occult_load | 神秘负荷 | 知识 | 高层导致低语、随机符号与施法失败 | 封签、教学、遗忘仪式 | 书页环 |
| pm:prepared_formula | 术式预备 | 知识 | 储存一次卷轴/术式 | 施放或超时 | 符号槽 |
| pm:knowledge_sealed | 知识封存 | 知识 | 相关配方/能力暂时不可用但低语降低 | 解除封签 | 封蜡图标 |
| pm:mechanical_overload | 机械过载 | 机械 | 效率提高、磨损累积 | 停机维护 | 齿轮火花 |
| pm:prototype_instability | 原型不稳 | 机械 | 构造体故障概率提高 | 校准、替换部件 | 抖动螺丝 |
| pm:archaeological_context | 出处完整 | 考古 | 遗物价值与假说可信度提高 | 错误搬运会失去 | 土层标记 |
| pm:artifact_leak | 封印泄漏 | 封印物 | 周期触发负面效果 | 重新封印、归还 | 容器裂光 |
| pm:artifact_resonance | 封印共鸣 | 封印物 | 多件高危封印物互相强化代价 | 分库存放 | 双重波纹 |
| pm:spore_infection | 孢子感染 | 生态 | 分阶段虚弱/咳嗽/植物异变 | 药物、烟熏、隔离 | 绿色孢子 |
| pm:spirit_parasite | 灵体寄生 | 生态 | 灵性恢复被吸收、梦境异常 | 会客法、驱逐仪式 | 透明触须 |
| pm:calmed_spirit | 安魂 | 死亡 | 亡灵敌意降低、可交流窗口 | 超时 | 墓灯微光 |
| pm:nightmare_mark | 梦魇标记 | 梦境 | 梦魇猎犬追踪、睡眠风险提高 | 梦境治疗、守夜 | 黑蓝爪印 |
| pm:dream_lucid | 清醒梦 | 梦境 | 可主动识别象征与退出点 | 压力过高会失去 | 银色眼睑 |
| pm:memory_fixed | 记忆固定 | 梦境 | 防止一条线索被篡改 | 使用封蜡、超时 | 暖色书签 |
| pm:time_drift_fast | 时漂加速 | 天气 | 作物/机器/状态计时加快 | 时间锚、离区 | 上升尘 |
| pm:time_drift_slow | 时漂迟缓 | 天气 | 移动与设备周期减慢 | 时间锚、离区 | 下落尘 |
| pm:storm_charged | 灵暴充能 | 天气 | 电系/风系能力增强，金属导电 | 接地、释放 | 蓝白电弧 |
| pm:sea_sickness | 晕船 | 生活 | 移动和瞄准波动 | 茴香饮、休息 | 浅绿泡 |
| pm:well_rested_humanity | 生活安定 | 生活 | 锚和压力恢复提高 | 高压/战斗会消耗 | 暖灯点 |
| pm:urban_fear | 城市恐慌 | 社会 | NPC 夜间关闭、价格和传闻变化 | 救援、报纸、事件解决 | 窗帘剪影 |
| pm:masked_identity | 伪装身份 | 社会 | 进入特定组织区域，证据积累会破除 | 主动解除/被识破 | 面具薄影 |
| pm:mythic_aftershock | 神话余震 | 高序列 | 形态结束后理智/灵性恢复受限 | 锚活动、休息 | 残留几何 |
| pm:humanity_recalled | 人性回忆 | 高序列 | 短时抵抗人格侵蚀 | 纪念物/关系事件 | 照片光点 |
| pm:graceful_recovery | 受控恢复 | 安全 | 失控救援后防止连续触发 | 时间、治疗完成 | 稳定圆环 |

## 71.1 状态实现规范

1. 长期状态存 Capability/附件数据，原版 MobEffect 只承担可见短时层；防止死亡/维度切换丢失。
2. 客户端动画不决定效果；服务器权威计算层数、持续时间和免疫。
3. 同类状态采用显式叠加规则：刷新、加层、取最大、互斥、转化，禁止默认猜测。
4. 高风险状态在手账里显示来源和处理方向，但未学知识时不显示完整配方。
5. 任何导致界面错位、闪烁、声音侵入的效果都有无障碍替代模式。

# 72. 粒子、材质、模型、音效、天气渲染与 UI 规范 v2

在 v0.8 的 34 种粒子基础上新增 **38** 种，目标总数 **72**。新增粒子强调“信息表达”，每种都对应状态、天气或交互，不做无意义常驻烟花。

## 72.1 新增粒子表

| ID | 名称 | 视觉语言 | 使用点 | 预算 |
|---|---|---|---|---|
| pm:spirit_mist_mote | 灵雾微粒 | 半透明彩边小滴 | 灵界雾/航路 | 低 |
| pm:knowledge_glyph_burn | 知识灼符 | 短暂几何符号灼痕 | 神话直视/研究事故 | 中 |
| pm:imprint_pulse | 烙印脉冲 | 从特性向持有者扩散的暗环 | 特性躁动 | 低 |
| pm:anchor_thread | 锚线 | 暖色细线连接人/地/物 | 锚恢复/高序列 | 中 |
| pm:anchor_fray | 锚线断裂 | 褪色线头和碎片 | 锚蚀 | 中 |
| pm:identity_crack | 身份裂纹 | 屏幕边缘细裂纹 | 身份裂隙 | 低 |
| pm:role_mask_afterimage | 角色面具残影 | 面具轮廓短闪 | 过度扮演 | 低 |
| pm:prayer_star | 祈祷星点 | 远处升起的深红/自定义星点 | 祈祷队列 | 低 |
| pm:honorific_misroute | 尊名错位字 | 错位字符沿阵式倒流 | 错误尊名 | 中 |
| pm:spirit_route_arrow | 灵路箭屑 | 短箭头碎片沿航路漂浮 | 灵界导航 | 低 |
| pm:memory_snow_scene | 忆雪场景片 | 雪片内短暂显影轮廓 | 记忆雪 | 高 |
| pm:whisper_rain_text | 低语雨字滴 | 落地化为不可读字符 | 低语雨 | 中 |
| pm:fate_dice_spark | 命骰火花 | 黑白骰点闪烁 | 幸运/厄运 | 低 |
| pm:entropy_crack | 熵债裂片 | 悬停后突然坠落的裂片 | 厄运结算 | 中 |
| pm:jurisdiction_grid | 管辖网格 | 方形细线标出区域 | 法庭/命令 | 低 |
| pm:disorder_warp | 混乱扭格 | 网格弯曲、局部翻转 | 规则扭曲 | 中 |
| pm:contract_silver_thread | 契约银线 | 双方之间短暂银线结 | 契约签订 | 低 |
| pm:breach_snap | 失信断线 | 银线断裂、墨点飞散 | 违约 | 低 |
| pm:desire_string | 欲望弦 | 颜色随情绪变化的细线 | 欲望观察 | 中 |
| pm:depravity_footprint | 罪迹足印 | 暗红脚印逐渐渗入地面 | 罪迹追踪 | 低 |
| pm:restraint_chain_glint | 克制链光 | 银链环绕肢体 | 束缚/变形 | 低 |
| pm:wraith_mirror_fog | 怨镜雾 | 镜面冒出冷雾和反向轮廓 | 怨魂形态 | 中 |
| pm:occult_page_orbit | 神秘书页环 | 小型书页绕玩家旋转 | 神秘负荷 | 低 |
| pm:sealed_knowledge_wax | 知识封蜡 | 蜡滴覆盖符号 | 知识封存 | 低 |
| pm:prototype_screw_pop | 原型螺丝火花 | 螺丝与弹簧短促弹出 | 机械故障 | 低 |
| pm:artifact_leak_haze | 封印泄雾 | 从物品裂缝渗出的定向雾 | 封印泄漏 | 中 |
| pm:artifact_resonance_wave | 封印共鸣波 | 两物品间往返波纹 | 封印共鸣 | 中 |
| pm:spore_cloud_cluster | 孢子簇 | 群集孢子使用一批粒子 | 尸花/疫蚋 | 中 |
| pm:calmed_soul_lantern | 安魂灯点 | 暖白灯点缓慢上升 | 安魂 | 低 |
| pm:nightmare_claw_smoke | 梦魇爪烟 | 黑蓝爪痕化烟 | 梦魇标记 | 中 |
| pm:dream_lucid_ring | 清醒梦环 | 银色同心环 | 梦境识别 | 低 |
| pm:time_drift_up | 时漂上尘 | 粒子逆重力上升 | 时间加速 | 中 |
| pm:time_drift_down | 时漂下尘 | 粒子缓慢重落 | 时间迟缓 | 中 |
| pm:storm_charge_arc | 灵暴电弧 | 短链式蓝白电弧 | 灵性风暴 | 中 |
| pm:urban_fear_window | 恐慌窗影 | 远处窗帘后人影闪过 | 城市恐慌 | 低 |
| pm:mythic_aftershock_glyph | 神话余震符 | 不完整符号碎裂消散 | 形态结束 | 中 |
| pm:humanity_photo_glow | 人性照片光 | 暖光照片边缘和灰尘 | 锚/回忆 | 低 |
| pm:ritual_backflow | 仪式逆流 | 粒子沿法阵反方向冲回 | 仪式失败 | 高 |

## 72.2 粒子性能预算

- 客户端默认同屏自定义粒子上限 1200；低配模式 350；高配模式 2200。
- 群体孢子、灯蛾、文字雨使用批量/合并表现，不为每个个体创建实体。
- 远处粒子改用低频 billboard 或天气层；神话形态只在观察者视锥内生成知识符号。
- 网络只同步事件种子、位置、强度和持续时间，不逐粒子发包。
- 所有天气和高序列效果可单独降低密度，不能通过降低密度获得游戏优势。

## 72.3 材质与色彩

| 主题 | 基础材质 | 色彩策略 | 动态层 | 禁止做法 |
|---|---|---|---|---|
| 雾都生活 | 砖、木、铸铁、纸、羊毛 | 低饱和暖灰与煤烟冷色对照 | 窗光、煤气灯呼吸 | 每个方块都做脏污噪点 |
| 教会安全 | 石、深色木、黄铜、蜡 | 稳定暖光与大面积暗部 | 烛焰、钟摆、彩窗光 | 纯白过曝和廉价圣光 |
| 灵界 | 半透明膜、漂浮碎片、无固定材质 | 局部高纯度色，背景压低 | 颜色漂移、方向错觉 | 全屏彩虹与持续闪烁 |
| 深渊/污染 | 有机质、硫痕、黏液、裂纹 | 深红/黑只作局部警告 | 脉动、逆流、呼吸 | 过度血腥、以恶心代替恐怖 |
| 秩序 | 石、银、黑木、方格 | 冷静对称；混乱时几何扭曲 | 网格、印章、银线 | 单纯蓝色“法术光” |
| 知识/机械 | 黄铜、纸、玻璃、齿轮 | 暖铜与墨蓝，信息层清晰 | 书页、星图、机械节拍 | 蒸汽朋克零件无功能堆砌 |

## 72.4 GeckoLib 动画状态机扩展

统一状态：`idle / move / alert / cast_start / cast_loop / cast_release / hurt / breakdown / ritual / interact / transform_in / transform_loop / transform_out / death`。能力 JSON 只引用动画语义名，由实体/物品控制器映射具体动画。GeckoLib 的声音、粒子和自定义事件关键帧用于精确触发，但伤害判定仍由服务器能力执行器决定。

## 72.5 声音层

- **环境底层**：风、雨、机器、城市人声、远钟；保持可定位。
- **神秘侵入层**：低语、反向尾音、延迟脚步；根据压力和来源方向混入。
- **途径签名层**：纸牌切风、门框回响、银链、齿轮节拍、海浪、电弧、书页。
- **危险确认层**：每项高风险效果都有固定且可学习的声音标记，不能只靠视觉。
- **可访问性**：字幕描述声音类别和方向；低语可改为非语言音，不影响判定。

## 72.6 UI 层级

常驻 HUD 只保留灵性、压力/污染警示和当前高风险状态；消化、锚、组织、知识可信度进入手账。调查板使用节点图但限制同屏数量；能力轮盘按用途分组；复杂仪式使用分步确认和可撤销准备阶段。

# N. 事件、活动、指引与兼容（v0.9 新增）

# 73. 世界事件、周期活动、调查任务与可重复玩法

## 73.1 新增世界事件（在 v0.8 的 25 种基础上追加）

| ID | 名称 | 前兆 | 主体阶段 | 玩家选择 | 长期结果 |
|---|---|---|---|---|---|
| `pm:event/spirit_route_collapse` | 灵界航路坍塌 | 罗盘鸟迁徙、航图墨迹漂移 | 航站失联、漂流者求救、未知航路开放 | 修复、封锁、冒险改道 | 新航线/失踪名单/灵界暴露 |
| `pm:event/archive_rebellion` | 档案反叛 | 书页自行换位、档案蛛结网 | 城市记录被篡改，身份与债务错配 | 保护档案、追踪活墨、利用混乱 | 组织关系和历史假说变化 |
| `pm:event/last_train` | 不存在的末班车 | 时刻表多出一行、车站钟慢一分钟 | 幽灵列车停靠多个异常站 | 上车调查、疏散居民、封印车票 | 解锁交通节点或留下时间债 |
| `pm:event/public_miracle` | 公开神迹 | 大量同向祈祷、天气异常稳定 | 城市居民集结，教会与隐秘组织争夺解释权 | 承认、掩盖、调查、保护祈祷者 | 暴露度、信仰锚、组织政治改变 |
| `pm:event/anchor_eclipse` | 锚蚀日 | 熟悉物品褪色、NPC 忘记小事 | 高序列玩家锚持续衰减 | 举办纪念、修复地标、追查源头 | 活历史条目可被保留或扭曲 |
| `pm:event/characteristic_migration` | 特性迁徙 | 生物异常聚集、灵性金线朝同方向 | 多区块沉积汇成精英凝集体 | 组织狩猎、保护生态、诱导迁移 | 特性市场供给与生物群落变化 |
| `pm:event/false_saint` | 伪圣者降临 | 大量低风险祈祷被同一回应截获 | 代理人制造神迹和组织分裂 | 证伪、利用、救出信徒、反向追踪 | 新隐秘细胞或教会改革 |
| `pm:event/fog_quarantine` | 雾区隔离 | 工业雾变厚、居民咳嗽、灯影异动 | 街区封锁、物资短缺、灵体增长 | 医疗、运输、走私、查源 | 街区状态变为恢复/贫困/废弃 |
| `pm:event/memory_auction` | 记忆拍卖 | 地下市场出现无出处照片 | 玩家竞拍记忆、身份、历史线索 | 购买、揭露、抢救、替换赝品 | 身份锚、案件和纪元假说变化 |
| `pm:event/court_of_silence` | 无声法庭 | 城市声音逐步消失 | 律令石像审判所有“未履约者” | 提交证据、辩护、破坏规则核心 | 契约生态重置或秩序强化 |
| `pm:event/desire_carnival` | 欲望嘉年华 | 剧院节目单自行印刷 | 城市情绪放大、面灵和欲望幼体出现 | 参与并自控、关闭舞台、心理救援 | 过度扮演、城市恐慌、剧院状态 |
| `pm:event/full_moon_chains` | 满月锁链夜 | 月制束环发热、锁链藤开花 | 囚犯途径与诅咒生物变形压力激增 | 建庇护、狩猎、治疗、释放旧怨 | 被缚者组织声望和生态变化 |
| `pm:event/knowledge_flare` | 知识耀斑 | 天文台符号过亮、书页发热 | 破碎知识随机显现并灼伤观察者 | 抄录、封存、公开、销毁 | 知识库扩展或污染加深 |
| `pm:event/fate_hail_insurance` | 命雹与保险危机 | 小事故连续发生、悬赏异常 | 保险机构、怪物途径与灾祸教士争夺熵债 | 追责、转移、救援、公开审计 | 经济规则与命运活动权重改变 |
| `pm:event/sea_of_singing_lights` | 歌光之海 | 海面出现移动光带 | 船队被诱入灵性风暴和雾鲸迁徙区 | 跟随、绕行、采样、护航 | 新海域航线、海怪生态、港口繁荣 |
| `pm:event/sealed_artifact_strike` | 封印物罢工 | 多件封印物同时拒绝生效 | 共鸣使保管规则失效、仓库暴露 | 分离、协商拟态意识、紧急转运 | 封印制度改革或灾难性泄漏 |
| `pm:event/history_overlap` | 纪元重叠 | 旧建筑轮廓覆盖城市 | 不同时代结构与 NPC 投影短时共存 | 考古、救援、阻止历史固化 | 新遗址、假说更新、街区永久改造 |
| `pm:event/ritual_blackout` | 仪式停摆 | 蜡烛无法点燃、尊名无回应 | 区域所有常规仪式失效，机械与生活系统受压 | 查找遮蔽源、改用物理方法、撤离 | 推动非神秘职业与组织合作 |
| `pm:event/saint_observation` | 圣者观测窗 | 天空出现无法理解的符号 | 高位投影短暂经过，知识灼伤扩散 | 断视、低保真观测、疏散、祈祷 | 高序列知识、城市创伤、组织争夺 |
| `pm:event/railway_conspiracy` | 铁路阴谋 | 货运清单和时刻表冲突 | 多组织争夺一列封印物/特性货运 | 护送、卧底、调包、公开截停 | 经济、组织战争、交通安全 |
| `pm:event/city_wide_dream` | 全城同梦 | 居民描述相同细节 | 城市夜间进入共享梦境，现实身体无防护 | 分队守夜/入梦、寻找梦核、唤醒 | 心理创伤或共同锚形成 |
| `pm:event/quiet_harvest` | 寂静丰收 | 农作物异常整齐、虫鸣消失 | 作物高产但逐步失去种子与生态多样性 | 接受收益、烧毁、研究、恢复生态 | 食物价格、耕种者路线、荒芜风险 |
| `pm:event/gray_fog_market_crash` | 灰雾市场崩盘 | 报价失真、祈求延迟 | 假特性和伪配方冲击市场 | 鉴定、做市、冻结交易、追查源头 | 知识经济规则和塔罗会信任变化 |

## 73.2 活动玩法（不依赖主线也能长期游玩）

| 活动 | 周期 | 核心操作 | 奖励 | 与途径/系统连接 |
|---|---|---|---|---|
| 报社截稿夜 | 每周 | 采访、核实、排版、选择掩盖尺度 | 金钱、城市影响力、传闻 | 阅读者/观众/律师、暴露度 |
| 教会夜巡 | 每晚可选 | 分区巡逻、处理目击、安抚居民 | 声望、封印物租借券 | 不眠者/歌颂者/仲裁人 |
| 地下神秘聚会 | 每 3–7 日 | 交易、鉴定、暗号、识别卧底 | 稀有知识、组织线索 | 全途径、知识经济 |
| 灵界摆渡班 | 灵潮窗口 | 导航、支付代价、救援漂流者 | 航图、渡票、灵界材料 | 学徒/收尸人/怪物 |
| 纪元考古季 | 季节 | 发掘、记录地层、博物馆评审 | 历史假说、遗物、大学声望 | 通识者/阅读者/窥秘人 |
| 剧院演出周 | 每月 | 排练、演出、观众情绪管理 | 扮演消化、锚、传闻 | 小丑/观众/刺客/律师 |
| 海上护航 | 航班 | 配船、天气判断、战斗与维修 | 港口声望、海洋材料 | 水手/猎人/机械专家 |
| 神秘作物展 | 季节 | 培育、嫁接、鉴定、拍卖 | 种苗、农业技术、食谱 | 耕种者/药师/通识者 |
| 封印库盘点 | 每周 | 检查规则、分库存放、处理共鸣 | 租借权限、研究数据 | 教会/窥秘人/仲裁人 |
| 悬赏公会竞标 | 每日轮换 | 评估风险、组队报价、完成委托 | 金钱、通缉线索、称号 | 猎人/侦探/治安官 |
| 公共法庭日 | 每周 | 证词、证据、辩护、裁定 | 权威/论证点、社会声望 | 律师/仲裁人 |
| 梦境诊疗 | 事件/预约 | 守夜、入梦、识别象征、唤醒 | 压力治疗、梦境知识 | 观众/不眠者/收尸人 |
| 天文观测夜 | 月相/天象 | 架设仪器、记录、公开预测 | 星图、天气配方 | 星象师/天文学家/怪物 |
| 城市修复工程 | 灾后 | 建材运输、建造、居民安置 | 地点锚、生活指数 | 战士/机械/耕种者 |
| 灵性生态调查 | 季节 | 标记迁徙、采样、控制捕杀 | 图鉴、材料配额、生态奖励 | 药师/耕种者/猎人 |
| 灰雾议程 | 固定周期 | 匿名提案、交换情报、投票 | 席位影响、共同研究 | 塔罗会、全服政治 |
| 失控救援演练 | 每月 | 隔离、断视、束缚、治疗 | 团队认证、应急物资 | 多途径协作 |
| 生活纪念日 | 玩家自定 | 聚餐、写报、拍照、修缮家园 | 锚稳定、人性回忆 | 高序列与生活系统 |

## 73.3 新增任务链（在 v0.8 的 8 条基础上追加 12 条）

| 链 | 预计时长 | 主要场景 | 核心选择 | 结局分支 |
|---|---:|---|---|---|
| 九「会说话的时刻表」 | 3–5h | 车站、幽灵列车、灵界站台 | 救人还是追查货物 | 新航线/封印车票/时间债 |
| 十「谁改写了档案」 | 4–6h | 图书馆、警局、报社 | 恢复旧历史或保留更公正的新记录 | 组织关系、城市身份与报纸档案改变 |
| 十一「一场公开的奇迹」 | 5–8h | 广场、教堂、地下祭坛 | 掩盖、承认、证伪或保护 | 信仰锚/暴露度/伪圣者线 |
| 十二「月下的锁链」 | 4–7h | 监狱遗址、满月街区、镜面迷宫 | 镇压诅咒或帮助其克制 | 囚犯途径组织与城市态度改变 |
| 十三「记忆不属于买家」 | 6–9h | 地下拍卖、梦境、旧宅 | 归还、公开、封存或利用记忆 | 身份锚、家系线、拍卖规则改变 |
| 十四「沉默的判决」 | 5–7h | 无声法庭、法庭遗址 | 遵守、辩护、修改或摧毁规则 | 秩序/混乱阵营和法律事件池变化 |
| 十五「第七码头没有船」 | 4–6h | 港口、海雾、灵界渡口 | 追随雾鲸、救船员或封锁航路 | 海图、港口繁荣、海怪事件 |
| 十六「丰收之后」 | 4–8h | 农场、大学温室、荒芜地 | 接受高产、揭露代价或修复生态 | 食价、种苗与耕种者声望 |
| 十七「被印刷的真相」 | 3–6h | 报社、铅版机、城市街区 | 让危险真相公开到何种程度 | 暴露度、居民信任、组织追查 |
| 十八「神话形态观测协议」 | 8–12h | 高位事件、实验室、保护祭坛 | 研究收益与观察者安全平衡 | 安全观测技术/知识灼伤灾难 |
| 十九「锚正在忘记你」 | 8–15h | 玩家家园、关系 NPC、纪念地 | 选择要保留的人性痕迹 | 锚网络重构、序列 3 入场资格 |
| 二十「最后一份特性去了哪里」 | 10–18h | 多组织、生态迁徙、沉积区 | 狩猎、保护、调停或公开分配 | 特性市场、组织战争、凝集体 Boss |

## 73.4 调查任务标准结构

每个调查任务至少包含：案发现场、两条真实线索、一条自然误导、一条人为假线索、一个可错过但可替代的证据、至少两种解决方式、失败恢复节点和事后社会反馈。禁止把“调查”写成顺序点击 12 个高亮方块。

## 73.5 动态案件生成器

生成器使用“主体—动机—手段—地点—异常—掩盖—受害影响—证据”八槽模板，并由组织、天气和生态状态填充。生成后先运行逻辑校验：凶手/原因必须能产生全部关键证据；任何必需证据都至少有两个获取途径；目标离线或 NPC 死亡时可替换。

# 74. 新手指引、知识手账、任务提示与可访问性 v2

## 74.1 三层指引

1. **生存层**：如何获得工作、食物、住所、交通和普通工具。
2. **神秘层**：如何识别危险、记录线索、鉴材、拒绝未知仪式、处理污染。
3. **非凡层**：如何服药、扮演、维持自我、准备晋升、理解特性与锚。

玩家可以选择“剧情引导、轻提示、只显示安全警告”三种强度。服务器强制安全警告不会泄露解谜答案。

## 74.2 首两小时重构

| 时间 | 体验 | 学到的规则 | 可选路径 |
|---:|---|---|---|
| 0–15min | 选择普通职业、领取城市地图与报纸 | 经济、街区、交通 | 报童/学徒工/事务所助手/教堂志愿者 |
| 15–35min | 普通委托出现不合理细节 | 线索不是高亮答案 | 跟踪、询问、查档案、求助组织 |
| 35–55min | 首次异常天气或灵体前兆 | 危险可提前辨认，逃跑有效 | 庇护、拍照、记录、冒险接近 |
| 55–80min | 获得不完整配方与材料 | 知识需核验、JEI 不公开未知配方 | 大学、地下聚会、教会、自己实验 |
| 80–105min | 制作/服用第一份魔药 | 稳定性、低语、能力与代价 | 选择 5 条首发途径之一或延迟服用 |
| 105–120min | 第一次扮演与生活锚任务 | 扮演不是失去自我 | 加入组织、独立侦探、灰色市场 |

## 74.3 手账页面结构

- **首页**：当前风险、最近线索、推荐但非强制的下一步。
- **途径页**：已知序列、能力、扮演准则候选、反制和来源标记。
- **材料页**：观察结果、来源生态、处理方法、已知配方关系。
- **案件页**：证据、假说、证人、时间线、可信度与未解决矛盾。
- **组织页**：公开身份、已知单位、关系和信息来源，未知内容显示问号。
- **世界页**：结构、天气、灵界航路、历史假说和活历史。
- **人性页**：身份卡、锚、过度扮演与恢复任务；默认私密，不共享给其他玩家。

## 74.4 情境提示新增规则

Toast 只在首次发生或玩家主动开启重复提示时出现。连续高风险提示合并为一张卡，不遮挡准星。示例：

- “这份特性仍在低语。直接服用不是唯一选择。”
- “你理解了准则，但这次行为没有产生可验证结果。”
- “灵界航图已经过期；旧路线仍可能可行，但风险上升。”
- “这件封印物与背包中的另一件正在共鸣。”
- “附近居民开始把异常当作事实，城市暴露度正在上升。”
- “你正在扮演，但也需要记得自己是谁。”

## 74.5 可访问性

| 需求 | 设置 | 机制不变的替代表达 |
|---|---|---|
| 闪烁敏感 | `reduce_flashing` | 闪光改为亮度渐变+固定图标 |
| 低语不适 | `nonverbal_whispers` | 语言低语改为音色/字幕类别 |
| 色觉差异 | `symbolic_aura` | 情绪/污染颜色同时显示纹样 |
| 晕动/空间错觉 | `stable_spirit_camera` | 灵界漂移改为 UI 罗盘偏移 |
| 恐怖内容 | `abstract_horror` | 有机/面部变化改为几何轮廓 |
| UI 认知负荷 | `guided_ritual_ui` | 仪式分步、字段解释、撤销准备 |
| 听力 | 完整方向字幕 | 声音危险标记显示方向与类别 |
| 操作障碍 | 长按/连按替代、自动步行辅助 | 不自动完成判定，只减少输入负担 |

# 75. Mod 兼容矩阵与软依赖架构

## 75.1 兼容原则

- 核心 Mod 在没有任何外部内容 Mod 时完整可玩。
- 适配器只在目标 Mod 存在且版本能力探测成功时加载。
- 外部自动化可以替代重复劳动，不得绕过知识、特性守恒、仪式同意和高序列风险。
- 优先使用 Forge 标签、能力和公开 API；禁止反射私有字段作为长期方案。
- 每个兼容模块有独立数据包和 GameTest，可单独禁用。

## 75.2 兼容总表

| Mod/类别 | 适配等级 | 具体内容 | 不允许绕过 | 失败回退 |
|---|---|---|---|---|
| JEI | 高 | 已解锁配方、用途、工位分类、知识条目链接 | 未知配方、尊名全文、调查答案 | 使用手账内置配方页 |
| EMI | 中 | 与 JEI 同一知识门控接口、配方树 | 通过索引读取服务器全表 | 静默不加载 EMI 插件 |
| Curios | 高 | 面部、颈部、背部、腰带、护符、手部封印物槽 | 多槽叠加绕过封印共鸣 | 无 Curios 时使用单独饰品背包 |
| Create | 高 | 研磨、搅拌、动力印刷、蒸馏热源、机械臂执行已知模板 | 自动服药、高风险仪式、特性复制 | 原生工位手动加工 |
| Farmer’s Delight | 中 | 神秘料理、厨刀处理植物、宴会/纪念日食物 | 用烹饪刀处理特性或封印物 | 原版营火/锅具配方 |
| Immersive Engineering | 中 | 蒸馏热、灵暴接地、黄铜/钢构件、远距供电 | FE 直接生成玩家灵性 | 原生燃料/手动接地 |
| Applied Energistics 2 | 中 | 已知中间产物自动化、来源/污染 NBT 安全存储、知识样板 | 复制特性、唯一性、祈祷、封印物危险绕过 | 普通箱与样本柜 |
| Botania | 中 | 植物对应关系、魔力辅助仪式、自然材料互换标签 | 魔力替代核心非凡特性 | 原生植物/灵性材料 |
| FTB Quests | 高 | 任务镜像、队伍进度桥、管理员整合包路线 | 直接写玩家序列/消化 100% | 内置任务链 |
| FTB Teams/Chunks | 高 | 大型仪式同意、领地权限、组织小队 | 跨领地强制附身/破坏 | 原生许可列表 |
| Jade/WTHIT | 高 | 已知状态、工位阶段、生态观察、未知占位 | 显示隐藏身份和未解锁配方 | 手账/灵视提示 |
| Patchouli | 低/可选 | 将手账内容导出为轻量书 | 替代动态案件板和私密锚页 | 自研手账仍为主 |
| Ars Nouveau/同类魔法 | 低 | 通过标签兼容通用法术材料和仪式场地 | 把外部魔法自动视为途径权柄 | 仅材料与视觉兼容 |
| Origins | 低 | 普通种族/出身与途径数据分离 | 出身直接授予非凡序列 | 不适配时各自运行 |
| MineColonies | 中 | 城市职业、医院、警卫、仓库和神秘暴露事件 | 大量殖民地 NPC 变成免费信徒锚 | 只统计有关系事件的锚 |
| Simple Voice Chat | 可选 | 仪式吟唱存在性、区域静默效果的视觉同步 | 录音/存储真实语音内容 | 按键/文字吟唱 |
| Velocity/Bungee | 研究 | 跨服灰雾只同步席位摘要、市场挂单和活历史 | 跨服物品复制、完整玩家 Capability 镜像 | 单服灰雾 |

## 75.3 Curios 槽位规范

Curios 本身提供可扩展、可配置的饰品槽，因此 Project Mystery 只声明语义槽，不强制所有整合包采用相同数量：`face / necklace / back / belt / charm / hands`。同 ID 槽位由 Curios 合并兼容；服务器可以将高危封印物限制为专用 `sealed_artifact` 槽，放入普通背包时负面效果更强。

## 75.4 自动化红线

| 可自动化 | 半自动 | 永不全自动 |
|---|---|---|
| 普通植物采收、低风险研磨、蒸馏、印刷、料理、样本归档 | 已知魔药辅料预处理、封印物例行检查、天气观测 | 服药、晋升仪式、特性析出、尊名呼名、高危封印、祈祷回应、唯一性转移 |

# 76. 多人、服务器赛季、AI/QQ/直播桥接与隐私边界 v2

## 76.1 服务器角色分层

- **玩家角色**：普通人、非凡者、组织成员、灰雾席位。
- **剧情权限**：主持事件、生成委托、审阅报纸；不等于管理权限。
- **技术管理**：配置、备份、封禁、回滚；不通过游戏物品授予。
- **外部桥接**：只接收白名单事件摘要，不可执行任意命令。

## 76.2 小雨 AI 桥接事件白名单

允许出站：赛季开始/结束、公开 Boss、城市暴露警报、玩家自愿公开成就、报纸标题、服务器维护状态。默认禁止：私聊、坐标、背包内容、身份卡、锚关系、未公开组织身份、祈祷全文。

入站动作仅能触发预注册操作：发布公告、创建无奖励投票、查询公开状态、安排直播活动。任何改变物品/序列/声望的动作必须由游戏内管理员确认并写审计日志。

## 76.3 赛季模式

| 模式 | 时长 | 上限 | 重置 | 适用 |
|---|---:|---|---|---|
| 永久世界 | 无固定 | 序列 4 | 不重置角色；事件改变世界 | 私服/RP/长期建筑 |
| 标准赛季 | 12–16 周 | 序列 3 可选 | 高序列与唯一性封存，建筑/历史保留 | 公共服务器 |
| 终局赛季 | 6–10 周 | 序列 1 叙事化 | 赛季末结算神位战争 | 熟练团队 |
| 调查短季 | 2–4 周 | 序列 7–6 | 只重置案件状态 | 新玩家/直播活动 |

## 76.4 反作弊与恢复

- 能力、配方解锁、特性转移、市场交易、唯一性状态全部服务器权威。
- 对高价值物品记录来源事件、容器转移和销毁摘要；只用于查复制，不展示玩家隐私。
- 大型仪式开始前生成轻量快照，崩溃后回到“准备完成、尚未结算”而不是重复奖励。
- 事件实例有超时和管理员安全结束；NPC 卡死或区块卸载时可重建任务状态。
- 每日自动验证 SavedData、Capability schema 和关系图，不一致时隔离条目而非整服崩溃。


# O. 工程、路线图、测试与总账（v0.9 新增）

# 77. Forge 1.20.1 技术架构 v2、数据迁移与性能预算

## 77.1 注册体系：静态注册与数据包注册分离

Forge 1.20.x 官方文档建议静态游戏对象使用 `DeferredRegister`；数据包动态注册表不能靠 `DeferredRegister` 创建，必须通过 `DataPackRegistryEvent.NewRegistry` 注册。v0.9 因此采用两轨：

```java
// 静态对象：Item / Block / EntityType / ParticleType / MenuType...
public final class PMItems {
    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, ProjectMystery.MOD_ID);

    public static final RegistryObject<Item> SPIRIT_LENS_MK2 = ITEMS.register(
        "spirit_lens_mk2",
        () -> new SpiritLensItem(new Item.Properties().stacksTo(1))
    );
}

// 数据定义：Pathway / Sequence / Ability / Ritual / WorldEvent...
@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class PMDataRegistries {
    public static final ResourceKey<Registry<PathwayDefinition>> PATHWAYS =
        ResourceKey.createRegistryKey(new ResourceLocation(ProjectMystery.MOD_ID, "pathway"));

    @SubscribeEvent
    public static void onNewRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(PATHWAYS,
            PathwayDefinition.DIRECT_CODEC,
            PathwayDefinition.NETWORK_CODEC);
    }
}
```

不适合动态 Registry 的大批内容（呓语池、报纸模板、案件模板、关系图）继续使用 `SimpleJsonResourceReloadListener`，在 `AddReloadListenerEvent` 挂载，并在一次 reload 中完成解析、交叉引用、不可变快照替换。

## 77.2 Codec 与 Schema 版本

每类数据同时提供：

- `DIRECT_CODEC`：服务端加载完整定义；
- `NETWORK_CODEC`：只同步客户端展示和执行所需字段，剔除隐藏答案、管理员字段和未解锁内容；
- `schema_version`：当前 v0.9 通用版本 4；
- `DataFix`：旧字段名、ID 别名、默认值和拆分规则；
- `validation_report`：生成 `build/reports/pm-content-validation.json`。

## 77.3 Capability 生命周期修正

Forge Capability 返回 `LazyOptional`，并要求在 provider 生命周期结束时失效。v0.8 已有基础实现；v0.9 增加：

- Provider 在 `invalidateCaps` 中调用 `invalidate()`；
- 玩家克隆时只复制允许跨死亡保留的区段；
- 登录/重生/换维度只做摘要同步，详细知识按需分页；
- `PlayerMysteryData` 拆为 Core、Knowledge、Social、Endgame 四个 dirty mask；
- 长期任务和组织网络放 SavedData，不塞进每个玩家 NBT；
- 客户端只缓存展示数据，不接受客户端写回完整 NBT。

### v3 → v4 数据迁移

| 旧字段 | 新字段 | 迁移 |
|---|---|---|
| `Characteristics: List<ResourceLocation>` | `CharacteristicBundles` | 每个旧 ID 建单层 bundle，默认烙印与纯度 |
| `actingHistory` | `actingState` | 保留时间戳，新增语义标签、新颖度与过度扮演默认 0 |
| `sanity` | `sanity + anchorState + humanityState` | 旧理智保留，锚从空网络开始并发放建档任务 |
| `knownKnowledge: Set` | `knowledgeLedger` | 旧条目标记为 `verified_legacy`，不丢失 |
| `tarotTitle` | `prayerIdentity` | 仅迁移显示称号，不自动开放祈祷响应 |

## 77.4 网络层

包按用途分组：

| 通道 | 方向 | 内容 | 限制 |
|---|---|---|---|
| Core Sync | S→C | 灵性、压力、污染、当前状态摘要 | 变更或每 5 秒校正 |
| Ability Request | C→S | 能力 ID、目标意图、客户端序列号 | 服务端重算距离、冷却、权限；每秒限流 |
| Knowledge Page | 双向 | 请求已解锁页面；返回分页数据 | 每页压缩后 ≤64 KiB，不在登录时全量推送 |
| Investigation | 双向 | 移动节点、提交假说、标记证据 | 队伍权限与版本号冲突解决 |
| Ritual | C→S/S→C | 准备操作、参与同意、阶段反馈 | 每步服务端校验；高危操作二次确认 |
| Visual Event | S→C | 粒子/声音种子、强度、持续时间 | 不同步逐粒子数据 |
| Admin/Ops | 双向 | 明确白名单管理请求 | 仅 OP 权限，完整审计 |

所有客户端请求都携带递增序列号和玩家当前会话 nonce，防止旧包重放。大对象使用分页或数据包，不把 1000+ JSON 在登录阶段塞进单个网络包。

## 77.5 服务端权威能力执行

能力执行器遵循：解析意图 → 校验玩家状态/序列/冷却/灵性 → 解析服务器目标 → 权限/同意/领地检查 → 计算效果 → 写数据 → 广播视觉事件。客户端动画先行只能做 100–150ms 预测，服务端拒绝时平滑取消。

## 77.6 关系图编译器

`scripts/build_content_graph.py` 读取所有数据定义，输出：

- `content_graph.json`：完整 ID 与边；
- `orphan_report.csv`：无用途材料、无来源物品、无解除状态；
- `spoiler_leak_report.csv`：低剧透条目引用高剧透文本；
- `compat_report.csv`：硬引用缺失 Mod ID；
- `localization_report.csv`：缺失语言键；
- `asset_report.csv`：模型、纹理、动画、音效引用缺失。

CI 中任何 `error` 失败；`warning` 必须在 PR 说明中解释。关系图同时用于生成 Wiki 的“来源/用途/反制”页面。

## 77.7 数据生成与资产生成

- Java DataGenerator 生成基础 recipes、tags、loot tables、models、blockstates、advancements。
- Python/Gradle 脚本从 CSV/JSON 主表生成大批途径、能力、文案索引和 Wiki 页面。
- 自动生成文件头写 `generated=true` 和源文件路径，禁止手改；修改必须回到主表。
- 文案、数值、正典审计、资产状态分表，避免一个 CSV 超过可维护范围。
- 所有生成器使用稳定排序，减少无意义 Git diff。

## 77.8 兼容模块目录

```text
src/main/java/top/aurora/projectmystery/compat/
├─ CompatBootstrap.java
├─ curios/
├─ jei/
├─ emi/
├─ create/
├─ farmersdelight/
├─ immersiveengineering/
├─ ae2/
├─ botania/
├─ ftb/
├─ jade/
└─ voicechat/
```

每个模块通过 `ModList.get().isLoaded()` 加载入口，但类链接也必须隔离，避免 JVM 在目标 Mod 缺失时解析其类型。建议使用独立 compat source set 或只在 `DistExecutor`/反射边界后触发明确入口；不得让核心类字段直接声明外部 API 类型。

## 77.9 性能预算 v2

| 系统 | 服务器预算 | 客户端预算 | 降级策略 |
|---|---:|---:|---|
| 玩家核心 tick | 平均 <0.05ms/玩家/tick | — | 分片到 1s/5s 周期，事件驱动 dirty sync |
| 组织网络 | <2ms/世界/秒 | — | 抽象节点，不加载远处 NPC |
| 生态调度 | <3ms/维度/5s | — | 区域预算、休眠、群体实体 |
| 内容关系查询 | reload 时完成；运行 O(1)/O(log n) | 手账页 <16ms | 不在 tick 中遍历全图 |
| 神话形态 | 服务端效果 <0.3ms/实体/tick | 单实体渲染 <2ms | LOD、骨骼隐藏、低保真投影 |
| 粒子 | 只发事件种子 | 默认同屏 ≤1200 | 密度档位、合批、远距 billboard |
| 动态案件 | 生成 <50ms，异步准备/主线程提交 | UI 节点 ≤120 | 分页、折叠低权重证据 |
| 灵界航路 | 查询 <1ms | 地图刷新 <10ms | 节点缓存、只同步附近 |

## 77.10 存档安全

- 写 SavedData 使用 copy-on-write 快照和临时文件替换；
- 每次 schema 迁移前自动备份相关数据文件；
- 未知 ID 进入 `orphaned_entries`，保留原 NBT，不直接删除；
- 关闭可选数据包后，其物品变为“失效遗物”，恢复数据包可重新激活；
- 唯一性、市场、祈祷队列和活历史分别持久化，避免一个损坏拖垮全部；
- 提供 `/pm doctor scan|repair|export`，`repair` 只做有记录的安全修复。

# 78. 开发路线图 v0.9：从文档到可玩版本

## 78.1 状态定义

| 状态 | 含义 | 不能宣称什么 |
|---|---|---|
| `planned` | 只有设计 | 不能说“已加入 Mod” |
| `data_ready` | JSON/CSV、lang、校验通过 | 不能说能力可用 |
| `code_ready` | 执行器/实体/工位可运行 | 不能说资产和体验完成 |
| `asset_ready` | 模型/纹理/音效完成 | 不能说平衡合格 |
| `playable` | 可在开发环境完整走通 | 不能说发布质量 |
| `verified` | GameTest、多人、性能、回归验收通过 | 可计入正式内容量 |

## 78.2 总体阶段（建议 18–24 个月，按小团队现实规模）

| 阶段 | 建议周期 | 核心交付 | 依赖 | 退出标准 |
|---|---:|---|---|---|
| M0 内容基建 | 3–4 周 | Schema v4、关系图、生成器、迁移框架、CI | 无 | 100 个样例数据通过 reload/校验，v0.8 存档可迁移 |
| M1 核心垂直切片 | 6–8 周 | 占卜家 9–7、第一魔药、扮演 v2、特性 bundle、一个城市街区 | M0 | 新玩家 2 小时完整走通，无管理员干预 |
| M2 调查与生活 | 6–8 周 | 报社、事务所、警局、证据板、经济、3 个动态案件 | M1 | 4 人服务器连续 8 小时无任务锁死 |
| M3 首发五途径 | 8–10 周 | 占卜家/观众/猎人/偷盗者/学徒 9–5 | M1–M2 | 每途径生活/调查/战斗/风险各有闭环；无唯一最优途径 |
| M4 组织与封印物 | 6–8 周 | 七教会框架、5 个隐秘组织、24 个已验证封印物 | M2 | 组织行动能独立发生，封印物均有保管/泄漏/追回 |
| M5 灵界与梦境 | 8–10 周 | 灵界航路、共享梦境、12 种生态生物、6 种异常天气 | M1–M4 | 30 分钟灵界远征有进入、导航、风险、收益、退出闭环 |
| M6 第二批四途径 | 8–10 周 | 水手/不眠者/收尸人/歌颂者 9–5 | M5 | 海上、夜巡、亡灵、净化四种玩法均可独立组队 |
| M7 生产与兼容 | 6–8 周 | 10 个工位、JEI/EMI、Curios、Create、FD、Jade | M0–M6 | 目标 Mod 缺失不崩；自动化不绕过知识和特性 |
| M8 第三批六途径 | 10–12 周 | 战士/秘祈人/阅读者/刺客/耕种者/药师 9–5 | M3–M7 | 15 途径全部 `playable`，服务器 20 人压测达标 |
| M9 七途径补齐 | 10–12 周 | 本文第 60 章七途径 9–5 | M8 | 22 途径 9–5 均至少 `playable`；同意敏感能力验收 |
| M10 半神与神话形态 | 10–14 周 | 序列 4、知识灼伤、49+ 模型、锚系统 | M3–M9 | 5 条优先途径序列 4 `verified`，低配模式可用 |
| M11 世界与赛季 | 8–10 周 | 40+ 结构类别、48 世界事件、活历史、赛季结算 | M4–M10 | 12 周模拟世界不出现经济/特性不可逆枯竭 |
| M12 实验终局 | 12–16 周 | 序列 3–1 可选、祈祷、唯一性、神位战争 | M10–M11 | 可完全关闭；不授予管理员权力；赛季可安全封存 |
| 1.0 发布候选 | 6–8 周 | 本地化、教程、兼容、性能、版权/原创化处理 | 全部 | 100 小时公开测试无 P0/P1，授权或原创化达成 |

## 78.3 优先级分层

### P0：决定项目能否继续

- 数据 Schema、迁移、关系图和内容生成器；
- 核心玩家数据与网络安全；
- 第一份魔药、扮演、污染、失控、特性守恒；
- 一条 2 小时可玩切片；
- GitHub Actions 构建、GameTest 和存档回归。

### P1：形成差异化

- 调查板、组织行动、灰雾、灵界/梦境、动态报纸；
- 5 条首发途径 9–5；
- 封印物管理而非普通饰品；
- 神秘暴露度与生活锚。

### P2：扩大内容深度

- 其余 17 条途径；
- 生态、天气、海上、考古、剧院、法庭等活动；
- 兼容矩阵；
- 序列 4 与神话形态。

### P3：服务器终局

- 序列 3–1、祈祷响应、锚政治、唯一性和神位战争；
- 跨服灰雾；
- 第二部/非标准途径可选包。

## 78.4 小团队分工建议

| 角色 | 主要责任 | 每两周可交付量（保守） |
|---|---|---|
| 核心程序 | Capability、网络、执行器、工位、SavedData | 1 个中型系统或 3–5 个执行器 |
| 内容程序 | 数据加载、生成器、关系图、任务/事件 | 10–20 个数据条目接入 |
| 设计/数值 | 途径、状态、配方、事件与平衡 | 1 个序列包或 1 条任务链规格 |
| 3D/动画 | GeckoLib 模型与动画 | 1 个复杂实体或 2–3 个简单实体 |
| 2D/UI | 纹理、图标、手账和界面 | 20–40 物品纹理或 1 个复杂页面 |
| 文案/正典审计 | lang、手账、报纸、来源与原创化 | 30–60 条短文案或 1 个专题审计 |
| QA/运维 | GameTest、整合包、性能、服务器 | 每阶段完整回归与报告 |

若实际只有 1–2 名核心开发者，应先完成 M0–M4，冻结 22 途径全实装目标；文档内容可以保留，但排期不得假设不存在的人力。

## 78.5 两周 Sprint 模板

- 第 1 天：选定一个玩家可见目标和退出标准；
- 第 2–3 天：数据、接口、资产占位和测试先行；
- 第 4–7 天：最小实现；
- 第 8 天：多人试玩；
- 第 9–10 天：修复与数值；
- 第 11 天：兼容/低配/存档测试；
- 第 12 天：文档与 Wiki 自动生成；
- 第 13 天：冻结候选；
- 第 14 天：回顾，只接受 P0 修复。

任何 Sprint 最多同时进行一个新系统和一个内容包，避免“所有系统都有 30%”。

## 78.6 首三个 Sprint 的可执行清单

### Sprint A：内容图与迁移

1. 建 `schema_version=4` 通用元数据；
2. 实现 `CharacteristicBundle` Codec 与 v3→v4 迁移；
3. 实现内容关系图和孤儿报告；
4. 加入 10 个状态、10 个物品、5 个配方作为样本；
5. 建保存/加载/死亡/换维度 GameTest。

### Sprint B：魔药—扮演垂直切片

1. 占卜家序列 9 完整数据；
2. 鉴材、研磨、坩埚、封存、服用五阶段；
3. 扮演四段模型、反刷和反思日志；
4. 精神烙印低语；
5. 首两小时教程和三个失败恢复路径。

### Sprint C：调查—城市反馈

1. 调查板节点与假说；
2. 一个警局、报社、事务所街区；
3. 动态案件生成器最小模板；
4. 神秘暴露度和报纸掩盖；
5. 4 人 8 小时测试与存档回滚演练。

# 79. 测试、平衡、正典与发布验收

## 79.1 测试金字塔

| 层 | 工具 | 覆盖 |
|---|---|---|
| 单元测试 | JUnit | Codec、数值、关系图、迁移、概率表 |
| GameTest | Minecraft GameTest | 方块/实体/配方/能力/仪式/掉落 |
| 集成测试 | Dedicated Server + 测试客户端 | 网络、登录、维度、兼容、多人同意 |
| 存档回归 | 固定 v0.8/v0.9 世界样本 | 迁移、关闭数据包、恢复、崩溃回滚 |
| 内容审计 | Python/Gradle | ID、lang、资产、来源、孤儿、剧透 |
| 体验测试 | 1/4/20 人 | 教程、信息负荷、平衡、社交与性能 |

## 79.2 原著一致性验收

每个正典条目在 `docs/canon_audit.csv` 记录：条目 ID、声称内容、来源等级、资料链接/章节索引、审阅人、审阅日期、争议、改编说明。验收不要求把小说文本写进仓库；只记录足以复核的摘要。

**禁止项**：

- 将社区猜测标成正典；
- 把游戏平衡数字说成小说设定；
- 复制大段小说、维基文案或角色对白；
- 用原著角色替玩家完成关键主线；
- 将序列/途径名称和能力随意混组却不标改编；
- 忽略第二部剧透而默认对所有玩家展示。

## 79.3 平衡指标

| 指标 | 目标 |
|---|---|
| 序列 9 首次获得 | 新玩家 1.5–4 小时，中位约 2.5 小时 |
| 序列 9 消化 | 2–4 小时有效玩法，不计算纯挂机 |
| 序列 8–7 | 每级 5–12 小时，至少跨 2 类玩法 |
| 序列 6–5 | 每级 15–40 小时，含组织/仪式/材料闭环 |
| 序列 4 | 40–100 小时终局项目，不强制 PvP |
| 单途径战斗优势 | 同序列标准场景胜率不长期超过 60%，必须存在场景性短板 |
| 非战斗价值 | 每条途径至少 30% 能力/事件用于调查、生活、制造或组织 |
| 稀有材料来源 | 至少 2 条，其中至少 1 条非 PvP |
| 负面状态恢复 | 80% 可在 30 分钟内看到明确改善；长期状态有阶段目标 |
| 教程流失点 | 服药前、服药后、第一次低语三个节点分别记录并优化 |

## 79.4 多人同意测试

附身、催眠、契约、审讯、强制交易、梦境连接、仪式献祭、领地破坏、身份揭露均进入 `consent_sensitive` 测试集。测试包括：拒绝、超时、断线、跨维度、目标死亡、领地权限变化、队伍解散、恶意重复请求。默认策略宁可失败，也不能静默绕过同意。

## 79.5 P0/P1 发布阻断

- P0：复制唯一性/特性、存档损坏、远程越权、客户端任意目标、永久任务锁死、关闭兼容 Mod 后崩档。
- P1：教程无法完成、主要途径能力失效、世界事件无法结束、严重性能回退、未解锁知识泄露、无障碍模式丢失关键警告。
- P2：单条文案/纹理/音效错误、次要数值偏差。

发布候选必须 0 个 P0、0 个已知高概率 P1；P2 可进入明确公开清单。

# 80. 研究来源索引与使用边界

## 80.1 原著专题资料（A 级索引）

以下网页用于结构化核对，不代表替代小说正文，也不授权复制原文：

- Lord of Mysteries Wiki — Pathways: `https://lordofthemysteries.fandom.com/wiki/Pathways`
- Beyonder Characteristics: `https://lordofthemysteries.fandom.com/wiki/Beyonder_Characteristics`
- Acting Method: `https://lordofthemysteries.fandom.com/wiki/Acting_Method`
- Mythical Creature Form: `https://lordofthemysteries.fandom.com/wiki/Mythical_Creature_Form`
- Sequence: `https://lordofthemysteries.fandom.com/wiki/Sequence`
- Potion System: `https://lordofthemysteries.fandom.com/wiki/Potion_System`
- Anchor: `https://lordofthemysteries.fandom.com/wiki/Anchor`
- History of the World / Timeline / Locations / Events / Organizations / Creatures and Plants / Currency / Prices and Wages 等专题分类。

本次研究确认该 Wiki 同时覆盖第一部、第二部、标准途径、非标准途径、魔药、恩赐、扮演、锚、特性、唯一性、封印物、历史、组织、地点、生物植物与神秘学等大类。由于 Wiki 会继续更新且含重大剧透，仓库只记录审计日期和摘要，不在运行时依赖网页。

## 80.2 Minecraft 工程资料（B-tech）

- Forge 1.20.x Documentation — Registries、Capabilities、Networking、Recipes、Tags、Loot、Menus、Key Mappings、Data Generation：`https://docs.minecraftforge.net/en/1.20.x/`
- GeckoLib 4 Wiki / official repository：`https://github.com/bernie-g/geckolib/wiki`
- Curios official repository：`https://github.com/TheIllusiveC4/Curios`
- 各兼容 Mod 的官方 Wiki、API 文档、源码和 Issue；论坛帖子只用于发现问题，最终实现以当前版本 API 和本地测试为准。

## 80.3 社区与论坛（C 级）

参考范围包括 Lord of Mysteries Wiki 讨论区、相关 Reddit/贴吧/论坛中的途径玩法、神话形态表现和世界观争议，以及 Forge Forums、GitHub Issues、Modding 社区中的数据驱动、资源重载、登录包大小、兼容与性能踩坑。社区内容只形成需求或测试案例，不直接成为正典结论。

## 80.4 “所有网站”的现实边界

互联网资料持续变化、存在登录墙、地区限制、重复转载、错误翻译与未标剧透，因此不存在可证明“搜索了所有相关网站”的完成状态。v0.9 采用的是：以原著专题维基和官方技术文档为主，交叉搜索主要社区与论坛，并建立可持续审计表。后续每次版本都应增量更新 `docs/research_sources.csv`，而不是宣称一次性穷尽互联网。

## 80.5 版权与原创化

- 不复制小说正文、维基段落、角色对白或大段配方描述；
- 正典事实用项目自己的结构化摘要；
- 原创任务、日记、报纸、低语和封印物文案继续全部自写；
- `docs/ip_mapping.csv` 必须覆盖显示名、组织、地名、尊名、图标和资源包符号；
- 对外发布前完成授权评估、平台规则检查和素材许可证审计。

# 81. v0.9 总账、保留校验与下一步

## 81.1 设计总账（不要与已实现量混淆）

| 类别 | v0.8 记录 | v0.9 文档目标/新增 | v0.9 生成时实际状态 |
|---|---:|---:|---|
| 标准途径 | 22（15 可玩设计） | 22 条全部达到序列 9–5 详设；高序列框架重构 | 文档规格完成，代码实现沿用 v0.8 状态 |
| 序列设计节点 | 约 90 | 110 个 9–5 节点 + 序列 4/3 通用规则 | 7 条途径新增详设 |
| 能力 | 150+ | 280+ 设计目标 | 新增七途径能力规格 140 项级别，未宣称已编码 |
| 物品 | 180+ | 284+ 明确注册设计，另含生态产物 | 本章新增 104 个条目 |
| 配方 | 60+ 工作台 + 75 魔药配方 | 180+ 去重后的玩家可见多工位目标 | 本章新增 80 条代表规格 |
| 封印物 | 72 | 84 | 新增 12 件原创封印物设计 |
| 生物 | 18 新增（v0.8） | 58+ 设计生态条目 | 新增/重构 40 个生物条目 |
| 神秘植物 | 80+ 材料中部分 | 36 种明确植物/作物 | 新增 36 个植物条目 |
| 粒子 | 34 | 72 | 新增 38 种规格 |
| 状态 | 分散 | 64 个统一状态 | 新增/重构 64 个状态 |
| 异常天气 | 分散事件 | 18 种 | 18 种完整规格 |
| 结构类别 | 14 | 46+ | 新增 32 类结构 |
| 世界事件 | 25 | 48 | 新增 23 个事件 |
| 任务链 | 8 | 20 | 新增 12 条任务链 |
| 周期活动 | 少量运营 | 18 | 新增 18 个长期活动 |
| 兼容 | 6 项概述 | 17 类适配目标 | 兼容契约完成，代码待排期 |
| 文档章节 | 56 | 81 | v0.8 全量 + v0.9 25 章增量 |

## 81.2 v0.8 字节级保留校验

生成 v0.9 时执行以下校验：

```text
baseline_source = /mnt/data/Project_Mystery_Design_Doc_v0_8.md
baseline_sha256 = 22a281c3731f256cb473682936f885af8c3e3168fa66114d759ce32b6b30b073
baseline_bytes  = 315305
baseline_lines  = 5060
embedded_match  = true
```

`embedded_match=true` 才允许交付。此校验只证明 v0.8 基线被完整嵌入，不代表 v0.8 中每段旧代码都已在当前 Forge 环境重新编译；编译正确性仍由 M0/M1 的测试处理。

## 81.3 v0.9 的关键优化结论

1. 从“数量总账”转为“设计—数据—代码—资产—可玩—验证”状态账。
2. 从单层特性转为包含前序层、额外负担和精神烙印的特性束。
3. 从行为积分转为准则理解、情境实践、结果验证和身份区分。
4. 从神话形态皮肤转为权柄、知识灼伤、人性与锚的高序列系统。
5. 从组织商店转为行动网络、辖区、掩盖、招募和政治。
6. 从生物掉落表转为受天气、结构、食物网和玩家行为影响的生态。
7. 从孤立物品/配方转为可由 CI 校验的内容关系图。
8. 从“兼容就加配方”转为软依赖适配器与自动化红线。
9. 从大而全愿望清单转为 18–24 个月、带依赖和退出标准的路线图。
10. 从一次性查 Wiki 转为可持续的正典审计、剧透等级和来源登记。

## 81.4 立即执行顺序

1. **先建 M0**：Schema v4、关系图、迁移、CI；不先批量画纹理。
2. 将 v0.9 新增表拆为 `docs/master/*.csv`，生成器输出 JSON/lang/Wiki。
3. 选择占卜家序列 9 + 罗盘鸟 + 工业浓雾 + 调查板作为统一垂直切片。
4. 把 v0.8 中“完整代码”逐个放入真实工程编译，建立 `code_audit.csv`，修正文档与代码差异。
5. 完成 2 小时新手流程后，再扩首发五途径；未通过验收前不同时开做 22 条途径。
6. Curios、JEI/EMI、Jade 先做只读适配；Create/IE/AE2 等自动化放到核心闭环稳定之后。
7. 序列 3–1、唯一性、跨服灰雾和非标准途径继续保持功能开关关闭，直到常规序列 4 内容成熟。

> v0.9 的目标不是让文档看起来更大，而是让每一件新增内容都知道自己从哪里来、被谁使用、产生什么风险、如何反制、何时制作、怎样验收。v0.8 的全部构想仍然保留；v0.9 为它补上原著机制约束、内容关系、工程边界和现实开发顺序。

