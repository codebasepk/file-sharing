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

import com.byteshaft.filesharing.ActivitySendFile;
import com.byteshaft.filesharing.R;
import com.byteshaft.filesharing.utils.ThumbnailCreationTask;

import java.io.File;
import java.util.ArrayList;

public class VideosFragment extends Fragment {

    public ArrayList<String> videoList;
    private Adapter adapter;
    private static VideosFragment sInstance;

    public static VideosFragment getInstance() {
        return sInstance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.videos_fragment, container, false);
        sInstance = this;
        videoList = new ArrayList<>();
        GridView gridLayout = (GridView) rootView.findViewById(R.id.video_grid);
        adapter = new Adapter(getActivity().getApplicationContext(),
                R.layout.delegate_video_fragment, videoList);
        gridLayout.setAdapter(adapter);
        gridLayout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                CheckBox videosCheckbox = (CheckBox) view.findViewById(R.id.videos_checkbox);
                    File file = new File(videoList.get(i));
                    if (!ActivitySendFile.selectedHashMap.containsKey(file.getName())) {
                        ActivitySendFile.selectedHashMap.put(file.getName(), file.toString());
                        videosCheckbox.setChecked(true);
                        videosCheckbox.setVisibility(View.VISIBLE);
                    } else {
                        ActivitySendFile.selectedHashMap.remove(file.getName());
                        videosCheckbox.setChecked(false);
                        videosCheckbox.setVisibility(View.GONE);
                    }
                ActivitySendFile.getInstance().setSelection();
            }
        });
        new GetVideosTask().execute();
        return rootView;
    }

    class GetVideosTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getAllShownVideos();
                }
            });
            return null;
        }
    }

    public  void getAllShownVideos() {
        Uri uri;
        Cursor cursor;
        int column_index_data;
        String absolutePathOfImage;
        uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        String[] projection = { MediaStore.MediaColumns.DATA,
                MediaStore.Video.Media._ID };

        cursor = getActivity().getContentResolver().query(uri, projection, null,
                null,  MediaStore.Files.FileColumns.DATE_ADDED + " DESC");

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);

        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);
            if (!videoList.contains(absolutePathOfImage)) {
                videoList.add(absolutePathOfImage);
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
                convertView = getActivity().getLayoutInflater().inflate(R.layout.delegate_video_fragment, parent, false);
                viewHolder.videoImage = (ImageView) convertView.findViewById(R.id.folder_video);
                viewHolder.checkbox = (CheckBox) convertView.findViewById(R.id.videos_checkbox);
                viewHolder.position = position;
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            File file = new File(folderList.get(position));
            new ThumbnailCreationTask(getActivity().getApplicationContext(), viewHolder, position).execute();
            if (ActivitySendFile.selectedHashMap.containsKey(file.getName())) {
                viewHolder.checkbox.setVisibility(View.VISIBLE);
                viewHolder.checkbox.setChecked(true);
            } else {
                viewHolder.checkbox.setVisibility(View.INVISIBLE);
                viewHolder.checkbox.setChecked(false);
            }
            return convertView;
        }

        @Override
        public int getCount() {
            return folderList.size();
        }
    }

    public class ViewHolder {
        public int position;
        public ImageView videoImage;
        public CheckBox checkbox;
    }
}
