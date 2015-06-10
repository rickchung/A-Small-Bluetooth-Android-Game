package com.rick.androidgame.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

/**
 * Created by rickchung on 6/5/15.
 */
public class BluetoothModule {
    private final String LOG_TAG = "BluetoothModule";
    private final String SERVICE_NAME = "BT_PiKaChu";
    private final UUID SERVICE_UUID = UUID.fromString("821897b0-0ae0-11e5-b939-0800200c9a66");
    private final String CHAR_SET = "UTF-8";
    public static final int REQUEST_ENABLE_BIT = 1;
    public static final int SERVERSOCK_THREAD_WHAT = 0;
    public static final int CLIENTSOCK_THREAD_WHAT = 1;
    public static final int RECEIVER_THREAD_WHAT = 2;
    public static final int SYS_MSG_WHAT = 3;
    public static final String SERVERSOCK_MSG_KEY = "serversocket_thread_msg";
    public static final String CLIENTSOCK_MSG_KEY = "clientsocket_thread_msg";
    public static final String RECEIVER_MSG_KEY = "clientsocket_thread_msg";
    public static final String SYS_MSG_KEY = "system_msg";
    public static final String RESUlT_CONN_OK = "connection_OK";
    // Application context
    private final Activity app;
    // Bluetooth Adapter
    private BluetoothAdapter btAdapter;
    // Adapter for storing data
    private ArrayAdapter<String> testMsgAdapter;
    private BtListAdapter btListAdapter;
    Set<BluetoothDevice> btPairedDevices;
    // Message handler for Threads
    private Handler msgHandler;
    // Thread & Messages
    private BtAcceptAsServerThread serverSocketThread;
    private BtConnectAsClientThread connectClientSocketThread;
    private BtMessageReceiverThread messageReceiverThread;
    private BtMessageSender messageSender;


