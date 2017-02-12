package com.matchblock.matchgame;

import com.matchblock.engine.Block;
import com.matchblock.engine.CellRef;
import com.matchblock.engine.ColoredBlock;
import com.matchblock.engine.Grid;

import java.util.Random;

public class Piece extends Grid {
    public static class Generator {
        private float[] freqs;

        public Generator(float one, float two, float three, float four) {
            freqs = new float[4];
            setFreqs(one, two, three, four);
        }

        public void setFreqs(float one, float two, float three, float four) {
            freqs[0] = one;
            freqs[1] = one + two;
            freqs[2] = one + two + three;
            freqs[3] = one + two + three + four;
        }

        public Piece generate() {
            //Get type count from frequency distribution
            Random rand = new Random();
            float typeCountFreq = rand.nextFloat();
            int typeCount = 1;
            for (int i = 0; i < freqs.length; i++) {
                if (freqs[i] >= typeCountFreq) {
                    typeCount = i + 1;
                    break;
                }
            }

            //Randomize block types
            ColoredBlock.Type[] blockTypes = new ColoredBlock.Type[] {
                    ColoredBlock.Type.RED,
                    ColoredBlock.Type.BLUE,
                    ColoredBlock.Type.GREEN,
                    ColoredBlock.Type.PURPLE,
                    ColoredBlock.Type.ORANGE,
                    ColoredBlock.Type.MAGENTA
            };
            for (int i = blockTypes.length - 1; i > 0; i--) {
                int j = rand.nextInt(i+1);
                ColoredBlock.Type temp = blockTypes[j];
                blockTypes[j] = blockTypes[i];
                blockTypes[i] = temp;
            }

            //Randomize block type positioning
            Block[] blocks = new Block[4];
            int typeCounter = 1;
            for (int i = 0; i < 4; i++) {
                blocks[i] = new ColoredBlock(blockTypes[typeCounter - 1]);
                if (typeCounter < typeCount) {
                    typeCounter++;
                }
            }
            for (int i = 3; i > 0; i--) {
                int j = rand.nextInt(i+1);
                Block temp = blocks[j];
                blocks[j] = blocks[i];
                blocks[i] = temp;
            }

            Piece group = new Piece(2);
            group.setBlock(blocks[0], 0, 0);
            group.setBlock(blocks[1], 0, 1);
            group.setBlock(blocks[2], 1, 0);
            group.setBlock(blocks[3], 1, 1);

            return group;
        }
    }

    public int size, gridX, gridY;

    public Piece(int size) {
        super(size, size);
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
                Block temp = blocks[x][y];
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
                Block temp = blocks[x][y];
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
                Block temp = blocks[x][y];
                blocks[x][y] = blocks[x][n-1-y];
                blocks[x][n-1-y] = temp;
            }
        }
    }

    public boolean collidesLeft(Grid grid) {
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

    public boolean collidesRight(Grid grid) {
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

    public boolean collidesDown(Grid grid) {
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
