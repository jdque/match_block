package com.matchblock.engine;

public class CellRef<T extends Block> {
    private Grid<T> grid;
    private int gridX, gridY;

    public CellRef(Grid<T> grid, int x, int y) {
        this.grid = grid;
        this.gridX = x;
        this.gridY = y;
    }

    public T getTarget() {
        return grid.getBlock(gridX, gridY);
    }

    public void setTarget(T block) {
        grid.setBlock(block, gridX, gridY);
    }

    public void clearTarget() {
        grid.clearBlock(gridX, gridY);
    }

    public int x() {
        return gridX;
    }

    public int y() {
        return gridY;
    }

    public void moveBy(int dx, int dy) {
        gridX += dx;
        gridY += dy;
    }

    public void moveTo(int x, int y) {
        gridX = x;
        gridY = y;
    }
}