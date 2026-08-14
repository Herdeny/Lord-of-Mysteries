package top.aurora.lordofmysteries.ritual;

public final class RitualRecoveryLogic {

    public static final int MAX_OFFLINE_TICKS = 1200;

    private RitualRecoveryLogic() {}

    public static Action decide(boolean invoking, boolean leaderPresent,
                                int offlineTicks) {
        return decide(invoking, leaderPresent, leaderPresent, offlineTicks);
    }

    public static Action decide(boolean invoking, boolean leaderPresent,
                                boolean leaderEligible, int offlineTicks) {
        if (!invoking) return Action.IGNORE;
        if (leaderPresent) {
            return leaderEligible ? Action.CONTINUE : Action.CANCEL;
        }
        return offlineTicks >= MAX_OFFLINE_TICKS ? Action.CANCEL : Action.PAUSE;
    }

    public enum Action {
        IGNORE,
        CONTINUE,
        PAUSE,
        CANCEL
    }
}
