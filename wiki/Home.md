# Project Mystery Wiki

<!-- project-status:start -->
- 当前版本：**`0.9.0-1.20.1`**
- 开发阶段：**v0.9 M0 内容基建 / M1 纵切迁移**（M0）
- 技术基线：Minecraft **1.20.1** · Forge **47.4.20** · Java **17**
- 最后更新：**2026-07-17 16:17:22 UTC+01:00**（`2026-07-17T15:17:22Z`）
<!-- project-status:end -->

Project Mystery 是受《诡秘之主》启发的非官方 Minecraft 生存冒险 Mod。玩家从普通人开始，通过调查、炼制魔药、扮演对应身份、管理灵性与精神风险逐步晋升。仓库现有可运行行为是唯一实现基线；v0.9 设计文档定义目标和门禁，不会把设计数量直接当作完成度。

> 本项目未获得原作权利人、Mojang 或 Microsoft 的授权、认可或赞助。原创代码采用 GPL-3.0-or-later；原创文档与美术采用 CC BY-NC-SA 4.0；原作名称、设定、商标与第三方素材不在仓库许可范围内。

## v0.9 当前重点

当前正式里程碑是 **M0 内容基建**，不是旧路线中的 M1/M2：

- 完整 v0.9 设计、增量说明与 manifest 已精确导入并由 CI 校验。
- schema v4 公共元数据进入 22 途径目录、15 个现有序列、3 个委托和 3 条任务链。
- 当前内容图包含 71 节点、63 关系，并输出孤儿、剧透、兼容、本地化与资产报告。
- 玩家 Capability 升至 schema 16，旧 schema 15 非凡者存档自动回填特性 bundle。
- 每次序列晋升记录特性层、纯度、印记、污染和来源哈希。
- 扮演 v2 新增原则理解、角色过度认同和每日 `/pm reflect` 反思。
- 网络协议升至 7，状态面板同步显示新的扮演身份字段。

M0 尚未完成：目录仍需扩至 100 节点，并补齐未知 ID 保存、迁移前备份、dirty mask、正式 DataFix 和完整 GameTest 存档矩阵。

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
- 轻量雾都前哨、废弃教堂、邪教营地、神秘学家小屋和四名 NPC。
- 3 种委托、3 条任务链、真假配方、三解救援、记者护送和三波夜袭。
- 2–4 人持久调查账本、离线追赶、换队清理和个人独立结算。

### 引导与诊断

- `/pm next`：当前唯一优先目标。
- `/pm recover`：补回手稿与罗盘。
- `/pm status`：查看途径、序列、灵性、风险、原则理解与过度认同。
- `/pm reflect`：每游戏日一次反思。
- `/pm handbook [1-9]`：按知识门槛阅读九章手账。
- `/pm journal`：查看已发现规则、能力和生物。
- `/pm doctor`：修复玩家数据并诊断内容、协议和世界目标。
- `/pm trial ...`：保留旧一小时追踪器作为 M1 迁移证据。
- `/pm party` / `/pm party sync`：多人调查账本和追赶。

## v0.9 实现边界

- 内容图是 71 节点，不是设计规格中的全部内容。
- 特性 bundle 已存档，但提取、实体掉落、合并和完整死亡守恒未完成。
- 扮演 v2 已有数据与命令基础，但身份卡、反思日志物品和四阶段验收未完成。
- M1 的目标变为两小时完整纵切；旧一小时追踪器不能直接证明新 M1 通过。
- M2 需要正式报社、事务所、警局、调查板、经济和三个动态案件；现有轻量委托只是迁移资产。
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
- [仓库健康与资源完整性](Repository-Health-and-Resource-Integrity)
- [M1 一小时迁移追踪器](M1-Trial-Tracker)
- [完整路线图](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/ROADMAP.md)
- [完整 v0.9 设计](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/docs/Project_Mystery_Design_Doc_v0_9.md)
- [v0.9 差异报告](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/docs/V0.9_DIFFERENCE_REPORT.md)
- [在线资料站](https://herdeny.github.io/Lord-of-Mysteries/)

## 贡献

欢迎 Issue、设计讨论、数据内容、代码、测试、本地化和原创美术贡献。提交内容必须说明来源与许可，并接受 schema v4、内容图、资源完整性、Gradle 测试和专用服务器门禁。不要提交未经授权的原作插图、商业素材或从其他 Mod 直接提取的资源。
