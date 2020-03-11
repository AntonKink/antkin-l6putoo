package com.antkin.smsapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, AdapterView.OnItemClickListener, View.OnClickListener {

    //code for asking permission
    private static final int MY_PERMISSIONS_REQUEST_RECEIVE_SMS = 0;
    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final int REQUEST_CODE_LOC =1;
    private static final int REQ_ENABLE_BT = 10;
    public static final int BT_BOUNDED = 21;
    public static final int BT_SEARCH = 22;

    TextView messageTV, numberTV;

    private FrameLayout frameMessage;
    private LinearLayout frameControls;
    private RelativeLayout frameLedControls;
    private Button btnDisconnect;
    private Switch switchEnableBT;
    private Button btnEnableSearch;
    private ProgressBar pbProgress;
    private ListView listBtDevices;

    private BluetoothAdapter bluetoothAdapter;
    private BtListAdapter listAdapter;
    private ArrayList<BluetoothDevice> bluetoothDevices;

    private ConnectedThread connectedThread;
    private ConnectThread connectThread;

    MyReceiver receiver = new MyReceiver(){
        //ctrl + O -> onReceive
        @Override
        public void onReceive(Context context, Intent intent) {
            super.onReceive(context, intent);
            String command = "";
            messageTV.setText(msg);
            numberTV.setText(phoneNo);
            command = phoneNo + "\n" + msg;
            if (connectedThread != null){
                connectedThread.write(command);
            }

        }
    };
    //ctrl + O -> onResume
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(SMS_RECEIVED));
    }
    //ctrl + O -> onDestroy
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        unregisterReceiver(MinuReceiver);

        if(connectThread != null){
            connectThread.cancel();
        }
        if(connectedThread != null){
            connectedThread.cancel();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messageTV = findViewById(R.id.message);
        numberTV = findViewById(R.id.number);

        frameMessage = findViewById(R.id.frame_message);
        frameControls = findViewById(R.id.frame_control);

        switchEnableBT = findViewById(R.id.switch_enable_bt);
        btnEnableSearch = findViewById(R.id.btn_enable_search);
        pbProgress = findViewById(R.id.pb_progress);
        listBtDevices = findViewById(R.id.lv_bt_device);

        frameLedControls = findViewById(R.id.frameLedcontrol);
        btnDisconnect = findViewById(R.id.btn_disconnect);

        switchEnableBT.setOnCheckedChangeListener(this);
        btnEnableSearch.setOnClickListener(this);
        listBtDevices.setOnItemClickListener(this);

        btnDisconnect.setOnClickListener(this);

        bluetoothDevices = new ArrayList<>();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(MinuReceiver, filter);



        if(bluetoothAdapter == null){
            Toast.makeText(this, "BT is not supported!", Toast.LENGTH_LONG).show();
            Log.d(TAG, "onCreate: BT is not supported!");
            finish();
        }

        if (bluetoothAdapter.isEnabled()){
            showFrameControls();
            switchEnableBT.setChecked(true);
            setListAdapter(BT_BOUNDED);
        }

        //Check if the permission is not granted
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED){
            //if the permission is not been granted then check if the user has denied the permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECEIVE_SMS)){
                //Do nothing as user has denied
            }
            else{
                //a pop up will appear asking for required permission i.e. Allow or Deny
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, MY_PERMISSIONS_REQUEST_RECEIVE_SMS);
            }
        }
    }//onCreate

    //ctrl+o ->  onClick, onItemClick, onCheckedChanged
    @Override
    public void onClick(View v) {
        if(v.equals(btnEnableSearch)){
            enableSearch();
        }
        else if(v.equals(btnDisconnect)){
            if(connectThread != null){
                connectThread.cancel();
            }
            if(connectedThread != null){
                connectedThread.cancel();
            }
            showFrameControls();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(parent.equals(listBtDevices)){
            BluetoothDevice device = bluetoothDevices.get(position);
            if(device != null){
                connectThread = new ConnectThread(device);
                connectThread.start();
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(buttonView.equals(switchEnableBT)){
            enableBt(isChecked);

            if(!isChecked){
                showFrameMessage();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQ_ENABLE_BT){
            if(resultCode == RESULT_OK && bluetoothAdapter.isEnabled()){
                showFrameControls();
                setListAdapter(BT_BOUNDED);
            }
            else if (resultCode == RESULT_CANCELED){
                Toast.makeText(this, "BT isnt Enabled", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void showFrameMessage(){
        frameMessage.setVisibility(View.VISIBLE);
        frameControls.setVisibility(View.GONE);
        frameLedControls.setVisibility(View.GONE);
    }
    private void showFrameControls(){
        frameMessage.setVisibility(View.GONE);
        frameLedControls.setVisibility(View.GONE);
        frameControls.setVisibility(View.VISIBLE);
    }
    private void showFrameLedControls(){
        frameLedControls.setVisibility(View.VISIBLE);
        frameMessage.setVisibility(View.GONE);
        frameControls.setVisibility(View.GONE);
    }

    //after getting the result of permission requests the result will be passed through this method
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        //will check the requestCode
        switch (requestCode){
            case MY_PERMISSIONS_REQUEST_RECEIVE_SMS:
                //check whether the length of grantResults is greather than 0 and is equal to PERMISSION_GRANTED
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //Now broadcastreceiver will work in background
                    Toast.makeText(this, "Permission Allowed!", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(this, "Permission Denied!", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            case REQUEST_CODE_LOC:
                if (grantResults.length > 0) {
                    for (int gr : grantResults) {
                        // Check if request is granted or not
                        if (gr != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                    }
                    //TODO - Add your code here to start Discovery
                }
                break;
        }
    }

    private void enableBt(boolean flag){
        if(flag){
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQ_ENABLE_BT);
        }
        else{
            bluetoothAdapter.disable();
        }
    }

    private void setListAdapter(int type){
        bluetoothDevices.clear();
        int iconType = R.drawable.ic_bluetooth_bounded_device;
        switch (type){
            case BT_BOUNDED:
                bluetoothDevices = getBoundedBtDevices();
                iconType = R.drawable.ic_bluetooth_bounded_device;
                break;
            case BT_SEARCH:
                iconType = R.drawable.ic_bluetooth_search_device;
                break;
        }
        listAdapter = new BtListAdapter(this,bluetoothDevices, iconType);
        listBtDevices.setAdapter(listAdapter);
    }

    private ArrayList<BluetoothDevice> getBoundedBtDevices(){
        Set<BluetoothDevice> deviceSet = bluetoothAdapter.getBondedDevices();
        ArrayList<BluetoothDevice> tmpArrayList = new ArrayList<>();
        if(deviceSet.size() > 0){
            for (BluetoothDevice device: deviceSet) {
                tmpArrayList.add(device);
            }
        }
        return tmpArrayList;
    }

    public boolean isLocationServiceEnabled(){
        LocationManager locationManager = null;
        boolean gps_enabled= false,network_enabled = false;

        if(locationManager ==null)
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try{
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }catch(Exception ex){
            //do nothing...
        }

        try{
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }catch(Exception ex){
            //do nothing...
        }

        return gps_enabled || network_enabled;

    }
    public void LocationServiceNotify(){
        if (isLocationServiceEnabled()) {
            //DO what you need...
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("For some reason (especially if u have android 10), location service should be ON for Bluetooth device discorery.")
                    .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    }).setNegativeButton("I will try without it", null).create().show();
        }
    }

    private void enableSearch(){
        if(bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
        }
        else {
            accessLocationPermission();
            LocationServiceNotify();
            bluetoothAdapter.startDiscovery();
            //Toast.makeText(this, "Start Discovering!", Toast.LENGTH_SHORT).show();
        }
    }

    private final BroadcastReceiver MinuReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                //discovery starts, we can show progress dialog or perform other tasks
                btnEnableSearch.setText("stop search");
                pbProgress.setVisibility(View.VISIBLE);
                setListAdapter(BT_SEARCH);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //discovery finishes, dismis progress dialog
                btnEnableSearch.setText("start search");
                pbProgress.setVisibility(View.GONE);
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device != null){
                    bluetoothDevices.add(device);
                    listAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    private void accessLocationPermission() {
        int accessCoarseLocation = this.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        int accessFineLocation   = this.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);

        List<String> listRequestPermission = new ArrayList<String>();

        if (accessCoarseLocation != PackageManager.PERMISSION_GRANTED) {
            listRequestPermission.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (accessFineLocation != PackageManager.PERMISSION_GRANTED) {
            listRequestPermission.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (!listRequestPermission.isEmpty()) {
            String[] strRequestPermission = listRequestPermission.toArray(new String[listRequestPermission.size()]);
            this.requestPermissions(strRequestPermission, REQUEST_CODE_LOC);
        }
    }

    private class ConnectThread extends Thread{
        private BluetoothSocket bluetoothSocket = null;
        private boolean success = false;

        public ConnectThread(BluetoothDevice device) {
            try {
                Method method = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                bluetoothSocket = (BluetoothSocket) method.invoke(device, 1);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try{
                bluetoothSocket.connect();
                success = true;
            }
            catch (IOException e){
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Cant connect", Toast.LENGTH_LONG).show();
                    }
                });
                cancel();
            }
            if(success){
                connectedThread = new ConnectedThread(bluetoothSocket);
                connectedThread.start();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showFrameLedControls();
                    }
                });
            }
        }

        public boolean isConnect(){
            return bluetoothSocket.isConnected();
        }

        private void cancel(){
            try {
                bluetoothSocket.close();
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private class ConnectedThread extends Thread{
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public ConnectedThread(BluetoothSocket bluetoothSocket) {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = bluetoothSocket.getInputStream();
                outputStream = bluetoothSocket.getOutputStream();
            }catch (IOException e){
                e.printStackTrace();
            }
            this.inputStream = inputStream;
            this.outputStream = outputStream;
        }

        @Override
        public void run() {
            //esli nado 4t0-to s4itatj
        }

        public void write(String string){
            byte[] bytes = string.getBytes();
            if(outputStream != null){
                try {
                    outputStream.write(bytes);
                    outputStream.flush();
                }catch (IOException e){
                    e.printStackTrace();
                }

            }
        }

        public void cancel(){
            try {
                inputStream.close();
                outputStream.close();
            }catch (IOException e){
                e.printStackTrace();
            }

        }
    }
}
