# 专用服务器与多人一致性验证

> 当前版本：0.8.7-1.20.1 · Capability schema 15 · 网络协议 6

## 自动冒烟

```bash
python scripts/run_server_smoke.py --timeout 180
```

脚本实际启动 Forge 专用服务器，确认 3 个委托、3 条任务链完成加载并进入 `Done`，随后执行
`pm servercheck`、`list` 和 `save-all flush`。只有运行诊断、命令循环、世界强制保存均成功且未发现
服务端线程致命错误时才发送 `stop`，并要求进程以 0 退出。Build 工作流会在完整构建后执行同一检查。

`./gradlew check` 还会运行 `scripts/check_m1_playability.py`，核对 60 分钟阶段目标、三份 M1 魔药、
十二项保底补给、十个关键命令入口和中英文本地化；M2 合同保护真假配方、三解与旧 13 步索引；当前自动化基线为 166 项测试。

## M1 连续性

开测先执行 `/pm doctor`，再用 `/pm trial reset` 与 `/pm trial start` 创建干净记录。
`stop` 只结算在线时长，`resume` 或再次 `start` 会恢复保留记录。

测试过程中用 `/pm trial report` 对照营地 10 分钟、序列 9/8/7 为 25/45/60 分钟的阶段目标，
并记录首次特殊生物击杀、首次正收益扮演和首次 25 风险峰值。

`/pm trial verify` 要求：

- 六项核心目标 6/6：一小时、营地、序列 7、三次狩猎、两次有效扮演、风险峰值。
- 四项连续性 4/4：至少一次重连、一次专服重启、两次维度变化和一次死亡恢复。

机器证据不能替代真实一小时材料、战斗与数值平衡记录。

## M2 多人基础

- 同一原版记分板队伍中的 2–4 名在线玩家可共享 `shared_progress` 任务目标。
- 记者护送与夜袭按 UUID 选出唯一协调者；波次状态在在线队员间同步。
- 不同队伍互不共享；超过 `max_party=4` 时安全降级为单人。
- 离线成员追赶、正式队伍 UI 和五途径多人负载矩阵尚未完成。

完整场景、网络重放检查和证据模板见
[`docs/DEDICATED_SERVER_TEST_MATRIX.md`](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/docs/DEDICATED_SERVER_TEST_MATRIX.md)。
