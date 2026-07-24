package top.aurora.lordofmysteries.world;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MistCityWorldEventModifiersTest {

    @Test
    void onlySpiritualSurgeAcceleratesNaturalRecovery() {
        for (MistCityWorldEvent event : MistCityWorldEvent.values()) {
            assertEquals(
                    event == MistCityWorldEvent.SPIRITUAL_SURGE ? 1.5f : 1f,
                    MistCityWorldEventModifiers
                            .spiritualityRegenMultiplier(event));
        }
    }

    @Test
    void onlyRitualResonanceImprovesRitualCompletion() {
        for (MistCityWorldEvent event : MistCityWorldEvent.values()) {
            assertEquals(
                    event == MistCityWorldEvent.RITUAL_RESONANCE ? 0.10f : 0f,
                    MistCityWorldEventModifiers
                            .ritualCompletionBonus(event));
        }
    }
}
