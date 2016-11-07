package com.x10hosting.studybuddy.studybuddy;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by david on 14/9/16.
 */
public class APIRequestHandler {
    private static APIRequestHandler Instance;
    private RequestQueue mRequestQueue;
    private static Context mContext;

    private APIRequestHandler(Context context) {
        mContext = context;
        mRequestQueue = getRequestQueue();
    }

    public RequestQueue getRequestQueue() {
        if(mRequestQueue == null) mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public static synchronized APIRequestHandler getInstance(Context context) {
        if (Instance == null) {
            Instance = new APIRequestHandler(context);
        }
        return Instance;
    }
}
