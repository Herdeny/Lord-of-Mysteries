# 0.9.41 验证记录与边界

日期：2026-11-12（Europe/London）。本轮为午夜诗人8与美术增量，不是v0.9全规格完成。

## 运行记录

已完成534项JUnit（0失败/0错误）、62/62 Forge GameTest。新增注册防漏测、非有限值、采梦反刷、材质/骨架断言；真实配方、进阶、控制恢复、共享抗性和寻路扮演均在Forge世界运行。
资源静态检查：641 JSON、2153双语键、859引用、194模型、172物品、16方块、30实体。
视觉校验：30 renderer、133原创sprite、16方块模型、16粒子provider。
当前内容图166节点/218关系/27孤立节点/36知识门过渡，未伪造为零缺口；121生存资源、354图鉴（含3项新增技能说明）。

当前发现并修复晋升NaN/Infinity绕过；鸟类脸部零件原先未随头转动，现改为同一枢轴。
新增GameTest类必须显式注册；首轮只运行旧53项，因此不算新内容通过，已补注册与自动防漏测JUnit。

## 测试发现的真实问题

- 交叉系统复查补上持久秘偶所有权与个人灵界遭遇排除；异常所有权标记同样拒绝，避免只看原版宠物接口遗漏M3/M5实体。
- 恐惧最初只尝试一次随机逃跑位置，在有可行地面的fixture中也可能所有目标不动。保留“必须实际寻路”的断言，增加有界扇形候选与path.canReach检查；不以播放粒子代替逃散成功。
- 旧模型断言认为猫头鹰面盘在root；改为验证面盘/耳羽/喙/发光眼都属于head，并校验有限角度，不删除形体差异断言。
- 研钵合同由14条更新到实际16条，原消耗、容器、安全输出断言全部保留。
- 首轮GameTest仅执行53旧项，不计为新功能验收；显式注册后执行62项，另加JUnit从源码验证所有GameTest类已注册。

## 范围

GameTest使用真实Forge世界、注册物、配方、导航和伤害事件；测试ServerPlayer替代网络发送，不等于两个真人客户端。
坩埚验证加速调用1200个tick，非真人连续60秒等候。
已完成Jade 11.13.3+forge / JEI 15.56.0.205可选配置clean build与62/62 GameTest；专服双启动、schema30 marker、种子一致、保存停服通过。回滚fixture及19 issues/68 comments乱码审计通过。
客户端在既有一次性测试世界“PM Visual QA 0.9.36”运行0.9.41，并加载Jade与JEI；实际查看四件新物品、手持魔药、更新材质的夜枭/不眠残影与成熟灵草提示，然后保存退出。未把创造模式物品展示当成生存全流程或真人施法验收。

- [材质、持物与Jade实拍](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/docs/assets/visuals/poet-materials-0.9.41.png)
- [JEI搜索新增物品实拍](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/docs/assets/visuals/poet-jei-0.9.41.png)
- [移动端图鉴详情实拍](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/docs/assets/visuals/poet-pages-mobile-0.9.41.png)

本地Pages检查354条图鉴、新PNG加载、搜索与详情弹窗、序列9→8顺序，以及约390像素移动端视口无横向溢出；服务器部署状态需按发布commit另行核对，不以本地网页代替线上部署。
未覆盖完整生产整合包、所有第三方AI/Boss/领地插件、长时多人平衡或全硬件GPU。
