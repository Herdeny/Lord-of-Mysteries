package top.aurora.lordofmysteries.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class OccultActivityPolicyTest {

    @Test
    void ordinaryPlayerCanStartExclusiveActivity() {
        assertEquals(OccultActivityPolicy.Activity.NONE,
                classify(false, OccultActivityPolicy.DreamPhase.NONE,
                        false, false));
    }

    @Test
    void lobbyBlocksOtherExclusiveSystems() {
        assertEquals(OccultActivityPolicy.Activity.DREAM_LOBBY,
                classify(false, OccultActivityPolicy.DreamPhase.LOBBY,
                        false, false));
    }

    @Test
    void activeDreamBlocksOtherExclusiveSystems() {
        assertEquals(OccultActivityPolicy.Activity.DREAM_ACTIVE,
                classify(true, OccultActivityPolicy.DreamPhase.ACTIVE,
                        false, false));
    }

    @Test
    void queuedDreamRecoveryRemainsExclusiveOutsideDimension() {
        assertEquals(OccultActivityPolicy.Activity.DREAM_RECOVERY,
                classify(false, OccultActivityPolicy.DreamPhase.RECOVERY,
                        false, false));
    }

    @Test
    void orphanedDreamDimensionFailsClosed() {
        assertEquals(OccultActivityPolicy.Activity.DREAM_ORPHANED,
                classify(true, OccultActivityPolicy.DreamPhase.NONE,
                        false, false));
    }

    @Test
    void spiritRecordRemainsExclusiveAfterUnexpectedTeleport() {
        assertEquals(OccultActivityPolicy.Activity.SPIRIT_EXPEDITION,
                classify(false, OccultActivityPolicy.DreamPhase.NONE,
                        false, true));
    }

    @Test
    void orphanedSpiritDimensionRemainsExclusive() {
        assertEquals(OccultActivityPolicy.Activity.SPIRIT_EXPEDITION,
                classify(false, OccultActivityPolicy.DreamPhase.NONE,
                        true, false));
    }

    @Test
    void simultaneousDreamAndSpiritEvidenceIsConflict() {
        assertEquals(OccultActivityPolicy.Activity.CONFLICT,
                classify(false, OccultActivityPolicy.DreamPhase.LOBBY,
                        false, true));
    }

    @Test
    void recoveryOutranksPhysicalDreamDimension() {
        assertEquals(OccultActivityPolicy.Activity.DREAM_RECOVERY,
                classify(true, OccultActivityPolicy.DreamPhase.RECOVERY,
                        false, false));
    }

    @Test
    void onlyNoneIsAvailableForOrdinaryOperations() {
        assertTrue(OccultActivityPolicy.Activity.NONE.available());
        for (OccultActivityPolicy.Activity activity
                : OccultActivityPolicy.Activity.values()) {
            if (activity != OccultActivityPolicy.Activity.NONE) {
                assertFalse(activity.available(), activity.name());
            }
        }
    }

    private static OccultActivityPolicy.Activity classify(
            boolean dreamDimension,
            OccultActivityPolicy.DreamPhase dreamPhase,
            boolean spiritDimension,
            boolean spiritRecord) {
        return OccultActivityPolicy.classify(
                dreamDimension, dreamPhase, spiritDimension, spiritRecord);
    }
}
