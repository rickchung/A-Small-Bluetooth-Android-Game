package com.wanlin.androidgame.pikachuvolleyball;

/**
 * Created by rickchung on 6/9/15.
 */
public class Volleyball {
    private static final String LOG_TAG = "Volleyball";
    private static final double GRAV_ACC = 2.0;
    private int x;
    private int y;
    private int centerX;
    private int centerY;
    private int width;
    private int height;
    private int halfWidth;
    private int halfHeight;
    private int radius;
    private double speedX;
    private double speedY;

    Volleyball(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height= height;
        this.halfWidth  = width/2;
        this.halfHeight = height/2;
        this.centerX = this.x + halfWidth;
        this.centerY = this.y + halfHeight;
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
        centerX = x+halfWidth;
        centerY = y+halfHeight;
        // Update speedY with gravitational acceleration
        speedY += GRAV_ACC;
    }

    public boolean detectCollision(int x, int y, int radius) {
        int dx = this.centerX - x;
        int dy = this.centerY - y;
        double distance = Math.sqrt(dx*dx + dy*dy);

        return ( distance <= this.radius + radius );
    }

    public void updateSpeed(int x, int y, int speedX, int speedY) {
//        this.speedY = this.speedY * -1 + speedY;
//        this.speedX = this.speedX * -1 + speedX;
    }

    public void boundVertically() {
        speedY = -speedY;
    }

    public void boundHorizontally(){
        speedX = -speedX;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
        // update centerXY
        this.centerX = x + halfWidth;
        this.centerY = y + halfHeight;
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

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }
}
