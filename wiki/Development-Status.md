# 开发状态

<!-- project-status:start -->
- 当前版本：**`0.9.8-1.20.1`**
- 开发阶段：**v0.9 M2 结案评价与复盘**（M2）
- 技术基线：Minecraft **1.20.1** · Forge **47.4.20** · Java **17**
- 最后更新：**2026-07-18 21:50:00 UTC+01:00**（`2026-07-18T20:50:00Z`）
<!-- project-status:end -->

## 里程碑

<!-- roadmap:start -->
> 设计基线：**v0.9** · 当前里程碑：**M2**

| 里程碑 | 阶段 | 状态 | 目标 |
|---|---|---|---|
| M0 | Content foundation | 已完成 | Schema v4、内容关系图、生成器、迁移与回滚工具、来源审计及 CI 门禁。 |
| M1 | Core vertical slice | 已完成 | 占卜家 9–7、魔药、扮演 v2、特性守恒、新手营地与可生活的雾都街区闭环。 |
| M2 | Investigation and life | 进行中 | 把现有雾都前哨、委托和持久队伍资产扩展为正式报社、事务所、警局、经济与动态案件。 |
| M3 | Five launch pathways | 规划 | 占卜家、观众、猎人、偷盗者和学徒序列 9–5 的差异化闭环。 |
| M4 | Organizations and artifacts | 规划 | 七教会框架、五个隐秘组织和 24 个已验证封印物。 |
| M5 | Spirit world and dreams | 规划 | 灵界航路、共享梦境、12 种生态生物和 6 种异常天气。 |
| M6 | Second pathway wave | 规划 | 水手、不眠者、收尸人和歌颂者序列 9–5。 |
| M7 | Production and compatibility | 规划 | 十个工位及 JEI、EMI、Curios、Create、Farmer's Delight 和 Jade 兼容。 |
| M8 | Third pathway wave | 远期 | 战士、秘祈人、阅读者、刺客、耕种者和药师序列 9–5。 |
| M9 | Complete 22 pathways | 远期 | 补齐 v0.9 规格中的剩余七条途径序列 9–5。 |
| M10 | Demigods | 远期 | 序列 4、知识灼伤、39+ 模型和锚系统。 |
| M11 | World and seasons | 远期 | 40+ 结构类别、28 个世界事件、活历史和赛季结算。 |
| M12 | Experimental endgame | 远期 | 可完全关闭的序列 3–0、祈祷、唯一性与神位战争实验模块。 |

> 门禁规则：设计规格、data_ready、code_ready、asset_ready、playable 与 verified 分开统计；完成自动化实施门禁不等于完成全部真人平衡与公开发布验证。
<!-- roadmap:end -->

## M0 完成证据

| 能力 | 当前证据 | 状态 |
|---|---|---|
| v0.9 来源 | 完整文档、增量说明、manifest 与内嵌 v0.8 哈希精确校验 | verified |
| schema v4 | 途径、15 序列、3 委托、3 任务链和 M0 目录 | code_ready |
| 内容图 | 114 节点、160 关系、6 类报告 | verified |
| 玩家迁移 | schema 0/15/16/17→18 DataFix、世界快照、精确恢复、原始 NBT 备份与孤儿保留 | verified |
| 状态同步 | 四区 dirty mask、生命周期核心摘要、5 秒校正、协议 11 与调查/证据/推理快照 | code_ready |
| 生命周期回归 | 229 JUnit、6 Forge GameTest、隔离回滚与两次专服启动矩阵 | verified automation |
| M2 调查板 | 分页案件、三案证据档案、12 条关系、三阶段推理、下一步、关键物品恢复与服务端邻近校验 | code_ready |
| M2 结案复盘 | 四维 100 分评价、S–D 评级、耗时/路线、改进建议与 schema 18 持久化 | code_ready |
| M2 城市报纸 | 六类确定性头条、案件简报、轮班状态、余额与报社产出 | code_ready |
| 非凡特性 | 分层序列、纯度、印记、污染、来源哈希与永久失控精确掉落 | playable |
| 扮演 v2 | 身份卡、反思日志、原则理解、过度认同、每日反思与 UI | verified |
| 既有玩法 | 五途径 9–7、3 委托、3 任务链、持久队伍 | playable migration assets |
| 构建门禁 | 设计源、生成器、内容图、合同、资源、Gradle、回滚与双启动专服 | verified automation |

## M0/M1 完成结论

- M0：114 节点内容图、schema 18 迁移链、精确回滚、来源审计和 CI 门禁完成。
- M1：占卜家 9–7 两小时纵切、9 项目标、7 个里程碑、扮演 v2、身份/反思、特性死亡守恒与城市工作闭环完成。
- 真人死亡/重生、维度往返、断线重连、两小时手感和多人长时测试继续记录为发布质量证据。
- 所有后续内容必须继续保持来源、剧透、知识门槛、关系、翻译和资产证据完整。

## 后续迁移边界

- M1：进入回归维护，不再新增阻塞 M2 的基础门禁。
- M2：当前进行中；调查板、三案证据、关联推理、案件恢复、结案评级、每日报纸和首批城市服务已完成，继续建设正式城市街区、经济、动态案件和四人八小时无锁死验收。
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
python scripts/check_save_rollback.py
python scripts/sync_project_metadata.py --check
./gradlew clean build
./gradlew runGameTestServer
python scripts/run_server_restart_matrix.py --timeout 180
```

相关文档：

- [v0.9 实施基线](V0.9-Implementation-Baseline)
- [M0 内容图与迁移](V0.9-M0-Content-Graph-and-Migration)
- [v0.9 差异报告](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/docs/V0.9_DIFFERENCE_REPORT.md)
- [代码审计表](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/docs/code_audit.csv)
- [完整路线](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/ROADMAP.md)
