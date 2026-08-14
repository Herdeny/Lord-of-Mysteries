package top.aurora.lordofmysteries.ritual;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RitualRecoveryLogicTest {

    @Test
    void onlineLeaderContinuesInvocation() {
        assertEquals(RitualRecoveryLogic.Action.CONTINUE,
                RitualRecoveryLogic.decide(true, true, true, 0));
    }

    @Test
    void onlineButIneligibleLeaderCancelsImmediately() {
        assertEquals(RitualRecoveryLogic.Action.CANCEL,
                RitualRecoveryLogic.decide(true, true, false, 0));
    }

    @Test
    void inactiveRitualIgnoresIneligibleOnlineLeader() {
        assertEquals(RitualRecoveryLogic.Action.IGNORE,
                RitualRecoveryLogic.decide(false, true, false, 0));
    }

    @Test
    void shortDisconnectPausesWithoutConsumingProgress() {
        assertEquals(RitualRecoveryLogic.Action.PAUSE,
                RitualRecoveryLogic.decide(true, false, 200));
    }

    @Test
    void abandonedInvocationEventuallyCancels() {
        assertEquals(RitualRecoveryLogic.Action.CANCEL,
                RitualRecoveryLogic.decide(true, false,
                        RitualRecoveryLogic.MAX_OFFLINE_TICKS));
    }

    @Test
    void inactiveRitualIgnoresRecoveryPolicy() {
        assertEquals(RitualRecoveryLogic.Action.IGNORE,
                RitualRecoveryLogic.decide(false, false, 5000));
    }
}
