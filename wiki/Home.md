# Project Mystery Wiki

<!-- project-status:start -->
- 当前版本：**`0.9.12-1.20.1`**
- 开发阶段：**v0.9 M2 案件主体、受影响者与差异化物证**（M2）
- 技术基线：Minecraft **1.20.1** · Forge **47.4.20** · Java **17**
- 最后更新：**2026-07-19 11:19:27 UTC+01:00**（`2026-07-19T10:19:27Z`）
<!-- project-status:end -->

Project Mystery 是受《诡秘之主》启发的非官方 Minecraft 生存冒险 Mod。玩家从普通人开始，通过调查、炼制魔药、扮演对应身份、管理灵性与精神风险逐步晋升。仓库现有可运行行为是唯一实现基线；v0.9 设计文档定义目标和门禁，不会把设计数量直接当作完成度。

> 本项目未获得原作权利人、Mojang 或 Microsoft 的授权、认可或赞助。原创代码采用 GPL-3.0-or-later；原创文档与美术采用 CC BY-NC-SA 4.0；原作名称、设定、商标与第三方素材不在仓库许可范围内。

## 第一次来到这里

| 目标 | 从这里开始 |
|---|---|
| 安装并开始单人游戏 | [安装与构建](Installation-and-Build) → [入门指南](Getting-Started) |
| 继续 M2 调查内容 | 前往雾都，右键调查板；先读 [调查板玩家手册](M2-Investigation-Board) |
| 查看证据与每日情报 | [证据档案与城市报纸](M2-Evidence-and-Newspaper) |
| 分析关系或恢复案件物品 | [证据关联、案件分析与恢复](M2-Evidence-Reasoning-and-Recovery) |
| 提出假说并纠正误判 | [玩家假说与误判恢复](M2-Player-Hypotheses-and-Reasoning-Strain) |
| 游玩每日生成案件 | [八槽位动态案件轮换](M2-Dynamic-Case-Rotation) |
| 查看结案评分与改进建议 | [结案评价与复盘](M2-Case-Debrief-and-Ratings) |
| 使用事务所与巡警服务 | [城市服务台与功能区](M2-City-Service-Desks) |
| 搭建多人服务器 | [专用服务器与多人验证](Dedicated-Server-Test-Matrix) |
| 查看当前开发进度 | [开发状态](Development-Status) → [完整路线图](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/ROADMAP.md) |
| 贡献代码、文案或美术 | [贡献指南](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/CONTRIBUTING.md) |

## v0.9 当前重点

M0 与 M1 的机器可验证退出门禁已经关闭，当前正式里程碑是 **M2 调查与生活**：

- 完整 v0.9 设计、增量说明与 manifest 已精确导入并由 CI 校验。
- schema v4 公共元数据进入 22 途径目录、15 个现有序列、4 个委托和 4 条任务链。
- 当前内容图包含 116 节点、168 关系，并输出孤儿、剧透、兼容、本地化与资产报告。
- 玩家 Capability 升至 schema 19，旧 schema 15/16/17/18 存档自动迁移并回填 M1 状态、空结案归档与空假说工作区。
- 世界加载前创建 schema 19 快照；恢复工具支持 dry-run、清单校验、恢复前安全备份、精确替换与路径穿越拒绝。
- Core/Knowledge/Social/Endgame 四区 dirty mask、生命周期摘要同步和 6 项 Forge GameTest 已进入 CI。
- 每次序列晋升记录特性层、纯度、印记、污染和来源哈希；永久失控精确转移并掉落特性载荷。
- 扮演 v2 包含身份卡、反思日志、原则理解、角色过度认同和每日反思。
- M1 两小时追踪器包含 9 项核心目标、7 个时间里程碑与 4 项连续性门禁。
- 雾都报社工作班次提供纸张、便士和面包的基础城市生活循环。
- 0.9.5 调查板新增三案证据档案；真假配方的水印、墨迹和材料顺序由服务端鉴定后展示。
- 报社轮班发放每日动态报纸，显示六类城市头条、当前案件提示、轮班状态和余额。
- 0.9.6 为新旧前哨增加事务所/巡警功能亭、调查补给交易、安全室恢复与 `/pm city` 服务目录。
- 0.9.7 为三案增加 12 条证据关系、三阶段推理、档案完整度、明确下一步、结案档案和调查板物品恢复。
- 0.9.8 增加 40/30/20/10 四维结案评价、S–D 评级、耗时/路线归档和持久化复盘命令。
- 0.9.9 增加稳定关系 ID、玩家自定义假说、误判压力/推理负担、纠正/复议恢复和结案程序扣分。
- 0.9.10 增加三类每日轮换案件、八槽位稳定生成、三轮双入口取证、植入式误导、三结论与可恢复误判。
- 0.9.11 增加绑定案件实例的证物袋、现场方块采样、指定 NPC 证词、报纸档案核验、队伍阶段同步及调查板补领/结案回收。
- 0.9.12 增加案件相关人、受影响者、可直接采样的可视物证、四类实例绑定封存样本、旧现场清理和跨日队伍实例对齐。
- 网络协议升至 12，14 个固定消息 ID 分离状态面板、周期核心摘要与调查/证据/推理/假说快照。

自动化已经验证 116 节点/168 关系内容图、260 项 JUnit、6 项 Forge GameTest、精确回滚和两次真实专服启动。真人两小时节奏、死亡/跨维度与多人长时记录继续作为发布前回归证据，而不再阻塞路线进入 M2。

