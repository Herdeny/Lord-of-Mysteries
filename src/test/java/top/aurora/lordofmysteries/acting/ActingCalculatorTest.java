package top.aurora.lordofmysteries.acting;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ActingCalculatorTest {

    @Test
    void firstTriggerHasFullNovelty() {
        assertEquals(1f, ActingCalculator.novelty(0L, 100L, 1200L));
    }

    @Test
    void repeatedTriggerIsClampedToAntiFarmFloor() {
        assertEquals(0.1f, ActingCalculator.novelty(100L, 101L, 1200L));
    }

    @Test
    void riskScalesToOnePointFiveAtMaximumPressure() {
        assertEquals(1.5f, ActingCalculator.risk(100f));
    }

    @Test
    void gainIncludesPotionQualityAndServerMultiplier() {
        assertEquals(7f, ActingCalculator.gain(10f, 1f, 1f, 1f, 0.7f, 1f));
    }

    @Test
    void penaltiesAreNotAmplifiedByRisk() {
        assertEquals(-5f, ActingCalculator.gain(-5f, 1.2f, 1f, 1.5f, 1.2f, 10f));
    }
}
