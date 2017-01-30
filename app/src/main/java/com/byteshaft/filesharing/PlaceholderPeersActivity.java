package com.byteshaft.filesharing;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.WorkerThread;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.byteshaft.filesharing.utils.Helpers;
import com.byteshaft.filesharing.utils.RadarView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;

import static android.R.attr.data;
import static com.byteshaft.filesharing.utils.Helpers.intToInetAddress;
import static com.byteshaft.filesharing.utils.Helpers.locationEnabled;

public class PlaceholderPeersActivity extends AppCompatActivity implements View.OnClickListener {
    private ScanResult mPeer;
    private boolean mConnectionRequested;
    private boolean mScanRequested;
    private WifiManager mWifiManager;
    private RadarView mRadarView;
    private FrameLayout radarLayout;
    private static final int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 0;
    private HashMap<Integer, ScanResult> mResults = new HashMap<>();
    private ImageButton mRefreshButton;
    private static final int LOCATION_OFF = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peers_list);
        mRefreshButton = (ImageButton) findViewById(R.id.button_refresh_peers);
        radarLayout = (FrameLayout) findViewById(R.id.radar_layout);
        mRefreshButton.setOnClickListener(this);
        mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        registerReceiver(
                mWifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mScanRequested = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            if (locationEnabled() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mWifiManager.startScan();
                mRadarView = (RadarView) findViewById(R.id.radarView);
                mRadarView.setShowCircles(true);
                startAnimation(mRadarView);
            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                mWifiManager.startScan();
                mRadarView = (RadarView) findViewById(R.id.radarView);
                mRadarView.setShowCircles(true);
                startAnimation(mRadarView);

            } else {
                Toast.makeText(this, "location not enabled", Toast.LENGTH_SHORT).show();
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setMessage("Location is not enabled");
                dialog.setPositiveButton("Turn on", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        // TODO Auto-generated method stub
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(myIntent, LOCATION_OFF);
                        //get gps
                    }
                });
                dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        // TODO Auto-generated method stub

                    }
                });
                dialog.show();
            }
            //do something, permission was previously granted; or legacy device
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Do something with granted permission

            if (locationEnabled() || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                mWifiManager.startScan();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Toast.makeText(this, "location not enabled", Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                    dialog.setMessage("Location is not enabled");
                    dialog.setPositiveButton("Turn on", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            // TODO Auto-generated method stub
                            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(myIntent, LOCATION_OFF);
                            //get gps
                        }
                    });
                    dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            // TODO Auto-generated method stub

                        }
                    });
                    dialog.show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case LOCATION_OFF:
                if (locationEnabled()) {
                    mWifiManager.startScan();
                    mRadarView = (RadarView) findViewById(R.id.radarView);
                    mRadarView.setShowCircles(true);
                    startAnimation(mRadarView);
                }
        }
    }

    public void stopAnimation(View view) {
        if (mRadarView != null) mRadarView.stopAnimation();
    }

    public void startAnimation(View view) {
        if (mRadarView != null) mRadarView.startAnimation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(
                wifiStateReceiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(wifiStateReceiver);
            unregisterReceiver(mWifiScanReceiver);
        } catch (IllegalArgumentException ignore) {

        }
    }

    private final BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) && mScanRequested) {
                mScanRequested = false;
                radarLayout = (FrameLayout) findViewById(R.id.radar_layout);
                mResults = new HashMap<>();
                List<ScanResult> mScanResults = mWifiManager.getScanResults();
                int index = 0;
                for (ScanResult result : mScanResults) {
                    if (result.SSID.startsWith("SH-")) {
                        Log.i("TAG", " Name " + result.SSID);
                        mResults.put(index, result);
                        showOnMap(result, index);
                        index++;
                    }
                }
                if (mResults.size() == 0) {
                    mRefreshButton.setVisibility(View.VISIBLE);
                    mRadarView.setVisibility(View.INVISIBLE);
                }
            }
        }
    };

    private void showOnMap(ScanResult result, int index) {
        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setFocusable(true);
        layout.setClickable(true);
        Log.i("TAG", "set id " + layout.getId());
        LinearLayout.LayoutParams params = new LinearLayout
                .LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        
        layout.setLayoutParams(params);
        ImageButton imageView = new ImageButton(getApplicationContext());
        imageView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        imageView.setId(index);
        int width = 80;
        int height = 80;
        LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(width,height);
        imageView.setLayoutParams(parms);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(R.mipmap.ic_launcher);
        imageView.requestLayout();
        TextView textView = new TextView(getApplicationContext());
        String[] ssidData = result.SSID.split("-");
        textView.setText(Helpers.decodeString(ssidData[1]));
        layout.addView(imageView);
        layout.addView(textView);
        layout.setX(18);
        layout.setY(18);
        if (radarLayout != null) {
            radarLayout.addView(layout);
        }
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view instanceof ImageButton) {
                    startActivity(new Intent(getApplicationContext(), SendProgressActivity.class));
                    Log.i("TAG", "true");
                    Log.i("TAG", "id " + view.getId());
                    mPeer = mResults.get(view.getId());
                    if (!mWifiManager.getConnectionInfo().getSSID().contains(mPeer.SSID)) {
                        System.out.println(String.format("Not connected to %s, trying to connect", mPeer.SSID));
                        connectToSSID(mPeer.SSID);
                    } else {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String hostIP = intToInetAddress(
                                        mWifiManager.getDhcpInfo().serverAddress).toString().replace("/", "");
                                int count = 0;
                                for (HashMap<String, String> fileItem : ActivitySendFile.sendList.values()) {
                                    count++;
                                    sendFileOverNetwork(
                                            hostIP,
                                            Helpers.decodeString(mPeer.SSID.split("-")[2]),
                                            new File(fileItem.get("path")).getAbsolutePath(),
                                            fileItem.get("type"),
                                            count,
                                            ActivitySendFile.sendList.size()
                                    );
                                }
                            }
                        }).start();
                    }
                }
            }
        });
    }

    private void connectToSSID(String SSID) {
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + SSID + "\"";
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        final WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiManager.addNetwork(conf);
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration i : list) {
            if (i.SSID != null && i.SSID.equals("\"" + SSID + "\"")) {
                wifiManager.disconnect();
                wifiManager.enableNetwork(i.networkId, true);
                wifiManager.reconnect();
                mConnectionRequested = true;
                break;
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_refresh_peers:
                mScanRequested = true;
                if (locationEnabled() && ActivityCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    mWifiManager.startScan();
                    mRadarView.setVisibility(View.VISIBLE);
                    mRefreshButton.setVisibility(View.INVISIBLE);
                } else {
                    Toast.makeText(this, "location not enabled", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @WorkerThread
    public static void sendFileOverNetwork(String hostIP, String port, String filePath,
                                           String fileType, int currentFile, int filesCount) {
        try {
            Socket sock = new Socket(hostIP, Integer.valueOf(port));
            File myFile = new File(filePath);
            byte[] fileBytesArray = new byte[(int) myFile.length()];
            FileInputStream fis = new FileInputStream(myFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            DataInputStream dis = new DataInputStream(bis);
            dis.readFully(fileBytesArray, 0, fileBytesArray.length);
            OutputStream os = sock.getOutputStream();


            int bytes_read = 0;
            int bytesReadTotal = 0;
            int buffer_size = 1024;
            byte[] buffer = new byte[buffer_size];
            int progress = 0;

            //Sending file name and file size to the server
            DataOutputStream dos = new DataOutputStream(os);
            dos.writeUTF(
                    getMetadata(
                            myFile.getName(), fileType, myFile.length(), currentFile, filesCount));
            dos.writeLong(fileBytesArray.length);
//            while ((bytes_read = fis.read(buffer, 0, buffer_size)) > 0) {
//                dos.write(buffer, 0, bytes_read);
//                bytesReadTotal += bytes_read;
//                progress = (int) (100.0f * bytesReadTotal / myFile.length());
//                Log.i("asd", "direct progress: " + progress);
//            }
            dos.write(fileBytesArray, 0, fileBytesArray.length);
            dos.flush();

            //Closing socket
            sock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getMetadata(
            String name, String type, long size, int currentFileNumber, int filesCount) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("name", name);
            obj.put("size", String.valueOf(size));
            obj.put("type", type);
            obj.put("filesCount", String.valueOf(filesCount));
            obj.put("currentFileNumber", String.valueOf(currentFileNumber));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj.toString();
    }

    private BroadcastReceiver wifiStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo networkInfo =
                        intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (ConnectivityManager.TYPE_WIFI == networkInfo.getType() &&
                        networkInfo.isConnected() && mConnectionRequested) {
                    mConnectionRequested = false;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            int count = 0;
                            try {
                                Thread.sleep(10000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            String hostIP = intToInetAddress(
                                    mWifiManager.getDhcpInfo().serverAddress).toString().replace("/", "");
                            for (HashMap<String, String> fileItem : ActivitySendFile.sendList.values()) {
                                count++;
                                sendFileOverNetwork(
                                        hostIP,
                                        Helpers.decodeString(mPeer.SSID.split("-")[2]),
                                        new File(fileItem.get("path")).getAbsolutePath(),
                                        fileItem.get("type"),
                                        count,
                                        ActivitySendFile.sendList.size()
                                );
                            }
                        }
                    }).start();
                }
            }
        }
    };
}
