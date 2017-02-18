package com.matchblock.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.matchblock.matchgame.ColoredBlock;
import com.matchblock.engine.Grid;

import java.util.Map;

public class GridActor extends Group {
    private final Grid<ColoredBlock> grid;
    private final Map<ColoredBlock.Type, Texture> textureMap;
    private float cellWidth, cellHeight;

    public GridActor(Grid<ColoredBlock> grid, Map<ColoredBlock.Type, Texture> textureMap) {
        this.grid = grid;
        this.textureMap = textureMap;

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
        for (int iy = 0; iy < grid.height; iy++) {
            for (int ix = 0; ix < grid.width; ix++) {
                float drawX = getX() + toRelativeX(ix);
                float drawY = getY() + toRelativeY(iy);
                Texture texture = textureMap.get(grid.getBlock(ix, iy).type);
                if (texture != null) {
                    batch.draw(texture, drawX, drawY, cellWidth, cellHeight);
                }
            }
        }
        batch.setColor(Color.WHITE);
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

    public float toRelativeX(int gridX) {
        return gridX * cellWidth;
    }

    public float toRelativeY(int gridY) {
        return getHeight() - (gridY * cellHeight) - cellHeight;
    }

    public BlockActor popOut(int gridX, int gridY) {
        ColoredBlock block = grid.getBlock(gridX, gridY);
        BlockActor blockActor = new BlockActor(block, textureMap.get(block.type));
        blockActor.setSize(cellWidth, cellHeight);
        blockActor.setPosition(toRelativeX(gridX), toRelativeY(gridY));
        addActor(blockActor);

        grid.clearBlock(gridX, gridY);

        return blockActor;
    }

    public void popIn(BlockActor blockActor, int gridX, int gridY) {
        ColoredBlock block = blockActor.block;
        grid.setBlock(block, gridX, gridY);

        removeActor(blockActor);
    }
}
