package com.matchblock.matchgame;

import com.badlogic.gdx.math.Vector2;
import com.matchblock.engine.CellRef;
import com.matchblock.engine.Grid;
import com.matchblock.engine.Point;
import com.matchblock.engine.State;
import com.matchblock.engine.TimeInterval;

import java.util.List;

public class GameRunner {
    public abstract static class EventHandler {
        void onActivePieceSpawned(Piece activePiece) {};
        void onNextPieceSpawned(Piece nextPiece) {};
        void onActivePieceRemoved() {};
        void onActivePieceMove(Piece activePiece, int dx, int dy, float speedMul) {};
        void onBlockClear(CellRef ref) {};
        void onBlockMove(CellRef fromRef, CellRef toRef, float speedMul) {};
    }

    public enum GameState {
        DROPPING,
        FAST_DROP,
        SCORING,
        REMOVING_SHIFT,
        REMOVING_CLEAR
    };

    private State.Context stateContext;
    private State.TransitionHandler transitionHandler;

    private EventHandler eventHandler;

    public Grid grid;
    public Piece activePiece;
    public Piece nextPiece;

    public Logic.Score scoreLogic;
    public Logic.Speed speedLogic;
    private int chainCount;

    private class DroppingState extends State {
        private TimeInterval dropDelay;

        @Override
        public void enter(Object previous) {
            dropDelay = new TimeInterval(speedLogic.getCurrentSpeed());
        }

        @Override
        public void update(float delta) {
            dropDelay.step(delta);
            if (dropDelay.ready()) {
                if (activePiece.collidesDown(grid)) {
                    grid.joinGrid(activePiece, activePiece.gridX, activePiece.gridY);
                    eventHandler.onActivePieceRemoved();
                    stateContext.set(GameState.SCORING, transitionHandler);
                }
                else {
                    activePiece.drop(1);
                    eventHandler.onActivePieceMove(activePiece, 0, 1, 8.0f);
                    stateContext.set(GameState.DROPPING, transitionHandler);
                }
            }
        }
    }

    private class FastDropState extends State {
        @Override
        public void enter(Object previous) {
            grid.joinGrid(activePiece, activePiece.gridX, activePiece.gridY);
            eventHandler.onActivePieceRemoved();
        }

        @Override
        public void update(float delta) {
            for (int ix = 0; ix < grid.width; ix++) {
                Vector2[] shiftVectors = grid.getColumnShiftVectors(ix);
                for (int iy = shiftVectors.length - 1; iy >= 0; iy--) {
                    if (shiftVectors[iy].y == 0)
                        continue;

                    CellRef fromRef = new CellRef(grid, ix, iy);
                    CellRef toRef = new CellRef(grid, ix + (int)shiftVectors[ix].x, iy + (int)shiftVectors[iy].y);
                    eventHandler.onBlockMove(fromRef, toRef, 1.0f);
                }
            }
            stateContext.set(GameState.SCORING, transitionHandler);
        }
    }

    private class ScoringState extends State {
        @Override
        public void enter(Object previous) {
            chainCount = 1;
        }

        @Override
        public void update(float delta) {
            stateContext.set(GameState.REMOVING_SHIFT, transitionHandler);
        }
    }

    private class ClearMatchesState extends State {
        private TimeInterval clearDelay;

        @Override
        public void enter(Object previous) {
            clearDelay = new TimeInterval(0.3f);
        }

        @Override
        public void update(float delta) {
            clearDelay.step(delta);
            if (clearDelay.ready()) {
                List<List<Point>> groups = MatchRemover.getTypeGroups(grid, 4);
                if (groups.size() > 0) {
                    for (List<Point> group : groups) {
                        for (Point point : group) {
                            CellRef ref = new CellRef(grid, point.x, point.y);
                            eventHandler.onBlockClear(ref);
                        }
                    }
                    scoreLogic.scoreGroups(groups, chainCount);
                    speedLogic.step();
                    chainCount++;
                    stateContext.set(GameState.REMOVING_SHIFT, transitionHandler);
                }
                else {
                    generateGroups();
                    stateContext.set(GameState.DROPPING, transitionHandler);
                }
            }
        }
    }

