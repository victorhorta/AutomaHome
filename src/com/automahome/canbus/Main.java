package com.automahome.canbus;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.automahome.canbus.btmanager.BTService;
import com.automahome.canbus.mvc.CanNode;

public class Main extends Activity {
    static final String TAG = "Main";
    BluetoothAdapter mBluetoothAdapter;
    String deviceAddressSelected;
    String deviceTypeSelected;
    String deviceNameSelected;
    
    static ArrayAdapter<CanNode> canNodeAdapter;
    List<CanNode> canNodeList = new ArrayList<CanNode>();
    
    static TextView t1;
    static TextView t2;
    static ImageView i1;
    
    private static boolean sConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        canNodeAdapter = new ArrayAdapter<CanNode>(this, R.layout.adapter_list_item, canNodeList);
        
        t1 = (TextView) findViewById(R.id.templist_title);
        t2 = (TextView) findViewById(R.id.templist_status);
        i1 = (ImageView) findViewById(R.id.templist_icon);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_conectar) {
            conectarBluetooth();
            return true;
        }

        if (id == R.id.action_desconectar) {
            desconectarBluetooth();
            return true;
        }

        if (id == R.id.action_atualizar) {
            requestRefresh();
            return true;
        }
        
        if (id == R.id.action_on) {
            action("ON");
            return true;
        }
        
        if (id == R.id.action_off) {
            action("OFF");
            return true;
        }
        
        if (id == R.id.action_R) {
            action("R");
            return true;
        }
        
        if (id == R.id.action_G) {
            action("G");
            return true;
        }
        
        if (id == R.id.action_B) {
            action("B");
            return true;
        }
        
        if (id == R.id.action_W) {
            action("W");
            return true;
        }
        
        if (id == R.id.action_C_on) {
            action("C_on");
            return true;
        }
        
        if (id == R.id.action_C_off) {
            action("C_off");
            return true;
        }
        
        if (id == R.id.action_C_Man) {
            action("C_Man");
            return true;
        }
        
        if (id == R.id.action_C_A20) {
            action("A20");
            return true;
        }
        
        if (id == R.id.action_C_A25) {
            action("A25");
            return true;
        }
        
        if (id == R.id.action_C_A30) {
            action("A30");
            return true;
        }
        
        
        return super.onOptionsItemSelected(item);
    }


    private void conectarBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            //textviewDeviceName.setText("No bluetooth adapter available");
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //startActivityForResult(enableBluetooth, REQUEST_ENABLE_BT);
            startActivityForResult(enableBluetooth, 0);
            
        }

        Intent serverIntent = null;
        serverIntent = new Intent(getApplicationContext(),
                com.automahome.canbus.btmanager.DeviceListActivity.class);
        startActivityForResult(serverIntent, Values.REQUEST_CONNECT_DEVICE);
        
    }
    
    private void desconectarBluetooth() {
        // Launch the DeviceListActivity to see devices and do scan
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            //textviewDeviceName.setText("No bluetooth adapter available");
            return;
        }

        if (sConnected) {
            // use this to start and trigger a service
            Intent i = new Intent(this, BTService.class);
            // add data to the intent
            this.stopService(i);
        } else {
            Log.d(TAG, "Can't cancel service when not connected!");
        }
        
    }
    
    private void requestRefresh() {
        byte[] commandArray = new byte[4];
        
        commandArray[0] = Values.INSTRUCT_REQ_UPDATE;
        commandArray[1] = Values.INSTRUCT_REQ_UPDATE;
        commandArray[2] = Values.INSTRUCT_REQ_UPDATE;
        commandArray[3] = Values.INSTRUCT_REQ_UPDATE;
        
        Intent intentRequest;
        intentRequest = new Intent(Values.INTENT_COMMAND_REQUEST);
        intentRequest.putExtra(Values.EXTRA_COMMAND_REQUEST_COMMAND, commandArray);
        LocalBroadcastManager.getInstance(Main.this).sendBroadcast(intentRequest);
        
    }
    
    
    private void action(String s) {
        
        byte[] commandArray = new byte[4];
        
        commandArray[0] = Values.INSTRUCT_SET;
        commandArray[1] = 0x11; // id da lampada
        
        
        if(s.equals("ON")) {
            commandArray[2] = 0x00;
            commandArray[3] = 0x01;
        }
        if(s.equals("OFF")) {
            commandArray[2] = 0x00;
            commandArray[3] = 0x02;
        }
        if(s.equals("R")){
            commandArray[2] = 0x01;
            commandArray[3] = 0x01;
            
        }
        if(s.equals("G")){
            commandArray[2] = 0x01;
            commandArray[3] = 0x02;
            
        }
        if(s.equals("B")){
            commandArray[2] = 0x01;
            commandArray[3] = 0x03;
            
        }
        if(s.equals("W")){
            commandArray[2] = 0x01;
            commandArray[3] = 0x04;
        }
        
        if(s.equals("C_on")){
            commandArray[1] = 0x22; // id do clima
            commandArray[2] = 0x00;
            commandArray[3] = 0x01;
        }
        
        if(s.equals("C_off")){
            commandArray[1] = 0x22; // id do clima
            commandArray[2] = 0x00;
            commandArray[3] = 0x02;
        }
        
        if(s.equals("C_Man")){
            commandArray[1] = 0x22; // id do clima
            commandArray[2] = 0x01;
            commandArray[3] = 0x00;
        }
        
        if(s.equals("A20")){
            commandArray[1] = 0x22; // id do clima
            commandArray[2] = 0x01;
            commandArray[3] = 0x14;
        }
        
        if(s.equals("A25")){
            commandArray[1] = 0x22; // id do clima
            commandArray[2] = 0x01;
            commandArray[3] = 0x19;
        }
        
        if(s.equals("A30")){
            commandArray[1] = 0x22; // id do clima
            commandArray[2] = 0x01;
            commandArray[3] = 0x1E;
        }
        
        
        Intent intentRequest;
        intentRequest = new Intent(Values.INTENT_COMMAND_REQUEST);
        intentRequest.putExtra(Values.EXTRA_COMMAND_REQUEST_COMMAND, commandArray);
        LocalBroadcastManager.getInstance(Main.this).sendBroadcast(intentRequest);
        
    }
    
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case Values.REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "REQUEST_CONNECT_DEVICE --- success!!!");
                deviceAddressSelected = data.getExtras().getString(Values.EXTRA_DEVICE_ADDRESS);
                deviceNameSelected = data.getExtras().getString(Values.EXTRA_DEVICE_NAME);
                //deviceTypeSelected = ((RadioButton) findViewById(radioGroup.getCheckedRadioButtonId())).getText().toString();
                
                
                
                // use this to start and trigger a service
                Intent i = new Intent(this, BTService.class);
                // add data to the intent
                i.putExtra(Values.EXTRA_DEVICE_ADDRESS, deviceAddressSelected);
                i.putExtra(Values.EXTRA_DEVICE_TYPE_SELECTED, deviceTypeSelected);
                this.startService(i);
                
                sConnected = true;
                //updateDeviceLabel();
            }
            break;
        case Values.REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "BT enabled!");
                //Toast.makeText(this, "Bluetooth habilitado!", Toast.LENGTH_SHORT).show();
                // Bluetooth is now enabled, so set up a chat session
//                setupChat();
            } else {
                // User did not enable Bluetooth or an error occurred
                Log.d(TAG, "BT not enabled...");
//                Toast.makeText(this, "Bluetooth not enable, leaving...", Toast.LENGTH_SHORT).show();
//                finish();
            }
        }
    }
    
    public static void atualizarListView(String nodename, String nodestatus, int nodeicon) {
        //canNodeAdapter.notifyDataSetChanged();
        if((t1 != null) && (t2 != null) && (i1 != null)) {
            t1.setText(nodename);
            t2.setText(nodestatus);
            i1.setImageResource(nodeicon);            
        }
    }
}
