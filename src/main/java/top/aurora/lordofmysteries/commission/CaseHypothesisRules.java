package top.aurora.lordofmysteries.commission;

public final class CaseHypothesisRules {

    public static final long TEST_COOLDOWN_TICKS = 200L;
    public static final long RECONSIDER_COOLDOWN_TICKS = 600L;
    public static final int WRONG_TEST_PRESSURE = 4;

    private CaseHypothesisRules() {}

    public static long testCooldownRemaining(
            CaseHypothesisRecord record, long gameTime) {
        if (record.lastTestTick() < 0L) return 0L;
        return Math.max(0L,
                record.lastTestTick() + TEST_COOLDOWN_TICKS - gameTime);
    }

    public static long reconsiderCooldownRemaining(
            CaseHypothesisRecord record, long gameTime) {
        if (record.lastTestTick() < 0L) return 0L;
        return Math.max(0L,
                record.lastTestTick() + RECONSIDER_COOLDOWN_TICKS - gameTime);
    }

    public static TestResult test(
            CaseHypothesisRecord record,
            EvidenceRelationKind actualKind,
            long gameTime) {
        if (record == null || !record.hasDraft()) {
            throw new IllegalArgumentException("a hypothesis draft is required");
        }
        if (testCooldownRemaining(record, gameTime) > 0L) {
            throw new IllegalStateException("hypothesis test is on cooldown");
        }
        boolean supported = record.stance().matches(actualKind);
        if (supported) {
            CaseHypothesisRecord updated = record.tested(
                    CaseHypothesisStatus.SUPPORTED,
                    Math.max(0, record.unresolvedStrain() - 1),
                    record.failedTests(), record.successfulTests() + 1,
                    gameTime);
            return new TestResult(updated, true, 0);
        }
        CaseHypothesisRecord updated = record.tested(
                CaseHypothesisStatus.REJECTED,
                Math.min(CaseHypothesisRecord.MAX_STRAIN,
                        record.unresolvedStrain() + 1),
                record.failedTests() + 1, record.successfulTests(),
                gameTime);
        return new TestResult(updated, false, WRONG_TEST_PRESSURE);
    }

    public static CaseHypothesisRecord reconsider(
            CaseHypothesisRecord record, long gameTime) {
        if (record == null || record.unresolvedStrain() <= 0) {
            throw new IllegalArgumentException("reasoning strain is required");
        }
        if (reconsiderCooldownRemaining(record, gameTime) > 0L) {
            throw new IllegalStateException("reconsideration is on cooldown");
        }
        return record.reconsidered(gameTime);
    }

    public record TestResult(
            CaseHypothesisRecord record,
            boolean supported,
            int pressureCost) {}
}
