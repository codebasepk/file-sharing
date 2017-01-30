package com.byteshaft.filesharing;

import android.app.Dialog;
import android.app.ProgressDialog;
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
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.byteshaft.filesharing.utils.Helpers;
import com.byteshaft.filesharing.utils.Hotspot;
import com.github.siyamed.shapeimageview.CircularImageView;

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

import pl.bclogic.pulsator4droid.library.PulsatorLayout;

public class ActivityReceiveFile extends AppCompatActivity {

    private boolean mNotInitialized;
    private CircularImageView imageView;
    private TextView mUserName;
    private TextView mStatusText;
    private String user;
    private Hotspot mHotspot;
    private PulsatorLayout pulsator;
    private ProgressDialog pDialog;
    public static final int progress_bar_type = 0;

    private RoundCornerProgressBar mProgressBar;
    private FrameLayout progressLayout;
    private TextView percentAge;
    private TextView uploadDetails;

    private long mSize;
    private long mSent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_file);

        uploadDetails = (TextView) findViewById(R.id.file_number);
        mProgressBar = (RoundCornerProgressBar) findViewById(R.id.progressbar_Horizontal);
        progressLayout = (FrameLayout) findViewById(R.id.progress_layout);
        percentAge = (TextView) findViewById(R.id.percentage);

        user = PreferenceManager.getDefaultSharedPreferences(
                getApplicationContext()).getString("username", "User");
        pulsator = (PulsatorLayout) findViewById(R.id.pulsator);
        pulsator.start();

        imageView = (CircularImageView) findViewById(R.id.image_view);
        mStatusText = (TextView) findViewById(R.id.tv_status);
        mUserName = (TextView) findViewById(R.id.user_name);
        mUserName.setText("Username: " + user);
        mHotspot = new Hotspot(getApplicationContext());
//        toggleWifi();
    }

    private void toggleWifi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(getApplicationContext())) {
                startActivity(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS));
                mNotInitialized = true;
                return;
            }
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!isSharingWiFi()) {
                mHotspot.createForM(Helpers.generateSSID(
                        user, String.valueOf(serverSocket.getLocalPort())));
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("Hotspot");
                alertDialogBuilder.setMessage("Android M or above devices prohibits auto starting " +
                        "Wifi-Hotspot please set manually.").setCancelable(false)
                        .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                            }
                        });
                alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        finish();
                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        } else {
            incomingFileRequestThread.start();
        }
    }

    public boolean isSharingWiFi() {
        try
        {
            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            final Method method = wifiManager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true); //in the case of visibility change in future APIs
            return (Boolean) method.invoke(wifiManager);
        }
        catch (final Throwable ignored)
        {
        }

        return false;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case progress_bar_type: // we set this to 0
                pDialog = new ProgressDialog(this);
                pDialog.setMessage("Receiving file. Please wait...");
                pDialog.setIndeterminate(false);
                pDialog.setMax(100);
                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pDialog.setCancelable(true);
                pDialog.show();
                return pDialog;
            default:
                return null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        toggleWifi();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mHotspot.destroy();
    }

    private Thread incomingFileRequestThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                int bytesRead;
                ServerSocket serverSocket = new ServerSocket(0);
                mHotspot.create(Helpers.generateSSID(
                        user, String.valueOf(serverSocket.getLocalPort())));
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    InputStream in = clientSocket.getInputStream();
                    DataInputStream clientData = new DataInputStream(in);
                    String metadata = clientData.readUTF();
                    final JSONObject jsonObject = new JSONObject(metadata);
                    File mainDirectory = new File(
                            Environment.getExternalStorageDirectory()
                                    + File.separator
                                    + "FileShare"
                                    + File.separator
                                    + jsonObject.optString("type"));
                    if (!mainDirectory.exists()) {
                        mainDirectory.mkdirs();
                    }
                    final File outputFile = new File(
                            mainDirectory.getAbsolutePath() + "/" + jsonObject.optString("name"));
                    OutputStream output = new FileOutputStream(outputFile);
                    long size = clientData.readLong();
                    mSent = 0;
                    mSize = size;
                    byte[] buffer = new byte[8192];
                    System.out.println(jsonObject);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            uploadDetails.setText(
                                    jsonObject.optString("currentFileNumber") + "/" + jsonObject.optString("filesCount"));
                        }
                    });
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressBar.setMax(100);
                        }
                    });
                    while (size > 0 && (bytesRead = clientData.read(
                            buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                        output.write(buffer, 0, bytesRead);
                        size -= bytesRead;
                        mSent += bytesRead;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mStatusText.setText("Receiving Files..");
                                mProgressBar.setProgress((int) ((float) mSent / mSize * 100));
                            }
                        });
                    }
//                    output.flush();
                    output.close();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mStatusText.setText("All Done !!");
                            Toast.makeText(
                                    getApplicationContext(),
                                    outputFile.getAbsolutePath(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    });
}
