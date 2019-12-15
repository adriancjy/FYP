package com.example.fypprototype;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.util.Log;

public class MagneticFieldEventListener implements SensorEventListener{
    private static final String TAG = "MagFieldEventListener";
    private static final String CSV_HEADER = "M_X Axis, M_Y Axis, M_Z Axis, M_Init, M_Azimuth, M_Time";
    private static final char CSV_DELIM = ',';

    private MainActivity mainActivity;
    private AcceleratorEventListener accEvent;
    private GyroscopeEventListener gyroEvent;


    private long startTime;
    private float[] values;
    private float[] rotationMatrix;
    private float[] orientationValues;
    private float[] initDegree;
    private float[] oriDegree;
    private float initAzimuth;
    private float azimuth;

    private float initRadAzimuth;
    private float RadAzimuth;

    public boolean checkOrient = false;

    // private Handler mHandler;

    public MagneticFieldEventListener(MainActivity mainActivity,
                                      AcceleratorEventListener accEvent, GyroscopeEventListener gyroEvent) {

        this.mainActivity = mainActivity;
        this.accEvent = accEvent;
        this.gyroEvent = gyroEvent;
        // mHandler = new Handler();

        startTime = SystemClock.uptimeMillis();

        values = new float[3];
        rotationMatrix = new float[16];
        orientationValues = new float[3];
        initDegree = new float[3];
        oriDegree = new float[3];
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        values = event.values.clone();

        if (gyroEvent.Norm == true && checkOrient == false)

            setOrientation();
        if (checkOrient == true)
            calcAzimuth();

        //if (mainActivity.isRecord == true)
            //writeSensorEvent(event.timestamp);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "onAccuracyChanged");

    }

    public void setOrientation() {
        float[] acc = accEvent.getFilteredValues();

        SensorManager.getRotationMatrix(rotationMatrix, null, acc, values);
        SensorManager.getOrientation(rotationMatrix, orientationValues);

        setInitAzimuth();

        checkOrient = true;
    }

    private void setInitAzimuth() {
        initRadAzimuth = orientationValues[0];
        initAzimuth = (float) Math.toDegrees(orientationValues[0]);

        Log.e(TAG, "" + initAzimuth);
        gyroEvent.setInitAzimuth(initAzimuth);
        gyroEvent.kalmanYaw.setAngle(initRadAzimuth);
        gyroEvent.kalAngleZ = initRadAzimuth;
        gyroEvent.compZ = initRadAzimuth;
        gyroEvent.compDeg = initAzimuth;
    }

    private void calcAzimuth() {
        float[] acc = accEvent.getFilteredValues();

        SensorManager.getRotationMatrix(rotationMatrix, null, acc, values);
        SensorManager.getOrientation(rotationMatrix, orientationValues);

        RadAzimuth = (float) orientationValues[0];
        azimuth = (float) Math.toDegrees(orientationValues[0]);

        //mainActivity.mapView.setAngle(azimuth);
    }

    public float getInitAzimuth() {
        return initAzimuth;
    }

    public float getAzimuth() {
        return azimuth;
    }

    public float getRadAzimuth(){
        return RadAzimuth;
    }

    public float[] getValues() {
        return values;
    }



}
