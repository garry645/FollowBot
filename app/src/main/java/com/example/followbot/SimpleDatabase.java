package com.example.followbot;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SimpleDatabase extends SQLiteOpenHelper{
    public static final String DATABASE_NAME = "wifimap.db";
    public static final String AP_TABLE = "access_points";
    public static final String READINGS_TABLE = "readings";
    public static final String AP_CREATE = "CREATE TABLE 'access_points' "
            + "('layout_id' TEXT NOT NULL ,'ssid' TEXT NOT NULL,'mac_id' TEXT NOT NULL )";
    public static final String READINGS_CREATE = "CREATE TABLE 'readings' ('layout_id' TEXT NOT NULL , "
            + "'position_id' TEXT NOT NULL ,"
            + " 'ssid' TEXT NOT NULL , 'mac_id' TEXT NOT NULL , 'rssi' INTEGER NOT NULL )";

    private HashMap hp;

    public SimpleDatabase(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, null, 1);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(AP_CREATE);
        db.execSQL(READINGS_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

       // db.execSQL("DROP TABLE IF EXISTS " + AP_CREATE);
        //db.execSQL("DROP TABLE IF EXISTS " + READINGS_CREATE);
        onCreate(db);
    }

    public void deleteReading(String layout_id, String position_id) {
        SQLiteDatabase db = getWritableDatabase();
        String[] args = new String[] { layout_id, position_id };
        db.delete(READINGS_TABLE, "layout_id=? and position_id=?", args);

    }

    public void deleteLayout(String layout_id) {
        SQLiteDatabase db = getWritableDatabase();
        String[] args = new String[] {layout_id };
        db.delete(AP_TABLE,"layout_id=?",args);
        db.delete(READINGS_TABLE, "layout_id=?", args);


    }
    public ArrayList<String> getLayout(String layout_id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select distinct layout_id from "
                + READINGS_TABLE, null);
        ArrayList<String> result = new ArrayList<String>();
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            result.add(cursor.getString(0));
            cursor.moveToNext();
        }
        return result;

    }

    public ArrayList<Router> getFriendlyWifis(String layout_id) {
        ArrayList<Router> result = new ArrayList<Router>();
        System.out.println(layout_id);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select ssid,mac_id from " + AP_TABLE
                + " where layout_id=?", new String[] { layout_id });
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            result.add(new Router(cursor.getString(0), cursor.getString(1)));
            cursor.moveToNext();
        }
        return result;

    }

    public void deleteFriendlyWifis(String layout_id) {
        SQLiteDatabase db = getWritableDatabase();
        String[] args = new String[] { layout_id };
        db.delete(AP_TABLE, "layout_id=?", args);

    }

    public void addFriendlyWifis(String layout_id, ArrayList<Router> wifis) {
        deleteFriendlyWifis(layout_id);
        SQLiteDatabase db = getWritableDatabase();
        for (int i = 0; i < wifis.size(); i++) {
            ContentValues cv = new ContentValues();
            cv.put("layout_id", layout_id);
            cv.put("ssid", wifis.get(i).getSSID());
            cv.put("mac_id", wifis.get(i).getBSSID());
            db.insert(AP_TABLE, null, cv);
        }
        System.out.println("Adding done");
    }

    public ArrayList<String> getPositions(String layout_id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select distinct position_id from "
                        + READINGS_TABLE + " where layout_id=?",
                new String[] { layout_id });
        ArrayList<String> result = new ArrayList<String>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            result.add(cursor.getString(0));
            cursor.moveToNext();
        }
        return result;
    }

    public boolean addReadings(String layout_id, PosData positionData) {
        Log.v("Just Before db : ", positionData.toString());
        deleteReading(layout_id, positionData.getName());

        SQLiteDatabase db = getWritableDatabase();
        for (Map.Entry<String, Integer> e : positionData.getValues().entrySet()) {
            ContentValues cv = new ContentValues();
            cv.put("layout_id", layout_id);
            cv.put("position_id", positionData.getName());
            cv.put("ssid",positionData.routers.get(e.getKey()));
            cv.put("mac_id",e.getKey());
            cv.put("rssi", e.getValue());
            Log.v(e.getKey(), e.getValue().toString());
            db.insert(READINGS_TABLE, null, cv);
        }
        System.out.println("Adding done");
        return true;

    }


    public ArrayList<PosData> getReadings(String layout_id) {
        HashMap<String, PosData> positions = new HashMap<String, PosData>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select distinct * from " + READINGS_TABLE
                + " where layout_id='" + layout_id + "'", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String position_id = cursor.getString(1);
            Router router = new Router(cursor.getString(2), cursor.getString(3));
            Log.v(cursor.getString(2), cursor.getInt(4) + "");
            if (positions.containsKey(position_id)) {

                positions.get(position_id).addValue(router, cursor.getInt(4));
            } else {
                PosData positionData = new PosData(
                        cursor.getString(1));
                positionData.addValue(router, cursor.getInt(4));
                positions.put(position_id, positionData);
            }
            cursor.moveToNext();

        }
        System.out.println("Reading done");
        ArrayList<PosData> result = new ArrayList<PosData>();
        for (Map.Entry<String, PosData> e : positions.entrySet())
            result.add(e.getValue());
        return result;

    }
}

