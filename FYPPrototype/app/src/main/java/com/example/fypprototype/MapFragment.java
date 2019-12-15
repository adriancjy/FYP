package com.example.fypprototype;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.geojson.GeoJsonLayer;
import android.hardware.SensorEventListener;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import static android.content.Context.SENSOR_SERVICE;

public class MapFragment extends Fragment implements GoogleMap.OnCameraIdleListener, OnMapReadyCallback, SensorEventListener {

    MapView mapView;
    static GoogleMap map;
    private SensorManager senSensorManager;
    private Sensor senAccelerometer, mMagnetometer, mLinear;
    LatLng defaultLT = new LatLng(1.377481, 103.849090);
    long prevTime;
    float velX, velY, velZ, prevVelX, prevVelY, prevVelZ, prevAccelX, prevAccelY, prevAccelZ, distX, distY, distZ;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = (MapView) v.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        Calendar c = Calendar.getInstance();
        // Sensor setup
        senSensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = senSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mLinear = senSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        senSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_FASTEST);
        senSensorManager.registerListener(this, mLinear, SensorManager.SENSOR_DELAY_FASTEST);

        return v;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        //map.setMinZoomPreference(18);
        LatLngBounds SIT = new LatLngBounds(
                new LatLng(1.3768321076897605,103.84782068431379), new LatLng(1.3779526119211989,103.849414922297));
        try {
            GeoJsonLayer layer = new GeoJsonLayer(map, R.raw.map, getActivity().getApplicationContext());
            layer.addLayerToMap();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        map.setOnCameraIdleListener(this);
        map.addMarker(new MarkerOptions().position(defaultLT)
                .title("Default"));
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(SIT, 0));

    }
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        senSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        senSensorManager.registerListener(this, mLinear, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }
    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
        senSensorManager.unregisterListener(this);
    }
    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
    @Override
    public void onCameraIdle() {
        LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
            float accelX = event.values[0];
            float accelY = event.values[1];
            float accelZ = event.values[2];

            long currentTime = System.currentTimeMillis() / 1000;

            if(prevTime == 0)
                prevTime = currentTime;

            long interval = currentTime - prevTime;
            prevTime = currentTime;

            velX += accelX * interval;
            velY += accelY * interval;
            velZ += accelZ * interval;

            distX += prevVelX + velX * interval;
            distY += prevVelY + velY * interval;
            distZ += prevVelZ + velZ * interval;

            prevAccelX = accelX;
            prevAccelY = accelY;
            prevAccelZ = accelZ;

            prevVelX = velX;
            prevVelY = velY;
            prevVelZ = velZ;

            TextView distval = (TextView) getActivity().findViewById(R.id.distanceVal);
            distval.setText(String.valueOf(distX));

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }






}
