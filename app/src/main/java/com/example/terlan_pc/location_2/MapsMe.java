package com.example.terlan_pc.location_2;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.mapswithme.maps.api.MWMPoint;
import com.mapswithme.maps.api.MWMResponse;
import com.mapswithme.maps.api.MapsWithMeApi;

public class MapsMe extends AppCompatActivity {

    public static String EXTRA_FROM_MWM = "from-maps-with-me";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_me);

        handleIntent(getIntent());


        if(MapsWithMeApi.isMapsWithMeInstalled(this)){
            MapsWithMeApi.showPointsOnMap(this, "Locations");
            showMultiplePointsWithPendingIntent();
        }
        else{
            Toast.makeText(this, "MapsWithMe must be installed.", Toast.LENGTH_LONG).show();
        }
    }

    void showMultiplePointsWithPendingIntent()
    {
        PendingIntent pendingIntent = MapsMe.getPendingIntent(MapsMe.this);
        // Convert objects to MWMPoints
        final MWMPoint[] points = new MWMPoint[2];
        points[0] = new MWMPoint(1.1231231, 1.1231231, "Hi 1", "1");
        points[1] = new MWMPoint(12.12323, 12.12323, "Hi 2", "2");

        // Show all points on the map, you could also provide some title
        MapsWithMeApi.showPointsOnMap(this, "This title says that user should choose some point", pendingIntent, points);
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        // if defined your activity as "SingleTop"- you should use onNewIntent callback
        handleIntent(intent);
    }

    void handleIntent(Intent intent)
    {
        // Apply MWMResponse extraction method to intent
        final MWMResponse mwmResponse = MWMResponse.extractFromIntent(this, intent);
        // Here is your point that user selected
        final MWMPoint point = mwmResponse.getPoint();
        // Now, for instance you can do some work depending on point id
        //processUserInteraction(point.getId());
    }

    public static PendingIntent getPendingIntent(Context context)
    {
        final Intent i = new Intent(context, MapsMe.class);
        i.putExtra(EXTRA_FROM_MWM, true);
        return PendingIntent.getActivity(context, 0, i, 0);
    }
}
