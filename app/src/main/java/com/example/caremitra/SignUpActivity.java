package com.example.caremitra;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignUpActivity extends Activity {

    private EditText inputName, inputEmail, inputPassword, inputConfirmPassword;
    private Button buttonSignUp;
    private TextView loginLink;

    private FrameLayout signUpButtonContainer;
    private ProgressBar signUpProgress;
    private boolean isSigningUp = false;

    private static final String SUPABASE_URL = "https://uvxkiqrqnxgmsipkjhbe.supabase.co";
    private static final String SUPABASE_ANON_KEY = BuildConfig.SUPABASE_API_KEY;

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

        signUpButtonContainer = findViewById(R.id.signUpButtonContainer);
        signUpProgress = findViewById(R.id.signUpProgress);

        buttonSignUp.setOnClickListener(v -> attemptSignUp());

        loginLink.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void setSignUpLoading(boolean loading) {
        isSigningUp = loading;
        buttonSignUp.setEnabled(!loading);
        buttonSignUp.setText(loading ? "" : "Sign Up");
        signUpProgress.setVisibility(loading ? android.view.View.VISIBLE : android.view.View.GONE);
    }

    private void attemptSignUp() {
        if (isSigningUp) return;

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

        setSignUpLoading(true);
        signUpWithSupabase(email, password);
    }

    private void signUpWithSupabase(String email, String password) {
        OkHttpClient client = new OkHttpClient();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", email);
            jsonObject.put("password", password);
        } catch (Exception e) {
            runOnUiThread(() -> {
                setSignUpLoading(false);
                Toast.makeText(this, "Error creating sign up request", Toast.LENGTH_SHORT).show();
            });
            return;
        }

        RequestBody body = RequestBody.create(
                jsonObject.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/auth/v1/signup")
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    setSignUpLoading(false);
                    Toast.makeText(SignUpActivity.this, "Sign Up failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                String errorMsg = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        setSignUpLoading(false);
                        Toast.makeText(SignUpActivity.this, "Sign Up successful! Check email to confirm your account.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    });
                } else {
                    runOnUiThread(() -> {
                        setSignUpLoading(false);
                        Toast.makeText(SignUpActivity.this, "Sign Up failed: " + errorMsg, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }
}
