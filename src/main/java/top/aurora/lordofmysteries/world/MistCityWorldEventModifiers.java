package top.aurora.lordofmysteries.world;

public final class MistCityWorldEventModifiers {

    private MistCityWorldEventModifiers() {}

    public static float spiritualityRegenMultiplier(
            MistCityWorldEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("world event is required");
        }
        return event == MistCityWorldEvent.SPIRITUAL_SURGE ? 1.5f : 1f;
    }

    public static float ritualCompletionBonus(
            MistCityWorldEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("world event is required");
        }
        return event == MistCityWorldEvent.RITUAL_RESONANCE ? 0.10f : 0f;
    }
}
