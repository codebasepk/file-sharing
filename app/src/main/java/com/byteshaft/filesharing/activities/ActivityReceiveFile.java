package com.byteshaft.filesharing.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.byteshaft.filesharing.R;
import com.byteshaft.filesharing.utils.AppGlobals;
import com.byteshaft.filesharing.utils.Helpers;
import com.byteshaft.filesharing.utils.Hotspot;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;

import pl.bclogic.pulsator4droid.library.PulsatorLayout;

public class ActivityReceiveFile extends AppCompatActivity {

    private boolean mNotInitialized;
    private String user;
    private Hotspot mHotspot;

    private long mSize;
    private long mSent;
    private static final int OPEN_SETTING = 0;
    private static final int OPEN_HOTSPOT = 1;
    private static final int CLOSE_HOTSPOT = 2;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_file);
        user = PreferenceManager.getDefaultSharedPreferences(
                getApplicationContext()).getString("username", "User");
        PulsatorLayout pulsator = (PulsatorLayout) findViewById(R.id.pulsator);
        pulsator.start();
        TextView mUserName = (TextView) findViewById(R.id.user_name);
        mUserName.setText("Username: " + user);
        mHotspot = new Hotspot(getApplicationContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.i("TAG", "settings " + Settings.System.canWrite(getApplicationContext()));
            if (!Settings.System.canWrite(getApplicationContext())) {
                startActivityForResult(
                        new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS), OPEN_SETTING);
                mNotInitialized = true;
            } else {
                Log.i("TAG", "settings else boolean " + mNotInitialized);
                incomingFileRequestThread.start();
                mNotInitialized = false;
            }
        } else {
            incomingFileRequestThread.start();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case OPEN_SETTING:
                if (isSharingWiFi()) {
                    incomingFileRequestThread.start();
                } else {
                    finish();
                }
                break;
            case CLOSE_HOTSPOT:
                finish();
        }
    }

    public static boolean isSharingWiFi() {
        try {
            WifiManager manager = (WifiManager) AppGlobals.getContext().getSystemService(
                    Context.WIFI_SERVICE);
            final Method method = manager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true); //in the case of visibility change in future APIs
            return (Boolean) method.invoke(manager);
        } catch (Throwable ignored) {
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        mHotspot.destroy(ActivityReceiveFile.this, CLOSE_HOTSPOT);
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isSharingWiFi()) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_WIRELESS_SETTINGS);
                    startActivityForResult(intent, CLOSE_HOTSPOT);
                } else {
                    finish();
                }
            }
        }, 500);
    }

    private Thread incomingFileRequestThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                int bytesRead;
                final ServerSocket serverSocket = new ServerSocket(0);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Log.i("TAG", "creating for M ");
                    Log.i("TAG", "wifi sharing" + isSharingWiFi());
                    if (!isSharingWiFi()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                final AlertDialog.Builder alertDialogBuilder = new
                                        AlertDialog.Builder(ActivityReceiveFile.this);
                                alertDialogBuilder.setTitle("");
                                alertDialogBuilder.setMessage(
                                        "Android 6.0 or above prohibits auto starting" +
                                                " Wifi-hotspot. Please enable manually.").setCancelable(false)
                                        .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialogInterface, int id) {
                                                dialogInterface.dismiss();
                                                mHotspot.createForM(Helpers.generateSSID(
                                                        user,
                                                        String.valueOf(serverSocket.getLocalPort())
                                                        ),
                                                        ActivityReceiveFile.this, OPEN_HOTSPOT);
                                            }
                                        });
                                alertDialogBuilder.setNegativeButton("Quit", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        finish();
                                    }
                                });
                                alertDialog = alertDialogBuilder.create();
                                alertDialog.show();
                            }
                        });
                    }
                } else {
                    if (!isSharingWiFi()) {
                        mHotspot.create(Helpers.generateSSID(
                                user,
                                String.valueOf(serverSocket.getLocalPort())),
                                ActivityReceiveFile.this, OPEN_HOTSPOT);
                    }
                }
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    InputStream in = clientSocket.getInputStream();
                    DataInputStream clientData = new DataInputStream(in);
                    String metadata = clientData.readUTF();
                    final JSONObject jsonObject = new JSONObject(metadata);
                    File mainDirectory = new File(
                            Environment.getExternalStorageDirectory()
                                    + File.separator
                                    + "iShare"
                                    + File.separator
                                    + jsonObject.optString("type"));
                    if (!mainDirectory.exists()) {
                        mainDirectory.mkdirs();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (ReceiveProgressActivity.getInstance() == null) {
                                startActivity(new Intent(getApplicationContext(),
                                        ReceiveProgressActivity.class));
                            }
                        }
                    });
                    Thread.sleep(500);
                    final File outputFile = new File(
                            mainDirectory.getAbsolutePath() + "/" + jsonObject.optString("name"));
                    ReceiveProgressActivity.getInstance().file.add(outputFile.getAbsolutePath());
                    OutputStream output = new FileOutputStream(outputFile);
                    long size = clientData.readLong();
                    mSent = 0;
                    mSize = size;
                    byte[] buffer = new byte[8192];
                    while (size > 0 && (bytesRead = clientData.read(
                            buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                        output.write(buffer, 0, bytesRead);
                        size -= bytesRead;
                        mSent += bytesRead;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ReceiveProgressActivity.getInstance().receiveProgressHashMap.put(outputFile.getAbsolutePath(), (int)
                                        ((float) mSent / mSize * 100));
                                ReceiveProgressActivity.getInstance().fileAdapter.notifyDataSetChanged();
                                ReceiveProgressActivity.getInstance().currentFileCounter.setText(
                                        jsonObject.optString("currentFileNumber"));
                                ReceiveProgressActivity.getInstance().totalFileCounter.setText(
                                        jsonObject.optString("filesCount"));
                            }
                        });
                    }
                    output.flush();
                    output.close();
                    if (jsonObject.optInt("currentFileNumber") == (jsonObject.optInt("filesCount") - 1)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                            }
                        });
                        Iterator entries = ReceiveProgressActivity.getInstance().receiveProgressHashMap.entrySet().iterator();
                        while (entries.hasNext()) {
                            Map.Entry thisEntry = (Map.Entry) entries.next();
                            Object key = thisEntry.getKey();
                            ReceiveProgressActivity.getInstance().receiveProgressHashMap.put(key.toString(), 100);
                            ReceiveProgressActivity.getInstance().fileAdapter.notifyDataSetChanged();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    });
}