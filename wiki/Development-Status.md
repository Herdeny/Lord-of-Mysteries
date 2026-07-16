# 开发状态

<!-- project-status:start -->
- 当前版本：**`0.8.3-1.20.1`**
- 开发阶段：**M1 验收 Beta / M2 预研**（M1）
- 技术基线：Minecraft **1.20.1** · Forge **47.4.20** · Java **17**
- 最后更新：**2026-07-16 16:41:03 UTC+01:00**（`2026-07-16T15:41:03Z`）
<!-- project-status:end -->

## 里程碑

<!-- roadmap:start -->
> 设计基线：**v0.8** · 当前里程碑：**M1**

| 里程碑 | 阶段 | 状态 | 目标 |
|---|---|---|---|
| M0 | Foundation | 已完成 | Capability、网络、配置、注册、构建与手册骨架。 |
| M1 | MVP vertical slice | 进行中 | 完成占卜家 9-7 的魔药、扮演、灵视、失控和一小时生存切片。 |
| M2 | MVP alpha | 规划 | 五途径 9-7、雾都镇区、委托、调查链一和原创化词表。 |
| M3 | MVP beta | 规划 | 24 件封印物、6 个世界事件、值夜者线和经济基础。 |
| M4 | EP1 | 规划 | 五途径序列 6-5、晋升仪式、特性完整版、灰雾与塔罗会。 |
| M5 | EP2 | 规划 | 序列 4、GeckoLib 神话形态、第二批四途径和组织战争。 |
| M6 | EP3 | 远期 | 灵界维度、1 级封印物、贝克兰德大城市与真神级事件。 |
| M7 | Ecosystem | 远期 | 22 途径社区共创、Addon API 冻结与稳定版生态。 |

> 门禁规则：当前里程碑验收未完成前，不得把后续阶段预研标记为该阶段已完成。
<!-- roadmap:end -->

当前按 v0.8 硬门禁处于 M1 验收：占卜家序列 9–7 的代码纵切、材料、生物、能力、
扮演、确定性新手营地、野外风险工具、睡眠恢复、精神守护符、三类自然生物、
本地化调查日志、九章神秘学手账、十九段教程/晋升成就、Capability schema 11 六目标试炼追踪器与委托持久化、生物弱点反制和指引命令已完成；
一小时生存、死亡/重连和专用服务器平衡记录仍是验收缺口。
观众、猎人、偷盗者、学徒序列 9–7 与通用仪式底座作为后续阶段预研保留。

M2 已开始预研：22 途径 CSV 主表、34 个生成资源、知识经济与四条调查传闻已经落地。
五条 MVP 途径序列 9-7 已形成完整代码预研纵切；偷盗者/学徒含十八项扮演、分层按键、知识副本、空间与解密玩法。
轻量雾都前哨、委托板、事务所便士账本、2 种委托、2 条任务链、寻猫引导和失踪调查小队序章已经可玩。

## 当前限制

- 灵视使用只对施术者可见的服务端粒子，实体描边尚未完成。
- 五途径失控体、幻形蛇、灵体微光和灰烬傀儡暂时复用原版模型。
- 当前只接入净化封印仪式；尊名呼名、晋升饮药窗口与多人参与加成尚未实现。
- 调查营地入口已可稳定定位，但仍是轻量程序结构。
- 占卜家序列 9–7 已具备自动验收记录，但尚未完成真实一小时切片和专用服务器回归。
- 完整雾都镇区、NPC、调查链一护送/夜袭后半段、多人一致性矩阵与 M2 schema 冻结仍待实现。
- 专用像素美术仍待制作。

## 验证基线

- `python scripts/sync_project_metadata.py --check`
- JSON 资源解析
- `./gradlew clean build`
- GitHub Build、CodeQL、Documentation Consistency、Pages、Wiki Sync
- M1 记录表：[`docs/M1_ACCEPTANCE_CHECKLIST.md`](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/docs/M1_ACCEPTANCE_CHECKLIST.md)
- M1 追踪器：[`docs/M1_TRIAL_TRACKER.md`](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/docs/M1_TRIAL_TRACKER.md)
- v0.8/M2 预研：[`docs/Project_Mystery_Design_Doc_v0.8.md`](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/docs/Project_Mystery_Design_Doc_v0.8.md)
