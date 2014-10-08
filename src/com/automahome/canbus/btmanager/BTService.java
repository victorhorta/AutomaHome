package com.automahome.canbus.btmanager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.automahome.canbus.Main;
import com.automahome.canbus.R;
import com.automahome.canbus.Values;
import com.automahome.canbus.mvc.EngineController;

public class BTService extends Service {
    
    static final String TAG = "BTService";
    private final boolean DEBUG = true;
    
    public static boolean SERVICE_IS_RUNNING = false;
    
    // Intents Service -> Activity
//    public static final String INTENT_SAVE_ERROR = "save_error";
//    public static final String INTENT_SAVE_SUCCESS = "save_success";
//    public static final String EXTRA_SAVE_SUCCESS_TIMESTAMP = "save_success_timestamp";
//    public static final String EXTRA_SAVE_SUCCESS_AMOUNTDATA = "save_success_amountdata";
//    public static final String EXTRA_SAVE_SUCCESS_FILEPATH = "save_success_filepath";
    
    public static final String INTENT_CONNECT_SUCCESS = "connect_success";
    public static final String INTENT_CONNECT_ERROR = "connect_error";
    
    public static final String INTENT_DATA_ARRIVED_FROM_SENSOR = "data_arrived_from_sensor";
    public static final String EXTRA_DATA_ARRIVED_LABELS = "extra_data_arrived_labels";
    public static final String EXTRA_DATA_ARRIVED_VALUES = "extra_data_arrived_values";
    
    
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    int counter;
    volatile boolean stopWorker;
    
    // Notification parameters
    private boolean notificationBeingShown = false;
    private NotificationManager notificationManager;
    public static final int NOTIFICATION_ID = 8001;
    
    String deviceAddressSelected;
    String deviceTypeSelected;
    public EngineController mEngineController = new EngineController();
    
    // Registra as mensagens que chegarem por bluetooth!!!
    static StringBuffer history;

    // Do not call this method directly!
    // Performs one-time setup procedures (before it calls either onStartCommand() or onBind())
    @Override
    public void onCreate() {
    	SERVICE_IS_RUNNING = true;
        
        history = new StringBuffer();
        
        notificationManager =  (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mCommandRequest, new IntentFilter(Values.INTENT_COMMAND_REQUEST));
        
        
        
        //IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        //IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        //IntentFilter filter3 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        //this.registerReceiver(mReceiver, filter1);
        //this.registerReceiver(mReceiver, filter2);
        this.registerReceiver(mBluetoothStatReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
    }
    
    private BroadcastReceiver mCommandRequest = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // String action = intent.getAction();
            // String command = intent.getStringExtra(Values.EXTRA_COMMAND_REQUEST_COMMAND);
            byte[] command = intent.getByteArrayExtra(Values.EXTRA_COMMAND_REQUEST_COMMAND);

            Intent intentReply;

            try {       
                sendCRBytestreamCR(command);
                intentReply = new Intent(Values.INTENT_COMMAND_SUCCESS);
                intentReply.putExtra(Values.EXTRA_COMMAND_REQUEST_COMMAND, command);
                LocalBroadcastManager.getInstance(BTService.this).sendBroadcast(intentReply);
            } catch (IOException e) {
                intentReply = new Intent(Values.INTENT_COMMAND_ERROR);
                intentReply.putExtra(Values.EXTRA_COMMAND_REQUEST_COMMAND, command);
                LocalBroadcastManager.getInstance(BTService.this).sendBroadcast(intentReply);
            }
        }
    };
    
    
    
    //The BroadcastReceiver that listens for bluetooth broadcasts
    private final BroadcastReceiver mBluetoothStatReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