    private class ShiftBlocksState extends State {
        private TimeInterval shiftDelay;

        @Override
        public void enter(Object previous) {
            shiftDelay = new TimeInterval(0.3f);
            if (previous == GameState.SCORING) {
                shiftDelay.resetToReady();
            }
        }

        @Override
        public void update(float delta) {
            shiftDelay.step(delta);
            if (shiftDelay.ready()) {
                for (int ix = 0; ix < grid.width; ix++) {
                    Vector2[] shiftVectors = grid.getColumnShiftVectors(ix);
                    for (int iy = shiftVectors.length - 1; iy >= 0; iy--) {
                        if (shiftVectors[iy].y == 0)
                            continue;

                        CellRef fromRef = new CellRef(grid, ix, iy);
                        CellRef toRef = new CellRef(grid, ix + (int) shiftVectors[ix].x, iy + (int) shiftVectors[iy].y);
                        eventHandler.onBlockMove(fromRef, toRef, 4.0f);
                    }
                }
                stateContext.set(GameState.REMOVING_CLEAR, transitionHandler);
            }
        }
    }

    public GameRunner(Grid grid, Logic.Score scoreLogic, Logic.Speed speedLogic) {
        this.grid = grid;
        this.scoreLogic = scoreLogic;
        this.speedLogic = speedLogic;
        this.eventHandler = new EventHandler() {};

        this.stateContext = new State.Context();
        this.stateContext.add(GameState.DROPPING, new DroppingState());
        this.stateContext.add(GameState.REMOVING_CLEAR, new ClearMatchesState());
        this.stateContext.add(GameState.REMOVING_SHIFT, new ShiftBlocksState());
        this.stateContext.add(GameState.FAST_DROP, new FastDropState());
        this.stateContext.add(GameState.SCORING, new ScoringState());
    }

    public void update(float delta) {
        stateContext.update(delta);
    }

    public void setEventHandler(EventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    public void setTransitionHandler(State.TransitionHandler transitionHandler) {
        this.transitionHandler = transitionHandler;
    }

    public void startDrop() {
        generateGroups();
        stateContext.set(GameState.DROPPING);
    }

    public void continueDrop(Piece activePiece, Piece nextPiece) {
        this.activePiece = activePiece;
        this.nextPiece = nextPiece;
        stateContext.set(GameState.DROPPING);
    }

    public boolean isGameOver() {
        return grid.getMaxColumnHeight() >= grid.height;
    }

    private void generateGroups() {
        Piece.Generator gen = new Piece.Generator(0.1f, 0.35f, 0.35f, 0.2f);

        if (nextPiece == null) {
            activePiece = gen.generate();
        } else {
            activePiece = new Piece(2);
            activePiece.joinGrid(nextPiece, 0, 0);
        }
        activePiece.setPosition(4, 0);

        nextPiece = gen.generate();

        eventHandler.onActivePieceSpawned(activePiece);
        eventHandler.onNextPieceSpawned(nextPiece);
    }

    public void pivotLeft() {
        if (stateContext.get() != GameState.DROPPING)
            return;

        if (!activePiece.collidesLeft(grid)) {
            activePiece.pivot(-1);
            eventHandler.onActivePieceMove(activePiece, -1, 0, 2.0f);
        }
    }

    public void pivotRight() {
        if (stateContext.get() != GameState.DROPPING)
            return;

        if (!activePiece.collidesRight(grid)) {
            activePiece.pivot(1);
            eventHandler.onActivePieceMove(activePiece, 1, 0, 2.0f);
        }
    }

    public void rotate() {
        if (stateContext.get() != GameState.DROPPING)
            return;

        activePiece.rotateRight();
    }

    public void dropFast() {
        if (stateContext.get() != GameState.DROPPING)
            return;

        stateContext.set(GameState.FAST_DROP);
    }
}
