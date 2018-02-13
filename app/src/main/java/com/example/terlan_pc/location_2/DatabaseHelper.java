package com.example.terlan_pc.location_2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

/**
 * Created by Terlan-Pc on 2/8/2018.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "loc.db";
    private static final String TABLE_NAME = "locations";
    
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table locations (id integer primary key autoincrement, latitude VARCHAR(10), longitude VARCHAR(10), waited integer, CONSTRAINT loc_unique UNIQUE (latitude, longitude) )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists locations");
        onCreate(db);
    }

    public int insert(String latitude, String longitude, int waited)
    {
        Cursor chk = getDB().rawQuery("SELECT id,waited FROM locations WHERE latitude=? AND longitude=?",new String[] {latitude, longitude});

        if(chk.getCount() == 0)
        {
            ContentValues contentValues = new ContentValues();
            contentValues.put("latitude", latitude);
            contentValues.put("longitude", longitude);
            contentValues.put("waited", waited);

            return ( getDB().insert("locations", null, contentValues) == -1 ? 1 : 2 );
        }
        else
        {
            chk.moveToNext();
            ContentValues contentValues = new ContentValues();
            contentValues.put("waited", chk.getInt(1) + waited);

            return ( getDB().update("locations", contentValues, "id = ?", new String[]{ chk.getString(0) }) == -1 ? 3 : 4 );
        }
    }

    public SQLiteDatabase getDB()
    {
        return this.getWritableDatabase();
    }
}
