package com.example.instapets.utilities;

import android.app.Application;
//Volley library for making network requests
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * This class is responsible for initializing the application instance.
 * When you install the application, this class runs first and initializes a few essential variables.
 * It is referenced in the manifest file.
 */
public class ApplicationClass extends Application {
    public static final String TAG = ApplicationClass.class.getSimpleName();
    private static ApplicationClass mInstance;

    // Ensures that there will be no extra requests from the app with the same user.
    private RequestQueue mRequestQueue;

    /**
     * Get the singleton instance of the ApplicationClass.
     * @return The instance of the ApplicationClass.
     */
    public static synchronized ApplicationClass getInstance() {
        return mInstance;
    }

    /**
     * This override is called when the application starts.
     * It sets the mInstance to the instance of the Application class.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    /**
     * This method returns an instance of RequestQueue created using the Volley library.
     * If the mRequestQueue is null, it initializes a new RequestQueue using the application context.
     *
     * @return The RequestQueue instance for making network requests.
     */
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // If mRequestQueue is null, initialize a new RequestQueue using the application context.
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }

    /**
     * Add a request to the RequestQueue and tag it with a specific tag.
     *
     * @param req The request to be added to the queue.
     * @param <T> The type of the request.
     */
       public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }
}