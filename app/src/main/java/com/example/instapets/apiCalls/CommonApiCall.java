package com.example.instapets.apiCalls;

import android.app.Activity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.instapets.utilities.ApplicationClass;
import com.example.instapets.utilities.SharedPrefUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * This class manages the API calls whenever we need to use the phpMyAdmin server with our php files
 * (such as checking for the existence of a user or registering a new user).
 */
public class CommonApiCall {

    /**
     * Initiates the API call and handles the response and error callbacks.
     */
    public void Call() {
        // ...
    }

    /**
     * Constructor for the CommonApiCall class.
     *
     * @param activity     The Android Activity from which the API call is made.
     * @param url          The URL for the API call.
     * @param map          A map containing parameters for the API call.
     * @param serviceType  A string identifying the type of service being called.
     */
    public CommonApiCall(Activity activity, String url, Map<String, String> map, String serviceType) {
        // ...
    }

    /**
     * Interface for handling API response and error callbacks.
     */
    public interface ResponseHandler {
        /**
         * Called when a successful API response is received.
         *
         * @param response     The response data from the API.
         * @param serviceType  The type of service associated with the response.
         */
        void onResponse(String response, String serviceType);

        /**
         * Called when an error occurs during the API call.
         *
         * @param error        The VolleyError representing the error.
         * @param serviceType  The type of service associated with the error.
         */
        void onError(VolleyError error, String serviceType);
    }
}