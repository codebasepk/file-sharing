package com.byteshaft.filesharing;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;



/**
 * Created by s9iper1 on 1/30/17.
 */

public class SendProgressActivity extends PlaceholderPeersActivity{

    private ListView listView;
    private ArrayList<String> file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_layout);
        listView = (ListView) findViewById(R.id.list_view);
        file = new ArrayList<>();
        Iterator entries = ActivitySendFile.sendList.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry thisEntry = (Map.Entry) entries.next();
            Object key = thisEntry.getKey();
            file.add(key.toString());
        }
        Adapter adapter = new Adapter(getApplicationContext(), R.layout.delegate_files, file);
        listView.setAdapter(adapter);
    }

    class Adapter extends ArrayAdapter<String> {

        private ViewHolder viewHolder;
        private ArrayList<String> toBeSend;

        public Adapter(Context context, int resource, ArrayList<String> toBeSend) {
            super(context, resource);
            this.toBeSend = toBeSend;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.delegate_files, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.fileName = (TextView) convertView.findViewById(R.id.file_name);
                viewHolder.fileUri = (TextView) convertView.findViewById(R.id.file_uri);
                viewHolder.pulsatorLayout = (RoundCornerProgressBar) convertView.findViewById(R.id.progressbar_horizontal);
                convertView.setTag(viewHolder);
            }else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            File file = new File(toBeSend.get(position));
            Log.i("TAG", "file " + toBeSend.get(position));
            viewHolder.fileName.setText(file.getName());
            viewHolder.fileUri.setText(file.toString());
            return convertView;
        }

        @Override
        public int getCount() {
            return toBeSend.size();
        }
    }

    class ViewHolder {
        TextView fileName;
        TextView fileUri;
        RoundCornerProgressBar pulsatorLayout;

    }
}
