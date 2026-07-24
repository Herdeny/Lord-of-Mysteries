package top.aurora.lordofmysteries.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MistCityWorldEventPlayerEffectsTest {

    @Test
    void denseFogOnlyObscuresOutdoorVision() {
        assertTrue(MistCityWorldEventPlayerEffects
                .obscuresOutdoorVision(
                        MistCityWorldEvent.DENSE_FOG, true));
        assertFalse(MistCityWorldEventPlayerEffects
                .obscuresOutdoorVision(
                        MistCityWorldEvent.DENSE_FOG, false));
        assertFalse(MistCityWorldEventPlayerEffects
                .obscuresOutdoorVision(
                        MistCityWorldEvent.CLEAR, true));
    }

    @Test
    void witchHuntExemptsCommonersAndHonorsShelter() {
        assertFalse(MistCityWorldEventPlayerEffects.minuteEffect(
                MistCityWorldEvent.WITCH_HUNT_NIGHT,
                false, false).active());
        assertFalse(MistCityWorldEventPlayerEffects.minuteEffect(
                MistCityWorldEvent.WITCH_HUNT_NIGHT,
                true, true).active());

        MistCityWorldEventPlayerEffects.MinuteEffect exposed =
                MistCityWorldEventPlayerEffects.minuteEffect(
                        MistCityWorldEvent.WITCH_HUNT_NIGHT,
                        true, false);
        assertEquals(0.3f, exposed.pressureIncrease());
        assertEquals(0.5f, exposed.exposureIncrease());
    }

    @Test
    void bloodMoonAndEvilGazeApplyMinutePressure() {
        assertEquals(0.4f,
                MistCityWorldEventPlayerEffects.minuteEffect(
                        MistCityWorldEvent.BLOOD_MOON,
                        true, false).pressureIncrease());
        assertEquals(0.2f,
                MistCityWorldEventPlayerEffects.minuteEffect(
                        MistCityWorldEvent.EVIL_GAZE,
                        true, false).pressureIncrease());
        assertFalse(MistCityWorldEventPlayerEffects.minuteEffect(
                MistCityWorldEvent.RITUAL_RESONANCE,
                true, false).active());
    }

    @Test
    void nullEventsAreRejectedInsteadOfSilentlyApplyingEffects() {
        assertThrows(IllegalArgumentException.class,
                () -> MistCityWorldEventPlayerEffects
                        .obscuresOutdoorVision(null, true));
        assertThrows(IllegalArgumentException.class,
                () -> MistCityWorldEventPlayerEffects
                        .minuteEffect(null, true, false));
    }
}
