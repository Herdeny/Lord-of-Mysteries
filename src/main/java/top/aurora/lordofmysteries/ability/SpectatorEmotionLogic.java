package top.aurora.lordofmysteries.ability;

public final class SpectatorEmotionLogic {

    public enum Emotion {
        ANGER("anger"),
        FEAR("fear"),
        CURIOSITY("curiosity"),
        CALM("calm");

        private final String id;

        Emotion(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }
    }

    private SpectatorEmotionLogic() {}

    public static Emotion classify(boolean aggressive, float healthRatio, boolean attentive) {
        if (aggressive) return Emotion.ANGER;
        if (healthRatio <= 0.25f) return Emotion.FEAR;
        if (attentive) return Emotion.CURIOSITY;
        return Emotion.CALM;
    }
}

