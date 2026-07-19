# M2 玩家假说与误判恢复

> 适用版本：`0.9.12-1.20.1` · Minecraft 1.20.1 · Capability schema 19 · 网络协议 12
>
> 定位：让玩家对已经发现的证据关系写出自己的解释、承担可见误判代价，并在结案前通过纠正或复议恢复；不会开放未发现证据，也不会把自然语言交给客户端裁决。

## 最短玩法流程

1. 在雾都调查板接取任一正式案件，并收集至少两端都已发现的证据关系。
2. 输入 `/pm case hypothesis`，查看当前案件可用的稳定关系 ID。
3. 站在真实调查板附近提出假说，例如：
   - `/pm case hypothesis propose supports tracks_to_recovery 足迹与发现位置属于同一路线`
   - `/pm case hypothesis propose contradicts appraisal_to_ink 墨迹与正规鉴定规范冲突`
   - `/pm case hypothesis propose leads_to departure_to_camp 离站记录指向最后营地`
4. 在调查板关联推理页点击“检验假说”，或输入 `/pm case hypothesis test`。
5. 若立场与服务端关系类型一致，假说得到支持并恢复 1 点既有推理负担；若不一致，压力增加 4 点、推理负担增加 1 点。
6. 错误后可以按新证据提出正确立场再检验；也可等待 30 秒后在调查板点击“复议”，清除草稿并恢复 1 点负担。
7. 结案前尽量把未解决推理负担降至 0，再使用 `/pm case archive` 与 `/pm case debrief <案件>` 查看评价。

## 命令

| 命令 | 用途 | 调查板要求 |
|---|---|---|
| `/pm case hypothesis` | 查看当前草稿、状态、负担、统计与已揭示关系 ID | 无 |
| `/pm case hypothesis propose supports <关系ID> <说明>` | 提出“该解释支持关系”的假说 | 必须靠近 |
| `/pm case hypothesis propose contradicts <关系ID> <说明>` | 提出“该解释与关系矛盾”的假说 | 必须靠近 |
| `/pm case hypothesis propose leads_to <关系ID> <说明>` | 提出“该解释指向下一线索”的假说 | 必须靠近 |
| `/pm case hypothesis test` | 用服务端真实关系类型检验当前草稿 | 必须靠近 |
| `/pm case hypothesis reconsider` | 复议一次错误，清草稿并恢复 1 点负担 | 必须靠近 |
| `/pm case hypothesis clear` | 仅清除草稿，不恢复已有负担 | 必须靠近 |

说明最多 160 个字符。格式控制符、控制字符和多余空白会在服务端清理；空说明、未知关系和未揭示关系会被拒绝。

## 稳定关系 ID

关系只在两端证据都已发现后出现。以下 ID 是当前三个正式案件的稳定引用：

| 案件 | 关系 ID | 服务端类型 |
|---|---|---|
| 走失的煤球 | `record_to_tracks` | `leads_to` |
| 走失的煤球 | `tracks_to_recovery` | `supports` |
| 失踪调查小队 | `departure_to_camp` | `leads_to` |
| 失踪调查小队 | `camp_to_notes` | `supports` |
| 失踪调查小队 | `notes_to_authorization` | `leads_to` |
| 失踪调查小队 | `notes_to_testimony` | `supports` |
| 失踪调查小队 | `testimony_to_defense` | `supports` |
| 真假配方 | `registry_to_dossier` | `supports` |
| 真假配方 | `appraisal_to_watermark` | 由卷宗鉴定结果决定 `supports` / `contradicts` |
| 真假配方 | `appraisal_to_ink` | 由卷宗鉴定结果决定 `supports` / `contradicts` |
| 真假配方 | `appraisal_to_order` | 由卷宗鉴定结果决定 `supports` / `contradicts` |
| 真假配方 | `clue_synthesis` | 由三项综合鉴定决定 `supports` / `contradicts` |

`/pm case hypothesis` 只显示当前已经揭示的子集。表格用于理解稳定接口，不代表玩家可跳过取证直接引用尚未发送的关系。

## 代价、冷却与恢复

