# 版本与发布

<!-- project-status:start -->
- 当前版本：**`0.9.18-1.20.1`**
- 开发阶段：**v0.9 M2 自动化收尾与 M3 五途径序列 6–5 首批实现**（M2）
- 技术基线：Minecraft **1.20.1** · Forge **47.4.20** · Java **17**
- 最后更新：**2026-07-24 18:11:16 UTC+01:00**（`2026-07-24T17:11:16Z`）
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
- Pages 图鉴由注册表和双语文件生成；手工条目只能补充能力/系统说明，不能替代 85 物品、5 方块、8 实体覆盖检查。
- Wiki Sync 工作流把主仓库 `wiki/` 同步到 GitHub Wiki 独立仓库。
- Build 从 `project-status.json` 读取 Mod 版本。
