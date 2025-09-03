package com.example.caremitra;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SignUpActivity extends Activity {

    private EditText inputName, inputEmail, inputPassword, inputConfirmPassword;
    private Button buttonSignUp;
    private TextView loginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        inputName = findViewById(R.id.inputName);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        loginLink = findViewById(R.id.loginLink);

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSignUp();
            }
        });

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to Login screen
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void attemptSignUp() {
        String name = inputName.getText().toString().trim();
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString();
        String confirmPassword = inputConfirmPassword.getText().toString();

        if (TextUtils.isEmpty(name)) {
            inputName.setError("Please enter your full name");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            inputEmail.setError("Please enter your email");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            inputPassword.setError("Please enter your password");
            return;
        }

        if (!password.equals(confirmPassword)) {
            inputConfirmPassword.setError("Passwords do not match");
            return;
        }

        // TODO: Add sign-up logic here (server API call etc.)

        Toast.makeText(this, "Sign Up successful (placeholder)", Toast.LENGTH_SHORT).show();

        // On success, navigate to Login or Home
        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
