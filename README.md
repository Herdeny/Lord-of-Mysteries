# Project Mystery —《诡秘之主》Minecraft Mod

[![Build](https://github.com/Herdeny/Lord-of-Mysteries/actions/workflows/build.yml/badge.svg)](https://github.com/Herdeny/Lord-of-Mysteries/actions/workflows/build.yml)
[![CodeQL](https://github.com/Herdeny/Lord-of-Mysteries/actions/workflows/codeql.yml/badge.svg)](https://github.com/Herdeny/Lord-of-Mysteries/actions/workflows/codeql.yml)
[![Documentation](https://github.com/Herdeny/Lord-of-Mysteries/actions/workflows/docs-consistency.yml/badge.svg)](https://github.com/Herdeny/Lord-of-Mysteries/actions/workflows/docs-consistency.yml)
[![Pages](https://github.com/Herdeny/Lord-of-Mysteries/actions/workflows/pages.yml/badge.svg)](https://github.com/Herdeny/Lord-of-Mysteries/actions/workflows/pages.yml)
[![License: GPL v3+](https://img.shields.io/badge/code-GPL--3.0--or--later-blue.svg)](LICENSE)
[![Assets: CC BY-NC-SA 4.0](https://img.shields.io/badge/assets-CC%20BY--NC--SA%204.0-lightgrey.svg)](ASSET_LICENSE.md)

> 在未知中承担风险，通过扮演消化力量，逐步成为非凡者。

<!-- project-status:start -->
> - 当前版本：**`0.5.0-1.20.1`**
> - 开发阶段：**M1 验收 Beta**（M1）
> - 技术基线：Minecraft **1.20.1** · Forge **47.4.20** · Java **17**
> - 最后更新：**2026-07-05 09:25:26 UTC+01:00**（`2026-07-05T08:25:26Z`）
<!-- project-status:end -->

Project Mystery 是一个以魔药、序列、扮演和失控风险为核心的 Minecraft 生存冒险 Mod。
仓库现有实现是唯一技术基线；当前设计基线为 v0.6，审计摘要与实施边界见
[`docs/DESIGN_BASELINE.md`](docs/DESIGN_BASELINE.md)，可维护执行版见
[`docs/Project_Mystery_Design_Doc_v0.6.md`](docs/Project_Mystery_Design_Doc_v0.6.md)。
设计文档中的其他加载器或版本示例
只用于机制与数值参考。

> **非官方同人项目：** 本项目未获得《诡秘之主》作者、出版/运营方、Mojang 或
> Microsoft 的授权、认可或赞助。不要将本项目或任何分支描述为官方作品。
> **NOT AN OFFICIAL MINECRAFT PRODUCT. NOT APPROVED BY OR ASSOCIATED WITH
> MOJANG OR MICROSOFT.**

📖 [资料站](https://herdeny.github.io/Lord-of-Mysteries/) ·
[Wiki](https://github.com/Herdeny/Lord-of-Mysteries/wiki) ·
[入门指南](docs/GETTING_STARTED.md) ·
[精神风险与调查日志](docs/MENTAL_RISK_AND_KNOWLEDGE.md) ·
[完整路线](ROADMAP.md) ·
[版本记录](CHANGELOG.md) ·
[版本规范](VERSIONING.md) ·
[路线与任务](https://github.com/Herdeny/Lord-of-Mysteries/issues) ·
[参与贡献](CONTRIBUTING.md)

## 当前可玩内容

当前已形成“普通人 → 序列 9 占卜家 → 序列 8 小丑 → 序列 7 魔术师”、
“普通人 → 序列 9 观众 → 序列 8
读心者”与“普通人 → 序列 9 猎人 → 序列 8 挑衅者”三条可玩路径：

按 v0.6 路线，当前正式里程碑是 M1“占卜家序列 9-7 纵切”；现有观众、猎人和
通用仪式功能作为后续阶段预研保留。占卜家序列 8-7 代码纵切已完成，
但一小时生存与平衡记录尚未通过，因此 M1 仍未关闭。

1. 合成坩埚、灵性草药、占卜水晶与月华水。
2. 按顺序把材料放进坩埚，空手点击开始 1200 tick 炼制。
3. 热源决定温度与魔药品质：完美、完整、瑕疵或污染失败。
4. 服用占卜家魔药，获得 122 灵性上限与三项序列能力。
5. 使用扮演事件消化魔药；重复行为受到新颖度衰减和过度占卜惩罚。
6. 管理污染与失控压力；污染达到 100 会按服务器配置触发失控结局。
7. 消化占卜家魔药后，以深灰灵体之泪、石楠和灵性酒精炼制小丑魔药。
8. 使用秘术纸牌、完美平衡、表情操控和直觉预判完成小丑扮演。
9. 狩猎主世界幻形蛇，以毒腺、灰烬粉和银屑炼制魔术师魔药。
10. 使用火焰跳跃、替身纸人、空气子弹和舞台幻术完成序列 7 纵切。
11. 观众、读心者、猎人和挑衅者作为 M2 预研路径继续保留。
12. 用半径 3 八点阵列执行净化封印，验证仪式失败、污染与失控分支。

### v0.4.0 探索与生存闭环

- 首次登录同时获得调查手稿和调查员罗盘；右键罗盘会指向最近的已生成营地，若尚未生成则指向世界种子确定的新手营地。
- 新手营地稳定分布在出生点外 10–16 区块，并以完整粗泥平台避免海面或不平整地形破坏首小时入口。
- 镇静熏香降低 18 点失控压力并增加 2 点污染，形成“短期稳定换长期代价”的明确规则。
- 灵视提灯令 16 格内敌对生物发光 10 秒；非凡者消耗 3 灵性，普通人或灵性不足者承受 2 点失控压力。
- 灵体微光会抽取附近玩家灵性并掉落灵性盐；灰烬傀儡会施加虚弱与缓慢并掉落灰烬丝线。
- 八段教程成就覆盖手稿、营地罗盘、第一份配方、晋升占卜家、野外装备、猎杀幻形蛇、晋升小丑和抵达魔术师。
- 失控低语扩展为轻度、中度、重度各四条随机文案，减少机械重复并强化风险反馈。

### v0.5.0 精神恢复与调查日志

- 每个游戏日首次完成整夜睡眠会降低 20 点失控压力，并记录“睡眠与精神恢复”知识；提前离床不能触发。
- 精神守护符会在重度失控事件发生时自动消耗，将该次压力增加、混乱和缓慢完整抵消。
- 守护符不能阻止污染达到 100 后的完整失控，避免一次性物品绕过核心风险门槛。
- 玩家首次击杀幻形蛇、灵体微光、灰烬傀儡或占卜家失控体时，自动解锁对应生物档案。
- `/pm journal` 以本地化名称列出已掌握的扮演准则、能力、野外规则和生物档案。
- 非凡者档案不再显示原始 `namespace:knowledge/...` ID，途径和知识条目均按中英文语言文件显示。

### 野外物品

| 物品 | 获取与配方 | 规则 |
|---|---|---|
| 调查员罗盘 | 首次登录、营地战利品；指南针 + 纸 + 紫水晶 | 定位最近营地并记录坐标 |
| 灵性盐 | 灵体微光掉落；也可由糖、紫水晶和深灰灵体之泪净化 | 熏香与提灯的基础材料 |
| 镇静熏香 | 灵性盐 + 灵性草药 + 木炭 + 纸 | 压力 -18，污染 +2，冷却 5 秒 |
| 灰烬丝线 | 灰烬傀儡掉落 | 约束提灯灵火的结构材料 |
| 灵视提灯 | 灵魂灯笼 + 灵性盐×4 + 灰烬丝线×4 | 16 格显形，耐久 64，冷却 5 秒 |
| 精神守护符 | 纸×2 + 灵性盐×2 + 白蜡烛 | 自动抵消一次重度精神事件，污染 100 时无效 |

### 生物与掉落

| 生物 | 行为 | 主要掉落 |
|---|---|---|
| 幻形蛇 | 靠近玩家前隐形，快速近战 | 幻形蛇毒腺、蜘蛛眼 |
| 灵体微光 | 飞行，每 4 秒抽取灵性；不足时增加压力 | 灵性盐、概率深灰灵体之泪 |
| 灰烬傀儡 | 高生命低移速，攻击施加虚弱与缓慢 | 灰烬丝线、概率灰烬粉 |
| 占卜家失控体 | 追击宿主、泄露坐标并干扰精神 | 破碎特性、概率材料 |

### 指引命令

所有 `/pm` 指令均可由普通玩家使用，不需要管理员权限。

| 命令 | 用途 |
|---|---|
| `/pm guide` | 重读当前阶段目标与按键 |
| `/pm status` | 查看途径、序列、灵性、消化、污染与压力 |
| `/pm camp` | 获取营地方向、距离和坐标 |
| `/pm m1check` | 按当前序列和消化度显示下一道 M1 门槛 |
| `/pm rules` | 查看灵性、污染、压力、扮演和代价规则 |
| `/pm items` | 查看五类野外装备与来源 |
| `/pm bestiary` | 查看四类当前生物档案 |
| `/pm journal` | 查看已解锁的能力、准则、规则和生物调查记录 |

### 能力与按键

| 按键 | 功能 | 规则 |
|---|---|---|
| `V` | 灵视 | 0.8 灵性/秒，32 格服务端定向彩色粒子 |
| `B` | 简易占卜 | 15 灵性，60 秒冷却，结果按可信度扭曲 |
| 被动 | 危险直觉 | 35% 概率拦截一次致命攻击，30 秒冷却 |
| `R` | 纸牌飞刃 | 小丑序列 8；3 张纸牌、12 灵性，可消耗火焰弹附加燃烧 |
| 被动 | 完美平衡 / 直觉预判 | 小丑序列 8；降低摔落与击退，15% 近战完全闪避 |
| 潜行右键村民 | 表情操控 | 小丑序列 8；8 灵性，短暂改善交易 |
| `Z` | 火焰跳跃 | 魔术师序列 7；18 灵性，最多 8 格，6 秒冷却 |
| `X` | 替身纸人 | 魔术师序列 7；25 灵性，30 秒致命伤替代窗口 |
| `C` | 空气子弹 | 魔术师序列 7；6 灵性，1 秒冷却 |
| `M` | 舞台幻术 | 魔术师序列 7；20 灵性，误导附近生物 10 秒 |
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
- 占卜家序列 9-7 三种魔药、连续晋升、八项能力和六个扮演事件
- 幻形蛇、灵体微光和灰烬傀儡自然生成、材料掉落、疯言先知坐标泄露与预判闪避
- 首次登录调查手稿与罗盘、确定性新手营地、阶段目标、指引命令和状态面板
- 观众序列 9 与读心者序列 8、二次晋升门槛、精神能力 PvP 配置与可见抵抗反馈
- 猎人序列 9 与挑衅者序列 8、自然生成追踪、捕兽夹、挑衅、激怒与战斗意志
- 坩埚多配方识别：占卜家 9-7、观众、读心者、猎人与挑衅者七种魔药及交叉污染检测
- 扮演收益公式、新颖度衰减、风险系数和过度占卜惩罚
- 污染分级事件与 `recoverable` / `permanent` / `death` 三种失控模式
- 永燃火柴盒：灵火、灵体额外伤害、每次使用增加 15 失控压力
- 通用仪式底座：半径 3 八点阵列、20 tick 结构复检、材料/环境/主持者评分与五档结算
- 净化封印失败分支：压力、污染、失控体与非破坏性仪式爆炸；阵列断裂则取消并保留材料
- 中英双语、九段教程成就、十二条随机低语、知识本地化、基础配方与原版纹理回退
- JUnit 5 的 81 项测试覆盖玩家数据、能力逻辑、恢复规则、灵性消耗、炼药、仪式、扮演与引导

### 当前限制

- 灵视仍使用只对施术者可见的服务端粒子；客户端实体描边渲染尚未完成。
- 占卜家失控体、幻形蛇、灵体微光与灰烬傀儡已可玩，但暂时复用原版实体模型。
- 当前只接入净化封印这一种完整仪式；尊名呼名、晋升饮药窗口和更多仪式仍待后续实现。
- 废弃调查员营地已保证新手入口可定位，但仍采用轻量程序生成；完整结构模板与七步调查任务链尚未实装。
- 占卜家序列 9–7 代码纵切已完成，但一小时生存/专用服务器平衡验收尚未记录。
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
├── command/       # /pm 自助引导与阶段检查
├── entity/        # 失控体与非凡生物
├── knowledge/     # 调查手稿与阶段引导
├── network/       # 服务端权威网络包
├── player/        # Capability、灵性、污染与失控
├── potion/        # 坩埚、魔药品质与晋升
├── ritual/        # 多方块阵列、仪式状态机、结算与失败风险
├── registry/      # 方块、物品、方块实体和创造栏注册
└── world/         # 营地生成、坐标持久化与新手入口
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
