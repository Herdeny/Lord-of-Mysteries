/*
 * Lord of Mysteries Wiki — 数据源
 * 与项目 data/*.json 与设计文档 v0.4 同源整理。
 * 纯静态数据，GitHub Pages 直接加载，无需构建。
 */

window.LOM = window.LOM || {};
var projectMeta = window.LOM_PROJECT_META || {};

/* ── 项目元信息 ── */
LOM.meta = {
  modId: "lord_of_mysteries",
  name: "Lord of Mysteries",
  cnName: "诡秘之主",
  version: projectMeta.version || "unknown",
  mc: projectMeta.mc || "Minecraft Java 1.20.1",
  loader: projectMeta.loader || "Forge 47.4.20",
  java: projectMeta.java || "17",
  stage: (projectMeta.stage || "开发中") + " · " + (projectMeta.milestone || ""),
  lastUpdated: projectMeta.lastUpdated,
  lastUpdatedUtc: projectMeta.lastUpdatedUtc,
  lastUpdatedDisplay: projectMeta.lastUpdatedDisplay || "unknown",
  repo: "https://github.com/Herdeny/Lord-of-Mysteries",
  pages: "https://herdeny.github.io/Lord-of-Mysteries/",
  authors: [
    { role: "发起 / 共享", name: "Herdeny（星魂）", link: "https://github.com/Herdeny" },
    { role: "协同开发", name: "Zijian-Ni（小倪）", link: "https://github.com/Zijian-Ni" }
  ]
};

/* ── 里程碑路线图 ── */
LOM.roadmap = [
  { id: "M0", title: "技术验证（框架）", state: "done",
    points: ["Forge 1.20.1 完整工程 + Gradle wrapper", "玩家 Capability 数据载荷 + NBT 持久化 + 死亡跨维度继承",
      "灵性自然恢复 / 污染分级检定骨架", "13 个模块包 + 数据驱动 JSON 示例", "中英双语本地化", "compileJava test → BUILD SUCCESSFUL"] },
  { id: "M1", title: "占卜家序列9功能闭环", state: "done",
    points: ["三能力、占卜可信度与服务端访问控制", "扮演事件、消化公式与反刷取衰减",
      "可交互坩埚、热源温控、四档品质与服药晋升", "污染分级、三种失控模式与专属失控体",
      "永燃火柴盒 + 净化封印仪式", "N 键档案 + 废弃调查员营地基础"] },
  { id: "M2", title: "三途径序列9-8", state: "active",
    points: ["观众与猎人序列 9-8 已实装", "占卜家序列 8 待开发",
      "通用仪式 + 多人一致性测试", "灰雾空间基础版"] },
  { id: "M3", title: "序列7与世界扩展", state: "planned",
    points: ["三途径序列 7 + 偷盗者 / 学徒 9-7", "阶段 Boss + 任务链", "首发结构 + 世界事件"] },
  { id: "M4", title: "MVP 1.0", state: "planned",
    points: ["平衡 / 性能 / 兼容 / 存档迁移", "完整本地化"] },
  { id: "M5", title: "深化扩展", state: "planned",
    points: ["序列 6-5、途径扩至 10 条", "组织深化 + 塔罗会完整功能"] }
];

/* ── 三途径概览 ── */
LOM.pathwaysOverview = [
  { id: "seer", name: "占卜家", en: "Seer", accent: "#7c5cff",
    desc: "以观察、占卜与灵视见长。信息获取能力极强，能预知危险、洞悉真伪，但正面战斗偏弱。M1 首个可玩途径。",
    traits: ["灵视", "危险直觉", "占卜", "洞察"], baseSpirit: 100, growth: 22, status: "M1 实装" },
  { id: "hunter", name: "猎人", en: "Hunter", accent: "#cc5b4d",
    desc: "力量与战斗的途径。序列 9 擅长追踪、陷阱与荒野生存，序列 8 以挑衅、激怒和战斗意志控制战局。",
    traits: ["追踪", "陷阱", "挑衅", "战斗意志"],
    spirit: "序列9 118 · 序列8 142", status: "M2 序列9-8实装" },
  { id: "spectator", name: "观众", en: "Spectator", accent: "#6bcad0",
    desc: "精神与心灵的途径。序列 9 读取情绪并预判行为，序列 8 可读取表层思维并施加可抵抗的心理暗示。",
    traits: ["情绪读取", "行为预判", "表层读心", "心理暗示"],
    spirit: "序列9 112 · 序列8 138", status: "M2 序列9-8实装" },
  { id: "apprentice", name: "学徒", en: "Apprentice", accent: "#d4af37",
    desc: "知识与元素的途径。掌控元素、空间与造物，博学而多能。M3 规划。",
    traits: ["元素", "造物", "空间", "博学"], baseSpirit: 100, growth: 24, status: "M3 规划" },
  { id: "thief", name: "偷盗者", en: "Thief", accent: "#74b86f",
    desc: "敏捷与幸运的途径。身法、潜行、概率操纵，机敏而难以捉摸。M3 规划。",
    traits: ["敏捷", "潜行", "幸运", "概率"], baseSpirit: 100, growth: 20, status: "M3 规划" }
];

