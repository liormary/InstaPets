package com.example.instapets.utilities;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;


/**
 * This class is meant for saving temporary values on the phone
 * (such as faster login for user to the application)
 */

public class SharedPrefUtils {
    public static final String MY_PREFS_NAME = "MiSentinelApp";

    private final Context activity;

    public SharedPrefUtils(Context activity) {
        this.activity = activity;
    }

    public String get(String key) {
        SharedPreferences prefs = activity.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(key, null);
    }

    public void setString(String key, String value) {
        SharedPreferences.Editor editor = activity.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.apply();
    }

}
