# 安装与构建

## 玩家安装

1. 安装 Minecraft Java Edition 1.20.1。
2. 安装 Forge 47.4.20。
3. 下载与当前版本号一致的 Mod Jar。
4. 将 Jar 放入游戏目录的 `mods/` 文件夹。
5. 使用对应 Forge 1.20.1 配置启动游戏。

默认按键：

| 按键 | 功能 |
|---|---|
| `V` | 灵视 |
| `B` | 简易占卜 |
| `N` | 非凡者档案 |
| `G` | 情绪读取 |
| `H` | 表层读心 |
| `J` | 心理暗示 |
| `K` | 挑衅 |
| `L` | 激怒 |

## 净化封印仪式

1. 使用骨粉和木炭制作 8 个仪式粉笔阵纹。
2. 以祭坛为中心，在 `(±3,0)`、`(0,±3)`、`(±2,±2)` 放置八点阵列。
3. 投入永燃火柴盒、纯净水×3、青兰花×5和白蜡烛×8。
4. 夜晚晴天空手点击祭坛启动；阵列中途破坏会取消并保留材料。
5. 正常结算会消耗纯净水和青兰花；失败可能增加压力、污染或生成失控体。

## 源码构建

需要 JDK 17：

```bash
./gradlew clean build
```

Windows：

```powershell
.\gradlew.bat clean build
```

构建产物位于 `build/libs/`。

## 开发运行

```bash
./gradlew runClient
./gradlew runServer
./gradlew runGameTestServer
```

如果 Forge 资源下载失败，先确认 Mojang CDN 与 Gradle 网络可用，再重试。

---

版本与更新时间见 [Wiki 首页](Home)。
