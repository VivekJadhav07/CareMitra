package com.example.caremitra;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private OnboardingAdapter adapter;
    private Button buttonNext, buttonSkip;
    private DotsIndicator dotsIndicator;

    // REMOVED: SharedPreferences declarations and logic

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        // WOW EFFECT: Make Activity Full Screen and Transparent Status Bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        super.onCreate(savedInstanceState);

        // REMOVED: SharedPreferences check that was redirecting to LoginActivity

        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPagerOnboarding);
        buttonNext = findViewById(R.id.buttonNext);
        buttonSkip = findViewById(R.id.buttonSkip);
        dotsIndicator = findViewById(R.id.dotsIndicator);

        // VISUAL CONTRAST: Set navigation text to White for contrast
        buttonNext.setTextColor(Color.WHITE);
        buttonSkip.setTextColor(Color.WHITE);

        List<OnboardingItem> items = new ArrayList<>();

        items.add(new OnboardingItem(
                "Seamless Care, One App",
                "Link hospitals, clinics, and labs in one place to view records, book visits, and manage health effortlessly.",
                R.drawable.seamless
        ));

        items.add(new OnboardingItem(
                "Smart Appointments",
                "Find doctors, check slots, and book or reschedule in seconds with reminders so nothing gets missed.",
                R.drawable.smart
        ));

        items.add(new OnboardingItem(
                "Your Records. Your Control.",
                "Access prescriptions, reports, and history securely anytime. Share with providers when needed.",
                R.drawable.records
        ));

        items.add(new OnboardingItem(
                "Private and Secure",
                "Bank‑grade security with verified profiles. Data stays encrypted and in your control.",
                R.drawable.blockchain
        ));

        adapter = new OnboardingAdapter(items);
        viewPager.setAdapter(adapter);
        dotsIndicator.setViewPager2(viewPager);

        // Update skip and next actions to simply goToLogin()
        buttonSkip.setOnClickListener(v -> goToLogin());

        buttonNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() + 1 < adapter.getItemCount()) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            } else {
                goToLogin(); // Direct navigation without setting SharedPreferences flag
            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                buttonNext.setText(position == adapter.getItemCount() - 1 ? "Start Now" : "Next");
            }
        });
    }

    // REMOVED: completeOnboarding() method

    private void goToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}