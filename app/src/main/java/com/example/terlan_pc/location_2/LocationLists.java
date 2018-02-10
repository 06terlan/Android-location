package com.example.terlan_pc.location_2;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class LocationLists extends AppCompatActivity {

    private ListView lw_locations;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_lists);
        lw_locations = (ListView) findViewById(R.id.lw_locations);
        String[] str = {};

        db = new DatabaseHelper(this);
        Cursor all = db.getDB().rawQuery("SELECT latitude,longitude,waited FROM locations",null);
        Toast.makeText(this, "Count: "+all.getCount(), Toast.LENGTH_SHORT).show();

        if(all.getCount() != 0)
        {
            int i = 0;
            str = new String[all.getCount()];
            while (all.moveToNext()){
                str[i] = "Latitude: " + all.getString(0) + " Longitude: " + all.getString(1) + " waited: " +  (int)(all.getInt(2)/60) + ":" + (int)(all.getInt(2)%60);
                i++;
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, str);
        lw_locations.setAdapter(adapter);
    }
}
