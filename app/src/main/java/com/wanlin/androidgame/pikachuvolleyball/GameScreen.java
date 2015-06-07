package com.wanlin.androidgame.pikachuvolleyball;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import com.kilobolt.framework.Game;
import com.kilobolt.framework.Graphics;
import com.kilobolt.framework.Image;
import com.kilobolt.framework.Input;
import com.kilobolt.framework.Screen;
import com.rick.androidgame.bluetooth.BluetoothModule;

import java.util.List;

/**
 * Created by wanlin on 15/6/4.
 */
public class GameScreen extends Screen {
    enum GameState {
        Ready, Running, Paused, GameOver
    }

    private static final String LOG_TAG = "GameScreen";

    GameState state = GameState.Ready;
    private static Pikachu me, enemy;
    private Image characterA, characterB, characterAA, characterBB, characterBM, characterAM,
            volleyball, currentSpriteA, currentSpriteB;
    private Image cJumpA, cJumpAM, cJumpAA, cJumpB, cJumpBM, cJumpBB;
    private static int screenWidth;
    private static int screenHeight;
    private static int pauseHeight = 300;
    public static int MIDDLE_BOUNDARY;
    public static int ME_BOUNDARY;
    public static int ENEMY_BOUNDARY;
    private Animation meAnim, meJumpAnim;
    private Animation enemyAnim, enemyJumpAnim;
    private int score = 0;

    public static final int PAUSE_GAME = 0;
    public static final int YOU_GOOD_TO_GO = 312849;
    public static final int START_THAT_FUKING_GAMEEEE = 12345;
    public final int MOVE_LEFT = 1;
    public final int MOVE_RIGHT = 2;
    public final int STOP_LEFT = 3;
    public final int STOP_RIGHT = 4;
    public final int STOP_BOTH = 123;
    public final int PAUSE = 10;
    public final int JUMP = 5;
    public static final int YOU_ARE_LOSE = 938495;
    private boolean isMoving = false;
    private boolean isHolding = false;
    private final int ANI_RATE = 150;
    private int touchDownY;
    private boolean musicIsPlaying = false;

    private BluetoothModule bluetoothModule;

    private boolean isWin = false;

    private Game game;

    int targetScore = 15;
    Paint paint;
    float densityRatio;

    public GameScreen(Game game) {
        super(game);
        this.game = game;
        init();
    }

