package com.byteshaft.filesharing;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by s9iper1 on 1/30/17.
 */

public class SendProgressActivity extends PlaceholderPeersActivity{

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_layout);
        listView = (ListView) findViewById(R.id.list_view);
    }

    class Adapter extends ArrayAdapter<String> {

        public Adapter(Context context, int resource) {
            super(context, resource);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.delegate_files, parent, false);
                
            }else {

            }
            return convertView;
        }
    }
}
