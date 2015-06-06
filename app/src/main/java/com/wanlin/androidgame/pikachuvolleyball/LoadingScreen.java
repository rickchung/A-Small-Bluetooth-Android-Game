package com.wanlin.androidgame.pikachuvolleyball;

import android.graphics.Point;

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

    Point screenSizePoint;
    int screenWidth, screenHeight;

    public LoadingScreen(Game game) {
        super(game);
        screenSizePoint = ((PikachuVolleyball) game).getSizePoint();
        screenWidth = screenSizePoint.x;
        screenHeight = screenSizePoint.y;
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

        // set background image
        Assets.gameBgImage = g.newImage("gameBgImage.png", Graphics.ImageFormat.RGB565, screenWidth, screenHeight);
        Assets.menuBgImage = g.newImage("menuBgImage.jpg", Graphics.ImageFormat.RGB565, screenSizePoint.x, screenSizePoint.y);

        // set pikachu character image
        Assets.characterA = g.newImage("meImage.png", Graphics.ImageFormat.ARGB4444);
        Assets.characterB = g.newImage("enemyImage.png", Graphics.ImageFormat.ARGB4444);

        // set button image
        Assets.startButton = g.newImage("start-button.png", Graphics.ImageFormat.RGB565);
        Assets.makeDiscoverableBt = g.newImage("make_discoverable_bt.png", Graphics.ImageFormat.RGB565);
        Assets.findDevicesBt = g.newImage("find_devices_bt.png", Graphics.ImageFormat.RGB565);

        // set background music
        Assets.bgMusic = game.getAudio().createMusic("kimisa.mp3");
        Assets.bgMusic.setLooping(true);
        Assets.bgMusic.setVolume(0.85f);

        // set short-kimisa sound effect
        Assets.shortKimisa = game.getAudio().createMusic("short_kimisa.mp3");
        Assets.shortKimisa.setLooping(false);
        Assets.shortKimisa.setVolume(0.85f);

        game.setScreen(new MainMenuScreen(game));
    }

    @Override
    public void paint(float deltaTime) {
        Graphics g = game.getGraphics();

        Assets.loadingBgImage = g.newImage("loadingBgImage.jpg", Graphics.ImageFormat.RGB565, screenWidth, screenHeight);
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
