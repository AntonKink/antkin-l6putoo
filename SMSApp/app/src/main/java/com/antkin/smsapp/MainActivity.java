package com.antkin.smsapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.security.PrivateKey;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, AdapterView.OnItemClickListener, View.OnClickListener {

    //code for asking permission
    private static final int MY_PERMISSIONS_REQUEST_RECEIVE_SMS = 0;
    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQENABLEBT = 10;

    TextView messageTV, numberTV;

    private FrameLayout frameMessage;
    private LinearLayout frameControls;
    private Switch switchEnableBT;
    private Button btnEnableSearch;
    private ProgressBar pbProgress;
    private ListView listBtDevices;

    private BluetoothAdapter bluetoothAdapter;


    MyReceiver receiver = new MyReceiver(){
        //ctrl + O -> onReceive
        @Override
        public void onReceive(Context context, Intent intent) {
            super.onReceive(context, intent);
            messageTV.setText(msg);
            numberTV.setText(phoneNo);
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

        switchEnableBT.setOnCheckedChangeListener(this);
        btnEnableSearch.setOnClickListener(this);
        listBtDevices.setOnItemClickListener(this);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(bluetoothAdapter == null){
            Toast.makeText(this, "BT is not supported!", Toast.LENGTH_LONG).show();
            Log.d(TAG, "onCreate: BT is not supported!");
            finish();
        }

        if (bluetoothAdapter.isEnabled()){
            showFrameControls();
            switchEnableBT.setChecked(true);
        }

        //Check if the permission is not granted
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED){
            //if the permission is not been granted then check if the user has denied the permission
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECEIVE_SMS)){
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
    public void onClick(View view) {

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

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
        if (requestCode == REQENABLEBT){
            if(resultCode == RESULT_OK && bluetoothAdapter.isEnabled()){
                showFrameControls();
            }
            else if (resultCode == RESULT_CANCELED){
                Toast.makeText(this, "BT is Enabled", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void showFrameMessage(){
        frameMessage.setVisibility(View.VISIBLE);
        frameControls.setVisibility(View.GONE);
    }
    private void showFrameControls(){
        frameMessage.setVisibility(View.GONE);
        frameControls.setVisibility(View.VISIBLE);
    }

    //after getting the result of permission requests the result will be passed through this method
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        //will check the requestCode
        switch (requestCode){
            case MY_PERMISSIONS_REQUEST_RECEIVE_SMS:{
                //check whether the length of grantResults is greather than 0 and is equal to PERMISSION_GRANTED
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //Now broadcastreceiver will work in background
                    Toast.makeText(this, "Permission Allowed!", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(this, "Permission Denied!", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
    }

    private void enableBt(boolean flag){
        if(flag){
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQENABLEBT);
        }
        else{
            bluetoothAdapter.disable();
        }
    }
}
