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
import android.widget.Toast;


public class SensorFragment extends Fragment implements SensorEventListener{

    private SensorManager mSensorManager;
    private Sensor mAcc, mGyro, mMag;
    static final float ALPHA = 0.25f;
    protected float[] gravSensorVals;
    protected float[] magSensorVals;
    protected float[] accelLinearData;
    protected int steps = 0;

    double lastAccelZValue = -9999;
    long lastCheckTime = 0; boolean highLineState = true;
    boolean lowLineState = true;
    boolean passageState = false; double highLine = 1;
    double highBoundaryLine = 0;
    double highBoundaryLineAlpha = 1.0; double highLineMin = 0.50;
    double highLineMax = 1.5;
    double highLineAlpha = 0.0005; double lowLine = -1;
    double lowBoundaryLine = 0;
    double lowBoundaryLineAlpha = -1.0; double lowLineMax = -0.50;
    double lowLineMin = -1.5;
    double lowLineAlpha = 0.0005; double lowPassFilterAlpha = 0.9;  float[] rotationData = new float[9];
    float[] resultData = new float[3];


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
//        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
//            float xAcc = event.values[0];
//            float yAcc = event.values[1];
//            float zAcc = event.values[2];
//            gravSensorVals = lowPass(event.values.clone(), gravSensorVals);
//            String test = "X:" + xAcc + " Y:" + yAcc + " Z:" + zAcc;
//            double total = Math.sqrt(xAcc * xAcc + yAcc * yAcc + zAcc * zAcc);
//            String valueAcc = Double.toString(total);
//            TextView accText = (TextView) getActivity().findViewById(R.id.accText);
//            accText.setText(test);
//        }

        if (event.sensor.getType() ==  Sensor.TYPE_LINEAR_ACCELERATION) {
            this.accelLinearData = event.values.clone();
        }  if (this.accelLinearData != null) {
            readStepDetection(accelLinearData);
        }
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

    private void readStepDetection(float[] accelLinearData) {  long currentTime = System.currentTimeMillis();
        long gapTime1 = (currentTime - lastCheckTime);  if (lastAccelZValue == -9999){
            lastAccelZValue = accelLinearData[2];
        }  if (highLineState && highLine > highLineMin) {
            highLine = highLine - highLineAlpha;
            highBoundaryLine = highLine * highBoundaryLineAlpha;
        }  if (lowLineState && lowLine < lowLineMax) {
            lowLine = lowLine + lowLineAlpha;
            lowBoundaryLine = lowLine * lowBoundaryLineAlpha;
        }//perform a low pass filter for sensor reading
        double zValue = (lowPassFilterAlpha * lastAccelZValue) + (1 -lowPassFilterAlpha) * accelLinearData[2];  if (highLineState && gapTime1 > 100 && zValue > highBoundaryLine){
            highLineState = false;
        }  if (lowLineState && zValue < lowBoundaryLine && passageState) {
            lowLineState = false;
        }  if (!highLineState) {
            if (zValue > highLine) {
                highLine = zValue;
                highBoundaryLine = highLine * highBoundaryLineAlpha;

                if (highLine > highLineMax) {
                    highLine = highLineMax;
                    highBoundaryLine = highLine * highBoundaryLineAlpha;
                }
            } else {
                if (highBoundaryLine > zValue) {
                    highLineState = true;
                    passageState = true;
                }
            }
        }  if (!lowLineState && passageState) {
            if (zValue < lowLine) {
                lowLine = zValue;
                lowBoundaryLine = lowLine * lowBoundaryLineAlpha;

                if (lowLine < lowLineMin) {
                    lowLine = lowLineMin;
                    lowBoundaryLine = lowLine * lowBoundaryLineAlpha;
                }
            } else {
                if (lowBoundaryLine < zValue) {
                    lowLineState = true;
                    passageState = false;
                    steps++;
                    Toast.makeText(getActivity().getApplicationContext(), "Detected" + steps, Toast.LENGTH_LONG).show();


                    lastCheckTime = currentTime;
                }
            }
        }

        lastAccelZValue = zValue;
    }
}
