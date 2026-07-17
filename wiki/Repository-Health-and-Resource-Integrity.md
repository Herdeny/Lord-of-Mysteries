# 仓库健康与资源完整性

> 当前版本：0.9.2-1.20.1 · Capability schema 16 · 内容 schema v4 · 网络协议 8

0.8.9 对现有代码、存档、资源和 CI 做了全面稳定性审计。重点修复换队/退队残留、队伍同步重复结算、
超限成员污染进度、损坏账本恢复和任务计数溢出，并统一委托/任务链运行时与生成期校验。

构建脚本已移除旧 Maven DSL；Gradle `--warning-mode all` 中剩余的 `downloadMcpConfig` / `reobfJar`
弃用提示来自 ForgeGradle 6 上游内部实现，不影响当前 Gradle 8.14.5 构建。

## 资源门禁

`python scripts/check_resource_integrity.py` 当前检查：

- 261 个可解析 JSON；
- 764 个中英成对翻译键和 330 个静态引用键；
- 73 个模型；
- 68 个物品、5 个方块、8 个实体的名称与资源。

模型/纹理断链、双语键漂移、注册资源缺失或 JSON 损坏都会令 Build 与 Documentation Consistency 失败。

## 队伍与存档

- 换队或退队会移除旧账本成员关系，不删除个人任务进度。
- 放弃和结算不依赖当前队伍规模，5 人降级期间也能正确注销。
- 已完成不可重复委托不能通过 `/pm party sync` 重复领奖。
- 只有已登记、未结算成员能推进账本；无效键、UUID、步骤和完成账本会安全清理。
- `/pm servercheck` 输出 `active_parties` 和 `party_members` 供专服诊断。

## 验证

自动化基线为 202 项 JUnit、3 项 Forge GameTest、v0.9 设计源/内容图、schema 16 迁移备份与孤儿保留、四区 dirty mask、M1/M2 合同、资源完整性、Forge clean build 和真实专服
启动—诊断—保存—停服烟测。M1 一小时手感、自动重启/回滚、真人跨维度、真实多人负载、队名复用与正式队伍 GUI 仍需人工验收。

完整技术报告：
[`docs/REPOSITORY_HEALTH_AND_RESOURCE_INTEGRITY.md`](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/docs/REPOSITORY_HEALTH_AND_RESOURCE_INTEGRITY.md)。
