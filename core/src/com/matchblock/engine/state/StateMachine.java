package com.matchblock.engine.state;

import java.util.HashMap;
import java.util.Map;

public class StateMachine {
    private Map<Object, State> stateMap;
    private Map<Object, Runnable> transitionMap;
    private State nullState;
    private Object currentId;
    private State currentState;
    private Transition currentTransition;
    private Transition.Handler currentTransitionHandler;

    public StateMachine() {
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

    public void set(Object nextId, Transition.Handler transitionHandler) {
        if (!stateMap.containsKey(nextId))
            return;

        currentTransition = new Transition(currentId, nextId, this);
        currentTransitionHandler = transitionHandler;

        currentState.exit(nextId);
        currentState = nullState;
    }

    public void enter(Object id) {
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
