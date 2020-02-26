package com.antkin.smsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    //code for asking permission
    private static final int MY_PERMISSIONS_REQUEST_RECEIVE_SMS = 0;
    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    TextView messageTV, numberTV;

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
}
