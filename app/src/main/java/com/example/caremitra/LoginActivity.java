package com.example.caremitra;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
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

public class LoginActivity extends Activity {

    private EditText inputEmail, inputPassword;
    private Button buttonLogin;
    private TextView forgotPassword, signUpLink;

    private static final String SUPABASE_URL = "https://uvxkiqrqnxgmsipkjhbe.supabase.co";
    private static final String key =BuildConfig.SUPABASE_API_KEY;

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
            Toast.makeText(this, "Error creating login request", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody body = RequestBody.create(
                jsonObject.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/auth/v1/token?grant_type=password")
                .header("apikey", key)
                .header("Authorization", "Bearer " + key)
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
                        JSONObject user = json.optJSONObject("user");
                        String userId = user != null ? user.optString("id", "") : "";
                        String accessToken = json.optString("access_token", "");

                        SharedPreferences prefs = getSharedPreferences("user", MODE_PRIVATE);
                        prefs.edit().putString("supabase_user_id", userId).apply();
                        prefs.edit().putString("supabase_access_token", accessToken).apply();

                        runOnUiThread(() -> {
                            Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        });
                    } catch (Exception e) {
                        runOnUiThread(() ->
                                Toast.makeText(LoginActivity.this, "Login failed: Unable to parse user info", Toast.LENGTH_LONG).show()
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
}
