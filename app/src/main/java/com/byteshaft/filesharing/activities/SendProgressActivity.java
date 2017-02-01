package com.byteshaft.filesharing.activities;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.byteshaft.filesharing.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class SendProgressActivity extends PlaceholderPeersActivity {

    private ListView listView;
    private ArrayList<String> file;
    private static SendProgressActivity sInstance;
    public Adapter adapter;

    public static SendProgressActivity getInstance() {
        return sInstance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_layout);
        sInstance = this;
        listView = (ListView) findViewById(R.id.list_view);
        file = new ArrayList<>();
        Iterator entries = ActivitySendFile.sendList.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry thisEntry = (Map.Entry) entries.next();
            Object key = thisEntry.getKey();
            file.add(key.toString());
            progressHashMap.put(key.toString(), 0);
        }
        adapter = new Adapter(getApplicationContext(), R.layout.delegate_files, file);
        listView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.retry_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_retry) {
            method();
        }
        return super.onOptionsItemSelected(item);
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
                viewHolder.roundCornerProgressBar = (RoundCornerProgressBar) convertView.findViewById(R.id.progressbar_horizontal);
                convertView.setTag(viewHolder);
            }else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            File file = new File(toBeSend.get(position));
            viewHolder.fileName.setText(file.getName());
            viewHolder.fileUri.setText(file.toString());
            viewHolder.roundCornerProgressBar.setMax(100);
            viewHolder.roundCornerProgressBar.setProgress(progressHashMap.get(file.toString()));
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
        RoundCornerProgressBar roundCornerProgressBar;

    }
}
