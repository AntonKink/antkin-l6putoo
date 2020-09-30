package com.antkin.smsapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class MyReceiver extends BroadcastReceiver {

    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private static final String TAG = "SmsBroadcastReceiver";
    String msg, phoneNo = "";
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String FILTERED_NUMBER = "number";

    @Override
    public void onReceive(Context context, Intent intent) {
        //hangib üldise toimingu, mida tuleb täita ja logil kuvada
        Log.i(TAG,"Intent Received: "+ intent.getAction());
        if(intent.getAction() == SMS_RECEIVED){
            //otsib kavatsusest laiendatud andmete kaardi
            Bundle dataBundle = intent.getExtras();
            if(dataBundle != null){
                //PDU (Protocol Data Unit) objekti loomine, mis on sõnumi edastamise protokoll
                Object[] mypdu = (Object[])dataBundle.get("pdus");
                final SmsMessage[] message = new SmsMessage[mypdu.length];

                for (int i = 0; i < mypdu.length; i++){
                    //for build versions >= API LEVEL 23
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        String format = dataBundle.getString("format");
                        //PDU-st saame kõik objektid ja SmsMessage Objekti järgmise koodirea abil
                        message[i] = SmsMessage.createFromPdu((byte[])mypdu[i], format);
                    }
                    else{
                        //api level 23
                        message[i] = SmsMessage.createFromPdu((byte[])mypdu[i]);
                    }
                    //olulised muutujad, sisaldavad saatja numbrit ja SMS-i teksti
                    msg = message[i].getMessageBody();
                    phoneNo = message[i].getOriginatingAddress();
                }

            }
        }
    }
}
