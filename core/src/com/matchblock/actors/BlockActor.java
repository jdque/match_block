package com.matchblock.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.matchblock.matchgame.ColoredBlock;

public class BlockActor extends Actor {
    public final ColoredBlock block;
    private Texture texture;

    public BlockActor(ColoredBlock block, Texture texture) {
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
        super.draw(batch, parentAlpha);

        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        batch.draw(texture, getX(), getY(), getWidth(), getHeight());
        batch.setColor(Color.WHITE);
    }
}
