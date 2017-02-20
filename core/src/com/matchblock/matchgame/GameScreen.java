package com.matchblock.matchgame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
import com.matchblock.engine.*;
import com.matchblock.engine.state.Transition;
import com.matchblock.ui.MenuScreen;
import com.matchblock.ui.GameOverGroup;

import java.util.HashMap;
import java.util.Map;

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
        Grid<ColoredBlock> grid = new Grid<>(10, 15, new ColoredBlock(ColoredBlock.Type.NONE));

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

        GameRunner gameRunner;

        try {
            Grid<ColoredBlock> grid = new Grid<>(10, 15, new ColoredBlock(ColoredBlock.Type.NONE));

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
            Piece<ColoredBlock> activePiece = loader.getPiece("activePiece");
            Piece<ColoredBlock> nextPiece = loader.getPiece("nextPiece");

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
        private Map<ColoredBlock.Type, TextureRegion> blockTextureMap;

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

            this.blockTextureMap = new HashMap<>();
            blockTextureMap.put(ColoredBlock.Type.RED, new TextureRegion(new Texture(Gdx.files.internal("element_red_square.png"))));
            blockTextureMap.put(ColoredBlock.Type.BLUE, new TextureRegion(new Texture(Gdx.files.internal("element_blue_square.png"))));
            blockTextureMap.put(ColoredBlock.Type.GREEN, new TextureRegion(new Texture(Gdx.files.internal("element_green_square.png"))));
            blockTextureMap.put(ColoredBlock.Type.ORANGE, new TextureRegion(new Texture(Gdx.files.internal("element_yellow_square.png"))));
            blockTextureMap.put(ColoredBlock.Type.PURPLE, new TextureRegion(new Texture(Gdx.files.internal("element_purple_square.png"))));
            blockTextureMap.put(ColoredBlock.Type.MAGENTA, new TextureRegion(new Texture(Gdx.files.internal("element_grey_square.png"))));
        }

        @Override
        public void act(float delta) {
            super.act(delta);
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            //batch.end();
            //Gdx.gl.glEnable(GL20.GL_BLEND);
            //Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            if (gridActor != null) {
                drawGridBackground(batch, parentAlpha);
            }
            super.draw(batch, parentAlpha);

            //Gdx.gl.glDisable(GL20.GL_BLEND);
            //batch.begin();
        }

        public void setGameRunner(GameRunner gameRunner) {
            this.gameRunner = gameRunner;

            this.gameRunner.setTransitionHandler(new Transition.Handler() {
                @Override
                public void update(Transition transition) {
                    if (!actionManager.hasBlockingActions()) {
                        transition.proceed();
                    }
                }
            });

            this.gameRunner.setEventHandler(new GameRunner.EventHandler() {
                public void onActivePieceSpawned(Piece<ColoredBlock> activePiece) {
                    setActivePiece(activePiece);
                }

                public void onNextPieceSpawned(Piece<ColoredBlock> nextPiece) {
                    setNextPiece(nextPiece);
                }

                public void onActivePieceMove(Piece<ColoredBlock> activePiece, int dx, int dy, float speedMul) {
                    float cellWidth = gridActor.getCellWidth();
                    float cellHeight = gridActor.getCellHeight();

                    float duration = Vector2.len(dx, dy) * 0.025f * speedMul;

                    Action moveAction = Actions.moveBy(dx * cellWidth, -dy * cellHeight, duration);
                    moveAction.setActor(activePieceActor);
                    activePieceActor.addAction(moveAction);
                }

                public void onActivePieceRemoved() {
                    activePieceActor.clear();
                    removeActor(activePieceActor);
                }

                public void onActivePieceRotate() {
                    activePieceActor.rotateBy(90f);
                    Action rotateAction = Actions.rotateBy(-90f, 0.15f);
                    rotateAction.setActor(activePieceActor);
                    activePieceActor.addAction(rotateAction);
                }

                public void onBlockClear(final CellRef<ColoredBlock> ref) {
                    final BlockActor blockActor = gridActor.popOut(ref.x(), ref.y());

                    SequenceAction clearAction = Actions.sequence(
                            Actions.alpha(0.0f, 0.15f),
                            Actions.run(new Runnable() {
                                @Override
                                public void run() {
                                    blockActor.remove();
                                }
                            })
                    );
                    clearAction.setActor(blockActor);

                    actionManager.addBlockingAction(clearAction);
                }

                public void onBlockMove(final CellRef<ColoredBlock> fromRef, final CellRef<ColoredBlock> toRef, float speedMul) {
                    final int x0 = fromRef.x();
                    final int y0 = fromRef.y();
                    final int x1 = toRef.x();
                    final int y1 = toRef.y();
                    final BlockActor blockActor = gridActor.popOut(x0, y0);

                    float duration = Vector2.dst(x0, y0, x1, y1) * 0.025f * speedMul;
                    SequenceAction dropAction = Actions.sequence(
                            Actions.moveTo(gridActor.toRelativeX(x1), gridActor.toRelativeY(y1), duration),
                            Actions.run(new Runnable() {
                                @Override
                                public void run() {
                                    gridActor.popIn(blockActor, x1, y1);
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

        private void setGrid(Grid<ColoredBlock> grid) {
            if (gridActor != null) {
                gridActor.clear();
                this.removeActor(gridActor);
            }

            gridActor = new GridActor(grid, blockTextureMap);
            gridActor.setPosition(20, 20);
            gridActor.setSize(getWidth() - 40, (float)((getWidth() - 40) * 1.5));
            this.addActor(gridActor);
        }

        private void setActivePiece(Piece<ColoredBlock> piece) {
            if (activePieceActor != null) {
                activePieceActor.clear();
                this.removeActor(activePieceActor);
            }

            if (piece == null) {
                return;
            }

            float cellWidth = gridActor.getCellWidth();
            float cellHeight = gridActor.getCellHeight();

            activePieceActor = new PieceActor(piece, blockTextureMap);
            activePieceActor.setSize(cellWidth * 2, cellHeight * 2);
            activePieceActor.setOrigin(Align.center);
            float x = gridActor.getX() + activePieceActor.piece.gridX * cellWidth;
            float y = (gridActor.getY() + gridActor.getHeight()) - activePieceActor.getHeight() - (activePieceActor.piece.gridY * cellHeight);
            activePieceActor.setPosition(x, y);
            this.addActor(activePieceActor);
            activePieceActor.toFront();
        }

        private void setNextPiece(Piece<ColoredBlock> piece) {
            if (nextPieceActor != null) {
                nextPieceActor.clear();
                this.removeActor(nextPieceActor);
            }

            if (piece == null) {
                return;
            }

            nextPieceActor = new PieceActor(piece, blockTextureMap);
            nextPieceActor.setSize(80, 80);
            nextPieceActor.setPosition(this.getWidth() - nextPieceActor.getWidth() - 20, this.getHeight() - nextPieceActor.getHeight() - 15);
            this.addActor(nextPieceActor);
            nextPieceActor.toFront();
        }

        private void drawGridBackground (Batch batch, float parentAlpha) {
            batch.end();

            float worldX = gridActor.getX();
            float worldY = gridActor.getY();
            float worldWidth = gridActor.getWidth();
            float worldHeight = gridActor.getHeight();
            float cellWidth = gridActor.getCellWidth();
            float cellHeight = gridActor.getCellHeight();

            //background
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(Color.BLACK);
            shapeRenderer.rect(worldX, worldY, worldWidth, worldHeight);
            shapeRenderer.end();
            //top row
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(Color.GRAY);
            shapeRenderer.rect(worldX, worldY + worldHeight - cellHeight, worldWidth, cellHeight);
            shapeRenderer.end();
            //border
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.WHITE);
            shapeRenderer.rect(worldX, worldY, worldWidth, worldHeight);
            shapeRenderer.end();

            batch.begin();
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
