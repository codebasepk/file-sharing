package com.byteshaft.filesharing;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.byteshaft.filesharing.utils.Helpers;
import com.byteshaft.filesharing.utils.Hotspot;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import pl.bclogic.pulsator4droid.library.PulsatorLayout;

public class ActivityReceiveFile extends AppCompatActivity {

    private boolean mNotInitialized;
//    private RadarView mRadarView;
    private Hotspot mHotspot;
    PulsatorLayout pulsator;
    // FIXME: Make this configurable by user, must be >= 10 characters.
    private final String USERNAME = "testing123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_file);
        pulsator = (PulsatorLayout) findViewById(R.id.pulsator);
        pulsator.start();
//        mRadarView = (RadarView) findViewById(R.id.radarView);
//        mRadarView.setShowCircles(true);
//        startAnimation(mRadarView);
        mHotspot = new Hotspot(getApplicationContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(getApplicationContext())) {
                startActivity(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS));
                mNotInitialized = true;
                return;
            }
        }
        incomingFileRequestThread.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(getApplicationContext())) {
                if (mNotInitialized) {
                    incomingFileRequestThread.start();
                    mNotInitialized = false;
                }
            } else {
                startActivity(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS));
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mHotspot.destroy();
    }
//
//    public void stopAnimation(View view) {
//        if (mRadarView != null) mRadarView.stopAnimation();
//    }
//
//    public void startAnimation(View view) {
//        if (mRadarView != null) mRadarView.startAnimation();
//    }

    private Thread incomingFileRequestThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                int bytesRead;
                ServerSocket serverSocket = new ServerSocket(0);
                mHotspot.create(Helpers.generateSSID(
                        USERNAME, String.valueOf(serverSocket.getLocalPort())));
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    InputStream in = clientSocket.getInputStream();
                    DataInputStream clientData = new DataInputStream(in);
                    String fileName = clientData.readUTF();
                    final File f = new File(
                            Environment.getExternalStorageDirectory() + "/" + fileName);
                    OutputStream output = new FileOutputStream(f);
                    long size = clientData.readLong();
                    byte[] buffer = new byte[8192];
                    while (size > 0 && (bytesRead = clientData.read(
                            buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                        output.write(buffer, 0, bytesRead);
                        size -= bytesRead;
                    }
                    output.close();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(
                                    getApplicationContext(),
                                    f.getAbsolutePath(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    });
}
