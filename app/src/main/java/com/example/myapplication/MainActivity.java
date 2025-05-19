package com.example.myapplication;

import android.content.Intent;

import android.widget.Button;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {


    private EditText editTextKeyword;
    // 新增按钮引用
    private Button buttonWebCheck;
    private Button buttonWeatherForecast;

    private Button buttonReadContactList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // 设置标题
            actionBar.setTitle("2411664 大作业");
        }

        editTextKeyword = findViewById(R.id.editTextKeyword);

        buttonWebCheck = findViewById(R.id.buttonWeb_Check);
        buttonWeatherForecast = findViewById(R.id.buttonWeather_forecast);
        buttonReadContactList = findViewById(R.id.buttonRead_ContactList);


        //"网络检测"
        buttonWebCheck.setOnClickListener(v -> {
            String keyword = editTextKeyword.getText().toString();
            Intent intent = new Intent("WEB_CHECK");
            intent.putExtra("sreach", keyword); // 将 keyword 作为搜索词传递
            intent.addCategory(Intent.CATEGORY_DEFAULT); // 添加 Category
            // 检查是否能够正常跳转
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "无法打开网络检测页面", Toast.LENGTH_SHORT).show();
            }
        });

        // "城市天气预报"
        buttonWeatherForecast.setOnClickListener(v -> {
            String keyword = editTextKeyword.getText().toString(); // 将作为城市名
            Intent intent = new Intent("WEATHER_FORECAST");
            intent.putExtra("city_name", keyword); // 将 keyword 作为城市名参数传递
            intent.addCategory(Intent.CATEGORY_DEFAULT); // 添加 Category
            // 检查是否能够正常跳转
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "无法打开天气预报页面", Toast.LENGTH_SHORT).show();
            }
        });


        //"读取联系人列表"
        buttonReadContactList.setOnClickListener(v -> {
            Intent intent = new Intent("READ_CONTACT_LIST"); //
            intent.addCategory(Intent.CATEGORY_DEFAULT); // 添加 Category

            // 检查是否能够正常跳转
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "无法打开联系人列表", Toast.LENGTH_SHORT).show();
            }
        });
    }

}


