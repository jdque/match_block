package com.matchblock.engine;

public class Piece<T extends Block> extends Grid<T> {
    public int size, gridX, gridY;

    public Piece(int size, T emptyBlock) {
        super(size, size, emptyBlock);
        this.size = size;
        this.gridX = 0;
        this.gridY = 0;
    }

    public void setPosition(int gridX, int gridY) {
        this.gridX = gridX;
        this.gridY = gridY;
    }

    public void pivot(int dx) {
        gridX += dx;
    }

    public void drop(int dy) {
        gridY += dy;
    }

    public void dropTo(int y) {
        gridY = y;
    }

    private void transpose() {
        int n = size;
        for (int x = 0; x < n - 1; x++) {
            for (int y = x + 1; y < n; y++) {
                T temp = blocks[x][y];
                blocks[x][y] = blocks[y][x];
                blocks[y][x] = temp;
            }
        }
    }

    public void rotateLeft() {
        transpose();

        int n = size;
        for (int y = 0; y < n; y++) {
            for (int x = 0; x < n / 2; x++) {
                T temp = blocks[x][y];
                blocks[x][y] = blocks[n-1-x][y];
                blocks[n-1-x][y] = temp;
            }
        }
    }

    public void rotateRight() {
        transpose();

        int n = size;
        for (int x = 0; x < n; x++) {
            for (int y = 0; y < n / 2; y++) {
                T temp = blocks[x][y];
                blocks[x][y] = blocks[x][n-1-y];
                blocks[x][n-1-y] = temp;
            }
        }
    }

    public boolean collidesLeft(Grid<T> grid) {
        if (gridX <= grid.left)
            return true;

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (!getBlock(x, y).isEmpty() &&
                        !grid.getBlock(gridX + x - 1, gridY + y).isEmpty())
                    return true;
            }
        }

        return false;
    }

    public boolean collidesRight(Grid<T> grid) {
        if (gridX + width - 1 >= grid.right)
            return true;

        for (int x = size - 1; x >= 0; x--) {
            for (int y = 0; y < size; y++) {
                if (!getBlock(x, y).isEmpty() &&
                        !grid.getBlock(gridX + x + 1, gridY + y).isEmpty())
                    return true;
            }
        }

        return false;
    }

    public boolean collidesDown(Grid<T> grid) {
        if (gridY + height - 1 >= grid.bottom)
            return true;

        for (int y = size - 1; y >= 0; y--) {
            for (int x = 0; x < size; x++) {
                if (!getBlock(x, y).isEmpty() &&
                        !grid.getBlock(gridX + x, gridY + y + 1).isEmpty())
                    return true;
            }
        }

        return false;
    }
}
