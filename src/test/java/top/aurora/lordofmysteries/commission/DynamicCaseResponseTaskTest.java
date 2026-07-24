package top.aurora.lordofmysteries.commission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.nbt.CompoundTag;

import org.junit.jupiter.api.Test;

class DynamicCaseResponseTaskTest {

    @Test
    void nbtRoundTripPreservesOrganizationAssignment() {
        DynamicCaseResponseTask task = task(
                DynamicCaseResponseTask.Stage.BRIEFED);

        DynamicCaseResponseTask restored =
                DynamicCaseResponseTask.load(task.save());

        assertEquals(task, restored);
        assertTrue(DynamicCaseResponseTask.isValid(task.save()));
    }

    @Test
    void mismatchedDirectiveAndOrganizationAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new DynamicCaseResponseTask(
                        "response-test",
                        DynamicCaseProfile.Organization.CONSTABULARY,
                        DynamicCaseProfile.Subject.RETIRED_CONSTABLE,
                        DynamicCaseWeeklyDirective.PATTERN_AUDIT,
                        10L, 13L,
                        DynamicCaseResponseTask.Stage.ASSIGNED));
    }

    @Test
    void nonCanonicalExpiryWindowIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new DynamicCaseResponseTask(
                        "response-test",
                        DynamicCaseProfile.Organization.MIST_CITY_PRESS,
                        DynamicCaseProfile.Subject.APPRENTICE_REPORTER,
                        DynamicCaseWeeklyDirective.SOURCE_VERIFICATION,
                        10L, 14L,
                        DynamicCaseResponseTask.Stage.ASSIGNED));
    }

    @Test
    void malformedPayloadFailsClosed() {
        CompoundTag malformed = task(
                DynamicCaseResponseTask.Stage.ASSIGNED).save();
        malformed.putString("contact", "unknown");

        assertFalse(DynamicCaseResponseTask.isValid(malformed));
        assertThrows(IllegalArgumentException.class,
                () -> DynamicCaseResponseTask.load(malformed));
    }

    private static DynamicCaseResponseTask task(
            DynamicCaseResponseTask.Stage stage) {
        return new DynamicCaseResponseTask(
                "response-test",
                DynamicCaseProfile.Organization.MIST_CITY_PRESS,
                DynamicCaseProfile.Subject.APPRENTICE_REPORTER,
                DynamicCaseWeeklyDirective.SOURCE_VERIFICATION,
                10L, 13L, stage);
    }
}
