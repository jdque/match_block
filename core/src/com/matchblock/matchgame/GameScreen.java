package com.matchblock.matchgame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.matchblock.actors.BlockActor;
import com.matchblock.actors.PieceActor;
import com.matchblock.actors.GridActor;
import com.matchblock.engine.CellRef;
import com.matchblock.engine.ColoredBlock;
import com.matchblock.engine.Grid;
import com.matchblock.engine.State;
import com.matchblock.ui.MenuScreen;
import com.matchblock.ui.GameOverGroup;

public class GameScreen implements Screen {
    public enum RunState {
        RUNNING,
        PAUSED
    };

    public static final int SCREEN_WIDTH = 480, SCREEN_HEIGHT = 800;
    public static final String SAVE_FILE_NAME = "save.dat";

    private Game game;
    private RunState runState;
    private GameRunner runner;
    private Stage mainStage;
    private GameLayer gameLayer;
    private TouchInputLayer inputLayer;
    private HudLayer hudLayer;

    public GameScreen(Game game, boolean tryResume) {
        this.game = game;

        mainStage = new Stage(new ScreenViewport());

        gameLayer = new GameLayer(mainStage);
        hudLayer = new HudLayer(mainStage);
        inputLayer = new TouchInputLayer(mainStage) {
            public void onTapLeft() { runner.pivotLeft(); }
            public void onTapRight() { runner.pivotRight(); }
            public void onTapMiddle() { runner.rotate(); }
            public void onSwipeDown() { runner.dropFast(); }
        };

        mainStage.addActor(gameLayer);
        mainStage.addActor(hudLayer);
        mainStage.addActor(inputLayer);
        Gdx.input.setInputProcessor(mainStage);

        startGame(tryResume);
    }

