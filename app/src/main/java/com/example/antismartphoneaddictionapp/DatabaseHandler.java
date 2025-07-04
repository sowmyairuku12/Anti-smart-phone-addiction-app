package com.example.antismartphoneaddictionapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.antismartphoneaddictionapp.Models.LocalAppModel;
import com.example.antismartphoneaddictionapp.Models.RestrictedAppModel;
import com.example.antismartphoneaddictionapp.Utility.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "anti_smart_phone.db";
    private static final String TABLE_APPS = "apps";

    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "packageName";
    private static final String KEY_DATE_TIME = "dateTime";

    //NEW TABLE & COLUMNS
    private static final String TABLE_RESTRICTED_APPS = "restricted_apps";  // New table name
    private static final String KEY_RESTRICTED_ID = "id";
    private static final String KEY_RESTRICTED_PACKAGE_NAME = "packageName";
    private static final String KEY_RESTRICTED_DATE = "date";
    private static final String KEY_RESTRICTED_TIME = "time";
    private static final String KEY_RESTRICTED_EXPIRY_TIME = "expiryTime";
    private static final String KEY_TEMP_UNLOCK_EXPIRY_TIME = "tempUnlockExpiryTime";



    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_APP_TABLE = "CREATE TABLE " + TABLE_APPS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_DATE_TIME + " TEXT" + ")";

        // Create the new RestrictedApps table
        String CREATE_RESTRICTED_APPS_TABLE = "CREATE TABLE " + TABLE_RESTRICTED_APPS + "("
                + KEY_RESTRICTED_ID + " INTEGER PRIMARY KEY,"
                + KEY_RESTRICTED_PACKAGE_NAME + " TEXT,"
                + KEY_RESTRICTED_DATE + " TEXT,"
                + KEY_RESTRICTED_TIME + " TEXT,"
                + KEY_RESTRICTED_EXPIRY_TIME + " TEXT,"
                + KEY_TEMP_UNLOCK_EXPIRY_TIME +" TEXT" + ")";  // New column
        db.execSQL(CREATE_RESTRICTED_APPS_TABLE);

        db.execSQL(CREATE_APP_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESTRICTED_APPS);

        onCreate(db);
    }

    void addApp(LocalAppModel appModel) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, appModel.getPackageName());
        values.put(KEY_DATE_TIME, appModel.getDateTime());


        db.insert(TABLE_APPS, null, values);
        db.close();
    }

    public ArrayList<LocalAppModel> getAllApps() {
        ArrayList<LocalAppModel> appModels = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_APPS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                LocalAppModel appModel = new LocalAppModel();
                appModel.setId(Integer.parseInt(cursor.getString(0)));
                appModel.setPackageName(cursor.getString(1));
                appModel.setDateTime(cursor.getString(2));
                appModels.add(appModel);
            } while (cursor.moveToNext());
        }
        return appModels;
    }

    public int updateApp(LocalAppModel appModel) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, appModel.getPackageName());
        values.put(KEY_DATE_TIME, appModel.getDateTime());

        return db.update(TABLE_APPS, values, KEY_ID + " = ?",
                new String[]{String.valueOf(appModel.getId())});
    }

    public void deleteApp(LocalAppModel appModel) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_APPS, KEY_ID + " = ?",
                new String[]{String.valueOf(appModel.getId())});
        db.close();
    }

    //NEW Methods to add a restricted app
    void addRestrictedApp(RestrictedAppModel appModel) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_RESTRICTED_PACKAGE_NAME, appModel.getPackageName());
        values.put(KEY_RESTRICTED_DATE, appModel.getDate());
        values.put(KEY_RESTRICTED_TIME, appModel.getTime());
        values.put(KEY_RESTRICTED_EXPIRY_TIME, appModel.getExpiryTime());
        values.put(KEY_TEMP_UNLOCK_EXPIRY_TIME, appModel.getTempUnlockExpiryTime());  // Add the new column

        db.insert(TABLE_RESTRICTED_APPS, null, values);
        db.close();
    }

    // Method to get all restricted apps
    public ArrayList<RestrictedAppModel> getAllRestrictedApps() {
        ArrayList<RestrictedAppModel> appModels = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_RESTRICTED_APPS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                RestrictedAppModel appModel = new RestrictedAppModel();
                appModel.setId(Integer.parseInt(cursor.getString(0)));
                appModel.setPackageName(cursor.getString(1));
                appModel.setDate(cursor.getString(2));
                appModel.setTime(cursor.getString(3));
                appModel.setExpiryTime(cursor.getString(4));
                appModel.setTempUnlockExpiryTime(cursor.getString(5));  // Fetch the new column
                appModels.add(appModel);
            } while (cursor.moveToNext());
        }
        return appModels;
    }

    // Method to update TempUnlockExpiryTime for a restricted app
    public void updateTempUnlockExpiryTime(int appId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Get current time and add 2 minutes for the temporary unlock expiry time
        long currentTimeMillis = System.currentTimeMillis();
        long tempUnlockExpiryTime = currentTimeMillis + (Constants.TEMP_EXPIRY_TIME);  // Add 2 minutes (in milliseconds)

        // Format the time to "yyyy-MM-dd HH:mm:ss" format
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String formattedExpiryTime = sdf.format(new Date(tempUnlockExpiryTime));

        // Update TempUnlockExpiryTime in the database for the app with the given ID
        ContentValues values = new ContentValues();
        values.put(KEY_TEMP_UNLOCK_EXPIRY_TIME, formattedExpiryTime);

        // Update the restricted app record with the new TempUnlockExpiryTime
        db.update(TABLE_RESTRICTED_APPS, values, KEY_RESTRICTED_ID + " = ?", new String[]{String.valueOf(appId)});
        db.close();
    }


    // Method to update a restricted app
    public int updateRestrictedApp(RestrictedAppModel appModel) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_RESTRICTED_PACKAGE_NAME, appModel.getPackageName());
        values.put(KEY_RESTRICTED_DATE, appModel.getDate());
        values.put(KEY_RESTRICTED_TIME, appModel.getTime());
        values.put(KEY_RESTRICTED_EXPIRY_TIME, appModel.getExpiryTime());
        values.put(KEY_TEMP_UNLOCK_EXPIRY_TIME, appModel.getTempUnlockExpiryTime());

        return db.update(TABLE_RESTRICTED_APPS, values, KEY_RESTRICTED_ID + " = ?",
                new String[]{String.valueOf(appModel.getId())});
    }

    // Method to delete a restricted app
    public void deleteRestrictedApp(RestrictedAppModel appModel) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_RESTRICTED_APPS, KEY_RESTRICTED_ID + " = ?",
                new String[]{String.valueOf(appModel.getId())});
        db.close();
    }
}
