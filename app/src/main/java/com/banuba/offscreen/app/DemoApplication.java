package com.banuba.offscreen.app;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.util.Size;

import androidx.annotation.NonNull;

public class DemoApplication extends Application {

    public static final Size SIZE = new Size(1920, 1080);
    public static final String BNB_KEY = '';  // Put Valid Key here

    @SuppressLint("StaticFieldLeak")
    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
    }

    @NonNull
    public static Context getAppContext() {
        return sContext;
    }

}
