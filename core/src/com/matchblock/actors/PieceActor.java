package com.matchblock.actors;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.matchblock.engine.ColoredBlock;
import com.matchblock.matchgame.Piece;

public class PieceActor extends GridActor {
    public final Piece<ColoredBlock> piece;

    public PieceActor(Piece<ColoredBlock> piece, ShapeRenderer shapeRenderer) {
        super(piece, shapeRenderer);
        this.piece = piece;
    }
}