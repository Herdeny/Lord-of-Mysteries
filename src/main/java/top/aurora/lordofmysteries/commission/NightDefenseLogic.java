package top.aurora.lordofmysteries.commission;

public final class NightDefenseLogic {

    private NightDefenseLogic() {}

    public static Action decide(boolean night, boolean waveSpawned,
                                boolean enemiesRemain, long gameTime,
                                long nextWaveTick) {
        if (waveSpawned) {
            return enemiesRemain ? Action.WAIT : Action.COMPLETE_WAVE;
        }
        if (!night) return Action.WAIT_FOR_NIGHT;
        return gameTime >= nextWaveTick ? Action.SPAWN_WAVE : Action.WAIT;
    }

    public enum Action {
        WAIT_FOR_NIGHT,
        WAIT,
        SPAWN_WAVE,
        COMPLETE_WAVE
    }
}
