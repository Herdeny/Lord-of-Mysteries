# Project Mystery —《诡秘之主》Minecraft Mod

> 在未知中承担风险、通过**扮演**消化力量、逐步成为非凡者的 Minecraft 生存冒险 Mod。
>
> 仓库代号：`Lord-of-Mysteries`　·　项目代号：`Project Mystery`　·　当前阶段：**M0 技术验证（框架）**

本 Mod 把《诡秘之主》的核心体验搬进 Minecraft：玩家从普通人出发，调配**魔药**晋升序列，
靠**扮演对应序列的角色**来消化力量，同时对抗灵性枯竭与失控污染，逐步揭开非凡世界。

---

## 一、技术基线（已锁定）

| 项目 | 版本 | 说明 |
|------|------|------|
| Minecraft | **Java Edition 1.20.1** | 锁定，不升级 |
| Mod Loader | **Forge 47.4.20**（`[47,)`） | 经典 Forge，非 NeoForge |
| Java | **17** | Forge 1.20.1 工具链要求 |
| 构建 | Gradle 8.8 + **ForgeGradle 6** | `./gradlew` 自带 wrapper |
| 映射 | official（Mojang 官方映射） | |
| `mod_id` | `lord_of_mysteries` | |
| Java 包根 | `top.aurora.lordofmysteries` | |

> 玩家数据采用 **Forge Capability**（1.20.1 无 NeoForge 的 Attachments API），
> 随玩家存档以 NBT 持久化，死亡/跨维度自动继承。

---

## 二、当前进度：M0 技术验证

已落地一套**可编译、可运行、带单测**的最小骨架，验证核心技术路线可行。

### ✅ 已完成

**工程基础**
- Forge 1.20.1 完整工程（`build.gradle` / `gradle.properties` / `settings.gradle` / gradle wrapper / `mods.toml`）
- `./gradlew compileJava test` → **BUILD SUCCESSFUL**（3 个单元测试通过）

**核心系统骨架**
- `ProjectMystery`：Mod 入口，挂载注册、配置、事件总线
- `PlayerMysteryData`：玩家非凡者数据载荷（途径 / 序列 / 灵性 / 消化 / 污染 / 失控压力 / 知识 / 扮演历史 / 组织声望 / NBT 序列化）
- `MysteryCapability` + `PlayerCapabilityEvents`：Capability 附着 / 注册 / 死亡跨维度数据继承（§5.1）
- `SpiritualityRegenHandler`：灵性自然恢复（非战斗 + 光照≥7 + 失控压力<30，§5.2）
- `PollutionEffectHandler`：污染 / 失控压力分级检定节奏（§5.3）
- `ServerConfig`：服务端平衡开关（灵性恢复 / 消化 / 污染倍率、失控模式、灰雾开关等，§19.1）

**注册管线**
- 方块：仪式祭坛 `ritual_altar`、坩埚 `crucible`
- 物品：占位材料（灵性草药 / 占卜水晶 / 月华水 / 污染混合物）、封印物（永燃火柴盒）、对应方块物品
- 方块实体注册器（坩埚 BlockEntity 占位，M1 绑定）
- 创造模式物品栏 `lord_of_mysteries:main`

**数据驱动示例（§18，全部为合法 JSON）**
- 途径 `pathways/seer.json`（占卜家）
- 序列 `sequences/seer_9.json`
- 扮演事件 `acting_events/seer9_divination_success.json`
- 魔药 `potions/seer_potion_9.json`
- 仪式 `rituals/calm_sealing_ritual.json`
- 封印物 `artifacts/eternal_matchbox.json`

**资源与本地化**
- 中英双语 `lang/zh_cn.json` + `lang/en_us.json`
- blockstate / 方块模型 / 物品模型（纹理待补，见 `assets/.../textures/TEXTURES_TODO.md`）

**模块包结构**（按设计文档 §17，13 个模块均已建包 + `package-info` 说明职责）
`core` `player` `ability` `potion` `ritual` `divination` `knowledge`
`artifact` `acting` `world` `organization` `grayfog` `client` `compat`

