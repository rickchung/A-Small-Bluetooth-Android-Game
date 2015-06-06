package com.wanlin.androidgame.pikachuvolleyball;

import android.graphics.Color;
import android.graphics.Paint;

import com.rick.androidgame.bluetooth.BluetoothModule;
import com.kilobolt.framework.Game;
import com.kilobolt.framework.Graphics;
import com.kilobolt.framework.Image;
import com.kilobolt.framework.Input;
import com.kilobolt.framework.Screen;
import com.kilobolt.framework.implementation.AndroidGame;

import java.util.List;

/**
 * Created by wanlin on 15/6/4.
 */
public class GameScreen extends Screen {
    enum GameState {
        Ready, Running, Paused, GameOver
    }

    GameState state = GameState.Ready;
    private static Pikachu me, enemy;
    private Image characterA, characterB, volleyball, currentSpriteA, currentSpriteB;
    private static int screenWidth = 1280;
    private static int screenHeight = 720;
    private static int pauseHeight = 300;
    private Animation meAnim;
    private Animation enemyAnim;
    private int score = 0;

    public static final int STOP_MOVING = 0;
    public static final int YOU_GOOD_TO_GO = 312849;
    public static final int START_THAT_FUKING_GAMEEEE = 12345;
    public final int MOVE_LEFT = 1;
    public final int MOVE_RIGHT = 2;
    public final int STOP_LEFT = 3;
    public final int STOP_RIGHT = 4;
    public final int PAUSE = 10;
    private final int JUMP = 5;
    private boolean isMoving = false;

    private BluetoothModule bluetoothModule;

    // Variable Setup

    // You would create game objects here.

    int targetScore = 15;
    Paint paint;

    public GameScreen(Game game) {
        super(game);

        // Initialize game objects here
        if ( ((AndroidGame) game).isHost() ) {
            characterA = Assets.characterA;
            characterB = Assets.characterB;
            me = new Pikachu(screenWidth - characterA.getWidth(), screenHeight - characterA.getHeight());
            enemy = new Pikachu(0, screenHeight - characterB.getHeight());
            // create an animation and add two characterA and characterB into the frame
            meAnim = new Animation();
            meAnim.addFrame(characterA, 50);
            enemyAnim = new Animation();
            enemyAnim.addFrame(characterB, 50);
        }
        else {
            characterA = Assets.characterB;
            characterB = Assets.characterA;
            me = new Pikachu(0, screenHeight - characterB.getHeight());
            enemy = new Pikachu(screenWidth - characterA.getWidth(), screenHeight - characterA.getHeight());
            // create an animation and add two characterA and characterB into the frame
            meAnim = new Animation();
            meAnim.addFrame(characterB, 50);
            enemyAnim = new Animation();
            enemyAnim.addFrame(characterA, 50);
        }

        // current frame
        currentSpriteA = meAnim.getImage();
        currentSpriteB = enemyAnim.getImage();




        // Defining a paint object
        paint = new Paint();

        // Set text properties
        paint.setTextSize(100);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);

