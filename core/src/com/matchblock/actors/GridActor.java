package com.matchblock.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.matchblock.engine.Block;
import com.matchblock.engine.ColoredBlock;
import com.matchblock.engine.Grid;

public class GridActor extends Actor {
    private final Grid grid;
    private ShapeRenderer shapeRenderer;
    private float cellWidth, cellHeight;

    public GridActor(Grid grid, ShapeRenderer shapeRenderer) {
        this.grid = grid;
        this.shapeRenderer = shapeRenderer;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        //batch.end();
        for (int iy = 0; iy < grid.height; iy++) {
            for (int ix = 0; ix < grid.width; ix++) {
                Color color = ColoredBlock.getBlockColor(grid.getBlock(ix, iy));
                if (color != null) {
                    float drawX = toStageX(ix);
                    float drawY = toStageY(iy);
                    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                    shapeRenderer.setColor(color);
                    shapeRenderer.rect(drawX, drawY, cellWidth, cellHeight);
                    shapeRenderer.end();
                    shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                    shapeRenderer.setColor(Color.WHITE);
                    shapeRenderer.rect(drawX, drawY, cellWidth, cellHeight);
                    shapeRenderer.end();
                }
            }
        }
        //batch.begin();
    }

    @Override
    protected void sizeChanged() {
        cellWidth = getWidth() / grid.width;
        cellHeight = getHeight() / grid.height;
    }

    public float getCellWidth() {
        return cellWidth;
    }

    public float getCellHeight() {
        return cellHeight;
    }

    public float toStageX(int gridX) {
        return getX() + gridX * cellWidth;
    }

    public float toStageY(int gridY) {
        return (getY() + getHeight()) - (gridY * cellHeight) - cellHeight;
    }
}
