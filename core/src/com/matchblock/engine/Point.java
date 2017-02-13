package com.matchblock.engine;

public class Point {
    public final int x;
    public final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (getClass() != o.getClass()) {
            return false;
        }
        Point pt = (Point) o;
        return x == pt.x && y == pt.y;
    }

    @Override
    public int hashCode() {
        return x * 31 + y;
    }
}
