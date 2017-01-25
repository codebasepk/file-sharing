package com.byteshaft.filesharing;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.byteshaft.filesharing.adapters.PeersListAdapter;
import com.byteshaft.filesharing.utils.Helpers;

import java.util.ArrayList;
import java.util.List;

import static com.byteshaft.filesharing.utils.Helpers.intToInetAddress;
import static com.byteshaft.filesharing.utils.Helpers.sendFileOverNetwork;

public class PlaceholderPeersActivity extends ListActivity implements View.OnClickListener {
    private String mPort;
    private boolean mConnectionRequested;
    private ProgressBar mDiscoveryProgressBar;
    private boolean mScanRequested;
    private WifiManager mWifiManager;
    private String mImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peers_list);
        mImagePath = getIntent().getExtras().getString("image_url");
        Button refreshButton = (Button) findViewById(R.id.button_refresh_peers);
        refreshButton.setOnClickListener(this);
        mDiscoveryProgressBar = (ProgressBar) findViewById(R.id.progress_bar_searching);
        mDiscoveryProgressBar.setVisibility(View.VISIBLE);
        mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        registerReceiver(
                mWifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mScanRequested = true;
        mWifiManager.startScan();
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
    }

    private final BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) && mScanRequested) {
                mScanRequested = false;
                List<ScanResult> filteredResults = new ArrayList<>();
                List<ScanResult> mScanResults = mWifiManager.getScanResults();
                for (ScanResult result : mScanResults) {
                    if (result.SSID.startsWith("SH-")) {
                        filteredResults.add(result);
                    }
                }
                mDiscoveryProgressBar.setVisibility(View.GONE);
                PeersListAdapter peersListAdapter = new PeersListAdapter(
                        getApplicationContext(), filteredResults);
                setListAdapter(peersListAdapter);
            }
        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_refresh_peers:
                getListView().setAdapter(null);
                mDiscoveryProgressBar.setVisibility(View.VISIBLE);
                mScanRequested = true;
                mWifiManager.startScan();
                break;
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        ScanResult device = (ScanResult) l.getAdapter().getItem(position);
        String[] ssidData = device.SSID.split("-");
        mPort = Helpers.decodeString(ssidData[2]);
        if (mWifiManager.getConnectionInfo().getSSID().contains(device.SSID)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String hostIP = intToInetAddress(
                            mWifiManager.getDhcpInfo().serverAddress).toString().replace("/", "");
                    sendFileOverNetwork(hostIP, mPort, mImagePath);
                }
            }).start();
        } else {
            WifiConfiguration conf = new WifiConfiguration();
            conf.SSID = "\"" + device.SSID + "\"";
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            final WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
            wifiManager.addNetwork(conf);
            List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
            for( WifiConfiguration i : list ) {
                if(i.SSID != null && i.SSID.equals("\"" + device.SSID + "\"")) {
                    wifiManager.disconnect();
                    wifiManager.enableNetwork(i.networkId, true);
                    wifiManager.reconnect();
                    mConnectionRequested = true;
                    break;
                }
            }
        }
    }

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
                            sendFileOverNetwork(hostIP, mPort, mImagePath);
                        }
                    }).start();
                }
            }
        }
    };
}
