package com.example.caremitra;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.io.IOException;
import java.util.Calendar;

public class EditProfileActivity extends AppCompatActivity {

    private EditText editName, editPhone, editDob, editBloodGroup;
    private Spinner editGender;
    private Button saveButton;

    private static final String SUPABASE_URL = "https://uvxkiqrqnxgmsipkjhbe.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InV2eGtpcXJxbnhnbXNpcGtqaGJlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTYyMjc0MTYsImV4cCI6MjA3MTgwMzQxNn0.GEYSncagmsr8BkBPe8IGRSGke0llj4skHWBENnyyTJI";

    private String userId, accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        editName = findViewById(R.id.editName);
        editPhone = findViewById(R.id.editPhone);
        editDob = findViewById(R.id.editDob);
        editBloodGroup = findViewById(R.id.editBloodGroup);
        editGender = findViewById(R.id.editGender);
        saveButton = findViewById(R.id.saveButton);

        // Get existing data passed from ProfileActivity
        Intent intent = getIntent();
        editName.setText(intent.getStringExtra("name"));
        editPhone.setText(intent.getStringExtra("phone"));
        editDob.setText(intent.getStringExtra("dob"));
        editBloodGroup.setText(intent.getStringExtra("bloodGroup"));
        userId = intent.getStringExtra("userId");
        accessToken = intent.getStringExtra("accessToken");

        // --- Date of Birth (DOB) Picker ---
        setupDatePicker();

        // --- Gender Spinner ---
        setupGenderSpinner(intent.getStringExtra("gender"));

        saveButton.setOnClickListener(v -> saveChanges());
    }

    private void setupDatePicker() {
        editDob.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, selectedYear, monthOfYear, dayOfMonth) -> {
                        String formattedDate = String.format("%d-%02d-%02d", selectedYear, monthOfYear + 1, dayOfMonth);
                        editDob.setText(formattedDate);
                    }, year, month, day);
            datePickerDialog.show();
        });
    }

    private void setupGenderSpinner(String currentGender) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.gender_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editGender.setAdapter(adapter);

        // Set the spinner's initial selection based on the current gender
        if (currentGender != null && !currentGender.isEmpty()) {
            int spinnerPosition = adapter.getPosition(currentGender);
            editGender.setSelection(spinnerPosition);
        }
    }

    private void saveChanges() {
        String name = editName.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String dob = editDob.getText().toString().trim();
        String bloodGroup = editBloodGroup.getText().toString().trim();
        String gender = editGender.getSelectedItem().toString(); // Get the selected gender

        if (name.isEmpty() || phone.isEmpty() || dob.isEmpty() || bloodGroup.isEmpty() || gender.isEmpty()) {
            runOnUiThread(() -> Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show());
            return;
        }

        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("name", name);
                jsonObject.put("phone", phone);
                jsonObject.put("dob", dob);
                jsonObject.put("blood_group", bloodGroup);
                jsonObject.put("gender", gender);
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error creating JSON request", Toast.LENGTH_SHORT).show());
                return;
            }

            RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json"));

            Request request = new Request.Builder()
                    .url(SUPABASE_URL + "/rest/v1/users?id=eq." + userId)
                    .header("apikey", SUPABASE_ANON_KEY)
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .header("Prefer", "return=minimal")
                    .patch(body)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(EditProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(EditProfileActivity.this, "Failed to update profile: " + response.code(), Toast.LENGTH_LONG).show());
                }
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(EditProfileActivity.this, "Network error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }
}