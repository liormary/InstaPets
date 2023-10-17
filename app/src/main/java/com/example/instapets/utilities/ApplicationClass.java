package com.example.instapets.utilities;

import android.app.Application;
//Volley library for making network requests
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * this class is responsible to initialize instance.
 * Whenever you install the application this class runs first and initialise few variable first.
 * Its been used inside manifest.
 */
public class ApplicationClass extends Application {
    public static final String TAG = ApplicationClass.class.getSimpleName();
    private static ApplicationClass mInstance;
    private RequestQueue mRequestQueue;

    public static synchronized ApplicationClass getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }
}
