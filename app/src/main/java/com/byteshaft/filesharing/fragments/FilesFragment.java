package com.byteshaft.filesharing.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.byteshaft.filesharing.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by shahid on 17/01/2017.
 */

public class FilesFragment extends Fragment {

    private GridView gridLayout;
    private ArrayList<String> folderList;
    private Adapter adapter;
    private static final int STORAGE_PERMISSION = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.files_fragment, container, false);
        folderList = new ArrayList<>();
        folderList.add("Document");
        folderList.add("Zip");
        folderList.add("E-Book");
        gridLayout = (GridView) rootView.findViewById(R.id.folders_grid);
        adapter = new Adapter(getActivity().getApplicationContext(),
                R.layout.delegate_folder, folderList);
        gridLayout.setAdapter(adapter);
        gridLayout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i("TAG", "click");
                Bundle bundle = new Bundle();
                bundle.putString("folder", folderList.get(i));
                FolderDetailFragment nextFrag= new FolderDetailFragment();
                nextFrag.setArguments(bundle);
                getFragmentManager().beginTransaction()
                        .replace(R.id.frame_folder, nextFrag, "Folder_detail")
                        .addToBackStack("folder_detail")
                        .commit();
            }
        });
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION);
        } else {

        }
        return rootView;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case STORAGE_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(getActivity(), "permission denied!", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private class Adapter extends ArrayAdapter<ArrayList<String>> {

        private ArrayList<String> folderList;
        private ViewHolder viewHolder;

        public Adapter(Context context, int resource, ArrayList<String> folderList) {
            super(context, resource);
            this.folderList = folderList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = getActivity().getLayoutInflater().inflate(R.layout.delegate_folder, parent, false);
                viewHolder.folderImage = (ImageView) convertView.findViewById(R.id.folder_image);
                viewHolder.folderName = (TextView) convertView.findViewById(R.id.folder_name);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            if (folderList.get(position).endsWith("Zip")) {
                viewHolder.folderImage.setImageResource(R.mipmap.zip);
            } else if (folderList.get(position).endsWith("Document")) {
                viewHolder.folderImage.setImageResource(R.mipmap.document);
            } else if (folderList.get(position).endsWith("E-Book")) {
                viewHolder.folderImage.setImageResource(R.mipmap.ebook);
            }
            viewHolder.folderName.setText(folderList.get(position));

            return convertView;

        }

        @Override
        public int getCount() {
            return folderList.size();
        }
    }

    private class ViewHolder {
        TextView folderName;
        ImageView folderImage;

    }

}
