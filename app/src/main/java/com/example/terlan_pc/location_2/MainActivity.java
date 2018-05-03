package com.example.terlan_pc.location_2;

import android.Manifest;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
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

import com.mapswithme.maps.api.MWMPoint;
import com.mapswithme.maps.api.MapsWithMeApi;

public class MainActivity extends AppCompatActivity {

    private Button btn_map_of, btn_map, btn_locationLists, btn_start, btn_stop;
    private TextView tw_location;
    private CountDownTimer countDownTimer;
    private String currentLatitude = "0";
    private String currentLongitude = "0";
    MyReceiver myReceiver;
    public static String EXTRA_FROM_MWM = "from-maps-with-me";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        init();
    }

    void init()
    {
        btn_map = (Button) findViewById(R.id.btn_map);
        btn_map_of = (Button) findViewById(R.id.btn_map_ofline);
        btn_start = (Button) findViewById(R.id.btn_start);
        btn_stop = (Button) findViewById(R.id.btn_stop);
        btn_locationLists = (Button) findViewById(R.id.btn_location_lists);
        tw_location = (TextView) findViewById(R.id.tw_location);

        btn_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent map = new Intent(MainActivity.this, Map.class);
                map.putExtra("currentLatitude", currentLatitude);
                map.putExtra("currentLongitude", currentLongitude);
                startActivity(map);
            }
        });

        btn_map_of.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MapsWithMeApi.isMapsWithMeInstalled(MainActivity.this)){
                    PendingIntent pendingIntent = MapsMe.getPendingIntent(MainActivity.this);
                    MapsWithMeApi.showPointsOnMap(MainActivity.this, "Locations");

                    //locations
                    DatabaseHelper db = new DatabaseHelper(getBaseContext());
                    Cursor all = db.getDB().rawQuery("SELECT latitude,longitude,waited FROM locations",null);

                    if(all.getCount() != 0)
                    {
                        int i = 0;
                        final MWMPoint[] points = new MWMPoint[all.getCount()];
                        while (all.moveToNext()){
                            points[i] = new MWMPoint(all.getFloat(0), all.getFloat(1), "Waited: " +  (int)(all.getInt(2)/60) + ":" + (int)(all.getInt(2)%60), String.valueOf(i));
                            i++;
                        }

                        MapsWithMeApi.showPointsOnMap(MainActivity.this, "This title says that user should choose some point", pendingIntent, points);
                    }
                }
                else{
                    Toast.makeText(MainActivity.this, "MapsWithMe must be installed.", Toast.LENGTH_LONG).show();
                }
            }
        });

        btn_locationLists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent locations = new Intent(MainActivity.this, LocationLists.class);
                startActivity(locations);
            }
        });

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent service = new Intent(MainActivity.this, LocationService.class);
            startService(service);
            }
        });

        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent service = new Intent(MainActivity.this, LocationService.class);
                stopService(service);
            }
        });
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onStop() {
        unregisterReceiver(myReceiver);
        super.onStop();
    }

    @Override
    protected void onStart() {
        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocationService.MY_ACTION);
        registerReceiver(myReceiver, intentFilter);

        super.onStart();
    }

    //some
    public static PendingIntent getPendingIntent(Context context)
    {
        final Intent i = new Intent(context, MainActivity.class);
        i.putExtra(EXTRA_FROM_MWM, true);
        return PendingIntent.getActivity(context, 0, i, 0);
    }

    //for receiving data from service
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub

            currentLatitude = arg1.getStringExtra("currentLatitude");
            currentLongitude = arg1.getStringExtra("currentLongitude");

            tw_location.setText("Latitude: " + currentLatitude + " Longitude:" + currentLongitude);
        }
    }
}
