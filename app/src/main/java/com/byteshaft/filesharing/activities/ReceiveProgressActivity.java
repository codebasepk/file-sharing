package com.byteshaft.filesharing.activities;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.byteshaft.filesharing.BuildConfig;
import com.byteshaft.filesharing.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class ReceiveProgressActivity extends AppCompatActivity {

    private ListView receiveFileList;
    public FileAdapter fileAdapter;
    public   ArrayList<String> file;
    public HashMap<String, Integer> receiveProgressHashMap;
    private static ReceiveProgressActivity sInstance;

    public static ReceiveProgressActivity getInstance() {
        return sInstance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_progress);
        sInstance = this;
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.delegate_receive_files, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.openFileButton = (Button) convertView.findViewById(R.id.button_open);
                viewHolder.fileName = (TextView) convertView.findViewById(R.id.received_file_name);
                viewHolder.roundCornerProgressBar = (RoundCornerProgressBar) convertView.findViewById(R.id.pb_receive);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            File file = new File(toBeReceived.get(position));
            viewHolder.fileName.setText(file.getName());
            viewHolder.roundCornerProgressBar.setProgress(receiveProgressHashMap.get(file.getAbsolutePath()));
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
                            Uri contentUri = FileProvider.getUriForFile(getContext(), "com.byteshaft.filesharing.fileProvider", file);
                            intent.setDataAndType(contentUri, type);
                        } else {
                            intent.setDataAndType(Uri.fromFile(file), type);
                        }
                        startActivity(intent);
                    } catch (ActivityNotFoundException anfe) {
                        Toast.makeText(getContext(), "No activity found to open this attachment.", Toast.LENGTH_LONG).show();
                    }
//                    File file = new File(toBeReceived.get(position));
//                    Uri uri_path = Uri.fromFile(file);
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                        uri_path = FileProvider.getUriForFile(getApplicationContext(),
//                                BuildConfig.APPLICATION_ID + ".provider", file);
//                    }
//                    String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension
//                            (MimeTypeMap.getFileExtensionFromUrl(toBeReceived.get(position)));
//                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                        Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName(), file);
//                        intent.setDataAndType(contentUri, mimeType);
//                    } else {
//                        intent.setDataAndType(uri_path, mimeType);
//                    }
//                    startActivity(intent);
                }
            });
            return convertView;
        }

        private String getMimeType(Context context, Uri uri) {
            String extension;

            //Check uri format to avoid null
            if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
                //If scheme is a content
                final MimeTypeMap mime = MimeTypeMap.getSingleton();
                extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
            } else {
                //If scheme is a File
                //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
                extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());

            }

            return extension;
        }

        @Override
        public int getCount() {
            return toBeReceived.size();
        }
    }

    private class ViewHolder {
        Button openFileButton;
        TextView fileName;
        RoundCornerProgressBar roundCornerProgressBar;

    }
}
