package com.example.caremitra;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private ViewSwitcher profileViewSwitcher;
    private ShimmerFrameLayout shimmerContainer;
    private Handler handler;

    private ImageView backButton, menuButton;
    private CircleImageView profilePic;
    private TextView profileName, profileAge, profileVerified,
            patientId, dob, bloodGroup, gender, versionInfo;
    private Switch themeSwitch;
    private Button downloadRecordsBtn, bookAppointmentBtn,
            editProfileBtn, medicalHistoryBtn, appointmentsBtn,
            prescriptionsBtn, settingsBtn, helpSupportBtn,
            feedbackBtn, logoutBtn;

    private SharedPreferences userPrefs;
    private NetworkHelper networkHelper;
    private static final int SKELETON_DELAY_MS = 2000;
    private static final int EDIT_PROFILE_REQUEST_CODE = 1;

    private static final String SUPABASE_URL = "https://uvxkiqrqnxgmsipkjhbe.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InV2eGtpcXJxbnhnbXNpcGtqaGJlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTYyMjc0MTYsImV4cCI6MjA3MTgwMzQxNn0.GEYSncagmsr8BkBPe8IGRSGke0llj4skHWBENnyyTJI";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        networkHelper = new NetworkHelper(this);
        handler = new Handler();
        userPrefs = getSharedPreferences("user", MODE_PRIVATE);

        profileViewSwitcher = findViewById(R.id.profileViewSwitcher);
        shimmerContainer = findViewById(R.id.shimmerContainer);

        backButton = findViewById(R.id.backButton);
        menuButton = findViewById(R.id.menuButton);

        profilePic = findViewById(R.id.profilePic);
        profileName = findViewById(R.id.profileName);
        profileAge = findViewById(R.id.profileAge);
        profileVerified = findViewById(R.id.profileVerified);
        patientId = findViewById(R.id.patientId);
        dob = findViewById(R.id.dob);
        bloodGroup = findViewById(R.id.bloodGroup);
        gender = findViewById(R.id.gender);
        versionInfo = findViewById(R.id.versionInfo);
        themeSwitch = findViewById(R.id.themeSwitch);

        downloadRecordsBtn = findViewById(R.id.downloadRecordsBtn);
        bookAppointmentBtn = findViewById(R.id.bookAppointmentBtn);
        editProfileBtn = findViewById(R.id.editProfileBtn);
        medicalHistoryBtn = findViewById(R.id.medicalHistoryBtn);
        appointmentsBtn = findViewById(R.id.appointmentsBtn);
        prescriptionsBtn = findViewById(R.id.prescriptionsBtn);
        settingsBtn = findViewById(R.id.settingsBtn);
        helpSupportBtn = findViewById(R.id.helpSupportBtn);
        feedbackBtn = findViewById(R.id.feedbackBtn);
        logoutBtn = findViewById(R.id.logoutBtn);

        setupClickListeners();
        loadProfileWithSkeleton();
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());
        menuButton.setOnClickListener(v -> loadProfileWithSkeleton());

        downloadRecordsBtn.setOnClickListener(v -> Toast.makeText(this, "My Records", Toast.LENGTH_SHORT).show());
        bookAppointmentBtn.setOnClickListener(v -> Toast.makeText(this, "Book Appointment", Toast.LENGTH_SHORT).show());
        medicalHistoryBtn.setOnClickListener(v -> Toast.makeText(this, "Medical History", Toast.LENGTH_SHORT).show());
        appointmentsBtn.setOnClickListener(v -> startActivity(new Intent(this, AppointmentsActivity.class)));
        prescriptionsBtn.setOnClickListener(v -> Toast.makeText(this, "Prescriptions", Toast.LENGTH_SHORT).show());
        settingsBtn.setOnClickListener(v -> Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show());
        helpSupportBtn.setOnClickListener(v -> Toast.makeText(this, "Help & Support", Toast.LENGTH_SHORT).show());
        feedbackBtn.setOnClickListener(v -> Toast.makeText(this, "Feedback", Toast.LENGTH_SHORT).show());
        logoutBtn.setOnClickListener(v -> {
            userPrefs.edit().clear().apply();
            finish();
        });

        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                Toast.makeText(this, isChecked ? "Light mode" : "Dark mode", Toast.LENGTH_SHORT).show()
        );

        editProfileBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            intent.putExtra("userId", userPrefs.getString("user_id", ""));
            intent.putExtra("accessToken", userPrefs.getString("supabase_access_token", ""));
            intent.putExtra("name", userPrefs.getString("user_name", ""));
            intent.putExtra("phone", userPrefs.getString("patient_id", ""));
            intent.putExtra("dob", userPrefs.getString("dob", ""));
            intent.putExtra("bloodGroup", userPrefs.getString("blood_group", ""));
            intent.putExtra("gender", userPrefs.getString("gender", ""));
            startActivityForResult(intent, EDIT_PROFILE_REQUEST_CODE);
        });
    }

    private void loadProfileWithSkeleton() {
        profileViewSwitcher.setDisplayedChild(0);
        shimmerContainer.startShimmer();

        handler.postDelayed(() -> {
            shimmerContainer.stopShimmer();
            if (networkHelper.isNetworkAvailable()) {
                fetchProfileData();
            } else {
                Toast.makeText(this, "No internet connection", Toast.LENGTH_LONG).show();
            }
        }, SKELETON_DELAY_MS);
    }

    private void fetchProfileData() {
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                String userId = userPrefs.getString("user_id", "");
                String accessToken = userPrefs.getString("supabase_access_token", "");

                if (userId.isEmpty() || accessToken.isEmpty()) {
                    runOnUiThread(() -> Toast.makeText(this, "User not logged in or token missing.", Toast.LENGTH_SHORT).show());
                    return;
                }

                URL url = new URL(SUPABASE_URL + "/rest/v1/users?id=eq." + userId);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("apikey", SUPABASE_ANON_KEY);
                connection.setRequestProperty("Authorization", "Bearer " + accessToken);
                connection.setRequestProperty("Content-Type", "application/json");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    String jsonResponse = response.toString();
                    Log.d("SupabaseResponse", jsonResponse);

                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<UserData>>() {}.getType();
                    List<UserData> users = gson.fromJson(jsonResponse, listType);

                    if (users != null && !users.isEmpty()) {
                        UserData user = users.get(0);
                        runOnUiThread(() -> updateProfileUI(user));
                    } else {
                        runOnUiThread(() -> Toast.makeText(this, "User data not found.", Toast.LENGTH_LONG).show());
                    }

                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Failed to fetch data: " + responseCode, Toast.LENGTH_LONG).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Network error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }

    private void updateProfileUI(UserData user) {
        if (user != null) {
            String calculatedAge = calculateAge(user.getDob());
            userPrefs.edit()
                    .putString("user_name", user.getName())
                    .putString("user_age", calculatedAge)
                    .putString("patient_id", user.getPhone())
                    .putString("dob", user.getDob())
                    .putString("blood_group", user.getBlood_group())
                    .putString("gender", user.getGender())
                    .putBoolean("is_verified", user.getIs_verified())
                    .apply();
        }
        populateProfileData();
        profileViewSwitcher.setDisplayedChild(1);
    }

    private void populateProfileData() {
        String name = userPrefs.getString("user_name", "N/A");
        String age = userPrefs.getString("user_age", "N/A");
        String pid = userPrefs.getString("patient_id", "N/A");
        String birth = userPrefs.getString("dob", "N/A");
        String bg = userPrefs.getString("blood_group", "N/A");
        String userGender = userPrefs.getString("gender", "N/A");
        boolean verf = userPrefs.getBoolean("is_verified", false);

        profileName.setText(name);
        profileAge.setText("Age: " + age + " years");
        patientId.setText("Patient ID: " + pid);
        dob.setText("DOB: " + birth);
        bloodGroup.setText("Blood Group: " + bg);
        gender.setText("Gender: " + userGender);
        profileVerified.setVisibility(verf ? View.VISIBLE : View.GONE);
        versionInfo.setText("Version 1.0");
    }

    private String calculateAge(String dobString) {
        if (dobString == null || dobString.isEmpty()) {
            return "N/A";
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date dob = sdf.parse(dobString);
            if (dob == null) {
                return "N/A";
            }
            Calendar dobCal = Calendar.getInstance();
            dobCal.setTime(dob);
            Calendar nowCal = Calendar.getInstance();
            int age = nowCal.get(Calendar.YEAR) - dobCal.get(Calendar.YEAR);
            if (nowCal.get(Calendar.DAY_OF_YEAR) < dobCal.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }
            return String.valueOf(age);
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e("AgeCalculation", "Failed to parse DOB: " + dobString);
            return "N/A";
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_PROFILE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                loadProfileWithSkeleton();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}