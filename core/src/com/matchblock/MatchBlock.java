package com.matchblock;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Game;
import com.matchblock.ui.MenuScreen;

public class MatchBlock extends Game {
	@Override
    public void create() {
        setScreen(new MenuScreen(this));
    }
}
