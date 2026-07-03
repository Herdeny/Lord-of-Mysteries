# 开发状态

<!-- project-status:start -->
- 当前版本：**`0.0.1-1.20.1`**
- 开发阶段：**M2 开发 Alpha**（M2）
- 技术基线：Minecraft **1.20.1** · Forge **47.4.20** · Java **17**
- 最后更新：**2026-07-03 06:52:20 UTC+01:00**（`2026-07-03T05:52:20Z`）
<!-- project-status:end -->

## 里程碑

| 里程碑 | 状态 | 内容 |
|---|---|---|
| M0 | 已完成 | Forge 工程、Capability、注册、配置和测试骨架 |
| M1 | 已完成 | 占卜家序列 9、炼药、扮演、失控、封印与调查营地 |
| M2 | 进行中 | 观众序列 9–8 已完成；猎人、通用仪式、多人验证与灰雾继续开发 |
| M3 | 规划 | 序列 7、更多途径、Boss、任务链和世界事件 |
| M4 | 规划 | 平衡、性能、兼容、存档迁移与 MVP 1.0 |

## 当前限制

- 灵视使用只对施术者可见的服务端粒子，实体描边尚未完成。
- 占卜家失控体暂时复用原版僵尸模型。
- 仪式多方块圆阵与失败分支仍待深化。
- 调查营地仍是轻量程序结构。
- 猎人途径和灰雾空间尚未实装。
- 专用像素美术仍待制作。

## 验证基线

- `python scripts/sync_project_metadata.py --check`
- JSON 资源解析
- `./gradlew clean build`
- GitHub Build、CodeQL、Documentation Consistency、Pages、Wiki Sync
