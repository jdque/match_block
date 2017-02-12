package com.matchblock.ui;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.matchblock.matchgame.GameScreen;

public class MenuScreen implements Screen {
    private Game game;
    private Stage stage;
    private ShapeRenderer shape;

    public MenuScreen(Game game) {
        this.game = game;

        Texture titleTexture = new Texture(Gdx.files.internal("title.png"));
        titleTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        Texture txrNewGame = new Texture(Gdx.files.internal("text_new.png"));
        txrNewGame.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        Texture txrResume = new Texture(Gdx.files.internal("text_resume.png"));
        txrResume.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        float scale = stage.getWidth() / 1080;

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.98f, 0.98f, 0.98f, 1.0f));
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        TextureRegionDrawable backgroundDrawable = new TextureRegionDrawable(new TextureRegion(texture));

        Table table = new Table();
        table.setBackground(backgroundDrawable);
        table.setFillParent(true);
        stage.addActor(table);

        Image title = new Image(titleTexture);

        Image resume = new Image(new Sprite(txrResume));
        resume.setColor(Color.BLACK);
        resume.addListener(new TextButtonSelectListener(resume) {
            public void select() {
                onResumeGame();
            }
        });

        Image newGame = new Image(new Sprite(txrNewGame));
        newGame.setColor(Color.BLACK);
        newGame.addListener(new TextButtonSelectListener(newGame) {
            public void select() {
                onNewGame();
            }
        });

        table.align(Align.center | Align.top);
        table.add(title).padTop(stage.getHeight() * 20 / 100).expandX().size(title.getWidth() * scale, title.getHeight() * scale).row();
        if (Gdx.files.local(GameScreen.SAVE_FILE_NAME).exists()) {
            table.add(resume).padTop(stage.getHeight() * 20 / 100).size(resume.getWidth() * scale, resume.getHeight() * scale).row();
            table.add(newGame).padTop(stage.getHeight() * 8 / 100).size(newGame.getWidth() * scale, newGame.getHeight() * scale).row();
        }
        else {
            table.add(newGame).padTop(stage.getHeight() * 24 / 100).size(newGame.getWidth() * scale, newGame.getHeight() * scale).row();
        }
    }

    @Override
    public void show() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void render(float delta) {
        stage.act(delta);
        stage.draw();
    }

    private void onNewGame() {
        game.setScreen(new GameScreen(game, false));
    }

    private void onResumeGame() {
        game.setScreen(new GameScreen(game, true));
    }

    private abstract static class TextButtonSelectListener extends ActorGestureListener {
        private Image target;

        public TextButtonSelectListener(Image image) {
            target = image;
        }

        public void touchDown(InputEvent event, float x, float y, int count, int button) {
            target.setColor(Color.BLUE);
        }

        public void touchUp(InputEvent event, float x, float y, int count, int button) {
            target.setColor(Color.BLACK);
        }

        public void tap(InputEvent event, float x, float y, int count, int button) {
            select();
        }

        public abstract void select();
    }
}
