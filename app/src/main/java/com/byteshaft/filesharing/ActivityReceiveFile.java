package com.byteshaft.filesharing;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.byteshaft.filesharing.utils.RadarView;

public class ActivityReceiveFile extends AppCompatActivity {

    RadarView mRadarView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_file);

        mRadarView = (RadarView) findViewById(R.id.radarView);
        mRadarView.setShowCircles(true);
        startAnimation(mRadarView);
    }

    public void stopAnimation(View view) {
        if (mRadarView != null) mRadarView.stopAnimation();
    }

    public void startAnimation(View view) {
        if (mRadarView != null) mRadarView.startAnimation();
    }
}
