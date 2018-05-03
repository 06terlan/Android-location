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
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

public class LocationService extends Service {

    public LocationManager locationManager;
    public MyLocationListener listener;
    public double currentLatitude = 0;
    public double currentLongitude = 0;
    private static final int CHECK_PER_TIME = 5; //second
    private static final int ENROLL_TIME = 2 * 60; //second
    public int remainigTime = ENROLL_TIME;
    public int not_sended = 0;
    public Intent boardCast = null;
    public final static String MY_ACTION = "LocationReceiver";

    public boolean running = true;
    private DatabaseHelper db;
    private Thread thread;

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
                while(running)
                {
                    remainigTime -= CHECK_PER_TIME;
                    not_sended += CHECK_PER_TIME;

                    if (remainigTime <= 0) {
                        int r = db.insert(String.valueOf(currentLatitude), String.valueOf(currentLongitude), not_sended);
                        not_sended = 0;
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
    public void onCreate() {

        boardCast = new Intent();
        boardCast.setAction(MY_ACTION);

        running = true;

        db = new DatabaseHelper(this);

        super.onCreate();

        Toast.makeText(this, "Service Created", Toast.LENGTH_SHORT).show();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new MyLocationListener();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "No permision", Toast.LENGTH_SHORT).show();
            return;
        }
        //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 4000, 0, listener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, CHECK_PER_TIME * 1000, 30f, listener);

        Location loc =  locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        if(loc != null) locationChanged(loc);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        thread = new Thread(new MyThreadClass(startId));

        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            running = true;
            if(!thread.isAlive()) thread.start();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        running = false;
        locationManager.removeUpdates(listener);
    }


    public class MyLocationListener implements LocationListener
    {

        public void onLocationChanged(final Location location)
        {
            locationChanged(location);
        }

        public void onProviderDisabled(String provider)
        {
            Toast.makeText( getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT ).show();

            running = false;
        }

        public void onProviderEnabled(String provider)
        {
            Toast.makeText( getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();

            running = true;
            if(!thread.isAlive()) thread.start();
        }

        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Toast.makeText( getApplicationContext(), "Status Changed", Toast.LENGTH_SHORT).show();
        }

    }

    //my functions
    private void locationChanged(Location location)
    {
        if(! String.format("%.4f",currentLatitude).equals(String.format("%.4f", location.getLatitude())) || !String.format("%.4f",currentLongitude).equals(String.format("%.4f", location.getLongitude())))
        {
            remainigTime = ENROLL_TIME;
            not_sended = 0;
        }

        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();

        boardCast.putExtra("currentLatitude", String.format("%.4f", currentLatitude));
        boardCast.putExtra("currentLongitude", String.format("%.4f", currentLongitude));
        sendBroadcast(boardCast);
    }
}