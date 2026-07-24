package top.aurora.lordofmysteries.commission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.nbt.CompoundTag;

import org.junit.jupiter.api.Test;

class DynamicCaseContactEventTest {

    @Test
    void caseResolutionRoundTripPreservesReasonAndIdentity() {
        DynamicCaseContactEvent event =
                DynamicCaseContactEvent.caseClosed(historyEntry());

        DynamicCaseContactEvent restored =
                DynamicCaseContactEvent.load(event.save());

        assertEquals(event, restored);
        assertEquals("memory-case:case", restored.identityKey());
        assertEquals("a", restored.detail());
        assertEquals(2, restored.standingDelta());
    }

    @Test
    void responseEventsShareOneTerminalIdentity() {
        DynamicCaseResponseTask task = new DynamicCaseResponseTask(
                "memory-case",
                DynamicCaseProfile.Organization.MIST_CITY_PRESS,
                DynamicCaseProfile.Subject.APPRENTICE_REPORTER,
                DynamicCaseWeeklyDirective.SOURCE_VERIFICATION,
                12L, 16L,
                DynamicCaseResponseTask.Stage.BRIEFED,
                DynamicCaseResponseBranch.PRIORITY);

        DynamicCaseContactEvent completed =
                DynamicCaseContactEvent.response(
                        task,
                        DynamicCaseContactEvent.Kind.RESPONSE_COMPLETED,
                        13L, 2);
        DynamicCaseContactEvent expired =
                DynamicCaseContactEvent.response(
                        task,
                        DynamicCaseContactEvent.Kind.RESPONSE_EXPIRED,
                        16L, -1);

        assertEquals(completed.identityKey(), expired.identityKey());
        assertEquals("priority", completed.detail());
    }

    @Test
    void malformedDetailAndPayloadFailClosed() {
        assertThrows(IllegalArgumentException.class,
                () -> new DynamicCaseContactEvent(
                        "memory-case",
                        DynamicCaseProfile.Subject.APPRENTICE_REPORTER,
                        DynamicCaseProfile.Organization.MIST_CITY_PRESS,
                        DynamicCaseContactEvent.Kind.CASE_CLOSED,
                        "unknown",
                        12L, 0));

        CompoundTag malformed =
                DynamicCaseContactEvent.caseClosed(historyEntry()).save();
        malformed.putString("contact", "unknown");
        assertFalse(DynamicCaseContactEvent.isValid(malformed));
        assertThrows(IllegalArgumentException.class,
                () -> DynamicCaseContactEvent.load(malformed));
        assertTrue(DynamicCaseContactEvent.isValid(
                DynamicCaseContactEvent.caseClosed(historyEntry()).save()));
    }

    private static DynamicCaseHistoryEntry historyEntry() {
        return new DynamicCaseHistoryEntry(
                12L, 1L, "memory-case",
                DynamicCaseProfile.Archetype.MISSING_PERSON,
                DynamicCaseProfile.Subject.APPRENTICE_REPORTER,
                DynamicCaseProfile.Organization.MIST_CITY_PRESS,
                DynamicCaseProfile.CaseLocation.MIST_CITY_OUTPOST,
                CaseGrade.A, 86, 2400L, 2,
                DynamicCaseHistoryEntry.FollowUpStatus.CLAIMED);
    }
}
