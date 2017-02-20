package com.matchblock.actors;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.matchblock.matchgame.ColoredBlock;
import com.matchblock.engine.Piece;

import java.util.Map;

public class PieceActor extends GridActor {
    public final Piece<ColoredBlock> piece;

    public PieceActor(Piece<ColoredBlock> piece, Map<ColoredBlock.Type, TextureRegion> textureMap) {
        super(piece, textureMap);
        this.piece = piece;
    }
}