package com.wanlin.androidgame.pikachuvolleyball;

/**
 * Created by rickchung on 6/9/15.
 */
public class Volleyball {
    private static final String LOG_TAG = "Volleyball";
    private static final double GRAV_ACC = 5;
    private int x;
    private int y;
    private int centerX;
    private int centerY;
    private int width;
    private int height;
    private int radius;
    private double speedX;
    private double speedY;

    Volleyball(int x, int y) {
        this.x = x;
        this.y = y;
        this.speedX = 5.0; // 5.0 for test
        this.speedY = 5.0;
    }

    /**
     * Update status
     */
    public void update() {
        // Update the current position
        x += speedX;
        y += speedY;
        // Update speedY with gravitational acceleration
        speedY += GRAV_ACC;
    }

    public boolean detectCollision(int x, int y, int radius) {
        int dx = this.centerX - x;
        int dy = this.centerY - y;
        double distance = Math.sqrt(dx*dx + dy*dy);

        return ( distance <= this.radius + radius );
    }


    /*========== Getters And Setters ==========*/
    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getCenterY() {
        return centerY;
    }

    public void setCenterY(int centerY) {
        this.centerY = centerY;
    }

    public int getCenterX() {
        return centerX;
    }

    public void setCenterX(int centerX) {
        this.centerX = centerX;
    }

    public double getSpeedX() {
        return speedX;
    }

    public void setSpeedX(double speedX) {
        this.speedX = speedX;
    }

    public double getSpeedY() {
        return speedY;
    }

    public void setSpeedY(double speedY) {
        this.speedY = speedY;
    }

    public void updateSpeed(int x, int y, int speedX, int speedY) {
        // TODO For test here
        boundHorizontally();
        boundVertically();
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void boundVertically() {
        speedY = -speedY;
    }

    public void boundHorizontally(){
        speedX = -speedX;
    }
}