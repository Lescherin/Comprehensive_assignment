package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class MyBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent alertIntent = new Intent(context, Web_Check.class);
        alertIntent.putExtra("message", "Received in MyBroadcastReceiver");
        alertIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(alertIntent);
    }
}