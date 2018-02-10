package com.example.terlan_pc.location_2;

import android.database.Cursor;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class Map extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DatabaseHelper db;
    private double currentLatitude, currentLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        db = new DatabaseHelper(this);
        currentLatitude = Double.valueOf(getIntent().getStringExtra("currentLatitude"));
        currentLongitude = Double.valueOf(getIntent().getStringExtra("currentLongitude"));

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        int current_waited = 0;
        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/

        Cursor locs = db.getDB().rawQuery("SELECT latitude,longitude,waited FROM locations", null);
        while (locs.moveToNext()){
            if(currentLatitude == locs.getDouble(0) && currentLongitude == locs.getDouble(1)) current_waited = locs.getInt(2);
            else mMap.addMarker(new MarkerOptions().position(new LatLng(locs.getDouble(0),locs.getDouble(1))).title("Waited: " + (int)(locs.getInt(2)/60) + ":" + (int)(locs.getInt(2)%60)).icon(BitmapDescriptorFactory.fromResource(R.drawable.waited)));
        }

        mMap.addMarker(new MarkerOptions().position(new LatLng(currentLatitude,currentLongitude)).title("Current Waited: " + (int)(current_waited/60) + ":" + (int)(current_waited%60)));
    }
}