/* ── 占卜家序列阶梯（9→0，M1 聚焦序列9） ── */
LOM.seerSequences = [
  { seq: 9, name: "占卜家 Seer", state: "active", spiritMax: 122,
    abilities: ["灵视", "危险直觉", "简易占卜"],
    desc: "序列起点。初窥灵界，能感知超凡气息、预警致命危险、进行简易占卜。M1 完整实装。" },
  { seq: 8, name: "小丑 Clown", state: "planned", spiritMax: 144,
    abilities: ["敏捷强化", "戏法", "误导"], desc: "身手敏捷，擅长戏法与误导。M2 规划。" },
  { seq: 7, name: "魔术师 Magician", state: "planned", spiritMax: 168,
    abilities: ["造物幻象", "催眠暗示", "道具戏法"], desc: "以幻象与暗示操纵感知。M3 规划。" },
  { seq: 6, name: "无面人 Faceless", state: "planned", spiritMax: 200,
    abilities: ["易容", "拟态", "身份夺取"], desc: "可完美易容与拟态。M5 规划。" },
  { seq: 5, name: "秘偶大师 Marionettist", state: "planned", spiritMax: 240,
    abilities: ["秘偶操控", "傀儡", "远程操纵"], desc: "操控秘偶与傀儡。M5 规划。" },
  { seq: 4, name: "预言家 Scryer", state: "future", spiritMax: 300,
    abilities: ["强化预言", "命运窥探"], desc: "更高维度的预知。规划中。" },
  { seq: 3, name: "占卜之王 (待定)", state: "future", spiritMax: 380, abilities: ["—"], desc: "高序列，暂未设计。" },
  { seq: 2, name: "(待定)", state: "future", spiritMax: 480, abilities: ["—"], desc: "高序列，暂未设计。" },
  { seq: 1, name: "(待定)", state: "future", spiritMax: 620, abilities: ["—"], desc: "高序列，暂未设计。" },
  { seq: 0, name: "旧日 / 途径之主", state: "future", spiritMax: 999, abilities: ["神性"], desc: "途径顶点，遥远愿景。" }
];

LOM.spectatorSequences = [
  { pathway: "观众", seq: 9, name: "观众 Spectator", state: "active", spiritMax: 112,
    abilities: ["情绪读取", "行为预判", "镇定"],
    desc: "持续观察目标情绪，并在战斗中预判来袭行为。M2 已实装。" },
  { pathway: "观众", seq: 8, name: "读心者 Telepathist", state: "active", spiritMax: 138,
    abilities: ["表层读心", "心理暗示", "精神抵抗"],
    desc: "读取目标当前表层状态，并施加带可见反馈和玩家抵抗窗口的心理暗示。M2 已实装。" }
];

LOM.hunterSequences = [
  { pathway: "猎人", seq: 9, name: "猎人 Hunter", state: "active", spiritMax: 118,
    abilities: ["战斗追踪", "陷阱精通", "荒野感知"],
    desc: "攻击后追踪目标，布置专属捕兽夹，并在户外感知附近敌对目标。M2 已实装。" },
  { pathway: "猎人", seq: 8, name: "挑衅者 Provoker", state: "active", spiritMax: 142,
    abilities: ["挑衅", "激怒", "战斗意志"],
    desc: "吸引低抗性敌人仇恨、激怒单个目标，并在被围攻时获得减伤。M2 已实装。" }
];

