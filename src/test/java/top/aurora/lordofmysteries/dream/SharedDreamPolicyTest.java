package top.aurora.lordofmysteries.dream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import top.aurora.lordofmysteries.spirit.SpiritEcologyKind;
import top.aurora.lordofmysteries.spirit.SpiritEncounter;

class SharedDreamPolicyTest {

    @Test
    void scenariosAndSymbolsAreDeterministicAndBounded() {
        UUID host = UUID.fromString("10000000-0000-0000-0000-000000000001");
        DreamScenario scenario = SharedDreamPolicy.scenario(73L, host);
        assertEquals(scenario, SharedDreamPolicy.scenario(73L, host));
        Set<DreamSymbol> symbols = new HashSet<>();
        for (int step = 0; step < SharedDreamPolicy.SYMBOL_STEPS; step++) {
            DreamSymbol symbol = SharedDreamPolicy.symbol(73L, scenario, step);
            assertNotNull(symbol);
            assertNotNull(symbol.preferredAction());
            symbols.add(symbol);
        }
        assertFalse(symbols.isEmpty());
        assertThrows(IllegalArgumentException.class,
                () -> SharedDreamPolicy.symbol(
                        73L, scenario, SharedDreamPolicy.SYMBOL_STEPS));
    }

    @Test
    void correctAndIncorrectActionsHaveRecoverableOppositeOutcomes() {
        DreamSymbol symbol = DreamSymbol.BACKWARD_CLOCK;
        SharedDreamPolicy.ActionOutcome correct = SharedDreamPolicy.resolve(
                symbol, symbol.preferredAction(), 4);
        SharedDreamPolicy.ActionOutcome wrong = SharedDreamPolicy.resolve(
                symbol, DreamAction.COMFORT, 4);
        assertTrue(correct.correct());
        assertTrue(correct.coherenceDelta() > 0);
        assertTrue(correct.traumaDelta() < 0);
        assertFalse(wrong.correct());
        assertTrue(wrong.coherenceDelta() < 0);
        assertTrue(wrong.traumaDelta() > 0);
    }

    @Test
    void timeAndRiskMathSaturateWithoutOverflow() {
        assertEquals(Long.MAX_VALUE,
                SharedDreamPolicy.deadline(Long.MAX_VALUE - 2L, 40L));
        assertEquals(0, SharedDreamPolicy.clampCoherence(-99));
        assertEquals(SharedDreamPolicy.MAX_COHERENCE,
                SharedDreamPolicy.clampCoherence(Integer.MAX_VALUE));
        assertEquals(SharedDreamPolicy.MAX_TRAUMA,
                SharedDreamPolicy.clampTrauma(Integer.MAX_VALUE));
        assertTrue(SharedDreamPolicy.failed(0, 0));
        assertTrue(SharedDreamPolicy.failed(50, 100));
    }

    @Test
    void allTwelveEncounterProfilesHavePhysicalEntityMappings() {
        assertEquals(12, SpiritEncounter.values().length);
        assertEquals(12, SpiritEcologyKind.values().length);
        for (SpiritEncounter encounter : SpiritEncounter.values()) {
            SpiritEcologyKind kind = SpiritEcologyKind.fromId(encounter.id());
            assertNotNull(kind);
            assertEquals(encounter, kind.encounter());
        }
    }
}
