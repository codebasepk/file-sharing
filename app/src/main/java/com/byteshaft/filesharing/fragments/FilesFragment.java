package com.byteshaft.filesharing.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.format.Formatter;
import android.util.Log;
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

import java.io.File;
import java.util.ArrayList;

public class FilesFragment extends Fragment {

    private GridView gridLayout;
    private ArrayList<String> folderList;
    private Adapter adapter;

    private FilesAdapter filesAdapter;
    private ListView listView;
    public ArrayList<String> zipList;
    public ArrayList<String> documentList;
    public ArrayList<String> eBook;
    private File path;
    private FilesHolder filesHolder;
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
                if (selectedFolder == 0) {
                    File file = new File(documentList.get(i));
                    if (!ActivitySendFile.selectedHashMap.containsKey(file.getName())) {
                        ActivitySendFile.selectedHashMap.put(file.getName(), file.toString());
                        ((CheckBox) view.findViewById(R.id.selectionCheckbox)).setChecked(true);
                        ((CheckBox) view.findViewById(R.id.selectionCheckbox)).setVisibility(View.VISIBLE);
                    } else {
                        ActivitySendFile.selectedHashMap.remove(file.getName());
                        ((CheckBox) view.findViewById(R.id.selectionCheckbox)).setChecked(false);
                        ((CheckBox) view.findViewById(R.id.selectionCheckbox)).setVisibility(View.GONE);
                    }
                } else if (selectedFolder == 1) {
                    File file = new File(zipList.get(i));
                    if (!ActivitySendFile.selectedHashMap.containsKey(file.getName())) {
                        ((CheckBox) view.findViewById(R.id.selectionCheckbox)).setChecked(true);
                        ((CheckBox) view.findViewById(R.id.selectionCheckbox)).setVisibility(View.VISIBLE);
                        ActivitySendFile.selectedHashMap.put(file.getName(), file.toString());
                    } else {
                        ActivitySendFile.selectedHashMap.remove(file.getName());
                        ((CheckBox) view.findViewById(R.id.selectionCheckbox)).setChecked(false);
                        ((CheckBox) view.findViewById(R.id.selectionCheckbox)).setVisibility(View.GONE);
                    }
                } else if (selectedFolder == 2) {
                    File file = new File(eBook.get(i));
                    if (!ActivitySendFile.selectedHashMap.containsKey(file.getName())) {
                        ActivitySendFile.selectedHashMap.put(file.getName(), file.toString());
                        ((CheckBox) view.findViewById(R.id.selectionCheckbox)).setChecked(true);
                        ((CheckBox) view.findViewById(R.id.selectionCheckbox)).setVisibility(View.VISIBLE);
                    } else {
                        ActivitySendFile.selectedHashMap.remove(file.getName());
                        ((CheckBox) view.findViewById(R.id.selectionCheckbox)).setChecked(false);
                        ((CheckBox) view.findViewById(R.id.selectionCheckbox)).setVisibility(View.GONE);
                    }
                }
                adapter.notifyDataSetChanged();
                ActivitySendFile.getInstance().setSelection();
            }
        });
        eBook = new ArrayList<>();
        documentList = new ArrayList<>();
        zipList = new ArrayList<>();
        gridLayout = (GridView) rootView.findViewById(R.id.photo_grid);
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


    private void setUpAdapter(String selected) {
        switch (selected) {
            case "Zip":
                filesAdapter = new FilesAdapter(getActivity().getApplicationContext(),
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

        public FilesAdapter(Context context, int resource, ArrayList<String> folderList) {
            super(context, resource);
            this.folderList = folderList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
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
            if (ActivitySendFile.selectedHashMap.containsKey(file.getName())) {
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
                for (int i = 0; i < listFile.length; i++) {
                    if (listFile[i].isDirectory()) {
                        if (!listFile[i].getParentFile().getName().equals("Android"))
                            searchFolderRecursive(listFile[i]);
                    } else {
                        if (listFile[i].getName().endsWith(pdfPattern) ||
                                listFile[i].getName().endsWith(txtPattern) ||
                                listFile[i].getName().endsWith(docPattern) ||
                                listFile[i].getName().endsWith(zipPattern)) {
                            File file = listFile[i];
                            if (listFile[i].getName().endsWith(".zip") &&
                                    !zipList.contains(listFile[i].toString())) {
                                zipList.add(listFile[i].toString());
                                adapter.notifyDataSetChanged();
                                publishProgress();
                            } else if (listFile[i].getName().endsWith(".pdf") ||
                                    listFile[i].getName().endsWith(".doc") && !documentList.contains(listFile[i].toString())) {
                                documentList.add(listFile[i].toString());
                                adapter.notifyDataSetChanged();
                                publishProgress();
                            } else if (listFile[i].getName().endsWith(".txt") && eBook.contains(listFile[i].toString())) {
                                eBook.add(listFile[i].toString());
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
