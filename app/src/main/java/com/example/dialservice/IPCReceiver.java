package com.example.dialservice;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class IPCReceiver extends BroadcastReceiver {

    private String TAG = "IPCReceiver";


    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        if (action != null) {
            if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
                Log.d(TAG, "BootCompleted");
            }
        }

        String ReceiveDataFromClientApp = intent.getStringExtra("data");

        Toast.makeText(context, "Service app : " + ReceiveDataFromClientApp, Toast.LENGTH_LONG).show();

        //To update the value to mainactivity
        MainActivity.Datareceive = true;
        MainActivity.clientdata = ReceiveDataFromClientApp;

       /* //send reply to client app
        Intent explicitIntent = new Intent();
        explicitIntent.putExtra("data", "RX app2 : " + ReceiveDataFromClientApp);
        explicitIntent.setClassName("com.example.dialclient", "com.example.dialclient.IPCReceiver");
        context.sendBroadcast(explicitIntent);*/

    }
}



