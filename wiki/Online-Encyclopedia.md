# 在线资料站与可搜索图鉴

> 版本：0.9.20-1.20.1
> 入口：<https://herdeny.github.io/Lord-of-Mysteries/>

## 最快使用方式

| 目标 | 入口 |
|---|---|
| 第一次游玩 | [资料站首页](https://herdeny.github.io/Lord-of-Mysteries/#start) |
| 比较五条途径 | [途径定位](https://herdeny.github.io/Lord-of-Mysteries/#pathways) |
| 查看序列 9–5 | [序列浏览器](https://herdeny.github.io/Lord-of-Mysteries/#sequences) |
| 查物品、方块、实体或能力 | [可搜索图鉴](https://herdeny.github.io/Lord-of-Mysteries/#catalog) |
| 查看真实开发进度 | [路线概览](https://herdeny.github.io/Lord-of-Mysteries/#roadmap) |

## 图鉴怎么用

- 输入中文名、英文名、注册 ID 或标签关键词。
- 使用“能力、机制、魔药、方块、物品、封印物、仪式、组织、世界、状态”分类缩小范围。
- 首页先显示 24 条，点击“加载更多”继续浏览；所有 178 条仍可直接搜索。
- 点击任一卡片查看完整说明、标签和细节；按 Esc 可关闭并返回原卡片。
- 手机端分类是一排可横向滑动的按钮，图鉴卡为单列。

## 数据可信度

- 85 个物品、5 个方块、8 个实体由实际 Forge 注册表和中英语言资源自动生成。
- 能力、机制、仪式、组织与世界条目来自仓库维护数据。
- CI 会拒绝注册表、翻译、生成图鉴或页面布局契约漂移。
- `playable`、`code_ready`、真人门禁和规划内容会分别标注，不会把路线目标冒充已实现。

## 0.9.19 修复

- 修复外部链接直达 `#catalog` 时可能只显示空白背景。
- 修复动态内容载入后锚点停在错误区域。
- 首次渲染从 173 张卡片降为 24 张，显著缩短手机页面。
- 序列从 30 行长列表改为一次一途径的 Tab 浏览器。
- 补齐移动导航、键盘切换、弹窗焦点与 `hidden` 控件语义。

## 0.9.20 内容同步

- 新增提线、梦行、战火、盗梦与开门五种序列 5 晋升仪式条目。
- 图鉴总量从 173 条增至 178 条，仍由 24 条分批加载、全文搜索和分类筛选共同承载。

维护与验证细节见仓库文档
[`docs/PAGES_INFORMATION_ARCHITECTURE.md`](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/docs/PAGES_INFORMATION_ARCHITECTURE.md)。
