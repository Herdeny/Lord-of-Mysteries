# 专用服务器与多人一致性验证

> 当前版本：0.9.3-1.20.1 · Capability schema 17 · 内容 schema v4 · 网络协议 8

## 自动冒烟

```bash
python scripts/check_save_rollback.py
python scripts/run_server_restart_matrix.py --timeout 180
```

回滚脚本在隔离世界中验证 dry-run、完整恢复、快照后文件清除、恢复前安全备份与路径穿越拒绝。
重启矩阵连续启动 Forge 专用服务器两次，确认 3 个委托、3 条任务链完成加载并进入 `Done`，随后执行
`pm servercheck`、`list` 和 `save-all flush`；第二次必须保留相同世界种子与 schema 17 marker。Build 工作流会执行同一检查。

`./gradlew check` 还会运行 `scripts/check_m1_playability.py`，核对 120 分钟、9 项核心目标、7 个里程碑、三份 M1 魔药、
十二项保底补给、十个关键命令入口和中英文本地化；M2 合同保护真假配方、三解、持久队伍恢复与旧 13 步索引；
统一门禁保护 v0.9 设计源、内容图、JSON、双语、模型与注册资源；当前自动化基线为 202 项 JUnit、5 项真实 Forge GameTest，并验证 schema 17 首启迁移快照、Capability Clone、精确特性载荷和两小时状态往返。

`./gradlew runGameTestServer` 必须显示 5 项测试实际运行并全部通过；报告 `0 tests` 不视为有效门禁。

## M1 连续性

开测先执行 `/pm doctor`，再用 `/pm trial reset` 与 `/pm trial start` 创建干净记录。
`stop` 只结算在线时长，`resume` 或再次 `start` 会恢复保留记录。

测试过程中用 `/pm trial report` 对照营地 10 分钟、序列 9/8/7 为 30/60/90 分钟、身份/反思/城市生活为 100/110/120 分钟的阶段目标，
并记录首次特殊生物击杀、首次正收益扮演和首次 25 风险峰值。

`/pm trial verify` 要求：

- 九项核心目标 9/9：两小时、营地、序列 7、三次狩猎、两次有效扮演、风险峰值、身份绑定、反思和城市生活。
- 四项连续性 4/4：至少一次重连、一次专服重启、两次维度变化和一次死亡恢复。

机器证据不能替代真实两小时材料、战斗与数值平衡记录。

## M2 多人基础

- 同一原版记分板队伍中的 2–4 名玩家可共享 `shared_progress` 任务目标并写入持久账本。
- 记者护送与夜袭按 UUID 选出唯一在线协调者；波次、记者和路线状态持久化。
- 不同队伍互不共享；超过 `max_party=4` 时安全降级为单人。
- 已登记离线成员上线自动追赶，中途加入使用 `/pm party sync`；每名成员独立结算。
- 换队/退队自动清理与超员结算已实现；正式队伍 GUI、管理员离线改队/队名复用人工矩阵和五途径多人负载矩阵尚未完成。

完整场景、网络重放检查和证据模板见
[`docs/DEDICATED_SERVER_TEST_MATRIX.md`](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/docs/DEDICATED_SERVER_TEST_MATRIX.md)。
