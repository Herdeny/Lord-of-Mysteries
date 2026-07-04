# Project Mystery —《诡秘之主》Minecraft Mod

[![Build](https://github.com/Herdeny/Lord-of-Mysteries/actions/workflows/build.yml/badge.svg)](https://github.com/Herdeny/Lord-of-Mysteries/actions/workflows/build.yml)
[![CodeQL](https://github.com/Herdeny/Lord-of-Mysteries/actions/workflows/codeql.yml/badge.svg)](https://github.com/Herdeny/Lord-of-Mysteries/actions/workflows/codeql.yml)
[![Documentation](https://github.com/Herdeny/Lord-of-Mysteries/actions/workflows/docs-consistency.yml/badge.svg)](https://github.com/Herdeny/Lord-of-Mysteries/actions/workflows/docs-consistency.yml)
[![Pages](https://github.com/Herdeny/Lord-of-Mysteries/actions/workflows/pages.yml/badge.svg)](https://github.com/Herdeny/Lord-of-Mysteries/actions/workflows/pages.yml)
[![License: GPL v3+](https://img.shields.io/badge/code-GPL--3.0--or--later-blue.svg)](LICENSE)
[![Assets: CC BY-NC-SA 4.0](https://img.shields.io/badge/assets-CC%20BY--NC--SA%204.0-lightgrey.svg)](ASSET_LICENSE.md)

> 在未知中承担风险，通过扮演消化力量，逐步成为非凡者。

<!-- project-status:start -->
> - 当前版本：**`0.2.1-1.20.1`**
> - 开发阶段：**M1 开发 Alpha**（M1）
> - 技术基线：Minecraft **1.20.1** · Forge **47.4.20** · Java **17**
> - 最后更新：**2026-07-04 08:28:01 UTC+01:00**（`2026-07-04T07:28:01Z`）
<!-- project-status:end -->

Project Mystery 是一个以魔药、序列、扮演和失控风险为核心的 Minecraft 生存冒险 Mod。
仓库现有实现是唯一技术基线；当前设计基线为 v0.6，审计摘要与实施边界见
[`docs/DESIGN_BASELINE.md`](docs/DESIGN_BASELINE.md)。设计文档中的其他加载器或版本示例
只用于机制与数值参考。

> **非官方同人项目：** 本项目未获得《诡秘之主》作者、出版/运营方、Mojang 或
> Microsoft 的授权、认可或赞助。不要将本项目或任何分支描述为官方作品。
> **NOT AN OFFICIAL MINECRAFT PRODUCT. NOT APPROVED BY OR ASSOCIATED WITH
> MOJANG OR MICROSOFT.**

📖 [资料站](https://herdeny.github.io/Lord-of-Mysteries/) ·
[Wiki](https://github.com/Herdeny/Lord-of-Mysteries/wiki) ·
[完整路线](ROADMAP.md) ·
[版本记录](CHANGELOG.md) ·
[版本规范](VERSIONING.md) ·
[路线与任务](https://github.com/Herdeny/Lord-of-Mysteries/issues) ·
[参与贡献](CONTRIBUTING.md)

## 当前可玩内容

当前已形成“普通人 → 序列 9 占卜家”、“普通人 → 序列 9 观众 → 序列 8
读心者”与“普通人 → 序列 9 猎人 → 序列 8 挑衅者”三条可玩路径：

按 v0.6 路线，当前正式里程碑是 M1“占卜家序列 9-7 纵切”；现有观众、猎人和
通用仪式功能作为后续阶段预研保留，但不能替代占卜家序列 8-7 的验收门禁。

1. 合成坩埚、灵性草药、占卜水晶与月华水。
2. 按顺序把材料放进坩埚，空手点击开始 1200 tick 炼制。
3. 热源决定温度与魔药品质：完美、完整、瑕疵或污染失败。
4. 服用占卜家魔药，获得 122 灵性上限与三项序列能力。
5. 使用扮演事件消化魔药；重复行为受到新颖度衰减和过度占卜惩罚。
6. 管理污染与失控压力；污染达到 100 会按服务器配置触发失控结局。
7. 使用灵性草药、发酵蛛眼和可选蜂蜜瓶炼制观众魔药。
8. 完全消化观众魔药后，使用灵性草药、书和可选紫水晶碎片炼制读心者魔药。
9. 使用灵性草药、骨头和可选兔子脚炼制猎人魔药，并用捕兽夹、追踪和荒野感知狩猎。
10. 完全消化猎人魔药后，使用灵性草药、火药和可选红石炼制挑衅者魔药。
11. 用骨粉与木炭制作 8 个仪式粉笔阵纹，在祭坛半径 3 的八个固定位置构成完整圆阵。
12. 夜晚晴天投入永燃火柴盒、纯净水×3、青兰花×5、白蜡烛×8，执行有风险的净化封印。

### 能力与按键

| 按键 | 功能 | 规则 |
|---|---|---|
| `V` | 灵视 | 0.8 灵性/秒，32 格服务端定向彩色粒子 |
| `B` | 简易占卜 | 15 灵性，60 秒冷却，结果按可信度扭曲 |
| 被动 | 危险直觉 | 35% 概率拦截一次致命攻击，30 秒冷却 |
| `N` | 非凡者档案 | 查看灵性、污染、压力、魔药品质与已知知识 |
| `G` | 情绪观察 | 观众序列 9；0.5 灵性/秒，读取视线目标情绪 |
| `H` | 表层读心 | 读心者序列 8；18 灵性，30 秒冷却 |
| `J` | 心理暗示 | 读心者序列 8；25 灵性，40 秒冷却；玩家可潜行抵抗 |
| 被动 | 行为预测 | 观众序列 9；每 8 秒降低一次来袭伤害 40% |
| `K` | 挑衅 | 挑衅者序列 8；15 灵性，20 秒冷却；低抗性敌人优先攻击施术者 8 秒 |
| `L` | 激怒 | 挑衅者序列 8；20 灵性，30 秒冷却；目标攻击 +30%、护甲 -25%，持续 8 秒 |
| 被动 | 追踪与荒野感知 | 猎人序列 9；攻击后追踪 45 秒，户外感知附近敌对目标 |
| 被动 | 战斗意志 | 挑衅者序列 8；被至少 3 名敌人围攻时减伤 15%，持续 10 秒 |

### 已完成系统

- Forge Capability 玩家数据、NBT 持久化、死亡/跨维度继承
- 灵性恢复、能力消耗与冷却、服务端权威 C2S/S2C 数据包
- 占卜可信度、方向与文本扭曲、序列 9 能力访问控制
- 可交互 `CrucibleBlockEntity`、温度模拟、投料顺序和四档品质
- 占卜家魔药物品、普通人晋升、魔药品质对消化倍率的影响
- 观众序列 9 与读心者序列 8、二次晋升门槛、精神能力 PvP 配置与可见抵抗反馈
- 猎人序列 9 与挑衅者序列 8、自然生成追踪、捕兽夹、挑衅、激怒与战斗意志
- 坩埚多配方识别：占卜家、观众、读心者、猎人与挑衅者五种魔药及交叉污染检测
- 扮演收益公式、新颖度衰减、风险系数和过度占卜惩罚
- 污染分级事件与 `recoverable` / `permanent` / `death` 三种失控模式
- 永燃火柴盒：灵火、灵体额外伤害、每次使用增加 15 失控压力
- 通用仪式底座：半径 3 八点阵列、20 tick 结构复检、材料/环境/主持者评分与五档结算
- 净化封印失败分支：压力、污染、失控体与非破坏性仪式爆炸；阵列断裂则取消并保留材料
- 中英双语、基础配方与方块掉落、缺失专用美术时的原版纹理回退
- JUnit 5 的 63 项测试覆盖玩家数据、能力逻辑、灵性消耗、炼药、仪式与扮演公式

### 当前限制

- 灵视仍使用只对施术者可见的服务端粒子；客户端实体描边渲染尚未完成。
- 占卜家失控体已有独立实体、属性、灵性干扰和掉落，但暂时复用原版僵尸模型。
- 当前只接入净化封印这一种完整仪式；尊名呼名、晋升饮药窗口和更多仪式仍待后续实现。
- 废弃调查员营地采用轻量程序生成；完整结构模板与七步调查任务链尚未实装。
- 占卜家当前仅完成序列 9；观众与猎人途径已完成序列 9–8，灰雾空间尚未实装。
- 专用像素美术仍待制作，当前模型引用原版纹理以保证游戏内可见。

## 技术基线

| 项目 | 版本 |
|---|---|
| Minecraft | Java Edition 1.20.1 |
| Mod Loader | Forge 47.4.20（`[47,)`） |
| Java | 17 |
| 构建 | Gradle 8.8 + ForgeGradle 6 |
| 映射 | Parchment `2023.09.03-1.20.1` |
| `mod_id` | `lord_of_mysteries` |
| Java 包根 | `top.aurora.lordofmysteries` |

## 构建

```bash
./gradlew compileJava test
./gradlew build
./gradlew runClient
./gradlew runServer
```

构建产物位于 `build/libs/`，放入 Forge 1.20.1 的 `mods/` 目录即可加载。

## 版本与文档同步

`project-status.json` 是版本号、开发阶段、技术基线和最后更新时间的唯一来源。
功能变更必须同时更新 README、Pages、Wiki 和 `CHANGELOG.md`，然后运行：

```bash
python scripts/sync_project_metadata.py
python scripts/sync_project_metadata.py --check
```

CI 会检查同步状态，并在 `wiki/` 内容变化后自动发布 GitHub Wiki。完整规则见
[`VERSIONING.md`](VERSIONING.md)。

## 项目结构

```text
src/main/java/top/aurora/lordofmysteries/
├── ability/       # 灵视、危险直觉、简易占卜与可信度
├── acting/        # 扮演事件、消化公式与反刷取
├── artifact/      # 封印物行为
├── client/        # 按键与非凡者档案 UI
├── network/       # 服务端权威网络包
├── player/        # Capability、灵性、污染与失控
├── potion/        # 坩埚、魔药品质与晋升
├── ritual/        # 多方块阵列、仪式状态机、结算与失败风险
└── registry/      # 方块、物品、方块实体和创造栏注册
```

数据定义位于 `src/main/resources/data/lord_of_mysteries/`，静态资料站位于 `docs/`，
发布地址为 <https://herdeny.github.io/Lord-of-Mysteries/>。

## 路线图

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

完整阶段门禁、验收标准和 v0.6 内容规模目标见 [`ROADMAP.md`](ROADMAP.md)。

## IP 与授权

本仓库只对项目成员有权授权的原创部分授予许可：

- 原创代码与机器可读逻辑：[`GPL-3.0-or-later`](LICENSE)。他人可以学习、
  修改和分发，但发布修改版时必须保留许可并提供对应源码，不得闭源占有。
- 原创文档与原创美术资产：
  [`CC BY-NC-SA 4.0`](ASSET_LICENSE.md)。要求署名、非商业、相同方式共享。
- 《诡秘之主》、Minecraft、Forge 及其他第三方名称、设定、角色、素材与商标
  不属于上述授权，权利归各自权利人所有。

完整边界、免责声明和权利人联系流程见 [`NOTICE.md`](NOTICE.md)；
专有名词原创化计划见 [`docs/IP_MAPPING.md`](docs/IP_MAPPING.md)。
欢迎贡献，但所有提交必须满足 [`CONTRIBUTING.md`](CONTRIBUTING.md) 的 DCO、
来源和许可要求。

协作成员：Herdeny（星魂）、Zijian-Ni（小倪）。
