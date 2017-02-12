package com.matchblock.actors;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.matchblock.matchgame.Piece;

public class PieceActor extends GridActor {
    public final Piece piece;

    public PieceActor(Piece piece, ShapeRenderer shapeRenderer) {
        super(piece, shapeRenderer);
        this.piece = piece;
    }
}
