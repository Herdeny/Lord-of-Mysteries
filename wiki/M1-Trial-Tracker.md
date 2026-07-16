# M1 一小时试炼追踪器

> 当前版本：0.8.5-1.20.1 · v0.8 M1 验收 / M2 预研

## 命令

| 命令 | 作用 |
|---|---|
| `/pm doctor` | 修复玩家数据并检查定义、协议和世界目标 |
| `/pm trial start` | 开始新记录；有暂停记录时继续 |
| `/pm trial resume` | 明确恢复暂停记录 |
| `/pm trial status` | 查看六项目标、四项连续性与附加统计 |
| `/pm trial verify` | 汇总核心 6/6 与连续性 4/4 |
| `/pm trial stop` | 停止计时并保留记录 |
| `/pm trial reset` | 清空全部试炼记录 |

命令无需管理员权限。试炼数据写入玩家 Capability schema 13，死亡、跨维度、重登和服务器重启后保留；
`stop` 后离线时间不计入，只有 `reset` 会清空。

## 自动目标

1. 累计记录 72,000 tick，即一小时。
2. 进入世界种子确定的调查营地 24 格范围。
3. 在占卜家途径抵达序列 7。
4. 击杀至少 3 个特殊生物。
5. 完成至少 2 次正收益扮演事件。
6. 让压力或污染峰值达到至少 25。

额外记录死亡次数、完整睡眠恢复次数和精神守护符消耗次数，供平衡分析使用。

## 连续性门禁

1. 退出重连至少 1 次。
2. Forge 专用服务器保存并重启至少 1 次。
3. 跨维度变化至少 2 次，形成往返。
4. 死亡并恢复玩家数据至少 1 次。

## 反制检查

- 灵视提灯强制幻形蛇显形 10 秒。
- 永燃火柴盒对灵体微光和占卜家失控体造成双倍伤害。
- 灰烬傀儡在水、雨或气泡中持续受到侵蚀。

## 结论边界

`/pm trial verify` 显示 6/6 + 4/4 只代表机器可核验目标完成。材料耗时、
数值平衡、复制风险和客户端越权仍需按
[M1 验收清单](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/docs/M1_ACCEPTANCE_CHECKLIST.md)
人工验证。未完成真实记录前，M1 保持 `active`。

专服和多人场景见 [专用服务器与多人验证](Dedicated-Server-Test-Matrix)。

完整技术说明：
[docs/M1_TRIAL_TRACKER.md](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/docs/M1_TRIAL_TRACKER.md)。
