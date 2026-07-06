package top.aurora.lordofmysteries.ability;

public final class M2Sequence7AbilityLogic {

    private M2Sequence7AbilityLogic() {}

    public static boolean canAffectMentalTarget(boolean playerTarget,
                                                boolean pvpMentalEnabled,
                                                float maxHealth) {
        if (playerTarget) return pvpMentalEnabled;
        return maxHealth <= 100f;
    }

    public static boolean crowdActingReady(int affectedTargets) {
        return affectedTargets >= 3;
    }

    public static boolean sustainedActingReady(int ticks) {
        return ticks >= 600;
    }

    public static boolean fireAffinityActingReady(int ticks) {
        return ticks >= 200;
    }

    public static float burningTargetDamage(float baseDamage,
                                            boolean targetBurning) {
        return targetBurning ? baseDamage * 1.2f : baseDamage;
    }
}
