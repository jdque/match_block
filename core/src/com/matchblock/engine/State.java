package com.matchblock.engine;

import java.util.HashMap;
import java.util.Map;

public abstract class State {
    public void update(float delta) {}
    public void enter(Object previous) {}
    public void exit(Object next) {}

    public static class Transition {
        public final Object from;
        public final Object to;
        private final Context context;

        public Transition(Object fromId, Object toId, Context context) {
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

    public interface TransitionHandler {
        void update(Transition transition);
    }

    private static class ImmediateTransitionHandler implements TransitionHandler {
        @Override public void update(Transition transition) {
            transition.proceed();
        }
    }

    public static class Context {
        private Map<Object, State> stateMap;
        private Map<Object, Runnable> transitionMap;
        private State nullState;
        private Object currentId;
        private State currentState;
        private Transition currentTransition;
        private TransitionHandler currentTransitionHandler;

        public Context() {
            this.stateMap = new HashMap<Object, State>();
            this.nullState = new State() {};
            this.currentId = null;
            this.currentState = nullState;

            this.currentTransition = null;
            this.currentTransitionHandler = null;
        }

        public void add(Object id, State state) {
            stateMap.put(id, state);
        }

        public final Object get() {
            return currentId;
        }

        public void set(Object nextId) {
            if (!stateMap.containsKey(nextId))
                return;
            State nextState = stateMap.get(nextId);

            currentTransition = null;
            currentTransitionHandler = null;

            currentState.exit(nextId);
            currentState = nullState;

            nextState.enter(currentId);
            currentId = nextId;
            currentState = nextState;
        }

        public void set(Object nextId, TransitionHandler transitionHandler) {
            if (!stateMap.containsKey(nextId))
                return;

            currentTransition = new Transition(currentId, nextId, this);
            currentTransitionHandler = transitionHandler;

            currentState.exit(nextId);
            currentState = nullState;
        }

        private void enter(Object id) {
            State state = stateMap.get(id);
            state.enter(currentId);
            currentId = id;
            currentState = state;

            currentTransition = null;
            currentTransitionHandler = null;
        }

        public void update(float delta) {
            if (currentState != null) {
                currentState.update(delta);
            }
            if (currentTransition != null) {
                if (currentTransitionHandler != null) {
                    currentTransitionHandler.update(currentTransition);
                }
                else {
                    enter(currentTransition.to);
                }
            }
        }
    }
}
