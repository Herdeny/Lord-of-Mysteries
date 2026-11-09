# 0.9.40 · 视觉动作与兼容基线

日期：2026-11-09；本轮是可玩内容和可复用表现层的增量，不是全部原著技能、高序列神话形态或最终美术验收。

## 本轮新增与普及

- 6款独立64×64 RGBA物品贴图：夜枭视络、咖啡豆、浓黑咖啡、暗影蘑菇、守夜辅料、不眠者魔药。所有运行PNG与Pages图标逐字节相同。
- 夜枭增加面盘、耳羽、分层翼羽；不眠残影增加兜帽、新月胸饰、垂落末梢。当前29个生物模型配置+旅行门专用渲染器，共30实体渲染器。
- 所有通用生物模型使用重置式姿态计算，加入呼吸、攻击前倾、差异相位、收翅/飞行、受攻击姿态与骑乘兼容。不会逐帧累加旋转；异常浮点输入被限制。原版受伤/死亡方向流程继续保留。
- 16种现有粒子使用8类语义运动：灵光盘旋、雾气扩散、符文旋转、丝线收束、气泡上升、余烬上飘收缩、气环扩展、花瓣飘落。淡入/淡出平滑，没有新增闪屏或相机震动。
- 这些粒子只展示，不新建伤害实体、不改碰撞、不扩大夜视权限。隐身生物不渲染发光层。

## 可关闭和性能边界

客户端 `config/lord_of_mysteries-client.toml`：

| 设置 | 默认 | 作用 |
|---|---|---|
| particleDensity | STANDARD | LOW/ STANDARD/ RICH，每tick最多48/96/160，寿命队列最多350/1200/2200 |
| reducedMotion | false | 关闭呼吸、漂浮、粒子旋转和盘旋；保留攻击前倾、移动和飞行翼拍以免丢失战斗信息 |
| emissiveDetails | true | 可关闭模型额外自发光、粒子全亮；保留基础材质 |
| ambientParticles | true | 可关闭维度装饰粒子；不关闭技能文本与结算反馈 |

不修改玩家粒子全局设置；仍受原版限制。每生物≤40分部、共享256材质集。没有引入GeckoLib、额外渲染管线或客户端类到专服入口。

## 代表性设计借鉴，不复制资源

- [Botania官方词典](https://botaniamod.net/lexicon.html)：参考“材料—操作—结果—下一步”的游戏内学习方式，本轮通过真实配方、状态命令和手册串联；没有搬运植物/材质/配方。
- [GeckoLib4官方控制器说明](https://github.com/bernie-g/geckolib/wiki/The-Animation-Controller-%28Geckolib4%29)：参考动作分层与状态切换思路。仓库现有ModelPart保持优先，本轮不是安装GeckoLib，也不声称具备骨骼过渡完整实现。
- [Forge1.20.1按键文档](https://docs.minecraftforge.net/en/1.20.1/misc/keymappings/)：复用既有客户端按键和服务端能力入口，保留重绑定与冲突检查。
- [JEI作者项目](https://modrinth.com/mod/jei)、[Jade作者项目](https://modrinth.com/mod/jade)：进行下面的真实共存检查，不将“可启动”宣传成所有API深度集成。

## 可复现的兼容配置

`./gradlew runGameTestServer -PpmCompatSmoke` / `./gradlew runClient -PpmCompatSmoke`：

- Jade：11.13.3+forge，Modrinth固定版本ID `xJQHCmWJ`。
- JEI：15.56.0.205，固定版本ID `9jqubC9n`（release，不选择beta）。
- 只在显式属性开启时下载至开发缓存、添加runtimeOnly；不打进成品JAR，不变成必需依赖。不开属性即是“可选mod缺失”路径。
- 首次实际客户端共存发现Jade字体Mixin引用仍是SRG名，开发环境无法匹配；已添加**仅兼容开发运行**的refmap重映射和createSrgToMcp任务依赖。没有禁用Mixin、删掉异常或降低安全校验。
- [Mixin官方源码说明](https://github.com/SpongePowered/Mixin/blob/master/src/main/java/org/spongepowered/asm/mixin/MixinEnvironment.java)描述该开发映射设置；不把开发映射修正误报为用户生产整合包冲突修复。

实际执行结果、失败修复和截图在[本轮验证记录](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/docs/VALIDATION_0.9.40.md)。未实测的OptiFine/Oculus/Embeddium/光影/Curios/Create/Farmer's Delight/整包组合仍为待办；JEI专用研钵与坩埚分类、Jade专用灵性数据展示属于后续M7，不冒称已集成。

## 1.0铺垫

本轮提取了无客户端依赖的纯数学运动曲线测试入口、独立保存的守夜状态、固定可选依赖兼容开关和同源网页图标生成流程。1.0可继续扩展恐惧/夜巡/多工位及分层动作，但仍沿用可恢复存档、服务器权威、反向测试和低配降级约束。

[1.0指导书](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/docs/PROJECT_MYSTERY_1_0_GUIDE.md)是后续设计；[v0.9全章审计](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/docs/V09_COMPLETION_AUDIT.md)保留未完成项；不能用资源注册数量证明文档全部完成。

## 资产来源

内置image_gen原创生成；[完整提示词与SHA256](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/docs/art-source/NIGHTWATCH_0.9.40.md)，原图 `docs/art-source/nightwatch-items-0.9.40.png`。网格导入脚本已支持非正方形图集；没有复制原著插画或其他mod资源。名称/设定权利仍归原权利方，遵循现有许可边界，本轮不替代法律授权。
