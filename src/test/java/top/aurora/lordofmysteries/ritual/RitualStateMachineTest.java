package top.aurora.lordofmysteries.ritual;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RitualStateMachineTest {

    @Test
    void validRitualMovesThroughAllPhases() {
        RitualStateMachine machine = new RitualStateMachine();
        assertTrue(machine.assemble(true));
        assertTrue(machine.prime(true, true));
        assertTrue(machine.invoke());
        assertTrue(machine.beginResolve());
        assertTrue(machine.finish(true));
        assertEquals(RitualStateMachine.State.COMPLETE, machine.state());
    }

    @Test
    void invalidEnvironmentCannotPrime() {
        RitualStateMachine machine = new RitualStateMachine();
        machine.assemble(true);
        assertFalse(machine.prime(false, true));
        assertEquals(RitualStateMachine.State.ASSEMBLED, machine.state());
    }

    @Test
    void resolutionCannotRunTwice() {
        RitualStateMachine machine = new RitualStateMachine();
        machine.assemble(true);
        machine.prime(true, true);
        machine.invoke();
        machine.beginResolve();
        assertTrue(machine.finish(false));
        assertFalse(machine.finish(true));
        assertEquals(RitualStateMachine.State.FAILED, machine.state());
    }

    @Test
    void interruptedResolutionRestoresToSafeInvokingState() {
        RitualStateMachine machine = new RitualStateMachine();
        machine.restore(RitualStateMachine.State.RESOLVING);
        assertEquals(RitualStateMachine.State.INVOKING, machine.state());
        assertTrue(machine.beginResolve());
    }
}
