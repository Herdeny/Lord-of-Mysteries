# M1 实现规划 — 占卜家序列9完整闭环

> 权威规格：`docs/Project_Mystery_Mod_Design_Doc_v0.4.pdf`
> 技术基线：**Forge 1.20.1 + Java 17 + Capability**（README「一、技术基线」为准）
> ⚠️ 设计文档正文写的是 NeoForge 1.21.1 + Attachment API，但工程已锁定 Forge 1.20.1；
> 一切 API 以 Forge 1.20.1 + Capability 为准，文档仅提供机制/数值规格。

## M1 交付标准（README「二」+ 文档「八」）
普通人 → 坩埚制魔药 → 晋升序列9占卜家 → 靠「扮演」把消化度 0→100% → 可晋升。
含：3能力 + 灵性消耗恢复 + 占卜可信度 + 扮演事件系统 + 坩埚魔药制作 + 消化与失控 + 1失控体 + 1封印物 + 基础知识手册UI。

## 数值规格（文档 §5/§6/§7/§8/§9）
- 占卜家序列9：灵性上限 122（基础100 + 序列成长22）
- 灵性恢复：非战斗 + 光照≥7 + 失控压力<30 时，序列9 = 0.05/s（1.0/20s）
- 污染分级：0-24稳定 / 25-49轻度(每5min检定) / 50-74危险(每2min) / 75-99临界(每30s) / 100立即失控
- 能力：
  - 灵视 Spirit Vision：持续被动可开关，0.8灵性/s，32格渲染灵体颜色（绿友好/黄中立/红敌意/紫污染/白高危/灰灵体）
  - 危险直觉 Danger Intuition：触发被动，30s冷却，35%概率致命攻击前0.8s预警
  - 简易占卜 Simple Divination：主动，消耗15灵性，60s冷却，返回扭曲结果
- 占卜可信度：finalScore = baseClear − interference + Gaussian(0,0.1)；分清晰/模糊/错误；服务端算真值，客户端按等级扭曲
- 扮演收益 = 基础收益 × 事件质量系数(0.7-1.2) × 新颖度系数 × 风险系数 × 魔药品质系数
  - 新颖度：elapsed/decayTicks，序列9 decayTicks=1200
  - 风险系数：1.0 + insanityPressure/200
  - 序列9扮演清单：divination_success(+12) / abstain_divination(+8) / interpret_ambiguous(+15) / help_player_escape(+18) / over_divination_penalty(-5)
- 坩埚：温度维持[60,80]，brewingTime 1200tick；品质：完美(顺序全对+温差≤5,×1.2)/完整(对+温差≤15,×1.0)/瑕疵(80%+温差≤30,×0.7且初始压力+20)/污染(不匹配)
- 失控：recoverable模式→倒地30s+原地生成占卜家失控体+掉破碎特性+24h精神创伤
- 失控体：拥有玩家失控前能力、无差别攻击、扭曲半透明紫色实体
- 永燃火柴盒：危险等级5，点燃灵火伤害灵体，每次使用+15失控压力，净化仪式封印(纯水×3+青兰花×5+白蜡烛×8，夜晚+晴天)

## 实现批次
- **批次1（能力子系统）**：ability 模块——灵性消耗/恢复接入、灵视被动、危险直觉、简易占卜+可信度计算；纯逻辑抽出可单测。
- 批次2（坩埚+魔药）：CrucibleBlockEntity 温度/时间/品质检定 + 晋升逻辑。
- 批次3（扮演+消化+失控）：acting 事件系统 + 消化收益公式 + 失控结局 + 失控体实体。
- 批次4（封印物+知识手册UI）：永燃火柴盒 + 净化仪式 + Knowledge Codex UI。
- 每批次结束跑 `./gradlew compileJava test` 保持 BUILD SUCCESSFUL。

---

## 批次1 完成记录（2026-07-01）

### 新增文件
- `src/main/java/top/aurora/lordofmysteries/ability/SpiritualityCost.java` — 灵性消耗统一 API（`canPay/tryConsume/forceConsume/refund`）
- `src/main/java/top/aurora/lordofmysteries/ability/AbilityCooldowns.java` — 冷却工具（基于 gameTime 截止 tick）
- `src/main/java/top/aurora/lordofmysteries/ability/DivinationCredibility.java` — **纯逻辑可信度计算**：`baseClearBySequence/baseClear/interference/finalScore/classify/distortDirection/distortText/yawToCardinal8`
- `src/main/java/top/aurora/lordofmysteries/ability/SpiritFactionColor.java` — 阵营分类枚举（GREEN/YELLOW/RED/PURPLE/WHITE/GRAY）+ 服务端分类
- `src/main/java/top/aurora/lordofmysteries/ability/SpiritVisionHandler.java` — 灵视被动：0.8/s 扣费、32 格扫描、彩色 dust 粒子占位渲染（仅对灵视持有者可见）
- `src/main/java/top/aurora/lordofmysteries/ability/DangerIntuitionHandler.java` — 危险直觉：`LivingAttackEvent` + 致命判定 + 35% 概率 + 30s 冷却
- `src/main/java/top/aurora/lordofmysteries/ability/SimpleDivinationHandler.java` — 简易占卜：15 灵性、60s 冷却、按 clarity 扭曲展示
- `src/main/java/top/aurora/lordofmysteries/network/PMNetwork.java` — Forge SimpleChannel `main`
- `src/main/java/top/aurora/lordofmysteries/network/ToggleSpiritVisionC2SPacket.java`
- `src/main/java/top/aurora/lordofmysteries/network/UseSimpleDivinationC2SPacket.java`
- `src/main/java/top/aurora/lordofmysteries/network/package-info.java`
- `src/main/java/top/aurora/lordofmysteries/client/PMKeyBindings.java` — `V`=灵视，`B`=占卜
- `src/main/java/top/aurora/lordofmysteries/client/ClientModEvents.java`
- `src/main/java/top/aurora/lordofmysteries/client/ClientForgeEvents.java`
- `src/test/java/top/aurora/lordofmysteries/ability/DivinationCredibilityTest.java` — 14 项
- `src/test/java/top/aurora/lordofmysteries/ability/SpiritualityCostTest.java` — 8 项

