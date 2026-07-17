package top.aurora.lordofmysteries.network;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PlayerSummarySyncHandlerTest {

    @Test
    void dirtyCoreSynchronizesImmediately() {
        assertTrue(PlayerSummarySyncHandler.shouldSend(true, 99L, 100L));
    }

    @Test
    void cleanCoreUsesFiveSecondCorrectionWindow() {
        assertFalse(PlayerSummarySyncHandler.shouldSend(false, 100L, 199L));
        assertTrue(PlayerSummarySyncHandler.shouldSend(false, 100L, 200L));
    }
}