    @Override
    public void show() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void pause() {
        saveGame();
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        mainStage.dispose();
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void render(float delta) {
        //Update
        if (runState == RunState.RUNNING) {
            handleInput();
            runner.update(delta);
            if (runner.isGameOver()) {
                endGame();
            }
        }

        //Render
        Gdx.gl.glClearColor(0.25f, 0.25f, 0.25f, 0.5f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        updateHud();

        mainStage.act(delta);
        mainStage.draw();
    }

    private void exitToMenu() {
        game.setScreen(new MenuScreen(game));
    }

    private void startGame(boolean tryResume) {
        if (tryResume) {
            runner = tryLoadGame();
            if (runner == null) {
                runner = createNewGame();
            }
        }
        else {
            runner = createNewGame();
        }

        gameLayer.setGameRunner(runner);

        inputLayer.setTouchable(Touchable.enabled);
        runState = RunState.RUNNING;
    }

    private void endGame() {
        runState = RunState.PAUSED;
        deleteSavedGame();

        inputLayer.setTouchable(Touchable.disabled);

        int score = runner.scoreLogic.getTotalScore();
        Preferences prefs = Gdx.app.getPreferences("MatchBlock");
        int bestScore = prefs.getInteger("best_score", 0);
        if (score > bestScore) {
            prefs.putInteger("best_score", score);
            prefs.flush();
            bestScore = score;
        }

        GameOverGroup gameOverGroup = new GameOverGroup(mainStage) {
            public void onBack() {
                exitToMenu();
            }

            public void onRestart() {
                this.remove();
                startGame(false);
            }
        };
        gameOverGroup.setScore(String.format("%010d", score));
        gameOverGroup.setBestScore(String.format("%010d", bestScore));
        mainStage.addActor(gameOverGroup);
    }

    private GameRunner createNewGame() {
        Grid grid = new Grid(10, 15);

        Logic.Score scoreLogic = new Logic.Score();
        scoreLogic.configure(4, 10, 5, 20, 2.0f);

        Logic.Speed speedLogic = new Logic.Speed();
        speedLogic.addSpeed(1.00f, 0);
        speedLogic.addSpeed(0.75f, 5);
        speedLogic.addSpeed(0.50f, 15);
        speedLogic.addSpeed(0.25f, 35);

        GameRunner gameRunner = new GameRunner(grid, scoreLogic, speedLogic);
        gameRunner.startDrop();

        return gameRunner;
    }

    private GameRunner tryLoadGame() {
        if (!Gdx.files.local(SAVE_FILE_NAME).exists())
            return null;

        GameRunner gameRunner = null;

        try {
            Grid grid = new Grid(10, 15);
            Logic.Score scoreLogic = new Logic.Score();
            scoreLogic.configure(4, 10, 5, 20, 2.0f);
            Logic.Speed speedLogic = new Logic.Speed();
            speedLogic.addSpeed(1.00f, 0);
            speedLogic.addSpeed(0.75f, 5);
            speedLogic.addSpeed(0.50f, 15);
            speedLogic.addSpeed(0.25f, 35);

            GamePersistence.Loader loader = new GamePersistence.Loader(SAVE_FILE_NAME);
            loader.getScore("logic", scoreLogic);
            loader.getSpeed("logic", speedLogic);
            loader.getGridBlocks("grid", grid);
            Piece activePiece = loader.getPiece("activePiece");
            Piece nextPiece = loader.getPiece("nextPiece");

            gameRunner = new GameRunner(grid, scoreLogic, speedLogic);
            gameRunner.continueDrop(activePiece, nextPiece);
        }
        catch (Exception e) {
            return null;
        }

        return gameRunner;
    }

    private void saveGame() {
        GamePersistence.Saver saver = new GamePersistence.Saver();
        try {
            saver.saveLogic(runner.scoreLogic, runner.speedLogic, "logic")
                .savePiece(runner.activePiece, "activePiece")
                .savePiece(runner.nextPiece, "nextPiece")
                .saveGrid(runner.grid, "grid")
                .commit(SAVE_FILE_NAME);
        }
        catch (Exception e) {
        }
    }

    private void deleteSavedGame() {
        Gdx.files.local(SAVE_FILE_NAME).delete();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            runner.pivotLeft();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            runner.pivotRight();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            runner.rotate();
            saveGame();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            runner.dropFast();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            saveGame();
            exitToMenu();
        }
    }

    private void updateHud() {
        Logic.Score scoreLogic = runner.scoreLogic;
        String score = String.format("%010d", scoreLogic.getTotalScore());
        hudLayer.setTotalScore(score);
    }

    private static class GameLayer extends Group {
        private ShapeRenderer shapeRenderer;
        private GameRunner gameRunner;
        private GridActor gridActor;
        private PieceActor activePieceActor;
        private PieceActor nextPieceActor;
        private ActionManager actionManager;

        private static class ActionManager {
            private Actor actor;

            public ActionManager(Group parent) {
                this.actor = new Actor();
                parent.addActor(this.actor);
            }

            public void addBlockingAction(Action action) {
                actor.addAction(action);
            }

            public boolean hasBlockingActions() {
                return actor.hasActions();
            }
        }

        public GameLayer(Stage stage) {
            super();
            this.setBounds(0, 0, stage.getWidth(), stage.getHeight());
            this.setTouchable(Touchable.disabled);

            this.shapeRenderer = new ShapeRenderer();
            this.actionManager = new ActionManager(this);
        }

        @Override
        public void act(float delta) {
            super.act(delta);
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            batch.end();
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            if (gridActor != null) {
                drawGridBackground();
            }
            super.draw(batch, parentAlpha);

            Gdx.gl.glDisable(GL20.GL_BLEND);
            batch.begin();
        }

        public void setGameRunner(GameRunner gameRunner) {
            this.gameRunner = gameRunner;

            this.gameRunner.setTransitionHandler(new State.TransitionHandler() {
                @Override
                public void update(State.Transition transition) {
                    if (!actionManager.hasBlockingActions()) {
                        transition.proceed();
                    }
                }
            });

            this.gameRunner.setEventHandler(new GameRunner.EventHandler() {
                void onActivePieceSpawned(Piece activePiece) {
                    setActivePiece(activePiece);
                }

                void onNextPieceSpawned(Piece nextPiece) {
                    setNextPiece(nextPiece);
                }

                void onActivePieceMove(Piece activePiece, int dx, int dy, float speedMul) {
                    float cellWidth = gridActor.getCellWidth();
                    float cellHeight = gridActor.getCellHeight();

                    float duration = Vector2.len(dx, dy) * 0.025f * speedMul;

                    SequenceAction moveAction = Actions.sequence(
                            Actions.moveBy(dx * cellWidth, -dy * cellHeight, duration),
                            Actions.run(new Runnable() {
                                public void run() {
                                }
                            })
                    );
                    moveAction.setActor(activePieceActor);

                    actionManager.addBlockingAction(moveAction);
                }

                void onActivePieceRemoved() {
                    activePieceActor.clear();
                    removeActor(activePieceActor);
                }

                void onBlockClear(final CellRef ref) {
                    float cellWidth = gridActor.getCellWidth();
                    float cellHeight = gridActor.getCellHeight();

                    final BlockActor blockActor = new BlockActor(ref.getTarget(), shapeRenderer);
                    blockActor.setSize(cellWidth, cellHeight);
                    blockActor.setPosition(gridActor.toStageX(ref.x()), gridActor.toStageY(ref.y()));
                    blockActor.setColor(ColoredBlock.getBlockColor(ref.getTarget()));
                    addActor(blockActor);

                    ref.clearTarget();

                    SequenceAction clearAction = Actions.sequence(
                            Actions.alpha(0.0f, 0.15f),
                            Actions.run(new Runnable() {
                                @Override
                                public void run() {
                                    removeActor(blockActor);
                                }
                            })
                    );
                    clearAction.setActor(blockActor);

                    actionManager.addBlockingAction(clearAction);
                }

                void onBlockMove(final CellRef fromRef, final CellRef toRef, float speedMul) {
                    float cellWidth = gridActor.getCellWidth();
                    float cellHeight = gridActor.getCellHeight();

                    final int x0 = fromRef.x();
                    final int y0 = fromRef.y();
                    final int x1 = toRef.x();
                    final int y1 = toRef.y();
                    final BlockActor blockActor = new BlockActor(fromRef.getTarget(), shapeRenderer);
                    blockActor.setSize(cellWidth, cellHeight);
                    blockActor.setPosition(gridActor.toStageX(x0), gridActor.toStageY(y0));
                    blockActor.setColor(ColoredBlock.getBlockColor(fromRef.getTarget()));
                    addActor(blockActor);

                    fromRef.clearTarget();

                    float duration = Vector2.dst(x0, y0, x1, y1) * 0.025f * speedMul;

                    SequenceAction dropAction = Actions.sequence(
                            Actions.moveTo(gridActor.toStageX(x1), gridActor.toStageY(y1), duration),
                            Actions.run(new Runnable() {
                                @Override
                                public void run() {
                                    removeActor(blockActor);
                                    toRef.setTarget(blockActor.block);
                                }
                            })
                    );
                    dropAction.setActor(blockActor);

                    actionManager.addBlockingAction(dropAction);
                }
            });

            setGrid(this.gameRunner.grid);
            setActivePiece(this.gameRunner.activePiece);
            setNextPiece(this.gameRunner.nextPiece);
        }

        private void setGrid(Grid grid) {
            if (gridActor != null) {
                gridActor.clear();
                this.removeActor(gridActor);
            }

            gridActor = new GridActor(grid, shapeRenderer);
            gridActor.setPosition(20, 20);
            gridActor.setSize(getWidth() - 40, (float)((getWidth() - 40) * 1.5));
            this.addActor(gridActor);
        }

        private void setActivePiece(Piece piece) {
            if (activePieceActor != null) {
                activePieceActor.clear();
                this.removeActor(activePieceActor);
            }

            if (piece == null) {
                return;
            }

            float cellWidth = gridActor.getCellWidth();
            float cellHeight = gridActor.getCellHeight();

            activePieceActor = new PieceActor(piece, shapeRenderer);
            activePieceActor.setSize(cellWidth * 2, cellHeight * 2);
            float x = gridActor.getX() + activePieceActor.piece.gridX * cellWidth;
            float y = (gridActor.getY() + gridActor.getHeight()) - activePieceActor.getHeight() - (activePieceActor.piece.gridY * cellHeight);
            activePieceActor.setPosition(x, y);
            this.addActor(activePieceActor);
            activePieceActor.toFront();
        }

        private void setNextPiece(Piece piece) {
            if (nextPieceActor != null) {
                nextPieceActor.clear();
                this.removeActor(nextPieceActor);
            }

            if (piece == null) {
                return;
            }

            nextPieceActor = new PieceActor(piece, shapeRenderer);
            nextPieceActor.setSize(80, 80);
            nextPieceActor.setPosition(this.getWidth() - nextPieceActor.getWidth() - 20, this.getHeight() - nextPieceActor.getHeight() - 15);
            this.addActor(nextPieceActor);
            nextPieceActor.toFront();
        }

        private void drawGridBackground () {
            float worldX = gridActor.getX();
            float worldY = gridActor.getY();
            float worldWidth = gridActor.getWidth();
            float worldHeight = gridActor.getHeight();
            float cellWidth = gridActor.getCellWidth();
            float cellHeight = gridActor.getCellHeight();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(Color.BLACK);
            shapeRenderer.rect(worldX, worldY, worldWidth, worldHeight);
            shapeRenderer.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(Color.GRAY);
            shapeRenderer.rect(worldX, worldY + worldHeight - cellHeight, worldWidth, cellHeight);
            shapeRenderer.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.WHITE);
            shapeRenderer.rect(worldX, worldY, worldWidth, worldHeight);
            shapeRenderer.end();
        }
    }

