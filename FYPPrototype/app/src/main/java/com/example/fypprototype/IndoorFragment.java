package com.example.fypprototype;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.os.Vibrator;


import com.google.zxing.Result;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.content.Context.SENSOR_SERVICE;

public class IndoorFragment extends Fragment implements ZXingScannerView.ResultHandler, SensorEventListener {

    public static String qrVal;
    private ZXingScannerView ScannerView;
    float[] mGravity;
    float[] mGeomagnetic;
    private SensorManager mSensorManager;
    private Sensor accelerometer, magnetometer, stepDetect;
    float azimuth;
    private int steps, prevSteps;
    float startX, startY = 0;
    float prevX, prevY, x, y;
    final Path mPath = new Path();
    private Canvas canvas;
    private Bitmap bitmap;
    private ImageView emptyView;
    float strideLength = 0.0f;
    String level = "";

    //step detection
    double lastAccelZValue = -9999;
    long lastCheckTime = 0; boolean highLineState = true;
    boolean lowLineState = true;
    boolean passageState = false; double highLine = 0.75;
    double highBoundaryLine = 0;
    double highBoundaryLineAlpha = 0.75; double highLineMin = 0.25;
    double highLineMax = 1.25;
    double highLineAlpha = 0.0005; double lowLine = -0.75;
    double lowBoundaryLine = 0;
    double lowBoundaryLineAlpha = -0.75; double lowLineMax = -0.25;
    double lowLineMin = -1.25;
    double lowLineAlpha = 0.0005; double lowPassFilterAlpha = 0.9;  float[] rotationData = new float[9];

    protected float[] accelLinearData;
    TextView stepCount;
    Handler ha;

