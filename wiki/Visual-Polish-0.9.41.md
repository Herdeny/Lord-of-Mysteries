# 0.9.41 视觉增量与资源来源

更新日期：2026-11-12（Europe/London）。本轮使用内置imagegen生成原创栅格美术，不下载或移植其他模组素材。

## 已接入而非仅概念图

- materials-0.9.41.png：保持16种材质的格位与配色，细化织物、纸、黑铁、黄铜、瓷、鳞、树皮、翼片、羽、木、符石、灵性玻璃、灰肤、熔岩、虚空、银色符纹。
- 游戏共享实体图集由256×256提高到512×512；不改变模型UV逻辑坐标。仅这一张允许512，物品64、粒子32、工位材料64，避免无边界放大所有纹理。
- 四格poet-items-0.9.41.png提供梦呓结晶、鸦墨羽笔、午夜诗剂、午夜诗人魔药；目前共133张原创物品sprite。原版生成蛋仍保留标准外观。
- 鸟类与飞蛾头部改为真实枢轴，喙、眼睛、猫头鹰面盘/耳羽、冠羽、触角随头部转向，避免脸部零件脱离。夜枭嵌套发光眼同步变换。
- 低吟24点页符环、恐惧暗片、安眠梦泡、采集提示以及暗处仅本人可见的韵律页符均来自既有粒子预算系统。

## 可重复来源链

源图、完整提示词与SHA-256都保存在 [美术清单](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/docs/art-source/manifest.json)。
此前 materials.png 和0.9.40源图保留不覆盖；新版为独立版本文件。
运行 python scripts/import_visual_atlases.py 可按清单再生成游戏PNG与Pages图标。
随后运行 python scripts/check_visual_assets.py，校验源图哈希、真实PNG块CRC、尺寸、注册引用与网页/游戏字节一致。
JUnit验证每张sprite可见、透明留白、无重复、模型预算及鸟头骨架关系。

## 不冒称完成

本轮没有新原创音轨或逐帧施法手部动画；共享材质不等于每种生物独立全身UV美术。
源图由内置生成工具产出并检查后接入，授权沿用ASSET_LICENSE；不代表取得原作IP授权。
低动态、关闭发光和粒子密度设置保留。真正实机验收与未测边界见 [本版验证](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/docs/VALIDATION_0.9.41.md)。
