package com.byteshaft.filesharing;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button buttonSend;
    Button buttonRecevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonSend = (Button) findViewById(R.id.button_send);
        buttonRecevice = (Button) findViewById(R.id.button_receive);
        buttonSend.setOnClickListener(this);
        buttonRecevice.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_send:
                startActivity(new Intent(this, ActivitySendFile.class));
                break;
            case R.id.button_receive:
                break;
        }
    }
}
