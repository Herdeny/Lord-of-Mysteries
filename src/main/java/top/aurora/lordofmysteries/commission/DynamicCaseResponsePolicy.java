package top.aurora.lordofmysteries.commission;

public final class DynamicCaseResponsePolicy {

    static final long TASK_DURATION_DAYS = 3L;

    private DynamicCaseResponsePolicy() {
    }

    static DynamicCaseResponseTask assign(
            DynamicCaseHistoryEntry entry,
            DynamicCaseWeeklyDirective directive,
            long currentDay) {
        if (entry == null || directive == null || currentDay < 0L
                || directive.organization() != entry.organization()) {
            throw new IllegalArgumentException(
                    "organization response assignment is invalid");
        }
        return new DynamicCaseResponseTask(
                entry.instanceId(),
                entry.organization(),
                entry.subject(),
                directive,
                currentDay,
                currentDay + TASK_DURATION_DAYS,
                DynamicCaseResponseTask.Stage.ASSIGNED);
    }

    static boolean isExpired(
            DynamicCaseResponseTask task, long currentDay) {
        if (task == null) return false;
        return currentDay >= task.expiresDay();
    }

    static Reward reward(DynamicCaseWeeklyDirective directive) {
        if (directive == null) {
            throw new IllegalArgumentException(
                    "organization response directive is required");
        }
        return switch (directive) {
            case PATTERN_AUDIT, SOURCE_VERIFICATION, CHAIN_OF_CUSTODY ->
                    new Reward(8L, 1, 1);
            case CLIENT_REINTERVIEW, PUBLIC_REASSURANCE, DISTRICT_PATROL ->
                    new Reward(6L, 1, 2);
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
