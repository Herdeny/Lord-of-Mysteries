package top.aurora.lordofmysteries.client.visual;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VisualBudgetTest {
    @Test
    void sustainedBurstsCannotExceedTheLifetimeBudget() {
        for (var density : VisualBudget.Density.values()) {
            var budget = new VisualBudget();
            int accepted = 0;
            for (int tick = 0; tick < 40; tick++) {
                for (int index = 0; index < density.perTick; index++) {
                    if (budget.allow(tick, density, 100)) accepted++;
                }
            }
            assertEquals(density.total, accepted);
            assertFalse(budget.allow(99, density, 100));
            assertTrue(budget.allow(100, density, 100));
        }
    }

    @Test
    void shorterParticlesExpireEvenWhenInsertedAfterLongerParticles() {
        var budget = new VisualBudget();
        for (int index = 0; index < 48; index++) assertTrue(budget.allow(0, VisualBudget.Density.LOW, 100));
        for (int tick = 1; tick <= 8; tick++) {
            for (int index = 0; index < 48; index++) budget.allow(tick, VisualBudget.Density.LOW, 10);
        }
        assertFalse(budget.allow(9, VisualBudget.Density.LOW, 10));
        assertTrue(budget.allow(11, VisualBudget.Density.LOW, 10));
    }

    @Test
    void invalidDurationsAndLoweredLimitsDoNotConsumeOrBypassBudget() {
        var budget = new VisualBudget();
        assertFalse(budget.allow(0, VisualBudget.Density.LOW, 0));
        assertFalse(budget.allow(0, VisualBudget.Density.LOW, 201));
        assertFalse(budget.allow(0, null, 10));
        for (int tick = 0; tick < 4; tick++) {
            for (int index = 0; index < 160; index++) budget.allow(tick, VisualBudget.Density.RICH, 50);
        }
        assertFalse(budget.allow(5, VisualBudget.Density.LOW, 50));
        budget.reset();
        assertTrue(budget.allow(5, VisualBudget.Density.LOW, 50));
    }
    @Test
    void everyDensityEnforcesAHardPerTickCap() {
        for (VisualBudget.Density density : VisualBudget.Density.values()) {
            VisualBudget budget = new VisualBudget();
            for (int index = 0; index < density.perTick; index++) assertTrue(budget.allow(80, density));
            for (int index = 0; index < 500; index++) assertFalse(budget.allow(80, density));
            assertTrue(budget.allow(81, density));
        }
    }

    @Test
    void worldTimeRollbackAndDisconnectRestoreBudget() {
        VisualBudget budget = new VisualBudget();
        for (int index = 0; index < 48; index++) assertTrue(budget.allow(900, VisualBudget.Density.LOW));
        assertFalse(budget.allow(900, VisualBudget.Density.LOW));
        assertTrue(budget.allow(0, VisualBudget.Density.LOW));
        for (int index = 0; index < 47; index++) budget.allow(0, VisualBudget.Density.LOW);
        assertFalse(budget.allow(0, VisualBudget.Density.LOW));
        budget.reset();
        assertTrue(budget.allow(0, VisualBudget.Density.LOW));
    }

    @Test
    void ReducingDensityCannotBypassCurrentTickLimit() {
        VisualBudget budget = new VisualBudget();
        for (int index = 0; index < 70; index++) assertTrue(budget.allow(1, VisualBudget.Density.RICH));
        assertFalse(budget.allow(1, VisualBudget.Density.LOW));
        assertTrue(budget.allow(2, VisualBudget.Density.LOW));
    }
}
