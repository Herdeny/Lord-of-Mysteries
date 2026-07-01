package top.aurora.lordofmysteries.ability;

import java.util.Locale;
import java.util.Random;

/**
 * 占卜可信度与结果扭曲（批次1，设计文档 §6 / §8）。
 *
 * <p>目标：把「结果对不对」和「玩家看到什么」分开。
 * 服务端算真实结果 + finalScore，客户端根据 clarity 等级把真实值扭曲展示。
 *
 * <p>公式：
 * <pre>
 *   baseClear     = baseBySequence + spiritualityPercent * 0.20
 *   interference  = Σ 干扰项（高序列实体、污染、屏蔽）
 *   finalScore    = baseClear − interference + Gaussian(0, 0.1)
 *
 *   clarity:
 *     finalScore ≥ 0.65 → CLEAR
 *     finalScore ≥ 0.30 → BLURRED
 *     otherwise         → WRONG
 * </pre>
 *
 * <p>所有关键计算都是纯静态方法，不依赖 Minecraft 对象，便于 JUnit5 覆盖。
 */
public final class DivinationCredibility {

    private DivinationCredibility() {}

    /** 可信度等级。展示时决定是否扭曲。 */
    public enum Clarity {
        CLEAR,    // 完整真实
        BLURRED,  // 模糊、片段
        WRONG;    // 反向或错误

        /** 中文本地化文本，供 HUD/聊天使用。 */
        public String displayZh() {
            return switch (this) {
                case CLEAR -> "清晰";
                case BLURRED -> "模糊";
                case WRONG -> "错误";
            };
        }
    }

    /** 基础清晰度阈值：≥ 0.65 清晰、≥ 0.30 模糊、否则错误。 */
    public static final double THRESHOLD_CLEAR = 0.65;
    public static final double THRESHOLD_BLURRED = 0.30;

    /**
     * 按序列返回基础可信度基线。序列越高，占卜越准。
     *
     * @param sequence 玩家序列（9 = 最低占卜家）
     */
    public static double baseClearBySequence(int sequence) {
        // 数值：9→0.60、8→0.68、7→0.76、6→0.84、5→0.90，其余高序列保底 0.95。
        if (sequence >= 9) return 0.60;
        if (sequence == 8) return 0.68;
        if (sequence == 7) return 0.76;
        if (sequence == 6) return 0.84;
        if (sequence == 5) return 0.90;
        if (sequence < 5 && sequence >= 0) return 0.95;
        return 0.30; // 非占卜家/普通人硬性下限
    }

    /**
     * 综合基础清晰度：随剩余灵性百分比线性提升，上限 +0.20。
     *
     * @param sequence 序列
     * @param spiritualityPercent 当前灵性 / 上限，取 [0,1]
     */
    public static double baseClear(int sequence, double spiritualityPercent) {
        double p = clamp01(spiritualityPercent);
        return baseClearBySequence(sequence) + p * 0.20;
    }

    /**
     * 计算总干扰值。
     *
     * @param highSequenceNearby 32 格内其他非凡者高序列（序列 ≤ 6）实体的个数，每个 +0.15
     * @param pollution          玩家污染度 0-100，>50 起每点 +0.006
     * @param shieldStrength     屏蔽媒介强度，直接叠加
     */
    public static double interference(int highSequenceNearby, double pollution, double shieldStrength) {
        double sum = 0.0;
        sum += Math.max(0, highSequenceNearby) * 0.15;
        if (pollution > 50.0) sum += (pollution - 50.0) * 0.006;
        sum += Math.max(0.0, shieldStrength);
        return sum;
    }

    /**
     * finalScore = baseClear − interference + Gaussian(0, 0.1)。
     *
     * <p>Random 由调用方注入，纯逻辑测试可传入固定种子。生产代码使用玩家 UUID + tick 作为种子。
     */
    public static double finalScore(double baseClear, double interference, Random rng) {
        double noise = rng.nextGaussian() * 0.10;
        return baseClear - interference + noise;
    }

    /**
     * 无噪声版本，测试用；生产不要用。
     */
    public static double finalScoreDeterministic(double baseClear, double interference) {
        return baseClear - interference;
    }

    /** 分级。 */
    public static Clarity classify(double finalScore) {
        if (finalScore >= THRESHOLD_CLEAR) return Clarity.CLEAR;
        if (finalScore >= THRESHOLD_BLURRED) return Clarity.BLURRED;
        return Clarity.WRONG;
    }

    /**
     * 把真实方向向量（yaw 弧度）按 clarity 扭曲。
     * <ul>
     *   <li>CLEAR：不变；</li>
     *   <li>BLURRED：叠加 [-π/6, π/6] 内的偏移；</li>
     *   <li>WRONG：偏移 [-π, π]（相当于任意方向）。</li>
     * </ul>
     */
    public static double distortDirection(double trueYawRad, Clarity clarity, Random rng) {
        return switch (clarity) {
            case CLEAR -> trueYawRad;
            case BLURRED -> trueYawRad + (rng.nextDouble() * 2.0 - 1.0) * (Math.PI / 6.0);
            case WRONG -> trueYawRad + (rng.nextDouble() * 2.0 - 1.0) * Math.PI;
        };
    }

    /**
     * 把真实文字按 clarity 扭曲：BLURRED 用「…」遮挡部分字符，WRONG 打乱。
     *
     * <p>此处刻意不依赖 Minecraft Component，方便复用与测试。渲染层再套颜色/字体。
     */
    public static String distortText(String truth, Clarity clarity, Random rng) {
        if (truth == null || truth.isEmpty()) return "";
        return switch (clarity) {
            case CLEAR -> truth;
            case BLURRED -> blur(truth, rng);
            case WRONG -> scramble(truth, rng);
        };
    }

    private static String blur(String s, Random rng) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            sb.append(rng.nextDouble() < 0.35 ? '…' : s.charAt(i));
        }
        return sb.toString();
    }

    private static String scramble(String s, Random rng) {
        char[] chars = s.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            char t = chars[i];
            chars[i] = chars[j];
            chars[j] = t;
        }
        return new String(chars);
    }

    /** 生成人类可读的方向词（八方位）。 */
    public static String yawToCardinal8(double yawRad) {
        double deg = normalizeDeg(Math.toDegrees(yawRad));
        String[] labels = {"北", "东北", "东", "东南", "南", "西南", "西", "西北"};
        int idx = (int) Math.round(deg / 45.0) % 8;
        return labels[(idx + 8) % 8];
    }

    static double normalizeDeg(double deg) {
        double d = deg % 360.0;
        if (d < 0) d += 360.0;
        return d;
    }

    /** 生成一行调试字符串，测试断言用。 */
    public static String debug(double baseClear, double interference, double score) {
        return String.format(Locale.ROOT, "base=%.3f interf=%.3f score=%.3f clarity=%s",
                baseClear, interference, score, classify(score));
    }

    static double clamp01(double v) {
        return v < 0.0 ? 0.0 : (v > 1.0 ? 1.0 : v);
    }
}
