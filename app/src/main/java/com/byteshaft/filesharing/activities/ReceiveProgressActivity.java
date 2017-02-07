package com.byteshaft.filesharing.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.byteshaft.filesharing.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class ReceiveProgressActivity extends AppCompatActivity {

    private ListView receiveFileList;
    public FileAdapter fileAdapter;
    private ArrayList<String> file;
    public HashMap<String, Integer> receiveProgressHashMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_progress);
        file = new ArrayList<>();
        receiveProgressHashMap = new HashMap<>();
        receiveFileList = (ListView) findViewById(R.id.receive_files_list);
        fileAdapter = new FileAdapter(getApplicationContext(), R.layout.delegate_receive_files, file);
        receiveFileList.setAdapter(fileAdapter);

    }

    class FileAdapter extends ArrayAdapter<String> {

        private ViewHolder viewHolder;
        private ArrayList<String> toBeReceived;


        public FileAdapter(Context context, int resource, ArrayList<String> toBeReceived) {
            super(context, resource);
            this.toBeReceived = toBeReceived;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.delegate_receive_files, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.openFileButton = (Button) convertView.findViewById(R.id.button_open);
                viewHolder.fileName = (TextView) convertView.findViewById(R.id.received_file_name);
                viewHolder.fileUri = (TextView) convertView.findViewById(R.id.received_file_uri);
                viewHolder.roundCornerProgressBar = (RoundCornerProgressBar) convertView.findViewById(R.id.pb_receive);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            File file = new File(toBeReceived.get(position));
            viewHolder.fileName.setText(file.getName());
            viewHolder.fileUri.setText(file.toString());
            viewHolder.roundCornerProgressBar.setProgress(receiveProgressHashMap.get(file.getAbsolutePath()));
            return convertView;
        }

        @Override
        public int getCount() {
            return toBeReceived.size();
        }
    }

    class ViewHolder {
        Button openFileButton;
        TextView fileName;
        TextView fileUri;
        RoundCornerProgressBar roundCornerProgressBar;

    }
}
