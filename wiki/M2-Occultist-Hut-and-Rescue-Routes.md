# 神秘学家小屋、真假配方与三解救援

> 当前版本：0.8.9-1.20.1 · M2 可玩预研

## 真假配方案件

完成“失踪的调查小队·第一阶段”后，雾都委托板会开放第三份案件“真假配方”。

1. 使用 `/pm case` 定位世界种子确定的神秘学家小屋。
2. 与神秘学鉴定师莫尔交谈并领取密封配方卷宗。
3. 右键卷宗或执行 `/pm formula inspect`。
4. 占卜家可支付 6 灵性；其他途径可携带占卜水晶并消耗 1 份神秘墨水。
5. 三项证据全部一致时提交 `/pm formula verdict authentic`；任何异常则提交 `forged`。
6. 错误结论增加 8 点失控压力但允许重试；正确后把卷宗交还莫尔结算。

小屋距离出生区约 8–10 区块，旧世界会补生成。莫尔被清除或卷宗丢失后均可自动恢复，避免永久卡步。

## 记者救援三解

在失踪小队记者营救步骤执行：

| 命令 | 条件 | 结果 |
|---|---|---|
| `/pm commission approach assault` | 无 | 生成四名队伍隔离守卫，全部击败后营救 |
| `/pm commission approach stealth` | 偷盗者/学徒序列 9+ | 在线队员获得限时隐形和加速 |
| `/pm commission approach divination` | 占卜家序列 9+、12 灵性 | 找到安全接近路线 |

路线会锁定到当前委托并同步给 2–4 名同队玩家；队伍持久账本支持离线追赶，结算或放弃按成员处理。Capability schema 15 保证死亡、重登、
跨维度和服务器保存后仍能恢复。

## 自动验证

- 数据基线：3 个委托、3 条任务链、15 类目标。
- `scripts/check_m2_investigation.py` 保护旧 13 步索引、三解、真假配方和双语键。
- 当前自动化基线：183 项测试、统一资源完整性、持久队伍恢复与 Forge 专服运行诊断冒烟。

完整工程说明见
[`docs/M2_OCCULTIST_HUT_AND_RESCUE_ROUTES.md`](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/docs/M2_OCCULTIST_HUT_AND_RESCUE_ROUTES.md)。
