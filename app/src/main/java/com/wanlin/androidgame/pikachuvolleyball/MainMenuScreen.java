package com.wanlin.androidgame.pikachuvolleyball;

import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;

import com.kilobolt.framework.Game;
import com.kilobolt.framework.Graphics;
import com.kilobolt.framework.Input;
import com.kilobolt.framework.Screen;

import java.util.List;

/**
 * Created by wanlin on 15/6/4.
 */
public class MainMenuScreen extends Screen {
    private final String LOG_TAG = "MainMenuScreen";
    private static String bluetoothMsg = "Hello!";
    private static final int textSize = 50;
    Paint paint;
    Point screenSizePoint;

    public MainMenuScreen(Game game) {
        super(game);

        // Get Point for screen size
        screenSizePoint = ((PikachuVolleyball)game).getSizePoint();
        Assets.bgMusic.play();

        paint = new Paint();

        paint.setTextSize(textSize);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
    }

    /**
     * when the user touches and releases inside the square with side length 250 with a corner
     * at (0, 0), we would call the: game.setScreen(new GameScreen(game));
     *
     * @param deltaTime
     */
    @Override
    public void update(float deltaTime) {
//        Graphics g = game.getGraphics();
        List<Input.TouchEvent> touchEvents = game.getInput().getTouchEvents();

        int len = touchEvents.size();
        for (int i = 0; i < len; i++) {
            Input.TouchEvent event = touchEvents.get(i);
            if (event.type == Input.TouchEvent.TOUCH_UP) {

                if (bluetoothMsg == "Successful MSG") {
                    if (inBounds(event, (screenSizePoint.x - Assets.startButton.getWidth()) / 2,
                            (screenSizePoint.y - Assets.startButton.getHeight()) / 2,
                            Assets.startButton.getWidth(),
                            Assets.startButton.getHeight())) {
                        //START GAME
                        Log.d(LOG_TAG, "Start Game");
                        startGame();
                    }
                }

                else if(bluetoothMsg == "FIND DEVICES") {
                    for (int j = 0; j < ((PikachuVolleyball) game).getFoundDevices().size(); j++) {
                        if (inBounds(event, (screenSizePoint.x - Assets.startButton.getWidth()) / 2,
                                j*Assets.startButton.getHeight(),
                                Assets.startButton.getWidth(), Assets.startButton.getHeight())) {
                            BluetoothDevice btDevice = ((PikachuVolleyball) game).getFoundDevices().get(j);
                            Log.e(LOG_TAG, "touch on: " + btDevice.getName() + " " + btDevice.getAddress());

                            // Connect to the remote device
                            ((PikachuVolleyball) game).getBtModule().btConnectAsClient(btDevice);
                        }
                    }
                }

                if (inBounds(event, 0, 0, Assets.makeDiscoverableBt.getWidth(), Assets.makeDiscoverableBt.getHeight())) {
                    // MAKE DISCOVERABLE
                    bluetoothMsg = "MAKE DISCOVERABLE";
                    Log.e(LOG_TAG, "MAKE DISCOVERABLE");
                    ((PikachuVolleyball) game).getBtModule().btMakeDiscoverable();
                }

                if (inBounds(event, 0, 40 + Assets.findDevicesBt.getHeight(), Assets.findDevicesBt.getWidth(), Assets.findDevicesBt.getHeight())) {
                    // FIND DEVICES
                    bluetoothMsg = "FIND DEVICES";
                    Log.e(LOG_TAG, "FIND DEVICES");
                    ((PikachuVolleyball) game).getBtModule().btFindDevices();
                }

//                if (inBounds(event, 0, (20 + Assets.startButton.getHeight())*2, Assets.startButton.getWidth(), Assets.startButton.getHeight())) {
//                    // CONNECT TO...
//                    bluetoothMsg = "CONNECT TO...";
//                    Log.e(LOG_TAG, "CONNECT TO...");
//                }
            }
        }

        paint(10);
        if (bluetoothMsg == "CONNECT TO...")
            bluetoothMsg = "Successful MSG";
    }

    /**
     * Used to create rectangles with coordinates (x, y, x2, y2).
     * We use this to create regions in the screen that we can touch to interact with the game.
     */
    private boolean inBounds(Input.TouchEvent event, int x, int y, int width, int height) {
        if (event.x > x && event.x < x + width - 1 && event.y > y
                && event.y < y + height - 1)
            return true;
        else
            return false;
    }

    @Override
    public void paint(float deltaTime) {
        Graphics g = game.getGraphics();
        g.drawImage(Assets.menuBgImage, 0, 0); // Draw bg image
        g.drawImage(Assets.makeDiscoverableBt, 0, 0); // Draw make discoverable bt image
        g.drawImage(Assets.findDevicesBt, 0, 40 + Assets.findDevicesBt.getHeight()); // Draw find devices bt image

        if (bluetoothMsg == "Successful MSG"){
            g.drawImage(Assets.startButton,
                    (screenSizePoint.x - Assets.startButton.getWidth())/2,
                    (screenSizePoint.y - Assets.startButton.getHeight())/2);
        }

        // draw bluetooth devices string
        else if (bluetoothMsg == "FIND DEVICES") {
            for (int i = 0; i < ((PikachuVolleyball) game).getFoundDevices().size(); i++) {
                BluetoothDevice btDevice = ((PikachuVolleyball) game).getFoundDevices().get(i);
                g.drawString(btDevice.getName() + " " + btDevice.getAddress(), screenSizePoint.x / 2, (textSize + 30) * (i + 1), paint);
            }
        }

        else {
//            g.drawARGB(155, 0, 0, 0);
            g.drawString(bluetoothMsg, screenSizePoint.x / 2, 0, paint);
        }
    }

    @Override
    public void pause() {
        Assets.bgMusic.pause();
    }

    @Override
    public void resume() {
        Assets.bgMusic.play();
    }

    @Override
    public void dispose() {
        Assets.bgMusic.pause();
    }

    @Override
    public void backButton() {
        //Display "Exit Game?" Box

    }

    public void startGame() {
        game.setCurScreenType(PikachuVolleyball.TYPE_SCREEN_GAME);
        game.setScreen(new GameScreen(game));
    }
}
