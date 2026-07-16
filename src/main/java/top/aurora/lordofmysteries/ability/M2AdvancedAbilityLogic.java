package top.aurora.lordofmysteries.ability;

public final class M2AdvancedAbilityLogic {

    public static final double SWAP_RANGE_SQUARED = 64d;
    public static final int REQUIRED_RUNE_TYPES = 3;
    public static final int REQUIRED_MOON_PHASES = 5;

    private M2AdvancedAbilityLogic() {}

    public static int selectedSequence(int currentSequence,
                                       boolean sneaking,
                                       boolean sprinting) {
        if (currentSequence == 7) {
            if (sprinting) return 9;
            if (sneaking) return 8;
            return 7;
        }
        if (currentSequence == 8) {
            return sneaking || sprinting ? 9 : 8;
        }
        return 9;
    }

    public static boolean canSwap(double distanceSquared,
                                  boolean hasLineOfSight,
                                  boolean targetIsPlayer,
                                  boolean pvpAllowed) {
        return distanceSquared <= SWAP_RANGE_SQUARED
                && hasLineOfSight
                && (!targetIsPlayer || pvpAllowed);
    }

    public static boolean runeMasteryReady(int uniqueRuneTargets) {
        return uniqueRuneTargets >= REQUIRED_RUNE_TYPES;
    }

    public static boolean perfectCrimeReady(long lastLockpickTick,
                                            long now) {
        return lastLockpickTick > 0L && now - lastLockpickTick <= 600L;
    }

    public static float knowledgeLinkChance(int knownKnowledge) {
        return Math.min(0.85f, 0.35f + Math.max(0, knownKnowledge - 2) * 0.08f);
    }

    public static boolean astrologyAvailable(boolean night,
                                             boolean canSeeSky) {
        return night && canSeeSky;
    }

    public static int forecastIndex(long day, int moonPhase) {
        return Math.floorMod((int) (day * 31L + moonPhase * 7L), 4);
    }

    public static boolean moonAtlasReady(int observedPhases) {
        return observedPhases >= REQUIRED_MOON_PHASES;
    }

    public static boolean honestDayReady(long trackedDay,
                                         long currentDay,
                                         boolean dayWasDirty) {
        return trackedDay >= 0L && currentDay > trackedDay && !dayWasDirty;
    }
}
