# 版本与发布

<!-- project-status:start -->
- 当前版本：**`0.2.0-1.20.1`**
- 开发阶段：**M2 开发 Alpha**（M2）
- 技术基线：Minecraft **1.20.1** · Forge **47.4.20** · Java **17**
- 最后更新：**2026-07-04 07:55:25 UTC+01:00**（`2026-07-04T06:55:25Z`）
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
是版本号、开发阶段和更新时间的唯一来源。

同步命令：

```bash
python scripts/sync_project_metadata.py
python scripts/sync_project_metadata.py --check
```

完整规则见
[`VERSIONING.md`](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/VERSIONING.md)，
版本变化见
[`CHANGELOG.md`](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/CHANGELOG.md)。

## 自动同步

- Documentation Consistency 检查 README、Pages 和 Wiki 源文件。
- Pages 工作流发布 `docs/`。
- Wiki Sync 工作流把主仓库 `wiki/` 同步到 GitHub Wiki 独立仓库。
- Build 从 `project-status.json` 读取 Mod 版本。
