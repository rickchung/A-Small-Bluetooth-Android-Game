package com.wanlin.androidgame.pikachuvolleyball;

/**
 * Created by rickchung on 6/9/15.
 */
public class Volleyball {
    private static final String LOG_TAG = "Volleyball";
    private static final double GRAV_ACC = 1.0;
    private int x;
    private int y;
    private int objWidth;
    private int objHeight;
    private int objRadius;
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

    public boolean canRebound(int x, int y, int width, int height) {
        // Left & Right (use x and width)
        // Up & Down (use y and height)

        return false;
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


    public boolean detectCollision(int x, int y, int radius) {
        int dx = this.x - x;
        int dy = this.y - y;
        double distance = Math.sqrt(dx*dx + dy*dy);

        if (distance < this.objRadius + radius) {
            // Collision detected, change speed here
            return true;
        }
        return false;
    }

    public void boundVertically() {
        speedY = -speedY;
    }

    public void boundHorizontally(){
        speedX = -speedX;
    }
}
