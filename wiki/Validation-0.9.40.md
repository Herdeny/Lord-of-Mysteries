# 0.9.40 验证记录与明确边界

日期：2026-11-09（Europe/London）；Minecraft 1.20.1 / Forge 47.4.20 / Java 17。最终发布时刻由project-status.json与同一提交记录，本报告日期不是精确完成时刻。

这是 **M6不眠者序列9纵切 + 表现层增量**，不是v0.9全部完成，也不是1.0正式版。既有M0–M5回归继续运行；原文和高阶需求不因本批测试通过而删掉。

## 实际执行结果

| 检查 | 实际结果 | 范围限制 |
|---|---|---|
| 干净构建 | clean build成功；527 JUnit，0失败/0错误 | 非全部用户硬件或整合包 |
| 基础Forge GameTest | 53/53通过，正常保存退出 | 无Jade/JEI，专服逻辑验证 |
| 可选模组GameTest | 同一53/53通过 | 实际加载Jade 11.13.3+forge与JEI 15.56.0.205 |
| 独立专服重启 | 两次启动/诊断/保存/退出；种子及schema30快照marker稳定 | 不是断电、20人负载或真人多人长测 |
| 回滚fixture | dry-run、精确恢复、安全备份、额外文件处理、路径穿越拒绝通过 | 不代替正式服务器备份演练 |
| 内容资源 | 632 JSON、2141双语键对、852静态引用、190模型 | 168物品、16方块、30实体不等于设计数量全部完成 |
| 视觉资源 | 30实体renderer、129原创sprite、16方块模型、16粒子provider | 原版生成蛋及复用原版音效仍保留 |
| 生成与契约 | 119生存资源、345图鉴、M1–M5合同、元数据与镜像通过 | 合同验证与真人体验不同 |
| 原文审计 | 488668字节/7044行v0.9原文完整；82章节、39任务、48类目标验收结构通过 | 48类是目标矩阵，不是本轮通过48类 |
| 内容图 | 165节点、216关系、27孤立节点、36知识门过渡 | 保留现有报告的孤立项，不写成零缺口 |

JUnit从507增至527（守夜状态14项、表现数学/模型6项）；GameTest从43增至53（以下10项）。没有删除真实断言或跳过原有套件来凑全绿。

## 新增行为覆盖

1. **实际坩埚**：营火加热、真实材料逐次投入、调用1200个炼制tick后取出不眠者魔药；实际物品服用入口写入途径/114灵性、特性与身份套件；第二瓶不扣料、不重复身份来源。是加速调用服务端tick，不冒称真人连续等待60秒。
2. **拒绝先于修改**：旁观者、活跃灵界远征拒绝服药/夜视；途径、药瓶、灵性不变。
3. **夜视边界**：外部更强夜视保留、每秒0.5扣费、最后0.5可用、耗尽自动清理、自有状态登出清理、NaN灵性拒绝。
4. **昼夜修饰器**：三层夜间+12%移速、白天-30%；实际咖啡返回玻璃瓶，立即移除当天负面修饰器但保留层数；重复不扣第二杯、次日恢复疲劳；清理不遗留专属属性，SavedData序列化层数保持。
5. **真实研钵交互**：普通人主手咖啡/副手暗影蘑菇制作萃取物；消耗正确、返回一个玻璃瓶，不误走饮用逻辑。
6. **守夜反刷**：刚出生敌人、非攻击者无效；同一UUID重复死亡回调只计一次；睡眠请求立即中断，不等下一次秒级tick。
7. **惊醒**：生物袭击取消首击并起身；1秒保护、10秒冷却、精确到期、虚空绕过、Long.MAX_VALUE损坏时间戳不提供无限保护。
8. **声音隔离**：夜间脚步降低70%，白天脚步和战斗提示保持原音量。
9. **创造/JEI发现性**：构建真实创造栏内容，逐一断言全部168个注册物品均可发现；这项由实机发现的漏项驱动。
10. **真实RecipeManager**：2豆+糖+水桶+玻璃瓶能匹配并组装咖啡、水桶返空桶；少一豆不能合成；可可豆营火烘焙600tick输出咖啡豆。

新增JUnit另覆盖完整安静夜/战斗夜、迟到、时间跳跃/回退、观测间隔、中断、五层上限、咖啡跨日、重启不续算、UUID隔离、未知schema原样只读、重复记录隔离、畸形列表拒绝；以及8种粒子有限值/透明度/降级、语义差异、模型重置/异常浮点/低动态保留战斗信息和两种新几何。

**测试替身边界**：GameTest使用真实ServerPlayer/世界/注册物/配方/能力，但测试玩家覆盖网络发送与睡眠清理回调，没有两个真实客户端接收包。夜晚时间由fixture推进；不声称完成真人整夜守望或真实多人压力测试。

## 实机发现的缺陷及修复

- 第一轮Jade客户端在开发映射环境无法解析字体Mixin的SRG目标。兼容开关新增refmap重映射与createSrgToMcp依赖，复测成功；没有禁用Mixin或降低异常门槛。该修复针对开发运行，不是生产混淆整合包承诺。
- 第一轮JEI搜索coffee为零：7个新注册物未进入显式维护的创造栏。修复创造栏遗漏，新增全注册表发现性GameTest，再重启客户端复测。最终搜索显示咖啡豆/浓黑咖啡，可打开工作台与30秒营火配方。
- 旧生存合同仍要求12条研钵，真实GameTest提示现有14条；更新到生成器真实数量，并保留材料消耗与容器断言。
- 数学测试初版将负零与正零做对象精确比较；改成有明确误差界限的数值断言，NaN、边界和重置断言保留。
- M3–M5脚本从整个gametest目录统计实际注解，避免把新增类漏掉或用硬编码假计数。

