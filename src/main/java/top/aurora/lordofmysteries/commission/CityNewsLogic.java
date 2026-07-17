package top.aurora.lordofmysteries.commission;

public final class CityNewsLogic {

    public static final int HEADLINE_COUNT = 6;

    private CityNewsLogic() {}

    public static Issue issue(long worldSeed, long day,
                              String activeCommissionId,
                              boolean shiftCompleted) {
        long mixed = worldSeed ^ Long.rotateLeft(day * 0x9E3779B97F4A7C15L, 17);
        mixed ^= mixed >>> 33;
        int headline = Math.floorMod((int) mixed, HEADLINE_COUNT);
        return new Issue(
                "message.lord_of_mysteries.newspaper.headline." + headline,
                caseBulletin(activeCommissionId),
                shiftCompleted
                        ? "message.lord_of_mysteries.newspaper.shift.done"
                        : "message.lord_of_mysteries.newspaper.shift.available");
    }

    private static String caseBulletin(String activeCommissionId) {
        if (CommissionService.LOST_CAT.toString().equals(activeCommissionId)) {
            return "message.lord_of_mysteries.newspaper.case.lost_cat";
        }
        if (CommissionService.MISSING_SQUAD.toString().equals(activeCommissionId)) {
            return "message.lord_of_mysteries.newspaper.case.missing_squad";
        }
        if (CommissionService.COUNTERFEIT_FORMULA.toString()
                .equals(activeCommissionId)) {
            return "message.lord_of_mysteries.newspaper.case.counterfeit_formula";
        }
        return "message.lord_of_mysteries.newspaper.case.none";
    }

    public record Issue(
            String headlineKey,
            String caseBulletinKey,
            String shiftBulletinKey) {}
}
