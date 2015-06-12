package com.wanlin.androidgame.pikachuvolleyball;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;

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
    private Pikachu me, enemy;
    private Volleyball volleyball;
    private Image characterA, characterB, characterAA, characterBB, characterBM, characterAM,
            volleyballImg, attackedBallImg, currentSpriteA, currentSpriteB, cWin, cDead;
    private Image cJumpA, cJumpAM, cJumpAA, cJumpB, cJumpBM, cJumpBB, cAttackA, cAttackAA, cAttackB, cAttackBB;
    private static int screenWidth;
    private static int halfScreenwidth;
    private static int screenHeight;
    private static int pauseHeight = 500;
    private static int otherScreenWidth;
    private static int otherScreenHeight;
    private static boolean otherSizeIsSet;
    public static int MIDDLE_BOUNDARY;
    public static int ME_BOUNDARY;
    public static int ENEMY_BOUNDARY;
    public static int GROUND_BOUNDARY;
    private Animation meAnim, meJumpAnim, meAttackAnim;
    private Animation enemyAnim, enemyJumpAnim, enemyAttackAnim;
    private int score = 0;

    public static final int PAUSE_GAME = 0;
    public static final int YOU_GOOD_TO_GO = 312849;
    public static final int START_THAT_FUKING_GAMEEEE = 12345;
    public static final String SCREEN_SIZE_KEY = "screensizekey";
    public final int MOVE_LEFT = 1;
    public final int MOVE_RIGHT = 2;
    public final int STOP_LEFT = 3;
    public final int STOP_RIGHT = 4;
    public final int STOP_BOTH = 123;
    public final int PAUSE = 10;
    public final int JUMP = 5;
    public static final String VOLLEYBALL_MSG = "volleyball_msg";
    public static final int YOU_ARE_LOSE = 938495;
    public static final String SCORE_SYNC = "score_sync";
    private boolean isMoving = false;
    private boolean isHolding = false;
    private final int ANI_RATE = 150;
    private int touchDownY;
    private boolean musicIsPlaying = false;
    private BluetoothModule bluetoothModule;
    private boolean isWin = false;
    private Game game;
    private int targetScore = 15;
    private int myscore;
    private int enemyscore;
    Paint paint;
    float densityRatio;

    private int volleyballAddSpeed;
    private int windAcc = 1;

    private static int boundX;
    private static int boundY;

    private boolean isAttacking;
    private boolean iAmAttacking;
    private boolean enemyIsAttacking;
    private int attackDuration;
    private static final int ATTACK_DURATION_BOUND = 20;


    public GameScreen(Game game) {
        super(game);
        this.game = game;
        init();
    }

    private void init() {
        // Init some variables
        isAttacking = false;
        iAmAttacking = false;
        enemyIsAttacking = false;
        attackDuration = 0;
        Assets.shortKimisa = game.getAudio().createMusic("short_kimisa.mp3");
        Assets.playingBgm = game.getAudio().createMusic("gameBGM.mp3");
        Assets.playingBgm.setLooping(true);

        // Set screen size
        Point screenSizePoint = ((PikachuVolleyball) game).getSizePoint();
        //((PikachuVolleyball)game).getWindowManager().getDefaultDisplay().getRealSize(screenSizePoint);
        screenWidth = screenSizePoint.x;
        screenHeight = screenSizePoint.y;
        halfScreenwidth = screenWidth / 2;

        // Screen Density
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
        cAttackA = Assets.cAttackA;
        cAttackAA = Assets.cAttackAA;
        cJumpB = Assets.cJumpB;
        cJumpBM = Assets.cJumpBM;
        cJumpBB = Assets.cJumpBB;
        cAttackB = Assets.cAttackB;
        cAttackBB = Assets.cAttackBB;
        volleyballImg = Assets.volleyballImage;
        attackedBallImg = Assets.attackedBallImage;
        cWin = Assets.cWin;
        cDead = Assets.cDead;

        // Init score
        targetScore = 10;
        myscore = enemyscore = 0;

        // Init test volleyball
        volleyball = new Volleyball(10, 10, volleyballImg.getWidth(), volleyballImg.getHeight());

        if (((PikachuVolleyball) game).isHost()) {
            // I'm at the right
            // Set boundaries
            ENEMY_BOUNDARY = 0;
            ME_BOUNDARY = screenWidth;
            // MIDDLE_BOUNDARY = screenWidth / 2;

            me = new Pikachu(
                    screenWidth / 2 + 400,
                    screenHeight - characterA.getHeight() - 130, screenSizePoint,
                    characterA.getWidth(), characterA.getHeight());
            enemy = new Pikachu(
                    screenWidth / 2 - 400 - characterB.getWidth(),
                    screenHeight - characterB.getHeight() - 130, screenSizePoint,
                    characterB.getWidth(), characterB.getHeight());

            volleyballAddSpeed = -10;

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

            // Attack frames
            meAttackAnim = new Animation();
            Image[] meAttackFrames = {cAttackA, cAttackAA};
            for (Image i : meAttackFrames) {
                meAttackAnim.addFrame(i, ANI_RATE);
            }

            enemyAttackAnim = new Animation();
            Image[] enAttackFrames = {cAttackB, cAttackBB};
            for (Image i : enAttackFrames) {
                enemyAttackAnim.addFrame(i, ANI_RATE);
            }
        }
        else {
            // I'm at the left
            // Set bound
            ME_BOUNDARY = 0;
            ENEMY_BOUNDARY = screenWidth;
            // MIDDLE_BOUNDARY = screenWidth / 2 - (characterA.getWidth());

            me = new Pikachu(
                    screenWidth / 2 - 400 - characterB.getWidth(),
                    screenHeight - characterB.getHeight() - 130, screenSizePoint,
                    characterB.getWidth(), characterB.getHeight());
            enemy = new Pikachu(
                    screenWidth / 2 + 400,
                    screenHeight - characterA.getHeight() - 130, screenSizePoint,
                    characterA.getWidth(), characterA.getHeight());

            volleyballAddSpeed = 10;

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

            // Attack frames
            meAttackAnim = new Animation();
            Image[] meAttackFrames = {cAttackB, cAttackBB};
            for (Image i : meAttackFrames) {
                meAttackAnim.addFrame(i, ANI_RATE);
            }

            enemyAttackAnim = new Animation();
            Image[] enAttackFrames = {cAttackA, cAttackAA};
            for (Image i : enAttackFrames) {
                enemyAttackAnim.addFrame(i, ANI_RATE);
            }
        }

        Log.e(LOG_TAG, String.format("Me prop: r=%d, centerX=%d, centerY=%d",
                me.getRadius(), me.getCenterX(), me.getCenterY()));
        Log.e(LOG_TAG, String.format("Enemy prop: r=%d, centerX=%d, centerY=%d",
                enemy.getRadius(), enemy.getCenterX(), enemy.getCenterY()));


        GROUND_BOUNDARY = screenHeight - 200;

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
        if (!otherSizeIsSet) {
            bluetoothModule.sendMessage(String.format("%d %d %s",
                    screenWidth, screenHeight, SCREEN_SIZE_KEY));
            otherSizeIsSet = true;
        }
        if (touchEvents.size() > 0) {
            stargGame();
            bluetoothModule.sendMessage(String.valueOf(START_THAT_FUKING_GAMEEEE));
        }
    }

    private void updateRunning(List<Input.TouchEvent> touchEvents, float deltaTime) {
        boolean triggerJump = false;
        int len = touchEvents.size();

        if (isAttacking) {
            attackDuration += deltaTime;
            if (attackDuration >= ATTACK_DURATION_BOUND) {
                isAttacking = false;
                iAmAttacking = false;
                volleyball.setIsAttacked(false);
                attackDuration = 0;
            }
        }

        if (isMoving) {
            if (((PikachuVolleyball)game).isHost()) {
                if (me.getX() < MIDDLE_BOUNDARY + Assets.stickImage.getWidth()) {
                    isMoving = false;
                    me.handleAction(STOP_LEFT);
                    bluetoothModule.sendMessage(String.valueOf(STOP_LEFT));
                    me.handleAction(STOP_RIGHT);
                    bluetoothModule.sendMessage(String.valueOf(STOP_RIGHT));
                }
            }
            else {
                if (me.getX() + me.getWidth() > MIDDLE_BOUNDARY) {
                    isMoving = false;
                    me.handleAction(STOP_LEFT);
                    bluetoothModule.sendMessage(String.valueOf(STOP_LEFT));
                    me.handleAction(STOP_RIGHT);
                    bluetoothModule.sendMessage(String.valueOf(STOP_RIGHT));
                }
            }
        }

        /*
            Handle all touch events here
         */
        for (int i = 0; i < len; i++) {
            Input.TouchEvent event = touchEvents.get(i);

            // ========== TOUCH DOWN Event
            if (event.type == Input.TouchEvent.TOUCH_DOWN) {
                isHolding = true;

                // Pause
                if (inBounds(event, 0, 0, 400, 200)) {
                    // Check volleyball positin
//                    Log.e(LOG_TAG, String.format(
//                            "VB: centerX=%d, centerY=%d",
//                            volleyball.getCenterX(), volleyball.getCenterY()
//                    ));
                    Log.e(LOG_TAG, String.format("Middle boundary=%d", MIDDLE_BOUNDARY));
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

                // attack
                if (me.isJumped() && inBounds(event, screenWidth-400, 0, 800, pauseHeight)) {
                    Log.e(LOG_TAG, "ATTACK!!!!");
                    iAmAttacking = true;
                    isAttacking = true;
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
                if (!me.isJumped()) {
                    if (event.y - touchDownY < -100) {
                        triggerJump = true;
                        me.handleAction(JUMP);
                    }
                }
            }
        }

        /*
            Volleyball rebound events
         */
        // Volleyball rebounds at boundaries
        if (((PikachuVolleyball)game).isHost()) {
            if ((volleyball.getX() - volleyballImg.getWidth() / 2) < 0) {
                volleyball.setX(1 + volleyballImg.getWidth() / 2);
                if (volleyball.isAttacked()) {
                    volleyball.setSpeedX( -1 * volleyball.getSpeedX() );
                }
                else {
                    volleyball.boundHorizontally();
                }
            }
            else if ((volleyball.getX() + volleyballImg.getWidth() / 2) > screenWidth) {
                volleyball.setX(screenWidth - volleyballImg.getWidth() / 2 - 1);

                if (volleyball.isAttacked()) {
                    volleyball.setSpeedX( -1 * volleyball.getSpeedX() );
                }
                else {
                    volleyball.boundHorizontally();
                }
            }
            // If ball hits the ground
            if ((volleyball.getY() + volleyballImg.getHeight() / 2) > GROUND_BOUNDARY) {
                volleyball.setY(GROUND_BOUNDARY - volleyballImg.getHeight() / 2 - 10);
                volleyball.boundVertically();
                volleyball.setIsAttacked(false);

                // Score statistic
                if (volleyball.getX() > MIDDLE_BOUNDARY) {
                    enemyscore += 1;
                }
                else {
                    myscore += 1;
                }
                // Send scores to the other
                bluetoothModule.sendMessage(String.format("%d %d %s", myscore, enemyscore, SCORE_SYNC));
            }
            // If the ball hits the celling
            else if ((volleyball.getY() - volleyballImg.getHeight() / 2) < 0) {
                volleyball.setY(1 + volleyballImg.getHeight() / 2);

                if (volleyball.isAttacked()) {
                    volleyball.setSpeedY( -1 * volleyball.getSpeedY() );
                }
                else {
                    volleyball.setSpeedY(0);
                }
            }
        }

        /*
            Check if volleyball collides with a Pikachu or the middle boundary
         */
        if (volleyball.detectCollision(me.getCenterX(), me.getCenterY(), me.getRadius())) {
            if (iAmAttacking) {
                volleyball.updateSpeed(me.getCenterX(), me.getCenterY(), true);
                volleyball.setIsAttacked(true);
            }
            else
                volleyball.updateSpeed(me.getCenterX(), me.getCenterY());
        }
        if (volleyball.detectCollision(enemy.getCenterX(), enemy.getCenterY(), enemy.getRadius())) {
            if (enemyIsAttacking) {
                volleyball.updateSpeed(enemy.getCenterX(), enemy.getCenterY(), true);
                volleyball.setIsAttacked(true);
            }
            else
                volleyball.updateSpeed(enemy.getCenterX(), enemy.getCenterY());
        }
        if (volleyball.detectCollision(boundX, boundY, 50)) {
            volleyball.updateSpeed(boundX, boundY, 0.8);
        }
        if (volleyball.detectCollision(boundX, boundY+50, 50)) {
            volleyball.updateSpeed(boundX, boundY+50, 0.8);
        }
        if (volleyball.detectCollision(boundX, boundY+150, 50)) {
            volleyball.updateSpeed(boundX, boundY+150, 0.8);
        }


        /*
            Check score
         */
        if (myscore == targetScore) {
            bluetoothModule.sendMessage(String.valueOf(YOU_ARE_LOSE));
            isWin = true;
            endGame();
        }

        /*
            Bonus music
         */
        if (Math.abs(me.getX()-enemy.getX()) < me.getWidth()) {
            bluetoothModule.sendMessage(String.valueOf(YOU_ARE_LOSE));
            isWin = true;
            endGame();
//            if (!musicIsPlaying) {
//                musicIsPlaying = true;
//                Assets.shortKimisa.play();
//                bluetoothModule.sendMessage(String.valueOf(YOU_ARE_LOSE));
//                isWin = true;
//                endGame();
//            }
        }
//        else {
//            if (musicIsPlaying) {
//                musicIsPlaying = false;
//                Assets.shortKimisa.stop();
//                Assets.shortKimisa.dispose();
//            }
//        }

        /*
            Update events here
         */
        // Me update
        me.update();
        if (isAttacking)
            currentSpriteA = meAttackAnim.getImage();
        else if (me.isJumped())
            currentSpriteA = meJumpAnim.getImage();
        else
            currentSpriteA = meAnim.getImage();

        // Enemy update
        enemy.update();
        if (!enemy.isOnTheGround())
            currentSpriteB = enemyJumpAnim.getImage();
        else
            currentSpriteB = enemyAnim.getImage();

        /*
            Animation
         */
        // For animation
        animate();

        /*
            Bluetooth synchronization
         */
        // Send me position
        bluetoothModule.sendMessage(String.valueOf(
                String.format("%d %d %s", me.getX(), me.getY(), String.valueOf(triggerJump) )
        ));

        // Send volleyball position (host only)
        if (((PikachuVolleyball)game).isHost()) {
            // Volleyball update
            if (volleyball.isAttacked()) volleyball.update(true);
            else volleyball.update();

            bluetoothModule.sendMessage(String.valueOf(
                    String.format("%d %d %s", volleyball.getX(), volleyball.getY(), VOLLEYBALL_MSG)
            ));
        }
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
        g.drawImage(Assets.stickImage, boundX, boundY);
        g.drawImage(currentSpriteA, me.getX(), me.getY());
        g.drawImage(currentSpriteB, enemy.getX(), enemy.getY());
        g.drawImage(volleyballImg, volleyball.getX(), volleyball.getY());

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

        g.drawImage(currentSpriteA, me.getX(), me.getY());
        g.drawImage(currentSpriteB, enemy.getX(), enemy.getY());
        g.drawString("Pause", 200, 100, paint);

        if (volleyball.isAttacked()) {
            g.drawImage(attackedBallImg, volleyball.getX(), volleyball.getY());
        }
        else {
            g.drawImage(volleyballImg, volleyball.getX(), volleyball.getY());
        }
        if (((PikachuVolleyball)game).isHost())
            g.drawString(String.format("%d | %d", enemyscore, myscore),
                    halfScreenwidth, 100, paint);
        else {
            g.drawString(String.format("%d | %d", myscore, enemyscore),
                    halfScreenwidth, 100, paint);
        }
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
            g.drawImage(Assets.cWin, me.getX(), screenHeight - Assets.cWin.getHeight() - 130);
            g.drawImage(Assets.cDead, enemy.getX(), screenHeight - Assets.cDead.getHeight() - 130);
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
        Graphics g = game.getGraphics();

        meAnim.update(10);
        meJumpAnim.update(30);
        meAttackAnim.update(30);
        enemyAnim.update(10);
        enemyJumpAnim.update(30);
        enemyAttackAnim.update(30);
    }

    @Override
    public void pause() {
        if (state == GameState.Running) {
            me.handleAction(PAUSE);
            enemy.handleAction(PAUSE);
            state = GameState.Paused;
            try {
                Assets.playingBgm.pause();
            }
            catch (Exception e) { }
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
        if (state == GameState.GameOver) {
//            try {
//                Assets.playingBgm.dispose();
//            }
//            catch (Exception e) {}
//            try {
//                Assets.shortKimisa.dispose();
//            }
//            catch (Exception e) {}
            game.setScreen(new GameScreen(game));
        }
    }

    public Pikachu getEnemy() {
        return enemy;
    }

    public Volleyball getVolleyball() {
        return volleyball;
    }

    public void stargGame() {
        Assets.shortKimisa.pause();
        Assets.playingBgm.seekBegin();
        Assets.playingBgm.play();
        state = GameState.Running;
    }

    public void endGame() {
        Assets.playingBgm.pause();
        Assets.shortKimisa.seekBegin();
        Assets.shortKimisa.play();
        state = GameState.GameOver;
    }

    public void setOtherScreenSize(int width, int height) {
        Log.e(LOG_TAG, "other screen width: " + width);
        Log.e(LOG_TAG, "my screen width: " + screenWidth);
        otherScreenWidth = width;
        otherScreenHeight = height;
        if (screenWidth > otherScreenWidth) {
            MIDDLE_BOUNDARY = otherScreenWidth / 2;
        }
        else {
            MIDDLE_BOUNDARY = screenWidth / 2;
        }
        Log.e(LOG_TAG, "MIDDLE_BOUNDARY = " + MIDDLE_BOUNDARY);
        boundX = MIDDLE_BOUNDARY;
        boundY = me.getY()+me.getHeight()-Assets.stickImage.getHeight();
    }

    public void setScores(int myscore, int enemyscore) {
        this.myscore = myscore;
        this.enemyscore = enemyscore;
    }
}
