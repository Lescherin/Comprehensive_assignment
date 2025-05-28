package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {


    private EditText editTextKeyword;

    // 主页面按钮
    private Button buttonWebCheck;
    private Button buttonWeatherForecast;
    private Button buttonGmail; 
    private Button buttonReadContactList;
    private Button buttonSensor;
    private Button buttonMikaList;

    private Button buttonMGS;
    


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // 设置标题
            actionBar.setTitle("2411664 综合作业");
        }

        editTextKeyword = findViewById(R.id.editTextKeyword);

        buttonWebCheck = findViewById(R.id.buttonWeb_Check);
        buttonWeatherForecast = findViewById(R.id.buttonWeather_Forecast);
        buttonReadContactList = findViewById(R.id.buttonRead_ContactList);
        buttonSensor = findViewById(R.id.buttonSensor);
        buttonGmail = findViewById(R.id.buttonGmail);
        buttonMikaList = findViewById(R.id.buttonListview);
        buttonMGS = findViewById(R.id.buttonMGS);


        //每个跳转我都做了判断用于检测是否跳转成功
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
            Intent intent = new Intent("READ_CONTACTLIST"); //
            intent.addCategory(Intent.CATEGORY_DEFAULT); // 添加 Category
            // 检查是否能够正常跳转
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "无法打开联系人列表", Toast.LENGTH_SHORT).show();
            }
        });

        //"读取传感器信息"
        buttonSensor.setOnClickListener(v->{
            Intent intent = new Intent("SENSOR_CHECK"); //
            intent.addCategory(Intent.CATEGORY_DEFAULT); // 添加 Category
            // 检查是否能够正常跳转
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "无法打开传感器", Toast.LENGTH_SHORT).show();
            }
        });

        // "打开GMAIL邮箱,进行发送邮件"
        buttonGmail.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(android.net.Uri.parse("mailto:")); // 只显示邮件应用
            intent.setPackage("com.google.android.gm"); // 直接指定Gmail(包名)跳转
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "无法打开邮件应用", Toast.LENGTH_SHORT).show();
            }
        });

        /*跳转到ListviewDisplay页面*/
        buttonMikaList.setOnClickListener(v -> {
            Intent intent = new Intent("MikaListDisplay");
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "无法打开 Mika 表情列表页面", Toast.LENGTH_SHORT).show();
                }
            });

        /*跳转到多媒体页面*/
        buttonMGS.setOnClickListener(v -> {
            Intent intent = new Intent("MGS_PV"); //
            intent.addCategory(Intent.CATEGORY_DEFAULT); // 添加 Category
            // 检查是否能够正常跳转
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "无法打开多媒体页面", Toast.LENGTH_SHORT).show();
            }
        });



    }

    // 新增拨号功能
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        String phoneNumber = "";
        int itemId = item.getItemId();

        if (itemId == R.id.action_call_10000) {
            phoneNumber = "10000";
        } else if (itemId == R.id.action_call_10010) {
            phoneNumber = "10010";
        } else if (itemId == R.id.action_call_10086) {
            phoneNumber = "10086";
        } else {
            return super.onOptionsItemSelected(item);
        }

        if (!phoneNumber.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(android.net.Uri.parse("tel:" + phoneNumber));
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "无法打开拨号界面", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}


