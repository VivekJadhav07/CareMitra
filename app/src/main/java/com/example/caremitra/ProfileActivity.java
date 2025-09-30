package com.example.caremitra;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

    private static final String TAG = "ProfileActivity";

    private ViewSwitcher profileViewSwitcher;
    private ShimmerFrameLayout shimmerContainer;

    // All original components kept
    private ImageView backButton, menuButton;
    private CircleImageView profilePic;
    private TextView profileName, profileAge, profileVerified,
            patientId, dob, bloodGroup, gender, versionInfo;
    private Switch themeSwitch;
    private Button downloadRecordsBtn, bookAppointmentBtn,
            editProfileBtn, settingsBtn, helpSupportBtn,
            feedbackBtn, logoutBtn;

    private SharedPreferences userPrefs;
    private NetworkHelper networkHelper;
    private static final int SKELETON_DELAY_MS = 2000;
    private static final int EDIT_PROFILE_REQUEST_CODE = 1;

    private static final String SUPABASE_URL = "https://uvxkiqrqnxgmsipkjhbe.supabase.co";
    // NOTE: BuildConfig.SUPABASE_API_KEY is used, assuming it's correctly defined.
    private static final String SUPABASE_ANON_KEY =  BuildConfig.SUPABASE_API_KEY;

    // Runnable to handle the transition after the skeleton delay
    private final Runnable stopShimmerAndLoadContent = () -> {
        shimmerContainer.stopShimmer();
        if (networkHelper.isNetworkAvailable()) {
            fetchProfileData();
        } else {
            Toast.makeText(this, "No internet connection. Showing cached data.", Toast.LENGTH_LONG).show();
            // Still show content so user can see cached data if any
            populateProfileData();
            profileViewSwitcher.setDisplayedChild(1);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        networkHelper = new NetworkHelper(this);
        userPrefs = getSharedPreferences("user", MODE_PRIVATE);

        // Bind XML views exactly as provided
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
        settingsBtn = findViewById(R.id.settingsBtn);
        helpSupportBtn = findViewById(R.id.helpSupportBtn);
        feedbackBtn = findViewById(R.id.feedbackBtn);
        logoutBtn = findViewById(R.id.logoutBtn);

        setupClickListeners();
        loadProfileWithSkeleton();
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());
        // Reloads the profile data when menu button is clicked (used for manual refresh)
        menuButton.setOnClickListener(v -> loadProfileWithSkeleton());

        downloadRecordsBtn.setOnClickListener(v -> Toast.makeText(this, "My Records (Feature coming soon)", Toast.LENGTH_SHORT).show());
        bookAppointmentBtn.setOnClickListener(v -> Toast.makeText(this, "Book Appointment (Launching soon)", Toast.LENGTH_SHORT).show());
        settingsBtn.setOnClickListener(v -> Toast.makeText(this, "Settings Page", Toast.LENGTH_SHORT).show());
        helpSupportBtn.setOnClickListener(v -> Toast.makeText(this, "Help & Support Page", Toast.LENGTH_SHORT).show());
        feedbackBtn.setOnClickListener(v -> Toast.makeText(this, "Feedback Form", Toast.LENGTH_SHORT).show());

        logoutBtn.setOnClickListener(v -> {
            // Clear user preferences and navigate to Login
            userPrefs.edit().clear().apply();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
            startActivity(intent);
            finish();
        });

        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                Toast.makeText(this, isChecked ? "Switched to Light Mode" : "Switched to Dark Mode", Toast.LENGTH_SHORT).show()
        );

        editProfileBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            // Pass current data to the EditProfileActivity
            intent.putExtra("userId", userPrefs.getString("user_id", ""));
            intent.putExtra("accessToken", userPrefs.getString("supabase_access_token", ""));
            intent.putExtra("name", userPrefs.getString("user_name", ""));
            // The Patient ID is stored as the phone number in the original code logic
            intent.putExtra("phone", userPrefs.getString("patient_id", ""));
            intent.putExtra("dob", userPrefs.getString("dob", ""));
            intent.putExtra("bloodGroup", userPrefs.getString("blood_group", ""));
            intent.putExtra("gender", userPrefs.getString("gender", ""));
            startActivityForResult(intent, EDIT_PROFILE_REQUEST_CODE);
        });
    }

    private void loadProfileWithSkeleton() {
        // Show skeleton (child 0) and start shimmer effect
        profileViewSwitcher.setDisplayedChild(0);
        shimmerContainer.startShimmer();

        // Use postDelayed on the main thread for the skeleton effect duration
        profileViewSwitcher.postDelayed(stopShimmerAndLoadContent, SKELETON_DELAY_MS);
    }

    private void fetchProfileData() {
        // Run network operation on a separate thread
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                String userId = userPrefs.getString("user_id", "");
                String accessToken = userPrefs.getString("supabase_access_token", "");

                if (userId.isEmpty() || accessToken.isEmpty()) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "User not logged in or token missing. Showing cached data.", Toast.LENGTH_SHORT).show();
                        populateProfileData();
                        profileViewSwitcher.setDisplayedChild(1);
                    });
                    return;
                }

                // Supabase query to fetch user data by ID
                URL url = new URL(SUPABASE_URL + "/rest/v1/users?id=eq." + userId);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("apikey", SUPABASE_ANON_KEY);
                connection.setRequestProperty("Authorization", "Bearer " + accessToken);
                connection.setRequestProperty("Content-Type", "application/json");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) response.append(line);
                    in.close();

                    String jsonResponse = response.toString();
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<UserData>>() {}.getType();
                    List<UserData> users = gson.fromJson(jsonResponse, listType);

                    if (users != null && !users.isEmpty()) {
                        UserData user = users.get(0);
                        runOnUiThread(() -> updateProfileUI(user));
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(this, "User data not found. Showing cached data.", Toast.LENGTH_LONG).show();
                            populateProfileData();
                            profileViewSwitcher.setDisplayedChild(1);
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        Log.e(TAG, "Failed to fetch data. Response Code: " + responseCode);
                        Toast.makeText(this, "Failed to fetch data: " + responseCode + ". Showing cached data.", Toast.LENGTH_LONG).show();
                        populateProfileData();
                        profileViewSwitcher.setDisplayedChild(1);
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Network error during data fetch: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Network error. Showing cached data.", Toast.LENGTH_LONG).show();
                    populateProfileData();
                    profileViewSwitcher.setDisplayedChild(1);
                });
            } finally {
                if (connection != null) connection.disconnect();
            }
        }).start();
    }

    private void updateProfileUI(UserData user) {
        if (user != null) {
            String calculatedAge = calculateAge(user.getDob());
            // Store fetched data in SharedPreferences
            userPrefs.edit()
                    .putString("user_name", user.getName())
                    .putString("user_age", calculatedAge)
                    .putString("patient_id", user.getPhone())
                    .putString("dob", user.getDob())
                    .putString("blood_group", user.getBlood_group())
                    .putString("gender", user.getGender())
                    .putBoolean("is_verified", user.getIs_verified())
                    .apply();

            Log.i(TAG, "Profile data updated successfully from network.");
        }
        populateProfileData();
        // Show content (child 1)
        profileViewSwitcher.setDisplayedChild(1);
    }

    private void populateProfileData() {
        // Retrieve data from SharedPreferences and update UI TextViews
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
        // Toggle visibility based on verification status
        profileVerified.setVisibility(verf ? View.VISIBLE : View.GONE);
        versionInfo.setText("Version 1.0");
    }

    private String calculateAge(String dobString) {
        if (dobString == null || dobString.isEmpty()) return "N/A";
        try {
            // Assuming DOB format is 'YYYY-MM-DD' as is common with SQL/API dates
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date dob = sdf.parse(dobString);
            if (dob == null) return "N/A";
            Calendar dobCal = Calendar.getInstance();
            dobCal.setTime(dob);
            Calendar nowCal = Calendar.getInstance();
            int age = nowCal.get(Calendar.YEAR) - dobCal.get(Calendar.YEAR);
            // Adjust age if birthday hasn't passed this year
            if (nowCal.get(Calendar.DAY_OF_YEAR) < dobCal.get(Calendar.DAY_OF_YEAR)) age--;
            return String.valueOf(age);
        } catch (ParseException e) {
            Log.e(TAG, "Failed to parse DOB: " + dobString, e);
            return "N/A";
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // If EditProfileActivity successfully updated data, reload the profile
        if (requestCode == EDIT_PROFILE_REQUEST_CODE && resultCode == RESULT_OK) {
            Toast.makeText(this, "Profile Updated. Reloading data...", Toast.LENGTH_SHORT).show();
            loadProfileWithSkeleton();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up any pending runnables to prevent memory leaks
        profileViewSwitcher.removeCallbacks(stopShimmerAndLoadContent);
        if (shimmerContainer != null) shimmerContainer.stopShimmer();
    }
}