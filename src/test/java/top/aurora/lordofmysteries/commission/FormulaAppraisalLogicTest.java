package top.aurora.lordofmysteries.commission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class FormulaAppraisalLogicTest {

    @Test
    void dossierSeedIsDeterministicPerCase() {
        UUID player = UUID.fromString("cf686553-8319-44bc-a972-6f751cccfb20");
        long first = FormulaAppraisalLogic.dossierSeed(41L, player, 900L);
        assertEquals(first, FormulaAppraisalLogic.dossierSeed(41L, player, 900L));
        assertNotEquals(first, FormulaAppraisalLogic.dossierSeed(41L, player, 901L));
    }

    @Test
    void authenticDossierHasThreeConsistentClues() {
        assertEquals(0b111, FormulaAppraisalLogic.clueMask(55L, true));
    }

    @Test
    void forgedDossierAlwaysContainsAnAnomaly() {
        for (long seed = 0; seed < 200; seed++) {
            assertNotEquals(0b111, FormulaAppraisalLogic.clueMask(seed, false));
        }
    }

    @Test
    void spiritualMethodTakesPriorityForPreparedSeer() {
        assertEquals(FormulaAppraisalLogic.Method.SPIRITUAL,
                FormulaAppraisalLogic.selectMethod(true, 6f, true, true));
        assertEquals(FormulaAppraisalLogic.Method.REAGENT,
                FormulaAppraisalLogic.selectMethod(false, 0f, true, true));
    }

    @Test
    void verdictMustMatchHiddenAuthenticity() {
        assertTrue(FormulaAppraisalLogic.verdictMatches(true, true));
        assertFalse(FormulaAppraisalLogic.verdictMatches(true, false));
    }
}
