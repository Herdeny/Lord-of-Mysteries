# GitHub Pages 信息架构与验证

> 适用版本：0.9.23-1.20.1
> 页面入口：<https://herdeny.github.io/Lord-of-Mysteries/>

## 目标

Pages 是玩家资料入口，不是把 README、Wiki 和设计文档拼成一张无限长页面。0.9.19 按以下顺序组织内容：

1. 当前版本、里程碑与真实门禁状态；
2. 新玩家、M2 玩家、M3 测试者、服主/贡献者四类入口；
3. 调查—收集—炼制—晋升—扮演—风险六步核心循环；
4. M2 已实现范围、真人门禁与 M3 `code_ready` 边界；
5. 五途径定位与一次一途径的序列 9–5 浏览器；
6. 可搜索、可分类、分批渲染的完整图鉴；
7. M0–M3 当前路线与折叠的 M4–M12 远期路线；
8. Wiki、专服矩阵、Issue 和许可入口。

## 本次修复的根因

旧版为所有主 section 设置 `.reveal { opacity: 0 }`，再依赖 `IntersectionObserver` 增加 `.in`。
从 README 或 Wiki 直接打开 `#catalog` 时，浏览器会先按空容器定位，动态内容随后改变页面高度；
部分视口下 Observer 不再触发该 section，最终只看到背景。

0.9.19 采用以下约束：

- section 内容默认永久可见，动画不参与可见性；
- 图鉴和序列完成同步渲染后重新定位 URL hash；
- 重新定位时临时关闭平滑滚动，避免长页面在中途停留；
- `html` 统一提供顶部滚动留白，section 不重复叠加 `scroll-margin`；
- `[hidden]` 使用全局 `display: none !important`，避免组件布局覆盖原生语义。

## 图鉴性能与交互

- 数据仍由 `scripts/gen_pages_catalog.py` 从 `ModItems`、`ModBlocks`、`ModEntities` 和双语语言资源生成。
- 完整数据为 187 条，其中实际注册内容为 91 物品、7 方块、8 实体；方块物品与方块按 ID 去重后共有 99 条注册记录，另含随代码同步维护的能力、机制、仪式、组织、世界与状态条目。
- 初始仅渲染 24 条；每次“加载更多”再增加 24 条。
- 搜索范围包含中文名、英文名、ID、摘要和标签。
- 切换分类或修改搜索词会重置为首批 24 条。
- 结果区同时显示匹配总数与已显示数量；无结果和末页不会显示无效加载按钮。

本地 Chrome 390×844 视口实测：旧页首次创建 173 张卡片，页面高约 53,792px；
新页首次创建 24 张卡片，页面高约 14,599px，横向溢出为 0。高度会随字体与浏览器缩放变化，
该数据只用于证明重构方向，不作为固定像素门禁。

0.9.20 将五种序列 5 晋升仪式加入资料站，完整可搜索条目增至 178；首批 24 条和移动端布局约束保持不变。

0.9.21 新增旅行家磁石空间标记与同行远门两条图鉴，完整可搜索条目增至 180；费用、冷却、同意、失败恢复和跨维安全边界均可全文搜索。

0.9.22 从注册表自动加入特性分离台、烙印洗涤台、探针、封蜡和洗涤香，并增加特性处理机制详细页；手工方块说明覆盖同 ID 的自动摘要后，图鉴保持 186 条唯一记录。

0.9.23 从 `ModItems` 自动加入身份盐环，并扩展非凡特性机制条目，完整展示额外负载、危险吸收、消化/灵性/压力权衡、单人/多人活体析出、稳定度阈值和失败恢复；图鉴现为 187 条唯一记录。CI 同时验证有序配方宽高，避免图鉴展示实际无法被 Forge 加载的配方物品。

## 可访问性

- 提供“跳到主要内容”链接和唯一 `h1`。
- 移动导航同步 `aria-expanded` 与可读标签。
- 途径浏览器使用 `tablist` / `tab` / `tabpanel`，支持左右方向键。
- 图鉴分类使用 `aria-pressed`，结果数量使用 `aria-live`。
- 图鉴卡使用原生按钮；详情弹窗获得焦点、限制 Tab 循环、支持 Esc 关闭并把焦点还给原卡片。
- `prefers-reduced-motion` 下关闭背景粒子、平滑滚动和长动画。

## 自动门禁

```bash
python scripts/gen_pages_catalog.py --check
python scripts/check_pages_layout.py
node --check docs/assets/project-meta.js
node --check docs/assets/roadmap-data.js
node --check docs/assets/wiki-data.js
node --check docs/assets/catalog-data.js
node --check docs/assets/wiki.js
```

`check_pages_layout.py` 验证：

- `zh-CN`、viewport、唯一 `main` / `h1` / DOM ID；
- 开始、玩法、途径、序列、图鉴、路线、资源和弹窗关键节点；
- 所有脚本/样式均为 `docs/assets` 本地资源，缓存版本与 `project-status.json` 一致；
- section 不依赖透明 reveal；
- 平板/手机断点、横向分类滚动和 `hidden` 保护；
- 24 条批次、加载更多、锚点恢复、ARIA 状态与弹窗焦点恢复。

## 维护流程

1. 游戏注册内容变化后运行 `python scripts/gen_pages_catalog.py`。
2. 版本或路线变化后运行 `python scripts/sync_project_metadata.py`。
3. 修改 `docs/index.html`、`docs/assets/wiki.css` 或 `docs/assets/wiki.js` 时同步更新本页、README、Wiki 和 CHANGELOG。
4. 本地至少验证桌面与 390×844 手机视口的首页、`#sequences`、`#catalog`。
5. 验证搜索、分类、加载更多、详情弹窗、Esc/焦点恢复、移动导航和明暗主题。
6. 发布前运行完整文档门禁和 `./gradlew clean build`。
