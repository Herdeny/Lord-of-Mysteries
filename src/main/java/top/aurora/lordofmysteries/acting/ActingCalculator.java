package top.aurora.lordofmysteries.acting;

public final class ActingCalculator {

    private ActingCalculator() {}

    public static float novelty(long lastTrigger, long now, long decayTicks) {
        if (lastTrigger <= 0L || decayTicks <= 0L || now - lastTrigger >= decayTicks) return 1f;
        float linear = (float) Math.max(0L, now - lastTrigger) / decayTicks;
        return clamp(linear, 0.1f, 1f);
    }

    public static float risk(float insanityPressure) {
        return 1f + clamp(insanityPressure, 0f, 100f) / 200f;
    }

    public static float gain(float base, float eventQuality, float novelty, float risk,
                             float potionQuality, float serverMultiplier) {
        if (base < 0f) return base;
        return base
                * clamp(eventQuality, 0.7f, 1.2f)
                * clamp(novelty, 0.1f, 1f)
                * clamp(risk, 1f, 1.5f)
                * clamp(potionQuality, 0.4f, 1.2f)
                * Math.max(0f, serverMultiplier);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
