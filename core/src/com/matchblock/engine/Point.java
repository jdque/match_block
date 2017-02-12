package com.matchblock.engine;

public class Point {
    public final int x;
    public final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object point) {
        return x == ((Point)point).x && y == ((Point)point).y;
    }

    @Override
    public int hashCode() {
        return x * 31 + y;
    }
}
