# 设计基线

## 当前来源

- 设计版本：Project Mystery v0.9
- 最近同步：2026-07-17
- 完整设计：`docs/Project_Mystery_Design_Doc_v0_9.md`
- 增量说明：`docs/Project_Mystery_v0_9_Incremental_Additions.md`
- 导入清单：`docs/Project_Mystery_v0_9_manifest.json`
- 工程基线：Minecraft 1.20.1、Forge 47.4.20、Java 17
- 玩家 Capability：schema 16
- 内容定义：schema v4
- SimpleChannel：协议 8

`scripts/import_v09_design.py --check` 会核对完整文档、增量文档、内嵌 v0.8 基线的字节数、行数和 SHA-256，防止设计源在导入后被静默改写。仓库现有可运行行为始终优先于设计文档；文档中的示例包名、未注册 ID、其他加载器代码和概念伪代码不得直接复制，必须转换到 `lord_of_mysteries` 命名空间与当前 Forge 架构。

## v0.9 相对 v0.8 的核心变化

1. 路线从 M0–M7 重构为 M0–M12，先完成内容基础设施和纵切迁移，再分批扩展 22 条途径。
2. 内容定义统一采用 schema v4，强制记录来源层级/引用、正典状态、剧透级别、知识门槛、关系和实现状态。
3. 引入内容关系图、孤儿数据、剧透泄漏、兼容性、本地化与资产审计；设计规格数量不再等同于实现数量。
4. 玩家数据要求可迁移、可备份、可保留未知 ID，并为脏字段、DataFix 和 GameTest 留出正式门禁。
5. 非凡特性改为分层 `CharacteristicBundle`，记录序列、纯度、印记、污染和来源哈希，作为后续提取、掉落与守恒的基础。
6. 扮演法升级为 v2：区分原则理解与角色过度认同，加入反思机制，不再只累计消化度。
7. 首发体验改为两小时纵切目标；调查、生活、案件、城市街区和知识门控成为 M1/M2 的硬验收内容。
8. JEI、EMI、Jade 等兼容首先只读展示；高序列、祈祷、唯一性和神位战争保持可完全关闭的实验模块。

完整差异见 `docs/V0.9_DIFFERENCE_REPORT.md`。

## 本批已落实

- 精确导入 v0.9 三件套，并把设计源校验接入 Gradle、Build CI 与 Documentation Consistency。
- `ContentMetadata` 在途径、15 个现有序列、3 个委托和 3 条任务链中解析并验证 schema v4 元数据。
- `scripts/build_content_graph.py` 建立 71 节点、63 关系的当前实现图，并输出六类机器可读审计报告。
- `docs/master/m0_content_catalog.json` 建立首批 M0 迁移目录，覆盖物品、状态、配方和知识门槛。
- Capability schema 由 15 升至 16；旧档自动回填特性 bundle，Provider 生命周期补齐失效监听。
- 0.9.1 增加 schema 0/15→16 正式 DataFix、世界加载前相关文件快照、原始 NBT 回滚载荷、迁移历史与未知/非法数据孤儿保留。
- 0.9.2 增加 Core/Knowledge/Social/Endgame 四区 dirty mask、生命周期核心摘要、客户端只读缓存和 3 项 Forge GameTest。
- 五条现有途径的 15 次序列 9–7 晋升都会记录特性层；永久失控暂保留 bundle，等待 M1 提取/掉落实体闭环。
- 扮演事件写入原则理解和角色过度认同；`/pm reflect` 每游戏日可反思一次，并在状态包与界面显示。
- 网络协议由 7 升至 8；12 个固定消息 ID、202 项 JUnit、3 项 Forge GameTest 与专服烟测组成分层门禁。

## 事实边界

- 当前关系图是 **71 节点 / 63 关系**，未达到 M0 的 100 节点退出门槛。
- 当前已存在五途径序列 9–7、三委托与三任务链，但它们是 v0.9 的迁移资产，不代表 M1、M2 或 M3 完成。
- 设计文档中的 180+ 物品、75 魔药配方、49+ 模型、48 世界事件等是设计规格，不是仓库完成度。
- `CharacteristicBundle` 已持久化，但实体化提取、死亡掉落、重复特性合并和完整守恒审计尚未完成。
- 扮演 v2 已具备身份区分与反思基础，但身份卡、反思日志物品、四阶段验证和两小时真人验收尚未完成。
- 兼容报告目前只声明 JEI、EMI 与 Jade 的只读计划，没有运行时集成。
- 序列 3–1 和终局模块默认不进入当前实现承诺。

## 路线映射

- M0：当前 `active`。完成 schema v4、内容图、迁移框架、来源审计与 CI 门禁。
- M1：占卜家 9–7 两小时核心纵切、扮演 v2、特性守恒和可生活城市街区。
- M2：报社、事务所、警局、调查板、经济和三个动态案件。
- M3–M9：按五条、四条、六条、七条的波次完成 22 途径序列 9–5。
- M10–M11：半神、锚、世界事件、活历史和赛季。
- M12：可完全关闭的实验终局。

路线数据唯一来源是 `roadmap.json`。实现状态必须使用 `data_ready`、`code_ready`、`asset_ready`、`playable` 和 `verified` 分层陈述；当前里程碑退出条件未满足前，不得宣称后续里程碑完成。
