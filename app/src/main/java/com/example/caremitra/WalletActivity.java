package com.example.caremitra;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class WalletActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        setupToolbar();
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Wallet");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
}