//            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//               ... //Device found
//            }
//            else if (BluetoothAdapter.ACTION_ACL_CONNECTED.equals(action)) {
//               ... //Device is now connected
//            }
//            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
//               ... //Done searching
//            }
//            else if (BluetoothAdapter.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
//               ... //Device is about to disconnect
//            }
             if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                 if(device.equals(mmDevice)){
                     if (DEBUG) Log.d(TAG, "Bluetooth lost connection! true");                     
                 }
                 else {
                     if (DEBUG) Log.d(TAG, "Bluetooth lost connection! false");                     
                 }
                 
                 stopSelf();
//               ... //Device has disconnected
            }           
        }
    };
    
    
    
    
    
    //Do not call this method directly!
    // Called by the system to notify a Service that it is no longer used and is
    // being removed. The service should clean up any resources it holds
    // (threads, registered receivers, etc) at this point. Upon return, there
    // will be no more calls in to this Service object and it is effectively
    // dead.
    
    /**
     * If a component starts the service by calling startService() (which
     * results in a call to onStartCommand()), then the service remains running
     * until it stops itself with stopSelf() or another component stops it by
     * calling stopService().
     * 
     * @see android.app.Service#onDestroy()
     */
    @Override
    public void onDestroy() {
        deactivateNotification();
        try {
            closeBT();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        SERVICE_IS_RUNNING = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (DEBUG) Log.d(TAG, "onStartCommand");

        // Setting our device
        connectDevice(intent);
        
        // Opening our bluetooth connection!
        try {
            openBT();
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }
        
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }
    
    private void connectDevice(Intent data) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null)
            // We don't have an adapter, so we should stop!!!!
            stopSelf();
        
        // Get the device MAC address
        deviceAddressSelected = data.getExtras()
            .getString(Values.EXTRA_DEVICE_ADDRESS);
        
        deviceTypeSelected = data.getExtras()
                .getString(Values.EXTRA_DEVICE_TYPE_SELECTED);

        mmDevice = mBluetoothAdapter.getRemoteDevice(deviceAddressSelected);
