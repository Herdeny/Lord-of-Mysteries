package top.aurora.lordofmysteries.ability;

public final class HunterAbilityLogic {

    private HunterAbilityLogic() {}

    public static boolean validHuntTarget(boolean naturalSpawn, boolean hostile,
                                          boolean combatRecorded) {
        return naturalSpawn || hostile || combatRecorded;
    }

    public static boolean canProvoke(float maxHealth, boolean boss) {
        return !boss && maxHealth <= 80f;
    }

    public static boolean battleWillReady(int attackers, long cooldownEnd, long now) {
        return attackers >= 3 && AbilityCooldowns.ready(cooldownEnd, now);
    }

    public static float spiritualityRegen(float baseRate, int sequence, boolean outdoors) {
        return sequence == 9 && outdoors ? baseRate * 1.1f : baseRate;
    }
}
