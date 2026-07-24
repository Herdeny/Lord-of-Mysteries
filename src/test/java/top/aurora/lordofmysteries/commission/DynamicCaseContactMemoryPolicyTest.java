package top.aurora.lordofmysteries.commission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class DynamicCaseContactMemoryPolicyTest {

    @Test
    void duplicateCaseAndConflictingResponseOutcomeAreIgnored() {
        List<DynamicCaseContactEvent> events = new ArrayList<>();
        DynamicCaseContactEvent caseEvent = event(
                "case-1", DynamicCaseContactEvent.Kind.CASE_CLOSED,
                "a", 1L, 2);
        DynamicCaseContactEvent completed = event(
                "case-1",
                DynamicCaseContactEvent.Kind.RESPONSE_COMPLETED,
                "routine", 2L, 1);
        DynamicCaseContactEvent expired = event(
                "case-1",
                DynamicCaseContactEvent.Kind.RESPONSE_EXPIRED,
                "routine", 4L, -1);

        assertTrue(DynamicCaseContactMemoryPolicy.record(
                events, caseEvent));
        assertFalse(DynamicCaseContactMemoryPolicy.record(
                events, caseEvent));
        assertTrue(DynamicCaseContactMemoryPolicy.record(
                events, completed));
        assertFalse(DynamicCaseContactMemoryPolicy.record(
                events, expired));
        assertEquals(2, events.size());
    }

    @Test
    void ledgerRetainsOnlyNewestTwentyFourEvents() {
        List<DynamicCaseContactEvent> events = new ArrayList<>();
        for (int index = 0;
                index < DynamicCaseContactMemoryPolicy.MAX_EVENTS + 3;
                index++) {
            DynamicCaseContactMemoryPolicy.record(
                    events,
                    event("case-" + index,
                            DynamicCaseContactEvent.Kind.CASE_CLOSED,
                            "b", index, 1));
        }

        assertEquals(DynamicCaseContactMemoryPolicy.MAX_EVENTS,
                events.size());
        assertEquals("case-3", events.get(0).instanceId());
        assertEquals("case-26",
                events.get(events.size() - 1).instanceId());
    }

    @Test
    void summariesStayIsolatedByContact() {
        List<DynamicCaseContactEvent> events = new ArrayList<>();
        DynamicCaseContactEvent first = event(
                "case-1", DynamicCaseContactEvent.Kind.CASE_CLOSED,
                "s", 1L, 3);
        DynamicCaseContactEvent second = event(
                "case-1",
                DynamicCaseContactEvent.Kind.RESPONSE_COMPLETED,
                "priority", 2L, 2);
        events.add(first);
        events.add(second);
        events.add(new DynamicCaseContactEvent(
                "case-2",
                DynamicCaseProfile.Subject.DOCK_ACCOUNTANT,
                DynamicCaseProfile.Organization.DETECTIVE_AGENCY,
                DynamicCaseContactEvent.Kind.RESPONSE_EXPIRED,
                "routine", 3L, -1));

        DynamicCaseContactMemoryPolicy.Summary summary =
                DynamicCaseContactMemoryPolicy.summarize(
                        events,
                        DynamicCaseProfile.Subject.APPRENTICE_REPORTER);

        assertEquals(1, summary.resolvedCases());
        assertEquals(1, summary.completedResponses());
        assertEquals(0, summary.missedResponses());
        assertSame(second, summary.lastEvent());
    }

    @Test
    void responseBranchUsesStandingAndPromiseHistory() {
        DynamicCaseContactMemoryPolicy.Summary reliable =
                new DynamicCaseContactMemoryPolicy.Summary(
                        2, 2, 0, 0, null);
        DynamicCaseContactMemoryPolicy.Summary missed =
                new DynamicCaseContactMemoryPolicy.Summary(
                        2, 0, 1, 0, null);

        assertEquals(DynamicCaseResponseBranch.PRIORITY,
                DynamicCaseContactMemoryPolicy.selectResponseBranch(
                        8, reliable));
        assertEquals(DynamicCaseResponseBranch.RECONCILIATION,
                DynamicCaseContactMemoryPolicy.selectResponseBranch(
                        10, missed));
        assertEquals(DynamicCaseResponseBranch.RECONCILIATION,
                DynamicCaseContactMemoryPolicy.selectResponseBranch(
                        -3, reliable));
        assertEquals(DynamicCaseResponseBranch.ROUTINE,
                DynamicCaseContactMemoryPolicy.selectResponseBranch(
                        7, reliable));
    }

    @Test
    void sanitizerKeepsNewestDuplicateAndRemovesNulls() {
        List<DynamicCaseContactEvent> events = new ArrayList<>();
        events.add(event("case-1",
                DynamicCaseContactEvent.Kind.RESPONSE_ABANDONED,
                "routine", 3L, -1));
        events.add(null);
        DynamicCaseContactEvent newest = event(
                "case-1",
                DynamicCaseContactEvent.Kind.RESPONSE_COMPLETED,
                "routine", 4L, 1);
        events.add(newest);

        assertEquals(2,
                DynamicCaseContactMemoryPolicy.sanitize(events));
        assertEquals(List.of(newest), events);
    }

    private static DynamicCaseContactEvent event(
            String instanceId,
            DynamicCaseContactEvent.Kind kind,
            String detail,
            long caseDay,
            int standingDelta) {
        return new DynamicCaseContactEvent(
                instanceId,
                DynamicCaseProfile.Subject.APPRENTICE_REPORTER,
                DynamicCaseProfile.Organization.MIST_CITY_PRESS,
                kind,
                detail,
                caseDay,
                standingDelta);
    }
}
