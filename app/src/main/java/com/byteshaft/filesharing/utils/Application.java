package com.byteshaft.filesharing.utils;


import android.content.Context;
import android.content.SharedPreferences;

public class Application extends android.app.Application {

    public static final String FIRTS_TIME_KEY = "key";
    private static final String KEY_IS_RECEIVE_SUPPORTED = "receive_supported";

    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
    }

    public static Context getContext() {
        return sContext;
    }

    public static void saveBoolean(boolean value) {
        SharedPreferences sharedPreferences = getPreferenceManager();
        sharedPreferences.edit().putBoolean(Application.FIRTS_TIME_KEY, value).apply();
    }

    public static boolean isRunningFirstTime() {
        SharedPreferences sharedPreferences = getPreferenceManager();
        return sharedPreferences.getBoolean(Application.FIRTS_TIME_KEY, true);
    }

    public static SharedPreferences getPreferenceManager() {
        return getContext().getSharedPreferences("shared_prefs", MODE_PRIVATE);
    }

    public static boolean isSupportedToReceive() {
        SharedPreferences sharedPreferences = getPreferenceManager();
        return sharedPreferences.getBoolean(KEY_IS_RECEIVE_SUPPORTED, true);
    }

    public static void setIsReceiveSupported(boolean isSupported) {
        SharedPreferences sharedPreferences = getPreferenceManager();
        sharedPreferences.edit().putBoolean(KEY_IS_RECEIVE_SUPPORTED, isSupported).apply();
    }
}
