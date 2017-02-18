package com.matchblock.engine.state;

public class Transition {
    public interface Handler {
        void update(Transition transition);
    }

    public static class ImmediateHandler implements Handler {
        @Override public void update(Transition transition) {
            transition.proceed();
        }
    }

    public final Object from;
    public final Object to;
    private final StateMachine context;

    public Transition(Object fromId, Object toId, StateMachine context) {
        this.from = fromId;
        this.to = toId;
        this.context = context;
    }

    public boolean isFromTo(Object fromId, Object toId) {
        return this.from == fromId && this.to == toId;
    }

    public void proceed() {
        context.enter(to);
    }
}
