package com.example.terlan_pc.location_2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class LocationLists extends AppCompatActivity {

    ListView lw_locations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_lists);

        lw_locations = (ListView) findViewById(R.id.lw_locations);

        String[] str = {"asd", "asd"};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, str);
        lw_locations.setAdapter(adapter);
    }
}
