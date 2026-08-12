package top.aurora.lordofmysteries.spirit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;

import org.junit.jupiter.api.Test;

class SpiritExpeditionPolicyTest {

    @Test
    void routeGenerationIsDeterministicAcrossAllProjectionsAndWeathers() {
        EnumSet<SpiritWeather> observedWeather =
                EnumSet.noneOf(SpiritWeather.class);
        EnumSet<SpiritEncounter> observedEncounters =
                EnumSet.noneOf(SpiritEncounter.class);
        for (SpiritProjection projection : SpiritProjection.values()) {
            for (long seed = 0; seed < 512; seed++) {
                SpiritWeather weather = SpiritExpeditionPolicy.weatherFor(
                        seed, projection);
                observedWeather.add(weather);
                int tide = SpiritExpeditionPolicy.tideFor(seed);
                for (int step = 0;
                     step < SpiritExpeditionPolicy.ROUTE_LEGS; step++) {
                    SpiritDirection first =
                            SpiritExpeditionPolicy.expectedDirection(
                                    seed, step, tide, projection, weather);
                    SpiritDirection second =
                            SpiritExpeditionPolicy.expectedDirection(
                                    seed, step, tide, projection, weather);
                    assertEquals(first, second);
                    observedEncounters.add(
                            SpiritExpeditionPolicy.encounterFor(
                                    seed, step, weather));
                }
            }
        }
        assertEquals(EnumSet.allOf(SpiritWeather.class), observedWeather);
        assertEquals(EnumSet.allOf(SpiritEncounter.class), observedEncounters);
    }

    @Test
    void correctAndWrongNavigationHaveMeaningfullyDifferentConsequences() {
        long seed = 918273645L;
        SpiritProjection projection = SpiritProjection.HARBOR;
        SpiritWeather weather = SpiritWeather.SPIRITUAL_STORM;
        int tide = SpiritExpeditionPolicy.tideFor(seed);
        SpiritDirection expected = SpiritExpeditionPolicy.expectedDirection(
                seed, 2, tide, projection, weather);
        SpiritDirection wrong = SpiritDirection.values()[
                (expected.ordinal() + 1) % SpiritDirection.values().length];

        SpiritExpeditionPolicy.NavigationOutcome success =
                SpiritExpeditionPolicy.navigate(
                        seed, 2, tide, projection, weather, expected);
        SpiritExpeditionPolicy.NavigationOutcome failure =
                SpiritExpeditionPolicy.navigate(
                        seed, 2, tide, projection, weather, wrong);

        assertTrue(success.correct());
        assertFalse(failure.correct());
        assertTrue(success.stabilityDelta() > 0);
        assertTrue(success.driftDelta() < 0);
        assertTrue(success.rewardDelta() > 0);
        assertTrue(failure.stabilityDelta() < 0);
        assertTrue(failure.driftDelta() > 0);
        assertEquals(success.encounter(), failure.encounter());
    }

    @Test
    void everyEncounterHasOneEffectiveAndSeveralUnsafeResponses() {
        for (SpiritEncounter encounter : SpiritEncounter.values()) {
            SpiritExpeditionPolicy.EncounterOutcome effective =
                    SpiritExpeditionPolicy.resolve(
                            encounter, encounter.preferredAction());
            SpiritEncounterAction wrong = SpiritEncounterAction.values()[
                    (encounter.preferredAction().ordinal() + 1)
                            % SpiritEncounterAction.values().length];
            SpiritExpeditionPolicy.EncounterOutcome unsafe =
                    SpiritExpeditionPolicy.resolve(encounter, wrong);
            assertTrue(effective.correct());
            assertFalse(unsafe.correct());
            assertTrue(effective.rewardDelta() >= 3);
            assertTrue(unsafe.driftDelta() >= 7);
            assertNotEquals(encounter.preferredAction(), wrong);
        }
    }

    @Test
    void deadlinesAndCountersSaturateInsteadOfOverflowing() {
        assertEquals(Long.MAX_VALUE,
                SpiritExpeditionPolicy.safeDeadline(Long.MAX_VALUE - 1));
        assertEquals(SpiritExpeditionPolicy.DURATION_TICKS,
                SpiritExpeditionPolicy.safeDeadline(-100));
        assertTrue(SpiritExpeditionPolicy.timedOut(
                Long.MAX_VALUE, Long.MAX_VALUE));
        assertEquals(Integer.MAX_VALUE,
                SpiritExpeditionPolicy.saturatedAdd(
                        Integer.MAX_VALUE - 1, 100));
        assertEquals(0,
                SpiritExpeditionPolicy.saturatedAdd(1, -100));
    }
}
