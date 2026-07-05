# 开发状态

<!-- project-status:start -->
- 当前版本：**`0.4.0-1.20.1`**
- 开发阶段：**M1 验收 Beta**（M1）
- 技术基线：Minecraft **1.20.1** · Forge **47.4.20** · Java **17**
- 最后更新：**2026-07-05 01:48:51 UTC+01:00**（`2026-07-05T00:48:51Z`）
<!-- project-status:end -->

## 里程碑

<!-- roadmap:start -->
> 设计基线：**v0.6** · 当前里程碑：**M1**

| 里程碑 | 阶段 | 状态 | 目标 |
|---|---|---|---|
| M0 | Foundation | 已完成 | Capability、网络、配置、注册、构建与手册骨架。 |
| M1 | MVP vertical slice | 进行中 | 完成占卜家 9-7 的魔药、扮演、灵视、失控和一小时生存切片。 |
| M2 | MVP alpha | 规划 | 五途径 9-7、雾都镇区、委托、调查链一和原创化词表。 |
| M3 | MVP beta | 规划 | 24 件封印物、6 个世界事件、值夜者线和经济基础。 |
| M4 | EP1 | 规划 | 五途径序列 6-5、晋升仪式、特性完整版、灰雾与塔罗会。 |
| M5 | EP2 | 规划 | 序列 4、GeckoLib 神话形态、第二批四途径和组织战争。 |
| M6 | EP3 | 远期 | 灵界维度、1 级封印物、贝克兰德大城市与真神级事件。 |
| M7 | Ecosystem | 远期 | 22 途径社区共创、Addon API 冻结与稳定版生态。 |

> 门禁规则：当前里程碑验收未完成前，不得把后续阶段预研标记为该阶段已完成。
<!-- roadmap:end -->

当前按 v0.6 硬门禁处于 M1 验收：占卜家序列 9–7 的代码纵切、材料、生物、能力、
扮演、确定性新手营地、野外风险工具、三类自然生物、八段教程成就和指引命令已完成；
一小时生存、死亡/重连和专用服务器平衡记录仍是验收缺口。
观众/猎人序列 9–8 与通用仪式底座作为后续阶段预研保留。

## 当前限制

- 灵视使用只对施术者可见的服务端粒子，实体描边尚未完成。
- 占卜家失控体、幻形蛇、灵体微光和灰烬傀儡暂时复用原版模型。
- 当前只接入净化封印仪式；尊名呼名、晋升饮药窗口与多人参与加成尚未实现。
- 调查营地入口已可稳定定位，但仍是轻量程序结构。
- 占卜家序列 9–7 尚未完成真实一小时切片和专用服务器回归。
- 专用像素美术仍待制作。

## 验证基线

- `python scripts/sync_project_metadata.py --check`
- JSON 资源解析
- `./gradlew clean build`
- GitHub Build、CodeQL、Documentation Consistency、Pages、Wiki Sync
- M1 记录表：[`docs/M1_ACCEPTANCE_CHECKLIST.md`](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/docs/M1_ACCEPTANCE_CHECKLIST.md)
