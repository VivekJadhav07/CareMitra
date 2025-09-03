package com.example.caremitra;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class AppointmentsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointments);

        // Add your appointments screen logic here
        setupToolbar();
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Appointments");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
}
