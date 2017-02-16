package com.matchblock.matchgame;

import com.badlogic.gdx.math.CumulativeDistribution;
import com.matchblock.engine.Piece;

import java.util.Random;

public class PieceGenerator {
    private CumulativeDistribution<Integer> typeCountDist;

    public PieceGenerator(float one, float two, float three, float four) {
        CumulativeDistribution<Integer> typeCountDist = new CumulativeDistribution<>();
        typeCountDist.add(1, one);
        typeCountDist.add(2, two);
        typeCountDist.add(3, three);
        typeCountDist.add(4, four);
        typeCountDist.generate();
        this.typeCountDist = typeCountDist;
    }

    public void setTypeCountDist(CumulativeDistribution<Integer> typeCountDist) {
        this.typeCountDist = typeCountDist;
    }

    public Piece<ColoredBlock> generate() {
        //Get type count from frequency distribution
        Random rand = new Random();
        int typeCount = typeCountDist.value(rand.nextFloat());

        //Randomize block types
        ColoredBlock.Type[] blockTypes = ColoredBlock.Type.values();
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
