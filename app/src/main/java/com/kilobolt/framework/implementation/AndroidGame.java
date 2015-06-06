package com.kilobolt.framework.implementation;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;

import com.com.androidgame.rick.bluetooth.BluetoothModule;
import com.com.androidgame.rick.bluetooth.HandlerMessageCallback;
import com.kilobolt.framework.Audio;
import com.kilobolt.framework.FileIO;
import com.kilobolt.framework.Game;
import com.kilobolt.framework.Graphics;
import com.kilobolt.framework.Input;
import com.kilobolt.framework.Screen;
import com.pikachu.volleyball.GameScreen;
import com.pikachu.volleyball.MainMenuScreen;

import java.util.ArrayList;

public abstract class AndroidGame extends Activity implements Game, HandlerMessageCallback {
    AndroidFastRenderView renderView;
    Graphics graphics;
    Audio audio;
    Input input;
    FileIO fileIO;
    Screen screen;
    WakeLock wakeLock;

    private static final String LOG_TAG = "AndroidGame";
    public static final int TYPE_SCREEN_GAME = 0;
    public static final int TYPE_SCREEN_MENU = 1;
    private int currentSreentType;
    private boolean isHost = true;

    private BluetoothModule btModule;
    private BluetoothModule.BtListAdapter btDevicesListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        int frameBufferWidth = isPortrait ? 800: 1280;
        int frameBufferHeight = isPortrait ? 1280: 800;
        Bitmap frameBuffer = Bitmap.createBitmap(frameBufferWidth,
                frameBufferHeight, Config.RGB_565);
        
        float scaleX = (float) frameBufferWidth
                / getWindowManager().getDefaultDisplay().getWidth();
        float scaleY = (float) frameBufferHeight
                / getWindowManager().getDefaultDisplay().getHeight();

        renderView = new AndroidFastRenderView(this, frameBuffer);
        graphics = new AndroidGraphics(getAssets(), frameBuffer);
        fileIO = new AndroidFileIO(this);
        audio = new AndroidAudio(this);
        input = new AndroidInput(this, renderView, scaleX, scaleY);
        screen = getInitScreen();

        setCurScreenType(TYPE_SCREEN_MENU);

        setContentView(renderView);
        
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "MyGame");

        // Init Bluetooth
        Log.d(LOG_TAG, "Trying to init Bluetooth");
        btModule = new BluetoothModule(this, this);
        ListView btDevicesListView = new ListView(getApplicationContext());
        ListView btMsgListView = new ListView(getApplicationContext());
        btDevicesListAdapter =  btModule.bindBtDevicesAdapter(btDevicesListView);
        btModule.bindMsgAdapter(btMsgListView, android.R.layout.simple_list_item_1);
    }

    @Override
    public void onResume() {
        super.onResume();
        wakeLock.acquire();
        screen.resume();
        renderView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        wakeLock.release();
        renderView.pause();
        screen.pause();

        if (isFinishing())
            screen.dispose();
    }

    @Override
    public Input getInput() {
        return input;
    }

    @Override
    public FileIO getFileIO() {
        return fileIO;
    }

    @Override
    public Graphics getGraphics() {
        return graphics;
    }

    @Override
    public Audio getAudio() {
        return audio;
    }

    @Override
    public void setScreen(Screen screen) {
        if (screen == null)
            throw new IllegalArgumentException("Screen must not be null");

        this.screen.pause();
        this.screen.dispose();
        screen.resume();
        screen.update(0);
        this.screen = screen;
    }
    
    public Screen getCurrentScreen() {

    	return screen;
    }


    @Override
    public void setCurScreenType(int type) {
        currentSreentType = type;
    }
    @Override
    public int getCurScreenType() {
        return currentSreentType;
    }

    public BluetoothModule getBtModule() {
        return btModule;
    }

    public ArrayList<BluetoothDevice> getFoundDevices() {
        ArrayList<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
        int size = btDevicesListAdapter.getCount();

        for (int i = 0; i < size; i++) {
            BluetoothDevice btd = (BluetoothDevice)btDevicesListAdapter.getItem(i);
            devices.add(btd);
        }
        return devices;
    }

    public boolean isHost() {
        return this.isHost;
    }

    @Override
    public void msgCallback(Message msg) {
        String strMsg;
        switch (msg.what) {
            case BluetoothModule.SERVERSOCK_THREAD_WHAT:
                strMsg = msg.getData().getString(BluetoothModule.SERVERSOCK_MSG_KEY);
                Log.d(LOG_TAG, "Got message: " + strMsg);
                if (strMsg == BluetoothModule.RESUlT_CONN_OK) {
                    if (getCurScreenType() == TYPE_SCREEN_MENU) {
                        isHost = true;
                        ((MainMenuScreen) screen).startGame();
                    }
                }
                break;
            case BluetoothModule.CLIENTSOCK_THREAD_WHAT:
                strMsg = msg.getData().getString(BluetoothModule.CLIENTSOCK_MSG_KEY);
                Log.d(LOG_TAG, "Got message: " + strMsg);
                if (strMsg == BluetoothModule.RESUlT_CONN_OK) {
                    if (getCurScreenType() == TYPE_SCREEN_MENU) {
                        isHost = false;
                        ((MainMenuScreen) screen).startGame();
                    }
                }
                break;
            case BluetoothModule.RECEIVER_THREAD_WHAT:
                strMsg = msg.getData().getString(BluetoothModule.RECEIVER_MSG_KEY);
                Log.d(LOG_TAG, "Got message: " + strMsg);
                if (getCurScreenType() == TYPE_SCREEN_GAME) {
                    int controlCmd = Integer.parseInt(strMsg);
                    if (controlCmd == GameScreen.STOP_MOVING) {
                        ((GameScreen) screen).pause();
                    }
                    else if (controlCmd == GameScreen.YOU_GOOD_TO_GO) {
                        ((GameScreen) screen).resume();
                    }
                    else if (controlCmd == GameScreen.START_THAT_FUKING_GAMEEEE) {
                        ((GameScreen) screen).stargGame();
                    }
                    else {
                        ((GameScreen) screen).getEnemy().handleAction(controlCmd);
                    }
                }
                break;
            case BluetoothModule.SYS_MSG_WHAT:
                strMsg = msg.getData().getString(BluetoothModule.SYS_MSG_KEY);
                Log.d(LOG_TAG, "Got message: " + strMsg);
            default:
                Log.d(LOG_TAG, "Message error");
                strMsg = "Message error";
                break;
        }
    }
}
