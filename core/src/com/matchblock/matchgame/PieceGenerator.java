package com.matchblock.matchgame;

import com.badlogic.gdx.math.CumulativeDistribution;
import com.matchblock.engine.Piece;

import java.util.*;

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
        List<ColoredBlock.Type> blockTypes = new ArrayList<>(Arrays.asList(ColoredBlock.Type.values()));
        blockTypes.remove(ColoredBlock.Type.NONE);
        Collections.shuffle(blockTypes);

        //Randomize block type positioning
        List<ColoredBlock> blocks = new ArrayList<>();
        int typeCounter = 1;
        for (int i = 0; i < 4; i++) {
            blocks.add(new ColoredBlock(blockTypes.get(typeCounter - 1)));
            if (typeCounter < typeCount) {
                typeCounter++;
            }
        }
        Collections.shuffle(blocks);

        Piece<ColoredBlock> piece = new Piece<>(2, new ColoredBlock(ColoredBlock.Type.NONE));
        piece.setBlock(blocks.get(0), 0, 0);
        piece.setBlock(blocks.get(1), 0, 1);
        piece.setBlock(blocks.get(2), 1, 0);
        piece.setBlock(blocks.get(3), 1, 1);

        return piece;
    }
}
