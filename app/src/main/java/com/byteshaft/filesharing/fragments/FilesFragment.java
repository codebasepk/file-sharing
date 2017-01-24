package com.byteshaft.filesharing.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.byteshaft.filesharing.R;

import java.util.ArrayList;

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
        gridLayout = (GridView) rootView.findViewById(R.id.photo_grid);
        adapter = new Adapter(getActivity().getApplicationContext(),
                R.layout.delegate_folder, folderList);
        gridLayout.setAdapter(adapter);
        gridLayout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i("TAG", "click");
                Bundle bundle = new Bundle();
                bundle.putString("folder", folderList.get(i));
                FolderDetailFragment nextFrag = new FolderDetailFragment();
                nextFrag.setArguments(bundle);
                getFragmentManager().beginTransaction()
                        .replace(R.id.frame_folder, nextFrag, "Folder_detail")
                        .addToBackStack("folder_detail")
                        .commit();
            }
        });
        return rootView;
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
