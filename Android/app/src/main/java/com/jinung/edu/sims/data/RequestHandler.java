package com.jinung.edu.sims.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

@SuppressLint({"StaticFieldLeak"})
class RequestHandler {
    private static Context mCtx;
    private static RequestHandler mInstance;
    private RequestQueue mRequestQueue = getRequestQueue();

    private RequestHandler(Context context) {
        mCtx = context;
    }

    public static synchronized RequestHandler getInstance(Context context) {
        RequestHandler requestHandler;
        synchronized (RequestHandler.class) {
            if (mInstance == null) {
                mInstance = new RequestHandler(context.getApplicationContext());
            }
            requestHandler = mInstance;
        }
        return requestHandler;
    }

    private RequestQueue getRequestQueue() {
        if (this.mRequestQueue == null) {
            this.mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return this.mRequestQueue;
    }

    /* access modifiers changed from: package-private */
    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    static boolean isConnected(Context context) {
//        NetworkInfo activeNetwork = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
//        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return false;
    }
}
