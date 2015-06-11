package com.wanlin.androidgame.pikachuvolleyball;

/**
 * Created by rickchung on 6/9/15.
 */
public class Volleyball {
    private static final String LOG_TAG = "Volleyball";
    private static final double GRAV_ACC = 3.0;
    private static final double BOUND_ACC = 60.0;
    private static final double START_SPEED = 5.0;
    private static final double BOUND_ACC_Y = 65.0;
    private static final double BOUND_ACC_ATTACK = 130.0;
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
        this.speedX = START_SPEED;
        this.speedY = START_SPEED;
        this.radius = halfWidth;
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

    public void updateSpeed(int ox, int oy) {
        // Update speedX
        int mx = this.centerX;
        int my = this.centerY;

        double costheta =
                ((-1*ox) * (mx-ox) + 0) / (Math.sqrt((mx-ox)*(mx-ox) + (my-oy)*(my-oy)) * ox);
        double sintheta = Math.sqrt(1.0 - (costheta*costheta));

        speedX = -1 * BOUND_ACC_Y * costheta;

        // Update speedY
        speedY = -1 * BOUND_ACC_Y * sintheta;
    }

    public void updateSpeed(int ox, int oy, boolean isAttacking) {
        // Update speedX
        int mx = this.centerX;
        int my = this.centerY;

        double costheta =
                ((-1*ox) * (mx-ox) + 0) / (Math.sqrt((mx-ox)*(mx-ox) + (my-oy)*(my-oy)) * ox);
        double sintheta = Math.sqrt(1.0 - (costheta*costheta));


        if (isAttacking) {
            speedX = -1 * BOUND_ACC_ATTACK * costheta;
            // Update speedY
            speedY = -1 * BOUND_ACC_ATTACK * sintheta;
        }
        else {
            speedX = -1 * BOUND_ACC_Y * costheta;
            // Update speedY
            speedY = -1 * BOUND_ACC_Y * sintheta;
        }
    }

    public void boundVertically() {
        speedY = speedY > 0 ? -1 * BOUND_ACC : BOUND_ACC;
    }

    public void boundHorizontally(){
        speedX = speedX > 0 ? -1 * BOUND_ACC : BOUND_ACC;
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

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getHalfWidth() {
        return halfWidth;
    }

    public void setHalfWidth(int halfWidth) {
        this.halfWidth = halfWidth;
    }

    public int getHalfHeight() {
        return halfHeight;
    }

    public void setHalfHeight(int halfHeight) {
        this.halfHeight = halfHeight;
    }
}
