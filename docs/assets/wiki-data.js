/*
 * Lord of Mysteries Wiki — 数据源
 * 与项目 data/*.json 与设计文档 v0.4 同源整理。
 * 纯静态数据，GitHub Pages 直接加载，无需构建。
 */

window.LOM = window.LOM || {};

/* ── 项目元信息 ── */
LOM.meta = {
  modId: "lord_of_mysteries",
  name: "Lord of Mysteries",
  cnName: "诡秘之主",
  version: "0.0.1-1.20.1",
  mc: "Minecraft Java 1.20.1",
  loader: "Forge 47.4.20",
  java: "17",
  stage: "M1 开发中 · M0 技术验证已完成",
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
  { id: "M1", title: "占卜家序列9完整闭环", state: "active",
    points: ["灵视 / 危险直觉 / 简易占卜三能力 + 占卜可信度", "扮演事件系统 + 消化进度 0→100%",
      "坩埚魔药制作（温度 / 时间 / 品质四档）", "消化与失控机制 + 占卜家失控体", "永燃火柴盒封印物 + 净化仪式", "基础知识手册 UI"] },
  { id: "M2", title: "三途径序列9-8", state: "planned",
    points: ["占卜家 / 观众 / 猎人 序列 9-8", "通用仪式状态机 + 多人同步", "灰雾空间基础版"] },
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
    desc: "力量与战斗的途径。强化肉体、狂怒与狩猎本能，近战与生存能力顶尖。M2 规划。",
    traits: ["肉体强化", "狂怒", "狩猎", "夜视"], baseSpirit: 100, growth: 20, status: "M2 规划" },
  { id: "spectator", name: "观众", en: "Spectator", accent: "#6bcad0",
    desc: "精神与心灵的途径。读心、暗示、精神操控，擅长影响与洞察他人心智。M2 规划。",
    traits: ["读心", "精神暗示", "心灵洞察", "情绪感知"], baseSpirit: 100, growth: 22, status: "M2 规划" },
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

