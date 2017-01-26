package com.byteshaft.filesharing.utils;


import android.content.Context;

public class Application extends android.app.Application {

    private static Context sContext;
    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
    }

    public static Context getContext() {
        return sContext;
    }
}
