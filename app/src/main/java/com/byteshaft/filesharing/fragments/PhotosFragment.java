package com.byteshaft.filesharing.fragments;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;

import com.byteshaft.filesharing.ActivitySendFile;
import com.byteshaft.filesharing.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;


public class PhotosFragment extends Fragment {

    private GridView gridLayout;
    private ArrayList<String> photoList;
    private Adapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.photos_fragment, container, false);
        photoList = new ArrayList<>();
        gridLayout = (GridView) rootView.findViewById(R.id.photo_grid);
        adapter = new Adapter(getActivity().getApplicationContext(),
                R.layout.delegate_photo_fragment, photoList);
        gridLayout.setAdapter(adapter);
        gridLayout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                File file = new File(photoList.get(i));
                if (!ActivitySendFile.selectedHashMap.containsKey(file.getName())) {
                    ActivitySendFile.selectedHashMap.put(file.getName(), file.toString());
                    ((CheckBox) view.findViewById(R.id.photo_checkbox)).setChecked(true);
                    ((CheckBox) view.findViewById(R.id.photo_checkbox)).setVisibility(View.VISIBLE);
                } else {
                    ActivitySendFile.selectedHashMap.remove(file.getName());
                    ((CheckBox) view.findViewById(R.id.photo_checkbox)).setChecked(false);
                    ((CheckBox) view.findViewById(R.id.photo_checkbox)).setVisibility(View.GONE);
                }
                ActivitySendFile.getInstance().setSelection();
            }
        });
        new GetAllImages().execute();
        return rootView;
    }

    class GetAllImages extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getAllShownImages();
                }
            });

            return null;
        }
    }

    public  void getAllShownImages() {
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;
        String absolutePathOfImage = null;
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = { MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME };

        cursor = getActivity().getContentResolver().query(uri, projection, null,
                null,  MediaStore.Files.FileColumns.DATE_ADDED + " DESC");

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);
            File file = new File(absolutePathOfImage);
            if (!photoList.contains(absolutePathOfImage)) {
                photoList.add(absolutePathOfImage);
                adapter.notifyDataSetChanged();
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
                convertView = getActivity().getLayoutInflater().inflate(R.layout.delegate_photo_fragment, parent, false);
                viewHolder.folderImage = (ImageView) convertView.findViewById(R.id.folder_image);
                viewHolder.photoCheckBox = (CheckBox) convertView.findViewById(R.id.photo_checkbox);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            File f = new File(folderList.get(position));
            if (ActivitySendFile.selectedHashMap.containsKey(f.getName())) {
                viewHolder.photoCheckBox.setVisibility(View.VISIBLE);
                viewHolder.photoCheckBox.setChecked(true);
            } else {
                viewHolder.photoCheckBox.setVisibility(View.INVISIBLE);
                viewHolder.photoCheckBox.setChecked(false);
            }
            Picasso.with(getActivity())
                    .load(f)

                    .resize(250, 250)
                    .centerCrop()
                    .into(viewHolder.folderImage);
            return convertView;
        }

        @Override
        public int getCount() {
            return folderList.size();
        }
    }

    private class ViewHolder {
        ImageView folderImage;
        CheckBox photoCheckBox;
    }
}
