package top.aurora.lordofmysteries.commission;

public final class RescueApproachPolicy {

    private static final String SEER = "lord_of_mysteries:seer";
    private static final String THIEF = "lord_of_mysteries:thief";
    private static final String APPRENTICE = "lord_of_mysteries:apprentice";

    private RescueApproachPolicy() {}

    public static boolean stealthAllowed(String pathway, int sequence) {
        return sequence >= 0 && sequence <= 9
                && (THIEF.equals(pathway) || APPRENTICE.equals(pathway));
    }

    public static boolean divinationAllowed(String pathway, int sequence,
                                             float spirituality) {
        return SEER.equals(pathway) && sequence >= 0 && sequence <= 9
                && spirituality >= 12f;
    }
}
