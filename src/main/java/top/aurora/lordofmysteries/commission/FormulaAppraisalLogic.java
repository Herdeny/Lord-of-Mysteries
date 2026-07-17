package top.aurora.lordofmysteries.commission;

import java.util.UUID;

public final class FormulaAppraisalLogic {

    public enum Method {
        SPIRITUAL,
        REAGENT,
        UNAVAILABLE
    }

    private FormulaAppraisalLogic() {}

    public static long dossierSeed(long worldSeed, UUID playerId,
                                   long acceptedTick) {
        long value = worldSeed ^ playerId.getMostSignificantBits()
                ^ Long.rotateLeft(playerId.getLeastSignificantBits(), 19)
                ^ Long.rotateLeft(acceptedTick, 37)
                ^ 0x46524D4C41505052L;
        value = (value ^ (value >>> 30)) * 0xbf58476d1ce4e5b9L;
        value = (value ^ (value >>> 27)) * 0x94d049bb133111ebL;
        return value ^ (value >>> 31);
    }

    public static boolean isAuthentic(long seed) {
        return (Long.bitCount(seed) & 1) == 0;
    }

    public static int clueMask(long seed, boolean authentic) {
        if (authentic) return 0b111;
        return 1 << Math.floorMod((int) (seed >>> 11), 3);
    }

    public static Method selectMethod(boolean seer, float spirituality,
                                      boolean hasCrystal, boolean hasInk) {
        if (seer && spirituality >= 6f) return Method.SPIRITUAL;
        if (hasCrystal && hasInk) return Method.REAGENT;
        return Method.UNAVAILABLE;
    }

    public static boolean verdictMatches(boolean authentic,
                                         boolean authenticVerdict) {
        return authentic == authenticVerdict;
    }
}
