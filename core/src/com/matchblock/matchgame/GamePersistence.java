package com.matchblock.matchgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.matchblock.engine.ColoredBlock;
import com.matchblock.engine.Grid;

import org.json.JSONException;
import org.json.JSONObject;

public class GamePersistence {
    public static class Saver {
        private JSONObject data;

        public Saver() {
            data = new JSONObject();
        }

        public Saver savePiece(Piece<ColoredBlock> piece, String key) throws JSONException {
            JSONObject pieceObj = new JSONObject();
            pieceObj.put("x", piece.gridX);
            pieceObj.put("y", piece.gridY);
            String activePieceBlocks =
                    piece.getBlock(0, 0).toString() +
                    piece.getBlock(1, 0).toString() +
                    piece.getBlock(0, 1).toString() +
                    piece.getBlock(1, 1).toString();
            pieceObj.put("blocks", activePieceBlocks);
            data.put(key, pieceObj);

            return this;
        }

        public Saver saveGrid(Grid<ColoredBlock> grid, String key)  throws JSONException {
            JSONObject gridObj = new JSONObject();
            StringBuilder builder = new StringBuilder();
            for (int iy = 0; iy < grid.height; iy++) {
                for (int ix = 0; ix < grid.width; ix++) {
                    builder.append(grid.getBlock(ix, iy).toString());
                }
                builder.append(",");
            }
            gridObj.put("blocks", builder.toString());
            data.put(key, gridObj);

            return this;
        }

        public Saver saveLogic(Logic.Score score, Logic.Speed speed, String key) throws JSONException {
            JSONObject logicObj = new JSONObject();
            logicObj.put("score", score.getTotalScore());
            logicObj.put("speedSteps", speed.getStepTotal());
            data.put(key, logicObj);

            return this;
        }

        public void commit(String fileName) {
            FileHandle handle = Gdx.files.local(fileName);
            handle.writeString(data.toString(), false);
        }
    }

    public static class Loader {
        private JSONObject data;

        public Loader(String fileName) throws JSONException {
            FileHandle handle = Gdx.files.local(fileName);
            data = new JSONObject(handle.readString());
        }

        public Piece<ColoredBlock> getPiece(String key) throws JSONException {
            JSONObject pieceObj = data.getJSONObject(key);
            String blocks = pieceObj.getString("blocks");
            int x = pieceObj.getInt("x");
            int y = pieceObj.getInt("y");
            float speed = (float)pieceObj.getDouble("speed");
            Piece<ColoredBlock> piece = new Piece<>(2, new ColoredBlock(ColoredBlock.Type.NONE));
            piece.setPosition(x, y);
            piece.setBlock(ColoredBlock.fromString(Character.toString(blocks.charAt(0))), 0, 0);
            piece.setBlock(ColoredBlock.fromString(Character.toString(blocks.charAt(1))), 1, 0);
            piece.setBlock(ColoredBlock.fromString(Character.toString(blocks.charAt(2))), 0, 1);
            piece.setBlock(ColoredBlock.fromString(Character.toString(blocks.charAt(3))), 1, 1);

            return piece;
        }

        public void getGridBlocks(String key, Grid<ColoredBlock> outGrid) throws JSONException {
            JSONObject gridObj = data.getJSONObject(key);
            String[] rows = gridObj.getString("blocks").split(",");
            for (int iy = 0; iy < outGrid.height; iy++) {
                for (int ix = 0; ix < outGrid.width; ix++) {
                    outGrid.setBlock(ColoredBlock.fromString(rows[iy].substring(ix, ix + 1)), ix, iy);
                }
            }
        }

        public void getScore(String key, Logic.Score outScore) throws JSONException {
            JSONObject logicObj = data.getJSONObject(key);
            int score = logicObj.getInt("score");
            outScore.setTotalScore(score);
        }

        public void getSpeed(String key, Logic.Speed outSpeed) throws JSONException {
            JSONObject logicObj = data.getJSONObject(key);
            int speedSteps = logicObj.getInt("speedSteps");
            outSpeed.gotoStep(speedSteps);
        }
    }

    public class PersistenceException extends Exception {
        public PersistenceException(String message) {
            super(message);
        }
        public PersistenceException(Throwable cause) {
            super(cause);
        }
    }
}
