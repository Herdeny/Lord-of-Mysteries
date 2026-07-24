package top.aurora.lordofmysteries.commission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import top.aurora.lordofmysteries.world.MistCityWorldEvent;

class CityEconomyPolicyTest {

    @Test
    void sharedDailyLimitCannotBeBypassedByChangingJobs() {
        assertTrue(CityEconomyPolicy.canWork(Long.MIN_VALUE, 12L));
        assertTrue(CityEconomyPolicy.canWork(11L, 12L));
        assertFalse(CityEconomyPolicy.canWork(12L, 12L));
    }

    @Test
    void everyJobHasAPlayableClearDayTradeoff() {
        for (CityEconomyPolicy.Job job : CityEconomyPolicy.Job.values()) {
            CityEconomyPolicy.ShiftTerms terms =
                    CityEconomyPolicy.terms(
                            job, MistCityWorldEvent.CLEAR);
            assertTrue(terms.rewardPence() > 0L);
            assertTrue(terms.paperCost() >= 0);
            assertTrue(terms.pressureIncrease() >= 0f);
            assertTrue(terms.exposureReduction() > 0f);
        }
        assertEquals(3, CityEconomyPolicy.terms(
                CityEconomyPolicy.Job.PRESS,
                MistCityWorldEvent.CLEAR).paperCost());
        assertEquals(0, CityEconomyPolicy.terms(
                CityEconomyPolicy.Job.AGENCY,
                MistCityWorldEvent.CLEAR).paperCost());
    }

    @Test
    void hazardousEventsPayMoreButNeverHideTheirRisk() {
        for (CityEconomyPolicy.Job job : CityEconomyPolicy.Job.values()) {
            CityEconomyPolicy.ShiftTerms clear =
                    CityEconomyPolicy.terms(
                            job, MistCityWorldEvent.CLEAR);
            for (MistCityWorldEvent event
                    : MistCityWorldEvent.values()) {
                CityEconomyPolicy.ShiftTerms eventTerms =
                        CityEconomyPolicy.terms(job, event);
                assertTrue(eventTerms.rewardPence() > 0L);
                assertTrue(eventTerms.pressureIncrease() >= 0f);
                assertTrue(eventTerms.exposureReduction() > 0f);
                assertEquals(clear.paperCost(), eventTerms.paperCost());
                if (event == MistCityWorldEvent.CLEAR) continue;
                assertTrue(eventTerms.rewardPence()
                        > clear.rewardPence());
                assertTrue(eventTerms.pressureIncrease()
                        > clear.pressureIncrease());
                assertTrue(eventTerms.exposureReduction()
                        > clear.exposureReduction());
            }
        }
    }

    @Test
    void invalidInputsCannotCreateFreeOrUndefinedShifts() {
        assertThrows(IllegalArgumentException.class,
                () -> CityEconomyPolicy.canWork(0L, -1L));
        assertThrows(IllegalArgumentException.class,
                () -> CityEconomyPolicy.terms(
                        null, MistCityWorldEvent.CLEAR));
        assertThrows(IllegalArgumentException.class,
                () -> CityEconomyPolicy.terms(
                        CityEconomyPolicy.Job.PRESS, null));
    }
}
