package top.aurora.lordofmysteries.commission;

public final class DynamicCaseResponsePolicy {

    private DynamicCaseResponsePolicy() {
    }

    static DynamicCaseResponseTask assign(
            DynamicCaseHistoryEntry entry,
            DynamicCaseWeeklyDirective directive,
            long currentDay) {
        return assign(entry, directive, currentDay,
                DynamicCaseResponseBranch.ROUTINE);
    }

    static DynamicCaseResponseTask assign(
            DynamicCaseHistoryEntry entry,
            DynamicCaseWeeklyDirective directive,
            long currentDay,
            DynamicCaseResponseBranch branch) {
        if (entry == null || directive == null || currentDay < 0L
                || directive.organization() != entry.organization()
                || branch == null) {
            throw new IllegalArgumentException(
                    "organization response assignment is invalid");
        }
        return new DynamicCaseResponseTask(
                entry.instanceId(),
                entry.organization(),
                entry.subject(),
                directive,
                currentDay,
                currentDay + branch.durationDays(),
                DynamicCaseResponseTask.Stage.ASSIGNED,
                branch);
    }

    static boolean isExpired(
            DynamicCaseResponseTask task, long currentDay) {
        if (task == null) return false;
        return currentDay >= task.expiresDay();
    }

    static Reward reward(DynamicCaseWeeklyDirective directive) {
        return reward(directive, DynamicCaseResponseBranch.ROUTINE);
    }

    static Reward reward(
            DynamicCaseWeeklyDirective directive,
            DynamicCaseResponseBranch branch) {
        if (directive == null) {
            throw new IllegalArgumentException(
                    "organization response directive is required");
        }
        if (branch == null) {
            throw new IllegalArgumentException(
                    "organization response branch is required");
        }
        Reward base = switch (directive) {
            case PATTERN_AUDIT, SOURCE_VERIFICATION, CHAIN_OF_CUSTODY ->
                    new Reward(8L, 1, 1);
            case CLIENT_REINTERVIEW, PUBLIC_REASSURANCE, DISTRICT_PATROL ->
                    new Reward(6L, 1, 2);
        };
        return switch (branch) {
            case PRIORITY -> new Reward(
                    base.moneyPence() + 4L,
                    base.reputation() + 1,
                    base.contactStanding() + 1);
            case ROUTINE -> base;
            case RECONCILIATION -> new Reward(
                    Math.max(0L, base.moneyPence() - 2L),
                    base.reputation(),
                    base.contactStanding() + 2);
        };
    }

    record Reward(long moneyPence, int reputation, int contactStanding) {
        Reward {
            if (moneyPence < 0L || reputation < 0
                    || contactStanding < 0) {
                throw new IllegalArgumentException(
                        "organization response reward is invalid");
            }
        }
    }
}
