package top.aurora.lordofmysteries.ability;

import top.aurora.lordofmysteries.player.PlayerMysteryData;

/**
 * 灵性消耗 API（批次1，设计文档 §5.2 / §6）。
 *
 * <p>所有能力扣费必须走这里，保证：
 * <ul>
 *   <li>灵性不足时直接返回失败（能力调用方决定后续反馈）；</li>
 *   <li>扣费与判断合成一个原子步骤，避免出现「先算完再扣」的时序漏洞；</li>
 *   <li>扣费值始终 clamp 在 [0, spiritualityMax]。</li>
 * </ul>
 *
 * 这里刻意保持成静态工具类：能力实现通常只需要「能不能扣、扣多少」两件事，
 * 抽出对象反而增加噪声。真正需要生命周期的能力状态放在 {@link PlayerMysteryData}。
 */
public final class SpiritualityCost {

    private SpiritualityCost() {}

    /** 查询是否够扣。 */
    public static boolean canPay(PlayerMysteryData data, float cost) {
        return data != null && cost >= 0f && data.spirituality >= cost;
    }

    /**
     * 尝试扣除灵性。
     *
     * @return true 扣费成功；false 灵性不足（未修改数据）
     */
    public static boolean tryConsume(PlayerMysteryData data, float cost) {
        if (!canPay(data, cost)) return false;
        data.spirituality = clamp(data.spirituality - cost, 0f, data.spiritualityMax);
        return true;
    }

    /** 无条件扣费（内部工具用；能量已提前校验的场景），返回实际扣掉的量。 */
    public static float forceConsume(PlayerMysteryData data, float cost) {
        float before = data.spirituality;
        data.spirituality = clamp(before - Math.max(0f, cost), 0f, data.spiritualityMax);
        return before - data.spirituality;
    }

    /** 恢复灵性（能力取消/退款）。 */
    public static void refund(PlayerMysteryData data, float amount) {
        if (amount <= 0f) return;
        data.spirituality = clamp(data.spirituality + amount, 0f, data.spiritualityMax);
    }

    static float clamp(float v, float lo, float hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }
}