### 修改文件
- `PlayerMysteryData.java` — 新增 `spiritVisionActive` / `divinationCooldownEndTick` / `dangerIntuitionCooldownEndTick`（NBT + copyFrom 同步）
- `ProjectMystery.java` — `commonSetup` 中 `event.enqueueWork(PMNetwork::register)`
- `assets/lord_of_mysteries/lang/{en_us,zh_cn}.json` — 按键分类与两个 KeyMapping 翻译

### 数值实现
- 灵性上限：序列 9 = 122（现有 `SpiritualityRegenHandler` 保留 0.05/s，条件已由现有 handler 满足）
- 灵视：`DRAIN_PER_SECOND = 0.8`，`SCAN_RADIUS = 32`
- 危险直觉：`PROC_CHANCE = 0.35`，`COOLDOWN_TICKS = 600`
- 简易占卜：`COST = 15`，`COOLDOWN_TICKS = 1200`
- 可信度：`baseClearBySequence`（9→0.60、8→0.68、7→0.76、6→0.84、5→0.90）+ 灵性 % × 0.20；`THRESHOLD_CLEAR=0.65 / THRESHOLD_BLURRED=0.30`

### 测试结果
- `./gradlew compileJava` **BUILD SUCCESSFUL**（仅 2 个来自现有 `ProjectMystery` 的 Forge deprecation 警告，与本批次无关）
- `./gradlew test` **BUILD SUCCESSFUL**：`PlayerMysteryDataTest` 3 项 + `DivinationCredibilityTest` 14 项 + `SpiritualityCostTest` 8 项 = **25 项全部通过**

### 已知遗留 / TODO
- 灵性上限的 122 目前仍靠 `spiritualityMax` 字段人工设置；晋升逻辑（批次2 坩埚）落地时应在服务魔药时一并写入。
- 灵视目前用服务端定向 dust 粒子作 M1 占位渲染；后续应实现 `SpiritVisionSyncS2CPacket` + 客户端描边着色器。
- `SimpleDivinationHandler` 里附近高序列干扰暂只统计玩家（NPC 尚未接入 npc 序列数据）。
- 危险直觉已改为拦截一次致命攻击并提供 16 tick 加速窗口，作为 Forge 1.20.1 下的等价实现。

---

## 批次2-4完成记录（2026-07-02）

### 已落地
- `CrucibleBlock` / `CrucibleBlockEntity`：三槽顺序投料、热源温控、1200 tick 进度、NBT 持久化、破坏掉落与成品领取。
- `CrucibleRecipeLogic` / `PotionQuality`：完美、完整、瑕疵、污染四档品质及纯逻辑单测。
- `SeerPotionItem`：魔药品质 NBT、普通人晋升序列9、灵性上限122、初始压力/污染、知识解锁。
- `ActingCalculator` / `ActingEventHandler`：新颖度、风险、品质、服务器倍率与消化进度；清晰占卜、克制占卜、过度占卜已接线。
- `InsanityEventHandler`：污染分级反馈与 `recoverable` / `permanent` / `death` 三种失控结局。
- `EternalMatchboxItem`：灵魂火、实体伤害、失控体额外伤害、+15失控压力、耐久与冷却。
- `MysteryStatusScreen`：`N` 键请求服务端权威数据，显示灵性、污染、压力、品质与已知知识。
- 生存配方、方块掉落、中英本地化与原版纹理回退。

### 与设计规格的暂时差异
- 灵视使用定向彩色粒子，客户端描边渲染待实现。
- 危险直觉在 Forge 1.20.1 中拦截一次致命攻击并提供0.8秒加速，代替无法通用实现的“提前预告下一次攻击”。
- 失控体已有独立实体类型、属性、灵性干扰与破碎特性掉落，但暂复用原版僵尸模型。
- 净化封印已接入 `RitualStateMachine` 与祭坛库存交互；多方块圆阵扫描和完整失败权重待深化。
- 调查营地已用轻量程序生成落地；结构模板、定位线索与七步调查任务链待后续里程碑。

## M1补完记录（2026-07-02）
- 净化封印仪式：祭坛 BlockEntity、材料库存、环境校验、160 tick 调用、封印 NBT 与防重复结算。
- 占卜家失控体：独立 `EntityType`、30 生命、灵性干扰、紫色粒子、专属掉落与永燃火柴盒弱点。
- 废弃调查员营地：主世界区块级生成、`SavedData` 去重、可配置生成倍率与专属调查箱战利品。

