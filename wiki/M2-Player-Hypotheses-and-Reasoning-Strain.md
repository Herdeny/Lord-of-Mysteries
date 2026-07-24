# M2 玩家假说与误判恢复

> 当前版本：0.9.15-1.20.1 · Capability schema 20 · 网络协议 12

玩家现在可以把自己的解释绑定到**已经揭示**的证据关系，并通过调查板检验。错误会产生可见、可恢复的代价，不会复制奖励或永久锁死案件。

## 怎么使用

1. 在调查板关联推理页查看关系行的 `#关系ID`，或输入 `/pm case hypothesis`。
2. 靠近调查板提出假说：
   - `/pm case hypothesis propose supports <关系ID> <说明>`
   - `/pm case hypothesis propose contradicts <关系ID> <说明>`
   - `/pm case hypothesis propose leads_to <关系ID> <说明>`
3. 点击“检验假说”或输入 `/pm case hypothesis test`。
4. 若误判，按新证据提出正确立场再检验；也可等待 30 秒后点击“复议”或输入 `/pm case hypothesis reconsider`。
5. `/pm case hypothesis clear` 只清草稿，不消除既有负担。

## 规则

| 项目 | 规则 |
|---|---|
| 说明长度 | 最多 160 字，服务端清理格式符、控制字符和多余空白 |
| 可引用关系 | 仅当前案件已经揭示的关系 |
| 错误检验 | 压力 +4，未解决推理负担 +1，负担最多 3 |
| 正确检验 | 恢复 1 点已有负担 |
| 检验冷却 | 10 秒 |
| 复议冷却 | 距上次检验 30 秒，恢复 1 点并清草稿 |
| 结案影响 | 每点未解决负担扣 4 点程序分，最多扣 12 点 |
| 奖励影响 | 不修改便士、声望、知识、物品或任务奖励 |

## 安全与兼容

- 提出、检验、复议和清除都要求真实调查板邻近；客户端只发送操作意图。
- 未揭示关系不会进入快照，猜测 ID 也会被拒绝。
- schema 18 旧档自动迁移到 19 并获得空假说工作区，无需清档。
- 协议升至 12，但 14 个消息 ID 不重排；旧客户端会被严格拒绝。
- 282 项 JUnit、6 项 Forge GameTest、M2 合同、回滚与双启动专服矩阵保护该流程。

完整关系 ID、迁移和验收说明见[开发者手册](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/docs/M2_PLAYER_HYPOTHESES_AND_REASONING_STRAIN.md)。

继续阅读：[调查板](M2-Investigation-Board) · [证据关联与恢复](M2-Evidence-Reasoning-and-Recovery) · [结案评价](M2-Case-Debrief-and-Ratings)
