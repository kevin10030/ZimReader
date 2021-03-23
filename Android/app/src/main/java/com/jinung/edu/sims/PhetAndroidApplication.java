package com.jinung.edu.sims;

import android.app.Application;
import android.content.Context;

public class PhetAndroidApplication extends Application {

    public static Context mContext;
    private static PhetAndroidApplication mInstance;

    public void onCreate() {
        mContext = getApplicationContext();
        super.onCreate();
        mInstance = this;
    }

    public static synchronized PhetAndroidApplication getInstance() {
        return mInstance;
    }

    public static Context getContext(){
        return mContext;
    }
}
