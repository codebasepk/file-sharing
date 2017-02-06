package com.byteshaft.filesharing.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.byteshaft.filesharing.R;
import com.byteshaft.filesharing.utils.AppGlobals;
import com.byteshaft.filesharing.utils.Helpers;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final int RESULT_LOAD_IMAGE = 10;
    private static final int READ_STORAGE_PERMISSION = 0;
    private static final int WRITE_STORAGE_PERMISSION = 1;
    EditText mUsername;
    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button buttonSend = (Button) findViewById(R.id.button_send);
        Button buttonReceive = (Button) findViewById(R.id.button_receive);
        buttonSend.setOnClickListener(this);
        buttonReceive.setOnClickListener(this);
        if (AppGlobals.isRunningFirstTime()) {
            showDialog();
            AppGlobals.saveBoolean(false);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_send:
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.READ_EXTERNAL_STORAGE}, READ_STORAGE_PERMISSION);
                } else {
                    startActivity(new Intent(this, ActivitySendFile.class));
                }
                break;
            case R.id.button_receive:
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_STORAGE_PERMISSION);
                } else {
                    startActivity(new Intent(this, ActivityReceiveFile.class));
                }
                break;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case READ_STORAGE_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivity(new Intent(this, ActivitySendFile.class));
                } else {
                    Toast.makeText(this, "permission denied!", Toast.LENGTH_SHORT).show();
                }
                break;
            case WRITE_STORAGE_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivity(new Intent(this, ActivityReceiveFile.class));
                } else {
                    Toast.makeText(this, "permission denied!", Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            Intent placeholderActivityIntent = new Intent(
                    getApplicationContext(), PlaceholderPeersActivity.class);
            placeholderActivityIntent.putExtra(
                    "image_url", Helpers.getImgPath(getApplicationContext(), selectedImage));
            startActivity(placeholderActivityIntent);
        }
    }

    private void showDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(10);
        alertDialog.setTitle("User Name");
        alertDialog.setCancelable(false);
        mUsername = new EditText(MainActivity.this);
        mUsername.setHint("Enter your name..");
        mUsername.setFilters(FilterArray);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        mUsername.setLayoutParams(lp);
        alertDialog.setView(mUsername);
        alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (validate()) {
                            PreferenceManager.getDefaultSharedPreferences(
                                    getApplicationContext()).edit().putString("username", name).apply();
                        }
                    }
                });
        alertDialog.show();
    }


    public boolean validate() {
        boolean valid = true;
        name = mUsername.getText().toString();
        if (name.isEmpty() || name.length() < 4) {
            mUsername.setError("Enter minimum 4 characters");
            valid = false;
        } else {
            mUsername.setError(null);
        }
        return valid;
    }
}