    private abstract static class TouchInputLayer extends Group {
        public TouchInputLayer(Stage stage) {
            super();
            this.setBounds(0, 0, stage.getWidth(), stage.getHeight());

            Actor gridActions = new Actor();
            gridActions.setBounds(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            gridActions.addListener(new ActorGestureListener() {
                private boolean active = false;
                private boolean canTap = false;
                private boolean canDrop = false;
                private float sx, sy;

                public void touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    active = true;
                    canTap = true;
                    sx = x;
                    sy = y;
                }

                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    if (!active)
                        return;

                    if (canTap) {
                        if (x < Gdx.graphics.getWidth() / 3) {
                            onTapLeft();
                        }
                        else if (x > Gdx.graphics.getWidth()*  2 / 3) {
                            onTapRight();
                        }
                        else {
                            onTapMiddle();
                        }
                        canTap = false;
                    }
                    active = false;
                }

                public void pan(InputEvent event, float x, float y, float deltaX, float deltaY) {
                    if (!active)
                        return;

                    float yDiff = y - sy;
                    if (Math.abs(yDiff) > 64) {
                        canTap = false;
                        if (yDiff < -Gdx.graphics.getHeight() / 6) {
                            onSwipeDown();
                            active = false;
                        }
                    }
                }
            });
            this.addActor(gridActions);
        }

