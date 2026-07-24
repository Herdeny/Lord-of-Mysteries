package top.aurora.lordofmysteries.commission;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class DynamicCaseContinuityPolicy {

    static final int MAX_HISTORY_ENTRIES = 8;

    private DynamicCaseContinuityPolicy() {
    }

    static void record(
            List<DynamicCaseHistoryEntry> history,
            DynamicCaseHistoryEntry entry) {
        if (history == null || entry == null) {
            throw new IllegalArgumentException(
                    "dynamic case history and entry are required");
        }
        history.removeIf(existing -> existing == null
                || existing.instanceId().equals(entry.instanceId()));
        for (int index = 0; index < history.size(); index++) {
            DynamicCaseHistoryEntry existing = history.get(index);
            if (existing.followUpStatus()
                    == DynamicCaseHistoryEntry.FollowUpStatus.PENDING) {
                history.set(index, existing.withFollowUpStatus(
                        DynamicCaseHistoryEntry.FollowUpStatus.EXPIRED));
            }
        }
        history.add(entry);
        trim(history);
    }

    static Optional<DynamicCaseHistoryEntry> latestPending(
            List<DynamicCaseHistoryEntry> history) {
        if (history == null) return Optional.empty();
        for (int index = history.size() - 1; index >= 0; index--) {
            DynamicCaseHistoryEntry entry = history.get(index);
            if (entry != null && entry.followUpStatus()
                    == DynamicCaseHistoryEntry.FollowUpStatus.PENDING) {
                return Optional.of(entry);
            }
        }
        return Optional.empty();
    }

    public static int sanitize(List<DynamicCaseHistoryEntry> history) {
        if (history == null) return 0;
        int repairs = 0;
        for (int index = history.size() - 1; index >= 0; index--) {
            if (history.get(index) == null) {
                history.remove(index);
                repairs++;
            }
        }

        Set<String> retainedInstances = new HashSet<>();
        for (int index = history.size() - 1; index >= 0; index--) {
            if (!retainedInstances.add(history.get(index).instanceId())) {
                history.remove(index);
                repairs++;
            }
        }

        boolean pendingRetained = false;
        for (int index = history.size() - 1; index >= 0; index--) {
            DynamicCaseHistoryEntry entry = history.get(index);
            if (entry.followUpStatus()
                    != DynamicCaseHistoryEntry.FollowUpStatus.PENDING) {
                continue;
            }
            if (!pendingRetained) {
                pendingRetained = true;
            } else {
                history.set(index, entry.withFollowUpStatus(
                        DynamicCaseHistoryEntry.FollowUpStatus.EXPIRED));
                repairs++;
            }
        }

        int originalSize = history.size();
        trim(history);
        return repairs + originalSize - history.size();
    }

    static Reward reward(CaseGrade grade) {
        if (grade == null) {
            throw new IllegalArgumentException("case grade is required");
        }
        return switch (grade) {
            case S -> new Reward(Response.PRIORITY_BRIEFING, 8L, 1, 0f);
            case A -> new Reward(Response.PRIORITY_BRIEFING, 6L, 1, 0f);
            case B -> new Reward(Response.STANDARD_FOLLOW_UP, 4L, 0, 0f);
            case C -> new Reward(Response.STANDARD_FOLLOW_UP, 2L, 0, 0f);
            case D -> new Reward(Response.CORRECTIVE_REVIEW, 0L, 1, 4f);
        };
    }

    private static void trim(List<DynamicCaseHistoryEntry> history) {
        while (history.size() > MAX_HISTORY_ENTRIES) {
            history.remove(0);
        }
    }

    enum Response {
        PRIORITY_BRIEFING("priority_briefing"),
        STANDARD_FOLLOW_UP("standard_follow_up"),
        CORRECTIVE_REVIEW("corrective_review");

        private final String id;

        Response(String id) {
            this.id = id;
        }

        String translationKey() {
            return "dynamic_case.lord_of_mysteries.follow_up." + id;
        }
    }

    record Reward(
            Response response,
            long moneyPence,
            int reputation,
            float pressureRecovery) {

        Reward {
            if (response == null || moneyPence < 0L || reputation < 0
                    || pressureRecovery < 0f) {
                throw new IllegalArgumentException(
                        "dynamic case follow-up reward is invalid");
            }
        }
    }
}
