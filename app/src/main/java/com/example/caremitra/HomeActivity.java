package com.example.caremitra;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements HomeDataLoader.DataLoadListener {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView homeRecyclerView;
    private LinearLayout loadingLayout, noInternetLayout;
    private Toolbar toolbar;
    private HomeAdapter homeAdapter;
    private HomeDataLoader dataLoader;
    private NetworkHelper networkHelper;
    private BottomNavigationView bottomNavigation;
    private FloatingActionButton fabEmergency;
    private Button retryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // All setup "modules" are called here in order
        initializeViews();
        setSupportActionBar(toolbar);
        setupDataLoader();
        setupSwipeRefresh();
        setupClickListeners();
        checkConnectionAndLoad();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        loadingLayout = findViewById(R.id.loadingLayout);
        noInternetLayout = findViewById(R.id.noInternetLayout);
        retryButton = findViewById(R.id.retryButton);
        homeRecyclerView = findViewById(R.id.homeRecyclerView);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        fabEmergency = findViewById(R.id.fabEmergency);
        homeRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        homeAdapter = new HomeAdapter(this, new ArrayList<>());
        homeRecyclerView.setAdapter(homeAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_settings) {
            Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.action_share) {
            Toast.makeText(this, "Share clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.action_logout) {
            Toast.makeText(this, "Logout clicked", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupClickListeners() {
        // Bottom Navigation Listener
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_overview) {
                return true;
            } else if (itemId == R.id.nav_appointments) {
                startActivity(new Intent(HomeActivity.this, AppointmentsActivity.class));
                return true;
            } else if (itemId == R.id.nav_wallet) {
                startActivity(new Intent(HomeActivity.this, WalletActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                return true;
            }
            return false;
        });

        // Emergency FAB Listener
        fabEmergency.setOnClickListener(v -> makeEmergencyCall());

        // Retry Button Listener
        retryButton.setOnClickListener(v -> checkConnectionAndLoad());
    }

    private void makeEmergencyCall() {
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        // Using 102 for ambulance in India
        callIntent.setData(Uri.parse("tel:102"));
        startActivity(callIntent);
    }

    private void setupDataLoader() {
        dataLoader = new HomeDataLoader(this);
        networkHelper = new NetworkHelper(this);
    }

    private void setupSwipeRefresh() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this::checkConnectionAndLoad);
        }
    }

    private void checkConnectionAndLoad() {
        showLoadingState();
        if (dataLoader != null) {
            dataLoader.loadHomeData(this);
        }
    }

    @Override
    public void onDataLoaded(HomeDataLoader.HomeData data) {
        showContentState();
        List<Object> items = new ArrayList<>();
        SharedPreferences userPrefs = getSharedPreferences("user", MODE_PRIVATE);
        String userName = userPrefs.getString("user_name", "User");

        // Build the list of items
        items.add(new HomeAdapter.WelcomeData(userName));

        // --- CHANGED: Add the Vitals Header first, then the grid ---
        items.add(new HomeAdapter.SectionHeaderData("Your Vitals Today"));
        items.add(new HomeAdapter.VitalsGridData(data.heartRate, data.oxygenLevel, data.bodyTemp));

        if (data.scheduledAppointments != null && !data.scheduledAppointments.isEmpty()) {
            items.add(new HomeAdapter.SectionHeaderData("Scheduled Appointments"));
            AppointmentDetails nextAppointment = data.scheduledAppointments.get(0);
            items.add(new HomeAdapter.AppointmentCardData(nextAppointment));
        }

        // ... (the rest of the item list is the same)
        int[] bannerImages = { R.drawable.banner1, R.drawable.banner2, R.drawable.banner3 };
        items.add(new HomeAdapter.BannerData(bannerImages));
        items.add(new HomeAdapter.HealthAnalyticsData(data.sugarLevel, R.drawable.sugargraph));
        items.add(new HomeAdapter.AlertsData(data.alerts));

        homeAdapter = new HomeAdapter(this, items);
        homeRecyclerView.setAdapter(homeAdapter);

        // --- CHANGED: Updated the click listener logic ---
        homeAdapter.setOnItemClickListener(itemData -> {
            if (itemData instanceof HomeAdapter.HealthAnalyticsData) {
                startActivity(new Intent(HomeActivity.this, AnalyticsDashboardActivity.class));
            }
            else if (itemData instanceof HomeAdapter.SectionHeaderData) {
                // Check the title of the header to open the correct screen
                String title = ((HomeAdapter.SectionHeaderData) itemData).getTitle();
                if ("Scheduled Appointments".equals(title)) {
                    startActivity(new Intent(HomeActivity.this, AppointmentsActivity.class));
                } else if ("Your Vitals Today".equals(title)) {
                    startActivity(new Intent(HomeActivity.this, VitalsHistoryActivity.class));
                }
            }
        });
    }
    @Override
    public void onDataFailed(String error) {
        showContentState();
        Toast.makeText(this, "Failed to load data: " + error, Toast.LENGTH_LONG).show();
    }

    private void showLoadingState() {
        if (loadingLayout != null) loadingLayout.setVisibility(View.VISIBLE);
        if (homeRecyclerView != null) homeRecyclerView.setVisibility(View.GONE);
        if (noInternetLayout != null) noInternetLayout.setVisibility(View.GONE);
    }

    private void showContentState() {
        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
        if (loadingLayout != null) loadingLayout.setVisibility(View.GONE);
        if (homeRecyclerView != null) homeRecyclerView.setVisibility(View.VISIBLE);
        if (noInternetLayout != null) noInternetLayout.setVisibility(View.GONE);
    }

    private void showNoInternetState() {
        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
        if (loadingLayout != null) loadingLayout.setVisibility(View.GONE);
        if (homeRecyclerView != null) homeRecyclerView.setVisibility(View.GONE);
        if (noInternetLayout != null) noInternetLayout.setVisibility(View.VISIBLE);
    }
}