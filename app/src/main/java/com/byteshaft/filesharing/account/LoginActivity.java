package com.byteshaft.filesharing.account;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.byteshaft.filesharing.R;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    EditText emailAddress;
    EditText password;
    Button loginButton;
    TextView forgotPasswordButton;
    TextView signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        emailAddress = (EditText) findViewById(R.id.email_address);
        password = (EditText) findViewById(R.id.password);
        loginButton = (Button) findViewById(R.id.login_button);
        forgotPasswordButton = (TextView) findViewById(R.id.tv_forgot_password);
        signUpButton = (TextView) findViewById(R.id.sign_up);
        loginButton.setOnClickListener(this);
        forgotPasswordButton.setOnClickListener(this);
        signUpButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login_button:
                break;
            case R.id.tv_forgot_password:
                break;
            case R.id.sign_up:
                startActivity(new Intent(this, RegisterActivity.class));
        }
    }
}
