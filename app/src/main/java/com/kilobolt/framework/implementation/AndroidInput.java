package com.kilobolt.framework.implementation;

import android.content.Context;
import android.os.Build.VERSION;
import android.view.MotionEvent;
import android.view.View;

import com.kilobolt.framework.Input;

import java.util.List;

public class AndroidInput implements Input {
    private static final int ACTION_TYPE_UP = 1;
    private static final int ACTION_TYPE_RIGHT = 2;
    private static final int ACTION_TYPE_DOWN = 3;
    private static final int ACTION_TYPE_LEFT = 4;
    private static final int SLIDE_RANGE = 100;

    private float mTouchStartPointX;
    private float mTouchStartPointY;

    private int mActionType;

    TouchHandler touchHandler;

    public AndroidInput(Context context, View view, float scaleX, float scaleY) {
        if (Integer.parseInt(VERSION.SDK) < 5)
            touchHandler = new SingleTouchHandler(view, scaleX, scaleY);
        else {
            touchHandler = new MultiTouchHandler(view, scaleX, scaleY);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchStartPointX = event.getRawX();
                mTouchStartPointY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (mTouchStartPointX - x > SLIDE_RANGE) {
                    mActionType = ACTION_TYPE_LEFT;
                } else if (x - mTouchStartPointX > SLIDE_RANGE) {
                    mActionType = ACTION_TYPE_RIGHT;
                } else if (mTouchStartPointY - y > SLIDE_RANGE) {
                    mActionType = ACTION_TYPE_UP;
                } else if (y - mTouchStartPointY > SLIDE_RANGE) {
                    mActionType = ACTION_TYPE_DOWN;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mActionType == ACTION_TYPE_UP) {
                    slideUp();
                } else if (mActionType == ACTION_TYPE_RIGHT) {
                    slideToRight();
                } else if (mActionType == ACTION_TYPE_DOWN) {
                    slideDown();
                } else if (mActionType == ACTION_TYPE_LEFT) {
                    slideToLeft();
                }
                break;
            default:
                break;
        }
        return true;
    }

    protected void slideToLeft() {
        // slide to left
    }

    protected void slideToRight() {
        // slide to right
    }

    protected void slideUp() {
        // slide up
    }

    protected void slideDown() {
        // slide down
    }

    @Override
    public boolean isTouchDown(int pointer) {
        return touchHandler.isTouchDown(pointer);
    }

    @Override
    public int getTouchX(int pointer) {
        return touchHandler.getTouchX(pointer);
    }

    @Override
    public int getTouchY(int pointer) {
        return touchHandler.getTouchY(pointer);
    }


    @Override
    public List<TouchEvent> getTouchEvents() {
        return touchHandler.getTouchEvents();
    }
    
}