| 行为 | 结果 | 冷却 |
|---|---|---:|
| 正确检验 | 状态变为“已支持”；成功计数 +1；已有负担 -1 | 10 秒检验冷却 |
| 错误检验 | 状态变为“已驳回”；压力 +4；负担 +1，最多 3；误判计数 +1 | 10 秒检验冷却 |
| 重新提出 | 替换当前草稿，不直接改变负担 | 无额外收益 |
| 复议 | 清除草稿；负担 -1 | 距上次检验 30 秒 |
| 清除草稿 | 只清草稿，负担保持 | 无恢复 |

推理负担是一种案件内程序责任，不是永久角色惩罚。它不会阻止继续调查、提交任务或获得基础报酬，但会在结案评价中反映未纠正的错误。

## 结案评价

- 每点未解决推理负担扣除 4 点程序分，最多扣 12 点。
- 真假配方错误 `authentic / forged` 判断仍按原规则每次扣 5 点程序分，最多扣 15 点；两类代价分别记录。
- 程序分最低钳制为 0；总分与 S–D 评级继续使用既有公式。
- 正确检验或复议发生在结案前即可恢复负担，因此错误不会永久毁档。
- 假说系统不增加或扣除便士、声望、知识、物品和任务奖励。

## 调查板界面

关联推理页现在始终保留“玩家假说”区域：

- 无草稿时提示使用 `/pm case hypothesis`；关系行显示 `#关系ID`。
- 有草稿时显示关系 ID、立场、草稿/支持/驳回状态、负担和清理后的说明。
- “检验假说”只在存在草稿时显示；“复议”只在存在未解决负担时显示。
- 按钮发送的仍是操作意图。服务端重新检查真实调查板、活动案件、关系可见性和冷却后才修改状态。

## 存档与迁移

- `PlayerMysteryData` schema 19 新增按案件 ID 索引的 `case_hypotheses`。
- schema 18 通过命名 DataFix `case_hypothesis_workspace` 自动创建空工作区。
- 草稿关系、立场、说明、状态、未解决负担、成功/失败次数与最后检验时间均持久化。
- 重连、死亡、跨维度和服务器重启后状态保持；复制玩家 Capability 时同步工作区。
- 非法案件 ID、错误容器或损坏记录进入 `orphaned_entries`，不会阻止其余存档加载。
- 未来 schema 仍遵守隔离与原始 NBT 备份规则，不由旧版本静默降级。

## 服务端安全边界

- 客户端不会收到未揭示关系，也不能提交“真实关系类型”。
- 提出、检验、复议和清除都要求真实调查板水平 6 格、垂直 4 格邻近。
- 每次操作重新验证活动案件、稳定关系 ID、当前可见关系和冷却。
- 网络协议从 11 升至 12，消息数量仍为 14，discriminator 0–13 不重排；旧协议客户端被严格拒绝。
- 文本、计数、枚举和 S2C 快照都有界；关系不存在或已经因进度变化而失效时拒绝检验。

## 自动化验收

- 260 项 JUnit 覆盖文本消毒、NBT 往返、错误检验、纠正恢复、动态案件伪证复议、差异化样本/现场布局/跨日实例、复议冷却、非法载荷、协议快照、结案扣分和 schema 18→19 迁移。
- `scripts/check_m2_investigation.py` 固定三种立场、三种状态、160 字上限、3 点负担、4 点压力、10/30 秒冷却、结案扣分、调查板邻近与零奖励修改。
- `scripts/check_resource_integrity.py` 校验 273 个 JSON、1159 对双语键、462 个静态引用、80 个模型和全部注册资源。
- 完整发布继续执行 6 项 Forge GameTest、迁移回滚和同一世界双启动专服矩阵。

## 当前边界与下一步

- 当前不是自由拖拽节点编辑器；玩家把说明绑定到服务端已经揭示的关系，而不是创建新的证据边。
- 当前不做自然语言语义判定；可验证结果来自稳定立场与服务端关系类型，避免语言差异和客户端作弊。
- 当前每个案件保存一个最新工作区，不保存无限假说历史。
- 三类八槽位动态案件模板已提供稳定关系 ID 并复用恢复与结案契约；下一批继续建设真实 NPC/物证/现场互动、正式城市街区、长期经济和四人八小时真人负载验收。

关联说明：

- [调查板玩家手册](M2_INVESTIGATION_BOARD.md)
- [证据关联、案件分析与恢复](M2_EVIDENCE_REASONING_AND_RECOVERY.md)
- [结案评价与复盘](M2_CASE_DEBRIEF_AND_RATINGS.md)
- [M2 执行计划](M2_PLAN.md)
