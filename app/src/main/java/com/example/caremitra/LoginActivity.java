package com.example.caremitra;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

import okhttp3.*;

public class LoginActivity extends Activity {

    private EditText inputEmail, inputPassword;
    private Button buttonLogin;
    private TextView forgotPassword, signUpLink;

    private static final String SUPABASE_URL = "https://uvxkiqrqnxgmsipkjhbe.supabase.co";
    // NOTE: Replace this with your BuildConfig.SUPABASE_API_KEY if you use it.
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InV2eGtpcXJxbnhnbXNpcGtqaGJlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTYyMjc0MTYsImV4cCI6MjA3MTgwMzQxNn0.GEYSncagmsr8BkBPe8IGRSGke0llj4skHWBENnyyTJI";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        forgotPassword = findViewById(R.id.forgotPassword);
        signUpLink = findViewById(R.id.signUpLink);

        buttonLogin.setOnClickListener(v -> attemptLogin());

        forgotPassword.setOnClickListener(v ->
                Toast.makeText(LoginActivity.this, "Forgot Password feature coming soon", Toast.LENGTH_SHORT).show()
        );

        signUpLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
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

        loginWithSupabase(email, password);
    }

    private void loginWithSupabase(String email, String password) {
        OkHttpClient client = new OkHttpClient();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", email);
            jsonObject.put("password", password);
        } catch (Exception e) {
            runOnUiThread(() -> Toast.makeText(this, "Error creating login request", Toast.LENGTH_SHORT).show());
            return;
        }

        RequestBody body = RequestBody.create(
                jsonObject.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/auth/v1/token?grant_type=password")
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(LoginActivity.this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                if (response.isSuccessful()) {
                    try {
                        JSONObject json = new JSONObject(respBody);
                        String accessToken = json.optString("access_token", "");
                        String userId = json.optJSONObject("user").optString("id", "");

                        // Save tokens and user ID
                        SharedPreferences.Editor editor = getSharedPreferences("user", MODE_PRIVATE).edit();
                        editor.putString("supabase_access_token", accessToken);
                        editor.putString("user_id", userId);
                        editor.apply();

                        // Now, check if a profile exists for this user ID
                        checkAndCreateProfile(userId, accessToken);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() ->
                                Toast.makeText(LoginActivity.this, "Failed to parse login response", Toast.LENGTH_LONG).show()
                        );
                    }
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(LoginActivity.this, "Invalid credentials!", Toast.LENGTH_LONG).show()
                    );
                }
            }
        });
    }

    private void checkAndCreateProfile(String userId, String accessToken) {
        OkHttpClient client = new OkHttpClient();

        // 1. Check if profile exists
        Request checkRequest = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/users?id=eq." + userId)
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Authorization", "Bearer " + accessToken)
                .get()
                .build();

        client.newCall(checkRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Network error, proceed to HomeActivity
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, "Profile check failed. Proceeding.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONArray usersArray = new JSONArray(responseBody);
                        if (usersArray.length() > 0) {
                            // Profile exists, go to HomeActivity
                            Log.d("LoginActivity", "Profile exists. Proceeding.");
                            runOnUiThread(() -> {
                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finish();
                            });
                        } else {
                            // Profile does not exist, create it
                            Log.d("LoginActivity", "Profile does not exist. Creating new profile.");
                            createProfile(userId, accessToken);
                        }
                    } catch (JSONException e) {
                        // Error parsing JSON, assume profile does not exist and create it
                        e.printStackTrace();
                        Log.e("LoginActivity", "JSON parsing failed, assuming new profile. " + e.getMessage());
                        createProfile(userId, accessToken);
                    }
                } else {
                    // An error occurred, assume profile needs to be created
                    Log.e("LoginActivity", "Failed to check for profile: " + response.code() + ". Assuming new profile.");
                    createProfile(userId, accessToken);
                }
            }
        });
    }

    private void createProfile(String userId, String accessToken) {
        OkHttpClient client = new OkHttpClient();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", userId);
            jsonObject.put("name", "New User"); // Default name
            // Add other default fields if needed
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/users")
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .header("Prefer", "return=minimal")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, "Profile creation failed. Proceeding anyway.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("LoginActivity", "Profile created successfully.");
                    runOnUiThread(() -> {
                        Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    });
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    Log.e("LoginActivity", "Failed to create profile: " + response.code() + " - " + errorBody);
                    runOnUiThread(() -> {
                        Toast.makeText(LoginActivity.this, "Login successful, but profile creation failed.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    });
                }
            }
        });
    }
}