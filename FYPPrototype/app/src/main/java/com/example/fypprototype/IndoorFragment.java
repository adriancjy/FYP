package com.example.fypprototype;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.content.Context.SENSOR_SERVICE;

public class IndoorFragment extends Fragment implements ZXingScannerView.ResultHandler, SensorEventListener {

    public static String qrVal;
    private ZXingScannerView ScannerView;
    float[] mGravity;
    float[] mGeomagnetic;
    private SensorManager mSensorManager;
    private Sensor stepDetector;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private TextView TextView_count;
    private TextView TextView_azimuth;
    private Button button;
    float azimuth;
    private int steps;
    float startX = 0;
    float startY = 0;
    float prevX, prevY, x, y;
    final Path mPath = new Path();
    private Canvas canvas;
    private Bitmap bitmap;
    private ImageView emptyView;
    float strideLength = 0.0f;
    String level = "";



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_indoor, container, false);

        Bundle bundle = getArguments();
        if(bundle != null){
            strideLength = bundle.getFloat("strideLength", strideLength);
            startX = bundle.getFloat("startX", startX);
            startY = bundle.getFloat("startY", startY);
            level = bundle.getString("level");
        }else{
            System.err.println("null");
        }

        ScannerView = new ZXingScannerView(getActivity());
        RelativeLayout rl = (RelativeLayout) v.findViewById(R.id.relative_scan_take_single);
        Button sensor = (Button) v.findViewById(R.id.sensorBtn);
        rl.addView(ScannerView);
        ScannerView.setResultHandler(this);
        ScannerView.startCamera();
        ScannerView.setSoundEffectsEnabled(true);
        ScannerView.setAutoFocus(true);
        //Hiding the relativelayout
        rl.setVisibility(View.INVISIBLE);
        ImageView floorplanView = (ImageView) v.findViewById(R.id.floorplanView);
        if(level.equals("")) {
            floorplanView.setImageResource(R.drawable.defaultbg);
        }
        else if(level.equals("2")){
            getActivity().setTitle("You are at Level " + level);
            floorplanView.setImageResource(R.drawable.fpl2);
        }
        else if(level.equals("3")){
            getActivity().setTitle("You are at Level " + level);
            floorplanView.setImageResource(R.drawable.fpl3);
        }
        else if(level.equals("4")) {
            getActivity().setTitle("You are at Level " + level);
            floorplanView.setImageResource(R.drawable.fpl4);
        }
        emptyView = (ImageView) v.findViewById(R.id.emptyView);
        emptyView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                bitmap = Bitmap.createBitmap(emptyView.getWidth(), emptyView.getHeight(), Bitmap.Config.ARGB_8888);
                canvas = new Canvas(bitmap);

            }
            });

        //to be removed
        floorplanView.setClickable(true);
        floorplanView.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    float xVal = event.getX();
                    float yVal = event.getY();
                    Toast.makeText(getActivity().getApplicationContext(), "Value of x: " + xVal + " value of y: " + yVal, Toast.LENGTH_LONG).show();
                    ImageView emptyView = (ImageView) getActivity().findViewById(R.id.emptyView);
                    float width = emptyView.getWidth();
                    float height = emptyView.getHeight();
                    Bitmap bitmap = Bitmap.createBitmap(emptyView.getWidth(), emptyView.getHeight(), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    paint.setColor(Color.BLACK);
                    canvas.drawCircle(xVal, yVal, 10, paint);
                    emptyView.setImageBitmap(bitmap);

                }
                return true;
            }

        });



        sensor.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                move(azimuth, strideLength);
            }
        });

        mSensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        stepDetector = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR); // problème
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        steps=0;

        return v;
    }

    @Override
    public void handleResult(Result result){
        qrVal = result.getText();
        drawLines(qrVal);
        Toast.makeText(getActivity().getApplicationContext(), "You have been repositioned", Toast.LENGTH_LONG).show();
        onResume();

    }

    public void drawLines (String values){
        String[] indiQR = values.split(",");
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);
        paint.setColor(Color.RED);

        //canvas.save();
        if(indiQR.length == 3){
            if(indiQR[0].equals("Reposition")){
                mPath.moveTo(Float.parseFloat(indiQR [1]), Float.parseFloat(indiQR [2]));
                mPath.lineTo(prevX, prevY);
                canvas.drawPath(mPath, paint);
                startX = Float.parseFloat((indiQR[1]));
                startY = Float.parseFloat((indiQR[2]));
            }
        }
        else{
            mPath.moveTo(Float.parseFloat(indiQR [0]), Float.parseFloat(indiQR [1]));
            mPath.lineTo(prevX, prevY);
            canvas.drawPath(mPath, paint);
        }
        emptyView.setImageBitmap(bitmap);
        emptyView.invalidate();
    }


    @Override
    public void onPause() {
        mSensorManager.unregisterListener(this);
        super.onPause();
        ScannerView.stopCamera();

    }

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, stepDetector, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
        ScannerView.setResultHandler(this);
        ScannerView.startCamera();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR)
        {
            // if (event.values[0] == 1.0f) {
            steps++;

            Toast.makeText(getActivity().getApplicationContext(), "Step detected", Toast.LENGTH_LONG).show();


            move(azimuth, strideLength);
            // }

        }
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimuth = orientation[0];


            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public boolean move(float dir, float dist) {
        if(dist == 0.0){
            Toast.makeText(getActivity().getApplicationContext(), "Please set your stride length", Toast.LENGTH_LONG).show();
        }else{
            // TODO Auto-generated method stub
            double dx=Math.cos((double)dir)*dist;
            double dy=Math.sin((double)dir)*dist;
            x = startX;
            y = startY;
            startX += dx;
            startY += dy;
            prevX = startX;
            prevY = startY;
            String xVal = Float.toString(x);
            String yVal = Float.toString(y);
            drawLines(xVal + "," + yVal);


        }
        return true;
    }



}
