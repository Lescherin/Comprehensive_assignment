package com.example.myapplication;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;
import java.util.List;

public class Sensor_Check extends AppCompatActivity implements SensorEventListener {
    private SensorManager mgr;
    private TextView txtList;
    private Sensor accSensor, proxSensor, gravitySensor, stepSensor, magSensor;
    //在已有的传感器变量基础上，新增传感器变量压力,光照,环境温度和相对湿度传感器变量
    private Sensor pressureSensor, lightSensor, tempSensor, humiditySensor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor_check);

        mgr = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        txtList = findViewById(R.id.mainView);
        List<Sensor> sensorList = mgr.getSensorList(Sensor.TYPE_ALL);
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("Available sensors:\n");
        for (Sensor s : sensorList) {
            strBuilder.append(s.getName() + "\n");
        }
        txtList.setVisibility(View.VISIBLE);
        txtList.setText(strBuilder);

        accSensor = mgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        proxSensor = mgr.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        gravitySensor = mgr.getDefaultSensor(Sensor.TYPE_GRAVITY);
        stepSensor = mgr.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        magSensor = mgr.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        pressureSensor = mgr.getDefaultSensor(Sensor.TYPE_PRESSURE);
        lightSensor = mgr.getDefaultSensor(Sensor.TYPE_LIGHT);
        tempSensor = mgr.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        humiditySensor = mgr.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //注册各个传感器
        mgr.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_UI);
        mgr.registerListener(this, proxSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mgr.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        mgr.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mgr.registerListener(this, magSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mgr.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mgr.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mgr.registerListener(this, tempSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mgr.registerListener(this, humiditySensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mgr.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensorType = event.sensor.getType();

        TableRow accTable = findViewById(R.id.acc);
        TextView accX = (TextView) accTable.getChildAt(0);
        TextView accY = (TextView) accTable.getChildAt(1);
        TextView accZ = (TextView) accTable.getChildAt(2);


        TableRow gravTable = findViewById(R.id.gravity);
        TextView gravX = (TextView) gravTable.getChildAt(0);
        TextView gravY = (TextView) gravTable.getChildAt(1);
        TextView gravZ = (TextView) gravTable.getChildAt(2);


        TableRow magTable = findViewById(R.id.mag);
        TextView magX = (TextView) magTable.getChildAt(0);
        TextView magY = (TextView) magTable.getChildAt(1);
        TextView magZ = (TextView) magTable.getChildAt(2);

        TextView prox = findViewById(R.id.proxView);

        TextView step = findViewById(R.id.stepView);

        TextView pressure = findViewById(R.id.pressureView);

        TextView light = findViewById(R.id.lightView);

        TextView temp = findViewById(R.id.tempView);

        TextView humidity = findViewById(R.id.humidityView);

        DecimalFormat df = new DecimalFormat("#0.00");

        switch (sensorType) {
            case Sensor.TYPE_ACCELEROMETER:
                accX.setText(df.format(event.values[0]));
                accY.setText(df.format(event.values[1]));
                accZ.setText(df.format(event.values[2]));
                break;
            case Sensor.TYPE_PROXIMITY:
                prox.setText(df.format(event.values[0]));
                break;
            case Sensor.TYPE_GRAVITY:
                gravX.setText(df.format(event.values[0]));
                gravY.setText(df.format(event.values[1]));
                gravZ.setText(df.format(event.values[2]));
                break;
            case Sensor.TYPE_STEP_COUNTER:
                step.setText(df.format(event.values[0]));
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                magX.setText(df.format(event.values[0]));
                magY.setText(df.format(event.values[1]));
                magZ.setText(df.format(event.values[2]));
                break;
            case Sensor.TYPE_PRESSURE:
                pressure.setText(df.format(event.values[0]));
                break;
            case Sensor.TYPE_LIGHT:
                light.setText(df.format(event.values[0]));
                break;
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                temp.setText(df.format(event.values[0]));
                break;
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                humidity.setText(df.format(event.values[0]));
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }
}