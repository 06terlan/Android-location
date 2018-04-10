package com.example.terlan_pc.location_2;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Terlan-Pc on 2/10/2018.
 */

public class LocationService extends Service {

    private static final String TAG = "LOGGER";
    private LocationManager mLocationManager = null;
    //private static final int LOCATION_INTERVAL = 1000;
    //private static final float LOCATION_DISTANCE = 10f;
    final static String MY_ACTION = "LocationReceiver";
    private String currentLatitude = "0";
    private String currentLongitude = "0";
    private Thread thread;
    private DatabaseHelper db;
    private static final int CHECK_PER_TIME = 5; //second
    private static final int ENROLL_TIME = 1 * 60; //second
    private int remainigTime = ENROLL_TIME;
    private int not_sended = 0;
    private boolean running = true;
    private Context mainContext;

    private class LocationListener implements android.location.LocationListener
    {
        Location mLastLocation;

        LocationListener(String provider)
        {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location)
        {
            mLastLocation.set(location);

            Toast.makeText(mainContext, "Location Change check", Toast.LENGTH_SHORT).show();

            if( currentLatitude.equals(String.format("%.4f", location.getLatitude())) == false || currentLongitude.equals(String.format("%.4f", location.getLongitude())) == false )
            {
                Toast.makeText(mainContext, "Location Changed", Toast.LENGTH_SHORT).show();
                remainigTime = ENROLL_TIME;
                not_sended = 0;

                if(running && !thread.isAlive())  thread.start();

            }

            currentLatitude = String.format("%.4f", location.getLatitude());
            currentLongitude = String.format("%.4f", location.getLongitude());

            Intent intent = new Intent();
            intent.setAction(MY_ACTION);
            intent.putExtra("currentLatitude", currentLatitude);
            intent.putExtra("currentLongitude", currentLongitude);
            sendBroadcast(intent);
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
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Toast.makeText(this, "Thread started", Toast.LENGTH_SHORT).show();

        thread =  new Thread(new MyThreadClass(startId));

        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        Toast.makeText(this, "Service Created", Toast.LENGTH_SHORT).show();
        db = new DatabaseHelper(this);
        mainContext = this;

        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, CHECK_PER_TIME * 1000, 0,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 0, 10,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }

        running = true;
    }

    @Override
    public void onDestroy()
    {
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_SHORT).show();

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
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

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
                while(running == true)
                {
                    remainigTime -= CHECK_PER_TIME;
                    not_sended += CHECK_PER_TIME;

                    if (remainigTime <= 0) {
                        db.insert(currentLatitude, currentLongitude, not_sended);
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
}