## 当前可玩迁移资产

这些内容已经可以游玩，但需要按 v0.9 新门禁重新归档和验收：

### 五条序列 9–7

- 占卜家 → 小丑 → 魔术师
- 观众 → 读心者 → 心理医生
- 猎人 → 挑衅者 → 纵火家
- 偷盗者 → 诈骗师 → 解密学者
- 学徒 → 戏法大师 → 占星人

五条途径均有魔药、材料、扮演事件、主动/被动能力、独立失控反馈和中英文案。序列 6–5 尚未实现；现有五途径不能据此标记 v0.9 M3 完成。

### 生存与调查闭环

- 首次登录调查手稿、罗盘、确定性新手营地和补给桶。
- 坩埚多配方、温度、投料顺序、品质与交叉污染。
- 灵性、污染、压力、睡眠恢复、精神守护符和三类失控模式。
- 幻形蛇、灵体微光、灰烬傀儡与五途径失控体。
- 轻量雾都前哨、废弃教堂、邪教营地、神秘学家小屋和六名任务/服务 NPC。
- 4 种委托、4 条任务链、真假配方、三类八槽位动态案件、三解救援、记者护送和三波夜袭。
- 轮换案件实体证物袋、案件相关人、受影响者、可视物证、四类封存样本、标签 NPC 证词、报纸档案核验、调查板补领和队伍阶段同步。
- 2–4 人持久调查账本、离线追赶、换队清理和个人独立结算。
- 调查板案件/证据/关联推理三页签，以及世界种子与日期确定的雾都晨报。
- 玩家假说工作区、可恢复误判代价与结案程序责任。

### 引导与诊断

- `/pm next`：当前唯一优先目标。
- `/pm recover`：补回手稿、罗盘及非凡者身份物品。
- `/pm status`：查看途径、序列、灵性、风险、原则理解与过度认同。
- `/pm reflect`：每游戏日一次反思。
- `/pm life`：查看或执行当前城市生活班次引导。
- `/pm city`：查看报社、事务所和巡警岗亭交互目录。
- `/pm case analyze` / `hypothesis` / `archive` / `debrief` / `recover`：分析当前案件、提出和检验假说、查看评级与详细复盘，或在调查板附近恢复关键物品。
- `/pm handbook [1-9]`：按知识门槛阅读九章手账。
- `/pm journal`：查看已发现规则、能力和生物。
- `/pm doctor`：修复玩家数据并诊断内容、协议和世界目标。
- `/pm trial ...`：记录两小时 9 项核心目标与连续性证据。
- `/pm party` / `/pm party sync`：多人调查账本和追赶。
- `/pm commission board`：站在调查板附近重新打开案件界面。

## v0.9 实现边界

- 内容图是 116 节点，不代表设计规格中的所有内容已经实现。
- 特性 bundle 已完成永久失控转移和精确死亡掉落；重复合并与更高序列守恒继续后置。
- 扮演 v2 已完成身份卡、反思日志与验收追踪；真人体验仍需持续回归。
- M1 已通过两小时合同、9 项目标和 7 个里程碑的机器验收。
- M2 调查板、四案证据/关联推理、玩家假说与误判恢复、持久化结案评分、每日报纸、前哨三类服务台、八槽位动态案件、案件相关人/受影响者、可视物证及四类封存样本已完成；正式城市结构、完整经济、角色关系/日程、原创物证美术与周级案件调度仍在建设。
- JEI、EMI、Jade 当前只有只读兼容计划。
- 专用原创模型、完整城市、高序列和实验终局仍未完成。

## 文档入口

- [v0.9 实施基线](V0.9-Implementation-Baseline)
- [M0 内容图与迁移](V0.9-M0-Content-Graph-and-Migration)
- [开发状态](Development-Status)
- [安装与构建](Installation-and-Build)
- [入门与占卜家 9–7](Getting-Started)
- [精神风险与调查日志](Risk-and-Investigation)
- [专用服务器与多人验证](Dedicated-Server-Test-Matrix)
- [调查板玩家手册](M2-Investigation-Board)
- [证据档案与城市报纸](M2-Evidence-and-Newspaper)
- [证据关联、案件分析与恢复](M2-Evidence-Reasoning-and-Recovery)
- [玩家假说与误判恢复](M2-Player-Hypotheses-and-Reasoning-Strain)
- [八槽位动态案件轮换](M2-Dynamic-Case-Rotation)
- [结案评价与复盘](M2-Case-Debrief-and-Ratings)
- [城市服务台与功能区](M2-City-Service-Desks)
- [仓库健康与资源完整性](Repository-Health-and-Resource-Integrity)
- [M0/M1 完成报告](V0.9-M0-M1-Completion)
- [M1 两小时纵切追踪器](M1-Trial-Tracker)
- [完整路线图](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/ROADMAP.md)
- [完整 v0.9 设计](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/docs/Project_Mystery_Design_Doc_v0_9.md)
- [v0.9 差异报告](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/docs/V0.9_DIFFERENCE_REPORT.md)
- [在线资料站](https://herdeny.github.io/Lord-of-Mysteries/)

## 贡献

欢迎 Issue、设计讨论、数据内容、代码、测试、本地化和原创美术贡献。提交内容必须说明来源与许可，并接受 schema v4、内容图、资源完整性、Gradle 测试和专用服务器门禁。不要提交未经授权的原作插图、商业素材或从其他 Mod 直接提取的资源。
