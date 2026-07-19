# M2 八槽位动态案件轮换

> 当前版本：`0.9.11-1.20.1` · Capability schema 19 · 网络协议 12
> 最后更新：2026-07-19 10:17:00 UTC+01:00

完成 `真假配方` 后，雾都调查板会解锁每日可重复的 `雾都轮换调查`。服务器按世界种子与接案时间稳定生成案件；跨午夜、重连和服务器重启不会改变已接案件。

## 今日案件

```text
/pm case rotation
```

轮换覆盖失踪者、异常物品、神秘犯罪三类案件。每份卷宗包含主体、动机、手段、地点、异常、掩饰、受害影响和关键物证八个槽位；异常、动机、手段与掩饰会随取证和复议逐步揭示，预览不会提前泄底。

接案时会自动获得绑定当前案件实例的“轮换案件证物袋”。悬浮提示显示案件编号、关键物证与三轮归档进度；三项证据齐全后证物袋发光。旧案件证物袋不能推进新轮换。

## 三轮调查

```text
/pm case rotation investigate field
/pm case rotation investigate desk
```

| 阶段 | 推荐直接交互 | `field` 兼容入口 | 调查板替代入口 |
|---|---|---|---|
| 现场 | 带证物袋到卷宗地点，右键现场方块 | 到卷宗地点 28 格内 | 复原现场 |
| 证人 | 右键指定报社职员/鉴定师/联络员 | 到指定 NPC 10 格内 | 复原离线口供 |
| 档案 | 右键当日报纸 | 持有报纸 | 调取备份 |

调查板替代入口收取 6 便士；余额不足时增加 3 点压力但不会卡死。三条真线索与一条植入式误导会进入证据板，并揭示五个稳定关系 ID。

证物袋遗失时在真实调查板附近使用 `/pm case recover`，会按服务端进度恢复当前阶段且不会重复发放。2–4 人在线队员的证物袋阶段随共享任务同步；中途 `/pm party sync` 或离线追赶成功会自动取得当前阶段证物袋，结案或放弃时自动回收。

## 结论与恢复

```text
/pm case rotation conclude human_concealment
/pm case rotation conclude extraordinary_distortion
/pm case rotation conclude ritual_diversion
```

错误结论增加 6 点压力并进入 `reconsider`。它不会清空进度，也不能靠连续穷举绕过：

```text
/pm case rotation recover
# 或 /pm case recover
```

在真实调查板附近恢复后，伪证会被确认成矛盾，恢复 2 点压力，并允许再次提交结论。正确结论后返回调查板结算 60 便士和 4 点事务所声望。

## 多人与边界

- 2–4 人持久队伍共享步骤、接案 tick、结论和恢复状态。
- 费用、压力与玩家假说保持个人责任。
- NPC 死亡、成员离线、报纸遗失或结构暂未加载时都能走调查板替代入口。
- 本版没有新增存档字段或网络包；schema 19、协议 12 和 14 个消息 ID 保持稳定。

当前已完成动态案件的生成、实体证物袋、现场采样、NPC 证词、报纸档案、替代入口、误判恢复和结算闭环；案件专属主体/受害者、差异化物证模型、正式城市街区、周级案件池和四人八小时并发矩阵继续建设。

完整生成规则、证据关系和验收清单见[仓库开发者文档](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/docs/M2_DYNAMIC_CASE_ROTATION.md)。

继续阅读：[调查板](M2-Investigation-Board) · [证据关联与恢复](M2-Evidence-Reasoning-and-Recovery) · [玩家假说](M2-Player-Hypotheses-and-Reasoning-Strain) · [持久队伍](M2-Persistent-Party-Recovery)