    // Create a BroadcastReceiver for ACTION_FOUND (used to enable Bluetooth discovery function)
    private final BroadcastReceiver btBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get Bluetooth devices from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and the address to an array adapter and update it
                Log.d(LOG_TAG, device.getName() + ": " + device.getAddress());
                btListAdapter.add(device);
                btListAdapter.updateAdapter();
            }
        }
    };

    /* ========== Public Interfaces ========== */

    public BluetoothModule(Activity app, HandlerMessageCallback hmc) {
        this.app = app;

        // Set Handler
        msgHandler = new BtThreadHandler(hmc);

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        app.registerReceiver(btBroadcastReceiver, filter);

        // Init Bluetooth and start server socket
        btInit();
        // btConnectAsServer();
    }

    /**
     * Set the adapter of listView with predefined Bluetooth devices list adapter
     * @param listView
     * @return the predefined adapter
     */
    public BtListAdapter bindBtDevicesAdapter(ListView listView) {
        btListAdapter = new BtListAdapter();
        listView.setAdapter(btListAdapter);
        return btListAdapter;
    }

    public BtListAdapter getBtDevicesAdapter() {
        return btListAdapter;
    }

    /**
     * Set the adapter of listView with the default messages ArrayAdapter
     * @param listView
     * @param listViewItemLayoutId
     * @return the default message ArrayAdapter
     */
    public ArrayAdapter<String> bindMsgAdapter(ListView listView, int listViewItemLayoutId) {
        testMsgAdapter = new ArrayAdapter<String>(app.getApplicationContext(), listViewItemLayoutId);
        listView.setAdapter(testMsgAdapter);
        return testMsgAdapter;
    }

    public boolean hasMessageSender() {
        if (messageSender != null) return true;
        else return false;
    }

    public void sendMessage(String message) {
        messageSender.send(message);
        // TODO Test sent messages list
        testMsgAdapter.add(message);
        testMsgAdapter.notifyDataSetChanged();
    }

    public void cancelBtActivities() {
        // Interrupt the waiting server socket
        if (serverSocketThread != null) {
            serverSocketThread.cancel();
            if (!serverSocketThread.isInterrupted()) {
                Log.d(LOG_TAG, "Interrupt the waiting server socket thread");
                serverSocketThread.interrupt();
            }
        }
        if (connectClientSocketThread != null) {
            connectClientSocketThread.cancel();
            if (!connectClientSocketThread.isInterrupted()) {
                Log.d(LOG_TAG, "Interrupt the connecting client socket");
                connectClientSocketThread.interrupt();
            }
        }
        if (messageReceiverThread != null) {
            messageReceiverThread.cancel();
            if (!messageReceiverThread.isInterrupted()) {
                Log.d(LOG_TAG, "Interrupt the receiver thread");
                messageReceiverThread.interrupt();
            }
        }
        // Cancel the sender
        if (messageSender != null) {
            messageSender.cancel();
        }
    }

    public void unRegisterBroadcastReceiver() {
        app.unregisterReceiver(btBroadcastReceiver);
    }

    /**
     * Init Bluetooth Adapter
     */
    public void btInit() {
        // Get the Bluetooth controller
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            // This device does not support Bluetooth
        }
        else {
            // Check if the Bluetooth is enabled
            if (!btAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                app.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BIT);
            }
            else {
                btOKCallback();
            }
        }
    }

    /**
     * Bluetooth discovery
     */
    public void btFindDevices() {
        // Clear views
        btListAdapter.clear();
        btListAdapter.updateAdapter();

        // Start Bluetooth discovery
        Log.d(LOG_TAG, "Async devices discovery...");
        Bundle msgBundle = new Bundle();
        msgBundle.putString(SYS_MSG_KEY, "Async devices discovery...");
        Message msg = new Message();
        msg.what = SYS_MSG_WHAT;
        msg.setData(msgBundle);
        msgHandler.sendMessage(msg);

        // For some reasons, try cancel first and start again
        btAdapter.cancelDiscovery();
        btAdapter.startDiscovery();
    }

    /**
     * Get the list of already known devices
     */
    public void btGetKnownDevices() {
        // Clear views
        btListAdapter.clear();
        btListAdapter.updateAdapter();

        // Querying paired devices
        btPairedDevices = btAdapter.getBondedDevices();
        // If there are paired devices
        if (btPairedDevices.size() > 0) {
            for (BluetoothDevice device : btPairedDevices) {
                btListAdapter.add(device);
            }
        }
        else {
            Log.d(LOG_TAG, "No paired devices");
        }
    }

    /**
     * Callback if everything is OK after btInit
     */
    public String btOKCallback() {
        String message = "Bluetooth is OK";
        Log.d(LOG_TAG, message);
        // Start server thread
        btConnectAsServer();
        return message;
    }

    /**
     * Callback if there is an error after btInit
     */
    public String btErrorCallback() {
        String message = "Bluetooth IS NOT OK";
        Log.d(LOG_TAG, message);
        return message;
    }

    /**
     * Make the device discoverable
     */
    public void btMakeDiscoverable() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        app.startActivity(discoverableIntent);
    }

    /**
     * Fire thread and let BluetoothServerSocket accept other device
     */
    public void btConnectAsServer() {
        if (serverSocketThread != null && !serverSocketThread.isInterrupted()) {
            // ServerThread has already been accepting others
            serverSocketThread.cancel();
            serverSocketThread.interrupt();
        }
        serverSocketThread = new BtAcceptAsServerThread();
        serverSocketThread.start();
    }

    /**
     * Fire thread and try to connect to other Bluetooth device
     */
    public void btConnectAsClient(BluetoothDevice btDevice) {
        if (btDevice == null) {
            Log.d(LOG_TAG, "[btConnectAsClient] btDevice is null");
        }
        else {
            if (connectClientSocketThread != null && !connectClientSocketThread.isInterrupted()) {
                // Client socket has been trying to connect
                connectClientSocketThread.cancel();
                connectClientSocketThread.interrupt();
            }
            connectClientSocketThread = new BtConnectAsClientThread(btDevice);
            connectClientSocketThread.start();
        }
    }

    public String getBtDeviceName() {
        return btAdapter.getName();
    }

    private void btManageConnection(BluetoothSocket btSocket) {
        // Start receiver thread
        messageReceiverThread = new BtMessageReceiverThread(btSocket);
        messageReceiverThread.start();
        Log.d(LOG_TAG, "Message Receiver starts receiving");
        // Init sender object
        messageSender = new BtMessageSender(btSocket);
        Log.d(LOG_TAG, "Sender OK");
    }


    /* ========== User-defined Classes ========== */

    /**
     * List adapter for ListView to show found devices
     */
    public class BtListAdapter extends BaseAdapter {
        public ArrayList<BluetoothDevice> btDevicesList;
        public BluetoothDevice clickedDevice;

        public BtListAdapter() {
            btDevicesList = new ArrayList<BluetoothDevice>();
            clickedDevice = null;
        }

        public void add(BluetoothDevice device) {
            btDevicesList.add(device);
        }

        public void clear() {
            btDevicesList.clear();
        }

        public void updateAdapter() {
            notifyDataSetChanged();
        }

        public void setClickedDevice(BluetoothDevice btDevice) {
            clickedDevice = btDevice;
        }
        public BluetoothDevice getClickedBtDevice() {
            return clickedDevice;
        }

        @Override
        public int getCount() {
            return btDevicesList.size();
        }

        @Override
        public Object getItem(int position) {
            return btDevicesList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            BluetoothDevice btDevice = btDevicesList.get(position);
            TextView textView = new TextView(app.getApplicationContext());
            textView.setText(btDevice.getName() + ": " + btDevice.getAddress());
            return textView;
        }
    }

    /**
     * Thread use to accept connection through BluetoothServerSocket
     */
    public class BtAcceptAsServerThread extends Thread {
        public final BluetoothServerSocket btServerSocket;

        public BtAcceptAsServerThread() {
            BluetoothServerSocket tmpSocket = null;

            try {
                Log.d(LOG_TAG, "[BtAcceptAsServerThread] Get a BluetoothServerSocket");
                tmpSocket = btAdapter.listenUsingRfcommWithServiceRecord(SERVICE_NAME, SERVICE_UUID);
            }
            catch (IOException e) {
                Log.d(LOG_TAG, "[BtAcceptAsServerThread] IOException on BluetoothServerSocket creation");
            }

            btServerSocket = tmpSocket;
        }

        @Override
        public void run() {
            BluetoothSocket btSocket = null;

            while (true) {
                try {
                    Log.d(LOG_TAG, "[BtAcceptAsServerThread] Listening and waiting for socket...");
                    btSocket = btServerSocket.accept();
                }
                catch (IOException e) {
                    Log.d(LOG_TAG, "[BtAcceptAsServerThread] IOException");
                    break;
                }

                if (btSocket != null) {
                    Log.d(LOG_TAG, "[BtAcceptAsServerThread] Connection established!");
                    // updateUI("Connection established with: " + btSocket.getRemoteDevice().getName());
                    updateUI(RESUlT_CONN_OK);

                    // Manage connection
                    btManageConnection(btSocket);

                    try {
                        // Close the useless BluetoothServerSocket
                        btServerSocket.close();
                    }
                    catch (IOException e) {
                        Log.d(LOG_TAG, "[BtAcceptAsServerThread] IOException when .close()");
                    }
                }
            }
        }

        // Cancel the listening socket and cause the thread to finish
        public void cancel() {
            try {
                btServerSocket.close();
            }
            catch (IOException e) {
                Log.d(LOG_TAG, "[BtAcceptThread] IOException when .close()");
            }
        }

        public void updateUI(String msgString) {
            Bundle msgBundle = new Bundle();
            msgBundle.putString(SERVERSOCK_MSG_KEY, msgString);
            Message msg = new Message();
            msg.what = SERVERSOCK_THREAD_WHAT;
            msg.setData(msgBundle);

            msgHandler.sendMessage(msg);
        }
    }

    /**
     * Thread used to connect another Bluetooth device
     */
    public class BtConnectAsClientThread extends Thread {
        public final BluetoothSocket btSocket;
        public final BluetoothDevice btDevice;

        public BtConnectAsClientThread(BluetoothDevice device) {
            BluetoothSocket tmpSocket = null;
            btDevice = device;

            // Get a BluetoothSocket for connection with the given device
            try {
                Log.d(LOG_TAG, "[BtConnectAsClientThread] Get a BluetoothSocket for connection with the given device");
                tmpSocket = btDevice.createRfcommSocketToServiceRecord(SERVICE_UUID);
            }
            catch (IOException e) {
                Log.d(LOG_TAG, "[BtConnectAsClientThread] IOException occurred on socket creation");
            }

            btSocket = tmpSocket;
        }

        @Override
        public void run() {
            // Cancel discovery because it will slow down the connection
            Log.d(LOG_TAG, "[BtConnectAsClientThread] Cancel discovery for efficiency concern");
            btAdapter.cancelDiscovery();

            try {
                // Connect the remote device through the socket
                Log.d(LOG_TAG, "[BtConnectAsClientThread] Try to connect with the device...");

                // Send message to the UI thread
                updateUI("Trying to connect to " + btDevice.getName());

                // Connect
                btSocket.connect();
            }
            catch (IOException connectExp) {
                // Unable to connect. Get out immediately
                try {
                    updateUI("Failed to connecto to "+btDevice.getName());
                    String conExpMsg = connectExp.getMessage();
                    Log.d(LOG_TAG, "[BtConnectAsClientThread] IOException connectExp, Close the socket");
                    Log.d(LOG_TAG, "Message: " + conExpMsg);
                    btSocket.close();
                }
                catch (IOException closeExp) {
                    Log.d(LOG_TAG, "[BtConnectAsClientThread] IOException closeExp, Close exception");
                }
                return;
            }

            Log.d(LOG_TAG, "[BtConnectAsClientThread] Connection is established!");
            // Send message to the UI thread
            // updateUI("Connection is established with " + btDevice.getName());
            updateUI(RESUlT_CONN_OK);

            // Manage the connection
            btManageConnection(btSocket);
        }

        // Cancel the blocked connect() and close the socket
        public void cancel() {
            try {
                Log.d(LOG_TAG, "[BtConnectAsClientThread] Close the socket");
                btSocket.close();
            }
            catch (IOException e) {
                Log.d(LOG_TAG, "[BtConnectAsClientThread] Close exception");
            }
        }

        public void updateUI(String msgString) {
            Bundle msgBundle = new Bundle();
            msgBundle.putString(CLIENTSOCK_MSG_KEY, msgString);
            Message msg = new Message();
            msg.what = CLIENTSOCK_THREAD_WHAT;
            msg.setData(msgBundle);
            msgHandler.sendMessage(msg);
        }
    }

    /**
     * Manage BluetoothSocket connection
     */
    public class BtMessageReceiverThread extends Thread {
        public final BluetoothSocket btSocket;
        public final InputStream btIStream;;

        public BtMessageReceiverThread(BluetoothSocket otherSocket) {
            btSocket = otherSocket;
            InputStream tmpIStream = null;

            try {
                Log.d(LOG_TAG, "[BtMessageReceiverThread] Trying to get I/O Stream from BT socket");
                tmpIStream = btSocket.getInputStream();
            }
            catch (IOException e) {
                Log.d(LOG_TAG, "[BtMessageReceiverThread] IOException while getting I/O Streams");
            }

            btIStream = tmpIStream;
        }

        /**
         * Receive data with Thread function
         */
        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int numBytes;

            // Keep listening to the InputStream until an exception occurs
            Log.d(LOG_TAG, "[ConnectionManager] Listening InputStream fore data");
            while (true) {
                try {
                    numBytes = btIStream.read(buffer);
                    // Log.d(LOG_TAG, "[BtMessageReceiverThread] Read bytes: " + numBytes);
                    // Send the received data to the UI thread
                    String receivedMsg = new String(buffer, 0, numBytes, CHAR_SET);
                    receivedMsg = receivedMsg.trim();
                    updateUI(receivedMsg);
                }
                catch (IOException e) {
                    Log.d(LOG_TAG, "[ConnectionManager] IOException while receiving data from the InputStream");
                    break;
                }
            }
        }

        /**
         * Shutdown the connection
         */
        public void cancel() {
            try {
                Log.d(LOG_TAG, "[ConnectionManager] Trying to shutdown the connection");
                btSocket.close();
            }
            catch (IOException e) {
                Log.d(LOG_TAG, "[ConnectionManager] IOException while canceling");
            }
        }

        public void updateUI(String msgString) {
            Bundle msgBundle = new Bundle();
            msgBundle.putString(RECEIVER_MSG_KEY, msgString);
            Message msg = new Message();
            msg.what = RECEIVER_THREAD_WHAT;
            msg.setData(msgBundle);
            msgHandler.sendMessage(msg);
        }
    }

    /**
     * Used to manage message sending
     */
    public class BtMessageSender {
        public final BluetoothSocket btSocket;
        public final OutputStream btOStream;

        public BtMessageSender(BluetoothSocket otherSocket) {
            btSocket = otherSocket;
            OutputStream tmpOStream = null;

            try {
                Log.d(LOG_TAG, "[BtMessageSender] Trying to get I/O Stream from BT socket");
                tmpOStream = btSocket.getOutputStream();
            }
            catch (IOException e) {
                Log.d(LOG_TAG, "[BtMessageSender] IOException while getting I/O Streams");
            }

            btOStream = tmpOStream;
        }

        public void send(String msg) {
            try {
                byte[] bytes = msg.getBytes(CHAR_SET);
                // Log.d(LOG_TAG, "[BtMessageSender] Trying to write bytes...");
                btOStream.write(bytes);
                btOStream.flush();
            }
            catch (UnsupportedEncodingException e) {
                Log.e(LOG_TAG, "[BtMessageSender] UnsupportedEncodingException");
            }
            catch (IOException e) {
                Log.d(LOG_TAG, "[BtMessageSender] IOException while writing OutputStreams");
            }
        }

        public void cancel() {
            try {
                Log.d(LOG_TAG, "[BtMessageSender] Trying to shutdown the connection");
                btSocket.close();
            }
            catch (IOException e) {
                Log.d(LOG_TAG, "[BtMessageSender] IOException while canceling");
            }
        }
    }

    /**
     * Thread message handler
     */
    public class BtThreadHandler extends Handler {
        private HandlerMessageCallback hmc;

        BtThreadHandler(HandlerMessageCallback hmc) {
            this.hmc = hmc;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            hmc.msgCallback(msg);
//            String strMsg;
//            switch (msg.what) {
//                case SERVERSOCK_THREAD_WHAT:
//                    strMsg = msg.getData().getString(SERVERSOCK_MSG_KEY);
//                    break;
//                case CLIENTSOCK_THREAD_WHAT:
//                    strMsg = msg.getData().getString(CLIENTSOCK_MSG_KEY);
//                    break;
//                case RECEIVER_THREAD_WHAT:
//                    strMsg = msg.getData().getString(RECEIVER_MSG_KEY);
//                    testMsgAdapter.add(strMsg);
//                    testMsgAdapter.notifyDataSetChanged();
//                    break;
//                default:
//                    Log.d(LOG_TAG, "Message error");
//                    strMsg = "Message error";
//                    break;
//            }
//            Log.d(LOG_TAG, "[BtClientConnectThreadHandler] Got message: " + strMsg);
        }
    }
}
