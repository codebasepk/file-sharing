package com.byteshaft.filesharing.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
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

public class FilesFragment extends Fragment implements AdapterView.OnItemClickListener {

    private static final String EXT_PDF = ".pdf";
    private static final String EXT_TXT = ".txt";
    private static final String EXT_DOC = ".doc";
    private static final String EXT_ZIP = ".zip";
    private static final String TITLE_DOCUMENT = "Document";
    private static final String TITLE_ZIP = "Zip";
    private static final String TITLE_EBOOK = "E-Book";
    private static final String PLURAL_MAKER = "s";
    private ArrayList<String> mFoldersList;
    private Adapter mAdapter;
    private ListView mListView;
    private File mPath;
    private int mSelectedFolder = -1;
    public ArrayList<String> mZipList;
    public ArrayList<String> mDocumentsList;
    public ArrayList<String> mEBookList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.files_fragment, container, false);
        mFoldersList = new ArrayList<>();
        mFoldersList.add(TITLE_DOCUMENT);
        mFoldersList.add(TITLE_ZIP);
        mFoldersList.add(TITLE_EBOOK);
        mPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        mListView = (ListView) rootView.findViewById(R.id.list_view);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        mListView.setOnItemClickListener(this);
        mEBookList = new ArrayList<>();
        mDocumentsList = new ArrayList<>();
        mZipList = new ArrayList<>();
        mAdapter = new Adapter(getActivity().getApplicationContext(),
                R.layout.delegate_folder, mFoldersList);
        GridView gridLayout = (GridView) rootView.findViewById(R.id.photo_grid);
        gridLayout.setAdapter(mAdapter);
        gridLayout.setOnItemClickListener(this);
        new GetFiles().execute();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    private void setUpAdapter(String selected) {
        switch (selected) {
            case TITLE_ZIP:
                FilesAdapter filesAdapter = new FilesAdapter(getActivity().getApplicationContext(),
                        R.layout.delegate_folder, mZipList);
                mListView.setAdapter(filesAdapter);
                break;
            case TITLE_DOCUMENT:
                filesAdapter = new FilesAdapter(getActivity().getApplicationContext(),
                        R.layout.delegate_folder, mDocumentsList);
                mListView.setAdapter(filesAdapter);
                break;
            case TITLE_EBOOK:
                filesAdapter = new FilesAdapter(getActivity().getApplicationContext(),
                        R.layout.delegate_folder, mEBookList);
                mListView.setAdapter(filesAdapter);
                break;
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> mAdapterView, View view, int i, long l) {
        switch (view.getId()) {
            case R.id.list_view:
                CheckBox selectionCheckbox = (CheckBox) view.findViewById(R.id.selectionCheckbox);
                String relevantList = getFilesListByIndex(mSelectedFolder).get(i);
                File file = new File(relevantList);
                HashMap<String, String> fileItem = Helpers.getFileMetadataMap(
                        file.getAbsolutePath(), getDirectoryNameByIndex(i));
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
                mAdapter.notifyDataSetChanged();
                ActivitySendFile.getInstance().setSelection();
                break;
            case R.id.photo_grid:
                mSelectedFolder = i;
                setUpAdapter(mFoldersList.get(i));
                break;
        }
    }

    private ArrayList<String> getFilesListByIndex(int index) {
        switch (index) {
            case 0:
                return mDocumentsList;
            case 1:
                return mZipList;
            case 2:
                return mEBookList;
            default:
                return new ArrayList<>();
        }
    }

    private String getDirectoryNameByIndex(int index) {
        switch (index) {
            case 0:
                return TITLE_DOCUMENT + PLURAL_MAKER;
            case 1:
                return TITLE_ZIP + PLURAL_MAKER;
            case 2:
                return TITLE_EBOOK + PLURAL_MAKER;
            default:
                return "UNKNOWN";
        }
    }

    private class Adapter extends ArrayAdapter<ArrayList<String>> {

        private ArrayList<String> mFoldersList;
        private ViewHolder viewHolder;

        Adapter(Context context, int resource, ArrayList<String> mFoldersList) {
            super(context, resource);
            this.mFoldersList = mFoldersList;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
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
            if (mFoldersList.get(position).endsWith(TITLE_ZIP)) {
                viewHolder.folderImage.setImageResource(R.mipmap.zip);
            } else if (mFoldersList.get(position).endsWith(TITLE_DOCUMENT)) {
                viewHolder.folderImage.setImageResource(R.mipmap.document);
            } else if (mFoldersList.get(position).endsWith(TITLE_EBOOK)) {
                viewHolder.folderImage.setImageResource(R.mipmap.ebook);
            }
            if (mSelectedFolder == position) {
                viewHolder.relativeLayout.setBackgroundResource(R.drawable.grid_background);
            } else {
                viewHolder.relativeLayout.setBackgroundResource(0);
            }
            viewHolder.folderName.setText(mFoldersList.get(position));

            return convertView;

        }

        @Override
        public int getCount() {
            return mFoldersList.size();
        }
    }

    private class ViewHolder {
        TextView folderName;
        ImageView folderImage;
        RelativeLayout relativeLayout;

    }

    private class FilesAdapter extends ArrayAdapter<ArrayList<String>> {

        private ArrayList<String> mFoldersList;

        FilesAdapter(Context context, int resource, ArrayList<String> mFoldersList) {
            super(context, resource);
            this.mFoldersList = mFoldersList;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
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
            File file = new File(mFoldersList.get(position));
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
            return mFoldersList.size();
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
                    searchFolderRecursive(mPath);
                }
            });
            return null;
        }

        private void searchFolderRecursive(File dir) {
            File listFile[] = dir.listFiles();
            for (File file : listFile) {
                if (file.isDirectory()) {
                    if (!file.getParentFile().getName().equals("Android"))
                        searchFolderRecursive(file);
                } else if (file.isFile()) {
                    if (file.getName().endsWith(EXT_ZIP) && !mZipList.contains(file.toString())) {
                        mZipList.add(file.toString());
                        mAdapter.notifyDataSetChanged();
                        publishProgress();
                    } else if (file.getName().endsWith(EXT_PDF) || file.getName().endsWith(EXT_DOC)
                            && !mDocumentsList.contains(file.toString())) {
                        mDocumentsList.add(file.toString());
                        mAdapter.notifyDataSetChanged();
                        publishProgress();
                    } else if (file.getName().endsWith(EXT_TXT)
                            && mEBookList.contains(file.toString())) {
                        mEBookList.add(file.toString());
                        mAdapter.notifyDataSetChanged();
                        publishProgress();
                    }
                }
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }
}
