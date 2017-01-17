package com.byteshaft.filesharing.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.byteshaft.filesharing.R;

/**
 * Created by shahid on 17/01/2017.
 */

public class MusicFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.music_fragment, container, false);
        return rootView;
    }
}
