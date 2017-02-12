package com.matchblock.engine;

public class TimeInterval {
    private float ellapsedTime;
    private float speed;
    private boolean ready;

    public TimeInterval(float speed) {
        this.ellapsedTime = 0;
        this.speed = speed;
        this.ready = false;
    }

    public boolean ready() {
        return ready;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void reset() {
        ellapsedTime = 0;
        ready = false;
    }

    public void resetToReady() {
        ellapsedTime = speed;
        ready = true;
    }

    public void step(float deltaTime) {
        ellapsedTime += deltaTime;
        if (ready) {
            ready = false;
        }

        if (ellapsedTime >= speed) {
            ellapsedTime = 0;
            ready = true;
        }
    }
}
