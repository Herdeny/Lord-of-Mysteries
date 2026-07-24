package top.aurora.lordofmysteries.world;

public final class MistCityWorldEventPlayerEffects {

    private MistCityWorldEventPlayerEffects() {}

    public static boolean obscuresOutdoorVision(
            MistCityWorldEvent event, boolean canSeeSky) {
        if (event == null) {
            throw new IllegalArgumentException("world event is required");
        }
        return event == MistCityWorldEvent.DENSE_FOG && canSeeSky;
    }

    public static MinuteEffect minuteEffect(
            MistCityWorldEvent event,
            boolean beyonder,
            boolean sheltered) {
        if (event == null) {
            throw new IllegalArgumentException("world event is required");
        }
        if (!beyonder) return MinuteEffect.NONE;
        return switch (event) {
            case BLOOD_MOON -> new MinuteEffect(0.4f, 0f);
            case EVIL_GAZE -> new MinuteEffect(0.2f, 0f);
            case WITCH_HUNT_NIGHT -> sheltered
                    ? MinuteEffect.NONE
                    : new MinuteEffect(0.3f, 0.5f);
            default -> MinuteEffect.NONE;
        };
    }

    public record MinuteEffect(
            float pressureIncrease,
            float exposureIncrease) {

        private static final MinuteEffect NONE =
                new MinuteEffect(0f, 0f);

        public MinuteEffect {
            if (!Float.isFinite(pressureIncrease)
                    || !Float.isFinite(exposureIncrease)
                    || pressureIncrease < 0f
                    || exposureIncrease < 0f) {
                throw new IllegalArgumentException(
                        "world event player effects must be finite and non-negative");
            }
        }

        public boolean active() {
            return pressureIncrease > 0f || exposureIncrease > 0f;
        }
    }
}
