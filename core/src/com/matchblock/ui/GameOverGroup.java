package com.matchblock.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

public abstract class GameOverGroup extends Group {
    private Label scoreValue;
    private Label bestScoreValue;

    public GameOverGroup(Stage stage) {
        super();
        this.setBounds(0, 0, stage.getWidth(), stage.getHeight());

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.25f, 0.25f, 0.25f, 0.75f));
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        TextureRegionDrawable greyBackground = new TextureRegionDrawable(new TextureRegion(texture));

        pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.0f, 0.0f, 0.75f, 1.0f));
        pixmap.fill();
        texture = new Texture(pixmap);
        pixmap.dispose();
        TextureRegionDrawable blueBackground = new TextureRegionDrawable(new TextureRegion(texture));

        Label.LabelStyle labelStyle = new Label.LabelStyle(new BitmapFont(), Color.WHITE);

        Container background = new Container();
        background.setBackground(greyBackground);
        background.setSize(this.getWidth(), this.getHeight());
        background.setPosition(0.0f, 0.0f);
        this.addActor(background);

        Table table = new Table();
        table.setBackground(blueBackground);
        table.setSize(this.getWidth() * 2 / 3, this.getHeight() * 1 / 3);
        table.setPosition(this.getWidth() / 2 - table.getWidth() / 2, this.getHeight() / 2 - table.getHeight() / 2);
        this.addActor(table);

        Label gameOver = new Label("Game Over", labelStyle);
        gameOver.setFontScale(3.0f);

        Label scoreLabel = new Label("Score:", labelStyle);
        scoreLabel.setFontScale(1.5f);

        scoreValue = new Label("0000000000", labelStyle);
        scoreValue.setFontScale(1.5f);

        Label bestScoreLabel = new Label("Best:", labelStyle);
        bestScoreLabel.setFontScale(1.5f);

        bestScoreValue = new Label("0000000000", labelStyle);
        bestScoreValue.setFontScale(1.5f);

        Label back = new Label("BACK", labelStyle);
        back.setFontScale(1.5f);
        back.addListener(new ActorGestureListener() {
            public void tap(InputEvent event, float x, float y, int count, int button) {
                onBack();
            }
        });

        Label restart = new Label("AGAIN", labelStyle);
        restart.setFontScale(1.5f);
        restart.addListener(new ActorGestureListener() {
            public void tap(InputEvent event, float x, float y, int count, int button) {
                onRestart();
            }
        });

        table.align(Align.top | Align.center);
        table.add(gameOver).padTop(this.getHeight() * 4 / 100).expandX().colspan(2);
        table.row();
        table.add(scoreLabel).padTop(this.getHeight() * 5 / 100).padLeft(this.getHeight() * 3 / 100).align(Align.right);
        table.add(scoreValue).padTop(this.getHeight() * 5 / 100).padRight(this.getHeight() * 3 / 100).align(Align.center);
        table.row();
        table.add(bestScoreLabel).padTop(this.getHeight() * 1 / 100).padLeft(this.getHeight() * 3 / 100).align(Align.right);
        table.add(bestScoreValue).padTop(this.getHeight() * 1 / 100).padRight(this.getHeight() * 3 / 100).align(Align.center);
        table.row();
        table.add(back).left().bottom().padLeft(this.getHeight() * 3 / 100).padBottom(this.getHeight() * 3 / 100).expandY();
        table.add(restart).right().bottom().padRight(this.getHeight() * 3 / 100).padBottom(this.getHeight() * 3 / 100);
    }

    public abstract void onBack();

    public abstract void onRestart();

    public void setScore(String score) {
        scoreValue.setText(score);
    }

    public void setBestScore(String score) {
        bestScoreValue.setText(score);
    }
}
