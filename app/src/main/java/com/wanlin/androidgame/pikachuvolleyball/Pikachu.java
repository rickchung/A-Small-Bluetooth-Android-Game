package com.wanlin.androidgame.pikachuvolleyball;

import android.graphics.Point;
import android.util.Log;

/**
 * Created by wanlin on 15/6/5.
 */
public class Pikachu {
    final String LOG_TAG = "Pikachu";
    final int JUMP_SPEED = -15;
    float MOVE_SPEED = 5.0f;

    private final int PAUSE = 0;
    private final int MOVE_LEFT = 1;
    private final int MOVE_RIGHT = 2;
    public final int STOP_LEFT = 3;
    public final int STOP_RIGHT = 4;
    public static final int STOP_BOTH = 123;
    private final int JUMP = 5;
    private final float NUM_MOVING_SLOTS = 900.0f;

    private boolean ducked = false;
    private boolean jumped = false;
    private boolean movingLeft = false;
    private boolean movingRight = false;

    private float speedX = 0;
    private float speedY = 0;

    private int centerX;
    private int centerY;

    private float screenDensityRatio;
//    private ArrayList<com.kilobolt.robotgame.Projectile> projectiles = new ArrayList<com.kilobolt.robotgame.Projectile>();


    Pikachu (int x, int y, Point sizePoint) {
        centerX = x;
        centerY = y;
        this.screenDensityRatio = sizePoint.x / NUM_MOVING_SLOTS;
        Log.d(LOG_TAG, "Ratio info: " + screenDensityRatio + " screen size: " + sizePoint.x);
    }

    public void update() {
        // Moves Character or Scrolls Background accordingly.

        if (isMovingRight() || isMovingLeft()) {
            // move right
            centerX += speedX;
        }

//        // Updates Y Position
//        centerY += speedY;
//
//        // Handles Jumping
//        speedY += 1;
//
//        if (speedY > 3){
//            jumped = true;
//        }

        // Prevents going beyond X coordinate of 0
//        if (centerX + speedX <= 60) {
//            centerX = 61;
//        }
    }

    private void jump() {
        if (jumped == false) {
            speedY = JUMP_SPEED;
            jumped = true;
        }
    }

    private void moveRight() {
        if (ducked == false) {
            speedX = MOVE_SPEED * screenDensityRatio;
            setMovingRight(true);
        }
    }

    private void moveLeft() {
        if (ducked == false) {
            speedX = -MOVE_SPEED * screenDensityRatio;
            setMovingLeft(true);
        }
    }

    private void stopRight() {
        setMovingRight(false);
        stop();
    }

    private void stopLeft() {
        setMovingLeft(false);
        stop();
    }

    private void stop() {
        setMovingLeft(false);
        setMovingRight(false);
        speedX = 0;
//        if (isMovingRight() == false && isMovingLeft() == false) {
//            speedX = 0;
//        }
//
//        if (isMovingRight() == false && isMovingLeft() == true) {
//            moveLeft();
//        }
//
//        if (isMovingRight() == true && isMovingLeft() == false) {
//            moveRight();
//        }
    }

    private void pause(){
        stopLeft();
        stopRight();
    }

    private boolean isMovingRight() {
        return movingRight;
    }

    private void setMovingRight(boolean movingRight) {
        this.movingRight = movingRight;
    }

    private boolean isMovingLeft() {
        return movingLeft;
    }

    private void setMovingLeft(boolean movingLeft) {
        this.movingLeft = movingLeft;
    }

    public boolean isJumped() {
        return jumped;
    }

    public void setJumped(boolean jumped) {
        this.jumped = jumped;
    }

    public int getCenterX() {
        return centerX;
    }

    public int getCenterY() {
        return centerY;
    }

    public void handleAction(final int action) {
        switch (action) {
            case PAUSE:
                pause();
                break;
            case MOVE_RIGHT:
                moveRight();
                break;
            case MOVE_LEFT:
                moveLeft();
                break;
            case STOP_LEFT:
                stopLeft();
                break;
            case STOP_RIGHT:
                stopRight();
                break;
            case STOP_BOTH:
                stop();
                break;
            case JUMP:
                jump();
                break;
        }
    }

//    public ArrayList getProjectiles() {
//        return projectiles;
//    }
}