    boolean firstTime = false;




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_indoor, container, false);
        Bundle bundle = getArguments();
        MainActivity ma = (MainActivity)getActivity();
        boolean status = ma.globalStatus;



        if(status){
            if(bundle != null){

                strideLength = bundle.getFloat("strideLength", strideLength);
                startX = bundle.getFloat("startX", startX);
                startY = bundle.getFloat("startY", startY);
                level = bundle.getString("level");
                setPrefVal();
            }else{
                getPrefVal();

            }
        }

        ScannerView = new ZXingScannerView(getActivity());
        RelativeLayout rl = (RelativeLayout) v.findViewById(R.id.relative_scan_take_single);
        //Button sensor = (Button) v.findViewById(R.id.sensorBtn);
        rl.addView(ScannerView);
        ScannerView.setResultHandler(this);
        ScannerView.startCamera();
        ScannerView.setSoundEffectsEnabled(true);
        ScannerView.setAutoFocus(true);
        //Hiding the relativelayout
        //rl.setVisibility(View.INVISIBLE);

        final ImageView floorplanView = (ImageView) v.findViewById(R.id.floorplanView);
        if(level.equals("")) {
            floorplanView.setImageResource(R.drawable.defaultbg);
        }
        else if(level.equals("2")){
            getActivity().setTitle("You are at Level " + level);
            floorplanView.setImageResource(R.drawable.fpl2new);
        }
        else if(level.equals("3")){
            getActivity().setTitle("You are at Level " + level);
            floorplanView.setImageResource(R.drawable.fpl3new);
        }
        else if(level.equals("4")) {
            getActivity().setTitle("You are at Level " + level);
            floorplanView.setImageResource(R.drawable.fpl4new);

        }else if(level.equals("nypl4")){
            getActivity().setTitle("You are at Level " + level);
            floorplanView.setImageResource(R.drawable.nypl4);
        }
        else if(level.equals("nypl5")){
            getActivity().setTitle("You are at Level " + level);
            floorplanView.setImageResource(R.drawable.nypl5);
        }else if(level.equals("blank")){
            getActivity().setTitle("Blank");
            floorplanView.setImageResource(R.drawable.blank);
        }

        emptyView = (ImageView) v.findViewById(R.id.emptyView);
        emptyView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                bitmap = Bitmap.createBitmap(floorplanView.getWidth(), floorplanView.getHeight(), Bitmap.Config.ARGB_8888);
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
                    float width = floorplanView.getWidth();
                    float height = floorplanView.getHeight();
                    Bitmap bitmap = Bitmap.createBitmap(floorplanView.getWidth(), floorplanView.getHeight(), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    paint.setColor(Color.BLACK);
                    canvas.drawCircle(xVal, yVal, 10, paint);
                    emptyView.setImageBitmap(bitmap);

//                    Toast.makeText(getActivity().getApplicationContext(), "Value of x: " + startX + " value of y: " + startY, Toast.LENGTH_LONG).show();

                }
                return true;
            }



        });

        mSensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        steps=0;

        ha = new Handler();
        ha.postDelayed(new Runnable() {

            @Override
            public void run() {
                if(!firstTime){
                    firstTime = true;
                    prevSteps = steps;
                    ha.postDelayed(this, 10000);
                }else{
                    if(prevSteps != steps){
                        alertStationary();
                        prevSteps = steps;
                        ha.postDelayed(this, 20000);
                    }else{
                        ha.postDelayed(this, 10000);
                    }
                }
            }
        }, 15000);


        return v;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        ha.removeCallbacksAndMessages(null);
    }

    @Override
    public void handleResult(Result result){
        qrVal = result.getText();
        drawLines(qrVal);
        onResume();

    }

    public void drawLines (String values){
        String[] indiQR = values.split(",");
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);
        paint.setColor(Color.RED);

        //canvas.save();
        if(indiQR.length == 4){
            if((indiQR[0]).toLowerCase().equals("reposition") && indiQR[3].equals(level)){
                mPath.moveTo(Float.parseFloat(indiQR [1]), Float.parseFloat(indiQR [2]));
                mPath.lineTo(prevX, prevY);
                canvas.drawPath(mPath, paint);
                startX = Float.parseFloat((indiQR[1]));
                startY = Float.parseFloat((indiQR[2]));
                Toast.makeText(getActivity().getApplicationContext(), "You have been repositioned", Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(getActivity().getApplicationContext(), "Invalid QR Code", Toast.LENGTH_LONG).show();
            }
        }
        else if(indiQR.length == 2){
            mPath.moveTo(Float.parseFloat(indiQR [0]), Float.parseFloat(indiQR [1]));
            mPath.lineTo(x, y);
            canvas.drawPath(mPath, paint);
        }else{
            Toast.makeText(getActivity().getApplicationContext(), "Invalid QR Code", Toast.LENGTH_LONG).show();
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
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
        ScannerView.setResultHandler(this);
        ScannerView.startCamera();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() ==  Sensor.TYPE_LINEAR_ACCELERATION) {
            this.accelLinearData = event.values.clone();
        }
        if (this.accelLinearData != null) {
            readStepDetection(accelLinearData);
        }
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION)
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
            prevX = startX;
            prevY = startY;
            startX += dx;
            startY += dy;
            x = startX;
            y = startY;
            String xVal = Float.toString(prevX);
            String yVal = Float.toString(prevY);
            setPrefVal();
            drawLines(xVal + "," + yVal);
        }
        return true;
    }


    private void readStepDetection(float[] accelLinearData) {
        long currentTime = System.currentTimeMillis();
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
                    stepCount = (TextView) getActivity().findViewById(R.id.stepCount);
                    stepCount.setText(String.valueOf(steps));
                    if(steps%2 == 0){
                        move(azimuth, strideLength);
                    }
                    lastCheckTime = currentTime;
                }
            }
        }

        lastAccelZValue = zValue;
    }


    public void setPrefVal(){
        SharedPreferences sp = this.getActivity().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putFloat("strideLength", strideLength);
        edit.putFloat("startX", startX);
        edit.putFloat("startY", startY);
        edit.putString("level", level);
        edit.commit();
    }

    public void getPrefVal(){
        SharedPreferences sp = this.getActivity().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        startX = sp.getFloat("startX", startX);
        startY = sp.getFloat("startY", startY);
        strideLength = sp.getFloat("strideLength", strideLength);
        level = sp.getString("level", level);
    }

    public void alertStationary(){
            Toast.makeText(getActivity(), "Stop for 5 seconds to allow for accelerometer reset", Toast.LENGTH_LONG).show();
    }


    }







