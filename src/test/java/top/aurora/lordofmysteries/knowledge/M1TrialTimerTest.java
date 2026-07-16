package top.aurora.lordofmysteries.knowledge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class M1TrialTimerTest {

    @Test
    void activeSessionAddsOnlyCurrentOnlineWindow() {
        assertEquals(500L, M1TrialTimer.elapsed(200L, true, 1000L, 1300L));
    }

    @Test
    void pausedRecordDoesNotCountLaterWorldTime() {
        assertEquals(500L, M1TrialTimer.elapsed(500L, true, -1L, 9000L));
        assertEquals(500L, M1TrialTimer.elapsed(500L, false, -1L, 9000L));
    }

    @Test
    void timeRollbackCannotReduceOrInflateStoredRecord() {
        assertEquals(500L, M1TrialTimer.elapsed(500L, true, 1000L, 100L));
    }
}