        // Get Bluetooth module
        bluetoothModule = ((PikachuVolleyball) game).getBtModule();
    }

    @Override
    public void update(float deltaTime) {
        List<Input.TouchEvent> touchEvents = game.getInput().getTouchEvents();

        // We have four separate update methods in this example.
        // Depending on the state of the game, we call different update methods.
        // Refer to Unit 3's code. We did a similar thing without separating the
        // update methods.

        if (state == GameState.Ready)
            updateReady(touchEvents);
        if (state == GameState.Running)
            updateRunning(touchEvents, deltaTime);
        if (state == GameState.Paused)
            updatePaused(touchEvents);
        if (state == GameState.GameOver)
            updateGameOver(touchEvents);
    }

    private void updateReady(List<Input.TouchEvent> touchEvents) {

        // This example starts with a "Ready" screen.
        // When the user touches the screen, the game begins.
        // state now becomes GameState.Running.
        // Now the updateRunning() method will be called!

        if (touchEvents.size() > 0) {
            stargGame();
            bluetoothModule.sendMessage(String.valueOf(START_THAT_FUKING_GAMEEEE));
        }
    }

    private void updateRunning(List<Input.TouchEvent> touchEvents, float deltaTime) {

        //This is identical to the update() method from our Unit 2/3 game.

        // handle me update
        int len = touchEvents.size();

        for (int i = 0; i < len; i++) {
            Input.TouchEvent event = touchEvents.get(i);

            // me moveX
            if (event.type == Input.TouchEvent.TOUCH_DOWN) {
                if (inBounds(event, 0, 0, screenWidth, pauseHeight)) {
                    if (isMoving) {
                        isMoving = false;
                        me.handleAction(STOP_LEFT);
                        bluetoothModule.sendMessage(String.valueOf(STOP_LEFT));
                        me.handleAction(STOP_RIGHT);
                        bluetoothModule.sendMessage(String.valueOf(STOP_RIGHT));
                    }
                    bluetoothModule.sendMessage(String.valueOf(STOP_MOVING));
                    pause();
                }

                if (inBounds(event, 0, pauseHeight, screenWidth / 2, screenHeight - pauseHeight)) {
                    // Move left;
                    me.handleAction(MOVE_LEFT);
                    bluetoothModule.sendMessage(String.valueOf(MOVE_LEFT));
                    isMoving = true;

                }
                else if (inBounds(event, screenWidth / 2, pauseHeight, screenWidth / 2, screenHeight - pauseHeight)) {
                    // Move right.
                    me.handleAction(MOVE_RIGHT);
                    bluetoothModule.sendMessage(String.valueOf(MOVE_RIGHT));
                    isMoving = true;
                }
            }

            // me stop moveX
            if (event.type == Input.TouchEvent.TOUCH_UP) {
                if (isMoving) {
                    isMoving = false;
                    me.handleAction(STOP_LEFT);
                    bluetoothModule.sendMessage(String.valueOf(STOP_LEFT));
                    me.handleAction(STOP_RIGHT);
                    bluetoothModule.sendMessage(String.valueOf(STOP_RIGHT));
                }
//                if (inBounds(event, 0, pauseHeight, screenWidth / 2, screenHeight - pauseHeight)) {
//                    // Stop moving left.
//                    me.handleAction(STOP_LEFT);
//                    bluetoothModule.sendMessage(String.valueOf(STOP_LEFT));
//                }
//                else if (inBounds(event, screenWidth / 2, pauseHeight, screenWidth / 2, screenHeight - pauseHeight)) {
//                    // Stop moving right.
//                    me.handleAction(STOP_RIGHT);
//                    bluetoothModule.sendMessage(String.valueOf(STOP_RIGHT));
//                }
            }
//                    me.handleAction(JUMP);
        }

        // handle enemy update
        // ...

//        enemy.handleAction(MOVE_RIGHT);

        // if (bluetooth.getAction == MOVE_RIGHT)

        // check score
        if (score == targetScore) {
            state = GameState.GameOver;
        }


        // 3. Call individual update() methods here.
        // This is where all the game updates happen.
        // For example, robot.update();

        me.update();
        currentSpriteA = meAnim.getImage();

        enemy.update();
        currentSpriteB = enemyAnim.getImage();

        animate();
    }

    private void updatePaused(List<Input.TouchEvent> touchEvents) {
        int len = touchEvents.size();

        for (int i = 0; i < len; i++) {
            Input.TouchEvent event = touchEvents.get(i);
            if (event.type == Input.TouchEvent.TOUCH_UP) {
                if (inBounds(event, 0, 0, 800, 240)) {

                    if (!inBounds(event, 0, 0, 35, 35)) {
                        bluetoothModule.sendMessage(String.valueOf(YOU_GOOD_TO_GO));
                        resume();
                    }
                }
            }
        }
//        for (int i = 0; i < len; i++) {
//            Input.TouchEvent event = touchEvents.get(i);
//        }
    }

    private void updateGameOver(List<Input.TouchEvent> touchEvents) {
        int len = touchEvents.size();
        for (int i = 0; i < len; i++) {
            Input.TouchEvent event = touchEvents.get(i);
            if (event.type == Input.TouchEvent.TOUCH_UP) {
                if (event.x > 300 && event.x < 980 && event.y > 100
                        && event.y < 500) {
                    nullify();

                    game.setCurScreenType(AndroidGame.TYPE_SCREEN_MENU);
                    game.setScreen(new MainMenuScreen(game));
                    return;
                }
            }
        }

    }

    @Override
    public void paint(float deltaTime) {
        Graphics g = game.getGraphics();

        // First draw the game elements.
        g.drawImage(Assets.gameBgImage, 0, 0);

        g.drawImage(currentSpriteA, me.getCenterX(),
                me.getCenterY());
        g.drawImage(currentSpriteB, enemy.getCenterX(),
                enemy.getCenterY());

        // Secondly, draw the UI above the game elements.
        if (state == GameState.Ready)
            drawReadyUI();
        if (state == GameState.Running)
            drawRunningUI();
        if (state == GameState.Paused)
            drawPausedUI();
        if (state == GameState.GameOver)
            drawGameOverUI();

    }

    private void nullify() {

        // Set all variables to null. You will be recreating them in the
        // constructor.
        paint = null;
        me = null;
        enemy = null;
        currentSpriteA = null;
        currentSpriteB = null;
        characterA = null;
        characterB = null;
        meAnim = null;
        enemyAnim = null;

        // Call garbage collector to clean up memory.
        System.gc();
    }

    private void drawReadyUI() {
        Graphics g = game.getGraphics();

        g.drawARGB(155, 0, 0, 0);
        g.drawString("Tap to Start!",
                640, 300, paint);

    }

    private void drawRunningUI() {
        Graphics g = game.getGraphics();

        g.drawImage(currentSpriteA, me.getCenterX(),
                me.getCenterY());
        g.drawImage(currentSpriteB, enemy.getCenterX(),
                enemy.getCenterY());
    }

    private void drawPausedUI() {
        Graphics g = game.getGraphics();
        // Darken the entire screen so you can display the Paused screen.
        g.drawARGB(155, 0, 0, 0);
        g.drawString("Resume", 400, 165, paint);

    }

    private void drawGameOverUI() {
        Graphics g = game.getGraphics();
        g.drawRect(0, 0, 1281, 801, Color.BLACK);
        g.drawString("GAME OVER.", 640, 300, paint);

    }

    private boolean inBounds(Input.TouchEvent event, int x, int y, int width,
                             int height) {
        if (event.x > x && event.x < x + width - 1 && event.y > y
                && event.y < y + height - 1)
            return true;
        else
            return false;
    }

    public void animate() {
        meAnim.update(10);
        enemyAnim.update(10);
    }

    @Override
    public void pause() {
        if (state == GameState.Running){
            me.handleAction(PAUSE);
            enemy.handleAction(PAUSE);
            state = GameState.Paused;
        }
    }

    @Override
    public void resume() {
        if (state == GameState.Paused)
            state = GameState.Running;
    }

    @Override
    public void dispose() {

    }

    @Override
    public void backButton() {
        pause();
        state = GameState.Paused;
        game.setScreen(new MainMenuScreen(game));
    }

    public Pikachu getEnemy() {
        return enemy;
    }

    public void stargGame() {
        state = GameState.Running;
    }
}
