package com.example.quickstart;

import android.app.Application;
import android.content.Context;

/**
 * Created by Zsuzska on 2017. 10. 09..
 */

public class MyApp extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        MyApp.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return MyApp.context;
    }
}