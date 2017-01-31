package com.byteshaft.filesharing.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.byteshaft.filesharing.ActivitySendFile;
import com.byteshaft.filesharing.R;
import com.byteshaft.filesharing.utils.Helpers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class FilesFragment extends Fragment {

    private ArrayList<String> folderList;
    private Adapter adapter;

    private ListView listView;
    public ArrayList<String> zipList;
    public ArrayList<String> documentList;
    public ArrayList<String> eBook;
    private File path;
    private int selectedFolder = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.files_fragment, container, false);
        folderList = new ArrayList<>();
        folderList.add("Document");
        folderList.add("Zip");
        folderList.add("E-Book");
        path = new File(Environment.getExternalStorageDirectory() + "");
        listView = (ListView) rootView.findViewById(R.id.list_view);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                CheckBox selectionCheckbox = (CheckBox) view.findViewById(R.id.selectionCheckbox);
                File file = null;
                HashMap<String, String> fileItem = null;
                if (selectedFolder == 0) {
                    file = new File(documentList.get(i));
                    fileItem = Helpers.getFileMetadataMap(file.getAbsolutePath(), "documents");
                } else if (selectedFolder == 1) {
                    file = new File(zipList.get(i));
                    fileItem = Helpers.getFileMetadataMap(file.getAbsolutePath(), "zips");
                } else if (selectedFolder == 2) {
                    file = new File(eBook.get(i));
                    fileItem = Helpers.getFileMetadataMap(file.getAbsolutePath(), "ebooks");
                }
                if (file == null) {
                    return;
                }
                String filePath = file.getAbsolutePath();
                if (!ActivitySendFile.sendList.containsKey(filePath)) {
                    ActivitySendFile.sendList.put(filePath, fileItem);
                    selectionCheckbox.setChecked(true);
                    selectionCheckbox.setVisibility(View.VISIBLE);
                } else {
                    ActivitySendFile.sendList.remove(filePath);
                    selectionCheckbox.setChecked(false);
                    selectionCheckbox.setVisibility(View.GONE);
                }
                adapter.notifyDataSetChanged();
                ActivitySendFile.getInstance().setSelection();
            }
        });
        eBook = new ArrayList<>();
        documentList = new ArrayList<>();
        zipList = new ArrayList<>();
        GridView gridLayout = (GridView) rootView.findViewById(R.id.photo_grid);
        adapter = new Adapter(getActivity().getApplicationContext(),
                R.layout.delegate_folder, folderList);
        gridLayout.setAdapter(adapter);
        gridLayout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedFolder = i;
                setUpAdapter(folderList.get(i));

            }
        });
        new GetFiles().execute();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    private void setUpAdapter(String selected) {
        switch (selected) {
            case "Zip":
                FilesAdapter filesAdapter = new FilesAdapter(getActivity().getApplicationContext(),
                        R.layout.delegate_folder, zipList);
                listView.setAdapter(filesAdapter);
                break;
            case "Document":
                filesAdapter = new FilesAdapter(getActivity().getApplicationContext(),
                        R.layout.delegate_folder, documentList);
                listView.setAdapter(filesAdapter);
                break;
            case "E-Book":
                filesAdapter = new FilesAdapter(getActivity().getApplicationContext(),
                        R.layout.delegate_folder, eBook);
                listView.setAdapter(filesAdapter);
                break;
        }
        adapter.notifyDataSetChanged();
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
                convertView = getActivity().getLayoutInflater().inflate(R.layout.delegate_folder, parent, false);
                viewHolder.folderImage = (ImageView) convertView.findViewById(R.id.folder_image);
                viewHolder.folderName = (TextView) convertView.findViewById(R.id.folder_name);
                viewHolder.relativeLayout = (RelativeLayout) convertView.findViewById(R.id.folder_background);
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
            if (selectedFolder == position) {
                viewHolder.relativeLayout.setBackgroundResource(R.drawable.grid_background);
            } else {
                viewHolder.relativeLayout.setBackgroundResource(0);
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
        RelativeLayout relativeLayout;

    }

    private class FilesAdapter extends ArrayAdapter<ArrayList<String>> {

        private ArrayList<String> folderList;

        FilesAdapter(Context context, int resource, ArrayList<String> folderList) {
            super(context, resource);
            this.folderList = folderList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FilesHolder filesHolder;
            if (convertView == null) {
                filesHolder = new FilesHolder();
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.delegate_folder_detail, parent, false);
                filesHolder.fileImage = (ImageView) convertView.findViewById(R.id.file_image);
                filesHolder.fileName = (TextView) convertView.findViewById(R.id.file_name);
                filesHolder.fileSize = (TextView) convertView.findViewById(R.id.file_size);
                filesHolder.mCheckBox = (CheckBox) convertView.findViewById(R.id.selectionCheckbox);
                convertView.setTag(filesHolder);
            } else {
                filesHolder = (FilesHolder) convertView.getTag();
            }
            File file = new File(folderList.get(position));
            filesHolder.fileName.setText(file.getName());
            filesHolder.fileSize.setText(Formatter.formatFileSize(getActivity(),file.length()));
            if (ActivitySendFile.sendList.containsKey(file.getAbsolutePath())) {
                filesHolder.mCheckBox.setVisibility(View.VISIBLE);
                filesHolder.mCheckBox.setChecked(true);
            } else {
                filesHolder.mCheckBox.setVisibility(View.INVISIBLE);
                filesHolder.mCheckBox.setChecked(false);
            }
            notifyDataSetChanged();

            return convertView;

        }

        @Override
        public int getCount() {
            return folderList.size();
        }
    }

    private class FilesHolder {
        TextView fileName;
        TextView fileSize;
        ImageView fileImage;
        CheckBox mCheckBox;

    }

    class GetFiles extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    searchFolderRecursive(path);
                }
            });
            return null;
        }

        private void searchFolderRecursive(File dir) {
            String pdfPattern = ".pdf";
            String txtPattern = ".txt";
            String docPattern = ".doc";
            String zipPattern = ".zip";

            File listFile[] = dir.listFiles();

            if (listFile != null) {
                for (File aListFile : listFile) {
                    if (aListFile.isDirectory()) {
                        if (!aListFile.getParentFile().getName().equals("Android"))
                            searchFolderRecursive(aListFile);
                    } else {
                        if (aListFile.getName().endsWith(pdfPattern) ||
                                aListFile.getName().endsWith(txtPattern) ||
                                aListFile.getName().endsWith(docPattern) ||
                                aListFile.getName().endsWith(zipPattern)) {
                            if (aListFile.getName().endsWith(".zip") &&
                                    !zipList.contains(aListFile.toString())) {
                                zipList.add(aListFile.toString());
                                adapter.notifyDataSetChanged();
                                publishProgress();
                            } else if (aListFile.getName().endsWith(".pdf") ||
                                    aListFile.getName().endsWith(".doc") && !documentList.contains(aListFile.toString())) {
                                documentList.add(aListFile.toString());
                                adapter.notifyDataSetChanged();
                                publishProgress();
                            } else if (aListFile.getName().endsWith(".txt") && eBook.contains(aListFile.toString())) {
                                eBook.add(aListFile.toString());
                                adapter.notifyDataSetChanged();
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
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }
}