    private void init() {
        // Set screen size
        Point screenSizePoint = ((PikachuVolleyball) game).getSizePoint();
        //((PikachuVolleyball)game).getWindowManager().getDefaultDisplay().getRealSize(screenSizePoint);
        screenWidth = screenSizePoint.x;
        screenHeight = screenSizePoint.y;

        // Density
        densityRatio = ((PikachuVolleyball) game).getResources().getDisplayMetrics().density;

        // Initialize game objects here
        characterA = Assets.characterA;
        characterAA = Assets.characterAA;
        characterAM = Assets.characterAM;
        characterB = Assets.characterB;
        characterBB = Assets.characterBB;
        characterBM = Assets.characterBM;
        cJumpA = Assets.cJumpA;
        cJumpAM = Assets.cJumpAM;
        cJumpAA = Assets.cJumpAA;
        cJumpB = Assets.cJumpB;
        cJumpBM = Assets.cJumpBM;
        cJumpBB = Assets.cJumpBB;

        if (((PikachuVolleyball) game).isHost()) {
            // I'm at the right

            // Set boundaries
            ENEMY_BOUNDARY = 0;
            ME_BOUNDARY = screenWidth;
            MIDDLE_BOUNDARY = screenWidth / 2;

            me = new Pikachu(screenWidth - characterA.getWidth(),
                    screenHeight - characterA.getHeight() - 130, screenSizePoint,
                    ME_BOUNDARY, MIDDLE_BOUNDARY);
            enemy = new Pikachu(0, screenHeight - characterB.getHeight() - 130, screenSizePoint,
                    ENEMY_BOUNDARY, MIDDLE_BOUNDARY);

            // Normal frame for me
            meAnim = new Animation();
            Image[] meFrames = {characterA, characterAM, characterAA, characterAM};
            for (Image i : meFrames) {
                meAnim.addFrame(i, ANI_RATE);
            }
            // Jump frame for me
            meJumpAnim = new Animation();
            Image[] meJumpFrames = {cJumpA, cJumpAM, cJumpAA, cJumpAM};
            for (Image i : meJumpFrames) {
                meJumpAnim.addFrame(i, ANI_RATE);
            }

            // Normal frame for enemy
            enemyAnim = new Animation();
            Image[] enFrames = {characterB, characterBM, characterBB, characterBM};
            for (Image i : enFrames) {
                enemyAnim.addFrame(i, ANI_RATE);
            }
            // Jump frame for enemy
            enemyJumpAnim = new Animation();
            Image[] enemyJumpFrames = {cJumpB, cJumpBM, cJumpBB, cJumpBM};
            for (Image i : enemyJumpFrames) {
                enemyJumpAnim.addFrame(i, ANI_RATE);
            }
        }
        else {
            // I'm at the left

            // Set bound
            ME_BOUNDARY = 0;
            ENEMY_BOUNDARY = screenWidth;
            MIDDLE_BOUNDARY = screenWidth / 2 - (characterA.getWidth());

            me = new Pikachu(0, screenHeight - characterB.getHeight() - 130, screenSizePoint,
                    ME_BOUNDARY, MIDDLE_BOUNDARY);
            enemy = new Pikachu(screenWidth - characterA.getWidth(), screenHeight - characterA.getHeight() - 130, screenSizePoint,
                    ENEMY_BOUNDARY, MIDDLE_BOUNDARY);

            // create an animation and add two characterA and characterB into the frame
            meAnim = new Animation();
            Image[] meFrames = {characterB, characterBM, characterBB, characterBM};
            for (Image i : meFrames) {
                meAnim.addFrame(i, ANI_RATE);
            }
            // Jump frame for me
            meJumpAnim = new Animation();
            Image[] meJumpFrames = {cJumpB, cJumpBM, cJumpBB, cJumpBM};
            for (Image i : meJumpFrames) {
                meJumpAnim.addFrame(i, ANI_RATE);
            }

            enemyAnim = new Animation();
            Image[] enFrames = {characterA, characterAM, characterAA, characterAM};
            for (Image i : enFrames) {
                enemyAnim.addFrame(i, ANI_RATE);
            }
            enemyJumpAnim = new Animation();
            Image[] enemyJumpFrames = {cJumpA, cJumpAM, cJumpAA, cJumpAM};
            for (Image i : enemyJumpFrames) {
                enemyJumpAnim.addFrame(i, ANI_RATE);
            }
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
        if (touchEvents.size() > 0) {
            stargGame();
            bluetoothModule.sendMessage(String.valueOf(START_THAT_FUKING_GAMEEEE));
        }
    }

    private void updateRunning(List<Input.TouchEvent> touchEvents, float deltaTime) {
        int len = touchEvents.size();
        if (isMoving && Math.abs(me.getCenterX() - ME_BOUNDARY) > MIDDLE_BOUNDARY) {
            isMoving = false;
            me.handleAction(STOP_LEFT);
            bluetoothModule.sendMessage(String.valueOf(STOP_LEFT));
            me.handleAction(STOP_RIGHT);
            bluetoothModule.sendMessage(String.valueOf(STOP_RIGHT));
        }

        for (int i = 0; i < len; i++) {
            Input.TouchEvent event = touchEvents.get(i);

            // ========== TOUCH DOWN Event
            if (event.type == Input.TouchEvent.TOUCH_DOWN) {
                isHolding = true;

                // Pause
                if (inBounds(event, 0, 0, 400, 200)) {
                    if (isMoving) {
                        isMoving = false;
                        me.handleAction(STOP_LEFT);
                        me.handleAction(STOP_RIGHT);
                    }
                    bluetoothModule.sendMessage(String.valueOf(PAUSE_GAME));
                    pause();
                }
                // Movd left
                if (inBounds(event, 0, pauseHeight, screenWidth / 2, screenHeight - pauseHeight)) {
                    // Move left
                    me.handleAction(MOVE_LEFT);
                    isMoving = true;
                }
                // Move right
                else if (inBounds(event, screenWidth / 2, pauseHeight, screenWidth / 2, screenHeight - pauseHeight)) {
                    // Move right
                    me.handleAction(MOVE_RIGHT);
                    isMoving = true;
                }

                // For jump
                touchDownY = event.y;
            }

            // ========== TOUCH UP Event ==========
            if (event.type == Input.TouchEvent.TOUCH_UP) {
                isHolding = false;

                if (isMoving) {
                    isMoving = false;
                    me.handleAction(STOP_LEFT);
                    me.handleAction(STOP_RIGHT);
                }
            }

            if (touchDownY != 0 && event.type == Input.TouchEvent.TOUCH_DRAGGED) {
                if (event.y - touchDownY < 0) {
                    me.handleAction(JUMP);
                }
            }
        }

        // check score
        if (score == targetScore) {
            state = GameState.GameOver;
        }

        // MUSIC!
        if (Math.abs(me.getCenterX()-enemy.getCenterX()) < 280) {
            if (!musicIsPlaying) {
                musicIsPlaying = true;
                Assets.shortKimisa = game.getAudio().createMusic("short_kimisa.mp3");
                Assets.shortKimisa.play();
                bluetoothModule.sendMessage(String.valueOf(YOU_ARE_LOSE));
                isWin = true;
                endGame();
            }
        }
        else {
            if (musicIsPlaying) {
                musicIsPlaying = false;
                Assets.shortKimisa.dispose();
            }
        }

        // Me update
        me.update();
        if (me.isJumped()) currentSpriteA = meJumpAnim.getImage();
        else               currentSpriteA = meAnim.getImage();
        // Enemy update
        enemy.update();
        if (enemy.isJumped())   currentSpriteB = enemyJumpAnim.getImage();
        else                    currentSpriteB = enemyAnim.getImage();

        animate();

        // Send me position
        bluetoothModule.sendMessage(String.valueOf(
                String.format("%d %d %s", me.getCenterX(), me.getCenterY(), String.valueOf(me.isJumped()) )
        ));
    }

    private void updatePaused(List<Input.TouchEvent> touchEvents) {
        int len = touchEvents.size();

        for (int i = 0; i < len; i++) {
            Input.TouchEvent event = touchEvents.get(i);
            if (event.type == Input.TouchEvent.TOUCH_UP) {
                if (inBounds(event, 0, 0, 400, 200)) {
                    bluetoothModule.sendMessage(String.valueOf(YOU_GOOD_TO_GO));
                    resume();
                }
            }
        }
    }

    private void updateGameOver(List<Input.TouchEvent> touchEvents) {
    }

    @Override
    public void paint(float deltaTime) {
        Graphics g = game.getGraphics();

        // First draw the game elements.
        g.drawImage(Assets.gameBgImage, 0, 0);
        g.drawImage(currentSpriteA, me.getCenterX(), me.getCenterY());
        g.drawImage(currentSpriteB, enemy.getCenterX(), enemy.getCenterY());

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
        g.drawString("Tap to Start!", screenWidth / 2, screenHeight / 2, paint);
        // g.drawString("Tap to Start!", 640, 300, paint);

    }

    private void drawRunningUI() {
        Graphics g = game.getGraphics();

        g.drawImage(currentSpriteA, me.getCenterX(), me.getCenterY());
        g.drawImage(currentSpriteB, enemy.getCenterX(), enemy.getCenterY());
        g.drawString("Pause", 200, 100, paint);
    }

    private void drawPausedUI() {
        Graphics g = game.getGraphics();
        // Darken the entire screen so you can display the Paused screen.
        g.drawARGB(155, 0, 0, 0);
        g.drawString("Resume", 400, 165, paint);

    }

    private void drawGameOverUI() {
        Graphics g = game.getGraphics();
        if (isWin) {
            g.drawImage(Assets.gameoverImage, 0, 0);
            g.drawImage(currentSpriteA, me.getCenterX(), me.getCenterY());
            g.drawImage(currentSpriteB, enemy.getCenterX(), enemy.getCenterY());
        }
        else
            g.drawImage(Assets.loserImage, 0, 0);

    }

    private boolean inBounds(Input.TouchEvent event, int x, int y, int width, int height) {
        if (event.x > x && event.x < x + width - 1 && event.y > y
                && event.y < y + height - 1)
            return true;
        else
            return false;
    }

    public void animate() {
        meAnim.update(10);
        meJumpAnim.update(30);
        enemyAnim.update(10);
        enemyJumpAnim.update(30);
    }

    @Override
    public void pause() {
        if (state == GameState.Running) {
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
        if (musicIsPlaying) {
            Assets.shortKimisa.dispose();
        }
        if (state == GameState.GameOver) {
            game.setScreen(new GameScreen(game));
        }
    }

    public Pikachu getEnemy() {
        return enemy;
    }

    public void stargGame() {
        state = GameState.Running;
    }

    public void endGame() { state = GameState.GameOver; }
}
