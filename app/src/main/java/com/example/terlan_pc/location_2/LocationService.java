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

public class LocationService extends Service {
    public LocationManager locationManager;
    public MyLocationListener listener;
    public String currentLatitude = "0";
    public String currentLongitude = "0";
    private static final int CHECK_PER_TIME = 5; //second
    private static final int ENROLL_TIME = 2 * 60; //second
    public int remainigTime = ENROLL_TIME;
    public int not_sended = 0;
    public Intent boardCast = null;
    public final static String MY_ACTION = "LocationReceiver";

    @Override
    public void onCreate() {

        boardCast = new Intent();
        boardCast.setAction(MY_ACTION);

        super.onCreate();

        Toast.makeText(this, "Service Created", Toast.LENGTH_SHORT).show();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new MyLocationListener();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "No permision", Toast.LENGTH_SHORT).show();
            return;
        }
        //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 4000, 0, listener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, CHECK_PER_TIME * 1000, 0, listener);

        Location loc =  locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        if(loc != null) locationChanged(loc);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
        }

        public void onProviderEnabled(String provider)
        {
            Toast.makeText( getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
        }

        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Toast.makeText( getApplicationContext(), "Status Changed", Toast.LENGTH_SHORT).show();
        }

    }

    //my functions
    private void locationChanged(Location location)
    {
        Toast.makeText( getApplicationContext(), "LocationChanged currentLatitude: " + currentLatitude, Toast.LENGTH_SHORT ).show();

        if(!currentLatitude.equals(String.format("%.4f", location.getLatitude())) || !currentLongitude.equals(String.format("%.4f", location.getLongitude())))
        {
            remainigTime = ENROLL_TIME;
            not_sended = 0;
        }

        currentLatitude = String.format("%.4f", location.getLatitude());
        currentLongitude = String.format("%.4f", location.getLongitude());

        boardCast.putExtra("currentLatitude", currentLatitude);
        boardCast.putExtra("currentLongitude", currentLongitude);
        sendBroadcast(boardCast);
    }
}