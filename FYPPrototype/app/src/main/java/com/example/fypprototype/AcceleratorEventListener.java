package com.example.fypprototype;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.SystemClock;
import android.util.Log;

public class AcceleratorEventListener implements SensorEventListener{

    private static final String TAG = "AccelerationEventListener";
    private static final String CSV_HEADER = "A_X Axis, A_Y Axis, A_Z Axis, F_X Axis, F_Y Axis, F_Z Axis, Acceleration, F_Acceleration, A_Time";
    private static final char CSV_DELIM = ',';

    private static final float maxPeakThreshold = 10.5f;
    private static final float minPeakThreshold = 9.0f;
    private static final float K = 0.5f;

    //private PrintWriter printWriter;

    private long startTime;

    private MainActivity mainActivity;

    private float[] values;
    private float[] filteredValues;

    private float stepSize;
    private int step;
    private boolean stepFlag;
    private float maxSumAcc;
    private float minSumAcc;

    private boolean minFlag;

    private long detectedTime;

    public AcceleratorEventListener(MainActivity mainActivity) {
        this.mainActivity = mainActivity;

        startTime = SystemClock.uptimeMillis();

        values = new float[3];
        filteredValues = new float[3];

        minFlag = false;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        values = event.values.clone();

        filteredValues = lowPassFilter(values);

        double sumOfSquares = (values[0] * values[0]) + (values[1] * values[1])
                + (values[2] * values[2]);
        double acceleration = Math.sqrt(sumOfSquares);

        double f_sumOfSquares = (filteredValues[0] * filteredValues[0])
                + (filteredValues[1] * filteredValues[1])
                + (filteredValues[2] * filteredValues[2]);
        double f_acceleration = Math.sqrt(f_sumOfSquares);



            detectedTime = (event.timestamp / 1000000) - startTime;
            //writeSensorEvent(acceleration, f_acceleration, detectedTime);


            stepDetection(f_acceleration);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d("TAG", "onAccuracyChanged");
    }

    private float[] lowPassFilter(float[] values) {
        float[] value = new float[3];
        final float alpha = 0.8f;

        value[0] = alpha * filteredValues[0] + (1 - alpha) * values[0];
        value[1] = alpha * filteredValues[1] + (1 - alpha) * values[1];
        value[2] = alpha * filteredValues[2] + (1 - alpha) * values[2];

        return value;
    }

    private void stepDetection(double acceleration) {
        if (acceleration > maxPeakThreshold && stepFlag == false) {
            stepFlag = true;
            maxSumAcc = (float) acceleration;

        } else if (acceleration > maxPeakThreshold && stepFlag == true) {
            if (maxSumAcc < acceleration) {
                maxSumAcc = (float) acceleration;
            }

        }else if (acceleration < minPeakThreshold && stepFlag == true) {
            minFlag = true;
            if(minSumAcc > acceleration){
                minSumAcc = (float) acceleration;
            }

        }

        if(acceleration < maxPeakThreshold && acceleration > minPeakThreshold){
            if(stepFlag == true && minFlag == true){
                step++;

                setStepSize();




                stepFlag = false;
                minFlag = false;
            }
        }
    }

    private void setStepSize() {
        stepSize = (float) (K * Math.sqrt(Math.sqrt(maxSumAcc - minSumAcc)));
    }

    public float[] getValues() {
        return this.values;
    }

    public float[] getFilteredValues() {
        return this.filteredValues;
    }





    public long getDetectedTime(){
        return detectedTime;
    }



}
