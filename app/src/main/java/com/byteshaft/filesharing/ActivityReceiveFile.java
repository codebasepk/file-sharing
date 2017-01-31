package com.byteshaft.filesharing;

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

import static android.R.attr.data;
import static android.R.attr.dial;

public class ActivityReceiveFile extends AppCompatActivity {

    private boolean mNotInitialized;
    private CircularImageView imageView;
    private TextView mUserName;
    private TextView mStatusText;
    private String user;
    private Hotspot mHotspot;
    private PulsatorLayout pulsator;

    private RoundCornerProgressBar mProgressBar;
    private FrameLayout progressLayout;
    private TextView percentAge;
    private TextView uploadDetails;

    private long mSize;
    private long mSent;
    private static final int OPEN_SETTING = 0;
    private static final int OPEN_HOTSPOT = 1;

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
        mUserName.setText(user);
        mHotspot = new Hotspot(getApplicationContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.i("TAG", "settings " + Settings.System.canWrite(getApplicationContext()));
            if (!Settings.System.canWrite(getApplicationContext())) {
                startActivityForResult(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS), OPEN_SETTING);
                mNotInitialized = true;
                return;
            } else {
                Log.i("TAG", "settings else boolean " + mNotInitialized);
//                if (mNotInitialized) {
                    incomingFileRequestThread.start();
                    mNotInitialized = false;
//                }
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
                incomingFileRequestThread.start();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (Settings.System.canWrite(getApplicationContext())) {
//                if (mNotInitialized) {
//                    incomingFileRequestThread.start();
//                    mNotInitialized = false;
//                }
//            } else {
//                startActivity(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS));
//            }
//        }
    }

    public boolean isSharingWiFi() {
        try {
            WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            final Method method = manager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true); //in the case of visibility change in future APIs
            return (Boolean) method.invoke(manager);
        } catch (final Throwable ignored) {
        }

        return false;
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Log.i("TAG", "creating for M ");
                    mHotspot.createForM(Helpers.generateSSID(
                            user, String.valueOf(serverSocket.getLocalPort())));
                    Log.i("TAG", "wifi sharing" + isSharingWiFi());
                    if (!isSharingWiFi()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder alertDialogBuilder = new
                                        AlertDialog.Builder(ActivityReceiveFile.this);
                                alertDialogBuilder.setTitle("");
                                alertDialogBuilder.setMessage("Android M or above prohibits auto starting" +
                                        " Wifi-hotspot. Please set manually.").setCancelable(false)
                                        .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.dismiss();
                                                Intent intent = new Intent();
                                                intent.setAction(Settings.ACTION_WIRELESS_SETTINGS);
//                        intent.setAction(Intent.ACTION_VIEW);
//                        intent.setClassName("com.android.settings.wifi", "com.android.settings.wifi.WifiApDialog");
                                                startActivityForResult(intent, OPEN_HOTSPOT);

                                            }
                                        });
                                alertDialogBuilder.setNegativeButton("Quit", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        ActivityReceiveFile.this.finish();
                                    }
                                });
                                AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.show();
                            }
                        });
                    }
                } else {
                    mHotspot.create(Helpers.generateSSID(
                            user, String.valueOf(serverSocket.getLocalPort())));
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
                                mProgressBar.setProgress((int) ((float) mSent / mSize * 100));
                            }
                        });
                    }
//                    output.flush();
                    output.close();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
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