package top.aurora.lordofmysteries.knowledge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GuideDirectionTest {

    @Test
    void mapsCardinalAndDiagonalDirections() {
        assertEquals("east", GuideDirection.fromDelta(10d, 0d));
        assertEquals("south", GuideDirection.fromDelta(0d, 10d));
        assertEquals("west", GuideDirection.fromDelta(-10d, 0d));
        assertEquals("north", GuideDirection.fromDelta(0d, -10d));
        assertEquals("southeast", GuideDirection.fromDelta(10d, 10d));
        assertEquals("northwest", GuideDirection.fromDelta(-10d, -10d));
        assertEquals("here", GuideDirection.fromDelta(0d, 0d));
    }
}
