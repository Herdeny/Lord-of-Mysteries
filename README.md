# Project Mystery —《诡秘之主》Minecraft Mod

[![Build](https://github.com/Herdeny/Lord-of-Mysteries/actions/workflows/build.yml/badge.svg)](https://github.com/Herdeny/Lord-of-Mysteries/actions/workflows/build.yml)
[![CodeQL](https://github.com/Herdeny/Lord-of-Mysteries/actions/workflows/codeql.yml/badge.svg)](https://github.com/Herdeny/Lord-of-Mysteries/actions/workflows/codeql.yml)
[![License: GPL v3+](https://img.shields.io/badge/code-GPL--3.0--or--later-blue.svg)](LICENSE)
[![Assets: CC BY-NC-SA 4.0](https://img.shields.io/badge/assets-CC%20BY--NC--SA%204.0-lightgrey.svg)](ASSET_LICENSE.md)

> 在未知中承担风险，通过扮演消化力量，逐步成为非凡者。
>
> 当前阶段：**M1 可玩 Alpha**　·　Minecraft **1.20.1**　·　Forge **47.4.20**　·　Java **17**

Project Mystery 是一个以魔药、序列、扮演和失控风险为核心的 Minecraft 生存冒险 Mod。
仓库现有实现是唯一技术基线；设计文档 `docs/Project_Mystery_Mod_Design_Doc_v0.4.pdf`
中的 NeoForge 1.21.1 示例只用于机制与数值参考。

> **非官方同人项目：** 本项目未获得《诡秘之主》作者、出版/运营方、Mojang 或
> Microsoft 的授权、认可或赞助。不要将本项目或任何分支描述为官方作品。
> **NOT AN OFFICIAL MINECRAFT PRODUCT. NOT APPROVED BY OR ASSOCIATED WITH
> MOJANG OR MICROSOFT.**

📖 [资料站](https://herdeny.github.io/Lord-of-Mysteries/) ·
[Wiki](https://github.com/Herdeny/Lord-of-Mysteries/wiki) ·
[路线与任务](https://github.com/Herdeny/Lord-of-Mysteries/issues) ·
[参与贡献](CONTRIBUTING.md)

## 当前可玩内容

M1 已形成“普通人 → 序列 9 占卜家”的基础闭环：

1. 合成坩埚、灵性草药、占卜水晶与月华水。
2. 按顺序把材料放进坩埚，空手点击开始 1200 tick 炼制。
3. 热源决定温度与魔药品质：完美、完整、瑕疵或污染失败。
4. 服用占卜家魔药，获得 122 灵性上限与三项序列能力。
5. 使用扮演事件消化魔药；重复行为受到新颖度衰减和过度占卜惩罚。
6. 管理污染与失控压力；污染达到 100 会按服务器配置触发失控结局。

### 能力与按键

| 按键 | 功能 | 规则 |
|---|---|---|
| `V` | 灵视 | 0.8 灵性/秒，32 格服务端定向彩色粒子 |
| `B` | 简易占卜 | 15 灵性，60 秒冷却，结果按可信度扭曲 |
| 被动 | 危险直觉 | 35% 概率拦截一次致命攻击，30 秒冷却 |
| `N` | 非凡者档案 | 查看灵性、污染、压力、魔药品质与已知知识 |

### 已完成系统

- Forge Capability 玩家数据、NBT 持久化、死亡/跨维度继承
- 灵性恢复、能力消耗与冷却、服务端权威 C2S/S2C 数据包
- 占卜可信度、方向与文本扭曲、序列 9 能力访问控制
- 可交互 `CrucibleBlockEntity`、温度模拟、投料顺序和四档品质
- 占卜家魔药物品、普通人晋升、魔药品质对消化倍率的影响
- 扮演收益公式、新颖度衰减、风险系数和过度占卜惩罚
- 污染分级事件与 `recoverable` / `permanent` / `death` 三种失控模式
- 永燃火柴盒：灵火、灵体额外伤害、每次使用增加 15 失控压力
- 中英双语、基础配方与方块掉落、缺失专用美术时的原版纹理回退
- JUnit 5 覆盖玩家数据、灵性消耗、占卜可信度、坩埚品质与扮演公式

### 当前限制

- 灵视仍使用只对施术者可见的服务端粒子；客户端实体描边渲染尚未完成。
- 占卜家失控体已有独立实体、属性、灵性干扰和掉落，但暂时复用原版僵尸模型。
- 净化封印已实现状态机与祭坛库存交互；多方块圆阵检测和失败分支仍待深化。
- 废弃调查员营地采用轻量程序生成；完整结构模板与七步调查任务链尚未实装。
- 观众/猎人途径和灰雾空间尚未实装。
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
└── registry/      # 方块、物品、方块实体和创造栏注册
```

数据定义位于 `src/main/resources/data/lord_of_mysteries/`，静态资料站位于 `docs/`，
发布地址为 <https://herdeny.github.io/Lord-of-Mysteries/>。

## 路线图

| 里程碑 | 状态 | 目标 |
|---|---|---|
| M0 | 完成 | Forge 工程、Capability、注册与配置骨架 |
| M1 | 功能闭环 Alpha | 占卜家序列 9、净化封印、专属失控体与调查营地基础 |
| M2 | 规划 | 观众/猎人序列 9-8、通用仪式、多人同步与灰雾基础 |
| M3 | 规划 | 序列 7、偷盗者/学徒、Boss、任务链、结构与世界事件 |
| M4 | 规划 | 平衡、性能、兼容、存档迁移与 MVP 1.0 |

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
