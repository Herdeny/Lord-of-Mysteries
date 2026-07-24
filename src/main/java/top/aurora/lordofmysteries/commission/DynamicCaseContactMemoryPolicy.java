package top.aurora.lordofmysteries.commission;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class DynamicCaseContactMemoryPolicy {

    public static final int MAX_EVENTS = 24;

    private DynamicCaseContactMemoryPolicy() {
    }

    static boolean record(
            List<DynamicCaseContactEvent> events,
            DynamicCaseContactEvent event) {
        if (events == null || event == null) {
            throw new IllegalArgumentException(
                    "contact memory and event are required");
        }
        boolean duplicate = events.stream()
                .filter(existing -> existing != null)
                .anyMatch(existing -> existing.identityKey()
                        .equals(event.identityKey()));
        if (duplicate) return false;
        events.add(event);
        trim(events);
        return true;
    }

    static Summary summarize(
            List<DynamicCaseContactEvent> events,
            DynamicCaseProfile.Subject contact) {
        if (contact == null) {
            throw new IllegalArgumentException("contact is required");
        }
        int cases = 0;
        int completed = 0;
        int abandoned = 0;
        int expired = 0;
        DynamicCaseContactEvent last = null;
        if (events != null) {
            for (DynamicCaseContactEvent event : events) {
                if (event == null || event.contact() != contact) continue;
                last = event;
                switch (event.kind()) {
                    case CASE_CLOSED -> cases++;
                    case RESPONSE_COMPLETED -> completed++;
                    case RESPONSE_ABANDONED -> abandoned++;
                    case RESPONSE_EXPIRED -> expired++;
                }
            }
        }
        return new Summary(cases, completed, abandoned, expired, last);
    }

    static DynamicCaseResponseBranch selectResponseBranch(
            int standing,
            Summary summary) {
        if (summary == null) {
            throw new IllegalArgumentException(
                    "contact memory summary is required");
        }
        int normalized = Math.max(
                DynamicCaseRelationshipPolicy.MIN_STANDING,
                Math.min(DynamicCaseRelationshipPolicy.MAX_STANDING,
                        standing));
        if (normalized <= -3
                || summary.missedResponses() > summary.completedResponses()) {
            return DynamicCaseResponseBranch.RECONCILIATION;
        }
        if (normalized >= 8
                && summary.completedResponses() > summary.missedResponses()) {
            return DynamicCaseResponseBranch.PRIORITY;
        }
        return DynamicCaseResponseBranch.ROUTINE;
    }

    public static int sanitize(List<DynamicCaseContactEvent> events) {
        if (events == null) return 0;
        int repairs = 0;
        for (int index = events.size() - 1; index >= 0; index--) {
            if (events.get(index) == null) {
                events.remove(index);
                repairs++;
            }
        }
        Set<String> identities = new HashSet<>();
        for (int index = events.size() - 1; index >= 0; index--) {
            if (!identities.add(events.get(index).identityKey())) {
                events.remove(index);
                repairs++;
            }
        }
        int originalSize = events.size();
        trim(events);
        return repairs + originalSize - events.size();
    }

    private static void trim(List<DynamicCaseContactEvent> events) {
        while (events.size() > MAX_EVENTS) {
            events.remove(0);
        }
    }

    record Summary(
            int resolvedCases,
            int completedResponses,
            int abandonedResponses,
            int expiredResponses,
            DynamicCaseContactEvent lastEvent) {

        Summary {
            if (resolvedCases < 0 || completedResponses < 0
                    || abandonedResponses < 0 || expiredResponses < 0) {
                throw new IllegalArgumentException(
                        "contact memory counters must be non-negative");
            }
        }

        int missedResponses() {
            return abandonedResponses + expiredResponses;
        }

        int eventCount() {
            return resolvedCases + completedResponses
                    + abandonedResponses + expiredResponses;
        }
    }
}
