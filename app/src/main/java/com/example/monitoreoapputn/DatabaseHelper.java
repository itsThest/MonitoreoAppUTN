package com.example.monitoreoapputn;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "vehicle_tracker.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_LOCATION = "location_data";
    private static final String TABLE_USERS = "users";
    private static final String CREATE_LOCATION_TABLE = "CREATE TABLE " + TABLE_LOCATION + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "latitude REAL, " +
            "longitude REAL, " +
            "timestamp INTEGER, " +
            "device_id TEXT)";
    private static final String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "username TEXT, " +
            "api_token TEXT)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        // Initialize with a default user
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_USERS + " (username, api_token) VALUES ('admin', 'secure_token_123')");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_LOCATION_TABLE);
        db.execSQL(CREATE_USERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    public void insertLocationData(double latitude, double longitude, long timestamp, String deviceId) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO " + TABLE_LOCATION + " (latitude, longitude, timestamp, device_id) VALUES (?, ?, ?, ?)",
                new Object[]{latitude, longitude, timestamp, deviceId});
    }

    public String getLocationData(long startTime, long endTime) {
        SQLiteDatabase db = getReadableDatabase();
        StringBuilder result = new StringBuilder("[");
        android.database.Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_LOCATION + " WHERE timestamp BETWEEN ? AND ?",
                new String[]{String.valueOf(startTime), String.valueOf(endTime)});
        boolean first = true;
        while (cursor.moveToNext()) {
            if (!first) result.append(",");
            result.append(String.format("{\"id\":%d,\"latitude\":%f,\"longitude\":%f,\"timestamp\":%d,\"device_id\":\"%s\"}",
                    cursor.getInt(0), cursor.getDouble(1), cursor.getDouble(2), cursor.getLong(3), cursor.getString(4)));
            first = false;
        }
        cursor.close();
        result.append("]");
        return result.toString();
    }

    public boolean validateToken(String token) {
        SQLiteDatabase db = getReadableDatabase();
        android.database.Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE api_token = ?", new String[]{token});
        boolean valid = cursor.getCount() > 0;
        cursor.close();
        return valid;
    }
}