package com.antkin.smsapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.security.PrivateKey;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, AdapterView.OnItemClickListener, View.OnClickListener {

    //code for asking permission
    private static final int MY_PERMISSIONS_REQUEST_RECEIVE_SMS = 0;
    public static final int REQUEST_CODE_LOC =1;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 2;
    private static final int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 3;
    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQ_ENABLE_BT = 10;
    public static final int BT_BOUNDED = 21;
    public static final int BT_SEARCH = 22;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String FILTERED_NUMBER = "number";

    private String textFromArduino;
    private String phoneNumber;
    String SENT_SMS = "SENT_SMS";
    String DELIVER_SMS = "DELIVERED_SMS";
    Intent sent_intent = new Intent(SENT_SMS);
    Intent deliver_intent = new Intent(DELIVER_SMS);
    PendingIntent sent_pi, deliver_pi;

    private Button btnsavefilter;
    private EditText filteredNo;

    private EditText etConsole, etConsole2;
    private ProgressDialog progressDialog;
    final StringBuffer sbConsole2 = new StringBuffer();
    final ScrollingMovementMethod movementMethod2 = new ScrollingMovementMethod();
    String msgNo, msgSave;
    String LOGFILE = "SMSApp_Log_File.txt";
    String LOGFILEDIR = "SmsAppDir";

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
            //load filtered number and compare it with resived number
            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
            if (sharedPreferences.getString(FILTERED_NUMBER, "").equals(phoneNo)){
                Toast.makeText(context, "Message: " +msg +"\nNumber" +phoneNo, Toast.LENGTH_LONG).show();
                String command = "";
                Date currentDate = new Date();
                DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                String dateText = dateFormat.format(currentDate);
                DateFormat timeFormat = new SimpleDateFormat("HH.mm.ss", Locale.getDefault());
                String timeText = timeFormat.format(currentDate);
                //received phone number - phoneNo
                //received message - msg
                msgNo = "\n--------------------\nDate: " +dateText +"\nTime: " +timeText+ "\nMessage: " +msg;
                msgSave = "\n--------------------\nReceived SMS from " +phoneNo +"\nDate: " +dateText +"\nTime: " +timeText+ "\nMessage: " +msg;
                sbConsole2.append(msgNo);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        etConsole2.setText(sbConsole2.toString());
                        etConsole2.setMovementMethod(movementMethod2);
                    }
                });
                //setText(msg);
                //setText(phoneNo);
                command = phoneNo + "\n" + msg;
                if (connectedThread != null){
                    connectedThread.write(command);
                }

            }else{
                Date currentDate = new Date();
                DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                String dateText = dateFormat.format(currentDate);
                DateFormat timeFormat = new SimpleDateFormat("HH.mm.ss", Locale.getDefault());
                String timeText = timeFormat.format(currentDate);
                msgSave = "\n--------------------\nReceived SMS from " +phoneNo +"\nDate: " +dateText +"\nTime: " +timeText+ "\nMessage: " +msg;
            }
            //anyway, save it to a file
            //тут нужно сохранить файл
            // write text to a file
            writeFileSD(msgSave);

        }
    };

    void writeFileSD(String msgS) {
        // проверяем доступность SD
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "External Storages unvailable: " + Environment.getExternalStorageState(), Toast.LENGTH_SHORT).show();
            return;
        }else{
            // получаем путь к SD
            File sdPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            // добавляем свой каталог к пути
            sdPath = new File(sdPath.getAbsolutePath() + "/" + LOGFILEDIR);
            // создаем каталог
            if (!sdPath.exists()) { //Если папка не существует
                sdPath.mkdirs();  //создаем её
            }
            // формируем объект File, который содержит путь к файлу
            File sdFile = new File(sdPath, LOGFILE);
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(sdFile, true);
                fos.write(msgS.getBytes());
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //ctrl + O -> onResume
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(SMS_RECEIVED));
        registerReceiver(sentReceiver, new IntentFilter(SENT_SMS));
        registerReceiver(deliverReceiver, new IntentFilter(DELIVER_SMS));
    }

    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(sentReceiver);
        unregisterReceiver(deliverReceiver);
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

        btnsavefilter = findViewById(R.id.btn_filter);
        filteredNo = findViewById(R.id.FilteredPhoneNo);

        sent_pi = PendingIntent.getBroadcast(MainActivity.this, 0, sent_intent, 0);
        deliver_pi = PendingIntent.getBroadcast(MainActivity.this, 0, deliver_intent, 0);

        etConsole = findViewById(R.id.et_console);
        etConsole2 = findViewById(R.id.et_console2);

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

        btnsavefilter.setOnClickListener(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Connecting...");
        progressDialog.setMessage("Please wait. I will took a while.");

        bluetoothDevices = new ArrayList<>();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(MinuReceiver, filter);

        //load savedata and update filteredNo
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        filteredNo.setText(sharedPreferences.getString(FILTERED_NUMBER, ""));

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
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
            //if the permission is not been granted then check if the user has denied the permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)){
                //Do nothing as user has denied
            }
            else{
                //a pop up will appear asking for required permission i.e. Allow or Deny
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            //if the permission is not been granted then check if the user has denied the permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                //Do nothing as user has denied
            }
            else{
                //a pop up will appear asking for required permission i.e. Allow or Deny
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE);
            }
        }
    }//onCreate



    BroadcastReceiver sentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (getResultCode()){
                case Activity.RESULT_OK:
                    Toast.makeText(context, "SMS send!", Toast.LENGTH_LONG).show();
                    break;
                default:
                    Toast.makeText(context, "SMS Failure!", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    BroadcastReceiver deliverReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (getResultCode()){
                case Activity.RESULT_OK:
                    Toast.makeText(context, "SMS Delivered!", Toast.LENGTH_LONG).show();
                    break;
                default:
                    Toast.makeText(context, "SMS deliver Failure!", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

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
        else if(v.equals(btnsavefilter)){
            //вывести текст
            String text = filteredNo.getText().toString();
            filteredNo.setText(text);
            // сохранить текст
            saveData(text);
        }
    }

    public void saveData(String filteredNo){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(FILTERED_NUMBER, filteredNo);

        editor.apply();
        Toast.makeText(this, "Data saved", Toast.LENGTH_SHORT).show();
    }

    /* public void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String text;
        text = sharedPreferences.getString(FILTERED_NUMBER, "");
    }
    */

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
            case MY_PERMISSIONS_REQUEST_SEND_SMS:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //Now broadcastreceiver will work in background
                    Toast.makeText(this, "Permission Allowed!", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(this, "Permission Denied!", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            case MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //Now broadcastreceiver will work in background
                    Toast.makeText(this, "Permission Allowed!", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(this, "Permission Denied!", Toast.LENGTH_LONG).show();
                    finish();
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

                progressDialog.show();
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

                progressDialog.dismiss();
            }
            catch (IOException e){
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
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

        private boolean isConnected = false;

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
            isConnected = true;
        }

        @Override
        public void run() {
            //esli nado 4t0-to s4itatj
            BufferedInputStream bis = new BufferedInputStream(inputStream);
            StringBuffer buffer = new StringBuffer();
            final StringBuffer sbConsole = new StringBuffer();
            final ScrollingMovementMethod movementMethod = new ScrollingMovementMethod();
            String msgNo2;

            while (isConnected){
                try {
                    //вытащить отсюда текст, чтоб дальше с ним можно было работать
                    int bytes = bis.read();
                    buffer.append((char)bytes);
                    int eof = buffer.indexOf("\r\n");
                    if (eof > 0){
                        Date currentDate = new Date();
                        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                        String dateText = dateFormat.format(currentDate);
                        DateFormat timeFormat = new SimpleDateFormat("HH.mm.ss", Locale.getDefault());
                        String timeText = timeFormat.format(currentDate);
                        msgNo2 = "\n--------------------\nDate: " +dateText +"\nTime: " +timeText+ "\nAnswer: " +buffer.toString();
                        msgSave = "\n--------------------\nAnswer from Arduino\nDate: " +dateText +"\nTime: " +timeText+ "\nAnswer: " +buffer.toString();
                        //anyway, save it to a file
                        //ТУТ нужно сохранить файл
                        writeFileSD(msgSave);
                        sbConsole.append(msgNo2);
                        textFromArduino = buffer.toString(); // prinjatie dannie s peremennuju
                        connectedThread.sendSMS();
                        buffer.delete(0, buffer.length());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                etConsole.setText(sbConsole.toString());
                                etConsole.setMovementMethod(movementMethod);
                            }
                        });
                    }

                }catch (IOException e){
                    e.printStackTrace();
                }
            }
            try {
                bis.close();
            }catch (IOException e){
                e.printStackTrace();
            }
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
                isConnected = false;
                inputStream.close();
                outputStream.close();
            }catch (IOException e){
                e.printStackTrace();
            }

        }

        public void sendSMS(){
            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
            phoneNumber = sharedPreferences.getString(FILTERED_NUMBER, "");

            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNumber, null, textFromArduino, sent_pi, deliver_pi);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
