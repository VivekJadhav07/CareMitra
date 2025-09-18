package com.example.caremitra;

import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class HomeActivity extends AppCompatActivity implements HomeDataLoader.DataLoadListener {

    // Main content views
    private SwipeRefreshLayout swipeRefreshLayout;
    private ScrollView mainContentLayout;
    private LinearLayout noInternetLayout;
    private LinearLayout loadingLayout;

    // Content views
    private TextView welcomeText, networkStatus, sugarLevels, heartRate, oxygenLevel, bodyTemp;
    private LinearLayout alertsContainer;
    private LinearLayout menuOverview, menuAppointments, menuHistory, menuWallet;
    private Button buttonEmergency;
    private ImageView circularEmergencyButton;

    // No internet views
    private ImageView noInternetImage;
    private Button retryButton;


    //profile view
    private  ImageView profile;

    // Data loader
    private HomeDataLoader dataLoader;
    private NetworkHelper networkHelper;

    ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initializeViews();
        setupDataLoader();
        setupClickListeners();
        setupSwipeRefresh();

        // Initial state check
        checkConnectionAndLoad();
    }

    private void initializeViews() {
        // Main layouts
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        mainContentLayout = findViewById(R.id.mainContentLayout);
        noInternetLayout = findViewById(R.id.noInternetLayout);
        loadingLayout = findViewById(R.id.loadingLayout);

        // No internet views
        noInternetImage = findViewById(R.id.noInternetImage);
        retryButton = findViewById(R.id.retryButton);

        // Start animation on no internet image
        startNoInternetAnimation();

        // Data views
        welcomeText = findViewById(R.id.welcomeText);
        networkStatus = findViewById(R.id.networkStatus);
        sugarLevels = findViewById(R.id.sugarLevels);
        heartRate = findViewById(R.id.heartRate);
        oxygenLevel = findViewById(R.id.oxygenLevel);
        bodyTemp = findViewById(R.id.bodyTemp);
        alertsContainer = findViewById(R.id.alertsContainer);

        // Navigation
        menuOverview = findViewById(R.id.menuOverview);
        menuAppointments = findViewById(R.id.menuAppointments);
        menuHistory = findViewById(R.id.menuHistory);
        menuWallet = findViewById(R.id.menuWallet);

        // Emergency buttons
        buttonEmergency = findViewById(R.id.buttonEmergency);
        circularEmergencyButton = findViewById(R.id.circularEmergencyButton);


        //profile  view
        profile = findViewById(R.id.profileIcon);
    }

    private void startNoInternetAnimation() {
        if (noInternetImage != null && noInternetImage.getDrawable() instanceof AnimatedVectorDrawable) {
            ((AnimatedVectorDrawable) noInternetImage.getDrawable()).start();
        } else if (noInternetImage != null && noInternetImage.getDrawable() instanceof Animatable) {
            ((Animatable) noInternetImage.getDrawable()).start();
        }
    }

    private void setupDataLoader() {
        dataLoader = new HomeDataLoader(this, this);
        networkHelper = new NetworkHelper(this);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::checkConnectionAndLoad);

        swipeRefreshLayout.setColorSchemeColors(
                getResources().getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_green_light),
                getResources().getColor(android.R.color.holo_orange_light),
                getResources().getColor(android.R.color.holo_red_light)
        );
    }

    private void setupClickListeners() {
        retryButton.setOnClickListener(v -> checkConnectionAndLoad());

        // Bottom navigation menu clicks
        menuOverview.setOnClickListener(v ->
                Toast.makeText(this, "Overview selected", Toast.LENGTH_SHORT).show());

        menuAppointments.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AppointmentsActivity.class);
            startActivity(intent);
            Toast.makeText(this, "Appointments selected", Toast.LENGTH_SHORT).show();
        });

        menuHistory.setOnClickListener(v ->
                Toast.makeText(this, "History selected", Toast.LENGTH_SHORT).show());

        menuWallet.setOnClickListener(v ->
                Toast.makeText(this, "Wallet selected", Toast.LENGTH_SHORT).show());

        profile.setOnClickListener(v ->
                Toast.makeText(this, "Profile selected", Toast.LENGTH_SHORT).show());




        // Emergency buttons
        buttonEmergency.setOnClickListener(v -> makeEmergencyCall());
        circularEmergencyButton.setOnClickListener(v -> makeEmergencyCall());
    }



    private void checkConnectionAndLoad() {
        if (networkHelper.isNetworkAvailable()) {
            showLoadingState();
            dataLoader.loadHomeData();
        } else {
            showNoInternetState();
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void showLoadingState() {
        mainContentLayout.setVisibility(View.GONE);
        noInternetLayout.setVisibility(View.GONE);
        loadingLayout.setVisibility(View.VISIBLE);
    }

    private void showNoInternetState() {
        mainContentLayout.setVisibility(View.GONE);
        loadingLayout.setVisibility(View.GONE);
        noInternetLayout.setVisibility(View.VISIBLE);

        // Restart animation when showing no internet screen
        startNoInternetAnimation();
    }

    private void showMainContent() {
        noInternetLayout.setVisibility(View.GONE);
        loadingLayout.setVisibility(View.GONE);
        mainContentLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDataLoaded(HomeDataLoader.HomeData data) {
        showMainContent();

        // Update UI with new data
        welcomeText.setText(data.welcomeMessage);
        networkStatus.setText(data.networkStatus);
        networkStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));

        sugarLevels.setText(data.sugarLevel);
        heartRate.setText(data.heartRate);
        oxygenLevel.setText(data.oxygenLevel);
        bodyTemp.setText(data.bodyTemp);

        updateAlerts(data.alerts);

        swipeRefreshLayout.setRefreshing(false);
        Toast.makeText(this, "Data loaded successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDataFailed(String error) {
        if (networkHelper.isNetworkAvailable()) {
            showMainContent();
            networkStatus.setText("Error: " + error);
            networkStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else {
            showNoInternetState();
        }

        swipeRefreshLayout.setRefreshing(false);
        Toast.makeText(this, "Failed to load: " + error, Toast.LENGTH_SHORT).show();
    }

    private void updateAlerts(String[] alerts) {
        alertsContainer.removeAllViews();

        for (String alert : alerts) {
            TextView alertView = new TextView(this);
            alertView.setText(alert);
            alertView.setTextColor(getResources().getColor(android.R.color.darker_gray));
            alertView.setTextSize(14);
            alertView.setPadding(0, 0, 0, 16);
            alertsContainer.addView(alertView);
        }
    }

    private void makeEmergencyCall() {
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:911"));
        startActivity(callIntent);
    }
}
