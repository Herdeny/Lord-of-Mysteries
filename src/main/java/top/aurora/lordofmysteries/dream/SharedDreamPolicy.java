package top.aurora.lordofmysteries.dream;

import java.util.UUID;

public final class SharedDreamPolicy {

    public static final int MAX_PARTICIPANTS = 4;
    public static final int TRUSTED_REPUTATION = 4;
    public static final int SYMBOL_STEPS = 4;
    public static final int STARTING_COHERENCE = 80;
    public static final int MAX_COHERENCE = 100;
    public static final int MAX_TRAUMA = 100;
    public static final long INVITE_TTL_TICKS = 1_200L;
    public static final long SESSION_TTL_TICKS = 12_000L;

    private SharedDreamPolicy() {}

    public static DreamScenario scenario(long seed, UUID host) {
        long mixed = mix(seed, host == null ? 0L
                : host.getMostSignificantBits() ^ host.getLeastSignificantBits());
        return DreamScenario.values()[Math.floorMod(
                mixed, DreamScenario.values().length)];
    }

    public static DreamSymbol symbol(
            long seed, DreamScenario scenario, int step) {
        if (scenario == null || step < 0 || step >= SYMBOL_STEPS) {
            throw new IllegalArgumentException("invalid dream symbol context");
        }
        long mixed = mix(seed ^ ((long) scenario.ordinal() << 32), step + 1L);
        return DreamSymbol.values()[Math.floorMod(
                mixed, DreamSymbol.values().length)];
    }

    public static ActionOutcome resolve(
            DreamSymbol symbol, DreamAction action, int participantCount) {
        if (symbol == null || action == null || participantCount < 2
                || participantCount > MAX_PARTICIPANTS) {
            throw new IllegalArgumentException("invalid shared dream action");
        }
        boolean correct = symbol.preferredAction() == action;
        int teamwork = participantCount - 1;
        return correct
                ? new ActionOutcome(true, 3 + teamwork, -2, 2 + teamwork)
                : new ActionOutcome(false, -(6 + teamwork), 5 + teamwork, 0);
    }

    public static int clampCoherence(int value) {
        return Math.max(0, Math.min(MAX_COHERENCE, value));
    }

    public static int clampTrauma(int value) {
        return Math.max(0, Math.min(MAX_TRAUMA, value));
    }

    public static long deadline(long now, long duration) {
        long start = Math.max(0L, now);
        long safeDuration = Math.max(1L, duration);
        if (Long.MAX_VALUE - start < safeDuration) return Long.MAX_VALUE;
        return start + safeDuration;
    }

    public static boolean invitationExpired(long now, long expiresAt) {
        return Math.max(0L, now) >= Math.max(0L, expiresAt);
    }

    public static boolean failed(int coherence, int trauma) {
        return coherence <= 0 || trauma >= MAX_TRAUMA;
    }

    public static long mix(long left, long right) {
        long value = left ^ Long.rotateLeft(right, 23)
                ^ 0x9E3779B97F4A7C15L;
        value ^= value >>> 30;
        value *= 0xBF58476D1CE4E5B9L;
        value ^= value >>> 27;
        value *= 0x94D049BB133111EBL;
        return value ^ value >>> 31;
    }

    public record ActionOutcome(
            boolean correct, int coherenceDelta,
            int traumaDelta, int clueScore) {}
}
