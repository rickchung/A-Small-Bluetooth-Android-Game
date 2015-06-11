package com.wanlin.androidgame.pikachuvolleyball;

import android.graphics.Point;

/**
 * Created by wanlin on 15/6/5.
 */
public class Pikachu {
    final String LOG_TAG = "Pikachu";
    float JUMP_SPEED = -40.0f;
    float MOVE_SPEED = 12.0f;

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
    private int x;
    private int y;
    private int jumpStartY;
    private float screenDensityRatioX;
    private float screenDensityRatioY;
    private int radius;
    private int width, halfwidth;
    private int height, halfheight;


    Pikachu(int x, int y, Point sizePoint, int width, int height) {
        this.x = x;
        this.y = y;
        screenDensityRatioX = sizePoint.x / NUM_MOVING_SLOTS;
        screenDensityRatioY = sizePoint.y / NUM_MOVING_SLOTS;
        jumped = false;

        this.width = width;
        this.height = height;
        this.halfwidth = width/2;
        this.halfheight = height/2;

        radius = halfwidth;
        centerX = this.x + halfwidth;
        centerY = this.y + halfheight;
    }

    public void update() {
        // Moves Character or Scrolls Background accordingly.

        if (isMovingRight() || isMovingLeft()) {
            // move right
            x += speedX;
            centerX += speedX;
        }

        if (jumped) {
            // Updates Y Position
            y += speedY;
            centerY += speedY;
            speedY += 4;
        }
        if (y >= jumpStartY) {
            jumped = false;
            speedY = 0;
        }
    }

    private void jump() {
        if (jumped == false) {
            jumpStartY = y;
            speedY = JUMP_SPEED * screenDensityRatioY;
            jumped = true;
        }
    }

    private void moveRight() {
        if (ducked == false) {
            speedX = MOVE_SPEED * screenDensityRatioX;
            setMovingRight(true);
        }
    }

    private void moveLeft() {
        if (ducked == false) {
            speedX = -MOVE_SPEED * screenDensityRatioX;
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
    }

    private void pause() {
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

    public boolean isOnTheGround() {
        if (y >= jumpStartY) return true;
        else return false;
    }

    public void setJumped(boolean jumped) {
        this.jumped = jumped;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getCenterX() {
        return centerX;
    }

    public void setCenterX(int centerX) {
        this.centerX = centerX;
    }

    public int getCenterY() {
        return centerY;
    }

    public void setCenterY(int centerY) {
        this.centerY = centerY;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getRadius() {
        return radius;
    }

    public float getSpeedX() {
        return speedX;
    }

    public void setSpeedX(float speedX) {
        this.speedX = speedX;
    }

    public float getSpeedY() {
        return speedY;
    }

    public void setSpeedY(float speedY) {
        this.speedY = speedY;
    }

    public void setPosition(int x, int y, boolean isJumped) {
        this.x = x;
        this.y = y;
        this.jumped = isJumped;
        if (isJumped) {
            jumpStartY = this.y;
        }
        // Update centerXY
        this.centerX = this.x + halfwidth;
        this.centerY = this.y + halfheight;
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
}
