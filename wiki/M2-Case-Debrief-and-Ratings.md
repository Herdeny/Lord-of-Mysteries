# M2 结案评价与复盘

> 当前版本：0.9.10-1.20.1 · Capability schema 19 · 网络协议 12
>
> 最后更新：2026-07-18 22:55:14 UTC+01:00

三个正式案件现在会在结算时自动生成持久化复盘：总分、S–D 评级、四项分数、耗时、结案路线和唯一改进建议。评分只用于学习和回顾，不会增加或扣除原有报酬。

## 怎么查看

- 完成案件：结算消息后自动显示复盘。
- `/pm case archive`：查看已完成案件、评级和平均分。
- `/pm case debrief`：查看档案摘要。
- `/pm case debrief lost_cat`：走失的煤球。
- `/pm case debrief missing_squad`：失踪调查小队。
- `/pm case debrief counterfeit_formula`：真假配方。

## 评分规则

| 维度 | 上限 | 说明 |
|---|---:|---|
| 证据 | 40 | 已发现证据占全部证据的比例 |
| 程序 | 30 | 档案是否足以结论；真假配方错误判断和未解决推理负担会扣分 |
| 安全 | 20 | 结案时的失控压力与污染 |
| 效率 | 10 | 从接案到结案的游戏内耗时 |

评级：S ≥ 90，A ≥ 80，B ≥ 65，C ≥ 50，其余为 D。

走失的煤球、真假配方和失踪调查小队的基准用时分别为 20、30、60 分钟。超时不锁任务，只降低效率分。失踪小队会保存强攻、潜入或占卜路线；普通线记录为标准调查。

## 真假配方保护

错误提交 authentic/forged 每次扣 5 点程序分，最多扣 15 点。系统先从卷宗记录三项鉴定证据和错误次数，写入复盘后才回收卷宗，避免结案数据丢失。

## 玩家假说责任

- 错误检验增加 4 点压力和 1 点推理负担，负担最多 3 点。
- 每点结案时未解决的负担扣 4 点程序分，最多扣 12 点。
- 用正确立场重新检验或等待 30 秒后在调查板复议，可恢复 1 点负担。
- 评分仍不修改任何基础奖励；完整规则见[玩家假说与误判恢复](M2-Player-Hypotheses-and-Reasoning-Strain)。

## 旧档兼容

- Capability schema 17 通过 `case_debrief_archive` 迁至 18，schema 18 再通过 `case_hypothesis_workspace` 迁至 19。
- 旧档已完成案件显示“历史存档未评级”，不伪造结果、不要求清档。
- 非法或损坏记录进入孤儿数据，不影响其余存档。
- 复盘在重连、死亡、维度切换和服务器重启后保留。

## 安全边界

- 评分只在服务端确认可结算后生成。
- 客户端不能提交分数、路线、耗时或评级。
- 网络协议为 12，14 个消息 ID 不变。
- 评分不改变便士、声望、物品和知识奖励。

完整实现、分数公式、迁移和自动化说明见[仓库文档](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/docs/M2_CASE_DEBRIEF_AND_RATINGS.md)。

继续阅读：[调查板玩家手册](M2-Investigation-Board) · [证据关联与恢复](M2-Evidence-Reasoning-and-Recovery) · [玩家假说与误判恢复](M2-Player-Hypotheses-and-Reasoning-Strain) · [开发状态](Development-Status)
