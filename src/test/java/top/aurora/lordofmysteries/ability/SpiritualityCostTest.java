package top.aurora.lordofmysteries.ability;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import top.aurora.lordofmysteries.player.PlayerMysteryData;

/**
 * 灵性消耗 API 单元测试（批次1）。
 *
 * <p>覆盖：足量扣、余量不足拒扣、退款封顶、负值防御。
 */
class SpiritualityCostTest {

    @Test
    void tryConsumeSucceedsWhenEnough() {
        PlayerMysteryData d = new PlayerMysteryData();
        d.spirituality = 50f;
        d.spiritualityMax = 122f;
        assertTrue(SpiritualityCost.tryConsume(d, 15f));
        assertEquals(35f, d.spirituality, 1e-6);
    }

    @Test
    void tryConsumeFailsWhenInsufficient() {
        PlayerMysteryData d = new PlayerMysteryData();
        d.spirituality = 10f;
        d.spiritualityMax = 122f;
        assertFalse(SpiritualityCost.tryConsume(d, 15f));
        assertEquals(10f, d.spirituality, 1e-6, "失败时不应扣费");
    }

    @Test
    void canPayRejectsNegativeCost() {
        PlayerMysteryData d = new PlayerMysteryData();
        d.spirituality = 100f;
        assertFalse(SpiritualityCost.canPay(d, -1f));
    }

    @Test
    void refundIsClampedByMax() {
        PlayerMysteryData d = new PlayerMysteryData();
        d.spirituality = 100f;
        d.spiritualityMax = 122f;
        SpiritualityCost.refund(d, 999f);
        assertEquals(122f, d.spirituality, 1e-6);
    }

    @Test
    void refundIgnoresNonPositive() {
        PlayerMysteryData d = new PlayerMysteryData();
        d.spirituality = 30f;
        SpiritualityCost.refund(d, 0f);
        SpiritualityCost.refund(d, -5f);
        assertEquals(30f, d.spirituality, 1e-6);
    }

    @Test
    void forceConsumeReturnsActualAmount() {
        PlayerMysteryData d = new PlayerMysteryData();
        d.spirituality = 5f;
        d.spiritualityMax = 100f;
        // 想扣 20，只有 5，实际扣 5
        assertEquals(5f, SpiritualityCost.forceConsume(d, 20f), 1e-6);
        assertEquals(0f, d.spirituality, 1e-6);
    }

    @Test
    void cooldownReadyAndRemaining() {
        assertTrue(AbilityCooldowns.ready(100L, 100L));
        assertTrue(AbilityCooldowns.ready(100L, 200L));
        assertFalse(AbilityCooldowns.ready(200L, 100L));
        assertEquals(100L, AbilityCooldowns.remaining(200L, 100L));
        assertEquals(0L, AbilityCooldowns.remaining(50L, 100L));
    }

    @Test
    void cooldownStartAddsDuration() {
        assertEquals(1300L, AbilityCooldowns.start(100L, 1200L));
        // 负持续时间被夹到 0
        assertEquals(100L, AbilityCooldowns.start(100L, -50L));
    }
}
