package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri; // 确保导入 Uri
import android.os.Bundle;
import android.view.MenuItem; // 导入 MenuItem
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar; // 导入 ActionBar
import androidx.appcompat.app.AppCompatActivity;


public class Web_Check extends AppCompatActivity {
    private IntentFilter intentFilter;
    private NetworkChangeReceiver networkChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_check);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("网络状态诊断");
            actionBar.setDisplayHomeAsUpEnabled(true); // 显示返回按钮
        }

        // 初始化网络状态监听
        intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        networkChangeReceiver = new NetworkChangeReceiver();
        registerReceiver(networkChangeReceiver, intentFilter);

        // 自定义广播
        Button buttonBroadcast = findViewById(R.id.buttonBroadcast);
        buttonBroadcast.setOnClickListener(v -> {
            Intent broadcastIntent = new Intent("com.example.myapplication.MY_BROADCAST");
            sendBroadcast(broadcastIntent);
            Toast.makeText(Web_Check.this, "自定义广播已发送", Toast.LENGTH_SHORT).show();
        });


        // 浏览器搜索
        Button buttonBrowser = findViewById(R.id.buttonBrowser);
        buttonBrowser.setOnClickListener(v -> {
            // 获取从 MainActivity 传递过来的 Intent
            Intent intentThatStartedThisActivity = getIntent();
            String keywordToSearch = "";
            // 从 Intent 中接收名为 "sreach" 的关键词
            if (intentThatStartedThisActivity != null && intentThatStartedThisActivity.hasExtra("sreach")) {
                keywordToSearch = intentThatStartedThisActivity.getStringExtra("sreach");
            }

            // 检查关键词是否为空
            if (keywordToSearch != null && !keywordToSearch.isEmpty()) {
                String searchUrl = "http://www.baidu.com/s?wd=" + Uri.encode(keywordToSearch); // 对关键词进行URL编码
                // 创建隐式 Intent 以启动浏览器
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl));
                startActivity(browserIntent); 
            } else {
                // 如果关键词为空，给用户提示
                Toast.makeText(Web_Check.this, "没有传入文本框信息,请在主页输入搜索关键词后重试", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkChangeReceiver != null) {
            unregisterReceiver(networkChangeReceiver);
        }
    }

    // 处理 ActionBar 返回按钮的点击事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
                //获取系统服务后台的网络状态
                Network network = connectivityManager.getActiveNetwork();
                NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
                //信息提示
                if (networkCapabilities != null &&
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    Toast.makeText(context, "网络已连接", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "网络已断开", Toast.LENGTH_LONG).show();
                }
            }
        }
}