package com.matchblock.matchgame;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.matchblock.engine.*;
import com.matchblock.engine.state.State;
import com.matchblock.engine.state.StateMachine;
import com.matchblock.engine.state.Transition;

import java.util.List;

public class GameRunner {
    public interface EventHandler {
        void onActivePieceSpawned(Piece<ColoredBlock> activePiece);
        void onNextPieceSpawned(Piece<ColoredBlock> nextPiece);
        void onActivePieceRemoved();
        void onActivePieceMove(Piece<ColoredBlock> activePiece, int dx, int dy, float speedMul);
        void onActivePieceRotate();
        void onBlockClear(CellRef<ColoredBlock> ref);
        void onBlockMove(CellRef<ColoredBlock> fromRef, CellRef<ColoredBlock> toRef, float speedMul);
    }

    public static class DefaultEventHandler implements EventHandler {
        public void onActivePieceSpawned(Piece<ColoredBlock> activePiece) {}
        public void onNextPieceSpawned(Piece<ColoredBlock> nextPiece) {}
        public void onActivePieceRemoved() {}
        public void onActivePieceMove(Piece<ColoredBlock> activePiece, int dx, int dy, float speedMul) {}
        public void onActivePieceRotate() {}
        public void onBlockClear(CellRef<ColoredBlock> ref) {
            ref.clearTarget();
        }
        public void onBlockMove(CellRef<ColoredBlock> fromRef, CellRef<ColoredBlock> toRef, float speedMul) {
            fromRef.moveTo(toRef.x(), toRef.y());
        }
    }

    public enum GameState {
        DROPPING,
        FAST_DROP,
        SCORING,
        REMOVING_SHIFT,
        REMOVING_CLEAR
    };

    private StateMachine stateContext;
    private Transition.Handler transitionHandler;

    private EventHandler eventHandler;

    public Grid<ColoredBlock> grid;
    public Piece<ColoredBlock> activePiece;
    public Piece<ColoredBlock> nextPiece;

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

                    CellRef<ColoredBlock> fromRef = new CellRef<>(grid, ix, iy);
                    CellRef<ColoredBlock> toRef = new CellRef<>(grid, ix + (int)shiftVectors[ix].x, iy + (int)shiftVectors[iy].y);
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
                List<List<GridPoint2>> groups = MatchRemover.getTypeGroups(grid, 4);
                if (groups.size() > 0) {
                    for (List<GridPoint2> group : groups) {
                        for (GridPoint2 point : group) {
                            CellRef<ColoredBlock> ref = new CellRef<>(grid, point.x, point.y);
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

                        CellRef<ColoredBlock> fromRef = new CellRef<>(grid, ix, iy);
                        CellRef<ColoredBlock> toRef = new CellRef<>(grid, ix + (int) shiftVectors[ix].x, iy + (int) shiftVectors[iy].y);
                        eventHandler.onBlockMove(fromRef, toRef, 4.0f);
                    }
                }
                stateContext.set(GameState.REMOVING_CLEAR, transitionHandler);
            }
        }
    }

    public GameRunner(Grid<ColoredBlock> grid, Logic.Score scoreLogic, Logic.Speed speedLogic) {
        this.grid = grid;
        this.scoreLogic = scoreLogic;
        this.speedLogic = speedLogic;
        this.eventHandler = new DefaultEventHandler();
        this.transitionHandler = new Transition.ImmediateHandler();

        this.stateContext = new StateMachine();
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

    public void setTransitionHandler(Transition.Handler transitionHandler) {
        this.transitionHandler = transitionHandler;
    }

    public void startDrop() {
        generateGroups();
        stateContext.set(GameState.DROPPING);
    }

    public void continueDrop(Piece<ColoredBlock> activePiece, Piece<ColoredBlock> nextPiece) {
        this.activePiece = activePiece;
        this.nextPiece = nextPiece;
        stateContext.set(GameState.DROPPING);
    }

    public boolean isGameOver() {
        return grid.getMaxColumnHeight() >= grid.height;
    }

    private void generateGroups() {
        PieceGenerator gen = new PieceGenerator(0.1f, 0.35f, 0.25f, 0.3f);

        if (nextPiece == null) {
            activePiece = gen.generate();
        } else {
            activePiece = new Piece<ColoredBlock>(2, new ColoredBlock(ColoredBlock.Type.NONE));
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
        eventHandler.onActivePieceRotate();
    }

    public void dropFast() {
        if (stateContext.get() != GameState.DROPPING)
            return;

        stateContext.set(GameState.FAST_DROP);
    }
}
