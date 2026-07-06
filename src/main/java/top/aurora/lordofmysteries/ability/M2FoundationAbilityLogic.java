package top.aurora.lordofmysteries.ability;

public final class M2FoundationAbilityLogic {

    public static final int SHADOW_ACTING_TICKS = 600;

    private M2FoundationAbilityLogic() {}

    public static boolean canPilfer(double distanceSquared,
                                    long targetLockEnd,
                                    long now) {
        return distanceSquared <= 9d && targetLockEnd <= now;
    }

    public static float alertChance(boolean playerTarget,
                                    boolean targetAlreadyAggressive,
                                    float luck) {
        float base = playerTarget ? 0.75f : targetAlreadyAggressive ? 0.55f : 0.30f;
        return Math.max(0.10f, Math.min(0.90f, base - luck * 0.02f));
    }

    public static boolean shadowActingReady(int accumulatedTicks) {
        return accumulatedTicks >= SHADOW_ACTING_TICKS;
    }

    public static boolean copyableKnowledge(String id) {
        return id != null
                && id.startsWith("lord_of_mysteries:knowledge/")
                && !id.endsWith("_acting");
    }

    public static boolean fieldNoteReady(int distinctBiomes) {
        return distinctBiomes >= 3;
    }
}
