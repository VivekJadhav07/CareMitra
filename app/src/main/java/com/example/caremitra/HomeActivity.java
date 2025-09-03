package com.example.caremitra;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class HomeActivity extends AppCompatActivity {

    // UI Components
    private LinearLayout menuOverview, menuAppointments, menuHistory, menuWallet;
    private Button buttonEmergency;
    private ImageView profileIcon, logo;
    private TextView overviewTitle;
    private ImageView circularEmergencyButton;
    private LinearLayout alertItem1, alertItem2, alertItem3, alertItem4;
    private LinearLayout moreAlertsContainer, showMoreButton;
    private TextView showMoreText;
    private boolean isExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize all views
        initializeViews();

        // Set up navigation listeners
        setupNavigationListeners();

        // Set up other button listeners
        setupOtherListeners();

        // Set Overview as selected by default
        setSelectedNavItem(menuOverview);

        // Welcome Toast with Logo
        showWelcomeToast();
    }

    private void initializeViews() {
        // Bottom Navigation
        menuOverview = findViewById(R.id.menuOverview);
        menuAppointments = findViewById(R.id.menuAppointments);
        menuHistory = findViewById(R.id.menuHistory);
        menuWallet = findViewById(R.id.menuWallet);
        circularEmergencyButton = findViewById(R.id.circularEmergencyButton);
        alertItem1 = findViewById(R.id.alertItem1);
        alertItem2 = findViewById(R.id.alertItem2);
        alertItem3 = findViewById(R.id.alertItem3);
        alertItem4 = findViewById(R.id.alertItem4);
        moreAlertsContainer = findViewById(R.id.moreAlertsContainer);
        showMoreButton = findViewById(R.id.showMoreButton);
        showMoreText = findViewById(R.id.showMoreText);
        // New alerts in expandable section
        LinearLayout alertItem5 = findViewById(R.id.alertItem5);
        LinearLayout alertItem6 = findViewById(R.id.alertItem6);
        LinearLayout alertItem7 = findViewById(R.id.alertItem7);

        if (alertItem5 != null) {
            alertItem5.setOnClickListener(v -> showCustomToast("🩺 Blood Pressure Check"));
        }
        if (alertItem6 != null) {
            alertItem6.setOnClickListener(v -> showCustomToast("💊 Medication Schedule"));
        }
        if (alertItem7 != null) {
            alertItem7.setOnClickListener(v -> showCustomToast("🏃 Exercise Routine"));
        }



        // Other UI elements
        buttonEmergency = findViewById(R.id.buttonEmergency);
        profileIcon = findViewById(R.id.profileIcon);
        overviewTitle = findViewById(R.id.overviewTitle);
        logo = findViewById(R.id.logo);
    }

    private void setupNavigationListeners() {

        // Overview Navigation
        menuOverview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectedNavItem(menuOverview);
                showCustomToast("📊 Overview Selected");
            }
        });

        // Appointments Navigation
        menuAppointments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectedNavItem(menuAppointments);
                showCustomToast("📅 Opening Appointments");

                // Delay the intent to show toast first
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Intent appointmentIntent = new Intent(HomeActivity.this, AppointmentsActivity.class);
                            startActivity(appointmentIntent);
                        } catch (Exception e) {
                            showSimpleToast("Coming soon!");
                        }
                    }
                }, 600); // Reduced delay
            }
        });

        // History Navigation
        menuHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectedNavItem(menuHistory);
                showCustomToast("📋 Opening History");

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Intent historyIntent = new Intent(HomeActivity.this, HistoryActivity.class);
                            startActivity(historyIntent);
                        } catch (Exception e) {
                            showSimpleToast("Coming soon!");
                        }
                    }
                }, 600);
            }
        });

        // Wallet Navigation
        menuWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectedNavItem(menuWallet);
                showCustomToast("💰 Opening Wallet");

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Intent walletIntent = new Intent(HomeActivity.this, WalletActivity.class);
                            startActivity(walletIntent);
                        } catch (Exception e) {
                            showSimpleToast("Coming soon!");
                        }
                    }
                }, 600);
            }
        });
    }

    private void setupOtherListeners() {

        // Emergency Button
        if (circularEmergencyButton != null) {
            circularEmergencyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Animate button press
                    v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100)
                            .withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100);
                                }
                            });

                    showCustomToast("🚨 Emergency Services (108)");

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Intent emergencyIntent = new Intent(Intent.ACTION_DIAL);
                                emergencyIntent.setData(Uri.parse("tel:108"));
                                startActivity(emergencyIntent);
                            } catch (Exception e) {
                                showSimpleToast("Unable to open dialer");
                            }
                        }
                    }, 400);
                }
            });
        }

        // Alert Items Click Listeners
        setupAlertClickListeners();

        // Show More/Less Functionality
        if (showMoreButton != null) {
            showMoreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleMoreAlerts();
                }
            });
        }
    }

    private void setupAlertClickListeners() {
        if (alertItem1 != null) {
            alertItem1.setOnClickListener(v -> showCustomToast("📋 Lab Report Details"));
        }
        if (alertItem2 != null) {
            alertItem2.setOnClickListener(v -> showCustomToast("📅 Appointment Details"));
        }
        if (alertItem3 != null) {
            alertItem3.setOnClickListener(v -> showCustomToast("💊 Prescription Details"));
        }
        if (alertItem4 != null) {
            alertItem4.setOnClickListener(v -> showCustomToast("🏥 Health Checkup Details"));
        }
    }

    private void toggleMoreAlerts() {
        if (!isExpanded) {
            // Expand with slide down animation
            moreAlertsContainer.setVisibility(View.VISIBLE);
            moreAlertsContainer.setAlpha(0f);
            moreAlertsContainer.setTranslationY(-50f);

            moreAlertsContainer.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(300)
                    .start();

            showMoreText.setText("Show Less Alerts");
            showMoreText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_expand_less, 0);
            isExpanded = true;
            showCustomToast("📋 Showing more alerts");

        } else {
            // Collapse with slide up animation
            moreAlertsContainer.animate()
                    .alpha(0f)
                    .translationY(-50f)
                    .setDuration(300)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            moreAlertsContainer.setVisibility(View.GONE);
                        }
                    })
                    .start();

            showMoreText.setText("Show More Alerts");
            showMoreText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_expand_more, 0);
            isExpanded = false;
            showCustomToast("📋 Showing less alerts");
        }

    // Logo Click (Optional bonus feature)
        if (logo != null) {
            logo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showCustomToast("🏥 CareMitra Health App");
                }
            });
        }

        // Title Click (Optional)
        if (overviewTitle != null) {
            overviewTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showCustomToast("📊 Dashboard Overview");
                }
            });
        }
    }

    // Custom Toast with Logo - BOTTOM POSITION & SHORT DURATION
    private void showCustomToast(String message) {
        try {
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.custom_toast, null);

            ImageView toastLogo = layout.findViewById(R.id.toast_logo);
            TextView toastText = layout.findViewById(R.id.toast_text);

            toastLogo.setImageResource(R.drawable.logo);
            toastText.setText(message);

            Toast toast = new Toast(getApplicationContext());
            // BOTTOM POSITION with margin from bottom
            toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 150); // 150px from bottom
            toast.setDuration(Toast.LENGTH_SHORT); // SHORT DURATION
            toast.setView(layout);
            toast.show();

        } catch (Exception e) {
            // Fallback to simple toast if custom layout fails
            showSimpleToast(message);
        }
    }

    // Simple Toast (backup method) - BOTTOM POSITION & SHORT DURATION
    private void showSimpleToast(String message) {
        Toast toast = Toast.makeText(this, "🏥 " + message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 150); // 150px from bottom
        toast.show();
    }

    // Welcome Toast on App Start
    private void showWelcomeToast() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showCustomToast("🏥 Welcome to CareMitra!");
            }
        }, 1000); // Reduced from 1500ms to 1000ms
    }

    // Method to highlight selected navigation item
    private void setSelectedNavItem(LinearLayout selectedItem) {
        // Reset all navigation items to normal state
        resetAllNavItems();

        // Set selected item to active state (white background, blue text/icon)
        if (selectedItem != null) {
            try {
                selectedItem.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white));

                if (selectedItem.getChildCount() >= 2) {
                    ImageView icon = (ImageView) selectedItem.getChildAt(0);
                    TextView text = (TextView) selectedItem.getChildAt(1);

                    // Set active colors (blue for selected on white background)
                    icon.setColorFilter(ContextCompat.getColor(this, R.color.nav_selected));
                    text.setTextColor(ContextCompat.getColor(this, R.color.nav_selected));
                    text.setTypeface(null, android.graphics.Typeface.BOLD);
                }
            } catch (Exception e) {
                showSimpleToast("Navigation error");
            }
        }
    }

    // Reset all navigation items to normal state
    private void resetAllNavItems() {
        resetNavItem(menuOverview);
        resetNavItem(menuAppointments);
        resetNavItem(menuHistory);
        resetNavItem(menuWallet);
    }

    // Helper method to reset individual nav item
    private void resetNavItem(LinearLayout navItem) {
        if (navItem != null) {
            try {
                // Set transparent background (shows blue parent background)
                navItem.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));

                if (navItem.getChildCount() >= 2) {
                    ImageView icon = (ImageView) navItem.getChildAt(0);
                    TextView text = (TextView) navItem.getChildAt(1);

                    // Set normal colors (white for unselected on blue background)
                    icon.setColorFilter(ContextCompat.getColor(this, android.R.color.white));
                    text.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                    text.setTypeface(null, android.graphics.Typeface.NORMAL);
                }
            } catch (Exception e) {
                // Handle any color resource errors silently
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ensure Overview is selected when returning to this activity
        setSelectedNavItem(menuOverview);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // You can add any cleanup here if needed
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cleanup resources if needed
    }

    @Override
    public void onBackPressed() {
        showCustomToast("👋 Thanks for using CareMitra!");

        // Small delay before closing
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                HomeActivity.super.onBackPressed();
                finishAffinity(); // Closes all activities in the app
            }
        }, 800); // Reduced delay
    }

    // Optional: Handle low memory situations
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        // Handle low memory if needed
    }
}
