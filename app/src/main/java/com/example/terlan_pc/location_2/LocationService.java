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
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Terlan-Pc on 2/10/2018.
 */

public class LocationService extends Service {

    private LocationManager mLocationManager = null;
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
    private static final float LOCATION_DISTANCE = 0;
    private static final String TAG = "LOCATIONLISTENER";

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

    private class LocationListener implements android.location.LocationListener
    {
        Location mLastLocation;

        LocationListener(String provider)
        {
            mLastLocation = new Location(provider);

            currentLatitude = String.format("%.4f", mLastLocation.getLatitude());
            currentLongitude = String.format("%.4f", mLastLocation.getLongitude());

            Intent intent = new Intent();
            intent.setAction(MY_ACTION);
            intent.putExtra("currentLatitude", currentLatitude);
            intent.putExtra("currentLongitude", currentLongitude);
            sendBroadcast(intent);
        }

        @Override
        public void onLocationChanged(Location location)
        {
            mLastLocation.set(location);

            if(currentLatitude != String.format("%.4f", location.getLatitude()) || currentLongitude != String.format("%.4f", location.getLongitude()))
            {
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
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Toast.makeText(mainContext, "Disabled", Toast.LENGTH_SHORT).show();
            running = false;
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Toast.makeText(mainContext, "Enabled", Toast.LENGTH_SHORT).show();
            running = true;
            if(!thread.isAlive()) thread.start();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            //new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }

        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_SHORT).show();
        running = false;
    }

    @Override
    public void onCreate() {
        db = new DatabaseHelper(this);
        mainContext = this;
        Toast.makeText(this, "Service created", Toast.LENGTH_SHORT).show();


        initializeLocationManager();
        /*try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, CHECK_PER_TIME * 1000, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }*/
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, CHECK_PER_TIME * 1000, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }


    }

    private void initializeLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service started", Toast.LENGTH_SHORT).show();
        /*thread =  new Thread(new MyThreadClass(startId));

        if(mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            running = true;
            thread.start();
        }*/

        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }
}
