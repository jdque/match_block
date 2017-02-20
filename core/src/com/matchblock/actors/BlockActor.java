package com.matchblock.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.matchblock.matchgame.ColoredBlock;

public class BlockActor extends Actor {
    public final ColoredBlock block;
    private TextureRegion texture;

    public BlockActor(ColoredBlock block, TextureRegion texture) {
        this.block = block.clone();
        this.texture = texture;

        setColor(Color.WHITE);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        batch.draw(texture,
                getX(), getY(),
                getX() - getOriginX(), getY() - getOriginY(),
                getWidth(), getHeight(),
                getScaleX(), getScaleY(),
                getRotation());
        batch.setColor(Color.WHITE);

        super.draw(batch, parentAlpha);
    }
}
