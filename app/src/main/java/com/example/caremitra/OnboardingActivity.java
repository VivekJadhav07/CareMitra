package com.example.caremitra;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPagerOnboarding);
        buttonNext = findViewById(R.id.buttonNext);
        buttonSkip = findViewById(R.id.buttonSkip);
        dotsIndicator = findViewById(R.id.dotsIndicator);

        List<OnboardingItem> items = new ArrayList<>();
        items.add(new OnboardingItem("Seamless Integration Across Providers",
                "Integrate seamlessly with hospitals and clinics, ensuring a cohesive healthcare experience.",
                R.drawable.logo_tbi));
        items.add(new OnboardingItem("2nd Page Title",
                "Description for second onboarding screen.",
                R.drawable.logo_tbi));
        items.add(new OnboardingItem("3rd Page Title",
                "Description for third onboarding screen.",
                R.drawable.logo_tbi));
        items.add(new OnboardingItem("4th Page Title",
                "Description for fourth onboarding screen.",
                R.drawable.logo_tbi));

        adapter = new OnboardingAdapter(items);
        viewPager.setAdapter(adapter);

        dotsIndicator.setViewPager2(viewPager);

        buttonSkip.setOnClickListener(v -> {
            goToLogin();
        });

        buttonNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() + 1 < adapter.getItemCount()) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            } else {
                goToLogin();
            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback(){
            @Override
            public void onPageSelected(int position){
                super.onPageSelected(position);
                if(position == adapter.getItemCount() - 1){
                    buttonNext.setText("Finish");
                } else {
                    buttonNext.setText("Next");
                }
            }
        });
    }

    private void goToLogin(){
        Intent intent = new Intent(OnboardingActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
