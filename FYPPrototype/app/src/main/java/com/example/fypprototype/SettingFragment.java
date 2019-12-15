package com.example.fypprototype;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class SettingFragment extends Fragment implements ZXingScannerView.ResultHandler {

    private ZXingScannerView ScannerView;
    public static String qrVal;
    Bundle args = new Bundle();
    float content = 0.0f;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        final EditText strideLength = (EditText) v.findViewById(R.id.strideInput);
        final TextView tvX = (TextView) v.findViewById(R.id.tvX);
        final TextView tvY = (TextView) v.findViewById(R.id.tvY);
        ScannerView = new ZXingScannerView(getActivity());
        RelativeLayout rl = (RelativeLayout) v.findViewById(R.id.startScan);
        rl.addView(ScannerView);
        ScannerView.setResultHandler(this);
        ScannerView.startCamera();
        ScannerView.setSoundEffectsEnabled(true);
        ScannerView.setAutoFocus(true);
        getActivity().setTitle("Settings");



        Button saveBtn = (Button) v.findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(strideLength.getText().toString().isEmpty()){
                    Toast.makeText(getActivity().getApplicationContext(), "Please key in your stride length value.", Toast.LENGTH_LONG).show();
                }else{
                    if(args.containsKey("startX") && args.containsKey("startY")){
                        content = Float.valueOf(strideLength.getText().toString());
                        args.putFloat("strideLength", content);
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        IndoorFragment fragment = new IndoorFragment();
                        fragment.setArguments(args);
                        fragmentTransaction.replace(R.id.fragment_container, fragment);
                        fragmentTransaction.commit();
                        Toast.makeText(getActivity().getApplicationContext(), "Successfully saved", Toast.LENGTH_LONG).show();
                    }
                    else{
                        Toast.makeText(getActivity().getApplicationContext(), "Please scan your initial QR code.", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        Button resetBtn = (Button) v.findViewById(R.id.resetBtn);
        resetBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(args.containsKey("startX")){
                    args.remove("startX");
                }
                if(args.containsKey("startY")){
                    args.remove("startY");
                }
                if(args.containsKey("strideLength")){
                    args.remove("strideLength");
                }
                if(args.containsKey("level")){
                    args.remove("level");
                }
                strideLength.setText("");
                tvX.setText("");
                tvY.setText("");
                onResume();



            }
        });

        return v;

    }

    @Override
    public void handleResult(Result result){
        qrVal = result.getText();
        String[] indiQR = qrVal.split(",");
        if(indiQR.length == 4){
            if(indiQR[0].equals("Initial")){
                args.putFloat("startX", Float.valueOf(indiQR[1]));
                args.putFloat("startY", Float.valueOf(indiQR[2]));
                args.putString("level", indiQR[3]);
                TextView tvX = (TextView) getActivity().findViewById(R.id.tvX);
                TextView tvY = (TextView) getActivity().findViewById(R.id.tvY);
                tvX.setText(indiQR[1]);
                tvY.setText(indiQR[2]);
                onPause();
            }
        }else{
            Toast.makeText(getActivity().getApplicationContext(), "Wrong QR Code scanned. Please scan the initial QR Code", Toast.LENGTH_LONG).show();
            onResume();
        }




    }

    @Override
    public void onPause() {
        super.onPause();
        ScannerView.stopCamera();

    }

    @Override
    public void onResume() {
        super.onResume();
        ScannerView.setResultHandler(this);
        ScannerView.startCamera();
    }
}
