package com.matchblock.matchgame;

import com.matchblock.engine.Piece;

import java.util.Random;

public class PieceGenerator {
    private float[] freqs;

    public PieceGenerator(float one, float two, float three, float four) {
        freqs = new float[4];
        setFreqs(one, two, three, four);
    }

    public void setFreqs(float one, float two, float three, float four) {
        freqs[0] = one;
        freqs[1] = one + two;
        freqs[2] = one + two + three;
        freqs[3] = one + two + three + four;
    }

    public Piece<ColoredBlock> generate() {
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
        ColoredBlock[] blocks = new ColoredBlock[4];
        int typeCounter = 1;
        for (int i = 0; i < 4; i++) {
            blocks[i] = new ColoredBlock(blockTypes[typeCounter - 1]);
            if (typeCounter < typeCount) {
                typeCounter++;
            }
        }
        for (int i = 3; i > 0; i--) {
            int j = rand.nextInt(i+1);
            ColoredBlock temp = blocks[j];
            blocks[j] = blocks[i];
            blocks[i] = temp;
        }

        Piece<ColoredBlock> group = new Piece<>(2, new ColoredBlock(ColoredBlock.Type.NONE));
        group.setBlock(blocks[0], 0, 0);
        group.setBlock(blocks[1], 0, 1);
        group.setBlock(blocks[2], 1, 0);
        group.setBlock(blocks[3], 1, 1);

        return group;
    }
}