//        mActiveDeviceStreamHandler = allDeviceStreamHandlers.get(deviceTypeSelected);
//        if(mActiveDeviceStreamHandler == null)
//            mActiveDeviceStreamHandler = new EcoMiniStreamHandler(this); 
        
        Log.d(TAG, "Bluetooth Device Found");
    }
    
    
    private void openBT() throws IOException {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); // SSP
                                                                             // UUID

        mmSocket = mmDevice.createInsecureRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();

        beginListenForData();
        activateNotification();
        Log.d(TAG, "Bluetooth Opened");
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(INTENT_CONNECT_SUCCESS));
    }
    
    void beginListenForData() {
        final Handler handler = new Handler();
        final byte delimiter1 = 10; // This is the ASCII code for a newline
                                    // character
        final byte delimiter2 = 13; // This is the ASCII code for a carriage-return
                                    // character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        int bytesAvailable = mmInputStream.available();
                        if (bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for (int i = 0; i < bytesAvailable; i++) {
                                byte b = packetBytes[i];
                                if (b == delimiter1 || b == delimiter2) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0,
                                            encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    final byte[] finalEncodedBytes = Arrays.copyOf(encodedBytes, encodedBytes.length); 
                                    readBufferPosition = 0;

                                    handler.post(new Runnable() {
                                        public void run() {
                                            if (DEBUG) Log.d(TAG, "Data received!");
                                            
                                            String dataInRunnable = data; 
                                            
                                            mEngineController.handleIncomingDataFromMaster(finalEncodedBytes);
                                            history.append(dataInRunnable + "\n");
                                        }
                                    });
                                } else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        } else {
                            Thread.sleep(100);
                        }
                    } catch (IOException e) {
                        stopWorker = true;
                        deactivateNotification();
                        stopSelf();
                    } catch (InterruptedException e) {
                        stopWorker = true;
                        deactivateNotification();
                        stopSelf();
                    }
                }
            }
        });

        workerThread.start();
    }
    
    void sendCRBytestreamCR(final byte[] bytestream) throws IOException {
        final byte carriageReturn = 13;
        
        ByteArrayOutputStream outputStream0 = new ByteArrayOutputStream();
        ByteArrayOutputStream outputStream1 = new ByteArrayOutputStream();
        ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream();
        ByteArrayOutputStream outputStream3 = new ByteArrayOutputStream();
        ByteArrayOutputStream outputStreamCR = new ByteArrayOutputStream();

        try {
        int tempo = 100;
            //outputStream0.write(bytestream[0]);
            //mmOutputStream.write(outputStream0.toByteArray());
            mmOutputStream.write(bytestream, 0, 1);
            Thread.sleep(tempo);
            
            mmOutputStream.write(bytestream, 1, 1);
            Thread.sleep(tempo);
            
            mmOutputStream.write(bytestream, 2, 1);
            Thread.sleep(tempo);
            
            mmOutputStream.write(bytestream, 3, 1);
            Thread.sleep(tempo);
            
            mmOutputStream.write(13);
            Thread.sleep(tempo);
            
            
//            outputStream1.write(bytestream[1]);
//            mmOutputStream.write(outputStream1.toByteArray());
//            Thread.sleep(1000);
//            
//            outputStream2.write(bytestream[2]);
//            mmOutputStream.write(outputStream2.toByteArray());
//            Thread.sleep(1000);
//            
//            outputStream3.write(bytestream[3]);
//            mmOutputStream.write(outputStream3.toByteArray());
//            Thread.sleep(1000);
//            
//            outputStreamCR.write(carriageReturn);
//            mmOutputStream.write(outputStream3.toByteArray());
//            Thread.sleep(1000);
            
        
            
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //outputStream.write(bytestream);
        //outputStream.write(carriageReturn);
        //outputStream.write(carriageReturn);
        
        
        if (DEBUG) Log.d(TAG, "Data sent!");
    }
    
    void sendDataCR(final String text) throws IOException {
        final byte carriageReturn = 13;
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        outputStream.write(text.getBytes());
        outputStream.write(carriageReturn);
        mmOutputStream.write(outputStream.toByteArray());
        
        if (DEBUG) Log.d(TAG, "Data sent!");
    }
    
    void closeBT() throws IOException {
        deactivateNotification();
        
        stopWorker = true;
        LocalBroadcastManager.getInstance(BTService.this).sendBroadcast(new Intent(INTENT_CONNECT_ERROR));
        
        LocalBroadcastManager.getInstance(BTService.this).unregisterReceiver(mCommandRequest);
        
        this.unregisterReceiver(mBluetoothStatReceiver);
        
        if(mmOutputStream != null)
            mmOutputStream.close();
        
        if(mmInputStream != null)
            mmInputStream.close();
        
        if(mmSocket != null)
            mmSocket.close();
        
        Log.d(TAG, "Bluetooth Closed");
    }
    
    /** Notifications ---------------*/
    private void activateNotification() {
        if(!notificationBeingShown) {
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Intent intent = new Intent(this, Main.class);
            PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
            
            Notification notification = new Notification.Builder(this)
            .setContentTitle("AutomaHome")
            .setContentText("Estamos conectados!").setSmallIcon(R.drawable.ic_launcher)
            .setAutoCancel(false)
            .setContentIntent(pIntent).build();
            
            notification.flags |= Notification.FLAG_ONGOING_EVENT;
            notification.flags |= Notification.FLAG_SHOW_LIGHTS;
            notification.ledOnMS = 100; 
            notification.ledOffMS = 500; 
            notification.ledARGB = 0xFFff00ff;
            
            notificationManager.notify(NOTIFICATION_ID, notification);        
        }
        notificationBeingShown = true;
    }
    
    private void deactivateNotification() {
        if(notificationBeingShown) {
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(NOTIFICATION_ID);
        }
        notificationBeingShown = false;
    }
    
    
    
    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

}
