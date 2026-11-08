package top.aurora.lordofmysteries.client.visual;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VisualBudgetTest {
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