### 🚧 待实现（按里程碑）

| 里程碑 | 目标 |
|--------|------|
| **M1** | 占卜家序列 9 完整闭环：灵视 / 危险直觉 / 简易占卜 + 占卜可信度计算 + 扮演事件系统 + 坩埚魔药制作 + 消化与失控 + 1 个失控体 + 1 件封印物 |
| **M2** | 三途径（占卜家 / 观众 / 猎人）序列 9-8 + 通用仪式状态机 + 多人同步 + 灰雾空间基础版 |
| **M3** | 三途径序列 7 + 偷盗者/学徒 9-7 + 阶段 Boss + 任务链 + 首发结构 + 世界事件 |
| **M4** | 平衡 / 性能 / 兼容 / 存档迁移 / 本地化 → MVP 1.0 |
| **M5** | 序列 6-5、更多途径（→10 条）、组织深化、塔罗会完整功能 |

---

## 三、目录结构

```text
Lord-of-Mysteries/
├── build.gradle / settings.gradle / gradle.properties   # Forge 1.20.1 工程
├── gradlew / gradlew.bat / gradle/                       # Gradle 8.8 wrapper
├── docs/
│   ├── Project_Mystery_Mod_Design_Doc_v0.4.pdf           # 唯一权威设计规格
│   └── IP_MAPPING.md                                     # 原作→原创化映射表（发布前必处理）
└── src/
    ├── main/
    │   ├── java/top/aurora/lordofmysteries/
    │   │   ├── ProjectMystery.java                       # 入口
    │   │   ├── core/        （MysteryRegistries / config/ServerConfig）
    │   │   ├── player/      （PlayerMysteryData / MysteryCapability / 事件 / Handler）
    │   │   ├── registry/    （ModBlocks / ModItems / ModBlockEntities / ModCreativeTabs）
    │   │   └── ability potion ritual divination knowledge
    │   │       artifact acting world organization grayfog client compat   # 模块占位包
    │   └── resources/
    │       ├── META-INF/mods.toml
    │       ├── data/lord_of_mysteries/    （pathways/sequences/potions/rituals/...）
    │       └── assets/lord_of_mysteries/  （lang/ models/ blockstates/ textures/）
    └── test/java/...                                     # JUnit 5 单元测试
```

---

## 四、构建与运行

> 需要 **JDK 17**。首次构建会下载 Forge 并反编译 Minecraft，耗时较长、约需 3GB 内存。

```bash
# 编译 + 跑单元测试
./gradlew compileJava test

# 打出可加载的 mod jar（产物在 build/libs/）
./gradlew build

# 开发期启动客户端 / 专用服务端 / 数据生成
./gradlew runClient
./gradlew runServer
./gradlew runData

# IDE：IntelliJ 直接 import build.gradle；Eclipse 先 ./gradlew genEclipseRuns
```

构建产物 `build/libs/lord_of_mysteries-<version>.jar` 放进 Forge 1.20.1 的 `mods/` 即可加载。

---

## 五、IP 与授权声明 ⚠️

当前所有专有名词（途径名、序列名、组织名、封印物名等）**仅用于内部原型开发**。

- 公开发布前**必须**完成 IP 处理或原创化映射，详见 [`docs/IP_MAPPING.md`](docs/IP_MAPPING.md)。
- 代码与数据层统一使用稳定英文代号（如 `seer`），不含原作中文专有名词；
  所有玩家可见文本走 `lang/*.json`，发布时只替换展示名即可完成原创化，无需改逻辑。
- License 当前为 `All Rights Reserved`（见 `gradle.properties` 的 `mod_license`）。

---

## 六、协作

| 角色 | 成员 |
|------|------|
| 发起 / 共享 | **Herdeny**（星魂） |
| 协同开发 | **Zijian-Ni**（小倪） |

设计文档 v0.4 为唯一权威规格，任何系统实现以文档为准；如需偏离请先更新文档。
