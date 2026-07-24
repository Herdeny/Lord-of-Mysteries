package top.aurora.lordofmysteries.ability;

import java.util.Set;

public final class M3LaunchAbilityLogic {

    private static final Set<String> PATHWAYS = Set.of(
            "seer", "spectator", "hunter", "thief", "apprentice");

    private M3LaunchAbilityLogic() {}

    public static boolean supports(String pathway, int sequence) {
        return pathway != null
                && PATHWAYS.contains(pathway)
                && (sequence == 6 || sequence == 5);
    }

    public static boolean canControl(boolean playerTarget, float maximumHealth) {
        return !playerTarget && maximumHealth > 0f && maximumHealth <= 80f;
    }

    public static boolean canRestrain(
            boolean playerTarget, float maximumHealth, float health) {
        return canControl(playerTarget, maximumHealth)
                && health > 0f
                && health <= maximumHealth * 0.35f;
    }

    public static boolean canRetrieveItem(
            boolean hasOwner, boolean ownedByCaster) {
        return !hasOwner || ownedByCaster;
    }

    public static int copiedBookGeneration(int currentGeneration) {
        if (currentGeneration < 0 || currentGeneration >= 2) return -1;
        return currentGeneration + 1;
    }
}
