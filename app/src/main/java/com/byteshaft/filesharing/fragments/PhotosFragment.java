package com.byteshaft.filesharing.fragments;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;

import com.byteshaft.filesharing.R;
import com.byteshaft.filesharing.activities.ActivitySendFile;
import com.byteshaft.filesharing.utils.Helpers;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;


public class PhotosFragment extends Fragment {

    private ArrayList<String> photoList;
    private Adapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.photos_fragment, container, false);
        photoList = new ArrayList<>();
        GridView gridLayout = (GridView) rootView.findViewById(R.id.photo_grid);
        adapter = new Adapter(getActivity().getApplicationContext(),
                R.layout.delegate_photo_fragment, photoList);
        gridLayout.setAdapter(adapter);
        gridLayout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                CheckBox photoCheckbox = (CheckBox) view.findViewById(R.id.photo_checkbox);
                File file = new File(photoList.get(i));
                String filePath = file.getAbsolutePath();
                HashMap<String, String> fileItem = Helpers.getFileMetadataMap(
                        file.getAbsolutePath(), "photos");
                if (!ActivitySendFile.sendList.containsKey(filePath)) {
                    ActivitySendFile.sendList.put(filePath, fileItem);
                    photoCheckbox.setChecked(true);
                    photoCheckbox.setVisibility(View.VISIBLE);
                } else {
                    ActivitySendFile.sendList.remove(filePath);
                    photoCheckbox.setChecked(false);
                    photoCheckbox.setVisibility(View.GONE);
                }
                ActivitySendFile.getInstance().setSelection();
            }
        });
        new GetAllImages().execute();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
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
        int column_index_data;
        String absolutePathOfImage;
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = { MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME };

        cursor = getActivity().getContentResolver().query(uri, projection, null,
                null,  MediaStore.Files.FileColumns.DATE_ADDED + " DESC");

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);
            if (!photoList.contains(absolutePathOfImage)) {
                photoList.add(absolutePathOfImage);
                adapter.notifyDataSetChanged();
            }
        }
    }

    private class Adapter extends ArrayAdapter<ArrayList<String>> {

        private ArrayList<String> folderList;
        private ViewHolder viewHolder;

        Adapter(Context context, int resource, ArrayList<String> folderList) {
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
            if (ActivitySendFile.sendList.containsKey(f.getAbsolutePath())) {
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
