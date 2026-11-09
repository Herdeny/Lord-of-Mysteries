# 0.9.39 验证记录与边界

日期：2026-11-09（Europe/London）；实现基线：Forge 1.20.1 / 47.4.20 / Java 17。
本报告记录本轮真实执行，不是全量v0.9或1.0完成证书。最终提交时间以project-status.json和Git为准。

## 1. 本轮交付范围

- 生存经济：8种村民/流浪商人交易候选，两种草药加入原版堆肥。
- 开放水域真实鱼竿上下文20%概率附加1灵盐，不取代原版渔获。
- 新增4条研钵配方，总计12条；10节点双语原生进度；/pm survival增加经济/发现说明。
- 修复玩家偷窃先移除物品、后检查灵性的问题；遮挡、旁观、独占活动和受害者保护。
- 实际客户端暴露L进度键与激怒冲突：默认改I，客户端tick末尾才分派输入，界面/断线丢弃待处理按键；保留旧用户改键。
- 82章节审计、20领域1.0指导书、39项依赖任务、48类目标验收，以及自动Wiki镜像漂移检查。

## 2. 自动验证

| 验证 | 本轮结果 | 不代表什么 |
|---|---|---|
| clean build + runGameTestServer | 507 JUnit，0失败/0错误；43/43 Forge GameTest | 不等于所有用户环境或真人联机 |
| 专服重启矩阵 | 两次启动、命令诊断、保存、退出；种子/schema30快照稳定 | 不等于断电或20人压力测试 |
| 回滚fixture | 原样恢复/重复执行等既有门禁通过 | 不替代正式服务器操作前备份 |
| 资源完整性 | 616 JSON、2115双语键对、844引用、183模型；161物品/16方块/28实体 | 注册数量不等于v0.9增量数量完成 |
| 视觉资源 | 28实体renderer、123sprite、16方块模型、16粒子provider | 没有新增全套高级动画/音频 |
| 生成与文档 | 117生存资源、334图鉴、元数据与M1–M5契约通过 | Pages源码不代表线上已部署 |
| 原文完整性 | v0.9原文488668字节/7044行、内嵌v0.8保留 | 82章节覆盖不是原子需求逐条完成 |
| 设计校验 | 缺章节、假complete、重复任务、依赖环、未知用例、越界证据6类输入均拒绝 | 只校验结构与引用，不证明设计可玩 |

本轮新增4项JUnit和4项GameTest；总数从503/39升到507/43。没有把待办48类验收统计成48项通过。

### 行为断言细节

1. 真实MerchantOffer：原交易sentinel保留；买卖基准价、库存耗尽、正常补货、牧师与流浪商人库存。
2. 真实原版ComposterBlock交互：消耗一株草，空堆肥桶进入第一层；注册概率0.65。
3. 真实服务端Fishing LootTable调用800次：2种开放水域状态×2工具×200固定seed；原版输出保留、附加至多1、无鱼竿/封闭水0补给，合法状态出现概率补给。钓钩子类固定开放水域状态以隔离谓词，**并非真实水体抛竿800次**。
4. 服务端解析全部10个进度、criteria与父节点；不是只比较JSON文本。
5. 真实ServerPlayer偷窃：0灵性拒绝且目标库存/冷却不变；服务器PvP关闭拒绝；石块遮挡拒绝；成功只取一件/扣12灵性；冷却重试不重复。
6. 既有矿脉、作物并发、工位容器/权限、结构真实生成、M0–M5跨系统测试继续运行。

### 保留失败证据，不把修复过程说成一路全绿

- 临时恢复旧偷窃先扣料顺序：43项GameTest中准确1项失败，断言为“rejected theft must not destroy the victim item or set cooldown”。随后恢复修复，完整重跑通过。
- 初版钓鱼实体子谓词误写为minecraft:fishing_hook，真实加载报错；查1.20.1实现后改成fishing_hook并重跑，不放宽断言。
- GameTest服务器默认PvP为关闭；测试显式开启并在finally恢复，分别验证拒绝/成功；没有删除成功路径。
- 客户端初版仅在InputEvent.Key检查screen仍不能阻止旧L配置误触，因为事件发生在原版打开进度之前。改成ClientTickEvent.END后重新验证，不能只更默认键便宣称旧配置已修复。

