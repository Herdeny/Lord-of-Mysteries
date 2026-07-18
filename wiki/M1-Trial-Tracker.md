# M1 两小时试炼追踪器

> 当前版本：`0.9.8-1.20.1` · Capability schema 18

使用 `/pm trial start|resume|status|report|verify|stop|reset` 记录 M1 两小时玩法。命令无需管理员权限，离线时间不计入时长。

## 九项目标

- 两小时在线时间、营地、占卜家序列 7。
- 3 次神秘生物击杀、2 次有效扮演、风险峰值 25。
- 身份锚定、每日反思、雾都报社谋生。

## 七个里程碑

营地 10 分钟；序列 9/8/7 为 30/60/90 分钟；身份、反思、谋生为 100/110/120 分钟。

## 连续性

追踪器独立记录重连、专服重启、维度往返和死亡恢复。状态使用 schema 18 保存，并由 6 项 Forge GameTest 和同一世界双启动专服矩阵验证。

完整字段和命令见 [仓库文档](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/docs/M1_TRIAL_TRACKER.md)。