## 客户端与网页证据

仅使用已授权的一次性测试存档 **PM Visual QA 0.9.36**；没有修改其他或生产存档。

- 固定Jade/JEI与本模组共载，标题页显示5个mod；进入旧QA世界，Jade展示已成熟石楠，JEI正常呈现物品。
- /pm nightwatch显示新的英文守夜指引；创造展示夜枭与残影独立造型，夜枭翼拍可见。展示实体使用NoAI，不以截图证明自然生成或真实战斗平衡。
- 创造栏修复后重新启动，实际搜索coffee显示两个新物品；点击浓黑咖啡看到两豆/糖/水桶/瓶，点击豆看到30秒营火配方。
- 最终保存退出到标题，再正常Quit Game，Gradle BUILD SUCCESSFUL；旧QA世界跨本轮多次启动可载入。
- 桌面与390×844移动视口设置检查345条图鉴；不眠者标签仅显示序列9与8–5未完成提示；搜索strong_coffee和night_owl，物品图标、获取方法、长ID及详情换行可读。移动端关闭详情后焦点回到原触发卡片，无页面横向溢出。浏览器实际innerWidth取整为391，这里记录视口设置值而非虚构精确设备。
- 本地页面控制台无error；用新缓存上下文复核途径入口计数，区分“五条首发”和“新增不眠者9”。恢复临时视口并关闭验收标签页。

[JEI咖啡实拍](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/docs/assets/visuals/jei-coffee-0.9.40.png) · [营火配方实拍](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/docs/assets/visuals/jei-campfire-0.9.40.png) · [模型展示实拍](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/docs/assets/visuals/nightwatch-creatures-0.9.40.png) · [手机详情](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/docs/assets/visuals/pages-coffee-mobile-0.9.40.png) · [本地Pages](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/docs/assets/visuals/pages-nightwatch-0.9.40.png)。

前三张来自游戏F2，后两张来自浏览器截图，不是生成的假游戏/假网页。模型截图是开发QA，不代表正式生存通关。

## 复现与产物

~~~powershell
$env:JAVA_TOOL_OPTIONS='-Duser.language=en -Dfile.encoding=UTF-8'
.\gradlew.bat clean build runGameTestServer --offline --no-daemon
.\gradlew.bat runGameTestServer -PpmCompatSmoke --offline --no-daemon
python scripts/run_server_restart_matrix.py --timeout 240
.\gradlew.bat runClient -PpmCompatSmoke --offline --no-daemon
python scripts/check_resource_integrity.py
python scripts/check_visual_assets.py
python scripts/check_save_rollback.py
python scripts/check_pages_layout.py
python scripts/check_design_roadmap.py --check
python scripts/sync_project_metadata.py --check
~~~

离线命令要求依赖已缓存；新机器首次去掉--offline。可选依赖仅显式pmCompatSmoke启用，不嵌入成品JAR；固定版本与依据见[视觉兼容说明](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/docs/VISUAL_POLISH_0.9.40.md)。

本机原始日志位于未入库的.codex-temp/：validation-0.9.40-clean.log、validation-0.9.40-release.log、compat-0.9.40-final.log、restart-0.9.40-final.log、client-0.9.40-final.log，以及保留的第一轮兼容失败日志。JUnit XML在build/test-results/test，clean会清理该目录。

产物：Lord of Mysteries-0.9.40-1.20.1.jar；SHA256：

~~~text
c19a6f1ab237ad52160f74261df16c9db3d3c65e31c7a20da41e88ce2cb87ae0
~~~

开发环境仍有Forge内部库mods.toml、union资源协议、Netty反射、原版缺音效与shader sampler、Gradle9弃用提示等警告。不将“构建成功”写成“日志零警告”，不擅自升级现有Forge工具链。

## 未验收与发布状态区别

- M6不眠者8–5/其余三途径、晨祷设施及M7–M12实质内容仍待推进；本轮按v0.9主表和既有代码进行有记录的适配，不复制“全状态起身”形成回血循环。
- 未执行四人8小时、20人性能、100小时公开测试；未验收所有渲染器、显卡、光影、资源包或整合包。新路径状态隔离自动测试不能替代真实多客户端网络验收。
- 未实现每技能专属骨骼动画、全部NPC新材质、神话形态、原创音效音乐或正式美术终审。16粒子和29生物模型配置的通用增强不等于这些内容全部完成。
- Jade/JEI是固定版本开发共存与原版配方发现性验收；不是生产混淆安装矩阵，亦没有专用研钵/坩埚JEI分类或Jade灵性插件。Curios/Create/Farmer's Delight/Oculus/Embeddium等具体组合未测。
- main代码、Wiki直推与Pages云端部署是三个独立状态。此前GitHub账号计费锁定使Actions零步骤失败；本轮发布后按精确SHA检查，不循环重跑、不更改付款/地区/安全设置。在线是否更新以公开Pages返回版本和对应运行结果为准。

[玩家手册](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/docs/M6_NIGHTWATCH.md) · [资产来源与配置](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/docs/VISUAL_POLISH_0.9.40.md) · [v0.9全章审计](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/docs/V09_COMPLETION_AUDIT.md) · [1.0接续设计](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/docs/PROJECT_MYSTERY_1_0_GUIDE.md)。
