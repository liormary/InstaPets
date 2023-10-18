package com.example.instapets.utilities;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * This class is  is a simple key-value storage system in
 * Android that allows you to store data persistently across app sessions.
 *
 * meant for saving temporary values on the phone
 * (such as faster login for user to the application)
 */

public class SharedPrefUtils {

    //All data stored using this utility will be saved in a file named "MiSentinelApp".
    public static final String MY_PREFS_NAME = "MiSentinelApp";

    private final Context activity;

    public SharedPrefUtils(Context activity) {
        this.activity = activity;
    }

    /*
     * takes a String key as a parameter and retrieves the corresponding
     * value from the SharedPreferences. It returns the value associated
     * with the given key
     */
    public String get(String key) {
        SharedPreferences prefs = activity.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(key, null);
    }

    /*
     * creates an editor of the SharedPreferences and adds a new key&value pair to our
     * SharedPreferences obj
     */
    public void setString(String key, String value) {
        SharedPreferences.Editor editor = activity.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.apply();
    }
}