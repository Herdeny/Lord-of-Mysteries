# 版本与文档同步规范

## 单一版本源

`project-status.json` 是项目版本、开发阶段、技术基线和最后更新时间的唯一来源。
Gradle 构建、README、GitHub Pages 和 GitHub Wiki 都必须与该文件一致。

版本格式：

```text
<SemVer>-<Minecraft 版本>
```

当前示例：`0.2.0-1.20.1`。

## 版本规则

- `0.x.y` 表示尚未达到稳定版，允许快速迭代。
- 增加可玩途径、序列或大型系统时提升次版本号。
- 修复缺陷、兼容性或文档发布问题时提升修订号。
- Minecraft 大版本变化必须建立独立兼容分支并修改版本后缀。
- 发布版本必须在 `CHANGELOG.md` 中有对应日期和内容。

## 每次功能更新

1. 修改代码、资源、测试和本地化。
2. 更新 `project-status.json` 的版本、阶段和两个更新时间字段。
3. 更新 `CHANGELOG.md`、`README.md`、`docs/` 和 `wiki/` 中的功能内容。
4. 执行：

   ```bash
   python scripts/sync_project_metadata.py
   ./gradlew clean build
   ```

5. 确认 Documentation Consistency、Build、CodeQL、Pages 和 Wiki Sync 均通过。

CI 会执行 `python scripts/sync_project_metadata.py --check`。元数据不一致时禁止合并。

## 发布时间

- `last_updated` 使用带时区的 ISO 8601 本地时间。
- `last_updated_utc` 使用对应 UTC 时间。
- 两者必须表示同一时刻，同步脚本会验证。
