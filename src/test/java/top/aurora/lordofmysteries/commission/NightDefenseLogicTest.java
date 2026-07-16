package top.aurora.lordofmysteries.commission;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class NightDefenseLogicTest {

    @Test
    void waitsForNightBeforeFirstWave() {
        assertEquals(NightDefenseLogic.Action.WAIT_FOR_NIGHT,
                NightDefenseLogic.decide(false, false, false, 100L, 0L));
    }

    @Test
    void waitsForScheduledWaveAtNight() {
        assertEquals(NightDefenseLogic.Action.WAIT,
                NightDefenseLogic.decide(true, false, false, 99L, 100L));
    }

    @Test
    void startsWaveWhenDelayExpires() {
        assertEquals(NightDefenseLogic.Action.SPAWN_WAVE,
                NightDefenseLogic.decide(true, false, false, 100L, 100L));
    }

    @Test
    void onlyCompletesSpawnedWaveAfterEnemiesAreGone() {
        assertEquals(NightDefenseLogic.Action.WAIT,
                NightDefenseLogic.decide(true, true, true, 120L, 0L));
        assertEquals(NightDefenseLogic.Action.COMPLETE_WAVE,
                NightDefenseLogic.decide(true, true, false, 120L, 0L));
    }
}
