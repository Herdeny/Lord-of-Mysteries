# 版本与发布

<!-- project-status:start -->
- 当前版本：**`0.9.27-1.20.1`**
- 开发阶段：**v0.9 M2 真人验收准备与 M3 持久秘偶编队闭环**（M2）
- 技术基线：Minecraft **1.20.1** · Forge **47.4.20** · Java **17**
- 最后更新：**2026-07-26 02:31:12 UTC+01:00**（`2026-07-26T01:31:12Z`）
<!-- project-status:end -->

## 版本格式

```text
<SemVer>-<Minecraft 版本>
```

- 新增途径、序列或大型系统：提升次版本号。
- 缺陷、兼容性或文档发布修复：提升修订号。
- Minecraft 大版本变化：建立独立兼容分支并修改后缀。

## 单一版本源

主仓库的
[`project-status.json`](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/project-status.json)
是版本号、开发阶段和更新时间的唯一来源；
[`roadmap.json`](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/roadmap.json)
是设计版本、里程碑、门禁和内容规模目标的唯一来源。

同步命令：

```bash
python scripts/sync_project_metadata.py
python scripts/import_v09_design.py --check
python scripts/gen_datapack.py --check
python scripts/build_content_graph.py
python scripts/gen_pages_catalog.py --check
python scripts/check_pages_layout.py
python scripts/check_resource_integrity.py
python scripts/sync_project_metadata.py --check
```

正式发布的最后一步必须执行：

```bash
python scripts/stamp_release.py
git commit
git push
python scripts/check_release_timestamp.py --require-head
```

`stamp_release.py` 使用 Europe/London 与 UTC 同时写入 `project-status.json`，然后同步 README、Pages 与 Wiki 元数据。它必须紧邻最终 commit 执行；CI 要求 push 时元数据最后修改提交就是当前 HEAD，并限制时间漂移，禁止用早期开发时间冒充发布时间。

完整规则见
[`VERSIONING.md`](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/VERSIONING.md)，
版本变化见
[`CHANGELOG.md`](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/CHANGELOG.md)。

## 自动同步

- Documentation Consistency 检查 README、Pages 和 Wiki 源文件。
- Build 与 Documentation Consistency 都校验 v0.9 精确设计源和内容关系图。
- `ROADMAP.md`、README 路线表、Wiki 里程碑和 Pages 路线卡由 `roadmap.json` 自动生成。
- Pages 工作流发布 `docs/`。
- Pages 图鉴由注册表和双语文件生成；手工条目只能补充能力/系统说明，不能替代当前 91 物品、7 方块、9 实体覆盖检查。
- Pages 布局契约固定检查锚点恢复、24 条分批渲染、响应式断点、`hidden` 语义、ARIA 状态与弹窗焦点恢复。
- Wiki Sync 工作流把主仓库 `wiki/` 同步到 GitHub Wiki 独立仓库。
- Build 从 `project-status.json` 读取 Mod 版本。

## 0.9.27 发布证据

- 网络协议 13、Capability schema 27，397 项 JUnit 与 17 项真实 Forge GameTest。
- 新增三槽持久秘偶编队、同维跟随/协助、安全召回、玩家友军保护、显式解除及旅行家门主/管理员端点运维。
- Pages 图鉴增至 190 条唯一记录，资源基线为 322 JSON、1500 对双语键、593 个静态引用、98 个模型、91 个物品、7 个方块和 9 个实体。

## 0.9.26 发布证据

- 网络协议 13、Capability schema 26，392 项 JUnit 与 16 项真实 Forge GameTest。
- 新增 32 字符远门命名、32 人持久黑名单、出生点保护、三动作可取消 Forge 领地事件和实时门端点名单更新。
- Pages 图鉴维持 189 条唯一记录，旅行家能力卡、M3 总览、专服、迁移与资源数据全部同步。

## 0.9.25 发布证据

- 网络协议 13、Capability schema 25，384 项 JUnit 与 15 项真实 Forge GameTest。
- 新增 20 秒旅行家双向门实体、私人/原版队伍/公开权限、主动穿门同意、安全落点复检、失败退款、反回弹和实体 NBT 往返。
- Pages 图鉴为 189 条唯一记录，注册覆盖 91 物品、7 方块、9 实体。

## 0.9.24 发布证据

- 网络协议 13、Capability schema 25，384 项 JUnit 与 15 项真实 Forge GameTest。
- 新增主世界统一特性来源 SavedData、五类原子消费审计、旧玩家来源 nonce 迁移和匿名管理员统计。
- Pages 图鉴为 188 条唯一记录，新增全服特性来源账本机制页。

## 0.9.23 发布证据

- 网络协议 13、Capability schema 23，371 项 JUnit 与 13 项真实 Forge GameTest。
- Pages 图鉴为 187 条唯一记录，包含自动生成的身份盐环和人工维护的额外负载/活体析出机制条目。
- 发布前必须刷新 `project-status.json` 时间、运行元数据同步、完整资源/Pages/回滚门禁、clean build 和专服双启动矩阵。
