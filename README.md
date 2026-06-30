# Project Mystery —《诡秘之主》Minecraft Mod

> 在未知中承担风险、通过扮演消化力量、逐步成为非凡者的 Minecraft 生存冒险 Mod。
> 仓库代号：Lord-of-Mysteries ・ 项目代号：Project Mystery

## 技术基线

| 项 | 版本 |
|----|------|
| Minecraft | Java Edition **1.21.1** |
| Mod Loader | **NeoForge 21.1.x** |
| Java | **21** |
| 构建 | Gradle 8.8 + NeoForged ModDevGradle |
| mod_id | `project_mystery` |
| 包根 | `top.aurora.projectmystery` |

> ⚠️ 设计文档 v0.4 的基线是 NeoForge 1.21.1；本工程已按此重建。
> （此前仓库里的 Forge 1.20.1 example 模板已清理。）

## 当前进度：M0 技术验证（框架阶段）

已落地的骨架（可编译运行的最小闭环）：

- ✅ NeoForge 1.21.1 工程配置（build.gradle / settings / gradle.properties / wrapper / neoforge.mods.toml）
- ✅ Mod 入口 `ProjectMystery`（注册附件、注册表、配置、事件总线）
- ✅ 玩家数据 `PlayerMysteryData` + `MysteryAttachments`（Attachment + Codec + copyOnDeath，§5.1）
- ✅ 灵性恢复 `SpiritualityRegenHandler`（§5.2）
- ✅ 污染/失控检定节奏 `PollutionEffectHandler`（§5.3）
- ✅ 注册管线：方块（仪式祭坛/坩埚）、物品（占位材料/封印物）、方块实体占位、创造标签
- ✅ 服务端配置 `ServerConfig`（§19.1 平衡开关）
- ✅ 数据驱动 JSON 示例（途径/序列/扮演事件/魔药/仪式/封印物，§18）
- ✅ 中英双语 lang + blockstate/model 资源
- ✅ M0 单元测试框架（`PlayerMysteryDataTest`）
- ✅ 按 §17 建立全部 13 个模块包结构（含 package-info 说明）

待填充（按文档里程碑）：M1 占卜家序列9闭环 → M2 三途径+灰雾 → M3 MVP 内容 → M4 发布 → M5 扩展。

## 目录结构

遵循设计文档 §17。核心源码在 `src/main/java/top/aurora/projectmystery/`，
按 core / player / ability / potion / ritual / divination / knowledge / artifact /
acting / world / organization / grayfog / client / compat 分模块。

数据包在 `src/main/resources/data/project_mystery/`，资源在 `assets/project_mystery/`。

## 构建 & 运行

```bash
# 需要 Java 21
./gradlew build              # 编译 + 测试 + 打 jar
./gradlew test               # 仅跑单元测试
./gradlew runClient          # 启动带 mod 的客户端（首次会下载/反编译 MC，较慢）
./gradlew runServer          # 启动专用服务端
./gradlew runData            # 运行数据生成
```

> 首次 `./gradlew build` 会下载 NeoForge 与 Minecraft 并反编译，需要较长时间和约 3GB 内存。

## 文档

- 设计文档：`docs/Project_Mystery_Mod_Design_Doc_v0.4.pdf`（唯一权威规格）
- IP 原创化映射表：`docs/IP_MAPPING.md`（公开发布前必须处理）

## 授权与 IP 声明

所有专有名词当前仅用于内部原型。公开发布前必须完成 IP 处理或原创化映射
（见 `docs/IP_MAPPING.md`）。

## 协作

- Herdeny（星魂）：项目发起 / 共享
- Zijian-Ni（小倪）：协同开发
