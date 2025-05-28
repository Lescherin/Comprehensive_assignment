package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

//这是一个自定义的广播接收器
//当接收到在 AndroidManifest.xml 文件中为其 <intent-filter> 定义的特定广播时，
// 它的 onReceive 方法会被调用。
public class MyBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent alertIntent = new Intent(context, Web_Check.class);
        // 向 Intent 中添加一些额外的数据，这些数据可以在 Web_Check Activity 中被检索和使用。
        alertIntent.putExtra("message", "Received in MyBroadcastReceiver");
        //添加这个标志目的是创建一个新的任务栈来承载该 Activity。
        //不加这个标志，在某些情况下（例如应用在后台时收到广播并尝试启动 Activity）可能会导致崩溃。
        alertIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // 使用提供的 context 启动 Web_Check Activity。
        context.startActivity(alertIntent);
    }
}