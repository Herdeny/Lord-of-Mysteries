# Project Mystery Wiki

> 在未知中承担风险，通过扮演消化力量，逐步成为非凡者。

<!-- project-status:start -->
- 当前版本：**`0.1.0-1.20.1`**
- 开发阶段：**M2 开发 Alpha**（M2）
- 技术基线：Minecraft **1.20.1** · Forge **47.4.20** · Java **17**
- 最后更新：**2026-07-04 02:36:45 UTC+01:00**（`2026-07-04T01:36:45Z`）
<!-- project-status:end -->

Project Mystery 是受《诡秘之主》启发的非官方、非商业 Minecraft Forge 同人模组。
仓库现有代码是唯一实现基线，设计文档只作为机制与数值参考。

## 当前可玩路径

### 占卜家序列 9

- 灵视：持续消耗灵性，以定向粒子显示实体阵营与危险。
- 危险直觉：有冷却的致命伤害规避。
- 简易占卜：按可信度返回清晰、模糊或错误结果。
- 扮演事件：通过符合占卜家身份的行为消化魔药。

### 观众序列 9 → 读心者序列 8

- 情绪读取：`G`，每秒消耗 0.5 灵性。
- 行为预判：每 8 秒降低一次来袭伤害 40%。
- 表层读心：`H`，18 灵性，30 秒冷却。
- 心理暗示：`J`，25 灵性，40 秒冷却；玩家可潜行抵抗。
- 读心者晋升要求：观众序列 9 且消化度达到 100%。

### 猎人序列 9 → 挑衅者序列 8

- 追踪与荒野感知：攻击后追踪目标 45 秒，户外提示附近敌对目标。
- 猎人捕兽夹：绑定放置者，触发后施加缓慢与虚弱并自动消耗。
- 挑衅：`K`，15 灵性，20 秒冷却，使低抗性敌人优先攻击玩家。
- 激怒：`L`，20 灵性，30 秒冷却，提高目标攻击并降低其防御。
- 战斗意志：被至少 3 名敌人围攻时获得 15% 减伤。
- 挑衅者晋升要求：猎人序列 9 且消化度达到 100%。

## 核心系统

- 服务端权威的 Capability、NBT、能力消耗、冷却和晋升校验。
- 坩埚多配方、顺序、温度、品质和交叉污染。
- 灵性、污染、失控压力、三种失控模式和独立失控体。
- 永燃火柴盒、净化封印仪式、调查员营地和非凡者档案。
- 中英双语、55 项单元测试、Build、CodeQL、Pages 与文档一致性检查。

## 导航

- [安装与构建](Installation-and-Build)
- [开发状态](Development-Status)
- [版本与发布](Versioning-and-Releases)
- [在线资料站](https://herdeny.github.io/Lord-of-Mysteries/)
- [README](https://github.com/Herdeny/Lord-of-Mysteries#readme)
- [问题与路线](https://github.com/Herdeny/Lord-of-Mysteries/issues)
- [参与贡献](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/CONTRIBUTING.md)
- [版权与许可](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/NOTICE.md)

## 非官方声明

本项目未获得原作权利人、Mojang 或 Microsoft 的授权、认可或赞助。
原创代码使用 GPL-3.0-or-later；原创文档与原创美术使用 CC BY-NC-SA 4.0。
第三方名称、设定、素材与商标不在项目许可范围内。

**NOT AN OFFICIAL MINECRAFT PRODUCT. NOT APPROVED BY OR ASSOCIATED WITH MOJANG
OR MICROSOFT.**
