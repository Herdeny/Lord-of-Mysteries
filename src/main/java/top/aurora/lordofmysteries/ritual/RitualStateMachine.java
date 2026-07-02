package top.aurora.lordofmysteries.ritual;

public final class RitualStateMachine {

    public enum State {
        IDLE, ASSEMBLED, PRIMED, INVOKING, RESOLVING, COMPLETE, FAILED, CANCELLED
    }

    private State state = State.IDLE;

    public State state() {
        return state;
    }

    public boolean assemble(boolean structureComplete) {
        if (state != State.IDLE || !structureComplete) return false;
        state = State.ASSEMBLED;
        return true;
    }

    public boolean prime(boolean environmentValid, boolean materialsValid) {
        if (state != State.ASSEMBLED || !environmentValid || !materialsValid) return false;
        state = State.PRIMED;
        return true;
    }

    public boolean invoke() {
        if (state != State.PRIMED) return false;
        state = State.INVOKING;
        return true;
    }

    public boolean beginResolve() {
        if (state != State.INVOKING) return false;
        state = State.RESOLVING;
        return true;
    }

    public boolean finish(boolean success) {
        if (state != State.RESOLVING) return false;
        state = success ? State.COMPLETE : State.FAILED;
        return true;
    }

    public void cancel() {
        if (state != State.COMPLETE && state != State.FAILED) state = State.CANCELLED;
    }

    public void reset() {
        state = State.IDLE;
    }

    public void restore(State restored) {
        state = restored == null ? State.IDLE : restored;
    }
}
