package com.matchblock.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.matchblock.engine.Block;
import com.matchblock.engine.ColoredBlock;

public class BlockActor extends Actor {
    public final ColoredBlock block;
    private ShapeRenderer shapeRenderer;

    public BlockActor(ColoredBlock block, ShapeRenderer shapeRenderer) {
        this.block = block.clone();
        this.shapeRenderer = shapeRenderer;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        //batch.end();
        float worldX = this.getX();
        float worldY = this.getY();
        float worldWidth = this.getWidth();
        float worldHeight = this.getHeight();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(getColor());
        shapeRenderer.rect(worldX, worldY, worldWidth, worldHeight);
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(255, 255, 255, getColor().a);
        shapeRenderer.rect(worldX, worldY, worldWidth, worldHeight);
        shapeRenderer.end();
        //batch.begin();
    }
}
