package com.example.caremitra;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignUpActivity extends Activity {

    // UI Components
    private EditText editTextName, editTextEmail, editTextPhone, editTextDOB, editTextPassword, editTextConfirmPassword;
    private Spinner spinnerGender, spinnerBloodGroup;
    private Button buttonSignUp;

    // Supabase Credentials
    private static final String SUPABASE_URL = "https://uvxkiqrqnxgmsipkjhbe.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InV2eGtpcXJxbnhnbXNpcGtqaGJlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTYyMjc0MTYsImV4cCI6MjA3MTgwMzQxNn0.GEYSncagmsr8BkBPe8IGRSGke0llj4skHWBENnyyTJI";
    private static final OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        initializeViews();
        setupSpinners();
        setupDatePicker();

        buttonSignUp.setOnClickListener(v -> validateAndSignUp());
    }

    private void initializeViews() {
        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextDOB = findViewById(R.id.editTextDOB);
        spinnerGender = findViewById(R.id.spinnerGender);
        spinnerBloodGroup = findViewById(R.id.spinnerBloodGroup);
        buttonSignUp = findViewById(R.id.buttonSignUp);
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        ArrayAdapter<CharSequence> bloodGroupAdapter = ArrayAdapter.createFromResource(this,
                R.array.blood_group_array, android.R.layout.simple_spinner_item);
        bloodGroupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBloodGroup.setAdapter(bloodGroupAdapter);
    }

    private void setupDatePicker() {
        final Calendar myCalendar = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener date = (view, year, month, day) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, day);
            // Format for Supabase 'date' type: YYYY-MM-DD
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            editTextDOB.setText(sdf.format(myCalendar.getTime()));
        };

        editTextDOB.setOnClickListener(v -> new DatePickerDialog(SignUpActivity.this, date,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show());
    }

    private void validateAndSignUp() {
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString();
        String confirmPassword = editTextConfirmPassword.getText().toString();
        String phone = editTextPhone.getText().toString().trim();
        String dob = editTextDOB.getText().toString().trim();

        // Stricter Validation
        if (TextUtils.isEmpty(name)) { Toast.makeText(this, "Full Name is required.", Toast.LENGTH_SHORT).show(); editTextName.requestFocus(); return; }
        if (TextUtils.isEmpty(email)) { Toast.makeText(this, "Email is required.", Toast.LENGTH_SHORT).show(); editTextEmail.requestFocus(); return; }
        if (TextUtils.isEmpty(password)) { Toast.makeText(this, "Password is required.", Toast.LENGTH_SHORT).show(); editTextPassword.requestFocus(); return; }
        if (!password.equals(confirmPassword)) { Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show(); editTextConfirmPassword.requestFocus(); return; }
        if (TextUtils.isEmpty(phone)) { Toast.makeText(this, "Phone number is required.", Toast.LENGTH_SHORT).show(); editTextPhone.requestFocus(); return; }
        if (TextUtils.isEmpty(dob)) { Toast.makeText(this, "Date of Birth is required.", Toast.LENGTH_SHORT).show(); return; }
        if (spinnerGender.getSelectedItemPosition() == 0) { Toast.makeText(this, "Please select your gender.", Toast.LENGTH_SHORT).show(); return; }
        if (spinnerBloodGroup.getSelectedItemPosition() == 0) { Toast.makeText(this, "Please select your blood group.", Toast.LENGTH_SHORT).show(); return; }

        String gender = spinnerGender.getSelectedItem().toString();
        String bloodGroup = spinnerBloodGroup.getSelectedItem().toString();

        // If all checks pass, call the single function to sign up and create profile
        signUpUserAndCreateProfile(name, email, password, phone, dob, gender, bloodGroup);
    }

    private void signUpUserAndCreateProfile(String name, String email, String password, String phone, String dob, String gender, String bloodGroup) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email);
            jsonBody.put("password", password);

            JSONObject options = new JSONObject();
            JSONObject data = new JSONObject();
            data.put("name", name);
            data.put("phone", phone);
            data.put("dob", dob);
            data.put("gender", gender);
            data.put("blood_group", bloodGroup);

            options.put("data", data);
            jsonBody.put("options", options);

        } catch (JSONException e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(SignUpActivity.this, "Error building request", Toast.LENGTH_SHORT).show());
            return;
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/auth/v1/signup")
                .post(body)
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(SignUpActivity.this, "Sign Up Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(SignUpActivity.this, "Sign Up Successful! Check email for verification.", Toast.LENGTH_LONG).show();
                        finish();
                    });
                } else {
                    final String errorMsg = response.body().string();
                    runOnUiThread(() -> Toast.makeText(SignUpActivity.this, "Sign Up Failed: " + errorMsg, Toast.LENGTH_LONG).show());
                }
            }
        });
    }
}