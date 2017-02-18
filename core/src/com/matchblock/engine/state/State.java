package com.matchblock.engine.state;

public abstract class State {
    public void update(float delta) {}
    public void enter(Object previous) {}
    public void exit(Object next) {}
}