/* ── 详细条目（卡片 + 详情弹窗） ── */
LOM.entries = [
  /* 能力 ability */
  { type: "ability", id: "lord_of_mysteries:spirit_vision", name: "灵视", en: "Spirit Vision",
    summary: "持续被动，可开关。开启后 32 格内以颜色区分渲染生物灵体，识别阵营与威胁。",
    tags: ["能力", "序列9", "被动", "M1"],
    details: [["类型", "持续被动（可开关）"], ["灵性消耗", "0.8 / 秒"], ["范围", "32 格"],
      ["耗尽", "灵性归零自动关闭"], ["注册", "ability/SpiritVision"]],
    long: "灵视是占卜家最标志性的能力。开启时持续消耗灵性，将 32 格内的生物以灵体颜色标注：<b style='color:#74b86f'>绿</b>=友好、<b style='color:#e2c85a'>黄</b>=中立、<b style='color:#cc5b4d'>红</b>=敌意、<b style='color:#8b5cf6'>紫</b>=污染、<b style='color:#f1eee7'>白</b>=高危、<b style='color:#9a9a9a'>灰</b>=灵体。阵营判定在服务端完成后同步到客户端渲染，防止外挂窥探。" },
  { type: "ability", id: "lord_of_mysteries:danger_intuition", name: "危险直觉", en: "Danger Intuition",
    summary: "触发被动。将受致命伤害前 0.8 秒，35% 概率获得预警，是保命核心。",
    tags: ["能力", "序列9", "被动", "M1"],
    details: [["类型", "触发被动"], ["冷却", "30 秒"], ["触发概率", "35%"], ["预警提前量", "0.8 秒"]],
    long: "当玩家即将受到会致命的攻击（伤害 ≥ 当前生命）时，有 35% 概率在 0.8 秒前触发预警——通过音效、屏幕提示与短暂状态给玩家反应窗口。冷却 30 秒，避免连续触发。灵性越充裕、序列越高，未来将提升触发概率。" },
  { type: "ability", id: "lord_of_mysteries:simple_divination", name: "简易占卜", en: "Simple Divination",
    summary: "主动能力。消耗 15 灵性询问方向 / 危险 / 状态，结果按可信度扭曲呈现。",
    tags: ["能力", "序列9", "主动", "占卜", "M1"],
    details: [["类型", "主动"], ["灵性消耗", "15"], ["冷却", "60 秒"], ["结果", "按可信度分清晰 / 模糊 / 错误"]],
    long: "占卜家可对方向、危险源、目标状态进行简易占卜。服务端计算真实结果，再依据可信度得分把结果「扭曲」后返回：清晰=准确，模糊=方向偏移 / 用语含糊，错误=误导。对同一目标反复占卜会累积「过度占卜」惩罚，降低可信度并影响消化。" },

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
    long: "「扮演」是《诡秘之主》的灵魂机制：你必须像你想成为的角色那样行动，才能消化体内的魔药之力。<br>序列9 占卜家的扮演清单：<br>• <b>成功占卜并规避风险</b> +12<br>• <b>灵性不足时主动放弃使用能力</b> +8<br>• <b>正确解读模糊占卜结果</b> +15<br>• <b>用占卜帮他人脱险</b> +18<br>• <b>对同一目标过度占卜</b> −5（惩罚）<br>短时间重复同一扮演，新颖度系数线性衰减，收益骤降——鼓励多样化行为。" },
  { type: "system", id: "digestion", name: "消化与晋升", en: "Digestion & Advancement",
    summary: "消化度 0→100%。满额且完成扮演考验后可服用高一阶魔药晋升序列。",
    tags: ["机制", "消化", "晋升", "M1"],
    details: [["范围", "0 - 100%"], ["提升", "靠扮演事件累积"], ["满额", "可晋升下一序列"], ["显示", "非调试默认隐藏精确值"]],
    long: "服用魔药后力量并不会立刻属于你——必须通过持续「扮演」逐步消化。消化度满 100% 意味着完全掌控当前序列，才能安全地晋升。消化不足强行晋升 = 极高失控风险。默认配置隐藏精确数值（show_exact_digestion=false），只给模糊反馈，还原原作的未知感。" },
  { type: "system", id: "breakdown", name: "失控机制", en: "Loss of Control",
    summary: "灵性 / 污染 / 失控压力失衡将导致失控。默认 recoverable 模式：倒地并生成失控体。",
    tags: ["机制", "失控", "危险", "M1"],
    details: [["触发", "污染达100 / 高压检定失败"], ["recoverable", "倒地30s + 生成失控体 + 掉破碎特性 + 24h精神创伤"], ["config", "breakdown_mode"]],
    long: "失控是踏足非凡的代价。污染累积到 100、或高压区间检定失败会触发失控。默认 recoverable 模式下：玩家倒地 30 秒，原地生成一个占卜家失控体，掉落「破碎特性」，并获得 24 小时精神创伤 debuff。硬核配置可改为不可恢复。" },
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
    long: "序列9 占卜家魔药需在坩埚中精确调配：维持温度 60-80℃、按正确顺序投料、熬煮 1200 tick。品质分四档——完美（×1.2）/完整（×1.0）/瑕疵（×0.7 且初始压力+20）/污染（无效且大幅增污）。品质越高，后续扮演消化收益越好。" },

  /* 坩埚 / 方块 */
  { type: "block", id: "lord_of_mysteries:crucible", name: "坩埚", en: "Crucible",
    summary: "魔药制作的核心方块，绑定 CrucibleBlockEntity，掌管温度、投料顺序与熬煮进度。",
    tags: ["方块", "魔药", "方块实体", "M1"],
    details: [["硬度", "3.5"], ["爆炸抗性", "6.0"], ["温控", "维持 60-80℃"], ["熬煮", "1200 tick"], ["品质检定", "顺序 + 温差"]],
    long: "坩埚是玩家亲手炼制魔药的地方。放入材料后记录投料顺序，玩家需通过燃料 / 环境控制温度维持在配方要求区间。完美：顺序全对且温差≤5；完整：对且温差≤15；瑕疵：匹配80%且温差≤30；污染：材料不匹配。CrucibleBlockEntity 每 tick 推进熬煮与品质判定。" },
  { type: "block", id: "lord_of_mysteries:ritual_altar", name: "仪式祭坛", en: "Ritual Altar",
    summary: "仪式结构核心方块。围绕它布置阵法与材料，执行封印、净化等仪式。",
    tags: ["方块", "仪式", "M0/M1"],
    details: [["硬度", "3.0"], ["爆炸抗性", "6.0"], ["用途", "仪式核心"], ["工具", "需正确工具掉落"]],
    long: "仪式祭坛是所有仪式的中心。玩家需在其周围以特定半径布置蜡烛、材料与图案，在正确的环境条件（时间、天气、高度）下发动仪式。M1 用于净化封印仪式，封印永燃火柴盒等封印物。" },

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
    details: [["危险等级", "5"], ["主动效果", "点燃灵性火焰（可伤灵体）"], ["持续", "100 tick"], ["代价", "每次使用 失控压力 +15"], ["封印", "净化仪式"]],
    long: "永燃火柴盒是 M1 的样例封印物。它能点燃特殊的灵性火焰，对灵体类实体造成有效伤害——是对抗灵界威胁的利器。但每次使用都会累积 15 点失控压力，滥用将走向失控。要安全收纳它，需执行净化仪式将其封印。" },

  /* 仪式 */
  { type: "ritual", id: "lord_of_mysteries:calm_sealing", name: "净化封印仪式", en: "Calm Sealing Ritual",
    summary: "围绕仪式祭坛布置半径3圆阵，在夜晚晴天封印异常封印物。",
    tags: ["仪式", "封印", "夜晚", "M1"],
    details: [["环境", "夜晚 + 晴天"], ["材料", "纯净水×3 · 青色兰花×5 · 白蜡烛×8"], ["布局", "祭坛半径3圆阵"], ["失败风险", "低阶灵体 / 污染 / 爆炸"]],
    long: "净化封印仪式用于安全收纳封印物（如永燃火柴盒）。需在夜晚且晴天，以仪式祭坛为中心布置半径 3 的圆阵，投入纯净水×3、青色兰花×5、白蜡烛×8。若材料或环境不符，仪式可能失败——召唤低阶灵体、扩散污染甚至爆炸。" },

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
