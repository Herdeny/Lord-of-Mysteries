package top.aurora.lordofmysteries.ability;

/**
 * 能力冷却工具（批次1）。
 *
 * <p>冷却统一用「结束时的世界 gameTime tick」表达：
 * <ul>
 *   <li>好处 1：无需在离线期间递减，重新登录也不会「白等」；</li>
 *   <li>好处 2：直接与 {@code level().getGameTime()} 比较即可判定，逻辑无状态；</li>
 *   <li>好处 3：便于 NBT 持久化。</li>
 * </ul>
 *
 * gameTime 由服务端每 tick 单调递增，客户端拿到的是同步值；离线期间仍在推进。
 */
public final class AbilityCooldowns {

    private AbilityCooldowns() {}

    /** 判定冷却是否已过。 */
    public static boolean ready(long endTick, long nowTick) {
        return nowTick >= endTick;
    }

    /** 返回剩余 tick 数；已就绪返回 0。 */
    public static long remaining(long endTick, long nowTick) {
        return Math.max(0L, endTick - nowTick);
    }

    /** 计算下一次冷却结束的 tick。 */
    public static long start(long nowTick, long durationTicks) {
        return nowTick + Math.max(0L, durationTicks);
    }
}
