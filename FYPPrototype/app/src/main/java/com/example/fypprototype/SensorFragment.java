package com.example.fypprototype;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;



public class SensorFragment extends Fragment implements SensorEventListener{

    private SensorManager mSensorManager;
    private Sensor mAcc, mGyro, mMag;
    static final float ALPHA = 0.25f;
    protected float[] gravSensorVals;
    protected float[] magSensorVals;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sensor, container, false);


        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mMag = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        return v;

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        //Accelerometer
        if(event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            float xAcc = event.values[0];
            float yAcc = event.values[1];
            float zAcc = event.values[2];
            gravSensorVals = lowPass(event.values.clone(), gravSensorVals);
            String test = "X:" + xAcc + " Y:" + yAcc + " Z:" + zAcc;
            double total = Math.sqrt(xAcc * xAcc + yAcc * yAcc + zAcc * zAcc);
            String valueAcc = Double.toString(total);
            TextView accText = (TextView) getActivity().findViewById(R.id.accText);
            accText.setText(test);
        }
        else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
            float xAcc = event.values[0];
            float yAcc = event.values[1];
            float zAcc = event.values[2];
            double total = Math.sqrt(xAcc * xAcc + yAcc * yAcc + zAcc * zAcc);
            /*String valueAcc = Double.toString(total);
            TextView gyroText = (TextView) getActivity().findViewById(R.id.gyroText);
            gyroText.setText(valueAcc);*/
        }
        else if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            float xAcc = event.values[0];
            float yAcc = event.values[1];
            float zAcc = event.values[2];
            double total = Math.sqrt(xAcc * xAcc + yAcc * yAcc + zAcc * zAcc);
           /* String valueAcc = Double.toString(total);
            TextView magText = (TextView) getActivity().findViewById(R.id.magText);
            magText.setText(valueAcc);*/
        }


        //Gyroscope



        //Magnetometer
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGyro, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMag, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    protected float[] lowPass (float[] input, float[] output){
        if(output == null){
            return input;
        }
        for(int i = 0; i < input.length; i++){
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }
}
