# Contributing to Project Mystery

感谢你愿意参与 Project Mystery。我们接受代码、测试、文档、翻译、原创美术、
玩法设计和问题复现等贡献。

## 开始之前

1. 阅读 `README.md`、`ROADMAP.md`、`NOTICE.md`、`ASSET_LICENSE.md` 和
   `docs/IP_MAPPING.md`。
2. 对较大的功能或世界观内容，先创建 Feature request 或 Content proposal。
3. 不要提交小说原文、官方插画、反编译资源、游戏提取资产、未授权字体/音乐或
   无法证明来源的 AI 生成素材。
4. 不要在 Issue、日志或截图中提交令牌、私钥、真实服务器地址或个人信息。

## 开发环境

- Minecraft Java Edition 1.20.1
- Forge 47.4.20
- Java 17

```bash
./gradlew compileJava test
./gradlew build
./gradlew runClient
```

Windows 可使用对应的 `gradlew.bat`。

## 工作流程

1. 从最新 `main` 创建短期分支：`feat/...`、`fix/...`、`docs/...` 或
   `chore/...`。
2. 一个 Pull Request 只解决一个清晰问题。
3. 补充或更新相关测试、数据文件和文档。
4. 变更必须符合 `roadmap.json` 的当前里程碑门禁；后续阶段预研不得标记为当前阶段完成。
5. 玩家可见功能必须同步更新 `README.md`、`docs/`、`wiki/` 和
   `CHANGELOG.md`；版本、阶段或更新时间变化时修改 `project-status.json`。
6. 路线变化只修改 `roadmap.json`，不要手工维护 README、Pages 或 Wiki 的路线表。
7. 运行 `python scripts/sync_project_metadata.py`，并确保
   `python scripts/sync_project_metadata.py --check` 通过。
8. 确保 `./gradlew clean build` 通过。
9. 使用清晰的提交信息，推荐 Conventional Commits，例如
   `feat(ritual): add failure feedback`。
10. 填写 Pull Request 模板，关联 Issue，并说明游戏内验证结果。

`main` 只通过 Pull Request 更新。合并前需要 CI 通过、讨论已解决，并由维护者
审核。维护者通常使用 squash merge。

版本和发布时间规则见 `VERSIONING.md`。主仓库 `wiki/` 是 GitHub Wiki 的受控源，
合并到 `main` 后由 Wiki Sync 工作流自动发布；不要只在网页端修改 Wiki。

## 贡献许可与 DCO

本项目使用 [Developer Certificate of Origin 1.1](https://developercertificate.org/)
确认贡献者有权提交内容。每个提交都必须包含：

```text
Signed-off-by: Your Name <your.email@example.com>
```

使用以下命令自动添加：

```bash
git commit -s
```

提交贡献即表示：

- 你创作了该贡献，或有权按适用许可证提交；
- 代码和机器可读逻辑按 `GPL-3.0-or-later` 提供；
- 原创文档和原创资产按 `CC BY-NC-SA 4.0` 提供；
- 你理解贡献和签署信息将永久公开保存；
- 你没有获得《诡秘之主》、Minecraft 或其他第三方权利的授权代理资格。

## 代码与数据要求

- 服务端必须对晋升、资源消耗、伤害、污染和冷却作最终裁决。
- 保持 Forge 1.20.1 / Java 17 兼容，不擅自迁移加载器或版本。
- 玩家可见文本进入 `lang` 文件，不在 Java 中硬编码。
- 数据驱动内容优先使用 JSON，并保持稳定的英文资源 ID。
- 修复缺陷时优先加入回归测试；不要顺带重构无关代码。
- 新增第三方依赖必须说明用途、许可证、体积和替代方案。

## 美术与 IP 要求

- 只接受原创、明确兼容许可或已获书面授权的资产。
- 在 PR 中填写素材作者、来源、许可证和修改说明。
- 不允许冒充官方项目、使用官方 Logo，或将 Minecraft 作为项目主品牌。
- 原作专有名词必须登记在 `docs/IP_MAPPING.md`，并保持可原创化替换。
- 对来源不明或权利风险过高的内容，维护者可以拒绝或移除。

## 行为规范与支持

参与即表示同意 `CODE_OF_CONDUCT.md`。普通使用问题请阅读 `SUPPORT.md`；
安全漏洞按 `SECURITY.md` 私下报告。