/* ── 详细条目（卡片 + 详情弹窗） ── */
LOM.entries = [
  /* 能力 ability */
  { type: "ability", id: "lord_of_mysteries:spirit_vision", name: "灵视", en: "Spirit Vision",
    summary: "持续被动，可开关。开启后扫描 32 格内实体，并以定向彩色粒子标记阵营与威胁。",
    tags: ["能力", "序列9", "被动", "M1"],
    details: [["类型", "持续被动（可开关）"], ["灵性消耗", "0.8 / 秒"], ["范围", "32 格"],
      ["耗尽", "灵性归零自动关闭"], ["注册", "ability/SpiritVision"]],
    long: "灵视是占卜家最标志性的能力。当前 Alpha 由服务端每 10 tick 扫描一次，并只向灵视持有者发送彩色粒子：<b style='color:#74b86f'>绿</b>=友好、<b style='color:#e2c85a'>黄</b>=中立、<b style='color:#cc5b4d'>红</b>=敌意、<b style='color:#8b5cf6'>紫</b>=污染、<b style='color:#f1eee7'>白</b>=高危、<b style='color:#9a9a9a'>灰</b>=灵体。实体描边着色器仍在后续计划中。" },
  { type: "ability", id: "lord_of_mysteries:danger_intuition", name: "危险直觉", en: "Danger Intuition",
    summary: "触发被动。受到致命攻击时有 35% 概率避开该次伤害并获得短暂移动窗口。",
    tags: ["能力", "序列9", "被动", "M1"],
    details: [["类型", "触发被动"], ["冷却", "30 秒"], ["触发概率", "35%"], ["效果", "取消该次致命伤害 + 0.8 秒加速"]],
    long: "Forge 1.20.1 没有通用的攻击预告事件，因此 Alpha 采用等价实现：致命伤害结算前有 35% 概率取消该次攻击，同时播放预警音效并提供 0.8 秒移动加速。冷却 30 秒。" },
  { type: "ability", id: "lord_of_mysteries:simple_divination", name: "简易占卜", en: "Simple Divination",
    summary: "主动能力。消耗 15 灵性询问方向 / 危险 / 状态，结果按可信度扭曲呈现。",
    tags: ["能力", "序列9", "主动", "占卜", "M1"],
    details: [["类型", "主动"], ["灵性消耗", "15"], ["冷却", "60 秒"], ["结果", "按可信度分清晰 / 模糊 / 错误"]],
    long: "占卜家可对方向、危险源、目标状态进行简易占卜。服务端计算真实结果，再依据可信度得分把结果「扭曲」后返回：清晰=准确，模糊=方向偏移 / 用语含糊，错误=误导。对同一目标反复占卜会累积「过度占卜」惩罚，降低可信度并影响消化。" },
  { type: "ability", id: "lord_of_mysteries:emotion_read", name: "情绪读取", en: "Emotion Read",
    summary: "观众序列 9 主动开关能力。持续读取视线目标情绪，每秒消耗 0.5 灵性。",
    tags: ["能力", "观众", "序列9", "M2"],
    details: [["按键", "G"], ["类型", "持续主动"], ["灵性消耗", "0.5 / 秒"], ["范围", "16 格"], ["权威端", "服务端"]],
    long: "开启后由服务端对视线目标进行射线判定，并在快捷栏显示平静、好奇、愤怒、恐惧或敌意等状态。能力不会读取玩家聊天、背包或其他隐私数据；灵性耗尽时自动关闭。" },
  { type: "ability", id: "lord_of_mysteries:behavior_prediction", name: "行为预判", en: "Behavior Prediction",
    summary: "观众序列 9 被动能力。每 8 秒可预判一次来袭攻击，使该次伤害降低 40%。",
    tags: ["能力", "观众", "序列9", "被动", "M2"],
    details: [["类型", "触发被动"], ["冷却", "8 秒"], ["效果", "该次伤害 ×0.6"], ["扮演", "连续预判 5 次触发事件"]],
    long: "当玩家受到生物攻击且冷却就绪时，服务端降低本次伤害并给出音效与快捷栏反馈。连续成功预判会推进观众的扮演消化事件。" },
  { type: "ability", id: "lord_of_mysteries:surface_read", name: "表层读心", en: "Surface Thought Reading",
    summary: "读心者序列 8 主动能力。读取视线目标当前表层状态，不读取聊天或背包。",
    tags: ["能力", "观众", "序列8", "M2"],
    details: [["按键", "H"], ["灵性消耗", "18"], ["冷却", "30 秒"], ["范围", "16 格"], ["权威端", "服务端"]],
    long: "能力仅根据目标当下的战斗、生命与实体类型状态生成平静、好奇、愤怒、恐惧或敌意反馈。所有目标选择和消耗均由服务端校验。" },
  { type: "ability", id: "lord_of_mysteries:mental_suggestion", name: "心理暗示", en: "Mental Suggestion",
    summary: "读心者序列 8 主动能力。对低抗性目标施加缓慢与虚弱，并提供明确可见的抵抗反馈。",
    tags: ["能力", "观众", "序列8", "精神", "M2"],
    details: [["按键", "J"], ["灵性消耗", "25"], ["冷却", "40 秒"], ["范围", "12 格"],
      ["持续", "普通目标 8 秒；观众 4 秒；玩家潜行 3 秒"], ["PvP", "受服务器配置控制"]],
    long: "心理暗示会播放施法音效与粒子，玩家目标会收到施法者和持续时间提示。潜行可显著缩短效果，观众途径拥有额外精神抵抗；高生命非玩家目标会直接抵抗。服务器可完全关闭玩家间精神能力。" },
  { type: "ability", id: "lord_of_mysteries:tracking", name: "战斗追踪", en: "Combat Tracking",
    summary: "猎人序列 9 被动能力。攻击目标后保留 45 秒追踪标记，并显示距离与剩余时间。",
    tags: ["能力", "猎人", "序列9", "被动", "M2"],
    details: [["类型", "触发被动"], ["持续", "45 秒"], ["扮演", "追踪超过 30 秒后击杀"], ["权威端", "服务端"]],
    long: "猎人命中目标后由服务端记录目标 UUID，并只向猎人发送追踪粒子与快捷栏距离反馈。目标为自然生成、敌对生物或已参与战斗的实体时，持续追踪超过 30 秒再完成击杀可推进扮演。" },
  { type: "ability", id: "lord_of_mysteries:trap_mastery", name: "陷阱精通", en: "Trap Mastery",
    summary: "猎人序列 9 可制作和布置专属捕兽夹，命中后施加缓慢与虚弱。",
    tags: ["能力", "猎人", "序列9", "陷阱", "M2"],
    details: [["载体", "猎人捕兽夹"], ["效果", "缓慢 IV + 虚弱"], ["持续", "5 秒"], ["消耗", "触发后破坏"]],
    long: "捕兽夹在放置时绑定所有者，不会伤害放置者。有效猎物踩中后由服务端施加控制效果并消耗方块；连续设置有效陷阱可推进猎人扮演。" },
  { type: "ability", id: "lord_of_mysteries:wilderness_sense", name: "荒野感知", en: "Wilderness Sense",
    summary: "猎人序列 9 户外被动，每 2 秒提示 24 格内最近的敌对或战斗目标。",
    tags: ["能力", "猎人", "序列9", "被动", "M2"],
    details: [["类型", "户外被动"], ["范围", "24 格"], ["检查", "每 2 秒"], ["目标", "敌对或战斗记录目标"]],
    long: "只有玩家所在位置可见天空时生效，并以仅猎人可见的粒子提示最近威胁。序列 9 猎人在户外的基础灵性恢复速度额外提高 10%。" },
  { type: "ability", id: "lord_of_mysteries:provoke", name: "挑衅", en: "Provoke",
    summary: "挑衅者序列 8 主动能力，使附近低抗性敌人在 8 秒内优先攻击施术者。",
    tags: ["能力", "猎人", "序列8", "主动", "M2"],
    details: [["按键", "K"], ["灵性消耗", "15"], ["冷却", "20 秒"], ["范围", "10 格"], ["持续", "8 秒"]],
    long: "服务端筛选 10 格内生命上限不超过 80 的敌对或已进入战斗的生物，强制其优先锁定施术者。一次影响至少 3 个目标可推进挑衅者扮演。" },
  { type: "ability", id: "lord_of_mysteries:enrage", name: "激怒", en: "Enrage",
    summary: "挑衅者序列 8 主动能力，使目标攻击提高 30%、护甲降低 25%，持续 8 秒。",
    tags: ["能力", "猎人", "序列8", "主动", "M2"],
    details: [["按键", "L"], ["灵性消耗", "20"], ["冷却", "30 秒"], ["范围", "12 格"], ["持续", "8 秒"]],
    long: "服务端射线选择目标并应用临时属性修正。玩家目标仍受原版 PvP 伤害权限约束；效果结束后属性修正自动清理。" },
  { type: "ability", id: "lord_of_mysteries:battle_will", name: "战斗意志", en: "Battle Will",
    summary: "挑衅者序列 8 被动能力，被至少 3 名敌人围攻时获得 15% 减伤。",
    tags: ["能力", "猎人", "序列8", "被动", "M2"],
    details: [["触发", "至少 3 名敌人锁定"], ["减伤", "15%"], ["持续", "10 秒"], ["冷却", "45 秒"]],
    long: "服务端周期性统计 16 格内锁定玩家的生物。条件满足且冷却就绪时触发，持续时间内对生物造成的来袭伤害统一乘以 0.85。" },

  /* 系统机制 system */
  { type: "system", id: "credibility", name: "占卜可信度", en: "Divination Credibility",
    summary: "决定占卜结果准确度的核心算法。finalScore = baseClear − interference + 随机扰动。",
    tags: ["机制", "占卜", "算法", "M1"],
    details: [["公式", "baseClear − interference + Gaussian(0, 0.1)"], ["baseClear", "由序列与剩余灵性%决定"],
      ["interference", "附近高序列实体 / 污染 / 屏蔽增加"], ["结果分级", "清晰 / 模糊 / 错误"]],
    long: "可信度是占卜家所有信息类能力的底层。得分越高结果越准。<br><b>baseClear</b> 随序列提升与灵性充裕而升高；<b>interference</b> 会被附近的高序列存在、污染区域、被屏蔽目标推高；再叠加一个高斯随机扰动模拟灵界的不确定性。分级后服务端算真值、客户端按等级扭曲——纯逻辑抽为静态方法便于单元测试。" },
  { type: "system", id: "acting", name: "扮演事件系统", en: "Acting Events",
    summary: "消化魔药的唯一手段。契合序列身份的行为累积消化度，直至满 100% 可晋升。",
    tags: ["机制", "消化", "核心", "M1"],
    details: [["收益公式", "基础 × 质量(0.7-1.2) × 新颖度 × 风险 × 魔药品质"],
      ["新颖度", "elapsed / decayTicks，序列9 decayTicks=1200"], ["风险系数", "1.0 + 失控压力/200"]],
    long: "「扮演」是消化魔药的核心。当前已自动接入清晰占卜、灵性不足时克制占卜，以及短时间重复占卜的 −5 惩罚；正确解读模糊结果和帮助队友脱险的数据定义已保留，待任务/事后验证系统接线。短时间重复同类行为会被新颖度系数压低到最低 0.1。" },
  { type: "system", id: "digestion", name: "消化与晋升", en: "Digestion & Advancement",
    summary: "消化度 0→100%。满额且完成扮演考验后可服用高一阶魔药晋升序列。",
    tags: ["机制", "消化", "晋升", "M1"],
    details: [["范围", "0 - 100%"], ["提升", "靠扮演事件累积"], ["满额", "可晋升下一序列"], ["显示", "非调试默认隐藏精确值"]],
    long: "服用魔药后力量并不会立刻属于你——必须通过持续「扮演」逐步消化。消化度满 100% 意味着完全掌控当前序列，才能安全地晋升。消化不足强行晋升 = 极高失控风险。默认配置隐藏精确数值（show_exact_digestion=false），只给模糊反馈，还原原作的未知感。" },
  { type: "system", id: "breakdown", name: "失控机制", en: "Loss of Control",
    summary: "灵性 / 污染 / 失控压力失衡将导致失控。默认 recoverable 模式：倒地并生成失控体。",
    tags: ["机制", "失控", "危险", "M1"],
    details: [["触发", "污染达到 100"], ["recoverable", "倒地30s + 生成专属失控体 + 24h精神创伤"], ["config", "recoverable / permanent / death"]],
    long: "失控是踏足非凡的代价。污染达到 100 时，默认 recoverable 模式让玩家倒地 30 秒、生成独立注册的占卜家失控体，并施加 24 小时精神创伤（灵性恢复减半）。失控体拥有 30 生命、近战追踪、周期性黑暗/虚弱干扰、紫色灵界粒子和破碎特性掉落；当前渲染暂复用僵尸模型。" },
  { type: "system", id: "spirituality", name: "灵性", en: "Spirituality",
    summary: "能力消耗与自然恢复的核心资源。非战斗 + 光照≥7 + 低压时缓慢恢复。",
    tags: ["资源", "灵性", "M1"],
    details: [["序列9上限", "122"], ["恢复条件", "非战斗 + 光照≥7 + 失控压力<30"], ["序列9恢复", "0.05 / 秒（1.0 / 20s）"]],
    long: "灵性是施展一切超凡能力的燃料。序列9 上限 122（基础 100 + 序列成长 22）。满足条件时缓慢自然恢复；战斗中、黑暗处或高压状态下停止恢复。合理管理灵性、必要时「放弃使用能力」本身也是一种能提升消化的扮演。" },
  { type: "status", id: "lord_of_mysteries:pollution", name: "污染", en: "Pollution",
    summary: "核心风险值。25 / 50 / 75 / 100 进入更高检定区间，满值立即失控。",
    tags: ["状态", "风险", "失控", "M1"],
    details: [["0-24", "稳定，无检定"], ["25-49", "轻度异常，每 5 分钟检定"], ["50-74", "危险，每 2 分钟检定"], ["75-99", "临界，每 30 秒检定"], ["100", "立即触发失控"]],
    long: "污染代表灵性层面被侵蚀的程度。滥用能力、饮用瑕疵 / 污染魔药、接触封印物都会累积污染。分级越高，负面检定越频繁——从偶发低语、幻象，到周期性负面状态，直至满值失控。" },
  { type: "status", id: "insanity_pressure", name: "失控压力", en: "Insanity Pressure",
    summary: "短期精神压力值。抑制灵性恢复，同时提升扮演的风险系数收益。",
    tags: ["状态", "精神", "M1"],
    details: [["范围", "0 - 100"], ["≥30", "停止灵性自然恢复"], ["扮演风险系数", "1.0 + 压力/200"]],
    long: "失控压力是短期的精神紧绷程度。它是把双刃剑：压力 ≥30 会切断灵性自然恢复，但高压下完成扮演会获得更高风险系数加成（1.0 + 压力/200）。在危险边缘行走，正是非凡者消化力量的方式之一。" },

  /* 魔药 potion */
  { type: "potion", id: "lord_of_mysteries:seer_potion_9", name: "占卜家魔药 · 序列9", en: "Seer Potion Seq.9",
    summary: "踏入占卜家途径的起点魔药。以灵性草药、占卜水晶为主，月华水提升品质。",
    tags: ["魔药", "序列9", "坩埚", "M1"],
    details: [["途径", "占卜家"], ["温度", "60 - 80 ℃"], ["制作时间", "1200 tick"], ["失败产物", "污染混合物"], ["核心材料", "灵性草药 · 占卜水晶"], ["品质材料", "月华水（+0.2）"]],
    long: "依次投入灵性草药、占卜水晶与可选月华水，空手点击启动 1200 tick 炼制。营火、灵魂营火、火与岩浆提供不同温度；系统按全程平均温度和顺序结算完美（×1.2）、完整（×1.0）、瑕疵（×0.7）或污染失败。成品保存品质并在服用后影响扮演消化。" },
  { type: "potion", id: "lord_of_mysteries:spectator_potion_9", name: "观众魔药 · 序列9", en: "Spectator Potion Seq.9",
    summary: "踏入观众途径的起点魔药，服用后获得 112 灵性上限、情绪读取与行为预判。",
    tags: ["魔药", "观众", "序列9", "M2"],
    details: [["途径", "观众"], ["温度", "60 - 80 ℃"], ["制作时间", "1200 tick"],
      ["核心材料", "灵性草药 · 发酵蛛眼"], ["品质材料", "蜂蜜瓶（+0.2）"], ["晋升要求", "普通人"]],
    long: "依次投入灵性草药、发酵蛛眼与可选蜂蜜瓶炼制。材料顺序、全程平均温度和可选品质材料共同决定成品品质；错误混入其他魔药材料会生成污染混合物。" },
  { type: "potion", id: "lord_of_mysteries:spectator_potion_8", name: "读心者魔药 · 序列8", en: "Telepathist Potion Seq.8",
    summary: "观众途径的第二阶段魔药，只有完全消化序列 9 后才能安全服用。",
    tags: ["魔药", "观众", "序列8", "M2"],
    details: [["途径", "观众"], ["温度", "60 - 80 ℃"], ["制作时间", "1200 tick"],
      ["核心材料", "灵性草药 · 书"], ["品质材料", "紫水晶碎片（+0.2）"],
      ["晋升要求", "观众序列9 · 消化度100%"]],
    long: "依次投入灵性草药、书与可选紫水晶碎片炼制。服用校验由服务端执行：必须处于观众序列 9 且消化度达到 100%，晋升后灵性上限提升至 138，并解锁表层读心与心理暗示。" },
  { type: "potion", id: "lord_of_mysteries:hunter_potion_9", name: "猎人魔药 · 序列9", en: "Hunter Potion Seq.9",
    summary: "踏入猎人途径的起点魔药，服用后获得 118 灵性上限、追踪、陷阱和荒野感知。",
    tags: ["魔药", "猎人", "序列9", "M2"],
    details: [["途径", "猎人"], ["温度", "60 - 80 ℃"], ["制作时间", "1200 tick"],
      ["核心材料", "灵性草药 · 骨头"], ["品质材料", "兔子脚（+0.2）"], ["晋升要求", "普通人"]],
    long: "依次投入灵性草药、骨头与可选兔子脚炼制。序列 9 猎人在户外基础灵性恢复提高 10%，并解锁战斗追踪、猎人捕兽夹与荒野感知。" },
  { type: "potion", id: "lord_of_mysteries:hunter_potion_8", name: "挑衅者魔药 · 序列8", en: "Provoker Potion Seq.8",
    summary: "猎人途径的第二阶段魔药，只有完全消化序列 9 后才能安全服用。",
    tags: ["魔药", "猎人", "序列8", "M2"],
    details: [["途径", "猎人"], ["温度", "60 - 80 ℃"], ["制作时间", "1200 tick"],
      ["核心材料", "灵性草药 · 火药"], ["品质材料", "红石（+0.2）"],
      ["晋升要求", "猎人序列9 · 消化度100%"]],
    long: "依次投入灵性草药、火药与可选红石炼制。服务端要求玩家处于猎人序列 9 且消化度达到 100%；晋升后灵性上限提升至 142、失控压力增加 12，并解锁挑衅、激怒与战斗意志。" },

  /* 坩埚 / 方块 */
  { type: "block", id: "lord_of_mysteries:crucible", name: "坩埚", en: "Crucible",
    summary: "魔药制作的核心方块，绑定 CrucibleBlockEntity，掌管温度、投料顺序与熬煮进度。",
    tags: ["方块", "魔药", "方块实体", "M1"],
    details: [["硬度", "3.5"], ["爆炸抗性", "6.0"], ["温控", "维持 60-80℃"], ["熬煮", "1200 tick"], ["品质检定", "顺序 + 温差"]],
    long: "右键依次投入材料，空手右键启动或查询进度，完成后空手取出产物。CrucibleBlockEntity 持久化材料、进度、当前温度和累计温度；破坏方块会安全掉落内容物。灵魂营火与普通营火最适合稳定炼制，火和岩浆更容易导致过热。" },
  { type: "block", id: "lord_of_mysteries:hunter_snare", name: "猎人捕兽夹", en: "Hunter Snare",
    summary: "猎人序列 9 的一次性陷阱，绑定放置者并控制踩中的有效猎物。",
    tags: ["方块", "猎人", "陷阱", "M2"],
    details: [["配方产量", "2"], ["触发", "非所有者有效猎物"], ["效果", "缓慢 IV + 虚弱 5 秒"], ["结果", "触发后消耗"]],
    long: "以铁粒、线、绊线钩与橡木压力板合成。放置后记录所有者 UUID；自然生成、敌对或已参与战斗的生物踩中时触发控制效果，并向在线所有者反馈目标名称。" },
  { type: "block", id: "lord_of_mysteries:ritual_altar", name: "仪式祭坛", en: "Ritual Altar",
    summary: "可交互仪式核心方块，保存材料、阶段、进度与封印产物。",
    tags: ["方块", "仪式", "M1"],
    details: [["硬度", "3.0"], ["爆炸抗性", "6.0"], ["用途", "仪式核心"], ["工具", "需正确工具掉落"]],
    long: "右键投入永燃火柴盒、纯净水×3、青兰花×5和白蜡烛×8，在夜晚晴天空手启动。状态机依次经过 ASSEMBLED、PRIMED、INVOKING、RESOLVING 与 COMPLETE，并把封印状态写入物品 NBT。当前 Alpha 使用祭坛库存代表圆阵材料，多方块摆放检测留待深化。" },

  /* 物品 */
  { type: "item", id: "lord_of_mysteries:spirit_herb", name: "灵性草药", en: "Spirit Herb",
    summary: "基础灵性材料，魔药与仪式的常用输入。",
    tags: ["材料", "魔药", "M1"], details: [["用途", "魔药 / 仪式材料"], ["来源", "采集 / 掉落 / 交易（规划）"], ["注册", "ModItems.SPIRIT_HERB"]],
    long: "最基础的灵性材料，几乎所有低序列魔药都会用到。M1 作为占卜家魔药的核心原料之一。" },
  { type: "item", id: "lord_of_mysteries:divination_crystal", name: "占卜水晶", en: "Divination Crystal",
    summary: "占卜与序列9魔药的关键材料，也作为创造模式标签图标。",
    tags: ["材料", "占卜", "魔药", "M1"], details: [["用途", "占卜家魔药 / 仪式聚焦"], ["注册", "ModItems.DIVINATION_CRYSTAL"]],
    long: "蕴含微弱预言之力的水晶，是占卜家途径魔药不可或缺的材料，也用于聚焦占卜仪式。" },
  { type: "item", id: "lord_of_mysteries:moonwater", name: "月华水", en: "Moonwater",
    summary: "夜晚 / 月相相关的魔药辅助材料，提供品质加成。",
    tags: ["材料", "夜晚", "品质", "M1"], details: [["品质加成", "+0.2"], ["关联", "占卜家魔药 序列9"]],
    long: "在月光下采集的清水，蕴含月之灵性。加入魔药可提升制作品质档位。" },
  { type: "item", id: "lord_of_mysteries:contaminated_mixture", name: "污染混合物", en: "Contaminated Mixture",
    summary: "魔药制作失败或污染反应的产物，危险且无用。",
    tags: ["失败产物", "污染", "M1"], details: [["来源", "魔药失败结果"], ["关联", "污染 / 失控压力"]],
    long: "炼药失败的残渣，充满失控的灵性。饮用会大幅增加污染，应妥善处理。" },

  /* 封印物 */
  { type: "artifact", id: "lord_of_mysteries:eternal_matchbox", name: "封印物 · 永燃火柴盒", en: "Eternal Matchbox",
    summary: "危险等级 5。点燃能伤害灵体的灵性火焰，代价是每次使用累积失控压力。",
    tags: ["封印物", "灵火", "危险等级5", "M1"],
    details: [["危险等级", "5"], ["主动效果", "放置灵魂火 / 攻击实体"], ["灵体伤害", "对占卜家失控体造成 12 点魔法伤害"], ["代价", "每次使用 失控压力 +15"], ["封印", "封印后无法使用"]],
    long: "永燃火柴盒可点燃灵魂火并灼烧实体，对占卜家失控体造成双倍魔法伤害。每次使用增加 15 失控压力并消耗耐久。完成净化封印后，效果与代价都会被 NBT 封印状态抑制。" },

  /* 仪式 */
  { type: "ritual", id: "lord_of_mysteries:calm_sealing", name: "净化封印仪式", en: "Calm Sealing Ritual",
    summary: "夜晚晴天在祭坛上执行的封印仪式，可安全抑制永燃火柴盒。",
    tags: ["仪式", "封印", "夜晚", "M1"],
    details: [["环境", "夜晚 + 晴天"], ["材料", "纯净水×3 · 青色兰花×5 · 白蜡烛×8"], ["布局", "祭坛半径3圆阵"], ["失败风险", "低阶灵体 / 污染 / 爆炸"]],
    long: "投入完整材料并在夜晚晴天启动后，祭坛会锁定状态并持续 160 tick。成功时消耗纯净水与青兰花，白蜡烛保留，永燃火柴盒获得 sealed NBT。环境在施术中失效会进入 FAILED，材料不会被错误重复结算。" },

  { type: "world", id: "abandoned_investigator_camp", name: "废弃调查员营地", en: "Abandoned Investigator Camp",
    summary: "主世界低概率生成的小型营地，是普通人接触神秘世界的自然入口。",
    tags: ["世界", "结构", "探索", "M1"],
    details: [["维度", "主世界"], ["基础概率", "每区块 0.5%"], ["配置", "structure_generation_rate"], ["战利品", "残缺手稿 / 灵性草药 / 占卜水晶"]],
    long: "新加载区块会通过世界级 SavedData 保证只检查一次。命中后在地表生成熄灭营火、破损帐篷、木桶和调查箱；箱内可发现残缺手稿与低序列材料。当前为轻量程序结构，后续将迁移到结构模板并接入完整调查任务链。" },

  /* 组织 */
  { type: "org", id: "org_nighthawks", name: "值夜者", en: "Nighthawks",
    summary: "守序的非凡者组织，清剿失控与灵界威胁，玩家可积累声望。",
    tags: ["组织", "守序", "规划"], details: [["立场", "守序 / 治安"], ["声望", "orgReputation 记录"], ["里程碑", "M2+ 深化"]],
    long: "值夜者是维护凡俗与非凡秩序的组织。玩家协助清剿失控体、封印危险物可积累声望，解锁委托与资源。M1 已在数据结构中预留 orgReputation，深化在 M2+。" },
  { type: "org", id: "org_aurora", name: "极光会", en: "Aurora Order",
    summary: "追寻知识与晋升之路的非凡者结社。（呼应极光宇宙设定的彩蛋组织）",
    tags: ["组织", "知识", "规划"], details: [["立场", "求知 / 晋升"], ["里程碑", "M5 深化"]],
    long: "极光会专注非凡知识的收集与途径研究，为成员提供魔药配方与晋升指引。（这也是对「极光」系列的一个小小致敬 🎀）" },

  /* 世界 / 空间 */
  { type: "world", id: "grayfog", name: "灰雾空间", en: "The Gray Fog",
    summary: "介于现实与灵界之间的诡秘空间，高序列非凡者的舞台。M2 基础版。",
    tags: ["空间", "灰雾", "规划"], details: [["性质", "半灵界维度"], ["里程碑", "M2 基础版"]],
    long: "灰雾是弥漫在世界之上的神秘空间，隐藏着信息、交易与危险。高序列占卜家可窥探甚至进入灰雾。M2 将实现基础版本。" }
];

/* ── 分类标签映射 ── */
LOM.labels = {
  ability: "能力", system: "机制", potion: "魔药", block: "方块", item: "物品",
  artifact: "封印物", ritual: "仪式", org: "组织", world: "世界", status: "状态"
};
