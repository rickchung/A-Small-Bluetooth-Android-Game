package com.wanlin.androidgame.pikachuvolleyball;

import com.kilobolt.framework.Game;
import com.kilobolt.framework.Graphics;
import com.kilobolt.framework.Screen;

/**
 * Created by wanlin on 15/6/4.
 */

/**
 * All screen classes have three important classes.
 * The update()     method
 * The paint()      method
 * The backButton() method (which is called when the user presses the back button in the game).
 */
public class LoadingScreen extends Screen {
    public LoadingScreen(Game game) {
        super(game);
    }

    /**
     * Load all the resources that will use in the game
     * (i.e. all the resources that we have created in the Assets class).
     * We would not need anything in our paint() method,
     * unless you would like to have an image while the game loads these resources
     * (make sure you load this in another class).
     *
     * @param deltaTime
     */
    @Override
    public void update(float deltaTime) {
        Graphics g = game.getGraphics();

        //set background Image
        Assets.loadingBgImage = g.newImage("loadingBgImage.jpg", Graphics.ImageFormat.RGB565);
        Assets.gameBgImage = g.newImage("gameBgImage.png", Graphics.ImageFormat.RGB565);
        Assets.characterA = g.newImage("meImage.png", Graphics.ImageFormat.ARGB4444);
        Assets.characterB = g.newImage("enemyImage.png", Graphics.ImageFormat.ARGB4444);

        game.setScreen(new MainMenuScreen(game));
    }

    @Override
    public void paint(float deltaTime) {
        Graphics g = game.getGraphics();
        g.drawImage(Assets.loadingBgImage, 0, 0);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }


    @Override
    public void dispose() {

    }


    @Override
    public void backButton() {

    }
}
