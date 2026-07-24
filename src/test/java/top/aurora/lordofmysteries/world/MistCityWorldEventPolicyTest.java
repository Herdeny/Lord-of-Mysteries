package top.aurora.lordofmysteries.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.EnumMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import net.minecraft.nbt.CompoundTag;

class MistCityWorldEventPolicyTest {

    @Test
    void eventCalendarIsDeterministicAndContainsEveryEvent() {
        long seed = 918273645L;
        EnumSet<MistCityWorldEvent> events =
                EnumSet.noneOf(MistCityWorldEvent.class);
        Map<MistCityWorldEvent, Integer> counts =
                new EnumMap<>(MistCityWorldEvent.class);
        for (long day = 0L;
                day < MistCityWorldEventPolicy.CYCLE_DAYS;
                day++) {
            MistCityWorldEvent first =
                    MistCityWorldEventPolicy.eventForDay(seed, day);
            MistCityWorldEvent second =
                    MistCityWorldEventPolicy.eventForDay(seed, day);
            assertEquals(first, second);
            events.add(first);
            counts.merge(first, 1, Integer::sum);
        }
        assertEquals(EnumSet.allOf(MistCityWorldEvent.class), events);
        assertEquals(2, counts.get(MistCityWorldEvent.DENSE_FOG));
        assertEquals(3, counts.get(
                MistCityWorldEvent.WITCH_HUNT_NIGHT));
        assertEquals(1, counts.get(
                MistCityWorldEvent.SPIRITUAL_SURGE));
        assertEquals(1, counts.get(
                MistCityWorldEvent.RITUAL_RESONANCE));
        assertEquals(7, counts.get(MistCityWorldEvent.CLEAR));
    }

    @Test
    void savedCalendarStateIsIdempotentAcrossRestart() {
        MistCityWorldEventSavedData data =
                new MistCityWorldEventSavedData();
        assertTrue(data.update(5L, MistCityWorldEvent.SPIRITUAL_SURGE));
        assertFalse(data.update(5L, MistCityWorldEvent.SPIRITUAL_SURGE));

        MistCityWorldEventSavedData restored =
                MistCityWorldEventSavedData.load(
                        data.save(new CompoundTag()));
        assertEquals(5L, restored.currentDay());
        assertEquals(MistCityWorldEvent.SPIRITUAL_SURGE,
                restored.currentEvent());
        assertFalse(restored.update(
                5L, MistCityWorldEvent.SPIRITUAL_SURGE));
    }

    @Test
    void invalidPersistedEventFallsBackToSafeClearState() {
        CompoundTag invalid = new CompoundTag();
        invalid.putLong("current_day", 9L);
        invalid.putString("current_event", "removed_event");

        MistCityWorldEventSavedData restored =
                MistCityWorldEventSavedData.load(invalid);

        assertEquals(9L, restored.currentDay());
        assertEquals(MistCityWorldEvent.CLEAR,
                restored.currentEvent());
    }
}
