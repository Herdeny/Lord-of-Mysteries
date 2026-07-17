# 开发状态

<!-- project-status:start -->
- 当前版本：**`0.9.1-1.20.1`**
- 开发阶段：**v0.9 M0 内容基建 / M1 纵切迁移**（M0）
- 技术基线：Minecraft **1.20.1** · Forge **47.4.20** · Java **17**
- 最后更新：**2026-07-17 17:23:46 UTC+01:00**（`2026-07-17T16:23:46Z`）
<!-- project-status:end -->

## 里程碑

<!-- roadmap:start -->
> 设计基线：**v0.9** · 当前里程碑：**M0**

| 里程碑 | 阶段 | 状态 | 目标 |
|---|---|---|---|
| M0 | Content foundation | 进行中 | Schema v4、内容关系图、生成器、迁移框架、来源审计与 CI 门禁。 |
| M1 | Core vertical slice | 规划 | 占卜家 9–7、第一魔药、扮演 v2、特性守恒与一个可生活的城市街区。 |
| M2 | Investigation and life | 规划 | 报社、事务所、警局、证据板、经济和三个动态案件。 |
| M3 | Five launch pathways | 规划 | 占卜家、观众、猎人、偷盗者和学徒序列 9–5 的差异化闭环。 |
| M4 | Organizations and artifacts | 规划 | 七教会框架、五个隐秘组织和 24 个已验证封印物。 |
| M5 | Spirit world and dreams | 规划 | 灵界航路、共享梦境、12 种生态生物和 6 种异常天气。 |
| M6 | Second pathway wave | 规划 | 水手、不眠者、收尸人和歌颂者序列 9–5。 |
| M7 | Production and compatibility | 规划 | 十个工位以及 JEI、EMI、Curios、Create、Farmer's Delight 和 Jade 兼容。 |
| M8 | Third pathway wave | 远期 | 战士、秘祈人、阅读者、刺客、耕种者和药师序列 9–5。 |
| M9 | Complete 22 pathways | 远期 | 补齐 v0.9 第 60 章的七条途径序列 9–5。 |
| M10 | Demigods | 远期 | 序列 4、知识灼伤、49+ 模型和锚系统。 |
| M11 | World and seasons | 远期 | 40+ 结构类别、48 个世界事件、活历史和赛季结算。 |
| M12 | Experimental endgame | 远期 | 可完全关闭的序列 3–1、祈祷、唯一性与神位战争实验模块。 |

> 门禁规则：设计规格、data_ready、code_ready、asset_ready、playable 与 verified 必须分开统计；当前里程碑退出标准未满足前，后续阶段的既有或预研内容只能标注为迁移资产，不能宣称该阶段完成。
<!-- roadmap:end -->

## M0 完成证据

| 能力 | 当前证据 | 状态 |
|---|---|---|
| v0.9 来源 | 完整文档、增量说明、manifest 与内嵌 v0.8 哈希精确校验 | verified |
| schema v4 | 途径、15 序列、3 委托、3 任务链和 M0 目录 | code_ready |
| 内容图 | 71 节点、63 关系、6 类报告 | data_ready |
| 玩家迁移 | schema 0/15→16 DataFix、世界快照、原始 NBT 备份与孤儿保留 | code_ready |
| 非凡特性 | 分层序列、纯度、印记、污染、来源哈希 | code_ready |
| 扮演 v2 | 原则理解、角色过度认同、每日反思、UI 同步 | code_ready |
| 既有玩法 | 五途径 9–7、3 委托、3 任务链、持久队伍 | playable migration assets |
| 构建门禁 | 设计源、生成器、内容图、合同、资源、Gradle 与专服冒烟 | verified automation |

## M0 退出缺口

- 内容目录由 71 节点扩展到至少 100 个有效样例。
- Core/Knowledge/Social/Endgame dirty mask，避免无关区段全量同步。
- 存档、死亡、重生、维度切换、专服重启和降级回滚 GameTest。
- 所有新增内容保持来源、剧透、知识门槛、关系、翻译和资产证据完整。

## 后续迁移边界

- M1：旧占卜家 9–7 与一小时追踪器只是起点；必须增加两小时合同、身份卡、反思日志、特性提取/掉落和可生活城市街区。
- M2：旧三委托与轻量雾都只是起点；必须增加正式调查板、经济、三个动态案件和四人八小时无锁死验收。
- M3：五途径 9–7 已可玩，但序列 6–5、进阶仪式和多人互补矩阵未完成。
- M7：JEI、EMI 和 Jade 目前仅为只读计划，没有运行时集成。
- M12：实验终局保持默认关闭，不进入当前版本承诺。

## 验证基线

```bash
python scripts/import_v09_design.py --check
python scripts/gen_datapack.py --check
python scripts/build_content_graph.py
python scripts/check_m1_playability.py
python scripts/check_m2_investigation.py
python scripts/check_resource_integrity.py
python scripts/sync_project_metadata.py --check
./gradlew clean build
python scripts/run_server_smoke.py --timeout 180
```

相关文档：

- [v0.9 实施基线](V0.9-Implementation-Baseline)
- [M0 内容图与迁移](V0.9-M0-Content-Graph-and-Migration)
- [v0.9 差异报告](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/docs/V0.9_DIFFERENCE_REPORT.md)
- [代码审计表](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/docs/code_audit.csv)
- [完整路线](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/ROADMAP.md)
