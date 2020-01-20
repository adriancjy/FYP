package com.example.fypprototype;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavgiation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        //Set the settings as selected default.
        bottomNav.setSelectedItemId(R.id.nav_settings);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new SettingFragment()).commit();

    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {


            switch (menuItem.getItemId()){
                case R.id.nav_home:
                    if(!menuItem.isChecked()){
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                new IndoorFragment()).commit();
                        break;
                    }
                   else{
                       break;
                    }
                case R.id.nav_sensor:
                    if(!menuItem.isChecked()) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                new SensorFragment()).commit();
                        break;
                    }else{
                        break;
                    }
                case R.id.nav_map:
                    if(!menuItem.isChecked()) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                new MapFragment()).commit();
                        break;
                    }else{
                        break;
                    }
                case R.id.nav_settings:
                    if(!menuItem.isChecked()) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                new SettingFragment()).commit();
                        break;
                    }else{
                        break;
                    }
            }


            return true;

        }
    };


}
