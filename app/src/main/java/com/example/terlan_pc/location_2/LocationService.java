package com.example.terlan_pc.location_2;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

/**
 * Created by Terlan-Pc on 2/10/2018.
 */

public class LocationService extends Service {

    private LocationManager locationManager;
    private LocationListener locationListener;
    private String currentLatitude = "0";
    private String currentLongitude = "0";
    private static final int CHECK_PER_TIME = 5; //second
    private static final int ENROLL_TIME = 2 * 60; //second
    private int remainigTime = ENROLL_TIME;
    private int not_sended = 0;
    private DatabaseHelper db;
    private Thread thread;
    private Context mainContext;
    private boolean running = true;
    final static String MY_ACTION = "LocationReceiver";

    final class MyThreadClass implements Runnable{

        int service_id;

        MyThreadClass(int service_id)
        {
            this.service_id = service_id;
        }

        @Override
        public void run() {

            synchronized (this)
            {
                while(running==true)
                {
                    remainigTime -= CHECK_PER_TIME;
                    not_sended += CHECK_PER_TIME;

                    if (remainigTime <= 0) {
                        int r = db.insert(currentLatitude, currentLongitude, not_sended);
                        not_sended = 0;
                        //Toast.makeText(mainContext, "You have waited so long " + r, Toast.LENGTH_SHORT).show();
                    }
                    try {
                        wait(CHECK_PER_TIME * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flag, int startId) {
        Toast.makeText(this, "Service started", Toast.LENGTH_SHORT).show();
        db = new DatabaseHelper(this);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mainContext = this;

        thread =  new Thread(new MyThreadClass(startId));

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLatitude = String.format("%.4f", location.getLatitude());
                currentLongitude = String.format("%.4f", location.getLongitude());
                remainigTime = ENROLL_TIME;
                not_sended = 0;

                Intent intent = new Intent();
                intent.setAction(MY_ACTION);
                intent.putExtra("currentLatitude", currentLatitude);
                intent.putExtra("currentLongitude", currentLongitude);
                sendBroadcast(intent);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                //Toast.makeText(mainContext, "onStatusChanged", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProviderEnabled(String provider) {
                Toast.makeText(mainContext, "Enabled", Toast.LENGTH_SHORT).show();
                running = true;
                if(!thread.isAlive()) thread.start();
            }

            @Override
            public void onProviderDisabled(String provider) {
                Toast.makeText(mainContext, "Disabled", Toast.LENGTH_SHORT).show();
                running = false;
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "You don't have the permissions", Toast.LENGTH_SHORT).show();
        }
        else {
            locationManager.requestLocationUpdates("gps", CHECK_PER_TIME * 1000, 10, locationListener);
            //Toast.makeText(this, "Listener binded", Toast.LENGTH_SHORT).show();
        }

        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            running = true;
            thread.start();
        }

        return START_STICKY;
    }
}
