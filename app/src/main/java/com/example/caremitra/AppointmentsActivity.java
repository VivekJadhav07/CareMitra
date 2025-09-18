package com.example.caremitra;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.facebook.shimmer.ShimmerFrameLayout;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AppointmentsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AppointmentAdapter adapter;
    private TextView emptyView;
    private Button btnScheduled, btnCompleted, btnCancelled;
    private SwipeRefreshLayout swipeRefreshAppointments;
    private ShimmerFrameLayout shimmerContainer;
    private ApiService apiService;
    private String patientId;
    private String currentStatus = "scheduled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointments);

        initializeViews();
        setupSwipeRefresh();
        setupClickListeners();

        SharedPreferences prefs = getSharedPreferences("user", MODE_PRIVATE);
        patientId = prefs.getString("supabase_user_id", "");

        apiService = ApiClient.getClient(this).create(ApiService.class);

        // Set initial selected state
        btnScheduled.setSelected(true);
        loadAppointments("scheduled");
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.appointmentsRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppointmentAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        emptyView = findViewById(R.id.emptyView);
        swipeRefreshAppointments = findViewById(R.id.swipeRefreshAppointments);
        shimmerContainer = findViewById(R.id.shimmerContainer);

        btnScheduled = findViewById(R.id.btnScheduled);
        btnCompleted = findViewById(R.id.btnCompleted);
        btnCancelled = findViewById(R.id.btnCancelled);
    }

    private void setupSwipeRefresh() {
        swipeRefreshAppointments.setOnRefreshListener(() -> {
            loadAppointments(currentStatus);
        });

        swipeRefreshAppointments.setColorSchemeColors(
                getResources().getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_green_light),
                getResources().getColor(android.R.color.holo_orange_light),
                getResources().getColor(android.R.color.holo_red_light)
        );
    }

    private void setupClickListeners() {
        btnScheduled.setOnClickListener(v -> {
            clearButtonSelection();
            btnScheduled.setSelected(true);
            currentStatus = "scheduled";
            loadAppointments("scheduled");
        });

        btnCompleted.setOnClickListener(v -> {
            clearButtonSelection();
            btnCompleted.setSelected(true);
            currentStatus = "completed";
            loadAppointments("completed");
        });

        btnCancelled.setOnClickListener(v -> {
            clearButtonSelection();
            btnCancelled.setSelected(true);
            currentStatus = "cancelled";
            loadAppointments("cancelled");
        });
    }

    private void clearButtonSelection() {
        btnScheduled.setSelected(false);
        btnCompleted.setSelected(false);
        btnCancelled.setSelected(false);
    }

    private void loadAppointments(String status) {
        // Show shimmer loading if not from swipe refresh
        if (!swipeRefreshAppointments.isRefreshing()) {
            showShimmerLoading();
        }

        Call<List<AppointmentDetails>> call = apiService.getAppointments("eq." + status);

        call.enqueue(new Callback<List<AppointmentDetails>>() {
            @Override
            public void onResponse(Call<List<AppointmentDetails>> call, Response<List<AppointmentDetails>> response) {
                // Hide shimmer and refresh
                hideShimmerLoading();
                swipeRefreshAppointments.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isEmpty()) {
                        showEmptyState("No " + status + " appointments found.");
                    } else {
                        showAppointments(response.body());
                        if (swipeRefreshAppointments.isRefreshing()) {
                            Toast.makeText(AppointmentsActivity.this, "Appointments refreshed", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    showEmptyState("Failed to fetch appointments.");
                    Toast.makeText(AppointmentsActivity.this, "Failed to fetch appointments.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<AppointmentDetails>> call, Throwable t) {
                // Hide shimmer and refresh
                hideShimmerLoading();
                swipeRefreshAppointments.setRefreshing(false);

                showEmptyState("Error loading appointments.");
                Toast.makeText(AppointmentsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showShimmerLoading() {
        shimmerContainer.setVisibility(View.VISIBLE);
        shimmerContainer.startShimmer();
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
    }

    private void hideShimmerLoading() {
        shimmerContainer.setVisibility(View.GONE);
        shimmerContainer.stopShimmer();
    }

    private void showAppointments(List<AppointmentDetails> appointments) {
        hideShimmerLoading();
        adapter.updateData(appointments);
        recyclerView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
    }

    private void showEmptyState(String message) {
        hideShimmerLoading();
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
        emptyView.setText(message);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (shimmerContainer.getVisibility() == View.VISIBLE) {
            shimmerContainer.startShimmer();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        shimmerContainer.stopShimmer();
    }
}
