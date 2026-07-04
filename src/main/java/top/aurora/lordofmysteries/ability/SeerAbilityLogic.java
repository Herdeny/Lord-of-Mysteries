package top.aurora.lordofmysteries.ability;

public final class SeerAbilityLogic {

    public static final double MAX_FLAME_LEAP_DISTANCE = 8d;
    public static final float INTUITIVE_DODGE_CHANCE = 0.15f;

    private SeerAbilityLogic() {}

    public static boolean canUseSequence(int currentSequence, int requiredSequence) {
        return currentSequence >= 0 && currentSequence <= requiredSequence;
    }

    public static boolean intuitiveDodge(float roll, boolean meleeAttack,
                                         long cooldownEndTick, long nowTick) {
        return meleeAttack
                && AbilityCooldowns.ready(cooldownEndTick, nowTick)
                && roll >= 0f
                && roll < INTUITIVE_DODGE_CHANCE;
    }

    public static double clampFlameLeapDistance(double requestedDistance) {
        return Math.max(0d, Math.min(MAX_FLAME_LEAP_DISTANCE, requestedDistance));
    }

    public static boolean paperSubstituteTriggers(float incomingDamage, float currentHealth,
                                                  long armedEndTick, long nowTick) {
        return incomingDamage >= currentHealth && nowTick < armedEndTick;
    }

    public static int cardVolleyDamage(boolean spiritBurning) {
        return spiritBurning ? 8 : 6;
    }
}
