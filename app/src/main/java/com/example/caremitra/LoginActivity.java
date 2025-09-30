package com.example.caremitra;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends Activity {

    private EditText inputEmail, inputPassword;
    private Button buttonLogin;
    private TextView forgotPassword, signUpLink;

    private FrameLayout loginButtonContainer;
    private ProgressBar loginProgress;
    private boolean isLoggingIn = false;

    private static final String SUPABASE_URL = "https://uvxkiqrqnxgmsipkjhbe.supabase.co";
    private static final String SUPABASE_ANON_KEY = BuildConfig.SUPABASE_API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        forgotPassword = findViewById(R.id.forgotPassword);
        signUpLink = findViewById(R.id.signUpLink);

        loginButtonContainer = findViewById(R.id.loginButtonContainer);
        loginProgress = findViewById(R.id.loginProgress);

        buttonLogin.setOnClickListener(v -> attemptLogin());

        forgotPassword.setOnClickListener(v ->
                Toast.makeText(LoginActivity.this, "Forgot Password feature coming soon", Toast.LENGTH_SHORT).show()
        );

        signUpLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }

    private void setLoginLoading(boolean loading) {
        isLoggingIn = loading;
        buttonLogin.setEnabled(!loading);
        buttonLogin.setText(loading ? "" : "Login");
        loginProgress.setVisibility(loading ? android.view.View.VISIBLE : android.view.View.GONE);
    }

    private void attemptLogin() {
        if (isLoggingIn) return;

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

        setLoginLoading(true);
        loginWithSupabase(email, password);
    }

    private void loginWithSupabase(String email, String password) {
        OkHttpClient client = new OkHttpClient();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", email);
            jsonObject.put("password", password);
        } catch (Exception e) {
            runOnUiThread(() -> {
                setLoginLoading(false);
                Toast.makeText(this, "Error creating login request", Toast.LENGTH_SHORT).show();
            });
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
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    setLoginLoading(false);
                    Toast.makeText(LoginActivity.this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    try {
                        JSONObject json = new JSONObject(respBody);
                        String accessToken = json.optString("access_token", "");
                        String userId = json.optJSONObject("user").optString("id", "");

                        SharedPreferences.Editor editor = getSharedPreferences("user", MODE_PRIVATE).edit();
                        editor.putString("supabase_access_token", accessToken);
                        editor.putString("user_id", userId);
                        editor.apply();

                        // keep spinner on during profile check/create
                        checkAndCreateProfile(userId, accessToken);

                    } catch (JSONException e) {
                        runOnUiThread(() -> {
                            setLoginLoading(false);
                            Toast.makeText(LoginActivity.this, "Failed to parse login response", Toast.LENGTH_LONG).show();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        setLoginLoading(false);
                        Toast.makeText(LoginActivity.this, "Invalid credentials!", Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    private void checkAndCreateProfile(String userId, String accessToken) {
        OkHttpClient client = new OkHttpClient();

        Request checkRequest = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/users?id=eq." + userId)
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Authorization", "Bearer " + accessToken)
                .get()
                .build();

        client.newCall(checkRequest).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                // Proceed anyway
                finishLoginAndGoHome("Profile check failed. Proceeding.");
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body() != null ? response.body().string() : "[]";
                    try {
                        JSONArray usersArray = new JSONArray(responseBody);
                        if (usersArray.length() > 0) {
                            finishLoginAndGoHome(null);
                        } else {
                            createProfile(userId, accessToken);
                        }
                    } catch (JSONException e) {
                        createProfile(userId, accessToken);
                    }
                } else {
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
            jsonObject.put("name", "New User");
        } catch (Exception e) {
            finishLoginAndGoHome("Profile creation skipped.");
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
            @Override public void onFailure(Call call, IOException e) {
                finishLoginAndGoHome("Profile creation failed. Proceeding.");
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                finishLoginAndGoHome(null);
            }
        });
    }

    private void finishLoginAndGoHome(String toastMsg) {
        runOnUiThread(() -> {
            if (toastMsg != null && !toastMsg.isEmpty()) {
                Toast.makeText(LoginActivity.this, toastMsg, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
            }
            setLoginLoading(false);
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
