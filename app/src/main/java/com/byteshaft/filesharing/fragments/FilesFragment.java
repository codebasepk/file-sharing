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
    private File path;
    private HashMap<String, String> foldersList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.files_fragment, container, false);
        path = new File(Environment.getExternalStorageDirectory() + "");
        folderList = new ArrayList<>();
        foldersList = new HashMap<>();
        gridLayout = (GridView) rootView.findViewById(R.id.folders_grid);
        adapter = new Adapter(getActivity().getApplicationContext(),
                R.layout.delegate_folder, folderList);
        gridLayout.setAdapter(adapter);
        gridLayout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i("TAG", "Folder "+ foldersList.get(folderList.get(i)));
            }
        });
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION);
        } else {
            new GetFiles().execute();
        }
        return rootView;
    }

    class GetFiles extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            searchFolderRecursive(path);
            return null;
        }

        private void searchFolderRecursive(File dir) {
            String pdfPattern = ".pdf";
            String txtPattern = ".txt";
            String docPattern = ".doc";
            String apkPatterns = ".apk";
            String zipPattern = ".zip";

            File listFile[] = dir.listFiles();

            if (listFile != null) {
                for (int i = 0; i < listFile.length; i++) {

                    if (listFile[i].isDirectory()) {
                        if (!listFile[i].getParentFile().getName().equals("Android"))
                        searchFolderRecursive(listFile[i]);
                    } else {
                        if (listFile[i].getName().endsWith(pdfPattern) ||
                                listFile[i].getName().endsWith(txtPattern) ||
                                listFile[i].getName().endsWith(docPattern) ||
                                listFile[i].getName().endsWith(apkPatterns) ||
                                listFile[i].getName().endsWith(zipPattern)) {
                            File file = listFile[i];
                            if (!folderList.contains(file.getParentFile().getName())) {
                                Log.i("File", "" + listFile[i].getParentFile());
                                foldersList.put(file.getParentFile().getName(),
                                        file.getParentFile().toString());
                                folderList.add(file.getParentFile().getName());
                                publishProgress();
                            }
                        }
                    }
                }
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            adapter.notifyDataSetChanged();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case STORAGE_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new GetFiles().execute();
                } else {
                    Toast.makeText(getActivity(), "permission denied!", Toast.LENGTH_SHORT).show();
                }
        }
    }

    class Adapter extends ArrayAdapter<ArrayList<String>> {

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
            viewHolder.folderName.setText(folderList.get(position));

            return convertView;

        }

        @Override
        public int getCount() {
            return folderList.size();

        }
    }

    class ViewHolder {
        TextView folderName;
        ImageView folderImage;

    }

}
