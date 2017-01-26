package com.byteshaft.filesharing;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.byteshaft.filesharing.adapters.PeersListAdapter;
import com.byteshaft.filesharing.utils.Helpers;
import com.byteshaft.filesharing.utils.RadarView;

import java.util.ArrayList;
import java.util.List;

import static com.byteshaft.filesharing.utils.Helpers.intToInetAddress;

public class PlaceholderPeersActivity extends AppCompatActivity implements View.OnClickListener {
    private String mPort;
    private boolean mConnectionRequested;
    private boolean mScanRequested;
    private WifiManager mWifiManager;
    private RadarView mRadarView;
    private FrameLayout radarLayout;
    private static final int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peers_list);
//        mImagePath = getIntent().getExtras().getString("image_url");
        Button refreshButton = (Button) findViewById(R.id.button_refresh_peers);
        radarLayout = (FrameLayout) findViewById(R.id.radar_layout);
        refreshButton.setOnClickListener(this);
        mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        registerReceiver(
                mWifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mScanRequested = true;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        }else{
            if (Helpers.locationEnabled()) {
                mWifiManager.startScan();
            } else {
                Toast.makeText(this, "location not enabled", Toast.LENGTH_SHORT).show();
            }
            //do something, permission was previously granted; or legacy device
        }
        mRadarView = (RadarView) findViewById(R.id.radarView);
        mRadarView.setShowCircles(true);
        startAnimation(mRadarView);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Do something with granted permission
            if (Helpers.locationEnabled()) {
                mWifiManager.startScan();
            } else {
                Toast.makeText(this, "location not enabled", Toast.LENGTH_SHORT).show();
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
        unregisterReceiver(wifiStateReceiver);
        unregisterReceiver(mWifiScanReceiver);
    }

    private final BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) && mScanRequested) {
                mScanRequested = false;
                List<ScanResult> filteredResults = new ArrayList<>();
                List<ScanResult> mScanResults = mWifiManager.getScanResults();
                Log.i("TAG", " mScanResults "+ mScanResults.size());
                for (ScanResult result : mScanResults) {
                    Log.i("TAG", " Random "+ result.SSID);
                    if (result.SSID.startsWith("SH-")) {
                        Log.i("TAG", " Name "+ result.SSID);
                        filteredResults.add(result);
                        LinearLayout layout = new LinearLayout(getApplicationContext());
                        LinearLayout.LayoutParams params = new LinearLayout
                                .LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        layout.setLayoutParams(params);
                        ImageView imageView = new ImageView(getApplicationContext());
                        imageView.setImageResource(R.mipmap.ic_launcher);
                        TextView textView = new TextView(getApplicationContext());
                        textView.setText(result.SSID);
                        layout.addView(imageView);
                        layout.addView(textView);
                        layout.setX(10);
                        layout.setY(10);
                        radarLayout.addView(layout);
                    }
                }
                PeersListAdapter peersListAdapter = new PeersListAdapter(
                        getApplicationContext(), filteredResults);
//                setListAdapter(peersListAdapter);
            }
        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_refresh_peers:
//                getListView().setAdapter(null);
                mScanRequested = true;
                if (Helpers.locationEnabled() && ActivityCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    mWifiManager.startScan();
                } else {
                    Toast.makeText(this, "location not enabled", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

//    @Override
//    protected void onListItemClick(ListView l, View v, int position, long id) {
//        super.onListItemClick(l, v, position, id);
//        ScanResult device = (ScanResult) l.getAdapter().getItem(position);
//        String[] ssidData = device.SSID.split("-");
//        mPort = Helpers.decodeString(ssidData[2]);
//        if (mWifiManager.getConnectionInfo().getSSID().contains(device.SSID)) {
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    String hostIP = intToInetAddress(
//                            mWifiManager.getDhcpInfo().serverAddress).toString().replace("/", "");
//                    sendFileOverNetwork(hostIP, mPort, mImagePath);
//                }
//            }).start();
//        } else {
//            WifiConfiguration conf = new WifiConfiguration();
//            conf.SSID = "\"" + device.SSID + "\"";
//            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//            final WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
//            wifiManager.addNetwork(conf);
//            List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
//            for( WifiConfiguration i : list ) {
//                if(i.SSID != null && i.SSID.equals("\"" + device.SSID + "\"")) {
//                    wifiManager.disconnect();
//                    wifiManager.enableNetwork(i.networkId, true);
//                    wifiManager.reconnect();
//                    mConnectionRequested = true;
//                    break;
//                }
//            }
//        }
//    }

    private BroadcastReceiver wifiStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo networkInfo =
                        intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (networkInfo.isConnected() && mConnectionRequested) {
                    mConnectionRequested = false;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String hostIP = intToInetAddress(
                                    mWifiManager.getDhcpInfo().serverAddress).toString().replace("/", "");
                            try {
                                Thread.sleep(6000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
//                            sendFileOverNetwork(hostIP, mPort, mImagePath);
                        }
                    }).start();
                }
            }
        }
    };
}
