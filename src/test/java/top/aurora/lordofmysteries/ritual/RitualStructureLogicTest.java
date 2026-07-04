package top.aurora.lordofmysteries.ritual;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class RitualStructureLogicTest {

    @Test
    void radiusThreeCircleUsesEightUniqueMarkers() {
        var offsets = RitualStructureLogic.circleOffsets(3);
        assertEquals(8, offsets.size());
        assertEquals(8, offsets.stream().distinct().count());
    }

    @Test
    void completionIsClamped() {
        assertEquals(0f, RitualStructureLogic.completion(-1, 8));
        assertEquals(0.5f, RitualStructureLogic.completion(4, 8));
        assertEquals(1f, RitualStructureLogic.completion(12, 8));
        assertEquals(0f, RitualStructureLogic.completion(1, 0));
    }

    @Test
    void circleRejectsInvalidRadius() {
        assertThrows(IllegalArgumentException.class,
                () -> RitualStructureLogic.circleOffsets(1));
    }
}
