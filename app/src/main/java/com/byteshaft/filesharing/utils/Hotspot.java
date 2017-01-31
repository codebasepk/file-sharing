package com.byteshaft.filesharing.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.byteshaft.filesharing.ActivityReceiveFile;

import java.lang.reflect.Method;

import static com.byteshaft.filesharing.ActivityReceiveFile.isSharingWiFi;

public class Hotspot {

    private boolean mCreated;
    private Context mContext;
    private boolean mWasWifiDisabled;
    private WifiManager mWifiManager;
    private String mName;

    public Hotspot(Context context) {
        mContext = context;
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
    }

    private void turnOffWifiIfOn() {
        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
            mWasWifiDisabled = true;
        }
    }

    public boolean isCreated() {
        try {
            Method getWifiApConfigurationMethod = mWifiManager.getClass().getMethod("getWifiApConfiguration");
            WifiConfiguration netConfig = (WifiConfiguration) getWifiApConfigurationMethod.invoke(mWifiManager);
            return netConfig.SSID.equals(mName);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isAPCreated() {
        try {
            Method isWifiApEnabledMethod = mWifiManager.getClass().getMethod("isWifiApEnabled");
            return (boolean) (Boolean) isWifiApEnabledMethod.invoke(mWifiManager);
        } catch (Exception e) {
            return false;
        }
    }

    public void create(String name, Activity activity , int CODE) {
        mName = name;
        turnOffWifiIfOn();
        WifiConfiguration netConfig = new WifiConfiguration();
        netConfig.SSID = name;
        netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        try {
            Method setWifiApMethod = mWifiManager.getClass().getMethod(
                    "setWifiApEnabled", WifiConfiguration.class, boolean.class);
            mCreated = (boolean) (Boolean) setWifiApMethod.invoke(mWifiManager, netConfig, true);
            while (!isAPCreated()) {
            }
//            Method getWifiApStateMethod = mWifiManager.getClass().getMethod("getWifiApState");
//            int apstate = (Integer) getWifiApStateMethod.invoke(mWifiManager);
        } catch (java.lang.reflect.InvocationTargetException e) {
            if (e.getCause().getMessage().contains("android.permission.CONNECTIVITY_INTERNAL")) {
                AppGlobals.setIsReceiveSupported(false);
                createForM(name, activity, CODE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createForM(String name, Activity activity , int CODE) {
        try {
            WifiManager wifiManager = (WifiManager) AppGlobals.getContext().getSystemService(Context.WIFI_SERVICE);
            Method getConfigMethod = wifiManager.getClass().getMethod("getWifiApConfiguration");
            WifiConfiguration wifiConfig = (WifiConfiguration) getConfigMethod.invoke(wifiManager);
            wifiConfig.SSID = name;
            wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            Method setConfigMethod = wifiManager.getClass().getMethod("setWifiApConfiguration", WifiConfiguration.class);
            setConfigMethod.invoke(wifiManager, wifiConfig);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_WIRELESS_SETTINGS);
        activity.startActivityForResult(intent, CODE);
    }

    public void destroy(final Activity activity, final int CLOSE_HOTSPOT) {
        try {
            try {
                Method setWifiApMethod = mWifiManager.getClass().getMethod(
                        "setWifiApEnabled", WifiConfiguration.class, boolean.class);
                mCreated = (boolean) setWifiApMethod.invoke(mWifiManager, null, false);
            } catch (java.lang.reflect.InvocationTargetException e) {
                if (e.getCause().getMessage().contains("android.permission.CONNECTIVITY_INTERNAL")) {
                    new android.os.Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (isSharingWiFi()) {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_WIRELESS_SETTINGS);
                                activity.startActivityForResult(intent, CLOSE_HOTSPOT);
                            } else {
                                activity.onBackPressed();
                            }
                        }
                    }, 500);
                }
            } catch (Exception e) {
                Log.e("HOTSPOT", "", e);
            }
            if (mWasWifiDisabled) {
                mWifiManager.setWifiEnabled(true);
                mWasWifiDisabled = false;
            }
        } finally {

        }
    }
}
