package top.aurora.lordofmysteries.ritual;

public final class RitualRecoveryLogic {

    public static final int MAX_OFFLINE_TICKS = 1200;

    private RitualRecoveryLogic() {}

    public static Action decide(boolean invoking, boolean leaderPresent,
                                int offlineTicks) {
        if (!invoking) return Action.IGNORE;
        if (leaderPresent) return Action.CONTINUE;
        return offlineTicks >= MAX_OFFLINE_TICKS ? Action.CANCEL : Action.PAUSE;
    }

    public enum Action {
        IGNORE,
        CONTINUE,
        PAUSE,
        CANCEL
    }
}
