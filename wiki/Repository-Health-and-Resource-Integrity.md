# 仓库健康与资源完整性

> 当前版本：0.9.31-1.20.1 · Capability schema 30 · 内容 schema v4 · 网络协议 13

0.8.9 对现有代码、存档、资源和 CI 做了全面稳定性审计。重点修复换队/退队残留、队伍同步重复结算、
超限成员污染进度、损坏账本恢复和任务计数溢出，并统一委托/任务链运行时与生成期校验。

构建脚本已移除旧 Maven DSL；Gradle `--warning-mode all` 中剩余的 `downloadMcpConfig` / `reobfJar`
弃用提示来自 ForgeGradle 6 上游内部实现，不影响当前 Gradle 8.14.5 构建。

## 资源门禁

`python scripts/check_resource_integrity.py` 当前检查：

- 389 个可解析 JSON；
- 1741 个中英成对翻译键和 713 个静态引用键；
- 发布资源过滤与 Forge 开发运行配置固定使用 UTF-8，Mod 简介、控制台和 `latest.log` 不再受 Windows 系统代码页影响；专服冒烟严格拒绝解码错误与乱码；
- 122 个模型；
- 115 个物品、7 个方块、16 个实体的名称与资源；
- 12 份组织与 7 份封印物定义进入 Pages 生成和内容关系图；
- 直接注册、`simple` 与 `ManagedArtifactKind` 辅助注册统一统计；
- 所有有序配方必须在 3×3 内且每行宽度一致。

模型/纹理断链、双语键漂移、注册资源缺失或 JSON 损坏都会令 Build 与 Documentation Consistency 失败。

## 队伍与存档

- 换队或退队会移除旧账本成员关系，不删除个人任务进度。
- 放弃和结算不依赖当前队伍规模；初始 5 人安全降级，已登记 1–4 人账本后来扩容仍可由原成员继续并正确注销。
- 已完成不可重复委托不能通过 `/pm party sync` 重复领奖。
- 只有已登记、未结算成员能推进账本；无效键、UUID、步骤和完成账本会安全清理。
- 护送记者按队伍隔离，旧公共记者存档自动迁移，拆队/换队创建独立记者。
- 委托书、卷宗、证物袋和样本满背包时不落地，清出空间后按当前绑定恢复。
- 联系人事件/态度与组织响应分支按玩家隔离；错误 NPC、远程提交、重复结算、任务过期和损坏迁移均有服务端恢复边界。
- `/pm servercheck` 输出 `active_parties` 和 `party_members` 供专服诊断。

## 验证

自动化基线覆盖 439 项 JUnit、24 项 Forge GameTest、147 节点/197 关系内容图、schema 30 迁移与精确回滚、M1/M2/M3/M4 合同、既有多人/权限/守恒路径，以及 M4 两玩家组织行动隔离、封印物唯一保管、离线泄漏、恢复、复制滥用、退役和 SavedData 往返。Pages 现有 237 条图鉴并验证 12 个组织、7 件封印物与普通玩家位置隐私；Forge clean build 和两次真实专服启动—诊断—保存—停服矩阵保持。M2 四人八小时、M3 精确视觉/多人互补，以及 M4 完整 24 件、更多据点/NPC 日程、跨周战略和真人平衡仍需人工验收；具体领地 Mod 联调归入 M7。

完整技术报告：
[`docs/REPOSITORY_HEALTH_AND_RESOURCE_INTEGRITY.md`](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/docs/REPOSITORY_HEALTH_AND_RESOURCE_INTEGRITY.md)。