        public abstract void onTapLeft();

        public abstract void onTapRight();

        public abstract void onTapMiddle();

        public abstract void onSwipeDown();
    }

    private static class HudLayer extends Group {
        private Label scoreValueLabel;

        @Override
        public void act(float delta) {
            super.act(delta);
        }

        public HudLayer(Stage stage) {
            super();
            this.setBounds(0, 0, stage.getWidth(), stage.getHeight());

            Label.LabelStyle labelStyle = new Label.LabelStyle(new BitmapFont(), Color.WHITE);

            Table table = new Table();
            table.setFillParent(true);
            this.addActor(table);

            Label scoreLabel = new Label("SCORE", labelStyle);

            scoreValueLabel = new Label("0000000000", labelStyle);
            scoreValueLabel.setFontScale(1.0f);

            Label nextLabel = new Label("NEXT", labelStyle);

            table.align(Align.top | Align.left);
            table.padTop(this.getHeight() * 3 / 100)
                    .padBottom(this.getHeight() * 3 / 100)
                    .padLeft(this.getHeight() * 4 / 100)
                    .padRight(this.getHeight() * 4 / 100);
            table.add(scoreLabel).center().left().expandX();
            table.add(nextLabel).center().left().expandX().padLeft(this.getWidth() * 20 / 100);
            table.row();
            table.add(scoreValueLabel).center().left().spaceTop(this.getHeight() * 2 / 100);
        }

        public void setTotalScore(String score) {
            if (!score.equals(scoreValueLabel.getText().toString())) {
                scoreValueLabel.setText(score);
            }
        }
    }
}
