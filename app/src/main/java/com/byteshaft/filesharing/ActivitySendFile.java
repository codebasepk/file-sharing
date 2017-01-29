package com.byteshaft.filesharing;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.byteshaft.filesharing.fragments.FilesFragment;
import com.byteshaft.filesharing.fragments.MusicFragment;
import com.byteshaft.filesharing.fragments.PhotosFragment;
import com.byteshaft.filesharing.fragments.VideosFragment;

import java.util.ArrayList;


public class ActivitySendFile extends AppCompatActivity {

    public static ArrayList<String> sendList;
    private Button selectedButton;
    private static ActivitySendFile sInstance;

    public static ActivitySendFile getInstance() {
        return sInstance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_file);
        sInstance = this;
        sendList = new ArrayList<>();
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        ViewPager mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        selectedButton = (Button) findViewById(R.id.selected);
        Button nextButton = (Button) findViewById(R.id.next);
        selectedButton.setText("Selected");
        setSelection();
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sendList.size() > 0) {
                    startActivity(new Intent(getApplicationContext(), PlaceholderPeersActivity.class));
                } else {
                    Toast.makeText(getApplicationContext(),
                            "No file selected", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void setSelection() {
        selectedButton.setText(String.format("selected(%d)", sendList.size()));
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    return new FilesFragment();
                case 1:
                    return new VideosFragment();
                case 2:
                    return new PhotosFragment();
                case 3:
                    return new MusicFragment();
                default:
                    return new FilesFragment();
            }
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Files";
                case 1:
                    return "Videos";
                case 2:
                    return "Photos";
                case 3:
                    return "Music";
            }
            return null;
        }
    }
}
