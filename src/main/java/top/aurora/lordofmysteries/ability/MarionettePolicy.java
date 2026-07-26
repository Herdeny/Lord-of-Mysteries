package top.aurora.lordofmysteries.ability;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class MarionettePolicy {

    public static final int MAX_MARIONETTES = 3;
    public static final float MAX_TARGET_HEALTH = 80f;
    public static final float MAX_CAPTURE_HEALTH_RATIO = 0.2f;
    public static final float CREATION_COST = 60f;
    public static final long CREATION_COOLDOWN_TICKS = 2_400L;

    private MarionettePolicy() {}

    public static boolean canCreate(
            boolean playerTarget,
            boolean hostileTarget,
            boolean ownedByAnother,
            float maximumHealth,
            float health,
            int rosterSize) {
        return !playerTarget
                && hostileTarget
                && !ownedByAnother
                && maximumHealth > 0f
                && maximumHealth <= MAX_TARGET_HEALTH
                && health > 0f
                && health <= maximumHealth * MAX_CAPTURE_HEALTH_RATIO
                && rosterSize >= 0
                && rosterSize < MAX_MARIONETTES;
    }

    public static List<UUID> normalizeRoster(Collection<UUID> values) {
        if (values == null || values.isEmpty()) return List.of();
        List<UUID> normalized = new ArrayList<>();
        Set<UUID> seen = new HashSet<>();
        for (UUID value : values) {
            if (value == null || !seen.add(value)) continue;
            normalized.add(value);
            if (normalized.size() >= MAX_MARIONETTES) break;
        }
        return List.copyOf(normalized);
    }

    public static boolean canDamage(
            UUID attackerOwner,
            UUID attackerId,
            boolean victimPlayer,
            UUID victimOwner) {
        if (attackerOwner != null
                && (victimPlayer || victimOwner != null)) {
            return false;
        }
        return victimOwner == null
                || attackerId == null
                || !victimOwner.equals(attackerId);
    }
}
