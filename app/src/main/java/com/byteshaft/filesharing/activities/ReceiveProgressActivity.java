package com.byteshaft.filesharing.activities;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.byteshaft.filesharing.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class ReceiveProgressActivity extends AppCompatActivity {

    private ListView receiveFileList;
    public FileAdapter fileAdapter;
    public ArrayList<String> file;
    public HashMap<String, Integer> receiveProgressHashMap;
    private static ReceiveProgressActivity sInstance;
    public TextView currentFileCounter;
    public TextView totalFileCounter;

    public static ReceiveProgressActivity getInstance() {
        return sInstance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_progress);
        sInstance = this;
        currentFileCounter = (TextView) findViewById(R.id.remaining_counter);
        totalFileCounter = (TextView) findViewById(R.id.total_counter);
        file = new ArrayList<>();
        receiveProgressHashMap = new HashMap<>();
        receiveFileList = (ListView) findViewById(R.id.receive_files_list);
        fileAdapter = new FileAdapter(getApplicationContext(), R.layout.delegate_receive_files, file);
        receiveFileList.setAdapter(fileAdapter);
    }

    class FileAdapter extends ArrayAdapter<String> {

        private ViewHolder viewHolder;
        private ArrayList<String> toBeReceived;


        FileAdapter(Context context, int resource, ArrayList<String> toBeReceived) {
            super(context, resource);
            this.toBeReceived = toBeReceived;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.delegate_receive_files, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.openFileButton = (Button) convertView.findViewById(R.id.button_open);
                viewHolder.fileName = (TextView) convertView.findViewById(R.id.received_file_name);
                viewHolder.roundCornerProgressBar = (ProgressBar) convertView.findViewById(R.id.pb_receive);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            File file = new File(toBeReceived.get(position));
            viewHolder.roundCornerProgressBar.setProgress(receiveProgressHashMap.get(file.getAbsolutePath()));
            viewHolder.fileName.setText(file.getName());
            viewHolder.openFileButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    File file = new File(toBeReceived.get(position));
                    MimeTypeMap mime = MimeTypeMap.getSingleton();
                    String ext = file.getName().substring(file.getName().lastIndexOf(".") + 1);
                    String type = mime.getMimeTypeFromExtension(ext);
                    try {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            Uri contentUri = FileProvider.getUriForFile(
                                    getContext(), "com.byteshaft.filesharing.provider", file);
                            intent.setDataAndType(contentUri, type);
                        } else {
                            intent.setDataAndType(Uri.fromFile(file), type);
                        }
                        startActivity(intent);
                    } catch (ActivityNotFoundException anfe) {
                        Toast.makeText(getContext(), "No activity found to open this attachment.", Toast.LENGTH_LONG).show();
                    }
                }
            });
            return convertView;
        }

        @Override
        public int getCount() {
            return toBeReceived.size();
        }
    }

    private class ViewHolder {
        Button openFileButton;
        TextView fileName;
        ProgressBar roundCornerProgressBar;
    }
}