## 3. 真实客户端与网页

使用已经授权的一次性世界 **PM Visual QA 0.9.36**，没有改其他或生产存档。
- 第一轮实际执行/pm survival，看到经济、开放水域钓鱼和进度说明；通过/give取得草药触发原生进度入口。此处是创造QA，不是普通新玩家生存通关。
- 保存退出并重进同一个存档，生存进度根节点保持已取得；新增分支有正确物品图标和英文说明。
- 旧L绑定打开原版进度不再出现猎人能力拒绝消息；在进度界面按N/V，关闭后无状态面板或灵视积压；回到世界按N正常打开非凡档案，验证并非把所有快捷键禁掉。
- 最终修复版正常保存、退出到标题、退出客户端。没有真实双客户端或全部技能动画验收。
- [原版进度树实拍](assets/visuals/survival-advancements-0.9.39.png) · [经济指引实拍](assets/visuals/survival-economy-0.9.39.png)。截图来自游戏F2，不是生成图或假UI。

本地Pages桌面和390×844手机视口：334条图鉴、12条加工配方；搜索beeswax得到蜜脾+线→2白蜡烛，手机详情底部面板可读；关闭恢复触发按钮焦点。已复原视口设置。
本轮不声称访问了全部图鉴详情、所有浏览器或无障碍设备。

## 4. 可重复命令与日志

```powershell
$env:JAVA_TOOL_OPTIONS='-Duser.language=en -Dfile.encoding=UTF-8'
.\gradlew.bat clean build runGameTestServer --offline --no-daemon
python scripts/run_server_restart_matrix.py
python scripts/check_save_rollback.py
python scripts/sync_project_metadata.py --check
python scripts/generate_survival_content.py --check
python scripts/gen_pages_catalog.py --check
python scripts/check_pages_layout.py
python scripts/check_resource_integrity.py
python scripts/check_visual_assets.py
python scripts/check_design_roadmap.py --check
```

离线构建要求已有Gradle/Forge依赖缓存；新机器先按构建手册联网初始化。
原始本机日志在未入库的.codex-temp/：validation-0.9.39-final.log、validation-0.9.39-regression.log、restart-0.9.39-final.log、client-0.9.39*.log。
JUnit XML在build/test-results/test；clean会清理构建结果，日志另行保留。
Forge开发运行有mods.toml内部库/union资源协议/Netty反射/离线Realms等既有警告；不将“没有崩溃”写成日志完全无告警。Gradle9弃用提示未通过擅自升级Forge工具链处理。

构建产物：Lord of Mysteries-0.9.39-1.20.1.jar；SHA256：

```text
28a4e46724c99a97cd811c8f496435f31445a121cdd91ba69e78b8c48ab57ed6
```

## 5. 尚未完成与发布区别

- v0.9 M6–M12实质内容、逐表格行审计、四人8小时、20人性能、100小时公开测试仍未完成；[全章审计](V09_COMPLETION_AUDIT.md)与[1.0矩阵](V1_ACCEPTANCE_MATRIX.md)保留待办。
- 交易极端治愈/英雄/其他mod折扣、长期经济、自然钓鱼分布、全量第三方整合包、更多语言与所有显卡/光影组合未验收。
- 未新增独立新群系、怪物、途径或注册材料；十工位和大型地牢是1.0设计，不是此JAR功能。
- GitHub此前25ffb92的Actions因账号计费限制0步骤失败。新提交必须按精确SHA核验；本地通过不等于云端通过，Wiki直推不等于Pages部署。
- 不关闭上层史诗Issue，不反复重跑制造邮件，不修改付款/地区/安全设置来绕过账号限制。
