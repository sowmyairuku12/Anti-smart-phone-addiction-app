package com.example.antismartphoneaddictionapp.Utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class Constants {
    private static final String PREF_NAME = "ANTISMARTPHONEADDICTION";
    private static SharedPreferences sharedPreferences;

    public static long FOREGROUND_CHECK_TIME = 7200000; // 2 hours default
    public static int EXPIRY_TIME = 2; // 2 Hours default
    public static long TEMP_EXPIRY_TIME = 3600000; // 3600000 (1 hour)  //120000 (2 minutes)  //300000 (5 minutes)

    public static final long OPT = 1234;
    public static String PHONE_NUMBER = "";

    //Methods to update the Share preference with the time
    public static void init(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Load saved values or use default ones
        FOREGROUND_CHECK_TIME = sharedPreferences.getLong("FOREGROUND_CHECK_TIME", 7200000);
        EXPIRY_TIME = sharedPreferences.getInt("EXPIRY_TIME", 2);
        TEMP_EXPIRY_TIME = sharedPreferences.getLong("TEMP_EXPIRY_TIME", 3600000);
        PHONE_NUMBER = sharedPreferences.getString("PHONE_NUMBER", "");
    }

    public static void updateValues(Context context, long foregroundCheckTime, int expiryTime, long tempExpiryTime, String phoneNumber) {
        // Save new values to SharedPreferences
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putLong("FOREGROUND_CHECK_TIME", foregroundCheckTime);
        editor.putInt("EXPIRY_TIME", expiryTime);
        editor.putLong("TEMP_EXPIRY_TIME", tempExpiryTime);
        editor.putString("PHONE_NUMBER", phoneNumber);
        editor.apply();

        // Update constants
        FOREGROUND_CHECK_TIME = foregroundCheckTime;
        EXPIRY_TIME = expiryTime;
        TEMP_EXPIRY_TIME = tempExpiryTime;
        PHONE_NUMBER = phoneNumber;
    }

    // Optional: get phone number (can also use PHONE_NUMBER directly)
    public static String getPhoneNumber(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString("PHONE_NUMBER", "");
    }
}
