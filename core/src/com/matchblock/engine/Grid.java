package com.matchblock.engine;

import com.badlogic.gdx.math.Vector2;

import java.util.BitSet;
import java.util.Iterator;

public class Grid {
    public enum ShiftDirection {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    protected Block[][] blocks;
    public final int width, height, top, bottom, left, right;

    public Grid(int width, int height) {
        this.width = width;
        this.height = height;
        this.top = 0;
        this.bottom = this.height - 1;
        this.left = 0;
        this.right = this.width - 1;
        this.blocks = new Block[height][width];

        for (int iy = 0; iy < this.height; iy++) {
            for (int ix = 0; ix < this.width; ix++) {
                this.blocks[iy][ix] = new Block.EmptyBlock();
            }
        }
    }

    public Block getBlock(int x, int y) {
        return blocks[y][x];
    }

    public void setBlock(Block block, int x, int y) {
        blocks[y][x] = block;
    }

    public void clearBlock(int x, int y) {
        blocks[y][x] = new Block.EmptyBlock();
    }

    public void moveBlock(int fromX, int fromY, int toX, int toY) {
        if (fromX == toX && fromY == toY)
            return;

        setBlock(getBlock(fromX, fromY), toX, toY);
        clearBlock(fromX, fromY);
    }

    public Grid getSubGrid(int x, int y, int width, int height) {
        Grid subGrid = new Grid(width, height);
        for (int iy = 0; iy < height; iy++) {
            if (y + iy > bottom)
                break;

            for (int ix = 0; ix < width; ix++) {
                if (x + ix > right)
                    break;
                subGrid.setBlock(getBlock(x + ix, y + iy), ix, iy);
            }
        }

        return subGrid;
    }

    public Iterable<CellRef> getRow(int rowIdx) {
        final Grid self = this;
        final int y = rowIdx;
        final Block[] row = blocks[y];
        return new Iterable<CellRef>() {
            @Override public Iterator<CellRef> iterator() {
                return new Iterator<CellRef>() {
                    int x = 0;
                    CellRef cellRef = new CellRef(self, 0, 0);

                    @Override public boolean hasNext() {
                        return x < width;
                    }

                    @Override public CellRef next() {
                        cellRef.moveTo(x, y);
                        return cellRef;
                    }

                    @Override public void remove() {

                    }
                };
            }
        };
    }

    public BitSet toBitSet(Block.State onState) {
        BitSet bitSet = new BitSet();
        for (int iy = top; iy <= bottom; iy++) {
            for (int ix = left; ix <= right; ix++) {
                if (getBlock(ix, iy).state == onState) {
                    bitSet.set(iy * width + ix, true);
                }
            }
        }

        return bitSet;
    }

    public boolean overlaps(Grid grid) {
        for (int iy = top; iy <= bottom; iy++) {
            for (int ix = left; ix <= right; ix++) {
                if (!getBlock(ix, iy).isEmpty() && !grid.getBlock(ix, iy).isEmpty())
                    return true;
            }
        }

        return false;
    }

    public void joinBlocks(Block[][] blocks, int x, int y) {
        if (blocks == null || blocks.length == 0) {
            return;
        }

        int w = blocks[0].length;
        int h = blocks.length;

        for (int iy = 0; iy < h; iy++) {
            for (int ix = 0; ix < w; ix++) {
                if (x + ix <= right && y + iy <= bottom) {
                    if (getBlock(x + ix, y + iy).isEmpty()) {
                        setBlock(blocks[iy][ix], x + ix, y + iy);
                    }
                }
            }
        }
    }

    public void joinGrid(Grid grid, int x, int y) {
        if (grid == null) {
            return;
        }

        joinBlocks(grid.blocks, x, y);
    }

    public int getFreeSpaceBelow(int column, int topY) {
        int freeY = -1;
        for (int iy = topY + 1; iy <= bottom; iy++) {
            if (getBlock(column, iy).isEmpty()) {
                freeY = iy;
            }
            else {
                break;
            }
        }

        return freeY;
    }

    public Vector2[] getColumnShiftVectors(int column) {
        Vector2[] shiftVectors = new Vector2[height];
        for (int i = 0; i < shiftVectors.length; i++) {
            shiftVectors[i] = new Vector2(0, 0);
        }
        for (int iy = bottom; iy >= top; iy--) {
            if (getBlock(column, iy).isEmpty()) {
                for (int sy = iy - 1; sy >= top; sy--) {
                    if (!getBlock(column, sy).isEmpty()) {
                        shiftVectors[sy].add(0, 1);
                    }
                }
            }
        }

        return shiftVectors;
    }

    public Vector2 getShiftVector(int cellX, int cellY) {
        Vector2 shiftVector = new Vector2(0, 0);
        for (int iy = bottom; iy > cellY; iy--) {
            if (getBlock(cellX, iy).isEmpty()) {
                shiftVector.add(0, 1);
            }
        }
        return shiftVector;
    }

    public void shiftColumnBlocks(int column, ShiftDirection direction) {
        if (direction == ShiftDirection.DOWN) {
            int emptyY = bottom;
            int emptyCount = 0;
            for (int iy = bottom; iy >= top; iy--) {
                if (getBlock(column, iy).isEmpty()) {
                    emptyCount++;
                }
                else {
                    if (emptyCount > 0) {
                        moveBlock(column, iy, column, emptyY);
                        emptyCount++;
                    }
                    emptyY--;
                }
            }
        }
    }

    public int getMaxColumnHeight() {
        int maxHeight = 0;
        for (int ix = left; ix <= right; ix++) {
            int colHeight = 0;
            for (int iy = bottom; iy >= top; iy--) {
                if (getBlock(ix, iy).isEmpty())
                    break;

                colHeight++;
            }
            if (colHeight > maxHeight) {
                maxHeight = colHeight;
            }
        }

        return maxHeight;
    }
}
