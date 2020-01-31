package com.example.fypprototype;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.zxing.Result;

import java.util.Map;
import java.util.Set;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class SettingFragment extends Fragment implements ZXingScannerView.ResultHandler {

    private ZXingScannerView ScannerView;
    public static String qrVal;
    Bundle args = new Bundle();
    float content = 0.0f, strideCalculate = 0.0f;




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        final EditText height = (EditText) v.findViewById(R.id.heightInput);
        final TextView tvX = (TextView) v.findViewById(R.id.tvX);
        final TextView tvY = (TextView) v.findViewById(R.id.tvY);
        final RadioButton radioMale = (RadioButton) v.findViewById(R.id.rdbMale);
        final RadioButton radioFemale = (RadioButton) v.findViewById(R.id.rdbFemale);
        final RadioGroup rdG = (RadioGroup) v.findViewById(R.id.rdGroup);
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
                if(rdG.getCheckedRadioButtonId() == -1){
                    Toast.makeText(getActivity().getApplicationContext(), "Please select your gender", Toast.LENGTH_LONG).show();
                }
                else if(height.getText().toString().isEmpty()){
                    Toast.makeText(getActivity().getApplicationContext(), "Please fill in your height", Toast.LENGTH_LONG).show();
                }else{
                    if(args.containsKey("startX") && args.containsKey("startY")){
                        //get the best estimate
                        content = ((Float.valueOf(height.getText().toString()) * strideCalculate)/2.54f);
                        args.putFloat("strideLength", content);
                        BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottomNavgiation);
                        bottomNav.getMenu().findItem(R.id.nav_home).setChecked(true);
                        //bottomNav.setSelectedItemId(R.id.nav_home);
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

        rdG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected
                RadioButton rb = (RadioButton)group.findViewById(checkedId);
                if(rb.getText().equals("Male")){
                    strideCalculate = 0.415f;
                }else{
                    strideCalculate = 0.413f;
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
                height.setText("");
                tvX.setText("");
                tvY.setText("");
                if(rdG.getCheckedRadioButtonId() != -1){
                    if(rdG.getCheckedRadioButtonId() == R.id.rdbMale){
                        radioMale.setChecked(false);
                    }else if(rdG.getCheckedRadioButtonId() == R.id.rdbFemale){
                        radioFemale.setChecked(false);
                    }
                }
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
            if(indiQR[0].equals("Initial") || indiQR[0].equals("initial")){
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
