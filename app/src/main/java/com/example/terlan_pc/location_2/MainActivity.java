package com.example.terlan_pc.location_2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Button btn_map, btn_locationLists;
    private TextView tw_location;
    private LocationManager locationManager;
    private LocationListener locationListener;
    CountDownTimer countDownTimer;

    //
    private static final int CHECK_PER_TIME = 5; //second
    private static final int ENROLL_TIME = 1 * 60; //second
    private double currentLatitude = 0;
    private double currentLongitude = 0;
    private int remainigTime = ENROLL_TIME;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        init();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                tw_location.setText("Current Latitude: " + location.getLatitude() + " Longitude:" + location.getLongitude());

                remainigTime = ENROLL_TIME;
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.INTERNET
            }, 10);
            return;
        }
        else{
            confugureButton();
        }
    }

    void init()
    {
        btn_map = (Button) findViewById(R.id.btn_map);
        btn_locationLists = (Button) findViewById(R.id.btn_location_lists);
        tw_location = (TextView) findViewById(R.id.tw_location);

        btn_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent map = new Intent(MainActivity.this, Map.class);
                startActivity(map);
            }
        });

        btn_locationLists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent locations = new Intent(MainActivity.this, LocationLists.class);
                startActivity(locations);
            }
        });

        countDownTimer = new CountDownTimer(Long.MAX_VALUE, CHECK_PER_TIME * 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainigTime -= CHECK_PER_TIME;

                if(remainigTime <= 0)
                {
                    Toast.makeText(MainActivity.this,"You have waited so long",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFinish() {

            }
        }.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 10:
                confugureButton();
                break;
        }
    }

    void confugureButton(){
        /*btn_get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationManager.requestLocationUpdates("gps", 5000, 10, locationListener);
            }
        });*/

        locationManager.requestLocationUpdates("gps", CHECK_PER_TIME * 1000, 10, locationListener);
    }
}
