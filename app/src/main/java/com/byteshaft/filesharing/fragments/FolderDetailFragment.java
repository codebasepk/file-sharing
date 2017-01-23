package com.byteshaft.filesharing.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.byteshaft.filesharing.R;

import java.io.File;
import java.util.ArrayList;

import static android.R.attr.path;

/**
 * Created by s9iper1 on 1/23/17.
 */

public class FolderDetailFragment extends Fragment {

    private View mBaseView;
    private ArrayList<String> selectedFolderList;
    private Adapter adapter;
    private ListView listView;
    public ArrayList<String> zipList;
    public ArrayList<String> documentList;
    public ArrayList<String> eBook;
    private File path;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBaseView = inflater.inflate(R.layout.folder_detail_fragment, container, false);
        path = new File(Environment.getExternalStorageDirectory() + "");
        listView = (ListView) mBaseView.findViewById(R.id.list_view);
        String selectedFolder = getArguments().getString("folder");
        eBook = new ArrayList<>();
        documentList = new ArrayList<>();
        zipList = new ArrayList<>();
        Log.i("TAG", "folder " + selectedFolder);
        if (selectedFolder.equals("Zip")) {
            adapter = new Adapter(getActivity().getApplicationContext(),
                    R.layout.delegate_folder, zipList);
        } else if (selectedFolder.equals("Document")) {
            adapter = new Adapter(getActivity().getApplicationContext(),
                    R.layout.delegate_folder, documentList);
        } else if (selectedFolder.equals("E-Book")) {
            adapter = new Adapter(getActivity().getApplicationContext(),
                    R.layout.delegate_folder, eBook);
        }
        listView.setAdapter(adapter);
        new GetFiles().execute();
        return mBaseView;
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
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.delegate_folder_detail, parent, false);
                viewHolder.fileImage = (ImageView) convertView.findViewById(R.id.file_image);
                viewHolder.fileName = (TextView) convertView.findViewById(R.id.file_name);
                viewHolder.fileSize = (TextView) convertView.findViewById(R.id.file_size);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            File file = new File(folderList.get(position));
            viewHolder.fileName.setText(file.getName());
            viewHolder.fileSize.setText(Formatter.formatFileSize(getActivity(),file.length()));

            return convertView;

        }

        @Override
        public int getCount() {
            return folderList.size();
        }
    }

    private class ViewHolder {
        TextView fileName;
        TextView fileSize;
        ImageView fileImage;

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
                                Log.i("Zip", "" + listFile[i]);
                                zipList.add(listFile[i].toString());
                                publishProgress();
                            } else if (listFile[i].getName().endsWith(".pdf") ||
                                    listFile[i].getName().endsWith(".doc") && !documentList.contains(listFile[i].toString())) {
                                Log.i("Document", "" + listFile[i]);
                                documentList.add(listFile[i].toString());
                                publishProgress();
                            } else if (listFile[i].getName().endsWith(".txt") && eBook.contains(listFile[i].toString())) {
                                Log.i("E-book", "" + listFile[i]);
                                eBook.add(listFile[i].toString());
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

}
