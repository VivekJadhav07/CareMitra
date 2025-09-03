package com.example.caremitra;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {

    private EditText inputEmail, inputPassword;
    private Button buttonLogin;
    private TextView forgotPassword, signUpLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        forgotPassword = findViewById(R.id.forgotPassword);
        signUpLink = findViewById(R.id.signUpLink);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "Forgot Password feature coming soon", Toast.LENGTH_SHORT).show();
            }
        });

        signUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to SignUp activity (implement separately)
              Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
               startActivity(intent);
            }
        });
    }

    private void attemptLogin() {
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            inputEmail.setError("Please enter email");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            inputPassword.setError("Please enter password");
            return;
        }

        // TODO: Add your login validation/authentication here

        Toast.makeText(this, "Login successful (placeholder)", Toast.LENGTH_SHORT).show();

        // On successful login, navigate to Home/Dashboard
       Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(intent);
       finish();
    }
}
