package com.example.caremitra;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;
import androidx.appcompat.app.AppCompatActivity;
import com.facebook.shimmer.ShimmerFrameLayout;
import de.hdodenhof.circleimageview.CircleImageView;


public class ProfileActivity extends AppCompatActivity {

    private ViewSwitcher profileViewSwitcher;
    private ShimmerFrameLayout shimmerContainer;
    private Handler handler;

    private ImageView backButton, menuButton;
    private CircleImageView profilePic;
    private TextView profileName, profileAge, profileVerified,
            patientId, dob, bloodGroup, versionInfo;
    private Switch themeSwitch;
    private Button downloadRecordsBtn, bookAppointmentBtn,
            editProfileBtn, medicalHistoryBtn, appointmentsBtn,
            prescriptionsBtn, settingsBtn, helpSupportBtn,
            feedbackBtn, logoutBtn;

    private SharedPreferences userPrefs;
    private NetworkHelper networkHelper;

    private static final int SKELETON_DELAY_MS = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize helpers
        networkHelper = new NetworkHelper(this);
        handler = new Handler();
        userPrefs = getSharedPreferences("user", MODE_PRIVATE);

        // Find views
        profileViewSwitcher = findViewById(R.id.profileViewSwitcher);
        shimmerContainer = findViewById(R.id.shimmerContainer);

        backButton = findViewById(R.id.backButton);
        menuButton = findViewById(R.id.menuButton);

        profilePic      = findViewById(R.id.profilePic);
        profileName     = findViewById(R.id.profileName);
        profileAge      = findViewById(R.id.profileAge);
        profileVerified = findViewById(R.id.profileVerified);
        patientId       = findViewById(R.id.patientId);
        dob             = findViewById(R.id.dob);
        bloodGroup      = findViewById(R.id.bloodGroup);
        versionInfo     = findViewById(R.id.versionInfo);
        themeSwitch     = findViewById(R.id.themeSwitch);

        downloadRecordsBtn   = findViewById(R.id.downloadRecordsBtn);
        bookAppointmentBtn   = findViewById(R.id.bookAppointmentBtn);
        editProfileBtn       = findViewById(R.id.editProfileBtn);
        medicalHistoryBtn    = findViewById(R.id.medicalHistoryBtn);
        appointmentsBtn      = findViewById(R.id.appointmentsBtn);
        prescriptionsBtn     = findViewById(R.id.prescriptionsBtn);
        settingsBtn          = findViewById(R.id.settingsBtn);
        helpSupportBtn       = findViewById(R.id.helpSupportBtn);
        feedbackBtn          = findViewById(R.id.feedbackBtn);
        logoutBtn            = findViewById(R.id.logoutBtn);

        setupClickListeners();
        loadProfileWithSkeleton();
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());
        menuButton.setOnClickListener(v -> loadProfileWithSkeleton());

        downloadRecordsBtn.setOnClickListener(v -> Toast.makeText(this, "My Records", Toast.LENGTH_SHORT).show());
        bookAppointmentBtn.setOnClickListener(v -> Toast.makeText(this, "Book Appointment", Toast.LENGTH_SHORT).show());
        editProfileBtn.setOnClickListener(v -> Toast.makeText(this, "Edit Profile", Toast.LENGTH_SHORT).show());
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
    }

    private void loadProfileWithSkeleton() {
        profileViewSwitcher.setDisplayedChild(0);
        shimmerContainer.startShimmer();

        handler.postDelayed(() -> {
            shimmerContainer.stopShimmer();
            if (networkHelper.isNetworkAvailable()) {
                populateProfileData();
                profileViewSwitcher.setDisplayedChild(1);
            } else {
                Toast.makeText(this, "No internet connection", Toast.LENGTH_LONG).show();
                // Keep skeleton visible or show an error overlay if desired
            }
        }, SKELETON_DELAY_MS);
    }

    private void populateProfileData() {
        // Fetch from SharedPreferences
        String name    = userPrefs.getString("user_name", "Dnyaneshwar Nikam");
        String age     = userPrefs.getString("user_age", "21");
        String pid     = userPrefs.getString("patient_id", "8669113136");
        String birth   = userPrefs.getString("dob", "02/02/2005");
        String bg      = userPrefs.getString("blood_group", "B+");
        boolean verf   = userPrefs.getBoolean("is_verified", true);

        profileName.setText(name);
        profileAge.setText("Age: " + age + " years");
        patientId.setText("Patient ID: " + pid);
        dob.setText("DOB: " + birth);
        bloodGroup.setText("Blood Group: " + bg);
        profileVerified.setVisibility(verf ? View.VISIBLE : View.GONE);
        versionInfo.setText("Version " + "1.0");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
