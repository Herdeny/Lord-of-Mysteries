package top.aurora.lordofmysteries.ability;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import org.junit.jupiter.api.Test;

import top.aurora.lordofmysteries.ability.DivinationCredibility.Clarity;

/**
 * 占卜可信度纯逻辑测试（批次1）。
 *
 * <p>覆盖：
 * <ul>
 *   <li>baseClearBySequence 单调递增；</li>
 *   <li>baseClear 随灵性百分比线性上升；</li>
 *   <li>interference 三种来源相加；</li>
 *   <li>classify 分段阈值；</li>
 *   <li>distortText / distortDirection 在 CLEAR 下保持不变；</li>
 *   <li>yawToCardinal8 覆盖 8 方向。</li>
 * </ul>
 */
class DivinationCredibilityTest {

    private static final double EPS = 1e-9;

    @Test
    void baseClearBySequenceIsMonotonic() {
        double s9 = DivinationCredibility.baseClearBySequence(9);
        double s7 = DivinationCredibility.baseClearBySequence(7);
        double s5 = DivinationCredibility.baseClearBySequence(5);
        assertTrue(s9 < s7, "序列 9 应低于序列 7");
        assertTrue(s7 < s5, "序列 7 应低于序列 5");
    }

    @Test
    void baseClearAddsSpiritualityBonus() {
        double lo = DivinationCredibility.baseClear(9, 0.0);
        double mid = DivinationCredibility.baseClear(9, 0.5);
        double hi = DivinationCredibility.baseClear(9, 1.0);
        assertEquals(0.60, lo, EPS);
        assertEquals(0.70, mid, EPS);
        assertEquals(0.80, hi, EPS);
    }

    @Test
    void baseClearClampsPercent() {
        // 负数/超 1 均应被 clamp
        assertEquals(0.60, DivinationCredibility.baseClear(9, -1.0), EPS);
        assertEquals(0.80, DivinationCredibility.baseClear(9, 5.0), EPS);
    }

    @Test
    void interferenceSumsSources() {
        double i = DivinationCredibility.interference(2, 60.0, 0.10);
        // 2*0.15 + (60-50)*0.006 + 0.10 = 0.30 + 0.06 + 0.10 = 0.46
        assertEquals(0.46, i, 1e-9);
    }

    @Test
    void interferenceIgnoresLowPollution() {
        double i = DivinationCredibility.interference(0, 40.0, 0.0);
        assertEquals(0.0, i, EPS);
    }

    @Test
    void classifyBoundaries() {
        assertEquals(Clarity.CLEAR, DivinationCredibility.classify(0.65));
        assertEquals(Clarity.CLEAR, DivinationCredibility.classify(1.20));
        assertEquals(Clarity.BLURRED, DivinationCredibility.classify(0.30));
        assertEquals(Clarity.BLURRED, DivinationCredibility.classify(0.64));
        assertEquals(Clarity.WRONG, DivinationCredibility.classify(0.29));
        assertEquals(Clarity.WRONG, DivinationCredibility.classify(-1.0));
    }

    @Test
    void finalScoreDeterministicIsPurelyDifference() {
        assertEquals(0.30, DivinationCredibility.finalScoreDeterministic(0.60, 0.30), EPS);
    }

    @Test
    void finalScoreWithNoiseNearBase() {
        // 高斯 σ=0.1，10000 次均值应接近 baseClear-interference
        Random r = new Random(42);
        double sum = 0;
        int n = 10_000;
        for (int i = 0; i < n; i++) {
            sum += DivinationCredibility.finalScore(0.60, 0.10, r);
        }
        double mean = sum / n;
        assertEquals(0.50, mean, 0.01, "均值应接近 0.50，实际 " + mean);
    }

    @Test
    void clearDoesNotDistort() {
        Random r = new Random(1);
        String truth = "危险位于北方 12 格";
        assertEquals(truth, DivinationCredibility.distortText(truth, Clarity.CLEAR, r));
        assertEquals(1.234, DivinationCredibility.distortDirection(1.234, Clarity.CLEAR, r), EPS);
    }

    @Test
    void blurredDistortIsBoundedButChanges() {
        Random r = new Random(1);
        double base = 1.0;
        double d = DivinationCredibility.distortDirection(base, Clarity.BLURRED, r);
        assertTrue(Math.abs(d - base) <= Math.PI / 6.0 + EPS,
                "模糊偏移应在 ±π/6 内，实际偏移 " + (d - base));
    }

    @Test
    void wrongDistortIsWithinFullCircle() {
        Random r = new Random(3);
        double d = DivinationCredibility.distortDirection(0.0, Clarity.WRONG, r);
        assertTrue(Math.abs(d) <= Math.PI + EPS);
    }

    @Test
    void yawToCardinalCoversEightDirections() {
        assertEquals("北", DivinationCredibility.yawToCardinal8(Math.toRadians(0)));
        assertEquals("东北", DivinationCredibility.yawToCardinal8(Math.toRadians(45)));
        assertEquals("东", DivinationCredibility.yawToCardinal8(Math.toRadians(90)));
        assertEquals("东南", DivinationCredibility.yawToCardinal8(Math.toRadians(135)));
        assertEquals("南", DivinationCredibility.yawToCardinal8(Math.toRadians(180)));
        assertEquals("西南", DivinationCredibility.yawToCardinal8(Math.toRadians(225)));
        assertEquals("西", DivinationCredibility.yawToCardinal8(Math.toRadians(270)));
        assertEquals("西北", DivinationCredibility.yawToCardinal8(Math.toRadians(315)));
    }

    @Test
    void yawNormalizesNegative() {
        assertEquals("西", DivinationCredibility.yawToCardinal8(Math.toRadians(-90)));
    }

    @Test
    void debugStringMentionsClarity() {
        String s = DivinationCredibility.debug(0.6, 0.1, 0.5);
        assertTrue(s.contains("BLURRED"), s);
    }
}
