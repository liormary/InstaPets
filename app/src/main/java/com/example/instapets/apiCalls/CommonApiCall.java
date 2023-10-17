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
 * this class manages the API calls whenever
 * we need to use the phpMyAdmin server with our php files
 * (such as check for existence of a user or register a new user)
 */
public class CommonApiCall {

    private Activity activity;
    private String url;
    SharedPrefUtils prefUtils;
    private Map<String, String> param;
    private ResponseHandler responseHandler;
    private String serviceType;

    public void Call() {
        StringRequest sr = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                responseHandler.onResponse(response, serviceType);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                responseHandler.onError(error, serviceType);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                return param;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                return headers;
            }

        };
        sr.setRetryPolicy(new DefaultRetryPolicy(10000, 1, 1.0f));
        ApplicationClass.getInstance().addToRequestQueue(sr);
    }

    public CommonApiCall(Activity activity, String url, Map<String, String> map, String serviceType) {
        this.activity = activity;
        this.responseHandler = (ResponseHandler) activity;
        this.url = url;
        this.param = map;
        this.serviceType = serviceType;
        this.prefUtils = new SharedPrefUtils(activity);
    }

    public interface ResponseHandler {
        void onResponse(String response, String serviceType);

        void onError(VolleyError error, String serviceType);
    }

}
