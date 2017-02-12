package com.matchblock.engine;

import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;

public class CellAreaRef {
    private Grid grid;
    private Rectangle rect;

    public CellAreaRef(Grid grid, Rectangle rect) {
        this.grid = grid;
        this.rect = rect;
    }

    public CellAreaRef(Grid grid, int x, int y, int width, int height) {
        this(grid, new Rectangle(x, y, width, height));
    }

    public ArrayList<Block> getTargets() {
        ArrayList blocks = new ArrayList<Block>();
        for (int y = (int)rect.y; y < (int)(rect.y + rect.height); y++) {
            for (int x = (int)rect.x; x < (int)(rect.x + rect.width); x++) {
                blocks.add(grid.getBlock(x, y));
            }
        }
        return blocks;
    }

    public Grid getTargetsAsGrid() {
        return grid.getSubGrid((int)rect.x, (int)rect.y, (int)rect.width, (int)rect.height);
    }

    public int x() {
        return (int)rect.x;
    }

    public int y() {
        return (int)rect.y;
    }

    public void moveBy(int dx, int dy) {
        rect.x += dx;
        rect.y += dy;
    }

    public void moveTo(int x, int y) {
        rect.x = x;
        rect.y = y;
    }

    public boolean collidesLeft() {
        int x0 = (int)rect.x;
        int x1 = x0 - 1;
        int y0 = (int)(rect.y);
        int y1 = y0 + (int)rect.height;
        for (int y = y0; y < y1; y++) {
            if (!grid.getBlock(x0, y).isEmpty() && !grid.getBlock(x1, y).isEmpty())
                return true;
        }
        return false;
    }

    public boolean collidesRight() {
        int x0 = (int)(rect.x + rect.width) - 1;
        int x1 = x0 + 1;
        int y0 = (int)(rect.y);
        int y1 = y0 + (int)rect.height;
        for (int y = y0; y < y1; y++) {
            if (!grid.getBlock(x0, y).isEmpty() && !grid.getBlock(x1, y).isEmpty())
                return true;
        }
        return false;
    }

    public boolean collidesDown() {
        int y0 = (int)(rect.y + rect.height) - 1;
        int y1 = y0 + 1;
        int x0 = (int)rect.x;
        int x1 = x0 + (int)rect.width;
        for (int x = x0; x < x1; x++) {
            if (!grid.getBlock(x, y0).isEmpty() && !grid.getBlock(x, y1).isEmpty())
                return true;
        }
        return false;
    }
}
