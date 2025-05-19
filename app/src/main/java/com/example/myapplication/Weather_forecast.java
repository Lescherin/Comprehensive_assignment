package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.MyTrustManager;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;


public class Weather_forecast extends AppCompatActivity {
    private TextView textView;
    private EditText editText;
    private Button button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_forecast);

        textView = findViewById(R.id.textView);
        editText = findViewById(R.id.editText);
        button = findViewById(R.id.button);


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // 设置标题
            actionBar.setTitle("城市当日天气预报");
            // 显示返回按钮
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        // 接收从 MainActivity 传递过来的城市名称
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("city_name")) {
            String cityName = intent.getStringExtra("city_name");
            if (cityName != null && !cityName.isEmpty()) {
                editText.setText(cityName);
                // 接收到城市名后，可以自动触发一次查询
                sendRequestWithHttpURLConnection(cityName);
            }
        }

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendRequestWithHttpURLConnection(editText.getText().toString());
            }
        });
    }

    private void sendRequestWithHttpURLConnection(final String keyWord) {
        new Thread(new Runnable() {
            private BufferedReader reader;
            private HttpsURLConnection connection;

            @Override
            public void run() {
                try {
                    Map<String, String> parameters = new HashMap<>();
                    parameters.put("appid", getResources().getString(R.string.appid));
                    parameters.put("appsecret", getResources().getString(R.string.appsecret));
                    parameters.put("unescape", "1");

                    if (!keyWord.equals(""))
                        parameters.put("city", keyWord);

                    String content = "";
                    if (null != parameters && parameters.size() > 0) {
                        content += "?";
                        for (Map.Entry<String, String> entry : parameters.entrySet()) {
                            content += "&" + entry.getKey() + "=" + URLEncoder.encode(entry.getValue(),
                                    "utf-8");
                        }
                    }

                    URL url = new URL("https://v1.yiketianqi.com/free/day" + content);

                    connection = (HttpsURLConnection) url.openConnection();
                    SSLContext tls = SSLContext.getInstance("TLS");
                    tls.init(null, new TrustManager[]{new MyTrustManager()}, new SecureRandom());
                    SSLSocketFactory factory = tls.getSocketFactory();
                    connection.setHostnameVerifier((new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    }));
                    connection.setSSLSocketFactory(factory);

                    connection.setRequestMethod("GET");
                    connection.setDoInput(true);
                    connection.setUseCaches(false);
                    connection.connect();

                    connection.setConnectTimeout(3000);
                    connection.setReadTimeout(3000);
                    InputStream inputStream = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder builder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                    showResponse(builder.toString());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (KeyManagementException e) {
                    e.printStackTrace();
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();

    }

    private void showResponse(final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(s + "\n\n\n" + parseJSONWithJSONObject(s));
            }
        });
    }

    //根据文档
    private String parseJSONWithJSONObject(String jsonData) {
        String ret = "";
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            String city = jsonObject.getString("city");
            String updateTime = jsonObject.getString("update_time");
            String wea = jsonObject.getString("wea");
            String tem = jsonObject.getString("tem");
            String temDay = jsonObject.getString("tem_day");
            String temNight = jsonObject.getString("tem_night");
            String win = jsonObject.getString("win");
            String winSpeed = jsonObject.getString("win_speed");
            String winMeter = jsonObject.getString("win_meter");
            String air = jsonObject.getString("air");
            ret = "城市：" + city + "\n"
                    + "更新时间：" + updateTime + "\n"
                    + "天气：" + wea + "\n"
                    + "平均气温：" + tem + "\n"
                    + "日间气温：" + temDay + "\n"
                    + "夜间气温：" + temNight + "\n"
                    + "风向：" + win + "\n"
                    + "风力：" + winSpeed + "\n"
                    + "风速：" + winMeter + "\n"
                    + "空气质量：" + air;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
}